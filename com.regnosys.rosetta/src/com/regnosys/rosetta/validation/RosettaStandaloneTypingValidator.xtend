package com.regnosys.rosetta.validation

import java.util.List
import org.eclipse.emf.ecore.EPackage
import java.util.ArrayList
import com.regnosys.rosetta.typing.validation.RosettaTypingValidator
import org.eclipse.xtext.validation.EValidatorRegistrar

class RosettaStandaloneTypingValidator extends RosettaTypingValidator {
	protected override List<EPackage> getEPackages() {
		val result = new ArrayList<EPackage>();
		result.add(EPackage.Registry.INSTANCE.getEPackage("http://www.rosetta-model.com/Rosetta"));
		result.add(EPackage.Registry.INSTANCE.getEPackage("http://www.rosetta-model.com/RosettaSimple"));
		return result;
	}
	
	override register(EValidatorRegistrar registrar) { }
}
