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

import java.util.NoSuchElementException;

import org.eclipse.xtend2.lib.StringConcatenationClient.TargetStringConcatenation;

public class GeneratedIdentifier implements TargetLanguageRepresentation {
	protected final GeneratorScope<?> scope;
	private final String desiredName;
	
	public GeneratedIdentifier(GeneratorScope<?> scope, String desiredName) {
		this.scope = scope;
		this.desiredName = desiredName;
	}
	
	public String getDesiredName() {
		return this.desiredName;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " (desired name=\"" + desiredName + "\")";
	}

	@Override
	public void appendTo(TargetStringConcatenation target) {
		String actualName = getActualName();
		target.append(actualName);
	}
	
	protected String getActualName() {
		return this.scope.getActualName(this)
				.orElseThrow(() -> new NoSuchElementException("No actual name for " + this.toString() + " in scope.\n" + scope));
	}
}
