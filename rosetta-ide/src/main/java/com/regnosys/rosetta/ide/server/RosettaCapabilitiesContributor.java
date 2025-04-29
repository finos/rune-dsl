package com.regnosys.rosetta.ide.server;

import java.util.List;

import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InlayHintRegistrationOptions;
import org.eclipse.lsp4j.SemanticTokensWithRegistrationOptions;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.xtext.ide.server.ICapabilitiesContributor;

import com.regnosys.rosetta.ide.semantictokens.ISemanticTokensService;

import jakarta.inject.Inject;

public class RosettaCapabilitiesContributor implements ICapabilitiesContributor {
	@Inject
	private ISemanticTokensService semanticTokensService;

	@Override
	public ServerCapabilities contribute(ServerCapabilities capabilities, InitializeParams params) {
		InlayHintRegistrationOptions inlayHintRegistrationOptions = new InlayHintRegistrationOptions();
		inlayHintRegistrationOptions.setResolveProvider(true);
		capabilities.setInlayHintProvider(inlayHintRegistrationOptions);
		
        CodeActionOptions codeActionProvider = new CodeActionOptions();
        codeActionProvider.setResolveProvider(true);
        codeActionProvider.setCodeActionKinds(List.of(CodeActionKind.QuickFix, CodeActionKind.SourceOrganizeImports));
        codeActionProvider.setWorkDoneProgress(true);
        capabilities.setCodeActionProvider(codeActionProvider);
		
		SemanticTokensWithRegistrationOptions semanticTokensOptions = new SemanticTokensWithRegistrationOptions();
		semanticTokensOptions.setLegend(semanticTokensService.getLegend());
		semanticTokensOptions.setFull(true);
		semanticTokensOptions.setRange(true);
		capabilities.setSemanticTokensProvider(semanticTokensOptions);
		
		RosettaAdditionalServerCapabilities additionalCapabilities = new RosettaAdditionalServerCapabilities();
		additionalCapabilities.setParentsProvider(true);
		
		capabilities.setExperimental(additionalCapabilities);
		
		return capabilities;
	}

}
