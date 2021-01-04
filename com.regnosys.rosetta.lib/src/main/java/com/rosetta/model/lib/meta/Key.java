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
public interface Key {

	public String getScope();
	public String getKeyValue();
	
	Key build();
	KeyBuilder toBuilder();
	
	static KeyBuilder newBuilder() {
		return new KeyBuilderImpl();
	}
	
	interface KeyBuilder extends Key {
		KeyBuilder setScope(String scope);
		KeyBuilder setKeyValue(String keyValue);
	}
	
	class KeyImpl implements Key {
		
		private final String scope;
		private final String keyValue;
		public KeyImpl(KeyBuilder builder) {
			super();
			this.scope = builder.getScope();
			this.keyValue = builder.getKeyValue();
		}
		public String getScope() {
			return scope;
		}
		public String getKeyValue() {
			return keyValue;
		}
	
		public KeyBuilder toBuilder() {
			KeyBuilder key = newBuilder();
			key.setKeyValue(keyValue);
			key.setScope(scope);
			return key;
		}
		
		public Key build() {
			return this;
		}
	}
	
	public static class KeyBuilderImpl implements KeyBuilder{
		private String scope;
		private String keyValue;
		
		public Key build() {
			return new KeyImpl(this);
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

		@Override
		public KeyBuilder toBuilder() {
			return this;
		}
	}
}
