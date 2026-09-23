package test.typeformatvalidation;

import com.google.common.collect.ImmutableList;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.annotations.Accessor;
import com.rosetta.model.lib.annotations.AccessorType;
import com.rosetta.model.lib.annotations.Multi;
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
import com.rosetta.util.ListEquals;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import test.typeformatvalidation.meta.MeasurementsMeta;

import static java.util.Optional.ofNullable;

/**
 * @version 0.0.0
 */
@RosettaDataType(value="Measurements", builder=Measurements.MeasurementsBuilderImpl.class, version="0.0.0")
@RuneDataType(value="Measurements", model="test", builder=Measurements.MeasurementsBuilderImpl.class, version="0.0.0")
public interface Measurements extends RosettaModelObject {

	MeasurementsMeta metaData = new MeasurementsMeta();

	/*********************** Getter Methods  ***********************/
	Integer getAmount();
	String getNote();
	List<Integer> getAmounts();

	/*********************** Build Methods  ***********************/
	Measurements build();
	
	Measurements.MeasurementsBuilder toBuilder();
	
	static Measurements.MeasurementsBuilder builder() {
		return new Measurements.MeasurementsBuilderImpl();
	}

	/*********************** Utility Methods  ***********************/
	@Override
	default RosettaMetaData<? extends Measurements> metaData() {
		return metaData;
	}
	
	@Override
	@RuneAttribute("@type")
	default Class<? extends Measurements> getType() {
		return Measurements.class;
	}
	
	@Override
	default void process(RosettaPath path, Processor processor) {
		processor.processBasic(path.newSubPath("amount"), Integer.class, getAmount(), this);
		processor.processBasic(path.newSubPath("note"), String.class, getNote(), this);
		processor.processBasic(path.newSubPath("amounts"), Integer.class, getAmounts(), this);
	}
	

	/*********************** Builder Interface  ***********************/
	interface MeasurementsBuilder extends Measurements, RosettaModelObjectBuilder {
		Measurements.MeasurementsBuilder setAmount(Integer amount);
		Measurements.MeasurementsBuilder setNote(String note);
		Measurements.MeasurementsBuilder addAmounts(Integer amounts);
		Measurements.MeasurementsBuilder addAmounts(Integer amounts, int idx);
		Measurements.MeasurementsBuilder addAmounts(List<Integer> amounts);
		Measurements.MeasurementsBuilder setAmounts(List<Integer> amounts);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processor.processBasic(path.newSubPath("amount"), Integer.class, getAmount(), this);
			processor.processBasic(path.newSubPath("note"), String.class, getNote(), this);
			processor.processBasic(path.newSubPath("amounts"), Integer.class, getAmounts(), this);
		}
		

