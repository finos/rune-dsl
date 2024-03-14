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
import java.time.ZonedDateTime;
import java.util.Objects;

import static java.util.Optional.ofNullable;

/**
 * @version 1
 */
@RosettaDataType(value="FieldWithMetaZonedDateTime", builder=FieldWithMetaZonedDateTime.FieldWithMetaZonedDateTimeBuilderImpl.class, version="0.0.0")
public interface FieldWithMetaZonedDateTime extends RosettaModelObject, FieldWithMeta<ZonedDateTime>, GlobalKey {

	FieldWithMetaZonedDateTimeMeta metaData = new FieldWithMetaZonedDateTimeMeta();

	/*********************** Getter Methods  ***********************/
	ZonedDateTime getValue();
	MetaFields getMeta();

	/*********************** Build Methods  ***********************/
	FieldWithMetaZonedDateTime build();
	
	FieldWithMetaZonedDateTime.FieldWithMetaZonedDateTimeBuilder toBuilder();
	
	static FieldWithMetaZonedDateTime.FieldWithMetaZonedDateTimeBuilder builder() {
		return new FieldWithMetaZonedDateTime.FieldWithMetaZonedDateTimeBuilderImpl();
	}

	/*********************** Utility Methods  ***********************/
	@Override
	default RosettaMetaData<? extends FieldWithMetaZonedDateTime> metaData() {
		return metaData;
	}
	
	@Override
	default Class<? extends FieldWithMetaZonedDateTime> getType() {
		return FieldWithMetaZonedDateTime.class;
	}
	
	@Override
	default Class<ZonedDateTime> getValueType() {
		return ZonedDateTime.class;
	}
	
	@Override
	default void process(RosettaPath path, Processor processor) {
		processor.processBasic(path.newSubPath("value"), ZonedDateTime.class, getValue(), this);
		processRosetta(path.newSubPath("meta"), processor, MetaFields.class, getMeta());
	}
	

	/*********************** Builder Interface  ***********************/
	interface FieldWithMetaZonedDateTimeBuilder extends FieldWithMetaZonedDateTime, RosettaModelObjectBuilder, GlobalKey.GlobalKeyBuilder, FieldWithMeta.FieldWithMetaBuilder<ZonedDateTime> {
		MetaFields.MetaFieldsBuilder getOrCreateMeta();
		MetaFields.MetaFieldsBuilder getMeta();
		FieldWithMetaZonedDateTime.FieldWithMetaZonedDateTimeBuilder setValue(ZonedDateTime value);
		FieldWithMetaZonedDateTime.FieldWithMetaZonedDateTimeBuilder setMeta(MetaFields meta);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processor.processBasic(path.newSubPath("value"), ZonedDateTime.class, getValue(), this);
			processRosetta(path.newSubPath("meta"), processor, MetaFields.MetaFieldsBuilder.class, getMeta());
		}
		

		FieldWithMetaZonedDateTime.FieldWithMetaZonedDateTimeBuilder prune();
	}

	/*********************** Immutable Implementation of FieldWithMetaZonedDateTime  ***********************/
	class FieldWithMetaZonedDateTimeImpl implements FieldWithMetaZonedDateTime {
		private final ZonedDateTime value;
		private final MetaFields meta;
		
		protected FieldWithMetaZonedDateTimeImpl(FieldWithMetaZonedDateTime.FieldWithMetaZonedDateTimeBuilder builder) {
			this.value = builder.getValue();
			this.meta = ofNullable(builder.getMeta()).map(f->f.build()).orElse(null);
		}
		
		@Override
		@RosettaAttribute("value")
		public ZonedDateTime getValue() {
			return value;
		}
		
		@Override
		@RosettaAttribute("meta")
		public MetaFields getMeta() {
			return meta;
		}
		
		@Override
		public FieldWithMetaZonedDateTime build() {
			return this;
		}
		
		@Override
		public FieldWithMetaZonedDateTime.FieldWithMetaZonedDateTimeBuilder toBuilder() {
			FieldWithMetaZonedDateTime.FieldWithMetaZonedDateTimeBuilder builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(FieldWithMetaZonedDateTime.FieldWithMetaZonedDateTimeBuilder builder) {
			ofNullable(getValue()).ifPresent(builder::setValue);
			ofNullable(getMeta()).ifPresent(builder::setMeta);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			FieldWithMetaZonedDateTime _that = getType().cast(o);
		
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
			return "FieldWithMetaZonedDateTime {" +
				"value=" + this.value + ", " +
				"meta=" + this.meta +
			'}';
		}
	}

	/*********************** Builder Implementation of FieldWithMetaZonedDateTime  ***********************/
	class FieldWithMetaZonedDateTimeBuilderImpl implements FieldWithMetaZonedDateTime.FieldWithMetaZonedDateTimeBuilder {
	
		protected ZonedDateTime value;
		protected MetaFields.MetaFieldsBuilder meta;
	
		public FieldWithMetaZonedDateTimeBuilderImpl() {
		}
	
		@Override
		@RosettaAttribute("value")
		public ZonedDateTime getValue() {
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
		public FieldWithMetaZonedDateTime.FieldWithMetaZonedDateTimeBuilder setValue(ZonedDateTime value) {
			this.value = value==null?null:value;
			return this;
		}
		@Override
		@RosettaAttribute("meta")
		public FieldWithMetaZonedDateTime.FieldWithMetaZonedDateTimeBuilder setMeta(MetaFields meta) {
			this.meta = meta==null?null:meta.toBuilder();
			return this;
		}
		
		@Override
		public FieldWithMetaZonedDateTime build() {
			return new FieldWithMetaZonedDateTime.FieldWithMetaZonedDateTimeImpl(this);
		}
		
		@Override
		public FieldWithMetaZonedDateTime.FieldWithMetaZonedDateTimeBuilder toBuilder() {
			return this;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public FieldWithMetaZonedDateTime.FieldWithMetaZonedDateTimeBuilder prune() {
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
		public FieldWithMetaZonedDateTime.FieldWithMetaZonedDateTimeBuilder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
			FieldWithMetaZonedDateTime.FieldWithMetaZonedDateTimeBuilder o = (FieldWithMetaZonedDateTime.FieldWithMetaZonedDateTimeBuilder) other;
			
			merger.mergeRosetta(getMeta(), o.getMeta(), this::setMeta);
			
			merger.mergeBasic(getValue(), o.getValue(), this::setValue);
			return this;
		}
	
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			FieldWithMetaZonedDateTime _that = getType().cast(o);
		
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
			return "FieldWithMetaZonedDateTimeBuilder {" +
				"value=" + this.value + ", " +
				"meta=" + this.meta +
			'}';
		}
	}
}

class FieldWithMetaZonedDateTimeMeta extends BasicRosettaMetaData<FieldWithMetaZonedDateTime>{

}
