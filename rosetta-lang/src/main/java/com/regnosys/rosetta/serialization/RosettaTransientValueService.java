package com.regnosys.rosetta.serialization;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.parsetree.reconstr.impl.DefaultTransientValueService;

import com.regnosys.rosetta.rosetta.expression.ExpressionPackage;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;

public class RosettaTransientValueService extends DefaultTransientValueService {
	private EStructuralFeature generatedInputWasSetFeature = ExpressionPackage.eINSTANCE.getHasGeneratedInput_GeneratedInputWasSet();
	
	@Override
	public boolean isTransient(EObject owner, EStructuralFeature feature, int index) {
		if (super.isTransient(owner, feature, index)) {
			return true;
		}
		if (feature.equals(generatedInputWasSetFeature)) {
			return true;
		}
		Object value = owner.eGet(feature);
		if (value instanceof RosettaExpression && ((RosettaExpression)value).isGenerated()) {
			return true;
		}
		return false;
	}

}
