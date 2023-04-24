package com.regnosys.rosetta.interpreter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;

import com.google.common.collect.Streams;
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs;
import com.regnosys.rosetta.rosetta.RosettaSymbol;
import com.regnosys.rosetta.rosetta.expression.ArithmeticOperation;
import com.regnosys.rosetta.rosetta.expression.AsKeyOperation;
import com.regnosys.rosetta.rosetta.expression.ChoiceOperation;
import com.regnosys.rosetta.rosetta.expression.ComparisonOperation;
import com.regnosys.rosetta.rosetta.expression.DistinctOperation;
import com.regnosys.rosetta.rosetta.expression.EqualityOperation;
import com.regnosys.rosetta.rosetta.expression.FilterOperation;
import com.regnosys.rosetta.rosetta.expression.FirstOperation;
import com.regnosys.rosetta.rosetta.expression.FlattenOperation;
import com.regnosys.rosetta.rosetta.expression.JoinOperation;
import com.regnosys.rosetta.rosetta.expression.LastOperation;
import com.regnosys.rosetta.rosetta.expression.ListLiteral;
import com.regnosys.rosetta.rosetta.expression.LogicalOperation;
import com.regnosys.rosetta.rosetta.expression.MapOperation;
import com.regnosys.rosetta.rosetta.expression.MaxOperation;
import com.regnosys.rosetta.rosetta.expression.MinOperation;
import com.regnosys.rosetta.rosetta.expression.ModifiableBinaryOperation;
import com.regnosys.rosetta.rosetta.expression.OneOfOperation;
import com.regnosys.rosetta.rosetta.expression.ReduceOperation;
import com.regnosys.rosetta.rosetta.expression.ReverseOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaAbsentExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaBooleanLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaConditionalExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaContainsExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaCountOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaDisjointExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaExistsExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaFeatureCall;
import com.regnosys.rosetta.rosetta.expression.RosettaImplicitVariable;
import com.regnosys.rosetta.rosetta.expression.RosettaIntLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaNumberLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaOnlyElement;
import com.regnosys.rosetta.rosetta.expression.RosettaOnlyExistsExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaStringLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference;
import com.regnosys.rosetta.rosetta.expression.SortOperation;
import com.regnosys.rosetta.rosetta.expression.SumOperation;
import com.regnosys.rosetta.rosetta.expression.ThenOperation;
import com.regnosys.rosetta.types.RosettaTypeProvider;
import com.regnosys.rosetta.types.RType;
import com.regnosys.rosetta.types.builtin.RNumberType;
import com.regnosys.rosetta.types.builtin.RStringType;
import com.regnosys.rosetta.utils.RosettaExpressionSwitch;

public class RosettaInterpreter extends RosettaExpressionSwitch<RosettaValue, RosettaInterpreterContext> {
	@Inject
	private RosettaTypeProvider typeProvider;
	
	public RosettaValue interpret(RosettaExpression expr) {
		return interpret(expr, new RosettaInterpreterContext());
	}
	public RosettaValue interpret(RosettaExpression expr, RosettaInterpreterContext context) {
		Validate.notNull(expr);
		Validate.notNull(context);
		return doSwitch(expr, context);
	}
	
	@Override
	protected RosettaValue caseListLiteral(ListLiteral expr, RosettaInterpreterContext context) {
		List<RosettaValueItem> results = expr.getElements().stream()
				.flatMap(elem -> interpret(elem, context).getItems().stream())
				.collect(Collectors.toList());
		return new RosettaValue(results);
	}

	@Override
	protected RosettaValue caseConditionalExpression(RosettaConditionalExpression expr,
			RosettaInterpreterContext context) {
		boolean condition = interpret(expr.getIf(), context).getSingleBooleanOrThrow();
		if (condition) {
			return interpret(expr.getIfthen(), context);
		} else {
			return interpret(expr.getElsethen(), context);
		}
	}

