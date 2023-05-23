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
