package test.deeppath;

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
import test.deeppath.meta.Option1Meta;

import static java.util.Optional.ofNullable;

/**
 * @version 0.0.0
 */
@RosettaDataType(value="Option1", builder=Option1.Option1BuilderImpl.class, version="0.0.0")
@RuneDataType(value="Option1", model="test", builder=Option1.Option1BuilderImpl.class, version="0.0.0")
public interface Option1 extends RosettaModelObject {

	Option1Meta metaData = new Option1Meta();

	/*********************** Getter Methods  ***********************/
	String getCommon();
	List<String> getItems();
	Integer getOnly1();

	/*********************** Build Methods  ***********************/
	Option1 build();
	
	Option1.Option1Builder toBuilder();
	
	static Option1.Option1Builder builder() {
		return new Option1.Option1BuilderImpl();
	}

	/*********************** Utility Methods  ***********************/
	@Override
	default RosettaMetaData<? extends Option1> metaData() {
		return metaData;
	}
	
	@Override
	@RuneAttribute("@type")
	default Class<? extends Option1> getType() {
		return Option1.class;
	}
	
	@Override
	default void process(RosettaPath path, Processor processor) {
		processor.processBasic(path.newSubPath("common"), String.class, getCommon(), this);
		processor.processBasic(path.newSubPath("items"), String.class, getItems(), this);
		processor.processBasic(path.newSubPath("only1"), Integer.class, getOnly1(), this);
	}
	

	/*********************** Builder Interface  ***********************/
	interface Option1Builder extends Option1, RosettaModelObjectBuilder {
		Option1.Option1Builder setCommon(String common);
		Option1.Option1Builder addItems(String items);
		Option1.Option1Builder addItems(String items, int idx);
		Option1.Option1Builder addItems(List<String> items);
		Option1.Option1Builder setItems(List<String> items);
		Option1.Option1Builder setOnly1(Integer only1);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processor.processBasic(path.newSubPath("common"), String.class, getCommon(), this);
			processor.processBasic(path.newSubPath("items"), String.class, getItems(), this);
			processor.processBasic(path.newSubPath("only1"), Integer.class, getOnly1(), this);
		}
		

