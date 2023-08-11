package com.regnosys.rosetta.types;

import java.util.Objects;

import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.rosetta.util.DottedPath;

public class REnumType extends RAnnotateType {
	private final RosettaEnumeration enumeration;

	public REnumType(final RosettaEnumeration enumeration) {
		super();
		this.enumeration = enumeration;
	}

	@Override
	public String getName() {
		return this.enumeration.getName();
	}
	
	@Override
	public DottedPath getNamespace() {
		return DottedPath.splitOnDots(enumeration.getModel().getName());
	}


	public RosettaEnumeration getEnumeration() {
		return this.enumeration;
	}

	@Override
	public int hashCode() {
		return 31 * 1 + ((this.enumeration == null) ? 0 : this.enumeration.hashCode());
	}

	@Override
	public boolean equals(final Object object) {
		if (object == null) return false;
        if (this.getClass() != object.getClass()) return false;
        
        REnumType other = (REnumType)object;
        return Objects.equals(enumeration, other.enumeration);
	}
}
