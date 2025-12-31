package com.regnosys.rosetta.validation;

import com.regnosys.rosetta.RosettaEcoreUtil;
import com.regnosys.rosetta.rosetta.RosettaPackage;
import com.regnosys.rosetta.rosetta.RosettaSymbol;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.simple.Condition;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.utils.ExpressionHelper;
import jakarta.inject.Inject;
import org.eclipse.xtext.validation.Check;

import java.util.List;
import java.util.Stack;

public class ConditionValidator extends AbstractDeclarativeRosettaValidator {
    @Inject
    private RosettaEcoreUtil rosettaEcoreUtil;
    @Inject
    private ExpressionHelper exprHelper;

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

    @Check
    public void checkConditionDontUseOutput(Function ele) {
        for (Condition cond : ele.getConditions()) {
            if (cond.isPostCondition()) continue;
            RosettaExpression expr = cond.getExpression();
            if (expr == null) continue;

            Stack<String> trace = new Stack<>();
            List<RosettaSymbol> outRef = exprHelper.findOutputRef(expr, trace);
            if (outRef != null && !outRef.isEmpty()) {
                RosettaSymbol first = outRef.get(0);
                StringBuilder msg = new StringBuilder();
                msg.append("output '").append(first.getName()).append("' or alias on output '")
                        .append(first.getName()).append("' not allowed in condition blocks.");
                if (!trace.isEmpty()) {
                    msg.append("\n").append(String.join(" > ", trace)).append(" > ").append(first.getName());
                }
                error(msg.toString(), expr, null);
            }
        }
    }
}
