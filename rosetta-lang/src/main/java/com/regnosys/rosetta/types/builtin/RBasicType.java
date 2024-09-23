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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.regnosys.rosetta.interpreter.RosettaValue;
import com.regnosys.rosetta.scoping.RosettaScopeProvider;
import com.regnosys.rosetta.types.RMetaAttribute;
import com.regnosys.rosetta.types.RParametrizedType;
import com.rosetta.model.lib.ModelSymbolId;
import com.rosetta.util.DottedPath;

public class RBasicType extends RParametrizedType {
	private final ModelSymbolId symbolId;
	private final boolean hasNaturalOrder;
	
	public RBasicType(String name, Map<String, RosettaValue> parameters, boolean hasNaturalOrder, List<RMetaAttribute> metaAttributes) {
		super(parameters, metaAttributes);
		this.symbolId = new ModelSymbolId(DottedPath.splitOnDots(RosettaScopeProvider.LIB_NAMESPACE), name);
		this.hasNaturalOrder = hasNaturalOrder;
	}
	public RBasicType(String name, List<RMetaAttribute> metaAttributes, boolean hasNaturalOrder) {
		this(name, new LinkedHashMap<>(), hasNaturalOrder, metaAttributes);
	}

	@Override
	public ModelSymbolId getSymbolId() {
		return symbolId;
	}
	
	@Override
	public boolean hasNaturalOrder() {
		return hasNaturalOrder;
	}
}
