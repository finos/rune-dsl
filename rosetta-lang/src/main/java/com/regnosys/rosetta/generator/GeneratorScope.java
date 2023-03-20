package com.regnosys.rosetta.generator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;

import com.google.common.collect.LinkedListMultimap;
import com.regnosys.rosetta.rosetta.RosettaNamed;
import com.regnosys.rosetta.utils.ImplicitVariableUtil;

public abstract class GeneratorScope<Scope extends GeneratorScope<Scope>> {
	protected final ImplicitVariableUtil implicitVarUtil;
	
	protected final Optional<Scope> parent;
	private final Map<Object, GeneratedIdentifier> identifiers = new LinkedHashMap<>();
	
	private boolean isClosed = false;
	private Map<GeneratedIdentifier, String> actualNames = null;
	
	public GeneratorScope(ImplicitVariableUtil implicitVarUtil) {
		this.implicitVarUtil = implicitVarUtil;
		this.parent = Optional.empty();
	}
	
	protected GeneratorScope(ImplicitVariableUtil implicitVarUtil, Scope parent) {
		this.implicitVarUtil = implicitVarUtil;
		this.parent = Optional.of(parent);
	}
	
	/**
	 * Create a child scope from this one.
	 * The implementation should simply call the constructor,
	 * e.g., `return new MyScope(this.implicitVarUtil, this);`.
	 */
	public abstract Scope childScope();
	/**
	 * Determine whether `name` is a valid identifier in the target language.
	 * E.g., this method should return `false` if `name` is a keyword in the target language.
	 */
	public abstract boolean isValidIdentifier(String name);

	public String escapeName(String name) {
		return name + "_";
	}
	
	public Set<GeneratedIdentifier> getIdentifiers() {
		Set<GeneratedIdentifier> result = this.parent
				.map(p -> p.getIdentifiers())
				.orElseGet(() -> new HashSet<>());
		result.addAll(identifiers.values());
		return result;
	}
	
	/**
	 * Get the generated identifier of the given Rosetta object in the current
	 * scope, if it exists.
	 */
	public Optional<GeneratedIdentifier> getIdentifier(Object obj) {
		GeneratedIdentifier id = parent
				.flatMap(p -> p.getIdentifier(obj))
				.orElse(this.identifiers.get(obj));
		return Optional.ofNullable(id);
	}
	/**
	 * Define the desired name for a Rosetta object in this scope.
	 * 
	 * @throws IllegalStateException if this scope is closed.
	 * @throws IllegalStateException if this scope already contains an identifier for `obj`.
	 */
	public GeneratedIdentifier createIdentifier(Object obj, String name) {
		if (isClosed) {
			throw new IllegalStateException("Cannot create a new identifier for a closed scope.");
		}
		if (this.getIdentifier(obj).isPresent()) {
			throw new IllegalStateException("There is already a name defined for object `" + obj + "`.");
		}
		GeneratedIdentifier id = new GeneratedIdentifier(this, name);
		this.identifiers.put(obj, id);
		return id;
	}
	/**
	 * Create a new identifier with the desired name that is guaranteed
	 * not to clash with any identifiers defined before.
	 * 
	 * @throws IllegalStateException if this scope is closed.
	 */
	public GeneratedIdentifier createUniqueIdentifier(String name) {
		return createIdentifier(new Object(), name);
	}
	/**
	 * Get the generated identifier of the given Rosetta object in the current
	 * scope, or create a new one with the given desired name if it doesn't exist.
	 */
	public GeneratedIdentifier getOrCreateIdentifier(Object obj, String name) {
		return this.getIdentifier(obj).orElseGet(() -> createIdentifier(obj, name));
	}
	/**
	 * Get the generated identifier of the given Rosetta object in the current
	 * scope, or create a new one if it doesn't exist.
	 */
	public GeneratedIdentifier getOrCreateIdentifier(RosettaNamed obj) {
		return getOrCreateIdentifier(obj, obj.getName());
	}
	
