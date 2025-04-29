package com.regnosys.rosetta.validation.expression;

import com.regnosys.rosetta.types.*;

import jakarta.inject.Inject;

import org.eclipse.xtext.validation.Check;

import com.regnosys.rosetta.RosettaEcoreUtil;
import com.regnosys.rosetta.rosetta.expression.ParseOperation;
import com.regnosys.rosetta.rosetta.expression.ToEnumOperation;
import com.regnosys.rosetta.rosetta.expression.ToStringOperation;

import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.*;

public class ParseOperationValidator extends ExpressionValidator {
    @Inject
    private RosettaEcoreUtil ecoreUtil;

    
    @Check
    public void checkParseOpArgument(ParseOperation ele) {
        var arg = ele.getArgument();
        if (ecoreUtil.isResolved(arg)) {
            isSingleCheck(arg, ele, ROSETTA_UNARY_OPERATION__ARGUMENT, ele);
            
            RMetaAnnotatedType argumentRMetaType = typeProvider.getRMetaAnnotatedType(arg);
            if (ele instanceof ToEnumOperation) {
                RType argumentRType = typeSystem.stripFromTypeAliases(argumentRMetaType.getRType());
                if (argumentRType.equals(builtins.NOTHING)) {
                    return;
                }
                if (arg instanceof ToStringOperation) {
                    var toStringArgument = ((ToStringOperation) arg).getArgument();
                    RType toStringArgumnetRType = typeSystem.stripFromTypeAliases(typeProvider.getRMetaAnnotatedType(toStringArgument).getRType());
                    if (toStringArgumnetRType instanceof REnumType) {
                        warning("Using to-string on enumeration to convert to another enum is not required", ROSETTA_UNARY_OPERATION__ARGUMENT);
                    }
                }
                if (!typeSystem.isSubtypeOf(argumentRMetaType, builtins.UNCONSTRAINED_STRING_WITH_NO_META)
                        && !(argumentRType instanceof REnumType)) {
                    unsupportedTypeError(argumentRMetaType, ele.getOperator(), ele, ROSETTA_UNARY_OPERATION__ARGUMENT, "Supported argument types are strings and enumerations");
                }
            } else {
                subtypeCheck(builtins.UNCONSTRAINED_STRING_WITH_NO_META, ele.getArgument(), ele, ROSETTA_UNARY_OPERATION__ARGUMENT, ele);
            }
        }
    }
    
}
