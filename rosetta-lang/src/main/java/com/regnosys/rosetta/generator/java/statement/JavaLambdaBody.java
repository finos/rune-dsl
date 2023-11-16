package com.regnosys.rosetta.generator.java.statement;

import com.regnosys.rosetta.generator.TargetLanguageRepresentation;

/**
 * Based on the Java specification: https://docs.oracle.com/javase/specs/jls/se11/html/jls-15.html#jls-LambdaBody
 * 
 * The body of a lambda expression can be either
 * - an expression, as in `x -> 42` - see `JavaExpression`, or
 * - a block statement, as in `x -> { return 42; }` - see `JavaBlock`.
 */
public interface JavaLambdaBody extends TargetLanguageRepresentation {
}
