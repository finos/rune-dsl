package test.pojo;

import com.google.common.collect.ImmutableList;
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
import com.rosetta.model.metafields.FieldWithMetaString;
import com.rosetta.util.ListEquals;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import test.pojo.meta.Foo1Meta;

import static java.util.Optional.ofNullable;

/**
 * @version 0.0.0
 */
@RosettaDataType(value="Foo1", builder=Foo1.Foo1BuilderImpl.class, version="0.0.0")
@RuneDataType(value="Foo1", model="test", builder=Foo1.Foo1BuilderImpl.class, version="0.0.0")
public interface Foo1 extends RosettaModelObject {

	Foo1Meta metaData = new Foo1Meta();

	/*********************** Getter Methods  ***********************/
	Integer getAttr();
	BigDecimal getNumberAttr();
	Parent getParent();
	List<? extends Parent> getParentList();
	List<? extends Parent> getOtherParentList();
	FieldWithMetaString getStringAttr();

	/*********************** Build Methods  ***********************/
	Foo1 build();
	
	Foo1.Foo1Builder toBuilder();
	
	static Foo1.Foo1Builder builder() {
		return new Foo1.Foo1BuilderImpl();
	}

	/*********************** Utility Methods  ***********************/
	@Override
	default RosettaMetaData<? extends Foo1> metaData() {
		return metaData;
	}
	
	@Override
	@RuneAttribute("@type")
	default Class<? extends Foo1> getType() {
		return Foo1.class;
	}
	
	@Override
	default void process(RosettaPath path, Processor processor) {
		processor.processBasic(path.newSubPath("attr"), Integer.class, getAttr(), this);
		processor.processBasic(path.newSubPath("numberAttr"), BigDecimal.class, getNumberAttr(), this);
		processRosetta(path.newSubPath("parent"), processor, Parent.class, getParent());
		processRosetta(path.newSubPath("parentList"), processor, Parent.class, getParentList());
		processRosetta(path.newSubPath("otherParentList"), processor, Parent.class, getOtherParentList());
		processRosetta(path.newSubPath("stringAttr"), processor, FieldWithMetaString.class, getStringAttr());
	}
	

	/*********************** Builder Interface  ***********************/
	interface Foo1Builder extends Foo1, RosettaModelObjectBuilder {
		Parent.ParentBuilder getOrCreateParent();
		@Override
		Parent.ParentBuilder getParent();
		Parent.ParentBuilder getOrCreateParentList(int index);
		@Override
		List<? extends Parent.ParentBuilder> getParentList();
		Parent.ParentBuilder getOrCreateOtherParentList(int index);
		@Override
		List<? extends Parent.ParentBuilder> getOtherParentList();
		FieldWithMetaString.FieldWithMetaStringBuilder getOrCreateStringAttr();
		@Override
		FieldWithMetaString.FieldWithMetaStringBuilder getStringAttr();
		Foo1.Foo1Builder setAttr(Integer attr);
		Foo1.Foo1Builder setNumberAttr(BigDecimal numberAttr);
		Foo1.Foo1Builder setParent(Parent parent);
		Foo1.Foo1Builder addParentList(Parent parentList);
		Foo1.Foo1Builder addParentList(Parent parentList, int idx);
		Foo1.Foo1Builder addParentList(List<? extends Parent> parentList);
		Foo1.Foo1Builder setParentList(List<? extends Parent> parentList);
		Foo1.Foo1Builder addOtherParentList(Parent otherParentList);
		Foo1.Foo1Builder addOtherParentList(Parent otherParentList, int idx);
		Foo1.Foo1Builder addOtherParentList(List<? extends Parent> otherParentList);
		Foo1.Foo1Builder setOtherParentList(List<? extends Parent> otherParentList);
		Foo1.Foo1Builder setStringAttr(FieldWithMetaString stringAttr);
		Foo1.Foo1Builder setStringAttrValue(String stringAttr);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processor.processBasic(path.newSubPath("attr"), Integer.class, getAttr(), this);
			processor.processBasic(path.newSubPath("numberAttr"), BigDecimal.class, getNumberAttr(), this);
			processRosetta(path.newSubPath("parent"), processor, Parent.ParentBuilder.class, getParent());
			processRosetta(path.newSubPath("parentList"), processor, Parent.ParentBuilder.class, getParentList());
			processRosetta(path.newSubPath("otherParentList"), processor, Parent.ParentBuilder.class, getOtherParentList());
			processRosetta(path.newSubPath("stringAttr"), processor, FieldWithMetaString.FieldWithMetaStringBuilder.class, getStringAttr());
		}
		

