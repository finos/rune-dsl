package com.regnosys.rosetta.generator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.LinkedListMultimap;
import com.regnosys.rosetta.rosetta.RosettaNamed;

public abstract class GeneratorScope<Scope extends GeneratorScope<Scope>> {	
	private final Optional<Scope> parent;
	private final Map<Object, GeneratedIdentifier> identifiers = new LinkedHashMap<>();
	
	private boolean isClosed = false;
	private Map<GeneratedIdentifier, String> actualNames = null;
	
	private final String description;
	
	public GeneratorScope(String description) {
		this.description = description;
		this.parent = Optional.empty();
	}
	
	protected GeneratorScope(String description, Scope parent) {
		this.description = description;
		this.parent = Optional.of(parent);
	}
	
	/**
	 * Create a child scope from this one.
	 * The implementation should simply call the constructor,
	 * e.g., `return new MyScope(this.implicitVarUtil, this);`.
	 */
	public abstract Scope childScope(String description);
	/**
	 * Determine whether `name` is a valid identifier in the target language.
	 * E.g., this method should return `false` if `name` is a keyword in the target language.
	 */
	public abstract boolean isValidIdentifier(String name);

	public String escapeName(String name) {
		return "_" + name;
	}
	
	public boolean isClosed() {
		return this.isClosed;
	}
	public Optional<Scope> getParent() {
		return this.parent;
	}
	
	public String getDebugInfo() {
		StringBuilder b = new StringBuilder();
		b.append(this.description);
		if (!this.isClosed) {
			b.append(" <unclosed>");
		}
		b.append(":");
		if (this.identifiers.isEmpty()) {
			b.append(" <no identifiers>");
		} else {
			this.identifiers.entrySet().forEach(e ->
					b.append("\n\t").append(normalizeKey(e.getKey())).append(" -> \"").append(e.getValue().getDesiredName()).append("\""));
		}
		parent.ifPresent(p -> {
			b.append("\n").append(p.getDebugInfo().replaceAll("(?m)^", "\t"));
		});
		return b.toString();
	}
	private String normalizeKey(Object key) {
		return key.toString().replace("\n", "\\n");
	}
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("=========== Scope Description ==========\n");
		b.append(getDebugInfo());
		b.append("\n========================================\n");
		
		return b.toString();
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
	 * Get the generated identifier of the given Rosetta object in the current
	 * scope, or throw if it does not exist.
	 */
	public GeneratedIdentifier getIdentifierOrThrow(Object obj) {
		return getIdentifier(obj).orElseThrow(() -> new NoSuchElementException("No identifier defined for " + normalizeKey(obj) + " in scope.\n" + this));
	}
	/**
	 * Define the desired name for a Rosetta object in this scope.
	 * 
	 * @throws IllegalStateException if this scope is closed.
	 * @throws IllegalStateException if this scope already contains an identifier for `obj`.
	 */
	public GeneratedIdentifier createIdentifier(Object obj, String name) {
		if (isClosed) {
			throw new IllegalStateException("Cannot create a new identifier in a closed scope. (" + normalizeKey(obj) + " -> " + name + ")\n" + this);
		}
		if (this.getIdentifier(obj).isPresent()) {
			throw new IllegalStateException("There is already a name defined for object `" + normalizeKey(obj) + "`.\n" + this);
		}
		GeneratedIdentifier id = new GeneratedIdentifier(this, name);
		this.identifiers.put(obj, id);
		return id;
	}
	/**
	 * Create an identifier for the given named Rosetta object.
	 * 
	 * @throws IllegalStateException if this scope is closed.
	 * @throws IllegalStateException if this scope already contains an identifier for `obj`.
	 */
	public GeneratedIdentifier createIdentifier(RosettaNamed obj) {
		return createIdentifier(obj, obj.getName());
	}
	/**
	 * Create a new identifier with the desired name that is guaranteed
	 * not to clash with any identifiers defined before.
	 * 
	 * @throws IllegalStateException if this scope is closed.
	 */
	public GeneratedIdentifier createUniqueIdentifier(String name) {
		Object token = new Object() {
			@Override
			public String toString() {
				return "{unique token for \"" + name + "\"}";
			}
		};
		return createIdentifier(token, name);
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
	
	/**
	 * Mark this scope as closed. New identifiers cannot be added to a closed scope.
	 * 
	 * @throws IllegalStateException if this scope is closed.
	 */
	public void close() {
		if (this.isClosed) {
			throw new IllegalStateException("The scope is already closed.\n" + this);
		}
		this.isClosed = true;
	}
	/**
	 * Get the actual name of the given identifier. Also closes the scope and
	 * its parent scopes if they weren't closed yet.
	 */
	public Optional<String> getActualName(GeneratedIdentifier identifier) {
		if (!this.isClosed) {
			this.close();
		}
		return this.parent
				.flatMap(p -> p.getActualName(identifier))
				.or(() -> {
					if (this.actualNames == null) {
						this.computeActualNames();
					}
					return Optional.ofNullable(this.actualNames.get(identifier));
				});
	}
	private void computeActualNames() {
		this.actualNames = new HashMap<>();
		
		Set<String> takenNames = parent
			.map(p -> p.getTakenNames())
			.orElseGet(() -> new HashSet<>());
		LinkedListMultimap<String, GeneratedIdentifier> idsByDesiredName = localIdentifiersByDesiredName();
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
	private LinkedListMultimap<String, GeneratedIdentifier> localIdentifiersByDesiredName() {
		LinkedListMultimap<String, GeneratedIdentifier> result = LinkedListMultimap.create();
		identifiers.values().forEach(id -> result.put(id.getDesiredName(), id));
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
