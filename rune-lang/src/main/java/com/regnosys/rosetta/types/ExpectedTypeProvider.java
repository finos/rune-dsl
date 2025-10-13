package com.regnosys.rosetta.types;

import com.google.inject.ImplementedBy;
import com.regnosys.rosetta.rosetta.RosettaRule;
import com.regnosys.rosetta.rosetta.RosettaSymbol;
import com.regnosys.rosetta.rosetta.expression.*;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.rosetta.simple.Operation;
import com.regnosys.rosetta.rosetta.simple.Segment;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;
import com.regnosys.rosetta.utils.RosettaExpressionSwitch;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.*;
import static com.regnosys.rosetta.rosetta.simple.SimplePackage.Literals.OPERATION__EXPRESSION;
import static com.regnosys.rosetta.types.RMetaAnnotatedType.withNoMeta;


@ImplementedBy(ExpectedTypeProvider.Impl.class)
public interface ExpectedTypeProvider {
    int INSIGNIFICANT_INDEX = -1;

    RMetaAnnotatedType getExpectedTypeFromContainer(EObject owner);

    default RMetaAnnotatedType getExpectedType(EObject owner, EReference reference) {
        return getExpectedType(owner, reference, INSIGNIFICANT_INDEX);
    }

    RMetaAnnotatedType getExpectedType(EObject owner, EReference reference, int index);

    class Impl implements ExpectedTypeProvider {
        private static final Logger LOGGER = LoggerFactory.getLogger(Impl.class);

        private final RBuiltinTypeService builtins;
        private final RosettaTypeProvider typeProvider;
        private final TypeSystem typeSystem;
        private final ExpectedTypeSwitch expressionSwitch;

        @Inject
        public Impl(RBuiltinTypeService builtins, RosettaTypeProvider typeProvider, TypeSystem typeSystem) {
            this.builtins = builtins;
            this.typeProvider = typeProvider;
            this.typeSystem = typeSystem;
            this.expressionSwitch = new ExpectedTypeSwitch();
        }

        @Override
        public RMetaAnnotatedType getExpectedTypeFromContainer(EObject owner) {
            EObject container = owner.eContainer();
            EReference reference = owner.eContainmentFeature();
            if (container == null || reference == null) {
                return null;
            }
            if (reference.isMany()) {
                int index = ((List<?>) container.eGet(reference)).indexOf(owner);
                return getExpectedType(container, reference, index);
            }
            return getExpectedType(container, reference);
        }

        @Override
        public RMetaAnnotatedType getExpectedType(EObject owner, EReference reference, int index) {
            if (OPERATION__EXPRESSION.equals(reference) && owner instanceof Operation op) {
                if (op.getPath() == null) {
                    return typeProvider.getRTypeOfSymbol(op.getAssignRoot());
                }
                List<Segment> path = op.pathAsSegmentList();
                return typeProvider.getRTypeOfFeature(path.get(path.size() - 1).getFeature(), null);
            } else if (CONSTRUCTOR_KEY_VALUE_PAIR__VALUE.equals(reference) && owner instanceof ConstructorKeyValuePair pair) {
                return typeProvider.getRTypeOfFeature(pair.getKey(), null);
            } else if (WITH_META_ENTRY__VALUE.equals(reference) && owner instanceof WithMetaEntry entry) {
                return typeProvider.getRTypeOfFeature(entry.getKey(), reference);
            } else if (owner instanceof RosettaExpression) {
                return this.expressionSwitch.doSwitch((RosettaExpression) owner, reference, index);
            } else if (INLINE_FUNCTION__BODY.equals(reference)) {
                EObject operation = owner.eContainer();
                if (operation instanceof ReduceOperation) {
                    return getExpectedTypeFromContainer(operation);
                } else if (operation instanceof FilterOperation) {
                    return builtins.BOOLEAN_WITH_NO_META;
                } else if (operation instanceof MapOperation) {
                    return getExpectedTypeFromContainer(operation);
                } else if (operation instanceof ThenOperation) {
                    return getExpectedTypeFromContainer(operation);
                } else if (operation instanceof ComparingFunctionalOperation) {
                    return builtins.BOOLEAN_WITH_NO_META;
                } else {
                    LOGGER.debug("Unexpected functional operation of type " + operation.getClass().getCanonicalName());
                }
            } else if (owner instanceof SwitchCaseOrDefault switchCase) {
                if (SWITCH_CASE_OR_DEFAULT__EXPRESSION.equals(reference)) {
                    return getExpectedTypeFromContainer(switchCase.getSwitchOperation());
                }
            }
            return null;
        }

