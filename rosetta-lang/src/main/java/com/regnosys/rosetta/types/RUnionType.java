package com.regnosys.rosetta.types;

import java.util.List;
import java.util.Objects;

import com.rosetta.model.lib.ModelSymbolId;

/**
 * An `RType` representing an unknown join of multiple types.
 * A Rune expression should never have this type - it is purely internal to Rune's type system,
 * so code generators can ignore it.
 */
public class RUnionType extends RType {
	private final List<RType> types;
	
	public RUnionType(List<RType> types) {
		this.types = types;
	}
	public RUnionType(RType... types) {
		this(List.of(types));
	}
	
	public List<RType> getTypes() {
		return types;
	}
	
	@Override
	public ModelSymbolId getSymbolId() {
		return null;
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder("one of ");
		for (int i = 0; i<types.size(); i++) {
			result.append(types.get(i).toString());
			if (i < types.size()-1) {
				result.append(", ");
			}
		}
		return result.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(types);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RUnionType other = (RUnionType) obj;
		return Objects.equals(types, other.types);
	}
}
