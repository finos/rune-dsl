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
import test.deeppath.meta.Option2Meta;

import static java.util.Optional.ofNullable;

/**
 * @version 0.0.0
 */
@RosettaDataType(value="Option2", builder=Option2.Option2BuilderImpl.class, version="0.0.0")
@RuneDataType(value="Option2", model="test", builder=Option2.Option2BuilderImpl.class, version="0.0.0")
public interface Option2 extends RosettaModelObject {

	Option2Meta metaData = new Option2Meta();

	/*********************** Getter Methods  ***********************/
	String getCommon();
	List<String> getItems();

	/*********************** Build Methods  ***********************/
	Option2 build();
	
	Option2.Option2Builder toBuilder();
	
	static Option2.Option2Builder builder() {
		return new Option2.Option2BuilderImpl();
	}

	/*********************** Utility Methods  ***********************/
	@Override
	default RosettaMetaData<? extends Option2> metaData() {
		return metaData;
	}
	
	@Override
	@RuneAttribute("@type")
	default Class<? extends Option2> getType() {
		return Option2.class;
	}
	
	@Override
	default void process(RosettaPath path, Processor processor) {
		processor.processBasic(path.newSubPath("common"), String.class, getCommon(), this);
		processor.processBasic(path.newSubPath("items"), String.class, getItems(), this);
	}
	

	/*********************** Builder Interface  ***********************/
	interface Option2Builder extends Option2, RosettaModelObjectBuilder {
		Option2.Option2Builder setCommon(String common);
		Option2.Option2Builder addItems(String items);
		Option2.Option2Builder addItems(String items, int idx);
		Option2.Option2Builder addItems(List<String> items);
		Option2.Option2Builder setItems(List<String> items);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processor.processBasic(path.newSubPath("common"), String.class, getCommon(), this);
			processor.processBasic(path.newSubPath("items"), String.class, getItems(), this);
		}
		

		Option2.Option2Builder prune();
	}

	/*********************** Immutable Implementation of Option2  ***********************/
	class Option2Impl implements Option2 {
		private final String common;
		private final List<String> items;
		
		protected Option2Impl(Option2.Option2Builder builder) {
			this.common = builder.getCommon();
			this.items = ofNullable(builder.getItems()).filter(_l->!_l.isEmpty()).map(ImmutableList::copyOf).orElse(null);
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
		public Option2 build() {
			return this;
		}
		
		@Override
		public Option2.Option2Builder toBuilder() {
			Option2.Option2Builder builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(Option2.Option2Builder builder) {
			ofNullable(getCommon()).ifPresent(builder::setCommon);
			ofNullable(getItems()).ifPresent(builder::setItems);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			Option2 _that = getType().cast(o);
		
			if (!Objects.equals(common, _that.getCommon())) return false;
			if (!ListEquals.listEquals(items, _that.getItems())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (common != null ? common.hashCode() : 0);
			_result = 31 * _result + (items != null ? items.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "Option2 {" +
				"common=" + this.common + ", " +
				"items=" + this.items +
			'}';
		}
	}

	/*********************** Builder Implementation of Option2  ***********************/
	class Option2BuilderImpl implements Option2.Option2Builder {
	
		protected String common;
		protected List<String> items = new ArrayList<>();
		
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
		
		@RosettaAttribute("common")
		@Accessor(AccessorType.SETTER)
		@Required
		@RuneAttribute("common")
		@Override
		public Option2.Option2Builder setCommon(String _common) {
			this.common = _common == null ? null : _common;
			return this;
		}
		
		@RosettaAttribute("items")
		@Accessor(AccessorType.ADDER)
		@Multi
		@RuneAttribute("items")
		@Override
		public Option2.Option2Builder addItems(String _items) {
			if (_items != null) {
				this.items.add(_items);
			}
			return this;
		}
		
		@Override
		public Option2.Option2Builder addItems(String _items, int idx) {
			getIndex(this.items, idx, () -> _items);
			return this;
		}
		
		@Override
		public Option2.Option2Builder addItems(List<String> itemss) {
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
		public Option2.Option2Builder setItems(List<String> itemss) {
			if (itemss == null) {
				this.items = new ArrayList<>();
			} else {
				this.items = itemss.stream()
					.collect(Collectors.toCollection(()->new ArrayList<>()));
			}
			return this;
		}
		
		@Override
		public Option2 build() {
			return new Option2.Option2Impl(this);
		}
		
		@Override
		public Option2.Option2Builder toBuilder() {
			return this;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public Option2.Option2Builder prune() {
			return this;
		}
		
		@Override
		public boolean hasData() {
			if (getCommon()!=null) return true;
			if (getItems()!=null && !getItems().isEmpty()) return true;
			return false;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public Option2.Option2Builder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
			Option2.Option2Builder o = (Option2.Option2Builder) other;
			
			
			merger.mergeBasic(getCommon(), o.getCommon(), this::setCommon);
			merger.mergeBasic(getItems(), o.getItems(), (Consumer<String>) this::addItems);
			return this;
		}
	
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			Option2 _that = getType().cast(o);
		
			if (!Objects.equals(common, _that.getCommon())) return false;
			if (!ListEquals.listEquals(items, _that.getItems())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (common != null ? common.hashCode() : 0);
			_result = 31 * _result + (items != null ? items.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "Option2Builder {" +
				"common=" + this.common + ", " +
				"items=" + this.items +
			'}';
		}
	}
}
