package com.regnosys.rosetta.utils;

import java.util.HashSet;
import java.util.List;

import com.google.common.collect.Sets;
import com.regnosys.rosetta.types.RMetaAnnotatedType;
import com.regnosys.rosetta.types.RMetaAttribute;
import com.regnosys.rosetta.types.RType;

public class MetaUtil {

	public static List<RMetaAttribute> intersectMeta(RMetaAnnotatedType t1, RMetaAnnotatedType t2) {
		return Sets
				.intersection(new HashSet<>(t1.getMetaAttributes()), new HashSet<>(t2.getMetaAttributes()))
				.immutableCopy()
				.asList();
	}
	
	public static boolean hasSupersetOfMetaAttributes(RMetaAnnotatedType t1, RMetaAnnotatedType t2) {
		if (t1.getMetaAttributes().isEmpty() && t2.getMetaAttributes().isEmpty()) {
			return true;
		}
		HashSet<RMetaAttribute> t1Metas = new HashSet<>(t1.getMetaAttributes());
		HashSet<RMetaAttribute> t2Metas = new HashSet<>(t2.getMetaAttributes());
		if (t1Metas.equals(t2Metas)) {
			return true;
		}
		if (!Sets.difference(t1Metas, t2Metas).isEmpty()) {
			return true;
		}
		return false;
	}
}
