package com.regnosys.rosetta.ide.quickfix;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.xtext.ide.server.codeActions.ICodeActionService2.Options;

import com.regnosys.rosetta.ide.util.CodeActionUtils;
import com.regnosys.rosetta.rosetta.Import;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.utils.ImportManagementService;

public class RosettaCodeActionProvider implements ICodeActionProvider {
	@Inject
	private ImportManagementService importManagementService;
	@Inject
	private CodeActionUtils codeActionUtils;

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
	public List<TextEdit> sortImportsResolution(EObject object) {
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
	}

}
