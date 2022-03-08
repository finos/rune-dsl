package com.regnosys.rosetta.types

import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.RosettaFeatureOwner
import com.regnosys.rosetta.rosetta.RosettaNamed
import org.eclipse.xtend.lib.annotations.Data

abstract class RType {
	def String getName()
	def boolean hasMeta() { false }
	
	override String toString() { name }
}

abstract class RAnnotateType extends RType {
	boolean meta = false

	def void setWithMeta(boolean meta) {
		this.meta = meta
	}

	override hasMeta() {
		this.meta
	}
}

@Data
class RDataType extends RAnnotateType {
	com.regnosys.rosetta.rosetta.simple.Data data

	override getName() {
		data.name
	}
	
	// prevent @Data annotation from overriding `toString`
	override String toString() { super.toString }
}

@Data
class REnumType extends RAnnotateType {
	RosettaEnumeration enumeration

	override getName() {
		enumeration.name
	}
	
	// prevent @Data annotation from overriding `toString`
	override String toString() { super.toString }
}


@Data
class RRecordType extends RAnnotateType {
	RosettaFeatureOwner record

	override getName() {
		(record as RosettaNamed).name
	}
	
	// prevent @Data annotation from overriding `toString`
	override String toString() { super.toString }
}

@Data
class RBuiltinType extends RAnnotateType {
	public static val ANY = new RBuiltinType('any')
	public static val BOOLEAN = new RBuiltinType('boolean')
	public static val STRING = new RBuiltinType('string')

	public static val INT = new RNumberType('int', 0)
	public static val NUMBER = new RNumberType('number', 1)

	public static val DATE = new RBuiltinType('date')
	public static val DATE_TIME = new RBuiltinType('dateTime')
	public static val ZONED_DATE_TIME = new RBuiltinType('zonedDateTime')
	public static val TIME = new RBuiltinType('time')

	public static val MISSING = new RBuiltinType('missing')
	public static val VOID = new RBuiltinType('void')
	
	public static val NOTHING = new RBuiltinType('nothing')
	
	val String name
	
	// prevent @Data annotation from overriding `toString`
	override String toString() { super.toString }
}



@Data
class RQualifiedType extends RBuiltinType {
	public static val PRODUCT_TYPE = new RQualifiedType('string', 'productType')
	public static val EVENT_TYPE = new RQualifiedType('string', 'eventType')

	val String qualifiedType
	
	// prevent @Data annotation from overriding `toString`
	override String toString() { super.toString }
}

@Data
class RCalculationType extends RBuiltinType {
	public static val CALCULATION = new RCalculationType('string', 'calculation')
	
	val String calculationType
	
	// prevent @Data annotation from overriding `toString`
	override String toString() { super.toString }
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
	
	// prevent @Data annotation from overriding `toString`
	override String toString() { super.toString }
}

@Data
class RUnionType extends RType {
	val RType from
	val RType to
	val String name

	new(RType from, RType to) {
		this.from = from
		this.to = to
		this.name = from.name + ' or ' + to.name
	}

	override getName() {
		name
	}
	
	def String getToName() {
		if (to instanceof RUnionType) to.toName else to.name
	}
	
	// prevent @Data annotation from overriding `toString`
	override String toString() { super.toString }
}

@Data
class RErrorType extends RType {

	val String message

	override getName() {
		message
	}
	
	// prevent @Data annotation from overriding `toString`
	override String toString() { super.toString }
}
