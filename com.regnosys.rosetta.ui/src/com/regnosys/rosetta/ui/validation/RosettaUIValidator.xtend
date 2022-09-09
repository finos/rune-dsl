package com.regnosys.rosetta.ui.validation

import com.regnosys.rosetta.rosetta.RosettaPackage
import com.regnosys.rosetta.validation.RosettaIssueCodes
import java.util.List
import org.eclipse.emf.ecore.EPackage
import org.eclipse.xtext.validation.AbstractDeclarativeValidator

class RosettaUIValidator extends AbstractDeclarativeValidator implements RosettaIssueCodes {

	override protected List<EPackage> getEPackages() {
		return #[RosettaPackage.eINSTANCE]
	}

}
