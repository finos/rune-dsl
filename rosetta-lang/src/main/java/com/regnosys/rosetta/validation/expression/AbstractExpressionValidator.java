package com.regnosys.rosetta.validation.expression;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import com.regnosys.rosetta.rosetta.expression.RosettaBinaryOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaOperation;
import com.regnosys.rosetta.types.CardinalityProvider;
import com.regnosys.rosetta.types.RMetaAnnotatedType;
import com.regnosys.rosetta.types.RType;
import com.regnosys.rosetta.types.RosettaTypeProvider;
import com.regnosys.rosetta.types.TypeSystem;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;
import com.regnosys.rosetta.validation.AbstractDeclarativeRosettaValidator;

public class AbstractExpressionValidator extends AbstractDeclarativeRosettaValidator {
	@Inject
	protected RosettaTypeProvider typeProvider;
	@Inject
	protected TypeSystem typeSystem;
	@Inject
	protected RBuiltinTypeService builtins;
	@Inject
	protected CardinalityProvider cardinalityProvider;
	
	protected String relevantTypeDescription(RMetaAnnotatedType type, RMetaAnnotatedType context) {
		RType valueType = type.getRType();
		RType valueContext = context.getRType();
		if (valueType.equals(valueContext)) {
			// Include meta info
			return type.toString();
		}
		if (valueType.getName().equals(valueContext.getName())) {
			// Include type parameters
			return valueType.toString();
		}
		return valueType.getName();
	}
	
	protected String notASubtypeMessage(RMetaAnnotatedType expected, RMetaAnnotatedType actual) {
		return new StringBuilder()
				.append("Expected type `")
				.append(relevantTypeDescription(expected, actual))
				.append("`, but got `")
				.append(relevantTypeDescription(actual, expected))
				.append("` instead")
				.toString();
	}
	protected boolean subtypeCheck(RMetaAnnotatedType expected, RosettaExpression expr, EObject sourceObject, EStructuralFeature feature) {
		return subtypeCheck(expected, typeProvider.getRMetaAnnotatedType(expr), sourceObject, feature, INSIGNIFICANT_INDEX);
	}
	protected boolean subtypeCheck(RMetaAnnotatedType expected, RosettaExpression expr, EObject sourceObject, EStructuralFeature feature, int featureIndex) {
		return subtypeCheck(expected, typeProvider.getRMetaAnnotatedType(expr), sourceObject, feature, featureIndex);
	}
	protected boolean subtypeCheck(RMetaAnnotatedType expected, RMetaAnnotatedType actual, EObject sourceObject, EStructuralFeature feature) {
		return subtypeCheck(expected, actual, sourceObject, feature, INSIGNIFICANT_INDEX);
	}
	protected boolean subtypeCheck(RMetaAnnotatedType expected, RMetaAnnotatedType actual, EObject sourceObject, EStructuralFeature feature, int featureIndex) {
		if (!typeSystem.isSubtypeOf(actual, expected)) {
			error(notASubtypeMessage(expected, actual), sourceObject, feature, featureIndex);
			return false;
		}
		return true;
	}
	
	protected String notComparableMessage(RMetaAnnotatedType left, RMetaAnnotatedType right) {
		return new StringBuilder()
				.append("Types `")
				.append(relevantTypeDescription(left, right))
				.append("` and `")
				.append(relevantTypeDescription(right, left))
				.append("` are not comparable")
				.toString();
	}
	protected boolean comparableTypeCheck(RosettaBinaryOperation sourceObject) {
		RMetaAnnotatedType tl = typeProvider.getRMetaAnnotatedType(sourceObject.getLeft());
		RMetaAnnotatedType tr = typeProvider.getRMetaAnnotatedType(sourceObject.getRight());
		if (!typeSystem.isComparable(tl, tr)) {
			error(notComparableMessage(tl, tr), sourceObject, null);
			return false;
		}
		return true;
	}
	
