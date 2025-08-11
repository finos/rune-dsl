package test.pojo;

import com.google.common.collect.ImmutableList;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.annotations.RosettaAttribute;
import com.rosetta.model.lib.annotations.RosettaDataType;
import com.rosetta.model.lib.annotations.RosettaIgnore;
import com.rosetta.model.lib.annotations.RuneAttribute;
import com.rosetta.model.lib.annotations.RuneDataType;
import com.rosetta.model.lib.mapper.MapperC;
import com.rosetta.model.lib.meta.RosettaMetaData;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.process.BuilderMerger;
import com.rosetta.model.lib.process.BuilderProcessor;
import com.rosetta.model.lib.process.Processor;
import com.rosetta.model.metafields.FieldWithMetaString;
import com.rosetta.util.ListEquals;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import test.pojo.meta.Foo2Meta;
import test.pojo.metafields.ReferenceWithMetaChild;

import static java.util.Optional.ofNullable;

/**
 * @version 0.0.0
 */
@RosettaDataType(value="Foo2", builder=Foo2.Foo2BuilderImpl.class, version="0.0.0")
@RuneDataType(value="Foo2", model="test", builder=Foo2.Foo2BuilderImpl.class, version="0.0.0")
public interface Foo2 extends Foo1 {

	Foo2Meta metaData = new Foo2Meta();

	/*********************** Getter Methods  ***********************/
	BigInteger getNumberAttrOverriddenAsBigInteger();
	@Override
	Child getParent();
	ReferenceWithMetaChild getParentListOverriddenAsSingleReferenceWithMetaChild();
	@Override
	List<? extends Child> getOtherParentList();

	/*********************** Build Methods  ***********************/
	Foo2 build();
	
	Foo2.Foo2Builder toBuilder();
	
	static Foo2.Foo2Builder builder() {
		return new Foo2.Foo2BuilderImpl();
	}

	/*********************** Utility Methods  ***********************/
	@Override
	default RosettaMetaData<? extends Foo2> metaData() {
		return metaData;
	}
	
	@Override
	@RuneAttribute("@type")
	default Class<? extends Foo2> getType() {
		return Foo2.class;
	}
	
	@Override
	default void process(RosettaPath path, Processor processor) {
		processor.processBasic(path.newSubPath("attr"), Integer.class, getAttr(), this);
		processor.processBasic(path.newSubPath("numberAttr"), BigInteger.class, getNumberAttrOverriddenAsBigInteger(), this);
		processRosetta(path.newSubPath("parent"), processor, Child.class, getParent());
		processRosetta(path.newSubPath("parentList"), processor, ReferenceWithMetaChild.class, getParentListOverriddenAsSingleReferenceWithMetaChild());
		processRosetta(path.newSubPath("otherParentList"), processor, Child.class, getOtherParentList());
		processRosetta(path.newSubPath("stringAttr"), processor, FieldWithMetaString.class, getStringAttr());
	}
	

