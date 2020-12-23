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
	<R extends RosettaModelObject> boolean processRosetta(RosettaPath path, Class<R> rosettaType, 
    		R instance, RosettaModelObject parent, AttributeMeta... metas);
	
	<R extends RosettaModelObject> boolean processRosetta(RosettaPath path, Class<R> rosettaType, 
    		List<R> instance, RosettaModelObject parent, AttributeMeta... metas);
    
	/**
     * process a rosetta primitive type
     * @param path the RosettaPath taken to the objects
     * @param rosettaType the Type of the objects
     * @param instance the primitive value to be processed
     * @param parent the RosettaModelObject which contains these object as an attribute
     * @param metas Flags indicating meta information about the attribute
     */
    <T> void processBasic(RosettaPath path, Class<T> rosettaType, T instance, RosettaModelObject parent, AttributeMeta... metas);
    
    <T> void processBasic(RosettaPath path, Class<T> rosettaType, Collection<T> instance, RosettaModelObject parent, AttributeMeta... metas);
    
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
