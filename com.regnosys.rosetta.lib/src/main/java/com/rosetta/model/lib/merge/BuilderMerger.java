package com.rosetta.model.lib.merge;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.rosetta.model.lib.RosettaModelObjectBuilder;

public interface BuilderMerger {
	
	<B extends RosettaModelObjectBuilder> B merge(B left, B right);
	
	<B extends RosettaModelObjectBuilder> void mergeRosetta(B left, B right, Consumer<B> setter);

	<B extends RosettaModelObjectBuilder> void mergeRosetta(List<B> left, List<B> right, Function<Integer, B> getOrCreate, Consumer<B> add);
	
	<T> void mergeBasic(T left, T right, Consumer<T> setter);
	
	<T> void mergeBasic(List<T> left, List<T> right, Consumer<T> add);
}