package com.rosetta.util.types;

public interface JavaReferenceType extends JavaType, JavaTypeArgument {
	@Override
	default void accept(JavaTypeArgumentVisitor visitor) {
		visitor.visitTypeArgument(this);
	}
}
