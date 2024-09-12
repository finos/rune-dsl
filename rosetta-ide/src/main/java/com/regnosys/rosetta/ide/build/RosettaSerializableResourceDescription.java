package com.regnosys.rosetta.ide.build;

import static com.google.common.collect.Iterables.transform;
import static org.eclipse.xtext.resource.persistence.SerializationExtensions.readCastedObject;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IReferenceDescription;
import org.eclipse.xtext.resource.persistence.SerializableEObjectDescription;
import org.eclipse.xtext.resource.persistence.SerializableEObjectDescriptionProvider;
import org.eclipse.xtext.resource.persistence.SerializableReferenceDescription;
import org.eclipse.xtext.resource.persistence.SerializableResourceDescription;

import com.google.common.collect.Lists;
import com.regnosys.rosetta.resource.IImplicitReferenceDescription;
import com.regnosys.rosetta.resource.IRosettaResourceDescription;

public class RosettaSerializableResourceDescription extends SerializableResourceDescription implements IRosettaResourceDescription {
	
	private List<SerializableImplicitReferenceDescription> implicitReferences = Collections.emptyList();

	@Override
	public void updateResourceURI(URI uri) {
		for (SerializableImplicitReferenceDescription implRef : implicitReferences) {
			implRef.updateResourceURI(uri, getURI());
		}
		super.updateResourceURI(uri);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Iterable<IImplicitReferenceDescription> getImplicitReferenceDescriptions() {
		return ((Iterable<IImplicitReferenceDescription>) ((Iterable<?>) implicitReferences));
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		int implicitReferencesSize = in.readInt();
		implicitReferences = new ArrayList<>(implicitReferencesSize);
		for (int i = 0; i < implicitReferencesSize; i++)
			implicitReferences.add(readCastedObject(in));
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeInt(implicitReferences.size());
		for (SerializableImplicitReferenceDescription implRef : implicitReferences)
			out.writeObject(implRef);
	}
	
	public List<SerializableImplicitReferenceDescription> getImplicitReferences() {
		return implicitReferences;
	}
	
	public void setImplicitReferences(List<SerializableImplicitReferenceDescription> implicitReferences) {
		this.implicitReferences = implicitReferences;
	}
	
	public static RosettaSerializableResourceDescription createCopy(IRosettaResourceDescription desc) {
		RosettaSerializableResourceDescription description = new RosettaSerializableResourceDescription();
		description.setURI(desc.getURI());
		description.setDescriptions(Lists.newArrayList(
				transform(desc.getExportedObjects(), RosettaSerializableResourceDescription::createCopy)));
		description.setReferences(Lists.newArrayList(
				transform(desc.getReferenceDescriptions(), RosettaSerializableResourceDescription::createCopy)));
		description.setImportedNames(Lists.newArrayList(desc.getImportedNames()));
		description.setImplicitReferences(Lists.newArrayList(
				transform(desc.getImplicitReferenceDescriptions(), RosettaSerializableResourceDescription::createCopy)));
		return description;
	}
	
	private static SerializableEObjectDescription createCopy(IEObjectDescription desc) {
		if (desc instanceof SerializableEObjectDescriptionProvider)
			return ((SerializableEObjectDescriptionProvider) desc).toSerializableEObjectDescription();
		SerializableEObjectDescription result = new SerializableEObjectDescription();
		result.setEClass(desc.getEClass());
		result.setEObjectURI(desc.getEObjectURI());
		result.setQualifiedName(desc.getQualifiedName());
		result.setUserData(new HashMap<String, String>(desc.getUserDataKeys().length));
		for (String key : desc.getUserDataKeys())
			result.getUserData().put(key, desc.getUserData(key));
		return result;
	}

	private static SerializableReferenceDescription createCopy(IReferenceDescription desc) {
		SerializableReferenceDescription result = new SerializableReferenceDescription();
		result.setSourceEObjectUri(desc.getSourceEObjectUri());
		result.setTargetEObjectUri(desc.getTargetEObjectUri());
		result.setEReference(desc.getEReference());
		result.setIndexInList(desc.getIndexInList());
		result.setContainerEObjectURI(desc.getContainerEObjectURI());
		return result;
	}
	
	private static SerializableImplicitReferenceDescription createCopy(IImplicitReferenceDescription desc) {
		SerializableImplicitReferenceDescription result = new SerializableImplicitReferenceDescription();
		result.setSourceEObjectUri(desc.getSourceEObjectUri());
		result.setTargetEObjectUri(desc.getTargetEObjectUri());
		return result;
	}
}
