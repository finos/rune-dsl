package com.rosetta.util.types;

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.Validate;

public class JavaWildcardTypeArgument implements JavaTypeArgument {
	/*
	 * If extendsBound is `true`, represents `? extends T`
	 * If extendsBound is `false`, represents `? super T`
	 */
	private final boolean extendsBound;
	private final Optional<JavaReferenceType> bound;
	
	protected JavaWildcardTypeArgument() {
		this.extendsBound = false; // the value of `extendsBound` does not matter in this case.
		this.bound = Optional.empty();
	}
	protected JavaWildcardTypeArgument(boolean extendsBound, JavaReferenceType bound) {
		Validate.notNull(bound);
		this.extendsBound = extendsBound;
		this.bound = Optional.of(bound);
	}
	
	public static JavaWildcardTypeArgument unbounded() {
		return new JavaWildcardTypeArgument();
	}
	public static JavaWildcardTypeArgument extendsBound(JavaReferenceType bound) {
		return new JavaWildcardTypeArgument(true, bound);
	}
	public static JavaWildcardTypeArgument superBound(JavaReferenceType bound) {
		return new JavaWildcardTypeArgument(false, bound);
	}
	
	public boolean hasExtendsBound() {
		return bound.isPresent() && extendsBound;
	}
	public boolean hasSuperBound() {
		return bound.isPresent() && !extendsBound;
	}
	
	public Optional<JavaReferenceType> getBound() {
		return bound;
	}
	
	@Override
	public String toString() {
		StringBuilder target = new StringBuilder();
		target.append("?");
		if (hasExtendsBound()) {
			target.append(" extends ");
			target.append(bound.get());
		}
		if (hasSuperBound()) {
			target.append(" super ");
			target.append(bound.get());
		}
		return target.toString();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(extendsBound, bound);
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == null) return false;
        if (this.getClass() != object.getClass()) return false;

        JavaWildcardTypeArgument other = (JavaWildcardTypeArgument) object;
        return extendsBound == other.extendsBound
        		&& Objects.equals(bound, other.bound);
	}
	@Override
	public void accept(JavaTypeArgumentVisitor visitor) {
		visitor.visitTypeArgument(this);
	}
}
