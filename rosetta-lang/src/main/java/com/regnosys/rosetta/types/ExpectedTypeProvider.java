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
import com.regnosys.rosetta.rosetta.expression.SwitchCase;
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
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.rosetta.simple.Operation;
import com.regnosys.rosetta.rosetta.simple.Segment;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;
import com.regnosys.rosetta.utils.RosettaExpressionSwitch;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import static com.regnosys.rosetta.types.RMetaAnnotatedType.withNoMeta;


@ImplementedBy(ExpectedTypeProvider.Impl.class)
public interface ExpectedTypeProvider {	
	static final int INSIGNIFICANT_INDEX = -1;
	
	RMetaAnnotatedType getExpectedTypeFromContainer(EObject owner);
	default RMetaAnnotatedType getExpectedType(EObject owner, EReference reference) {
		return getExpectedType(owner, reference, INSIGNIFICANT_INDEX);
	}
	RMetaAnnotatedType getExpectedType(EObject owner, EReference reference, int index);
	
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
		public RMetaAnnotatedType getExpectedTypeFromContainer(EObject owner) {
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
		public RMetaAnnotatedType getExpectedType(EObject owner, EReference reference, int index) {
			return cache.<RMetaAnnotatedType>get(new CacheKey(owner, reference, index), () -> {
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
						return withNoMeta(builtins.BOOLEAN);
					} else if (operation instanceof MapOperation) {
						return getExpectedTypeFromContainer(operation);
					} else if (operation instanceof ThenOperation) {
						return getExpectedTypeFromContainer(operation);
					} else if (operation instanceof ComparingFunctionalOperation) {
						return withNoMeta(builtins.BOOLEAN);
					} else {
						LOGGER.debug("Unexpected functional operation of type " + operation.getClass().getCanonicalName());
					}
				} else if (owner instanceof SwitchCase) {
					SwitchCase switchCase = (SwitchCase) owner;
					SwitchOperation op = switchCase.getSwitchOperation();
					if (SWITCH_CASE__EXPRESSION.equals(reference)) {
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
					return withNoMeta(builtins.BOOLEAN);
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
				if (ROSETTA_SYMBOL_REFERENCE__RAW_ARGS.equals(context.reference)) {
					RosettaSymbol symbol = expr.getSymbol();
					if (symbol instanceof Function) {
						Function fun = (Function)symbol;
						return typeProvider.getRTypeOfSymbol(fun.getInputs().get(context.index));
					} else if (symbol instanceof RosettaRule) {
						RosettaRule rule = (RosettaRule)symbol;
						return withNoMeta(typeSystem.typeCallToRType(rule.getInput()));
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
				return withNoMeta(builtins.UNCONSTRAINED_NUMBER);
			}

			@Override
			protected RMetaAnnotatedType caseDivideOperation(ArithmeticOperation expr, Context context) {
				return withNoMeta(builtins.UNCONSTRAINED_NUMBER);
			}

			@Override
			protected RMetaAnnotatedType caseJoinOperation(JoinOperation expr, Context context) {
				return withNoMeta(builtins.UNCONSTRAINED_STRING);
			}

			@Override
			protected RMetaAnnotatedType caseAndOperation(LogicalOperation expr, Context context) {
				return withNoMeta(builtins.BOOLEAN);
			}

			@Override
			protected RMetaAnnotatedType caseOrOperation(LogicalOperation expr, Context context) {
				return withNoMeta(builtins.BOOLEAN);
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
					return withNoMeta(builtins.UNCONSTRAINED_STRING);
				}
				return null;
			}

			@Override
			protected RMetaAnnotatedType caseToIntOperation(ToIntOperation expr, Context context) {
				if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
					return withNoMeta(builtins.UNCONSTRAINED_STRING);
				}
				return null;
			}

			@Override
			protected RMetaAnnotatedType caseToTimeOperation(ToTimeOperation expr, Context context) {
				if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
					return withNoMeta(builtins.UNCONSTRAINED_STRING);
				}
				return null;
			}

			@Override
			protected RMetaAnnotatedType caseToEnumOperation(ToEnumOperation expr, Context context) {
				if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
					return withNoMeta(builtins.UNCONSTRAINED_STRING);
				}
				return null;
			}

			@Override
			protected RMetaAnnotatedType caseToDateOperation(ToDateOperation expr, Context context) {
				if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
					return withNoMeta(builtins.UNCONSTRAINED_STRING);
				}
				return null;
			}

			@Override
			protected RMetaAnnotatedType caseToDateTimeOperation(ToDateTimeOperation expr, Context context) {
				if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
					return withNoMeta(builtins.UNCONSTRAINED_STRING);
				}
				return null;
			}

			@Override
			protected RMetaAnnotatedType caseToZonedDateTimeOperation(ToZonedDateTimeOperation expr, Context context) {
				if (ROSETTA_UNARY_OPERATION__ARGUMENT.equals(context.reference)) {
					return withNoMeta(builtins.UNCONSTRAINED_STRING);
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
					if (expr.getCases().stream().allMatch(c -> leavesItemTypeUnchanged(c.getExpression())) && (expr.getDefault() == null || leavesItemTypeUnchanged(expr.getDefault()))) {
						return getExpectedTypeFromContainer(expr);
					}
				} else if (SWITCH_OPERATION__DEFAULT.equals(context.reference)) {
					return getExpectedTypeFromContainer(expr);
				}
				return null;
			}
		}
	}
}
