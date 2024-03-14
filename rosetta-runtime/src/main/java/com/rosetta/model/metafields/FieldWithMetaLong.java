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
@RosettaDataType(value="FieldWithMetaLong", builder=FieldWithMetaLong.FieldWithMetaLongBuilderImpl.class, version="0.0.0")
public interface FieldWithMetaLong extends RosettaModelObject, FieldWithMeta<Long>, GlobalKey {

	FieldWithMetaLongMeta metaData = new FieldWithMetaLongMeta();

	/*********************** Getter Methods  ***********************/
	Long getValue();
	MetaFields getMeta();

	/*********************** Build Methods  ***********************/
	FieldWithMetaLong build();
	
	FieldWithMetaLong.FieldWithMetaLongBuilder toBuilder();
	
	static FieldWithMetaLong.FieldWithMetaLongBuilder builder() {
		return new FieldWithMetaLong.FieldWithMetaLongBuilderImpl();
	}

	/*********************** Utility Methods  ***********************/
	@Override
	default RosettaMetaData<? extends FieldWithMetaLong> metaData() {
		return metaData;
	}
	
	@Override
	default Class<? extends FieldWithMetaLong> getType() {
		return FieldWithMetaLong.class;
	}
	
	@Override
	default Class<Long> getValueType() {
		return Long.class;
	}
	
	@Override
	default void process(RosettaPath path, Processor processor) {
		processor.processBasic(path.newSubPath("value"), Long.class, getValue(), this);
		processRosetta(path.newSubPath("meta"), processor, MetaFields.class, getMeta());
	}
	

	/*********************** Builder Interface  ***********************/
	interface FieldWithMetaLongBuilder extends FieldWithMetaLong, RosettaModelObjectBuilder, GlobalKey.GlobalKeyBuilder, FieldWithMeta.FieldWithMetaBuilder<Long> {
		MetaFields.MetaFieldsBuilder getOrCreateMeta();
		MetaFields.MetaFieldsBuilder getMeta();
		FieldWithMetaLong.FieldWithMetaLongBuilder setValue(Long value);
		FieldWithMetaLong.FieldWithMetaLongBuilder setMeta(MetaFields meta);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processor.processBasic(path.newSubPath("value"), Long.class, getValue(), this);
			processRosetta(path.newSubPath("meta"), processor, MetaFields.MetaFieldsBuilder.class, getMeta());
		}
		

		FieldWithMetaLong.FieldWithMetaLongBuilder prune();
	}

	/*********************** Immutable Implementation of FieldWithMetaLong  ***********************/
	class FieldWithMetaLongImpl implements FieldWithMetaLong {
		private final Long value;
		private final MetaFields meta;
		
		protected FieldWithMetaLongImpl(FieldWithMetaLong.FieldWithMetaLongBuilder builder) {
			this.value = builder.getValue();
			this.meta = ofNullable(builder.getMeta()).map(f->f.build()).orElse(null);
		}
		
		@Override
		@RosettaAttribute("value")
		public Long getValue() {
			return value;
		}
		
		@Override
		@RosettaAttribute("meta")
		public MetaFields getMeta() {
			return meta;
		}
		
		@Override
		public FieldWithMetaLong build() {
			return this;
		}
		
		@Override
		public FieldWithMetaLong.FieldWithMetaLongBuilder toBuilder() {
			FieldWithMetaLong.FieldWithMetaLongBuilder builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(FieldWithMetaLong.FieldWithMetaLongBuilder builder) {
			ofNullable(getValue()).ifPresent(builder::setValue);
			ofNullable(getMeta()).ifPresent(builder::setMeta);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			FieldWithMetaLong _that = getType().cast(o);
		
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
			return "FieldWithMetaLong {" +
				"value=" + this.value + ", " +
				"meta=" + this.meta +
			'}';
		}
	}

	/*********************** Builder Implementation of FieldWithMetaLong  ***********************/
	class FieldWithMetaLongBuilderImpl implements FieldWithMetaLong.FieldWithMetaLongBuilder {
	
		protected Long value;
		protected MetaFields.MetaFieldsBuilder meta;
	
		public FieldWithMetaLongBuilderImpl() {
		}
	
		@Override
		@RosettaAttribute("value")
		public Long getValue() {
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
		public FieldWithMetaLong.FieldWithMetaLongBuilder setValue(Long value) {
			this.value = value==null?null:value;
			return this;
		}
		@Override
		@RosettaAttribute("meta")
		public FieldWithMetaLong.FieldWithMetaLongBuilder setMeta(MetaFields meta) {
			this.meta = meta==null?null:meta.toBuilder();
			return this;
		}
		
		@Override
		public FieldWithMetaLong build() {
			return new FieldWithMetaLong.FieldWithMetaLongImpl(this);
		}
		
		@Override
		public FieldWithMetaLong.FieldWithMetaLongBuilder toBuilder() {
			return this;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public FieldWithMetaLong.FieldWithMetaLongBuilder prune() {
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
		public FieldWithMetaLong.FieldWithMetaLongBuilder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
			FieldWithMetaLong.FieldWithMetaLongBuilder o = (FieldWithMetaLong.FieldWithMetaLongBuilder) other;
			
			merger.mergeRosetta(getMeta(), o.getMeta(), this::setMeta);
			
			merger.mergeBasic(getValue(), o.getValue(), this::setValue);
			return this;
		}
	
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			FieldWithMetaLong _that = getType().cast(o);
		
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
			return "FieldWithMetaLongBuilder {" +
				"value=" + this.value + ", " +
				"meta=" + this.meta +
			'}';
		}
	}
}

class FieldWithMetaLongMeta extends BasicRosettaMetaData<FieldWithMetaLong>{

}
