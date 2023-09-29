package com.regnosys.rosetta.tools.modelimport;

import com.regnosys.rosetta.rosetta.RosettaBasicType;
import com.regnosys.rosetta.rosetta.RosettaEnumValue;
import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaRecordType;
import com.regnosys.rosetta.rosetta.RosettaType;
import com.regnosys.rosetta.rosetta.RosettaTypeAlias;
import com.regnosys.rosetta.rosetta.RosettaExternalSynonymSource;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;

import javax.inject.Inject;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.xmlet.xsdparser.xsdelements.XsdBuiltInDataType;
import org.xmlet.xsdparser.xsdelements.XsdComplexType;
import org.xmlet.xsdparser.xsdelements.XsdNamedElements;
import org.xmlet.xsdparser.xsdelements.XsdSimpleType;
import org.xmlet.xsdparser.xsdelements.xsdrestrictions.XsdEnumeration;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * This class is responsible for holding context necessary to resolve references.
 */
public class RosettaXsdMapping {
	private final String xsdTypesFileName = "builtinxsdtypes.rosetta";
	private final String[] xsdTypesPath = new String[]{"modelimport", xsdTypesFileName};
	
	public final URI xsdTypesURI = URI.createHierarchicalURI("classpath", null, null, xsdTypesPath, null, null);
	public final URL xsdTypesURL = Objects.requireNonNull(this.getClass().getResource(xsdTypesURI.path()));
	
	private Optional<RosettaExternalSynonymSource> synonymSource = Optional.empty();
	
	private final Map<String, RosettaType> builtinTypesMap = new HashMap<>();
	private final Map<XsdSimpleType, RosettaTypeAlias> simpleTypesMap = new HashMap<>();
	private final Map<XsdSimpleType, RosettaEnumeration> enumTypesMap = new HashMap<>();
	private final Map<XsdComplexType, Data> complexTypesMap = new HashMap<>();
	
	private final Map<XsdNamedElements, Attribute> attributeMap = new HashMap<>();
	private final Map<XsdEnumeration, RosettaEnumValue> enumValueMap = new HashMap<>();

	private final RBuiltinTypeService builtins;
	private final XsdUtil util;
	
	@Inject
	public RosettaXsdMapping(RBuiltinTypeService builtins, XsdUtil util) {
		this.builtins = builtins;
		this.util = util;
	}
	
