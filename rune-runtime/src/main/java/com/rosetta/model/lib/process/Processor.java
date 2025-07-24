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

package com.rosetta.model.lib.process;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.path.RosettaPath;

/**
 * @author TomForwood
 * Class defining a processor that runs on a RosettaModelObject
 * instances of this class are designed to be used with the RosettaModelObject process method to perform visitor pattern
 * processing on the object tree. They can return an accumulated result in the report object
 *
 */
public interface Processor {
	
	Set<RosettaModelObject> identitySet = Collections.newSetFromMap(new IdentityHashMap<>());
	
	/**
     * Process a rosetta object
     * @param path the RosettaPath taken to the object
     * @param rosettaType the Type of the object
	 * @param instance a RosettaModelObject representing the object
     * @param parent the RosettaModelObject which contains this object as an attribute
     * @param metas Flags indicating meta information about the attribute
	 * @return 
	 */
	<R extends RosettaModelObject> boolean processRosetta(RosettaPath path, Class<? extends R> rosettaType, 
    		R instance, RosettaModelObject parent, AttributeMeta... metas);
	
	<R extends RosettaModelObject> boolean processRosetta(RosettaPath path, Class<? extends R> rosettaType, 
    		List<? extends R> instance, RosettaModelObject parent, AttributeMeta... metas);
    
	/**
     * process a rosetta primitive type
     * @param path the RosettaPath taken to the objects
     * @param rosettaType the Type of the objects
     * @param instance the primitive value to be processed
     * @param parent the RosettaModelObject which contains these object as an attribute
     * @param metas Flags indicating meta information about the attribute
     */
    <T> void processBasic(RosettaPath path, Class<? extends T> rosettaType, T instance, RosettaModelObject parent, AttributeMeta... metas);
    
    <T> void processBasic(RosettaPath path, Class<? extends T> rosettaType, Collection<? extends T> instance, RosettaModelObject parent, AttributeMeta... metas);
    
    /**
     * @return a report representing the result of this processor
     */
    Report report();
    /**
     * @author TomForwood
     * Marker interface for the result of a processor
     */
    public static interface Report {
    }
}
