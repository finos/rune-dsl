package test.expressions.functions;

import com.google.inject.ImplementedBy;
import com.rosetta.model.lib.functions.ModelObjectValidator;
import com.rosetta.model.lib.functions.RosettaFunction;
import com.rosetta.model.lib.mapper.MapperC;
import com.rosetta.model.lib.mapper.MapperS;
import com.rosetta.model.metafields.FieldWithMetaString;
import com.rosetta.model.metafields.MetaFields;
import java.util.Collections;
import java.util.Optional;
import javax.inject.Inject;
import test.expressions.Foo;
import test.expressions.metafields.FieldWithMetaFoo;
import test.expressions.metafields.ReferenceWithMetaFoo;


@ImplementedBy(WithMeta.WithMetaDefault.class)
public abstract class WithMeta implements RosettaFunction {

    @Inject protected ModelObjectValidator objectValidator;

    /**
     * @param code
     * @param foo
     * @param metaFoo
     * @return result
     */
    public Foo evaluate(FieldWithMetaString code, Foo foo, FieldWithMetaFoo metaFoo) {
        Foo.FooBuilder resultBuilder = doEvaluate(code, foo, metaFoo);

        final Foo result;
        if (resultBuilder == null) {
            result = null;
        } else {
            result = resultBuilder.build();
            objectValidator.validate(Foo.class, result);
        }
        return result;
    }

    protected abstract Foo.FooBuilder doEvaluate(FieldWithMetaString code, Foo foo, FieldWithMetaFoo metaFoo);

    protected abstract MapperC<String> withMetas(FieldWithMetaString code, Foo foo, FieldWithMetaFoo metaFoo);

    protected abstract MapperC<? extends Foo> typeMetas(FieldWithMetaString code, Foo foo, FieldWithMetaFoo metaFoo);

    protected abstract MapperS<? extends ReferenceWithMetaFoo> referenceMeta(FieldWithMetaString code, Foo foo, FieldWithMetaFoo metaFoo);

    public static class WithMetaDefault extends WithMeta {
        @Override
        protected Foo.FooBuilder doEvaluate(FieldWithMetaString code, Foo foo, FieldWithMetaFoo metaFoo) {
            Foo.FooBuilder result = Foo.builder();
            return assignOutput(result, code, foo, metaFoo);
        }

        protected Foo.FooBuilder assignOutput(Foo.FooBuilder result, FieldWithMetaString code, Foo foo, FieldWithMetaFoo metaFoo) {
            result = toBuilder(Foo.builder()
                .setS((code == null || code.getValue() == null ? Collections.<String>emptyList() : Collections.singletonList(code.getValue())))
                .build());
            return Optional.ofNullable(result)
                .map(o -> o.prune())
                .orElse(null);
        }

        @Override
        protected MapperC<String> withMetas(FieldWithMetaString code, Foo foo, FieldWithMetaFoo metaFoo) {
            final String withMetaArgument0 = "a";
            final FieldWithMetaString.FieldWithMetaStringBuilder withMetaArgument1 = code == null ? null : code.toBuilder();
            withMetaArgument1.getOrCreateMeta().setScheme("t");
            final FieldWithMetaString fieldWithMetaString = FieldWithMetaString.builder().setValue(withMetaArgument0).setMeta(MetaFields.builder().setScheme("s"));
            return MapperC.<String>of((fieldWithMetaString == null ? MapperS.<String>ofNull() : MapperS.of(fieldWithMetaString.getValue())), (withMetaArgument1 == null ? MapperS.<String>ofNull() : MapperS.of(withMetaArgument1.getValue())));
        }

        @Override
        protected MapperC<? extends Foo> typeMetas(FieldWithMetaString code, Foo foo, FieldWithMetaFoo metaFoo) {
            final Foo.FooBuilder withMetaArgument0 = foo == null ? null : foo.toBuilder();
            withMetaArgument0.getOrCreateMeta().setExternalKey("k");
            final FieldWithMetaFoo.FieldWithMetaFooBuilder withMetaArgument1 = metaFoo == null ? null : metaFoo.toBuilder();
            withMetaArgument1.getOrCreateValue().getOrCreateMeta().setExternalKey("k");
            final Foo.FooBuilder withMetaArgument2 = foo == null ? null : foo.toBuilder();
            withMetaArgument2.getOrCreateMeta().setExternalKey("k");
            final FieldWithMetaFoo fieldWithMetaFoo = FieldWithMetaFoo.builder().setValue(withMetaArgument2).setMeta(MetaFields.builder().setScheme("s"));
            final FieldWithMetaFoo.FieldWithMetaFooBuilder withMetaArgument3 = metaFoo == null ? null : metaFoo.toBuilder();
            withMetaArgument3.getOrCreateValue().getOrCreateMeta().setExternalKey("k");
            withMetaArgument3.getOrCreateMeta().setScheme("s");
            final Foo.FooBuilder withMetaArgument4 = foo == null ? null : foo.toBuilder();
            return MapperC.<Foo>of(MapperS.of(withMetaArgument0), (withMetaArgument1 == null ? MapperS.<Foo>ofNull() : MapperS.of(withMetaArgument1.getValue())), (fieldWithMetaFoo == null ? MapperS.<Foo>ofNull() : MapperS.of(fieldWithMetaFoo.getValue())), (withMetaArgument3 == null ? MapperS.<Foo>ofNull() : MapperS.of(withMetaArgument3.getValue())), MapperS.of(withMetaArgument4));
        }

        @Override
        protected MapperS<? extends ReferenceWithMetaFoo> referenceMeta(FieldWithMetaString code, Foo foo, FieldWithMetaFoo metaFoo) {
            return MapperS.of(ReferenceWithMetaFoo.builder().setValue(foo == null ? null : foo.toBuilder()).setExternalReference("r").build());
        }
    }
}
