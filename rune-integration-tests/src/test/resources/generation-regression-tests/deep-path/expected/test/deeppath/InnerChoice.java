package test.deeppath;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.annotations.Accessor;
import com.rosetta.model.lib.annotations.AccessorType;
import com.rosetta.model.lib.annotations.RosettaAttribute;
import com.rosetta.model.lib.annotations.RosettaDataType;
import com.rosetta.model.lib.annotations.RuneAttribute;
import com.rosetta.model.lib.annotations.RuneChoiceType;
import com.rosetta.model.lib.annotations.RuneDataType;
import com.rosetta.model.lib.meta.RosettaMetaData;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.process.BuilderMerger;
import com.rosetta.model.lib.process.BuilderProcessor;
import com.rosetta.model.lib.process.Processor;
import java.util.Objects;
import test.deeppath.meta.InnerChoiceMeta;

import static java.util.Optional.ofNullable;

/**
 * @version 0.0.0
 */
@RosettaDataType(value="InnerChoice", builder=InnerChoice.InnerChoiceBuilderImpl.class, version="0.0.0")
@RuneDataType(value="InnerChoice", model="test", builder=InnerChoice.InnerChoiceBuilderImpl.class, version="0.0.0")
@RuneChoiceType
public interface InnerChoice extends RosettaModelObject {

	InnerChoiceMeta metaData = new InnerChoiceMeta();

	/*********************** Getter Methods  ***********************/
	Option1 getOption1();
	Option2 getOption2();

	/*********************** Build Methods  ***********************/
	InnerChoice build();
	
	InnerChoice.InnerChoiceBuilder toBuilder();
	
	static InnerChoice.InnerChoiceBuilder builder() {
		return new InnerChoice.InnerChoiceBuilderImpl();
	}

	/*********************** Utility Methods  ***********************/
	@Override
	default RosettaMetaData<? extends InnerChoice> metaData() {
		return metaData;
	}
	
	@Override
	@RuneAttribute("@type")
	default Class<? extends InnerChoice> getType() {
		return InnerChoice.class;
	}
	
	@Override
	default void process(RosettaPath path, Processor processor) {
		processRosetta(path.newSubPath("Option1"), processor, Option1.class, getOption1());
		processRosetta(path.newSubPath("Option2"), processor, Option2.class, getOption2());
	}
	

	/*********************** Builder Interface  ***********************/
	interface InnerChoiceBuilder extends InnerChoice, RosettaModelObjectBuilder {
		Option1.Option1Builder getOrCreateOption1();
		@Override
		Option1.Option1Builder getOption1();
		Option2.Option2Builder getOrCreateOption2();
		@Override
		Option2.Option2Builder getOption2();
		InnerChoice.InnerChoiceBuilder setOption1(Option1 _Option1);
		InnerChoice.InnerChoiceBuilder setOption2(Option2 _Option2);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processRosetta(path.newSubPath("Option1"), processor, Option1.Option1Builder.class, getOption1());
			processRosetta(path.newSubPath("Option2"), processor, Option2.Option2Builder.class, getOption2());
		}
		

