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

package com.rosetta.model.lib;

import com.rosetta.util.DottedPath;

public interface ModelSymbol {
	
	ModelSymbolId getSymbolId();
	
	default DottedPath getNamespace() {
		return getSymbolId().getNamespace();
	}
	default String getName() {
		return getSymbolId().getName();
	}
	default DottedPath getQualifiedName() {
		return getSymbolId().getQualifiedName();
	}
	
	public static abstract class AbstractModelSymbol extends ModelSymbolId implements ModelSymbol {
		public AbstractModelSymbol(DottedPath namespace, String name) {
			super(namespace, name);
		}

		@Override
		public ModelSymbolId getSymbolId() {
			return new ModelSymbolId(getNamespace(), getName());
		}
	}
}
