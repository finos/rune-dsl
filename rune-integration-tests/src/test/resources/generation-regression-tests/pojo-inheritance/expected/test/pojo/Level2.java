package test.pojo;

import com.google.common.collect.ImmutableList;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.annotations.Accessor;
import com.rosetta.model.lib.annotations.AccessorType;
import com.rosetta.model.lib.annotations.Multi;
import com.rosetta.model.lib.annotations.Required;
import com.rosetta.model.lib.annotations.RosettaAttribute;
import com.rosetta.model.lib.annotations.RosettaDataType;
import com.rosetta.model.lib.annotations.RosettaIgnore;
import com.rosetta.model.lib.annotations.RuneAttribute;
import com.rosetta.model.lib.annotations.RuneDataType;
import com.rosetta.model.lib.annotations.RuneIgnore;
import com.rosetta.model.lib.mapper.MapperC;
import com.rosetta.model.lib.meta.RosettaMetaData;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.process.BuilderMerger;
import com.rosetta.model.lib.process.BuilderProcessor;
import com.rosetta.model.lib.process.Processor;
import com.rosetta.model.metafields.FieldWithMetaString;
import com.rosetta.util.ListEquals;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import test.pojo.meta.Level2Meta;
import test.pojo.metafields.ReferenceWithMetaChild;

import static java.util.Optional.ofNullable;

/**
 * @version 0.0.0
 */
@RosettaDataType(value="Level2", builder=Level2.Level2BuilderImpl.class, version="0.0.0")
@RuneDataType(value="Level2", model="test", builder=Level2.Level2BuilderImpl.class, version="0.0.0")
public interface Level2 extends Level1 {

    Level2Meta metaData = new Level2Meta();

    /*********************** Getter Methods  ***********************/
    @Override
    Integer getAttr();
    @Override
    FieldWithMetaString getMetaSingle();
    @Override
    List<? extends FieldWithMetaString> getOtherMetaList();
    ReferenceWithMetaChild getSingleParentOverriddenAsReferenceWithMetaChild();
    FieldWithMetaString getMetaListOverriddenAsSingle();

    /*********************** Build Methods  ***********************/
    Level2 build();

    Level2.Level2Builder toBuilder();

    static Level2.Level2Builder builder() {
        return new Level2.Level2BuilderImpl();
    }

    /*********************** Utility Methods  ***********************/
    @Override
    default RosettaMetaData<? extends Level2> metaData() {
        return metaData;
    }

    @Override
    @RuneAttribute("@type")
    default Class<? extends Level2> getType() {
        return Level2.class;
    }

    @Override
    default void process(RosettaPath path, Processor processor) {
        processor.processBasic(path.newSubPath("attr"), Integer.class, getAttr(), this);
        processRosetta(path.newSubPath("metaSingle"), processor, FieldWithMetaString.class, getMetaSingle());
        processRosetta(path.newSubPath("otherMetaList"), processor, FieldWithMetaString.class, getOtherMetaList());
        processRosetta(path.newSubPath("singleParent"), processor, ReferenceWithMetaChild.class, getSingleParentOverriddenAsReferenceWithMetaChild());
        processRosetta(path.newSubPath("metaList"), processor, FieldWithMetaString.class, getMetaListOverriddenAsSingle());
    }