	@Override
	protected RosettaValue caseFeatureCall(RosettaFeatureCall expr, RosettaInterpreterContext context) {
		// TODO
		throw new RosettaInterpreterException("Feature calls are not supported yet.");
	}

	@Override
	protected RosettaValue caseBooleanLiteral(RosettaBooleanLiteral expr, RosettaInterpreterContext context) {
		return RosettaValue.of(expr.isValue());
	}

	@Override
	protected RosettaValue caseIntLiteral(RosettaIntLiteral expr, RosettaInterpreterContext context) {
		return RosettaValue.of(RosettaNumber.valueOf(expr.getValue()));
	}

	@Override
	protected RosettaValue caseNumberLiteral(RosettaNumberLiteral expr, RosettaInterpreterContext context) {
		return RosettaValue.of(new RosettaNumber(expr.stringValue()));
	}

	@Override
	protected RosettaValue caseStringLiteral(RosettaStringLiteral expr, RosettaInterpreterContext context) {
		return RosettaValue.of(expr.getValue());
	}

	@Override
	protected RosettaValue caseOnlyExists(RosettaOnlyExistsExpression expr, RosettaInterpreterContext context) {
		// TODO
		throw new RosettaInterpreterException("Only exists operations are not supported yet.");
	}

	@Override
	protected RosettaValue caseImplicitVariable(RosettaImplicitVariable expr, RosettaInterpreterContext context) {
		// TODO
		throw new RosettaInterpreterException("Implicit variables are not supported yet.");
	}

	@Override
	protected RosettaValue caseSymbolReference(RosettaSymbolReference expr, RosettaInterpreterContext context) {
		RosettaSymbol symbol = expr.getSymbol();
		if (symbol instanceof RosettaCallableWithArgs) {
			// TODO
			throw new RosettaInterpreterException("Function calls are not supported yet.");
		} else {
			return context.getVariableValue(symbol);
		}
	}

	@Override
	protected RosettaValue caseAddOperation(ArithmeticOperation expr, RosettaInterpreterContext context) {
		RType leftType = typeProvider.getRType(expr.getLeft());
		if (leftType instanceof RNumberType) {
			RosettaNumber left = interpret(expr.getLeft(), context).getSingleNumberOrThrow();
			RosettaNumber right = interpret(expr.getRight(), context).getSingleNumberOrThrow();
			RosettaNumber result = left.add(right);
			return RosettaValue.of(result);
		} else if (leftType instanceof RStringType) {
			String left = interpret(expr.getLeft(), context).getSingleStringOrThrow();
			String right = interpret(expr.getRight(), context).getSingleStringOrThrow();
			String result = left + right;
			return RosettaValue.of(result);
		} else {
			LocalDate left = interpret(expr.getLeft(), context).getSingleDateOrThrow();
			LocalTime right = interpret(expr.getRight(), context).getSingleTimeOrThrow();
			LocalDateTime result = LocalDateTime.of(left, right);
			return RosettaValue.of(result);
		}
	}
	
	@Override
	protected RosettaValue caseSubtractOperation(ArithmeticOperation expr, RosettaInterpreterContext context) {
		RType leftType = typeProvider.getRType(expr.getLeft());
		if (leftType instanceof RNumberType) {
			RosettaNumber left = interpret(expr.getLeft(), context).getSingleNumberOrThrow();
			RosettaNumber right = interpret(expr.getRight(), context).getSingleNumberOrThrow();
			RosettaNumber result = left.subtract(right);
			return RosettaValue.of(result);
		} else {
			LocalDate left = interpret(expr.getLeft(), context).getSingleDateOrThrow();
			LocalDate right = interpret(expr.getRight(), context).getSingleDateOrThrow();
			RosettaNumber result = RosettaNumber.valueOf(ChronoUnit.DAYS.between(right, left));
			return RosettaValue.of(result);
		}
	}
	
