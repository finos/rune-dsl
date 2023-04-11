package com.regnosys.rosetta.parsing;

import java.math.BigDecimal;

import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.conversion.impl.AbstractValueConverter;
import org.eclipse.xtext.nodemodel.INode;

public class BigDecimalConverter extends AbstractValueConverter<BigDecimal> {

	@Override
	public BigDecimal toValue(String string, INode node) throws ValueConverterException {
		try {
			return new BigDecimal(string);
		} catch (NumberFormatException e) {
			throw new ValueConverterException("Invalid number format.", node, e);
		}
	}

	@Override
	public String toString(BigDecimal value) throws ValueConverterException {
		return value.toPlainString();
	}

}
