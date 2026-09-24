package test.pojo;

import com.google.common.collect.ImmutableList;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.annotations.Accessor;
import com.rosetta.model.lib.annotations.AccessorType;
import com.rosetta.model.lib.annotations.Multi;
import com.rosetta.model.lib.annotations.RosettaAttribute;
import com.rosetta.model.lib.annotations.RosettaDataType;
import com.rosetta.model.lib.annotations.RuneAttribute;
import com.rosetta.model.lib.annotations.RuneDataType;
import com.rosetta.model.lib.meta.RosettaMetaData;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.process.BuilderProcessor;
import com.rosetta.model.lib.process.Processor;
import com.rosetta.model.metafields.FieldWithMetaString;
import com.rosetta.util.ListEquals;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import test.pojo.meta.Level1Meta;

import static java.util.Optional.ofNullable;

/**
 * @version 0.0.0
 */
@RosettaDataType(value="Level1", builder=Level1.Level1BuilderImpl.class, version="0.0.0")
@RuneDataType(value="Level1", model="test", builder=Level1.Level1BuilderImpl.class, version="0.0.0")
public interface Level1 extends RosettaModelObject {

    Level1Meta metaData = new Level1Meta();

    /*********************** Getter Methods  ***********************/
    Integer getAttr();
    FieldWithMetaString getMetaSingle();
    List<? extends FieldWithMetaString> getOtherMetaList();
    Parent getSingleParent();
    List<? extends FieldWithMetaString> getMetaList();

    /*********************** Build Methods  ***********************/
    Level1 build();

    Level1.Level1Builder toBuilder();

    static Level1.Level1Builder builder() {
        return new Level1.Level1BuilderImpl();
    }

    /*********************** Utility Methods  ***********************/
    @Override
    default RosettaMetaData<? extends Level1> metaData() {
        return metaData;
    }

    @Override
    @RuneAttribute("@type")
    default Class<? extends Level1> getType() {
        return Level1.class;
    }

    @Override
    default void process(RosettaPath path, Processor processor) {
        processor.processBasic(path.newSubPath("attr"), Integer.class, getAttr(), this);
        processRosetta(path.newSubPath("metaSingle"), processor, FieldWithMetaString.class, getMetaSingle());
        processRosetta(path.newSubPath("otherMetaList"), processor, FieldWithMetaString.class, getOtherMetaList());
        processRosetta(path.newSubPath("singleParent"), processor, Parent.class, getSingleParent());
        processRosetta(path.newSubPath("metaList"), processor, FieldWithMetaString.class, getMetaList());
    }


    /*********************** Builder Interface  ***********************/
    interface Level1Builder extends Level1, RosettaModelObjectBuilder {
        FieldWithMetaString.FieldWithMetaStringBuilder getOrCreateMetaSingle();
        @Override
        FieldWithMetaString.FieldWithMetaStringBuilder getMetaSingle();
        FieldWithMetaString.FieldWithMetaStringBuilder getOrCreateOtherMetaList(int index);
        @Override
        List<? extends FieldWithMetaString.FieldWithMetaStringBuilder> getOtherMetaList();
        Parent.ParentBuilder getOrCreateSingleParent();
        @Override
        Parent.ParentBuilder getSingleParent();
        FieldWithMetaString.FieldWithMetaStringBuilder getOrCreateMetaList(int index);
        @Override
        List<? extends FieldWithMetaString.FieldWithMetaStringBuilder> getMetaList();
        Level1.Level1Builder setAttr(Integer attr);
        Level1.Level1Builder setMetaSingle(FieldWithMetaString metaSingle);
        Level1.Level1Builder setMetaSingleValue(String metaSingle);
        Level1.Level1Builder addOtherMetaList(FieldWithMetaString otherMetaList);
        Level1.Level1Builder addOtherMetaList(FieldWithMetaString otherMetaList, int idx);
        Level1.Level1Builder addOtherMetaListValue(String otherMetaList);
        Level1.Level1Builder addOtherMetaListValue(String otherMetaList, int idx);
        Level1.Level1Builder addOtherMetaList(List<? extends FieldWithMetaString> otherMetaList);
        Level1.Level1Builder setOtherMetaList(List<? extends FieldWithMetaString> otherMetaList);
        Level1.Level1Builder addOtherMetaListValue(List<? extends String> otherMetaList);
        Level1.Level1Builder setOtherMetaListValue(List<? extends String> otherMetaList);
        Level1.Level1Builder setSingleParent(Parent singleParent);
        Level1.Level1Builder addMetaList(FieldWithMetaString metaList);
        Level1.Level1Builder addMetaList(FieldWithMetaString metaList, int idx);
        Level1.Level1Builder addMetaListValue(String metaList);
        Level1.Level1Builder addMetaListValue(String metaList, int idx);
        Level1.Level1Builder addMetaList(List<? extends FieldWithMetaString> metaList);
        Level1.Level1Builder setMetaList(List<? extends FieldWithMetaString> metaList);
        Level1.Level1Builder addMetaListValue(List<? extends String> metaList);
        Level1.Level1Builder setMetaListValue(List<? extends String> metaList);

