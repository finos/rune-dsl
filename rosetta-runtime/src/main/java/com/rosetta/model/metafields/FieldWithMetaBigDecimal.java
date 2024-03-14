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
import java.math.BigDecimal;
import java.util.Objects;

import static java.util.Optional.ofNullable;

/**
 * @version 1
 */
@RosettaDataType(value="FieldWithMetaBigDecimal", builder=FieldWithMetaBigDecimal.FieldWithMetaBigDecimalBuilderImpl.class, version="0.0.0")
public interface FieldWithMetaBigDecimal extends RosettaModelObject, FieldWithMeta<BigDecimal>, GlobalKey {

	FieldWithMetaBigDecimalMeta metaData = new FieldWithMetaBigDecimalMeta();

	/*********************** Getter Methods  ***********************/
	BigDecimal getValue();
	MetaFields getMeta();

	/*********************** Build Methods  ***********************/
	FieldWithMetaBigDecimal build();
	
	FieldWithMetaBigDecimal.FieldWithMetaBigDecimalBuilder toBuilder();
	
	static FieldWithMetaBigDecimal.FieldWithMetaBigDecimalBuilder builder() {
		return new FieldWithMetaBigDecimal.FieldWithMetaBigDecimalBuilderImpl();
	}

	/*********************** Utility Methods  ***********************/
	@Override
	default RosettaMetaData<? extends FieldWithMetaBigDecimal> metaData() {
		return metaData;
	}
	
	@Override
	default Class<? extends FieldWithMetaBigDecimal> getType() {
		return FieldWithMetaBigDecimal.class;
	}
	
	@Override
	default Class<BigDecimal> getValueType() {
		return BigDecimal.class;
	}
	
	@Override
	default void process(RosettaPath path, Processor processor) {
		processor.processBasic(path.newSubPath("value"), BigDecimal.class, getValue(), this);
		processRosetta(path.newSubPath("meta"), processor, MetaFields.class, getMeta());
	}
	

	/*********************** Builder Interface  ***********************/
	interface FieldWithMetaBigDecimalBuilder extends FieldWithMetaBigDecimal, RosettaModelObjectBuilder, GlobalKey.GlobalKeyBuilder, FieldWithMeta.FieldWithMetaBuilder<BigDecimal> {
		MetaFields.MetaFieldsBuilder getOrCreateMeta();
		MetaFields.MetaFieldsBuilder getMeta();
		FieldWithMetaBigDecimal.FieldWithMetaBigDecimalBuilder setValue(BigDecimal value);
		FieldWithMetaBigDecimal.FieldWithMetaBigDecimalBuilder setMeta(MetaFields meta);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processor.processBasic(path.newSubPath("value"), BigDecimal.class, getValue(), this);
			processRosetta(path.newSubPath("meta"), processor, MetaFields.MetaFieldsBuilder.class, getMeta());
		}
		

		FieldWithMetaBigDecimal.FieldWithMetaBigDecimalBuilder prune();
	}

	/*********************** Immutable Implementation of FieldWithMetaBigDecimal  ***********************/
	class FieldWithMetaBigDecimalImpl implements FieldWithMetaBigDecimal {
		private final BigDecimal value;
		private final MetaFields meta;
		
		protected FieldWithMetaBigDecimalImpl(FieldWithMetaBigDecimal.FieldWithMetaBigDecimalBuilder builder) {
			this.value = builder.getValue();
			this.meta = ofNullable(builder.getMeta()).map(f->f.build()).orElse(null);
		}
		
		@Override
		@RosettaAttribute("value")
		public BigDecimal getValue() {
			return value;
		}
		
		@Override
		@RosettaAttribute("meta")
		public MetaFields getMeta() {
			return meta;
		}
		
		@Override
		public FieldWithMetaBigDecimal build() {
			return this;
		}
		
		@Override
		public FieldWithMetaBigDecimal.FieldWithMetaBigDecimalBuilder toBuilder() {
			FieldWithMetaBigDecimal.FieldWithMetaBigDecimalBuilder builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(FieldWithMetaBigDecimal.FieldWithMetaBigDecimalBuilder builder) {
			ofNullable(getValue()).ifPresent(builder::setValue);
			ofNullable(getMeta()).ifPresent(builder::setMeta);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			FieldWithMetaBigDecimal _that = getType().cast(o);
		
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
			return "FieldWithMetaBigDecimal {" +
				"value=" + this.value + ", " +
				"meta=" + this.meta +
			'}';
		}
	}

	/*********************** Builder Implementation of FieldWithMetaBigDecimal  ***********************/
	class FieldWithMetaBigDecimalBuilderImpl implements FieldWithMetaBigDecimal.FieldWithMetaBigDecimalBuilder {
	
		protected BigDecimal value;
		protected MetaFields.MetaFieldsBuilder meta;
	
		public FieldWithMetaBigDecimalBuilderImpl() {
		}
	
		@Override
		@RosettaAttribute("value")
		public BigDecimal getValue() {
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
		public FieldWithMetaBigDecimal.FieldWithMetaBigDecimalBuilder setValue(BigDecimal value) {
			this.value = value==null?null:value;
			return this;
		}
		@Override
		@RosettaAttribute("meta")
		public FieldWithMetaBigDecimal.FieldWithMetaBigDecimalBuilder setMeta(MetaFields meta) {
			this.meta = meta==null?null:meta.toBuilder();
			return this;
		}
		
		@Override
		public FieldWithMetaBigDecimal build() {
			return new FieldWithMetaBigDecimal.FieldWithMetaBigDecimalImpl(this);
		}
		
		@Override
		public FieldWithMetaBigDecimal.FieldWithMetaBigDecimalBuilder toBuilder() {
			return this;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public FieldWithMetaBigDecimal.FieldWithMetaBigDecimalBuilder prune() {
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
		public FieldWithMetaBigDecimal.FieldWithMetaBigDecimalBuilder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
			FieldWithMetaBigDecimal.FieldWithMetaBigDecimalBuilder o = (FieldWithMetaBigDecimal.FieldWithMetaBigDecimalBuilder) other;
			
			merger.mergeRosetta(getMeta(), o.getMeta(), this::setMeta);
			
			merger.mergeBasic(getValue(), o.getValue(), this::setValue);
			return this;
		}
	
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			FieldWithMetaBigDecimal _that = getType().cast(o);
		
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
			return "FieldWithMetaBigDecimalBuilder {" +
				"value=" + this.value + ", " +
				"meta=" + this.meta +
			'}';
		}
	}
}

class FieldWithMetaBigDecimalMeta extends BasicRosettaMetaData<FieldWithMetaBigDecimal>{

}
