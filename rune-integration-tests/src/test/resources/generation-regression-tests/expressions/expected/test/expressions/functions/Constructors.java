package test.expressions.functions;

import com.google.inject.ImplementedBy;
import com.rosetta.model.lib.functions.ModelObjectValidator;
import com.rosetta.model.lib.functions.RosettaFunction;
import com.rosetta.model.lib.mapper.MapperC;
import com.rosetta.model.lib.mapper.MapperS;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import test.expressions.Foo;
import test.expressions.metafields.ReferenceWithMetaFoo;


@ImplementedBy(Constructors.ConstructorsDefault.class)
public abstract class Constructors implements RosettaFunction {

    @Inject protected ModelObjectValidator objectValidator;

    /**
     * @param foo
     * @param foos
     * @return result
     */
    public Foo evaluate(Foo foo, List<? extends Foo> foos) {
        Foo.FooBuilder resultBuilder = doEvaluate(foo, foos);

        final Foo result;
        if (resultBuilder == null) {
            result = null;
        } else {
            result = resultBuilder.build();
            objectValidator.validate(Foo.class, result);
        }
        return result;
    }

    protected abstract Foo.FooBuilder doEvaluate(Foo foo, List<? extends Foo> foos);

    protected abstract Foo.FooBuilder fromOutput(Foo.FooBuilder result, Foo foo, List<? extends Foo> foos);

    public static class ConstructorsDefault extends Constructors {
        @Override
        protected Foo.FooBuilder doEvaluate(Foo foo, List<? extends Foo> foos) {
            if (foos == null) {
                foos = Collections.emptyList();
            }
            Foo.FooBuilder result = Foo.builder();
            return assignOutput(result, foo, foos);
        }

        protected Foo.FooBuilder assignOutput(Foo.FooBuilder result, Foo foo, List<? extends Foo> foos) {
            result = toBuilder(Foo.builder()
                .setI(MapperS.of(foo).<Integer>map("getI", __foo -> __foo.getI()).get())
                .setS(MapperC.<String>of(MapperS.of("a")).getMulti())
                .setCodeValue("x")
                .setRef(ReferenceWithMetaFoo.builder()
                    .setGlobalReference(Optional.ofNullable(foo)
                        .map(r -> r.getMeta())
                        .map(m -> m.getGlobalKey())
                        .orElse(null))
                    .setExternalReference(Optional.ofNullable(foo)
                        .map(r -> r.getMeta())
                        .map(m -> m.getExternalKey())
                        .orElse(null))
                    .build())
                .setRefs(MapperC.<Foo>of(foos)
                    .getItems()
                    .map(item -> ReferenceWithMetaFoo.builder()
                        .setExternalReference(item.getMappedObject().getMeta().getExternalKey())
                        .setGlobalReference(item.getMappedObject().getMeta().getGlobalKey())
                        .build())
                    .collect(Collectors.toList()))
                .build(), () -> Foo.builder());
            final Foo resultRef = fromOutput(result.toBuilder(), foo, foos).build();
            result
                .setRef(ReferenceWithMetaFoo.builder()
                .setGlobalReference(Optional.ofNullable(resultRef)
                    .map(r -> r.getMeta())
                    .map(m -> m.getGlobalKey())
                    .orElse(null))
                .setExternalReference(Optional.ofNullable(resultRef)
                    .map(r -> r.getMeta())
                    .map(m -> m.getExternalKey())
                    .orElse(null))
                .build());
            return Optional.ofNullable(result)
                .map(o -> o.prune())
                .orElse(null);
        }

        @Override
        protected Foo.FooBuilder fromOutput(Foo.FooBuilder result, Foo foo, List<? extends Foo> foos) {
            return toBuilder(result);
        }
    }
}
