package com.regnosys.rosetta.ide.quickfix;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.xtext.ide.editor.quickfix.DiagnosticResolution;
import org.eclipse.xtext.ide.editor.quickfix.IQuickFixProvider;
import org.eclipse.xtext.ide.server.codeActions.ICodeActionService2.Options;

public class RosettaQuickFixResolutionService implements ICodeActionResolutionService {
	@Inject
	private IQuickFixProvider quickfixes;

	@Override
	public CompletableFuture<CodeAction> getCodeActionResolution(CodeAction unresolved) {
		for (Diagnostic diagnostic : unresolved.getDiagnostics()) {
			Options diagnosticOptions = (Options) unresolved.getData();
			List<DiagnosticResolution> resolutions = quickfixes.getResolutions(diagnosticOptions, diagnostic).stream()
					.sorted(Comparator.nullsLast(Comparator.comparing(DiagnosticResolution::getLabel)))
					.collect(Collectors.toList());

			for (DiagnosticResolution resolution : resolutions) {
				return CompletableFuture.completedFuture(createFix(resolution, diagnostic));
			}
		}

		return CompletableFuture.failedFuture(null); // return failed future
	}

	private CodeAction createFix(DiagnosticResolution resolution, Diagnostic diagnostic) {
		CodeAction codeAction = new CodeAction();
		codeAction.setDiagnostics(Collections.singletonList(diagnostic));
		codeAction.setTitle(resolution.getLabel());
		codeAction.setEdit(resolution.apply());
		codeAction.setKind(CodeActionKind.QuickFix);

		return codeAction;
	}

}
