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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.regex.Pattern;

import jakarta.inject.Inject;

import com.regnosys.rosetta.interpreter.RosettaValue;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;
import com.regnosys.rosetta.types.builtin.RNumberType;
import com.regnosys.rosetta.types.builtin.RStringType;
import com.regnosys.rosetta.utils.BigDecimalInterval;
import com.regnosys.rosetta.utils.PositiveIntegerInterval;

public class TypeFactory {
	private final RBuiltinTypeService builtinTypes;
	
	@Inject
	public TypeFactory(RBuiltinTypeService builtinTypes) {
		this.builtinTypes = builtinTypes;
	}
	
	public RMetaAnnotatedType intWithNoMeta(Optional<Integer> digits, Optional<BigInteger> min, Optional<BigInteger> max) {
		return RMetaAnnotatedType.withNoMeta(constrainedInt(digits, min, max));
	}
	public RMetaAnnotatedType intWithNoMeta(int digits, BigInteger min, BigInteger max) {
		return RMetaAnnotatedType.withNoMeta(constrainedInt(digits, min, max));
	}
	public RMetaAnnotatedType intWithNoMeta(int digits, String min, String max) {
		return RMetaAnnotatedType.withNoMeta(constrainedInt(digits, min, max));
	}
	public RAliasType constrainedInt(Optional<Integer> digits, Optional<BigInteger> min, Optional<BigInteger> max) {
		RNumberType refersTo = constrainedNumber(digits, Optional.of(0), min.map(BigDecimal::new), max.map(BigDecimal::new), Optional.empty());
		LinkedHashMap<String, RosettaValue> args = new LinkedHashMap<>(refersTo.getArguments());
		args.remove(RNumberType.FRACTIONAL_DIGITS_PARAM_NAME);
		args.remove(RNumberType.SCALE_PARAM_NAME);
		return new RAliasType(builtinTypes.INT_FUNCTION, args, refersTo, new ArrayList<>());
	}
	public RAliasType constrainedInt(int digits, BigInteger min, BigInteger max) {
		return constrainedInt(Optional.of(digits), Optional.of(min), Optional.of(max));
	}
	public RAliasType constrainedInt(int digits, String min, String max) {
		return constrainedInt(Optional.of(digits), Optional.of(new BigInteger(min)), Optional.of(new BigInteger(max)));
	}
	
	public RMetaAnnotatedType numberWithNoMeta(Optional<Integer> digits, Optional<Integer> fractionalDigits, 
			Optional<BigDecimal> min, Optional<BigDecimal> max, Optional<BigDecimal> scale) {
		return RMetaAnnotatedType.withNoMeta(constrainedNumber(digits, fractionalDigits, min, max, scale));
	}
	public RMetaAnnotatedType numberWithNoMeta(Optional<Integer> digits, Optional<Integer> fractionalDigits, 
			BigDecimalInterval interval, Optional<BigDecimal> scale) {
		return RMetaAnnotatedType.withNoMeta(constrainedNumber(digits, fractionalDigits, interval, scale));
	}
	public RMetaAnnotatedType numberWithNoMeta(int digits, int fractionalDigits, BigDecimal min, BigDecimal max) {
		return RMetaAnnotatedType.withNoMeta(constrainedNumber(digits, fractionalDigits, min, max));
	}
	public RMetaAnnotatedType numberWithNoMeta(int digits, int fractionalDigits, String min, String max) {
		return RMetaAnnotatedType.withNoMeta(constrainedNumber(digits, fractionalDigits, min, max));
	}
	public RNumberType constrainedNumber(Optional<Integer> digits, Optional<Integer> fractionalDigits, 
			Optional<BigDecimal> min, Optional<BigDecimal> max, Optional<BigDecimal> scale) {
		return new RNumberType(digits, fractionalDigits, min, max, scale);
	}
	public RNumberType constrainedNumber(Optional<Integer> digits, Optional<Integer> fractionalDigits, 
			BigDecimalInterval interval, Optional<BigDecimal> scale) {
		return new RNumberType(digits, fractionalDigits, interval, scale);
	}
	public RNumberType constrainedNumber(int digits, int fractionalDigits, BigDecimal min, BigDecimal max) {
		return constrainedNumber(Optional.of(digits), Optional.of(fractionalDigits), Optional.of(min), Optional.of(max), Optional.empty());
	}
	public RNumberType constrainedNumber(int digits, int fractionalDigits, String min, String max) {
		return constrainedNumber(Optional.of(digits), Optional.of(fractionalDigits), Optional.of(new BigDecimal(min)), Optional.of(new BigDecimal(max)), Optional.empty());
	}
	
	public RMetaAnnotatedType stringWithNoMeta(Optional<Integer> minLength, Optional<Integer> maxLength, Optional<Pattern> pattern) {
		return RMetaAnnotatedType.withNoMeta(constrainedString(minLength, maxLength, pattern));
	}
	public RMetaAnnotatedType stringWithNoMeta(PositiveIntegerInterval interval, Optional<Pattern> pattern) {
		return RMetaAnnotatedType.withNoMeta(constrainedString(interval, pattern));
	}
	public RMetaAnnotatedType stringWithNoMeta(int minLength, int maxLength) {
		return RMetaAnnotatedType.withNoMeta(constrainedString(minLength, maxLength));
	}
	public RStringType constrainedString(Optional<Integer> minLength, Optional<Integer> maxLength, Optional<Pattern> pattern) {
		return new RStringType(minLength, maxLength, pattern);
	}
	public RStringType constrainedString(PositiveIntegerInterval interval, Optional<Pattern> pattern) {
		return new RStringType(interval, pattern);
	}
	public RStringType constrainedString(int minLength, int maxLength) {
		return new RStringType(Optional.of(minLength), Optional.of(maxLength), Optional.empty());
	}
}
