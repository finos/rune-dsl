package com.regnosys.rosetta.types;

import java.util.Objects;

import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.rosetta.model.lib.ModelSymbolId;
import com.rosetta.util.DottedPath;

public class REnumType extends RAnnotateType {
	private final RosettaEnumeration enumeration;
	private final ModelSymbolId symbolId;

	public REnumType(final RosettaEnumeration enumeration) {
		super();
		this.enumeration = enumeration;
		this.symbolId = new ModelSymbolId(
				DottedPath.splitOnDots(enumeration.getModel().getName()),
				enumeration.getName()
			);
	}
	
	@Override
	public ModelSymbolId getSymbolId() {
		return this.symbolId;
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
