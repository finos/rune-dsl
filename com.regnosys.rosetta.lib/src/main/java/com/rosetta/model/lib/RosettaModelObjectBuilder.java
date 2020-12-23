package com.rosetta.model.lib;

import java.util.List;
import java.util.function.Supplier;

import com.rosetta.model.lib.process.BuilderMerger;

/**
 * @author TomForwood
 *
 * @param <T>
 */
public interface RosettaModelObjectBuilder extends RosettaModelObject {
	
	/**
	 * Recursively removes object that have no field set from the object tree
	 * i.e. A {
	 * 			b->B{c=null}}
	 * will get reduced to 
	 * A {b=null}
	 */
	public abstract <B extends RosettaModelObjectBuilder> B prune();

	/**
	 * @return true if any of the primitive fields on this object are set or if any of its complex attributes have data
	 */
	boolean hasData();
	
	default <A> A getIndex(List<A> list, int index, Supplier<A> supplier) {
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
	
	public abstract <B extends RosettaModelObjectBuilder> B merge(B other, BuilderMerger merger);
}