	/*********************** Builder Interface  ***********************/
	interface Foo2Builder extends Foo2, Foo1.Foo1Builder {
		Child.ChildBuilder getOrCreateParent();
		@Override
		Child.ChildBuilder getParent();
		ReferenceWithMetaChild.ReferenceWithMetaChildBuilder getOrCreateParentListOverriddenAsSingleReferenceWithMetaChild();
		@Override
		ReferenceWithMetaChild.ReferenceWithMetaChildBuilder getParentListOverriddenAsSingleReferenceWithMetaChild();
		Child.ChildBuilder getOrCreateOtherParentList(int index);
		@Override
		List<? extends Child.ChildBuilder> getOtherParentList();
		@Override
		Foo2.Foo2Builder setAttr(Integer attr);
		@Override
		Foo2.Foo2Builder setNumberAttr(BigDecimal numberAttr);
		@Override
		Foo2.Foo2Builder setParent(Parent parent);
		@Override
		Foo2.Foo2Builder addParentList(Parent parentList);
		@Override
		Foo2.Foo2Builder addParentList(Parent parentList, int idx);
		@Override
		Foo2.Foo2Builder addParentList(List<? extends Parent> parentList);
		@Override
		Foo2.Foo2Builder setParentList(List<? extends Parent> parentList);
		@Override
		Foo2.Foo2Builder addOtherParentList(Parent otherParentList);
		@Override
		Foo2.Foo2Builder addOtherParentList(Parent otherParentList, int idx);
		@Override
		Foo2.Foo2Builder addOtherParentList(List<? extends Parent> otherParentList);
		@Override
		Foo2.Foo2Builder setOtherParentList(List<? extends Parent> otherParentList);
		@Override
		Foo2.Foo2Builder setStringAttr(FieldWithMetaString stringAttr);
		@Override
		Foo2.Foo2Builder setStringAttrValue(String stringAttr);
		Foo2.Foo2Builder setNumberAttr(BigInteger numberAttr);
		Foo2.Foo2Builder setParent(Child parent);
		Foo2.Foo2Builder setParentList(ReferenceWithMetaChild parentList);
		Foo2.Foo2Builder setParentListValue(Child parentList);
		Foo2.Foo2Builder addOtherParentListOverriddenAsChild(Child otherParentList);
		Foo2.Foo2Builder addOtherParentListOverriddenAsChild(Child otherParentList, int idx);
		Foo2.Foo2Builder addOtherParentListOverriddenAsChild(List<? extends Child> otherParentList);
		Foo2.Foo2Builder setOtherParentListOverriddenAsChild(List<? extends Child> otherParentList);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processor.processBasic(path.newSubPath("attr"), Integer.class, getAttr(), this);
			processor.processBasic(path.newSubPath("numberAttr"), BigInteger.class, getNumberAttrOverriddenAsBigInteger(), this);
			processRosetta(path.newSubPath("parent"), processor, Child.ChildBuilder.class, getParent());
			processRosetta(path.newSubPath("parentList"), processor, ReferenceWithMetaChild.ReferenceWithMetaChildBuilder.class, getParentListOverriddenAsSingleReferenceWithMetaChild());
			processRosetta(path.newSubPath("otherParentList"), processor, Child.ChildBuilder.class, getOtherParentList());
			processRosetta(path.newSubPath("stringAttr"), processor, FieldWithMetaString.FieldWithMetaStringBuilder.class, getStringAttr());
		}
		

