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

import com.rosetta.model.lib.ModelSymbolId;
import com.rosetta.util.DottedPath;

// TODO: remove this type
public class RErrorType extends RType {
	private final String message;

	public RErrorType(final String message) {
		super();
		this.message = message;
	}
	
	@Override
	public ModelSymbolId getSymbolId() {
		return null;
	}

	@Override
	public String getName() {
		return this.message;
	}
	
	@Override
	public DottedPath getNamespace() {
		return null;
	}
	
	@Override
	public DottedPath getQualifiedName() {
		return DottedPath.of(message);
	}


	public String getMessage() {
		return this.message;
	}

	@Override
	public int hashCode() {
		return 31 * 1 + ((this.message == null) ? 0 : this.message.hashCode());
	}

	@Override
	public boolean equals(final Object object) {
		if (object == null) return false;
        if (this.getClass() != object.getClass()) return false;
        
		RErrorType other = (RErrorType) object;
		return Objects.equals(message, other.message);
	}
}