        @Override
        default void process(RosettaPath path, BuilderProcessor processor) {
            processor.processBasic(path.newSubPath("attr"), Integer.class, getAttr(), this);
            processRosetta(path.newSubPath("metaSingle"), processor, FieldWithMetaString.FieldWithMetaStringBuilder.class, getMetaSingle());
            processRosetta(path.newSubPath("otherMetaList"), processor, FieldWithMetaString.FieldWithMetaStringBuilder.class, getOtherMetaList());
            processRosetta(path.newSubPath("singleParent"), processor, Parent.ParentBuilder.class, getSingleParent());
            processRosetta(path.newSubPath("metaList"), processor, FieldWithMetaString.FieldWithMetaStringBuilder.class, getMetaList());
        }


        Level1.Level1Builder prune();
    }

    /*********************** Immutable Implementation of Level1  ***********************/
    class Level1Impl implements Level1 {
        private final Integer attr;
        private final FieldWithMetaString metaSingle;
        private final List<? extends FieldWithMetaString> otherMetaList;
        private final Parent singleParent;
        private final List<? extends FieldWithMetaString> metaList;

        protected Level1Impl(Level1.Level1Builder builder) {
            this.attr = builder.getAttr();
            this.metaSingle = ofNullable(builder.getMetaSingle()).map(f->f.build()).orElse(null);
            this.otherMetaList = ofNullable(builder.getOtherMetaList()).filter(_l->!_l.isEmpty()).map(list -> list.stream().filter(Objects::nonNull).map(f->f.build()).filter(Objects::nonNull).collect(ImmutableList.toImmutableList())).orElse(null);
            this.singleParent = ofNullable(builder.getSingleParent()).map(f->f.build()).orElse(null);
            this.metaList = ofNullable(builder.getMetaList()).filter(_l->!_l.isEmpty()).map(list -> list.stream().filter(Objects::nonNull).map(f->f.build()).filter(Objects::nonNull).collect(ImmutableList.toImmutableList())).orElse(null);
        }

        @Override
        @RosettaAttribute("attr")
        @Accessor(AccessorType.GETTER)
        @RuneAttribute("attr")
        public Integer getAttr() {
            return attr;
        }

        @Override
        @RosettaAttribute("metaSingle")
        @Accessor(AccessorType.GETTER)
        @RuneAttribute("metaSingle")
        public FieldWithMetaString getMetaSingle() {
            return metaSingle;
        }

        @Override
        @RosettaAttribute("otherMetaList")
        @Accessor(AccessorType.GETTER)
        @Multi
        @RuneAttribute("otherMetaList")
        public List<? extends FieldWithMetaString> getOtherMetaList() {
            return otherMetaList;
        }

        @Override
        @RosettaAttribute("singleParent")
        @Accessor(AccessorType.GETTER)
        @RuneAttribute("singleParent")
        public Parent getSingleParent() {
            return singleParent;
        }

        @Override
        @RosettaAttribute("metaList")
        @Accessor(AccessorType.GETTER)
        @Multi
        @RuneAttribute("metaList")
        public List<? extends FieldWithMetaString> getMetaList() {
            return metaList;
        }

        @Override
        public Level1 build() {
            return this;
        }

        @Override
        public Level1.Level1Builder toBuilder() {
            Level1.Level1Builder builder = builder();
            setBuilderFields(builder);
            return builder;
        }

        protected void setBuilderFields(Level1.Level1Builder builder) {
            ofNullable(getAttr()).ifPresent(builder::setAttr);
            ofNullable(getMetaSingle()).ifPresent(builder::setMetaSingle);
            ofNullable(getOtherMetaList()).ifPresent(builder::setOtherMetaList);
            ofNullable(getSingleParent()).ifPresent(builder::setSingleParent);
            ofNullable(getMetaList()).ifPresent(builder::setMetaList);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;

            Level1 _that = getType().cast(o);

            if (!Objects.equals(attr, _that.getAttr())) return false;
            if (!Objects.equals(metaSingle, _that.getMetaSingle())) return false;
            if (!ListEquals.listEquals(otherMetaList, _that.getOtherMetaList())) return false;
            if (!Objects.equals(singleParent, _that.getSingleParent())) return false;
            if (!ListEquals.listEquals(metaList, _that.getMetaList())) return false;
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
            return "Level1 {" +
                "attr=" + this.attr + ", " +
                "metaSingle=" + this.metaSingle + ", " +
                "otherMetaList=" + this.otherMetaList + ", " +
                "singleParent=" + this.singleParent + ", " +
                "metaList=" + this.metaList +
            '}';
        }
    }

