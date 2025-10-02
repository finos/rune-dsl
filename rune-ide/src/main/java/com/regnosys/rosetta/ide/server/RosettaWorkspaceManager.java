package com.regnosys.rosetta.ide.server;

import java.util.Collections;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.ide.server.WorkspaceManager;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.Issue;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;

/**
 * Makes the language server robust against a null baseDir.
 * TODO: This should be contributed back to Xtext.
 */
public class RosettaWorkspaceManager extends WorkspaceManager {
	@Override
	public void initialize(URI baseDir, Procedure2<? super URI, ? super Iterable<Issue>> issueAcceptor,
			CancelIndicator cancelIndicator) {
		if (baseDir == null) {
			initialize(Collections.emptyList(), issueAcceptor, cancelIndicator);
		} else {
			super.initialize(baseDir, issueAcceptor, cancelIndicator);
		}
	}
}