        private record Context(EReference reference, int index) {
        }

        private class ExpectedTypeSwitch extends RosettaExpressionSwitch<RMetaAnnotatedType, Context> {
            public RMetaAnnotatedType doSwitch(RosettaExpression expr, EReference reference, int index) {
                return doSwitch(expr, new Context(reference, index));
            }

            private boolean leavesItemTypeUnchanged(RosettaExpression expr) {
                if (expr instanceof RosettaImplicitVariable) {
                    return true;
                } else if (
                        expr instanceof AsKeyOperation
                        || expr instanceof FilterOperation
                        || expr instanceof FlattenOperation
                        || expr instanceof DistinctOperation
                        || expr instanceof FirstOperation
                        || expr instanceof LastOperation
                        || expr instanceof MaxOperation
                        || expr instanceof MinOperation
                        || expr instanceof ReverseOperation
                        || expr instanceof SortOperation
                        || expr instanceof RosettaOnlyElement
                ) {
                    return leavesItemTypeUnchanged(((RosettaUnaryOperation) expr).getArgument());
                } else if (expr instanceof MapOperation || expr instanceof ThenOperation) {
                    RosettaFunctionalOperation f = (RosettaFunctionalOperation) expr;
                    return leavesItemTypeUnchanged(f.getFunction().getBody());
                }
                return false;
            }

