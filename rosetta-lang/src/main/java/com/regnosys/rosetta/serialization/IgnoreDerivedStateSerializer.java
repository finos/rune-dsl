package com.regnosys.rosetta.serialization;

import java.io.IOException;
import java.io.Writer;

import javax.inject.Inject;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.resource.SaveOptions;
import org.eclipse.xtext.serializer.ISerializationContext;
import org.eclipse.xtext.serializer.acceptor.ISequenceAcceptor;
import org.eclipse.xtext.serializer.diagnostic.ISerializationDiagnostic;
import org.eclipse.xtext.serializer.impl.Serializer;

import com.regnosys.rosetta.derivedstate.RosettaDerivedStateComputer;

public class IgnoreDerivedStateSerializer extends Serializer {
	@Inject
	RosettaDerivedStateComputer derivedStateComputer;
	
	@Override
	public String serialize(EObject obj, SaveOptions options) {
		derivedStateComputer.removeAllDerivedState(obj);
		String result = super.serialize(obj, options);
		derivedStateComputer.setAllDerivedState(obj);
		return result;
	}
	
	@Override
	public void serialize(EObject obj, Writer writer, SaveOptions options) throws IOException {
		derivedStateComputer.removeAllDerivedState(obj);
		super.serialize(obj, writer, options);
		derivedStateComputer.setAllDerivedState(obj);
	}
	
	@Override
	protected void serialize(ISerializationContext context, EObject semanticObject, ISequenceAcceptor tokens,
			ISerializationDiagnostic.Acceptor errors) {
		derivedStateComputer.removeAllDerivedState(semanticObject);
		super.serialize(context, semanticObject, tokens, errors);
		derivedStateComputer.setAllDerivedState(semanticObject);
	}
}
