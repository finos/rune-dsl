package com.regnosys.rosetta.types;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.inject.Inject;

import com.google.common.collect.Sets;
import com.regnosys.rosetta.interpreter.RosettaValue;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;
import com.regnosys.rosetta.types.builtin.RNumberType;
import com.regnosys.rosetta.types.builtin.RStringType;

import com.regnosys.rosetta.rosetta.simple.Condition;

/**
 * An implementation of Rune's subtype relation. This class
 * allows you to check whether one type is a subtype of the other,
 * and also allows you to compute the least common supertype of two
 * types - also called the "join".
 * 
 * Subtyping rules:
 * 1. [Identity]    A type is a subtype of itself.
 * 2. [Bottom type] The builtin type `nothing` is a subtype of everything.
 * 3. [Top type]    Everything is a subtype of the builtin type `any`.
 * 4. [Number]      A `number` type is a subtype of any other `number` type - no matter the type parameters.
 * 5. [String]      A `string` type is a subtype of any other `string` type - no matter the type parameters.
 * 6. [Data]        A data type `S` is a subtype of a type `T` if `S` extends a type which is a subtype of `T`.
 * 7. [Choice]      Choice types are treated as data wrapper types.
 * 8. [Alias]       Aliases are treated as the type they refer to. They are effectively ignored.
 */
public class SubtypeRelation {
	@Inject 
	private RBuiltinTypeService builtins;
	
	public boolean isSubtypeOf(RMetaAnnotatedType t1, RMetaAnnotatedType t2, boolean treatChoiceTypesAsDataTypes) {
		if (t1.equals(t2)) {
			return true;
		}
		return isSubtypeOf(t1.getRType(), t2.getRType(), treatChoiceTypesAsDataTypes);
	}
	
	public boolean isSubtypeOf(RType t1, RType t2, boolean treatChoiceTypesAsDataTypes) {
		if (t1.equals(t2)) {
			return true;
		}
		return isSubtypeOf(t1, t2, treatChoiceTypesAsDataTypes, null);
	}
	public boolean isSubtypeOf(RType t1, RType t2, boolean treatChoiceTypesAsDataTypes, Stack<RType> visited) {
		if (treatChoiceTypesAsDataTypes) {
			if (t1 instanceof RChoiceType) {
				t1 = ((RChoiceType) t1).asRDataType();
			}
			if (t2 instanceof RChoiceType) {
				t2 = ((RChoiceType) t2).asRDataType();
			}
		}
		
		if (t1.equals(t2)) {
			return true;
		} else if (t1.equals(builtins.NOTHING) || t2.equals(builtins.ANY)) {
			return true;
		} else if (t1 instanceof RNumberType && t2 instanceof RNumberType) {
			return true;
		} else if (t1 instanceof RStringType && t2 instanceof RStringType) {
			return true;
		} else if (t1 instanceof RChoiceType) {
			RType t1_ = t1;
			RType t2_ = t2;
			return ((RChoiceType)t1).getOwnOptions().stream().allMatch(t -> safeIsSubtypeOf(t.getType().getRType(), t2_, false, t1_, visited));
		} else if (t2 instanceof RChoiceType) {
			RType t1_ = t1;
			RType t2_ = t2;
			return ((RChoiceType)t2).getOwnOptions().stream().anyMatch(t -> safeIsSubtypeOf(t1_, t.getType().getRType(), false, t2_, visited));
		} else if (t1 instanceof RDataType) {
			RType st = ((RDataType)t1).getSuperType();
			if (st == null) {
				return false;
			}
			return safeIsSubtypeOf(st, t2, treatChoiceTypesAsDataTypes, t1, visited);
		} else if (t1 instanceof RAliasType) {
			return safeIsSubtypeOf(((RAliasType)t1).getRefersTo(), t2, treatChoiceTypesAsDataTypes, t1, visited);
		} else if (t2 instanceof RAliasType) {
			return safeIsSubtypeOf(t1, ((RAliasType)t2).getRefersTo(), treatChoiceTypesAsDataTypes, t2, visited);
		}
		return false;
	}
	private boolean safeIsSubtypeOf(RType t1, RType t2, boolean treatChoiceTypesAsDataTypes, RType currentlyVisited, Stack<RType> visited) {
		if (visited == null) {
			visited = new Stack<>();
		}
		if (visited.contains(currentlyVisited)) {
			// If the type is already visited, return true.
			return true;
		}
		visited.add(currentlyVisited);
		boolean result = isSubtypeOf(t1, t2, treatChoiceTypesAsDataTypes, visited);
		visited.pop();
		return result;
	}
	
	public RMetaAnnotatedType join(RMetaAnnotatedType t1, RMetaAnnotatedType t2) {
	    if (t1.equals(builtins.NOTHING_WITH_ANY_META)) {
	        return t2;
	    }
	    if (t2.equals(builtins.NOTHING_WITH_ANY_META)) {
	        return t1;
	    }
	    
		RType t1RType = t1.getRType();
		RType t2RType = t2.getRType();
		if (t1RType.equals(t2RType)) {
			return RMetaAnnotatedType.withMeta(t1RType, intersectMeta(t1, t2));
		}
		return RMetaAnnotatedType.withNoMeta(join(t1RType, t2RType));
	}
	
