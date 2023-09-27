package com.rosetta.model.lib;

import java.util.Objects;

import org.apache.commons.lang3.Validate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.rosetta.util.DottedPath;

public class ModelSymbolId implements Comparable<ModelSymbolId> {
	private DottedPath namespace;
	private String name;
	

	public ModelSymbolId(DottedPath namespace, String name) {
		Objects.requireNonNull(namespace);
		Objects.requireNonNull(name);
		
		this.namespace = namespace;
		this.name = name;
	}
	
	@JsonCreator
	public static ModelSymbolId fromQualifiedName(String str) {
		DottedPath qualifiedName = DottedPath.splitOnDots(str);
		return new ModelSymbolId(qualifiedName.parent(), qualifiedName.last());
	}
	public static ModelSymbolId fromRegulatoryReference(DottedPath namespace, String body, String... corpusList) {
		Objects.requireNonNull(namespace);
		Objects.requireNonNull(body);
		Validate.noNullElements(corpusList);
		return new ModelSymbolId(namespace, body + String.join("", corpusList));
	}
	
	public DottedPath getNamespace() {
		return namespace;
	}
	public String getName() {
		return name;
	}

	@JsonValue
	public DottedPath getQualifiedName() {
		return namespace.child(name);
	}

	@Override
	public String toString() {
		return getQualifiedName().withDots();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(name, namespace);
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
		return Objects.equals(name, other.name) && Objects.equals(namespace, other.namespace);
	}

	@Override
	public int compareTo(ModelSymbolId o) {
		return this.getQualifiedName().compareTo(o.getQualifiedName());
	}
}
