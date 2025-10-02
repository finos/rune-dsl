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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.SemanticTokensLegend;
import org.eclipse.lsp4j.SemanticTokensParams;
import org.eclipse.lsp4j.SemanticTokensRangeParams;
import org.eclipse.xtext.ide.server.Document;
import org.eclipse.xtext.ide.server.DocumentExtensions;
import org.eclipse.xtext.resource.ILocationInFileProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.ITextRegion;
import com.regnosys.rosetta.ide.util.AbstractLanguageServerService;

/**
 * TODO: contribute to Xtext.
 *
 */
public class AbstractSemanticTokensService extends AbstractLanguageServerService<SemanticToken> implements ISemanticTokensService {	
	@Inject
	private DocumentExtensions documentExtensions;
	
	@Inject
	private ILocationInFileProvider locationInFileProvider;
	
	private final List<ISemanticTokenType> tokenTypes;
	private final List<ISemanticTokenModifier> tokenModifiers;
	
	@Inject
	public AbstractSemanticTokensService(ISemanticTokenTypesProvider tokenTypesProvider, ISemanticTokenModifiersProvider tokenModifiersProvider) {
		super(SemanticToken.class, MarkSemanticToken.class);
		this.tokenTypes = tokenTypesProvider.getSemanticTokenTypes();
		this.tokenModifiers = tokenModifiersProvider.getSemanticTokenModifiers();
	}
	
	@Override
	public SemanticTokensLegend getLegend() {
		return new SemanticTokensLegend(
					this.tokenTypes.stream().map(ISemanticTokenType::getValue).collect(Collectors.toList()),
					this.tokenModifiers.stream().map(ISemanticTokenModifier::getValue).collect(Collectors.toList())
				);
	}
	
	@Override
	public SemanticTokens toSemanticTokensResponse(List<SemanticToken> tokens) {
		List<Integer> data = new ArrayList<Integer>(tokens.size()*5);
		
		tokens.sort(null);
		
		int lastLine = 0;
		int lastStartChar = 0;
		for (int i=0; i < tokens.size(); i++) {
			SemanticToken token = tokens.get(i);
			int deltaLine = token.getLine() - lastLine;
			int deltaStartChar;
			if (deltaLine == 0) {
				deltaStartChar = token.getStartChar() - lastStartChar;
			} else {
				deltaStartChar = token.getStartChar();
			}			
			
			data.add(deltaLine);
			data.add(deltaStartChar);
			data.add(token.getLength());
			data.add(getTokenTypeRepr(token.getTokenType()));
			data.add(getTokenModifiersRepr(token.getTokenModifiers()));
			
			lastLine = token.getLine();
			lastStartChar = token.getStartChar();
		}
		
		return new SemanticTokens(data);
	}
	
	private int getTokenTypeRepr(final ISemanticTokenType tokenType) {
		int repr = this.tokenTypes.indexOf(tokenType);
		if (repr == -1) {
			throw new Error(String.format("Token type `%s` not found. Did you forget to bind it in the `%s`?", tokenType.getValue(), ISemanticTokenTypesProvider.class.getSimpleName()));
		}
		return repr;
	}

	private int getTokenModifiersRepr(final ISemanticTokenModifier[] tokenModifiers) {
		int bitmask = 0;
		for (ISemanticTokenModifier mod : tokenModifiers) {
			int repr = this.tokenModifiers.indexOf(mod);
			if (repr == -1) {
				throw new Error(String.format("Token modifier `%s` not found. Did you forget to bind it in the `%s`?", mod.getValue(), ISemanticTokenModifiersProvider.class.getSimpleName()));
			}
			bitmask |= 1 << repr;
		}
		return bitmask;
	}
	
	@Override
	public List<SemanticToken> computeSemanticTokens(Document document, XtextResource resource, SemanticTokensParams params, CancelIndicator cancelIndicator) {
		return computeResult(document, resource, cancelIndicator);
	}
	
	@Override
	public List<SemanticToken> computeSemanticTokensInRange(Document document, XtextResource resource, SemanticTokensRangeParams params, CancelIndicator cancelIndicator) {
		return computeResult(document, resource, params.getRange(), cancelIndicator);
	}

	protected SemanticToken createSemanticToken(EObject tokenObject, ISemanticTokenType tokenType, ISemanticTokenModifier... tokenModifiers) {
		ITextRegion region = locationInFileProvider.getFullTextRegion(tokenObject);
		Range range = documentExtensions.newRange(tokenObject.eResource(), region);
		return new SemanticToken(range.getStart().getLine(), range.getStart().getCharacter(), region.getLength(), tokenType, tokenModifiers);
	}
	
	protected SemanticToken createSemanticToken(EObject tokenObject, EStructuralFeature feature, ISemanticTokenType tokenType, ISemanticTokenModifier... tokenModifiers) {
		return createSemanticToken(tokenObject, feature, -1, tokenType, tokenModifiers);
	}
	
	protected SemanticToken createSemanticToken(EObject tokenObject, EStructuralFeature feature, int featureIndex, ISemanticTokenType tokenType, ISemanticTokenModifier... tokenModifiers) {
		ITextRegion region = locationInFileProvider.getFullTextRegion(tokenObject, feature, featureIndex);
		Range range = documentExtensions.newRange(tokenObject.eResource(), region);
		return new SemanticToken(range.getStart().getLine(), range.getStart().getCharacter(), region.getLength(), tokenType, tokenModifiers);
	}
}
