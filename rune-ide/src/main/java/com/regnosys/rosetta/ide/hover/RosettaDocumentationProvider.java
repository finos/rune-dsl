package com.regnosys.rosetta.ide.hover;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.documentation.IEObjectDocumentationProvider;

import com.google.common.collect.Streams;
import com.regnosys.rosetta.rosetta.RosettaDefinable;
import com.regnosys.rosetta.rosetta.RosettaEnumValue;
import com.regnosys.rosetta.rosetta.RosettaSymbol;
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference;
import com.regnosys.rosetta.types.CardinalityProvider;
import com.regnosys.rosetta.types.ExpectedTypeProvider;
import com.regnosys.rosetta.types.RType;

public class RosettaDocumentationProvider implements IEObjectDocumentationProvider {

	@Inject
	private ExpectedTypeProvider expectedTypeProvider;
	@Inject
	private CardinalityProvider cardinalityProvider;
	
	@Override
	public String getDocumentation(EObject o) {
		return Streams.concat(
				getDocumentationFromReference(o).stream(),
				getDocumentationFromOwner(o).stream()
			).collect(Collectors.joining("\n\n"));
	}

	public List<String> getDocumentationFromReference(EObject o) {
		List<String> docs = new ArrayList<>();
		if (o instanceof RosettaSymbol) {
			RosettaSymbol symbol = (RosettaSymbol)o;
			boolean isMulti = cardinalityProvider.isSymbolMulti(symbol);
			
			if (isMulti) {
				docs.add("**Multi cardinality.**");
			}
		} 
		if (o instanceof RosettaDefinable) {
			RosettaDefinable objectWithDocs = (RosettaDefinable)o;
			if (objectWithDocs.getDefinition() != null) {
				docs.add(objectWithDocs.getDefinition());
			}
		}
		return docs;
	}

	public List<String> getDocumentationFromOwner(EObject o) {
		List<String> docs = new ArrayList<>();
		if (o instanceof RosettaSymbolReference) {
			RosettaSymbol symbol = ((RosettaSymbolReference)o).getSymbol();
			if (symbol instanceof RosettaEnumValue) {
				RType t = expectedTypeProvider.getExpectedTypeFromContainer(o).getRType();
				docs.add(t.toString());
			}
		}
		return docs;
	}
}
