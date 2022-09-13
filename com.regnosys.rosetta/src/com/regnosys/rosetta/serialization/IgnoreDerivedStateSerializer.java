package com.regnosys.rosetta.serialization;

import java.io.IOException;
import java.io.Writer;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.resource.SaveOptions;
import org.eclipse.xtext.serializer.impl.Serializer;

import com.google.inject.Inject;
import com.regnosys.rosetta.derivedstate.RosettaDerivedStateComputer;

@SuppressWarnings("restriction")
public class IgnoreDerivedStateSerializer extends Serializer {
	@Inject
	RosettaDerivedStateComputer derivedStateComputer;
	
	@Override
	public String serialize(EObject obj, SaveOptions options) {
		derivedStateComputer.removeAllDerivedState(obj.eAllContents());
		String result = super.serialize(obj, options);
		derivedStateComputer.setAllDerivedState(obj.eAllContents());
		return result;
	}
	
	@Override
	public void serialize(EObject obj, Writer writer, SaveOptions options) throws IOException {
		derivedStateComputer.removeAllDerivedState(obj.eAllContents());
		super.serialize(obj, writer, options);
		derivedStateComputer.setAllDerivedState(obj.eAllContents());
	}
}
