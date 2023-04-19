package com.regnosys.rosetta.types;

import com.google.inject.Inject;
import com.regnosys.rosetta.rosetta.expression.ModifiableBinaryOperation;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;
import com.regnosys.rosetta.rosetta.RosettaCardinality;

public class TypeValidationUtil {
	@Inject
	TypeSystem typing;
	@Inject
	TypeFactory fac;
	@Inject
	RBuiltinTypeService service;
	
	public String unequalListTypesMessage(RListType expected, RListType actual) {		
		if (!expected.getItemType().equals(actual.getItemType())) {
			if (!expected.getConstraint().constraintEquals(actual.getConstraint()) && !(expected.isPlural() && actual.isPlural())) {
				return new StringBuilder()
						.append("Expected ")
						.append(toCompleteDescription(expected, actual.getItemType()))
						.append(", but got ")
						.append(toCompleteDescription(actual, expected.getItemType()))
						.append(" instead.")
						.toString();
			}
			return notASubtypeMessage(expected.getItemType(), actual.getItemType());
		}
		return notLooserConstraintMessage(expected.getConstraint(), actual);
	}
	public String unequalTypesMessage(RType expected, RType actual) {
		return new StringBuilder()
				.append("Expected type `")
				.append(relevantItemTypeDescription(expected, actual))
				.append("`, but got `")
				.append(relevantItemTypeDescription(actual, expected))
				.append("` instead.")
				.toString();
	}
	public String notAListSubtypeMessage(RListType expected, RListType actual) {
		if (!typing.isSubtypeOf(actual.getItemType(), expected.getItemType())) {
			if (!actual.getConstraint().isSubconstraintOf(expected.getConstraint()) && !(expected.isPlural() && actual.isPlural())) {
				return new StringBuilder()
						.append("Expected ")
						.append(toCompleteDescription(expected, actual.getItemType()))
						.append(", but got ")
						.append(toCompleteDescription(actual, expected.getItemType()))
						.append(" instead.")
						.toString();
			}
			return notASubtypeMessage(expected.getItemType(), actual.getItemType());
		}
		return notLooserConstraintMessage(expected.getConstraint(), actual);
	}
	public String notASubtypeMessage(RType expected, RType actual) {
		return new StringBuilder()
				.append("Expected type `")
				.append(relevantItemTypeDescription(expected, actual))
				.append("`, but got `")
				.append(relevantItemTypeDescription(actual, expected))
				.append("` instead.")
				.toString();
	}
	public String notListComparableMessage(RListType left, RListType right) {
		if (!typing.isComparable(left.getItemType(), right.getItemType())) {
			return notComparableMessage(left.getItemType(), right.getItemType());
		}
		StringBuilder b = new StringBuilder()
				.append("Cannot compare ")
				.append(toConstraintDescription(left.getConstraint()))
				.append(" to ")
				.append(toConstraintDescription(right.getConstraint()))
				.append(", as they cannot be of the same length.");
		if (left.isSingular() || right.isSingular()) {
			b.append(" Perhaps you forgot to write `all` or `any` in front of the operator?");
		}
		return b.toString();
	}
	public String notComparableMessage(RType left, RType right) {
		return new StringBuilder()
				.append("Types `")
				.append(relevantItemTypeDescription(left, right))
				.append("` and `")
				.append(relevantItemTypeDescription(right, left))
				.append("` are not comparable.")
				.toString();
	}
	public String bothAreSingularMessage(ModifiableBinaryOperation op) {
		return new StringBuilder()
				.append("The cardinality operator `")
				.append(op.getCardMod())
				.append("` is redundant when comparing two single values.")
				.toString();
	}
	public String notRightIsSingularButLeftIsMessage(RListType actual) {
		return new StringBuilder()
				.append("Expected ")
				.append(toConstraintDescription(fac.single))
				.append(", but got ")
				.append(toConstraintDescription(actual.getConstraint()))
				.append(" instead. Perhaps you meant to swap the left and right operands?")
				.toString();
	}
	public String notConstraintMessage(RosettaCardinality expected, RListType actual) {
		return new StringBuilder()
				.append("Expected ")
				.append(toConstraintDescription(expected))
				.append(", but got ")
				.append(toConstraintDescription(actual.getConstraint()))
				.append(" instead.")
				.toString();
	}
	public String wrongConstraintMessage(RosettaCardinality wrong, RListType actual) {
		return new StringBuilder()
				.append("May not be ")
				.append(toConstraintDescription(wrong))
				.append(".")
				.toString();
	}
	public String notLooserConstraintMessage(RosettaCardinality expected, RListType actual) {
		return new StringBuilder()
				.append("Expected ")
				.append(toConstraintDescription(expected))
				.append(", but got ")
				.append(toConstraintDescription(actual.getConstraint()))
				.append(" instead.")
				.toString();
	}
	
