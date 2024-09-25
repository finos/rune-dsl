package com.regnosys.rosetta.types;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import com.regnosys.rosetta.interpreter.RosettaValue;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;
import com.regnosys.rosetta.types.builtin.RNumberType;
import com.regnosys.rosetta.types.builtin.RStringType;

import static com.regnosys.rosetta.utils.MetaUtil.*;

public class SubtypeRelation {
	@Inject 
	private RBuiltinTypeService builtins;
	
	public boolean isSubtypeOf(RMetaAnnotatedType t1, RMetaAnnotatedType t2) {
		RType t1RType = t1.getRType();
		RType t2RType = t2.getRType();
		if ((t1.hasMeta() || t2.hasMeta()) && (t1RType.equals(t2RType))) {
			return hasSupersetOfMetaAttributes(t1, t2);
		}
		return isSubtypeOf(t1RType, t2RType);
	}
	
	
	public boolean isSubtypeOf(RType t1, RType t2) {
		if (t1.equals(t2)) {
			return true;
		} else if (t1.equals(builtins.NOTHING) || t2.equals(builtins.ANY)) {
			return true;
		} else if (t1 instanceof RNumberType && t2 instanceof RNumberType) {
			return true;
		} else if (t1 instanceof RStringType && t2 instanceof RStringType) {
			return true;
		} else if (t1 instanceof RDataType) {
			RType st = ((RDataType)t1).getSuperType();
			if (st == null) {
				return false;
			}
			return isSubtypeOf(st, t2);
		} else if (t1 instanceof RAliasType) {
			return isSubtypeOf(((RAliasType)t1).getRefersTo(), t2);
		} else if (t2 instanceof RAliasType) {
			return isSubtypeOf(t1, ((RAliasType)t2).getRefersTo());
		}
		return false;
	}
	
	public RMetaAnnotatedType join(RMetaAnnotatedType t1, RMetaAnnotatedType t2) {
		RType t1RType = t1.getRType();
		RType t2RType = t2.getRType();
		if (t1RType.equals(t2RType)) {
			return new RMetaAnnotatedType(t1RType, intersectMeta(t1, t2));
		}
		return new RMetaAnnotatedType(join(t1RType, t2RType), List.of());
	}
	
	public RType join(RType t1, RType t2) {
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
			return aliasParams.<RType>map(p -> new RAliasType(typeFunc, p, underlyingJoin))
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
}
