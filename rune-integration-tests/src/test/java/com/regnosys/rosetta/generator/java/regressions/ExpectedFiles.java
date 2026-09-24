/*
 * Copyright 2026 REGnosys
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

package com.regnosys.rosetta.generator.java.regressions;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Selects which generated files of an {@link AbstractJavaGeneratorRegressionTest} fixture are compared
 * against a file under {@code expected/}. Use it when a fixture needs supporting types whose output
 * another fixture already pins. Every generated file is still compiled.
 *
 * <p>Patterns are globs over the path relative to {@code expected/}, e.g.
 * {@code test/expressions/functions/*.java}: {@code *} matches within a folder and {@code **}
 * across folders. A file is compared when it matches an {@link #include()} pattern and no
 * {@link #exclude()} pattern. Without this annotation every generated file is compared.
 *
 * <p>The test fails when a pattern matches no generated file, and when {@code expected/} holds a
 * file that is not selected; running with {@code -Drune.updateExpectations} deletes such a file.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExpectedFiles {
	/** The generated files to compare. */
	String[] include() default "**";

	/** Generated files to leave out, even though they match {@link #include()}. */
	String[] exclude() default {};
}
