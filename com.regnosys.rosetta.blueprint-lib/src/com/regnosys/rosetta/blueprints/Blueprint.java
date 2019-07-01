package com.regnosys.rosetta.blueprints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public interface Blueprint<IN, OUT, KIN extends Comparable<KIN>, KOUT extends Comparable<KOUT>> {
    String getName();
    String getURI();
    
	@SafeVarargs
	static public <A> List<A> of(A a, A... aas) {
		List<A> r = new ArrayList<>();
		r.add(a);
		r.addAll(Arrays.asList(aas));
		return r;
	}
	
	static public <A> List<A> of(A a) {
		List<A> r = new ArrayList<>();
		r.add(a);
		return r;
	}
	
	static public <A> List<A> of() {
		return Collections.emptyList();
	}

	BlueprintInstance<IN, OUT, KIN, KOUT> blueprint();
}