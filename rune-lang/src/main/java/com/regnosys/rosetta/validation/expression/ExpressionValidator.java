package com.regnosys.rosetta.validation.expression;

import com.regnosys.rosetta.types.*;
import jakarta.inject.Inject;

import jakarta.inject.Provider;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.ComposedChecks;

import com.google.common.collect.Iterables;
import com.regnosys.rosetta.RosettaEcoreUtil;
import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions;
import com.regnosys.rosetta.rosetta.*;
import com.regnosys.rosetta.rosetta.expression.*;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Condition;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.rosetta.simple.Operation;
import com.regnosys.rosetta.utils.ExpressionHelper;
import com.regnosys.rosetta.utils.ImplicitVariableUtil;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.ROSETTA_NAMED__NAME;
import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.*;
import static com.regnosys.rosetta.rosetta.simple.SimplePackage.Literals.*;
import static com.regnosys.rosetta.types.RMetaAnnotatedType.withNoMeta;

// TODO: move over expression validations from RosettaSimpleValidator
@ComposedChecks(validators = { ConstructorValidator.class, SwitchValidator.class })
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
    public void checkThenOperation(ThenOperation operation) {
        InlineFunction inlineFunction = operation.getFunction();

        /*
         *  Look at all symbol references and check if they reference an attribute.
         *  If those attribute references have a reference to implicit features of the inline function then don't error.
         */
        List<RosettaSymbolReference> symbolReferences = EcoreUtil2.getAllContentsOfType(inlineFunction, RosettaSymbolReference.class)
	        .stream()
            .peek(RosettaSymbolReference::getArgs) //Calling getArgs on the SymbolReference will trigger the lazy population of the implicit argument
	        .filter(symbolReference -> Objects.equals(implicitVarUtil.findContainerDefiningImplicitVariable(symbolReference).orElse(null), operation))
	        .toList();
        
        Set<RosettaFeature> implicitFeatures = StreamSupport.stream(typeProvider.findFeaturesOfImplicitVariable(inlineFunction).spliterator(), false)
                .collect(Collectors.toSet());

        boolean symbolReferencesFeatureOfAttribute = symbolReferences.stream()
                .anyMatch(ref -> implicitFeatures.contains(ref.getSymbol()));

        if (!symbolReferencesFeatureOfAttribute) {
            /*
             * Implicit variable has a reference to where it comes from we should filter the implicit variable so that
             *  we only error on the one that comes from the left hand side of the `then` operation
             */
            List<RosettaImplicitVariable> implicitVariables =
                    EcoreUtil2.getAllContentsOfType(inlineFunction, RosettaImplicitVariable.class)
                            .stream()
                            .filter(implicitVar -> Objects.equals(implicitVarUtil.findContainerDefiningImplicitVariable(implicitVar).orElse(null), operation))
                            .toList();
            
            if (implicitVariables.isEmpty()) {
                error("The input item is not used in the `then` expression", inlineFunction, INLINE_FUNCTION__BODY);
            }
        }
    }
	
	@Check
	public void checkWithMetaOperation(WithMetaOperation operation) {
		RosettaExpression argument = operation.getArgument();
		isSingleCheckError(argument, operation, ROSETTA_UNARY_OPERATION__ARGUMENT, "The with-meta operator can only be used with single cardinality arguments");
	}

	@Check
	public void checkWithMetaEntry(WithMetaEntry entry) {
		RosettaFeature metaType = entry.getKey();

		RMetaAnnotatedType expectedType = typeProvider.getRTypeOfFeature(metaType, null);
		isSingleCheckError(entry.getValue(), entry, WITH_META_ENTRY__VALUE, String.format("Meta attribute '%s' was multi cardinality", metaType.getName()));
		subtypeCheck(expectedType, entry.getValue(), entry, WITH_META_ENTRY__VALUE, actual -> String.format("Meta attribute '%s' should be of type '%s'", metaType.getName(), expectedType.getRType().getName()));
	}

	@Check
	public void checkCondition(Condition c) {
		isSingleCheck(c.getExpression(), c, CONDITION__EXPRESSION, "A condition should be single cardinality");
		subtypeCheck(builtins.BOOLEAN_WITH_NO_META, c.getExpression(), c, CONDITION__EXPRESSION, actual -> "A condition must be a boolean");
	}
	
	@Check
	public void checkFunctionOperation(Operation op) {
		RosettaExpression expr = op.getExpression();
		if (expr != null && cardinalityProvider.isOutputListOfLists(expr)) {
			error("Assign expression contains a list of lists, use flatten to create a list", op, OPERATION__EXPRESSION);
		}
		
		//Any segments that are not symbols will be picked up by the syntax checker, this is to stop noisy errors
		List<RosettaSymbol> segmentsAsSymbols = op.pathAsSegmentList()
				.stream()
				.map(s -> s.getFeature())
				.filter(f -> f instanceof RosettaSymbol)
				.map(f -> (RosettaSymbol)f)
				.collect(Collectors.toList());
		
		RosettaSymbol attr = op.getPath() != null && !segmentsAsSymbols.isEmpty()
				? segmentsAsSymbols.get(segmentsAsSymbols.size() - 1)
				: op.getAssignRoot();
		subtypeCheck(typeProvider.getRTypeOfSymbol(attr, null), expr, op, OPERATION__EXPRESSION, actual -> "Cannot assign `" + actual + "` to output `" + attr.getName() + "`");
		boolean isList = cardinalityProvider.isSymbolMulti(attr);
		if (op.isAdd() && !isList) {
			error("`add` must be used with a list", op, OPERATION__ASSIGN_ROOT);
		}
		if (!isList) {
			isSingleCheck(expr, op, OPERATION__EXPRESSION, "Cannot assign a list to a single value");
		}
	}
	
	@Check
	public void checkArithmeticOperation(ArithmeticOperation op) {
		RosettaExpression left = op.getLeft();
		RosettaExpression right = op.getRight();
		String operator = op.getOperator();
		RMetaAnnotatedType leftType = typeProvider.getRMetaAnnotatedType(left);
		RMetaAnnotatedType rightType = typeProvider.getRMetaAnnotatedType(right);
		isSingleCheck(left, op, ROSETTA_BINARY_OPERATION__LEFT, op);
		isSingleCheck(right, op, ROSETTA_BINARY_OPERATION__RIGHT, op);
		if (operator.equals("+")) {
			if (typeSystem.isSubtypeOf(leftType, builtins.NOTHING_WITH_ANY_META)) {
				// Do not check right type
			} else if (typeSystem.isSubtypeOf(leftType, builtins.DATE_WITH_NO_META)) {
				subtypeCheck(builtins.TIME_WITH_NO_META, rightType, op, ROSETTA_BINARY_OPERATION__RIGHT, actual -> "Cannot add `" + actual + "` to a `date`");
			} else if (typeSystem.isSubtypeOf(leftType, builtins.UNCONSTRAINED_STRING_WITH_NO_META)) {
				subtypeCheck(builtins.UNCONSTRAINED_STRING_WITH_NO_META, rightType, op, ROSETTA_BINARY_OPERATION__RIGHT, actual -> "Cannot add `" + actual + "` to a `string`");
			} else if (typeSystem.isSubtypeOf(leftType, builtins.UNCONSTRAINED_NUMBER_WITH_NO_META)) {
				subtypeCheck(builtins.UNCONSTRAINED_NUMBER_WITH_NO_META, rightType, op, ROSETTA_BINARY_OPERATION__RIGHT, actual -> "Cannot add `" + actual + "` to a `number`");
			} else {
				unsupportedTypeError(leftType, op, ROSETTA_BINARY_OPERATION__LEFT, builtins.UNCONSTRAINED_NUMBER, builtins.UNCONSTRAINED_STRING, builtins.DATE);
				if (!typeSystem.isSubtypeOf(rightType, builtins.TIME_WITH_NO_META) 
						&& !typeSystem.isSubtypeOf(rightType, builtins.UNCONSTRAINED_STRING_WITH_NO_META)
						&& !typeSystem.isSubtypeOf(rightType, builtins.UNCONSTRAINED_NUMBER_WITH_NO_META)) {
					unsupportedTypeError(rightType, op, ROSETTA_BINARY_OPERATION__RIGHT, builtins.UNCONSTRAINED_NUMBER, builtins.UNCONSTRAINED_STRING, builtins.TIME);
				}
			}
		} else if (operator.equals("-")) {
			if (typeSystem.isSubtypeOf(leftType, builtins.NOTHING_WITH_ANY_META)) {
				// Do not check right type
			} else if (typeSystem.isSubtypeOf(leftType, builtins.DATE_WITH_NO_META)) {
				subtypeCheck(builtins.DATE_WITH_NO_META, rightType, op, ROSETTA_BINARY_OPERATION__RIGHT, actual -> "Cannot subtract `" + actual + "` from a `date`");
			} else if (typeSystem.isSubtypeOf(leftType, builtins.UNCONSTRAINED_NUMBER_WITH_NO_META)) {
				subtypeCheck(builtins.UNCONSTRAINED_NUMBER_WITH_NO_META, rightType, op, ROSETTA_BINARY_OPERATION__RIGHT, actual -> "Cannot subtract `" + actual + "` from a `number`");
			} else {
				unsupportedTypeError(leftType, op, ROSETTA_BINARY_OPERATION__LEFT, builtins.UNCONSTRAINED_NUMBER, builtins.DATE);
				if (!typeSystem.isSubtypeOf(rightType, builtins.DATE_WITH_NO_META) 
						&& !typeSystem.isSubtypeOf(rightType, builtins.UNCONSTRAINED_NUMBER_WITH_NO_META)) {
					unsupportedTypeError(rightType, op, ROSETTA_BINARY_OPERATION__RIGHT, builtins.UNCONSTRAINED_NUMBER, builtins.DATE);
				}
			}
		} else {
			subtypeCheck(builtins.UNCONSTRAINED_NUMBER_WITH_NO_META, leftType, op, ROSETTA_BINARY_OPERATION__LEFT, op);
			subtypeCheck(builtins.UNCONSTRAINED_NUMBER_WITH_NO_META, rightType, op, ROSETTA_BINARY_OPERATION__RIGHT, op);
		}
	}
	
	private void checkModifiedBinaryOperation(ModifiableBinaryOperation op) {
		RosettaExpression left = op.getLeft();
		RosettaExpression right = op.getRight();
		String removeModifierSuggestion = "Did you mean to remove the `" + op.getCardMod().getLiteral() + "` modifier on the `" + op.getOperator() + "` operator?";
		String flipOperandsSuggestion = "Did you mean to flip around the operands of the `" + op.getOperator() + "` operator?";
		String leftSuggestion;
		if (cardinalityProvider.isMulti(right)) {
			// Multi and single are flipped
			leftSuggestion = flipOperandsSuggestion;
		} else {
			// Both are single
			leftSuggestion = removeModifierSuggestion;
		}
		String rightSuggestion;
		if (cardinalityProvider.isMulti(left)) {
			// Both are multi
			rightSuggestion = null;
		} else {
			// Multi and single are flipped
			rightSuggestion = flipOperandsSuggestion;
		}
		isMultiCheck(left, op, ROSETTA_BINARY_OPERATION__LEFT, leftSuggestion);
		isSingleCheck(right, op, ROSETTA_BINARY_OPERATION__RIGHT, rightSuggestion);
	}
	
	@Check
	public void checkEqualityOperation(EqualityOperation op) {
		comparableTypeCheck(op);
		if (op.getCardMod() != CardinalityModifier.NONE) {
			checkModifiedBinaryOperation(op);
		} else {
			boolean leftIsMulti = cardinalityProvider.isMulti(op.getLeft());
			boolean rightIsMulti = cardinalityProvider.isMulti(op.getRight());
			if (leftIsMulti != rightIsMulti) {
				error("Operator `" + op.getOperator() + "` should specify `all` or `any` when comparing a list to a single value", op, null);
			}
		}
	}
	
	@Check
	public void checkLogicalOperation(LogicalOperation op) {
		RosettaExpression left = op.getLeft();
		RosettaExpression right = op.getRight();
		RMetaAnnotatedType leftType = typeProvider.getRMetaAnnotatedType(left);
		RMetaAnnotatedType rightType = typeProvider.getRMetaAnnotatedType(right);
		isSingleCheck(left, op, ROSETTA_BINARY_OPERATION__LEFT, op);
		isSingleCheck(right, op, ROSETTA_BINARY_OPERATION__RIGHT, op);
		subtypeCheck(builtins.BOOLEAN_WITH_NO_META, leftType, op, ROSETTA_BINARY_OPERATION__LEFT, op);
		subtypeCheck(builtins.BOOLEAN_WITH_NO_META, rightType, op, ROSETTA_BINARY_OPERATION__RIGHT, op);
	}
	
	@Check
	public void checkComparisonOperation(ComparisonOperation op) {
		RosettaExpression left = op.getLeft();
		RosettaExpression right = op.getRight();
		RMetaAnnotatedType leftType = typeProvider.getRMetaAnnotatedType(left);
		RMetaAnnotatedType rightType = typeProvider.getRMetaAnnotatedType(right);
		if (op.getCardMod() != CardinalityModifier.NONE) {
			checkModifiedBinaryOperation(op);
		} else {
			isSingleCheck(left, op, ROSETTA_BINARY_OPERATION__LEFT, "Did you mean to use `all` or `any` in front of the `" + op.getOperator() + "` operator?");
			isSingleCheck(right, op, ROSETTA_BINARY_OPERATION__RIGHT, "Did you mean to use `all` or `any` in front of the `" + op.getOperator() + "` operator?");
		}
		if (typeSystem.isSubtypeOf(leftType, builtins.NOTHING_WITH_ANY_META)) {
			// Do not check right type
		} else if (typeSystem.isSubtypeOf(leftType, builtins.ZONED_DATE_TIME_WITH_NO_META)) {
			subtypeCheck(builtins.ZONED_DATE_TIME_WITH_NO_META, rightType, op, ROSETTA_BINARY_OPERATION__RIGHT, actual -> "Cannot compare a `" + actual + "` to a `zonedDateTime`");
		} else if (typeSystem.isSubtypeOf(leftType, builtins.DATE_WITH_NO_META)) {
			subtypeCheck(builtins.DATE_WITH_NO_META, rightType, op, ROSETTA_BINARY_OPERATION__RIGHT, actual -> "Cannot compare a `" + actual + "` to a `date`");
		} else if (typeSystem.isSubtypeOf(leftType, builtins.UNCONSTRAINED_NUMBER_WITH_NO_META)) {
			subtypeCheck(builtins.UNCONSTRAINED_NUMBER_WITH_NO_META, rightType, op, ROSETTA_BINARY_OPERATION__RIGHT, actual -> "Cannot compare a `" + actual + "` to a `number`");
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
		isMultiCheck(expr.getLeft(), expr, ROSETTA_BINARY_OPERATION__LEFT, "Did you mean to use the `=` operator instead?");
		comparableTypeCheck(expr);
	}
	
	@Check
	public void checkDisjointExpression(RosettaDisjointExpression expr) {
		isMultiCheck(expr.getLeft(), expr, ROSETTA_BINARY_OPERATION__LEFT, expr);
		isMultiCheck(expr.getRight(), expr, ROSETTA_BINARY_OPERATION__LEFT, expr);
		comparableTypeCheck(expr);
	}
	
	@Check
	public void checkConditionalExpression(RosettaConditionalExpression expr) {
		isSingleCheck(expr.getIf(), expr, ROSETTA_CONDITIONAL_EXPRESSION__IF, "The condition of an if-then-else expression should be single cardinality");
		subtypeCheck(builtins.BOOLEAN_WITH_NO_META, expr.getIf(), expr, ROSETTA_CONDITIONAL_EXPRESSION__IF, actual -> "The condition of an if-then-else expression must be a boolean");
		commonTypeCheck(expr.getIfthen(), expr.getElsethen(), expr, ROSETTA_CONDITIONAL_EXPRESSION__ELSETHEN);
	}
	
	@Check
	public void checkListLiteral(ListLiteral expr) {
		commonTypeCheck(expr.getElements(), expr, LIST_LITERAL__ELEMENTS);
	}
	
	@Check
	public void checkDefaultOperation(DefaultOperation op) {
		commonTypeCheck(op.getLeft(), op.getRight(), op, ROSETTA_BINARY_OPERATION__RIGHT);
	}
	
	@Check
	public void checkSymbolReference(RosettaSymbolReference expr) {
		RosettaSymbol s = expr.getSymbol();
		if (ecoreUtil.isResolved(s)) {
			if (s instanceof RosettaCallableWithArgs callable) {
                checkCallableReference(expr, callable);
			} else {
				if (s instanceof Attribute) {
					if (functionExtensions.isOutput((Attribute) s)) {
						Iterable<? extends RosettaFeature> implicitFeatures = typeProvider.findFeaturesOfImplicitVariable(expr);
						if (Iterables.any(implicitFeatures, f -> f.getName().equals(s.getName()))) {
							error(
								"Ambiguous reference. `" + s.getName() + "` may either refer to `item -> " + s.getName() + "` or to the output variable.",
								expr,
								ROSETTA_SYMBOL_REFERENCE__SYMBOL
							);
						}
					}
				}
				if (s instanceof RosettaEnumeration) {
					if (!(expr.eContainer() instanceof RosettaFeatureCall)) {
						var enumValues = ((RosettaEnumeration) s).getEnumValues().stream()
								.map(v -> v.getName())
								.collect(Collectors.joining(", "));
						error("Enum type `" + s.getName() + "` must be followed by ` -> <enum value>`. Possible values are: " + enumValues, expr, ROSETTA_SYMBOL_REFERENCE__SYMBOL);
					}
				}
				if (expr.isExplicitArguments()) {
					error(
						"A variable may not be called",
						expr,
						ROSETTA_CALLABLE_REFERENCE__EXPLICIT_ARGUMENTS
					);
				}
			}
		}
	}
    
    @Check
    public void checkSuperCall(RosettaSuperCall expr) {
        Function superFunction = expr.getSuperFunction();
        if (superFunction == null) {
            error("Calling `super` is only allowed when extending a function", expr, ROSETTA_NAMED__NAME);
        } else {
            checkCallableReference(expr, superFunction);
        }
    }
    
    private void checkCallableReference(RosettaCallableReference expr, RosettaCallableWithArgs callable) {
        if (ecoreUtil.isResolved(callable)) {
            int paramCount = callable.numberOfParameters();
            int argCount = expr.getArgs().size();
            if (paramCount != argCount) {
                error("Expected " + paramCount + " argument" + (paramCount == 1 ? "" : "s") + ", but got " + argCount + " instead", expr, null);
            }
            int minCount = Math.min(paramCount, argCount);

            if (callable instanceof RosettaExternalFunction f) {
                for (int i=0; i<minCount; i++) {
                    RosettaParameter param = f.getParameters().get(i);
                    RMetaAnnotatedType paramType = typeProvider.getRTypeOfSymbol(param, null);
                    RosettaExpression arg = expr.getArgs().get(i);
                    isSingleCheck(arg, expr, ROSETTA_CALLABLE_REFERENCE__RAW_ARGS, i, null);
                    subtypeCheck(paramType, arg, expr, ROSETTA_CALLABLE_REFERENCE__RAW_ARGS, i, actual -> "Cannot assign `" + actual + "` to parameter `" + param.getName() + "`");
                }
            } else if (callable instanceof Function f) {
                for (int i=0; i<minCount; i++) {
                    Attribute param = f.getInputs().get(i);
                    RMetaAnnotatedType paramType = typeProvider.getRTypeOfSymbol(param, null);
                    RosettaExpression arg = expr.getArgs().get(i);
                    if (!cardinalityProvider.isSymbolMulti(f.getInputs().get(i))) {
                        isSingleCheck(arg, expr, ROSETTA_CALLABLE_REFERENCE__RAW_ARGS, i, null);
                    }
                    subtypeCheck(paramType, arg, expr, ROSETTA_CALLABLE_REFERENCE__RAW_ARGS, i, actual -> "Cannot assign `" + actual + "` to input `" + param.getName() + "`");
                }
            } else if (callable instanceof RosettaRule f) {
                if (minCount >= 1) {
                    RMetaAnnotatedType paramType = withNoMeta(typeSystem.getRuleInputType(f));
                    RosettaExpression arg = expr.getArgs().get(0);
                    isSingleCheck(arg, expr, ROSETTA_CALLABLE_REFERENCE__RAW_ARGS, 0, null);
                    subtypeCheck(paramType, arg, expr, ROSETTA_CALLABLE_REFERENCE__RAW_ARGS, 0, actual -> "Rule `" + f.getName() + "` cannot be called with type `" + actual + "`");
                }
            }
        }
    }
	
	@Check
	public void checkExistsExpression(RosettaExistsExpression expr) {
		if (expr.getModifier() == ExistsModifier.MULTIPLE || expr.getModifier() == ExistsModifier.SINGLE) {
			isMultiCheck(expr.getArgument(), expr, ROSETTA_UNARY_OPERATION__ARGUMENT, expr);
		}
	}
	
	private boolean mayBeEmpty(RType t) {
		return t instanceof RDataType && ((RDataType) t).getAllAttributes().stream().allMatch(a -> a.getCardinality().isOptional()) || t instanceof RChoiceType;
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
			
			RMetaAnnotatedType parentType;
			if (parent != null) {
				isSingleCheck(parent, parent, null, "The `only exists` operator requires a single cardinality input");
				parentType = typeProvider.getRMetaAnnotatedType(parent);
			} else {
				if (cardinalityProvider.isImplicitVariableMulti(expr)) {
					error("Expecting single cardinality input", expr, ROSETTA_ONLY_EXISTS_EXPRESSION__ARGS, 0);
				}
				parentType = typeProvider.typeOfImplicitVariable(expr);
			}
			if (typeSystem.isSubtypeOf(parentType, builtins.NOTHING_WITH_ANY_META)) {
				return;
			}
			RType parentData = parentType.getRType();
			if (!mayBeEmpty(parentData)) {
				unsupportedTypeError(parentType, "only exists", first, null, "All attributes of input type should be optional");
			}
		}
	}
	
	@Check
	public void checkOneOfOperation(OneOfOperation op) {
		isSingleCheck(op.getArgument(), op, ROSETTA_UNARY_OPERATION__ARGUMENT, op);
		RMetaAnnotatedType argType = typeProvider.getRMetaAnnotatedType(op.getArgument());
		if (typeSystem.isSubtypeOf(argType, builtins.NOTHING_WITH_ANY_META)) {
			return;
		}
		if (!mayBeEmpty(argType.getRType())) {
			unsupportedTypeError(argType, op.getOperator(), op, ROSETTA_UNARY_OPERATION__ARGUMENT, "All attributes of input type should be optional");
		}
	}
	
	@Check
	public void checkChoiceOperation(ChoiceOperation op) {
		isSingleCheck(op.getArgument(), op, ROSETTA_UNARY_OPERATION__ARGUMENT, op);
		RMetaAnnotatedType argType = typeProvider.getRMetaAnnotatedType(op.getArgument());
		if (typeSystem.isSubtypeOf(argType, builtins.NOTHING_WITH_ANY_META)) {
			return;
		}
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
		isMultiCheck(op.getLeft(), op, ROSETTA_BINARY_OPERATION__LEFT, op);
		isSingleCheck(op.getRight(), op, ROSETTA_BINARY_OPERATION__RIGHT, op);
		subtypeCheck(builtins.UNCONSTRAINED_STRING_WITH_NO_META, op.getLeft(), op, ROSETTA_BINARY_OPERATION__LEFT, op);
		subtypeCheck(builtins.UNCONSTRAINED_STRING_WITH_NO_META, op.getRight(), op, ROSETTA_BINARY_OPERATION__RIGHT, op);
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
