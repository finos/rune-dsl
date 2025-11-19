package com.regnosys.rosetta.tests.testmodel;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.regnosys.rosetta.rosetta.*;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.simple.Condition;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.tests.util.ExpressionParser;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IResourceDescriptionsProvider;

/**
 * A test utility for accessing elements in a Rune model by name.
 */
public class RosettaTestModel {
	private final String source;
	private final RosettaModel model;

    private final IResourceDescriptionsProvider indexAccess;
	private final ExpressionParser expressionParser;
	
	public RosettaTestModel(CharSequence source, RosettaModel model, IResourceDescriptionsProvider indexAccess, ExpressionParser expressionParser) {
		this.source = source.toString();
		this.model = model;
		
        this.indexAccess = indexAccess;
		this.expressionParser = expressionParser;
	}
	
	public RosettaModel getModel() {
		return model;
	}
	public Resource getResource() {
		return model.eResource();
	}
	public ResourceSet getResourceSet() {
		return getResource().getResourceSet();
	}
	
	public RosettaExpression parseExpression(CharSequence expressionSource, String... attributes) {
		return expressionParser.parseExpression(expressionSource, List.of(model), List.of(attributes));
	}
	
    public RosettaScope getScope(String name) {
        for (IEObjectDescription descr : indexAccess.getResourceDescriptions(getResourceSet()).getExportedObjectsByType(RosettaPackage.eINSTANCE.getRosettaScope())) {
            if (name.equals(descr.getName().getLastSegment())) {
                return (RosettaScope) descr.getEObjectOrProxy();
            }
        }
        throw new NoSuchElementException("No scope named '" + name + "' found in model.\n\n" + source);
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
	public Condition getCondition(String typeName, String conditionName) {
		RosettaTypeWithConditions t = getNamedElement(RosettaTypeWithConditions.class, typeName);
		return getNamedElement(t.getConditions(), Condition.class, conditionName);
				
	}
	public RosettaExternalRuleSource getRuleSource(String name) {
		return getNamedElement(RosettaExternalRuleSource.class, name);
	}
	
	private <T extends EObject> T getElementMatching(List<? extends EObject> elements, Class<T> clazz, Predicate<T> match, Supplier<? extends RuntimeException> exceptionSupplier) {
		return elements.stream()
				.filter(clazz::isInstance)
				.map(clazz::cast)
				.filter(match)
				.findAny()
				.orElseThrow(exceptionSupplier);
	}
	private <T extends EObject> T getElementMatching(Class<T> clazz, Predicate<T> match, Supplier<? extends RuntimeException> exceptionSupplier) {
		return getElementMatching(model.getElements(), clazz, match, exceptionSupplier);
	}
	private <T extends RosettaNamed> T getNamedElement(List<? extends EObject> elements, Class<T> clazz, String name) {
		RosettaNamed elem = getElementMatching(elements, RosettaNamed.class, x -> name.equals(x.getName()), () -> new NoSuchElementException("No element named '" + name + "' found in model.\n\n" + source));
		if (!clazz.isInstance(elem)) {
			throw new ClassCastException("The element named '" + name + "' is of type " + elem.getClass().getSimpleName() + ", not " + clazz.getSimpleName() + ".\n\n" + source);
		}
		return clazz.cast(elem);
	}
	private <T extends RosettaRootElement & RosettaNamed> T getNamedElement(Class<T> clazz, String name) {
		return getNamedElement(model.getElements(), clazz, name);
	}
}