	@Override
	protected RosettaValue caseMultiplyOperation(ArithmeticOperation expr, RosettaInterpreterContext context) {
		RosettaNumber left = interpret(expr.getLeft(), context).getSingleNumberOrThrow();
		RosettaNumber right = interpret(expr.getRight(), context).getSingleNumberOrThrow();
		RosettaNumber result = left.multiply(right);
		return RosettaValue.of(result);
	}
	
	@Override
	protected RosettaValue caseDivideOperation(ArithmeticOperation expr, RosettaInterpreterContext context) {
		RosettaNumber left = interpret(expr.getLeft(), context).getSingleNumberOrThrow();
		RosettaNumber right = interpret(expr.getRight(), context).getSingleNumberOrThrow();
		RosettaNumber result = left.divide(right);
		return RosettaValue.of(result);
	}

	@Override
	protected RosettaValue caseJoinOperation(JoinOperation expr, RosettaInterpreterContext context) {
		List<String> argument = interpret(expr.getLeft()).getItemsAsString();
		String separator = interpret(expr.getRight()).getSingleStringOrThrow();
		String result = argument.stream().collect(Collectors.joining(separator));
		return RosettaValue.of(result);
	}

	@Override
	protected RosettaValue caseAndOperation(LogicalOperation expr, RosettaInterpreterContext context) {
		boolean left = interpret(expr.getLeft(), context).getSingleBooleanOrThrow();
		boolean right = interpret(expr.getRight(), context).getSingleBooleanOrThrow();
		boolean result = left && right;
		return RosettaValue.of(result);
	}

	@Override
	protected RosettaValue caseOrOperation(LogicalOperation expr, RosettaInterpreterContext context) {
		boolean left = interpret(expr.getLeft(), context).getSingleBooleanOrThrow();
		boolean right = interpret(expr.getRight(), context).getSingleBooleanOrThrow();
		boolean result = left || right;
		return RosettaValue.of(result);
	}
	
	private RosettaValue caseComparisonOperation(ModifiableBinaryOperation expr, RosettaInterpreterContext context, Function<Integer, Boolean> computeResult) {
		RosettaValue left = interpret(expr.getLeft(), context);
		RosettaValue right = interpret(expr.getRight(), context);
		RosettaValueItemWithNaturalOrder<?> r = right.getSingleWithNaturalOrderOrThrow();
		boolean result;
		switch (expr.getCardMod()) {
		case NONE:
			RosettaValueItemWithNaturalOrder<?> l = left.getSingleWithNaturalOrderOrThrow();
			result = computeResult.apply(l.compareTo(r));
			break;
		case ALL:
			result = left.getItemsOfType(r.getClass()).stream().allMatch(_l -> computeResult.apply(_l.compareTo(r)));
			break;
		case ANY:
			result = left.getItemsOfType(r.getClass()).stream().anyMatch(_l -> computeResult.apply(_l.compareTo(r)));
			break;
		default:
			throw new UnsupportedOperationException("Unknown cardinality modifier " + expr.getCardMod());
		}
		return RosettaValue.of(result);
	}

	@Override
	protected RosettaValue caseLessThanOperation(ComparisonOperation expr, RosettaInterpreterContext context) {
		return caseComparisonOperation(expr, context, v -> v < 0);
	}

	@Override
	protected RosettaValue caseLessThanOrEqualOperation(ComparisonOperation expr, RosettaInterpreterContext context) {
		return caseComparisonOperation(expr, context, v -> v <= 0);
	}

	@Override
	protected RosettaValue caseGreaterThanOperation(ComparisonOperation expr, RosettaInterpreterContext context) {
		return caseComparisonOperation(expr, context, v -> v > 0);
	}

	@Override
	protected RosettaValue caseGreaterThanOrEqualOperation(ComparisonOperation expr,
			RosettaInterpreterContext context) {
		return caseComparisonOperation(expr, context, v -> v >= 0);
	}
	
