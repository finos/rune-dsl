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
import com.rosetta.model.metafields.FieldWithMetaString.FieldWithMetaStringBuilder;
import com.rosetta.util.ListEquals;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import test.pojo.Child;
import test.pojo.Child.ChildBuilder;
import test.pojo.Foo2;
import test.pojo.Foo2.Foo2Builder;
import test.pojo.Foo3;
import test.pojo.Foo3.Foo3Builder;
import test.pojo.Foo3.Foo3BuilderImpl;
import test.pojo.Foo3.Foo3Impl;
import test.pojo.GrandChild;
import test.pojo.Parent;
import test.pojo.Parent.ParentBuilder;
import test.pojo.meta.Foo3Meta;
import test.pojo.metafields.ReferenceWithMetaChild;
import test.pojo.metafields.ReferenceWithMetaChild.ReferenceWithMetaChildBuilder;
import test.pojo.metafields.ReferenceWithMetaGrandChild;
import test.pojo.metafields.ReferenceWithMetaGrandChild.ReferenceWithMetaGrandChildBuilder;

import static java.util.Optional.ofNullable;

/**
 * @version 0.0.0
 */
@RosettaDataType(value="Foo3", builder=Foo3.Foo3BuilderImpl.class, version="0.0.0")
@RuneDataType(value="Foo3", model="test", builder=Foo3.Foo3BuilderImpl.class, version="0.0.0")
public interface Foo3 extends Foo2 {

	Foo3Meta metaData = new Foo3Meta();

	/*********************** Getter Methods  ***********************/
	Integer getNumberAttrOverriddenAsInteger();
	ReferenceWithMetaGrandChild getParentListOverriddenAsReferenceWithMetaGrandChild();

	/*********************** Build Methods  ***********************/
	Foo3 build();
	
	Foo3.Foo3Builder toBuilder();
	
	static Foo3.Foo3Builder builder() {
		return new Foo3.Foo3BuilderImpl();
	}

	/*********************** Utility Methods  ***********************/
	@Override
	default RosettaMetaData<? extends Foo3> metaData() {
		return metaData;
	}
	
	@Override
	@RuneAttribute("@type")
	default Class<? extends Foo3> getType() {
		return Foo3.class;
	}
	
	@Override
	default void process(RosettaPath path, Processor processor) {
		processor.processBasic(path.newSubPath("attr"), Integer.class, getAttr(), this);
		processor.processBasic(path.newSubPath("numberAttr"), Integer.class, getNumberAttrOverriddenAsInteger(), this);
		processRosetta(path.newSubPath("parent"), processor, Child.class, getParent());
		processRosetta(path.newSubPath("parentList"), processor, ReferenceWithMetaGrandChild.class, getParentListOverriddenAsReferenceWithMetaGrandChild());
		processRosetta(path.newSubPath("otherParentList"), processor, Child.class, getOtherParentList());
		processRosetta(path.newSubPath("stringAttr"), processor, FieldWithMetaString.class, getStringAttr());
	}
	

