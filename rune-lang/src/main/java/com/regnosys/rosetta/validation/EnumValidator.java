package com.regnosys.rosetta.validation;

import com.regnosys.rosetta.RosettaEcoreUtil;
import com.regnosys.rosetta.rosetta.RosettaEnumValue;
import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import jakarta.inject.Inject;
import org.eclipse.xtext.validation.Check;

import java.util.HashSet;
import java.util.Set;

import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.ROSETTA_ENUMERATION__PARENT;
import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.ROSETTA_NAMED__NAME;
import static com.regnosys.rosetta.validation.RosettaIssueCodes.INVALID_CASE;

public class EnumValidator extends AbstractDeclarativeRosettaValidator {
	@Inject
	private RosettaEcoreUtil ecoreUtil;
    @Inject
    private CycleValidationHelper cycleValidationHelper;
	@Inject
	private WarningSuppressionHelper warningSuppressionHelper;
	
	@Check
	public void checkEnumNameIsCapitalized(RosettaEnumeration enumeration) {
        String name = enumeration.getName();
		boolean suppressed = warningSuppressionHelper.isCapitalisationSuppressed(enumeration);
		if (!suppressed && name != null && Character.isLowerCase(name.charAt(0))) {
			warning("Enumeration name should start with a capital", ROSETTA_NAMED__NAME, INVALID_CASE);
		}
	}
	
	@Check
	public void checkCyclicExtensions(RosettaEnumeration enumeration) {
        cycleValidationHelper.detectCycle(
                enumeration,
                RosettaEnumeration::getParent,
                "extends",
                (pathMsg) -> error("Cyclic extension: " + pathMsg, enumeration, ROSETTA_ENUMERATION__PARENT)
        );
	}
	
	@Check
	public void checkEnumValuesAreUnique(RosettaEnumeration enumeration) {
		Set<String> usedNames = new HashSet<>();
		if (enumeration.getParent() != null) {
			ecoreUtil.getAllEnumValues(enumeration.getParent()).forEach(v -> usedNames.add(v.getName()));
		}
		for (RosettaEnumValue value: enumeration.getEnumValues()) {
			if (!usedNames.add(value.getName())) {
				error("Duplicate enum value '" + value.getName() + "'", value, ROSETTA_NAMED__NAME);
			}
		}
	}
}
