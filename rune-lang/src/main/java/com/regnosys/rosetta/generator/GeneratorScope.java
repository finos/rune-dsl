/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.regnosys.rosetta.generator;

import java.util.Collection;
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

public abstract class GeneratorScope<Scope extends GeneratorScope<?>> {	
	private final Scope parent;
	private final Map<Object, GeneratedIdentifier> identifiers = new LinkedHashMap<>();
	private final Map<Object, Object> keySynonyms = new HashMap<>();
	
	private boolean isClosed = false;
	private Map<GeneratedIdentifier, String> actualNames = null;
	
	private final String description;
	
	protected GeneratorScope(String description) {
		this(description, null);
	}
	
	protected GeneratorScope(String description, Scope parent) {
		this.description = description;
		this.parent = parent;
	}

	/**
	 * Determine whether `name` is a valid identifier in the target language.
	 * E.g., this method should return `false` if `name` is a reserved keyword in the target language.
	 */
	public abstract boolean isValidIdentifier(String name);

	public String escapeName(String name) {
		return "_" + name;
	}
	
	public boolean isClosed() {
		return this.isClosed;
	}
	/**
	 * May be null.
	 */
	public Scope getParent() {
		return this.parent;
	}
	
	public String getDebugInfo() {
		StringBuilder b = new StringBuilder();
		b.append(this.description);
		if (!this.isClosed) {
			b.append(" <unclosed>");
		}
		b.append(":");
		Map<Object, GeneratedIdentifier> ownIdentifiers = getOwnIdentifiersMap();
		if (ownIdentifiers.isEmpty()) {
			b.append(" <no identifiers>");
		} else {
			ownIdentifiers.entrySet().forEach(e ->
					b.append("\n\t").append(normalizeKey(e.getKey())).append(" -> \"").append(e.getValue().getDesiredName()).append("\""));
		}
		this.keySynonyms.entrySet().forEach(e -> 
			b.append("\n\t")
			.append("(keySynonym): ")
			.append(normalizeKey(e.getKey()))
			.append(" -> ")
			.append(normalizeKey(e.getValue())));
		
		if (parent != null) {
			b.append("\n").append(parent.getDebugInfo().replaceAll("(?m)^", "\t"));
		}
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
	
	protected Map<Object, GeneratedIdentifier> getOwnIdentifiersMap() {
		return identifiers;
	}

	public boolean isNameTaken(String desiredName) {
		return
				getOwnIdentifiersMap().values().stream().anyMatch(id -> desiredName.equals(id.getDesiredName()))
				|| getParent() != null && getParent().isNameTaken(desiredName);
	}
	
	/**
	 * Get the generated identifier of the given Rosetta object in the current
	 * scope, if it exists.
	 */
	public Optional<GeneratedIdentifier> getIdentifier(Object obj) {
		return Optional.ofNullable(parent)
				.flatMap(p -> p.getIdentifier(obj))
				.or(() -> Optional.ofNullable(getOwnIdentifiersMap().get(obj)))
				.or(() -> Optional.ofNullable(this.keySynonyms.get(obj)).flatMap(key -> getIdentifier(key)));				
	}
	/**
	 * Get the generated identifier of the given Rosetta object in the current
	 * scope, or throw if it does not exist.
	 */
	public GeneratedIdentifier getIdentifierOrThrow(Object obj) {
		return getIdentifier(obj).orElseThrow(() -> new NoSuchElementException("No identifier defined for " + normalizeKey(obj) + " in scope.\n" + this));
	}
	
	protected GeneratedIdentifier overwriteIdentifier(Object obj, String name) {
		if (isClosed) {
			throw new IllegalStateException("Cannot create a new identifier in a closed scope. (" + normalizeKey(obj) + " -> " + name + ")\n" + this);
		}
		GeneratedIdentifier id = new GeneratedIdentifier(this, name);
		this.identifiers.put(obj, id);
		return id;
	}
	/**
	 * Define the desired name for a Rosetta object in this scope.
	 * 
	 * @throws IllegalStateException if this scope is closed.
	 * @throws IllegalStateException if this scope already contains an identifier for `obj`.
	 */
	public GeneratedIdentifier createIdentifier(Object obj, String name) {
		if (this.getIdentifier(obj).isPresent()) {
			throw new IllegalStateException("There is already a name defined for object `" + normalizeKey(obj) + "`.\n" + this);
		}
		return overwriteIdentifier(obj, name);
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
	 * Create an synonym between an object and an already existing identifiable object.
	 * 
	 * @throws IllegalStateException if this scope is closed.
	 * @throws IllegalStateException if this scope already contains an identifier for `key`.
	 * @throws IllegalStateException if this scope does not contain an identifier for `keyWithIdentifier`.
	 */
	public void createKeySynonym(Object key, Object keyWithIdentifier) {
		if (isClosed) {
			throw new IllegalStateException("Cannot create a new key synonym in a closed scope. (" + normalizeKey(key) + " -> " + normalizeKey(keyWithIdentifier) + ")\n" + this);
		}
		if (this.getIdentifier(key).isPresent()) {
			throw new IllegalStateException("There is already a name defined for key `" + normalizeKey(key) + "`.\n" + this);
		}
		if (this.getIdentifier(keyWithIdentifier).isEmpty()) {
			throw new IllegalStateException("There is no name defined for key `" + normalizeKey(keyWithIdentifier) + "`.\n" + this);
		}
		
		this.keySynonyms.put(key, keyWithIdentifier);
	}
	/**
	 * Create another key for a given identifier.
	 * 
	 * @throws IllegalStateException if this scope is closed.
	 * @throws IllegalStateException if this scope already contains an identifier for `key`.
	 */
	public void createSynonym(Object key, GeneratedIdentifier identifier) {
		if (isClosed) {
			throw new IllegalStateException("Cannot create a new synonym in a closed scope. (" + normalizeKey(key) + " -> " + normalizeKey(identifier) + ")\n" + this);
		}
		if (this.getIdentifier(key).isPresent()) {
			throw new IllegalStateException("There is already a name defined for key `" + normalizeKey(key) + "`.\n" + this);
		}
		
		this.identifiers.put(key, identifier);
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
		return Optional.ofNullable(parent)
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
		
		Set<String> takenNames = getAllTakenNamesFromParent();
		LinkedListMultimap<String, GeneratedIdentifier> idsByDesiredName = localIdentifiersByDesiredName();
		for (String desiredName: idsByDesiredName.keySet()) {
			List<GeneratedIdentifier> ids = idsByDesiredName.get(desiredName);
			for (int i = 0; i < ids.size(); i++) {
				GeneratedIdentifier id = ids.get(i);
				String name = desiredName;
				if (ids.size() > 1) {
					name += i;
				}
				boolean lastWasValid = true;
				while (true) {
					boolean isValid = isValidIdentifier(name);
					if (!lastWasValid && !isValid) {
						// Escaping the invalid identifier did not work - throw an exception. Otherwise we could end up in an infinite loop.
						// If this is thrown, this usually indicates there is an implementation error in `escapeName` or `isValidIdentifier`.
						throw new RuntimeException("Tried escaping the identifier `" + name + "`, but it is still not a valid identifier.");
					}
					if (takenNames.contains(name) || !isValid) {
						name = escapeName(name);
					} else {
						break;
					}
					lastWasValid = isValid;
				}
				takenNames.add(name);
				this.actualNames.put(id, name);
			}
		}
	}
	private LinkedListMultimap<String, GeneratedIdentifier> localIdentifiersByDesiredName() {
		LinkedListMultimap<String, GeneratedIdentifier> result = LinkedListMultimap.create();
		identifiers.values().stream().distinct().forEach(id -> result.put(id.getDesiredName(), id));
		return result;
	}
	protected Set<String> getAllTakenNames() {
		Set<String> result = getAllTakenNamesFromParent();
		result.addAll(this.getOwnTakenNames());
		return result;
	}
	protected Collection<String> getOwnTakenNames() {
		if (this.actualNames == null) {
			this.computeActualNames();
		}
		return this.actualNames.values();
	}
	protected Set<String> getAllTakenNamesFromParent() {
		if (parent == null) {
			return new HashSet<>();
		}
		return parent.getAllTakenNames();
	}
}
