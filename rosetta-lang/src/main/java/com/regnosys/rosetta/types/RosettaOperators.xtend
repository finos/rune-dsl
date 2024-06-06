package com.regnosys.rosetta.types

import com.google.inject.Singleton
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService
import com.regnosys.rosetta.types.builtin.RStringType
import java.util.Optional
import com.regnosys.rosetta.types.builtin.RNumberType
import com.regnosys.rosetta.utils.OptionalUtil
import javax.inject.Inject

@Singleton
class RosettaOperators {
	
	public static val ARITHMETIC_OPS = #['+', '-', '*', '/']
	public static val COMPARISON_OPS = #['<', '<=', '>', '>=']
	public static val EQUALITY_OPS = #['=', '<>', 'contains', 'disjoint']
	public static val LOGICAL_OPS = #['and', 'or']
	public static val JOIN_OP = 'join'
	public static val DEFAULT_OP = 'default'

	@Inject extension RBuiltinTypeService service
	@Inject extension TypeSystem

	def RType resultType(String op, RType left, RType right) {
		val resultType = if (op == '+') {
			if (left.isSubtypeOf(DATE) && right.isSubtypeOf(TIME)) {
				DATE_TIME
			} else if (left.isSubtypeOf(UNCONSTRAINED_STRING) && right.isSubtypeOf(UNCONSTRAINED_STRING)) {
				keepTypeAliasIfPossible(left, right, [l,r|
					val s1 = l as RStringType
					val s2 = r as RStringType
					val newInterval = s1.interval.add(s2.interval)
					new RStringType(newInterval, Optional.empty())
				])
			} else if (left.isSubtypeOf(UNCONSTRAINED_NUMBER) && right.isSubtypeOf(UNCONSTRAINED_NUMBER)) {
				keepTypeAliasIfPossible(left, right, [l,r|
					val n1 = l as RNumberType
					val n2 = r as RNumberType
					val newFractionalDigits = OptionalUtil.zipWith(n1.fractionalDigits, n2.fractionalDigits, [a,b|Math.max(a,b)])
					val newInterval = n1.interval.add(n2.interval)
					new RNumberType(Optional.empty(), newFractionalDigits, newInterval, Optional.empty())
				])
			}
		} else if (op == '-') {
			if (left.isSubtypeOf(DATE) && right.isSubtypeOf(DATE)) {
				UNCONSTRAINED_INT
			} else if (left.isSubtypeOf(UNCONSTRAINED_NUMBER) && right.isSubtypeOf(UNCONSTRAINED_NUMBER)) {
				keepTypeAliasIfPossible(left, right, [l,r|
					val n1 = l as RNumberType
					val n2 = r as RNumberType
					val newFractionalDigits = OptionalUtil.zipWith(n1.fractionalDigits, n2.fractionalDigits, [a,b|Math.max(a,b)])
					val newInterval = n1.interval.subtract(n2.interval)
					new RNumberType(Optional.empty(), newFractionalDigits, newInterval, Optional.empty())
				])
			}
		} else if (op == '*') {
			if (left.isSubtypeOf(UNCONSTRAINED_NUMBER) && right.isSubtypeOf(UNCONSTRAINED_NUMBER)) {
				keepTypeAliasIfPossible(left, right, [l,r|
					val n1 = l as RNumberType
					val n2 = r as RNumberType
					val newFractionalDigits = OptionalUtil.zipWith(n1.fractionalDigits, n2.fractionalDigits, [a,b|a+b])
					val newInterval = n1.interval.multiply(n2.interval)
					new RNumberType(Optional.empty(), newFractionalDigits, newInterval, Optional.empty())
				])
			}
		} else if (op == '/') {
			if (left.isSubtypeOf(UNCONSTRAINED_NUMBER) && right.isSubtypeOf(UNCONSTRAINED_NUMBER)) {
				UNCONSTRAINED_NUMBER
			}
		} else if (COMPARISON_OPS.contains(op) || EQUALITY_OPS.contains(op)) {
			if (left === null || right === null || left.isComparable(right)) {
				BOOLEAN
			}
		} else if (op == JOIN_OP) {
			return bothString(left, right, op)
		} else if (LOGICAL_OPS.contains(op)) {
			return bothBoolean(left, right, op)
		} else if (op == DEFAULT_OP) {
			val result = left.join(right)
			if (result != ANY) {
				result
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
		if (!left.isSubtypeOf(BOOLEAN))
			return new RErrorType('''Left hand side of '«op»' expression must be boolean''')
		if (!right.isSubtypeOf(BOOLEAN))
			return new RErrorType('''Right hand side of '«op»' expression must be boolean''')
		return BOOLEAN
	}
	
	def private bothString(RType left, RType right, String op) {
		if (!left.isSubtypeOf(UNCONSTRAINED_STRING))
			return new RErrorType('''Left hand side of '«op»' expression must be string''')
		if (!right.isSubtypeOf(UNCONSTRAINED_STRING))
			return new RErrorType('''Right hand side of '«op»' expression must be string''')
		return UNCONSTRAINED_STRING
	}
}