	/*********************** Builder Interface  ***********************/
	interface Foo3Builder extends Foo3, Foo2.Foo2Builder {
		ReferenceWithMetaGrandChild.ReferenceWithMetaGrandChildBuilder getOrCreateParentListOverriddenAsReferenceWithMetaGrandChild();
		@Override
		ReferenceWithMetaGrandChild.ReferenceWithMetaGrandChildBuilder getParentListOverriddenAsReferenceWithMetaGrandChild();
		@Override
		Foo3.Foo3Builder setAttr(Integer attr);
		@Override
		Foo3.Foo3Builder setNumberAttr(BigDecimal numberAttr);
		@Override
		Foo3.Foo3Builder setParent(Parent parent);
		@Override
		Foo3.Foo3Builder addParentList(Parent parentList);
		@Override
		Foo3.Foo3Builder addParentList(Parent parentList, int _idx);
		@Override
		Foo3.Foo3Builder addParentList(List<? extends Parent> parentList);
		@Override
		Foo3.Foo3Builder setParentList(List<? extends Parent> parentList);
		@Override
		Foo3.Foo3Builder addOtherParentList(Parent otherParentList);
		@Override
		Foo3.Foo3Builder addOtherParentList(Parent otherParentList, int _idx);
		@Override
		Foo3.Foo3Builder addOtherParentList(List<? extends Parent> otherParentList);
		@Override
		Foo3.Foo3Builder setOtherParentList(List<? extends Parent> otherParentList);
		@Override
		Foo3.Foo3Builder setStringAttr(FieldWithMetaString stringAttr);
		@Override
		Foo3.Foo3Builder setStringAttrValue(String stringAttr);
		@Override
		Foo3.Foo3Builder setNumberAttr(BigInteger numberAttr);
		@Override
		Foo3.Foo3Builder setParent(Child parent);
		@Override
		Foo3.Foo3Builder setParentList(ReferenceWithMetaChild parentList);
		@Override
		Foo3.Foo3Builder setParentListValue(Child parentList);
		@Override
		Foo3.Foo3Builder addOtherParentList(Child otherParentList);
		@Override
		Foo3.Foo3Builder addOtherParentList(Child otherParentList, int _idx);
		@Override
		Foo3.Foo3Builder addOtherParentListOverriddenAsChild(List<? extends Child> otherParentList);
		@Override
		Foo3.Foo3Builder setOtherParentListOverriddenAsChild(List<? extends Child> otherParentList);
		Foo3.Foo3Builder setNumberAttr(Integer numberAttr);
		Foo3.Foo3Builder setParentList(ReferenceWithMetaGrandChild parentList);
		Foo3.Foo3Builder setParentListValue(GrandChild parentList);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processor.processBasic(path.newSubPath("attr"), Integer.class, getAttr(), this);
			processor.processBasic(path.newSubPath("numberAttr"), Integer.class, getNumberAttrOverriddenAsInteger(), this);
			processRosetta(path.newSubPath("parent"), processor, Child.ChildBuilder.class, getParent());
			processRosetta(path.newSubPath("parentList"), processor, ReferenceWithMetaGrandChild.ReferenceWithMetaGrandChildBuilder.class, getParentListOverriddenAsReferenceWithMetaGrandChild());
			processRosetta(path.newSubPath("otherParentList"), processor, Child.ChildBuilder.class, getOtherParentList());
			processRosetta(path.newSubPath("stringAttr"), processor, FieldWithMetaString.FieldWithMetaStringBuilder.class, getStringAttr());
		}
		