    /*********************** Builder Implementation of Level1  ***********************/
    class Level1BuilderImpl implements Level1.Level1Builder {

        protected Integer attr;
        protected FieldWithMetaString.FieldWithMetaStringBuilder metaSingle;
        protected List<FieldWithMetaString.FieldWithMetaStringBuilder> otherMetaList = new ArrayList<>();
        protected Parent.ParentBuilder singleParent;
        protected List<FieldWithMetaString.FieldWithMetaStringBuilder> metaList = new ArrayList<>();

        @Override
        @RosettaAttribute("attr")
        @Accessor(AccessorType.GETTER)
        @RuneAttribute("attr")
        public Integer getAttr() {
            return attr;
        }

        @Override
        @RosettaAttribute("metaSingle")
        @Accessor(AccessorType.GETTER)
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
        public Parent.ParentBuilder getSingleParent() {
            return singleParent;
        }

        @Override
        public Parent.ParentBuilder getOrCreateSingleParent() {
            Parent.ParentBuilder result;
            if (singleParent!=null) {
                result = singleParent;
            }
            else {
                result = singleParent = Parent.builder();
            }

            return result;
        }

        @Override
        @RosettaAttribute("metaList")
        @Accessor(AccessorType.GETTER)
        @Multi
        @RuneAttribute("metaList")
        public List<? extends FieldWithMetaString.FieldWithMetaStringBuilder> getMetaList() {
            return metaList;
        }

        @Override
        public FieldWithMetaString.FieldWithMetaStringBuilder getOrCreateMetaList(int index) {
            if (metaList==null) {
                this.metaList = new ArrayList<>();
            }
            return getIndex(metaList, index, () -> {
                        FieldWithMetaString.FieldWithMetaStringBuilder newMetaList = FieldWithMetaString.builder();
                        return newMetaList;
                    });
        }

        @RosettaAttribute("attr")
        @Accessor(AccessorType.SETTER)
        @RuneAttribute("attr")
        @Override
        public Level1.Level1Builder setAttr(Integer _attr) {
            this.attr = _attr == null ? null : _attr;
            return this;
        }

        @RosettaAttribute("metaSingle")
        @Accessor(AccessorType.SETTER)
        @RuneAttribute("metaSingle")
        @Override
        public Level1.Level1Builder setMetaSingle(FieldWithMetaString _metaSingle) {
            this.metaSingle = _metaSingle == null ? null : _metaSingle.toBuilder();
            return this;
        }

        @Override
        public Level1.Level1Builder setMetaSingleValue(String _metaSingle) {
            this.getOrCreateMetaSingle().setValue(_metaSingle);
            return this;
        }

        @RosettaAttribute("otherMetaList")
        @Accessor(AccessorType.ADDER)
        @Multi
        @RuneAttribute("otherMetaList")
        @Override
        public Level1.Level1Builder addOtherMetaList(FieldWithMetaString _otherMetaList) {
            if (_otherMetaList != null) {
                this.otherMetaList.add(_otherMetaList.toBuilder());
            }
            return this;
        }

        @Override
        public Level1.Level1Builder addOtherMetaList(FieldWithMetaString _otherMetaList, int idx) {
            getIndex(this.otherMetaList, idx, () -> _otherMetaList.toBuilder());
            return this;
        }

        @Override
        public Level1.Level1Builder addOtherMetaListValue(String _otherMetaList) {
            this.getOrCreateOtherMetaList(-1).setValue(_otherMetaList);
            return this;
        }

        @Override
        public Level1.Level1Builder addOtherMetaListValue(String _otherMetaList, int idx) {
            this.getOrCreateOtherMetaList(idx).setValue(_otherMetaList);
            return this;
        }

        @Override
        public Level1.Level1Builder addOtherMetaList(List<? extends FieldWithMetaString> otherMetaLists) {
            if (otherMetaLists != null) {
                for (final FieldWithMetaString toAdd : otherMetaLists) {
                    this.otherMetaList.add(toAdd.toBuilder());
                }
            }
            return this;
        }

