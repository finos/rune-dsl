package com.regnosys.rosetta.ide.semantictokens;

import java.util.List;

import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.SemanticTokensLegend;
import org.eclipse.lsp4j.SemanticTokensParams;
import org.eclipse.lsp4j.SemanticTokensRangeParams;
import org.eclipse.xtext.ide.server.Document;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.CancelIndicator;

public interface ISemanticTokensService {
	SemanticTokensLegend getLegend();
	
	List<SemanticToken> computeSemanticTokens(Document document, XtextResource resource, SemanticTokensParams params, CancelIndicator cancelIndicator);

	List<SemanticToken> computeSemanticTokensInRange(Document document, XtextResource resource, SemanticTokensRangeParams params, CancelIndicator cancelIndicator);
	
	SemanticTokens toSemanticTokensResponse(List<SemanticToken> tokens);
}