	public RType join(RType t1, RType t2) {
		if (t1 instanceof RChoiceType) {
			t1 = ((RChoiceType) t1).asRDataType();
		}
		if (t2 instanceof RChoiceType) {
			t2 = ((RChoiceType) t2).asRDataType();
		}
		
		if (t1.equals(t2) || t2.equals(builtins.NOTHING)) {
			return t1;
		} else if (t1.equals(builtins.NOTHING)) {
			return t2;
		} else if (t1 instanceof RNumberType && t2 instanceof RNumberType) {
			return join((RNumberType)t1, (RNumberType)t2);
		} else if (t1 instanceof RStringType && t2 instanceof RStringType) {
			return join((RStringType)t1, (RStringType)t2);
		} else if (t1 instanceof RDataType && t2 instanceof RDataType) {
			return join((RDataType)t1, (RDataType)t2);
		} else if (t1 instanceof RAliasType && t2 instanceof RAliasType) {
			return join((RAliasType)t1, (RAliasType)t2);
		} else if (t1 instanceof RAliasType) {
			return join(((RAliasType)t1).getRefersTo(), t2);
		} else if (t2 instanceof RAliasType) {
			return join(t1, ((RAliasType)t2).getRefersTo());
		}
		return builtins.ANY;
	}
	
	public RNumberType join(RNumberType t1, RNumberType t2) {
		return t1.join(t2);
	}
	public RStringType join(RStringType t1, RStringType t2) {
		return t1.join(t2);
	}
	public RType join(RDataType t1, RDataType t2) {
		if (t1.equals(t2)) {
			return t1;
		} else {
			return joinByTraversingAncestorsAndAliases(t1, t2);
		}
	}
	public RType join(RAliasType t1, RAliasType t2) {
		if (t1.equals(t2)) {
			return t1;
		} else if (t1.getTypeFunction().equals(t2.getTypeFunction())) {
			// Attempt to keep the alias
			RTypeFunction typeFunc = t1.getTypeFunction();
			RType underlyingJoin = join(t1.getRefersTo(), t2.getRefersTo());
			Optional<LinkedHashMap<String, RosettaValue>> aliasParams = typeFunc.reverse(underlyingJoin);
			//Condition intersection should be considered in the long term. Picked one set of conditions since are tightly coupled with typeFunctions
			return aliasParams.<RType>map(p -> new RAliasType(typeFunc, p, underlyingJoin, t1.getConditions()))
				.orElse(underlyingJoin);
		} else {
			return joinByTraversingAncestorsAndAliases(t1, t2);
		}
	}
	private RType joinByTraversingAncestorsAndAliases(RType t1, RType t2) {
		// Get a list of all Data/Alias ancestors of t1, including t1 itself.
		List<RDataType> dataAncestors = new ArrayList<>();
		List<RAliasType> aliasAncestors = new ArrayList<>();
		RType curr1 = t1;
		while (true) {
			if (curr1 instanceof RDataType) {
				RDataType currData = (RDataType) curr1;
				dataAncestors.add(currData);
				curr1 = currData.getSuperType();
			} else if (curr1 instanceof RAliasType) {
				RAliasType currAlias = (RAliasType) curr1;
				aliasAncestors.add(currAlias);
				curr1 = currAlias.getRefersTo();
			} else {
				break;
			}
		}
		// For each ancestor of t2, see if there is a matching Data/Alias type in t1's ancestors.
		// If there is, join those types. If not, join their top (basic) types, if they extend one.
		RType curr2 = t2;
		while (true) {
			if (curr2 instanceof RDataType) {
				RDataType currData = (RDataType) curr2;
				if (dataAncestors.contains(currData)) {
					return curr2;
				}
				curr2 = currData.getSuperType();
			} else if (curr2 instanceof RAliasType) {
				RAliasType currAlias = (RAliasType) curr2;
				RTypeFunction tf = currAlias.getTypeFunction();
				RAliasType match = aliasAncestors.stream().filter(a -> tf.equals(a.getTypeFunction())).findFirst().orElse(null);
				if (match != null) {
					return join(match, currAlias);
				}
				curr2 = currAlias.getRefersTo();
			} else {
				break;
			}
		}
		if (curr1 == null || curr2 == null) {
			return builtins.ANY;
		}
		return join(curr1, curr2);
	}
	
	private List<RMetaAttribute> intersectMeta(RMetaAnnotatedType t1, RMetaAnnotatedType t2) {
		return Sets
				.intersection(new HashSet<>(t1.getMetaAttributes()), new HashSet<>(t2.getMetaAttributes()))
				.immutableCopy()
				.asList();
	}
	
}