    /*********************** Builder Interface  ***********************/
    interface Level2Builder extends Level2, Level1.Level1Builder {
        FieldWithMetaString.FieldWithMetaStringBuilder getOrCreateMetaSingle();
        @Override
        FieldWithMetaString.FieldWithMetaStringBuilder getMetaSingle();
        FieldWithMetaString.FieldWithMetaStringBuilder getOrCreateOtherMetaList(int index);
        @Override
        List<? extends FieldWithMetaString.FieldWithMetaStringBuilder> getOtherMetaList();
        ReferenceWithMetaChild.ReferenceWithMetaChildBuilder getOrCreateSingleParentOverriddenAsReferenceWithMetaChild();
        @Override
        ReferenceWithMetaChild.ReferenceWithMetaChildBuilder getSingleParentOverriddenAsReferenceWithMetaChild();
        FieldWithMetaString.FieldWithMetaStringBuilder getOrCreateMetaListOverriddenAsSingle();
        @Override
        FieldWithMetaString.FieldWithMetaStringBuilder getMetaListOverriddenAsSingle();
        @Override
        Level2.Level2Builder setAttr(Integer attr);
        @Override
        Level2.Level2Builder setMetaSingle(FieldWithMetaString metaSingle);
        @Override
        Level2.Level2Builder setMetaSingleValue(String metaSingle);
        @Override
        Level2.Level2Builder addOtherMetaList(FieldWithMetaString otherMetaList);
        @Override
        Level2.Level2Builder addOtherMetaList(FieldWithMetaString otherMetaList, int idx);
        @Override
        Level2.Level2Builder addOtherMetaListValue(String otherMetaList);
        @Override
        Level2.Level2Builder addOtherMetaListValue(String otherMetaList, int idx);
        @Override
        Level2.Level2Builder addOtherMetaList(List<? extends FieldWithMetaString> otherMetaList);
        @Override
        Level2.Level2Builder setOtherMetaList(List<? extends FieldWithMetaString> otherMetaList);
        @Override
        Level2.Level2Builder addOtherMetaListValue(List<? extends String> otherMetaList);
        @Override
        Level2.Level2Builder setOtherMetaListValue(List<? extends String> otherMetaList);
        @Override
        Level2.Level2Builder setSingleParent(Parent singleParent);
        @Override
        Level2.Level2Builder addMetaList(FieldWithMetaString metaList);
        @Override
        Level2.Level2Builder addMetaList(FieldWithMetaString metaList, int idx);
        @Override
        Level2.Level2Builder addMetaListValue(String metaList);
        @Override
        Level2.Level2Builder addMetaListValue(String metaList, int idx);
        @Override
        Level2.Level2Builder addMetaList(List<? extends FieldWithMetaString> metaList);
        @Override
        Level2.Level2Builder setMetaList(List<? extends FieldWithMetaString> metaList);
        @Override
        Level2.Level2Builder addMetaListValue(List<? extends String> metaList);
        @Override
        Level2.Level2Builder setMetaListValue(List<? extends String> metaList);
        Level2.Level2Builder setAttrOverriddenAsInteger(Integer attr);
        Level2.Level2Builder setMetaSingleOverriddenAsFieldWithMetaString(FieldWithMetaString metaSingle);
        Level2.Level2Builder setMetaSingleOverriddenAsFieldWithMetaStringValue(String metaSingle);
        Level2.Level2Builder addOtherMetaListOverriddenAsFieldWithMetaString(FieldWithMetaString otherMetaList);
        Level2.Level2Builder addOtherMetaListOverriddenAsFieldWithMetaString(FieldWithMetaString otherMetaList, int idx);
        Level2.Level2Builder addOtherMetaListOverriddenAsFieldWithMetaStringValue(String otherMetaList);
        Level2.Level2Builder addOtherMetaListOverriddenAsFieldWithMetaStringValue(String otherMetaList, int idx);
        Level2.Level2Builder addOtherMetaListOverriddenAsFieldWithMetaString(List<? extends FieldWithMetaString> otherMetaList);
        Level2.Level2Builder setOtherMetaListOverriddenAsFieldWithMetaString(List<? extends FieldWithMetaString> otherMetaList);
        Level2.Level2Builder addOtherMetaListOverriddenAsFieldWithMetaStringValue(List<? extends String> otherMetaList);
        Level2.Level2Builder setOtherMetaListOverriddenAsFieldWithMetaStringValue(List<? extends String> otherMetaList);
        Level2.Level2Builder setSingleParent(ReferenceWithMetaChild singleParent);
        Level2.Level2Builder setSingleParentValue(Child singleParent);
        Level2.Level2Builder setMetaList(FieldWithMetaString metaList);
        Level2.Level2Builder setMetaListValue(String metaList);

