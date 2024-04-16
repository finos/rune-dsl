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
	 * @param instance an object to process
	 * @return A resulting report of the processing.
	 */
	<T extends RosettaModelObject> PostProcessorReport runProcessStep(Class<? extends T> topClass, T instance);

}
