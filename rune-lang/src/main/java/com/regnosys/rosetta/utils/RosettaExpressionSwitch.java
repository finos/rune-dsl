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

package com.regnosys.rosetta.utils;

import com.regnosys.rosetta.rosetta.expression.ArithmeticOperation;
import com.regnosys.rosetta.rosetta.expression.AsKeyOperation;
import com.regnosys.rosetta.rosetta.expression.ChoiceOperation;
import com.regnosys.rosetta.rosetta.expression.AsOperation;
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
import com.regnosys.rosetta.rosetta.expression.RosettaBinaryOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaBooleanLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaCallableReference;
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
import com.regnosys.rosetta.rosetta.expression.RosettaLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaNumberLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaOnlyElement;
import com.regnosys.rosetta.rosetta.expression.RosettaOnlyExistsExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaStringLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaSuperCall;
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
import com.regnosys.rosetta.rosetta.expression.WithMetaOperation;

public abstract class RosettaExpressionSwitch<Return, Context> {

	protected Return doSwitch(RosettaExpression expr, Context context) {
		return switch (expr) {
			case RosettaConstructorExpression e -> caseConstructorExpression(e, context);
			case ListLiteral e -> caseListLiteral(e, context);
			case RosettaConditionalExpression e -> caseConditionalExpression(e, context);
			case RosettaFeatureCall e -> caseFeatureCall(e, context);
			case RosettaDeepFeatureCall e -> caseDeepFeatureCall(e, context);
			case RosettaLiteral e -> doSwitch(e, context);
			case RosettaOnlyExistsExpression e -> caseOnlyExists(e, context);
			case RosettaImplicitVariable e -> caseImplicitVariable(e, context);
			case RosettaCallableReference e -> doSwitch(e, context);
			case RosettaOperation e -> doSwitch(e, context);
			case null, default -> throw errorMissedCase(expr);
		};
	}
	protected Return doSwitch(RosettaLiteral expr, Context context) {
		return switch (expr) {
			case RosettaBooleanLiteral e -> caseBooleanLiteral(e, context);
			case RosettaIntLiteral e -> caseIntLiteral(e, context);
			case RosettaNumberLiteral e -> caseNumberLiteral(e, context);
			case RosettaStringLiteral e -> caseStringLiteral(e, context);
			case null, default -> throw errorMissedCase(expr);
		};
	}
	protected Return doSwitch(RosettaCallableReference expr, Context context) {
		return switch (expr) {
			case RosettaSuperCall e -> caseSuperCall(e, context);
			case RosettaSymbolReference e -> caseSymbolReference(e, context);
			case null, default -> throw errorMissedCase(expr);
		};
	}
	protected Return doSwitch(RosettaOperation expr, Context context) {
		return switch (expr) {
			case RosettaBinaryOperation e -> doSwitch(e, context);
			case RosettaUnaryOperation e -> doSwitch(e, context);
			case null, default -> throw errorMissedCase(expr);
		};
	}
	protected Return doSwitch(RosettaBinaryOperation expr, Context context) {
		return switch (expr) {
			case ArithmeticOperation e -> doSwitch(e, context);
			case JoinOperation e -> caseJoinOperation(e, context);
			case LogicalOperation e -> doSwitch(e, context);
			case ModifiableBinaryOperation e -> doSwitch(e, context);
			case RosettaContainsExpression e -> caseContainsOperation(e, context);
			case RosettaDisjointExpression e -> caseDisjointOperation(e, context);
			case DefaultOperation e -> caseDefaultOperation(e, context);
			case null, default -> throw errorMissedCase(expr);
		};
	}
	protected Return doSwitch(ArithmeticOperation expr, Context context) {
		return switch (expr.getOperator()) {
			case "+" -> caseAddOperation(expr, context);
			case "-" -> caseSubtractOperation(expr, context);
			case "*" -> caseMultiplyOperation(expr, context);
			case "/" -> caseDivideOperation(expr, context);
			default -> throw errorMissedCase(expr);
		};
	}
	protected Return doSwitch(LogicalOperation expr, Context context) {
		return switch (expr.getOperator()) {
			case "and" -> caseAndOperation(expr, context);
			case "or" -> caseOrOperation(expr, context);
			default -> throw errorMissedCase(expr);
		};
	}
	protected Return doSwitch(ModifiableBinaryOperation expr, Context context) {
		return switch (expr) {
			case ComparisonOperation e -> doSwitch(e, context);
			case EqualityOperation e -> doSwitch(e, context);
			case null, default -> throw errorMissedCase(expr);
		};
	}
	protected Return doSwitch(ComparisonOperation expr, Context context) {
		return switch (expr.getOperator()) {
			case "<" -> caseLessThanOperation(expr, context);
			case "<=" -> caseLessThanOrEqualOperation(expr, context);
			case ">" -> caseGreaterThanOperation(expr, context);
			case ">=" -> caseGreaterThanOrEqualOperation(expr, context);
			default -> throw errorMissedCase(expr);
		};
	}
	protected Return doSwitch(EqualityOperation expr, Context context) {
		return switch (expr.getOperator()) {
			case "=" -> caseEqualsOperation(expr, context);
			case "<>" -> caseNotEqualsOperation(expr, context);
			default -> throw errorMissedCase(expr);
		};
	}
	protected Return doSwitch(RosettaUnaryOperation expr, Context context) {
		return switch (expr) {
			case AsKeyOperation e -> caseAsKeyOperation(e, context);
			case ChoiceOperation e -> caseChoiceOperation(e, context);
			case OneOfOperation e -> caseOneOfOperation(e, context);
			case RosettaAbsentExpression e -> caseAbsentOperation(e, context);
			case RosettaCountOperation e -> caseCountOperation(e, context);
			case RosettaExistsExpression e -> caseExistsOperation(e, context);
			case DistinctOperation e -> caseDistinctOperation(e, context);
			case FirstOperation e -> caseFirstOperation(e, context);
			case FlattenOperation e -> caseFlattenOperation(e, context);
			case LastOperation e -> caseLastOperation(e, context);
			case ReverseOperation e -> caseReverseOperation(e, context);
			case RosettaOnlyElement e -> caseOnlyElementOperation(e, context);
			case SumOperation e -> caseSumOperation(e, context);
			case ToStringOperation e -> caseToStringOperation(e, context);
			case ToNumberOperation e -> caseToNumberOperation(e, context);
			case ToIntOperation e -> caseToIntOperation(e, context);
			case ToTimeOperation e -> caseToTimeOperation(e, context);
			case ToEnumOperation e -> caseToEnumOperation(e, context);
			case AsOperation e -> caseAsOperation(e, context);
			case ToDateOperation e -> caseToDateOperation(e, context);
			case ToDateTimeOperation e -> caseToDateTimeOperation(e, context);
			case ToZonedDateTimeOperation e -> caseToZonedDateTimeOperation(e, context);
			case SwitchOperation e -> caseSwitchOperation(e, context);
			case WithMetaOperation e -> caseWithMetaOperation(e, context);
			case RosettaFunctionalOperation e -> doSwitch(e, context);
			case null, default -> throw errorMissedCase(expr);
		};
	}
	protected Return doSwitch(RosettaFunctionalOperation expr, Context context) {
		return switch (expr) {
			case FilterOperation e -> caseFilterOperation(e, context);
			case MapOperation e -> caseMapOperation(e, context);
			case MaxOperation e -> caseMaxOperation(e, context);
			case MinOperation e -> caseMinOperation(e, context);
			case ReduceOperation e -> caseReduceOperation(e, context);
			case SortOperation e -> caseSortOperation(e, context);
			case ThenOperation e -> caseThenOperation(e, context);
			case null, default -> throw errorMissedCase(expr);
		};
	}
	private UnsupportedOperationException errorMissedCase(RosettaExpression expr) {
		String className = expr == null ? "null" : expr.getClass().getCanonicalName();
		return new UnsupportedOperationException("Unexpected expression of type " + className);
	}
	
