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

package com.regnosys.rosetta.generator.java;

import java.util.Objects;

import com.regnosys.rosetta.types.RFunction;

public class FunctionInstanceRepresentation {
	private final RFunction func;
	public FunctionInstanceRepresentation(RFunction func) {
		this.func = func;
	}
	
	@Override
	public String toString() {
		return "FunctionInstance[" + func.getId() + "]";
	}
	@Override
	public int hashCode() {
		return Objects.hash(this.getClass(), func);
	}
	@Override
	public boolean equals(Object object) {
		if (object == null) return false;
        if (this.getClass() != object.getClass()) return false;

        FunctionInstanceRepresentation other = (FunctionInstanceRepresentation) object;
        return Objects.equals(func, other.func);
	}
}
