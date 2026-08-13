package com.regnosys.rosetta.ide.validation;

import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticTag;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.xtext.ide.server.IMultiRootWorkspaceConfigFactory;
import org.eclipse.xtext.ide.server.UriExtensions;
import org.eclipse.xtext.workspace.FileProjectConfig;
import org.eclipse.xtext.workspace.IWorkspaceConfig;
import org.eclipse.xtext.workspace.WorkspaceConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.inject.Module;
import com.regnosys.rosetta.ide.server.RosettaServerModule;
import com.regnosys.rosetta.ide.tests.AbstractRosettaLanguageServerValidationTest;

import jakarta.inject.Inject;

/**
 * Pins the assumption behind {@code WorkspaceDerivedDiagnosticsService}'s single-project-many-source-folders
 * javadoc: the sweep is keyed by project resource set, not by source folder, so a declaration in one source
 * folder of a project that is referenced only from another source folder of the <em>same</em> project must not
 * be flagged unused.
 *
 * <p>The production factory that turns every workspace folder into one project's source folders
 * ({@code SingleProjectWorkspaceConfigFactory}) lives outside this repository, in {@code bsp-server}. Rather
 * than depend on that module, this test binds a small equivalent here: one {@link FileProjectConfig} rooted at
 * the test workspace root, with an explicit source folder per directory. The test harness supplies a single
 * workspace folder, so Xtext's own default would produce one project spanning both directories too and this
 * test would pass without the binding — it is here to state the deployment shape the assertion is about, not
 * to be the thing that makes it hold.
 */
public class UnusedElementMultiSourceFolderTest extends AbstractRosettaLanguageServerValidationTest {

	@Override
	protected Module getServerModule() {
		return RosettaServerModule.create(TestServerModule.class);
	}

	@Test
	void declarationInOneSourceFolderReferencedFromAnotherIsNotFlagged() {
		String declURI = createModel("folderA/decl.rosetta", """
				namespace decl

				func F:
					output: r int (1..1)
					set r: 42
				""");
		createModel("folderB/caller.rosetta", """
				namespace caller

				func Caller:
					output: r int (1..1)
					set r: decl.F()
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of(), unusedMarkerMessages(declURI),
				"Expected no unused marker: 'F' is called from a different source folder of the same project");
	}

	private List<String> unusedMarkerMessages(String uri) {
		List<Diagnostic> diagnostics = getDiagnostics().get(uri);
		if (diagnostics == null) {
			return List.of();
		}
		return diagnostics.stream()
				.filter(d -> d.getTags() != null && d.getTags().contains(DiagnosticTag.Unnecessary))
				.map(Diagnostic::getMessage)
				.toList();
	}

	public static class TestServerModule extends RosettaServerModule {
		public Class<? extends IMultiRootWorkspaceConfigFactory> bindIMultiRootWorkspaceConfigFactory() {
			return TwoSourceFoldersFactory.class;
		}
	}

	static class TwoSourceFoldersFactory implements IMultiRootWorkspaceConfigFactory {
		@Inject
		private UriExtensions uriExtensions;

		@Override
		public IWorkspaceConfig getWorkspaceConfig(List<WorkspaceFolder> workspaceFolders) {
			WorkspaceConfig workspaceConfig = new WorkspaceConfig();
			URI root = uriExtensions.toUri(workspaceFolders.get(0).getUri());
			FileProjectConfig project = new FileProjectConfig(root, workspaceConfig);
			project.addSourceFolder("folderA");
			project.addSourceFolder("folderB");
			// Builtins (basictypes/annotations) are written under "model" by the test harness.
			project.addSourceFolder("model");
			workspaceConfig.addProject(project);
			return workspaceConfig;
		}
	}
}
