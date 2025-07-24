package com.rosetta.model.metafields;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.annotations.RosettaAttribute;
import com.rosetta.model.lib.annotations.RosettaDataType;
import com.rosetta.model.lib.annotations.RuneAttribute;
import com.rosetta.model.lib.annotations.RuneDataType;
import com.rosetta.model.lib.annotations.RuneMetaType;
import com.rosetta.model.lib.meta.BasicRosettaMetaData;
import com.rosetta.model.lib.meta.Reference;
import com.rosetta.model.lib.meta.ReferenceWithMeta;
import com.rosetta.model.lib.meta.RosettaMetaData;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.process.AttributeMeta;
import com.rosetta.model.lib.process.BuilderMerger;
import com.rosetta.model.lib.process.BuilderProcessor;
import com.rosetta.model.lib.process.Processor;
import java.util.Objects;

import static java.util.Optional.ofNullable;

@RosettaDataType(value="ReferenceWithMetaString", builder=ReferenceWithMetaString.ReferenceWithMetaStringBuilderImpl.class, version="0.0.0")
@RuneDataType(value="ReferenceWithMetaString", model="com", builder=ReferenceWithMetaString.ReferenceWithMetaStringBuilderImpl.class, version="0.0.0")
public interface ReferenceWithMetaString extends RosettaModelObject, ReferenceWithMeta<String> {

	ReferenceWithMetaStringMeta metaData = new ReferenceWithMetaStringMeta();

	/*********************** Getter Methods  ***********************/
	String getValue();
	String getGlobalReference();
	String getExternalReference();
	Reference getReference();

	/*********************** Build Methods  ***********************/
	ReferenceWithMetaString build();
	
	ReferenceWithMetaString.ReferenceWithMetaStringBuilder toBuilder();
	
	static ReferenceWithMetaString.ReferenceWithMetaStringBuilder builder() {
		return new ReferenceWithMetaString.ReferenceWithMetaStringBuilderImpl();
	}

	/*********************** Utility Methods  ***********************/
	@Override
	default RosettaMetaData<? extends ReferenceWithMetaString> metaData() {
		return metaData;
	}
	
	@Override
	@RuneAttribute("@type")
	default Class<? extends ReferenceWithMetaString> getType() {
		return ReferenceWithMetaString.class;
	}
	
	@Override
	default Class<String> getValueType() {
		return String.class;
	}
	
	@Override
	default void process(RosettaPath path, Processor processor) {
		processor.processBasic(path.newSubPath("value"), String.class, getValue(), this);
		processor.processBasic(path.newSubPath("globalReference"), String.class, getGlobalReference(), this, AttributeMeta.META);
		processor.processBasic(path.newSubPath("externalReference"), String.class, getExternalReference(), this, AttributeMeta.META);
		processRosetta(path.newSubPath("reference"), processor, Reference.class, getReference());
	}
	

	/*********************** Builder Interface  ***********************/
	interface ReferenceWithMetaStringBuilder extends ReferenceWithMetaString, RosettaModelObjectBuilder, ReferenceWithMeta.ReferenceWithMetaBuilder<String> {
		Reference.ReferenceBuilder getOrCreateReference();
		@Override
		Reference.ReferenceBuilder getReference();
		ReferenceWithMetaString.ReferenceWithMetaStringBuilder setValue(String value);
		ReferenceWithMetaString.ReferenceWithMetaStringBuilder setGlobalReference(String globalReference);
		ReferenceWithMetaString.ReferenceWithMetaStringBuilder setExternalReference(String externalReference);
		ReferenceWithMetaString.ReferenceWithMetaStringBuilder setReference(Reference reference);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processor.processBasic(path.newSubPath("value"), String.class, getValue(), this);
			processor.processBasic(path.newSubPath("globalReference"), String.class, getGlobalReference(), this, AttributeMeta.META);
			processor.processBasic(path.newSubPath("externalReference"), String.class, getExternalReference(), this, AttributeMeta.META);
			processRosetta(path.newSubPath("reference"), processor, Reference.ReferenceBuilder.class, getReference());
		}
		

