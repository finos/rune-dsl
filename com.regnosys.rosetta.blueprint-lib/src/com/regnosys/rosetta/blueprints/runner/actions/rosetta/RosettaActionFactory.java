package com.regnosys.rosetta.blueprints.runner.actions.rosetta;

import java.util.function.Function;
import java.util.function.Supplier;

import com.regnosys.rosetta.blueprints.runner.data.DataIdentifier;
import com.regnosys.rosetta.blueprints.runner.nodes.DataJoinNode;
import com.regnosys.rosetta.blueprints.runner.nodes.ExpanderNode;
import com.regnosys.rosetta.blueprints.runner.nodes.GroupNode;
import com.regnosys.rosetta.blueprints.runner.nodes.ProcessorNode;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.mapper.Mapper;
import com.rosetta.model.lib.mapper.MappingGroup;

public interface RosettaActionFactory {

	<R extends RosettaModelObject, K> ProcessorNode<String, R, K> newRosettaIngester(String uri, String label, DataIdentifier id, Class<R> modelClass);
	
	<I extends RosettaModelObject, O, K > ProcessorNode<I, O, K> newRosettaSimpleMapper(String uri, String label, DataIdentifier id, MappingGroup<I, O> mappings);
	
	<I extends RosettaModelObject, O, K> ProcessorNode<I, O, K> newRosettaSingleMapper(String uri, String label, DataIdentifier id, Function<I, Mapper<O>> function);
	
	<I extends RosettaModelObject, O, K> ExpanderNode<I, O, K> newRosettaMultipleMapper(String uri, String label, DataIdentifier id, Function<I, Mapper<O>> function);

	<I extends RosettaModelObject, K> ProcessorNode<I, I, K> newRosettaValidator(String uri, String label, DataIdentifier id, Class<I> ingestionType);
	
	<I extends RosettaModelObject, K1, K2> GroupNode<I, K1, K2> newRosettaGrouper(String uri, String label, DataIdentifier id, Function<I, Mapper<K2>> function);
	
	<I1 extends RosettaModelObject, I2 extends RosettaModelObject, K, FK> DataJoinNode<I1, I2, K> newRosettaDataJoin(String uri, String label, DataIdentifier id, Function<I1, Mapper<FK>> keyFunc, Function<I2, Mapper<FK>> foreignFunc, Class<I1> class1, Class<I2> class2);

	<I extends RosettaModelObject, O, K> ProcessorNode<I, O, K> newRosettaCalculationMapper(String uri, String label, DataIdentifier id, MappingGroup<I, O> mappings);

	<I, O, K> ProcessorNode<I, O, K> newRosettaReturn(String uri, String label, DataIdentifier id, Supplier<Mapper<O>> function);
	
	<I, O, K> ProcessorNode<I, O, K> newRosettaLookup(String uri, String label, DataIdentifier id, String lookupName);

	<I extends RosettaModelObject, O> void registerLookup(String lookupName, LookupFunction<I, O> function);

	interface LookupFunction<I, O> extends Function<I, Mapper<O>> {
	}
}
