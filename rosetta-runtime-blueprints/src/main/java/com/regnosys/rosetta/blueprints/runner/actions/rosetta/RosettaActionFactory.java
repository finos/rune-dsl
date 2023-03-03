package com.regnosys.rosetta.blueprints.runner.actions.rosetta;

import java.util.function.Function;
import java.util.function.Supplier;

import com.regnosys.rosetta.blueprints.runner.data.DataIdentifier;
import com.regnosys.rosetta.blueprints.runner.nodes.ExpanderNode;
import com.regnosys.rosetta.blueprints.runner.nodes.ProcessorNode;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.mapper.Mapper;

public interface RosettaActionFactory {
	
	<I extends RosettaModelObject, O, K> ProcessorNode<I, O, K> newRosettaSingleMapper(String uri, String label, DataIdentifier id, Function<I, Mapper<? extends O>> function);
	
	<I extends RosettaModelObject, O, K> ExpanderNode<I, O, K> newRosettaMultipleMapper(String uri, String label, DataIdentifier id, Function<I, Mapper<? extends O>> function);

	<I extends RosettaModelObject, O, K> ExpanderNode<I, O, K> newRosettaRepeatableMapper(String uri, String label, DataIdentifier id, Function<I, Mapper<? extends O>> function);

	<I, O, K> ProcessorNode<I, O, K> newRosettaReturn(String uri, String label, DataIdentifier id, Supplier<Mapper<O>> function);
	
	<I, O, K> ProcessorNode<I, O, K> newRosettaLookup(String uri, String label, DataIdentifier id, String lookupName);

	<I extends RosettaModelObject, O> void registerLookup(String lookupName, LookupFunction<I, O> function);

	interface LookupFunction<I, O> extends Function<I, Mapper<O>> {
	}
}
