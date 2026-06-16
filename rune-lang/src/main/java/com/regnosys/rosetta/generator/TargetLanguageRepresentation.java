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

package com.regnosys.rosetta.generator;

import org.eclipse.xtend2.lib.StringConcatenationClient.TargetStringConcatenation;

/**
 * An interface to indicate that a class represents a concept in a target language
 * of a generator.
 *
 * When appended to a {com.regnosys.rosetta.generator.TargetLanguageStringConcatenation},
 * `appendTo` will be called instead of `toString`.
 *
 * <p>Migration note: this interface ties representations to the legacy Xtend
 * template machinery. It extends the target-language-agnostic
 * {@link com.regnosys.rosetta.codegen.api.TargetLanguageRepresentation} as a bridge;
 * once all generators use the fluent API, implementations should implement that
 * interface directly and this one (together with {@code appendTo}) will be deleted.
 */
public interface TargetLanguageRepresentation extends com.regnosys.rosetta.codegen.api.TargetLanguageRepresentation {
	void appendTo(TargetStringConcatenation target);
}
