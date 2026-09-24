package test.expressions.functions;

import com.google.inject.ImplementedBy;
import com.rosetta.model.lib.expression.CardinalityOperator;
import com.rosetta.model.lib.functions.IsLeapYear;
import com.rosetta.model.lib.functions.Max;
import com.rosetta.model.lib.functions.Min;
import com.rosetta.model.lib.functions.RosettaFunction;
import com.rosetta.model.lib.mapper.MapperC;
import com.rosetta.model.lib.mapper.MapperS;
import com.rosetta.model.metafields.FieldWithMetaString;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import test.expressions.Bar;
import test.expressions.Colour;
import test.expressions.Foo;
import test.expressions.FooOrBar;
import test.expressions.metafields.ReferenceWithMetaFoo;
import test.expressions.util.FooOrBarDeepPathUtil;

import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.*;

@ImplementedBy(Control.ControlDefault.class)
public abstract class Control implements RosettaFunction {

    // RosettaFunction dependencies
    //
    @Inject protected FooOrBarDeepPathUtil fooOrBarDeepPathUtil;
    @Inject protected Take take;

    /**
     * @param colour
     * @param fooOrBar
     * @param fooOrBars
     * @param foo
     * @param foos
     * @param s
     * @return result
     */
    public Integer evaluate(Colour colour, FooOrBar fooOrBar, List<? extends FooOrBar> fooOrBars, Foo foo, List<? extends Foo> foos, String s) {
        Integer result = doEvaluate(colour, fooOrBar, fooOrBars, foo, foos, s);
        return result;
    }

    protected abstract Integer doEvaluate(Colour colour, FooOrBar fooOrBar, List<? extends FooOrBar> fooOrBars, Foo foo, List<? extends Foo> foos, String s);

    protected abstract MapperS<Integer> enumSwitch(Colour colour, FooOrBar fooOrBar, List<? extends FooOrBar> fooOrBars, Foo foo, List<? extends Foo> foos, String s);

    protected abstract MapperS<Integer> choiceSwitch(Colour colour, FooOrBar fooOrBar, List<? extends FooOrBar> fooOrBars, Foo foo, List<? extends Foo> foos, String s);

    protected abstract MapperS<Integer> dataSwitch(Colour colour, FooOrBar fooOrBar, List<? extends FooOrBar> fooOrBars, Foo foo, List<? extends Foo> foos, String s);

    protected abstract MapperS<Integer> literalSwitch(Colour colour, FooOrBar fooOrBar, List<? extends FooOrBar> fooOrBars, Foo foo, List<? extends Foo> foos, String s);

    protected abstract MapperC<? extends Bar> narrowed(Colour colour, FooOrBar fooOrBar, List<? extends FooOrBar> fooOrBars, Foo foo, List<? extends Foo> foos, String s);

    protected abstract MapperC<? extends Foo> either(Colour colour, FooOrBar fooOrBar, List<? extends FooOrBar> fooOrBars, Foo foo, List<? extends Foo> foos, String s);

    protected abstract MapperS<Integer> deepAttribute(Colour colour, FooOrBar fooOrBar, List<? extends FooOrBar> fooOrBars, Foo foo, List<? extends Foo> foos, String s);

    protected abstract MapperS<String> deepKey(Colour colour, FooOrBar fooOrBar, List<? extends FooOrBar> fooOrBars, Foo foo, List<? extends Foo> foos, String s);

    protected abstract MapperC<String> metas(Colour colour, FooOrBar fooOrBar, List<? extends FooOrBar> fooOrBars, Foo foo, List<? extends Foo> foos, String s);

    protected abstract MapperS<Colour> enumValue(Colour colour, FooOrBar fooOrBar, List<? extends FooOrBar> fooOrBars, Foo foo, List<? extends Foo> foos, String s);

    protected abstract MapperC<BigDecimal> called(Colour colour, FooOrBar fooOrBar, List<? extends FooOrBar> fooOrBars, Foo foo, List<? extends Foo> foos, String s);

    protected abstract MapperS<Boolean> leapYear(Colour colour, FooOrBar fooOrBar, List<? extends FooOrBar> fooOrBars, Foo foo, List<? extends Foo> foos, String s);

