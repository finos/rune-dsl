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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import com.regnosys.rosetta.interpreter.RosettaValue;
import com.regnosys.rosetta.rosetta.simple.Condition;
import com.rosetta.model.lib.ModelSymbolId;

public class RAliasType extends RParametrizedType {
	private final RTypeFunction typeFunction;
	private final RType refersTo;
	private final List<Condition> conditions;

	public RAliasType(RTypeFunction typeFunction, LinkedHashMap<String, RosettaValue> params, RType refersTo, List<Condition> conditions) {
		super(params);
		this.typeFunction = typeFunction;
		this.refersTo = refersTo;
		this.conditions = conditions;
	}

	@Override
	public ModelSymbolId getSymbolId() {
		return typeFunction.getSymbolId();
	}
	
	public RTypeFunction getTypeFunction() {
		return typeFunction;
	}

	public RType getRefersTo() {
		return refersTo;
	}
	
	public List<Condition> getConditions() {
		return conditions;
	}
	
	@Override
	public boolean hasNaturalOrder() {
	    return refersTo.hasNaturalOrder();
	}

	@Override
	public int hashCode() {
		return Objects.hash(typeFunction, getArguments(), refersTo);
	}

	@Override
	public boolean equals(final Object object) {
		if (object == null) return false;
        if (this.getClass() != object.getClass()) return false;
        
		RAliasType other = (RAliasType) object;
		return Objects.equals(typeFunction, other.typeFunction)
				&& Objects.equals(getArguments(), other.getArguments())
				&& Objects.equals(refersTo, other.refersTo)
				&& Objects.equals(conditions, other.conditions);
	}
}
