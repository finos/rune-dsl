package com.regnosys.rosetta.validation;

import java.util.HashSet;
import java.util.Set;

import jakarta.inject.Inject;

import org.eclipse.xtext.validation.Check;

import com.regnosys.rosetta.RosettaEcoreUtil;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Choice;
import com.regnosys.rosetta.rosetta.simple.Data;

import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*;
import static com.regnosys.rosetta.rosetta.simple.SimplePackage.Literals.*;
import static com.regnosys.rosetta.validation.RosettaIssueCodes.*;

public class TypeValidator extends AbstractDeclarativeRosettaValidator {
	@Inject
	private RosettaEcoreUtil ecoreUtil;
    @Inject
    private CycleValidationHelper cycleValidationHelper;
	@Inject
	private WarningSuppressionHelper warningSuppressionHelper;

	@Check
	public void checkTypeNameIsCapitalized(Data data) {
		// TODO: also enforce on Choice's once Choice does not extend Data anymore
		String name = data.getName();
		if (name != null) {
			boolean suppressed = warningSuppressionHelper.isCapitalisationSuppressed(data);
			if (!suppressed && Character.isLowerCase(name.charAt(0))) {
				warning("Type name should start with a capital", ROSETTA_NAMED__NAME, INVALID_CASE);
			}
		}
	}
	
	@Check
	public void checkDoNotExtendChoice(Data data) {
		// TODO: remove once Choice does not extend Data anymore
		if (data.getSuperType() instanceof Choice) {
			warning("Extending a choice type is deprecated", data, DATA__SUPER_TYPE);
		}
	}
	
	@Check
	public void checkCyclicExtensions(Data data) {
        cycleValidationHelper.detectCycle(
                data,
                Data::getSuperType,
                "extends",
                (pathMsg) -> error("Cyclic extension: " + pathMsg, data, DATA__SUPER_TYPE)
        );
	}

	@Check
	public void checkAttributeNamesAreUnique(Data data) {
		Set<String> usedNamesInSuperType = new HashSet<>();
		if (data.getSuperType() != null) {
			ecoreUtil.getAllAttributes(data.getSuperType()).forEach(attr -> usedNamesInSuperType.add(attr.getName()));
		}
		for (Attribute attr: data.getAttributes()) {
			if (!attr.isOverride() && usedNamesInSuperType.contains(attr.getName())) {
				// TODO: make this an error once `override` keyword is mandatory
				warning("Attribute '" + attr.getName() + "' already defined in super type. To override the type, cardinality or annotations of this attribute, use the keyword `override`", attr, ROSETTA_NAMED__NAME);
			}
		}
	}
	
	@Check
	public void checkAttributeOverridesShouldComeFirst(Data data) {
		boolean newAttrEncountered = false;
		for (Attribute attr: data.getAttributes()) {
			if (!attr.isOverride()) {
				newAttrEncountered = true;
			} else if (newAttrEncountered) {
				error("Attribute overrides should come before any new attributes", attr, null);
			}
		}
	}
}
