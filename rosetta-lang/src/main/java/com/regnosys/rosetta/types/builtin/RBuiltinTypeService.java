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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.emf.ecore.resource.ResourceSet;

import com.regnosys.rosetta.types.RAliasType;
import com.regnosys.rosetta.types.RType;
import com.regnosys.rosetta.types.RTypeFunction;
import com.rosetta.model.lib.RosettaNumber;
import com.rosetta.util.DottedPath;
import com.regnosys.rosetta.builtin.RosettaBuiltinsService;
import com.regnosys.rosetta.interpreter.RosettaNumberValue;
import com.regnosys.rosetta.interpreter.RosettaValue;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaType;
import com.regnosys.rosetta.scoping.RosettaScopeProvider;

@Singleton
public class RBuiltinTypeService {
	@Inject
	private RosettaBuiltinsService builtinsService;
	
	private Map<String, Function<Map<String, RosettaValue>, RType>> typeMap = new HashMap<>();

	public final String INT_NAME = "int";
	public final RTypeFunction INT_FUNCTION = new RTypeFunction(DottedPath.splitOnDots(RosettaScopeProvider.LIB_NAMESPACE), INT_NAME) {
		@Override
		public RNumberType evaluate(Map<String, RosettaValue> arguments) {
			Map<String, RosettaValue> numberArgs = new HashMap<>(arguments);
			numberArgs.put(RNumberType.FRACTIONAL_DIGITS_PARAM_NAME, RosettaNumberValue.of(RosettaNumber.ZERO));
			return RNumberType.from(numberArgs);
		}
		@Override
		public Optional<LinkedHashMap<String, RosettaValue>> reverse(RType type) {
			if (!(type instanceof RNumberType)) {
				return Optional.empty();
			}
			RNumberType nt = (RNumberType)type;
			if (!nt.isInteger() || nt.getScale().isPresent()) {
				return Optional.empty();
			}
			Map<String, RosettaValue> oldArgs = nt.getArguments();
			LinkedHashMap<String, RosettaValue> newArgs = new LinkedHashMap<>();
			newArgs.put(RNumberType.DIGITS_PARAM_NAME, oldArgs.get(RNumberType.DIGITS_PARAM_NAME));
			newArgs.put(RNumberType.MIN_PARAM_NAME, oldArgs.get(RNumberType.MIN_PARAM_NAME));
			newArgs.put(RNumberType.MAX_PARAM_NAME, oldArgs.get(RNumberType.MAX_PARAM_NAME));
			return Optional.of(newArgs);
		}
	};
	
	public final RBasicType BOOLEAN = registerConstantType(new RBasicType("boolean", true));
	public final RBasicType TIME = registerConstantType(new RBasicType("time", true));
	public final RBasicType PATTERN = registerConstantType(new RBasicType("pattern", false));
	// TODO: remove the MISSING type
	public final RBasicType MISSING = registerConstantType(new RBasicType("missing", true));
	public final RBasicType NOTHING = registerConstantType(new RBasicType("nothing", true));
	public final RBasicType ANY = registerConstantType(new RBasicType("any", false));
	public final RAliasType UNCONSTRAINED_INT = new RAliasType(INT_FUNCTION, new LinkedHashMap<>(Map.of(RNumberType.DIGITS_PARAM_NAME, RosettaValue.empty(), RNumberType.MIN_PARAM_NAME, RosettaValue.empty(), RNumberType.MAX_PARAM_NAME, RosettaValue.empty())), new RNumberType(Optional.empty(), Optional.of(0), Optional.empty(), Optional.empty(), Optional.empty()));
	public final RNumberType UNCONSTRAINED_NUMBER = new RNumberType(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
	public final RStringType UNCONSTRAINED_STRING = new RStringType(Optional.empty(), Optional.empty(), Optional.empty());
	
	public final RDateType DATE = registerConstantType(new RDateType());
	public final RDateTimeType DATE_TIME = registerConstantType(new RDateTimeType());
	public final RZonedDateTimeType ZONED_DATE_TIME = registerConstantType(new RZonedDateTimeType());
	
	public RBuiltinTypeService() {
		register("number", (m) -> RNumberType.from(m));
		register("string", (m) -> RStringType.from(m));
		
		// @Compat
		register("productType", (m) -> UNCONSTRAINED_STRING);
		register("eventType", (m) -> UNCONSTRAINED_STRING);
		register("calculation", (m) -> UNCONSTRAINED_STRING);
		register("int", (m) -> UNCONSTRAINED_INT);
	}
	
	public Optional<RType> getType(String name, Map<String, RosettaValue> params) {
		return Optional.ofNullable(typeMap.get(name))
				.map(constr -> constr.apply(params));
	}
	public Optional<RType> getType(RosettaType type, Map<String, RosettaValue> params) {
		return getType(type.getName(), params);
	}
	
	public <T extends RosettaType> T toRosettaType(RType builtinType, Class<T> resultType, ResourceSet resourceSet) {
		RosettaModel basicTypes = builtinsService.getBasicTypesModel(resourceSet);
		return basicTypes.getElements().stream()
				.filter(resultType::isInstance)
				.map(resultType::cast)
				.filter(t -> t.getName().equals(builtinType.getName()))
				.findAny()
				.orElseThrow();
	}

	private <T extends RType> T registerConstantType(T t) {
		register(t.getName(), (m) -> t);
		return t;
	}
	private void register(String name, Function<Map<String, RosettaValue>, RType> constructor) {
		if (typeMap.put(name, constructor) != null) {
			throw new IllegalStateException("There already exists a builtin type named `" + name + "`.");
		}
	}
}
