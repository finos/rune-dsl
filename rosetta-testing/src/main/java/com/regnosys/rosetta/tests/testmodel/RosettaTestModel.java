package com.regnosys.rosetta.tests.testmodel;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.eclipse.emf.ecore.EObject;

import com.regnosys.rosetta.rosetta.RegulatoryDocumentReference;
import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaNamed;
import com.regnosys.rosetta.rosetta.RosettaReport;
import com.regnosys.rosetta.rosetta.RosettaRootElement;
import com.regnosys.rosetta.rosetta.RosettaRule;
import com.regnosys.rosetta.rosetta.RosettaTypeAlias;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.rosetta.simple.Function;

/**
 * A test utility for accessing elements in a Rune model by name.
 */
public class RosettaTestModel {
	private final String source;
	private final RosettaModel model;
	
	public RosettaTestModel(CharSequence source, RosettaModel model) {
		this.source = source.toString();
		this.model = model;
	}
	
	public RosettaModel getModel() {
		return model;
	}
	
	private <T extends EObject> T getElementMatching(Class<T> clazz, Predicate<T> match, Supplier<? extends RuntimeException> exceptionSupplier) {
		return model.getElements().stream()
				.filter(elem -> clazz.isInstance(elem))
				.map(elem -> clazz.cast(elem))
				.filter(match)
				.findAny()
				.orElseThrow(exceptionSupplier);
	}
	private <T extends RosettaRootElement & RosettaNamed> T getNamedElement(Class<T> clazz, String name) {
		RosettaNamed elem = getElementMatching(RosettaNamed.class, x -> name.equals(x.getName()), () -> new NoSuchElementException("No element named '" + name + "' found in model.\n\n" + source));
		if (!clazz.isInstance(elem)) {
			throw new ClassCastException("The element named '" + name + "' is of type " + elem.getClass().getSimpleName() + ", not " + clazz.getSimpleName() + ".\n\n" + source);
		}
		return clazz.cast(elem);
	}
	
	public Data getType(String name) {
		return getNamedElement(Data.class, name);
	}
	public RosettaEnumeration getEnum(String name) {
		return getNamedElement(RosettaEnumeration.class, name);
	}
	public RosettaTypeAlias getTypeAlias(String name) {
		return getNamedElement(RosettaTypeAlias.class, name);
	}
	public Function getFunction(String name) {
		return getNamedElement(Function.class, name);
	}
	public RosettaRule getRule(String name) {
		return getNamedElement(RosettaRule.class, name);
	}
	public RosettaReport getReport(String body, String... corpusList) {
		return getElementMatching(RosettaReport.class, x -> {
			RegulatoryDocumentReference ref = x.getRegulatoryBody();
			if (!body.equals(ref.getBody().getName())) {
				return false;
			}
			if (corpusList.length != ref.getCorpusList().size()) {
				return false;
			}
			for (int i=0; i<corpusList.length; i++) {
				if (!corpusList[i].equals(ref.getCorpusList().get(i).getName())) {
					return false;
				}
			}
			return true;
		}, () -> new NoSuchElementException("No report with body " + body + " and corpus list " + Arrays.toString(corpusList) + " found in model.\n\n" + source));
	}
}