        @RosettaAttribute("otherMetaList")
        @Accessor(AccessorType.SETTER)
        @Multi
        @RuneAttribute("otherMetaList")
        @Override
        public Level1.Level1Builder setOtherMetaList(List<? extends FieldWithMetaString> otherMetaLists) {
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
        public Level1.Level1Builder addOtherMetaListValue(List<? extends String> otherMetaLists) {
            if (otherMetaLists != null) {
                for (final String toAdd : otherMetaLists) {
                    this.addOtherMetaListValue(toAdd);
                }
            }
            return this;
        }

        @Override
        public Level1.Level1Builder setOtherMetaListValue(List<? extends String> otherMetaLists) {
            this.otherMetaList.clear();
            if (otherMetaLists != null) {
                otherMetaLists.forEach(this::addOtherMetaListValue);
            }
            return this;
        }

        @RosettaAttribute("singleParent")
        @Accessor(AccessorType.SETTER)
        @RuneAttribute("singleParent")
        @Override
        public Level1.Level1Builder setSingleParent(Parent _singleParent) {
            this.singleParent = _singleParent == null ? null : _singleParent.toBuilder();
            return this;
        }

        @RosettaAttribute("metaList")
        @Accessor(AccessorType.ADDER)
        @Multi
        @RuneAttribute("metaList")
        @Override
        public Level1.Level1Builder addMetaList(FieldWithMetaString _metaList) {
            if (_metaList != null) {
                this.metaList.add(_metaList.toBuilder());
            }
            return this;
        }

        @Override
        public Level1.Level1Builder addMetaList(FieldWithMetaString _metaList, int idx) {
            getIndex(this.metaList, idx, () -> _metaList.toBuilder());
            return this;
        }

        @Override
        public Level1.Level1Builder addMetaListValue(String _metaList) {
            this.getOrCreateMetaList(-1).setValue(_metaList);
            return this;
        }

        @Override
        public Level1.Level1Builder addMetaListValue(String _metaList, int idx) {
            this.getOrCreateMetaList(idx).setValue(_metaList);
            return this;
        }

        @Override
        public Level1.Level1Builder addMetaList(List<? extends FieldWithMetaString> metaLists) {
            if (metaLists != null) {
                for (final FieldWithMetaString toAdd : metaLists) {
                    this.metaList.add(toAdd.toBuilder());
                }
            }
            return this;
        }

        @RosettaAttribute("metaList")
        @Accessor(AccessorType.SETTER)
        @Multi
        @RuneAttribute("metaList")
        @Override
        public Level1.Level1Builder setMetaList(List<? extends FieldWithMetaString> metaLists) {
            if (metaLists == null) {
                this.metaList = new ArrayList<>();
            } else {
                this.metaList = metaLists.stream()
                    .map(_a->_a.toBuilder())
                    .collect(Collectors.toCollection(()->new ArrayList<>()));
            }
            return this;
        }

        @Override
        public Level1.Level1Builder addMetaListValue(List<? extends String> metaLists) {
            if (metaLists != null) {
                for (final String toAdd : metaLists) {
                    this.addMetaListValue(toAdd);
                }
            }
            return this;
        }

        @Override
        public Level1.Level1Builder setMetaListValue(List<? extends String> metaLists) {
            this.metaList.clear();
            if (metaLists != null) {
                metaLists.forEach(this::addMetaListValue);
            }
            return this;
        }

        @Override
        public Level1 build() {
            return new Level1.Level1Impl(this);
        }

        @Override
        public Level1.Level1Builder toBuilder() {
            return this;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Level1.Level1Builder prune() {
            if (metaSingle!=null && !metaSingle.prune().hasData()) metaSingle = null;
            otherMetaList = otherMetaList.stream().filter(b->b!=null).<FieldWithMetaString.FieldWithMetaStringBuilder>map(b->b.prune()).filter(b->b.hasData()).collect(Collectors.toList());
            if (singleParent!=null && !singleParent.prune().hasData()) singleParent = null;
            metaList = metaList.stream().filter(b->b!=null).<FieldWithMetaString.FieldWithMetaStringBuilder>map(b->b.prune()).filter(b->b.hasData()).collect(Collectors.toList());
            return this;
        }

        @Override
        public boolean hasData() {
            if (getAttr()!=null) return true;
            if (getMetaSingle()!=null) return true;
            if (getOtherMetaList()!=null && !getOtherMetaList().isEmpty()) return true;
            if (getSingleParent()!=null && getSingleParent().hasData()) return true;
            if (getMetaList()!=null && !getMetaList().isEmpty()) return true;
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;

            Level1 _that = getType().cast(o);

            if (!Objects.equals(attr, _that.getAttr())) return false;
            if (!Objects.equals(metaSingle, _that.getMetaSingle())) return false;
            if (!ListEquals.listEquals(otherMetaList, _that.getOtherMetaList())) return false;
            if (!Objects.equals(singleParent, _that.getSingleParent())) return false;
            if (!ListEquals.listEquals(metaList, _that.getMetaList())) return false;
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
            return "Level1Builder {" +
                "attr=" + this.attr + ", " +
                "metaSingle=" + this.metaSingle + ", " +
                "otherMetaList=" + this.otherMetaList + ", " +
                "singleParent=" + this.singleParent + ", " +
                "metaList=" + this.metaList +
            '}';
        }
    }
}
