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

import com.regnosys.rosetta.rosetta.ParametrizedRosettaType;
import com.regnosys.rosetta.rosetta.RosettaBasicType;
import com.regnosys.rosetta.rosetta.RosettaEnumValue;
import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.rosetta.RosettaRecordType;
import com.regnosys.rosetta.rosetta.RosettaType;
import com.regnosys.rosetta.rosetta.RosettaTypeAlias;
import com.regnosys.rosetta.rosetta.TypeCall;
import com.regnosys.rosetta.rosetta.TypeCallArgument;
import com.regnosys.rosetta.rosetta.TypeParameter;
import com.regnosys.rosetta.rosetta.expression.ExpressionFactory;
import com.regnosys.rosetta.rosetta.expression.RosettaIntLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaStringLiteral;
import com.regnosys.rosetta.rosetta.RosettaFactory;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;

import jakarta.inject.Inject;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.xmlet.xsdparser.xsdelements.*;
import org.xmlet.xsdparser.xsdelements.xsdrestrictions.XsdEnumeration;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * This class is responsible for holding context necessary to resolve references.
 */
public class RosettaXsdMapping {
	private final Map<String, Supplier<TypeCall>> builtinTypeSuppliersMap = new HashMap<>();
	private final Map<XsdSimpleType, RosettaTypeAlias> simpleTypesMap = new HashMap<>();
	private final Map<XsdSimpleType, RosettaEnumeration> enumTypesMap = new HashMap<>();
	private final Map<XsdAbstractElement, Data> complexTypesMap = new HashMap<>();
    private final Map<String, Data> groupsMap = new HashMap<>();
	private final Map<XsdElement, Data> elementsMap = new HashMap<>();
	
	private final Map<XsdAbstractElement, Attribute> attributeMap = new HashMap<>();
	private final Map<XsdEnumeration, RosettaEnumValue> enumValueMap = new HashMap<>();

	private final RBuiltinTypeService builtins;
	private final XsdUtil util;
	
	@Inject
	public RosettaXsdMapping(RBuiltinTypeService builtins, XsdUtil util) {
		this.builtins = builtins;
		this.util = util;
	}
	
