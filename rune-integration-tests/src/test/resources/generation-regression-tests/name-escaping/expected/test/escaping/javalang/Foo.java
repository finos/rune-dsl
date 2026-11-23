package test.escaping.javalang;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.annotations.Accessor;
import com.rosetta.model.lib.annotations.AccessorType;
import com.rosetta.model.lib.annotations.RosettaAttribute;
import com.rosetta.model.lib.annotations.RosettaDataType;
import com.rosetta.model.lib.annotations.RuneAttribute;
import com.rosetta.model.lib.annotations.RuneDataType;
import com.rosetta.model.lib.meta.RosettaMetaData;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.process.BuilderProcessor;
import com.rosetta.model.lib.process.Processor;
import java.util.Objects;
import test.escaping.javalang.meta.FooMeta;

import static java.util.Optional.ofNullable;

/**
 * @version 0.0.0
 */
@RosettaDataType(value="Foo", builder=Foo.FooBuilderImpl.class, version="0.0.0")
@RuneDataType(value="Foo", model="test", builder=Foo.FooBuilderImpl.class, version="0.0.0")
public interface Foo extends RosettaModelObject {

    FooMeta metaData = new FooMeta();

    /*********************** Getter Methods  ***********************/
    test.escaping.javalang.Object getObj();

    /*********************** Build Methods  ***********************/
    Foo build();

    Foo.FooBuilder toBuilder();

    static Foo.FooBuilder builder() {
        return new Foo.FooBuilderImpl();
    }

    /*********************** Utility Methods  ***********************/
    @Override
    default RosettaMetaData<? extends Foo> metaData() {
        return metaData;
    }

    @Override
    @RuneAttribute("@type")
    default Class<? extends Foo> getType() {
        return Foo.class;
    }

    @Override
    default void process(RosettaPath path, Processor processor) {
        processRosetta(path.newSubPath("obj"), processor, test.escaping.javalang.Object.class, getObj());
    }


    /*********************** Builder Interface  ***********************/
    interface FooBuilder extends Foo, RosettaModelObjectBuilder {
        Object.ObjectBuilder getOrCreateObj();
        @Override
        Object.ObjectBuilder getObj();
        Foo.FooBuilder setObj(test.escaping.javalang.Object obj);

        @Override
        default void process(RosettaPath path, BuilderProcessor processor) {
            processRosetta(path.newSubPath("obj"), processor, Object.ObjectBuilder.class, getObj());
        }


        Foo.FooBuilder prune();
    }

    /*********************** Immutable Implementation of Foo  ***********************/
    class FooImpl implements Foo {
        private final test.escaping.javalang.Object obj;

        protected FooImpl(Foo.FooBuilder builder) {
            this.obj = ofNullable(builder.getObj()).map(f->f.build()).orElse(null);
        }

        @Override
        @RosettaAttribute("obj")
        @Accessor(AccessorType.GETTER)
        @RuneAttribute("obj")
        public test.escaping.javalang.Object getObj() {
            return obj;
        }

        @Override
        public Foo build() {
            return this;
        }

        @Override
        public Foo.FooBuilder toBuilder() {
            Foo.FooBuilder builder = builder();
            setBuilderFields(builder);
            return builder;
        }

        protected void setBuilderFields(Foo.FooBuilder builder) {
            ofNullable(getObj()).ifPresent(builder::setObj);
        }

        @Override
        public boolean equals(java.lang.Object o) {
            if (this == o) return true;
            if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;

            Foo _that = getType().cast(o);

            if (!Objects.equals(obj, _that.getObj())) return false;
            return true;
        }

        @Override
        public int hashCode() {
            int _result = 0;
            _result = 31 * _result + (obj != null ? obj.hashCode() : 0);
            return _result;
        }

        @Override
        public String toString() {
            return "Foo {" +
                "obj=" + this.obj +
            '}';
        }
    }

    /*********************** Builder Implementation of Foo  ***********************/
    class FooBuilderImpl implements Foo.FooBuilder {

        protected Object.ObjectBuilder obj;

        @Override
        @RosettaAttribute("obj")
        @Accessor(AccessorType.GETTER)
        @RuneAttribute("obj")
        public Object.ObjectBuilder getObj() {
            return obj;
        }

        @Override
        public Object.ObjectBuilder getOrCreateObj() {
            Object.ObjectBuilder result;
            if (obj!=null) {
                result = obj;
            }
            else {
                result = obj = test.escaping.javalang.Object.builder();
            }

            return result;
        }

        @RosettaAttribute("obj")
        @Accessor(AccessorType.SETTER)
        @RuneAttribute("obj")
        @Override
        public Foo.FooBuilder setObj(test.escaping.javalang.Object _obj) {
            this.obj = _obj == null ? null : _obj.toBuilder();
            return this;
        }

        @Override
        public Foo build() {
            return new Foo.FooImpl(this);
        }

        @Override
        public Foo.FooBuilder toBuilder() {
            return this;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Foo.FooBuilder prune() {
            if (obj!=null && !obj.prune().hasData()) obj = null;
            return this;
        }

        @Override
        public boolean hasData() {
            if (getObj()!=null && getObj().hasData()) return true;
            return false;
        }

        @Override
        public boolean equals(java.lang.Object o) {
            if (this == o) return true;
            if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;

            Foo _that = getType().cast(o);

            if (!Objects.equals(obj, _that.getObj())) return false;
            return true;
        }

        @Override
        public int hashCode() {
            int _result = 0;
            _result = 31 * _result + (obj != null ? obj.hashCode() : 0);
            return _result;
        }

        @Override
        public String toString() {
            return "FooBuilder {" +
                "obj=" + this.obj +
            '}';
        }
    }
}