	private RosettaType getType(String name, RosettaModel model) {
		return model.getElements().stream()
				.filter(elem -> elem instanceof RosettaType)
				.map(elem -> (RosettaType)elem)
				.filter(t -> name.equals(t.getName()))
				.findFirst()
				.orElseThrow();
	}
	public void initializeBuiltins(ResourceSet resourceSet) {
		Resource xsdResource = resourceSet.createResource(URI.createURI(xsdTypesFileName));
		try {
			xsdResource.load(xsdTypesURL.openStream(), null);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		RosettaModel xsdModel = (RosettaModel)xsdResource.getContents().get(0);
		resourceSet.getResources().remove(xsdResource);
		
		RosettaBasicType string = builtins.toRosettaType(builtins.UNCONSTRAINED_STRING, RosettaBasicType.class, resourceSet);
		RosettaBasicType number = builtins.toRosettaType(builtins.UNCONSTRAINED_NUMBER, RosettaBasicType.class, resourceSet);
		RosettaTypeAlias integer = builtins.toRosettaType(builtins.UNCONSTRAINED_INT, RosettaTypeAlias.class, resourceSet);
		RosettaBasicType booleanT = builtins.toRosettaType(builtins.BOOLEAN, RosettaBasicType.class, resourceSet);
		RosettaRecordType date = builtins.toRosettaType(builtins.DATE, RosettaRecordType.class, resourceSet);
		RosettaRecordType dateTime = builtins.toRosettaType(builtins.DATE_TIME, RosettaRecordType.class, resourceSet);
		RosettaBasicType time = builtins.toRosettaType(builtins.TIME, RosettaBasicType.class, resourceSet);
		RosettaRecordType zonedDateTime = builtins.toRosettaType(builtins.ZONED_DATE_TIME, RosettaRecordType.class, resourceSet);

		RosettaType anyURI = getType("anyURI", xsdModel);
		RosettaType duration = getType("duration", xsdModel);
		RosettaType negativeInteger = getType("negativeInteger", xsdModel);
		RosettaType nonPositiveInteger = getType("nonPositiveInteger", xsdModel);
		RosettaType nonNegativeInteger = getType("nonNegativeInteger", xsdModel);
		RosettaType positiveInteger = getType("positiveInteger", xsdModel);
		
		// TODO: make an extensive list of all builtin xsd types and map them to appropriate places.
		// see https://www.liquid-technologies.com/Reference/XmlStudio/XsdEditorNotation_BuiltInXsdTypes.html
		registerBuiltinType("anyURI", anyURI);
		registerBuiltinType("boolean", booleanT);
		registerBuiltinType("date", date);
		registerBuiltinType("dateTime", zonedDateTime);
		registerBuiltinType("time", time);
		registerBuiltinType("duration", duration);
		registerBuiltinType("dayTimeDuration", duration);
		registerBuiltinType("yearMonthDuration", duration);
		registerBuiltinType("gDay", integer);
		registerBuiltinType("gMonth", integer);
		registerBuiltinType("gMonthDay", integer);
		registerBuiltinType("gYear", integer);
		registerBuiltinType("gYearMonth", integer);
		registerBuiltinType("decimal", number);
		registerBuiltinType("integer", integer);
		registerBuiltinType("nonPositiveInteger", nonPositiveInteger);
		registerBuiltinType("negativeInteger", negativeInteger);
		registerBuiltinType("long", integer);
		registerBuiltinType("int", integer);
		registerBuiltinType("short", integer);
		registerBuiltinType("byte", integer);
		registerBuiltinType("nonNegativeInteger", nonNegativeInteger);
		registerBuiltinType("unsignedLong", nonNegativeInteger);
		registerBuiltinType("unsignedInt", nonNegativeInteger);
		registerBuiltinType("unsignedShort", nonNegativeInteger);
		registerBuiltinType("unsignedByte", nonNegativeInteger);
		registerBuiltinType("positiveInteger", positiveInteger);
		registerBuiltinType("double", number);
		registerBuiltinType("float", number);
		registerBuiltinType("QName", string);
		registerBuiltinType("NOTATION", string);
		registerBuiltinType("string", string);
		registerBuiltinType("normalizedString", string);
		registerBuiltinType("token", string);
		registerBuiltinType("language", string);
		registerBuiltinType("NMTOKEN", string);
		registerBuiltinType("Name", string);
		registerBuiltinType("NCName", string);
		registerBuiltinType("ID", string);
		registerBuiltinType("IDREF", string);
		registerBuiltinType("ENTITY", string);
		registerBuiltinType("untypedAtomic", string);
	}
	
	private void registerBuiltinType(String typeName, RosettaType type) {
		if (builtinTypesMap.containsKey(typeName)) {
			throw new IllegalArgumentException("There is already a registered type with the name " + typeName + ".");
		}
		builtinTypesMap.put(typeName, type);
		builtinTypesMap.put("xs_" + typeName, type);
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
	public void registerComplexType(XsdComplexType complexType, Data data) {
		if (complexTypesMap.containsKey(complexType)) {
			throw new IllegalArgumentException("There is already a registered type with the name " + complexType.getName() + ".");
		}
		complexTypesMap.put(complexType, data);
	}
	public void registerAttribute(XsdNamedElements elem, Attribute attr) {
		if (attributeMap.containsKey(elem)) {
			throw new IllegalArgumentException("There is already a registered attribute with the name " + elem.getName() + ".");
		}
		attributeMap.put(elem, attr);
	}
	public void registerEnumValue(XsdEnumeration elem, RosettaEnumValue value) {
		if (enumValueMap.containsKey(elem)) {
			throw new IllegalArgumentException("There is already a registered enum value with the name " + elem.getValue() + ".");
		}
		enumValueMap.put(elem, value);
	}
	public void registerSynonymSource(RosettaExternalSynonymSource source) {
		if (this.synonymSource.isPresent()) {
			throw new IllegalArgumentException("There is already a registered synonym source.");
		}
		this.synonymSource = Optional.of(source);
	}
	
	public RosettaType getRosettaType(XsdNamedElements element) {
		if (element instanceof XsdBuiltInDataType) {
			return getRosettaTypeFromBuiltin(element.getName());
		}  else if (element instanceof XsdSimpleType) {
			XsdSimpleType simple = (XsdSimpleType)element;
			if (util.isEnumType(simple)) {
				return getRosettaEnumerationFromSimple(simple);
			}
			return getRosettaTypeFromSimple(simple);
		} else if (element instanceof XsdComplexType) {
			return getRosettaTypeFromComplex((XsdComplexType)element);
		}
		throw new RuntimeException("Unsupported Xsd type " + element.getName() + " of class " + element.getClass().getSimpleName() + ".");
	}
	public RosettaType getRosettaTypeFromBuiltin(String builtinType) {
		RosettaType t = builtinTypesMap.get(builtinType);
		if (t == null) {
			throw new RuntimeException("The builtin type " + builtinType + " is not supported.");
		}
		return t;
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
	public Data getRosettaTypeFromComplex(XsdComplexType complexType) {
		Data t = complexTypesMap.get(complexType);
		if (t == null) {
			throw new RuntimeException("No registered complex type " + complexType.getName() + " was found.");
		}
		return t;
	}
	public Attribute getAttribute(XsdNamedElements elem) {
		Attribute a = attributeMap.get(elem);
		if (a == null) {
			throw new RuntimeException("No registered attribute " + elem.getName() + " was found.");
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
	
	public RosettaExternalSynonymSource getSynonymSource() {
		return this.synonymSource.get();
	}
}
