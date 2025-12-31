package com.regnosys.rosetta.validation;

import com.regnosys.rosetta.RosettaEcoreUtil;
import com.regnosys.rosetta.rosetta.RosettaPackage;
import com.regnosys.rosetta.rosetta.simple.Condition;
import jakarta.inject.Inject;
import org.eclipse.xtext.validation.Check;

public class ConditionValidator extends AbstractDeclarativeRosettaValidator {
    @Inject
    private RosettaEcoreUtil rosettaEcoreUtil;

    @Check
    public void checkConditionName(Condition condition) {
        if (condition.getName() == null && !rosettaEcoreUtil.isConstraintCondition(condition)) {
            warning("Condition name should be specified", RosettaPackage.Literals.ROSETTA_NAMED__NAME, RosettaIssueCodes.INVALID_NAME);
        } else {
            if (condition.getName() != null && Character.isLowerCase(condition.getName().charAt(0))) {
                warning("Condition name should start with a capital", RosettaPackage.Literals.ROSETTA_NAMED__NAME, RosettaIssueCodes.INVALID_CASE);
            }
        }
    }
}