		Measurements.MeasurementsBuilder prune();
	}

	/*********************** Immutable Implementation of Measurements  ***********************/
	class MeasurementsImpl implements Measurements {
		private final Integer amount;
		private final String note;
		private final List<Integer> amounts;
		
		protected MeasurementsImpl(Measurements.MeasurementsBuilder builder) {
			this.amount = builder.getAmount();
			this.note = builder.getNote();
			this.amounts = ofNullable(builder.getAmounts()).filter(_l->!_l.isEmpty()).map(ImmutableList::copyOf).orElse(null);
		}
		
		@Override
		@RosettaAttribute("amount")
		@Accessor(AccessorType.GETTER)
		@Required
		@RuneAttribute("amount")
		public Integer getAmount() {
			return amount;
		}
		
		@Override
		@RosettaAttribute("note")
		@Accessor(AccessorType.GETTER)
		@RuneAttribute("note")
		public String getNote() {
			return note;
		}
		
		@Override
		@RosettaAttribute("amounts")
		@Accessor(AccessorType.GETTER)
		@Multi
		@RuneAttribute("amounts")
		public List<Integer> getAmounts() {
			return amounts;
		}
		
		@Override
		public Measurements build() {
			return this;
		}
		
		@Override
		public Measurements.MeasurementsBuilder toBuilder() {
			Measurements.MeasurementsBuilder builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(Measurements.MeasurementsBuilder builder) {
			ofNullable(getAmount()).ifPresent(builder::setAmount);
			ofNullable(getNote()).ifPresent(builder::setNote);
			ofNullable(getAmounts()).ifPresent(builder::setAmounts);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			Measurements _that = getType().cast(o);
		
			if (!Objects.equals(amount, _that.getAmount())) return false;
			if (!Objects.equals(note, _that.getNote())) return false;
			if (!ListEquals.listEquals(amounts, _that.getAmounts())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (amount != null ? amount.hashCode() : 0);
			_result = 31 * _result + (note != null ? note.hashCode() : 0);
			_result = 31 * _result + (amounts != null ? amounts.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "Measurements {" +
				"amount=" + this.amount + ", " +
				"note=" + this.note + ", " +
				"amounts=" + this.amounts +
			'}';
		}
	}

	/*********************** Builder Implementation of Measurements  ***********************/
	class MeasurementsBuilderImpl implements Measurements.MeasurementsBuilder {
	
		protected Integer amount;
		protected String note;
		protected List<Integer> amounts = new ArrayList<>();
		
		@Override
		@RosettaAttribute("amount")
		@Accessor(AccessorType.GETTER)
		@Required
		@RuneAttribute("amount")
		public Integer getAmount() {
			return amount;
		}
		
		@Override
		@RosettaAttribute("note")
		@Accessor(AccessorType.GETTER)
		@RuneAttribute("note")
		public String getNote() {
			return note;
		}
		
		@Override
		@RosettaAttribute("amounts")
		@Accessor(AccessorType.GETTER)
		@Multi
		@RuneAttribute("amounts")
		public List<Integer> getAmounts() {
			return amounts;
		}
		
		@RosettaAttribute("amount")
		@Accessor(AccessorType.SETTER)
		@Required
		@RuneAttribute("amount")
		@Override
		public Measurements.MeasurementsBuilder setAmount(Integer _amount) {
			this.amount = _amount == null ? null : _amount;
			return this;
		}
		
		@RosettaAttribute("note")
		@Accessor(AccessorType.SETTER)
		@RuneAttribute("note")
		@Override
		public Measurements.MeasurementsBuilder setNote(String _note) {
			this.note = _note == null ? null : _note;
			return this;
		}
		
		@RosettaAttribute("amounts")
		@Accessor(AccessorType.ADDER)
		@Multi
		@RuneAttribute("amounts")
		@Override
		public Measurements.MeasurementsBuilder addAmounts(Integer _amounts) {
			if (_amounts != null) {
				this.amounts.add(_amounts);
			}
			return this;
		}
		
		@Override
		public Measurements.MeasurementsBuilder addAmounts(Integer _amounts, int idx) {
			getIndex(this.amounts, idx, () -> _amounts);
			return this;
		}
		
		@Override
		public Measurements.MeasurementsBuilder addAmounts(List<Integer> amountss) {
			if (amountss != null) {
				for (final Integer toAdd : amountss) {
					this.amounts.add(toAdd);
				}
			}
			return this;
		}
		
		@RosettaAttribute("amounts")
		@Accessor(AccessorType.SETTER)
		@Multi
		@RuneAttribute("amounts")
		@Override
		public Measurements.MeasurementsBuilder setAmounts(List<Integer> amountss) {
			if (amountss == null) {
				this.amounts = new ArrayList<>();
			} else {
				this.amounts = amountss.stream()
					.collect(Collectors.toCollection(()->new ArrayList<>()));
			}
			return this;
		}
		
		@Override
		public Measurements build() {
			return new Measurements.MeasurementsImpl(this);
		}
		
		@Override
		public Measurements.MeasurementsBuilder toBuilder() {
			return this;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public Measurements.MeasurementsBuilder prune() {
			return this;
		}
		
		@Override
		public boolean hasData() {
			if (getAmount()!=null) return true;
			if (getNote()!=null) return true;
			if (getAmounts()!=null && !getAmounts().isEmpty()) return true;
			return false;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public Measurements.MeasurementsBuilder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
			Measurements.MeasurementsBuilder o = (Measurements.MeasurementsBuilder) other;
			
			
			merger.mergeBasic(getAmount(), o.getAmount(), this::setAmount);
			merger.mergeBasic(getNote(), o.getNote(), this::setNote);
			merger.mergeBasic(getAmounts(), o.getAmounts(), (Consumer<Integer>) this::addAmounts);
			return this;
		}
	
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			Measurements _that = getType().cast(o);
		
			if (!Objects.equals(amount, _that.getAmount())) return false;
			if (!Objects.equals(note, _that.getNote())) return false;
			if (!ListEquals.listEquals(amounts, _that.getAmounts())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (amount != null ? amount.hashCode() : 0);
			_result = 31 * _result + (note != null ? note.hashCode() : 0);
			_result = 31 * _result + (amounts != null ? amounts.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "MeasurementsBuilder {" +
				"amount=" + this.amount + ", " +
				"note=" + this.note + ", " +
				"amounts=" + this.amounts +
			'}';
		}
	}
}
