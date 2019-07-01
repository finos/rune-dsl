package com.regnosys.rosetta.blueprints.runner.nodes;

import com.regnosys.rosetta.blueprints.runner.data.GroupableData;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import org.apache.log4j.Logger;

public interface ProcessorNode<I,O, K extends Comparable<K>> extends Node<I, O, K, K>{
    Logger LOGGER = Logger.getLogger(ProcessorNode.class);
    
	<T extends I> Optional<GroupableData<O, K>> process(GroupableData<T, K> input);
	default Collection<GroupableData<? extends O, K>> terminate() {
	    LOGGER.trace("calling teminate on "+getName()+" instance of "+this.getClass());
		return Collections.emptyList();
	}
	String getName();
	String getURI();
}
