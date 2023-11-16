package com.regnosys.rosetta.parsing;

import java.math.BigInteger;

import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.conversion.impl.AbstractValueConverter;
import org.eclipse.xtext.nodemodel.INode;

public class BigIntegerConverter extends AbstractValueConverter<BigInteger> {

	@Override
	public BigInteger toValue(String string, INode node) throws ValueConverterException {
		try {
			return new BigInteger(string);
		} catch (NumberFormatException e) {
			throw new ValueConverterException("Invalid integer format.", node, e);
		}
	}

	@Override
	public String toString(BigInteger value) throws ValueConverterException {
		return value.toString();
	}

}