		InnerChoice.InnerChoiceBuilder prune();
	}

	/*********************** Immutable Implementation of InnerChoice  ***********************/
	class InnerChoiceImpl implements InnerChoice {
		private final Option1 option1;
		private final Option2 option2;
		
		protected InnerChoiceImpl(InnerChoice.InnerChoiceBuilder builder) {
			this.option1 = ofNullable(builder.getOption1()).map(f->f.build()).orElse(null);
			this.option2 = ofNullable(builder.getOption2()).map(f->f.build()).orElse(null);
		}
		
		@Override
		@RosettaAttribute("Option1")
		@Accessor(AccessorType.GETTER)
		@RuneAttribute("Option1")
		public Option1 getOption1() {
			return option1;
		}
		
		@Override
		@RosettaAttribute("Option2")
		@Accessor(AccessorType.GETTER)
		@RuneAttribute("Option2")
		public Option2 getOption2() {
			return option2;
		}
		
		@Override
		public InnerChoice build() {
			return this;
		}
		
		@Override
		public InnerChoice.InnerChoiceBuilder toBuilder() {
			InnerChoice.InnerChoiceBuilder builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(InnerChoice.InnerChoiceBuilder builder) {
			ofNullable(getOption1()).ifPresent(builder::setOption1);
			ofNullable(getOption2()).ifPresent(builder::setOption2);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			InnerChoice _that = getType().cast(o);
		
			if (!Objects.equals(option1, _that.getOption1())) return false;
			if (!Objects.equals(option2, _that.getOption2())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (option1 != null ? option1.hashCode() : 0);
			_result = 31 * _result + (option2 != null ? option2.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "InnerChoice {" +
				"Option1=" + this.option1 + ", " +
				"Option2=" + this.option2 +
			'}';
		}
	}

	/*********************** Builder Implementation of InnerChoice  ***********************/
	class InnerChoiceBuilderImpl implements InnerChoice.InnerChoiceBuilder {
	
		protected Option1.Option1Builder option1;
		protected Option2.Option2Builder option2;
		
		@Override
		@RosettaAttribute("Option1")
		@Accessor(AccessorType.GETTER)
		@RuneAttribute("Option1")
		public Option1.Option1Builder getOption1() {
			return option1;
		}
		
		@Override
		public Option1.Option1Builder getOrCreateOption1() {
			Option1.Option1Builder result;
			if (option1!=null) {
				result = option1;
			}
			else {
				result = option1 = Option1.builder();
			}
			
			return result;
		}
		
		@Override
		@RosettaAttribute("Option2")
		@Accessor(AccessorType.GETTER)
		@RuneAttribute("Option2")
		public Option2.Option2Builder getOption2() {
			return option2;
		}
		
		@Override
		public Option2.Option2Builder getOrCreateOption2() {
			Option2.Option2Builder result;
			if (option2!=null) {
				result = option2;
			}
			else {
				result = option2 = Option2.builder();
			}
			
			return result;
		}
		
		@RosettaAttribute("Option1")
		@Accessor(AccessorType.SETTER)
		@RuneAttribute("Option1")
		@Override
		public InnerChoice.InnerChoiceBuilder setOption1(Option1 _option1) {
			this.option1 = _option1 == null ? null : _option1.toBuilder();
			return this;
		}
		
		@RosettaAttribute("Option2")
		@Accessor(AccessorType.SETTER)
		@RuneAttribute("Option2")
		@Override
		public InnerChoice.InnerChoiceBuilder setOption2(Option2 _option2) {
			this.option2 = _option2 == null ? null : _option2.toBuilder();
			return this;
		}
		
		@Override
		public InnerChoice build() {
			return new InnerChoice.InnerChoiceImpl(this);
		}
		
		@Override
		public InnerChoice.InnerChoiceBuilder toBuilder() {
			return this;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public InnerChoice.InnerChoiceBuilder prune() {
			if (option1!=null && !option1.prune().hasData()) option1 = null;
			if (option2!=null && !option2.prune().hasData()) option2 = null;
			return this;
		}
		
		@Override
		public boolean hasData() {
			if (getOption1()!=null && getOption1().hasData()) return true;
			if (getOption2()!=null && getOption2().hasData()) return true;
			return false;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public InnerChoice.InnerChoiceBuilder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
			InnerChoice.InnerChoiceBuilder o = (InnerChoice.InnerChoiceBuilder) other;
			
			merger.mergeRosetta(getOption1(), o.getOption1(), this::setOption1);
			merger.mergeRosetta(getOption2(), o.getOption2(), this::setOption2);
			
			return this;
		}
	
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			InnerChoice _that = getType().cast(o);
		
			if (!Objects.equals(option1, _that.getOption1())) return false;
			if (!Objects.equals(option2, _that.getOption2())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (option1 != null ? option1.hashCode() : 0);
			_result = 31 * _result + (option2 != null ? option2.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "InnerChoiceBuilder {" +
				"Option1=" + this.option1 + ", " +
				"Option2=" + this.option2 +
			'}';
		}
	}
}
