package com.rosetta.model.lib.merge;

import java.util.function.Consumer;

import com.rosetta.model.lib.RosettaModelObjectBuilder;

public interface BuilderMerger {
	
	<B extends RosettaModelObjectBuilder> boolean mergeRosetta(B b1, B b2, Consumer<B> setter);

	//<B extends RosettaModelObjectBuilder> boolean mergeRosetta(List<? extends B> b1, List<? extends B> b2, List<? extends B> setter);

	<T> void mergeBasic(T b1, T b2, Consumer<T> setter);
}