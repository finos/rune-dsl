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
import java.security.InvalidParameterException;
import java.util.*;
import java.util.stream.Collectors;

import com.regnosys.rosetta.rosetta.*;

import com.regnosys.rosetta.rosetta.expression.ExpressionFactory;
import com.regnosys.rosetta.rosetta.expression.RosettaIntLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaNumberLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaStringLiteral;
import com.regnosys.rosetta.types.builtin.RNumberType;
import com.regnosys.rosetta.types.builtin.RStringType;
import org.xmlet.xsdparser.core.XsdParserCore;
import org.xmlet.xsdparser.xsdelements.*;
import org.xmlet.xsdparser.xsdelements.xsdrestrictions.XsdStringRestrictions;

public class XsdUtil {
	private final Set<String> documentationSources = Set.of("Definition");
	
	public final String XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";
	
	public Optional<String> extractDocs(XsdAnnotatedElements ev) {
		return Optional.ofNullable(ev)
			.map(XsdAnnotatedElements::getAnnotation)
			.map(XsdAnnotation::getDocumentations)
			.map(xsdDocs -> xsdDocs.stream()
				// default to definition if source not specified
				.filter(x -> x.getSource() == null || documentationSources.contains(x.getSource()))
				.map(XsdAnnotationChildren::getContent)
				.map(x -> x.replace("\r\n", " "))
				.map(x -> x.replace('\n', ' '))
				.map(x -> x.replace('\r', ' '))
				.collect(Collectors.joining(" "))
			)
			.map(docs -> docs.isEmpty() ? null : docs);
	}
	
	public Optional<String> extractDocs(XsdAnnotatedElements ev, String docAnnotationSourceName) {
		return Optional.ofNullable(ev)
			.map(XsdAnnotatedElements::getAnnotation)
			.map(XsdAnnotation::getDocumentations)
			.map(xsdDocs -> xsdDocs.stream()
				.filter(x -> x.getSource() != null)
				.filter(x -> x.getSource().equals(docAnnotationSourceName))
				.map(XsdAnnotationChildren::getContent)
				.map(x -> x.replace("\r\n", " "))
				.map(x -> x.replace('\n', ' '))
				.map(x -> x.replace('\r', ' '))
				.collect(Collectors.joining(" "))
			)
			.map(docs -> docs.isEmpty() ? null : docs);
	}
	
	public boolean isEnumType(XsdSimpleType simpleType) {
		return getRestrictions(simpleType).stream()
				.anyMatch(e -> !e.getEnumeration().isEmpty());
	}

	public boolean isChoiceType(XsdSimpleType simpleType) {
		return !isEnumType(simpleType) && simpleType.getUnion() != null;
	}

	public List<XsdRestriction> getRestrictions(XsdSimpleType simpleType) {
		Map<String, XsdRestriction> restrictions = new HashMap<>();
		List<XsdRestriction> result = new ArrayList<>();

		XsdRestriction restriction = simpleType.getRestriction();
		XsdUnion union = simpleType.getUnion();

		if (restriction != null){
			result.add(restriction);
		}

		if (union != null){
			result.addAll(union.getUnionElements().stream().map(XsdSimpleType::getRestriction).toList());
		}
		return result;
	}
	
	public String getQualifiedName(XsdNamedElements elem) {
		String name = elem.getName();
		
		XsdAbstractElement original = getOriginalElement(elem);
		original.setParentAvailable(true);
		XsdSchema schema = original.getXsdSchema();
		if (schema == null) {
			return name;
		}
		
		String targetNamespace = schema.getTargetNamespace();
		if (targetNamespace == null) {
			return name;
		}
		return targetNamespace + "/" + name;
	}
	
	public boolean isTopLevelElement(XsdAbstractElement elem) {
		XsdAbstractElement original = getOriginalElement(elem);
		original.setParentAvailable(true);
		XsdAbstractElement p = original.getParent();
		return p instanceof XsdSchema;
	}
	private XsdAbstractElement getOriginalElement(XsdAbstractElement elem) {
		XsdAbstractElement original = elem;
		while (original.getCloneOf() != null) {
			original = original.getCloneOf();
		}
		return original;
	}

    public String toTypeName(String xsdName, ImportTargetConfig config) {
    	String overridenName = config.getNameOverrides().get(xsdName);
    	if (overridenName != null) {
    		return overridenName;
    	}
        String name = config.getPreferences().getTypeCasing().transform(xsdName);
        // TODO
        if (name.equals("Object")) {
        	return "_Object";
        }
        return name;
    }

    public String toAttributeName(String xsdName, ImportTargetConfig config) {
        return config.getPreferences().getAttributeCasing().transform(xsdName);
    }
    
    public String toEnumValueName(String xsdName, ImportTargetConfig config) {
    	return config.getPreferences().getEnumValueCasing().transform(xsdName);
    }

	public void makeNamesUnique(List<? extends RosettaNamed> objects) {
		objects.stream().collect(Collectors.groupingBy(RosettaNamed::getName)).forEach((name, group) -> {
			if (group.size() > 1) {
				for (int i=0; i<group.size(); i++) {
					group.get(i).setName(name + i);
				}
			}
		});
	}

	public void addTypeArguments(TypeCall tc, XsdRestriction restr) {
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
}
