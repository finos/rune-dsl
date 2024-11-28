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

package com.regnosys.rosetta.utils;

import com.regnosys.rosetta.types.RAliasType;
import com.regnosys.rosetta.types.RChoiceType;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.REnumType;
import com.regnosys.rosetta.types.RParametrizedType;
import com.regnosys.rosetta.types.RType;
import com.regnosys.rosetta.types.builtin.RBasicType;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;
import com.regnosys.rosetta.types.builtin.RDateTimeType;
import com.regnosys.rosetta.types.builtin.RDateType;
import com.regnosys.rosetta.types.builtin.RNumberType;
import com.regnosys.rosetta.types.builtin.RRecordType;
import com.regnosys.rosetta.types.builtin.RStringType;
import com.regnosys.rosetta.types.builtin.RZonedDateTimeType;

public abstract class RosettaTypeSwitch<Return, Context> {
	private final RBuiltinTypeService builtins;
	
	public RosettaTypeSwitch(RBuiltinTypeService builtins) {
		this.builtins = builtins;
	}
	
	private UnsupportedOperationException errorMissedCase(RType type) {
		String className = type == null ? "null" : type.getClass().getCanonicalName();
		return new UnsupportedOperationException("Unexpected type " + className);
	}
	
	protected Return doSwitch(RType type, Context context) {
		if (type instanceof RDataType) {
			return caseDataType((RDataType)type, context);
		} else if (type instanceof RChoiceType) {
			return caseChoiceType((RChoiceType)type, context);
		} else if (type instanceof REnumType) {
			return caseEnumType((REnumType)type, context);
		} else if (type instanceof RParametrizedType) {
			return doSwitch((RParametrizedType)type, context);
		} else if (type instanceof RRecordType) {
			return doSwitch((RRecordType)type, context);
		}
		throw errorMissedCase(type);
	}

	protected Return doSwitch(RParametrizedType type, Context context) {
		if (type instanceof RAliasType) {
			return caseAliasType((RAliasType)type, context);
		} else if (type instanceof RBasicType) {
			return doSwitch((RBasicType)type, context);
		}
		throw errorMissedCase(type);
	}
	protected Return doSwitch(RBasicType type, Context context) {
		if (type instanceof RNumberType) {
			return caseNumberType((RNumberType)type, context);
		} else if (type instanceof RStringType) {
			return caseStringType((RStringType)type, context);
		} else if (type.equals(builtins.BOOLEAN)) {
			return caseBooleanType(type, context);
		} else if (type.equals(builtins.TIME)) {
			return caseTimeType(type, context);
		} else if (type.equals(builtins.NOTHING)) {
			return caseNothingType(type, context);
		} else if (type.equals(builtins.ANY)) {
			return caseAnyType(type, context);
		}
		throw errorMissedCase(type);
	}
	protected Return doSwitch(RRecordType type, Context context) {
		if (type instanceof RDateType) {
			return caseDateType((RDateType)type, context);
		} else if (type instanceof RDateTimeType) {
			return caseDateTimeType((RDateTimeType)type, context);
		} else if (type instanceof RZonedDateTimeType) {
			return caseZonedDateTimeType((RZonedDateTimeType)type, context);
		}
		throw errorMissedCase(type);
	}
		
	protected abstract Return caseDataType(RDataType type, Context context);
	protected abstract Return caseChoiceType(RChoiceType type, Context context);
	protected abstract Return caseEnumType(REnumType type, Context context);
	
	protected abstract Return caseAliasType(RAliasType type, Context context);
	
	protected abstract Return caseNumberType(RNumberType type, Context context);
	protected abstract Return caseStringType(RStringType type, Context context);
	protected abstract Return caseBooleanType(RBasicType type, Context context);
	protected abstract Return caseTimeType(RBasicType type, Context context);
	protected abstract Return caseNothingType(RBasicType type, Context context);
	protected abstract Return caseAnyType(RBasicType type, Context context);
	
	protected abstract Return caseDateType(RDateType type, Context context);
	protected abstract Return caseDateTimeType(RDateTimeType type, Context context);
	protected abstract Return caseZonedDateTimeType(RZonedDateTimeType type, Context context);
}
