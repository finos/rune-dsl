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
import com.rosetta.model.lib.process.BuilderProcessor;
import com.rosetta.model.lib.process.Processor;
import com.rosetta.util.ListEquals;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import test.deeppath.meta.LeafMeta;

import static java.util.Optional.ofNullable;

/**
 * @version 0.0.0
 */
@RosettaDataType(value="Leaf", builder=Leaf.LeafBuilderImpl.class, version="0.0.0")
@RuneDataType(value="Leaf", model="test", builder=Leaf.LeafBuilderImpl.class, version="0.0.0")
public interface Leaf extends RosettaModelObject {

	LeafMeta metaData = new LeafMeta();

	/*********************** Getter Methods  ***********************/
	String getCommon();
	List<String> getItems();

	/*********************** Build Methods  ***********************/
	Leaf build();
	
	Leaf.LeafBuilder toBuilder();
	
	static Leaf.LeafBuilder builder() {
		return new Leaf.LeafBuilderImpl();
	}

	/*********************** Utility Methods  ***********************/
	@Override
	default RosettaMetaData<? extends Leaf> metaData() {
		return metaData;
	}
	
	@Override
	@RuneAttribute("@type")
	default Class<? extends Leaf> getType() {
		return Leaf.class;
	}
	
	@Override
	default void process(RosettaPath path, Processor processor) {
		processor.processBasic(path.newSubPath("common"), String.class, getCommon(), this);
		processor.processBasic(path.newSubPath("items"), String.class, getItems(), this);
	}
	

	/*********************** Builder Interface  ***********************/
	interface LeafBuilder extends Leaf, RosettaModelObjectBuilder {
		Leaf.LeafBuilder setCommon(String common);
		Leaf.LeafBuilder addItems(String items);
		Leaf.LeafBuilder addItems(String items, int idx);
		Leaf.LeafBuilder addItems(List<String> items);
		Leaf.LeafBuilder setItems(List<String> items);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processor.processBasic(path.newSubPath("common"), String.class, getCommon(), this);
			processor.processBasic(path.newSubPath("items"), String.class, getItems(), this);
		}
		

		Leaf.LeafBuilder prune();
	}

	/*********************** Immutable Implementation of Leaf  ***********************/
	class LeafImpl implements Leaf {
		private final String common;
		private final List<String> items;
		
		protected LeafImpl(Leaf.LeafBuilder builder) {
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
		public Leaf build() {
			return this;
		}
		
		@Override
		public Leaf.LeafBuilder toBuilder() {
			Leaf.LeafBuilder builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(Leaf.LeafBuilder builder) {
			ofNullable(getCommon()).ifPresent(builder::setCommon);
			ofNullable(getItems()).ifPresent(builder::setItems);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			Leaf _that = getType().cast(o);
		
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
			return "Leaf {" +
				"common=" + this.common + ", " +
				"items=" + this.items +
			'}';
		}
	}

	/*********************** Builder Implementation of Leaf  ***********************/
	class LeafBuilderImpl implements Leaf.LeafBuilder {
	
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
		public Leaf.LeafBuilder setCommon(String _common) {
			this.common = _common == null ? null : _common;
			return this;
		}
		
		@RosettaAttribute("items")
		@Accessor(AccessorType.ADDER)
		@Multi
		@RuneAttribute("items")
		@Override
		public Leaf.LeafBuilder addItems(String _items) {
			if (_items != null) {
				this.items.add(_items);
			}
			return this;
		}
		
		@Override
		public Leaf.LeafBuilder addItems(String _items, int idx) {
			getIndex(this.items, idx, () -> _items);
			return this;
		}
		
		@Override
		public Leaf.LeafBuilder addItems(List<String> itemss) {
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
		public Leaf.LeafBuilder setItems(List<String> itemss) {
			if (itemss == null) {
				this.items = new ArrayList<>();
			} else {
				this.items = itemss.stream()
					.collect(Collectors.toCollection(()->new ArrayList<>()));
			}
			return this;
		}
		
		@Override
		public Leaf build() {
			return new Leaf.LeafImpl(this);
		}
		
		@Override
		public Leaf.LeafBuilder toBuilder() {
			return this;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public Leaf.LeafBuilder prune() {
			return this;
		}
		
		@Override
		public boolean hasData() {
			if (getCommon()!=null) return true;
			if (getItems()!=null && !getItems().isEmpty()) return true;
			return false;
		}
	
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			Leaf _that = getType().cast(o);
		
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
			return "LeafBuilder {" +
				"common=" + this.common + ", " +
				"items=" + this.items +
			'}';
		}
	}
}
