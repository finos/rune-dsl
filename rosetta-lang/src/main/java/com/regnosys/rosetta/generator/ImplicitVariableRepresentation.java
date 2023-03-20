package com.regnosys.rosetta.generator;

import java.util.Objects;

import org.eclipse.emf.ecore.EObject;

public class ImplicitVariableRepresentation {
	private final EObject definingContainer;
	
	public ImplicitVariableRepresentation(EObject definingContainer) {
		this.definingContainer = definingContainer;
	}
	
	public EObject getDefiningContainer() {
		return definingContainer;
	}

	@Override
	public int hashCode() {
		return Objects.hash(definingContainer);
	}
	@Override
	public boolean equals(Object object) {
		if (object == this) return true;
        if (this.getClass() != object.getClass()) return false;

        ImplicitVariableRepresentation other = (ImplicitVariableRepresentation) object;
        return Objects.equals(definingContainer, other.definingContainer);
	}
}