        @Override
        default void process(RosettaPath path, BuilderProcessor processor) {
            processor.processBasic(path.newSubPath("attr"), Integer.class, getAttr(), this);
            processRosetta(path.newSubPath("metaSingle"), processor, FieldWithMetaString.FieldWithMetaStringBuilder.class, getMetaSingle());
            processRosetta(path.newSubPath("otherMetaList"), processor, FieldWithMetaString.FieldWithMetaStringBuilder.class, getOtherMetaList());
            processRosetta(path.newSubPath("singleParent"), processor, ReferenceWithMetaChild.ReferenceWithMetaChildBuilder.class, getSingleParentOverriddenAsReferenceWithMetaChild());
            processRosetta(path.newSubPath("metaList"), processor, FieldWithMetaString.FieldWithMetaStringBuilder.class, getMetaListOverriddenAsSingle());
        }


        Level2.Level2Builder prune();
    }

    /*********************** Immutable Implementation of Level2  ***********************/
    class Level2Impl implements Level2 {
        private final Integer attr;
        private final FieldWithMetaString metaSingle;
        private final List<? extends FieldWithMetaString> otherMetaList;
        private final ReferenceWithMetaChild singleParent;
        private final FieldWithMetaString metaList;

        protected Level2Impl(Level2.Level2Builder builder) {
            this.attr = builder.getAttr();
            this.metaSingle = ofNullable(builder.getMetaSingle()).map(f->f.build()).orElse(null);
            this.otherMetaList = ofNullable(builder.getOtherMetaList()).filter(_l->!_l.isEmpty()).map(list -> list.stream().filter(Objects::nonNull).map(f->f.build()).filter(Objects::nonNull).collect(ImmutableList.toImmutableList())).orElse(null);
            this.singleParent = ofNullable(builder.getSingleParentOverriddenAsReferenceWithMetaChild()).map(f->f.build()).orElse(null);
            this.metaList = ofNullable(builder.getMetaListOverriddenAsSingle()).map(f->f.build()).orElse(null);
        }

        @Override
        @RosettaAttribute("attr")
        @Accessor(AccessorType.GETTER)
        @Required
        @RuneAttribute("attr")
        public Integer getAttr() {
            return attr;
        }

        @Override
        @RosettaAttribute("metaSingle")
        @Accessor(AccessorType.GETTER)
        @Required
        @RuneAttribute("metaSingle")
        public FieldWithMetaString getMetaSingle() {
            return metaSingle;
        }

        @Override
        @RosettaAttribute("otherMetaList")
        @Accessor(AccessorType.GETTER)
        @Required
        @Multi
        @RuneAttribute("otherMetaList")
        public List<? extends FieldWithMetaString> getOtherMetaList() {
            return otherMetaList;
        }

        @Override
        @RosettaAttribute("singleParent")
        @Accessor(AccessorType.GETTER)
        @RuneAttribute("singleParent")
        public ReferenceWithMetaChild getSingleParentOverriddenAsReferenceWithMetaChild() {
            return singleParent;
        }

        @Override
        @RosettaIgnore
        @RuneIgnore
        public Parent getSingleParent() {
            return singleParent == null ? null : singleParent.getValue();
        }

        @Override
        @RosettaAttribute("metaList")
        @Accessor(AccessorType.GETTER)
        @Required
        @RuneAttribute("metaList")
        public FieldWithMetaString getMetaListOverriddenAsSingle() {
            return metaList;
        }

        @Override
        @RosettaIgnore
        @RuneIgnore
        public List<? extends FieldWithMetaString> getMetaList() {
            return metaList == null ? Collections.<FieldWithMetaString>emptyList() : Collections.singletonList(metaList);
        }

        @Override
        public Level2 build() {
            return this;
        }