    public static class ControlDefault extends Control {
        @Override
        protected Integer doEvaluate(Colour colour, FooOrBar fooOrBar, List<? extends FooOrBar> fooOrBars, Foo foo, List<? extends Foo> foos, String s) {
            if (fooOrBars == null) {
                fooOrBars = Collections.emptyList();
            }
            if (foos == null) {
                foos = Collections.emptyList();
            }
            Integer result = null;
            return assignOutput(result, colour, fooOrBar, fooOrBars, foo, foos, s);
        }

        protected Integer assignOutput(Integer result, Colour colour, FooOrBar fooOrBar, List<? extends FooOrBar> fooOrBars, Foo foo, List<? extends Foo> foos, String s) {
            if (exists(MapperS.of(foo)).getOrDefault(false)) {
                final FieldWithMetaString fieldWithMetaString = MapperS.of(foo).<FieldWithMetaString>map("getCode", _foo -> _foo.getCode()).get();
                final String string = "x";
                result = take.evaluate(1, (long) 1, BigInteger.valueOf(1), BigDecimal.valueOf(1), (fieldWithMetaString == null ? null : fieldWithMetaString.getValue()), (string == null ? FieldWithMetaString.builder().build() : FieldWithMetaString.builder().setValue(string).build()));
            } else {
                final Integer integer0 = MapperS.of(foo).<Integer>map("getI", _foo -> _foo.getI()).get();
                final Integer integer1 = MapperS.of(foo).<Integer>map("getI", _foo -> _foo.getI()).get();
                result = take.evaluate(MapperS.of(foo).<Integer>map("getI", _foo -> _foo.getI()).get(), (integer0 == null ? null : integer0.longValue()), (integer1 == null ? null : BigInteger.valueOf(integer1)), BigDecimal.valueOf(1), null, FieldWithMetaString.builder().build());
            }
            return result;
        }

        @Override
        protected MapperS<Integer> enumSwitch(Colour colour, FooOrBar fooOrBar, List<? extends FooOrBar> fooOrBars, Foo foo, List<? extends Foo> foos, String s) {
            if (colour == null) {
                return MapperS.<Integer>ofNull();
            }
            if (colour == Colour.RED) {
                return MapperS.of(1);
            }
            if (colour == Colour.GREEN) {
                return MapperS.of(2);
            }
            return MapperS.of(3);
        }

        @Override
        protected MapperS<Integer> choiceSwitch(Colour colour, FooOrBar fooOrBar, List<? extends FooOrBar> fooOrBars, Foo foo, List<? extends Foo> foos, String s) {
            final MapperS<FooOrBar> switchArgument = MapperS.of(fooOrBar);
            if (switchArgument.get() == null) {
                return MapperS.<Integer>ofNull();
            }
            if (switchArgument.<Foo>map("getFoo", _fooOrBar -> _fooOrBar.getFoo()).get() != null) {
                final MapperS<Foo> _foo = switchArgument.<Foo>map("getFoo", _fooOrBar -> _fooOrBar.getFoo());
                return _foo.<Integer>map("getI", __foo -> __foo.getI());
            }
            if (switchArgument.<Bar>map("getBar", _fooOrBar -> _fooOrBar.getBar()).get() != null) {
                final MapperS<Bar> bar = switchArgument.<Bar>map("getBar", _fooOrBar -> _fooOrBar.getBar());
                return MapperS.of(2);
            }
            return MapperS.<Integer>ofNull();
        }

        @Override
        protected MapperS<Integer> dataSwitch(Colour colour, FooOrBar fooOrBar, List<? extends FooOrBar> fooOrBars, Foo foo, List<? extends Foo> foos, String s) {
            if (foo == null) {
                return MapperS.<Integer>ofNull();
            }
            if (foo instanceof Bar) {
                final Bar bar = (Bar) foo;
                return MapperS.of(bar).<Integer>map("getI", _bar -> _bar.getI());
            }
            return MapperS.of(0);
        }

        @Override
        protected MapperS<Integer> literalSwitch(Colour colour, FooOrBar fooOrBar, List<? extends FooOrBar> fooOrBars, Foo foo, List<? extends Foo> foos, String s) {
            final MapperS<String> switchArgument = MapperS.of(s);
            if (switchArgument.get() == null) {
                return MapperS.<Integer>ofNull();
            }
            if (areEqual(switchArgument, MapperS.of("a"), CardinalityOperator.All).get()) {
                return MapperS.of(1);
            }
            return MapperS.of(2);
        }