	public void initializeBuiltins(ResourceSet resourceSet, ImportTargetConfig targetConfig) {
		RosettaBasicType string = builtins.toRosettaType(builtins.UNCONSTRAINED_STRING, RosettaBasicType.class, resourceSet);
		RosettaBasicType number = builtins.toRosettaType(builtins.UNCONSTRAINED_NUMBER, RosettaBasicType.class, resourceSet);
		RosettaTypeAlias integer = builtins.toRosettaType(builtins.UNCONSTRAINED_INT, RosettaTypeAlias.class, resourceSet);
		RosettaBasicType booleanT = builtins.toRosettaType(builtins.BOOLEAN, RosettaBasicType.class, resourceSet);
		RosettaRecordType date = builtins.toRosettaType(builtins.DATE, RosettaRecordType.class, resourceSet);
		RosettaRecordType dateTime = builtins.toRosettaType(builtins.DATE_TIME, RosettaRecordType.class, resourceSet);
		RosettaBasicType time = builtins.toRosettaType(builtins.TIME, RosettaBasicType.class, resourceSet);
		RosettaRecordType zonedDateTime = builtins.toRosettaType(builtins.ZONED_DATE_TIME, RosettaRecordType.class, resourceSet);
		
		TypeParameter pattern = findParameter("pattern", string);
		TypeParameter minInt = findParameter("min", integer);
		TypeParameter maxInt = findParameter("max", integer);
		
		// see https://www.liquid-technologies.com/Reference/XmlStudio/XsdEditorNotation_BuiltInXsdTypes.html
		registerBuiltinTypeSupplier("anyURI", () -> toTypeCall(string, createTypeArgument(pattern, "\\w+:(\\/?\\/?)[^\\s]+")));
		// anyAtomicType
		// anySimpleType
		registerBuiltinTypeSupplier("base64Binary", () -> toTypeCall(string));
		registerBuiltinTypeSupplier("boolean", () -> toTypeCall(booleanT));
		registerBuiltinTypeSupplier("byte", () -> toTypeCall(integer, createTypeArgument(minInt, -128), createTypeArgument(maxInt, 127)));
		registerBuiltinTypeSupplier("date", () -> toTypeCall(targetConfig.getPreferences().getXsdDateHandlingStrategy() == XsdDateHandlingStrategy.AS_RUNE_DATE ? date : zonedDateTime));
		registerBuiltinTypeSupplier("dateTime", () -> toTypeCall(zonedDateTime));
		// dateTimeStamp
		// dayTimeDuration
		registerBuiltinTypeSupplier("decimal", () -> toTypeCall(number));
		registerBuiltinTypeSupplier("double", () -> toTypeCall(number));
		registerBuiltinTypeSupplier("duration", () -> toTypeCall(dateTime));
		// ENTITIES
		// ENTITY
		registerBuiltinTypeSupplier("float", () -> toTypeCall(number));
		registerBuiltinTypeSupplier("gDay", () -> toTypeCall(string));
		registerBuiltinTypeSupplier("gMonth", () -> toTypeCall(string));
		registerBuiltinTypeSupplier("gMonthDay", () -> toTypeCall(string));
		registerBuiltinTypeSupplier("gYear", () -> toTypeCall(string));
		registerBuiltinTypeSupplier("gYearMonth", () -> toTypeCall(string));
		registerBuiltinTypeSupplier("hexBinary", () -> toTypeCall(string));
		registerBuiltinTypeSupplier("ID", () -> toTypeCall(string));
		registerBuiltinTypeSupplier("IDREF", () -> toTypeCall(string));
		// IDREFS
		registerBuiltinTypeSupplier("int", () -> toTypeCall(integer));
		registerBuiltinTypeSupplier("integer", () -> toTypeCall(integer));
		registerBuiltinTypeSupplier("language", () -> toTypeCall(string));
		registerBuiltinTypeSupplier("long", () -> toTypeCall(integer));
		// Name
		// NCName
		registerBuiltinTypeSupplier("negativeInteger", () -> toTypeCall(integer, createTypeArgument(maxInt, -1)));
		// NMTOKEN
		// NMTOKENS
		registerBuiltinTypeSupplier("nonNegativeInteger", () -> toTypeCall(integer, createTypeArgument(minInt, 0)));
		registerBuiltinTypeSupplier("nonPositiveInteger", () -> toTypeCall(integer, createTypeArgument(maxInt, 0)));
		registerBuiltinTypeSupplier("normalizedString", () -> toTypeCall(string));
		// NOTATION
		registerBuiltinTypeSupplier("positiveInteger", () -> toTypeCall(integer, createTypeArgument(minInt, 1)));
		registerBuiltinTypeSupplier("precisionDecimal", () -> toTypeCall(number));
		// QName
		registerBuiltinTypeSupplier("short", () -> toTypeCall(integer));
		registerBuiltinTypeSupplier("string", () -> toTypeCall(string));
		registerBuiltinTypeSupplier("time", () -> toTypeCall(time));
		registerBuiltinTypeSupplier("token", () -> toTypeCall(string));
		registerBuiltinTypeSupplier("unsignedByte", () -> toTypeCall(integer, createTypeArgument(minInt, 0), createTypeArgument(maxInt, 255)));
		registerBuiltinTypeSupplier("unsignedInt", () -> toTypeCall(integer, createTypeArgument(minInt, 0)));
		registerBuiltinTypeSupplier("unsignedLong", () -> toTypeCall(integer, createTypeArgument(minInt, 0)));
		registerBuiltinTypeSupplier("unsignedShort", () -> toTypeCall(integer, createTypeArgument(minInt, 0)));
		registerBuiltinTypeSupplier("yearMonthDuration", () -> toTypeCall(dateTime));
	}
	
