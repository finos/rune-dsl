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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.xtext.ide.editor.quickfix.DiagnosticResolution;
import org.eclipse.xtext.ide.editor.quickfix.IQuickFixProvider;
import org.eclipse.xtext.ide.server.codeActions.ICodeActionService2;
import org.eclipse.xtext.resource.XtextResource;

import com.regnosys.rosetta.ide.util.RangeUtils;
import com.regnosys.rosetta.rosetta.Import;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.validation.ImportManagementService;

/*
 * TODO: contribute to Xtext.
 * This is a patch of org.eclipse.xtext.ide.server.codeActions.QuickFixCodeActionService.
 */
public class RosettaQuickFixCodeActionService implements ICodeActionService2 {

	@Inject
	private IQuickFixProvider quickfixes;	
	@Inject
	private RangeUtils rangeUtils;
	@Inject 
	private ImportManagementService importManagementService;

	@Override
	public List<Either<Command, CodeAction>> getCodeActions(Options options) {
		boolean handleQuickfixes = options.getCodeActionParams().getContext().getOnly() == null
				|| options.getCodeActionParams().getContext().getOnly().isEmpty()
				|| options.getCodeActionParams().getContext().getOnly().contains(CodeActionKind.QuickFix);

		List<Either<Command, CodeAction>> result = new ArrayList<>();
		if (handleQuickfixes) {
			for (Diagnostic diagnostic : options.getCodeActionParams().getContext().getDiagnostics()) {
				Options diagnosticOptions = createOptionsForSingleDiagnostic(options, diagnostic);
				List<DiagnosticResolution> resolutions = quickfixes.getResolutions(diagnosticOptions, diagnostic).stream()
						.sorted(Comparator.nullsLast(Comparator.comparing(DiagnosticResolution::getLabel)))
						.collect(Collectors.toList());
				for (DiagnosticResolution resolution : resolutions) {
					result.add(Either.forRight(createFix(resolution, diagnostic, diagnosticOptions)));
				}
			}
		}
		
		// Handle Sorting Imports
//        if (shouldSortImports(options)) {
//            CodeAction sortImportsAction = createSortImportsAction(options);
//            result.add(Either.forRight(sortImportsAction));
//        }
        
		return result;
	}
	
	private boolean shouldSortImports(Options options) {
        // for now: should sort if it has imports
		RosettaModel model = (RosettaModel) options.getResource().getContents().get(0);
		return (model.getImports() != null & model.getImports().size() > 0);
    }
	
	private CodeAction createSortImportsAction(Options options) {
        // Create the "Sort Imports." CodeAction
        CodeAction action = new CodeAction("Sort Imports.");
        
        XtextResource resource = options.getResource();
        
        // Apply the sorting logic and generate TextEdits
        List<TextEdit> textEdits = sortImports(resource);

        // Add the edits to the CodeAction
        action.setEdit(new WorkspaceEdit(Collections.singletonMap(resource.getURI().toString(), textEdits)));
        
        action.setKind(CodeActionKind.SourceOrganizeImports);

        return action;
    }

    private List<TextEdit> sortImports(XtextResource resource) {
        List<TextEdit> edits = new ArrayList<>();

        EObject resourceContent = resource.getContents().get(0);
        if (resourceContent instanceof RosettaModel) {
            RosettaModel model = (RosettaModel) resourceContent;
            List<Import> imports = model.getImports();
            
            importManagementService.cleanupImports(model);
			String sortedImportsText = importManagementService.toString(imports);

			// find the range of all imports to replace
            Position importsStart = rangeUtils.getRange(imports.get(0)).getStart();
            Position importsEnd = rangeUtils.getRange(imports.get(imports.size() - 1)).getEnd();
            Range importsRange = new Range(importsStart, importsEnd);
            edits.add(new TextEdit(importsRange, sortedImportsText.toString().strip()));
        }
        return edits;
    }

	private CodeAction createFix(DiagnosticResolution resolution, Diagnostic diagnostic, Options diagnosticOptions) {
		CodeAction codeAction = new CodeAction();
		codeAction.setDiagnostics(Collections.singletonList(diagnostic));
		codeAction.setTitle(resolution.getLabel());
		codeAction.setKind(CodeActionKind.QuickFix);
		codeAction.setData(diagnosticOptions);
		
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