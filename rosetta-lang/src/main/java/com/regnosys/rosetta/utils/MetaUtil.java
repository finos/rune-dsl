package com.regnosys.rosetta.utils;

import java.util.HashSet;
import java.util.List;

import com.google.common.collect.Sets;
import com.regnosys.rosetta.types.RMetaAttribute;
import com.regnosys.rosetta.types.RType;

public class MetaUtil {

	public static List<RMetaAttribute> intersectMeta(RType t1, RType t2) {
		return Sets
				.intersection(new HashSet<>(t1.getMetaAttributes()), new HashSet<>(t2.getMetaAttributes()))
				.immutableCopy()
				.asList();
	}
}
