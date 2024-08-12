package com.regnosys.rosetta.types;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.regnosys.rosetta.rosetta.simple.SimplePackage.Literals.*;
import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.*;

import com.google.inject.ImplementedBy;
import com.regnosys.rosetta.cache.IRequestScopedCache;
import com.regnosys.rosetta.rosetta.RosettaRule;
import com.regnosys.rosetta.rosetta.RosettaSymbol;
import com.regnosys.rosetta.rosetta.expression.ArithmeticOperation;
import com.regnosys.rosetta.rosetta.expression.AsKeyOperation;
import com.regnosys.rosetta.rosetta.expression.AsReferenceOperation;
import com.regnosys.rosetta.rosetta.expression.CaseStatement;
import com.regnosys.rosetta.rosetta.expression.ChoiceOperation;
import com.regnosys.rosetta.rosetta.expression.ComparingFunctionalOperation;
import com.regnosys.rosetta.rosetta.expression.ComparisonOperation;
import com.regnosys.rosetta.rosetta.expression.ConstructorKeyValuePair;
import com.regnosys.rosetta.rosetta.expression.DefaultOperation;
import com.regnosys.rosetta.rosetta.expression.DistinctOperation;
import com.regnosys.rosetta.rosetta.expression.EqualityOperation;
import com.regnosys.rosetta.rosetta.expression.FilterOperation;
import com.regnosys.rosetta.rosetta.expression.FirstOperation;
import com.regnosys.rosetta.rosetta.expression.FlattenOperation;
import com.regnosys.rosetta.rosetta.expression.InlineFunction;
import com.regnosys.rosetta.rosetta.expression.JoinOperation;
import com.regnosys.rosetta.rosetta.expression.LastOperation;
import com.regnosys.rosetta.rosetta.expression.ListLiteral;
import com.regnosys.rosetta.rosetta.expression.LogicalOperation;
import com.regnosys.rosetta.rosetta.expression.MapOperation;
import com.regnosys.rosetta.rosetta.expression.MaxOperation;
import com.regnosys.rosetta.rosetta.expression.MinOperation;
import com.regnosys.rosetta.rosetta.expression.OneOfOperation;
import com.regnosys.rosetta.rosetta.expression.ReduceOperation;
import com.regnosys.rosetta.rosetta.expression.ReverseOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaAbsentExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaBooleanLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaConditionalExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaConstructorExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaContainsExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaCountOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaDeepFeatureCall;
import com.regnosys.rosetta.rosetta.expression.RosettaDisjointExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaExistsExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaFeatureCall;
import com.regnosys.rosetta.rosetta.expression.RosettaFunctionalOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaImplicitVariable;
import com.regnosys.rosetta.rosetta.expression.RosettaIntLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaNumberLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaOnlyElement;
import com.regnosys.rosetta.rosetta.expression.RosettaOnlyExistsExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaStringLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference;
import com.regnosys.rosetta.rosetta.expression.RosettaUnaryOperation;
import com.regnosys.rosetta.rosetta.expression.SortOperation;
import com.regnosys.rosetta.rosetta.expression.SumOperation;
import com.regnosys.rosetta.rosetta.expression.SwitchOperation;
import com.regnosys.rosetta.rosetta.expression.ThenOperation;
import com.regnosys.rosetta.rosetta.expression.ToDateOperation;
import com.regnosys.rosetta.rosetta.expression.ToDateTimeOperation;
import com.regnosys.rosetta.rosetta.expression.ToEnumOperation;
import com.regnosys.rosetta.rosetta.expression.ToIntOperation;
import com.regnosys.rosetta.rosetta.expression.ToNumberOperation;
import com.regnosys.rosetta.rosetta.expression.ToStringOperation;
import com.regnosys.rosetta.rosetta.expression.ToTimeOperation;
import com.regnosys.rosetta.rosetta.expression.ToZonedDateTimeOperation;
import com.regnosys.rosetta.rosetta.expression.TranslateDispatchOperation;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.rosetta.simple.Operation;
import com.regnosys.rosetta.rosetta.simple.Segment;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;
import com.regnosys.rosetta.utils.RosettaExpressionSwitch;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;


