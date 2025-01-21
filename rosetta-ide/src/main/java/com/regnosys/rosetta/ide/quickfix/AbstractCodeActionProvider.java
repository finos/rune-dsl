package com.regnosys.rosetta.ide.quickfix;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.log4j.Logger;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.xtext.ide.editor.quickfix.DiagnosticResolution;
import org.eclipse.xtext.ide.editor.quickfix.DiagnosticResolutionAcceptor;
import org.eclipse.xtext.ide.server.codeActions.ICodeActionService2.Options;

import com.regnosys.rosetta.ide.util.CodeActionUtils;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.utils.ImportManagementService;

public abstract class AbstractCodeActionProvider implements ICodeActionProvider {
	@Inject
	private ImportManagementService importManagementService;
	@Inject
	private CodeActionUtils codeActionUtils;
	@Inject
	private Provider<DiagnosticResolutionAcceptor> issueResolutionAcceptorProvider;

	private static final Logger LOG = Logger.getLogger(RosettaCodeActionProvider.class);

	@Override
	public List<CodeAction> getCodeActions(Options options) {
		List<CodeAction> result = new ArrayList<>();

		// Handle Sorting CodeAction
		RosettaModel model = (RosettaModel) options.getResource().getContents().get(0);
		if (!importManagementService.isSorted(model.getImports())) {
			result.add(codeActionUtils.createUnresolvedCodeAction("Sort imports", options.getCodeActionParams(),
					CodeActionKind.SourceOrganizeImports));
		}

		return result;
	}

	@Override
	public CodeAction getResolutions(CodeAction unresolved, Options baseOptions) {
		String title = unresolved.getTitle();
		if (unresolved == null || title == null) {
			return null;
		}

		Method resolutionMethod = findResolutionMethod(getClass(), title);

		if (resolutionMethod != null) {
			try {
				DiagnosticResolutionAcceptor acceptor = issueResolutionAcceptorProvider.get();
				resolutionMethod.invoke(this, acceptor);

				Options options = codeActionUtils.createOptionsForCodeAction(baseOptions, title);
				// Create resolved CodeAction with edits from the resolution
				unresolved.setEdit(getEditsFromAcceptor(acceptor, unresolved, options));
				return unresolved;
			} catch (Exception e) {
				LOG.error("Error resolving code action: " + title, e);
			}
		}
		return unresolved;
	}

	private WorkspaceEdit getEditsFromAcceptor(DiagnosticResolutionAcceptor acceptor, CodeAction unresolved,
			Options options) {
		for (DiagnosticResolution resolution : acceptor.getDiagnosticResolutions(options)) {
			return resolution.apply();
		}
		return null;
	}

	private Method findResolutionMethod(Class<? extends AbstractCodeActionProvider> clazz, String title) {
		return Arrays.stream(clazz.getMethods()).filter(method -> {
			CodeActionResolution annotation = method.getAnnotation(CodeActionResolution.class);
			return annotation != null && annotation.value().equals(title);
		}).findFirst().orElse(null);
	}
}
