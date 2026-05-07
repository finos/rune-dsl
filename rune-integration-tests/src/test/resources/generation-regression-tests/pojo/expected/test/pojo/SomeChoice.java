package test.pojo;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.annotations.Accessor;
import com.rosetta.model.lib.annotations.AccessorType;
import com.rosetta.model.lib.annotations.RosettaAttribute;
import com.rosetta.model.lib.annotations.RosettaDataType;
import com.rosetta.model.lib.annotations.RuneAttribute;
import com.rosetta.model.lib.annotations.RuneDataType;
import com.rosetta.model.lib.annotations.RuneChoiceType;
import com.rosetta.model.lib.meta.RosettaMetaData;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.process.BuilderMerger;
import com.rosetta.model.lib.process.BuilderProcessor;
import com.rosetta.model.lib.process.Processor;
import java.util.Objects;
import test.pojo.meta.SomeChoiceMeta;

import static java.util.Optional.ofNullable;

/**
 * @version 0.0.0
 */
@RosettaDataType(value="SomeChoice", builder=SomeChoice.SomeChoiceBuilderImpl.class, version="0.0.0")
@RuneDataType(value="SomeChoice", model="test", builder=SomeChoice.SomeChoiceBuilderImpl.class, version="0.0.0")
@RuneChoiceType
public interface SomeChoice extends RosettaModelObject {

	SomeChoiceMeta metaData = new SomeChoiceMeta();

	/*********************** Getter Methods  ***********************/
	Foo getFoo();
	Bar getBar();

	/*********************** Build Methods  ***********************/
	SomeChoice build();
	
	SomeChoice.SomeChoiceBuilder toBuilder();
	
	static SomeChoice.SomeChoiceBuilder builder() {
		return new SomeChoice.SomeChoiceBuilderImpl();
	}

	/*********************** Utility Methods  ***********************/
	@Override
	default RosettaMetaData<? extends SomeChoice> metaData() {
		return metaData;
	}
	
	@Override
	@RuneAttribute("@type")
	default Class<? extends SomeChoice> getType() {
		return SomeChoice.class;
	}
	
	@Override
	default void process(RosettaPath path, Processor processor) {
		processRosetta(path.newSubPath("Foo"), processor, Foo.class, getFoo());
		processRosetta(path.newSubPath("Bar"), processor, Bar.class, getBar());
	}
	

	/*********************** Builder Interface  ***********************/
	interface SomeChoiceBuilder extends SomeChoice, RosettaModelObjectBuilder {
		Foo.FooBuilder getOrCreateFoo();
		@Override
		Foo.FooBuilder getFoo();
		Bar.BarBuilder getOrCreateBar();
		@Override
		Bar.BarBuilder getBar();
		SomeChoice.SomeChoiceBuilder setFoo(Foo _Foo);
		SomeChoice.SomeChoiceBuilder setBar(Bar _Bar);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processRosetta(path.newSubPath("Foo"), processor, Foo.FooBuilder.class, getFoo());
			processRosetta(path.newSubPath("Bar"), processor, Bar.BarBuilder.class, getBar());
		}
		

