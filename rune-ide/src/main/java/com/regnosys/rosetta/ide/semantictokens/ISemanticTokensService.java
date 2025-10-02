/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.regnosys.rosetta.ide.semantictokens;

import java.util.List;

import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.SemanticTokensLegend;
import org.eclipse.lsp4j.SemanticTokensParams;
import org.eclipse.lsp4j.SemanticTokensRangeParams;
import org.eclipse.xtext.ide.server.Document;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.CancelIndicator;

/**
 * TODO: contribute to Xtext.
 *
 */
public interface ISemanticTokensService {
	SemanticTokensLegend getLegend();
	
	List<SemanticToken> computeSemanticTokens(Document document, XtextResource resource, SemanticTokensParams params, CancelIndicator cancelIndicator);

	List<SemanticToken> computeSemanticTokensInRange(Document document, XtextResource resource, SemanticTokensRangeParams params, CancelIndicator cancelIndicator);
	
	SemanticTokens toSemanticTokensResponse(List<SemanticToken> tokens);
}
