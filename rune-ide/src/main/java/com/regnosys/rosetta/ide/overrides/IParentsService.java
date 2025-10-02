package com.regnosys.rosetta.ide.overrides;

import java.util.List;

import org.eclipse.xtext.ide.server.Document;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.CancelIndicator;

public interface IParentsService {
	List<? extends ParentsResult> computeParents(Document document, XtextResource resource, ParentsParams params, CancelIndicator indicator);
}
