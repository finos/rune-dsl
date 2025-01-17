package com.regnosys.rosetta.ide.quickfix;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.xtext.ide.editor.quickfix.DiagnosticResolution;
import org.eclipse.xtext.ide.editor.quickfix.DiagnosticResolutionAcceptor;
import org.eclipse.xtext.ide.server.Document;
import org.eclipse.xtext.ide.server.codeActions.ICodeActionService2.Options;

import com.google.common.base.Strings;
import com.regnosys.rosetta.ide.util.CodeActionUtils;
import com.regnosys.rosetta.rosetta.Import;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.utils.ImportManagementService;
import com.regnosys.rosetta.validation.RosettaIssueCodes;

public class RosettaCodeActionProvider implements ICodeActionProvider {
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
			Diagnostic sortingDiagnostic = codeActionUtils
					.createSortImportsDiagnostic((RosettaModel) options.getResource().getContents().get(0));

			result.add(codeActionUtils.createUnresolvedCodeAction("Sort imports.", options.getCodeActionParams(),
					sortingDiagnostic, CodeActionKind.SourceOrganizeImports));
		}

		return result;
	}

	@Override
	public List<DiagnosticResolution> getResolutions(Options options, Diagnostic diagnostic) {
		if (diagnostic == null || diagnostic.getCode() == null || diagnostic.getMessage() == null
				|| diagnostic.getSeverity() == null) {
			return Collections.emptyList();
		}
		return getResolutions(options, getFixMethods(diagnostic));
	}

	private boolean getFixMethodPredicate(Method input, String issueCode) {
		for (CodeActionResolution annotation : input.getAnnotationsByType(CodeActionResolution.class)) {
			boolean result = annotation != null && Objects.equals(issueCode, annotation.value())
					&& input.getParameterTypes().length == 1 && Void.TYPE == input.getReturnType()
					&& input.getParameterTypes()[0].isAssignableFrom(DiagnosticResolutionAcceptor.class);
			if (result) {
				return true;
			}
		}
		return false;
	}

	private List<DiagnosticResolution> getResolutions(Options options, Iterable<Method> fixMethods) {
		DiagnosticResolutionAcceptor issueResolutionAcceptor = issueResolutionAcceptorProvider.get();
		for (Method fixMethod : fixMethods) {
			try {
				// will throw if this is not a public method, but it should be
				fixMethod.invoke(this, issueResolutionAcceptor);
			} catch (Exception e) {
				LOG.error("Error executing fix method", e);
			}
		}
		return issueResolutionAcceptor.getDiagnosticResolutions(options);
	}

	private Iterable<Method> collectMethods(Class<? extends RosettaCodeActionProvider> clazz, String issueCode) {
		return Arrays.stream(clazz.getMethods()).filter(method -> getFixMethodPredicate(method, issueCode))
				.collect(Collectors.toList());
	}

	private Iterable<Method> getFixMethods(Diagnostic diagnostic) {
		if (Strings.isNullOrEmpty(diagnostic.getCode().getLeft())) {
			return Collections.emptyList();
		}
		return collectMethods(getClass(), diagnostic.getCode().getLeft());
	}

	@CodeActionResolution(RosettaIssueCodes.UNSORTED_IMPORTS)
	public void sortImports(DiagnosticResolutionAcceptor acceptor) {
		acceptor.accept("Sort imports.", (Diagnostic diagnostic, EObject object, Document document) -> {
			Import importObj = (Import) object;
			EObject container = importObj.eContainer();

			if (container instanceof RosettaModel) {
				RosettaModel model = (RosettaModel) container;
				EList<Import> imports = model.getImports();

				Range importsRange = codeActionUtils.getImportsRange(imports);

				importManagementService.sortImports(imports);
				String sortedImportsText = importManagementService.toString(imports);

				return List.of(new TextEdit(importsRange, sortedImportsText));
			}

			// if not model, return empty list of edits
			return List.of();
		});
	}
}
