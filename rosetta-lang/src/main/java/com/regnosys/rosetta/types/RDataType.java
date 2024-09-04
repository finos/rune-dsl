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

import com.regnosys.rosetta.rosetta.simple.Data;
import com.rosetta.model.lib.ModelSymbolId;
import com.rosetta.util.DottedPath;

public class RDataType extends RAnnotateType {
	private final Data data;
	
	private RDataType superType = null;
	private ModelSymbolId symbolId = null;
	
	private final ModelIdProvider modelIdProvider;

	public RDataType(final Data data, final ModelIdProvider modelIdProvider) {
		super();
		this.data = data;
		
		this.modelIdProvider = modelIdProvider;
	}
	
	@Override
	public ModelSymbolId getSymbolId() {
		if (this.symbolId == null) {
			this.symbolId = modelIdProvider.getSymbolId(data);;
		}
		return this.symbolId;
	}

	public Data getData() {
		return this.data;
	}
	
	public RDataType getSuperType() {
		if (data.hasSuperType()) {
			if (this.superType == null) {
				this.superType = new RDataType(data.getSuperType(), modelIdProvider);
			}
			return this.superType;
		}
		return null;
	}

	@Override
	public int hashCode() {
		return 31 * 1 + ((this.data == null) ? 0 : this.data.hashCode());
	}

	@Override
	public boolean equals(final Object object) {
		if (object == null) return false;
        if (this.getClass() != object.getClass()) return false;
        
		RDataType other = (RDataType) object;
		return Objects.equals(this.data, other.data);
	}
}
