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

package com.rosetta.model.lib.qualify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.validation.ValidationResult;

/**
 * Contains results from applying expression and data rules to try to qualify a RosettaModelObject.
 * 
 * Includes name, expression / data rules applied, and whether there was a match.
 */
public class QualifyResult implements Comparable<QualifyResult> {
	
	private final String name;
	private final String definition;
	private final Collection<ExpressionDataRuleResult> expressionDataRuleResults;
	private final boolean success;
	
	public QualifyResult(QualifyResultBuilder builder) {
		this.name = builder.name;
		this.definition = builder.definition;
		
		boolean andDataRulesSuccess = builder.andDataRuleResults.stream().allMatch(r -> r.isSuccess());
		boolean orDataRulesSuccess = builder.orDataRuleResults.stream().anyMatch(r -> r.isSuccess());
		this.success = builder.expressionResult.isSuccess() && (andDataRulesSuccess || orDataRulesSuccess);
		
		this.expressionDataRuleResults = new ArrayList<>();
		this.expressionDataRuleResults.add(builder.expressionResult);
		this.expressionDataRuleResults.addAll(builder.andDataRuleResults);
		this.expressionDataRuleResults.addAll(builder.orDataRuleResults);
	}
	
	public String getName() {
		return name;
	}
	
	public String getDefinition() {
		return definition;
	}
	
	public Collection<ExpressionDataRuleResult> getExpressionDataRuleResults() {
		return expressionDataRuleResults;
	}
		
	public boolean isSuccess() {
		return success;
	}
	
	/**
	 * Sort results so successful results come first
	 */
	@Override
	public int compareTo(QualifyResult o) {
		return Boolean.compare(o.success, this.success);
	}
	
	@Override
	public String toString() {
		return String.format("QualifyResult.%s %s %s",  name, success ? "SUCCESS" : "FAILURE", expressionDataRuleResults);
	}

	public static QualifyResultBuilder builder() {
		return new QualifyResultBuilder();
	}
	
	public static class QualifyResultBuilder {
		
		private String name;
		private String definition;
		private ExpressionDataRuleResult expressionResult;
		private final Collection<ExpressionDataRuleResult> andDataRuleResults = new ArrayList<>();
		private final Collection<ExpressionDataRuleResult> orDataRuleResults = new ArrayList<>();
		
		public QualifyResultBuilder setName(String name) {
			this.name = name;
			return this;
		}
		
		public QualifyResultBuilder setDefinition(String definition) {
			this.definition = definition;
			return this;
		}
		
		public QualifyResultBuilder setExpressionResult(String definition, ComparisonResult result) {
			expressionResult = ExpressionDataRuleResult.fromExpression(definition, result);
			return this;
		}
				
		public QualifyResultBuilder addAndDataRuleResult(ValidationResult<?> result) {
			andDataRuleResults.add(ExpressionDataRuleResult.fromDataRule(result, "and"));
			return this;
		}
		
		public QualifyResultBuilder addOrDataRuleResult(ValidationResult<?> result) {
			orDataRuleResults.add(ExpressionDataRuleResult.fromDataRule(result, "or"));
			return this;
		}
		
		public QualifyResult build() {
			return new QualifyResult(this);
		}
	}
	
	/**
	 * Simple to class to hold expression and data rule results
	 */
	public static class ExpressionDataRuleResult {
		
		public enum Type { Expression, DataRule } 
		
		static ExpressionDataRuleResult fromExpression(String definition, ComparisonResult result) {
			return new ExpressionDataRuleResult("Expression", Type.Expression, definition, Optional.empty(), result.get(), result.getError());
		}
		
		static ExpressionDataRuleResult fromDataRule(ValidationResult<?> result, String operator) {
			return new ExpressionDataRuleResult(
					result.getName(),
					Type.DataRule,
					result.getDefinition(),
					Optional.ofNullable(operator), 
					result.isSuccess(), 
					result.getFailureReason().orElse(""));
		}
		
		private final String name;
		private final Type type;
		private final String definition;
		private final Optional<String> operator;
		private final boolean success;
		private final String error;
		
		private ExpressionDataRuleResult(String name, Type type, String definition, Optional<String> operator, boolean success, String error) {
			this.name = name;
			this.type = type;
			this.definition = definition;
			this.operator = operator;
			this.success = success;
			this.error = error;
		}
				
		public String getName() {
			return name;
		}
		
		public Type getType() {
			return type;
		}

		public String getDefinition() {
			return definition;
		}
		
		public String getOperator() {
			return operator.orElse("");
		}
		
		public boolean isSuccess() {
			return success;
		}
		
		public String getError() {
			return error;
		}
		
		@Override
		public String toString() {
			switch(type) {
			case Expression:
				return String.format("Result.Expression %s %s", success ? "SUCCESS" : "FAILURE", success ? "" : "Error: {" + error + "}").trim();
			case DataRule:
				return String.format("Result.DataRule.%s %s %s", name, success ? "SUCCESS" : "FAILURE", success ? "" : "Error: {" + error + "}").trim();
			default:
				return "ExpressionDataRuleResult [name=" + name + ", type=" + type + ", definition=" + definition
						+ ", operator=" + operator + ", success=" + success + ", error=" + error + "]";
			}
		}
	}
}
