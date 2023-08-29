package com.rosetta.util.types;

public interface JavaTypeArgument {
	public void accept(JavaTypeArgumentVisitor visitor);
}