	@Override
	protected RosettaValue caseEqualsOperation(EqualityOperation expr, RosettaInterpreterContext context) {
		RosettaValue left = interpret(expr.getLeft(), context);
		RosettaValue right = interpret(expr.getRight(), context);
		boolean result;
		switch (expr.getCardMod()) {
		case NONE:
			result = left.equals(right);
			break;
		case ALL:
			RosettaValueItem r1 = right.getSingleOrThrow();
			result = left.getItems().stream().allMatch(l -> l.equals(r1));
			break;
		case ANY:
			RosettaValueItem r2 = right.getSingleOrThrow();
			result = left.getItems().stream().anyMatch(l -> l.equals(r2));
			break;
		default:
			throw new UnsupportedOperationException("Unknown cardinality modifier " + expr.getCardMod());
		}
		return RosettaValue.of(result);
	}
	
	@Override
	protected RosettaValue caseNotEqualsOperation(EqualityOperation expr, RosettaInterpreterContext context) {
		RosettaValue left = interpret(expr.getLeft(), context);
		RosettaValue right = interpret(expr.getRight(), context);
		boolean result;
		switch (expr.getCardMod()) {
		case NONE:
			result = left.size() != right.size() || 
				Streams.zip(left.stream(), right.stream(), (l, r) -> !l.equals(r))
					.allMatch(c -> c);
			break;
		case ALL:
			RosettaValueItem r1 = right.getSingleOrThrow();
			result = left.getItems().stream().allMatch(l -> !l.equals(r1));
			break;
		case ANY:
			RosettaValueItem r2 = right.getSingleOrThrow();
			result = left.getItems().stream().anyMatch(l -> !l.equals(r2));
			break;
		default:
			throw new UnsupportedOperationException("Unknown cardinality modifier " + expr.getCardMod());
		}
		return RosettaValue.of(result);
	}

	@Override
	protected RosettaValue caseContainsOperation(RosettaContainsExpression expr, RosettaInterpreterContext context) {
		List<RosettaValueItem> left = interpret(expr.getLeft(), context).getItems();
		RosettaValue right = interpret(expr.getRight(), context);
		boolean result = right.stream().allMatch(r -> left.contains(r));
		return RosettaValue.of(result);
	}

	@Override
	protected RosettaValue caseDisjointOperation(RosettaDisjointExpression expr, RosettaInterpreterContext context) {
		RosettaValue left = interpret(expr.getLeft(), context);
		RosettaValue right = interpret(expr.getRight(), context);
		boolean result = Collections.disjoint(left.getItems(), right.getItems());
		return RosettaValue.of(result);
	}

	@Override
	protected RosettaValue caseAsKeyOperation(AsKeyOperation expr, RosettaInterpreterContext context) {
		// TODO
		throw new RosettaInterpreterException("As key operations are not supported yet.");
	}

	@Override
	protected RosettaValue caseChoiceOperation(ChoiceOperation expr, RosettaInterpreterContext context) {
		// TODO
		throw new RosettaInterpreterException("Choice operations are not supported yet.");
	}

	@Override
	protected RosettaValue caseOneOfOperation(OneOfOperation expr, RosettaInterpreterContext context) {
		// TODO
		throw new RosettaInterpreterException("One of operations are not supported yet.");
	}

	@Override
	protected RosettaValue caseAbsentOperation(RosettaAbsentExpression expr, RosettaInterpreterContext context) {
		RosettaValue arg = interpret(expr.getArgument(), context);
		boolean result = arg.size() == 0;
		return RosettaValue.of(result);
	}

	@Override
	protected RosettaValue caseCountOperation(RosettaCountOperation expr, RosettaInterpreterContext context) {
		RosettaValue arg = interpret(expr.getArgument(), context);
		RosettaNumber result = RosettaNumber.valueOf(arg.size());
		return RosettaValue.of(result);
	}

