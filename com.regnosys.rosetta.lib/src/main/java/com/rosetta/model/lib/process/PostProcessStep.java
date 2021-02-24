package com.rosetta.model.lib.process;

import com.rosetta.lib.postprocess.PostProcessorReport;
import com.rosetta.model.lib.RosettaModelObject;

/**
 * @author TomForwood
 * PostProcessors are called by the ingestion service to perform post-processing on the resulting
 * object after ingestion.
 */
public interface PostProcessStep {
	
	/**
	 * @return the priority to run the processor lower numbers get run first
	 * PostProcesssSteps that return null will not automatically run at all
	 */
	Integer getPriority();
	
	String getName();
	
	/**
	 * Process a rosettaObject
	 * @param topClass the class of the object being passed in to process
	 * @param builder an object to process
	 * @return A resulting report of the processing.
	 */
	<T extends RosettaModelObject> PostProcessorReport runProcessStep(Class<? extends T> topClass, T instance);

}
