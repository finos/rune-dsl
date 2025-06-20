package com.regnosys.rosetta.generator.java.scoping;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.regnosys.rosetta.generator.GeneratedIdentifier;

public class JavaClassScope extends AbstractJavaScope<AbstractJavaScope<?>> {
	private final JavaClassScope superClassScope;
	
	JavaClassScope(String className, AbstractJavaScope<?> parentScope, JavaClassScope superClassScope) {
		super("Class[" + className + "]", parentScope);
		this.superClassScope = superClassScope;
	}

	public JavaMethodScope methodScope(Object methodKey, String methodName) {
		this.createIdentifier(methodKey, methodName);
		return new JavaMethodScope(methodName, this);
	}
	public JavaMethodScope methodOverrideScope(Object methodKey) {
		return new JavaMethodScope(this.getIdentifierOrThrow(methodKey).getDesiredName(), this);
	}

	@Override
	public String escapeName(String name) {
		// How should members be escaped?
		return "_" + name;
	}
	
	/**
	 * Include inherited members from the superclass scope.
	 */
	@Override
	protected Map<Object, GeneratedIdentifier> getOwnIdentifiersMap() {
		if (superClassScope == null) {
			return super.getOwnIdentifiersMap();
		}
		Map<Object, GeneratedIdentifier> ownIdentifiers = new LinkedHashMap<>();
		addOwnIdentifiersToMap(ownIdentifiers);
		return ownIdentifiers;
	}
	private void addOwnIdentifiersToMap(Map<Object, GeneratedIdentifier> identifiersMap) {
		if (superClassScope != null) {
			superClassScope.addOwnIdentifiersToMap(identifiersMap);
		}
		identifiersMap.putAll(super.getOwnIdentifiersMap());
	}
	
	/**
	 * Make sure members from the superclass are considered "taken", so identifiers will be escaped appropriately.
	 */
	@Override
	protected Collection<String> getOwnTakenNames() {
		if (superClassScope == null) {
			return super.getOwnTakenNames();
		}
		Set<String> ownTakenNames = new HashSet<>();
		this.addOwnTakenNamesToSet(ownTakenNames);
		return ownTakenNames;
	}
	private void addOwnTakenNamesToSet(Set<String> takenNamesSet) {
		if (superClassScope != null) {
			superClassScope.addOwnTakenNamesToSet(takenNamesSet);
		}
		takenNamesSet.addAll(super.getOwnTakenNames());
	}
}
