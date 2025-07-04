package com.rosetta.model.metafields;

import com.rosetta.model.lib.GlobalKey;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.annotations.RosettaAttribute;
import com.rosetta.model.lib.annotations.RosettaDataType;
import com.rosetta.model.lib.annotations.RuneAttribute;
import com.rosetta.model.lib.annotations.RuneDataType;
import com.rosetta.model.lib.annotations.RuneMetaType;
import com.rosetta.model.lib.meta.BasicRosettaMetaData;
import com.rosetta.model.lib.meta.FieldWithMeta;
import com.rosetta.model.lib.meta.RosettaMetaData;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.process.BuilderMerger;
import com.rosetta.model.lib.process.BuilderProcessor;
import com.rosetta.model.lib.process.Processor;
import java.util.Objects;

import static java.util.Optional.ofNullable;

@RosettaDataType(value="FieldWithMetaString", builder=FieldWithMetaString.FieldWithMetaStringBuilderImpl.class, version="0.0.0")
@RuneDataType(value="FieldWithMetaString", model="com", builder=FieldWithMetaString.FieldWithMetaStringBuilderImpl.class, version="0.0.0")
public interface FieldWithMetaString extends RosettaModelObject, FieldWithMeta<String>, GlobalKey {

	FieldWithMetaStringMeta metaData = new FieldWithMetaStringMeta();

	/*********************** Getter Methods  ***********************/
	String getValue();
	MetaFields getMeta();

	/*********************** Build Methods  ***********************/
	FieldWithMetaString build();
	
	FieldWithMetaString.FieldWithMetaStringBuilder toBuilder();
	
	static FieldWithMetaString.FieldWithMetaStringBuilder builder() {
		return new FieldWithMetaString.FieldWithMetaStringBuilderImpl();
	}

	/*********************** Utility Methods  ***********************/
	@Override
	default RosettaMetaData<? extends FieldWithMetaString> metaData() {
		return metaData;
	}
	
	@Override
	@RuneAttribute("@type")
	default Class<? extends FieldWithMetaString> getType() {
		return FieldWithMetaString.class;
	}
	
	@Override
	default Class<String> getValueType() {
		return String.class;
	}
	
	@Override
	default void process(RosettaPath path, Processor processor) {
		processor.processBasic(path.newSubPath("value"), String.class, getValue(), this);
		processRosetta(path.newSubPath("meta"), processor, MetaFields.class, getMeta());
	}
	

	/*********************** Builder Interface  ***********************/
	interface FieldWithMetaStringBuilder extends FieldWithMetaString, RosettaModelObjectBuilder, FieldWithMeta.FieldWithMetaBuilder<String>, GlobalKey.GlobalKeyBuilder {
		MetaFields.MetaFieldsBuilder getOrCreateMeta();
		@Override
		MetaFields.MetaFieldsBuilder getMeta();
		FieldWithMetaString.FieldWithMetaStringBuilder setValue(String value);
		FieldWithMetaString.FieldWithMetaStringBuilder setMeta(MetaFields meta);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processor.processBasic(path.newSubPath("value"), String.class, getValue(), this);
			processRosetta(path.newSubPath("meta"), processor, MetaFields.MetaFieldsBuilder.class, getMeta());
		}
		

		FieldWithMetaString.FieldWithMetaStringBuilder prune();
	}

	/*********************** Immutable Implementation of FieldWithMetaString  ***********************/
	class FieldWithMetaStringImpl implements FieldWithMetaString {
		private final String value;
		private final MetaFields meta;
		
		protected FieldWithMetaStringImpl(FieldWithMetaString.FieldWithMetaStringBuilder builder) {
			this.value = builder.getValue();
			this.meta = ofNullable(builder.getMeta()).map(f->f.build()).orElse(null);
		}
		
		@Override
		@RosettaAttribute("value")
		@RuneAttribute("@data")
		public String getValue() {
			return value;
		}
		
		@Override
		@RosettaAttribute("meta")
		@RuneAttribute("meta")
		@RuneMetaType
		public MetaFields getMeta() {
			return meta;
		}
		
		@Override
		public FieldWithMetaString build() {
			return this;
		}
		
		@Override
		public FieldWithMetaString.FieldWithMetaStringBuilder toBuilder() {
			FieldWithMetaString.FieldWithMetaStringBuilder builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(FieldWithMetaString.FieldWithMetaStringBuilder builder) {
			ofNullable(getValue()).ifPresent(builder::setValue);
			ofNullable(getMeta()).ifPresent(builder::setMeta);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			FieldWithMetaString _that = getType().cast(o);
		
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
			return "FieldWithMetaString {" +
				"value=" + this.value + ", " +
				"meta=" + this.meta +
			'}';
		}
	}

	/*********************** Builder Implementation of FieldWithMetaString  ***********************/
	class FieldWithMetaStringBuilderImpl implements FieldWithMetaString.FieldWithMetaStringBuilder {
	
		protected String value;
		protected MetaFields.MetaFieldsBuilder meta;
		
		@Override
		@RosettaAttribute("value")
		@RuneAttribute("@data")
		public String getValue() {
			return value;
		}
		
		@Override
		@RosettaAttribute("meta")
		@RuneAttribute("meta")
		@RuneMetaType
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
		
		@RosettaAttribute("value")
		@RuneAttribute("@data")
		@Override
		public FieldWithMetaString.FieldWithMetaStringBuilder setValue(String _value) {
			this.value = _value == null ? null : _value;
			return this;
		}
		
		@RosettaAttribute("meta")
		@RuneAttribute("meta")
		@RuneMetaType
		@Override
		public FieldWithMetaString.FieldWithMetaStringBuilder setMeta(MetaFields _meta) {
			this.meta = _meta == null ? null : _meta.toBuilder();
			return this;
		}
		
		@Override
		public FieldWithMetaString build() {
			return new FieldWithMetaString.FieldWithMetaStringImpl(this);
		}
		
		@Override
		public FieldWithMetaString.FieldWithMetaStringBuilder toBuilder() {
			return this;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public FieldWithMetaString.FieldWithMetaStringBuilder prune() {
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
		public FieldWithMetaString.FieldWithMetaStringBuilder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
			FieldWithMetaString.FieldWithMetaStringBuilder o = (FieldWithMetaString.FieldWithMetaStringBuilder) other;
			
			merger.mergeRosetta(getMeta(), o.getMeta(), this::setMeta);
			
			merger.mergeBasic(getValue(), o.getValue(), this::setValue);
			return this;
		}
	
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			FieldWithMetaString _that = getType().cast(o);
		
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
			return "FieldWithMetaStringBuilder {" +
				"value=" + this.value + ", " +
				"meta=" + this.meta +
			'}';
		}
	}
}

class FieldWithMetaStringMeta extends BasicRosettaMetaData<FieldWithMetaString> {

}
