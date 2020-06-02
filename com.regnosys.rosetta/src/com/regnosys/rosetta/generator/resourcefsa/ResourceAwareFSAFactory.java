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

}