		SomeChoice.SomeChoiceBuilder prune();
	}

	/*********************** Immutable Implementation of SomeChoice  ***********************/
	class SomeChoiceImpl implements SomeChoice {
		private final Foo foo;
		private final Bar bar;
		
		protected SomeChoiceImpl(SomeChoice.SomeChoiceBuilder builder) {
			this.foo = ofNullable(builder.getFoo()).map(f->f.build()).orElse(null);
			this.bar = ofNullable(builder.getBar()).map(f->f.build()).orElse(null);
		}
		
		@Override
		@RosettaAttribute("Foo")
		@Accessor(AccessorType.GETTER)
		@RuneAttribute("Foo")
		public Foo getFoo() {
			return foo;
		}
		
		@Override
		@RosettaAttribute("Bar")
		@Accessor(AccessorType.GETTER)
		@RuneAttribute("Bar")
		public Bar getBar() {
			return bar;
		}
		
		@Override
		public SomeChoice build() {
			return this;
		}
		
		@Override
		public SomeChoice.SomeChoiceBuilder toBuilder() {
			SomeChoice.SomeChoiceBuilder builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(SomeChoice.SomeChoiceBuilder builder) {
			ofNullable(getFoo()).ifPresent(builder::setFoo);
			ofNullable(getBar()).ifPresent(builder::setBar);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			SomeChoice _that = getType().cast(o);
		
			if (!Objects.equals(foo, _that.getFoo())) return false;
			if (!Objects.equals(bar, _that.getBar())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (foo != null ? foo.hashCode() : 0);
			_result = 31 * _result + (bar != null ? bar.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "SomeChoice {" +
				"Foo=" + this.foo + ", " +
				"Bar=" + this.bar +
			'}';
		}
	}

	/*********************** Builder Implementation of SomeChoice  ***********************/
	class SomeChoiceBuilderImpl implements SomeChoice.SomeChoiceBuilder {
	
		protected Foo.FooBuilder foo;
		protected Bar.BarBuilder bar;
		
		@Override
		@RosettaAttribute("Foo")
		@Accessor(AccessorType.GETTER)
		@RuneAttribute("Foo")
		public Foo.FooBuilder getFoo() {
			return foo;
		}
		
		@Override
		public Foo.FooBuilder getOrCreateFoo() {
			Foo.FooBuilder result;
			if (foo!=null) {
				result = foo;
			}
			else {
				result = foo = Foo.builder();
			}
			
			return result;
		}
		
		@Override
		@RosettaAttribute("Bar")
		@Accessor(AccessorType.GETTER)
		@RuneAttribute("Bar")
		public Bar.BarBuilder getBar() {
			return bar;
		}
		
		@Override
		public Bar.BarBuilder getOrCreateBar() {
			Bar.BarBuilder result;
			if (bar!=null) {
				result = bar;
			}
			else {
				result = bar = Bar.builder();
			}
			
			return result;
		}
		
		@RosettaAttribute("Foo")
		@Accessor(AccessorType.SETTER)
		@RuneAttribute("Foo")
		@Override
		public SomeChoice.SomeChoiceBuilder setFoo(Foo _foo) {
			this.foo = _foo == null ? null : _foo.toBuilder();
			return this;
		}
		
		@RosettaAttribute("Bar")
		@Accessor(AccessorType.SETTER)
		@RuneAttribute("Bar")
		@Override
		public SomeChoice.SomeChoiceBuilder setBar(Bar _bar) {
			this.bar = _bar == null ? null : _bar.toBuilder();
			return this;
		}
		
		@Override
		public SomeChoice build() {
			return new SomeChoice.SomeChoiceImpl(this);
		}
		
		@Override
		public SomeChoice.SomeChoiceBuilder toBuilder() {
			return this;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public SomeChoice.SomeChoiceBuilder prune() {
			if (foo!=null && !foo.prune().hasData()) foo = null;
			if (bar!=null && !bar.prune().hasData()) bar = null;
			return this;
		}
		
		@Override
		public boolean hasData() {
			if (getFoo()!=null && getFoo().hasData()) return true;
			if (getBar()!=null && getBar().hasData()) return true;
			return false;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public SomeChoice.SomeChoiceBuilder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
			SomeChoice.SomeChoiceBuilder o = (SomeChoice.SomeChoiceBuilder) other;
			
			merger.mergeRosetta(getFoo(), o.getFoo(), this::setFoo);
			merger.mergeRosetta(getBar(), o.getBar(), this::setBar);
			
			return this;
		}
	
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			SomeChoice _that = getType().cast(o);
		
			if (!Objects.equals(foo, _that.getFoo())) return false;
			if (!Objects.equals(bar, _that.getBar())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (foo != null ? foo.hashCode() : 0);
			_result = 31 * _result + (bar != null ? bar.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "SomeChoiceBuilder {" +
				"Foo=" + this.foo + ", " +
				"Bar=" + this.bar +
			'}';
		}
	}
}
