package test.pojo;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.annotations.Accessor;
import com.rosetta.model.lib.annotations.AccessorType;
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
import test.pojo.meta.Level1Meta;

import static java.util.Optional.ofNullable;

/**
 * @version 0.0.0
 */
@RosettaDataType(value="Level1", builder=Level1.Level1BuilderImpl.class, version="0.0.0")
@RuneDataType(value="Level1", model="test", builder=Level1.Level1BuilderImpl.class, version="0.0.0")
public interface Level1 extends RosettaModelObject {

	Level1Meta metaData = new Level1Meta();

	/*********************** Getter Methods  ***********************/
	Integer getAttr();

	/*********************** Build Methods  ***********************/
	Level1 build();
	
	Level1.Level1Builder toBuilder();
	
	static Level1.Level1Builder builder() {
		return new Level1.Level1BuilderImpl();
	}

	/*********************** Utility Methods  ***********************/
	@Override
	default RosettaMetaData<? extends Level1> metaData() {
		return metaData;
	}
	
	@Override
	@RuneAttribute("@type")
	default Class<? extends Level1> getType() {
		return Level1.class;
	}
	
	@Override
	default void process(RosettaPath path, Processor processor) {
		processor.processBasic(path.newSubPath("attr"), Integer.class, getAttr(), this);
	}
	

	/*********************** Builder Interface  ***********************/
	interface Level1Builder extends Level1, RosettaModelObjectBuilder {
		Level1.Level1Builder setAttr(Integer attr);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processor.processBasic(path.newSubPath("attr"), Integer.class, getAttr(), this);
		}
		

		Level1.Level1Builder prune();
	}

	/*********************** Immutable Implementation of Level1  ***********************/
	class Level1Impl implements Level1 {
		private final Integer attr;
		
		protected Level1Impl(Level1.Level1Builder builder) {
			this.attr = builder.getAttr();
		}
		
		@Override
		@RosettaAttribute("attr")
		@Accessor(AccessorType.GETTER)
		@RuneAttribute("attr")
		public Integer getAttr() {
			return attr;
		}
		
		@Override
		public Level1 build() {
			return this;
		}
		
		@Override
		public Level1.Level1Builder toBuilder() {
			Level1.Level1Builder builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(Level1.Level1Builder builder) {
			ofNullable(getAttr()).ifPresent(builder::setAttr);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			Level1 _that = getType().cast(o);
		
			if (!Objects.equals(attr, _that.getAttr())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (attr != null ? attr.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "Level1 {" +
				"attr=" + this.attr +
			'}';
		}
	}

	/*********************** Builder Implementation of Level1  ***********************/
	class Level1BuilderImpl implements Level1.Level1Builder {
	
		protected Integer attr;
		
		@Override
		@RosettaAttribute("attr")
		@Accessor(AccessorType.GETTER)
		@RuneAttribute("attr")
		public Integer getAttr() {
			return attr;
		}
		
		@RosettaAttribute("attr")
		@Accessor(AccessorType.SETTER)
		@RuneAttribute("attr")
		@Override
		public Level1.Level1Builder setAttr(Integer _attr) {
			this.attr = _attr == null ? null : _attr;
			return this;
		}
		
		@Override
		public Level1 build() {
			return new Level1.Level1Impl(this);
		}
		
		@Override
		public Level1.Level1Builder toBuilder() {
			return this;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public Level1.Level1Builder prune() {
			return this;
		}
		
		@Override
		public boolean hasData() {
			if (getAttr()!=null) return true;
			return false;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public Level1.Level1Builder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
			Level1.Level1Builder o = (Level1.Level1Builder) other;
			
			
			merger.mergeBasic(getAttr(), o.getAttr(), this::setAttr);
			return this;
		}
	
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			Level1 _that = getType().cast(o);
		
			if (!Objects.equals(attr, _that.getAttr())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (attr != null ? attr.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "Level1Builder {" +
				"attr=" + this.attr +
			'}';
		}
	}
}
