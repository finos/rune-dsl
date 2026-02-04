package test.pojo.metafields;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.annotations.AccessorType;
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
import test.pojo.Child;

import static java.util.Optional.ofNullable;

@RosettaDataType(value="ReferenceWithMetaChild", builder=ReferenceWithMetaChild.ReferenceWithMetaChildBuilderImpl.class, version="0.0.0")
@RuneDataType(value="ReferenceWithMetaChild", model="test", builder=ReferenceWithMetaChild.ReferenceWithMetaChildBuilderImpl.class, version="0.0.0")
public interface ReferenceWithMetaChild extends RosettaModelObject, ReferenceWithMeta<Child> {

	ReferenceWithMetaChildMeta metaData = new ReferenceWithMetaChildMeta();

	/*********************** Getter Methods  ***********************/
	Child getValue();
	String getGlobalReference();
	String getExternalReference();
	Reference getReference();

	/*********************** Build Methods  ***********************/
	ReferenceWithMetaChild build();
	
	ReferenceWithMetaChild.ReferenceWithMetaChildBuilder toBuilder();
	
	static ReferenceWithMetaChild.ReferenceWithMetaChildBuilder builder() {
		return new ReferenceWithMetaChild.ReferenceWithMetaChildBuilderImpl();
	}

	/*********************** Utility Methods  ***********************/
	@Override
	default RosettaMetaData<? extends ReferenceWithMetaChild> metaData() {
		return metaData;
	}
	
	@Override
	@RuneAttribute("@type")
	default Class<? extends ReferenceWithMetaChild> getType() {
		return ReferenceWithMetaChild.class;
	}
	
	@Override
	default Class<Child> getValueType() {
		return Child.class;
	}
	
	@Override
	default void process(RosettaPath path, Processor processor) {
		processRosetta(path.newSubPath("value"), processor, Child.class, getValue());
		processor.processBasic(path.newSubPath("globalReference"), String.class, getGlobalReference(), this, AttributeMeta.META);
		processor.processBasic(path.newSubPath("externalReference"), String.class, getExternalReference(), this, AttributeMeta.META);
		processRosetta(path.newSubPath("reference"), processor, Reference.class, getReference());
	}
	

	/*********************** Builder Interface  ***********************/
	interface ReferenceWithMetaChildBuilder extends ReferenceWithMetaChild, RosettaModelObjectBuilder, ReferenceWithMeta.ReferenceWithMetaBuilder<Child> {
		Child.ChildBuilder getOrCreateValue();
		@Override
		Child.ChildBuilder getValue();
		Reference.ReferenceBuilder getOrCreateReference();
		@Override
		Reference.ReferenceBuilder getReference();
		ReferenceWithMetaChild.ReferenceWithMetaChildBuilder setValue(Child value);
		ReferenceWithMetaChild.ReferenceWithMetaChildBuilder setGlobalReference(String globalReference);
		ReferenceWithMetaChild.ReferenceWithMetaChildBuilder setExternalReference(String externalReference);
		ReferenceWithMetaChild.ReferenceWithMetaChildBuilder setReference(Reference reference);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processRosetta(path.newSubPath("value"), processor, Child.ChildBuilder.class, getValue());
			processor.processBasic(path.newSubPath("globalReference"), String.class, getGlobalReference(), this, AttributeMeta.META);
			processor.processBasic(path.newSubPath("externalReference"), String.class, getExternalReference(), this, AttributeMeta.META);
			processRosetta(path.newSubPath("reference"), processor, Reference.ReferenceBuilder.class, getReference());
		}
		

