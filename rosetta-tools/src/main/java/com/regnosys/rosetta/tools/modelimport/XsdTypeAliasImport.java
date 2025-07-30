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
			.filter(x -> !util.isEnumType(x))
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

	private Optional<TypeParameter> findParameter(String name, ParametrizedRosettaType type) {
		return type.getParameters().stream()
				.filter(p -> p.getName().equals(name))
				.findFirst();
	}
	private RosettaIntLiteral createIntLiteral(BigInteger value) {
		RosettaIntLiteral lit = ExpressionFactory.eINSTANCE.createRosettaIntLiteral();
		lit.setValue(value);
		return lit;
	}
	private RosettaNumberLiteral createNumberLiteral(BigDecimal value) {
		RosettaNumberLiteral lit = ExpressionFactory.eINSTANCE.createRosettaNumberLiteral();
		lit.setValue(value);
		return lit;
	}
	private RosettaStringLiteral createStringLiteral(String value) {
		RosettaStringLiteral lit = ExpressionFactory.eINSTANCE.createRosettaStringLiteral();
		lit.setValue(value);
		return lit;
	}
	private Optional<TypeCallArgument> createTypeArgument(ParametrizedRosettaType baseType, String parameterName, BigInteger value) {
		return createTypeArgumentWithoutValue(baseType, parameterName).map(arg -> {
			arg.setValue(createIntLiteral(value));
			return arg;
		});
	}
	private Optional<TypeCallArgument> createTypeArgument(ParametrizedRosettaType baseType, String parameterName, BigDecimal value) {
		return createTypeArgumentWithoutValue(baseType, parameterName).map(arg -> {
			arg.setValue(createNumberLiteral(value));
			return arg;
		});
	}
	private Optional<TypeCallArgument> createTypeArgument(ParametrizedRosettaType baseType, String parameterName, String value) {
		return createTypeArgumentWithoutValue(baseType, parameterName).map(arg -> {
			arg.setValue(createStringLiteral(value));
			return arg;
		});
	}
	private Optional<TypeCallArgument> createTypeArgumentWithoutValue(ParametrizedRosettaType baseType, String parameterName) {
		return findParameter(parameterName, baseType).map(param -> {
			TypeCallArgument arg = RosettaFactory.eINSTANCE.createTypeCallArgument();
			arg.setParameter(param);
			return arg;
		});
	}
	@Override
	public void completeType(XsdSimpleType xsdType, RosettaXsdMapping typeMappings) {
		RosettaTypeAlias typeAlias = typeMappings.getRosettaTypeFromSimple(xsdType);
		
		XsdRestriction restr = xsdType.getRestriction();
		TypeCall tc;
		if (xsdType.getUnion() != null) {
			completeChoiceType(xsdType, typeMappings, typeAlias);
			return;
		}
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
			addTypeArguments(tc, restr);
		}
	}

	private void completeChoiceType(XsdSimpleType xsdType, RosettaXsdMapping typeMappings, RosettaTypeAlias typeAlias) {
		Choice choiceType = SimpleFactory.eINSTANCE.createChoice();

		// replace typeAlias with choice object
		typeAlias.getModel().getElements().add(choiceType);
		typeAlias.getModel().getElements().remove(typeAlias);

		TypeCall tc = RosettaFactory.eINSTANCE.createTypeCall();
		tc.setType(choiceType);

		choiceType.setName(xsdType.getName());

		for (XsdSimpleType subType : xsdType.getUnion().getUnionElements()) {
			TypeCall tc1;
			XsdRestriction restriction = subType.getRestriction();
			if (subType.getRawName() == null) {
				String restrBase;
				if (subType.getCloneOf() == null) {
					restrBase = restriction.getBaseAsBuiltInDataType().getName();
				} else {
					restrBase = ((XsdSimpleType) subType.getCloneOf()).getRestriction().getBaseAsBuiltInDataType().getName();
				}

				tc1 = typeMappings.getRosettaTypeCallFromBuiltin(restrBase);

				ChoiceOption choiceOption = SimpleFactory.eINSTANCE.createChoiceOption();
				choiceOption.setTypeCall(tc1);
				choiceOption.set_hardcodedName("ChoiceOption" + xsdType.getUnion().getUnionElements().indexOf(subType));
				choiceType.getAttributes().add(choiceOption);

			} else {
				RosettaTypeAlias t;
				if (subType.getCloneOf() == null) {
					t = typeMappings.getRosettaTypeFromSimple(subType);
				} else {
					t = typeMappings.getRosettaTypeFromSimple((XsdSimpleType) subType.getCloneOf());
				}

				tc1 = RosettaFactory.eINSTANCE.createTypeCall();
				tc1.setType(t);

				ChoiceOption choiceOption = SimpleFactory.eINSTANCE.createChoiceOption();
				choiceOption.setTypeCall(tc1);
				choiceOption.set_hardcodedName(t.getName());
				choiceType.getAttributes().add(choiceOption);
			}

			if (tc1.getType() instanceof ParametrizedRosettaType) {
				addTypeArguments(tc1, restriction);
			}
		}
	}

	private void addTypeArguments(TypeCall tc, XsdRestriction restr) {
		ParametrizedRosettaType paramBaseType = (ParametrizedRosettaType) tc.getType();
		// add type arguments
		if (restr.getTotalDigits() != null) {
			BigInteger digits = BigInteger.valueOf(restr.getTotalDigits().getValue());
			createTypeArgument(paramBaseType, RNumberType.DIGITS_PARAM_NAME, digits).ifPresent(arg -> tc.getArguments().add(arg));
		}
		if (restr.getFractionDigits() != null) {
			BigInteger fractionalDigits = BigInteger.valueOf(restr.getFractionDigits().getValue());
			createTypeArgument(paramBaseType, RNumberType.FRACTIONAL_DIGITS_PARAM_NAME, fractionalDigits).ifPresent(arg -> tc.getArguments().add(arg));
		}
		if (restr.getMinInclusive() != null) {
			BigDecimal min = new BigDecimal(restr.getMinInclusive().getValue());
			createTypeArgument(paramBaseType, RNumberType.MIN_PARAM_NAME, min).ifPresent(arg -> tc.getArguments().add(arg));
		}
		if (restr.getMaxInclusive() != null) {
			BigDecimal max = new BigDecimal(restr.getMaxInclusive().getValue());
			createTypeArgument(paramBaseType, RNumberType.MAX_PARAM_NAME, max).ifPresent(arg -> tc.getArguments().add(arg));
		}
		if (restr.getMinExclusive() != null) {
			BigDecimal min = new BigDecimal(restr.getMinExclusive().getValue());
			createTypeArgument(paramBaseType, RNumberType.MIN_PARAM_NAME, min).ifPresent(arg -> tc.getArguments().add(arg));
		}
		if (restr.getMaxExclusive() != null) {
			BigDecimal max = new BigDecimal(restr.getMaxExclusive().getValue());
			createTypeArgument(paramBaseType, RNumberType.MAX_PARAM_NAME, max).ifPresent(arg -> tc.getArguments().add(arg));
		}

		if (restr.getLength() != null) {
			BigInteger length = BigInteger.valueOf(restr.getLength().getValue());
			createTypeArgument(paramBaseType, RStringType.MIN_LENGTH_PARAM_NAME, length).ifPresent(arg -> tc.getArguments().add(arg));
			createTypeArgument(paramBaseType, RStringType.MAX_LENGTH_PARAM_NAME, length).ifPresent(arg -> tc.getArguments().add(arg));
		}
		if (restr.getMinLength() != null && restr.getMinLength().getValue() != 0) {
			BigInteger minLength = BigInteger.valueOf(restr.getMinLength().getValue());
			createTypeArgument(paramBaseType, RStringType.MIN_LENGTH_PARAM_NAME, minLength).ifPresent(arg -> tc.getArguments().add(arg));
		}
		if (restr.getMaxLength() != null) {
			BigInteger maxLength = BigInteger.valueOf(restr.getMaxLength().getValue());
			createTypeArgument(paramBaseType, RStringType.MAX_LENGTH_PARAM_NAME, maxLength).ifPresent(arg -> tc.getArguments().add(arg));
		}
		if (restr.getPattern() != null) {
			String pattern = restr.getPatterns().stream().map(XsdStringRestrictions::getValue).collect(Collectors.joining("|"));
			createTypeArgument(paramBaseType, RStringType.PATTERN_PARAM_NAME, pattern).ifPresent(arg -> tc.getArguments().add(arg));
		}
	}
}