	protected abstract Return caseConstructorExpression(RosettaConstructorExpression expr, Context context);
	
	protected abstract Return caseListLiteral(ListLiteral expr, Context context);
	
	protected abstract Return caseConditionalExpression(RosettaConditionalExpression expr, Context context);
	
	protected abstract Return caseFeatureCall(RosettaFeatureCall expr, Context context);
	protected abstract Return caseDeepFeatureCall(RosettaDeepFeatureCall expr, Context context);
	
	protected abstract Return caseBooleanLiteral(RosettaBooleanLiteral expr, Context context);
	protected abstract Return caseIntLiteral(RosettaIntLiteral expr, Context context);
	protected abstract Return caseNumberLiteral(RosettaNumberLiteral expr, Context context);
	protected abstract Return caseStringLiteral(RosettaStringLiteral expr, Context context);
	
	protected abstract Return caseOnlyExists(RosettaOnlyExistsExpression expr, Context context);
	
	protected abstract Return caseImplicitVariable(RosettaImplicitVariable expr, Context context);
	protected abstract Return caseSymbolReference(RosettaSymbolReference expr, Context context);
	protected abstract Return caseSuperCall(RosettaSuperCall expr, Context context);
	
	protected abstract Return caseAddOperation(ArithmeticOperation expr, Context context);
	protected abstract Return caseSubtractOperation(ArithmeticOperation expr, Context context);
	protected abstract Return caseMultiplyOperation(ArithmeticOperation expr, Context context);
	protected abstract Return caseDivideOperation(ArithmeticOperation expr, Context context);
	protected abstract Return caseJoinOperation(JoinOperation expr, Context context);
	protected abstract Return caseAndOperation(LogicalOperation expr, Context context);
	protected abstract Return caseOrOperation(LogicalOperation expr, Context context);
	protected abstract Return caseLessThanOperation(ComparisonOperation expr, Context context);
	protected abstract Return caseLessThanOrEqualOperation(ComparisonOperation expr, Context context);
	protected abstract Return caseGreaterThanOperation(ComparisonOperation expr, Context context);
	protected abstract Return caseGreaterThanOrEqualOperation(ComparisonOperation expr, Context context);
	protected abstract Return caseEqualsOperation(EqualityOperation expr, Context context);
	protected abstract Return caseNotEqualsOperation(EqualityOperation expr, Context context);
	protected abstract Return caseContainsOperation(RosettaContainsExpression expr, Context context);
	protected abstract Return caseDisjointOperation(RosettaDisjointExpression expr, Context context);
	protected abstract Return caseDefaultOperation(DefaultOperation expr, Context context);

