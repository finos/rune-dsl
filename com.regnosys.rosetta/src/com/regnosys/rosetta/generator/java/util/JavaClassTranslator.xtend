package com.regnosys.rosetta.generator.java.util

import com.google.common.base.Splitter
import com.regnosys.rosetta.types.RCalculationType
import com.regnosys.rosetta.types.RQualifiedType
import com.rosetta.model.lib.meta.Key

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
	
	static def toJavaClass(String typeName) {
		switch typeName {
		case 'Key', case 'Keys':
			Key
		case 'string':
			String
		default :
			Object
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
}
