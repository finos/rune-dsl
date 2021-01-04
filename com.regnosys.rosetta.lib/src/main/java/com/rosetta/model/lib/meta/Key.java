package com.rosetta.model.lib.meta;

/**
 * @author TomForwood
 * This class represents a value that can be references elsewhere to link to the object the key is associated with
 * The keyValue is required to be unique within the scope defined by "scope"
 * 
 * Scope can be 
 *  - global - the key must be universally unique
 * 	- document - the key must be unique in this document
 *  - the name of the rosetta class e.g. TradeableProduct- the object bearing this key is inside a TradeableProduct and the key is only unique inside that TradeableProduct
 */
public class Key {
	private final String scope;
	private final String keyValue;
	public Key(String scope, String keyValue) {
		super();
		this.scope = scope;
		this.keyValue = keyValue;
	}
	public String getScope() {
		return scope;
	}
	public String getKeyValue() {
		return keyValue;
	}

	public KeyBuilder toBuilder() {
		KeyBuilder key = new KeyBuilder();
		key.setKeyValue(keyValue);
		key.setScope(scope);
		return key;
	}
	
	public static class KeyBuilder {
		private String scope;
		private String keyValue;
		
		public Key build() {
			return new Key(scope, keyValue);
		}

		public String getScope() {
			return scope;
		}

		public KeyBuilder setScope(String scope) {
			this.scope = scope;
			return this;
		}

		public String getKeyValue() {
			return keyValue;
		}

		public KeyBuilder setKeyValue(String keyValue) {
			this.keyValue = keyValue;
			return this;
		}
		
		public boolean hasData() {
			return keyValue!=null;
		}
	}
}
