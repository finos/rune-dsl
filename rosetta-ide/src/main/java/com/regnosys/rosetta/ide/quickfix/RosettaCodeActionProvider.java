package com.regnosys.rosetta.ide.quickfix;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.xtext.ide.editor.quickfix.DiagnosticResolutionAcceptor;
import org.eclipse.xtext.ide.server.Document;

import com.regnosys.rosetta.ide.util.CodeActionUtils;
import com.regnosys.rosetta.rosetta.Import;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.utils.ImportManagementService;

public class RosettaCodeActionProvider extends AbstractCodeActionProvider {
	@Inject
	private ImportManagementService importManagementService;
	@Inject
	private CodeActionUtils codeActionUtils;

	@CodeActionResolution("Sort imports")
	public void sortImports(DiagnosticResolutionAcceptor acceptor) {
		acceptor.accept("Sort imports", (Diagnostic diagnostic, EObject object, Document document) -> {
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
