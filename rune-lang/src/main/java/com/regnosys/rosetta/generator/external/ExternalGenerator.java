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

package com.regnosys.rosetta.generator.external;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

import com.regnosys.rosetta.rosetta.RosettaModel;
import com.rosetta.util.DemandableLock;

public interface ExternalGenerator {

	void beforeAllGenerate(ResourceSet set, Collection<? extends RosettaModel> models, String version, 
			Consumer<Map<String, ? extends CharSequence>> processResults, DemandableLock generateLock);
	
	void beforeGenerate(Resource resource, RosettaModel model, String version, 
			Consumer<Map<String, ? extends CharSequence>> processResults, DemandableLock generateLock);
	
	void generate(Resource resource, RosettaModel model, String version, 
			Consumer<Map<String, ? extends CharSequence>> processResults, DemandableLock generateLock);
	
	void afterGenerate(Resource resource, RosettaModel model, String version, 
			Consumer<Map<String, ? extends CharSequence>> processResults, DemandableLock generateLock);
	
	void afterAllGenerate(ResourceSet set, Collection<? extends RosettaModel> models, String version, 
			Consumer<Map<String, ? extends CharSequence>> processResults, DemandableLock generateLock);
	
	ExternalOutputConfiguration getOutputConfiguration();
	
}
