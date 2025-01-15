package com.regnosys.rosetta.ide.util;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler;
import org.eclipse.xtext.ide.server.codeActions.ICodeActionService2.Options;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.regnosys.rosetta.rosetta.Import;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.validation.RosettaIssueCodes;

public class CodeActionUtils {
	@Inject
	private RangeUtils rangeUtils;
	
	public Options createOptionsForSingleDiagnostic(Options base, Diagnostic diagnostic) {
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
	
	public CodeActionParams getCodeActionParams(CodeAction codeAction) {
        Object data = codeAction.getData();
        CodeActionParams codeActionParams = null;
        if (data instanceof CodeActionParams) {
        	codeActionParams = (CodeActionParams) data;
        }
        if (data instanceof JsonObject) {
            Gson gson = new MessageJsonHandler(Map.of()).getGson();
            codeActionParams = gson.fromJson(((JsonObject) data), CodeActionParams.class);
        }       
        return codeActionParams;
    }
	
	public CodeAction createUnresolvedFix(String resolutionLabel, CodeActionParams codeActionParams,
			Diagnostic diagnostic) {
		CodeAction codeAction = new CodeAction();
		codeAction.setDiagnostics(Collections.singletonList(diagnostic));
		codeAction.setTitle(resolutionLabel);
		codeAction.setData(codeActionParams);
		codeAction.setKind(CodeActionKind.QuickFix);

		return codeAction;
	}
	
	public CodeAction createUnresolvedCodeAction(String resolutionLabel, CodeActionParams codeActionParams,
			Diagnostic diagnostic, String codeActionKind) {
		CodeAction codeAction = createUnresolvedFix(resolutionLabel, codeActionParams, diagnostic);
		codeAction.setKind(codeActionKind);

		return codeAction;
	}
	
	public Diagnostic createSortImportsDiagnostic(RosettaModel model) {
		// Create a diagnostic for unsorted imports
		Diagnostic diagnostic = new Diagnostic();
		diagnostic.setCode(RosettaIssueCodes.UNSORTED_IMPORTS);
		diagnostic.setMessage("Imports are not sorted.");
		diagnostic.setSeverity(DiagnosticSeverity.Warning);

		Range range = getImportsRange(model.getImports());
		diagnostic.setRange(range);

		return diagnostic;
	}
	
	public Range getImportsRange(List<Import> imports) {
		Position importsStart = rangeUtils.getRange(imports.get(0)).getStart();
		Position importsEnd = rangeUtils.getRange(imports.get(imports.size() - 1)).getEnd();
		return new Range(importsStart, importsEnd);
	}
}
