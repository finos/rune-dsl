package com.regnosys.rosetta.validation.expression;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

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
		String prepend = valueType.getName().equals(valueContext.getName()) ? valueType.getNamespace() + "." : "";
		if (valueType.equals(valueContext)) {
			// Include meta info
			return prepend + type.toString();
		}
		if (valueType.getName().equals(valueContext.getName())) {
			// Include type parameters
			return prepend + valueType.toString();
		}
		return prepend + valueType.getName();
	}
	
	protected String notASubtypeMessage(RMetaAnnotatedType expected, RMetaAnnotatedType actual, Function<String, String> suggestion) {
		String actualDescr = relevantTypeDescription(actual, expected);
		StringBuilder msg = new StringBuilder()
				.append("Expected type `")
				.append(relevantTypeDescription(expected, actual))
				.append("`, but got `")
				.append(actualDescr)
				.append("` instead");
		if (suggestion != null) {
			msg.append(". ");
			msg.append(suggestion.apply(actualDescr));
		}
		return msg.toString();
	}
	private Function<String, String> defaultSubtypeSuggestion(RosettaOperation op) {
		return actual -> "Cannot use `" + actual + "` with operator `" + op.getOperator() + "`";
	}
	protected boolean subtypeCheck(RMetaAnnotatedType expected, RosettaExpression expr, EObject sourceObject, EStructuralFeature feature, RosettaOperation op) {
		return subtypeCheck(expected, typeProvider.getRMetaAnnotatedType(expr), sourceObject, feature, INSIGNIFICANT_INDEX, defaultSubtypeSuggestion(op));
	}
	protected boolean subtypeCheck(RMetaAnnotatedType expected, RosettaExpression expr, EObject sourceObject, EStructuralFeature feature, Function<String, String> suggestion) {
		return subtypeCheck(expected, typeProvider.getRMetaAnnotatedType(expr), sourceObject, feature, INSIGNIFICANT_INDEX, suggestion);
	}
	protected boolean subtypeCheck(RMetaAnnotatedType expected, RosettaExpression expr, EObject sourceObject, EStructuralFeature feature, int featureIndex, Function<String, String> suggestion) {
		return subtypeCheck(expected, typeProvider.getRMetaAnnotatedType(expr), sourceObject, feature, featureIndex, suggestion);
	}
	protected boolean subtypeCheck(RMetaAnnotatedType expected, RMetaAnnotatedType actual, EObject sourceObject, EStructuralFeature feature, RosettaOperation op) {
		return subtypeCheck(expected, actual, sourceObject, feature, INSIGNIFICANT_INDEX, defaultSubtypeSuggestion(op));
	}
	protected boolean subtypeCheck(RMetaAnnotatedType expected, RMetaAnnotatedType actual, EObject sourceObject, EStructuralFeature feature, Function<String, String> suggestion) {
		return subtypeCheck(expected, actual, sourceObject, feature, INSIGNIFICANT_INDEX, suggestion);
	}
	protected boolean subtypeCheck(RMetaAnnotatedType expected, RMetaAnnotatedType actual, EObject sourceObject, EStructuralFeature feature, int featureIndex, Function<String, String> suggestion) {
		if (!builtins.NOTHING.equals(expected.getRType()) && !typeSystem.isSubtypeOf(actual, expected)) {
			error(notASubtypeMessage(expected, actual, suggestion), sourceObject, feature, featureIndex);
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
	
	protected boolean isMultiCheck(RosettaExpression expr, EObject sourceObject, EStructuralFeature feature, RosettaOperation op) {
		String suggestion = "The `" + op.getOperator() + "` operator requires a multi cardinality input";
		return isMultiCheck(expr, sourceObject, feature, INSIGNIFICANT_INDEX, suggestion);
	}
	protected boolean isMultiCheck(RosettaExpression expr, EObject sourceObject, EStructuralFeature feature, String suggestion) {
		return isMultiCheck(expr, sourceObject, feature, INSIGNIFICANT_INDEX, suggestion);
	}
	protected boolean isMultiCheck(RosettaExpression expr, EObject sourceObject, EStructuralFeature feature, int featureIndex, String suggestion) {
		if (!cardinalityProvider.isMulti(expr)) {
			String msg = "Expecting multi cardinality";
			if (suggestion != null) {
				msg += ". " + suggestion;
			}
			warning(msg, sourceObject, feature, featureIndex);
			return false;
		}
		return true;
	}
	protected boolean isSingleCheck(RosettaExpression expr, EObject sourceObject, EStructuralFeature feature, RosettaOperation op) {
		String suggestion = "The `" + op.getOperator() + "` operator requires a single cardinality input";
		return isSingleCheck(expr, sourceObject, feature, INSIGNIFICANT_INDEX, suggestion);
	}
	protected boolean isSingleCheck(RosettaExpression expr, EObject sourceObject, EStructuralFeature feature, String suggestion) {
		return isSingleCheck(expr, sourceObject, feature, INSIGNIFICANT_INDEX, suggestion);
	}

	protected boolean isSingleCheckError(RosettaExpression expr, EObject sourceObject, EStructuralFeature feature, String suggestion) {
		return isSingleCheckError(expr, sourceObject, feature, INSIGNIFICANT_INDEX, suggestion);
	}

	protected boolean isSingleCheck(RosettaExpression expr, EObject sourceObject, EStructuralFeature feature, int featureIndex, String suggestion) {
		if (cardinalityProvider.isMulti(expr)) {
			String msg = "Expecting single cardinality";
			if (suggestion != null) {
				msg += ". " + suggestion;
			}
			warning(msg, sourceObject, feature, featureIndex);
			return false;
		}
		return true;
	}

	protected boolean isSingleCheckError(RosettaExpression expr, EObject sourceObject, EStructuralFeature feature, int featureIndex, String suggestion) {
		if (cardinalityProvider.isMulti(expr)) {
			String msg = "Expecting single cardinality";
			if (suggestion != null) {
				msg += ". " + suggestion;
			}
			error(msg, sourceObject, feature, featureIndex);
			return false;
		}
		return true;
	}
	
	protected boolean commonTypeCheck(RosettaExpression expr1, RosettaExpression expr2, EObject sourceObject, EStructuralFeature feature) {
		if (expr1 == null || expr2 == null) {
			return true;
		}
		return commonTypeCheck(List.of(expr1, expr2), sourceObject, feature);
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
		supportedTypesMsg.append("Supported types are `");
		supportedTypesMsg.append(supportedType1);
		if (moreSupportedTypes.length > 0) {
			supportedTypesMsg.append("`, `");
			supportedTypesMsg.append(supportedType2);
			for (int i=0; i<moreSupportedTypes.length-1; i++) {
				supportedTypesMsg.append("`, `");
				supportedTypesMsg.append(moreSupportedTypes[i]);
			}
			supportedTypesMsg.append("` and `");
			supportedTypesMsg.append(moreSupportedTypes[moreSupportedTypes.length-1]);
		} else {
			supportedTypesMsg.append("` and `");
			supportedTypesMsg.append(supportedType2);
		}
		supportedTypesMsg.append('`');
		unsupportedTypeError(type, op.getOperator(), op, feature, supportedTypesMsg.toString());
	}
	protected void unsupportedTypeError(RMetaAnnotatedType type, String operator, EObject sourceObject, EStructuralFeature feature, String supportedTypesMessage) {
		error("Operator `" + operator + "` is not supported for type `" + type.getRType().getName() + "`. " + supportedTypesMessage, sourceObject, feature);
	}
}