	private void registerBuiltinTypeSupplier(String typeName, Supplier<TypeCall> supplier) {
		if (builtinTypeSuppliersMap.containsKey(typeName)) {
			throw new IllegalArgumentException("There is already a registered type with the name " + typeName + ".");
		}
		builtinTypeSuppliersMap.put(typeName, supplier);
	}
	private TypeCall toTypeCall(RosettaType type, TypeCallArgument... arguments) {
		TypeCall tc = RosettaFactory.eINSTANCE.createTypeCall();
		tc.setType(type);
		for (TypeCallArgument arg : arguments) {
			tc.getArguments().add(arg);
		}
		return tc;
	}
	private TypeCallArgument createTypeArgument(TypeParameter parameter, int value) {
		TypeCallArgument arg = createTypeArgumentWithoutValue(parameter);
		arg.setValue(createIntLiteral(value));
		return arg;
	}
	private TypeCallArgument createTypeArgument(TypeParameter parameter, String value) {
		TypeCallArgument arg = createTypeArgumentWithoutValue(parameter);
		arg.setValue(createStringLiteral(value));
		return arg;
	}
	private TypeCallArgument createTypeArgumentWithoutValue(TypeParameter parameter) {
		TypeCallArgument arg = RosettaFactory.eINSTANCE.createTypeCallArgument();
		arg.setParameter(parameter);
		return arg;
	}
	private TypeParameter findParameter(String name, ParametrizedRosettaType type) {
		return type.getParameters().stream()
				.filter(p -> p.getName().equals(name))
				.findFirst()
				.orElseThrow();
	}
	private RosettaIntLiteral createIntLiteral(int value) {
		RosettaIntLiteral lit = ExpressionFactory.eINSTANCE.createRosettaIntLiteral();
		lit.setValue(BigInteger.valueOf(value));
		return lit;
	}
	private RosettaStringLiteral createStringLiteral(String value) {
		RosettaStringLiteral lit = ExpressionFactory.eINSTANCE.createRosettaStringLiteral();
		lit.setValue(value);
		return lit;
	}
	
	public void registerSimpleType(XsdSimpleType simpleType, RosettaTypeAlias type) {
		if (simpleTypesMap.containsKey(simpleType)) {
			throw new IllegalArgumentException("There is already a registered type with the name " + simpleType.getName() + ".");
		}
		simpleTypesMap.put(simpleType, type);
	}
	public void registerEnumType(XsdSimpleType simpleType, RosettaEnumeration type) {
		if (enumTypesMap.containsKey(simpleType)) {
			throw new IllegalArgumentException("There is already a registered type with the name " + simpleType.getName() + ".");
		}
		enumTypesMap.put(simpleType, type);
	}
	public void registerComplexType(XsdAbstractElement complexType, Data data) {
        if (!(complexType instanceof XsdComplexType || complexType instanceof XsdMultipleElements)) {
            throw new IllegalArgumentException("Illegal complex type " + complexType + " of class " + complexType.getClass() + ".");
        }
		if (complexTypesMap.containsKey(complexType)) {
			throw new IllegalArgumentException("There is already a registered type for " + complexType + ".");
		}
		complexTypesMap.put(complexType, data);
	}
    public void registerGroup(XsdGroup group, Data data) {
        if (groupsMap.containsKey(group.getName())) {
            throw new IllegalArgumentException("There is already a registered type for " + group.getName() + ".");
        }
        groupsMap.put(group.getName(), data);
    }
	public void registerElement(XsdElement element, Data data) {
		if (elementsMap.containsKey(element)) {
			throw new IllegalArgumentException("There is already a registered element with the name " + element.getName() + ".");
		}
		elementsMap.put(element, data);
	}
	public void registerAttribute(XsdAbstractElement elem, Attribute attr) {
		if (attributeMap.containsKey(elem)) {
			throw new IllegalArgumentException("There is already a registered attribute for " + elem + ".");
		}
		attributeMap.put(elem, attr);
	}
	public void registerEnumValue(XsdEnumeration elem, RosettaEnumValue value) {
		if (enumValueMap.containsKey(elem)) {
			throw new IllegalArgumentException("There is already a registered enum value with the name " + elem.getValue() + ".");
		}
		enumValueMap.put(elem, value);
	}
	
