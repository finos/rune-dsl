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
