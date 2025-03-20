package com.regnosys.rosetta.validation;

import javax.inject.Inject;

import com.regnosys.rosetta.types.*;
import org.eclipse.xtext.validation.Check;

import com.regnosys.rosetta.RosettaEcoreUtil;
import com.regnosys.rosetta.rosetta.expression.ParseOperation;
import com.regnosys.rosetta.rosetta.expression.ToEnumOperation;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;

import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.*;

public class ParseOperationValidator extends AbstractDeclarativeRosettaValidator {
    @Inject
    private RosettaTypeProvider typeProvider;
    @Inject
    private RosettaEcoreUtil ecoreUtil;
    @Inject
    private TypeSystem typeSystem;
    @Inject
    private CardinalityProvider cardinality;
    @Inject
    private RBuiltinTypeService builtInTypeService;
    
    @Check
    public void checkParseOpArgument(ParseOperation ele) {
        var arg = ele.getArgument();
        if (ecoreUtil.isResolved(arg)) {
            if (cardinality.isMulti(arg)) {
                error(String.format("The argument of %s should be of singular cardinality.'", ele.getOperator()), ele, ROSETTA_UNARY_OPERATION__ARGUMENT);
            }

            RMetaAnnotatedType argumentRMetaType = typeProvider.getRMetaAnnotatedType(arg);
            if (ele instanceof ToEnumOperation) {
                if (!typeSystem.isSubtypeOf(argumentRMetaType, builtInTypeService.UNCONSTRAINED_STRING_WITH_NO_META)
                        && !(argumentRMetaType.getRType() instanceof REnumType)) {
                    error(String.format("The argument of %s should be either a string or an enum.", ele.getOperator()), ele, ROSETTA_UNARY_OPERATION__ARGUMENT);
                }
            } else {
                if (!typeSystem.isSubtypeOf(argumentRMetaType, builtInTypeService.UNCONSTRAINED_STRING_WITH_NO_META)) {
                    error(String.format("he argument of  %s should be a string.", ele.getOperator()), ele, ROSETTA_UNARY_OPERATION__ARGUMENT);
                }
            }
        }
    }
    
}