	@Override
	protected RosettaValue caseExistsOperation(RosettaExistsExpression expr, RosettaInterpreterContext context) {
		RosettaValue arg = interpret(expr.getArgument(), context);
		boolean result;
		switch (expr.getModifier()) {
		case NONE:
			result = arg.size() >= 1;
			break;
		case SINGLE:
			result = arg.size() == 1;
			break;
		case MULTIPLE:
			result = arg.size() >= 2;
			break;
		default:
			throw new UnsupportedOperationException("Unknown exists modifier " + expr.getModifier());
		}
		return RosettaValue.of(result);
	}

	@Override
	protected RosettaValue caseDistinctOperation(DistinctOperation expr, RosettaInterpreterContext context) {
		// TODO
		throw new RosettaInterpreterException("Distinct operations are not supported yet.");
	}

	@Override
	protected RosettaValue caseFirstOperation(FirstOperation expr, RosettaInterpreterContext context) {
		RosettaValue arg = interpret(expr.getArgument(), context);
		if (arg.size() == 0) {
			return RosettaValue.empty();
		} else {
			return RosettaValue.of(arg.getItems().get(0));
		}
	}

	@Override
	protected RosettaValue caseFlattenOperation(FlattenOperation expr, RosettaInterpreterContext context) {
		// TODO
		throw new RosettaInterpreterException("Flatten operations are not supported yet.");
	}

	@Override
	protected RosettaValue caseLastOperation(LastOperation expr, RosettaInterpreterContext context) {
		RosettaValue arg = interpret(expr.getArgument(), context);
		if (arg.size() == 0) {
			return RosettaValue.empty();
		} else {
			return RosettaValue.of(arg.getItems().get(arg.size() - 1));
		}
	}

	@Override
	protected RosettaValue caseReverseOperation(ReverseOperation expr, RosettaInterpreterContext context) {
		// TODO
		throw new RosettaInterpreterException("Reverse operations are not supported yet.");
	}

	@Override
	protected RosettaValue caseOnlyElementOperation(RosettaOnlyElement expr, RosettaInterpreterContext context) {
		RosettaValue arg = interpret(expr.getArgument(), context);
		if (arg.size() != 1) {
			return RosettaValue.empty();
		} else {
			return arg;
		}
	}

	@Override
	protected RosettaValue caseSumOperation(SumOperation expr, RosettaInterpreterContext context) {
		List<RosettaNumber> arg = interpret(expr.getArgument(), context).getItemsAsNumber();
		RosettaNumber result = arg.stream().reduce(RosettaNumber.ZERO, RosettaNumber::add);
		return RosettaValue.of(result);
	}

	@Override
	protected RosettaValue caseFilterOperation(FilterOperation expr, RosettaInterpreterContext context) {
		// TODO
		throw new RosettaInterpreterException("Filter operations are not supported yet.");
	}

	@Override
	protected RosettaValue caseMapOperation(MapOperation expr, RosettaInterpreterContext context) {
		// TODO
		throw new RosettaInterpreterException("Map operations are not supported yet.");
	}

	@Override
	protected RosettaValue caseMaxOperation(MaxOperation expr, RosettaInterpreterContext context) {
		// TODO
		throw new RosettaInterpreterException("Max operations are not supported yet.");
	}

	@Override
	protected RosettaValue caseMinOperation(MinOperation expr, RosettaInterpreterContext context) {
		// TODO
		throw new RosettaInterpreterException("Min operations are not supported yet.");
	}

	@Override
	protected RosettaValue caseReduceOperation(ReduceOperation expr, RosettaInterpreterContext context) {
		// TODO
		throw new RosettaInterpreterException("Reduce operations are not supported yet.");
	}

	@Override
	protected RosettaValue caseSortOperation(SortOperation expr, RosettaInterpreterContext context) {
		// TODO
		throw new RosettaInterpreterException("Sort operations are not supported yet.");
	}

	@Override
	protected RosettaValue caseThenOperation(ThenOperation expr, RosettaInterpreterContext context) {
		// TODO
		throw new RosettaInterpreterException("Then operations are not supported yet.");
	}
}
