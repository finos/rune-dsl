package com.regnosys.rosetta.ide.contentassist;

import jakarta.inject.Inject;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.ide.editor.contentassist.ContentAssistContext;
import org.eclipse.xtext.ide.editor.contentassist.ContentAssistEntry;
import org.eclipse.xtext.ide.editor.contentassist.IIdeContentProposalAcceptor;
import org.eclipse.xtext.ide.editor.contentassist.IdeContentProposalProvider;
import org.eclipse.xtext.resource.EObjectDescription;

import com.regnosys.rosetta.RosettaEcoreUtil;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.services.RosettaGrammarAccess;

public class RosettaContentProposalProvider extends IdeContentProposalProvider {
	@Inject 
	private RosettaGrammarAccess grammarAccess;
	@Inject
	private RosettaEcoreUtil ecoreUtil;
	
	@Override
	protected void _createProposals(RuleCall ruleCall, ContentAssistContext context, IIdeContentProposalAcceptor acceptor) {
		if (context.getCurrentModel() instanceof Attribute && ruleCall.equals(grammarAccess.getAttributeAccess().getRosettaNamedParserRuleCall_1())) {
			createProposalsForAttributeName((Attribute) context.getCurrentModel(), context, acceptor);
		}
		super._createProposals(ruleCall, context, acceptor);
	}
	
	private void createProposalsForAttributeName(Attribute attr, ContentAssistContext context, IIdeContentProposalAcceptor acceptor) {
		if (attr.isOverride()) {
			EObject container = attr.eContainer();
			if (container instanceof Data) {
				Data data = (Data) container;
				if (data.getSuperType() != null) {
					ecoreUtil.getAllAttributes(data.getSuperType())
						.forEach((superAttr) -> {
							ContentAssistEntry proposal = getProposalCreator().createProposal(superAttr.getName(), context);
							int priority = getProposalPriorities().getCrossRefPriority(EObjectDescription.create(superAttr.getName(), superAttr), proposal);
							acceptor.accept(proposal, priority);
						});
				}
			}
		}
	}
}
