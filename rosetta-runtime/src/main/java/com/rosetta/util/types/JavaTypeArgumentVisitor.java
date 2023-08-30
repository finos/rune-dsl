package com.rosetta.util.types;

public interface JavaTypeArgumentVisitor {
	void visitTypeArgument(JavaWildcardTypeArgument arg);
	void visitTypeArgument(JavaReferenceType arg);
}
