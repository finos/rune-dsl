package com.regnosys.rosetta.types

import com.regnosys.rosetta.rosetta.RosettaCalculation
import com.regnosys.rosetta.rosetta.RosettaClass
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.RosettaFeatureOwner
import com.regnosys.rosetta.rosetta.RosettaNamed
import org.eclipse.xtend.lib.annotations.Data

interface RType {
	def String getName()
}

@Data
class RFeatureCallType implements RType {
	val RType featureType

	override getName() {
		'''featureCall(«featureType?.name»)'''
	}
}

@Data
class RClassType implements RType {
	RosettaClass clazz

	override getName() {
		clazz.name
	}
}

@Data
class REnumType implements RType {
	RosettaEnumeration enumeration

	override getName() {
		enumeration.name
	}
}

@Data
class RRecordType implements RType {
	RosettaFeatureOwner record

	override getName() {
		(record as RosettaNamed).name
	}
	
}

@Data
class RBuiltinType implements RType {
	public static val BOOLEAN = new RBuiltinType('boolean')
	public static val STRING = new RBuiltinType('string')

	public static val INT = new RNumberType('int', 0)
	public static val NUMBER = new RNumberType('number', 1)

	public static val DATE = new RBuiltinType('date')
	public static val DATE_TIME = new RBuiltinType('dateTime')
	public static val ZONED_DATE_TIME = new RBuiltinType('zonedDateTime')
	public static val TIME = new RBuiltinType('time')

	public static val FUNCTION = new RBuiltinType('function')

	public static val MISSING = new RBuiltinType('missing')
	public static val VOID = new RBuiltinType('void')
	
	val String name
}

@Data
class RQualifiedType extends RBuiltinType {
	public static val PRODUCT_TYPE = new RQualifiedType('string', 'productType')
	public static val EVENT_TYPE = new RQualifiedType('string', 'eventType')

	val String qualifiedType
}

@Data
class RCalculationType extends RBuiltinType {
	public static val CALCULATION = new RCalculationType('string', 'calculation')
	
	val String calculationType
}

@Data
class RNumberType extends RBuiltinType {
	val int rank

	def static RNumberType getCommonType(RNumberType t0, RNumberType t1) {
		if (t0.rank < t1.rank)
			t1
		else
			t0
	}
}

@Data
class RUnionType implements RType {
	val RType from
	val RType to
	val String name
	val RosettaCalculation converter

	new(RType from, RType to, RosettaCalculation converter) {
		this.from = from
		this.to = to
		this.converter = converter
		this.name = from.name + ' or ' + to.name
	}

	override getName() {
		name
	}
	
	def String getToName() {
		if (to instanceof RUnionType) to.toName else to.name
	}
}

@Data
class RErrorType implements RType {

	val String message

	override getName() {
		message
	}
}
