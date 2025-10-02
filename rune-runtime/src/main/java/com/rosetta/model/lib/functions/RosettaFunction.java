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

package com.rosetta.model.lib.functions;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A marker interface to be implemented by all generated instances of RosettaFunction
 */
public interface RosettaFunction {
	default <R extends RosettaModelObjectBuilder> R toBuilder(RosettaModelObject object) {
		return toBuilder(object, () -> null);
	}
	
   @SuppressWarnings("unchecked")
    default <R extends RosettaModelObjectBuilder> R toBuilder(RosettaModelObject object, Supplier<R> emptySupplier) {
        if (object==null) return emptySupplier.get();
        return (R) object.build().toBuilder();
    }
	
	@SuppressWarnings("unchecked")
	default <R extends RosettaModelObjectBuilder> List<R> toBuilder(List<? extends RosettaModelObject> objects) {
		if (objects==null) return null;
		return  objects.stream().map(b->(R)b.build().toBuilder()).collect(Collectors.toList());
	}
}
