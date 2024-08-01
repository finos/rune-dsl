package com.regnosys.rosetta.resource;

import org.eclipse.xtext.resource.IResourceDescription;

public interface IRosettaResourceDescription extends IResourceDescription {
	Iterable<IImplicitReferenceDescription> getImplicitReferenceDescriptions();
}
