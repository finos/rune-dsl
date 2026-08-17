package com.regnosys.rosetta.ide.server;

import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.xtext.ide.server.BuildManager.Buildable;
import org.eclipse.xtext.ide.server.WorkspaceManager;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.Issue;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;

import com.regnosys.rosetta.ide.server.diagnostics.WorkspaceDerivedDiagnosticsService;

import jakarta.inject.Inject;

/**
 * Makes the language server robust against a null baseDir, and drives
 * {@link WorkspaceDerivedDiagnosticsService} — the diagnostics that depend on more of the workspace than the
 * resource they are reported on, and so have to be recomputed once the whole build has settled.
 * TODO: The null baseDir handling should be contributed back to Xtext.
 */
public class RosettaWorkspaceManager extends WorkspaceManager {
	@Inject
	private WorkspaceDerivedDiagnosticsService derivedDiagnosticsService;

	@Override
	public void initialize(URI baseDir, Procedure2<? super URI, ? super Iterable<Issue>> issueAcceptor,
			CancelIndicator cancelIndicator) {
		if (baseDir == null) {
			initialize(Collections.emptyList(), issueAcceptor, cancelIndicator);
		} else {
			super.initialize(baseDir, issueAcceptor, cancelIndicator);
		}
	}

	/**
	 * Both public {@code initialize} overloads end up here, so this is the one place the acceptor is
	 * intercepted — and it happens before the initial build, which {@code super} triggers.
	 */
	@Override
	public void initialize(List<WorkspaceFolder> workspaceFolders,
			Procedure2<? super URI, ? super Iterable<Issue>> issueAcceptor, CancelIndicator cancelIndicator) {
		super.initialize(workspaceFolders, derivedDiagnosticsService.install(issueAcceptor), cancelIndicator);
	}

	/**
	 * Every build funnels through here or through {@link #refreshWorkspaceConfig}, which are also the two
	 * places handed a cancel indicator — {@code afterBuild} is not, and the recompute has to be interruptible.
	 */
	@Override
	public Buildable didChangeFiles(List<URI> dirtyFiles, List<URI> deletedFiles) {
		Buildable buildable = super.didChangeFiles(dirtyFiles, deletedFiles);
		return cancelIndicator -> {
			List<IResourceDescription.Delta> deltas = buildable.build(cancelIndicator);
			derivedDiagnosticsService.afterBuild(getProjectManagers(), deltas, cancelIndicator);
			return deltas;
		};
	}

	/**
	 * The initial build of every newly configured project runs inside {@code super}, which keeps its deltas;
	 * they are all additions, so there is nothing for the sweep to forget.
	 */
	@Override
	protected void refreshWorkspaceConfig(CancelIndicator cancelIndicator) {
		super.refreshWorkspaceConfig(cancelIndicator);
		derivedDiagnosticsService.afterBuild(getProjectManagers(), List.of(), cancelIndicator);
	}

	/**
	 * Re-reads the workspace configuration and rebuilds all sources. Used when the Rune
	 * configuration file changes, so generation and validation re-run with the new configuration.
	 */
	public void rebuildAll(CancelIndicator cancelIndicator) {
		refreshWorkspaceConfig(cancelIndicator);
	}
}
