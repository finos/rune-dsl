package com.regnosys.rosetta.ide.server.diagnostics;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.ide.server.ProjectManager;
import org.eclipse.xtext.parser.IEncodingProvider;
import org.eclipse.xtext.resource.IContainer;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.resource.impl.ResourceServiceProviderRegistryImpl;
import org.eclipse.xtext.service.OperationCanceledManager;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;
import org.eclipse.xtext.workspace.IProjectConfig;
import org.eclipse.xtext.workspace.ISourceFolder;
import org.eclipse.xtext.workspace.IWorkspaceConfig;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.multibindings.Multibinder;

/**
 * The failure paths of the sweep, which the language-server tests cannot reach: no real provider throws, and no
 * test can cancel a build mid-sweep. Everything here is wired by hand — a resource set of empty
 * {@code XtextResource}s, a project manager over it, and providers that fail on demand.
 *
 * <p>What the sweep must guarantee when something goes wrong is that the client keeps the answer it already
 * has: a resource is published either with every provider's contribution or not at all, and what the store
 * records is only ever what the client was actually sent, so the next build repairs whatever this one skipped.
 */
class WorkspaceDerivedDiagnosticsServiceTest {
	private static final URI URI_A = URI.createURI("file:/project/a.rosetta");
	private static final String BASE_ISSUE = "Unused import";
	private static final CancelIndicator CANCELLED = () -> true;

	private record Publish(URI uri, List<String> messages) {
	}

	private final List<Publish> published = new ArrayList<>();

	private DerivedDiagnosticsStore store;
	private WorkspaceDerivedDiagnosticsService service;
	private XtextResourceSet resourceSet;
	private Procedure2<? super URI, ? super Iterable<Issue>> buildAcceptor;
	private boolean publishFails;

	@Test
	void everyProvidersContributionIsPublishedAndRecorded() {
		start(new FirstProvider("Function 'F' is never used"), new SecondProvider("Type 'T' is never used"));

		build(CancelIndicator.NullImpl);

		Assertions.assertEquals(List.of(BASE_ISSUE, "Function 'F' is never used", "Type 'T' is never used"),
				lastPublished(URI_A));
		Assertions.assertEquals(2, store.derivedOf(URI_A).size());
	}

	/**
	 * A provider that cannot prepare its state contributes nothing for the rest of the build, but must not cost
	 * the other providers their contributions — nor escape into the build's write request, where it would take
	 * the build down with nothing in the log to say why the markers went.
	 */
	@Test
	void aProviderThatFailsToPrepareIsSkippedAndTheOthersStillPublish() {
		StubProvider broken = new FirstProvider("Function 'F' is never used");
		broken.failWhilePreparing = new IllegalStateException("cannot prepare");
		StubProvider working = new SecondProvider("Type 'T' is never used");
		start(broken, working);

		build(CancelIndicator.NullImpl);

		Assertions.assertEquals(List.of(BASE_ISSUE, "Type 'T' is never used"), lastPublished(URI_A));
	}

	/**
	 * A provider that throws for one resource takes that resource out of this build entirely: publishing the
	 * other providers' half would tell the client the missing markers had been cleared. The store keeps what
	 * the client has, so the next build publishes the complete answer.
	 */
	@Test
	void aProviderThatThrowsWhileComputingLeavesTheResourceUntilTheNextBuild() {
		StubProvider working = new FirstProvider("Function 'F' is never used");
		StubProvider broken = new SecondProvider("Type 'T' is never used");
		broken.failWhileComputing = new IllegalStateException("cannot compute");
		start(working, broken);

		build(CancelIndicator.NullImpl);

		Assertions.assertEquals(List.of(BASE_ISSUE), lastPublished(URI_A));
		Assertions.assertEquals(List.of(), store.derivedOf(URI_A));

		broken.failWhileComputing = null;
		build(CancelIndicator.NullImpl);

		Assertions.assertEquals(List.of(BASE_ISSUE, "Function 'F' is never used", "Type 'T' is never used"),
				lastPublished(URI_A));
	}