	protected boolean isMultiCheck(RosettaExpression expr, EObject sourceObject, EStructuralFeature feature) {
		return isMultiCheck(expr, sourceObject, feature, INSIGNIFICANT_INDEX);
	}
	protected boolean isMultiCheck(RosettaExpression expr, EObject sourceObject, EStructuralFeature feature, int featureIndex) {
		if (!cardinalityProvider.isMulti(expr)) {
			error("Expecting multi cardinality", sourceObject, feature, featureIndex);
			return false;
		}
		return true;
	}
	protected boolean isSingleCheck(RosettaExpression expr, EObject sourceObject, EStructuralFeature feature) {
		return isSingleCheck(expr, sourceObject, feature, INSIGNIFICANT_INDEX);
	}
	protected boolean isSingleCheck(RosettaExpression expr, EObject sourceObject, EStructuralFeature feature, int featureIndex) {
		if (cardinalityProvider.isMulti(expr)) {
			error("Expecting single cardinality", sourceObject, feature, featureIndex);
			return false;
		}
		return true;
	}
	
	protected boolean commonTypeCheck(List<RosettaExpression> expressions, EObject sourceObject, EStructuralFeature feature) {
		boolean haveCommonType = true;
		if (!expressions.isEmpty()) {
			Set<RMetaAnnotatedType> types = new LinkedHashSet<>();
			RMetaAnnotatedType firstElemType = typeProvider.getRMetaAnnotatedType(expressions.get(0));
			types.add(firstElemType);
			RMetaAnnotatedType commonType = firstElemType;
			for (int i=1; i<expressions.size(); i++) {
				RMetaAnnotatedType elemType = typeProvider.getRMetaAnnotatedType(expressions.get(i));
				RMetaAnnotatedType newCommonType = typeSystem.joinMetaAnnotatedTypes(commonType, elemType);
				if (typeSystem.isSubtypeOf(builtins.ANY_WITH_NO_META, newCommonType)) {
					error(
							"Types " + types.stream().map(t -> "`" + relevantTypeDescription(t, elemType) + "`").collect(Collectors.joining(", ")) + " and `" + relevantTypeDescription(elemType, newCommonType) + "` do not have a common supertype",
							sourceObject,
							feature,
							feature == null || !feature.isMany() ? INSIGNIFICANT_INDEX : i);
					haveCommonType = false;
				} else {
					types.add(elemType);
					commonType = newCommonType;
				}
			}
		}
		return haveCommonType;
	}
	
	protected void unsupportedTypeError(RMetaAnnotatedType type, RosettaOperation op, EStructuralFeature feature, RType supportedType1, RType supportedType2, RType... moreSupportedTypes) {
		StringBuilder supportedTypesMsg = new StringBuilder();
		supportedTypesMsg.append("Supported types are ");
		supportedTypesMsg.append(supportedType1);
		if (moreSupportedTypes.length > 0) {
			supportedTypesMsg.append(", ");
			supportedTypesMsg.append(supportedType2);
			for (int i=0; i<moreSupportedTypes.length-1; i++) {
				supportedTypesMsg.append(", ");
				supportedTypesMsg.append(moreSupportedTypes[i]);
			}
			supportedTypesMsg.append(" and ");
			supportedTypesMsg.append(moreSupportedTypes[moreSupportedTypes.length-1]);
		} else {
			supportedTypesMsg.append(" and ");
			supportedTypesMsg.append(supportedType2);
		}
		unsupportedTypeError(type, op.getOperator(), op, feature, supportedTypesMsg.toString());
	}
	protected void unsupportedTypeError(RMetaAnnotatedType type, String operator, EObject sourceObject, EStructuralFeature feature, String supportedTypesMessage) {
		error("Operator `" + operator + "` is not supported for type " + type.getRType() + ". " + supportedTypesMessage, sourceObject, feature);
	}
}
