package com.regnosys.rosetta.generator.daml.util

import com.regnosys.rosetta.types.RCalculationType
import com.regnosys.rosetta.types.RQualifiedType

class DamlTranslator {
				
	static def toDamlBasicType(String typename) {
		switch typename {
			case 'String':
				'Text'
			case 'string':
				'Text'
			case 'int':
				'Int'
			case 'time':
				'Text'
			case 'date':
				'Date'
			case 'dateTime':
				'Time'
			case 'zonedDateTime':
				'ZonedDateTime'
			case 'number':
				'Decimal'
			case 'boolean':
				'Bool'
			case RQualifiedType.PRODUCT_TYPE.qualifiedType:
				'Text'
			case RQualifiedType.EVENT_TYPE.qualifiedType:
				'Text'
			case RCalculationType.CALCULATION.calculationType:
				'Text'
		}
	}

	static def toDamlType(String typename) {
		val basicType = toDamlBasicType(typename);
		if (basicType !== null)
			return basicType
		else
			return typename
	}
}
