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

package com.regnosys.rosetta.parsing;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.regex.Pattern;

import jakarta.inject.Inject;

import org.eclipse.xtext.common.services.DefaultTerminalConverters;
import org.eclipse.xtext.conversion.IValueConverter;
import org.eclipse.xtext.conversion.ValueConverter;

public class RosettaValueConverterService extends DefaultTerminalConverters {
	@Inject private RosettaNameEscaper nameEscaper;
	@Inject private BigIntegerConverter bigIntegerConverter;
	@Inject private BigDecimalConverter bigDecimalConverter;
	@Inject private PATTERNValueConverter patternValueConverter;
	
	@ValueConverter(rule = "ValidID")
	public IValueConverter<String> getValidIDConverter() {
		return nameEscaper.getValidIDConverter();
	}
	
	@ValueConverter(rule = "TypeParameterValidID")
	public IValueConverter<String> getTypeParameterValidIDConverter() {
		return nameEscaper.getTypeParameterValidIDConverter();
	}
	
	@ValueConverter(rule = "QualifiedName")
	public IValueConverter<String> getQualifiedNameConverter() {
		return nameEscaper.getQualifiedNameConverter();
	}
	
	@ValueConverter(rule = "QualifiedNameWithWildcard")
	public IValueConverter<String> getQualifiedNameWithWildcardConverter() {
		return nameEscaper.getImportedNamespaceConverter();
	}
	
	@ValueConverter(rule = "Integer")
	public IValueConverter<BigInteger> getBigIntegerConverter() {
		return bigIntegerConverter;
	}
	
	@ValueConverter(rule = "BigDecimal")
	public IValueConverter<BigDecimal> getBigDecimalConverter() {
		return bigDecimalConverter;
	}
	
	@ValueConverter(rule = "PATTERN")
	public IValueConverter<Pattern> PATTERN() {
		return patternValueConverter;
	}
}
