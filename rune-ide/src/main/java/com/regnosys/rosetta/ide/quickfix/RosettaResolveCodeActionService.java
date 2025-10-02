package com.regnosys.rosetta.ide.quickfix;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.xtext.ide.editor.quickfix.DiagnosticResolution;
import org.eclipse.xtext.ide.editor.quickfix.IQuickFixProvider;
import org.eclipse.xtext.ide.server.codeActions.ICodeActionService2;
import org.eclipse.xtext.ide.server.codeActions.ICodeActionService2.Options;

import com.google.common.collect.Iterables;
import com.regnosys.rosetta.ide.util.CodeActionUtils;

public class RosettaResolveCodeActionService implements IResolveCodeActionService {
    @Inject
    CodeActionUtils codeActionUtils;
    @Inject
    IQuickFixProvider resolutionProvider;
    @Inject
    ICodeActionProvider codeActionProvider;

    @Override
    public CodeAction getCodeActionResolution(CodeAction codeAction, Options baseOptions) {
        // handling resolutions for quickFixes
        if (null != codeAction.getKind() && CodeActionKind.QuickFix.equals(codeAction.getKind())) {
            Diagnostic diagnostic = Iterables.getOnlyElement(codeAction.getDiagnostics());
            ICodeActionService2.Options options = codeActionUtils.createOptionsForSingleDiagnostic(baseOptions,
                    diagnostic);

            List<DiagnosticResolution> resolutions = resolutionProvider.getResolutions(options, diagnostic).stream()
                    .sorted(Comparator.nullsLast(Comparator.comparing(DiagnosticResolution::getLabel)))
                    .filter(r -> r.getLabel().equals(codeAction.getTitle())).collect(Collectors.toList());

            // since a CodeAction has only one diagnostic, only one resolution should be found
            codeAction.setEdit(resolutions.get(0).apply());

            return codeAction;
        }
        // handling resolutions for all other types of codeActions
        return codeActionProvider.resolve(codeAction, baseOptions);
    }

}
