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
import java.util.List;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.path.RosettaPath;

/**
 * @author TomForwood
 * Class defining a processor that runs on a RosettaModelObjectBuilder
 * instances of this class are designed to be used with the RosettaModelObjectBuilder process method to perform visitor pattern
 * processing on the object tree. They are permitted to make changes to the underlying objects and can return an accumulated result
 * in the report object
 * 
 * BuilderProcessors added to the rosetta model using the processor syntax will be automatically run during the ingestion stages 
 * and can be used to perform custom mapping logic
 * 
 */
public interface BuilderProcessor {
	
    /**
     * Process a rosetta object
     * @param path the RosettaPath taken to the object
     * @param rosettaType the Type of the object
     * @param builder a RosettaModelObjectBuilder representing the object
     * @param parent the RosettaModelObjectBuilder which contains this object as an attribute
     * @param metas Flags indicating meta information about the attribute
     * 
     * returns true if this processor is interested in processing the objects fields
     */
    <R extends RosettaModelObject> boolean processRosetta(RosettaPath path, Class<R> rosettaType, 
    		RosettaModelObjectBuilder builder, RosettaModelObjectBuilder parent, AttributeMeta... metas);
    /**
     * Processes a list of Rosetta objects - allows new values to be added or removed from the list
     * @param path the RosettaPath taken to the objects
     * @param rosettaType the Type of the objects
     * @param builders a List of RosettaModelObjectBuilder representing the objects
     * @param parent the RosettaModelObjectbuilder which contains these object as an attribute
     * @param metas Flags indicating meta information about the attribute
     */
    <R extends RosettaModelObject> boolean processRosetta(RosettaPath path, Class<R> rosettaType, 
    		List<? extends RosettaModelObjectBuilder> builders, RosettaModelObjectBuilder parent, AttributeMeta... metas);

    /**
     * process a rosetta primitive type
     * @param path the RosettaPath taken to the objects
     * @param rosettaType the Type of the objects
     * @param instance the primitive value to be processed
     * @param parent the RosettaModelObjectBuilder which contains these object as an attribute
     * @param metas Flags indicating meta information about the attribute
     */
    <T> void processBasic(RosettaPath path, Class<T> rosettaType, T instance, RosettaModelObjectBuilder parent, AttributeMeta... metas);
    /**
     * process a list of rosetta primitive type - allows new values to be added or removed from the list
     * @param path the RosettaPath taken to the objects
     * @param rosettaType the Type of the objects
     * @param instances the list of primitive value to be processed
     * @param parent the RosettaModelObjectbuilder which contains these object as an attribute
     * @param metas Flags indicating meta information about the attribute
     */
    <T> void processBasic(RosettaPath path, Class<T> rosettaType, Collection<? extends T> instances, RosettaModelObjectBuilder parent, AttributeMeta... metas);
    
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
