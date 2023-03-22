package com.regnosys.rosetta.generator.java.util

import com.rosetta.model.lib.meta.Key
import com.regnosys.rosetta.types.RBuiltinType
import static com.regnosys.rosetta.types.RBuiltinType.*
import com.regnosys.rosetta.generator.java.types.JavaType
import java.math.BigDecimal
import com.rosetta.model.lib.records.Date
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.LocalTime
import com.regnosys.rosetta.types.RRecordType
import com.regnosys.rosetta.types.RQualifiedType
import com.regnosys.rosetta.types.RCalculationType
import com.regnosys.rosetta.types.RType
import com.google.common.base.Splitter
import com.regnosys.rosetta.generator.java.types.JavaClass

class JavaClassTranslator {
	@Deprecated
	static def RType toRType(String typename) {
		switch typename {
			case 'any':
				ANY
			case 'string':
				STRING
			case 'int':
				INT
			case 'time':
				TIME
			case 'date':
				DATE
			case 'dateTime':
				DATE_TIME
			case 'zonedDateTime':
				ZONED_DATE_TIME
			case 'number':
				NUMBER
			case 'boolean':
				BOOLEAN
			case RQualifiedType.PRODUCT_TYPE.qualifiedType:
				RQualifiedType.PRODUCT_TYPE
			case RQualifiedType.EVENT_TYPE.qualifiedType:
				RQualifiedType.EVENT_TYPE
			case RCalculationType.CALCULATION.calculationType:
				RCalculationType.CALCULATION
		}
	}
	static def toJavaFullType(RBuiltinType t) {
		val clazz = switch t {
			case ANY: Object
			case BOOLEAN: Boolean
			case STRING: String
			case INT: Integer
			case NUMBER: BigDecimal
			case DATE: Date
			case DATE_TIME: LocalDateTime
			case ZONED_DATE_TIME: ZonedDateTime
			case TIME: LocalTime
			case VOID: Void
			case NOTHING: Void
			case RQualifiedType.PRODUCT_TYPE:
				String
			case RQualifiedType.EVENT_TYPE:
				String
			case RCalculationType.CALCULATION:
				String
		}
		JavaType.from(clazz)
	}
	static def JavaClass toJavaFullType(RRecordType t) {
		val clazz = switch t.name {
			case "date": Date
			case "zonedDateTime": ZonedDateTime
		}
		JavaClass.from(clazz)
	}

	static def toJavaClass(String typeName) {
		switch typeName {
			case 'Key',
			case 'Keys':
				Key
			case 'string':
				String
			case 'date':
				com.rosetta.model.lib.records.Date
			case 'int':
				java.lang.Integer
			case 'time':
				java.time.LocalTime
			case 'dateTime':
				java.time.LocalDateTime
			case 'zonedDateTime':
				java.time.ZonedDateTime
			case 'number':
				java.math.BigDecimal
			case 'boolean':
				java.lang.Boolean
			default:
				Object
		}
	}

	static def toJavaBuilderClass(String typeName) {
		switch typeName {
			case 'Key',
			case 'Keys':
				Key.KeyBuilder
			case 'string':
				String
			default:
				Object
		}
	}

	@Deprecated
	static def toJavaType(String typename) {
		val t = typename.toRType
		val jt = switch t {
			RBuiltinType: t.toJavaFullType.toString
			RRecordType: t.toJavaFullType.toString
			default: typename
		}
		return Splitter.on('.').splitToList(jt).last
	}
}
