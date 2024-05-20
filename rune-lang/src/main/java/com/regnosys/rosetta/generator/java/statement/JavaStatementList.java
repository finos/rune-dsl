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

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.xtend2.lib.StringConcatenationClient.TargetStringConcatenation;

import com.regnosys.rosetta.generator.TargetLanguageRepresentation;

public class JavaStatementList extends ArrayList<JavaStatement> implements TargetLanguageRepresentation {
	private static final long serialVersionUID = 1L;
	
	public JavaStatementList(JavaStatement... items) {
		super(Arrays.asList(items));
	}
	public JavaStatementList() {
		super();
	}
	
	public static JavaStatementList of(JavaStatement... items) {
		return new JavaStatementList(items);
	}

	@Override
	public void appendTo(TargetStringConcatenation target) {
		for (int i = 0; i < size(); i++) {
			target.append(get(i));
			if (i < size() - 1) {
				target.newLine();
			}
		}
	}
}