		Foo1.Foo1Builder prune();
	}

	/*********************** Immutable Implementation of Foo1  ***********************/
	class Foo1Impl implements Foo1 {
		private final Integer attr;
		private final BigDecimal numberAttr;
		private final Parent parent;
		private final List<? extends Parent> parentList;
		private final List<? extends Parent> otherParentList;
		private final FieldWithMetaString stringAttr;
		
		protected Foo1Impl(Foo1.Foo1Builder builder) {
			this.attr = builder.getAttr();
			this.numberAttr = builder.getNumberAttr();
			this.parent = ofNullable(builder.getParent()).map(f->f.build()).orElse(null);
			this.parentList = ofNullable(builder.getParentList()).filter(_l->!_l.isEmpty()).map(list -> list.stream().filter(Objects::nonNull).map(f->f.build()).filter(Objects::nonNull).collect(ImmutableList.toImmutableList())).orElse(null);
			this.otherParentList = ofNullable(builder.getOtherParentList()).filter(_l->!_l.isEmpty()).map(list -> list.stream().filter(Objects::nonNull).map(f->f.build()).filter(Objects::nonNull).collect(ImmutableList.toImmutableList())).orElse(null);
			this.stringAttr = ofNullable(builder.getStringAttr()).map(f->f.build()).orElse(null);
		}
		
		@Override
		@RosettaAttribute(value="attr", isRequired=true)
		@RuneAttribute(value="attr", isRequired=true)
		public Integer getAttr() {
			return attr;
		}
		
		@Override
		@RosettaAttribute("numberAttr")
		@RuneAttribute("numberAttr")
		public BigDecimal getNumberAttr() {
			return numberAttr;
		}
		
		@Override
		@RosettaAttribute(value="parent", isRequired=true)
		@RuneAttribute(value="parent", isRequired=true)
		public Parent getParent() {
			return parent;
		}
		
		@Override
		@RosettaAttribute("parentList")
		@RuneAttribute("parentList")
		public List<? extends Parent> getParentList() {
			return parentList;
		}
		
		@Override
		@RosettaAttribute("otherParentList")
		@RuneAttribute("otherParentList")
		public List<? extends Parent> getOtherParentList() {
			return otherParentList;
		}
		
		@Override
		@RosettaAttribute(value="stringAttr", isRequired=true)
		@RuneAttribute(value="stringAttr", isRequired=true)
		public FieldWithMetaString getStringAttr() {
			return stringAttr;
		}
		
		@Override
		public Foo1 build() {
			return this;
		}
		
		@Override
		public Foo1.Foo1Builder toBuilder() {
			Foo1.Foo1Builder builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(Foo1.Foo1Builder builder) {
			ofNullable(getAttr()).ifPresent(builder::setAttr);
			ofNullable(getNumberAttr()).ifPresent(builder::setNumberAttr);
			ofNullable(getParent()).ifPresent(builder::setParent);
			ofNullable(getParentList()).ifPresent(builder::setParentList);
			ofNullable(getOtherParentList()).ifPresent(builder::setOtherParentList);
			ofNullable(getStringAttr()).ifPresent(builder::setStringAttr);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			Foo1 _that = getType().cast(o);
		
			if (!Objects.equals(attr, _that.getAttr())) return false;
			if (!Objects.equals(numberAttr, _that.getNumberAttr())) return false;
			if (!Objects.equals(parent, _that.getParent())) return false;
			if (!ListEquals.listEquals(parentList, _that.getParentList())) return false;
			if (!ListEquals.listEquals(otherParentList, _that.getOtherParentList())) return false;
			if (!Objects.equals(stringAttr, _that.getStringAttr())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (attr != null ? attr.hashCode() : 0);
			_result = 31 * _result + (numberAttr != null ? numberAttr.hashCode() : 0);
			_result = 31 * _result + (parent != null ? parent.hashCode() : 0);
			_result = 31 * _result + (parentList != null ? parentList.hashCode() : 0);
			_result = 31 * _result + (otherParentList != null ? otherParentList.hashCode() : 0);
			_result = 31 * _result + (stringAttr != null ? stringAttr.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "Foo1 {" +
				"attr=" + this.attr + ", " +
				"numberAttr=" + this.numberAttr + ", " +
				"parent=" + this.parent + ", " +
				"parentList=" + this.parentList + ", " +
				"otherParentList=" + this.otherParentList + ", " +
				"stringAttr=" + this.stringAttr +
			'}';
		}
	}

	/*********************** Builder Implementation of Foo1  ***********************/
	class Foo1BuilderImpl implements Foo1.Foo1Builder {
	
		protected Integer attr;
		protected BigDecimal numberAttr;
		protected Parent.ParentBuilder parent;
		protected List<Parent.ParentBuilder> parentList = new ArrayList<>();
		protected List<Parent.ParentBuilder> otherParentList = new ArrayList<>();
		protected FieldWithMetaString.FieldWithMetaStringBuilder stringAttr;
		
		@Override
		@RosettaAttribute(value="attr", isRequired=true)
		@RuneAttribute(value="attr", isRequired=true)
		public Integer getAttr() {
			return attr;
		}
		
		@Override
		@RosettaAttribute("numberAttr")
		@RuneAttribute("numberAttr")
		public BigDecimal getNumberAttr() {
			return numberAttr;
		}
		
		@Override
		@RosettaAttribute(value="parent", isRequired=true)
		@RuneAttribute(value="parent", isRequired=true)
		public Parent.ParentBuilder getParent() {
			return parent;
		}
		
		@Override
		public Parent.ParentBuilder getOrCreateParent() {
			Parent.ParentBuilder result;
			if (parent!=null) {
				result = parent;
			}
			else {
				result = parent = Parent.builder();
			}
			
			return result;
		}
		
		@Override
		@RosettaAttribute("parentList")
		@RuneAttribute("parentList")
		public List<? extends Parent.ParentBuilder> getParentList() {
			return parentList;
		}
		
		@Override
		public Parent.ParentBuilder getOrCreateParentList(int index) {
			if (parentList==null) {
				this.parentList = new ArrayList<>();
			}
			return getIndex(parentList, index, () -> {
						Parent.ParentBuilder newParentList = Parent.builder();
						return newParentList;
					});
		}
		
		@Override
		@RosettaAttribute("otherParentList")
		@RuneAttribute("otherParentList")
		public List<? extends Parent.ParentBuilder> getOtherParentList() {
			return otherParentList;
		}
		
		@Override
		public Parent.ParentBuilder getOrCreateOtherParentList(int index) {
			if (otherParentList==null) {
				this.otherParentList = new ArrayList<>();
			}
			return getIndex(otherParentList, index, () -> {
						Parent.ParentBuilder newOtherParentList = Parent.builder();
						return newOtherParentList;
					});
		}
		
		@Override
		@RosettaAttribute(value="stringAttr", isRequired=true)
		@RuneAttribute(value="stringAttr", isRequired=true)
		public FieldWithMetaString.FieldWithMetaStringBuilder getStringAttr() {
			return stringAttr;
		}
		
		@Override
		public FieldWithMetaString.FieldWithMetaStringBuilder getOrCreateStringAttr() {
			FieldWithMetaString.FieldWithMetaStringBuilder result;
			if (stringAttr!=null) {
				result = stringAttr;
			}
			else {
				result = stringAttr = FieldWithMetaString.builder();
			}
			
			return result;
		}
		
		@RosettaAttribute(value="attr", isRequired=true)
		@RuneAttribute(value="attr", isRequired=true)
		@Override
		public Foo1.Foo1Builder setAttr(Integer _attr) {
			this.attr = _attr == null ? null : _attr;
			return this;
		}
		
		@RosettaAttribute("numberAttr")
		@RuneAttribute("numberAttr")
		@Override
		public Foo1.Foo1Builder setNumberAttr(BigDecimal _numberAttr) {
			this.numberAttr = _numberAttr == null ? null : _numberAttr;
			return this;
		}
		
		@RosettaAttribute(value="parent", isRequired=true)
		@RuneAttribute(value="parent", isRequired=true)
		@Override
		public Foo1.Foo1Builder setParent(Parent _parent) {
			this.parent = _parent == null ? null : _parent.toBuilder();
			return this;
		}
		
		@RosettaAttribute("parentList")
		@RuneAttribute("parentList")
		@Override
		public Foo1.Foo1Builder addParentList(Parent _parentList) {
			if (_parentList != null) {
				this.parentList.add(_parentList.toBuilder());
			}
			return this;
		}
		
		@Override
		public Foo1.Foo1Builder addParentList(Parent _parentList, int idx) {
			getIndex(this.parentList, idx, () -> _parentList.toBuilder());
			return this;
		}
		
		@Override
		public Foo1.Foo1Builder addParentList(List<? extends Parent> parentLists) {
			if (parentLists != null) {
				for (final Parent toAdd : parentLists) {
					this.parentList.add(toAdd.toBuilder());
				}
			}
			return this;
		}
		
		@RuneAttribute("parentList")
		@Override
		public Foo1.Foo1Builder setParentList(List<? extends Parent> parentLists) {
			if (parentLists == null) {
				this.parentList = new ArrayList<>();
			} else {
				this.parentList = parentLists.stream()
					.map(_a->_a.toBuilder())
					.collect(Collectors.toCollection(()->new ArrayList<>()));
			}
			return this;
		}
		
		@RosettaAttribute("otherParentList")
		@RuneAttribute("otherParentList")
		@Override
		public Foo1.Foo1Builder addOtherParentList(Parent _otherParentList) {
			if (_otherParentList != null) {
				this.otherParentList.add(_otherParentList.toBuilder());
			}
			return this;
		}
		
		@Override
		public Foo1.Foo1Builder addOtherParentList(Parent _otherParentList, int idx) {
			getIndex(this.otherParentList, idx, () -> _otherParentList.toBuilder());
			return this;
		}
		
		@Override
		public Foo1.Foo1Builder addOtherParentList(List<? extends Parent> otherParentLists) {
			if (otherParentLists != null) {
				for (final Parent toAdd : otherParentLists) {
					this.otherParentList.add(toAdd.toBuilder());
				}
			}
			return this;
		}
		
		@RuneAttribute("otherParentList")
		@Override
		public Foo1.Foo1Builder setOtherParentList(List<? extends Parent> otherParentLists) {
			if (otherParentLists == null) {
				this.otherParentList = new ArrayList<>();
			} else {
				this.otherParentList = otherParentLists.stream()
					.map(_a->_a.toBuilder())
					.collect(Collectors.toCollection(()->new ArrayList<>()));
			}
			return this;
		}
		
		@RosettaAttribute(value="stringAttr", isRequired=true)
		@RuneAttribute(value="stringAttr", isRequired=true)
		@Override
		public Foo1.Foo1Builder setStringAttr(FieldWithMetaString _stringAttr) {
			this.stringAttr = _stringAttr == null ? null : _stringAttr.toBuilder();
			return this;
		}
		
		@Override
		public Foo1.Foo1Builder setStringAttrValue(String _stringAttr) {
			this.getOrCreateStringAttr().setValue(_stringAttr);
			return this;
		}
		
		@Override
		public Foo1 build() {
			return new Foo1.Foo1Impl(this);
		}
		
		@Override
		public Foo1.Foo1Builder toBuilder() {
			return this;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public Foo1.Foo1Builder prune() {
			if (parent!=null && !parent.prune().hasData()) parent = null;
			parentList = parentList.stream().filter(b->b!=null).<Parent.ParentBuilder>map(b->b.prune()).filter(b->b.hasData()).collect(Collectors.toList());
			otherParentList = otherParentList.stream().filter(b->b!=null).<Parent.ParentBuilder>map(b->b.prune()).filter(b->b.hasData()).collect(Collectors.toList());
			if (stringAttr!=null && !stringAttr.prune().hasData()) stringAttr = null;
			return this;
		}
		
		@Override
		public boolean hasData() {
			if (getAttr()!=null) return true;
			if (getNumberAttr()!=null) return true;
			if (getParent()!=null && getParent().hasData()) return true;
			if (getParentList()!=null && getParentList().stream().filter(Objects::nonNull).anyMatch(a->a.hasData())) return true;
			if (getOtherParentList()!=null && getOtherParentList().stream().filter(Objects::nonNull).anyMatch(a->a.hasData())) return true;
			if (getStringAttr()!=null) return true;
			return false;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public Foo1.Foo1Builder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
			Foo1.Foo1Builder o = (Foo1.Foo1Builder) other;
			
			merger.mergeRosetta(getParent(), o.getParent(), this::setParent);
			merger.mergeRosetta(getParentList(), o.getParentList(), this::getOrCreateParentList);
			merger.mergeRosetta(getOtherParentList(), o.getOtherParentList(), this::getOrCreateOtherParentList);
			merger.mergeRosetta(getStringAttr(), o.getStringAttr(), this::setStringAttr);
			
			merger.mergeBasic(getAttr(), o.getAttr(), this::setAttr);
			merger.mergeBasic(getNumberAttr(), o.getNumberAttr(), this::setNumberAttr);
			return this;
		}
	
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			Foo1 _that = getType().cast(o);
		
			if (!Objects.equals(attr, _that.getAttr())) return false;
			if (!Objects.equals(numberAttr, _that.getNumberAttr())) return false;
			if (!Objects.equals(parent, _that.getParent())) return false;
			if (!ListEquals.listEquals(parentList, _that.getParentList())) return false;
			if (!ListEquals.listEquals(otherParentList, _that.getOtherParentList())) return false;
			if (!Objects.equals(stringAttr, _that.getStringAttr())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (attr != null ? attr.hashCode() : 0);
			_result = 31 * _result + (numberAttr != null ? numberAttr.hashCode() : 0);
			_result = 31 * _result + (parent != null ? parent.hashCode() : 0);
			_result = 31 * _result + (parentList != null ? parentList.hashCode() : 0);
			_result = 31 * _result + (otherParentList != null ? otherParentList.hashCode() : 0);
			_result = 31 * _result + (stringAttr != null ? stringAttr.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "Foo1Builder {" +
				"attr=" + this.attr + ", " +
				"numberAttr=" + this.numberAttr + ", " +
				"parent=" + this.parent + ", " +
				"parentList=" + this.parentList + ", " +
				"otherParentList=" + this.otherParentList + ", " +
				"stringAttr=" + this.stringAttr +
			'}';
		}
	}
}
