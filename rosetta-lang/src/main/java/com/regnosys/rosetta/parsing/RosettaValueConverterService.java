package com.regnosys.rosetta.parsing;

import javax.inject.Inject;

import org.eclipse.xtext.common.services.DefaultTerminalConverters;
import org.eclipse.xtext.conversion.IValueConverter;
import org.eclipse.xtext.conversion.ValueConverter;

public class RosettaValueConverterService extends DefaultTerminalConverters {
	@Inject BigDecimalConverter bigDecimalConverter;
	
	@ValueConverter(rule = "BigDecimal")
	public IValueConverter<String> getBigDecimalConverter() {
		return bigDecimalConverter;
	}
}
