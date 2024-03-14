package com.rosetta.model.metafields;

import com.rosetta.model.lib.GlobalKey;
import com.rosetta.model.lib.GlobalKey.GlobalKeyBuilder;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.annotations.RosettaAttribute;
import com.rosetta.model.lib.annotations.RosettaDataType;
import com.rosetta.model.lib.meta.BasicRosettaMetaData;
import com.rosetta.model.lib.meta.FieldWithMeta;
import com.rosetta.model.lib.meta.FieldWithMeta.FieldWithMetaBuilder;
import com.rosetta.model.lib.meta.RosettaMetaData;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.process.BuilderMerger;
import com.rosetta.model.lib.process.BuilderProcessor;
import com.rosetta.model.lib.process.Processor;
import java.math.BigInteger;
import java.util.Objects;

import static java.util.Optional.ofNullable;

/**
 * @version 1
 */
@RosettaDataType(value="FieldWithMetaBigInteger", builder=FieldWithMetaBigInteger.FieldWithMetaBigIntegerBuilderImpl.class, version="0.0.0")
public interface FieldWithMetaBigInteger extends RosettaModelObject, FieldWithMeta<BigInteger>, GlobalKey {

	FieldWithMetaBigIntegerMeta metaData = new FieldWithMetaBigIntegerMeta();

	/*********************** Getter Methods  ***********************/
	BigInteger getValue();
	MetaFields getMeta();

	/*********************** Build Methods  ***********************/
	FieldWithMetaBigInteger build();
	
	FieldWithMetaBigInteger.FieldWithMetaBigIntegerBuilder toBuilder();
	
	static FieldWithMetaBigInteger.FieldWithMetaBigIntegerBuilder builder() {
		return new FieldWithMetaBigInteger.FieldWithMetaBigIntegerBuilderImpl();
	}

	/*********************** Utility Methods  ***********************/
	@Override
	default RosettaMetaData<? extends FieldWithMetaBigInteger> metaData() {
		return metaData;
	}
	
	@Override
	default Class<? extends FieldWithMetaBigInteger> getType() {
		return FieldWithMetaBigInteger.class;
	}
	
	@Override
	default Class<BigInteger> getValueType() {
		return BigInteger.class;
	}
	
	@Override
	default void process(RosettaPath path, Processor processor) {
		processor.processBasic(path.newSubPath("value"), BigInteger.class, getValue(), this);
		processRosetta(path.newSubPath("meta"), processor, MetaFields.class, getMeta());
	}
	

	/*********************** Builder Interface  ***********************/
	interface FieldWithMetaBigIntegerBuilder extends FieldWithMetaBigInteger, RosettaModelObjectBuilder, GlobalKey.GlobalKeyBuilder, FieldWithMeta.FieldWithMetaBuilder<BigInteger> {
		MetaFields.MetaFieldsBuilder getOrCreateMeta();
		MetaFields.MetaFieldsBuilder getMeta();
		FieldWithMetaBigInteger.FieldWithMetaBigIntegerBuilder setValue(BigInteger value);
		FieldWithMetaBigInteger.FieldWithMetaBigIntegerBuilder setMeta(MetaFields meta);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processor.processBasic(path.newSubPath("value"), BigInteger.class, getValue(), this);
			processRosetta(path.newSubPath("meta"), processor, MetaFields.MetaFieldsBuilder.class, getMeta());
		}
		