        @Override
        public Level2.Level2Builder toBuilder() {
            Level2.Level2Builder builder = builder();
            setBuilderFields(builder);
            return builder;
        }

        protected void setBuilderFields(Level2.Level2Builder builder) {
            ofNullable(getAttr()).ifPresent(builder::setAttrOverriddenAsInteger);
            ofNullable(getMetaSingle()).ifPresent(builder::setMetaSingleOverriddenAsFieldWithMetaString);
            ofNullable(getOtherMetaList()).ifPresent(builder::setOtherMetaListOverriddenAsFieldWithMetaString);
            ofNullable(getSingleParentOverriddenAsReferenceWithMetaChild()).ifPresent(builder::setSingleParent);
            ofNullable(getMetaListOverriddenAsSingle()).ifPresent(builder::setMetaList);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;

            Level2 _that = getType().cast(o);

            if (!Objects.equals(attr, _that.getAttr())) return false;
            if (!Objects.equals(metaSingle, _that.getMetaSingle())) return false;
            if (!ListEquals.listEquals(otherMetaList, _that.getOtherMetaList())) return false;
            if (!Objects.equals(singleParent, _that.getSingleParentOverriddenAsReferenceWithMetaChild())) return false;
            if (!Objects.equals(metaList, _that.getMetaListOverriddenAsSingle())) return false;
            return true;
        }

        @Override
        public int hashCode() {
            int _result = 0;
            _result = 31 * _result + (attr != null ? attr.hashCode() : 0);
            _result = 31 * _result + (metaSingle != null ? metaSingle.hashCode() : 0);
            _result = 31 * _result + (otherMetaList != null ? otherMetaList.hashCode() : 0);
            _result = 31 * _result + (singleParent != null ? singleParent.hashCode() : 0);
            _result = 31 * _result + (metaList != null ? metaList.hashCode() : 0);
            return _result;
        }

        @Override
        public String toString() {
            return "Level2 {" +
                "attr=" + this.attr + ", " +
                "metaSingle=" + this.metaSingle + ", " +
                "otherMetaList=" + this.otherMetaList + ", " +
                "singleParent=" + this.singleParent + ", " +
                "metaList=" + this.metaList +
            '}';
        }
    }

    /*********************** Builder Implementation of Level2  ***********************/
    class Level2BuilderImpl implements Level2.Level2Builder {

        protected Integer attr;
        protected FieldWithMetaString.FieldWithMetaStringBuilder metaSingle;
        protected List<FieldWithMetaString.FieldWithMetaStringBuilder> otherMetaList = new ArrayList<>();
        protected ReferenceWithMetaChild.ReferenceWithMetaChildBuilder singleParent;
        protected FieldWithMetaString.FieldWithMetaStringBuilder metaList;

        @Override
        @RosettaAttribute("attr")
        @Accessor(AccessorType.GETTER)
        @Required
        @RuneAttribute("attr")
        public Integer getAttr() {
            return attr;
        }

        @Override
        @RosettaAttribute("metaSingle")
        @Accessor(AccessorType.GETTER)
        @Required
        @RuneAttribute("metaSingle")
        public FieldWithMetaString.FieldWithMetaStringBuilder getMetaSingle() {
            return metaSingle;
        }

        @Override
        public FieldWithMetaString.FieldWithMetaStringBuilder getOrCreateMetaSingle() {
            FieldWithMetaString.FieldWithMetaStringBuilder result;
            if (metaSingle!=null) {
                result = metaSingle;
            }
            else {
                result = metaSingle = FieldWithMetaString.builder();
            }

            return result;
        }

        @Override
        @RosettaAttribute("otherMetaList")
        @Accessor(AccessorType.GETTER)
        @Required
        @Multi
        @RuneAttribute("otherMetaList")
        public List<? extends FieldWithMetaString.FieldWithMetaStringBuilder> getOtherMetaList() {
            return otherMetaList;
        }

