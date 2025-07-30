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

package com.regnosys.rosetta.tools.modelimport;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.regnosys.rosetta.rosetta.*;
import com.regnosys.rosetta.rosetta.simple.Choice;
import com.regnosys.rosetta.rosetta.simple.ChoiceOption;
import com.regnosys.rosetta.rosetta.simple.SimpleFactory;
import jakarta.inject.Inject;

import org.xmlet.xsdparser.xsdelements.XsdAbstractElement;
import org.xmlet.xsdparser.xsdelements.XsdRestriction;
import org.xmlet.xsdparser.xsdelements.XsdSimpleType;

import com.regnosys.rosetta.rosetta.expression.ExpressionFactory;
import com.regnosys.rosetta.rosetta.expression.RosettaIntLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaNumberLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaStringLiteral;
import com.regnosys.rosetta.types.builtin.RNumberType;
import com.regnosys.rosetta.types.builtin.RStringType;
import org.xmlet.xsdparser.xsdelements.xsdrestrictions.XsdStringRestrictions;

public class XsdTypeAliasImport extends AbstractXsdImport<XsdSimpleType, RosettaTypeAlias> {	
	
	private final XsdUtil util;

	@Inject
	public XsdTypeAliasImport(XsdUtil util) {
		super(XsdSimpleType.class);
		this.util = util;
	}

	@Override
	public List<XsdSimpleType> filterTypes(List<XsdAbstractElement> elements) {
		return super.filterTypes(elements).stream()
			.filter(x -> !util.isEnumType(x) && x.getRestriction() != null)
			.collect(Collectors.toList());
	}

	@Override
	public RosettaTypeAlias registerType(XsdSimpleType xsdType, RosettaXsdMapping typeMappings, ImportTargetConfig targetConfig) {
		RosettaTypeAlias typeAlias = RosettaFactory.eINSTANCE.createRosettaTypeAlias();
		typeAlias.setName(xsdType.getName());
		util.extractDocs(xsdType).ifPresent(typeAlias::setDefinition);
		typeMappings.registerSimpleType(xsdType, typeAlias);
		return typeAlias;
	}

	@Override
	public void completeType(XsdSimpleType xsdType, RosettaXsdMapping typeMappings) {
		RosettaTypeAlias typeAlias = typeMappings.getRosettaTypeFromSimple(xsdType);
		
		XsdRestriction restr = xsdType.getRestriction();
		TypeCall tc;
		if (restr.getBaseAsBuiltInDataType() != null) {
			if (restr.getFractionDigits() != null && restr.getFractionDigits().getValue() == 0) {
				tc = typeMappings.getRosettaTypeCallFromBuiltin("integer");
			} else {
				tc = typeMappings.getRosettaTypeCallFromBuiltin(restr.getBaseAsBuiltInDataType().getName());
			}
		} else if (restr.getBaseAsSimpleType() != null) {
			tc = typeMappings.getRosettaTypeCall(restr.getBaseAsSimpleType());
		} else if (restr.getBaseAsComplexType() != null) {
			tc = typeMappings.getRosettaTypeCall(restr.getBaseAsComplexType());
		} else if (restr.getBase().equals("base64Binary")) {
			// TODO: hack for FpML
			tc = typeMappings.getRosettaTypeCallFromBuiltin("base64Binary");
		} else {
			throw new RuntimeException("Unrecognized restriction: " + restr.getBase());
		}
		typeAlias.setTypeCall(tc);
		if (tc.getType() instanceof ParametrizedRosettaType) {
			util.addTypeArguments(tc, restr);
		}
	}
}
