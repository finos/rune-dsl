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
import test.condition.meta.EscapedMeta;

import static java.util.Optional.ofNullable;

/**
 * @version 0.0.0
 */
@RosettaDataType(value="Escaped", builder=Escaped.EscapedBuilderImpl.class, version="0.0.0")
@RuneDataType(value="Escaped", model="test", builder=Escaped.EscapedBuilderImpl.class, version="0.0.0")
public interface Escaped extends RosettaModelObject {

	EscapedMeta metaData = new EscapedMeta();

	/*********************** Getter Methods  ***********************/
	String getVal();

	/*********************** Build Methods  ***********************/
	Escaped build();
	
	Escaped.EscapedBuilder toBuilder();
	
	static Escaped.EscapedBuilder builder() {
		return new Escaped.EscapedBuilderImpl();
	}

	/*********************** Utility Methods  ***********************/
	@Override
	default RosettaMetaData<? extends Escaped> metaData() {
		return metaData;
	}
	
	@Override
	@RuneAttribute("@type")
	default Class<? extends Escaped> getType() {
		return Escaped.class;
	}
	
	@Override
	default void process(RosettaPath path, Processor processor) {
		processor.processBasic(path.newSubPath("val"), String.class, getVal(), this);
	}
	

	/*********************** Builder Interface  ***********************/
	interface EscapedBuilder extends Escaped, RosettaModelObjectBuilder {
		Escaped.EscapedBuilder setVal(String val);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processor.processBasic(path.newSubPath("val"), String.class, getVal(), this);
		}
		

		Escaped.EscapedBuilder prune();
	}

	/*********************** Immutable Implementation of Escaped  ***********************/
	class EscapedImpl implements Escaped {
		private final String val;
		
		protected EscapedImpl(Escaped.EscapedBuilder builder) {
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
		public Escaped build() {
			return this;
		}
		
		@Override
		public Escaped.EscapedBuilder toBuilder() {
			Escaped.EscapedBuilder builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(Escaped.EscapedBuilder builder) {
			ofNullable(getVal()).ifPresent(builder::setVal);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			Escaped _that = getType().cast(o);
		
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
			return "Escaped {" +
				"val=" + this.val +
			'}';
		}
	}

	/*********************** Builder Implementation of Escaped  ***********************/
	class EscapedBuilderImpl implements Escaped.EscapedBuilder {
	
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
		public Escaped.EscapedBuilder setVal(String _val) {
			this.val = _val == null ? null : _val;
			return this;
		}
		
		@Override
		public Escaped build() {
			return new Escaped.EscapedImpl(this);
		}
		
		@Override
		public Escaped.EscapedBuilder toBuilder() {
			return this;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public Escaped.EscapedBuilder prune() {
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
		
			Escaped _that = getType().cast(o);
		
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
			return "EscapedBuilder {" +
				"val=" + this.val +
			'}';
		}
	}
}
