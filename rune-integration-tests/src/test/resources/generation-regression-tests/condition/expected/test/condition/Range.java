package test.condition;

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
import com.rosetta.model.lib.process.BuilderProcessor;
import com.rosetta.model.lib.process.Processor;
import java.util.Objects;
import test.condition.meta.RangeMeta;

import static java.util.Optional.ofNullable;

/**
 * @version 0.0.0
 */
@RosettaDataType(value="Range", builder=Range.RangeBuilderImpl.class, version="0.0.0")
@RuneDataType(value="Range", model="test", builder=Range.RangeBuilderImpl.class, version="0.0.0")
public interface Range extends RosettaModelObject {

	RangeMeta metaData = new RangeMeta();

	/*********************** Getter Methods  ***********************/
	Integer getQuantity();

	/*********************** Build Methods  ***********************/
	Range build();
	
	Range.RangeBuilder toBuilder();
	
	static Range.RangeBuilder builder() {
		return new Range.RangeBuilderImpl();
	}

	/*********************** Utility Methods  ***********************/
	@Override
	default RosettaMetaData<? extends Range> metaData() {
		return metaData;
	}
	
	@Override
	@RuneAttribute("@type")
	default Class<? extends Range> getType() {
		return Range.class;
	}
	
	@Override
	default void process(RosettaPath path, Processor processor) {
		processor.processBasic(path.newSubPath("quantity"), Integer.class, getQuantity(), this);
	}
	

	/*********************** Builder Interface  ***********************/
	interface RangeBuilder extends Range, RosettaModelObjectBuilder {
		Range.RangeBuilder setQuantity(Integer quantity);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processor.processBasic(path.newSubPath("quantity"), Integer.class, getQuantity(), this);
		}
		

		Range.RangeBuilder prune();
	}

	/*********************** Immutable Implementation of Range  ***********************/
	class RangeImpl implements Range {
		private final Integer quantity;
		
		protected RangeImpl(Range.RangeBuilder builder) {
			this.quantity = builder.getQuantity();
		}
		
		@Override
		@RosettaAttribute("quantity")
		@Accessor(AccessorType.GETTER)
		@Required
		@RuneAttribute("quantity")
		public Integer getQuantity() {
			return quantity;
		}
		
		@Override
		public Range build() {
			return this;
		}
		
		@Override
		public Range.RangeBuilder toBuilder() {
			Range.RangeBuilder builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(Range.RangeBuilder builder) {
			ofNullable(getQuantity()).ifPresent(builder::setQuantity);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			Range _that = getType().cast(o);
		
			if (!Objects.equals(quantity, _that.getQuantity())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (quantity != null ? quantity.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "Range {" +
				"quantity=" + this.quantity +
			'}';
		}
	}

	/*********************** Builder Implementation of Range  ***********************/
	class RangeBuilderImpl implements Range.RangeBuilder {
	
		protected Integer quantity;
		
		@Override
		@RosettaAttribute("quantity")
		@Accessor(AccessorType.GETTER)
		@Required
		@RuneAttribute("quantity")
		public Integer getQuantity() {
			return quantity;
		}
		
		@RosettaAttribute("quantity")
		@Accessor(AccessorType.SETTER)
		@Required
		@RuneAttribute("quantity")
		@Override
		public Range.RangeBuilder setQuantity(Integer _quantity) {
			this.quantity = _quantity == null ? null : _quantity;
			return this;
		}
		
		@Override
		public Range build() {
			return new Range.RangeImpl(this);
		}
		
		@Override
		public Range.RangeBuilder toBuilder() {
			return this;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public Range.RangeBuilder prune() {
			return this;
		}
		
		@Override
		public boolean hasData() {
			if (getQuantity()!=null) return true;
			return false;
		}
	
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			Range _that = getType().cast(o);
		
			if (!Objects.equals(quantity, _that.getQuantity())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (quantity != null ? quantity.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "RangeBuilder {" +
				"quantity=" + this.quantity +
			'}';
		}
	}
}
