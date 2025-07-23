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

package com.regnosys.rosetta.types.builtin;

import java.util.Collection;
import java.util.Objects;

import com.regnosys.rosetta.scoping.RosettaScopeProvider;
import com.regnosys.rosetta.types.RType;
import com.rosetta.model.lib.ModelSymbolId;
import com.rosetta.util.DottedPath;

public abstract class RRecordType extends RType {
	private final ModelSymbolId symbolId;
	
	public RRecordType(String name) {
		super();
		this.symbolId = new ModelSymbolId(DottedPath.splitOnDots(RosettaScopeProvider.LIB_NAMESPACE), name);
	}
	
	@Override
	public ModelSymbolId getSymbolId() {
		return this.symbolId;
	}
	@Override
	public boolean hasNaturalOrder() {
		return true;
	}
	
	// TODO: is this necessary?
	public abstract Collection<RRecordFeature> getFeatures();
	
	@Override
	public int hashCode() {
		return Objects.hash(getSymbolId(), getFeatures());
	}

	@Override
	public boolean equals(final Object object) {
		if (object == null) return false;
        if (this.getClass() != object.getClass()) return false;
        
		RRecordType other = (RRecordType) object;
		return Objects.equals(getSymbolId(), other.getSymbolId())
				&& Objects.equals(getFeatures(), other.getFeatures());
	}
}
