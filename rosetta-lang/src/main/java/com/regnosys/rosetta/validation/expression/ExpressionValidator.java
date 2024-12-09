package com.regnosys.rosetta.validation.expression;

import javax.inject.Inject;

import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.ComposedChecks;

import com.google.common.collect.Iterables;
import com.regnosys.rosetta.RosettaEcoreUtil;
import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions;
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs;
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
import com.regnosys.rosetta.rosetta.expression.RosettaConditionalExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaContainsExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaDisjointExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaExistsExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaFeatureCall;
import com.regnosys.rosetta.rosetta.expression.RosettaOnlyElement;
import com.regnosys.rosetta.rosetta.expression.RosettaOnlyExistsExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.types.RChoiceType;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.RMetaAnnotatedType;
import com.regnosys.rosetta.types.RType;
import com.regnosys.rosetta.utils.ExpressionHelper;
import com.regnosys.rosetta.utils.ImplicitVariableUtil;

import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

// TODO: move over expression validations from RosettaSimpleValidator
@ComposedChecks(validators = { SwitchValidator.class })
public class ExpressionValidator extends AbstractExpressionValidator {
	@Inject
	private ExpressionHelper exprHelper;
	@Inject
	private ImplicitVariableUtil implicitVarUtil;
	@Inject
	private RosettaEcoreUtil ecoreUtil;
	@Inject
	private RosettaFunctionExtensions functionExtensions;
	
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
		
		Set<Attribute> seen = new HashSet<>();
		for (var i = 0; i < op.getAttributes().size(); i++) {
			Attribute attr = op.getAttributes().get(i);
			if (!seen.add(attr)) {
				error("Duplicate attribute.", op, CHOICE_OPERATION__ATTRIBUTES, i);
			}
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
	public void checkOnlyElement(RosettaOnlyElement e) {
		// TODO: restore
//		RListType t = ts.inferType(e.getArgument());
//		if (t != null) {
//			RosettaCardinality minimalConstraint = tf.createConstraint(1, 2);
//			if (!minimalConstraint.isSubconstraintOf(t.getConstraint())) {
//				warning(tu.notLooserConstraintMessage(minimalConstraint, t), e, ROSETTA_UNARY_OPERATION__ARGUMENT);
//			}
//		}
	}
}
