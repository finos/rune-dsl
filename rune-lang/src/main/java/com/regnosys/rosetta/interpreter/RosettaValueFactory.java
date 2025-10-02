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

package com.regnosys.rosetta.interpreter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import com.regnosys.rosetta.types.RAliasType;
import com.regnosys.rosetta.types.RChoiceType;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.REnumType;
import com.regnosys.rosetta.types.RType;
import com.regnosys.rosetta.types.builtin.RBasicType;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;
import com.regnosys.rosetta.types.builtin.RDateTimeType;
import com.regnosys.rosetta.types.builtin.RDateType;
import com.regnosys.rosetta.types.builtin.RNumberType;
import com.regnosys.rosetta.types.builtin.RStringType;
import com.regnosys.rosetta.types.builtin.RZonedDateTimeType;
import com.regnosys.rosetta.utils.RosettaTypeSwitch;
import com.rosetta.model.lib.RosettaNumber;

public class RosettaValueFactory extends RosettaTypeSwitch<RosettaValue, List<?>> {
	@Inject
	public RosettaValueFactory(RBuiltinTypeService builtins) {
		super(builtins);
	}
	
	private <T> List<T> castList(List<?> list, Class<T> clazz) {
		if (list.stream().anyMatch(i -> !clazz.isInstance(i))) {
			throw new RosettaInterpreterTypeException(list + " is not of type " + clazz);
		}
		return list.stream().map(i -> clazz.cast(i)).collect(Collectors.toList());
	}
	
	public RosettaValue createOfType(RType type, List<?> items) {
		return doSwitch(type, items);
	}
	public RosettaValue createOfType(RType type, Object item) {
		return createOfType(type, List.of(item));
	}

	@Override
	protected RosettaValue caseDataType(RDataType type, List<?> context) {
		throw new UnsupportedOperationException("Data type is unsupported");
	}
	
	@Override
	protected RosettaValue caseChoiceType(RChoiceType type, List<?> context) {
		throw new UnsupportedOperationException("Choice type is unsupported");
	}

	@Override
	protected RosettaValue caseEnumType(REnumType type, List<?> context) {
		throw new UnsupportedOperationException("Enum type is unsupported");
	}

	@Override
	protected RosettaValue caseAliasType(RAliasType type, List<?> context) {
		return createOfType(type.getRefersTo(), context);
	}

	@Override
	protected RosettaValue caseNumberType(RNumberType type, List<?> context) {
		return new RosettaNumberValue(castList(context, RosettaNumber.class), type.getScale().map(s -> RosettaNumber.valueOf(s)).orElse(RosettaNumber.ONE));
	}

	@Override
	protected RosettaValue caseStringType(RStringType type, List<?> context) {
		return new RosettaStringValue(castList(context, String.class));
	}

	@Override
	protected RosettaValue caseBooleanType(RBasicType type, List<?> context) {
		return new RosettaBooleanValue(castList(context, Boolean.class));
	}

	@Override
	protected RosettaValue caseTimeType(RBasicType type, List<?> context) {
		return new RosettaTimeValue(castList(context, LocalTime.class));
	}

//	@Override
//	protected RosettaValue casePatternType(RBasicType type, List<?> context) {
//		return new RosettaPatternValue(castList(context, Pattern.class));
//	}

	@Override
	protected RosettaValue caseNothingType(RBasicType type, List<?> context) {
		if (!context.isEmpty()) {
			throw new RosettaInterpreterTypeException("Cannot create a non-empty Rosetta value of type " + type);
		}
		return RosettaValue.empty();
	}

	@Override
	protected RosettaValue caseAnyType(RBasicType type, List<?> context) {
		throw new UnsupportedOperationException("Cannot create a value of type " + type);
	}

	@Override
	protected RosettaValue caseDateType(RDateType type, List<?> context) {
		return new RosettaDateValue(castList(context, LocalDate.class));
	}

	@Override
	protected RosettaValue caseDateTimeType(RDateTimeType type, List<?> context) {
		return new RosettaDateTimeValue(castList(context, LocalDateTime.class));
	}

	@Override
	protected RosettaValue caseZonedDateTimeType(RZonedDateTimeType type, List<?> context) {
		return new RosettaZonedDateTimeValue(castList(context, ZonedDateTime.class));
	}
}