@ImplementedBy(ExpectedTypeProvider.Impl.class)
public interface ExpectedTypeProvider {	
	static final int INSIGNIFICANT_INDEX = -1;
	
	RType getExpectedTypeFromContainer(EObject owner);
	default RType getExpectedType(EObject owner, EReference reference) {
		return getExpectedType(owner, reference, INSIGNIFICANT_INDEX);
	}
	RType getExpectedType(EObject owner, EReference reference, int index);
	
	public static class Impl implements ExpectedTypeProvider {
		private static Logger LOGGER = LoggerFactory.getLogger(Impl.class);
		
		private final RBuiltinTypeService builtins;
		private final RosettaTypeProvider typeProvider;
		private final TypeSystem typeSystem;
		private final ExpectedTypeSwitch expressionSwitch;
		private final IRequestScopedCache cache;
		
		@Inject
		public Impl(RBuiltinTypeService builtins, RosettaTypeProvider typeProvider, TypeSystem typeSystem, IRequestScopedCache cache) {
			this.builtins = builtins;
			this.typeProvider = typeProvider;
			this.typeSystem = typeSystem;
			this.cache = cache;
			this.expressionSwitch = new ExpectedTypeSwitch();
		}
		
		@Override
		public RType getExpectedTypeFromContainer(EObject owner) {
			EObject container = owner.eContainer();
			EReference reference = owner.eContainmentFeature();
			if (container == null || reference == null) {
				return null;
			}
			if (reference.isMany()) {
				int index = ((List<?>)container.eGet(reference)).indexOf(owner);
				return getExpectedType(container, reference, index);
			}
			return getExpectedType(container, reference);
		}
		@Override
		public RType getExpectedType(EObject owner, EReference reference, int index) {
			return cache.<RType>get(new CacheKey(owner, reference, index), () -> {
				if (OPERATION__EXPRESSION.equals(reference) && owner instanceof Operation) {
					Operation op = (Operation)owner;
					if(op.getPath() == null) {
						return typeProvider.getRTypeOfSymbol(op.getAssignRoot());
					}
					List<Segment> path = op.pathAsSegmentList();
					return typeProvider.getRTypeOfSymbol(path.get(path.size() - 1).getAttribute());
				} else if (CONSTRUCTOR_KEY_VALUE_PAIR__VALUE.equals(reference) && owner instanceof ConstructorKeyValuePair) {
					ConstructorKeyValuePair pair = (ConstructorKeyValuePair) owner;
					return typeProvider.getRTypeOfFeature(pair.getKey(), null);
				} else if (owner instanceof RosettaExpression) {
					return this.expressionSwitch.doSwitch((RosettaExpression) owner, reference, index);
				} else if (INLINE_FUNCTION__BODY.equals(reference)) {
					EObject operation = owner.eContainer();
					if (operation instanceof ReduceOperation) {
						return getExpectedTypeFromContainer(operation);
					} else if (operation instanceof FilterOperation) {
						return builtins.BOOLEAN;
					} else if (operation instanceof MapOperation) {
						return getExpectedTypeFromContainer(operation);
					} else if (operation instanceof ThenOperation) {
						return getExpectedTypeFromContainer(operation);
					} else if (operation instanceof ComparingFunctionalOperation) {
						return builtins.BOOLEAN;
					} else {
						LOGGER.debug("Unexpected functional operation of type " + operation.getClass().getCanonicalName());
					}
				} else if (owner instanceof CaseStatement) {
					CaseStatement caseStat = (CaseStatement) owner;
					SwitchOperation op = caseStat.getSwitchOperation();
					if (CASE_STATEMENT__CONDITION.equals(reference)) {
						return typeProvider.getRType(op.getArgument());
					} else if (CASE_STATEMENT__EXPRESSION.equals(reference)) {
						return getExpectedTypeFromContainer(op);
					}
				}
				return null;
			});
		}
		
