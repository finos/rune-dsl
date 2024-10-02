package com.regnosys.rosetta.types;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class RMetaAnnotatedType {
	private final RType rType;
	private final List<RMetaAttribute> metaAttributes;
	
	public RMetaAnnotatedType(RType rType, List<RMetaAttribute> metaAttributes) {
		super();
		this.rType = rType;
		this.metaAttributes = metaAttributes == null ? List.of() : metaAttributes;
	}

	public RType getRType() {
		return rType;
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
		return Objects.hash(rType, metaAttributes);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RMetaAnnotatedType other = (RMetaAnnotatedType) obj;
		return Objects.equals(rType, other.rType) && Objects.equals(metaAttributes, other.metaAttributes);
	}

	@Override
	public String toString() {
		if (metaAttributes.isEmpty()) {
			return rType.toString();
		}
		return rType.toString() + " with " + metaAttributes.stream().map(RMetaAttribute::getName).collect(Collectors.joining(", "));
	}

}
