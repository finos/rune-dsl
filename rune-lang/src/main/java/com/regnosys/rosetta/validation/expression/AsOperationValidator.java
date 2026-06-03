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
        // The argument must be a choice type or a data type.
        if (!(rType instanceof RChoiceType) && !(rType instanceof RDataType)) {
            unsupportedTypeError(argumentType, op.getOperator(), op, ROSETTA_UNARY_OPERATION__ARGUMENT,
                    "Supported argument types are complex types and choice types");
            return;
        }

        // The result type of the operation is the target type (or `nothing` if the target is unresolved,
        // which is a subtype of anything and hence reported by linking rather than here).
        RType targetType = typeSystem.stripFromTypeAliases(typeProvider.getRMetaAnnotatedType(op).getRType());
        // The target type must be a subtype of the argument type: for a choice, any (nested) option;
        // for a data type, any (transitive) extension.
        if (!typeSystem.isSubtypeOf(targetType, rType, false)) {
            error("`" + targetType.getName() + "` is not a subtype of `" + rType.getName()
                    + "`. The `as` operator can only filter to a subtype of its argument.", op, AS_OPERATION__TYPE);
        }
    }
}
