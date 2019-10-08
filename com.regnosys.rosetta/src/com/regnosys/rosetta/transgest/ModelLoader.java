package com.regnosys.rosetta.transgest;

import java.util.List;

import org.eclipse.emf.ecore.resource.ResourceSet;

import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaRootElement;
import com.regnosys.rosetta.rosetta.RosettaType;
import com.rosetta.model.lib.RosettaModelObject;	

public interface ModelLoader {	

	RosettaType rosettaClass(Class<? extends RosettaModelObject> rootObject);	

	RosettaType rosettaClass(String className);	

	List<RosettaModel> models();	

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
	<T extends RosettaRootElement> List<T> rosettaElements(Class<T> clazz);	

	ResourceSet getResourceSet();	

	void addModel(RosettaModel model);	

} 