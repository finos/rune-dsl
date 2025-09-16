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

package com.regnosys.rosetta.generator.java.statement.builder;

import org.eclipse.xtend2.lib.StringConcatenationClient.TargetStringConcatenation;

import com.regnosys.rosetta.generator.GeneratedIdentifier;
import com.regnosys.rosetta.generator.java.scoping.JavaStatementScope;
import com.rosetta.util.types.JavaType;

/**
 * A Java variable.
 * 
 * See `JavaStatementBuilder` for more documentation.
 */
public class JavaVariable extends JavaExpression {
	private final GeneratedIdentifier id;

	public JavaVariable(GeneratedIdentifier id, JavaType type) {
		super(type);
		this.id = id;
	}
	
	@Override
	public JavaStatementBuilder declareAsVariable(boolean isFinal, String variableId, JavaStatementScope scope) {
		if (scope.getIdentifier(this).map(scopeId -> !scopeId.equals(id)).orElse(true)) {
			scope.createSynonym(this, id);
		}
		return this;
	}

	@Override
	public void appendTo(TargetStringConcatenation target) {
		target.append(id);
	}
}
