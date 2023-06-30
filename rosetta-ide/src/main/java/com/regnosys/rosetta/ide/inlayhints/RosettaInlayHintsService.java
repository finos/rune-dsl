package com.regnosys.rosetta.ide.inlayhints;

import javax.inject.Inject;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.lsp4j.InlayHint;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

import com.regnosys.rosetta.RosettaExtensions;
import com.regnosys.rosetta.types.CardinalityProvider;
import com.regnosys.rosetta.rosetta.RosettaBlueprint;
import com.regnosys.rosetta.rosetta.expression.InlineFunction;
import com.regnosys.rosetta.rosetta.expression.MapOperation;
import com.regnosys.rosetta.rosetta.expression.ReduceOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaFunctionalOperation;
import com.regnosys.rosetta.services.RosettaGrammarAccess;
import com.regnosys.rosetta.types.RType;
import com.regnosys.rosetta.types.RosettaTypeProvider;

public class RosettaInlayHintsService extends AbstractInlayHintsService {
	@Inject
	private RosettaExtensions extensions;
	@Inject
	private RosettaTypeProvider types;
	@Inject
	private CardinalityProvider card;
	@Inject
	private RosettaGrammarAccess grammar;
	
	private String typeInfo(RType type, boolean isMulti) {
		if (isMulti) {
			return type.getName() + " (0..*)";
		} else {
			return type.getName() + " (0..1)";
		}
	}
	
	@InlayHintCheck
	public InlayHint checkFunctionalOperation(RosettaFunctionalOperation op) {
		RosettaBlueprint rule = EcoreUtil2.getContainerOfType(op, RosettaBlueprint.class);
		if (rule == null || !rule.isLegacy()) {
			if (op.getFunction() != null && operationHasBrackets(op.getFunction())) {
				if (op instanceof ReduceOperation || op instanceof MapOperation) {
					if (extensions.isResolved(op.getFunction())) {
						RType outputType = types.getRType(op);
						boolean outputMulti = card.isMulti(op);
			
						if (outputType != null) {
							return inlayHintAfter(op, typeInfo(outputType, outputMulti), null);
						}
					}
				}
			}
		}
		return null;
	}
	
	private boolean operationHasBrackets(InlineFunction op) {
		Keyword keyword = grammar.getInlineFunctionAccess().getLeftSquareBracketKeyword_0_0_1();
		ICompositeNode node = NodeModelUtils.findActualNodeFor(op);

        for (INode n : node.getChildren()) {
            EObject ge = n.getGrammarElement();
            if (ge instanceof Keyword && ge == keyword) {
                return true;
            }
        }
        return false;
	}
}
