package com.regnosys.rosetta.validation;

import javax.inject.Inject;

import org.eclipse.xtext.validation.Check;

import com.regnosys.rosetta.RosettaEcoreUtil;
import com.regnosys.rosetta.rosetta.expression.ParseOperation;
import com.regnosys.rosetta.rosetta.expression.ToEnumOperation;
import com.regnosys.rosetta.types.CardinalityProvider;
import com.regnosys.rosetta.types.REnumType;
import com.regnosys.rosetta.types.RosettaTypeProvider;
import com.regnosys.rosetta.types.TypeSystem;
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
            
            if (ele instanceof ToEnumOperation) {
                if (!typeSystem.isSubtypeOf(typeProvider.getRMetaAnnotatedType(arg), builtInTypeService.UNCONSTRAINED_STRING_WITH_NO_META)
                        && !(typeProvider.getRMetaAnnotatedType(arg).getRType() instanceof REnumType)) {
                    error(String.format("The argument of %s should be either a string or an enum.", ele.getOperator()), ele, ROSETTA_UNARY_OPERATION__ARGUMENT);
                }
            } else {
                if (!typeSystem.isSubtypeOf(typeProvider.getRMetaAnnotatedType(arg), builtInTypeService.UNCONSTRAINED_STRING_WITH_NO_META)) {
                    error(String.format("he argument of  %s should be a string.", ele.getOperator()), ele, ROSETTA_UNARY_OPERATION__ARGUMENT);
                }
            }
        }
    }
    
}
