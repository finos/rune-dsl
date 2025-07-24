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
