package com.rosetta.model.lib.meta;

import java.util.Objects;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.process.AttributeMeta;
import com.rosetta.model.lib.process.BuilderMerger;
import com.rosetta.model.lib.process.BuilderProcessor;
import com.rosetta.model.lib.process.Processor;

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
public class Key extends RosettaModelObject {
	
	private final String scope;
	private final String keyValue;
	
	public Key(String scope, String keyValue) {
		this.scope = scope;
		this.keyValue = keyValue;
	}
	

	@Override
	public RosettaMetaData<? extends RosettaModelObject> metaData() {
		return null;
	}
	
	public String getScope() {
		return scope;
	}
	
	public String getKeyValue() {
		return keyValue;
	}

	@Override
	public KeyBuilder toBuilder() {
		KeyBuilder key = new KeyBuilder();
		key.setKeyValue(keyValue);
		key.setScope(scope);
		return key;
	}

	public static KeyBuilder builder() {
		return new KeyBuilder();
	}
	
	@Override 
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Key key = (Key) o;
		return Objects.equals(scope, key.scope) && Objects.equals(keyValue, key.keyValue);
	}

	@Override 
	public int hashCode() {
		return Objects.hash(scope, keyValue);
	}

	@Override 
	public String toString() {
		return "Key{" +
				"scope='" + scope + '\'' +
				", keyValue='" + keyValue + '\'' +
				'}';
	}
	

	@Override
	protected void process(RosettaPath path, Processor processor) {
		// do nothing
	}


	public static class KeyBuilder extends RosettaModelObjectBuilder {
		
		private String scope;
		private String keyValue;

		@Override
		public RosettaMetaData<? extends RosettaModelObject> metaData() {
			return null;
		}
		
		@Override
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

		@Override
		public boolean hasData() {
			return keyValue!=null;
		}

		@Override 
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			KeyBuilder that = (KeyBuilder) o;
			return Objects.equals(scope, that.scope) && Objects.equals(keyValue, that.keyValue);
		}

		@Override 
		public int hashCode() {
			return Objects.hash(scope, keyValue);
		}

		@Override 
		public String toString() {
			return "KeyBuilder{" +
					"scope='" + scope + '\'' +
					", keyValue='" + keyValue + '\'' +
					'}';
		}

		@Override
		public KeyBuilder prune() {
			return this;
		}

		

		@Override
		public void process(RosettaPath path, BuilderProcessor processor) {
			// do nothing
		}

		@Override
		public KeyBuilder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
			KeyBuilder o = (KeyBuilder) other;
			merger.mergeBasic(getScope(), o.getScope(), this::setScope, AttributeMeta.META, AttributeMeta.GLOBAL_KEY);
			merger.mergeBasic(getKeyValue(), o.getKeyValue(), this::setKeyValue, AttributeMeta.META, AttributeMeta.GLOBAL_KEY);
			return this;
		}
	}
}
