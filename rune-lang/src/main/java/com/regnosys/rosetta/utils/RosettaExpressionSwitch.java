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
		if (expr instanceof RosettaConstructorExpression) {
			return caseConstructorExpression((RosettaConstructorExpression)expr, context);
		} else if (expr instanceof ListLiteral) {
			return caseListLiteral((ListLiteral)expr, context);
		} else if (expr instanceof RosettaConditionalExpression) {
			return caseConditionalExpression((RosettaConditionalExpression)expr, context);
		} else if (expr instanceof RosettaFeatureCall) {
			return caseFeatureCall((RosettaFeatureCall)expr, context);
		} else if (expr instanceof RosettaDeepFeatureCall) {
			return caseDeepFeatureCall((RosettaDeepFeatureCall)expr, context);
		} else if (expr instanceof RosettaLiteral) {
			return doSwitch((RosettaLiteral)expr, context);
		} else if (expr instanceof RosettaOnlyExistsExpression) {
			return caseOnlyExists((RosettaOnlyExistsExpression)expr, context);
		} else if (expr instanceof RosettaImplicitVariable) {
			return caseImplicitVariable((RosettaImplicitVariable)expr, context);
		} else if (expr instanceof RosettaCallableReference) {
			return doSwitch((RosettaCallableReference)expr, context);
		} else if (expr instanceof RosettaOperation) {
			return doSwitch((RosettaOperation)expr, context);
		}
		throw errorMissedCase(expr);
	}
	protected Return doSwitch(RosettaLiteral expr, Context context) {
		if (expr instanceof RosettaBooleanLiteral) {
			return caseBooleanLiteral((RosettaBooleanLiteral)expr, context);
		} else if (expr instanceof RosettaIntLiteral) {
			return caseIntLiteral((RosettaIntLiteral)expr, context);
		} else if (expr instanceof RosettaNumberLiteral) {
			return caseNumberLiteral((RosettaNumberLiteral)expr, context);
		} else if (expr instanceof RosettaStringLiteral) {
			return caseStringLiteral((RosettaStringLiteral)expr, context);
		}
		throw errorMissedCase(expr);
	}
	protected Return doSwitch(RosettaCallableReference expr, Context context) {
		if (expr instanceof RosettaSymbolReference) {
			return caseSymbolReference((RosettaSymbolReference)expr, context);
		}
		throw errorMissedCase(expr);
	}
	protected Return doSwitch(RosettaOperation expr, Context context) {
		if (expr instanceof RosettaBinaryOperation) {
			return doSwitch((RosettaBinaryOperation)expr, context);
		} else if (expr instanceof RosettaUnaryOperation) {
			return doSwitch((RosettaUnaryOperation)expr, context);
		}
		throw errorMissedCase(expr);
	}
	protected Return doSwitch(RosettaBinaryOperation expr, Context context) {
		if (expr instanceof ArithmeticOperation) {
			return doSwitch((ArithmeticOperation)expr, context);
		} else if (expr instanceof JoinOperation) {
			return caseJoinOperation((JoinOperation)expr, context);
		} else if (expr instanceof LogicalOperation) {
			return doSwitch((LogicalOperation)expr, context);
		} else if (expr instanceof ModifiableBinaryOperation) {
			return doSwitch((ModifiableBinaryOperation)expr, context);
		} else if (expr instanceof RosettaContainsExpression) {
			return caseContainsOperation((RosettaContainsExpression)expr, context);
		} else if (expr instanceof RosettaDisjointExpression) {
			return caseDisjointOperation((RosettaDisjointExpression)expr, context);
		} else if (expr instanceof DefaultOperation) {
			return caseDefaultOperation((DefaultOperation)expr, context);
		}
		throw errorMissedCase(expr);
	}
	protected Return doSwitch(ArithmeticOperation expr, Context context) {
		if (expr.getOperator().equals("+")) {
			return caseAddOperation(expr, context);
		} else if (expr.getOperator().equals("-")) {
			return caseSubtractOperation(expr, context);
		} else if (expr.getOperator().equals("*")) {
			return caseMultiplyOperation(expr, context);
		} else if (expr.getOperator().equals("/")) {
			return caseDivideOperation(expr, context);
		}
		throw errorMissedCase(expr);
	}
	protected Return doSwitch(LogicalOperation expr, Context context) {
		if (expr.getOperator().equals("and")) {
			return caseAndOperation(expr, context);
		} else if (expr.getOperator().equals("or")) {
			return caseOrOperation(expr, context);
		}
		throw errorMissedCase(expr);
	}
	protected Return doSwitch(ModifiableBinaryOperation expr, Context context) {
		if (expr instanceof ComparisonOperation) {
			return doSwitch((ComparisonOperation)expr, context);
		} else if (expr instanceof EqualityOperation) {
			return doSwitch((EqualityOperation)expr, context);
		}
		throw errorMissedCase(expr);
	}
	protected Return doSwitch(ComparisonOperation expr, Context context) {
		if (expr.getOperator().equals("<")) {
			return caseLessThanOperation(expr, context);
		} else if (expr.getOperator().equals("<=")) {
			return caseLessThanOrEqualOperation(expr, context);
		} else if (expr.getOperator().equals(">")) {
			return caseGreaterThanOperation(expr, context);
		} else if (expr.getOperator().equals(">=")) {
			return caseGreaterThanOrEqualOperation(expr, context);
		}
		throw errorMissedCase(expr);
	}
	protected Return doSwitch(EqualityOperation expr, Context context) {
		if (expr.getOperator().equals("=")) {
			return caseEqualsOperation(expr, context);
		} else if (expr.getOperator().equals("<>")) {
			return caseNotEqualsOperation(expr, context);
		}
		throw errorMissedCase(expr);
	}
	protected Return doSwitch(RosettaUnaryOperation expr, Context context) {
		if (expr instanceof AsKeyOperation) {
			return caseAsKeyOperation((AsKeyOperation)expr, context);
		} else if (expr instanceof ChoiceOperation) {
			return caseChoiceOperation((ChoiceOperation)expr, context);
		} else if (expr instanceof OneOfOperation) {
			return caseOneOfOperation((OneOfOperation)expr, context);
		} else if (expr instanceof RosettaAbsentExpression) {
			return caseAbsentOperation((RosettaAbsentExpression)expr, context);
		} else if (expr instanceof RosettaCountOperation) {
			return caseCountOperation((RosettaCountOperation)expr, context);
		} else if (expr instanceof RosettaExistsExpression) {
			return caseExistsOperation((RosettaExistsExpression)expr, context);
		} else if (expr instanceof DistinctOperation) {
			return caseDistinctOperation((DistinctOperation)expr, context);
		} else if (expr instanceof FirstOperation) {
			return caseFirstOperation((FirstOperation)expr, context);
		} else if (expr instanceof FlattenOperation) {
			return caseFlattenOperation((FlattenOperation)expr, context);
		} else if (expr instanceof LastOperation) {
			return caseLastOperation((LastOperation)expr, context);
		} else if (expr instanceof ReverseOperation) {
			return caseReverseOperation((ReverseOperation)expr, context);
		} else if (expr instanceof RosettaOnlyElement) {
			return caseOnlyElementOperation((RosettaOnlyElement)expr, context);
		} else if (expr instanceof SumOperation) {
			return caseSumOperation((SumOperation)expr, context);
		} else if (expr instanceof ToStringOperation) {
			return caseToStringOperation((ToStringOperation)expr, context);
		} else if (expr instanceof ToNumberOperation) {
			return caseToNumberOperation((ToNumberOperation)expr, context);
		} else if (expr instanceof ToIntOperation) {
			return caseToIntOperation((ToIntOperation)expr, context);
		} else if (expr instanceof ToTimeOperation) {
			return caseToTimeOperation((ToTimeOperation)expr, context);
		} else if (expr instanceof ToEnumOperation) {
			return caseToEnumOperation((ToEnumOperation)expr, context);
		} else if (expr instanceof ToDateOperation) {
			return caseToDateOperation((ToDateOperation)expr, context);
		} else if (expr instanceof ToDateTimeOperation) {
			return caseToDateTimeOperation((ToDateTimeOperation)expr, context);
		} else if (expr instanceof ToZonedDateTimeOperation) {
			return caseToZonedDateTimeOperation((ToZonedDateTimeOperation)expr, context);
		}  else if (expr instanceof SwitchOperation) {
 			return caseSwitchOperation((SwitchOperation)expr, context);
 		} else if (expr instanceof WithMetaOperation) {
 			return caseWithMetaOperation((WithMetaOperation)expr, context);
 		} else if (expr instanceof RosettaFunctionalOperation) {
			return doSwitch((RosettaFunctionalOperation)expr, context);
		}
		throw errorMissedCase(expr);
	}
	protected Return doSwitch(RosettaFunctionalOperation expr, Context context) {
		if (expr instanceof FilterOperation) {
			return caseFilterOperation((FilterOperation)expr, context);
		} else if (expr instanceof MapOperation) {
			return caseMapOperation((MapOperation)expr, context);
		} else if (expr instanceof MaxOperation) {
			return caseMaxOperation((MaxOperation)expr, context);
		} else if (expr instanceof MinOperation) {
			return caseMinOperation((MinOperation)expr, context);
		} else if (expr instanceof ReduceOperation) {
			return caseReduceOperation((ReduceOperation)expr, context);
		} else if (expr instanceof SortOperation) {
			return caseSortOperation((SortOperation)expr, context);
		} else if (expr instanceof ThenOperation) {
			return caseThenOperation((ThenOperation)expr, context);
		}
		throw errorMissedCase(expr);
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
