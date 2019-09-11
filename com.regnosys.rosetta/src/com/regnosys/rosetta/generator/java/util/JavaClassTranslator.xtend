package com.regnosys.rosetta.generator.java.util

import com.google.common.base.Splitter
import com.regnosys.rosetta.generator.java.RosettaJavaPackages
import com.regnosys.rosetta.rosetta.RosettaRegularAttribute
import com.regnosys.rosetta.rosetta.RosettaCalculation
import com.regnosys.rosetta.rosetta.RosettaClass
import com.regnosys.rosetta.types.RCalculationType
import com.regnosys.rosetta.types.RQualifiedType

import static extension com.regnosys.rosetta.generator.util.RosettaAttributeExtensions.cardinalityIsListValue

class JavaClassTranslator {
				
	static def toJavaFullType(String typename) {
		switch typename {
			case 'any':
				'java.lang.Object'
			case 'string':
				'java.lang.String'
			case 'int':
				'java.lang.Integer'
			case 'time':
				'java.time.LocalTime'
			case 'date':
				'com.rosetta.model.lib.records.Date'
			case 'dateTime':
				'java.time.LocalDateTime'
			case 'zonedDateTime':
				'java.time.ZonedDateTime'
			case 'number':
				'java.math.BigDecimal'
			case 'boolean':
				'java.lang.Boolean'
			case RQualifiedType.PRODUCT_TYPE.qualifiedType:
				'java.lang.String'
			case RQualifiedType.EVENT_TYPE.qualifiedType:
				'java.lang.String'
			case RCalculationType.CALCULATION.calculationType:
				'java.lang.String'
		}
	}

	static def toJavaType(String typename) {
		val basicType = toJavaFullType(typename);
		if (basicType === null) {
			return typename
		} else {
			return Splitter.on('.').splitToList(basicType).last
		}
	}

	static def getPackages(RosettaRegularAttribute attribute, RosettaJavaPackages packages) {
		val imports = newLinkedList();
		val basicType = toJavaFullType(attribute.type.name);
		
		if (basicType !== null && !basicType.startsWith('java.lang')) {
			imports.add(basicType)
		}

		if (attribute.cardinalityIsListValue) {
			imports.add('java.util.List')
			imports.add('java.util.ArrayList')
			imports.add('java.util.stream.Collectors')
			imports.add('java.util.Objects')
		}
		
		if(attribute.type instanceof RosettaClass && (attribute.cardinalityIsListValue || !attribute.metaTypes.empty)) {
			imports.add('java.util.stream.Collectors')	
		}
		
		if(attribute.type instanceof RosettaClass) {
			imports.add('static java.util.Optional.ofNullable')
		}
		if(attribute.type instanceof RosettaCalculation) {
			imports.add(packages.calculation.packageName + "." + attribute.type.name)
		}
		return imports
	}

	static def boolean hasAttr(RosettaClass rosettaClass) {
		return rosettaClass !== null && (!rosettaClass.regularAttributes.isEmpty || hasAttr(rosettaClass.superType))
	}
	
	static def toJavaImportSet(RosettaClass rosettaClass, RosettaJavaPackages javaPacks) {
		val imports = rosettaClass
			.allSuperClasses
			.map[regularAttributes.map[getPackages(javaPacks)].flatten.filter[it !== null]]
			.flatten.toSet
		if (rosettaClass.hasAttr) {
			imports.add('static java.util.Optional.ofNullable')
		}
		return imports;
	}
	
	static def allSuperClasses(RosettaClass rosettaClass) {
		val allClasses = newArrayList
		var current = rosettaClass
		while (current !== null) {
			allClasses.add(current)
			current = current.superType
		}
		return allClasses
	}
}
