package com.regnosys.rosetta.types;

import java.util.List;
import java.util.Objects;

public class RAnnotatedType {
	private final RType baseType;
	private final List<RMetaAttribute> metaAttributes;
	
	public RAnnotatedType(RType baseType, List<RMetaAttribute> metaAttributes) {
		super();
		this.baseType = baseType;
		this.metaAttributes = metaAttributes;
	}

	public RType getBaseType() {
		return baseType;
	}

	public boolean hasMeta() {
		return !metaAttributes.isEmpty();
	}	
	
	public List<RMetaAttribute> getMetaAttributes() {
		return metaAttributes;
	}
	
	@Deprecated
	public boolean hasReferenceOrAddressMetadata() {
		return metaAttributes.stream().anyMatch(a -> a.getName().equals("reference") || a.getName().equals("address"));
	}

	@Override
	public int hashCode() {
		return Objects.hash(baseType, metaAttributes);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RAnnotatedType other = (RAnnotatedType) obj;
		return Objects.equals(baseType, other.baseType) && Objects.equals(metaAttributes, other.metaAttributes);
	}

	@Override
	public String toString() {
		return "RAnnotatedType [baseType=" + baseType.getName() + ", metaAttributes=" + metaAttributes + "]";
	}

}