		Foo2.Foo2Builder prune();
	}

	/*********************** Immutable Implementation of Foo2  ***********************/
	class Foo2Impl implements Foo2 {
		private final Integer attr;
		private final BigInteger numberAttr;
		private final Child parent;
		private final ReferenceWithMetaChild parentList;
		private final List<? extends Child> otherParentList;
		private final FieldWithMetaString stringAttr;
		
		protected Foo2Impl(Foo2.Foo2Builder builder) {
			this.attr = builder.getAttr();
			this.numberAttr = builder.getNumberAttrOverriddenAsBigInteger();
			this.parent = ofNullable(builder.getParent()).map(f->f.build()).orElse(null);
			this.parentList = ofNullable(builder.getParentListOverriddenAsSingleReferenceWithMetaChild()).map(f->f.build()).orElse(null);
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
		@RosettaAttribute(value="numberAttr", isRequired=true)
		@RuneAttribute(value="numberAttr", isRequired=true)
		public BigInteger getNumberAttrOverriddenAsBigInteger() {
			return numberAttr;
		}
		
		@Override
		@RosettaIgnore
		public BigDecimal getNumberAttr() {
			return numberAttr == null ? null : new BigDecimal(numberAttr);
		}
		
		@Override
		@RosettaAttribute(value="parent", isRequired=true)
		@RuneAttribute(value="parent", isRequired=true)
		public Child getParent() {
			return parent;
		}
		
		@Override
		@RosettaAttribute(value="parentList", isRequired=true)
		@RuneAttribute(value="parentList", isRequired=true)
		public ReferenceWithMetaChild getParentListOverriddenAsSingleReferenceWithMetaChild() {
			return parentList;
		}
		
		@Override
		@RosettaIgnore
		public List<? extends Parent> getParentList() {
			return parentList == null ? Collections.<Parent>emptyList() : Collections.singletonList(parentList.getValue());
		}
		
		@Override
		@RosettaAttribute("otherParentList")
		@RuneAttribute("otherParentList")
		public List<? extends Child> getOtherParentList() {
			return otherParentList;
		}
		
		@Override
		@RosettaAttribute(value="stringAttr", isRequired=true)
		@RuneAttribute(value="stringAttr", isRequired=true)
		public FieldWithMetaString getStringAttr() {
			return stringAttr;
		}
		
		@Override
		public Foo2 build() {
			return this;
		}
		
		@Override
		public Foo2.Foo2Builder toBuilder() {
			Foo2.Foo2Builder builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(Foo2.Foo2Builder builder) {
			ofNullable(getAttr()).ifPresent(builder::setAttr);
			ofNullable(getNumberAttrOverriddenAsBigInteger()).ifPresent(builder::setNumberAttr);
			ofNullable(getParent()).ifPresent(builder::setParent);
			ofNullable(getParentListOverriddenAsSingleReferenceWithMetaChild()).ifPresent(builder::setParentList);
			ofNullable(getOtherParentList()).ifPresent(builder::setOtherParentListOverriddenAsChild);
			ofNullable(getStringAttr()).ifPresent(builder::setStringAttr);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			Foo2 _that = getType().cast(o);
		
			if (!Objects.equals(attr, _that.getAttr())) return false;
			if (!Objects.equals(numberAttr, _that.getNumberAttrOverriddenAsBigInteger())) return false;
			if (!Objects.equals(parent, _that.getParent())) return false;
			if (!Objects.equals(parentList, _that.getParentListOverriddenAsSingleReferenceWithMetaChild())) return false;
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
			return "Foo2 {" +
				"attr=" + this.attr + ", " +
				"numberAttr=" + this.numberAttr + ", " +
				"parent=" + this.parent + ", " +
				"parentList=" + this.parentList + ", " +
				"otherParentList=" + this.otherParentList + ", " +
				"stringAttr=" + this.stringAttr +
			'}';
		}
	}

	/*********************** Builder Implementation of Foo2  ***********************/
	class Foo2BuilderImpl implements Foo2.Foo2Builder {
	
		protected Integer attr;
		protected BigInteger numberAttr;
		protected Child.ChildBuilder parent;
		protected ReferenceWithMetaChild.ReferenceWithMetaChildBuilder parentList;
		protected List<Child.ChildBuilder> otherParentList = new ArrayList<>();
		protected FieldWithMetaString.FieldWithMetaStringBuilder stringAttr;
		
		@Override
		@RosettaAttribute(value="attr", isRequired=true)
		@RuneAttribute(value="attr", isRequired=true)
		public Integer getAttr() {
			return attr;
		}
		
		@Override
		@RosettaAttribute(value="numberAttr", isRequired=true)
		@RuneAttribute(value="numberAttr", isRequired=true)
		public BigInteger getNumberAttrOverriddenAsBigInteger() {
			return numberAttr;
		}
		
		@Override
		@RosettaIgnore
		public BigDecimal getNumberAttr() {
			return numberAttr == null ? null : new BigDecimal(numberAttr);
		}
		
		@Override
		@RosettaAttribute(value="parent", isRequired=true)
		@RuneAttribute(value="parent", isRequired=true)
		public Child.ChildBuilder getParent() {
			return parent;
		}
		
		@Override
		public Child.ChildBuilder getOrCreateParent() {
			Child.ChildBuilder result;
			if (parent!=null) {
				result = parent;
			}
			else {
				result = parent = Child.builder();
			}
			
			return result;
		}
		
		@Override
		@RosettaAttribute(value="parentList", isRequired=true)
		@RuneAttribute(value="parentList", isRequired=true)
		public ReferenceWithMetaChild.ReferenceWithMetaChildBuilder getParentListOverriddenAsSingleReferenceWithMetaChild() {
			return parentList;
		}
		
		@Override
		public ReferenceWithMetaChild.ReferenceWithMetaChildBuilder getOrCreateParentListOverriddenAsSingleReferenceWithMetaChild() {
			ReferenceWithMetaChild.ReferenceWithMetaChildBuilder result;
			if (parentList!=null) {
				result = parentList;
			}
			else {
				result = parentList = ReferenceWithMetaChild.builder();
			}
			
			return result;
		}
		
		@Override
		@RosettaIgnore
		public List<? extends Parent.ParentBuilder> getParentList() {
			return parentList == null ? Collections.<Parent.ParentBuilder>emptyList() : Collections.singletonList(parentList.getValue().toBuilder());
		}
		
		@Override
		public Parent.ParentBuilder getOrCreateParentList(int index) {
			final ReferenceWithMetaChild referenceWithMetaChild = getOrCreateParentListOverriddenAsSingleReferenceWithMetaChild();
			return referenceWithMetaChild == null ? null : referenceWithMetaChild.getValue().toBuilder();
		}
		
		@Override
		@RosettaAttribute("otherParentList")
		@RuneAttribute("otherParentList")
		public List<? extends Child.ChildBuilder> getOtherParentList() {
			return otherParentList;
		}
		
		@Override
		public Child.ChildBuilder getOrCreateOtherParentList(int index) {
			if (otherParentList==null) {
				this.otherParentList = new ArrayList<>();
			}
			return getIndex(otherParentList, index, () -> {
						Child.ChildBuilder newOtherParentList = Child.builder();
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
		public Foo2.Foo2Builder setAttr(Integer _attr) {
			this.attr = _attr == null ? null : _attr;
			return this;
		}
		
		@RosettaAttribute(value="numberAttr", isRequired=true)
		@RuneAttribute(value="numberAttr", isRequired=true)
		@Override
		public Foo2.Foo2Builder setNumberAttr(BigInteger _numberAttr) {
			this.numberAttr = _numberAttr == null ? null : _numberAttr;
			return this;
		}
		
		@RosettaIgnore
		@Override
		public Foo2.Foo2Builder setNumberAttr(BigDecimal _numberAttr) {
			final BigInteger ifThenElseResult;
			if (_numberAttr == null) {
				ifThenElseResult = null;
			} else {
				ifThenElseResult = new BigDecimal(_numberAttr.toBigInteger()).compareTo(_numberAttr) == 0 ? _numberAttr.toBigInteger() : null;
			}
			return setNumberAttr(ifThenElseResult);
		}
		
		@RosettaAttribute(value="parent", isRequired=true)
		@RuneAttribute(value="parent", isRequired=true)
		@Override
		public Foo2.Foo2Builder setParent(Child _parent) {
			this.parent = _parent == null ? null : _parent.toBuilder();
			return this;
		}
		
		@RosettaIgnore
		@Override
		public Foo2.Foo2Builder setParent(Parent _parent) {
			final Child ifThenElseResult;
			if (_parent == null) {
				ifThenElseResult = null;
			} else {
				ifThenElseResult = _parent instanceof Child ? Child.class.cast(_parent) : null;
			}
			return setParent(ifThenElseResult);
		}
		
		@RosettaAttribute(value="parentList", isRequired=true)
		@RuneAttribute(value="parentList", isRequired=true)
		@Override
		public Foo2.Foo2Builder setParentList(ReferenceWithMetaChild _parentList) {
			this.parentList = _parentList == null ? null : _parentList.toBuilder();
			return this;
		}
		
		@Override
		public Foo2.Foo2Builder setParentListValue(Child _parentList) {
			this.getOrCreateParentListOverriddenAsSingleReferenceWithMetaChild().setValue(_parentList);
			return this;
		}
		
		@RosettaIgnore
		@Override
		public Foo2.Foo2Builder addParentList(Parent _parentList) {
			final ReferenceWithMetaChild ifThenElseResult;
			if (_parentList == null) {
				ifThenElseResult = ReferenceWithMetaChild.builder().build();
			} else {
				ifThenElseResult = _parentList instanceof Child ? ReferenceWithMetaChild.builder().setValue(Child.class.cast(_parentList)).build() : ReferenceWithMetaChild.builder().setValue(null).build();
			}
			return setParentList(ifThenElseResult);
		}
		
		@Override
		public Foo2.Foo2Builder addParentList(Parent _parentList, int idx) {
			final ReferenceWithMetaChild ifThenElseResult;
			if (_parentList == null) {
				ifThenElseResult = ReferenceWithMetaChild.builder().build();
			} else {
				ifThenElseResult = _parentList instanceof Child ? ReferenceWithMetaChild.builder().setValue(Child.class.cast(_parentList)).build() : ReferenceWithMetaChild.builder().setValue(null).build();
			}
			return setParentList(ifThenElseResult);
		}
		
		@Override
		public Foo2.Foo2Builder addParentList(List<? extends Parent> parentLists) {
			final Parent _parent = MapperC.of(parentLists).get();
			final ReferenceWithMetaChild ifThenElseResult;
			if (_parent == null) {
				ifThenElseResult = ReferenceWithMetaChild.builder().build();
			} else {
				ifThenElseResult = _parent instanceof Child ? ReferenceWithMetaChild.builder().setValue(Child.class.cast(_parent)).build() : ReferenceWithMetaChild.builder().setValue(null).build();
			}
			return setParentList(ifThenElseResult);
		}
		
		@RosettaIgnore
		@Override
		public Foo2.Foo2Builder setParentList(List<? extends Parent> parentLists) {
			final Parent _parent = MapperC.of(parentLists).get();
			final ReferenceWithMetaChild ifThenElseResult;
			if (_parent == null) {
				ifThenElseResult = ReferenceWithMetaChild.builder().build();
			} else {
				ifThenElseResult = _parent instanceof Child ? ReferenceWithMetaChild.builder().setValue(Child.class.cast(_parent)).build() : ReferenceWithMetaChild.builder().setValue(null).build();
			}
			return setParentList(ifThenElseResult);
		}
		
		@RosettaAttribute("otherParentList")
		@RuneAttribute("otherParentList")
		@Override
		public Foo2.Foo2Builder addOtherParentListOverriddenAsChild(Child _otherParentList) {
			if (_otherParentList != null) {
				this.otherParentList.add(_otherParentList.toBuilder());
			}
			return this;
		}
		
		@Override
		public Foo2.Foo2Builder addOtherParentListOverriddenAsChild(Child _otherParentList, int idx) {
			getIndex(this.otherParentList, idx, () -> _otherParentList.toBuilder());
			return this;
		}
		
		@Override
		public Foo2.Foo2Builder addOtherParentListOverriddenAsChild(List<? extends Child> otherParentLists) {
			if (otherParentLists != null) {
				for (final Child toAdd : otherParentLists) {
					this.otherParentList.add(toAdd.toBuilder());
				}
			}
			return this;
		}
		
		@RuneAttribute("otherParentList")
		@Override
		public Foo2.Foo2Builder setOtherParentListOverriddenAsChild(List<? extends Child> otherParentLists) {
			if (otherParentLists == null) {
				this.otherParentList = new ArrayList<>();
			} else {
				this.otherParentList = otherParentLists.stream()
					.map(_a->_a.toBuilder())
					.collect(Collectors.toCollection(()->new ArrayList<>()));
			}
			return this;
		}
		
		@RosettaIgnore
		@Override
		public Foo2.Foo2Builder addOtherParentList(Parent _otherParentList) {
			final Child ifThenElseResult;
			if (_otherParentList == null) {
				ifThenElseResult = null;
			} else {
				ifThenElseResult = _otherParentList instanceof Child ? Child.class.cast(_otherParentList) : null;
			}
			return addOtherParentListOverriddenAsChild(ifThenElseResult);
		}
		
		@Override
		public Foo2.Foo2Builder addOtherParentList(Parent _otherParentList, int idx) {
			final Child ifThenElseResult;
			if (_otherParentList == null) {
				ifThenElseResult = null;
			} else {
				ifThenElseResult = _otherParentList instanceof Child ? Child.class.cast(_otherParentList) : null;
			}
			return addOtherParentListOverriddenAsChild(ifThenElseResult, idx);
		}
		
		@Override
		public Foo2.Foo2Builder addOtherParentList(List<? extends Parent> otherParentLists) {
			return addOtherParentListOverriddenAsChild(otherParentLists.stream()
				.<Child>map(_parent -> _parent instanceof Child ? Child.class.cast(_parent) : null)
				.collect(Collectors.toList())
			);
		}
		
		@RosettaIgnore
		@Override
		public Foo2.Foo2Builder setOtherParentList(List<? extends Parent> otherParentLists) {
			return setOtherParentListOverriddenAsChild(otherParentLists.stream()
				.<Child>map(_parent -> _parent instanceof Child ? Child.class.cast(_parent) : null)
				.collect(Collectors.toList())
			);
		}
		
		@RosettaAttribute(value="stringAttr", isRequired=true)
		@RuneAttribute(value="stringAttr", isRequired=true)
		@Override
		public Foo2.Foo2Builder setStringAttr(FieldWithMetaString _stringAttr) {
			this.stringAttr = _stringAttr == null ? null : _stringAttr.toBuilder();
			return this;
		}
		
		@Override
		public Foo2.Foo2Builder setStringAttrValue(String _stringAttr) {
			this.getOrCreateStringAttr().setValue(_stringAttr);
			return this;
		}
		
		@Override
		public Foo2 build() {
			return new Foo2.Foo2Impl(this);
		}
		
		@Override
		public Foo2.Foo2Builder toBuilder() {
			return this;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public Foo2.Foo2Builder prune() {
			if (parent!=null && !parent.prune().hasData()) parent = null;
			if (parentList!=null && !parentList.prune().hasData()) parentList = null;
			otherParentList = otherParentList.stream().filter(b->b!=null).<Child.ChildBuilder>map(b->b.prune()).filter(b->b.hasData()).collect(Collectors.toList());
			if (stringAttr!=null && !stringAttr.prune().hasData()) stringAttr = null;
			return this;
		}
		
		@Override
		public boolean hasData() {
			if (getAttr()!=null) return true;
			if (getNumberAttrOverriddenAsBigInteger()!=null) return true;
			if (getParent()!=null && getParent().hasData()) return true;
			if (getParentListOverriddenAsSingleReferenceWithMetaChild()!=null && getParentListOverriddenAsSingleReferenceWithMetaChild().hasData()) return true;
			if (getOtherParentList()!=null && getOtherParentList().stream().filter(Objects::nonNull).anyMatch(a->a.hasData())) return true;
			if (getStringAttr()!=null) return true;
			return false;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public Foo2.Foo2Builder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
			Foo2.Foo2Builder o = (Foo2.Foo2Builder) other;
			
			merger.mergeRosetta(getParent(), o.getParent(), this::setParent);
			merger.mergeRosetta(getParentListOverriddenAsSingleReferenceWithMetaChild(), o.getParentListOverriddenAsSingleReferenceWithMetaChild(), this::setParentList);
			merger.mergeRosetta(getOtherParentList(), o.getOtherParentList(), this::getOrCreateOtherParentList);
			merger.mergeRosetta(getStringAttr(), o.getStringAttr(), this::setStringAttr);
			
			merger.mergeBasic(getAttr(), o.getAttr(), this::setAttr);
			merger.mergeBasic(getNumberAttrOverriddenAsBigInteger(), o.getNumberAttrOverriddenAsBigInteger(), this::setNumberAttr);
			return this;
		}
	
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			Foo2 _that = getType().cast(o);
		
			if (!Objects.equals(attr, _that.getAttr())) return false;
			if (!Objects.equals(numberAttr, _that.getNumberAttrOverriddenAsBigInteger())) return false;
			if (!Objects.equals(parent, _that.getParent())) return false;
			if (!Objects.equals(parentList, _that.getParentListOverriddenAsSingleReferenceWithMetaChild())) return false;
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
			return "Foo2Builder {" +
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
