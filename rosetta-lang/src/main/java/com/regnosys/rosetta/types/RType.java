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

import com.rosetta.model.lib.ModelSymbol;

public abstract class RType implements ModelSymbol {
	private List<RMetaAttribute> metaAttributes = List.of();
	
	public boolean hasMeta() {
		return !metaAttributes.isEmpty();
	}
	
	public boolean hasNaturalOrder() {
		return false;
	}
	
	public boolean isBuiltin() {
		return false;
	}
	
	public List<RMetaAttribute> getMetaAttributes() {
		return metaAttributes;
	}
	
	public void setMetaAttributes(List<RMetaAttribute> metaAttributes) {
		this.metaAttributes = metaAttributes == null ? List.of() : metaAttributes;
	}

	@Deprecated
	public boolean hasReferenceOrAddressMetadata() {
		return metaAttributes.stream().anyMatch(a -> a.getName().equals("reference") || a.getName().equals("address"));
	}
	
	@Override
	public String toString() {
		return this.getName();
	}
	

	
}