	private ImplicitVariableRepresentation getReprOfImplicitVariable(EObject context) {
		return new ImplicitVariableRepresentation(
				implicitVarUtil.findContainerDefiningImplicitVariable(context)
					.orElseThrow());
	}
	/**
	 * Get the generated identifier of the implicit variable in the given context,
	 * if it exists.
	 * 
	 * @throws NoSuchElementException if there is no implicit variable in the given context.
	 */
	public Optional<GeneratedIdentifier> getIdentifierOfImplicitVariable(EObject context) {
		return this.getIdentifier(getReprOfImplicitVariable(context));
	}
	/**
	 * Define the desired name for the implicit variable in the given context in this scope.
	 * 
	 * @throws IllegalStateException if this scope is closed.
	 * @throws IllegalStateException if this scope already contains an identifier for the implicit variable in the given context.
	 * @throws NoSuchElementException if there is no implicit variable in the given context.
	 */
	public GeneratedIdentifier createIdentifierOfImplicitVariable(EObject context, String name) {
		return createIdentifier(getReprOfImplicitVariable(context), name);
	}
	/**
	 * Get the generated identifier of the implicit variable in the given context in the current
	 * scope, or create a new one with the given desired name if it doesn't exist.
	 */
	public GeneratedIdentifier getOrCreateIdentifierOfImplicitVariable(EObject context, String name) {
		return getOrCreateIdentifier(getReprOfImplicitVariable(context), name);
	}
	/**
	 * Get the generated identifier of the implicit variable in the given context in the current
	 * scope, or create a new one with the default implicit variable name if it doesn't exist.
	 */
	public GeneratedIdentifier getOrCreateIdentifierOfImplicitVariable(EObject context) {
		return getOrCreateIdentifierOfImplicitVariable(context, implicitVarUtil.getDefaultImplicitVariable().getName());
	}
	
	/**
	 * Mark this scope as closed. New identifiers cannot be added to a closed scope.
	 * 
	 * @throws IllegalStateException if this scope is closed.
	 */
	public void close() {
		if (this.isClosed) {
			throw new IllegalStateException("The scope is already closed.");
		}
		this.isClosed = true;
	}
	/**
	 * Get the actual name of the given identifier.
	 * 
	 * @throws IllegalStateException if this scope or any of its parents is not closed.
	 */
	public String getActualName(GeneratedIdentifier identifier) {
		if (!this.isClosed) {
			throw new IllegalStateException("Cannot get the actual name of a scope that is not closed.");
		}
		return this.parent
				.map(p -> p.getActualName(identifier))
				.orElseGet(() -> {
					if (this.actualNames == null) {
						this.computeActualNames();
					}
					return this.actualNames.get(identifier);
				});
	}
	private void computeActualNames() {
		this.actualNames = new HashMap<>();
		
		Set<String> takenNames = parent
			.map(p -> p.getTakenNames())
			.orElseGet(() -> new HashSet<>());
		LinkedListMultimap<String, GeneratedIdentifier> idsByDesiredName = identifiersByDesiredName();
		for (String desiredName: idsByDesiredName.keySet()) {
			List<GeneratedIdentifier> ids = idsByDesiredName.get(desiredName);
			for (int i = 0; i < ids.size(); i++) {
				GeneratedIdentifier id = ids.get(i);
				String name = id.getDesiredName();
				if (ids.size() > 1) {
					name += i;
				}
				while (takenNames.contains(name) || !isValidIdentifier(name)) {
					name = escapeName(name);
				}
				takenNames.add(name);
				this.actualNames.put(id, name);
			}
		}
	}
	private LinkedListMultimap<String, GeneratedIdentifier> identifiersByDesiredName() {
		LinkedListMultimap<String, GeneratedIdentifier> result = LinkedListMultimap.create();
		this.getIdentifiers().forEach(id -> result.put(id.getDesiredName(), id));
		return result;
	}
	protected Set<String> getTakenNames() {
		Set<String> result = parent
				.map(p -> p.getTakenNames())
				.orElseGet(() -> new HashSet<>());
		if (this.actualNames == null) {
			this.computeActualNames();
		}
		result.addAll(this.actualNames.values());
		return result;
	}
}