        @Override
        protected MapperC<? extends Bar> narrowed(Colour colour, FooOrBar fooOrBar, List<? extends FooOrBar> fooOrBars, Foo foo, List<? extends Foo> foos, String s) {
            return MapperC.<Bar>of(MapperS.of(foo)
            	.filterSingleNullSafe(a -> a.get() instanceof Bar)
            	.map("as Bar", bar -> (Bar) bar), MapperC.<Foo>of(foos)
            	.filterItemNullSafe(a -> a.get() instanceof Bar)
            	.map("as Bar", bar -> (Bar) bar), MapperS.of(fooOrBar).<Bar>map("getBar", _fooOrBar -> _fooOrBar.getBar()), MapperC.<FooOrBar>of(fooOrBars).<Bar>map("getBar", _fooOrBar -> _fooOrBar.getBar()));
        }

        @Override
        protected MapperC<? extends Foo> either(Colour colour, FooOrBar fooOrBar, List<? extends FooOrBar> fooOrBars, Foo foo, List<? extends Foo> foos, String s) {
            if (exists(MapperS.of(s)).getOrDefault(false)) {
                return MapperC.<Foo>of(foos);
            }
            if (exists(MapperS.of(colour)).getOrDefault(false)) {
                return MapperC.of(Collections.singletonList(foo));
            }
            if (exists(MapperS.of(fooOrBar)).getOrDefault(false)) {
                return MapperC.of(MapperS.of(fooOrBar).<Foo>map("getFoo", _fooOrBar -> _fooOrBar.getFoo()));
            }
            return MapperC.<Foo>ofNull();
        }

        @Override
        protected MapperS<Integer> deepAttribute(Colour colour, FooOrBar fooOrBar, List<? extends FooOrBar> fooOrBars, Foo foo, List<? extends Foo> foos, String s) {
            return MapperS.of(fooOrBar).<Integer>map("chooseI", _fooOrBar -> fooOrBarDeepPathUtil.chooseI(_fooOrBar));
        }

        @Override
        protected MapperS<String> deepKey(Colour colour, FooOrBar fooOrBar, List<? extends FooOrBar> fooOrBars, Foo foo, List<? extends Foo> foos, String s) {
            return MapperS.of(fooOrBar).<String>map("metaChooseKey", _fooOrBar -> fooOrBarDeepPathUtil.metaChooseKey(_fooOrBar));
        }

        @Override
        protected MapperC<String> metas(Colour colour, FooOrBar fooOrBar, List<? extends FooOrBar> fooOrBars, Foo foo, List<? extends Foo> foos, String s) {
            return MapperC.<String>of(MapperS.of(foo).<FieldWithMetaString>map("getCode", _foo -> _foo.getCode()).map("getMeta", a->a.getMeta()).map("getScheme", a->a.getScheme()), MapperS.of(foo).<ReferenceWithMetaFoo>map("getRef", _foo -> _foo.getRef()).map("getReference", a->a.getExternalReference()), MapperS.of(foo).map("getMeta", a->a.getMeta()).map("getKey", a->a.getExternalKey()));
        }

        @Override
        protected MapperS<Colour> enumValue(Colour colour, FooOrBar fooOrBar, List<? extends FooOrBar> fooOrBars, Foo foo, List<? extends Foo> foos, String s) {
            if (areEqual(MapperS.of(colour), MapperS.of(Colour.GREEN), CardinalityOperator.All).getOrDefault(false)) {
                return MapperS.of(Colour.RED);
            }
            return MapperS.<Colour>ofNull();
        }

        @Override
        protected MapperC<BigDecimal> called(Colour colour, FooOrBar fooOrBar, List<? extends FooOrBar> fooOrBars, Foo foo, List<? extends Foo> foos, String s) {
            final Integer integer = new Max().execute(1, 2);
            return MapperC.<BigDecimal>of(MapperS.of(new Min().execute(BigDecimal.valueOf(1), new BigDecimal("2.5"))), (integer == null ? MapperS.<BigDecimal>ofNull() : MapperS.of(BigDecimal.valueOf(integer))));
        }

        @Override
        protected MapperS<Boolean> leapYear(Colour colour, FooOrBar fooOrBar, List<? extends FooOrBar> fooOrBars, Foo foo, List<? extends Foo> foos, String s) {
            return MapperS.of(new IsLeapYear().execute(BigDecimal.valueOf(2024)));
        }
    }
}
