package com.regnosys.rosetta.formatting2;

import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.XtextResource;

public interface CodeFormatterService {
	List<XtextResource> formatCollection(List<XtextResource> resources); 
}
