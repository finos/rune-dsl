package com.rosetta.model.lib;

import java.util.Objects;

import org.apache.commons.lang3.Validate;

import com.rosetta.util.DottedPath;

public class ModelSymbolId {
	private DottedPath namespace;
	private String name;

	public ModelSymbolId(DottedPath namespace, String name) {
		Objects.requireNonNull(namespace);
		Objects.requireNonNull(name);
		
		this.namespace = namespace;
		this.name = name;
	}
	
	public static ModelSymbolId fromRegulatorReference(DottedPath namespace, String body, String... corpusList) {
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
	public DottedPath getQualifiedName() {
		return namespace.child(name);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(namespace, name);
	}
	@Override
	public boolean equals(Object object) {
		if (object == this) return true;
        if (this.getClass() != object.getClass()) return false;

        ModelSymbolId other = (ModelSymbolId) object;
        return Objects.equals(namespace, other.namespace)
        		&& Objects.equals(name, other.name);
	}
}