		private static class CacheKey {
			private final EObject owner;
			private final EReference reference;
			private final int index;
			public CacheKey(EObject owner, EReference reference, int index) {
				this.owner = owner;
				this.reference = reference;
				this.index = index;
			}
			
			@Override
			public int hashCode() {
				return Objects.hash(CacheKey.class, index, owner, reference);
			}
			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				CacheKey other = (CacheKey) obj;
				return index == other.index && Objects.equals(owner, other.owner)
						&& Objects.equals(reference, other.reference);
			}
		}
		private static class Context {
			public final EReference reference;
			public final int index;
			
			public Context(EReference reference, int index) {
				this.reference = reference;
				this.index = index;
			}
		}
		private class ExpectedTypeSwitch extends RosettaExpressionSwitch<RType, Context> {
			public RType doSwitch(RosettaExpression expr, EReference reference, int index) {
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
			protected RType caseConstructorExpression(RosettaConstructorExpression expr, Context context) {
				return null;
			}

			@Override
			protected RType caseListLiteral(ListLiteral expr, Context context) {
				if (LIST_LITERAL__ELEMENTS.equals(context.reference)) {
					return getExpectedTypeFromContainer(expr);
				}
				return null;
			}

			@Override
			protected RType caseConditionalExpression(RosettaConditionalExpression expr, Context context) {
				if (ROSETTA_CONDITIONAL_EXPRESSION__IF.equals(context.reference)) {
					return builtins.BOOLEAN;
				} else if (ROSETTA_CONDITIONAL_EXPRESSION__IFTHEN.equals(context.reference)) {
					return getExpectedTypeFromContainer(expr);
				} else if (ROSETTA_CONDITIONAL_EXPRESSION__ELSETHEN.equals(context.reference)) {
					RType expectedType = getExpectedTypeFromContainer(expr);
					if (expectedType != null) {
						return expectedType;
					}
					return typeProvider.getRType(expr.getIfthen());
				}
				return null;
			}

			@Override
			protected RType caseFeatureCall(RosettaFeatureCall expr, Context context) {
				return null;
			}

			@Override
			protected RType caseDeepFeatureCall(RosettaDeepFeatureCall expr, Context context) {
				return null;
			}

			@Override
			protected RType caseBooleanLiteral(RosettaBooleanLiteral expr, Context context) {
				return null;
			}

			@Override
			protected RType caseIntLiteral(RosettaIntLiteral expr, Context context) {
				return null;
			}

			@Override
			protected RType caseNumberLiteral(RosettaNumberLiteral expr, Context context) {
				return null;
			}

			@Override
			protected RType caseStringLiteral(RosettaStringLiteral expr, Context context) {
				return null;
			}

			@Override
			protected RType caseOnlyExists(RosettaOnlyExistsExpression expr, Context context) {
				return null;
			}

			@Override
			protected RType caseImplicitVariable(RosettaImplicitVariable expr, Context context) {
				return null;
			}

			@Override
			protected RType caseSymbolReference(RosettaSymbolReference expr, Context context) {
				if (ROSETTA_SYMBOL_REFERENCE__RAW_ARGS.equals(context.reference)) {
					RosettaSymbol symbol = expr.getSymbol();
					if (symbol instanceof Function) {
						Function fun = (Function)symbol;
						return typeProvider.getRTypeOfSymbol(fun.getInputs().get(context.index));
					} else if (symbol instanceof RosettaRule) {
						RosettaRule rule = (RosettaRule)symbol;
						return typeSystem.typeCallToRType(rule.getInput());
					}
				}
				return null;
			}

			@Override
			protected RType caseAddOperation(ArithmeticOperation expr, Context context) {
				return null;
			}

			@Override
			protected RType caseSubtractOperation(ArithmeticOperation expr, Context context) {
				return null;
			}

			@Override
			protected RType caseMultiplyOperation(ArithmeticOperation expr, Context context) {
				return builtins.UNCONSTRAINED_NUMBER;
			}

			@Override
			protected RType caseDivideOperation(ArithmeticOperation expr, Context context) {
				return builtins.UNCONSTRAINED_NUMBER;
			}

			@Override
			protected RType caseJoinOperation(JoinOperation expr, Context context) {
				return builtins.UNCONSTRAINED_STRING;
			}

			@Override
			protected RType caseAndOperation(LogicalOperation expr, Context context) {
				return builtins.BOOLEAN;
			}

			@Override
			protected RType caseOrOperation(LogicalOperation expr, Context context) {
				return builtins.BOOLEAN;
			}

			@Override
			protected RType caseLessThanOperation(ComparisonOperation expr, Context context) {
				return null;
			}

			@Override
			protected RType caseLessThanOrEqualOperation(ComparisonOperation expr, Context context) {
				return null;
			}

			@Override
			protected RType caseGreaterThanOperation(ComparisonOperation expr, Context context) {
				return null;
			}

			@Override
			protected RType caseGreaterThanOrEqualOperation(ComparisonOperation expr, Context context) {
				return null;
			}

			@Override
			protected RType caseEqualsOperation(EqualityOperation expr, Context context) {
				if (ROSETTA_BINARY_OPERATION__RIGHT.equals(context.reference)) {
					return typeProvider.getRType(expr.getLeft());
				}
				return null;
			}

			@Override
			protected RType caseNotEqualsOperation(EqualityOperation expr, Context context) {
				if (ROSETTA_BINARY_OPERATION__RIGHT.equals(context.reference)) {
					return typeProvider.getRType(expr.getLeft());
				}
				return null;
			}

			@Override
			protected RType caseContainsOperation(RosettaContainsExpression expr, Context context) {
				if (ROSETTA_BINARY_OPERATION__RIGHT.equals(context.reference)) {
					return typeProvider.getRType(expr.getLeft());
				}
				return null;
			}

			@Override
			protected RType caseDisjointOperation(RosettaDisjointExpression expr, Context context) {
				if (ROSETTA_BINARY_OPERATION__RIGHT.equals(context.reference)) {
					return typeProvider.getRType(expr.getLeft());
				}
				return null;
			}

			@Override
			protected RType caseDefaultOperation(DefaultOperation expr, Context context) {
				if (ROSETTA_BINARY_OPERATION__RIGHT.equals(context.reference)) {
					return typeProvider.getRType(expr.getLeft());
				}
				return null;
			}

			@Override
			protected RType caseAsKeyOperation(AsKeyOperation expr, Context context) {
				if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
					return getExpectedTypeFromContainer(expr);
				}
				return null;
			}

