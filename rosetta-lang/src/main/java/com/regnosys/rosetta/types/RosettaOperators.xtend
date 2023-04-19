package com.regnosys.rosetta.types

import com.google.inject.Inject
import com.google.inject.Singleton
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService
import com.regnosys.rosetta.types.builtin.RStringType
import com.regnosys.rosetta.types.builtin.RRecordType
import java.util.Optional
import com.regnosys.rosetta.types.builtin.RNumberType
import com.regnosys.rosetta.utils.OptionalUtil

@Singleton
class RosettaOperators {
	
	public static val ARITHMETIC_OPS = #['+', '-', '*', '/']
	public static val COMPARISON_OPS = #['<', '<=', '>', '>=']
	public static val EQUALITY_OPS = #['=', '<>', 'contains', 'disjoint']
	public static val LOGICAL_OPS = #['and', 'or']
	public static val JOIN_OP = 'join'

	@Inject extension RBuiltinTypeService service
	@Inject extension TypeSystem

	def RType resultType(String op, RType left, RType right) {
		if (left == NOTHING || right == NOTHING) {
			return NOTHING
		}
		if (op == JOIN_OP) {
			return bothString(left, right, op)
		}
		if (LOGICAL_OPS.contains(op)) {
			return bothBoolean(left, right, op)
		}
		if (left instanceof REnumType && right instanceof REnumType &&
			(left.isSubtypeOf(right) || right.isSubtypeOf(left))) {
			if (EQUALITY_OPS.contains(op))
				return BOOLEAN
		}
		
		if ((  left instanceof RRecordType
			|| left instanceof RDataType
		) && left == right) {
			if (EQUALITY_OPS.contains(op))
				return BOOLEAN
		}
		
		val resultType = if (op == '+') {
			if (left == DATE && right == TIME) {
				DATE_TIME
			} else if (left instanceof RStringType && right instanceof RStringType) {
				val s1 = left as RStringType
				val s2 = right as RStringType
				val newInterval = s1.interval.add(s2.interval)
				new RStringType(newInterval, Optional.empty())
			} else if (left instanceof RNumberType && right instanceof RNumberType) {
				val n1 = left as RNumberType
				val n2 = right as RNumberType
				val newFractionalDigits = OptionalUtil.zipWith(n1.fractionalDigits, n2.fractionalDigits, [a,b|Math.max(a,b)])
				val newInterval = n1.interval.add(n2.interval)
				new RNumberType(Optional.empty(), newFractionalDigits, newInterval, Optional.empty())
			}
		} else if (op == '-') {
			if (left == DATE && right == DATE) {
				UNCONSTRAINED_INT
			} else if (left instanceof RNumberType && right instanceof RNumberType) {
				val n1 = left as RNumberType
				val n2 = right as RNumberType
				val newFractionalDigits = OptionalUtil.zipWith(n1.fractionalDigits, n2.fractionalDigits, [a,b|Math.max(a,b)])
				val newInterval = n1.interval.subtract(n2.interval)
				new RNumberType(Optional.empty(), newFractionalDigits, newInterval, Optional.empty())
			}
		} else if (op == '*') {
			if (left instanceof RNumberType && right instanceof RNumberType) {
				val n1 = left as RNumberType
				val n2 = right as RNumberType
				val newFractionalDigits = OptionalUtil.zipWith(n1.fractionalDigits, n2.fractionalDigits, [a,b|a+b])
				val newInterval = n1.interval.multiply(n2.interval)
				new RNumberType(Optional.empty(), newFractionalDigits, newInterval, Optional.empty())
			}
		} else if (op == '/') {
			if (left instanceof RNumberType && right instanceof RNumberType) {
				val n1 = left as RNumberType
				val n2 = right as RNumberType
				val newInterval = n1.interval.divide(n2.interval)
				new RNumberType(Optional.empty(), Optional.empty(), newInterval, Optional.empty())
			}
		} else if (COMPARISON_OPS.contains(op) || EQUALITY_OPS.contains(op)) {
			if (left === null || right === null || left.isComparable(right)) {
				BOOLEAN
			}
		}
		
		if (resultType === null) {
			return new RErrorType(
				"Incompatible types: cannot use operator '" + op + "' with " + left.name + " and " +
					right.name + ".")
		}
		else
			return resultType
	}
	
	def private bothBoolean(RType left, RType right, String op) {
		if (left!=BOOLEAN)
			return new RErrorType('''Left hand side of '«op»' expression must be boolean''')
		if (right!=BOOLEAN)
			return new RErrorType('''Right hand side of '«op»' expression must be boolean''')
		return BOOLEAN
	}
	
	def private bothString(RType left, RType right, String op) {
		if (!(left instanceof RStringType))
			return new RErrorType('''Left hand side of '«op»' expression must be string''')
		if (!(right instanceof RStringType))
			return new RErrorType('''Right hand side of '«op»' expression must be string''')
		return UNCONSTRAINED_STRING
	}
}