	public TypeCall getRosettaTypeCall(XsdAbstractElement element) {
		if (element instanceof XsdBuiltInDataType builtin) {
			return getRosettaTypeCallFromBuiltin(builtin.getName());
		} else if (element instanceof XsdSimpleType simple) {
            if (util.isEnumType(simple)) {
				return toTypeCall(getRosettaEnumerationFromSimple(simple));
			}
			return toTypeCall(getRosettaTypeFromSimple(simple));
		} else if (element instanceof XsdComplexType || element instanceof XsdMultipleElements) {
            return toTypeCall(getRosettaTypeFromComplex(element));
        } else if (element instanceof XsdGroup group) {
            return toTypeCall(getRosettaTypeFromGroup(group));
		} else if (element instanceof XsdElement) {
			return toTypeCall(getRosettaTypeFromElement((XsdElement)element));
		}
		throw new RuntimeException("Unsupported Xsd type " + element + " of class " + element.getClass().getSimpleName() + ".");
	}
	public boolean hasType(XsdAbstractElement element) {
		if (element instanceof XsdBuiltInDataType builtin) {
			return builtinTypeSuppliersMap.containsKey(builtin.getName());
		} else if (element instanceof XsdSimpleType simple) {
            if (util.isEnumType(simple)) {
				return enumTypesMap.containsKey(simple);
			}
			return simpleTypesMap.containsKey(simple);
		} else if (element instanceof XsdComplexType || element instanceof XsdMultipleElements) {
			return complexTypesMap.containsKey(element);
		} else if (element instanceof XsdGroup group) {
            return groupsMap.containsKey(group.getName());
        } else if (element instanceof XsdElement) {
			return elementsMap.containsKey((XsdElement)element);
		}
		throw new RuntimeException("Unsupported Xsd type " + element + " of class " + element.getClass().getSimpleName() + ".");
	}
	public TypeCall getRosettaTypeCallFromBuiltin(String builtinType) {
		Supplier<TypeCall> supp = builtinTypeSuppliersMap.get(normalizeBuiltinTypeName(builtinType));
		if (supp == null) {
			throw new RuntimeException("The builtin type " + builtinType + " is not supported.");
		}
		return supp.get();
	}
	private String normalizeBuiltinTypeName(String name) {
		String[] parts = name.split("_");
		return parts[parts.length - 1];
	}
	public RosettaTypeAlias getRosettaTypeFromSimple(XsdSimpleType simpleType) {
		RosettaTypeAlias t = simpleTypesMap.get(simpleType);
		if (t == null) {
			throw new RuntimeException("No registered simple type " + simpleType.getName() + " was found.");
		}
		return t;
	}
	public RosettaEnumeration getRosettaEnumerationFromSimple(XsdSimpleType simpleType) {
		RosettaEnumeration t = enumTypesMap.get(simpleType);
		if (t == null) {
			throw new RuntimeException("No registered simple type " + simpleType.getName() + " was found.");
		}
		return t;
	}
	public Data getRosettaTypeFromComplex(XsdAbstractElement complexType) {
		Data t = complexTypesMap.get(complexType);
		if (t == null) {
            if (complexType instanceof XsdNamedElements named) {
                throw new RuntimeException("No registered complex type " + complexType + " (" + named.getName() + ") was found.");
            } else {
                throw new RuntimeException("No registered complex type " + complexType + " was found.");
            }
		}
		return t;
	}
    public Data getRosettaTypeFromGroup(XsdGroup group) {
        Data t = groupsMap.get(group.getName());
        if (t == null) {
            throw new RuntimeException("No registered group " + group + " (" + group.getName() + ") was found.");
        }
        return t;
    }
	public Data getRosettaTypeFromElement(XsdElement element) {
		Data t = elementsMap.get(element);
		if (t == null) {
			throw new RuntimeException("No registered complex type " + element.getName() + " was found.");
		}
		return t;
	}
	public Attribute getAttribute(XsdAbstractElement elem) {
		Attribute a = attributeMap.get(elem);
		if (a == null) {
			throw new RuntimeException("No registered attribute " + elem + " was found.");
		}
		return a;
	}
	public RosettaEnumValue getEnumValue(XsdEnumeration elem) {
		RosettaEnumValue v = enumValueMap.get(elem);
		if (v == null) {
			throw new RuntimeException("No registered enum value " + elem.getValue() + " was found.");
		}
		return v;
	}
	public Stream<XsdElement> getElementsWithComplexType(XsdNamedElements complexType) {
		return elementsMap.keySet().stream().filter(elem -> complexType.equals(elem.getTypeAsXsd()));
	}
}