        @Override
        public FieldWithMetaString.FieldWithMetaStringBuilder getOrCreateOtherMetaList(int index) {
            if (otherMetaList==null) {
                this.otherMetaList = new ArrayList<>();
            }
            return getIndex(otherMetaList, index, () -> {
                        FieldWithMetaString.FieldWithMetaStringBuilder newOtherMetaList = FieldWithMetaString.builder();
                        return newOtherMetaList;
                    });
        }

        @Override
        @RosettaAttribute("singleParent")
        @Accessor(AccessorType.GETTER)
        @RuneAttribute("singleParent")
        public ReferenceWithMetaChild.ReferenceWithMetaChildBuilder getSingleParentOverriddenAsReferenceWithMetaChild() {
            return singleParent;
        }

        @Override
        public ReferenceWithMetaChild.ReferenceWithMetaChildBuilder getOrCreateSingleParentOverriddenAsReferenceWithMetaChild() {
            ReferenceWithMetaChild.ReferenceWithMetaChildBuilder result;
            if (singleParent!=null) {
                result = singleParent;
            }
            else {
                result = singleParent = ReferenceWithMetaChild.builder();
            }

            return result;
        }

        @Override
        @RosettaIgnore
        @RuneIgnore
        public Parent.ParentBuilder getSingleParent() {
            return singleParent == null ? null : singleParent.getValue().toBuilder();
        }

        @Override
        public Parent.ParentBuilder getOrCreateSingleParent() {
            final ReferenceWithMetaChild referenceWithMetaChild = getOrCreateSingleParentOverriddenAsReferenceWithMetaChild();
            return referenceWithMetaChild == null ? null : referenceWithMetaChild.getValue().toBuilder();
        }

        @Override
        @RosettaAttribute("metaList")
        @Accessor(AccessorType.GETTER)
        @Required
        @RuneAttribute("metaList")
        public FieldWithMetaString.FieldWithMetaStringBuilder getMetaListOverriddenAsSingle() {
            return metaList;
        }

        @Override
        public FieldWithMetaString.FieldWithMetaStringBuilder getOrCreateMetaListOverriddenAsSingle() {
            FieldWithMetaString.FieldWithMetaStringBuilder result;
            if (metaList!=null) {
                result = metaList;
            }
            else {
                result = metaList = FieldWithMetaString.builder();
            }

            return result;
        }

        @Override
        @RosettaIgnore
        @RuneIgnore
        public List<? extends FieldWithMetaString.FieldWithMetaStringBuilder> getMetaList() {
            return (metaList == null ? Collections.<FieldWithMetaString>emptyList() : Collections.singletonList(metaList)).stream().map(fieldWithMetaString -> fieldWithMetaString.toBuilder()).collect(Collectors.toList());
        }

        @Override
        public FieldWithMetaString.FieldWithMetaStringBuilder getOrCreateMetaList(int index) {
            return getOrCreateMetaListOverriddenAsSingle().toBuilder();
        }

        @RosettaAttribute("attr")
        @Accessor(AccessorType.SETTER)
        @Required
        @RuneAttribute("attr")
        @Override
        public Level2.Level2Builder setAttrOverriddenAsInteger(Integer _attr) {
            this.attr = _attr == null ? null : _attr;
            return this;
        }

        @RosettaIgnore
        @RuneIgnore
        @Override
        public Level2.Level2Builder setAttr(Integer _attr) {
            return setAttrOverriddenAsInteger(_attr);
        }

        @RosettaAttribute("metaSingle")
        @Accessor(AccessorType.SETTER)
        @Required
        @RuneAttribute("metaSingle")
        @Override
        public Level2.Level2Builder setMetaSingleOverriddenAsFieldWithMetaString(FieldWithMetaString _metaSingle) {
            this.metaSingle = _metaSingle == null ? null : _metaSingle.toBuilder();
            return this;
        }

        @Override
        public Level2.Level2Builder setMetaSingleOverriddenAsFieldWithMetaStringValue(String _metaSingle) {
            this.getOrCreateMetaSingle().setValue(_metaSingle);
            return this;
        }

