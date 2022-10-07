package com.regnosys.rosetta.validation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.xtext.validation.EValidatorRegistrar;

import com.regnosys.rosetta.typing.validation.RosettaTypingValidator;

public class StandaloneRosettaTypingValidator extends RosettaTypingValidator {
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
}