		Foo3.Foo3Builder prune();
	}

	/*********************** Immutable Implementation of Foo3  ***********************/
	class Foo3Impl implements Foo3 {
		private final Integer attr;
		private final Integer numberAttr;
		private final Child parent;
		private final ReferenceWithMetaGrandChild parentList;
		private final List<? extends Child> otherParentList;
		private final FieldWithMetaString stringAttr;
		
		protected Foo3Impl(Foo3.Foo3Builder builder) {
			this.attr = builder.getAttr();
			this.numberAttr = builder.getNumberAttrOverriddenAsInteger();
			this.parent = ofNullable(builder.getParent()).map(f->f.build()).orElse(null);
			this.parentList = ofNullable(builder.getParentListOverriddenAsReferenceWithMetaGrandChild()).map(f->f.build()).orElse(null);
			this.otherParentList = ofNullable(builder.getOtherParentList()).filter(_l->!_l.isEmpty()).map(list -> list.stream().filter(Objects::nonNull).map(f->f.build()).filter(Objects::nonNull).collect(ImmutableList.toImmutableList())).orElse(null);
			this.stringAttr = ofNullable(builder.getStringAttr()).map(f->f.build()).orElse(null);
		}
		
		@Override
		@RosettaAttribute("attr")
		@RuneAttribute("attr")
		public Integer getAttr() {
			return attr;
		}
		
		@Override
		@RosettaAttribute("numberAttr")
		@RuneAttribute("numberAttr")
		public Integer getNumberAttrOverriddenAsInteger() {
			return numberAttr;
		}
		
		@Override
		@RosettaIgnore
		public BigInteger getNumberAttrOverriddenAsBigInteger() {
			return numberAttr == null ? null : BigInteger.valueOf(numberAttr);
		}
		
		@Override
		@RosettaIgnore
		public BigDecimal getNumberAttr() {
			return numberAttr == null ? null : BigDecimal.valueOf(numberAttr);
		}
		
		@Override
		@RosettaAttribute("parent")
		@RuneAttribute("parent")
		public Child getParent() {
			return parent;
		}
		
		@Override
		@RosettaAttribute("parentList")
		@RuneAttribute("parentList")
		public ReferenceWithMetaGrandChild getParentListOverriddenAsReferenceWithMetaGrandChild() {
			return parentList;
		}
		
		@Override
		@RosettaIgnore
		public ReferenceWithMetaChild getParentListOverriddenAsSingleReferenceWithMetaChild() {
			if (parentList == null) {
				return ReferenceWithMetaChild.builder().build();
			}
			final GrandChild grandChild = parentList.getValue();
			return grandChild == null ? ReferenceWithMetaChild.builder().build() : ReferenceWithMetaChild.builder().setValue(grandChild).build();
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
		@RosettaAttribute("stringAttr")
		@RuneAttribute("stringAttr")
		public FieldWithMetaString getStringAttr() {
			return stringAttr;
		}
		
		@Override
		public Foo3 build() {
			return this;
		}
		
		@Override
		public Foo3.Foo3Builder toBuilder() {
			Foo3.Foo3Builder builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(Foo3.Foo3Builder builder) {
			ofNullable(getAttr()).ifPresent(builder::setAttr);
			ofNullable(getNumberAttrOverriddenAsInteger()).ifPresent(builder::setNumberAttr);
			ofNullable(getParent()).ifPresent(builder::setParent);
			ofNullable(getParentListOverriddenAsReferenceWithMetaGrandChild()).ifPresent(builder::setParentList);
			ofNullable(getOtherParentList()).ifPresent(builder::setOtherParentListOverriddenAsChild);
			ofNullable(getStringAttr()).ifPresent(builder::setStringAttr);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			Foo3 _that = getType().cast(o);
		
			if (!Objects.equals(attr, _that.getAttr())) return false;
			if (!Objects.equals(numberAttr, _that.getNumberAttrOverriddenAsInteger())) return false;
			if (!Objects.equals(parent, _that.getParent())) return false;
			if (!Objects.equals(parentList, _that.getParentListOverriddenAsReferenceWithMetaGrandChild())) return false;
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
			return "Foo3 {" +
				"attr=" + this.attr + ", " +
				"numberAttr=" + this.numberAttr + ", " +
				"parent=" + this.parent + ", " +
				"parentList=" + this.parentList + ", " +
				"otherParentList=" + this.otherParentList + ", " +
				"stringAttr=" + this.stringAttr +
			'}';
		}
	}

	/*********************** Builder Implementation of Foo3  ***********************/
	class Foo3BuilderImpl implements Foo3.Foo3Builder {
	
		protected Integer attr;
		protected Integer numberAttr;
		protected Child.ChildBuilder parent;
		protected ReferenceWithMetaGrandChild.ReferenceWithMetaGrandChildBuilder parentList;
		protected List<Child.ChildBuilder> otherParentList = new ArrayList<>();
		protected FieldWithMetaString.FieldWithMetaStringBuilder stringAttr;
		
		@Override
		@RosettaAttribute("attr")
		@RuneAttribute("attr")
		public Integer getAttr() {
			return attr;
		}
		
		@Override
		@RosettaAttribute("numberAttr")
		@RuneAttribute("numberAttr")
		public Integer getNumberAttrOverriddenAsInteger() {
			return numberAttr;
		}
		
		@Override
		@RosettaIgnore
		public BigInteger getNumberAttrOverriddenAsBigInteger() {
			return numberAttr == null ? null : BigInteger.valueOf(numberAttr);
		}
		
		@Override
		@RosettaIgnore
		public BigDecimal getNumberAttr() {
			return numberAttr == null ? null : BigDecimal.valueOf(numberAttr);
		}
		
		@Override
		@RosettaAttribute("parent")
		@RuneAttribute("parent")
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
		@RosettaAttribute("parentList")
		@RuneAttribute("parentList")
		public ReferenceWithMetaGrandChild.ReferenceWithMetaGrandChildBuilder getParentListOverriddenAsReferenceWithMetaGrandChild() {
			return parentList;
		}
		
		@Override
		public ReferenceWithMetaGrandChild.ReferenceWithMetaGrandChildBuilder getOrCreateParentListOverriddenAsReferenceWithMetaGrandChild() {
			ReferenceWithMetaGrandChild.ReferenceWithMetaGrandChildBuilder result;
			if (parentList!=null) {
				result = parentList;
			}
			else {
				result = parentList = ReferenceWithMetaGrandChild.builder();
			}
			
			return result;
		}
		
		@Override
		@RosettaIgnore
		public ReferenceWithMetaChild.ReferenceWithMetaChildBuilder getParentListOverriddenAsSingleReferenceWithMetaChild() {
			if (parentList == null) {
				return ReferenceWithMetaChild.builder().build().toBuilder();
			}
			final GrandChild _grandChild = parentList.getValue();
			return _grandChild == null ? ReferenceWithMetaChild.builder().build().toBuilder() : ReferenceWithMetaChild.builder().setValue(_grandChild).build().toBuilder();
		}
		
		@Override
		public ReferenceWithMetaChild.ReferenceWithMetaChildBuilder getOrCreateParentListOverriddenAsSingleReferenceWithMetaChild() {
			final ReferenceWithMetaGrandChild referenceWithMetaGrandChild0 = getOrCreateParentListOverriddenAsReferenceWithMetaGrandChild();
			if (referenceWithMetaGrandChild0 == null) {
				return ReferenceWithMetaChild.builder().build().toBuilder();
			}
			final GrandChild grandChild = referenceWithMetaGrandChild0.getValue();
			return grandChild == null ? ReferenceWithMetaChild.builder().build().toBuilder() : ReferenceWithMetaChild.builder().setValue(grandChild).build().toBuilder();
		}
		
		@Override
		@RosettaIgnore
		public List<? extends Parent.ParentBuilder> getParentList() {
			return parentList == null ? Collections.<Parent.ParentBuilder>emptyList() : Collections.singletonList(parentList.getValue().toBuilder());
		}
		
		@Override
		public Parent.ParentBuilder getOrCreateParentList(int _index) {
			final ReferenceWithMetaGrandChild referenceWithMetaGrandChild1 = getOrCreateParentListOverriddenAsReferenceWithMetaGrandChild();
			return referenceWithMetaGrandChild1 == null ? null : referenceWithMetaGrandChild1.getValue().toBuilder();
		}
		
		@Override
		@RosettaAttribute("otherParentList")
		@RuneAttribute("otherParentList")
		public List<? extends Child.ChildBuilder> getOtherParentList() {
			return otherParentList;
		}
		
		@Override
		public Child.ChildBuilder getOrCreateOtherParentList(int _index) {
		
			if (otherParentList==null) {
				this.otherParentList = new ArrayList<>();
			}
			Child.ChildBuilder result;
			return getIndex(otherParentList, _index, () -> {
						Child.ChildBuilder newOtherParentList = Child.builder();
						return newOtherParentList;
					});
		}
		
		@Override
		@RosettaAttribute("stringAttr")
		@RuneAttribute("stringAttr")
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
		
		@Override
		@RosettaAttribute("attr")
		@RuneAttribute("attr")
		public Foo3.Foo3Builder setAttr(Integer _attr) {
			this.attr = _attr == null ? null : _attr;
			return this;
		}
		
		@Override
		@RosettaAttribute("numberAttr")
		@RuneAttribute("numberAttr")
		public Foo3.Foo3Builder setNumberAttr(Integer _numberAttr) {
			this.numberAttr = _numberAttr == null ? null : _numberAttr;
			return this;
		}
		
		@Override
		@RosettaIgnore
		public Foo3.Foo3Builder setNumberAttr(BigInteger _numberAttr) {
			final Integer ifThenElseResult;
			if (_numberAttr == null) {
				ifThenElseResult = null;
			} else {
				ifThenElseResult = BigInteger.valueOf(_numberAttr.intValue()).equals(_numberAttr) ? _numberAttr.intValue() : null;
			}
			return setNumberAttr(ifThenElseResult);
		}
		
		@Override
		@RosettaIgnore
		public Foo3.Foo3Builder setNumberAttr(BigDecimal _numberAttr) {
			final Integer ifThenElseResult;
			if (_numberAttr == null) {
				ifThenElseResult = null;
			} else {
				ifThenElseResult = BigDecimal.valueOf(_numberAttr.intValue()).compareTo(_numberAttr) == 0 ? _numberAttr.intValue() : null;
			}
			return setNumberAttr(ifThenElseResult);
		}
		
		@Override
		@RosettaAttribute("parent")
		@RuneAttribute("parent")
		public Foo3.Foo3Builder setParent(Child _parent) {
			this.parent = _parent == null ? null : _parent.toBuilder();
			return this;
		}
		
		@Override
		@RosettaIgnore
		public Foo3.Foo3Builder setParent(Parent _parent) {
			final Child ifThenElseResult;
			if (_parent == null) {
				ifThenElseResult = null;
			} else {
				ifThenElseResult = _parent instanceof Child ? Child.class.cast(_parent) : null;
			}
			return setParent(ifThenElseResult);
		}
		
		@Override
		@RosettaAttribute("parentList")
		@RuneAttribute("parentList")
		public Foo3.Foo3Builder setParentList(ReferenceWithMetaGrandChild _parentList) {
			this.parentList = _parentList == null ? null : _parentList.toBuilder();
			return this;
		}
		
		@Override
		public Foo3.Foo3Builder setParentListValue(GrandChild _parentList) {
			this.getOrCreateParentListOverriddenAsReferenceWithMetaGrandChild().setValue(_parentList);
			return this;
		}
		
		@Override
		@RosettaIgnore
		public Foo3.Foo3Builder setParentList(ReferenceWithMetaChild _parentList) {
			final ReferenceWithMetaGrandChild ifThenElseResult;
			if (_parentList == null) {
				ifThenElseResult = ReferenceWithMetaGrandChild.builder().build();
			} else {
				final Child child = _parentList.getValue();
				if (child == null) {
					ifThenElseResult = ReferenceWithMetaGrandChild.builder().build();
				} else {
					ifThenElseResult = child instanceof GrandChild ? ReferenceWithMetaGrandChild.builder().setValue(GrandChild.class.cast(child)).build() : ReferenceWithMetaGrandChild.builder().setValue(null).build();
				}
			}
			return setParentList(ifThenElseResult);
		}
		
		@Override
		public Foo3.Foo3Builder setParentListValue(Child _parentList) {
			final GrandChild ifThenElseResult;
			if (_parentList == null) {
				ifThenElseResult = null;
			} else {
				ifThenElseResult = _parentList instanceof GrandChild ? GrandChild.class.cast(_parentList) : null;
			}
			return setParentListValue(ifThenElseResult);
		}
		
		@Override
		@RosettaIgnore
		public Foo3.Foo3Builder addParentList(Parent parentList0) {
			final ReferenceWithMetaGrandChild ifThenElseResult;
			if (parentList0 == null) {
				ifThenElseResult = ReferenceWithMetaGrandChild.builder().build();
			} else {
				ifThenElseResult = parentList0 instanceof GrandChild ? ReferenceWithMetaGrandChild.builder().setValue(GrandChild.class.cast(parentList0)).build() : ReferenceWithMetaGrandChild.builder().setValue(null).build();
			}
			return setParentList(ifThenElseResult);
		}
		
		@Override
		public Foo3.Foo3Builder addParentList(Parent _parentList, int _idx) {
			final ReferenceWithMetaGrandChild ifThenElseResult;
			if (_parentList == null) {
				ifThenElseResult = ReferenceWithMetaGrandChild.builder().build();
			} else {
				ifThenElseResult = _parentList instanceof GrandChild ? ReferenceWithMetaGrandChild.builder().setValue(GrandChild.class.cast(_parentList)).build() : ReferenceWithMetaGrandChild.builder().setValue(null).build();
			}
			return setParentList(ifThenElseResult);
		}
		
		@Override 
		public Foo3.Foo3Builder addParentList(List<? extends Parent> parentLists) {
			final Parent _parent = MapperC.of(parentLists).get();
			final ReferenceWithMetaGrandChild ifThenElseResult;
			if (_parent == null) {
				ifThenElseResult = ReferenceWithMetaGrandChild.builder().build();
			} else {
				ifThenElseResult = _parent instanceof GrandChild ? ReferenceWithMetaGrandChild.builder().setValue(GrandChild.class.cast(_parent)).build() : ReferenceWithMetaGrandChild.builder().setValue(null).build();
			}
			return setParentList(ifThenElseResult);
		}
		
		@Override 
		@RosettaIgnore
		public Foo3.Foo3Builder setParentList(List<? extends Parent> parentLists) {
			final Parent _parent = MapperC.of(parentLists).get();
			final ReferenceWithMetaGrandChild ifThenElseResult;
			if (_parent == null) {
				ifThenElseResult = ReferenceWithMetaGrandChild.builder().build();
			} else {
				ifThenElseResult = _parent instanceof GrandChild ? ReferenceWithMetaGrandChild.builder().setValue(GrandChild.class.cast(_parent)).build() : ReferenceWithMetaGrandChild.builder().setValue(null).build();
			}
			return setParentList(ifThenElseResult);
		}
		
		@Override
		@RosettaAttribute("otherParentList")
		@RuneAttribute("otherParentList")
		public Foo3.Foo3Builder addOtherParentList(Child _otherParentList) {
			if (_otherParentList != null) {
				this.otherParentList.add(_otherParentList.toBuilder());
			}
			return this;
		}
		
		@Override
		public Foo3.Foo3Builder addOtherParentList(Child _otherParentList, int _idx) {
			getIndex(this.otherParentList, _idx, () -> _otherParentList.toBuilder());
			return this;
		}
		
		@Override 
		public Foo3.Foo3Builder addOtherParentListOverriddenAsChild(List<? extends Child> otherParentLists) {
			if (otherParentLists != null) {
				for (final Child toAdd : otherParentLists) {
					this.otherParentList.add(toAdd.toBuilder());
				}
			}
			return this;
		}
		
		@Override 
		@RuneAttribute("otherParentList")
		public Foo3.Foo3Builder setOtherParentListOverriddenAsChild(List<? extends Child> otherParentLists) {
			if (otherParentLists == null) {
				this.otherParentList = new ArrayList<>();
			} else {
				this.otherParentList = otherParentLists.stream()
					.map(_a->_a.toBuilder())
					.collect(Collectors.toCollection(()->new ArrayList<>()));
			}
			return this;
		}
		
		@Override
		@RosettaIgnore
		public Foo3.Foo3Builder addOtherParentList(Parent otherParentList0) {
			final Child ifThenElseResult;
			if (otherParentList0 == null) {
				ifThenElseResult = null;
			} else {
				ifThenElseResult = otherParentList0 instanceof Child ? Child.class.cast(otherParentList0) : null;
			}
			return addOtherParentList(ifThenElseResult);
		}
		
		@Override
		public Foo3.Foo3Builder addOtherParentList(Parent _otherParentList, int _idx) {
			final Child ifThenElseResult;
			if (_otherParentList == null) {
				ifThenElseResult = null;
			} else {
				ifThenElseResult = _otherParentList instanceof Child ? Child.class.cast(_otherParentList) : null;
			}
			return addOtherParentList(ifThenElseResult, _idx);
		}
		
		@Override 
		public Foo3.Foo3Builder addOtherParentList(List<? extends Parent> otherParentLists) {
			return addOtherParentListOverriddenAsChild(otherParentLists.stream()
				.<Child>map(_parent -> _parent instanceof Child ? Child.class.cast(_parent) : null)
				.collect(Collectors.toList())
			);
		}
		
		@Override 
		@RosettaIgnore
		public Foo3.Foo3Builder setOtherParentList(List<? extends Parent> otherParentLists) {
			return setOtherParentListOverriddenAsChild(otherParentLists.stream()
				.<Child>map(_parent -> _parent instanceof Child ? Child.class.cast(_parent) : null)
				.collect(Collectors.toList())
			);
		}
		
		@Override
		@RosettaAttribute("stringAttr")
		@RuneAttribute("stringAttr")
		public Foo3.Foo3Builder setStringAttr(FieldWithMetaString _stringAttr) {
			this.stringAttr = _stringAttr == null ? null : _stringAttr.toBuilder();
			return this;
		}
		
		@Override
		public Foo3.Foo3Builder setStringAttrValue(String _stringAttr) {
			this.getOrCreateStringAttr().setValue(_stringAttr);
			return this;
		}
		
		@Override
		public Foo3 build() {
			return new Foo3.Foo3Impl(this);
		}
		
		@Override
		public Foo3.Foo3Builder toBuilder() {
			return this;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public Foo3.Foo3Builder prune() {
			if (parent!=null && !parent.prune().hasData()) parent = null;
			if (parentList!=null && !parentList.prune().hasData()) parentList = null;
			otherParentList = otherParentList.stream().filter(b->b!=null).<Child.ChildBuilder>map(b->b.prune()).filter(b->b.hasData()).collect(Collectors.toList());
			if (stringAttr!=null && !stringAttr.prune().hasData()) stringAttr = null;
			return this;
		}
		
		@Override
		public boolean hasData() {
			if (getAttr()!=null) return true;
			if (getNumberAttrOverriddenAsInteger()!=null) return true;
			if (getParent()!=null && getParent().hasData()) return true;
			if (getParentListOverriddenAsReferenceWithMetaGrandChild()!=null && getParentListOverriddenAsReferenceWithMetaGrandChild().hasData()) return true;
			if (getOtherParentList()!=null && getOtherParentList().stream().filter(Objects::nonNull).anyMatch(a->a.hasData())) return true;
			if (getStringAttr()!=null) return true;
			return false;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public Foo3.Foo3Builder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
			Foo3.Foo3Builder o = (Foo3.Foo3Builder) other;
			
			merger.mergeRosetta(getParent(), o.getParent(), this::setParent);
			merger.mergeRosetta(getParentListOverriddenAsReferenceWithMetaGrandChild(), o.getParentListOverriddenAsReferenceWithMetaGrandChild(), this::setParentList);
			merger.mergeRosetta(getOtherParentList(), o.getOtherParentList(), this::getOrCreateOtherParentList);
			merger.mergeRosetta(getStringAttr(), o.getStringAttr(), this::setStringAttr);
			
			merger.mergeBasic(getAttr(), o.getAttr(), this::setAttr);
			merger.mergeBasic(getNumberAttrOverriddenAsInteger(), o.getNumberAttrOverriddenAsInteger(), this::setNumberAttr);
			return this;
		}
	
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			Foo3 _that = getType().cast(o);
		
			if (!Objects.equals(attr, _that.getAttr())) return false;
			if (!Objects.equals(numberAttr, _that.getNumberAttrOverriddenAsInteger())) return false;
			if (!Objects.equals(parent, _that.getParent())) return false;
			if (!Objects.equals(parentList, _that.getParentListOverriddenAsReferenceWithMetaGrandChild())) return false;
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
			return "Foo3Builder {" +
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
