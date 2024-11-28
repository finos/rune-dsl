package com.regnosys.rosetta.validation;

import javax.inject.Inject;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.validation.Check;

import com.google.common.collect.Iterables;
import com.regnosys.rosetta.RosettaEcoreUtil;
import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions;
import com.regnosys.rosetta.interpreter.RosettaInterpreter;
import com.regnosys.rosetta.interpreter.RosettaValue;
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs;
import com.regnosys.rosetta.rosetta.RosettaEnumValue;
import com.regnosys.rosetta.rosetta.RosettaExternalFunction;
import com.regnosys.rosetta.rosetta.RosettaFeature;
import com.regnosys.rosetta.rosetta.RosettaMetaType;
import com.regnosys.rosetta.rosetta.RosettaNamed;
import com.regnosys.rosetta.rosetta.RosettaRule;
import com.regnosys.rosetta.rosetta.RosettaSymbol;
import com.regnosys.rosetta.rosetta.expression.ArithmeticOperation;
import com.regnosys.rosetta.rosetta.expression.CardinalityModifier;
import com.regnosys.rosetta.rosetta.expression.ChoiceOperation;
import com.regnosys.rosetta.rosetta.expression.ComparisonOperation;
import com.regnosys.rosetta.rosetta.expression.DefaultOperation;
import com.regnosys.rosetta.rosetta.expression.EqualityOperation;
import com.regnosys.rosetta.rosetta.expression.ExistsModifier;
import com.regnosys.rosetta.rosetta.expression.JoinOperation;
import com.regnosys.rosetta.rosetta.expression.ListLiteral;
import com.regnosys.rosetta.rosetta.expression.LogicalOperation;
import com.regnosys.rosetta.rosetta.expression.OneOfOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaBinaryOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaConditionalExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaContainsExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaDisjointExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaExistsExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaFeatureCall;
import com.regnosys.rosetta.rosetta.expression.RosettaLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaOnlyExistsExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference;
import com.regnosys.rosetta.rosetta.expression.SwitchCase;
import com.regnosys.rosetta.rosetta.expression.SwitchOperation;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.ChoiceOption;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.types.CardinalityProvider;
import com.regnosys.rosetta.types.RChoiceType;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.REnumType;
import com.regnosys.rosetta.types.RMetaAnnotatedType;
import com.regnosys.rosetta.types.RParametrizedType;
import com.regnosys.rosetta.types.RType;
import com.regnosys.rosetta.types.RosettaTypeProvider;
import com.regnosys.rosetta.types.TypeSystem;
import com.regnosys.rosetta.types.builtin.RBasicType;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;
import com.regnosys.rosetta.utils.ExpressionHelper;
import com.regnosys.rosetta.utils.ImplicitVariableUtil;

import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// TODO: move over expression validations from RosettaSimpleValidator
public class ExpressionValidator  extends AbstractDeclarativeRosettaValidator {
	@Inject
	private RosettaTypeProvider typeProvider;
	@Inject
	private TypeSystem typeSystem;
	@Inject
	private RBuiltinTypeService builtins;
	@Inject
	private CardinalityProvider cardinalityProvider;
	@Inject
	private ExpressionHelper exprHelper;
	@Inject
	private ImplicitVariableUtil implicitVarUtil;
	@Inject
	private RosettaInterpreter interpreter;
	@Inject
	private RosettaEcoreUtil ecoreUtil;
	@Inject
	private RosettaFunctionExtensions functionExtensions;
	
