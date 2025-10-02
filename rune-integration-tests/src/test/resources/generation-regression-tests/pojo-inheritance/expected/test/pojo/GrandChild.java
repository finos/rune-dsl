package test.pojo;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.annotations.RosettaAttribute;
import com.rosetta.model.lib.annotations.RosettaDataType;
import com.rosetta.model.lib.annotations.RuneAttribute;
import com.rosetta.model.lib.annotations.RuneDataType;
import com.rosetta.model.lib.annotations.RuneMetaType;
import com.rosetta.model.lib.meta.RosettaMetaData;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.process.BuilderMerger;
import com.rosetta.model.lib.process.BuilderProcessor;
import com.rosetta.model.lib.process.Processor;
import com.rosetta.model.metafields.MetaFields;
import test.pojo.meta.GrandChildMeta;


/**
 * @version 0.0.0
 */
@RosettaDataType(value="GrandChild", builder=GrandChild.GrandChildBuilderImpl.class, version="0.0.0")
@RuneDataType(value="GrandChild", model="test", builder=GrandChild.GrandChildBuilderImpl.class, version="0.0.0")
public interface GrandChild extends Child {

	GrandChildMeta metaData = new GrandChildMeta();

	/*********************** Getter Methods  ***********************/

	/*********************** Build Methods  ***********************/
	GrandChild build();
	
	GrandChild.GrandChildBuilder toBuilder();
	
	static GrandChild.GrandChildBuilder builder() {
		return new GrandChild.GrandChildBuilderImpl();
	}

	/*********************** Utility Methods  ***********************/
	@Override
	default RosettaMetaData<? extends GrandChild> metaData() {
		return metaData;
	}
	
	@Override
	@RuneAttribute("@type")
	default Class<? extends GrandChild> getType() {
		return GrandChild.class;
	}
	
	@Override
	default void process(RosettaPath path, Processor processor) {
		processRosetta(path.newSubPath("meta"), processor, MetaFields.class, getMeta());
	}
	

	/*********************** Builder Interface  ***********************/
	interface GrandChildBuilder extends GrandChild, Child.ChildBuilder {
		@Override
		GrandChild.GrandChildBuilder setMeta(MetaFields meta);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processRosetta(path.newSubPath("meta"), processor, MetaFields.MetaFieldsBuilder.class, getMeta());
		}
		

		GrandChild.GrandChildBuilder prune();
	}

	/*********************** Immutable Implementation of GrandChild  ***********************/
	class GrandChildImpl extends Child.ChildImpl implements GrandChild {
		
		protected GrandChildImpl(GrandChild.GrandChildBuilder builder) {
			super(builder);
		}
		
		@Override
		public GrandChild build() {
			return this;
		}
		
		@Override
		public GrandChild.GrandChildBuilder toBuilder() {
			GrandChild.GrandChildBuilder builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(GrandChild.GrandChildBuilder builder) {
			super.setBuilderFields(builder);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
			if (!super.equals(o)) return false;
		
		
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = super.hashCode();
			return _result;
		}
		
		@Override
		public String toString() {
			return "GrandChild {" +
			'}' + " " + super.toString();
		}
	}

	/*********************** Builder Implementation of GrandChild  ***********************/
	class GrandChildBuilderImpl extends Child.ChildBuilderImpl implements GrandChild.GrandChildBuilder {
	
		
		@RosettaAttribute("meta")
		@RuneAttribute("meta")
		@RuneMetaType
		@Override
		public GrandChild.GrandChildBuilder setMeta(MetaFields _meta) {
			this.meta = _meta == null ? null : _meta.toBuilder();
			return this;
		}
		
		@Override
		public GrandChild build() {
			return new GrandChild.GrandChildImpl(this);
		}
		
		@Override
		public GrandChild.GrandChildBuilder toBuilder() {
			return this;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public GrandChild.GrandChildBuilder prune() {
			super.prune();
			return this;
		}
		
		@Override
		public boolean hasData() {
			if (super.hasData()) return true;
			return false;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public GrandChild.GrandChildBuilder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
			super.merge(other, merger);
			GrandChild.GrandChildBuilder o = (GrandChild.GrandChildBuilder) other;
			
			
			return this;
		}
	
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
			if (!super.equals(o)) return false;
		
		
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = super.hashCode();
			return _result;
		}
		
		@Override
		public String toString() {
			return "GrandChildBuilder {" +
			'}' + " " + super.toString();
		}
	}
}
