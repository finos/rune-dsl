package com.rosetta.model.lib.process;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;

/**
 * 
 * @author davidalk
 * A runner which is used to chain together a collection of PostProcessStep objects and execute
 * them on a given RosettaModelObjectBuilder
 *
 */
public interface PostProcessor {
	
	<T extends RosettaModelObject> RosettaModelObjectBuilder postProcess(Class<T> rosettaType, RosettaModelObjectBuilder instance);

}
