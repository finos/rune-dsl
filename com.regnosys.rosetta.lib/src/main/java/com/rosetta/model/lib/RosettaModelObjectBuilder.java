package com.rosetta.model.lib;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

import com.rosetta.model.lib.merge.BuilderMerger;
import com.rosetta.model.lib.meta.RosettaMetaData;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.process.AttributeMeta;
import com.rosetta.model.lib.process.BuilderProcessor;

/**
 * @author TomForwood
 *
 * @param <T>
 */
public abstract class RosettaModelObjectBuilder {
	public abstract RosettaModelObject build();
	
	/**
	 * Recursively removes object that have no field set from the object tree
	 * i.e. A {
	 * 			b->B{c=null}}
	 * will get reduced to 
	 * A {b=null}
	 */
	public abstract <B extends RosettaModelObjectBuilder> B prune();
	
	/**
	 * @return true if any of the primitive fields on this builder are set or if and of the builder attributes have data
	 */
	public abstract boolean hasData();
	
	/**
	 * @return The MetaData {@link RosettaMetaData} object for this class providing access to things like validation
	 */
	public abstract RosettaMetaData<? extends RosettaModelObject> metaData();
	
	/**
	 * Recursively runs the  processors for all RosettaClasses 
	 * @param processors
	 */
	public abstract void process(RosettaPath path, BuilderProcessor processor);
	
	protected <A> A getIndex(List<A> list, int index, Supplier<A> supplier) {
		if (list.size()>index) {//this item already exists - return it
			A a = list.get(index);
			if (a==null) {//if there was null at this index before then create a new item
				a = supplier.get();
				list.set(index, a);
			}
			return a;
		}
		if (index==-1 || index==Integer.MAX_VALUE) {//either of these values are code for just give me the next index
			index = list.size();
		}
		
		//the size of the list is less than required index - create a new item and pad with null's as necessary
		for (int i=list.size();i<index;i++) {
			list.add(null);//pad with nulls
		}
		//now create a new item
		A item = supplier.get();
		list.add(item);
		return item;
		
	}
	
	protected <R extends RosettaModelObject> void processRosetta(RosettaPath path, BuilderProcessor processor, 
			Class<R> clazz, RosettaModelObjectBuilder child, AttributeMeta... metas) {
		boolean processFurther = processor.processRosetta(path, clazz, child, this, metas);
		if (child!=null && processFurther) child.process(path, processor);
	}
	protected <R extends RosettaModelObject> void processRosetta(RosettaPath path, BuilderProcessor processor, 
			Class<R> clazz, List<? extends RosettaModelObjectBuilder> children, AttributeMeta... metas) {
		processor.processRosetta(path, clazz, children, this, metas);
		if (children!=null)  {
			int index=0;
			// Iterate through a copy of children to prevent a fail-fast ConcurrentModificationException if a mapping processor modifies the children.
			List<? extends RosettaModelObjectBuilder> copy = new ArrayList<>(children);
			for (Iterator<? extends RosettaModelObjectBuilder> iterator = copy.iterator(); iterator.hasNext();) {
				RosettaModelObjectBuilder child = iterator.next();
				if (child!=null) {
					RosettaPath indexedPath = path.withIndex(index);
					child.process(indexedPath, processor);
					index++;
				}
			}
		}
	}
	
	public abstract <B extends RosettaModelObjectBuilder> B merge(B other, BuilderMerger merger);
}