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
import java.time.LocalDateTime;
import java.util.Objects;

import static java.util.Optional.ofNullable;

/**
 * @version 1
 */
@RosettaDataType(value="FieldWithMetaLocalDateTime", builder=FieldWithMetaLocalDateTime.FieldWithMetaLocalDateTimeBuilderImpl.class, version="0.0.0")
public interface FieldWithMetaLocalDateTime extends RosettaModelObject, FieldWithMeta<LocalDateTime>, GlobalKey {

	FieldWithMetaLocalDateTimeMeta metaData = new FieldWithMetaLocalDateTimeMeta();

	/*********************** Getter Methods  ***********************/
	LocalDateTime getValue();
	MetaFields getMeta();

	/*********************** Build Methods  ***********************/
	FieldWithMetaLocalDateTime build();
	
	FieldWithMetaLocalDateTime.FieldWithMetaLocalDateTimeBuilder toBuilder();
	
	static FieldWithMetaLocalDateTime.FieldWithMetaLocalDateTimeBuilder builder() {
		return new FieldWithMetaLocalDateTime.FieldWithMetaLocalDateTimeBuilderImpl();
	}

	/*********************** Utility Methods  ***********************/
	@Override
	default RosettaMetaData<? extends FieldWithMetaLocalDateTime> metaData() {
		return metaData;
	}
	
	@Override
	default Class<? extends FieldWithMetaLocalDateTime> getType() {
		return FieldWithMetaLocalDateTime.class;
	}
	
	@Override
	default Class<LocalDateTime> getValueType() {
		return LocalDateTime.class;
	}
	
	@Override
	default void process(RosettaPath path, Processor processor) {
		processor.processBasic(path.newSubPath("value"), LocalDateTime.class, getValue(), this);
		processRosetta(path.newSubPath("meta"), processor, MetaFields.class, getMeta());
	}
	

	/*********************** Builder Interface  ***********************/
	interface FieldWithMetaLocalDateTimeBuilder extends FieldWithMetaLocalDateTime, RosettaModelObjectBuilder, GlobalKey.GlobalKeyBuilder, FieldWithMeta.FieldWithMetaBuilder<LocalDateTime> {
		MetaFields.MetaFieldsBuilder getOrCreateMeta();
		MetaFields.MetaFieldsBuilder getMeta();
		FieldWithMetaLocalDateTime.FieldWithMetaLocalDateTimeBuilder setValue(LocalDateTime value);
		FieldWithMetaLocalDateTime.FieldWithMetaLocalDateTimeBuilder setMeta(MetaFields meta);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processor.processBasic(path.newSubPath("value"), LocalDateTime.class, getValue(), this);
			processRosetta(path.newSubPath("meta"), processor, MetaFields.MetaFieldsBuilder.class, getMeta());
		}
		

		FieldWithMetaLocalDateTime.FieldWithMetaLocalDateTimeBuilder prune();
	}

	/*********************** Immutable Implementation of FieldWithMetaLocalDateTime  ***********************/
	class FieldWithMetaLocalDateTimeImpl implements FieldWithMetaLocalDateTime {
		private final LocalDateTime value;
		private final MetaFields meta;
		
		protected FieldWithMetaLocalDateTimeImpl(FieldWithMetaLocalDateTime.FieldWithMetaLocalDateTimeBuilder builder) {
			this.value = builder.getValue();
			this.meta = ofNullable(builder.getMeta()).map(f->f.build()).orElse(null);
		}
		
		@Override
		@RosettaAttribute("value")
		public LocalDateTime getValue() {
			return value;
		}
		
		@Override
		@RosettaAttribute("meta")
		public MetaFields getMeta() {
			return meta;
		}
		
		@Override
		public FieldWithMetaLocalDateTime build() {
			return this;
		}
		
		@Override
		public FieldWithMetaLocalDateTime.FieldWithMetaLocalDateTimeBuilder toBuilder() {
			FieldWithMetaLocalDateTime.FieldWithMetaLocalDateTimeBuilder builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(FieldWithMetaLocalDateTime.FieldWithMetaLocalDateTimeBuilder builder) {
			ofNullable(getValue()).ifPresent(builder::setValue);
			ofNullable(getMeta()).ifPresent(builder::setMeta);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			FieldWithMetaLocalDateTime _that = getType().cast(o);
		
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
			return "FieldWithMetaLocalDateTime {" +
				"value=" + this.value + ", " +
				"meta=" + this.meta +
			'}';
		}
	}

	/*********************** Builder Implementation of FieldWithMetaLocalDateTime  ***********************/
	class FieldWithMetaLocalDateTimeBuilderImpl implements FieldWithMetaLocalDateTime.FieldWithMetaLocalDateTimeBuilder {
	
		protected LocalDateTime value;
		protected MetaFields.MetaFieldsBuilder meta;
	
		public FieldWithMetaLocalDateTimeBuilderImpl() {
		}
	
		@Override
		@RosettaAttribute("value")
		public LocalDateTime getValue() {
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
		public FieldWithMetaLocalDateTime.FieldWithMetaLocalDateTimeBuilder setValue(LocalDateTime value) {
			this.value = value==null?null:value;
			return this;
		}
		@Override
		@RosettaAttribute("meta")
		public FieldWithMetaLocalDateTime.FieldWithMetaLocalDateTimeBuilder setMeta(MetaFields meta) {
			this.meta = meta==null?null:meta.toBuilder();
			return this;
		}
		
		@Override
		public FieldWithMetaLocalDateTime build() {
			return new FieldWithMetaLocalDateTime.FieldWithMetaLocalDateTimeImpl(this);
		}
		
		@Override
		public FieldWithMetaLocalDateTime.FieldWithMetaLocalDateTimeBuilder toBuilder() {
			return this;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public FieldWithMetaLocalDateTime.FieldWithMetaLocalDateTimeBuilder prune() {
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
		public FieldWithMetaLocalDateTime.FieldWithMetaLocalDateTimeBuilder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
			FieldWithMetaLocalDateTime.FieldWithMetaLocalDateTimeBuilder o = (FieldWithMetaLocalDateTime.FieldWithMetaLocalDateTimeBuilder) other;
			
			merger.mergeRosetta(getMeta(), o.getMeta(), this::setMeta);
			
			merger.mergeBasic(getValue(), o.getValue(), this::setValue);
			return this;
		}
	
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			FieldWithMetaLocalDateTime _that = getType().cast(o);
		
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
			return "FieldWithMetaLocalDateTimeBuilder {" +
				"value=" + this.value + ", " +
				"meta=" + this.meta +
			'}';
		}
	}
}

class FieldWithMetaLocalDateTimeMeta extends BasicRosettaMetaData<FieldWithMetaLocalDateTime>{

}