	public CharSequence relevantItemTypeDescription(RType t, RType context) {
		if (t.getName().equals(context.getName())) {
			return t.toString();
		}
		return t.getName();
	}
	public CharSequence relevantItemTypeDescription(RListType t, RType context) {
		return relevantItemTypeDescription(t.getItemType(), context);
	}
	public CharSequence relevantItemTypeDescription(RListType t, RListType context) {
		return relevantItemTypeDescription(t.getItemType(), context.getItemType());
	}
	public CharSequence toShortDescription(RListType t, RType context) {
		StringBuilder b = new StringBuilder();
		if (t.isEmpty()) {
			if (t.getItemType().equals(service.NOTHING)) {
				return "an empty value";
			}
			b.append("an empty value of type");
		} else if (t.isOptional()) {
			b.append("an optional");
		} else if (t.isSingular()) {
			b.append("a single");
		} else {
			return b.append("a list of `")
					.append(t.getItemType())
					.append("`s")
					.toString();
		}
		return b.append(" `")
				.append(relevantItemTypeDescription(t, context))
				.append("`")
				.toString();
	}
	public CharSequence toConstraintDescription(RosettaCardinality c) {
		if (c.isEmpty()) {
			return "an empty value";
		} else if (c.isOptional()) {
			return "an optional value";
		} else if (c.isSingular()) {
			return "a single value";
		} else {
			StringBuilder b = new StringBuilder();
			if (c.isUnbounded()) {
				if (c.getInf() == 0) {
					b.append("an unbounded list of any length");
				} else {
					b.append("an unbounded list with at least ")
						.append(c.getInf())
						.append(" item")
						.append(pluralS(c.getInf()));
				}
			} else {
				b.append("a list with ");
				if (c.getInf() == c.getSup()) {
					b.append(c.getSup());
				} else {
					b.append(c.getInf())
						.append(" to ")
						.append(c.getSup());
				}
				b.append(" item")
					.append(pluralS(c.getSup()));
			}
			return b.toString();
		}
	}
	public CharSequence toCompleteDescription(RListType t, RType context) {
		if (t.isPlural()) {
			StringBuilder b = new StringBuilder();
			RosettaCardinality c = t.getConstraint();
			if (c.isUnbounded()) {
				b.append("an unbounded list of `")
					.append(relevantItemTypeDescription(t, context))
					.append("`s ");
				if (c.getInf() == 0) {
					b.append("of any length");
				} else {
					b.append("with at least ")
						.append(c.getInf())
						.append(" item")
						.append(pluralS(c.getInf()));
				}
			} else {
				b.append("a list of `")
					.append(relevantItemTypeDescription(t, context))
					.append("`s with ");
				if (c.getInf() == c.getSup()) {
					b.append(c.getSup());
				} else {
					b.append(c.getInf())
						.append(" to ")
						.append(c.getSup());
				}
				b.append(" item")
					.append(pluralS(c.getSup()));
			}
			return b.toString();
		}
		return toShortDescription(t, context);
	}
	private String pluralS(int count) {
		return count == 1 ? "" : "s";
	}
}
