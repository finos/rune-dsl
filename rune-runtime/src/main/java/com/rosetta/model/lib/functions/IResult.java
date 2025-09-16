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

import java.util.List;
import java.util.function.Function;

public interface IResult {
	
	/**
	 * @return key/value/type attribute for each result type.
	 */
	List<Attribute<?>> getAttributes();
	
	public static final class Attribute<T> {
		private String name;
		private Class<T> type;
		private Function<IResult, T> accesor;

		public Attribute(String name, Class<T> type, Function<IResult, T> accesor) {
			super();
			this.name = name;
			this.type = type;
			this.accesor = accesor;
		}

		public T get(IResult instance) {
			return accesor.apply(instance);
		}

		public String getName() {
			return name;
		}

		public Class<T> getType() {
			return type;
		}
	}
}