	protected abstract Return caseAsKeyOperation(AsKeyOperation expr, Context context);
	protected abstract Return caseChoiceOperation(ChoiceOperation expr, Context context);
	protected abstract Return caseOneOfOperation(OneOfOperation expr, Context context);
	protected abstract Return caseAbsentOperation(RosettaAbsentExpression expr, Context context);
	protected abstract Return caseCountOperation(RosettaCountOperation expr, Context context);
	protected abstract Return caseExistsOperation(RosettaExistsExpression expr, Context context);
	protected abstract Return caseDistinctOperation(DistinctOperation expr, Context context);
	protected abstract Return caseFirstOperation(FirstOperation expr, Context context);
	protected abstract Return caseFlattenOperation(FlattenOperation expr, Context context);
	protected abstract Return caseLastOperation(LastOperation expr, Context context);
	protected abstract Return caseReverseOperation(ReverseOperation expr, Context context);
	protected abstract Return caseOnlyElementOperation(RosettaOnlyElement expr, Context context);
	protected abstract Return caseSumOperation(SumOperation expr, Context context);
	protected abstract Return caseToStringOperation(ToStringOperation expr, Context context);
	protected abstract Return caseToNumberOperation(ToNumberOperation expr, Context context);
	protected abstract Return caseToIntOperation(ToIntOperation expr, Context context);
	protected abstract Return caseToTimeOperation(ToTimeOperation expr, Context context);
	protected abstract Return caseToEnumOperation(ToEnumOperation expr, Context context);
	protected abstract Return caseAsOperation(AsOperation expr, Context context);
	protected abstract Return caseToDateOperation(ToDateOperation expr, Context context);
	protected abstract Return caseToDateTimeOperation(ToDateTimeOperation expr, Context context);
	protected abstract Return caseToZonedDateTimeOperation(ToZonedDateTimeOperation expr, Context context);
	protected abstract Return caseSwitchOperation(SwitchOperation expr, Context context);
	protected abstract Return caseWithMetaOperation(WithMetaOperation expr, Context context);
	
	protected abstract Return caseFilterOperation(FilterOperation expr, Context context);
	protected abstract Return caseMapOperation(MapOperation expr, Context context);
	protected abstract Return caseMaxOperation(MaxOperation expr, Context context);
	protected abstract Return caseMinOperation(MinOperation expr, Context context);
	protected abstract Return caseReduceOperation(ReduceOperation expr, Context context);
	protected abstract Return caseSortOperation(SortOperation expr, Context context);
	protected abstract Return caseThenOperation(ThenOperation expr, Context context);
}
