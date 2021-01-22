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
     * @param builder a List of RosettaModelObjectBuilder representing the objects
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
