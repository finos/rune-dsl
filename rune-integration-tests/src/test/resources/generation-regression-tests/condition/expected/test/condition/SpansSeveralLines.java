package test.condition;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.annotations.Accessor;
import com.rosetta.model.lib.annotations.AccessorType;
import com.rosetta.model.lib.annotations.Required;
import com.rosetta.model.lib.annotations.RosettaAttribute;
import com.rosetta.model.lib.annotations.RosettaDataType;
import com.rosetta.model.lib.annotations.RuneAttribute;
import com.rosetta.model.lib.annotations.RuneDataType;
import com.rosetta.model.lib.meta.RosettaMetaData;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.process.BuilderProcessor;
import com.rosetta.model.lib.process.Processor;
import java.util.Objects;
import test.condition.meta.SpansSeveralLinesMeta;

import static java.util.Optional.ofNullable;

/**
 * @version 0.0.0
 */
@RosettaDataType(value="SpansSeveralLines", builder=SpansSeveralLines.SpansSeveralLinesBuilderImpl.class, version="0.0.0")
@RuneDataType(value="SpansSeveralLines", model="test", builder=SpansSeveralLines.SpansSeveralLinesBuilderImpl.class, version="0.0.0")
public interface SpansSeveralLines extends RosettaModelObject {

    SpansSeveralLinesMeta metaData = new SpansSeveralLinesMeta();

    /*********************** Getter Methods  ***********************/
    String getVal();

    /*********************** Build Methods  ***********************/
    SpansSeveralLines build();

    SpansSeveralLines.SpansSeveralLinesBuilder toBuilder();

    static SpansSeveralLines.SpansSeveralLinesBuilder builder() {
        return new SpansSeveralLines.SpansSeveralLinesBuilderImpl();
    }

    /*********************** Utility Methods  ***********************/
    @Override
    default RosettaMetaData<? extends SpansSeveralLines> metaData() {
        return metaData;
    }

    @Override
    @RuneAttribute("@type")
    default Class<? extends SpansSeveralLines> getType() {
        return SpansSeveralLines.class;
    }

    @Override
    default void process(RosettaPath path, Processor processor) {
        processor.processBasic(path.newSubPath("val"), String.class, getVal(), this);
    }


    /*********************** Builder Interface  ***********************/
    interface SpansSeveralLinesBuilder extends SpansSeveralLines, RosettaModelObjectBuilder {
        SpansSeveralLines.SpansSeveralLinesBuilder setVal(String val);

        @Override
        default void process(RosettaPath path, BuilderProcessor processor) {
            processor.processBasic(path.newSubPath("val"), String.class, getVal(), this);
        }


        SpansSeveralLines.SpansSeveralLinesBuilder prune();
    }

    /*********************** Immutable Implementation of SpansSeveralLines  ***********************/
    class SpansSeveralLinesImpl implements SpansSeveralLines {
        private final String val;

        protected SpansSeveralLinesImpl(SpansSeveralLines.SpansSeveralLinesBuilder builder) {
            this.val = builder.getVal();
        }

        @Override
        @RosettaAttribute("val")
        @Accessor(AccessorType.GETTER)
        @Required
        @RuneAttribute("val")
        public String getVal() {
            return val;
        }

        @Override
        public SpansSeveralLines build() {
            return this;
        }

        @Override
        public SpansSeveralLines.SpansSeveralLinesBuilder toBuilder() {
            SpansSeveralLines.SpansSeveralLinesBuilder builder = builder();
            setBuilderFields(builder);
            return builder;
        }

        protected void setBuilderFields(SpansSeveralLines.SpansSeveralLinesBuilder builder) {
            ofNullable(getVal()).ifPresent(builder::setVal);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;

            SpansSeveralLines _that = getType().cast(o);

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
            return "SpansSeveralLines {" +
                "val=" + this.val +
            '}';
        }
    }

    /*********************** Builder Implementation of SpansSeveralLines  ***********************/
    class SpansSeveralLinesBuilderImpl implements SpansSeveralLines.SpansSeveralLinesBuilder {

        protected String val;

        @Override
        @RosettaAttribute("val")
        @Accessor(AccessorType.GETTER)
        @Required
        @RuneAttribute("val")
        public String getVal() {
            return val;
        }

        @RosettaAttribute("val")
        @Accessor(AccessorType.SETTER)
        @Required
        @RuneAttribute("val")
        @Override
        public SpansSeveralLines.SpansSeveralLinesBuilder setVal(String _val) {
            this.val = _val == null ? null : _val;
            return this;
        }

        @Override
        public SpansSeveralLines build() {
            return new SpansSeveralLines.SpansSeveralLinesImpl(this);
        }

        @Override
        public SpansSeveralLines.SpansSeveralLinesBuilder toBuilder() {
            return this;
        }

        @SuppressWarnings("unchecked")
        @Override
        public SpansSeveralLines.SpansSeveralLinesBuilder prune() {
            return this;
        }

        @Override
        public boolean hasData() {
            if (getVal()!=null) return true;
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;

            SpansSeveralLines _that = getType().cast(o);

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
            return "SpansSeveralLinesBuilder {" +
                "val=" + this.val +
            '}';
        }
    }
}