		Option1.Option1Builder prune();
	}

	/*********************** Immutable Implementation of Option1  ***********************/
	class Option1Impl implements Option1 {
		private final String common;
		private final List<String> items;
		private final Integer only1;
		
		protected Option1Impl(Option1.Option1Builder builder) {
			this.common = builder.getCommon();
			this.items = ofNullable(builder.getItems()).filter(_l->!_l.isEmpty()).map(ImmutableList::copyOf).orElse(null);
			this.only1 = builder.getOnly1();
		}
		
		@Override
		@RosettaAttribute("common")
		@Accessor(AccessorType.GETTER)
		@Required
		@RuneAttribute("common")
		public String getCommon() {
			return common;
		}
		
		@Override
		@RosettaAttribute("items")
		@Accessor(AccessorType.GETTER)
		@Multi
		@RuneAttribute("items")
		public List<String> getItems() {
			return items;
		}
		
		@Override
		@RosettaAttribute("only1")
		@Accessor(AccessorType.GETTER)
		@RuneAttribute("only1")
		public Integer getOnly1() {
			return only1;
		}
		
		@Override
		public Option1 build() {
			return this;
		}
		
		@Override
		public Option1.Option1Builder toBuilder() {
			Option1.Option1Builder builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(Option1.Option1Builder builder) {
			ofNullable(getCommon()).ifPresent(builder::setCommon);
			ofNullable(getItems()).ifPresent(builder::setItems);
			ofNullable(getOnly1()).ifPresent(builder::setOnly1);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			Option1 _that = getType().cast(o);
		
			if (!Objects.equals(common, _that.getCommon())) return false;
			if (!ListEquals.listEquals(items, _that.getItems())) return false;
			if (!Objects.equals(only1, _that.getOnly1())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (common != null ? common.hashCode() : 0);
			_result = 31 * _result + (items != null ? items.hashCode() : 0);
			_result = 31 * _result + (only1 != null ? only1.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "Option1 {" +
				"common=" + this.common + ", " +
				"items=" + this.items + ", " +
				"only1=" + this.only1 +
			'}';
		}
	}

	/*********************** Builder Implementation of Option1  ***********************/
	class Option1BuilderImpl implements Option1.Option1Builder {
	
		protected String common;
		protected List<String> items = new ArrayList<>();
		protected Integer only1;
		
		@Override
		@RosettaAttribute("common")
		@Accessor(AccessorType.GETTER)
		@Required
		@RuneAttribute("common")
		public String getCommon() {
			return common;
		}
		
		@Override
		@RosettaAttribute("items")
		@Accessor(AccessorType.GETTER)
		@Multi
		@RuneAttribute("items")
		public List<String> getItems() {
			return items;
		}
		
		@Override
		@RosettaAttribute("only1")
		@Accessor(AccessorType.GETTER)
		@RuneAttribute("only1")
		public Integer getOnly1() {
			return only1;
		}
		
		@RosettaAttribute("common")
		@Accessor(AccessorType.SETTER)
		@Required
		@RuneAttribute("common")
		@Override
		public Option1.Option1Builder setCommon(String _common) {
			this.common = _common == null ? null : _common;
			return this;
		}
		
		@RosettaAttribute("items")
		@Accessor(AccessorType.ADDER)
		@Multi
		@RuneAttribute("items")
		@Override
		public Option1.Option1Builder addItems(String _items) {
			if (_items != null) {
				this.items.add(_items);
			}
			return this;
		}
		
		@Override
		public Option1.Option1Builder addItems(String _items, int idx) {
			getIndex(this.items, idx, () -> _items);
			return this;
		}
		
		@Override
		public Option1.Option1Builder addItems(List<String> itemss) {
			if (itemss != null) {
				for (final String toAdd : itemss) {
					this.items.add(toAdd);
				}
			}
			return this;
		}
		
		@RosettaAttribute("items")
		@Accessor(AccessorType.SETTER)
		@Multi
		@RuneAttribute("items")
		@Override
		public Option1.Option1Builder setItems(List<String> itemss) {
			if (itemss == null) {
				this.items = new ArrayList<>();
			} else {
				this.items = itemss.stream()
					.collect(Collectors.toCollection(()->new ArrayList<>()));
			}
			return this;
		}
		
		@RosettaAttribute("only1")
		@Accessor(AccessorType.SETTER)
		@RuneAttribute("only1")
		@Override
		public Option1.Option1Builder setOnly1(Integer _only1) {
			this.only1 = _only1 == null ? null : _only1;
			return this;
		}
		
		@Override
		public Option1 build() {
			return new Option1.Option1Impl(this);
		}
		
		@Override
		public Option1.Option1Builder toBuilder() {
			return this;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public Option1.Option1Builder prune() {
			return this;
		}
		
		@Override
		public boolean hasData() {
			if (getCommon()!=null) return true;
			if (getItems()!=null && !getItems().isEmpty()) return true;
			if (getOnly1()!=null) return true;
			return false;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public Option1.Option1Builder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
			Option1.Option1Builder o = (Option1.Option1Builder) other;
			
			
			merger.mergeBasic(getCommon(), o.getCommon(), this::setCommon);
			merger.mergeBasic(getItems(), o.getItems(), (Consumer<String>) this::addItems);
			merger.mergeBasic(getOnly1(), o.getOnly1(), this::setOnly1);
			return this;
		}
	
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			Option1 _that = getType().cast(o);
		
			if (!Objects.equals(common, _that.getCommon())) return false;
			if (!ListEquals.listEquals(items, _that.getItems())) return false;
			if (!Objects.equals(only1, _that.getOnly1())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (common != null ? common.hashCode() : 0);
			_result = 31 * _result + (items != null ? items.hashCode() : 0);
			_result = 31 * _result + (only1 != null ? only1.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "Option1Builder {" +
				"common=" + this.common + ", " +
				"items=" + this.items + ", " +
				"only1=" + this.only1 +
			'}';
		}
	}
}
