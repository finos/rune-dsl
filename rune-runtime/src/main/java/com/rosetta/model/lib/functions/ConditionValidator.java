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

package com.rosetta.model.lib.functions;

import java.util.function.Supplier;

import com.google.inject.ImplementedBy;
import com.rosetta.model.lib.expression.ComparisonResult;

@ImplementedBy(DefaultConditionValidator.class)
public interface ConditionValidator {

	/**
	 * Evaluates conditions. Implementation may throw an exception if condition fails.
	 * 
	 * @param condition
	 * @param description
	 * @throws ConditionException if condition fails
	 */
    void validate(Supplier<ComparisonResult> condition, String description);


    class ConditionException extends RuntimeException {
		private static final long serialVersionUID = 1L;

        public ConditionException(String message) {
            super(message);
        }

        public ConditionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
