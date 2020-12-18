package com.rosetta.model.lib.meta;

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
	}
}
