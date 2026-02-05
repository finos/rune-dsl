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
import com.rosetta.model.lib.meta.RosettaMetaData;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.process.BuilderMerger;
import com.rosetta.model.lib.process.BuilderProcessor;
import com.rosetta.model.lib.process.Processor;
import java.util.Objects;
import test.pojo.meta.BarMeta;

import static java.util.Optional.ofNullable;

/**
 * @version 0.0.0
 */
@RosettaDataType(value="Bar", builder=Bar.BarBuilderImpl.class, version="0.0.0")
@RuneDataType(value="Bar", model="test", builder=Bar.BarBuilderImpl.class, version="0.0.0")
public interface Bar extends RosettaModelObject {

	BarMeta metaData = new BarMeta();

	/*********************** Getter Methods  ***********************/
	Qux getBar();

	/*********************** Build Methods  ***********************/
	Bar build();
	
	Bar.BarBuilder toBuilder();
	
	static Bar.BarBuilder builder() {
		return new Bar.BarBuilderImpl();
	}

	/*********************** Utility Methods  ***********************/
	@Override
	default RosettaMetaData<? extends Bar> metaData() {
		return metaData;
	}
	
	@Override
	@RuneAttribute("@type")
	default Class<? extends Bar> getType() {
		return Bar.class;
	}
	
	@Override
	default void process(RosettaPath path, Processor processor) {
		processRosetta(path.newSubPath("bar"), processor, Qux.class, getBar());
	}
	

	/*********************** Builder Interface  ***********************/
	interface BarBuilder extends Bar, RosettaModelObjectBuilder {
		Qux.QuxBuilder getOrCreateBar();
		@Override
		Qux.QuxBuilder getBar();
		Bar.BarBuilder setBar(Qux bar);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processRosetta(path.newSubPath("bar"), processor, Qux.QuxBuilder.class, getBar());
		}
		

		Bar.BarBuilder prune();
	}

	/*********************** Immutable Implementation of Bar  ***********************/
	class BarImpl implements Bar {
		private final Qux bar;
		
		protected BarImpl(Bar.BarBuilder builder) {
			this.bar = ofNullable(builder.getBar()).map(f->f.build()).orElse(null);
		}
		
		@Override
		@RosettaAttribute("bar")
		@Accessor(AccessorType.GETTER)
		@Required
		@RuneAttribute("bar")
		public Qux getBar() {
			return bar;
		}
		
		@Override
		public Bar build() {
			return this;
		}
		
		@Override
		public Bar.BarBuilder toBuilder() {
			Bar.BarBuilder builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(Bar.BarBuilder builder) {
			ofNullable(getBar()).ifPresent(builder::setBar);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			Bar _that = getType().cast(o);
		
			if (!Objects.equals(bar, _that.getBar())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (bar != null ? bar.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "Bar {" +
				"bar=" + this.bar +
			'}';
		}
	}

	/*********************** Builder Implementation of Bar  ***********************/
	class BarBuilderImpl implements Bar.BarBuilder {
	
		protected Qux.QuxBuilder bar;
		
		@Override
		@RosettaAttribute("bar")
		@Accessor(AccessorType.GETTER)
		@Required
		@RuneAttribute("bar")
		public Qux.QuxBuilder getBar() {
			return bar;
		}
		
		@Override
		public Qux.QuxBuilder getOrCreateBar() {
			Qux.QuxBuilder result;
			if (bar!=null) {
				result = bar;
			}
			else {
				result = bar = Qux.builder();
			}
			
			return result;
		}
		
		@RosettaAttribute("bar")
		@Accessor(AccessorType.SETTER)
		@Required
		@RuneAttribute("bar")
		@Override
		public Bar.BarBuilder setBar(Qux _bar) {
			this.bar = _bar == null ? null : _bar.toBuilder();
			return this;
		}
		
		@Override
		public Bar build() {
			return new Bar.BarImpl(this);
		}
		
		@Override
		public Bar.BarBuilder toBuilder() {
			return this;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public Bar.BarBuilder prune() {
			if (bar!=null && !bar.prune().hasData()) bar = null;
			return this;
		}
		
		@Override
		public boolean hasData() {
			if (getBar()!=null && getBar().hasData()) return true;
			return false;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public Bar.BarBuilder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
			Bar.BarBuilder o = (Bar.BarBuilder) other;
			
			merger.mergeRosetta(getBar(), o.getBar(), this::setBar);
			
			return this;
		}
	
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			Bar _that = getType().cast(o);
		
			if (!Objects.equals(bar, _that.getBar())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (bar != null ? bar.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "BarBuilder {" +
				"bar=" + this.bar +
			'}';
		}
	}
}
