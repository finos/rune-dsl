package com.regnosys.rosetta.types

import com.regnosys.rosetta.typing.RosettaTyping
import com.google.inject.Inject
import com.regnosys.rosetta.rosetta.RosettaCardinality

class TypeValidationUtil {
	@Inject
	extension RosettaTyping
	
	def String unequalListTypesMessage(RListType expected, RListType actual) {
		if (expected.itemType == actual.itemType || !expected.constraint.constraintEquals(actual.constraint) && !(expected.isPlural && actual.isPlural)) {
			'''Expected «expected.toCompleteDescription», but got «actual.toCompleteDescription» instead.'''
		} else {
			unequalTypesMessage(expected.itemType, actual.itemType)
		}
	}
	def String unequalTypesMessage(RType expected, RType actual) {
		'''Expected type `«expected»`, but got `«actual»` instead.'''
	}
	def String notAListSubtypeMessage(RListType expected, RListType actual) {
		if (actual.itemType.subtype(expected.itemType).value || !actual.constraint.isSubconstraintOf(expected.constraint) && !(expected.isPlural && actual.isPlural)) {
			'''Expected «expected.toCompleteDescription», but got «actual.toCompleteDescription» instead.'''
		} else {
			notASubtypeMessage(expected.itemType, actual.itemType)
		}
	}
	def String notASubtypeMessage(RType expected, RType actual) {
		'''Expected type `«expected»`, but got `«actual»` instead.'''
	}
	def String notListComparableMessage(RListType left, RListType right) {
		if (!left.itemType.comparable(right.itemType)) {
			notComparableMessage(left.itemType, right.itemType)
		} else {
			if (left.isSingular || right.isSingular) {
				'''Cannot compare «left.toCompleteDescription» to «right.toCompleteDescription», as they cannot be of the same length. Perhaps you forgot to write `all` or `any` in front of the operator?'''
			} else {
				'''Cannot compare «left.toCompleteDescription» to «right.toCompleteDescription», as they cannot be of the same length.'''
			}
		}
	}
	def String notComparableMessage(RType left, RType right) {
		'''Types `«left»` and `«right»` are not comparable.'''
	}
	def String notConstraintMessage(RosettaCardinality expected, RListType actual) {
		'''Expected «expected.toConstraintDescription», but got «actual.constraint.toConstraintDescription» instead.'''
	}
	def String wrongConstraintMessage(RosettaCardinality wrong, RListType actual) {
		'''May not be «wrong.toConstraintDescription».'''
	}
	def String notLooserConstraintMessage(RosettaCardinality expected, RListType actual) {
		'''Expected «expected.toConstraintDescription», but got «actual.constraint.toConstraintDescription» instead.'''
	}
	
	def CharSequence toShortDescription(RListType t) {
		if (t.isEmpty) {
			if (t.itemType == RBuiltinType.NOTHING) {
				'''an empty value'''
			} else {
				'''an empty value of type `«t.itemType»`'''
			}
		} else if (t.isOptional) {
			'''an optional `«t.itemType»`'''
		} else if (t.isSingular) {
			'''a single `«t.itemType»`'''
		} else {
			'''a list of `«t.itemType»`s'''
		}
	}
	def CharSequence toConstraintDescription(RosettaCardinality c) {
		if (c.isEmpty) {
			'''an empty value'''
		} else if (c.isOptional) {
			'''an optional value'''
		} else if (c.isSingular) {
			'''a single value'''
		} else {
			if (c.unbounded) {
				if (c.inf === 0) {
					'''an unbounded list of any length'''
				} else {
					'''an unbounded list with at least «c.inf» item«pluralS(c.inf)»'''
				}
			} else {
				if (c.inf === c.sup) {
					'''a list with «c.sup» item«pluralS(c.sup)»'''
				} else {
					'''a list with «c.inf» to «c.sup» item«pluralS(c.sup)»'''
				}
			}
		}
	}
	def CharSequence toCompleteDescription(RListType t) {
		if (t.isPlural) {
			if (t.constraint.unbounded) {
				if (t.constraint.inf === 0) {
					'''an unbounded list of `«t.itemType»`s of any length'''
				} else {
					'''an unbounded list of `«t.itemType»`s with at least «t.constraint.inf» item«pluralS(t.constraint.inf)»'''
				}
			} else {
				if (t.constraint.inf === t.constraint.sup) {
					'''a list of `«t.itemType»`s with «t.constraint.sup» item«pluralS(t.constraint.sup)»'''
				} else {
					'''a list of `«t.itemType»`s with «t.constraint.inf» to «t.constraint.sup» item«pluralS(t.constraint.sup)»'''
				}
			}
		} else {
			t.toShortDescription
		}
	}
	private def String pluralS(int count) {
		count === 1 ? '' : 's'
	}
}