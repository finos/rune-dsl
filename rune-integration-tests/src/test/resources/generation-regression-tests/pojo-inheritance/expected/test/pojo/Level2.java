package test.pojo;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.annotations.AccessorType;
import com.rosetta.model.lib.annotations.RosettaAttribute;
import com.rosetta.model.lib.annotations.RosettaDataType;
import com.rosetta.model.lib.annotations.RosettaIgnore;
import com.rosetta.model.lib.annotations.RuneAttribute;
import com.rosetta.model.lib.annotations.RuneDataType;
import com.rosetta.model.lib.annotations.RuneIgnore;
import com.rosetta.model.lib.meta.RosettaMetaData;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.process.BuilderMerger;
import com.rosetta.model.lib.process.BuilderProcessor;
import com.rosetta.model.lib.process.Processor;
import java.util.Objects;
import test.pojo.meta.Level2Meta;

import static java.util.Optional.ofNullable;

/**
 * @version 0.0.0
 */
@RosettaDataType(value="Level2", builder=Level2.Level2BuilderImpl.class, version="0.0.0")
@RuneDataType(value="Level2", model="test", builder=Level2.Level2BuilderImpl.class, version="0.0.0")
public interface Level2 extends Level1 {

	Level2Meta metaData = new Level2Meta();

	/*********************** Getter Methods  ***********************/
	@Override
	Integer getAttr();

	/*********************** Build Methods  ***********************/
	Level2 build();
	
	Level2.Level2Builder toBuilder();
	
	static Level2.Level2Builder builder() {
		return new Level2.Level2BuilderImpl();
	}

	/*********************** Utility Methods  ***********************/
	@Override
	default RosettaMetaData<? extends Level2> metaData() {
		return metaData;
	}
	
	@Override
	@RuneAttribute("@type")
	default Class<? extends Level2> getType() {
		return Level2.class;
	}
	
	@Override
	default void process(RosettaPath path, Processor processor) {
		processor.processBasic(path.newSubPath("attr"), Integer.class, getAttr(), this);
	}
	

	/*********************** Builder Interface  ***********************/
	interface Level2Builder extends Level2, Level1.Level1Builder {
		@Override
		Level2.Level2Builder setAttr(Integer attr);
		Level2.Level2Builder setAttrOverriddenAsInteger(Integer attr);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processor.processBasic(path.newSubPath("attr"), Integer.class, getAttr(), this);
		}
		

		Level2.Level2Builder prune();
	}

	/*********************** Immutable Implementation of Level2  ***********************/
	class Level2Impl extends Level1.Level1Impl implements Level2 {
		private final Integer attr;
		
		protected Level2Impl(Level2.Level2Builder builder) {
			super(builder);
			this.attr = builder.getAttr();
		}
		
		@Override
		@RosettaAttribute(value="attr", isRequired=true, isMulti=false, accessorType=AccessorType.GETTER)
		@RuneAttribute(value="attr", isRequired=true)
		public Integer getAttr() {
			return attr;
		}
		
		@Override
		public Level2 build() {
			return this;
		}
		
		@Override
		public Level2.Level2Builder toBuilder() {
			Level2.Level2Builder builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(Level2.Level2Builder builder) {
			super.setBuilderFields(builder);
			ofNullable(getAttr()).ifPresent(builder::setAttrOverriddenAsInteger);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
			if (!super.equals(o)) return false;
		
			Level2 _that = getType().cast(o);
		
			if (!Objects.equals(attr, _that.getAttr())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = super.hashCode();
			_result = 31 * _result + (attr != null ? attr.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "Level2 {" +
				"attr=" + this.attr +
			'}' + " " + super.toString();
		}
	}

	/*********************** Builder Implementation of Level2  ***********************/
	class Level2BuilderImpl extends Level1.Level1BuilderImpl implements Level2.Level2Builder {
	
		protected Integer attr;
		
		@Override
		@RosettaAttribute(value="attr", isRequired=true, isMulti=false, accessorType=AccessorType.GETTER)
		@RuneAttribute(value="attr", isRequired=true)
		public Integer getAttr() {
			return attr;
		}
		
		@RosettaAttribute(value="attr", isRequired=true, isMulti=false, accessorType=AccessorType.SETTER)
		@RuneAttribute(value="attr", isRequired=true)
		@Override
		public Level2.Level2Builder setAttrOverriddenAsInteger(Integer _attr) {
			this.attr = _attr == null ? null : _attr;
			return this;
		}
		
		@RosettaIgnore
		@RuneIgnore
		@Override
		public Level2.Level2Builder setAttr(Integer _attr) {
			return setAttrOverriddenAsInteger(_attr);
		}
		
		@Override
		public Level2 build() {
			return new Level2.Level2Impl(this);
		}
		
		@Override
		public Level2.Level2Builder toBuilder() {
			return this;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public Level2.Level2Builder prune() {
			super.prune();
			return this;
		}
		
		@Override
		public boolean hasData() {
			if (super.hasData()) return true;
			if (getAttr()!=null) return true;
			return false;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public Level2.Level2Builder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
			super.merge(other, merger);
			Level2.Level2Builder o = (Level2.Level2Builder) other;
			
			
			merger.mergeBasic(getAttr(), o.getAttr(), this::setAttrOverriddenAsInteger);
			return this;
		}
	
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
			if (!super.equals(o)) return false;
		
			Level2 _that = getType().cast(o);
		
			if (!Objects.equals(attr, _that.getAttr())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = super.hashCode();
			_result = 31 * _result + (attr != null ? attr.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "Level2Builder {" +
				"attr=" + this.attr +
			'}' + " " + super.toString();
		}
	}
}
