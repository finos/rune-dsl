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

package com.regnosys.rosetta.ide.quickfix;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.xtext.ide.editor.quickfix.DiagnosticResolution;
import org.eclipse.xtext.ide.editor.quickfix.IQuickFixProvider;
import org.eclipse.xtext.ide.server.ILanguageServerAccess;
import org.eclipse.xtext.ide.server.codeActions.ICodeActionService2;

import com.regnosys.rosetta.ide.util.CodeActionUtils;

public class RosettaCodeActionService implements ICodeActionService2 {

	@Inject
	private IQuickFixProvider quickfixes;
	@Inject 
	private CodeActionUtils codeActionUtils;
	@Inject
	private ICodeActionProvider codeActionProvider;

	@Override
	public List<Either<Command, CodeAction>> getCodeActions(Options options) {
		List<Either<Command, CodeAction>> result = new ArrayList<>();
		
		// By default, Xtext only reads the resource if there is a diagnostic relevant for the quick fix provider.
		// However, we always require the resource, since we always check contents to, e.g., check whether the imports are sorted or not.
		options.getLanguageServerAccess()
			.doSyncRead(options.getURI(), (ILanguageServerAccess.Context context) -> {
				options.setDocument(context.getDocument());
				options.setResource(context.getResource());
				return null;
			});
		
		// Handle code actions
		List<Either<Command, CodeAction>> codeActions = codeActionProvider.getCodeActions(options).stream()
				.map(action -> Either.<Command, CodeAction>forRight(action))
				.collect(Collectors.toList());
		result.addAll(codeActions);
		
		boolean handleQuickfixes = options.getCodeActionParams().getContext().getOnly() == null
				|| options.getCodeActionParams().getContext().getOnly().isEmpty()
				|| options.getCodeActionParams().getContext().getOnly().contains(CodeActionKind.QuickFix);
		
		if (handleQuickfixes) {
			// Handle quick fixes
			List<Diagnostic> diagnostics = options.getCodeActionParams().getContext().getDiagnostics();

			for (Diagnostic diagnostic : diagnostics) {
				Options diagnosticOptions = codeActionUtils.createOptionsForSingleDiagnostic(options, diagnostic);
				List<DiagnosticResolution> resolutions = quickfixes.getResolutions(diagnosticOptions, diagnostic)
						.stream().sorted(Comparator.nullsLast(Comparator.comparing(DiagnosticResolution::getLabel)))
						.collect(Collectors.toList());
				for (DiagnosticResolution resolution : resolutions) {
					result.add(Either
							.forRight(codeActionUtils.createUnresolvedFix(resolution.getLabel(), options.getCodeActionParams(), diagnostic)));
				}
			}
		}

		return result;
	}

}