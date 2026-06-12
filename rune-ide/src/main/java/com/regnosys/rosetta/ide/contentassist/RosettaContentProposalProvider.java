package com.regnosys.rosetta.ide.contentassist;

import jakarta.inject.Inject;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.CrossReference;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.ide.editor.contentassist.ContentAssistContext;
import org.eclipse.xtext.ide.editor.contentassist.ContentAssistEntry;
import org.eclipse.xtext.ide.editor.contentassist.IIdeContentProposalAcceptor;
import org.eclipse.xtext.ide.editor.contentassist.IdeContentProposalProvider;
import org.eclipse.xtext.resource.EObjectDescription;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.regnosys.rosetta.RosettaEcoreUtil;
import com.regnosys.rosetta.rosetta.RosettaType;
import com.regnosys.rosetta.rosetta.expression.AsOperation;
import com.regnosys.rosetta.rosetta.expression.ExpressionPackage;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.services.RosettaGrammarAccess;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.RType;
import com.regnosys.rosetta.types.RosettaTypeProvider;
import com.regnosys.rosetta.types.TypeSystem;

public class RosettaContentProposalProvider extends IdeContentProposalProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(RosettaContentProposalProvider.class);

	@Inject
	private RosettaGrammarAccess grammarAccess;
	@Inject
	private RosettaEcoreUtil ecoreUtil;
	@Inject
	private RosettaTypeProvider typeProvider;
	@Inject
	private TypeSystem typeSystem;

	@Override
	protected void _createProposals(RuleCall ruleCall, ContentAssistContext context, IIdeContentProposalAcceptor acceptor) {
		if (context.getCurrentModel() instanceof Attribute && ruleCall.equals(grammarAccess.getAttributeAccess().getRosettaNamedParserRuleCall_1())) {
			createProposalsForAttributeName((Attribute) context.getCurrentModel(), context, acceptor);
		}
		super._createProposals(ruleCall, context, acceptor);
	}

	@Override
	protected Predicate<IEObjectDescription> getCrossrefFilter(CrossReference reference, ContentAssistContext context) {
		Predicate<IEObjectDescription> baseFilter = super.getCrossrefFilter(reference, context);
		EObject model = context.getCurrentModel();
		// Restrict the `as` target proposals to valid targets of the argument type. This only affects
		// auto-completion; the actual rule is enforced by validation (see AsOperationValidator).
		if (model instanceof AsOperation op
				&& ExpressionPackage.Literals.AS_OPERATION__TYPE.equals(GrammarUtil.getReference(reference))) {
			RType argumentType = typeSystem.stripFromTypeAliases(typeProvider.getRMetaAnnotatedType(op.getArgument()).getRType());
			return desc -> baseFilter.apply(desc) && isValidAsTarget(desc, argumentType, op);
		}
		return baseFilter;
	}

	private boolean isValidAsTarget(IEObjectDescription candidate, RType argumentType, EObject context) {
		// For a choice argument the scope already restricts proposals to its options. Only the data case
		// needs refining: the scope returns all data types, so keep only the (transitive) extensions.
		if (!(argumentType instanceof RDataType)) {
			return true;
		}
		try {
			EObject obj = candidate.getEObjectOrProxy();
			if (obj.eIsProxy()) {
				obj = EcoreUtil2.resolve(obj, context);
			}
			if (obj instanceof RosettaType type && !obj.eIsProxy()) {
				RType candidateType = typeSystem.stripFromTypeAliases(typeSystem.typeWithUnknownArgumentsToRType(type));
				return typeSystem.isSubtypeOf(candidateType, argumentType, false);
			}
		} catch (Exception e) {
			// On any failure, keep the candidate rather than hiding a potentially valid proposal.
			LOGGER.warn("Failed to determine whether '" + candidate.getName()
					+ "' is a valid `as` target; keeping it as a proposal.", e);
		}
		return true;
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
