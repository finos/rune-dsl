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
@RosettaDataType(value="FieldWithMetaBoolean", builder=FieldWithMetaBoolean.FieldWithMetaBooleanBuilderImpl.class, version="0.0.0")
public interface FieldWithMetaBoolean extends RosettaModelObject, FieldWithMeta<Boolean>, GlobalKey {

	FieldWithMetaBooleanMeta metaData = new FieldWithMetaBooleanMeta();

	/*********************** Getter Methods  ***********************/
	Boolean getValue();
	MetaFields getMeta();

	/*********************** Build Methods  ***********************/
	FieldWithMetaBoolean build();
	
	FieldWithMetaBoolean.FieldWithMetaBooleanBuilder toBuilder();
	
	static FieldWithMetaBoolean.FieldWithMetaBooleanBuilder builder() {
		return new FieldWithMetaBoolean.FieldWithMetaBooleanBuilderImpl();
	}

	/*********************** Utility Methods  ***********************/
	@Override
	default RosettaMetaData<? extends FieldWithMetaBoolean> metaData() {
		return metaData;
	}
	
	@Override
	default Class<? extends FieldWithMetaBoolean> getType() {
		return FieldWithMetaBoolean.class;
	}
	
	@Override
	default Class<Boolean> getValueType() {
		return Boolean.class;
	}
	
	@Override
	default void process(RosettaPath path, Processor processor) {
		processor.processBasic(path.newSubPath("value"), Boolean.class, getValue(), this);
		processRosetta(path.newSubPath("meta"), processor, MetaFields.class, getMeta());
	}
	

	/*********************** Builder Interface  ***********************/
	interface FieldWithMetaBooleanBuilder extends FieldWithMetaBoolean, RosettaModelObjectBuilder, GlobalKey.GlobalKeyBuilder, FieldWithMeta.FieldWithMetaBuilder<Boolean> {
		MetaFields.MetaFieldsBuilder getOrCreateMeta();
		MetaFields.MetaFieldsBuilder getMeta();
		FieldWithMetaBoolean.FieldWithMetaBooleanBuilder setValue(Boolean value);
		FieldWithMetaBoolean.FieldWithMetaBooleanBuilder setMeta(MetaFields meta);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processor.processBasic(path.newSubPath("value"), Boolean.class, getValue(), this);
			processRosetta(path.newSubPath("meta"), processor, MetaFields.MetaFieldsBuilder.class, getMeta());
		}
		

		FieldWithMetaBoolean.FieldWithMetaBooleanBuilder prune();
	}

	/*********************** Immutable Implementation of FieldWithMetaBoolean  ***********************/
	class FieldWithMetaBooleanImpl implements FieldWithMetaBoolean {
		private final Boolean value;
		private final MetaFields meta;
		
		protected FieldWithMetaBooleanImpl(FieldWithMetaBoolean.FieldWithMetaBooleanBuilder builder) {
			this.value = builder.getValue();
			this.meta = ofNullable(builder.getMeta()).map(f->f.build()).orElse(null);
		}
		
		@Override
		@RosettaAttribute("value")
		public Boolean getValue() {
			return value;
		}
		
		@Override
		@RosettaAttribute("meta")
		public MetaFields getMeta() {
			return meta;
		}
		
		@Override
		public FieldWithMetaBoolean build() {
			return this;
		}
		
		@Override
		public FieldWithMetaBoolean.FieldWithMetaBooleanBuilder toBuilder() {
			FieldWithMetaBoolean.FieldWithMetaBooleanBuilder builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(FieldWithMetaBoolean.FieldWithMetaBooleanBuilder builder) {
			ofNullable(getValue()).ifPresent(builder::setValue);
			ofNullable(getMeta()).ifPresent(builder::setMeta);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			FieldWithMetaBoolean _that = getType().cast(o);
		
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
			return "FieldWithMetaBoolean {" +
				"value=" + this.value + ", " +
				"meta=" + this.meta +
			'}';
		}
	}

	/*********************** Builder Implementation of FieldWithMetaBoolean  ***********************/
	class FieldWithMetaBooleanBuilderImpl implements FieldWithMetaBoolean.FieldWithMetaBooleanBuilder {
	
		protected Boolean value;
		protected MetaFields.MetaFieldsBuilder meta;
	
		public FieldWithMetaBooleanBuilderImpl() {
		}
	
		@Override
		@RosettaAttribute("value")
		public Boolean getValue() {
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
		public FieldWithMetaBoolean.FieldWithMetaBooleanBuilder setValue(Boolean value) {
			this.value = value==null?null:value;
			return this;
		}
		@Override
		@RosettaAttribute("meta")
		public FieldWithMetaBoolean.FieldWithMetaBooleanBuilder setMeta(MetaFields meta) {
			this.meta = meta==null?null:meta.toBuilder();
			return this;
		}
		
		@Override
		public FieldWithMetaBoolean build() {
			return new FieldWithMetaBoolean.FieldWithMetaBooleanImpl(this);
		}
		
		@Override
		public FieldWithMetaBoolean.FieldWithMetaBooleanBuilder toBuilder() {
			return this;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public FieldWithMetaBoolean.FieldWithMetaBooleanBuilder prune() {
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
		public FieldWithMetaBoolean.FieldWithMetaBooleanBuilder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
			FieldWithMetaBoolean.FieldWithMetaBooleanBuilder o = (FieldWithMetaBoolean.FieldWithMetaBooleanBuilder) other;
			
			merger.mergeRosetta(getMeta(), o.getMeta(), this::setMeta);
			
			merger.mergeBasic(getValue(), o.getValue(), this::setValue);
			return this;
		}
	
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			FieldWithMetaBoolean _that = getType().cast(o);
		
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
			return "FieldWithMetaBooleanBuilder {" +
				"value=" + this.value + ", " +
				"meta=" + this.meta +
			'}';
		}
	}
}

class FieldWithMetaBooleanMeta extends BasicRosettaMetaData<FieldWithMetaBoolean>{

}
