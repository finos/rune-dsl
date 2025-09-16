package test.pojo;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.annotations.RosettaDataType;
import com.rosetta.model.lib.annotations.RuneAttribute;
import com.rosetta.model.lib.annotations.RuneDataType;
import com.rosetta.model.lib.meta.RosettaMetaData;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.process.BuilderMerger;
import com.rosetta.model.lib.process.BuilderProcessor;
import com.rosetta.model.lib.process.Processor;
import test.pojo.meta.ParentMeta;


/**
 * @version 0.0.0
 */
@RosettaDataType(value="Parent", builder=Parent.ParentBuilderImpl.class, version="0.0.0")
@RuneDataType(value="Parent", model="test", builder=Parent.ParentBuilderImpl.class, version="0.0.0")
public interface Parent extends RosettaModelObject {

	ParentMeta metaData = new ParentMeta();

	/*********************** Getter Methods  ***********************/

	/*********************** Build Methods  ***********************/
	Parent build();
	
	Parent.ParentBuilder toBuilder();
	
	static Parent.ParentBuilder builder() {
		return new Parent.ParentBuilderImpl();
	}

	/*********************** Utility Methods  ***********************/
	@Override
	default RosettaMetaData<? extends Parent> metaData() {
		return metaData;
	}
	
	@Override
	@RuneAttribute("@type")
	default Class<? extends Parent> getType() {
		return Parent.class;
	}
	
	@Override
	default void process(RosettaPath path, Processor processor) {
	}
	

	/*********************** Builder Interface  ***********************/
	interface ParentBuilder extends Parent, RosettaModelObjectBuilder {

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
		}
		

		Parent.ParentBuilder prune();
	}

	/*********************** Immutable Implementation of Parent  ***********************/
	class ParentImpl implements Parent {
		
		protected ParentImpl(Parent.ParentBuilder builder) {
		}
		
		@Override
		public Parent build() {
			return this;
		}
		
		@Override
		public Parent.ParentBuilder toBuilder() {
			Parent.ParentBuilder builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(Parent.ParentBuilder builder) {
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
		
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			return _result;
		}
		
		@Override
		public String toString() {
			return "Parent {" +
			'}';
		}
	}

	/*********************** Builder Implementation of Parent  ***********************/
	class ParentBuilderImpl implements Parent.ParentBuilder {
	
		
		@Override
		public Parent build() {
			return new Parent.ParentImpl(this);
		}
		
		@Override
		public Parent.ParentBuilder toBuilder() {
			return this;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public Parent.ParentBuilder prune() {
			return this;
		}
		
		@Override
		public boolean hasData() {
			return false;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public Parent.ParentBuilder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
			Parent.ParentBuilder o = (Parent.ParentBuilder) other;
			
			
			return this;
		}
	
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
		
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			return _result;
		}
		
		@Override
		public String toString() {
			return "ParentBuilder {" +
			'}';
		}
	}
}
