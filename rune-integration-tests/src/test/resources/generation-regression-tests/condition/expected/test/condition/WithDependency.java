package test.condition;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.annotations.Accessor;
import com.rosetta.model.lib.annotations.AccessorType;
import com.rosetta.model.lib.annotations.Required;
import com.rosetta.model.lib.annotations.RosettaAttribute;
import com.rosetta.model.lib.annotations.RosettaDataType;
import com.rosetta.model.lib.annotations.RuneAttribute;
import com.rosetta.model.lib.annotations.RuneDataType;
import com.rosetta.model.lib.meta.RosettaMetaData;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.process.BuilderProcessor;
import com.rosetta.model.lib.process.Processor;
import java.util.Objects;
import test.condition.meta.WithDependencyMeta;

import static java.util.Optional.ofNullable;

/**
 * @version 0.0.0
 */
@RosettaDataType(value="WithDependency", builder=WithDependency.WithDependencyBuilderImpl.class, version="0.0.0")
@RuneDataType(value="WithDependency", model="test", builder=WithDependency.WithDependencyBuilderImpl.class, version="0.0.0")
public interface WithDependency extends RosettaModelObject {

	WithDependencyMeta metaData = new WithDependencyMeta();

	/*********************** Getter Methods  ***********************/
	String getVal();

	/*********************** Build Methods  ***********************/
	WithDependency build();
	
	WithDependency.WithDependencyBuilder toBuilder();
	
	static WithDependency.WithDependencyBuilder builder() {
		return new WithDependency.WithDependencyBuilderImpl();
	}

	/*********************** Utility Methods  ***********************/
	@Override
	default RosettaMetaData<? extends WithDependency> metaData() {
		return metaData;
	}
	
	@Override
	@RuneAttribute("@type")
	default Class<? extends WithDependency> getType() {
		return WithDependency.class;
	}
	
	@Override
	default void process(RosettaPath path, Processor processor) {
		processor.processBasic(path.newSubPath("val"), String.class, getVal(), this);
	}
	

	/*********************** Builder Interface  ***********************/
	interface WithDependencyBuilder extends WithDependency, RosettaModelObjectBuilder {
		WithDependency.WithDependencyBuilder setVal(String val);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processor.processBasic(path.newSubPath("val"), String.class, getVal(), this);
		}
		

		WithDependency.WithDependencyBuilder prune();
	}

	/*********************** Immutable Implementation of WithDependency  ***********************/
	class WithDependencyImpl implements WithDependency {
		private final String val;
		
		protected WithDependencyImpl(WithDependency.WithDependencyBuilder builder) {
			this.val = builder.getVal();
		}
		
		@Override
		@RosettaAttribute("val")
		@Accessor(AccessorType.GETTER)
		@Required
		@RuneAttribute("val")
		public String getVal() {
			return val;
		}
		
		@Override
		public WithDependency build() {
			return this;
		}
		
		@Override
		public WithDependency.WithDependencyBuilder toBuilder() {
			WithDependency.WithDependencyBuilder builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(WithDependency.WithDependencyBuilder builder) {
			ofNullable(getVal()).ifPresent(builder::setVal);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			WithDependency _that = getType().cast(o);
		
			if (!Objects.equals(val, _that.getVal())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (val != null ? val.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "WithDependency {" +
				"val=" + this.val +
			'}';
		}
	}

	/*********************** Builder Implementation of WithDependency  ***********************/
	class WithDependencyBuilderImpl implements WithDependency.WithDependencyBuilder {
	
		protected String val;
		
		@Override
		@RosettaAttribute("val")
		@Accessor(AccessorType.GETTER)
		@Required
		@RuneAttribute("val")
		public String getVal() {
			return val;
		}
		
		@RosettaAttribute("val")
		@Accessor(AccessorType.SETTER)
		@Required
		@RuneAttribute("val")
		@Override
		public WithDependency.WithDependencyBuilder setVal(String _val) {
			this.val = _val == null ? null : _val;
			return this;
		}
		
		@Override
		public WithDependency build() {
			return new WithDependency.WithDependencyImpl(this);
		}
		
		@Override
		public WithDependency.WithDependencyBuilder toBuilder() {
			return this;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public WithDependency.WithDependencyBuilder prune() {
			return this;
		}
		
		@Override
		public boolean hasData() {
			if (getVal()!=null) return true;
			return false;
		}
	
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			WithDependency _that = getType().cast(o);
		
			if (!Objects.equals(val, _that.getVal())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (val != null ? val.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "WithDependencyBuilder {" +
				"val=" + this.val +
			'}';
		}
	}
}
