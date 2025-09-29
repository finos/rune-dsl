package com.regnosys.rosetta.types;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;

import com.google.common.collect.Streams;

public class RMetaAnnotatedType {
    private static final Set<String> TYPE_META_NAMES = Set.of("key", "template");
    
	private final RType rType;
	private final List<RMetaAttribute> metaAttributes;
	
	protected RMetaAnnotatedType(RType rType, List<RMetaAttribute> metaAttributes) {
		this.rType = rType;
		this.metaAttributes = Validate.noNullElements(metaAttributes);
	}

	public static RMetaAnnotatedType withNoMeta(RType rType) {
		return new RMetaAnnotatedType(rType, List.of());
	}
	
	public static RMetaAnnotatedType withMeta(RType rType, List<RMetaAttribute> metaAttributes) {
		return new RMetaAnnotatedType(rType, metaAttributes);
	}
	
	public RMetaAnnotatedType addMeta(List<RMetaAttribute> metaAttributes) {
		List<RMetaAttribute> metas = Streams.concat(this.metaAttributes.stream(), metaAttributes.stream()).collect(Collectors.toList());
		return new RMetaAnnotatedType(rType, metas);
	}

	public RType getRType() {
		return rType;
	}

	public boolean hasAttributeMeta() {
		return metaAttributes.stream().anyMatch(x -> !TYPE_META_NAMES.contains(x.getName()));
	}
	
	public boolean hasTypeMeta() {
		return metaAttributes.stream().anyMatch(x -> TYPE_META_NAMES.contains(x.getName()));
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
