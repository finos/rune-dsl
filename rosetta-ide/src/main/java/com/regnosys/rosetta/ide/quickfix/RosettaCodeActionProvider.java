package com.regnosys.rosetta.ide.quickfix;

import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;

import org.eclipse.emf.common.util.EList;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.xtext.ide.server.codeActions.ICodeActionService2.Options;

import com.regnosys.rosetta.ide.util.CodeActionUtils;
import com.regnosys.rosetta.rosetta.Import;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.utils.ImportManagementService;

public class RosettaCodeActionProvider extends AbstractCodeActionProvider {
	private static final String SORT_IMPORTS_LABEL = "Sort imports";
	
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
			result.add(codeActionUtils.createUnresolvedCodeAction(SORT_IMPORTS_LABEL, options.getCodeActionParams(),
					CodeActionKind.SourceOrganizeImports));
		}

		return result;
	}

	@CodeActionResolution(SORT_IMPORTS_LABEL)
	public List<TextEdit> sortImports(RosettaModel model) {
		EList<Import> imports = model.getImports();

		Range importsRange = codeActionUtils.getImportsRange(imports);

		importManagementService.sortImports(imports);
		String sortedImportsText = importManagementService.toString(imports);

		return List.of(new TextEdit(importsRange, sortedImportsText));
	}
}