	/**
	 * The sweep runs in the build's write request, so it has to stop when the build is cancelled — a keystroke
	 * arriving while a large workspace is swept must not wait for it. Nothing is published for the resources it
	 * did not reach, and nothing is recorded for them either, so the next build finds them unequal and
	 * republishes.
	 */
	@Test
	void cancellationStopsTheSweepAndTheNextBuildRepairsIt() {
		start(new FirstProvider("Function 'F' is never used"));

		Throwable cancellation = Assertions.assertThrows(Throwable.class, () -> build(CANCELLED));

		Assertions.assertTrue(new OperationCanceledManager().isOperationCanceledException(cancellation),
				"Expected the sweep to report cancellation, but it threw " + cancellation);
		Assertions.assertEquals(List.of(BASE_ISSUE), lastPublished(URI_A));
		Assertions.assertEquals(List.of(), store.derivedOf(URI_A));

		build(CancelIndicator.NullImpl);

		Assertions.assertEquals(List.of(BASE_ISSUE, "Function 'F' is never used"), lastPublished(URI_A));
	}

	/**
	 * A provider is handed no cancel indicator, but the model it reads is: resolving a reference during a
	 * cancelled build raises cancellation from underneath it. That is not a broken provider, so it must
	 * propagate rather than be logged as one — logging it would report a failure on every cancelled build.
	 */
	@Test
	void aProviderCancellingIsNotLoggedAsAFailure() {
		StubProvider cancelling = new FirstProvider("Function 'F' is never used");
		cancelling.failWhileComputing = new CancellationException();
		start(cancelling);

		Assertions.assertThrows(CancellationException.class, () -> build(CancelIndicator.NullImpl));
		Assertions.assertEquals(List.of(), store.derivedOf(URI_A));

		cancelling.failWhileComputing = null;
		cancelling.failWhilePreparing = new CancellationException();

		Assertions.assertThrows(CancellationException.class, () -> build(CancelIndicator.NullImpl));
	}

	/**
	 * The store records what the client was told, so recording has to follow the publish rather than precede
	 * it. Recorded first, a publish that never arrives would leave the store claiming the client has markers it
	 * does not, and the next build would find nothing to correct.
	 */
	@Test
	void derivedDiagnosticsAreRecordedOnlyAfterTheyReachTheClient() {
		start(new FirstProvider("Function 'F' is never used"));
		publishFails = true;

		Assertions.assertThrows(IllegalStateException.class, () -> build(CancelIndicator.NullImpl));
		Assertions.assertEquals(List.of(), store.derivedOf(URI_A));

		publishFails = false;
		build(CancelIndicator.NullImpl);

		Assertions.assertEquals(List.of(BASE_ISSUE, "Function 'F' is never used"), lastPublished(URI_A));
	}

	/**
	 * Stands up the service over a workspace of one resource, with the given providers as the language's
	 * contribution, and publishes a build issue for it so that it is a resource the sweep will visit.
	 */
	private void start(IWorkspaceDerivedDiagnosticsProvider... providers) {
		Injector languageInjector = Guice.createInjector(binder -> {
			Multibinder<IWorkspaceDerivedDiagnosticsProvider> multibinder =
					Multibinder.newSetBinder(binder, IWorkspaceDerivedDiagnosticsProvider.class);
			for (IWorkspaceDerivedDiagnosticsProvider provider : providers) {
				multibinder.addBinding().toInstance(provider);
			}
		});
		Injector injector = Guice.createInjector(binder -> binder.bind(IResourceServiceProvider.Registry.class)
				.toInstance(new ResourceServiceProviderRegistryImpl()));
		service = injector.getInstance(WorkspaceDerivedDiagnosticsService.class);
		store = injector.getInstance(DerivedDiagnosticsStore.class);
		buildAcceptor = service.install((uri, issues) -> {
			if (publishFails) {
				throw new IllegalStateException("the client is gone");
			}
			List<String> messages = new ArrayList<>();
			issues.forEach(issue -> messages.add(issue.getMessage()));
			published.add(new Publish(uri, messages));
		});

		resourceSet = new XtextResourceSet();
		XtextResource resource = new XtextResource(URI_A);
		resource.setResourceServiceProvider(new LanguageStub(languageInjector));
		resourceSet.getResources().add(resource);
		// What a build publishes for the resource, and the reason the sweep considers it at all.
		buildAcceptor.apply(URI_A, List.of(issue(BASE_ISSUE)));
	}

