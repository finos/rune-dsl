package com.regnosys.rosetta.ide.quickfix;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.xtext.ide.editor.quickfix.DiagnosticResolution;
import org.eclipse.xtext.ide.editor.quickfix.IQuickFixProvider;
import org.eclipse.xtext.ide.server.Document;
import org.eclipse.xtext.ide.server.ILanguageServerAccess;
import org.eclipse.xtext.ide.server.codeActions.ICodeActionService2;
import org.eclipse.xtext.ide.server.codeActions.ICodeActionService2.Options;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.CancelIndicator;

import com.google.common.collect.Iterables;
import com.regnosys.rosetta.ide.util.CodeActionUtils;

public class RosettaResolveCodeActionService implements IResolveCodeActionService {
	@Inject
	CodeActionUtils codeActionUtils;

	@Override
	public Options createCodeActionBaseOptions(Document doc, XtextResource resource,
			ILanguageServerAccess languageServerAcces, CodeActionParams codeActionParams,
			CancelIndicator cancelIndicator) {
		Options baseOptions = new ICodeActionService2.Options();
		baseOptions.setDocument(doc);
		baseOptions.setResource(resource);
		baseOptions.setLanguageServerAccess(languageServerAcces);
		baseOptions.setCodeActionParams(codeActionParams);
		baseOptions.setCancelIndicator(cancelIndicator);

		return baseOptions;
	}

	@Override
	public CodeAction getCodeActionResolution(CodeAction codeAction, IQuickFixProvider quickfixes,
			Options baseOptions) {
		Diagnostic diagnostic = Iterables.getOnlyElement(codeAction.getDiagnostics());

		ICodeActionService2.Options options = codeActionUtils.createOptionsForSingleDiagnostic(baseOptions, diagnostic);

		List<DiagnosticResolution> resolutions = quickfixes.getResolutions(options, diagnostic).stream()
				.sorted(Comparator.nullsLast(Comparator.comparing(DiagnosticResolution::getLabel)))
				.filter(r -> r.getLabel().equals(codeAction.getTitle())).collect(Collectors.toList());

		// since a CodeAction has only one diagnostic, only one resolution should be found
		codeAction.setEdit(resolutions.get(0).apply());

		return codeAction;
	}

}
