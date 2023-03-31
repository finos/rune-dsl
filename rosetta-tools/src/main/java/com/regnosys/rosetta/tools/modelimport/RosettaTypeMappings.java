package com.regnosys.rosetta.tools.modelimport;

import com.regnosys.rosetta.rosetta.RosettaBasicType;
import com.regnosys.rosetta.rosetta.RosettaFactory;
import com.regnosys.rosetta.rosetta.RosettaRecordType;
import com.regnosys.rosetta.rosetta.RosettaType;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class RosettaTypeMappings {

	private final HashMap<String, RosettaType> xsdTypesToRosetta;

	public RosettaTypeMappings() {
		xsdTypesToRosetta = new HashMap<>();

		RosettaBasicType string = RosettaFactory.eINSTANCE.createRosettaBasicType();
		string.setName("string");

		RosettaBasicType number = RosettaFactory.eINSTANCE.createRosettaBasicType();
		number.setName("number");

		RosettaBasicType integer = RosettaFactory.eINSTANCE.createRosettaBasicType();
		integer.setName("int");

		RosettaBasicType booleanT = RosettaFactory.eINSTANCE.createRosettaBasicType();
		booleanT.setName("boolean");

		RosettaRecordType date = RosettaFactory.eINSTANCE.createRosettaRecordType();
		date.setName("date");

		RosettaRecordType time = RosettaFactory.eINSTANCE.createRosettaRecordType();
		time.setName("time");

		RosettaRecordType zonedDateTime = RosettaFactory.eINSTANCE.createRosettaRecordType();
		zonedDateTime.setName("zonedDateTime");

		RosettaRecordType dateRange = RosettaFactory.eINSTANCE.createRosettaRecordType();
		dateRange.setName("dateRange");

		addMapping("anyURI", string);
		addMapping("boolean", booleanT);
		addMapping("date", date);
		addMapping("dateTime", zonedDateTime);
		addMapping("time", time);
		addMapping("duration", integer);
		addMapping("dayTimeDuration", integer);
		addMapping("yearMonthDuration", integer);
		addMapping("gDay", zonedDateTime);
		addMapping("gMonth", zonedDateTime);
		addMapping("gMonthDay", zonedDateTime);
		addMapping("gYear", zonedDateTime);
		addMapping("gYearMonth", zonedDateTime);
		addMapping("decimal", number);
		addMapping("integer", number);
		addMapping("nonPositiveInteger", number);
		addMapping("negativeInteger", number);
		addMapping("long", number);
		addMapping("int", integer);
		addMapping("short", integer);
		addMapping("byte", integer);
		addMapping("nonNegativeInteger", integer);
		addMapping("unsignedLong", number);
		addMapping("unsignedInt", integer);
		addMapping("unsignedShort", integer);
		addMapping("unsignedByte", string);
		addMapping("positiveInteger", number);
		addMapping("double", number);
		addMapping("float", number);
		addMapping("QName", string);
		addMapping("NOTATION", string);
		addMapping("string", string);
		addMapping("normalizedString", string);
		addMapping("token", string);
		addMapping("language", string);
		addMapping("NMTOKEN", string);
		addMapping("Name", string);
		addMapping("NCName", string);
		addMapping("ID", string);
		addMapping("IDREF", string);
		addMapping("ENTITY", string);
		addMapping("untypedAtomic", string);
	}

	private void addMapping(String rawXsdType, RosettaType type) {
		xsdTypesToRosetta.put(rawXsdType, type);
		xsdTypesToRosetta.put("xsd:" + rawXsdType, type);
		xsdTypesToRosetta.put("xs:" + rawXsdType, type);

	}

	public RosettaType getRosettaBasicType(String rawXsdType) {
		return xsdTypesToRosetta.get(rawXsdType);
	}

	public Set<RosettaType> getAllBasicTypes() {
		return new HashSet<>(xsdTypesToRosetta.values());
	}

}