        @RosettaIgnore
        @RuneIgnore
        @Override
        public Level2.Level2Builder setMetaSingle(FieldWithMetaString _metaSingle) {
            return setMetaSingleOverriddenAsFieldWithMetaString(_metaSingle);
        }

        @Override
        public Level2.Level2Builder setMetaSingleValue(String _metaSingle) {
            return setMetaSingleOverriddenAsFieldWithMetaStringValue(_metaSingle);
        }

        @RosettaAttribute("otherMetaList")
        @Accessor(AccessorType.ADDER)
        @Required
        @Multi
        @RuneAttribute("otherMetaList")
        @Override
        public Level2.Level2Builder addOtherMetaListOverriddenAsFieldWithMetaString(FieldWithMetaString _otherMetaList) {
            if (_otherMetaList != null) {
                this.otherMetaList.add(_otherMetaList.toBuilder());
            }
            return this;
        }

        @Override
        public Level2.Level2Builder addOtherMetaListOverriddenAsFieldWithMetaString(FieldWithMetaString _otherMetaList, int idx) {
            getIndex(this.otherMetaList, idx, () -> _otherMetaList.toBuilder());
            return this;
        }

        @Override
        public Level2.Level2Builder addOtherMetaListOverriddenAsFieldWithMetaStringValue(String _otherMetaList) {
            this.getOrCreateOtherMetaList(-1).setValue(_otherMetaList);
            return this;
        }

        @Override
        public Level2.Level2Builder addOtherMetaListOverriddenAsFieldWithMetaStringValue(String _otherMetaList, int idx) {
            this.getOrCreateOtherMetaList(idx).setValue(_otherMetaList);
            return this;
        }

        @Override
        public Level2.Level2Builder addOtherMetaListOverriddenAsFieldWithMetaString(List<? extends FieldWithMetaString> otherMetaLists) {
            if (otherMetaLists != null) {
                for (final FieldWithMetaString toAdd : otherMetaLists) {
                    this.otherMetaList.add(toAdd.toBuilder());
                }
            }
            return this;
        }

        @RosettaAttribute("otherMetaList")
        @Accessor(AccessorType.SETTER)
        @Required
        @Multi
        @RuneAttribute("otherMetaList")
        @Override
        public Level2.Level2Builder setOtherMetaListOverriddenAsFieldWithMetaString(List<? extends FieldWithMetaString> otherMetaLists) {
            if (otherMetaLists == null) {
                this.otherMetaList = new ArrayList<>();
            } else {
                this.otherMetaList = otherMetaLists.stream()
                    .map(_a->_a.toBuilder())
                    .collect(Collectors.toCollection(()->new ArrayList<>()));
            }
            return this;
        }

        @Override
        public Level2.Level2Builder addOtherMetaListOverriddenAsFieldWithMetaStringValue(List<? extends String> otherMetaLists) {
            if (otherMetaLists != null) {
                for (final String toAdd : otherMetaLists) {
                    this.addOtherMetaListOverriddenAsFieldWithMetaStringValue(toAdd);
                }
            }
            return this;
        }

        @Override
        public Level2.Level2Builder setOtherMetaListOverriddenAsFieldWithMetaStringValue(List<? extends String> otherMetaLists) {
            this.otherMetaList.clear();
            if (otherMetaLists != null) {
                otherMetaLists.forEach(this::addOtherMetaListOverriddenAsFieldWithMetaStringValue);
            }
            return this;
        }

        @RosettaIgnore
        @RuneIgnore
        @Override
        public Level2.Level2Builder addOtherMetaList(FieldWithMetaString _otherMetaList) {
            return addOtherMetaListOverriddenAsFieldWithMetaString(_otherMetaList);
        }

        @Override
        public Level2.Level2Builder addOtherMetaList(FieldWithMetaString _otherMetaList, int idx) {
            return addOtherMetaListOverriddenAsFieldWithMetaString(_otherMetaList, idx);
        }

