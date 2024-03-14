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
import java.util.Objects;

import static java.util.Optional.ofNullable;

/**
 * @version 1
 */
@RosettaDataType(value="FieldWithMetaInteger", builder=FieldWithMetaInteger.FieldWithMetaIntegerBuilderImpl.class, version="0.0.0")
public interface FieldWithMetaInteger extends RosettaModelObject, FieldWithMeta<Integer>, GlobalKey {

	FieldWithMetaIntegerMeta metaData = new FieldWithMetaIntegerMeta();

	/*********************** Getter Methods  ***********************/
	Integer getValue();
	MetaFields getMeta();

	/*********************** Build Methods  ***********************/
	FieldWithMetaInteger build();
	
	FieldWithMetaInteger.FieldWithMetaIntegerBuilder toBuilder();
	
	static FieldWithMetaInteger.FieldWithMetaIntegerBuilder builder() {
		return new FieldWithMetaInteger.FieldWithMetaIntegerBuilderImpl();
	}

	/*********************** Utility Methods  ***********************/
	@Override
	default RosettaMetaData<? extends FieldWithMetaInteger> metaData() {
		return metaData;
	}
	
	@Override
	default Class<? extends FieldWithMetaInteger> getType() {
		return FieldWithMetaInteger.class;
	}
	
	@Override
	default Class<Integer> getValueType() {
		return Integer.class;
	}
	
	@Override
	default void process(RosettaPath path, Processor processor) {
		processor.processBasic(path.newSubPath("value"), Integer.class, getValue(), this);
		processRosetta(path.newSubPath("meta"), processor, MetaFields.class, getMeta());
	}
	

	/*********************** Builder Interface  ***********************/
	interface FieldWithMetaIntegerBuilder extends FieldWithMetaInteger, RosettaModelObjectBuilder, GlobalKey.GlobalKeyBuilder, FieldWithMeta.FieldWithMetaBuilder<Integer> {
		MetaFields.MetaFieldsBuilder getOrCreateMeta();
		MetaFields.MetaFieldsBuilder getMeta();
		FieldWithMetaInteger.FieldWithMetaIntegerBuilder setValue(Integer value);
		FieldWithMetaInteger.FieldWithMetaIntegerBuilder setMeta(MetaFields meta);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processor.processBasic(path.newSubPath("value"), Integer.class, getValue(), this);
			processRosetta(path.newSubPath("meta"), processor, MetaFields.MetaFieldsBuilder.class, getMeta());
		}
		

		FieldWithMetaInteger.FieldWithMetaIntegerBuilder prune();
	}

	/*********************** Immutable Implementation of FieldWithMetaInteger  ***********************/
	class FieldWithMetaIntegerImpl implements FieldWithMetaInteger {
		private final Integer value;
		private final MetaFields meta;
		
		protected FieldWithMetaIntegerImpl(FieldWithMetaInteger.FieldWithMetaIntegerBuilder builder) {
			this.value = builder.getValue();
			this.meta = ofNullable(builder.getMeta()).map(f->f.build()).orElse(null);
		}
		
		@Override
		@RosettaAttribute("value")
		public Integer getValue() {
			return value;
		}
		
		@Override
		@RosettaAttribute("meta")
		public MetaFields getMeta() {
			return meta;
		}
		
		@Override
		public FieldWithMetaInteger build() {
			return this;
		}
		
		@Override
		public FieldWithMetaInteger.FieldWithMetaIntegerBuilder toBuilder() {
			FieldWithMetaInteger.FieldWithMetaIntegerBuilder builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(FieldWithMetaInteger.FieldWithMetaIntegerBuilder builder) {
			ofNullable(getValue()).ifPresent(builder::setValue);
			ofNullable(getMeta()).ifPresent(builder::setMeta);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			FieldWithMetaInteger _that = getType().cast(o);
		
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
			return "FieldWithMetaInteger {" +
				"value=" + this.value + ", " +
				"meta=" + this.meta +
			'}';
		}
	}

	/*********************** Builder Implementation of FieldWithMetaInteger  ***********************/
	class FieldWithMetaIntegerBuilderImpl implements FieldWithMetaInteger.FieldWithMetaIntegerBuilder {
	
		protected Integer value;
		protected MetaFields.MetaFieldsBuilder meta;
	
		public FieldWithMetaIntegerBuilderImpl() {
		}
	
		@Override
		@RosettaAttribute("value")
		public Integer getValue() {
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
		public FieldWithMetaInteger.FieldWithMetaIntegerBuilder setValue(Integer value) {
			this.value = value==null?null:value;
			return this;
		}
		@Override
		@RosettaAttribute("meta")
		public FieldWithMetaInteger.FieldWithMetaIntegerBuilder setMeta(MetaFields meta) {
			this.meta = meta==null?null:meta.toBuilder();
			return this;
		}
		
		@Override
		public FieldWithMetaInteger build() {
			return new FieldWithMetaInteger.FieldWithMetaIntegerImpl(this);
		}
		
		@Override
		public FieldWithMetaInteger.FieldWithMetaIntegerBuilder toBuilder() {
			return this;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public FieldWithMetaInteger.FieldWithMetaIntegerBuilder prune() {
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
		public FieldWithMetaInteger.FieldWithMetaIntegerBuilder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
			FieldWithMetaInteger.FieldWithMetaIntegerBuilder o = (FieldWithMetaInteger.FieldWithMetaIntegerBuilder) other;
			
			merger.mergeRosetta(getMeta(), o.getMeta(), this::setMeta);
			
			merger.mergeBasic(getValue(), o.getValue(), this::setValue);
			return this;
		}
	
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			FieldWithMetaInteger _that = getType().cast(o);
		
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
			return "FieldWithMetaIntegerBuilder {" +
				"value=" + this.value + ", " +
				"meta=" + this.meta +
			'}';
		}
	}
}

class FieldWithMetaIntegerMeta extends BasicRosettaMetaData<FieldWithMetaInteger>{

}
