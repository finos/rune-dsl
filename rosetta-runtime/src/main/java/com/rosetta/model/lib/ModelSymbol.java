package com.rosetta.model.lib;

import com.rosetta.util.DottedPath;

public interface ModelSymbol {
	
	ModelSymbolId getSymbolId();
	
	default DottedPath getNamespace() {
		return getSymbolId().getNamespace();
	}
	default String getName() {
		return getSymbolId().getName();
	}
	default DottedPath getQualifiedName() {
		return getSymbolId().getQualifiedName();
	}
	
	public static abstract class AbstractModelSymbol extends ModelSymbolId implements ModelSymbol {
		public AbstractModelSymbol(DottedPath namespace, String name) {
			super(namespace, name);
		}

		@Override
		public ModelSymbolId getSymbolId() {
			return new ModelSymbolId(getNamespace(), getName());
		}
	}
}
