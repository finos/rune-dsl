package com.regnosys.rosetta.ide.server.diagnostics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.ISetup;
import org.eclipse.xtext.resource.FileExtensionProvider;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.IResourceServiceProvider.Registry;
import org.eclipse.xtext.resource.impl.ResourceServiceProviderRegistryImpl;
import org.eclipse.xtext.util.Modules2;
import org.eclipse.xtext.validation.Issue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;
import com.regnosys.rosetta.RosettaRuntimeModule;
import com.regnosys.rosetta.ide.RosettaIdeModule;
import com.regnosys.rosetta.ide.RosettaIdeSetup;
import com.regnosys.rosetta.ide.server.RosettaServerModule;
import com.regnosys.rosetta.ide.tests.AbstractRosettaLanguageServerValidationTest;

import jakarta.inject.Provider;

/**
 * The two-phase provider SPI exists so that whatever whole-workspace state a provider needs is prepared once
 * per sweep and then read for each resource. Preparing it per resource is what makes such a check quadratic —
 * which is the cost the interface's own javadoc says the two phases avoid.
 *
 * <p>{@code WorkspaceDerivedDiagnosticsService} keeps one prepared sweep per language, in a map keyed by the
 * {@code IResourceServiceProvider} the resource reports. That object is not a singleton: Xtext's
 * {@code DefaultResourceServiceProvider} carries no {@code @Singleton} and every {@code XtextResource} holds
 * its own, so an identity-keyed lookup misses for every resource and {@code beginSweep} is called once per
 * resource instead of once per sweep.
 *
 * <p>This test pins the guarantee rather than the mechanism: however the map is keyed, one build must not
 * prepare a sweep per resource.
 */
public class WorkspaceDerivedDiagnosticsSweepScalingTest extends AbstractRosettaLanguageServerValidationTest {

	/** Files created below, enough that "once per sweep" and "once per resource" cannot be confused. */
	private static final int FILE_COUNT = 30;

	/**
	 * Generous: one build may sweep more than one project, and the language server may build more than once
	 * while an edit settles. The failure this guards against is proportional to the workspace, not a small
	 * constant.
	 */
	private static final int MAX_EXPECTED_BEGIN_SWEEPS = 8;

	private static final AtomicInteger BEGIN_SWEEPS = new AtomicInteger();

	@Override
	protected Module getServerModule() {
		return RosettaServerModule.create(TestServerModule.class);
	}

	@Test
	void oneBuildPreparesASweepPerLanguageNotPerResource() {
		List<String> uris = new ArrayList<>();
		for (int i = 0; i < FILE_COUNT; i++) {
			uris.add(createModel("f" + i + ".rosetta", """
					namespace ns%d

					func F%d:
						output: r int (1..1)
						set r: %d
					""".formatted(i, i, i)));
		}
		assertNoIssues();

		BEGIN_SWEEPS.set(0);
		makeChange(uris.get(0), 4, 8, "0", "1");
		// Settles the build the edit triggered, so the count below covers it.
		getDiagnostics();

		Assertions.assertTrue(BEGIN_SWEEPS.get() <= MAX_EXPECTED_BEGIN_SWEEPS,
				"A single edit prepared " + BEGIN_SWEEPS.get() + " sweeps over a workspace of " + FILE_COUNT
						+ " files. Expected at most " + MAX_EXPECTED_BEGIN_SWEEPS
						+ ", i.e. one per language per build rather than one per resource.");
	}

	/** Counts how often the service asks a provider to prepare its whole-workspace state. */
	public static class CountingProvider implements IWorkspaceDerivedDiagnosticsProvider {
		@Override
		public Sweep beginSweep(ResourceSet resourceSet) {
			BEGIN_SWEEPS.incrementAndGet();
			return resource -> List.<Issue>of();
		}
	}

	static class TestIdeSetup extends RosettaIdeSetup {
		@Override
		public Injector createInjector() {
			RosettaIdeModule ideModule = new RosettaIdeModule() {
				@Override
				public void configureWorkspaceDerivedDiagnosticsProviders(Binder binder) {
					Multibinder.newSetBinder(binder, IWorkspaceDerivedDiagnosticsProvider.class)
							.addBinding().to(CountingProvider.class);
				}
			};
			return Guice.createInjector(Modules2.mixin(new RosettaRuntimeModule(), ideModule));
		}
	}

	static class TestServerModule extends RosettaServerModule {
		public Class<? extends Provider<IResourceServiceProvider.Registry>> providesIResourceServiceProvider$Registry() {
			return TestIResourceServiceProviderRegistryFactory.class;
		}
	}

	static class TestIResourceServiceProviderRegistryFactory
			implements Provider<IResourceServiceProvider.Registry>, javax.inject.Provider<IResourceServiceProvider.Registry> {
		private final Registry registry = loadRegistry();

		@Override
		public IResourceServiceProvider.Registry get() {
			return registry;
		}

		private Registry loadRegistry() {
			ResourceServiceProviderRegistryImpl registry = new ResourceServiceProviderRegistryImpl();
			ISetup setup = new TestIdeSetup();
			Injector injector = setup.createInjectorAndDoEMFRegistration();
			IResourceServiceProvider resourceServiceProvider = injector.getInstance(IResourceServiceProvider.class);
			FileExtensionProvider extensionProvider = injector.getInstance(FileExtensionProvider.class);
			String primaryFileExtension = extensionProvider.getPrimaryFileExtension();
			for (String extension : extensionProvider.getFileExtensions()) {
				if (registry.getExtensionToFactoryMap().containsKey(extension)) {
					if (primaryFileExtension.equals(extension)) {
						registry.getExtensionToFactoryMap().put(extension, resourceServiceProvider);
					}
				} else {
					registry.getExtensionToFactoryMap().put(extension, resourceServiceProvider);
				}
			}
			return registry;
		}
	}
}
