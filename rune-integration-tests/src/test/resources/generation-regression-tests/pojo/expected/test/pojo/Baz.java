package test.pojo;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.annotations.Accessor;
import com.rosetta.model.lib.annotations.AccessorType;
import com.rosetta.model.lib.annotations.Required;
import com.rosetta.model.lib.annotations.RosettaAttribute;
import com.rosetta.model.lib.annotations.RosettaDataType;
import com.rosetta.model.lib.annotations.RuneAttribute;
import com.rosetta.model.lib.annotations.RuneDataType;
import com.rosetta.model.lib.annotations.RuneScopedAttributeReference;
import com.rosetta.model.lib.meta.RosettaMetaData;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.process.BuilderMerger;
import com.rosetta.model.lib.process.BuilderProcessor;
import com.rosetta.model.lib.process.Processor;
import com.rosetta.model.metafields.ReferenceWithMetaString;
import java.util.Objects;
import test.pojo.meta.BazMeta;

import static java.util.Optional.ofNullable;

/**
 * @version 0.0.0
 */
@RosettaDataType(value="Baz", builder=Baz.BazBuilderImpl.class, version="0.0.0")
@RuneDataType(value="Baz", model="test", builder=Baz.BazBuilderImpl.class, version="0.0.0")
public interface Baz extends RosettaModelObject {

	BazMeta metaData = new BazMeta();

	/*********************** Getter Methods  ***********************/
	ReferenceWithMetaString getBaz();

	/*********************** Build Methods  ***********************/
	Baz build();
	
	Baz.BazBuilder toBuilder();
	
	static Baz.BazBuilder builder() {
		return new Baz.BazBuilderImpl();
	}

	/*********************** Utility Methods  ***********************/
	@Override
	default RosettaMetaData<? extends Baz> metaData() {
		return metaData;
	}
	
	@Override
	@RuneAttribute("@type")
	default Class<? extends Baz> getType() {
		return Baz.class;
	}
	
	@Override
	default void process(RosettaPath path, Processor processor) {
		processRosetta(path.newSubPath("baz"), processor, ReferenceWithMetaString.class, getBaz());
	}
	

	/*********************** Builder Interface  ***********************/
	interface BazBuilder extends Baz, RosettaModelObjectBuilder {
		ReferenceWithMetaString.ReferenceWithMetaStringBuilder getOrCreateBaz();
		@Override
		ReferenceWithMetaString.ReferenceWithMetaStringBuilder getBaz();
		Baz.BazBuilder setBaz(ReferenceWithMetaString baz);
		Baz.BazBuilder setBazValue(String baz);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processRosetta(path.newSubPath("baz"), processor, ReferenceWithMetaString.ReferenceWithMetaStringBuilder.class, getBaz());
		}
		

		Baz.BazBuilder prune();
	}

	/*********************** Immutable Implementation of Baz  ***********************/
	class BazImpl implements Baz {
		private final ReferenceWithMetaString baz;
		
		protected BazImpl(Baz.BazBuilder builder) {
			this.baz = ofNullable(builder.getBaz()).map(f->f.build()).orElse(null);
		}
		
		@Override
		@RosettaAttribute("baz")
		@Accessor(AccessorType.GETTER)
		@Required
		@RuneAttribute("baz")
		@RuneScopedAttributeReference
		public ReferenceWithMetaString getBaz() {
			return baz;
		}
		
		@Override
		public Baz build() {
			return this;
		}
		
		@Override
		public Baz.BazBuilder toBuilder() {
			Baz.BazBuilder builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(Baz.BazBuilder builder) {
			ofNullable(getBaz()).ifPresent(builder::setBaz);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			Baz _that = getType().cast(o);
		
			if (!Objects.equals(baz, _that.getBaz())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (baz != null ? baz.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "Baz {" +
				"baz=" + this.baz +
			'}';
		}
	}

	/*********************** Builder Implementation of Baz  ***********************/
	class BazBuilderImpl implements Baz.BazBuilder {
	
		protected ReferenceWithMetaString.ReferenceWithMetaStringBuilder baz;
		
		@Override
		@RosettaAttribute("baz")
		@Accessor(AccessorType.GETTER)
		@Required
		@RuneAttribute("baz")
		@RuneScopedAttributeReference
		public ReferenceWithMetaString.ReferenceWithMetaStringBuilder getBaz() {
			return baz;
		}
		
		@Override
		public ReferenceWithMetaString.ReferenceWithMetaStringBuilder getOrCreateBaz() {
			ReferenceWithMetaString.ReferenceWithMetaStringBuilder result;
			if (baz!=null) {
				result = baz;
			}
			else {
				result = baz = ReferenceWithMetaString.builder();
			}
			
			return result;
		}
		
		@RosettaAttribute("baz")
		@Accessor(AccessorType.SETTER)
		@Required
		@RuneAttribute("baz")
		@RuneScopedAttributeReference
		@Override
		public Baz.BazBuilder setBaz(ReferenceWithMetaString _baz) {
			this.baz = _baz == null ? null : _baz.toBuilder();
			return this;
		}
		
		@Override
		public Baz.BazBuilder setBazValue(String _baz) {
			this.getOrCreateBaz().setValue(_baz);
			return this;
		}
		
		@Override
		public Baz build() {
			return new Baz.BazImpl(this);
		}
		
		@Override
		public Baz.BazBuilder toBuilder() {
			return this;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public Baz.BazBuilder prune() {
			if (baz!=null && !baz.prune().hasData()) baz = null;
			return this;
		}
		
		@Override
		public boolean hasData() {
			if (getBaz()!=null) return true;
			return false;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public Baz.BazBuilder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
			Baz.BazBuilder o = (Baz.BazBuilder) other;
			
			merger.mergeRosetta(getBaz(), o.getBaz(), this::setBaz);
			
			return this;
		}
	
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			Baz _that = getType().cast(o);
		
			if (!Objects.equals(baz, _that.getBaz())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (baz != null ? baz.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "BazBuilder {" +
				"baz=" + this.baz +
			'}';
		}
	}
}
