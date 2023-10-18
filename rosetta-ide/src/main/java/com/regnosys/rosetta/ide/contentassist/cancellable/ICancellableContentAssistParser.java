package com.regnosys.rosetta.ide.contentassist.cancellable;

import java.util.Collection;

import org.eclipse.xtext.ide.editor.contentassist.antlr.FollowElement;
import org.eclipse.xtext.util.CancelIndicator;

// TODO: contribute to Xtext
public interface ICancellableContentAssistParser {
	Collection<FollowElement> getFollowElements(String input, boolean strict, CancelIndicator cancelIndicator);

	Collection<FollowElement> getFollowElements(FollowElement element, CancelIndicator cancelIndicator);
}
