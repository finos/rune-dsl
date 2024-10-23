package com.regnosys.rosetta.formatting2;

import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;

public interface CodeFormatterService {
	List<Resource> formatCollection(List<Resource> resources); 
}
