package com.rosetta.util.types;

public interface JavaReferenceType extends JavaType, JavaTypeArgument {
	@Override
	default JavaReferenceType toReferenceType() {
		return this;
	}
	@Override
	default void accept(JavaTypeArgumentVisitor visitor) {
		visitor.visitTypeArgument(this);
	}
}
