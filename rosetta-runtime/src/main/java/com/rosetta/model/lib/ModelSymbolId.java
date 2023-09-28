package com.rosetta.model.lib;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.rosetta.util.DottedPath;

public class ModelSymbolId extends ModelId implements Comparable<ModelSymbolId> {
	private final String name;
	

	public ModelSymbolId(DottedPath namespace, String name) {
		super(namespace);
		Objects.requireNonNull(namespace);
		Objects.requireNonNull(name);
		
		this.name = name;
	}
	
	@JsonCreator
	public static ModelSymbolId fromQualifiedName(String str) {
		DottedPath qualifiedName = DottedPath.splitOnDots(str);
		return new ModelSymbolId(qualifiedName.parent(), qualifiedName.last());
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String getAlphanumericName() {
		return name;
	}

	@JsonValue
	public DottedPath getQualifiedName() {
		return getNamespace().child(name);
	}

	@Override
	public String toString() {
		return getQualifiedName().withDots();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(name, getNamespace());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ModelSymbolId other = (ModelSymbolId) obj;
		return Objects.equals(name, other.name) && Objects.equals(getNamespace(), other.getNamespace());
	}

	@Override
	public int compareTo(ModelSymbolId o) {
		return this.getQualifiedName().compareTo(o.getQualifiedName());
	}
}
