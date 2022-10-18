package com.regnosys.rosetta.types

import com.google.inject.Inject
import com.google.inject.Singleton
import java.util.List
import java.util.Map

@Singleton
class RosettaOperators {
	
	public static val ARITHMETIC_OPS = #['+', '-', '*', '/']
	public static val COMPARISON_OPS = #['<', '<=', '>', '>=']
	public static val EQUALITY_OPS = #['=', '<>', 'contains', 'disjoint']
	public static val LOGICAL_OPS = #['and', 'or']
	public static val JOIN_OP = 'join'

	@Inject RosettaTypeCompatibility comaptibility

	val Map<BinaryOperation, RType> binaryTypeMap = newHashMap
	val List<RBuiltinType> builtinTypes = newArrayList

	def RType resultType(String op, RType left, RType right) {
		if (op == JOIN_OP) {
			if (right !== null) {
				return bothString(left, right, op)
			} else if (left != RBuiltinType.STRING) {
				return new RErrorType('''Left hand side of '«op»' expression must be string''')
			} else {
				return RBuiltinType.STRING
			}
		}
		if (LOGICAL_OPS.contains(op)) {
			return bothBoolean(left, right, op)
		}
		if (left instanceof REnumType && right instanceof REnumType &&
			(comaptibility.isUseableAs(left, right) || comaptibility.isUseableAs(right, left))) {
			if (EQUALITY_OPS.contains(op))
				return RBuiltinType.BOOLEAN
		}
		
		initialize()
		var leftToCheck = left
		var rightToCheck = right
		if ((  leftToCheck instanceof RRecordType
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
	
	def private bothBoolean(RType left, RType right, String op) {
		if (left!=RBuiltinType.BOOLEAN)
			return new RErrorType('''Left hand side of '«op»' expression must be boolean''')
		if (right!=RBuiltinType.BOOLEAN)
			return new RErrorType('''Right hand side of '«op»' expression must be boolean''')
		return RBuiltinType.BOOLEAN
	}
	
	def private bothString(RType left, RType right, String op) {
		if (left!=RBuiltinType.STRING)
			return new RErrorType('''Left hand side of '«op»' expression must be string''')
		if (right!=RBuiltinType.STRING)
			return new RErrorType('''Right hand side of '«op»' expression must be string''')
		return RBuiltinType.STRING
	}
	
	def RBuiltinType convertToBuiltIn(RType type) {
		if (type instanceof RBuiltinType) return type
		val tname = type.name
		builtinTypes.findFirst[name==tname]
	}
	
	def isSelfComparable(RType type) {
		return binaryTypeMap.containsKey(new BinaryOperation(COMPARISON_OPS.get(0), type.convertToBuiltIn, type.convertToBuiltIn));
	}

	@Inject
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
			operation(string, string) => bool
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