		ReferenceWithMetaChild.ReferenceWithMetaChildBuilder prune();
	}

	/*********************** Immutable Implementation of ReferenceWithMetaChild  ***********************/
	class ReferenceWithMetaChildImpl implements ReferenceWithMetaChild {
		private final Child value;
		private final String globalReference;
		private final String externalReference;
		private final Reference reference;
		
		protected ReferenceWithMetaChildImpl(ReferenceWithMetaChild.ReferenceWithMetaChildBuilder builder) {
			this.value = ofNullable(builder.getValue()).map(f->f.build()).orElse(null);
			this.globalReference = builder.getGlobalReference();
			this.externalReference = builder.getExternalReference();
			this.reference = ofNullable(builder.getReference()).map(f->f.build()).orElse(null);
		}
		
		@Override
		@RosettaAttribute(value="value", isRequired=false, isMulti=false, accessorType=AccessorType.GETTER)
		@RuneAttribute("@data")
		@RuneMetaType
		public Child getValue() {
			return value;
		}
		
		@Override
		@RosettaAttribute(value="globalReference", isRequired=false, isMulti=false, accessorType=AccessorType.GETTER)
		@RuneAttribute("@ref")
		public String getGlobalReference() {
			return globalReference;
		}
		
		@Override
		@RosettaAttribute(value="externalReference", isRequired=false, isMulti=false, accessorType=AccessorType.GETTER)
		@RuneAttribute("@ref:external")
		public String getExternalReference() {
			return externalReference;
		}
		
		@Override
		@RosettaAttribute(value="address", isRequired=false, isMulti=false, accessorType=AccessorType.GETTER)
		@RuneAttribute("@ref:scoped")
		@RuneMetaType
		public Reference getReference() {
			return reference;
		}
		
		@Override
		public ReferenceWithMetaChild build() {
			return this;
		}
		
		@Override
		public ReferenceWithMetaChild.ReferenceWithMetaChildBuilder toBuilder() {
			ReferenceWithMetaChild.ReferenceWithMetaChildBuilder builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(ReferenceWithMetaChild.ReferenceWithMetaChildBuilder builder) {
			ofNullable(getValue()).ifPresent(builder::setValue);
			ofNullable(getGlobalReference()).ifPresent(builder::setGlobalReference);
			ofNullable(getExternalReference()).ifPresent(builder::setExternalReference);
			ofNullable(getReference()).ifPresent(builder::setReference);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			ReferenceWithMetaChild _that = getType().cast(o);
		
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
			return "ReferenceWithMetaChild {" +
				"value=" + this.value + ", " +
				"globalReference=" + this.globalReference + ", " +
				"externalReference=" + this.externalReference + ", " +
				"reference=" + this.reference +
			'}';
		}
	}

	/*********************** Builder Implementation of ReferenceWithMetaChild  ***********************/
	class ReferenceWithMetaChildBuilderImpl implements ReferenceWithMetaChild.ReferenceWithMetaChildBuilder {
	
		protected Child.ChildBuilder value;
		protected String globalReference;
		protected String externalReference;
		protected Reference.ReferenceBuilder reference;
		
		@Override
		@RosettaAttribute(value="value", isRequired=false, isMulti=false, accessorType=AccessorType.GETTER)
		@RuneAttribute("@data")
		@RuneMetaType
		public Child.ChildBuilder getValue() {
			return value;
		}
		
		@Override
		public Child.ChildBuilder getOrCreateValue() {
			Child.ChildBuilder result;
			if (value!=null) {
				result = value;
			}
			else {
				result = value = Child.builder();
			}
			
			return result;
		}
		
		@Override
		@RosettaAttribute(value="globalReference", isRequired=false, isMulti=false, accessorType=AccessorType.GETTER)
		@RuneAttribute("@ref")
		public String getGlobalReference() {
			return globalReference;
		}
		
		@Override
		@RosettaAttribute(value="externalReference", isRequired=false, isMulti=false, accessorType=AccessorType.GETTER)
		@RuneAttribute("@ref:external")
		public String getExternalReference() {
			return externalReference;
		}
		
		@Override
		@RosettaAttribute(value="address", isRequired=false, isMulti=false, accessorType=AccessorType.GETTER)
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
		
		@RosettaAttribute(value="value", isRequired=false, isMulti=false, accessorType=AccessorType.SETTER)
		@RuneAttribute("@data")
		@RuneMetaType
		@Override
		public ReferenceWithMetaChild.ReferenceWithMetaChildBuilder setValue(Child _value) {
			this.value = _value == null ? null : _value.toBuilder();
			return this;
		}
		
		@RosettaAttribute(value="globalReference", isRequired=false, isMulti=false, accessorType=AccessorType.SETTER)
		@RuneAttribute("@ref")
		@Override
		public ReferenceWithMetaChild.ReferenceWithMetaChildBuilder setGlobalReference(String _globalReference) {
			this.globalReference = _globalReference == null ? null : _globalReference;
			return this;
		}
		
		@RosettaAttribute(value="externalReference", isRequired=false, isMulti=false, accessorType=AccessorType.SETTER)
		@RuneAttribute("@ref:external")
		@Override
		public ReferenceWithMetaChild.ReferenceWithMetaChildBuilder setExternalReference(String _externalReference) {
			this.externalReference = _externalReference == null ? null : _externalReference;
			return this;
		}
		
		@RosettaAttribute(value="address", isRequired=false, isMulti=false, accessorType=AccessorType.SETTER)
		@RuneAttribute("@ref:scoped")
		@RuneMetaType
		@Override
		public ReferenceWithMetaChild.ReferenceWithMetaChildBuilder setReference(Reference _reference) {
			this.reference = _reference == null ? null : _reference.toBuilder();
			return this;
		}
		
		@Override
		public ReferenceWithMetaChild build() {
			return new ReferenceWithMetaChild.ReferenceWithMetaChildImpl(this);
		}
		
		@Override
		public ReferenceWithMetaChild.ReferenceWithMetaChildBuilder toBuilder() {
			return this;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public ReferenceWithMetaChild.ReferenceWithMetaChildBuilder prune() {
			if (value!=null && !value.prune().hasData()) value = null;
			if (reference!=null && !reference.prune().hasData()) reference = null;
			return this;
		}
		
		@Override
		public boolean hasData() {
			if (getValue()!=null && getValue().hasData()) return true;
			if (getGlobalReference()!=null) return true;
			if (getExternalReference()!=null) return true;
			if (getReference()!=null && getReference().hasData()) return true;
			return false;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public ReferenceWithMetaChild.ReferenceWithMetaChildBuilder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
			ReferenceWithMetaChild.ReferenceWithMetaChildBuilder o = (ReferenceWithMetaChild.ReferenceWithMetaChildBuilder) other;
			
			merger.mergeRosetta(getValue(), o.getValue(), this::setValue);
			merger.mergeRosetta(getReference(), o.getReference(), this::setReference);
			
			merger.mergeBasic(getGlobalReference(), o.getGlobalReference(), this::setGlobalReference);
			merger.mergeBasic(getExternalReference(), o.getExternalReference(), this::setExternalReference);
			return this;
		}
	
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			ReferenceWithMetaChild _that = getType().cast(o);
		
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
			return "ReferenceWithMetaChildBuilder {" +
				"value=" + this.value + ", " +
				"globalReference=" + this.globalReference + ", " +
				"externalReference=" + this.externalReference + ", " +
				"reference=" + this.reference +
			'}';
		}
	}
}

class ReferenceWithMetaChildMeta extends BasicRosettaMetaData<ReferenceWithMetaChild> {

}
