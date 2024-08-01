package com.regnosys.rosetta.resource;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;

public class DefaultImplicitReferenceDescription implements IImplicitReferenceDescription {
	private URI sourceEObjectUri;
	private URI targetEObjectUri;
	
	public DefaultImplicitReferenceDescription(EObject from, EObject to) {
		this.sourceEObjectUri = EcoreUtil2.getPlatformResourceOrNormalizedURI(from);
		this.targetEObjectUri = EcoreUtil2.getPlatformResourceOrNormalizedURI(to);
	}

	public DefaultImplicitReferenceDescription(URI sourceEObjectUri, URI targetEObjectUri) {
		this.sourceEObjectUri = sourceEObjectUri;
		this.targetEObjectUri = targetEObjectUri;
	}

	@Override
	public URI getSourceEObjectUri() {
		return sourceEObjectUri;
	}

	@Override
	public URI getTargetEObjectUri() {
		return targetEObjectUri;
	}
}
