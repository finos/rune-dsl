package com.regnosys.rosetta.validation.expression;

import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.*;

import org.eclipse.xtext.validation.Check;

import com.regnosys.rosetta.rosetta.expression.AsOperation;
import com.regnosys.rosetta.types.RChoiceType;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.RMetaAnnotatedType;
import com.regnosys.rosetta.types.RType;

public class AsOperationValidator extends ExpressionValidator {

    @Check
    public void checkAs(AsOperation op) {
        RMetaAnnotatedType argumentType = typeProvider.getRMetaAnnotatedType(op.getArgument());
        RType rType = typeSystem.stripFromTypeAliases(argumentType.getRType());
        if (rType.equals(builtins.NOTHING)) {
            // If there is an error within the argument, do not check further.
            return;
        }
        // The result type of the operation is the target type (or `nothing` if the target is unresolved,
        // in which case linking - not this check - reports the problem).
        RType targetType = typeSystem.stripFromTypeAliases(typeProvider.getRMetaAnnotatedType(op).getRType());
        if (targetType.equals(builtins.NOTHING)) {
            return;
        }

        if (rType instanceof RChoiceType) {
            // A choice type may only be narrowed to one of its (nested) options. We deliberately do not
            // mix choice subtyping with data extension subtyping, so narrowing to a strict subtype of an
            // option is not allowed.
            RChoiceType choiceType = (RChoiceType) rType;
            RType target = targetType;
            boolean isOption = choiceType.getAllOptions().stream()
                    .anyMatch(o -> typeSystem.stripFromTypeAliases(o.getType().getRType()).equals(target));
            if (!isOption) {
                error("`" + targetType.getName() + "` is not an option of choice type `" + rType.getName()
                        + "`. The `as` operator can only narrow a choice type to one of its (nested) options.",
                        op, AS_OPERATION__TYPE);
            }
        } else if (rType instanceof RDataType) {
            // A data type may be narrowed to any (transitive) extension.
            if (!typeSystem.isSubtypeOf(targetType, rType, false)) {
                error("`" + targetType.getName() + "` is not a subtype of `" + rType.getName()
                        + "`. The `as` operator can only narrow a data type to one of its extensions.",
                        op, AS_OPERATION__TYPE);
            }
        } else {
            unsupportedTypeError(argumentType, op.getOperator(), op, ROSETTA_UNARY_OPERATION__ARGUMENT,
                    "Supported argument types are complex types and choice types");
        }
    }
}
