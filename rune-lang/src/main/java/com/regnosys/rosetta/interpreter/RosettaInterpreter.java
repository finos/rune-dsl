/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.regnosys.rosetta.interpreter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import com.google.common.collect.Streams;
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs;
import com.regnosys.rosetta.rosetta.RosettaSymbol;
import com.regnosys.rosetta.rosetta.expression.ArithmeticOperation;
import com.regnosys.rosetta.rosetta.expression.AsKeyOperation;
import com.regnosys.rosetta.rosetta.expression.ChoiceOperation;
import com.regnosys.rosetta.rosetta.expression.ComparisonOperation;
import com.regnosys.rosetta.rosetta.expression.DefaultOperation;
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
import com.regnosys.rosetta.rosetta.expression.RosettaConstructorExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaContainsExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaCountOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaDeepFeatureCall;
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
import com.regnosys.rosetta.rosetta.expression.RosettaSuperCall;
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference;
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
import com.regnosys.rosetta.rosetta.expression.WithMetaOperation;
import com.regnosys.rosetta.types.RosettaTypeProvider;
import com.regnosys.rosetta.types.TypeSystem;
import com.regnosys.rosetta.types.RType;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;
import com.regnosys.rosetta.utils.RosettaExpressionSwitch;
import com.rosetta.model.lib.RosettaNumber;

public class RosettaInterpreter extends RosettaExpressionSwitch<RosettaValue, RosettaInterpreterContext> {
	@Inject
	private RosettaTypeProvider typeProvider;
	@Inject
	private TypeSystem typeSystem;
	@Inject
	private RBuiltinTypeService builtins;
	@Inject
	private RosettaValueFactory valueFactory;
	
	public RosettaValue interpret(RosettaExpression expr) {
		return interpret(expr, new RosettaInterpreterContext());
	}
	public RosettaValue interpret(RosettaExpression expr, RosettaInterpreterContext context) {
		Objects.requireNonNull(expr);
		Objects.requireNonNull(context);
		return doSwitch(expr, context);
	}
	
	@Override
	protected RosettaValue caseListLiteral(ListLiteral expr, RosettaInterpreterContext context) {
		RType type = typeProvider.getRMetaAnnotatedType(expr).getRType();
		List<?> results = expr.getElements().stream()
				.flatMap(elem -> interpret(elem, context).getItems().stream())
				.collect(Collectors.toList());
		return valueFactory.createOfType(type, results);
	}