		ReferenceWithMetaString.ReferenceWithMetaStringBuilder prune();
	}

	/*********************** Immutable Implementation of ReferenceWithMetaString  ***********************/
	class ReferenceWithMetaStringImpl implements ReferenceWithMetaString {
		private final String value;
		private final String globalReference;
		private final String externalReference;
		private final Reference reference;
		
		protected ReferenceWithMetaStringImpl(ReferenceWithMetaString.ReferenceWithMetaStringBuilder builder) {
			this.value = builder.getValue();
			this.globalReference = builder.getGlobalReference();
			this.externalReference = builder.getExternalReference();
			this.reference = ofNullable(builder.getReference()).map(f->f.build()).orElse(null);
		}
		
		@Override
		@RosettaAttribute("value")
		@RuneAttribute("@data")
		public String getValue() {
			return value;
		}
		
		@Override
		@RosettaAttribute("globalReference")
		@RuneAttribute("@ref")
		public String getGlobalReference() {
			return globalReference;
		}
		
		@Override
		@RosettaAttribute("externalReference")
		@RuneAttribute("@ref:external")
		public String getExternalReference() {
			return externalReference;
		}
		
		@Override
		@RosettaAttribute("address")
		@RuneAttribute("@ref:scoped")
		@RuneMetaType
		public Reference getReference() {
			return reference;
		}
		
		@Override
		public ReferenceWithMetaString build() {
			return this;
		}
		
		@Override
		public ReferenceWithMetaString.ReferenceWithMetaStringBuilder toBuilder() {
			ReferenceWithMetaString.ReferenceWithMetaStringBuilder builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(ReferenceWithMetaString.ReferenceWithMetaStringBuilder builder) {
			ofNullable(getValue()).ifPresent(builder::setValue);
			ofNullable(getGlobalReference()).ifPresent(builder::setGlobalReference);
			ofNullable(getExternalReference()).ifPresent(builder::setExternalReference);
			ofNullable(getReference()).ifPresent(builder::setReference);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			ReferenceWithMetaString _that = getType().cast(o);
		
			if (!Objects.equals(value, _that.getValue())) return false;
			if (!Objects.equals(globalReference, _that.getGlobalReference())) return false;
			if (!Objects.equals(externalReference, _that.getExternalReference())) return false;
			if (!Objects.equals(reference, _that.getReference())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (value != null ? value.hashCode() : 0);
			_result = 31 * _result + (globalReference != null ? globalReference.hashCode() : 0);
			_result = 31 * _result + (externalReference != null ? externalReference.hashCode() : 0);
			_result = 31 * _result + (reference != null ? reference.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "ReferenceWithMetaString {" +
				"value=" + this.value + ", " +
				"globalReference=" + this.globalReference + ", " +
				"externalReference=" + this.externalReference + ", " +
				"reference=" + this.reference +
			'}';
		}
	}

	/*********************** Builder Implementation of ReferenceWithMetaString  ***********************/
	class ReferenceWithMetaStringBuilderImpl implements ReferenceWithMetaString.ReferenceWithMetaStringBuilder {
	
		protected String value;
		protected String globalReference;
		protected String externalReference;
		protected Reference.ReferenceBuilder reference;
		
		@Override
		@RosettaAttribute("value")
		@RuneAttribute("@data")
		public String getValue() {
			return value;
		}
		
		@Override
		@RosettaAttribute("globalReference")
		@RuneAttribute("@ref")
		public String getGlobalReference() {
			return globalReference;
		}
		
		@Override
		@RosettaAttribute("externalReference")
		@RuneAttribute("@ref:external")
		public String getExternalReference() {
			return externalReference;
		}
		
		@Override
		@RosettaAttribute("address")
		@RuneAttribute("@ref:scoped")
		@RuneMetaType
		public Reference.ReferenceBuilder getReference() {
			return reference;
		}
		
		@Override
		public Reference.ReferenceBuilder getOrCreateReference() {
			Reference.ReferenceBuilder result;
			if (reference!=null) {
				result = reference;
			}
			else {
				result = reference = Reference.builder();
			}
			
			return result;
		}
		
		@RosettaAttribute("value")
		@RuneAttribute("@data")
		@Override
		public ReferenceWithMetaString.ReferenceWithMetaStringBuilder setValue(String _value) {
			this.value = _value == null ? null : _value;
			return this;
		}
		
		@RosettaAttribute("globalReference")
		@RuneAttribute("@ref")
		@Override
		public ReferenceWithMetaString.ReferenceWithMetaStringBuilder setGlobalReference(String _globalReference) {
			this.globalReference = _globalReference == null ? null : _globalReference;
			return this;
		}
		
		@RosettaAttribute("externalReference")
		@RuneAttribute("@ref:external")
		@Override
		public ReferenceWithMetaString.ReferenceWithMetaStringBuilder setExternalReference(String _externalReference) {
			this.externalReference = _externalReference == null ? null : _externalReference;
			return this;
		}
		
		@RosettaAttribute("address")
		@RuneAttribute("@ref:scoped")
		@RuneMetaType
		@Override
		public ReferenceWithMetaString.ReferenceWithMetaStringBuilder setReference(Reference _reference) {
			this.reference = _reference == null ? null : _reference.toBuilder();
			return this;
		}
		
		@Override
		public ReferenceWithMetaString build() {
			return new ReferenceWithMetaString.ReferenceWithMetaStringImpl(this);
		}
		
		@Override
		public ReferenceWithMetaString.ReferenceWithMetaStringBuilder toBuilder() {
			return this;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public ReferenceWithMetaString.ReferenceWithMetaStringBuilder prune() {
			if (reference!=null && !reference.prune().hasData()) reference = null;
			return this;
		}
		
		@Override
		public boolean hasData() {
			if (getValue()!=null) return true;
			if (getGlobalReference()!=null) return true;
			if (getExternalReference()!=null) return true;
			if (getReference()!=null && getReference().hasData()) return true;
			return false;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public ReferenceWithMetaString.ReferenceWithMetaStringBuilder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
			ReferenceWithMetaString.ReferenceWithMetaStringBuilder o = (ReferenceWithMetaString.ReferenceWithMetaStringBuilder) other;
			
			merger.mergeRosetta(getReference(), o.getReference(), this::setReference);
			
			merger.mergeBasic(getValue(), o.getValue(), this::setValue);
			merger.mergeBasic(getGlobalReference(), o.getGlobalReference(), this::setGlobalReference);
			merger.mergeBasic(getExternalReference(), o.getExternalReference(), this::setExternalReference);
			return this;
		}
	
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			ReferenceWithMetaString _that = getType().cast(o);
		
			if (!Objects.equals(value, _that.getValue())) return false;
			if (!Objects.equals(globalReference, _that.getGlobalReference())) return false;
			if (!Objects.equals(externalReference, _that.getExternalReference())) return false;
			if (!Objects.equals(reference, _that.getReference())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (value != null ? value.hashCode() : 0);
			_result = 31 * _result + (globalReference != null ? globalReference.hashCode() : 0);
			_result = 31 * _result + (externalReference != null ? externalReference.hashCode() : 0);
			_result = 31 * _result + (reference != null ? reference.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "ReferenceWithMetaStringBuilder {" +
				"value=" + this.value + ", " +
				"globalReference=" + this.globalReference + ", " +
				"externalReference=" + this.externalReference + ", " +
				"reference=" + this.reference +
			'}';
		}
	}
}

class ReferenceWithMetaStringMeta extends BasicRosettaMetaData<ReferenceWithMetaString> {

}
