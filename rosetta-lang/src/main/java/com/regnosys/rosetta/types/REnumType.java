package com.regnosys.rosetta.types;

import java.util.Optional;

import org.eclipse.xtext.resource.XtextResource;

import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.rosetta.RosettaType;

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

	public RosettaEnumeration getEnumeration() {
		return this.enumeration;
	}

	@Override
	public int hashCode() {
		return 31 * 1 + ((this.enumeration == null) ? 0 : this.enumeration.hashCode());
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		REnumType other = (REnumType) obj;
		if (this.enumeration == null) {
			if (other.enumeration != null)
				return false;
		} else if (!this.enumeration.equals(other.enumeration))
			return false;
		return true;
	}
}
