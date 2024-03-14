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
import com.rosetta.model.lib.records.Date;
import java.util.Objects;

import static java.util.Optional.ofNullable;

/**
 * @version 1
 */
@RosettaDataType(value="FieldWithMetaDate", builder=FieldWithMetaDate.FieldWithMetaDateBuilderImpl.class, version="0.0.0")
public interface FieldWithMetaDate extends RosettaModelObject, FieldWithMeta<Date>, GlobalKey {

	FieldWithMetaDateMeta metaData = new FieldWithMetaDateMeta();

	/*********************** Getter Methods  ***********************/
	Date getValue();
	MetaFields getMeta();

	/*********************** Build Methods  ***********************/
	FieldWithMetaDate build();
	
	FieldWithMetaDate.FieldWithMetaDateBuilder toBuilder();
	
	static FieldWithMetaDate.FieldWithMetaDateBuilder builder() {
		return new FieldWithMetaDate.FieldWithMetaDateBuilderImpl();
	}

	/*********************** Utility Methods  ***********************/
	@Override
	default RosettaMetaData<? extends FieldWithMetaDate> metaData() {
		return metaData;
	}
	
	@Override
	default Class<? extends FieldWithMetaDate> getType() {
		return FieldWithMetaDate.class;
	}
	
	@Override
	default Class<Date> getValueType() {
		return Date.class;
	}
	
	@Override
	default void process(RosettaPath path, Processor processor) {
		processor.processBasic(path.newSubPath("value"), Date.class, getValue(), this);
		processRosetta(path.newSubPath("meta"), processor, MetaFields.class, getMeta());
	}
	

	/*********************** Builder Interface  ***********************/
	interface FieldWithMetaDateBuilder extends FieldWithMetaDate, RosettaModelObjectBuilder, GlobalKey.GlobalKeyBuilder, FieldWithMeta.FieldWithMetaBuilder<Date> {
		MetaFields.MetaFieldsBuilder getOrCreateMeta();
		MetaFields.MetaFieldsBuilder getMeta();
		FieldWithMetaDate.FieldWithMetaDateBuilder setValue(Date value);
		FieldWithMetaDate.FieldWithMetaDateBuilder setMeta(MetaFields meta);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processor.processBasic(path.newSubPath("value"), Date.class, getValue(), this);
			processRosetta(path.newSubPath("meta"), processor, MetaFields.MetaFieldsBuilder.class, getMeta());
		}
		

		FieldWithMetaDate.FieldWithMetaDateBuilder prune();
	}

	/*********************** Immutable Implementation of FieldWithMetaDate  ***********************/
	class FieldWithMetaDateImpl implements FieldWithMetaDate {
		private final Date value;
		private final MetaFields meta;
		
		protected FieldWithMetaDateImpl(FieldWithMetaDate.FieldWithMetaDateBuilder builder) {
			this.value = builder.getValue();
			this.meta = ofNullable(builder.getMeta()).map(f->f.build()).orElse(null);
		}
		
		@Override
		@RosettaAttribute("value")
		public Date getValue() {
			return value;
		}
		
		@Override
		@RosettaAttribute("meta")
		public MetaFields getMeta() {
			return meta;
		}
		
		@Override
		public FieldWithMetaDate build() {
			return this;
		}
		
		@Override
		public FieldWithMetaDate.FieldWithMetaDateBuilder toBuilder() {
			FieldWithMetaDate.FieldWithMetaDateBuilder builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(FieldWithMetaDate.FieldWithMetaDateBuilder builder) {
			ofNullable(getValue()).ifPresent(builder::setValue);
			ofNullable(getMeta()).ifPresent(builder::setMeta);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			FieldWithMetaDate _that = getType().cast(o);
		
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
			return "FieldWithMetaDate {" +
				"value=" + this.value + ", " +
				"meta=" + this.meta +
			'}';
		}
	}

	/*********************** Builder Implementation of FieldWithMetaDate  ***********************/
	class FieldWithMetaDateBuilderImpl implements FieldWithMetaDate.FieldWithMetaDateBuilder {
	
		protected Date value;
		protected MetaFields.MetaFieldsBuilder meta;
	
		public FieldWithMetaDateBuilderImpl() {
		}
	
		@Override
		@RosettaAttribute("value")
		public Date getValue() {
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
		public FieldWithMetaDate.FieldWithMetaDateBuilder setValue(Date value) {
			this.value = value==null?null:value;
			return this;
		}
		@Override
		@RosettaAttribute("meta")
		public FieldWithMetaDate.FieldWithMetaDateBuilder setMeta(MetaFields meta) {
			this.meta = meta==null?null:meta.toBuilder();
			return this;
		}
		
		@Override
		public FieldWithMetaDate build() {
			return new FieldWithMetaDate.FieldWithMetaDateImpl(this);
		}
		
		@Override
		public FieldWithMetaDate.FieldWithMetaDateBuilder toBuilder() {
			return this;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public FieldWithMetaDate.FieldWithMetaDateBuilder prune() {
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
		public FieldWithMetaDate.FieldWithMetaDateBuilder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
			FieldWithMetaDate.FieldWithMetaDateBuilder o = (FieldWithMetaDate.FieldWithMetaDateBuilder) other;
			
			merger.mergeRosetta(getMeta(), o.getMeta(), this::setMeta);
			
			merger.mergeBasic(getValue(), o.getValue(), this::setValue);
			return this;
		}
	
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			FieldWithMetaDate _that = getType().cast(o);
		
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
			return "FieldWithMetaDateBuilder {" +
				"value=" + this.value + ", " +
				"meta=" + this.meta +
			'}';
		}
	}
}

class FieldWithMetaDateMeta extends BasicRosettaMetaData<FieldWithMetaDate>{

}
