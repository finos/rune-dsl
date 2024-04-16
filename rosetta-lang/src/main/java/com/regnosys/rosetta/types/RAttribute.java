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

import java.util.List;
import java.util.Objects;

public class RAttribute implements RAssignedRoot {
	private String name;
	private String definition;
	private RType rType;
	private List<RAttribute> metaAnnotations;
	private boolean isMulti;

	public RAttribute(String name, String definition, RType rType, List<RAttribute> metaAnnotations, boolean isMulti) {
		this.name = name;
		this.definition = definition;
		this.rType = rType;
		this.metaAnnotations = metaAnnotations;
		this.isMulti = isMulti;
	}
	
	@Override
	public String getName() {		
		return name;
	}

	public RType getRType() {
		return rType;
	}

	public boolean isMulti() {
		return isMulti;
	}
	
	public String getDefinition() {
		return definition;
	}
	
	
	public List<RAttribute> getMetaAnnotations() {
		return metaAnnotations;
	}

	@Override
	public int hashCode() {
		return Objects.hash(definition, isMulti, metaAnnotations, name, rType);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RAttribute other = (RAttribute) obj;
		return Objects.equals(definition, other.definition) && isMulti == other.isMulti
				&& Objects.equals(metaAnnotations, other.metaAnnotations) && Objects.equals(name, other.name)
				&& Objects.equals(rType, other.rType);
	}



}
