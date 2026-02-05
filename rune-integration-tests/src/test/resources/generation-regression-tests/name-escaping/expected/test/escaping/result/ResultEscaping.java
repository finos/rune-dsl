package test.escaping.result;

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
import com.rosetta.model.lib.process.BuilderMerger;
import com.rosetta.model.lib.process.BuilderProcessor;
import com.rosetta.model.lib.process.Processor;
import java.util.Objects;
import test.escaping.result.meta.ResultEscapingMeta;

import static java.util.Optional.ofNullable;

/**
 * @version 0.0.0
 */
@RosettaDataType(value="ResultEscaping", builder=ResultEscaping.ResultEscapingBuilderImpl.class, version="0.0.0")
@RuneDataType(value="ResultEscaping", model="test", builder=ResultEscaping.ResultEscapingBuilderImpl.class, version="0.0.0")
public interface ResultEscaping extends RosettaModelObject {

	ResultEscapingMeta metaData = new ResultEscapingMeta();

	/*********************** Getter Methods  ***********************/
	Foo getResult();

	/*********************** Build Methods  ***********************/
	ResultEscaping build();
	
	ResultEscaping.ResultEscapingBuilder toBuilder();
	
	static ResultEscaping.ResultEscapingBuilder builder() {
		return new ResultEscaping.ResultEscapingBuilderImpl();
	}

	/*********************** Utility Methods  ***********************/
	@Override
	default RosettaMetaData<? extends ResultEscaping> metaData() {
		return metaData;
	}
	
	@Override
	@RuneAttribute("@type")
	default Class<? extends ResultEscaping> getType() {
		return ResultEscaping.class;
	}
	
	@Override
	default void process(RosettaPath path, Processor processor) {
		processRosetta(path.newSubPath("result"), processor, Foo.class, getResult());
	}
	

	/*********************** Builder Interface  ***********************/
	interface ResultEscapingBuilder extends ResultEscaping, RosettaModelObjectBuilder {
		Foo.FooBuilder getOrCreateResult();
		@Override
		Foo.FooBuilder getResult();
		ResultEscaping.ResultEscapingBuilder setResult(Foo result);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processRosetta(path.newSubPath("result"), processor, Foo.FooBuilder.class, getResult());
		}
		

		ResultEscaping.ResultEscapingBuilder prune();
	}

	/*********************** Immutable Implementation of ResultEscaping  ***********************/
	class ResultEscapingImpl implements ResultEscaping {
		private final Foo result;
		
		protected ResultEscapingImpl(ResultEscaping.ResultEscapingBuilder builder) {
			this.result = ofNullable(builder.getResult()).map(f->f.build()).orElse(null);
		}
		
		@Override
		@RosettaAttribute("result")
		@Accessor(AccessorType.GETTER)
		@Required
		@RuneAttribute("result")
		public Foo getResult() {
			return result;
		}
		
		@Override
		public ResultEscaping build() {
			return this;
		}
		
		@Override
		public ResultEscaping.ResultEscapingBuilder toBuilder() {
			ResultEscaping.ResultEscapingBuilder builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(ResultEscaping.ResultEscapingBuilder builder) {
			ofNullable(getResult()).ifPresent(builder::setResult);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			ResultEscaping _that = getType().cast(o);
		
			if (!Objects.equals(result, _that.getResult())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (result != null ? result.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "ResultEscaping {" +
				"result=" + this.result +
			'}';
		}
	}

	/*********************** Builder Implementation of ResultEscaping  ***********************/
	class ResultEscapingBuilderImpl implements ResultEscaping.ResultEscapingBuilder {
	
		protected Foo.FooBuilder result;
		
		@Override
		@RosettaAttribute("result")
		@Accessor(AccessorType.GETTER)
		@Required
		@RuneAttribute("result")
		public Foo.FooBuilder getResult() {
			return result;
		}
		
		@Override
		public Foo.FooBuilder getOrCreateResult() {
			Foo.FooBuilder _result;
			if (result!=null) {
				_result = result;
			}
			else {
				_result = result = Foo.builder();
			}
			
			return _result;
		}
		
		@RosettaAttribute("result")
		@Accessor(AccessorType.SETTER)
		@Required
		@RuneAttribute("result")
		@Override
		public ResultEscaping.ResultEscapingBuilder setResult(Foo _result) {
			this.result = _result == null ? null : _result.toBuilder();
			return this;
		}
		
		@Override
		public ResultEscaping build() {
			return new ResultEscaping.ResultEscapingImpl(this);
		}
		
		@Override
		public ResultEscaping.ResultEscapingBuilder toBuilder() {
			return this;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public ResultEscaping.ResultEscapingBuilder prune() {
			if (result!=null && !result.prune().hasData()) result = null;
			return this;
		}
		
		@Override
		public boolean hasData() {
			if (getResult()!=null && getResult().hasData()) return true;
			return false;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public ResultEscaping.ResultEscapingBuilder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
			ResultEscaping.ResultEscapingBuilder o = (ResultEscaping.ResultEscapingBuilder) other;
			
			merger.mergeRosetta(getResult(), o.getResult(), this::setResult);
			
			return this;
		}
	
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			ResultEscaping _that = getType().cast(o);
		
			if (!Objects.equals(result, _that.getResult())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (result != null ? result.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "ResultEscapingBuilder {" +
				"result=" + this.result +
			'}';
		}
	}
}