            @Override
            protected RMetaAnnotatedType caseConstructorExpression(RosettaConstructorExpression expr, Context context) {
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseListLiteral(ListLiteral expr, Context context) {
                if (LIST_LITERAL__ELEMENTS.equals(context.reference)) {
                    return getExpectedTypeFromContainer(expr);
                }
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseConditionalExpression(RosettaConditionalExpression expr, Context context) {
                if (ROSETTA_CONDITIONAL_EXPRESSION__IF.equals(context.reference)) {
                    return builtins.BOOLEAN_WITH_NO_META;
                } else if (ROSETTA_CONDITIONAL_EXPRESSION__IFTHEN.equals(context.reference)) {
                    return getExpectedTypeFromContainer(expr);
                } else if (ROSETTA_CONDITIONAL_EXPRESSION__ELSETHEN.equals(context.reference)) {
                    RMetaAnnotatedType expectedType = getExpectedTypeFromContainer(expr);
                    if (expectedType != null) {
                        return expectedType;
                    }
                    return typeProvider.getRMetaAnnotatedType(expr.getIfthen());
                }
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseFeatureCall(RosettaFeatureCall expr, Context context) {
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseDeepFeatureCall(RosettaDeepFeatureCall expr, Context context) {
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseBooleanLiteral(RosettaBooleanLiteral expr, Context context) {
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseIntLiteral(RosettaIntLiteral expr, Context context) {
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseNumberLiteral(RosettaNumberLiteral expr, Context context) {
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseStringLiteral(RosettaStringLiteral expr, Context context) {
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseOnlyExists(RosettaOnlyExistsExpression expr, Context context) {
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseImplicitVariable(RosettaImplicitVariable expr, Context context) {
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseSymbolReference(RosettaSymbolReference expr, Context context) {
                return caseCallableReference(expr::getSymbol, context);
            }

            @Override
            protected RMetaAnnotatedType caseSuperCall(RosettaSuperCall expr, Context context) {
                return caseCallableReference(expr::getSuperFunction, context);
            }
            
            private RMetaAnnotatedType caseCallableReference(Provider<RosettaSymbol> symbolProvider, Context context) {
                if (ROSETTA_CALLABLE_REFERENCE__RAW_ARGS.equals(context.reference)) {
                    RosettaSymbol symbol = symbolProvider.get();
                    if (symbol instanceof Function fun) {
                        return typeProvider.getRTypeOfSymbol(fun.getInputs().get(context.index));
                    } else if (symbol instanceof RosettaRule rule) {
                        return withNoMeta(typeSystem.getRuleInputType(rule));
                    }
                }
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseAddOperation(ArithmeticOperation expr, Context context) {
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseSubtractOperation(ArithmeticOperation expr, Context context) {
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseMultiplyOperation(ArithmeticOperation expr, Context context) {
                return builtins.UNCONSTRAINED_NUMBER_WITH_NO_META;
            }

            @Override
            protected RMetaAnnotatedType caseDivideOperation(ArithmeticOperation expr, Context context) {
                return builtins.UNCONSTRAINED_STRING_WITH_NO_META;
            }

            @Override
            protected RMetaAnnotatedType caseJoinOperation(JoinOperation expr, Context context) {
                return builtins.UNCONSTRAINED_STRING_WITH_NO_META;
            }

            @Override
            protected RMetaAnnotatedType caseAndOperation(LogicalOperation expr, Context context) {
                return builtins.BOOLEAN_WITH_NO_META;
            }

            @Override
            protected RMetaAnnotatedType caseOrOperation(LogicalOperation expr, Context context) {
                return builtins.BOOLEAN_WITH_NO_META;
            }

            @Override
            protected RMetaAnnotatedType caseLessThanOperation(ComparisonOperation expr, Context context) {
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseLessThanOrEqualOperation(ComparisonOperation expr, Context context) {
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseGreaterThanOperation(ComparisonOperation expr, Context context) {
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseGreaterThanOrEqualOperation(ComparisonOperation expr, Context context) {
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseEqualsOperation(EqualityOperation expr, Context context) {
                if (ROSETTA_BINARY_OPERATION__RIGHT.equals(context.reference)) {
                    return typeProvider.getRMetaAnnotatedType(expr.getLeft());
                }
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseNotEqualsOperation(EqualityOperation expr, Context context) {
                if (ROSETTA_BINARY_OPERATION__RIGHT.equals(context.reference)) {
                    return typeProvider.getRMetaAnnotatedType(expr.getLeft());
                }
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseContainsOperation(RosettaContainsExpression expr, Context context) {
                if (ROSETTA_BINARY_OPERATION__RIGHT.equals(context.reference)) {
                    return typeProvider.getRMetaAnnotatedType(expr.getLeft());
                }
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseDisjointOperation(RosettaDisjointExpression expr, Context context) {
                if (ROSETTA_BINARY_OPERATION__RIGHT.equals(context.reference)) {
                    return typeProvider.getRMetaAnnotatedType(expr.getLeft());
                }
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseDefaultOperation(DefaultOperation expr, Context context) {
                if (ROSETTA_BINARY_OPERATION__RIGHT.equals(context.reference)) {
                    return typeProvider.getRMetaAnnotatedType(expr.getLeft());
                }
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseAsKeyOperation(AsKeyOperation expr, Context context) {
                if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
                    return getExpectedTypeFromContainer(expr);
                }
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseChoiceOperation(ChoiceOperation expr, Context context) {
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseOneOfOperation(OneOfOperation expr, Context context) {
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseAbsentOperation(RosettaAbsentExpression expr, Context context) {
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseCountOperation(RosettaCountOperation expr, Context context) {
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseExistsOperation(RosettaExistsExpression expr, Context context) {
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseDistinctOperation(DistinctOperation expr, Context context) {
                if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
                    return getExpectedTypeFromContainer(expr);
                }
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseFirstOperation(FirstOperation expr, Context context) {
                if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
                    return getExpectedTypeFromContainer(expr);
                }
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseFlattenOperation(FlattenOperation expr, Context context) {
                if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
                    return getExpectedTypeFromContainer(expr);
                }
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseLastOperation(LastOperation expr, Context context) {
                if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
                    return getExpectedTypeFromContainer(expr);
                }
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseReverseOperation(ReverseOperation expr, Context context) {
                if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
                    return getExpectedTypeFromContainer(expr);
                }
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseOnlyElementOperation(RosettaOnlyElement expr, Context context) {
                if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
                    return getExpectedTypeFromContainer(expr);
                }
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseSumOperation(SumOperation expr, Context context) {
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseToStringOperation(ToStringOperation expr, Context context) {
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseToNumberOperation(ToNumberOperation expr, Context context) {
                if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
                    return builtins.UNCONSTRAINED_STRING_WITH_NO_META;
                }
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseToIntOperation(ToIntOperation expr, Context context) {
                if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
                    return builtins.UNCONSTRAINED_STRING_WITH_NO_META;
                }
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseToTimeOperation(ToTimeOperation expr, Context context) {
                if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
                    return builtins.UNCONSTRAINED_STRING_WITH_NO_META;
                }
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseToEnumOperation(ToEnumOperation expr, Context context) {
                if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
                    return builtins.UNCONSTRAINED_STRING_WITH_NO_META;
                }
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseToDateOperation(ToDateOperation expr, Context context) {
                if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
                    return builtins.UNCONSTRAINED_STRING_WITH_NO_META;
                }
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseToDateTimeOperation(ToDateTimeOperation expr, Context context) {
                if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
                    return builtins.UNCONSTRAINED_STRING_WITH_NO_META;
                }
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseToZonedDateTimeOperation(ToZonedDateTimeOperation expr, Context context) {
                if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
                    return builtins.UNCONSTRAINED_STRING_WITH_NO_META;
                }
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseFilterOperation(FilterOperation expr, Context context) {
                if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
                    return getExpectedTypeFromContainer(expr);
                }
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseMapOperation(MapOperation expr, Context context) {
                InlineFunction f = expr.getFunction();
                if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference) && f != null && leavesItemTypeUnchanged(f.getBody())) {
                    return getExpectedTypeFromContainer(expr);
                }
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseMaxOperation(MaxOperation expr, Context context) {
                if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
                    return getExpectedTypeFromContainer(expr);
                }
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseMinOperation(MinOperation expr, Context context) {
                if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
                    return getExpectedTypeFromContainer(expr);
                }
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseReduceOperation(ReduceOperation expr, Context context) {
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseSortOperation(SortOperation expr, Context context) {
                if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
                    return getExpectedTypeFromContainer(expr);
                }
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseThenOperation(ThenOperation expr, Context context) {
                if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference) && leavesItemTypeUnchanged(expr.getFunction().getBody())) {
                    return getExpectedTypeFromContainer(expr);
                }
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseSwitchOperation(SwitchOperation expr, Context context) {
                if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
                    if (expr.getCases().stream().allMatch(c -> leavesItemTypeUnchanged(c.getExpression()))) {
                        return getExpectedTypeFromContainer(expr);
                    }
                }
                return null;
            }

            @Override
            protected RMetaAnnotatedType caseWithMetaOperation(WithMetaOperation expr, Context context) {
                if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
                    Set<String> withMetaKeys = expr.getEntries()
                            .stream()
                            .map(e -> e.getKey().getName())
                            .collect(Collectors.toSet());

                    RMetaAnnotatedType expectedType = getExpectedTypeFromContainer(expr);
                    List<RMetaAttribute> metaAttributes = expectedType.getMetaAttributes()
                            .stream()
                            .filter(a -> !withMetaKeys.contains(a.getName()))
                            .collect(Collectors.toList());

                    return RMetaAnnotatedType.withMeta(expectedType.getRType(), metaAttributes);
                }
                return null;
            }
        }
    }
}