	private void build(CancelIndicator cancelIndicator) {
		service.afterBuild(List.of(projectManager()), List.of(), cancelIndicator);
	}

	private ProjectManager projectManager() {
		return new ProjectManager() {
			@Override
			public XtextResourceSet getResourceSet() {
				return resourceSet;
			}

			@Override
			public IProjectConfig getProjectConfig() {
				return PROJECT_CONFIG;
			}
		};
	}

	/** The messages of the last thing published for a resource, which is what the client is showing. */
	private List<String> lastPublished(URI uri) {
		return published.reversed().stream()
				.filter(publish -> publish.uri().equals(uri))
				.findFirst()
				.map(Publish::messages)
				.orElseThrow(() -> new AssertionError("Nothing was published for " + uri));
	}

	private static Issue issue(String message) {
		Issue.IssueImpl issue = new Issue.IssueImpl();
		issue.setMessage(message);
		issue.setOffset(0);
		issue.setLength(1);
		return issue;
	}

	/**
	 * Contributes one diagnostic per resource, and fails on demand in either phase. The store keys a
	 * contribution by its provider's class, so two providers in one sweep have to be two classes.
	 */
	private abstract static class StubProvider implements IWorkspaceDerivedDiagnosticsProvider {
		private final String message;
		private RuntimeException failWhilePreparing;
		private RuntimeException failWhileComputing;

		StubProvider(String message) {
			this.message = message;
		}

		@Override
		public Pass beginSweep(ResourceSet resourceSet) {
			if (failWhilePreparing != null) {
				throw failWhilePreparing;
			}
			return this::diagnosticsFor;
		}

		private List<Issue> diagnosticsFor(Resource resource) {
			if (failWhileComputing != null) {
				throw failWhileComputing;
			}
			return List.of(issue(message));
		}
	}

	private static final class FirstProvider extends StubProvider {
		private FirstProvider(String message) {
			super(message);
		}
	}

	private static final class SecondProvider extends StubProvider {
		private SecondProvider(String message) {
			super(message);
		}
	}

	/** Answers the language injector the service resolves providers through, and nothing else. */
	private record LanguageStub(Injector injector) implements IResourceServiceProvider {
		@Override
		public <T> T get(Class<T> clazz) {
			if (clazz == Injector.class) {
				return clazz.cast(injector);
			}
			throw new UnsupportedOperationException(clazz.getName());
		}

		@Override
		public boolean canHandle(URI uri) {
			return true;
		}

		@Override
		public IResourceValidator getResourceValidator() {
			throw new UnsupportedOperationException();
		}

		@Override
		public IResourceDescription.Manager getResourceDescriptionManager() {
			throw new UnsupportedOperationException();
		}

		@Override
		public IContainer.Manager getContainerManager() {
			throw new UnsupportedOperationException();
		}

		@Override
		public IEncodingProvider getEncodingProvider() {
			throw new UnsupportedOperationException();
		}
	}

	private static final IProjectConfig PROJECT_CONFIG = new IProjectConfig() {
		@Override
		public String getName() {
			return "test";
		}

		@Override
		public URI getPath() {
			return URI.createURI("file:/project/");
		}

		@Override
		public Set<? extends ISourceFolder> getSourceFolders() {
			return Set.of();
		}

		@Override
		public ISourceFolder findSourceFolderContaining(URI member) {
			return null;
		}

		@Override
		public IWorkspaceConfig getWorkspaceConfig() {
			return null;
		}
	};
}
