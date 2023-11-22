package com.regnosys.rosetta.parsing;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.eclipse.xtext.common.services.DefaultTerminalConverters;
import org.eclipse.xtext.conversion.IValueConverter;
import org.eclipse.xtext.conversion.ValueConverter;

public class RosettaValueConverterService extends DefaultTerminalConverters {
	@Inject private ValidIDConverter validIDValueConverter;
	@Inject private BigIntegerConverter bigIntegerConverter;
	@Inject private BigDecimalConverter bigDecimalConverter;
	@Inject private PATTERNValueConverter patternValueConverter;
	
	@ValueConverter(rule = "ValidID")
	public IValueConverter<String> getValidIDConverter() {
		return validIDValueConverter;
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