        @Override
        public Level2.Level2Builder addOtherMetaListValue(String _otherMetaList) {
            return addOtherMetaListOverriddenAsFieldWithMetaStringValue(_otherMetaList);
        }

        @Override
        public Level2.Level2Builder addOtherMetaListValue(String _otherMetaList, int idx) {
            return addOtherMetaListOverriddenAsFieldWithMetaStringValue(_otherMetaList, idx);
        }

        @Override
        public Level2.Level2Builder addOtherMetaList(List<? extends FieldWithMetaString> otherMetaLists) {
            return addOtherMetaListOverriddenAsFieldWithMetaString(otherMetaLists);
        }

        @RosettaIgnore
        @RuneIgnore
        @Override
        public Level2.Level2Builder setOtherMetaList(List<? extends FieldWithMetaString> otherMetaLists) {
            return setOtherMetaListOverriddenAsFieldWithMetaString(otherMetaLists);
        }

        @Override
        public Level2.Level2Builder addOtherMetaListValue(List<? extends String> otherMetaLists) {
            return addOtherMetaListOverriddenAsFieldWithMetaStringValue(new ArrayList(otherMetaLists));
        }

        @Override
        public Level2.Level2Builder setOtherMetaListValue(List<? extends String> otherMetaLists) {
            return setOtherMetaListOverriddenAsFieldWithMetaStringValue(new ArrayList(otherMetaLists));
        }

        @RosettaAttribute("singleParent")
        @Accessor(AccessorType.SETTER)
        @RuneAttribute("singleParent")
        @Override
        public Level2.Level2Builder setSingleParent(ReferenceWithMetaChild _singleParent) {
            this.singleParent = _singleParent == null ? null : _singleParent.toBuilder();
            return this;
        }

        @Override
        public Level2.Level2Builder setSingleParentValue(Child _singleParent) {
            this.getOrCreateSingleParentOverriddenAsReferenceWithMetaChild().setValue(_singleParent);
            return this;
        }

        @RosettaIgnore
        @RuneIgnore
        @Override
        public Level2.Level2Builder setSingleParent(Parent _singleParent) {
            final ReferenceWithMetaChild ifThenElseResult;
            if (_singleParent == null) {
                ifThenElseResult = ReferenceWithMetaChild.builder().build();
            } else {
                ifThenElseResult = _singleParent instanceof Child ? ReferenceWithMetaChild.builder().setValue(Child.class.cast(_singleParent)).build() : ReferenceWithMetaChild.builder().setValue(null).build();
            }
            return setSingleParent(ifThenElseResult);
        }

        @RosettaAttribute("metaList")
        @Accessor(AccessorType.SETTER)
        @Required
        @RuneAttribute("metaList")
        @Override
        public Level2.Level2Builder setMetaList(FieldWithMetaString _metaList) {
            this.metaList = _metaList == null ? null : _metaList.toBuilder();
            return this;
        }

        @Override
        public Level2.Level2Builder setMetaListValue(String _metaList) {
            this.getOrCreateMetaListOverriddenAsSingle().setValue(_metaList);
            return this;
        }

        @RosettaIgnore
        @RuneIgnore
        @Override
        public Level2.Level2Builder addMetaList(FieldWithMetaString _metaList) {
            return setMetaList(_metaList);
        }

        @Override
        public Level2.Level2Builder addMetaList(FieldWithMetaString _metaList, int idx) {
            return setMetaList(_metaList);
        }

        @Override
        public Level2.Level2Builder addMetaListValue(String _metaList) {
            return setMetaListValue(_metaList);
        }

        @Override
        public Level2.Level2Builder addMetaListValue(String _metaList, int idx) {
            return setMetaListValue(_metaList);
        }

        @Override
        public Level2.Level2Builder addMetaList(List<? extends FieldWithMetaString> metaLists) {
            return setMetaList(MapperC.of(metaLists).get());
        }

