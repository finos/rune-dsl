package com.regnosys.rosetta.parsing;

import java.math.BigDecimal;

import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.conversion.impl.AbstractValueConverter;
import org.eclipse.xtext.nodemodel.INode;

public class BigDecimalConverter extends AbstractValueConverter<String> {

	@Override
	public String toValue(String string, INode node) throws ValueConverterException {
		try {
			new BigDecimal(string);
		} catch (NumberFormatException e) {
			throw new ValueConverterException("Invalid number format.", node, e);
		}
		return string;
	}

	@Override
	public String toString(String value) throws ValueConverterException {
		return value;
	}

}
