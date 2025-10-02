package com.regnosys.rosetta.types;

import java.util.List;
import java.util.Objects;

/**
 * The result of stripping an RType of all type aliases, while tracking the aliases that were stripped.
 * 
 * It has two fields: the underlying type, which is guaranteed not to be an alias, and a list of aliases that were
 * followed to compute the underlying type, in order. If the original type was not an alias, it will equal the underlying type
 * and the alias hierarchy will be empty.
 */
public class AliasHierarchy {
	private final RType underlyingType;
	private final List<RAliasType> aliases;
	
	public AliasHierarchy(RType underlyingType, List<RAliasType> aliases) {
		this.underlyingType = underlyingType;
		this.aliases = aliases;
	}

	public RType getUnderlyingType() {
		return underlyingType;
	}

	public List<RAliasType> getAliases() {
		return aliases;
	}

	@Override
	public int hashCode() {
		return Objects.hash(aliases, underlyingType);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AliasHierarchy other = (AliasHierarchy) obj;
		return Objects.equals(aliases, other.aliases)
				&& Objects.equals(underlyingType, other.underlyingType);
	}
}
