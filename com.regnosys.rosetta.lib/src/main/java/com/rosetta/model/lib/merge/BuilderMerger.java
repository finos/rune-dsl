package com.rosetta.model.lib.merge;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.rosetta.model.lib.RosettaModelObjectBuilder;

public interface BuilderMerger {
	
	<B extends RosettaModelObjectBuilder> void mergeRosetta(B left, B right, Supplier<B> getOrCreate, Consumer<B> setter);

	<B extends RosettaModelObjectBuilder> void mergeRosetta(List<B> left, List<B> right, Function<Integer, B> getOrCreate, Consumer<B> add);
	
	<T> void mergeBasic(T left, T right, Consumer<T> setter);
}