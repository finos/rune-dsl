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

package com.regnosys.rosetta.transgest;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaRootElement;
import com.regnosys.rosetta.rosetta.RosettaType;
import com.rosetta.model.lib.RosettaModelObject;	

public interface ModelLoader {
	
	List<RosettaModel> loadRosettaModels(Stream<URL> res);
	List<RosettaModel> loadRosettaModels(URL... urls);
	List<RosettaModel> loadRosettaModels(Collection<String> resourceLocations);

	RosettaType rosettaClass(List<RosettaModel> rosettaModels, Class<? extends RosettaModelObject> rootObject);	

	RosettaType rosettaClass(List<RosettaModel> rosettaModels, String className);

	/**	
	 * Will return a list of objects which are assignment-compatible with the object represented 	
	 * by this {@code Class} which is a subclass of {@link RosettaRootElement RosettaRootElement}	
	 * i.e.	
	 * <blockquote><pre>	
    * 		List<RosettaSynonymSource> synonyms = loader.rosettaElements(RosettaSynonymSource.class);	
    * </pre></blockquote>	
	 * 	
	 * @param clazz	
	 * @return a list of concrete objects of the above class or subclasses of	
	 */	
	<T extends RosettaRootElement> List<T> rosettaElements(List<RosettaModel> rosettaModels, Class<T> clazz);	
} 