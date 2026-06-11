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

import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.mapper.Mapper;

public class CalculationFunction<T extends RosettaModelObject, R> implements Function<T, Mapper<? extends R>>{
	
	private final Function<CalculationArgs, Mapper<? extends R>> function;
	private final CalculationArgFunctions<T> argFunctions;
	
	public CalculationFunction(Function<CalculationArgs, Mapper<? extends R>> function, CalculationArgFunctions<T> argFunctions) {
		super();
		this.function = function;
		this.argFunctions = argFunctions;
	}
	
	public Mapper<? extends R> apply(CalculationArgs args) {
		return function.apply(args);
	}
	
	@Override
	public Mapper<? extends R> apply(T input) {
		CalculationArgs args = argFunctions.toArgs(input);
		return apply(args);
	}

	public static class CalculationArgs extends HashMap<String, Mapper<?>> {
		private static final long serialVersionUID = 1L;
	}
	
	public static class CalculationArgFunctions<T> extends HashMap<String, Function<T, Mapper<?>>> {
		
		private static final long serialVersionUID = 1L;
		
		public CalculationArgs toArgs(T input) {
			BiConsumer<CalculationArgs, Entry<String, Function<T, Mapper<?>>>> accumulator =
					(args, entry)->args.put(entry.getKey(), entry.getValue().apply(input));
			BinaryOperator<CalculationArgs> combiner = (args1, args2)->{args1.putAll(args2);return args1;};
			Collector<Entry<String, Function<T, Mapper<?>>>, CalculationArgs, CalculationArgs> col =
					Collector.of(CalculationArgs::new, accumulator , combiner );
			return this.entrySet().stream().collect(col);
		}
		
	}

}
