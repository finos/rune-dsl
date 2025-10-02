package com.regnosys.rosetta.validation.expression;

import org.eclipse.xtext.validation.Check;

import com.regnosys.rosetta.RosettaEcoreUtil;
import com.regnosys.rosetta.rosetta.RosettaFeature;
import com.regnosys.rosetta.rosetta.expression.ConstructorKeyValuePair;
import com.regnosys.rosetta.rosetta.expression.RosettaConstructorExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.types.RChoiceType;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.RType;
import com.regnosys.rosetta.types.builtin.RRecordType;
import com.regnosys.rosetta.utils.ConstructorManagementService;
import com.regnosys.rosetta.utils.ConstructorManagementService.RosettaFeatureGroup;

import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*;
import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.*;
import static com.regnosys.rosetta.validation.RosettaIssueCodes.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

public class ConstructorValidator extends AbstractExpressionValidator {
	@Inject
	private RosettaEcoreUtil ecoreUtil;
	@Inject
	private ConstructorManagementService constructorService;
	
	@Check
	public void checkConstructorExpression(RosettaConstructorExpression ele) {

		RType rType = typeProvider.getRMetaAnnotatedType(ele).getRType();
			
		RType baseRType = typeSystem.stripFromTypeAliases(rType);
		if (baseRType instanceof RChoiceType) {
			baseRType = ((RChoiceType) baseRType).asRDataType();
		}
		if (!(baseRType instanceof RDataType || baseRType instanceof RRecordType || baseRType.equals(builtins.NOTHING))) {
			error("Cannot construct an instance of type `" + rType + "`", ele, ROSETTA_TYPED__TYPE_CALL);
		}
		
		Set<RosettaFeature> seenFeatures = new HashSet<>();
		for (ConstructorKeyValuePair pair : ele.getValues()) {
			RosettaFeature feature = pair.getKey();
			if (ecoreUtil.isResolved(feature)) {
				RosettaExpression expr = pair.getValue();
				if (!seenFeatures.add(feature)) {
					error("Duplicate attribute `" + feature.getName() + "`", pair, CONSTRUCTOR_KEY_VALUE_PAIR__KEY);
				}
				subtypeCheck(typeProvider.getRTypeOfFeature(feature, pair), expr, pair, CONSTRUCTOR_KEY_VALUE_PAIR__VALUE, actual -> "Cannot assign `" + actual + "` to attribute `" + feature.getName() + "`");
				if (!cardinalityProvider.isFeatureMulti(feature)) {
					isSingleCheck(expr, pair, CONSTRUCTOR_KEY_VALUE_PAIR__VALUE, "Cannot assign a list to a single value");
				}
			}
		}
		
		RosettaFeatureGroup featureGroup = constructorService.groupConstructorFeatures(ele);
		List<RosettaFeature> absentRequiredAttributes = featureGroup.getAbsentRequiredAttributes();
		if (ele.isImplicitEmpty()) {
	        if (!absentRequiredAttributes.isEmpty()) {
	        	String absent = absentRequiredAttributes.stream().map(a -> a.getName()).collect(Collectors.joining("`, `", "`", "`"));
	            error("Missing attributes " + absent, ele, ROSETTA_TYPED__TYPE_CALL, MISSING_MANDATORY_CONSTRUCTOR_ARGUMENT);
	        }
	        List<RosettaFeature> optionalAbsentAttributes = featureGroup.getAbsentOptionalAttributes();
	        if (optionalAbsentAttributes.isEmpty()) {
	            error("There are no optional attributes left", ele, ROSETTA_CONSTRUCTOR_EXPRESSION__IMPLICIT_EMPTY);
	        }
	    } else {
	        List<RosettaFeature> allAbsentAttributes = featureGroup.getAbsentAttributes();
	        if (!allAbsentAttributes.isEmpty()) {
	        	String absent = allAbsentAttributes.stream().map(a -> a.getName()).collect(Collectors.joining("`, `", "`", "`"));
	        	String message = "Missing attributes " + absent;
	        	if (absentRequiredAttributes.isEmpty()) {
	        		message += ". Perhaps you forgot a `...` at the end of the constructor?";
	        	}
	            error(message, ele, ROSETTA_TYPED__TYPE_CALL, MISSING_MANDATORY_CONSTRUCTOR_ARGUMENT);
	        }
	    }
	}
}
