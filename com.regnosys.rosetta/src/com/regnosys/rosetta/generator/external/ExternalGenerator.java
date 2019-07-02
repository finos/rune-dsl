package com.regnosys.rosetta.generator.external;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.emf.ecore.resource.Resource;

import com.regnosys.rosetta.generator.java.RosettaJavaPackages;
import com.regnosys.rosetta.rosetta.RosettaRootElement;
import com.rosetta.util.DemandableLock;

public interface ExternalGenerator {

	void generate(RosettaJavaPackages packages, List<RosettaRootElement> elements, String version, 
			Consumer<Map<String, ? extends CharSequence>> processResults, Resource resource, DemandableLock generateLock);
	
	ExternalOutputConfiguration getOutputConfiguration();
	
}
