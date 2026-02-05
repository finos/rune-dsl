package test.escaping.getclass;

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
import test.escaping.getclass.meta.GetClassEscapingMeta;

import static java.util.Optional.ofNullable;

/**
 * @version 0.0.0
 */
@RosettaDataType(value="GetClassEscaping", builder=GetClassEscaping.GetClassEscapingBuilderImpl.class, version="0.0.0")
@RuneDataType(value="GetClassEscaping", model="test", builder=GetClassEscaping.GetClassEscapingBuilderImpl.class, version="0.0.0")
public interface GetClassEscaping extends RosettaModelObject {

	GetClassEscapingMeta metaData = new GetClassEscapingMeta();

	/*********************** Getter Methods  ***********************/
	Integer _getClass();

	/*********************** Build Methods  ***********************/
	GetClassEscaping build();
	
	GetClassEscaping.GetClassEscapingBuilder toBuilder();
	
	static GetClassEscaping.GetClassEscapingBuilder builder() {
		return new GetClassEscaping.GetClassEscapingBuilderImpl();
	}

	/*********************** Utility Methods  ***********************/
	@Override
	default RosettaMetaData<? extends GetClassEscaping> metaData() {
		return metaData;
	}
	
	@Override
	@RuneAttribute("@type")
	default Class<? extends GetClassEscaping> getType() {
		return GetClassEscaping.class;
	}
	
	@Override
	default void process(RosettaPath path, Processor processor) {
		processor.processBasic(path.newSubPath("class"), Integer.class, _getClass(), this);
	}
	

	/*********************** Builder Interface  ***********************/
	interface GetClassEscapingBuilder extends GetClassEscaping, RosettaModelObjectBuilder {
		GetClassEscaping.GetClassEscapingBuilder setClass(Integer _class);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processor.processBasic(path.newSubPath("class"), Integer.class, _getClass(), this);
		}
		

		GetClassEscaping.GetClassEscapingBuilder prune();
	}

	/*********************** Immutable Implementation of GetClassEscaping  ***********************/
	class GetClassEscapingImpl implements GetClassEscaping {
		private final Integer _class;
		
		protected GetClassEscapingImpl(GetClassEscaping.GetClassEscapingBuilder builder) {
			this._class = builder._getClass();
		}
		
		@Override
		@RosettaAttribute("class")
		@Accessor(AccessorType.GETTER)
		@Required
		@RuneAttribute("class")
		public Integer _getClass() {
			return _class;
		}
		
		@Override
		public GetClassEscaping build() {
			return this;
		}
		
		@Override
		public GetClassEscaping.GetClassEscapingBuilder toBuilder() {
			GetClassEscaping.GetClassEscapingBuilder builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(GetClassEscaping.GetClassEscapingBuilder builder) {
			ofNullable(_getClass()).ifPresent(builder::setClass);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			GetClassEscaping _that = getType().cast(o);
		
			if (!Objects.equals(_class, _that._getClass())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (_class != null ? _class.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "GetClassEscaping {" +
				"class=" + this._class +
			'}';
		}
	}

	/*********************** Builder Implementation of GetClassEscaping  ***********************/
	class GetClassEscapingBuilderImpl implements GetClassEscaping.GetClassEscapingBuilder {
	
		protected Integer _class;
		
		@Override
		@RosettaAttribute("class")
		@Accessor(AccessorType.GETTER)
		@Required
		@RuneAttribute("class")
		public Integer _getClass() {
			return _class;
		}
		
		@RosettaAttribute("class")
		@Accessor(AccessorType.SETTER)
		@Required
		@RuneAttribute("class")
		@Override
		public GetClassEscaping.GetClassEscapingBuilder setClass(Integer __class) {
			this._class = __class == null ? null : __class;
			return this;
		}
		
		@Override
		public GetClassEscaping build() {
			return new GetClassEscaping.GetClassEscapingImpl(this);
		}
		
		@Override
		public GetClassEscaping.GetClassEscapingBuilder toBuilder() {
			return this;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public GetClassEscaping.GetClassEscapingBuilder prune() {
			return this;
		}
		
		@Override
		public boolean hasData() {
			if (_getClass()!=null) return true;
			return false;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public GetClassEscaping.GetClassEscapingBuilder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
			GetClassEscaping.GetClassEscapingBuilder o = (GetClassEscaping.GetClassEscapingBuilder) other;
			
			
			merger.mergeBasic(_getClass(), o._getClass(), this::setClass);
			return this;
		}
	
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			GetClassEscaping _that = getType().cast(o);
		
			if (!Objects.equals(_class, _that._getClass())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (_class != null ? _class.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "GetClassEscapingBuilder {" +
				"class=" + this._class +
			'}';
		}
	}
}