		FieldWithMetaBigInteger.FieldWithMetaBigIntegerBuilder prune();
	}

	/*********************** Immutable Implementation of FieldWithMetaBigInteger  ***********************/
	class FieldWithMetaBigIntegerImpl implements FieldWithMetaBigInteger {
		private final BigInteger value;
		private final MetaFields meta;
		
		protected FieldWithMetaBigIntegerImpl(FieldWithMetaBigInteger.FieldWithMetaBigIntegerBuilder builder) {
			this.value = builder.getValue();
			this.meta = ofNullable(builder.getMeta()).map(f->f.build()).orElse(null);
		}
		
		@Override
		@RosettaAttribute("value")
		public BigInteger getValue() {
			return value;
		}
		
		@Override
		@RosettaAttribute("meta")
		public MetaFields getMeta() {
			return meta;
		}
		
		@Override
		public FieldWithMetaBigInteger build() {
			return this;
		}
		
		@Override
		public FieldWithMetaBigInteger.FieldWithMetaBigIntegerBuilder toBuilder() {
			FieldWithMetaBigInteger.FieldWithMetaBigIntegerBuilder builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(FieldWithMetaBigInteger.FieldWithMetaBigIntegerBuilder builder) {
			ofNullable(getValue()).ifPresent(builder::setValue);
			ofNullable(getMeta()).ifPresent(builder::setMeta);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			FieldWithMetaBigInteger _that = getType().cast(o);
		
			if (!Objects.equals(value, _that.getValue())) return false;
			if (!Objects.equals(meta, _that.getMeta())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (value != null ? value.hashCode() : 0);
			_result = 31 * _result + (meta != null ? meta.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "FieldWithMetaBigInteger {" +
				"value=" + this.value + ", " +
				"meta=" + this.meta +
			'}';
		}
	}

	/*********************** Builder Implementation of FieldWithMetaBigInteger  ***********************/
	class FieldWithMetaBigIntegerBuilderImpl implements FieldWithMetaBigInteger.FieldWithMetaBigIntegerBuilder {
	
		protected BigInteger value;
		protected MetaFields.MetaFieldsBuilder meta;
	
		public FieldWithMetaBigIntegerBuilderImpl() {
		}
	
		@Override
		@RosettaAttribute("value")
		public BigInteger getValue() {
			return value;
		}
		
		@Override
		@RosettaAttribute("meta")
		public MetaFields.MetaFieldsBuilder getMeta() {
			return meta;
		}
		
		@Override
		public MetaFields.MetaFieldsBuilder getOrCreateMeta() {
			MetaFields.MetaFieldsBuilder result;
			if (meta!=null) {
				result = meta;
			}
			else {
				result = meta = MetaFields.builder();
			}
			
			return result;
		}
	
		@Override
		@RosettaAttribute("value")
		public FieldWithMetaBigInteger.FieldWithMetaBigIntegerBuilder setValue(BigInteger value) {
			this.value = value==null?null:value;
			return this;
		}
		@Override
		@RosettaAttribute("meta")
		public FieldWithMetaBigInteger.FieldWithMetaBigIntegerBuilder setMeta(MetaFields meta) {
			this.meta = meta==null?null:meta.toBuilder();
			return this;
		}
		
		@Override
		public FieldWithMetaBigInteger build() {
			return new FieldWithMetaBigInteger.FieldWithMetaBigIntegerImpl(this);
		}
		
		@Override
		public FieldWithMetaBigInteger.FieldWithMetaBigIntegerBuilder toBuilder() {
			return this;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public FieldWithMetaBigInteger.FieldWithMetaBigIntegerBuilder prune() {
			if (meta!=null && !meta.prune().hasData()) meta = null;
			return this;
		}
		
		@Override
		public boolean hasData() {
			if (getValue()!=null) return true;
			return false;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public FieldWithMetaBigInteger.FieldWithMetaBigIntegerBuilder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
			FieldWithMetaBigInteger.FieldWithMetaBigIntegerBuilder o = (FieldWithMetaBigInteger.FieldWithMetaBigIntegerBuilder) other;
			
			merger.mergeRosetta(getMeta(), o.getMeta(), this::setMeta);
			
			merger.mergeBasic(getValue(), o.getValue(), this::setValue);
			return this;
		}
	
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			FieldWithMetaBigInteger _that = getType().cast(o);
		
			if (!Objects.equals(value, _that.getValue())) return false;
			if (!Objects.equals(meta, _that.getMeta())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (value != null ? value.hashCode() : 0);
			_result = 31 * _result + (meta != null ? meta.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "FieldWithMetaBigIntegerBuilder {" +
				"value=" + this.value + ", " +
				"meta=" + this.meta +
			'}';
		}
	}
}

class FieldWithMetaBigIntegerMeta extends BasicRosettaMetaData<FieldWithMetaBigInteger>{

}