	private String relevantTypeDescription(RMetaAnnotatedType type, RMetaAnnotatedType context) {
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
	
	private String notASubtypeMessage(RMetaAnnotatedType expected, RMetaAnnotatedType actual) {
		return new StringBuilder()
				.append("Expected type `")
				.append(relevantTypeDescription(expected, actual))
				.append("`, but got `")
				.append(relevantTypeDescription(actual, expected))
				.append("` instead")
				.toString();
	}
	private boolean subtypeCheck(RMetaAnnotatedType expected, RosettaExpression expr, EObject sourceObject, EStructuralFeature feature) {
		return subtypeCheck(expected, typeProvider.getRMetaAnnotatedType(expr), sourceObject, feature, INSIGNIFICANT_INDEX);
	}
	private boolean subtypeCheck(RMetaAnnotatedType expected, RosettaExpression expr, EObject sourceObject, EStructuralFeature feature, int featureIndex) {
		return subtypeCheck(expected, typeProvider.getRMetaAnnotatedType(expr), sourceObject, feature, featureIndex);
	}
	private boolean subtypeCheck(RMetaAnnotatedType expected, RMetaAnnotatedType actual, EObject sourceObject, EStructuralFeature feature) {
		return subtypeCheck(expected, actual, sourceObject, feature, INSIGNIFICANT_INDEX);
	}
	private boolean subtypeCheck(RMetaAnnotatedType expected, RMetaAnnotatedType actual, EObject sourceObject, EStructuralFeature feature, int featureIndex) {
		if (!typeSystem.isSubtypeOf(actual, expected)) {
			error(notASubtypeMessage(expected, actual), sourceObject, feature, featureIndex);
			return false;
		}
		return true;
	}
	
	private String notComparableMessage(RMetaAnnotatedType left, RMetaAnnotatedType right) {
		return new StringBuilder()
				.append("Types `")
				.append(relevantTypeDescription(left, right))
				.append("` and `")
				.append(relevantTypeDescription(right, left))
				.append("` are not comparable")
				.toString();
	}
	private boolean comparableTypeCheck(RosettaBinaryOperation sourceObject) {
		RMetaAnnotatedType tl = typeProvider.getRMetaAnnotatedType(sourceObject.getLeft());
		RMetaAnnotatedType tr = typeProvider.getRMetaAnnotatedType(sourceObject.getRight());
		if (!typeSystem.isComparable(tl, tr)) {
			error(notComparableMessage(tl, tr), sourceObject, null);
			return false;
		}
		return true;
	}
	
	private boolean isMultiCheck(RosettaExpression expr, EObject sourceObject, EStructuralFeature feature) {
		return isMultiCheck(expr, sourceObject, feature, INSIGNIFICANT_INDEX);
	}
	private boolean isMultiCheck(RosettaExpression expr, EObject sourceObject, EStructuralFeature feature, int featureIndex) {
		if (!cardinalityProvider.isMulti(expr)) {
			error("Expecting multi cardinality", sourceObject, feature, featureIndex);
			return false;
		}
		return true;
	}
	private boolean isSingleCheck(RosettaExpression expr, EObject sourceObject, EStructuralFeature feature) {
		return isSingleCheck(expr, sourceObject, feature, INSIGNIFICANT_INDEX);
	}
	private boolean isSingleCheck(RosettaExpression expr, EObject sourceObject, EStructuralFeature feature, int featureIndex) {
		if (cardinalityProvider.isMulti(expr)) {
			error("Expecting single cardinality", sourceObject, feature, featureIndex);
			return false;
		}
		return true;
	}
	
	private boolean commonTypeCheck(List<RosettaExpression> expressions, EObject sourceObject, EStructuralFeature feature) {
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
	
	private void unsupportedTypeError(RMetaAnnotatedType type, RosettaOperation op, EStructuralFeature feature, RType supportedType1, RType supportedType2, RType... moreSupportedTypes) {
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
	private void unsupportedTypeError(RMetaAnnotatedType type, String operator, EObject sourceObject, EStructuralFeature feature, String supportedTypesMessage) {
		error("Operator `" + operator + "` is not supported for type " + type.getRType() + ". " + supportedTypesMessage, sourceObject, feature);
	}
	
	@Check
	public void checkArithmeticOperation(ArithmeticOperation op) {
		RosettaExpression left = op.getLeft();
		RosettaExpression right = op.getRight();
		String operator = op.getOperator();
		RMetaAnnotatedType leftType = typeProvider.getRMetaAnnotatedType(left);
		RMetaAnnotatedType rightType = typeProvider.getRMetaAnnotatedType(right);
		isSingleCheck(left, op, ROSETTA_BINARY_OPERATION__LEFT);
		isSingleCheck(right, op, ROSETTA_BINARY_OPERATION__RIGHT);
		if (operator.equals("+")) {
			if (typeSystem.isSubtypeOf(leftType, builtins.NOTHING_WITH_NO_META)) {
				// Do not check right type
			} else if (typeSystem.isSubtypeOf(leftType, builtins.DATE_WITH_NO_META)) {
				subtypeCheck(builtins.TIME_WITH_NO_META, rightType, op, ROSETTA_BINARY_OPERATION__RIGHT);
			} else if (typeSystem.isSubtypeOf(leftType, builtins.UNCONSTRAINED_STRING_WITH_NO_META)) {
				subtypeCheck(builtins.UNCONSTRAINED_STRING_WITH_NO_META, rightType, op, ROSETTA_BINARY_OPERATION__RIGHT);
			} else if (typeSystem.isSubtypeOf(leftType, builtins.UNCONSTRAINED_NUMBER_WITH_NO_META)) {
				subtypeCheck(builtins.UNCONSTRAINED_NUMBER_WITH_NO_META, rightType, op, ROSETTA_BINARY_OPERATION__RIGHT);
			} else {
				unsupportedTypeError(leftType, op, ROSETTA_BINARY_OPERATION__LEFT, builtins.UNCONSTRAINED_NUMBER, builtins.UNCONSTRAINED_STRING, builtins.DATE);
				if (!typeSystem.isSubtypeOf(rightType, builtins.TIME_WITH_NO_META) 
						&& !typeSystem.isSubtypeOf(rightType, builtins.UNCONSTRAINED_STRING_WITH_NO_META)
						&& !typeSystem.isSubtypeOf(rightType, builtins.UNCONSTRAINED_NUMBER_WITH_NO_META)) {
					unsupportedTypeError(rightType, op, ROSETTA_BINARY_OPERATION__RIGHT, builtins.UNCONSTRAINED_NUMBER, builtins.UNCONSTRAINED_STRING, builtins.TIME);
				}
			}
		} else if (operator.equals("-")) {
			if (typeSystem.isSubtypeOf(leftType, builtins.NOTHING_WITH_NO_META)) {
				// Do not check right type
			} else if (typeSystem.isSubtypeOf(leftType, builtins.DATE_WITH_NO_META)) {
				subtypeCheck(builtins.DATE_WITH_NO_META, rightType, op, ROSETTA_BINARY_OPERATION__RIGHT);
			} else if (typeSystem.isSubtypeOf(leftType, builtins.UNCONSTRAINED_NUMBER_WITH_NO_META)) {
				subtypeCheck(builtins.UNCONSTRAINED_NUMBER_WITH_NO_META, rightType, op, ROSETTA_BINARY_OPERATION__RIGHT);
			} else {
				unsupportedTypeError(leftType, op, ROSETTA_BINARY_OPERATION__LEFT, builtins.UNCONSTRAINED_NUMBER, builtins.DATE);
				if (!typeSystem.isSubtypeOf(rightType, builtins.DATE_WITH_NO_META) 
						&& !typeSystem.isSubtypeOf(rightType, builtins.UNCONSTRAINED_NUMBER_WITH_NO_META)) {
					unsupportedTypeError(rightType, op, ROSETTA_BINARY_OPERATION__RIGHT, builtins.UNCONSTRAINED_NUMBER, builtins.DATE);
				}
			}
		} else {
			subtypeCheck(builtins.UNCONSTRAINED_NUMBER_WITH_NO_META, leftType, op, ROSETTA_BINARY_OPERATION__LEFT);
			subtypeCheck(builtins.UNCONSTRAINED_NUMBER_WITH_NO_META, rightType, op, ROSETTA_BINARY_OPERATION__RIGHT);
		}
	}
	
	@Check
	public void checkEqualityOperation(EqualityOperation op) {
		RosettaExpression left = op.getLeft();
		RosettaExpression right = op.getRight();
		comparableTypeCheck(op);
		if (op.getCardMod() != CardinalityModifier.NONE) {
			isMultiCheck(left, op, ROSETTA_BINARY_OPERATION__LEFT);
			isSingleCheck(right, op, ROSETTA_BINARY_OPERATION__RIGHT);
		} else {
			boolean leftIsMulti = cardinalityProvider.isMulti(op.getLeft());
			boolean rightIsMulti = cardinalityProvider.isMulti(op.getRight());
			if (leftIsMulti != rightIsMulti) {
				error("Operator `" + op.getOperator() + "` should specify 'all' or 'any' when comparing a list to a single value", op, null);
			}
		}
	}
	
	@Check
	public void checkLogicalOperation(LogicalOperation op) {
		RosettaExpression left = op.getLeft();
		RosettaExpression right = op.getRight();
		RMetaAnnotatedType leftType = typeProvider.getRMetaAnnotatedType(left);
		RMetaAnnotatedType rightType = typeProvider.getRMetaAnnotatedType(right);
		isSingleCheck(left, op, ROSETTA_BINARY_OPERATION__LEFT);
		isSingleCheck(right, op, ROSETTA_BINARY_OPERATION__RIGHT);
		subtypeCheck(builtins.BOOLEAN_WITH_NO_META, leftType, op, ROSETTA_BINARY_OPERATION__LEFT);
		subtypeCheck(builtins.BOOLEAN_WITH_NO_META, rightType, op, ROSETTA_BINARY_OPERATION__RIGHT);
	}
	
	@Check
	public void checkComparisonOperation(ComparisonOperation op) {
		RosettaExpression left = op.getLeft();
		RosettaExpression right = op.getRight();
		RMetaAnnotatedType leftType = typeProvider.getRMetaAnnotatedType(left);
		RMetaAnnotatedType rightType = typeProvider.getRMetaAnnotatedType(right);
		if (op.getCardMod() != CardinalityModifier.NONE) {
			isMultiCheck(left, op, ROSETTA_BINARY_OPERATION__LEFT);
			isSingleCheck(right, op, ROSETTA_BINARY_OPERATION__RIGHT);
		} else {
			isSingleCheck(left, op, ROSETTA_BINARY_OPERATION__LEFT);
			isSingleCheck(right, op, ROSETTA_BINARY_OPERATION__RIGHT);
		}
		if (typeSystem.isSubtypeOf(leftType, builtins.NOTHING_WITH_NO_META)) {
			// Do not check right type
		} else if (typeSystem.isSubtypeOf(leftType, builtins.ZONED_DATE_TIME_WITH_NO_META)) {
			subtypeCheck(builtins.ZONED_DATE_TIME_WITH_NO_META, rightType, op, ROSETTA_BINARY_OPERATION__RIGHT);
		} else if (typeSystem.isSubtypeOf(leftType, builtins.DATE_WITH_NO_META)) {
			subtypeCheck(builtins.DATE_WITH_NO_META, rightType, op, ROSETTA_BINARY_OPERATION__RIGHT);
		} else if (typeSystem.isSubtypeOf(leftType, builtins.UNCONSTRAINED_NUMBER_WITH_NO_META)) {
			subtypeCheck(builtins.UNCONSTRAINED_NUMBER_WITH_NO_META, rightType, op, ROSETTA_BINARY_OPERATION__RIGHT);
		} else {
			unsupportedTypeError(leftType, op, ROSETTA_BINARY_OPERATION__LEFT, builtins.UNCONSTRAINED_NUMBER, builtins.DATE, builtins.ZONED_DATE_TIME);
			if (!typeSystem.isSubtypeOf(rightType, builtins.ZONED_DATE_TIME_WITH_NO_META) 
					&& !typeSystem.isSubtypeOf(rightType, builtins.DATE_WITH_NO_META)
					&& !typeSystem.isSubtypeOf(rightType, builtins.UNCONSTRAINED_NUMBER_WITH_NO_META)) {
				unsupportedTypeError(rightType, op, ROSETTA_BINARY_OPERATION__RIGHT, builtins.UNCONSTRAINED_NUMBER, builtins.DATE, builtins.ZONED_DATE_TIME);
			}
		}
	}
	
	@Check
	public void checkContainsExpression(RosettaContainsExpression expr) {
		isMultiCheck(expr.getLeft(), expr, ROSETTA_BINARY_OPERATION__LEFT);
		comparableTypeCheck(expr);
	}
	
	@Check
	public void checkDisjointExpression(RosettaDisjointExpression expr) {
		isMultiCheck(expr.getLeft(), expr, ROSETTA_BINARY_OPERATION__LEFT);
		comparableTypeCheck(expr);
	}
	
	@Check
	public void checkConditionalExpression(RosettaConditionalExpression expr) {
		isSingleCheck(expr.getIf(), expr, ROSETTA_CONDITIONAL_EXPRESSION__IF);
		subtypeCheck(builtins.BOOLEAN_WITH_NO_META, expr.getIf(), expr, ROSETTA_CONDITIONAL_EXPRESSION__IF);
		commonTypeCheck(List.of(expr.getIfthen(), expr.getElsethen()), expr, ROSETTA_CONDITIONAL_EXPRESSION__ELSETHEN);
	}
	
	@Check
	public void checkListLiteral(ListLiteral expr) {
		commonTypeCheck(expr.getElements(), expr, LIST_LITERAL__ELEMENTS);
	}
	
	@Check
	public void checkDefaultOperation(DefaultOperation op) {
		commonTypeCheck(List.of(op.getLeft(), op.getRight()), op, ROSETTA_BINARY_OPERATION__RIGHT);
	}
	
	@Check
	public void checkSymbolReference(RosettaSymbolReference expr) {
		RosettaSymbol s = expr.getSymbol();
		if (ecoreUtil.isResolved(s)) {
			if (s instanceof RosettaCallableWithArgs) {
				RosettaCallableWithArgs callable = (RosettaCallableWithArgs) s;
				int paramCount = callable.numberOfParameters();
				int argCount = expr.getArgs().size();
				if (paramCount != argCount) {
					error("Expected " + paramCount + " argument" + (paramCount == 1 ? "" : "s") + ", but got " + argCount + " instead", expr, ROSETTA_SYMBOL_REFERENCE__SYMBOL);
				}
				int minCount = Math.min(paramCount, argCount);
				
				if (callable instanceof RosettaExternalFunction) {
					RosettaExternalFunction f = (RosettaExternalFunction) callable;
					for (int i=0; i<minCount; i++) {
						RMetaAnnotatedType paramType = typeProvider.getRTypeOfSymbol(f.getParameters().get(i), null);
						RosettaExpression arg = expr.getArgs().get(i);
						isSingleCheck(arg, expr, ROSETTA_SYMBOL_REFERENCE__RAW_ARGS, i);
						subtypeCheck(paramType, arg, expr, ROSETTA_SYMBOL_REFERENCE__RAW_ARGS, i);
					}
				} else if (callable instanceof Function) {
					Function f = (Function) callable;
					for (int i=0; i<minCount; i++) {
						RMetaAnnotatedType paramType = typeProvider.getRTypeOfSymbol(f.getInputs().get(i), null);
						RosettaExpression arg = expr.getArgs().get(i);
						if (!cardinalityProvider.isSymbolMulti(f.getInputs().get(i))) {
							isSingleCheck(arg, expr, ROSETTA_SYMBOL_REFERENCE__RAW_ARGS, i);
						}
						subtypeCheck(paramType, arg, expr, ROSETTA_SYMBOL_REFERENCE__RAW_ARGS, i);
					}
				} else if (callable instanceof RosettaRule) {
					RosettaRule f = (RosettaRule) callable;
					if (minCount >= 1) {
						RMetaAnnotatedType paramType = RMetaAnnotatedType.withEmptyMeta(typeSystem.typeCallToRType(f.getInput()));
						RosettaExpression arg = expr.getArgs().get(0);
						isSingleCheck(arg, expr, ROSETTA_SYMBOL_REFERENCE__RAW_ARGS, 0);
						subtypeCheck(paramType, arg, expr, ROSETTA_SYMBOL_REFERENCE__RAW_ARGS, 0);
					}
				}
			} else {
				if (s instanceof Attribute) {
					if (functionExtensions.isOutput((Attribute) s)) {
						RMetaAnnotatedType implicitType = typeProvider.typeOfImplicitVariable(expr);
						Iterable<? extends RosettaFeature> implicitFeatures = ecoreUtil.allFeatures(implicitType, expr);
						if (Iterables.any(implicitFeatures, f -> f.getName().equals(s.getName()))) {
							error(
								"Ambiguous reference. `" + s.getName() + "` may either refer to `item -> " + s.getName() + "` or to the output variable.",
								expr,
								ROSETTA_SYMBOL_REFERENCE__SYMBOL
							);
						}
					}
				}
				if (expr.isExplicitArguments()) {
					error(
						"A variable may not be called",
						expr,
						ROSETTA_SYMBOL_REFERENCE__EXPLICIT_ARGUMENTS
					);
				}
			}
		}
	}
	
	@Check
	public void checkExistsExpression(RosettaExistsExpression expr) {
		if (expr.getModifier() == ExistsModifier.MULTIPLE || expr.getModifier() == ExistsModifier.SINGLE) {
			isMultiCheck(expr.getArgument(), expr, ROSETTA_UNARY_OPERATION__ARGUMENT);
		}
	}
	
	private boolean mayBeEmpty(RType t) {
		return t instanceof RDataType && ((RDataType) t).getAllAttributes().stream().allMatch(a -> a.getCardinality().getMinBound() == 0) || t instanceof RChoiceType;
	}
	@Check
	public void checkOnlyExistsExpression(RosettaOnlyExistsExpression expr) {
		if (expr.getArgs().size() > 0) {
			for (RosettaExpression input : expr.getArgs()) {
				RosettaNamed invalidMetaFeature = null;
				EStructuralFeature structFeature = null;
				if (input instanceof RosettaFeatureCall && ((RosettaFeatureCall)input).getFeature() instanceof RosettaMetaType) {
					invalidMetaFeature = ((RosettaFeatureCall)input).getFeature();
					structFeature = ROSETTA_FEATURE_CALL__FEATURE;
				} else if (input instanceof RosettaSymbolReference && ((RosettaSymbolReference)input).getSymbol() instanceof RosettaMetaType) {
					invalidMetaFeature = ((RosettaSymbolReference)input).getSymbol();
					structFeature = ROSETTA_SYMBOL_REFERENCE__SYMBOL;
				}
				if (invalidMetaFeature != null) {
					error("Invalid use of `only exists` on meta feature " + invalidMetaFeature.getName(), input, structFeature);
				}
			}
			
			RosettaExpression first = expr.getArgs().get(0);
			RosettaExpression parent = exprHelper.getParentExpression(first);
			for (int i=1; i<expr.getArgs().size(); i++) {
				RosettaExpression other = expr.getArgs().get(i);
				for (int j=0; j<i; j++) {
					if (EcoreUtil2.equals(expr.getArgs().get(j), other)) {
						error("Duplicate attribute", expr, ROSETTA_ONLY_EXISTS_EXPRESSION__ARGS, i);
					}
				}
				RosettaExpression otherParent = exprHelper.getParentExpression(other);
				if ((parent == null) != (otherParent == null) || parent != null && otherParent != null && !EcoreUtil2.equals(parent, otherParent)) {
					if (otherParent != null) {
						error("All parent paths must be equal", otherParent, null);
					} else {
						error("All parent paths must be equal", other, null);
					}
				}
			}
			if (parent == null && !implicitVarUtil.implicitVariableExistsInContext(expr)) {
				error("Object must have a parent object", expr, ROSETTA_ONLY_EXISTS_EXPRESSION__ARGS, 0);
			}
			
			RMetaAnnotatedType parentType = null;
			if (parent != null) {
				isSingleCheck(parent, parent, null);
				parentType = typeProvider.getRMetaAnnotatedType(parent);
			} else {
				if (cardinalityProvider.isImplicitVariableMulti(expr)) {
					error("Expecting single cardinality parent", expr, ROSETTA_ONLY_EXISTS_EXPRESSION__ARGS, 0);
				}
				parentType = typeProvider.typeOfImplicitVariable(expr);
			}
			RType parentData = parentType.getRType();
			if (!mayBeEmpty(parentData)) {
				unsupportedTypeError(parentType, "only exists", first, null, "All attributes of input type should be optional");
			}
		}
	}
	
	@Check
	public void checkOneOfOperation(OneOfOperation op) {
		isSingleCheck(op.getArgument(), op, ROSETTA_UNARY_OPERATION__ARGUMENT);
		RMetaAnnotatedType argType = typeProvider.getRMetaAnnotatedType(op.getArgument());
		if (!mayBeEmpty(argType.getRType())) {
			unsupportedTypeError(argType, op.getOperator(), op, ROSETTA_UNARY_OPERATION__ARGUMENT, "All attributes of input type should be optional");
		}
	}
	
	@Check
	public void checkChoiceOperation(ChoiceOperation op) {
		isSingleCheck(op.getArgument(), op, ROSETTA_UNARY_OPERATION__ARGUMENT);
		RMetaAnnotatedType argType = typeProvider.getRMetaAnnotatedType(op.getArgument());
		if (!(argType.getRType() instanceof RDataType)) {
			unsupportedTypeError(argType, op.getOperator(), op, ROSETTA_UNARY_OPERATION__ARGUMENT, "Input should be a complex type");
		}
		if (op.getAttributes().size() < 2) {
			error("At least two attributes must be passed to a choice rule", op, CHOICE_OPERATION__ATTRIBUTES);
		}
	}
	
	@Check
	public void checkJoinOperation(JoinOperation op) {
		isMultiCheck(op.getLeft(), op, ROSETTA_BINARY_OPERATION__LEFT);
		isSingleCheck(op.getRight(), op, ROSETTA_BINARY_OPERATION__RIGHT);
		subtypeCheck(builtins.UNCONSTRAINED_STRING_WITH_NO_META, op.getLeft(), op, ROSETTA_BINARY_OPERATION__LEFT);
		subtypeCheck(builtins.UNCONSTRAINED_STRING_WITH_NO_META, op.getRight(), op, ROSETTA_BINARY_OPERATION__RIGHT);
	}
	
	@Check
	public void checkSwitch(SwitchOperation op) {
		isSingleCheck(op.getArgument(), op, ROSETTA_UNARY_OPERATION__ARGUMENT);
		RMetaAnnotatedType argumentType = typeProvider.getRMetaAnnotatedType(op.getArgument());
		RType rType = typeSystem.stripFromTypeAliases(argumentType.getRType());
		if (rType instanceof REnumType) {
			checkEnumSwitch((REnumType) rType, op);
		} else if (rType instanceof RBasicType) {
			checkBasicTypeSwitch((RBasicType) rType, op);
		} else if (rType instanceof RChoiceType) {
			checkChoiceSwitch((RChoiceType) rType, op);
		} else {
			unsupportedTypeError(argumentType, op.getOperator(), op, ROSETTA_UNARY_OPERATION__ARGUMENT, "Supported argument types are basic types, enumerations, and choice types");
		}
	}
	private void checkEnumSwitch(REnumType argumentType, SwitchOperation op) {
		// When the argument is an enum:
		// - all guards should be enum guards,
		// - there are no duplicate cases,
		// - all enum values must be covered.
		Set<RosettaEnumValue> seenValues = new HashSet<>();
		for (SwitchCase caseStatement : op.getCases()) {
			RosettaEnumValue guard = caseStatement.getGuard().getEnumGuard();
 			if (guard == null) {
 				error("Case should match an enum value of " + argumentType, caseStatement, SWITCH_CASE__GUARD);
 			} else {
 				if (!seenValues.add(guard)) {
 					error("Duplicate case " + guard.getName(), caseStatement, SWITCH_CASE__GUARD);
 				}
 			}
 		}

		if (op.getDefault() == null) {
			List<RosettaEnumValue> missingEnumValues = new ArrayList<>(argumentType.getAllEnumValues());
			missingEnumValues.removeAll(seenValues);
			if (!missingEnumValues.isEmpty()) {
				String missingValuesMsg = missingEnumValues.stream().map(v -> v.getName()).collect(Collectors.joining(", "));
				error("Missing the following cases: " + missingValuesMsg + ". Either provide all or add a default.", op, ROSETTA_OPERATION__OPERATOR);
			}
		}
	}
	private void checkBasicTypeSwitch(RBasicType argumentType, SwitchOperation op) {
		// When the argument is a basic type:
		// - all guards should be literal guards,
		// - there are no duplicate cases,
		// - all guards should be comparable to the input.
		Set<RosettaValue> seenValues = new HashSet<>();
		RMetaAnnotatedType argumentTypeWithoutMeta = RMetaAnnotatedType.withEmptyMeta(argumentType);
 		for (SwitchCase caseStatement : op.getCases()) {
 			RosettaLiteral guard = caseStatement.getGuard().getLiteralGuard();
 			if (guard == null) {
 				error("Case should match a literal of type " + argumentType, caseStatement, SWITCH_CASE__GUARD);
 			} else {
 				if (!seenValues.add(interpreter.interpret(guard))) {
 					error("Duplicate case", caseStatement, SWITCH_CASE__GUARD);
 				}
 				RMetaAnnotatedType conditionType = typeProvider.getRMetaAnnotatedType(guard);
	 			if (!typeSystem.isComparable(conditionType, argumentTypeWithoutMeta)) {
 					error("Invalid case: " + notComparableMessage(conditionType, argumentTypeWithoutMeta), caseStatement, SWITCH_CASE__GUARD);
 				}
 			}
 		}
	}
	private void checkChoiceSwitch(RChoiceType argumentType, SwitchOperation op) {
		// When the argument is a choice type:
		// - all guards should be choice option guards,
		// - all cases should be reachable,
		// - all choice options should be covered.
		Map<ChoiceOption, RMetaAnnotatedType> includedOptions = new HashMap<>();
		for (SwitchCase caseStatement : op.getCases()) {
			ChoiceOption guard = caseStatement.getGuard().getChoiceOptionGuard();
 			if (guard == null) {
 				error("Case should match a choice option of type " + argumentType, caseStatement, SWITCH_CASE__GUARD);
 			} else {
 				RMetaAnnotatedType alreadyCovered = includedOptions.get(guard);
 				if (alreadyCovered != null) {
 					error("Case already covered by " + alreadyCovered, caseStatement, SWITCH_CASE__GUARD);
 				} else {
 					RMetaAnnotatedType guardType = typeProvider.getRTypeOfSymbol(guard);
 					includedOptions.put(guard, guardType);
 					RType valueType = guardType.getRType();
 					if (valueType instanceof RChoiceType) {
 						((RChoiceType)valueType).getAllOptions().forEach(it -> includedOptions.put(it.getEObject(), guardType));
 					}
 				}
 			}
 		}
		if (op.getDefault() == null) {
 			List<RMetaAnnotatedType> missingOptions = new ArrayList<>();
 			argumentType.getOwnOptions().forEach(opt -> missingOptions.add(opt.getType()));
 			for (RMetaAnnotatedType guard : new LinkedHashSet<>(includedOptions.values())) {
 				for (var i=0; i<missingOptions.size(); i++) {
 					RMetaAnnotatedType opt = missingOptions.get(i);
 					RType optValueType = opt.getRType();
 					if (typeSystem.isSubtypeOf(opt, guard, false)) {
 						missingOptions.remove(i);
 						i--;
 					} else if (optValueType instanceof RChoiceType) {
 						if (typeSystem.isSubtypeOf(guard, opt, false)) {
 							missingOptions.remove(i);
 							i--;
 							((RChoiceType)optValueType).getOwnOptions()
 								.forEach(o -> missingOptions.add(o.getType()));
 						}
 					}
 				}
 			}
			if (!missingOptions.isEmpty()) {
				String missingOptsMsg = missingOptions.stream()
						.map(opt -> opt.toString())
						.collect(Collectors.joining(", "));
				error("Missing the following cases: " + missingOptsMsg + ". Either provide all or add a default.", op, ROSETTA_OPERATION__OPERATOR);
			}
		}
	}
}
