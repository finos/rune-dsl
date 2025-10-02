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

import java.util.Objects;

import org.eclipse.emf.ecore.EObject;

public class ImplicitVariableRepresentation {
	private final EObject definingContainer;
	
	public ImplicitVariableRepresentation(EObject definingContainer) {
		this.definingContainer = definingContainer;
	}
	
	public EObject getDefiningContainer() {
		return definingContainer;
	}

	@Override
	public String toString() {
		return "ImplicitVariable[" + definingContainer + "]";
	}
	@Override
	public int hashCode() {
		return Objects.hash(this.getClass(), definingContainer);
	}
	@Override
	public boolean equals(Object object) {
		if (object == null) return false;
        if (this.getClass() != object.getClass()) return false;

        ImplicitVariableRepresentation other = (ImplicitVariableRepresentation) object;
        return Objects.equals(definingContainer, other.definingContainer);
	}
}
