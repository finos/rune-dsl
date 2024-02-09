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

package com.regnosys.rosetta.generator.resourcefsa;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.generator.IFileSystemAccess2;

public interface ResourceAwareFSAFactory {
	
	/**
	 * @param resource The resource changing that caused this FSA to be invoked
	 * @param fsa -the Xtext supplied FSA to potentially delegate to
	 * @param wholeModel - Whether the generators are being called with the whole model rather than a single resource (i.e. in after generate)
	 * @return
	 */
	IFileSystemAccess2 resourceAwareFSA(Resource resource, IFileSystemAccess2 fsa, boolean wholeModel);
	
	void beforeGenerate(Resource resource);
	
	void afterGenerate(Resource resource);

}
