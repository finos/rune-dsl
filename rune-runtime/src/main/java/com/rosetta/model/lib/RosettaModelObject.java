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

import com.rosetta.model.lib.meta.RosettaMetaData;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.process.AttributeMeta;
import com.rosetta.model.lib.process.Processor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public interface RosettaModelObject {

	RosettaModelObjectBuilder toBuilder();
	RosettaModelObject build();

	/**
	 * @return The MetaData {@link RosettaMetaData} object for this class providing access to things like validation
	 */
	RosettaMetaData<? extends RosettaModelObject> metaData();
	
	Class<? extends RosettaModelObject> getType();

	void process(RosettaPath path, Processor processor);

	default <R extends RosettaModelObject> void processRosetta(RosettaPath path, Processor processor, Class<R> clazz, R child, AttributeMeta... metas) {
		boolean processFurther = processor.processRosetta(path, clazz, child, this, metas);
		if (child!=null && processFurther) child.process(path, processor);
	}

	default <R extends RosettaModelObject> void processRosetta(RosettaPath path, Processor processor, Class<R> clazz, List<? extends R> children, AttributeMeta... metas) {
		boolean processFurther = processor.processRosetta(path, clazz, children, this, metas);
		if (children!=null && processFurther)  {
			int index=0;
			// Iterate through a copy of children to prevent a fail-fast ConcurrentModificationException if a mapping processor modifies the children.
			List<? extends RosettaModelObject> copy = new ArrayList<>(children);
			for (Iterator<? extends RosettaModelObject> iterator = copy.iterator(); iterator.hasNext();) {
				RosettaModelObject child = iterator.next();
				if (child!=null) {
					RosettaPath indexedPath = path.withIndex(index);
					child.process(indexedPath, processor);
					index++;
				}
			}
		}
	}
}