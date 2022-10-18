package com.regnosys.rosetta.validation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.EValidatorRegistrar;

import com.google.inject.Inject;
import com.regnosys.rosetta.rosetta.RosettaCardinality;
import com.regnosys.rosetta.rosetta.expression.ListOperation;
import com.regnosys.rosetta.types.RListType;
import com.regnosys.rosetta.types.TypeFactory;
import com.regnosys.rosetta.types.TypeSystem;
import com.regnosys.rosetta.types.TypeValidationUtil;
import com.regnosys.rosetta.typing.validation.RosettaTypingValidator;

import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals;

public class StandaloneRosettaTypingValidator extends RosettaTypingValidator {
	@Inject
	private TypeSystem ts;
	
	@Inject
	private TypeFactory tf;
	
	@Inject
	private TypeValidationUtil tu;
	
	@Override
	protected List<EPackage> getEPackages() {
		List<EPackage> result = new ArrayList<EPackage>();
		result.add(EPackage.Registry.INSTANCE.getEPackage("http://www.rosetta-model.com/Rosetta"));
		result.add(EPackage.Registry.INSTANCE.getEPackage("http://www.rosetta-model.com/RosettaSimple"));
		result.add(EPackage.Registry.INSTANCE.getEPackage("http://www.rosetta-model.com/RosettaExpression"));
		return result;
	}
	
	@Override
	public void register(EValidatorRegistrar registrar) {
	}
	
	@Check
	public void checkListOperation(ListOperation e) {
		RListType t = ts.inferType(e.getArgument());
		if (t != null) {
			RosettaCardinality minimalConstraint = tf.createConstraint(1, 2);
			if (!minimalConstraint.isSubconstraintOf(t.getConstraint())) {
				warning(tu.notLooserConstraintMessage(minimalConstraint, t), e, Literals.ROSETTA_UNARY_OPERATION__ARGUMENT);
			}
		}
	}
}
