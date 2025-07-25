package test.escaping.gettype;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
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
import test.escaping.gettype.meta.GetTypeEscapingMeta;

import static java.util.Optional.ofNullable;

/**
 * @version 0.0.0
 */
@RosettaDataType(value="GetTypeEscaping", builder=GetTypeEscaping.GetTypeEscapingBuilderImpl.class, version="0.0.0")
@RuneDataType(value="GetTypeEscaping", model="test", builder=GetTypeEscaping.GetTypeEscapingBuilderImpl.class, version="0.0.0")
public interface GetTypeEscaping extends RosettaModelObject {

	GetTypeEscapingMeta metaData = new GetTypeEscapingMeta();

	/*********************** Getter Methods  ***********************/
	Integer _getType();

	/*********************** Build Methods  ***********************/
	GetTypeEscaping build();
	
	GetTypeEscaping.GetTypeEscapingBuilder toBuilder();
	
	static GetTypeEscaping.GetTypeEscapingBuilder builder() {
		return new GetTypeEscaping.GetTypeEscapingBuilderImpl();
	}

	/*********************** Utility Methods  ***********************/
	@Override
	default RosettaMetaData<? extends GetTypeEscaping> metaData() {
		return metaData;
	}
	
	@Override
	@RuneAttribute("@type")
	default Class<? extends GetTypeEscaping> getType() {
		return GetTypeEscaping.class;
	}
	
	@Override
	default void process(RosettaPath path, Processor processor) {
		processor.processBasic(path.newSubPath("type"), Integer.class, _getType(), this);
	}
	

	/*********************** Builder Interface  ***********************/
	interface GetTypeEscapingBuilder extends GetTypeEscaping, RosettaModelObjectBuilder {
		GetTypeEscaping.GetTypeEscapingBuilder setType(Integer type);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processor.processBasic(path.newSubPath("type"), Integer.class, _getType(), this);
		}
		

		GetTypeEscaping.GetTypeEscapingBuilder prune();
	}

	/*********************** Immutable Implementation of GetTypeEscaping  ***********************/
	class GetTypeEscapingImpl implements GetTypeEscaping {
		private final Integer type;
		
		protected GetTypeEscapingImpl(GetTypeEscaping.GetTypeEscapingBuilder builder) {
			this.type = builder._getType();
		}
		
		@Override
		@RosettaAttribute(value="type", isRequired=true)
		@RuneAttribute(value="type", isRequired=true)
		public Integer _getType() {
			return type;
		}
		
		@Override
		public GetTypeEscaping build() {
			return this;
		}
		
		@Override
		public GetTypeEscaping.GetTypeEscapingBuilder toBuilder() {
			GetTypeEscaping.GetTypeEscapingBuilder builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(GetTypeEscaping.GetTypeEscapingBuilder builder) {
			ofNullable(_getType()).ifPresent(builder::setType);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			GetTypeEscaping _that = getType().cast(o);
		
			if (!Objects.equals(type, _that._getType())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (type != null ? type.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "GetTypeEscaping {" +
				"type=" + this.type +
			'}';
		}
	}

	/*********************** Builder Implementation of GetTypeEscaping  ***********************/
	class GetTypeEscapingBuilderImpl implements GetTypeEscaping.GetTypeEscapingBuilder {
	
		protected Integer type;
		
		@Override
		@RosettaAttribute(value="type", isRequired=true)
		@RuneAttribute(value="type", isRequired=true)
		public Integer _getType() {
			return type;
		}
		
		@RosettaAttribute(value="type", isRequired=true)
		@RuneAttribute(value="type", isRequired=true)
		@Override
		public GetTypeEscaping.GetTypeEscapingBuilder setType(Integer _type) {
			this.type = _type == null ? null : _type;
			return this;
		}
		
		@Override
		public GetTypeEscaping build() {
			return new GetTypeEscaping.GetTypeEscapingImpl(this);
		}
		
		@Override
		public GetTypeEscaping.GetTypeEscapingBuilder toBuilder() {
			return this;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public GetTypeEscaping.GetTypeEscapingBuilder prune() {
			return this;
		}
		
		@Override
		public boolean hasData() {
			if (_getType()!=null) return true;
			return false;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public GetTypeEscaping.GetTypeEscapingBuilder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
			GetTypeEscaping.GetTypeEscapingBuilder o = (GetTypeEscaping.GetTypeEscapingBuilder) other;
			
			
			merger.mergeBasic(_getType(), o._getType(), this::setType);
			return this;
		}
	
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			GetTypeEscaping _that = getType().cast(o);
		
			if (!Objects.equals(type, _that._getType())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (type != null ? type.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "GetTypeEscapingBuilder {" +
				"type=" + this.type +
			'}';
		}
	}
}
