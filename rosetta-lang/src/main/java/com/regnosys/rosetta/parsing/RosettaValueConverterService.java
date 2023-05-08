package com.regnosys.rosetta.parsing;

import java.math.BigDecimal;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.eclipse.xtext.common.services.DefaultTerminalConverters;
import org.eclipse.xtext.conversion.IValueConverter;
import org.eclipse.xtext.conversion.ValueConverter;

public class RosettaValueConverterService extends DefaultTerminalConverters {
	@Inject BigDecimalConverter bigDecimalConverter;
	@Inject PATTERNValueConverter patternValueConverter;
	
	@ValueConverter(rule = "BigDecimal")
	public IValueConverter<BigDecimal> getBigDecimalConverter() {
		return bigDecimalConverter;
	}
	
	@ValueConverter(rule = "PATTERN")
	public IValueConverter<Pattern> PATTERN() {
		return patternValueConverter;
	}
}
