package com.regnosys.rosetta.types;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;

public class RMetaAnnotatedType {
	private final RType rType;
	private final List<RMetaAttribute> metaAttributes;
	
	private RMetaAnnotatedType(RType rType, List<RMetaAttribute> metaAttributes) {
		this.rType = rType;
		this.metaAttributes = Validate.noNullElements(metaAttributes);
	}

	public static RMetaAnnotatedType withNoMeta(RType rType) {
		return new RMetaAnnotatedType(rType, List.of());
	}
	
	public static RMetaAnnotatedType withMeta(RType rType, List<RMetaAttribute> metaAttributes) {
		return new RMetaAnnotatedType(rType, metaAttributes);
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
	
	public boolean hasMetaAttribute(String name) {
		return metaAttributes.stream().anyMatch(m -> m.getName().equals(name));
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
