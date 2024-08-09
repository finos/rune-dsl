package com.regnosys.rosetta.ide.build;

import static org.eclipse.xtext.resource.persistence.SerializationExtensions.readURI;
import static org.eclipse.xtext.resource.persistence.SerializationExtensions.writeURI;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.eclipse.emf.common.util.URI;

import com.regnosys.rosetta.resource.IImplicitReferenceDescription;

public class SerializableImplicitReferenceDescription implements IImplicitReferenceDescription, Externalizable {
	private URI sourceEObjectUri;

	private URI targetEObjectUri;

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		sourceEObjectUri = readURI(in);
		targetEObjectUri = readURI(in);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		writeURI(out, sourceEObjectUri);
		writeURI(out, targetEObjectUri);
	}

	public void updateResourceURI(URI newURI, URI oldURI) {
		sourceEObjectUri = newURI.appendFragment(sourceEObjectUri.fragment());
		if (targetEObjectUri.trimFragment().equals(oldURI))
			targetEObjectUri = newURI.appendFragment(targetEObjectUri.fragment());
	}

	public URI getSourceEObjectUri() {
		return sourceEObjectUri;
	}

	public void setSourceEObjectUri(URI sourceEObjectUri) {
		this.sourceEObjectUri = sourceEObjectUri;
	}

	public URI getTargetEObjectUri() {
		return targetEObjectUri;
	}

	public void setTargetEObjectUri(URI targetEObjectUri) {
		this.targetEObjectUri = targetEObjectUri;
	}
}
