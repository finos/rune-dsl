package com.rosetta.model.lib.process;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.rosetta.model.lib.RosettaModelObjectBuilder;

/**
 * Defines a processor that merges two RosettaModelObjectBuilder instances into o1 single instance.
 * 
 * Implementations of this interface are designed to be used with the RosettaModelObjectBuilder merge method to 
 * perform visitor pattern processing on the object, updating the underlying object as it traverses the tree.
 * 
 * Typically used to merge or un-merge an object with a "template".
 * 
 * @see RosettaModelObjectBuilder
 */
public interface BuilderMerger {
	
	/**
	 * Start merging the two RosettaModelObjectBuilder objects.
	 * 
	 * @param o1 - a RosettaModelObjectBuilder object, which o2 will be merged into.
	 * @param o2 - a RosettaModelObjectBuilder object, which will be merged into o1, and then discarded.  Typically o2 will be a "template" that will be merged into o1.
	 */
	<T extends RosettaModelObjectBuilder> void run(T o1, T o2);
	
	/**
	 * Merge the two RosettaModelObjectBuilder objects.
	 * 
	 * @param o1 - a RosettaModelObjectBuilder object, which o2 will be merged into.
	 * @param o2 - a RosettaModelObjectBuilder object, which will be merged into o1, and then discarded.
	 * @param o1Setter - method to overwrite object o1, e.g. lambda to the set method on the o1's parent object.
	 */
	<T extends RosettaModelObjectBuilder> void mergeRosetta(T o1, T o2, Consumer<T> o1Setter);

	/**
	 * Merge the two RosettaModelObjectBuilder object lists.
	 * 
	 * @param o1 - a list of RosettaModelObjectBuilder objects, which o2 will be merged into.
	 * @param o2 - a list of RosettaModelObjectBuilder objects, which will be merged into o1, and then discarded.
	 * @param o1GetOrCreateByIndex - method to get object at list index, or if absent create object at list index.
	 */
	<T extends RosettaModelObjectBuilder> void mergeRosetta(List<? extends T> o1, List<? extends T> o2, Function<Integer, T> o1GetOrCreateByIndex);
	
	/**
	 * Merge the two primitive types.
	 * 
	 * @param o1 - a primitive types, which o2 will be merged into.
	 * @param o2 - a primitive types, which will be merged into o1, and then discarded.
	 * @param o1Setter - method to overwrite object o1, e.g. lambda to the set method on the o1's parent object.
	 * @param metas - flags indicating meta information about the attribute, e.g. GLOBAL_KEY, EXTERNAL_KEY etc.
	 */
	<T> void mergeBasic(T o1, T o2, Consumer<T> o1Setter, AttributeMeta... metas);
	
	/**
	 * Merge the two lists of primitive types.
	 * 
	 * @param o1 - a primitive types, which o2 will be merged into.
	 * @param o2 - a primitive types, which will be merged into o1, and then discarded.
	 * @param o1Add - method to append an item to list o1, e.g. lambda to the add method on the o1's parent object.
	 */
	<T> void mergeBasic(List<? extends T> o1, List<? extends T> o2, Consumer<T> o1Add);
}