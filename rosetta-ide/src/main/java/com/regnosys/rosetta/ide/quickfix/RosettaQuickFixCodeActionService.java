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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.xtext.ide.editor.quickfix.DiagnosticResolution;
import org.eclipse.xtext.ide.editor.quickfix.IQuickFixProvider;
import org.eclipse.xtext.ide.server.codeActions.ICodeActionService2;

/*
 * TODO: contribute to Xtext.
 * This is a patch of org.eclipse.xtext.ide.server.codeActions.QuickFixCodeActionService.
 */
public class RosettaQuickFixCodeActionService implements ICodeActionService2 {

	@Inject
	private IQuickFixProvider quickfixes;

	@Override
	public List<Either<Command, CodeAction>> getCodeActions(Options options) {
		boolean handleQuickfixes = options.getCodeActionParams().getContext().getOnly() == null
				|| options.getCodeActionParams().getContext().getOnly().isEmpty()
				|| options.getCodeActionParams().getContext().getOnly().contains(CodeActionKind.QuickFix);

		if (!handleQuickfixes) {
			return Collections.emptyList();
		}

		List<Either<Command, CodeAction>> result = new ArrayList<>();
		for (Diagnostic diagnostic : options.getCodeActionParams().getContext().getDiagnostics()) {
			Options diagnosticOptions = createOptionsForSingleDiagnostic(options, diagnostic);
			List<DiagnosticResolution> resolutions = quickfixes.getResolutions(diagnosticOptions, diagnostic).stream()
					.sorted(Comparator.nullsLast(Comparator.comparing(DiagnosticResolution::getLabel)))
					.collect(Collectors.toList());
			for (DiagnosticResolution resolution : resolutions) {
				
				
				result.add(Either.forRight(createFix(resolution, diagnostic)));
			}
		}
		return result;
	}

	private CodeAction createFix(DiagnosticResolution resolution, Diagnostic diagnostic) {
		CodeAction codeAction = new CodeAction();
		codeAction.setDiagnostics(Collections.singletonList(diagnostic));
		codeAction.setTitle(resolution.getLabel());
		// This causes very slow perf as the fix is applied in memory before needed.
		// There needs to be another mechanism to do this. 
		codeAction.setEdit(resolution.apply());
		codeAction.setKind(CodeActionKind.QuickFix);

		return codeAction;
	}
	
	private Options createOptionsForSingleDiagnostic(Options base, Diagnostic diagnostic) {
		Options options = new Options();
		options.setCancelIndicator(base.getCancelIndicator());
		options.setDocument(base.getDocument());
		options.setLanguageServerAccess(base.getLanguageServerAccess());
		options.setResource(base.getResource());
		
		CodeActionParams baseParams = base.getCodeActionParams();
		CodeActionContext baseContext = baseParams.getContext();
		CodeActionContext context = new CodeActionContext(List.of(diagnostic), baseContext.getOnly());
		context.setTriggerKind(baseContext.getTriggerKind());
		CodeActionParams params = new CodeActionParams(baseParams.getTextDocument(), diagnostic.getRange(), context);
		
		options.setCodeActionParams(params);
		
		return options;
	}

}