        @RosettaIgnore
        @RuneIgnore
        @Override
        public Level2.Level2Builder setMetaList(List<? extends FieldWithMetaString> metaLists) {
            return setMetaList(MapperC.of(metaLists).get());
        }

        @Override
        public Level2.Level2Builder addMetaListValue(List<? extends String> metaLists) {
            return setMetaListValue(MapperC.of(metaLists).get());
        }

        @Override
        public Level2.Level2Builder setMetaListValue(List<? extends String> metaLists) {
            return setMetaListValue(MapperC.of(metaLists).get());
        }

        @Override
        public Level2 build() {
            return new Level2.Level2Impl(this);
        }

        @Override
        public Level2.Level2Builder toBuilder() {
            return this;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Level2.Level2Builder prune() {
            if (metaSingle!=null && !metaSingle.prune().hasData()) metaSingle = null;
            otherMetaList = otherMetaList.stream().filter(b->b!=null).<FieldWithMetaString.FieldWithMetaStringBuilder>map(b->b.prune()).filter(b->b.hasData()).collect(Collectors.toList());
            if (singleParent!=null && !singleParent.prune().hasData()) singleParent = null;
            if (metaList!=null && !metaList.prune().hasData()) metaList = null;
            return this;
        }

        @Override
        public boolean hasData() {
            if (getAttr()!=null) return true;
            if (getMetaSingle()!=null) return true;
            if (getOtherMetaList()!=null && !getOtherMetaList().isEmpty()) return true;
            if (getSingleParentOverriddenAsReferenceWithMetaChild()!=null && getSingleParentOverriddenAsReferenceWithMetaChild().hasData()) return true;
            if (getMetaListOverriddenAsSingle()!=null) return true;
            return false;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Level2.Level2Builder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
            Level2.Level2Builder o = (Level2.Level2Builder) other;

            merger.mergeRosetta(getMetaSingle(), o.getMetaSingle(), this::setMetaSingleOverriddenAsFieldWithMetaString);
            merger.mergeRosetta(getOtherMetaList(), o.getOtherMetaList(), this::getOrCreateOtherMetaList);
            merger.mergeRosetta(getSingleParentOverriddenAsReferenceWithMetaChild(), o.getSingleParentOverriddenAsReferenceWithMetaChild(), this::setSingleParent);
            merger.mergeRosetta(getMetaListOverriddenAsSingle(), o.getMetaListOverriddenAsSingle(), this::setMetaList);

            merger.mergeBasic(getAttr(), o.getAttr(), this::setAttrOverriddenAsInteger);
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;

            Level2 _that = getType().cast(o);

            if (!Objects.equals(attr, _that.getAttr())) return false;
            if (!Objects.equals(metaSingle, _that.getMetaSingle())) return false;
            if (!ListEquals.listEquals(otherMetaList, _that.getOtherMetaList())) return false;
            if (!Objects.equals(singleParent, _that.getSingleParentOverriddenAsReferenceWithMetaChild())) return false;
            if (!Objects.equals(metaList, _that.getMetaListOverriddenAsSingle())) return false;
            return true;
        }

        @Override
        public int hashCode() {
            int _result = 0;
            _result = 31 * _result + (attr != null ? attr.hashCode() : 0);
            _result = 31 * _result + (metaSingle != null ? metaSingle.hashCode() : 0);
            _result = 31 * _result + (otherMetaList != null ? otherMetaList.hashCode() : 0);
            _result = 31 * _result + (singleParent != null ? singleParent.hashCode() : 0);
            _result = 31 * _result + (metaList != null ? metaList.hashCode() : 0);
            return _result;
        }

        @Override
        public String toString() {
            return "Level2Builder {" +
                "attr=" + this.attr + ", " +
                "metaSingle=" + this.metaSingle + ", " +
                "otherMetaList=" + this.otherMetaList + ", " +
                "singleParent=" + this.singleParent + ", " +
                "metaList=" + this.metaList +
            '}';
        }
    }
}
