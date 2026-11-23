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
import test.escaping.javalang.meta.ObjectMeta;

import static java.util.Optional.ofNullable;

/**
 * @version 0.0.0
 */
@RosettaDataType(value="Object", builder=Object.ObjectBuilderImpl.class, version="0.0.0")
@RuneDataType(value="Object", model="test", builder=Object.ObjectBuilderImpl.class, version="0.0.0")
public interface Object extends RosettaModelObject {

    ObjectMeta metaData = new ObjectMeta();

    /*********************** Getter Methods  ***********************/
    String getVal();

    /*********************** Build Methods  ***********************/
    Object build();

    Object.ObjectBuilder toBuilder();

    static Object.ObjectBuilder builder() {
        return new Object.ObjectBuilderImpl();
    }

    /*********************** Utility Methods  ***********************/
    @Override
    default RosettaMetaData<? extends Object> metaData() {
        return metaData;
    }

    @Override
    @RuneAttribute("@type")
    default Class<? extends Object> getType() {
        return Object.class;
    }

    @Override
    default void process(RosettaPath path, Processor processor) {
        processor.processBasic(path.newSubPath("val"), String.class, getVal(), this);
    }


    /*********************** Builder Interface  ***********************/
    interface ObjectBuilder extends Object, RosettaModelObjectBuilder {
        Object.ObjectBuilder setVal(String val);

        @Override
        default void process(RosettaPath path, BuilderProcessor processor) {
            processor.processBasic(path.newSubPath("val"), String.class, getVal(), this);
        }


        Object.ObjectBuilder prune();
    }

    /*********************** Immutable Implementation of Object  ***********************/
    class ObjectImpl implements Object {
        private final String val;

        protected ObjectImpl(Object.ObjectBuilder builder) {
            this.val = builder.getVal();
        }

        @Override
        @RosettaAttribute("val")
        @Accessor(AccessorType.GETTER)
        @RuneAttribute("val")
        public String getVal() {
            return val;
        }

        @Override
        public Object build() {
            return this;
        }

        @Override
        public Object.ObjectBuilder toBuilder() {
            Object.ObjectBuilder builder = builder();
            setBuilderFields(builder);
            return builder;
        }

        protected void setBuilderFields(Object.ObjectBuilder builder) {
            ofNullable(getVal()).ifPresent(builder::setVal);
        }

        @Override
        public boolean equals(java.lang.Object o) {
            if (this == o) return true;
            if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;

            Object _that = getType().cast(o);

            if (!Objects.equals(val, _that.getVal())) return false;
            return true;
        }

        @Override
        public int hashCode() {
            int _result = 0;
            _result = 31 * _result + (val != null ? val.hashCode() : 0);
            return _result;
        }

        @Override
        public String toString() {
            return "Object {" +
                "val=" + this.val +
            '}';
        }
    }

    /*********************** Builder Implementation of Object  ***********************/
    class ObjectBuilderImpl implements Object.ObjectBuilder {

        protected String val;

        @Override
        @RosettaAttribute("val")
        @Accessor(AccessorType.GETTER)
        @RuneAttribute("val")
        public String getVal() {
            return val;
        }

        @RosettaAttribute("val")
        @Accessor(AccessorType.SETTER)
        @RuneAttribute("val")
        @Override
        public Object.ObjectBuilder setVal(String _val) {
            this.val = _val == null ? null : _val;
            return this;
        }

        @Override
        public Object build() {
            return new Object.ObjectImpl(this);
        }

        @Override
        public Object.ObjectBuilder toBuilder() {
            return this;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object.ObjectBuilder prune() {
            return this;
        }

        @Override
        public boolean hasData() {
            if (getVal()!=null) return true;
            return false;
        }

        @Override
        public boolean equals(java.lang.Object o) {
            if (this == o) return true;
            if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;

            Object _that = getType().cast(o);

            if (!Objects.equals(val, _that.getVal())) return false;
            return true;
        }

        @Override
        public int hashCode() {
            int _result = 0;
            _result = 31 * _result + (val != null ? val.hashCode() : 0);
            return _result;
        }

        @Override
        public String toString() {
            return "ObjectBuilder {" +
                "val=" + this.val +
            '}';
        }
    }
}
