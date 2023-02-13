package com.regnosys.rosetta.ide.inlayhints;

import org.eclipse.lsp4j.InlayHint;
import org.eclipse.lsp4j.InlayHintParams;
import org.eclipse.xtext.ide.server.Document;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.CancelIndicator;

import java.util.List;

/**
 * TODO: contribute to Xtext.
 *
 */
public interface IInlayHintsService {
	List<InlayHint> computeInlayHint(Document document, XtextResource resource, InlayHintParams params, CancelIndicator cancelIndicator);
}
