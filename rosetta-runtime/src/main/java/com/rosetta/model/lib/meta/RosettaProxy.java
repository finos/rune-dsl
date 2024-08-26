package com.rosetta.model.lib.meta;

public interface RosettaProxy<T> {
	String getKey();
	T getInstance();
	boolean isOriginal();
}
