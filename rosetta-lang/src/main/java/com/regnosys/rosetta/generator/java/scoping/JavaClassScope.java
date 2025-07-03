package com.regnosys.rosetta.generator.java.scoping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.regnosys.rosetta.generator.GeneratedIdentifier;
import com.rosetta.util.types.JavaTypeDeclaration;

public class JavaClassScope extends AbstractJavaScope<AbstractJavaScope<?>> {
	private final List<JavaClassScope> superClassScopes;
	
	JavaClassScope(String className, AbstractJavaScope<?> parentScope, List<JavaClassScope> superClassScopes) {
		super("Class[" + className + "]", parentScope);
		this.superClassScopes = superClassScopes;
	}
	
	public JavaClassScope createNestedClassScopeAndRegisterIdentifier(JavaTypeDeclaration<?> clazz) {
		this.createIdentifier(clazz, clazz.getSimpleName());
		this.getPackageScope().createIdentifier(clazz, clazz.getNestedTypeName().withDots());
		
		List<JavaClassScope> superClassScopes = new ArrayList<>();
//		if (clazz.getSuperclassDeclaration() != null) {
//			superClassScopes.add(getClassScope(clazz.getSuperclassDeclaration()));
//		}
//		for (var interf : clazz.getInterfaceDeclarations()) {
//			superClassScopes.add(getClassScope(interf));
//		}
		return new JavaClassScope(clazz.getSimpleName(), this, superClassScopes);
	}

	public JavaMethodScope createMethodScope(String methodName) {
		return new JavaMethodScope(methodName, this);
	}
	
	public JavaPackageScope getPackageScope() {
		return getFileScope().getParent();
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
		if (superClassScopes.isEmpty()) {
			return super.getOwnIdentifiersMap();
		}
		Map<Object, GeneratedIdentifier> ownIdentifiers = new LinkedHashMap<>();
		addOwnIdentifiersToMap(ownIdentifiers);
		return ownIdentifiers;
	}
	private void addOwnIdentifiersToMap(Map<Object, GeneratedIdentifier> identifiersMap) {
		for (var superClassScope : superClassScopes) {
			superClassScope.addOwnIdentifiersToMap(identifiersMap);
		}
		identifiersMap.putAll(super.getOwnIdentifiersMap());
	}
	
	/**
	 * Make sure members from the superclass are considered "taken", so identifiers will be escaped appropriately.
	 */
	@Override
	protected Collection<String> getOwnTakenNames() {
		if (superClassScopes.isEmpty()) {
			return super.getOwnTakenNames();
		}
		Set<String> ownTakenNames = new HashSet<>();
		this.addOwnTakenNamesToSet(ownTakenNames);
		return ownTakenNames;
	}
	private void addOwnTakenNamesToSet(Set<String> takenNamesSet) {
		for (var superClassScope : superClassScopes) {
			superClassScope.addOwnTakenNamesToSet(takenNamesSet);
		}
		takenNamesSet.addAll(super.getOwnTakenNames());
	}
}
