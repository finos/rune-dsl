package com.regnosys.rosetta.ide.inlayhints;

import javax.inject.Inject;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.lsp4j.InlayHint;

import com.regnosys.rosetta.generator.java.function.CardinalityProvider;
import com.regnosys.rosetta.rosetta.expression.ExtractAllOperation;
import com.regnosys.rosetta.rosetta.expression.InlineFunction;
import com.regnosys.rosetta.rosetta.expression.MapOperation;
import com.regnosys.rosetta.rosetta.expression.ReduceOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaFeatureCall;
import com.regnosys.rosetta.rosetta.expression.RosettaFunctionalOperation;
import com.regnosys.rosetta.rosetta.simple.Operation;
import com.regnosys.rosetta.rosetta.simple.Segment;
import com.regnosys.rosetta.types.RType;
import com.regnosys.rosetta.types.RosettaTypeProvider;

public class RosettaInlayHintsService extends AbstractInlayHintsService {
	@Inject
	private RosettaTypeProvider types;
	@Inject
	private CardinalityProvider card;
	
	// TODO: duplicate code with RosettaSemanticTokensService.
	private boolean isResolved(EObject obj) {
		return obj != null && !obj.eIsProxy();
	}
	
	private String typeInfo(RType type, boolean isMulti) {
		if (isMulti) {
			return type.getName() + " (0..*)";
		} else {
			return type.getName() + " (0..1)";
		}
	}
	
	@InlayHintCheck
	public InlayHint checkRosettaSegment(Segment seg) {
		if (seg.getAttribute().getCard().isPlural()) {
			return inlayHintAfter(seg, "(0..*)", null);
		}
		return null;
	}
	
	@InlayHintCheck
	public InlayHint checkFunctionalOperation(RosettaFunctionalOperation op) {
		if (op instanceof ReduceOperation || op instanceof MapOperation || op instanceof ExtractAllOperation) {
			if (isResolved(op.getFunctionRef()) && op.getFunctionRef() instanceof InlineFunction) {
				RType outputType = types.getRType(op);
				boolean outputMulti = card.isMulti(op);
	
				if (outputType != null) {
					return inlayHintAfter(op, typeInfo(outputType, outputMulti), null);
				}
			}
		}
		return null;
	}
}