			@Override
			protected RType caseChoiceOperation(ChoiceOperation expr, Context context) {
				return null;
			}

			@Override
			protected RType caseOneOfOperation(OneOfOperation expr, Context context) {
				return null;
			}

			@Override
			protected RType caseAbsentOperation(RosettaAbsentExpression expr, Context context) {
				return null;
			}

			@Override
			protected RType caseCountOperation(RosettaCountOperation expr, Context context) {
				return null;
			}

			@Override
			protected RType caseExistsOperation(RosettaExistsExpression expr, Context context) {
				return null;
			}

			@Override
			protected RType caseDistinctOperation(DistinctOperation expr, Context context) {
				if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
					return getExpectedTypeFromContainer(expr);
				}
				return null;
			}

			@Override
			protected RType caseFirstOperation(FirstOperation expr, Context context) {
				if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
					return getExpectedTypeFromContainer(expr);
				}
				return null;
			}

			@Override
			protected RType caseFlattenOperation(FlattenOperation expr, Context context) {
				if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
					return getExpectedTypeFromContainer(expr);
				}
				return null;
			}

			@Override
			protected RType caseLastOperation(LastOperation expr, Context context) {
				if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
					return getExpectedTypeFromContainer(expr);
				}
				return null;
			}

			@Override
			protected RType caseReverseOperation(ReverseOperation expr, Context context) {
				if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
					return getExpectedTypeFromContainer(expr);
				}
				return null;
			}

			@Override
			protected RType caseOnlyElementOperation(RosettaOnlyElement expr, Context context) {
				if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
					return getExpectedTypeFromContainer(expr);
				}
				return null;
			}

			@Override
			protected RType caseSumOperation(SumOperation expr, Context context) {
				return null;
			}

			@Override
			protected RType caseToStringOperation(ToStringOperation expr, Context context) {
				return null;
			}

			@Override
			protected RType caseToNumberOperation(ToNumberOperation expr, Context context) {
				if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
					return builtins.UNCONSTRAINED_STRING;
				}
				return null;
			}

			@Override
			protected RType caseToIntOperation(ToIntOperation expr, Context context) {
				if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
					return builtins.UNCONSTRAINED_STRING;
				}
				return null;
			}

			@Override
			protected RType caseToTimeOperation(ToTimeOperation expr, Context context) {
				if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
					return builtins.UNCONSTRAINED_STRING;
				}
				return null;
			}

			@Override
			protected RType caseToEnumOperation(ToEnumOperation expr, Context context) {
				if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
					return builtins.UNCONSTRAINED_STRING;
				}
				return null;
			}

			@Override
			protected RType caseToDateOperation(ToDateOperation expr, Context context) {
				if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
					return builtins.UNCONSTRAINED_STRING;
				}
				return null;
			}

			@Override
			protected RType caseToDateTimeOperation(ToDateTimeOperation expr, Context context) {
				if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
					return builtins.UNCONSTRAINED_STRING;
				}
				return null;
			}

			@Override
			protected RType caseToZonedDateTimeOperation(ToZonedDateTimeOperation expr, Context context) {
				if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
					return builtins.UNCONSTRAINED_STRING;
				}
				return null;
			}

			@Override
			protected RType caseFilterOperation(FilterOperation expr, Context context) {
				if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
					return getExpectedTypeFromContainer(expr);
				}
				return null;
			}

			@Override
			protected RType caseMapOperation(MapOperation expr, Context context) {
				InlineFunction f = expr.getFunction();
				if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference) && f != null && leavesItemTypeUnchanged(f.getBody())) {
					return getExpectedTypeFromContainer(expr);
				}
				return null;
			}

			@Override
			protected RType caseMaxOperation(MaxOperation expr, Context context) {
				if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
					return getExpectedTypeFromContainer(expr);
				}
				return null;
			}

			@Override
			protected RType caseMinOperation(MinOperation expr, Context context) {
				if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
					return getExpectedTypeFromContainer(expr);
				}
				return null;
			}

			@Override
			protected RType caseReduceOperation(ReduceOperation expr, Context context) {
				return null;
			}

			@Override
			protected RType caseSortOperation(SortOperation expr, Context context) {
				if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
					return getExpectedTypeFromContainer(expr);
				}
				return null;
			}

			@Override
			protected RType caseThenOperation(ThenOperation expr, Context context) {
				if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference) && leavesItemTypeUnchanged(expr.getFunction().getBody())) {
					return getExpectedTypeFromContainer(expr);
				}
				return null;
			}

			@Override
			protected RType caseTranslateDispatchOperation(TranslateDispatchOperation expr, Context context) {
				return null;
			}

			@Override
			protected RType caseToSwitchOperation(SwitchOperation expr, Context context) {
				if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference) && expr.getValues().stream().allMatch(c -> leavesItemTypeUnchanged(c.getExpression()))) {
					return getExpectedTypeFromContainer(expr);
				} else if (SWITCH_OPERATION__DEFAULT.equals(context.reference)) {
					return getExpectedTypeFromContainer(expr);
				}
				return null;
			}

			@Override
			protected RType caseAsReferenceOperation(AsReferenceOperation expr, Context context) {
				if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
					return builtins.UNCONSTRAINED_STRING;
				}
				return null;
			}
		}
	}
}
