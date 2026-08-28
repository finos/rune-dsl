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
import test.condition.meta.SimpleMeta;

import static java.util.Optional.ofNullable;

/**
 * @version 0.0.0
 */
@RosettaDataType(value="Simple", builder=Simple.SimpleBuilderImpl.class, version="0.0.0")
@RuneDataType(value="Simple", model="test", builder=Simple.SimpleBuilderImpl.class, version="0.0.0")
public interface Simple extends RosettaModelObject {

	SimpleMeta metaData = new SimpleMeta();

	/*********************** Getter Methods  ***********************/
	String getVal();

	/*********************** Build Methods  ***********************/
	Simple build();
	
	Simple.SimpleBuilder toBuilder();
	
	static Simple.SimpleBuilder builder() {
		return new Simple.SimpleBuilderImpl();
	}

	/*********************** Utility Methods  ***********************/
	@Override
	default RosettaMetaData<? extends Simple> metaData() {
		return metaData;
	}
	
	@Override
	@RuneAttribute("@type")
	default Class<? extends Simple> getType() {
		return Simple.class;
	}
	
	@Override
	default void process(RosettaPath path, Processor processor) {
		processor.processBasic(path.newSubPath("val"), String.class, getVal(), this);
	}
	

	/*********************** Builder Interface  ***********************/
	interface SimpleBuilder extends Simple, RosettaModelObjectBuilder {
		Simple.SimpleBuilder setVal(String val);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processor.processBasic(path.newSubPath("val"), String.class, getVal(), this);
		}
		

		Simple.SimpleBuilder prune();
	}

	/*********************** Immutable Implementation of Simple  ***********************/
	class SimpleImpl implements Simple {
		private final String val;
		
		protected SimpleImpl(Simple.SimpleBuilder builder) {
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
		public Simple build() {
			return this;
		}
		
		@Override
		public Simple.SimpleBuilder toBuilder() {
			Simple.SimpleBuilder builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(Simple.SimpleBuilder builder) {
			ofNullable(getVal()).ifPresent(builder::setVal);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			Simple _that = getType().cast(o);
		
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
			return "Simple {" +
				"val=" + this.val +
			'}';
		}
	}

	/*********************** Builder Implementation of Simple  ***********************/
	class SimpleBuilderImpl implements Simple.SimpleBuilder {
	
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
		public Simple.SimpleBuilder setVal(String _val) {
			this.val = _val == null ? null : _val;
			return this;
		}
		
		@Override
		public Simple build() {
			return new Simple.SimpleImpl(this);
		}
		
		@Override
		public Simple.SimpleBuilder toBuilder() {
			return this;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public Simple.SimpleBuilder prune() {
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
		
			Simple _that = getType().cast(o);
		
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
			return "SimpleBuilder {" +
				"val=" + this.val +
			'}';
		}
	}
}
