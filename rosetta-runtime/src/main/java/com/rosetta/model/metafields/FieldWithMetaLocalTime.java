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
import java.time.LocalTime;
import java.util.Objects;

import static java.util.Optional.ofNullable;

/**
 * @version 1
 */
@RosettaDataType(value="FieldWithMetaLocalTime", builder=FieldWithMetaLocalTime.FieldWithMetaLocalTimeBuilderImpl.class, version="0.0.0")
public interface FieldWithMetaLocalTime extends RosettaModelObject, FieldWithMeta<LocalTime>, GlobalKey {

	FieldWithMetaLocalTimeMeta metaData = new FieldWithMetaLocalTimeMeta();

	/*********************** Getter Methods  ***********************/
	LocalTime getValue();
	MetaFields getMeta();

	/*********************** Build Methods  ***********************/
	FieldWithMetaLocalTime build();
	
	FieldWithMetaLocalTime.FieldWithMetaLocalTimeBuilder toBuilder();
	
	static FieldWithMetaLocalTime.FieldWithMetaLocalTimeBuilder builder() {
		return new FieldWithMetaLocalTime.FieldWithMetaLocalTimeBuilderImpl();
	}

	/*********************** Utility Methods  ***********************/
	@Override
	default RosettaMetaData<? extends FieldWithMetaLocalTime> metaData() {
		return metaData;
	}
	
	@Override
	default Class<? extends FieldWithMetaLocalTime> getType() {
		return FieldWithMetaLocalTime.class;
	}
	
	@Override
	default Class<LocalTime> getValueType() {
		return LocalTime.class;
	}
	
	@Override
	default void process(RosettaPath path, Processor processor) {
		processor.processBasic(path.newSubPath("value"), LocalTime.class, getValue(), this);
		processRosetta(path.newSubPath("meta"), processor, MetaFields.class, getMeta());
	}
	

	/*********************** Builder Interface  ***********************/
	interface FieldWithMetaLocalTimeBuilder extends FieldWithMetaLocalTime, RosettaModelObjectBuilder, GlobalKey.GlobalKeyBuilder, FieldWithMeta.FieldWithMetaBuilder<LocalTime> {
		MetaFields.MetaFieldsBuilder getOrCreateMeta();
		MetaFields.MetaFieldsBuilder getMeta();
		FieldWithMetaLocalTime.FieldWithMetaLocalTimeBuilder setValue(LocalTime value);
		FieldWithMetaLocalTime.FieldWithMetaLocalTimeBuilder setMeta(MetaFields meta);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processor.processBasic(path.newSubPath("value"), LocalTime.class, getValue(), this);
			processRosetta(path.newSubPath("meta"), processor, MetaFields.MetaFieldsBuilder.class, getMeta());
		}
		

		FieldWithMetaLocalTime.FieldWithMetaLocalTimeBuilder prune();
	}

	/*********************** Immutable Implementation of FieldWithMetaLocalTime  ***********************/
	class FieldWithMetaLocalTimeImpl implements FieldWithMetaLocalTime {
		private final LocalTime value;
		private final MetaFields meta;
		
		protected FieldWithMetaLocalTimeImpl(FieldWithMetaLocalTime.FieldWithMetaLocalTimeBuilder builder) {
			this.value = builder.getValue();
			this.meta = ofNullable(builder.getMeta()).map(f->f.build()).orElse(null);
		}
		
		@Override
		@RosettaAttribute("value")
		public LocalTime getValue() {
			return value;
		}
		
		@Override
		@RosettaAttribute("meta")
		public MetaFields getMeta() {
			return meta;
		}
		
		@Override
		public FieldWithMetaLocalTime build() {
			return this;
		}
		
		@Override
		public FieldWithMetaLocalTime.FieldWithMetaLocalTimeBuilder toBuilder() {
			FieldWithMetaLocalTime.FieldWithMetaLocalTimeBuilder builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(FieldWithMetaLocalTime.FieldWithMetaLocalTimeBuilder builder) {
			ofNullable(getValue()).ifPresent(builder::setValue);
			ofNullable(getMeta()).ifPresent(builder::setMeta);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			FieldWithMetaLocalTime _that = getType().cast(o);
		
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
			return "FieldWithMetaLocalTime {" +
				"value=" + this.value + ", " +
				"meta=" + this.meta +
			'}';
		}
	}

	/*********************** Builder Implementation of FieldWithMetaLocalTime  ***********************/
	class FieldWithMetaLocalTimeBuilderImpl implements FieldWithMetaLocalTime.FieldWithMetaLocalTimeBuilder {
	
		protected LocalTime value;
		protected MetaFields.MetaFieldsBuilder meta;
	
		public FieldWithMetaLocalTimeBuilderImpl() {
		}
	
		@Override
		@RosettaAttribute("value")
		public LocalTime getValue() {
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
		public FieldWithMetaLocalTime.FieldWithMetaLocalTimeBuilder setValue(LocalTime value) {
			this.value = value==null?null:value;
			return this;
		}
		@Override
		@RosettaAttribute("meta")
		public FieldWithMetaLocalTime.FieldWithMetaLocalTimeBuilder setMeta(MetaFields meta) {
			this.meta = meta==null?null:meta.toBuilder();
			return this;
		}
		
		@Override
		public FieldWithMetaLocalTime build() {
			return new FieldWithMetaLocalTime.FieldWithMetaLocalTimeImpl(this);
		}
		
		@Override
		public FieldWithMetaLocalTime.FieldWithMetaLocalTimeBuilder toBuilder() {
			return this;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public FieldWithMetaLocalTime.FieldWithMetaLocalTimeBuilder prune() {
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
		public FieldWithMetaLocalTime.FieldWithMetaLocalTimeBuilder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
			FieldWithMetaLocalTime.FieldWithMetaLocalTimeBuilder o = (FieldWithMetaLocalTime.FieldWithMetaLocalTimeBuilder) other;
			
			merger.mergeRosetta(getMeta(), o.getMeta(), this::setMeta);
			
			merger.mergeBasic(getValue(), o.getValue(), this::setValue);
			return this;
		}
	
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			FieldWithMetaLocalTime _that = getType().cast(o);
		
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
			return "FieldWithMetaLocalTimeBuilder {" +
				"value=" + this.value + ", " +
				"meta=" + this.meta +
			'}';
		}
	}
}

class FieldWithMetaLocalTimeMeta extends BasicRosettaMetaData<FieldWithMetaLocalTime>{

}
