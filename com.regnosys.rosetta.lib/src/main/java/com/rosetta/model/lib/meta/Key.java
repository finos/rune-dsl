package com.rosetta.model.lib.meta;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.process.BuilderMerger;
import com.rosetta.model.lib.process.Processor;
import com.rosetta.model.lib.qualify.QualifyFunctionFactory;
import com.rosetta.model.lib.qualify.QualifyResult;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import com.rosetta.model.lib.validation.ValidatorFactory;
import com.rosetta.model.lib.validation.ValidatorWithArg;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import com.rosetta.model.lib.validation.ValidationResult.ValidationType;

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
public interface Key extends RosettaModelObject{

	public String getScope();
	public String getKeyValue();
	
	Key build();
	KeyBuilder toBuilder();
	
	final static KeyMeta meta = new KeyMeta();
	@Override
	default RosettaMetaData<? extends RosettaModelObject> metaData() {
		return meta;
	}
	
	default Class<? extends RosettaModelObject> getType() {
		return Key.class;
	}
	
	default void process(RosettaPath path, Processor processor) {
	}
	
	static KeyBuilder newBuilder() {
		return new KeyBuilderImpl();
	}
	
	interface KeyBuilder extends Key, RosettaModelObjectBuilder {
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

		@SuppressWarnings("unchecked")
		@Override
		public KeyBuilder prune() {
			return this;
		}

		@SuppressWarnings("unchecked")
		@Override
		public KeyBuilder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
			KeyBuilder otherKey = (KeyBuilder) other;
			merger.mergeBasic(getKeyValue(), otherKey.getKeyValue(), this::setKeyValue);
			merger.mergeBasic(getScope(), otherKey.getScope(), this::setScope);
			return this;
		}
	}
	
	class KeyMeta implements RosettaMetaData<Key> {


		@Override
		public List<Validator<? super Key>> dataRules(ValidatorFactory factory) {
			return Collections.emptyList();
		}

		@Override
		public List<Validator<? super Key>> choiceRuleValidators() {
			return Collections.emptyList();
		}

		@Override
		public List<Function<? super Key, QualifyResult>> getQualifyFunctions(QualifyFunctionFactory factory) {
			return Collections.emptyList();
		}

		@Override
		public Validator<? super Key> validator() {
			return new Validator<Key>() {

				@Override
				public ValidationResult<Key> validate(RosettaPath path, Key key) {
					if (key.getKeyValue()==null) {
						return ValidationResult.failure("Key.value",ValidationType.MODEL_INSTANCE, "Key", path, "", "Key value must be set");
					}
					if (key.getScope()==null) {
						return ValidationResult.failure("Key.scope",ValidationType.MODEL_INSTANCE, "Key", path, "", "Key scope must be set");
					}
					return ValidationResult.success("Key", ValidationType.MODEL_INSTANCE, "Key", path, "");
				}
			};
		}

		@Override
		public ValidatorWithArg<? super Key, String> onlyExistsValidator() {
			return null;
		}
	}
}
