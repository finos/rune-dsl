package com.regnosys.rosetta.ide.hover;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.documentation.IEObjectDocumentationProvider;

import com.regnosys.rosetta.rosetta.RosettaDefinable;
import com.regnosys.rosetta.rosetta.RosettaSymbol;
import com.regnosys.rosetta.types.CardinalityProvider;

public class RosettaDocumentationProvider implements IEObjectDocumentationProvider {

	@Inject
	private CardinalityProvider cardinalityProvider;

	@Override
	public String getDocumentation(EObject o) {
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
		if (docs.isEmpty()) {
			return null;
		}
		return docs.stream().collect(Collectors.joining("\n\n"));
	}

}
