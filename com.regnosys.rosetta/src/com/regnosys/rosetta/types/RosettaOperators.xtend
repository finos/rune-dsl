package com.regnosys.rosetta.types

import com.google.inject.Inject
import com.google.inject.Singleton
import java.util.List
import java.util.Map
import org.eclipse.xtend.lib.annotations.Data

@Singleton
class RosettaOperators {
	
	public static val ARITHMETIC_OPS = #['+', '-', '*', '/']
	public static val COMPARISON_OPS = #['<', '<=', '>', '>=']
	public static val EQUALITY_OPS = #['=', '<>']
	public static val LOGICAL_OPS = #['and', 'or']

	@Data
	private static class BinaryOperation {
		String operator
		RBuiltinType left
		RBuiltinType right
	}
	@Inject RosettaTypeCompatibility comaptibility

	val Map<BinaryOperation, RType> binaryTypeMap = newHashMap
	val List<RBuiltinType> builtinTypes = newArrayList

	def RType resultType(String op, RType left, RType right) {
		if (LOGICAL_OPS.contains(op)) {
			if(left instanceof RFeatureCallType) {
				return left
			}
			return commonType(left,right)
		}
		if (left instanceof REnumType && right instanceof REnumType &&
			(comaptibility.isUseableAs(left, right) || comaptibility.isUseableAs(right, left))) {
			if (EQUALITY_OPS.contains(op))
				return RBuiltinType.BOOLEAN
		}
		initialize()
		var leftToCheck = left
		var rightToCheck = right
		if (left instanceof RFeatureCallType) {
			leftToCheck = left.featureType
		}
		if (right instanceof RFeatureCallType) {
			rightToCheck = right.featureType
		}
		if ((  leftToCheck instanceof RRecordType
			|| leftToCheck instanceof RClassType
			|| leftToCheck instanceof RDataType
		) && leftToCheck == rightToCheck) {
			if (EQUALITY_OPS.contains(op))
				return RBuiltinType.BOOLEAN
		}
		if (leftToCheck instanceof RUnionType) {
			leftToCheck = leftToCheck.to
		}
		if (rightToCheck instanceof RUnionType) {
			rightToCheck = rightToCheck.to
		}
		if (leftToCheck instanceof RQualifiedType) {
			val tName=leftToCheck.name 
			leftToCheck = builtinTypes.findFirst[name==tName]
		}
		if (rightToCheck instanceof RQualifiedType) {
			val tName=rightToCheck.name 
			rightToCheck = builtinTypes.findFirst[name==tName]
		}
		val resultingType = binaryTypeMap.get(new BinaryOperation(op, leftToCheck.convertToBuiltIn, rightToCheck.convertToBuiltIn))
		if (resultingType === null) {
			return new RErrorType(
				"Incompatible types: cannot use operator '" + op + "' with " + leftToCheck.name + " and " +
					rightToCheck.name + ".")
		}
		else
			return resultingType
	}
	
	def private commonType(RType left, RType right) {
		if (left == right) {
			return left
		} else if (left instanceof RNumberType && right instanceof RNumberType) {
			return RNumberType.getCommonType(left as RNumberType, right as RNumberType)
		}
		RBuiltinType.BOOLEAN
	}
	
	def RBuiltinType convertToBuiltIn(RType type) {
		if (type instanceof RBuiltinType) return type
		val tname = type.name
		builtinTypes.findFirst[name==tname]
	}

	def private synchronized void initialize() {
		if (!binaryTypeMap.empty) {
			return
		}
		val number = RBuiltinType.NUMBER
		val _int = RBuiltinType.INT
		val string = RBuiltinType.STRING
		val bool = RBuiltinType.BOOLEAN
		val date = RBuiltinType.DATE
		val time = RBuiltinType.TIME
		val dateTime = RBuiltinType.DATE_TIME
		val zonedDateTime = RBuiltinType.ZONED_DATE_TIME

		for (it : ARITHMETIC_OPS) {
			val types = newArrayList
			if (it == '/') {
				operation(_int, _int) => number
			} else {
				operation(_int, _int) => _int
			}
			types += _int
			operation(_int, number) => number
			operation(number, _int) => number
			operation(number, number) => number
		}
		'+'.operation(string, string) => string
		'+'.operation(date, time) => string
		'-'.operation(date, date) => number

		for (it : COMPARISON_OPS) {
			operation(_int, _int) => bool
			operation(_int, number) => bool
			operation(number, _int) => bool
			operation(number, number) => bool
			operation(date, date) => bool
			operation(dateTime, dateTime) => bool
			operation(zonedDateTime, zonedDateTime) => bool
			operation(time, time) => bool
		}
		for (it : EQUALITY_OPS) {
			operation(_int, _int) => bool
			operation(_int, number) => bool
			operation(number, _int) => bool
			operation(number, number) => bool
			operation(bool, bool) => bool
			operation(string, string) => bool
			operation(date, date) => bool
			operation(dateTime, dateTime) => bool
			operation(zonedDateTime, zonedDateTime) => bool
			operation(time, time) => bool
		}
		
		builtinTypes.addAll(_int, number, bool, date, dateTime, zonedDateTime, time, string);
	}

	def private operation(String operator, RBuiltinType left, RBuiltinType right) {
		new BinaryOperation(operator, left, right)
	}

	def private void =>(BinaryOperation operation, RType result) {
		binaryTypeMap.put(operation, result)
	}
}