	@Override
	protected RosettaValue caseConditionalExpression(RosettaConditionalExpression expr,
			RosettaInterpreterContext context) {
		boolean condition = interpret(expr.getIf(), context).getSingleOrThrow(Boolean.class);
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
	protected RosettaValue caseDeepFeatureCall(RosettaDeepFeatureCall expr, RosettaInterpreterContext context) {
		// TODO
		throw new RosettaInterpreterException("Deep feature calls are not supported yet.");
	}

	@Override
	protected RosettaValue caseBooleanLiteral(RosettaBooleanLiteral expr, RosettaInterpreterContext context) {
		return RosettaBooleanValue.of(expr.isValue());
	}

	@Override
	protected RosettaValue caseIntLiteral(RosettaIntLiteral expr, RosettaInterpreterContext context) {
		return RosettaNumberValue.of(RosettaNumber.valueOf(expr.getValue()));
	}

	@Override
	protected RosettaValue caseNumberLiteral(RosettaNumberLiteral expr, RosettaInterpreterContext context) {
		return RosettaNumberValue.of(new RosettaNumber(expr.stringValue()));
	}

	@Override
	protected RosettaValue caseStringLiteral(RosettaStringLiteral expr, RosettaInterpreterContext context) {
		return RosettaStringValue.of(expr.getValue());
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
		RType type = typeProvider.getRMetaAnnotatedType(expr).getRType();
		if (typeSystem.isSubtypeOf(type, builtins.UNCONSTRAINED_NUMBER)) {
			RosettaNumber left = interpret(expr.getLeft(), context).getSingleOrThrow(RosettaNumber.class);
			RosettaNumber right = interpret(expr.getRight(), context).getSingleOrThrow(RosettaNumber.class);
			RosettaNumber result = left.add(right);
			return valueFactory.createOfType(type, result);
		} else if (typeSystem.isSubtypeOf(type, builtins.UNCONSTRAINED_STRING)) {
			String left = interpret(expr.getLeft(), context).getSingleOrThrow(String.class);
			String right = interpret(expr.getRight(), context).getSingleOrThrow(String.class);
			String result = left + right;
			return RosettaStringValue.of(result);
		} else {
			LocalDate left = interpret(expr.getLeft(), context).getSingleOrThrow(LocalDate.class);
			LocalTime right = interpret(expr.getRight(), context).getSingleOrThrow(LocalTime.class);
			LocalDateTime result = LocalDateTime.of(left, right);
			return RosettaDateTimeValue.of(result);
		}
	}
	
	@Override
	protected RosettaValue caseSubtractOperation(ArithmeticOperation expr, RosettaInterpreterContext context) {
		RType type = typeProvider.getRMetaAnnotatedType(expr).getRType();
		RType leftType = typeProvider.getRMetaAnnotatedType(expr.getLeft()).getRType();
		if (typeSystem.isSubtypeOf(leftType, builtins.UNCONSTRAINED_NUMBER)) {
			RosettaNumber left = interpret(expr.getLeft(), context).getSingleOrThrow(RosettaNumber.class);
			RosettaNumber right = interpret(expr.getRight(), context).getSingleOrThrow(RosettaNumber.class);
			RosettaNumber result = left.subtract(right);
			return valueFactory.createOfType(type, result);
		} else {
			LocalDate left = interpret(expr.getLeft(), context).getSingleOrThrow(LocalDate.class);
			LocalDate right = interpret(expr.getRight(), context).getSingleOrThrow(LocalDate.class);
			RosettaNumber result = RosettaNumber.valueOf(ChronoUnit.DAYS.between(right, left));
			return valueFactory.createOfType(type, result);
		}
	}
	
	@Override
	protected RosettaValue caseMultiplyOperation(ArithmeticOperation expr, RosettaInterpreterContext context) {
		RType type = typeProvider.getRMetaAnnotatedType(expr).getRType();
		RosettaNumber left = interpret(expr.getLeft(), context).getSingleOrThrow(RosettaNumber.class);
		RosettaNumber right = interpret(expr.getRight(), context).getSingleOrThrow(RosettaNumber.class);
		RosettaNumber result = left.multiply(right);
		return valueFactory.createOfType(type, result);
	}
	
	@Override
	protected RosettaValue caseDivideOperation(ArithmeticOperation expr, RosettaInterpreterContext context) {
		RType type = typeProvider.getRMetaAnnotatedType(expr).getRType();
		RosettaNumber left = interpret(expr.getLeft(), context).getSingleOrThrow(RosettaNumber.class);
		RosettaNumber right = interpret(expr.getRight(), context).getSingleOrThrow(RosettaNumber.class);
		RosettaNumber result = left.divide(right);
		return valueFactory.createOfType(type, result);
	}

	@Override
	protected RosettaValue caseJoinOperation(JoinOperation expr, RosettaInterpreterContext context) {
		List<String> argument = interpret(expr.getLeft()).getItems(String.class);
		String separator = interpret(expr.getRight()).getSingleOrThrow(String.class);
		String result = argument.stream().collect(Collectors.joining(separator));
		return RosettaStringValue.of(result);
	}

	@Override
	protected RosettaValue caseAndOperation(LogicalOperation expr, RosettaInterpreterContext context) {
		boolean left = interpret(expr.getLeft(), context).getSingleOrThrow(Boolean.class);
		boolean right = interpret(expr.getRight(), context).getSingleOrThrow(Boolean.class);
		boolean result = left && right;
		return RosettaBooleanValue.of(result);
	}

	@Override
	protected RosettaValue caseOrOperation(LogicalOperation expr, RosettaInterpreterContext context) {
		boolean left = interpret(expr.getLeft(), context).getSingleOrThrow(Boolean.class);
		boolean right = interpret(expr.getRight(), context).getSingleOrThrow(Boolean.class);
		boolean result = left || right;
		return RosettaBooleanValue.of(result);
	}
	
	private RosettaValue caseComparisonOperation(ModifiableBinaryOperation expr, RosettaInterpreterContext context, Function<Integer, Boolean> doCompare) {
		RosettaValueWithNaturalOrder<?> left = interpret(expr.getLeft(), context).withNaturalOrderOrThrow();
		RosettaValueWithNaturalOrder<?> right = interpret(expr.getRight(), context).withNaturalOrderOrThrow();
		boolean result;
		switch (expr.getCardMod()) {
		case NONE:
			Comparable<Object> l = left.getSingleComparableOrThrow();
			result = doCompare.apply(l.compareTo(right.getSingleOrThrow()));
			break;
		case ALL:
			Comparable<Object> r1 = right.getSingleComparableOrThrow();
			result = left.getItems().stream().allMatch(_l -> doCompare.apply(-r1.compareTo(_l)));
			break;
		case ANY:
			Comparable<Object> r2 = right.getSingleComparableOrThrow();
			result = left.getItems().stream().anyMatch(_l -> doCompare.apply(-r2.compareTo(_l)));
			break;
		default:
			throw new UnsupportedOperationException("Unknown cardinality modifier " + expr.getCardMod());
		}
		return RosettaBooleanValue.of(result);
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
			Object r1 = right.getSingleOrThrow();
			result = left.getItems().stream().allMatch(l -> l.equals(r1));
			break;
		case ANY:
			Object r2 = right.getSingleOrThrow();
			result = left.getItems().stream().anyMatch(l -> l.equals(r2));
			break;
		default:
			throw new UnsupportedOperationException("Unknown cardinality modifier " + expr.getCardMod());
		}
		return RosettaBooleanValue.of(result);
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
			Object r1 = right.getSingleOrThrow();
			result = left.getItems().stream().allMatch(l -> !l.equals(r1));
			break;
		case ANY:
			Object r2 = right.getSingleOrThrow();
			result = left.getItems().stream().anyMatch(l -> !l.equals(r2));
			break;
		default:
			throw new UnsupportedOperationException("Unknown cardinality modifier " + expr.getCardMod());
		}
		return RosettaBooleanValue.of(result);
	}

	@Override
	protected RosettaValue caseContainsOperation(RosettaContainsExpression expr, RosettaInterpreterContext context) {
		List<?> left = interpret(expr.getLeft(), context).getItems();
		RosettaValue right = interpret(expr.getRight(), context);
		boolean result = right.stream().allMatch(r -> left.contains(r));
		return RosettaBooleanValue.of(result);
	}
	
	@Override
	protected RosettaValue caseDefaultOperation(DefaultOperation expr, RosettaInterpreterContext context) {
		// TODO
		throw new RosettaInterpreterException("Defult operations are not supported yet.");
	}

	@Override
	protected RosettaValue caseDisjointOperation(RosettaDisjointExpression expr, RosettaInterpreterContext context) {
		RosettaValue left = interpret(expr.getLeft(), context);
		RosettaValue right = interpret(expr.getRight(), context);
		boolean result = Collections.disjoint(left.getItems(), right.getItems());
		return RosettaBooleanValue.of(result);
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
		return RosettaBooleanValue.of(result);
	}

	@Override
	protected RosettaValue caseCountOperation(RosettaCountOperation expr, RosettaInterpreterContext context) {
		RType type = typeProvider.getRMetaAnnotatedType(expr).getRType();
		RosettaValue arg = interpret(expr.getArgument(), context);
		RosettaNumber result = RosettaNumber.valueOf(arg.size());
		return valueFactory.createOfType(type, result);
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
		return RosettaBooleanValue.of(result);
	}

	@Override
	protected RosettaValue caseDistinctOperation(DistinctOperation expr, RosettaInterpreterContext context) {
		// TODO
		throw new RosettaInterpreterException("Distinct operations are not supported yet.");
	}

	@Override
	protected RosettaValue caseFirstOperation(FirstOperation expr, RosettaInterpreterContext context) {
		RType type = typeProvider.getRMetaAnnotatedType(expr).getRType();
		RosettaValue arg = interpret(expr.getArgument(), context);
		if (arg.size() == 0) {
			return RosettaValue.empty();
		} else {
			return valueFactory.createOfType(type, arg.getItems().get(0));
		}
	}

	@Override
	protected RosettaValue caseFlattenOperation(FlattenOperation expr, RosettaInterpreterContext context) {
		// TODO
		throw new RosettaInterpreterException("Flatten operations are not supported yet.");
	}

	@Override
	protected RosettaValue caseLastOperation(LastOperation expr, RosettaInterpreterContext context) {
		RType type = typeProvider.getRMetaAnnotatedType(expr).getRType();
		RosettaValue arg = interpret(expr.getArgument(), context);
		if (arg.size() == 0) {
			return RosettaValue.empty();
		} else {
			return valueFactory.createOfType(type, arg.getItems().get(arg.size() - 1));
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
		RType type = typeProvider.getRMetaAnnotatedType(expr).getRType();
		List<RosettaNumber> arg = interpret(expr.getArgument(), context).getItems(RosettaNumber.class);
		RosettaNumber result = arg.stream().reduce(RosettaNumber.ZERO, RosettaNumber::add);
		return valueFactory.createOfType(type, result);
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
	
	@Override
	protected RosettaValue caseToStringOperation(ToStringOperation expr, RosettaInterpreterContext context) {
		// TODO
		throw new RosettaInterpreterException("ToString operations are not supported yet.");
	}
	
	@Override
	protected RosettaValue caseToNumberOperation(ToNumberOperation expr, RosettaInterpreterContext context) {
		// TODO
		throw new RosettaInterpreterException("ToNumber operations are not supported yet.");
	}
	
	@Override
	protected RosettaValue caseToIntOperation(ToIntOperation expr, RosettaInterpreterContext context) {
		// TODO
		throw new RosettaInterpreterException("ToInt operations are not supported yet.");
	}
	
	@Override
	protected RosettaValue caseToTimeOperation(ToTimeOperation expr, RosettaInterpreterContext context) {
		// TODO
		throw new RosettaInterpreterException("ToTime operations are not supported yet.");
	}
	
	@Override
	protected RosettaValue caseToEnumOperation(ToEnumOperation expr, RosettaInterpreterContext context) {
		// TODO
		throw new RosettaInterpreterException("ToEnum operations are not supported yet.");
	}
	
	@Override
	protected RosettaValue caseToDateOperation(ToDateOperation expr, RosettaInterpreterContext context) {
		// TODO
		throw new RosettaInterpreterException("ToTime operations are not supported yet.");
	}
	
	@Override
	protected RosettaValue caseToDateTimeOperation(ToDateTimeOperation expr, RosettaInterpreterContext context) {
		// TODO
		throw new RosettaInterpreterException("ToTime operations are not supported yet.");
	}
	
	@Override
	protected RosettaValue caseToZonedDateTimeOperation(ToZonedDateTimeOperation expr, RosettaInterpreterContext context) {
		// TODO
		throw new RosettaInterpreterException("ToTime operations are not supported yet.");
	}
	
	@Override
 	protected RosettaValue caseSwitchOperation(SwitchOperation expr, RosettaInterpreterContext context) {
 		// TODO
 		throw new RosettaInterpreterException("Switch operations are not supported yet.");
 	}
	
	@Override
	protected RosettaValue caseConstructorExpression(RosettaConstructorExpression expr,
			RosettaInterpreterContext context) {
		// TODO
		throw new RosettaInterpreterException("Constructor expressions are not supported yet.");
	}
	
	@Override
	protected RosettaValue caseWithMetaOperation(WithMetaOperation expr, RosettaInterpreterContext context) {
 		// TODO
 		throw new RosettaInterpreterException("WithMeta operations are not supported yet.");
	}
	@Override
	protected RosettaValue caseSuperCall(RosettaSuperCall expr, RosettaInterpreterContext context) {
		// TODO
		 		throw new RosettaInterpreterException("super calls are not supported yet.");
	}
}
