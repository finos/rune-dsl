package com.regnosys.rosetta.generator.java.expression;

import java.util.Arrays;
import java.util.MissingFormatArgumentException;
import java.util.function.Function;

import com.regnosys.rosetta.rosetta.RosettaExpression;

/* FIXME Unused - remove if not needed*/
public interface Expression {

	String javaCode();
	
	String description();
	
	ResultType resultType();

		
	enum ResultType {
		LIST, OBJECT, BOOLEAN
	}
	
	interface Handler {
		Expression handle(RosettaExpression expression, ExprFunc exprFunc);
	}

	interface ExprFunc extends Function<String, String> {
		
	}

	static ExprFunc NoFunc = x->x;
	
	class Decision implements Expression {
		private String format;
		private Expression[] expressions;
		public Decision(ExprFunc exprFunc, String format, Expression... expressions) {
			this.format = format;
			this.expressions = expressions;
		}
		
		@Override
		public String javaCode() {
			try {
				return (String.format(format, Arrays.stream(expressions).map(x -> x.javaCode()).toArray()));
			} catch (MissingFormatArgumentException e) {
				throw new MissingFormatArgumentException(e.getMessage() + " on " + format + " with "+expressions.length+" args " + Arrays.toString(expressions));
			}
		}

		@Override
		public String description() {
			return javaCode();
		}

		@Override
		public ResultType resultType() {
			return ResultType.BOOLEAN;
		}
	}

	class Decorator implements Expression {
		private String format;
		private Expression expression;
		private ResultType resultType;
		private ExprFunc exprFunc;
		
		public Decorator(ExprFunc exprFunc, String format, Expression expression) {
			this.exprFunc = exprFunc;
			this.format = format;
			this.expression = expression;
			this.resultType = expression.resultType();
		}
		
		public Decorator(ExprFunc exprFunc, String format, Expression expression, ResultType resultType) {
			this.exprFunc = exprFunc;
			this.format = format;
			this.expression = expression;
			this.resultType = resultType;
		}

		@Override
		public String javaCode() {
			try {
				return exprFunc.apply(String.format(format, expression.javaCode()));
			} catch (MissingFormatArgumentException e) {
				throw new MissingFormatArgumentException(e.getMessage() + " on " + format + " with 1 arg " + expression);
			}
		}

		@Override
		public String description() {
			return javaCode();
		}

		@Override
		public ResultType resultType() {
			return resultType;
		}
	}

	
	class Call implements Expression {
		private String result;
		private ResultType resultType;
		private ExprFunc exprFunc;

		public Call(ExprFunc exprFunc, String result, ResultType resultType) {
			this.exprFunc = exprFunc;
			this.result = result;
			this.resultType = resultType;
		}
		
		@Override
		public String javaCode() {
			return exprFunc.apply(result);
		}
		
		@Override
		public String description() {
			return result;
		}

		@Override
		public ResultType resultType() {
			return resultType;
		}
	}
	
}
