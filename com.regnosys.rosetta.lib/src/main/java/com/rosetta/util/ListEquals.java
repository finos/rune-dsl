package com.rosetta.util;

import java.util.List;

public class ListEquals {
	
	public static boolean listEquals(List<?> a, List<?> b) {
		if ((a==null || a.isEmpty())==(b==null || b.isEmpty())) return true;
		if (a==null) return false;
		return a.equals(b);
	}
}
