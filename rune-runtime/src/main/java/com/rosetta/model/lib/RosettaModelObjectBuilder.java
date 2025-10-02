/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rosetta.model.lib;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.process.AttributeMeta;
import com.rosetta.model.lib.process.BuilderMerger;
import com.rosetta.model.lib.process.BuilderProcessor;

/**
 * @author TomForwood
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
	
	void process(RosettaPath path, BuilderProcessor processor);
	
	default <R extends RosettaModelObjectBuilder> void processRosetta(RosettaPath path, BuilderProcessor processor, Class<R> clazz, R child, AttributeMeta... metas) {
		boolean processFurther = processor.processRosetta(path, clazz, child, this, metas);
		if (child!=null && processFurther) child.process(path, processor);
	}
	default <R extends RosettaModelObjectBuilder> void processRosetta(RosettaPath path, BuilderProcessor processor, Class<R> clazz, List<? extends R> children, AttributeMeta... metas) {
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

	/**
	 * @return true if any of the primitive fields on this object are set or if any of its complex attributes have data
	 */
	boolean hasData();
	
	default <A> A getIndex(List<A> list, int index, Supplier<A> supplier) {
		if (index==-1 || index==Integer.MAX_VALUE) {//either of these values are code for just give me the next index
			index = list.size();
		}
		
		if (list.size()>index) {//this item already exists - return it
			A a = list.get(index);
			if (a==null) {//if there was null at this index before then create a new item
				a = supplier.get();
				list.set(index, a);
			}
			return a;
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