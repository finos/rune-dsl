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

package com.regnosys.rosetta.types;

import java.util.Objects;

import org.eclipse.emf.ecore.EObject;

import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.simple.Function;

public class RShortcut implements RAssignedRoot {
	private final String name;
	private final boolean isMulti;
	private final String definition;
	private final RosettaExpression expression;
	
	private final Function function;
	private final RObjectFactory objectFactory;
	
	public RShortcut(String name, boolean isMulti, String definition, RosettaExpression expression, Function function, RObjectFactory objectFactory) {
		this.name = name;
		this.isMulti = isMulti;
		this.definition = definition;
		this.expression = expression;
		
		this.function = function;
		this.objectFactory = objectFactory;
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public boolean isMulti() {
		return isMulti;
	}

	public String getDefinition() {
		return definition;
	}

	public RosettaExpression getExpression() {
		return expression;
	}
	
	public RFunction getFunction() {
		return objectFactory.buildRFunction(function);
	}

	@Override
	public int hashCode() {
		return Objects.hash(definition, expression, name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RShortcut other = (RShortcut) obj;
		return Objects.equals(definition, other.definition) && Objects.equals(expression, other.expression)
				&& Objects.equals(name, other.name);
	}

	@Override
	public EObject getEObject() {
		return expression.eContainer();
	}
}
