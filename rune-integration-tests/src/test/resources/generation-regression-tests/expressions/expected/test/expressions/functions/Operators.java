package test.expressions.functions;

import com.google.inject.ImplementedBy;
import com.rosetta.model.lib.expression.CardinalityOperator;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.expression.MapperMaths;
import com.rosetta.model.lib.functions.ConditionValidator;
import com.rosetta.model.lib.functions.RosettaFunction;
import com.rosetta.model.lib.mapper.MapperC;
import com.rosetta.model.lib.mapper.MapperS;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import test.expressions.Foo;

import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.*;

@ImplementedBy(Operators.OperatorsDefault.class)
public abstract class Operators implements RosettaFunction {

    @Inject protected ConditionValidator conditionValidator;

    /**
     * @param foo
     * @param foos
     * @param l
     * @param b
     * @param flag
     * @param flags
     * @return result
     */
    public List<Boolean> evaluate(Foo foo, List<? extends Foo> foos, Long l, BigInteger b, Boolean flag, List<Boolean> flags) {
        // pre-conditions
        conditionValidator.validate(() -> {
            if ((flag == null ? false : flag)) {
                return ComparisonResult.ofNullSafe(comparison(foo, foos, l, b, flag, flags)).andNullSafe(ComparisonResult.ofNullSafe(MapperS.of(flag))).andNullSafe(contains(MapperC.<Boolean>of(flags), MapperS.of(true))).orNullSafe(ComparisonResult.ofNullSafe(MapperS.of(false)));
            }
            return ComparisonResult.ofEmpty();
        },
            "");

        List<Boolean> result = doEvaluate(foo, foos, l, b, flag, flags);
        return result;
    }

    protected abstract List<Boolean> doEvaluate(Foo foo, List<? extends Foo> foos, Long l, BigInteger b, Boolean flag, List<Boolean> flags);

    protected abstract MapperS<BigDecimal> arithmetic(Foo foo, List<? extends Foo> foos, Long l, BigInteger b, Boolean flag, List<Boolean> flags);

    protected abstract MapperS<String> concatenation(Foo foo, List<? extends Foo> foos, Long l, BigInteger b, Boolean flag, List<Boolean> flags);

    protected abstract MapperC<BigDecimal> numberLiterals(Foo foo, List<? extends Foo> foos, Long l, BigInteger b, Boolean flag, List<Boolean> flags);

    protected abstract MapperC<String> otherLiterals(Foo foo, List<? extends Foo> foos, Long l, BigInteger b, Boolean flag, List<Boolean> flags);

    protected abstract MapperS<Boolean> comparison(Foo foo, List<? extends Foo> foos, Long l, BigInteger b, Boolean flag, List<Boolean> flags);

    protected abstract MapperS<Boolean> equality(Foo foo, List<? extends Foo> foos, Long l, BigInteger b, Boolean flag, List<Boolean> flags);

    protected abstract MapperS<Boolean> existence(Foo foo, List<? extends Foo> foos, Long l, BigInteger b, Boolean flag, List<Boolean> flags);

    protected abstract MapperS<Boolean> membership(Foo foo, List<? extends Foo> foos, Long l, BigInteger b, Boolean flag, List<Boolean> flags);

    protected abstract MapperC<Integer> defaults(Foo foo, List<? extends Foo> foos, Long l, BigInteger b, Boolean flag, List<Boolean> flags);

    public static class OperatorsDefault extends Operators {
        @Override
        protected List<Boolean> doEvaluate(Foo foo, List<? extends Foo> foos, Long l, BigInteger b, Boolean flag, List<Boolean> flags) {
            if (foos == null) {
                foos = Collections.emptyList();
            }
            if (flags == null) {
                flags = Collections.emptyList();
            }
            List<Boolean> result = new ArrayList<>();
            return assignOutput(result, foo, foos, l, b, flag, flags);
        }

        protected List<Boolean> assignOutput(List<Boolean> result, Foo foo, List<? extends Foo> foos, Long l, BigInteger b, Boolean flag, List<Boolean> flags) {
            result = new ArrayList<>(comparison(foo, foos, l, b, flag, flags).getMulti());
            if (flag == null) {
                result.addAll(Collections.<Boolean>emptyList());
            } else {
                result.addAll(Collections.singletonList(flag));
            }
            result.addAll(exists(MapperC.<Foo>of(foos)).getMulti());
            return result;
        }

        @Override
        protected MapperS<BigDecimal> arithmetic(Foo foo, List<? extends Foo> foos, Long l, BigInteger b, Boolean flag, List<Boolean> flags) {
            return MapperMaths.<BigDecimal, BigDecimal, BigDecimal>subtract(MapperMaths.<BigDecimal, BigDecimal, BigDecimal>add(MapperS.of(foo).<Integer>map("getI", _foo -> _foo.getI()).<BigDecimal>map("Type coercion", integer -> integer == null ? null : BigDecimal.valueOf(integer)), MapperMaths.<BigDecimal, BigDecimal, BigDecimal>multiply(MapperS.of(new BigDecimal("1.5")), MapperS.of(BigDecimal.valueOf(2)))), MapperMaths.<BigDecimal, BigInteger, BigInteger>divide((l == null ? MapperS.<BigInteger>ofNull() : MapperS.of(BigInteger.valueOf(l))), MapperS.of(b)));
        }

        @Override
        protected MapperS<String> concatenation(Foo foo, List<? extends Foo> foos, Long l, BigInteger b, Boolean flag, List<Boolean> flags) {
            return MapperMaths.<String, String, String>add(MapperS.of("a"), MapperS.of("b"));
        }

        @Override
        protected MapperC<BigDecimal> numberLiterals(Foo foo, List<? extends Foo> foos, Long l, BigInteger b, Boolean flag, List<Boolean> flags) {
            final BigInteger bigInteger = new BigInteger("30000000000000000000");
            return MapperC.<BigDecimal>of(MapperS.of(BigDecimal.valueOf(1)), MapperS.of(BigDecimal.valueOf(3000000000l)), (bigInteger == null ? MapperS.<BigDecimal>ofNull() : MapperS.of(new BigDecimal(bigInteger))), MapperS.of(new BigDecimal("1.5")));
        }

        @Override
        protected MapperC<String> otherLiterals(Foo foo, List<? extends Foo> foos, Long l, BigInteger b, Boolean flag, List<Boolean> flags) {
            return MapperC.<String>of(MapperS.of("s"), MapperS.<String>ofNull());
        }

        @Override
        protected MapperS<Boolean> comparison(Foo foo, List<? extends Foo> foos, Long l, BigInteger b, Boolean flag, List<Boolean> flags) {
            return greaterThan(MapperS.of(foo).<Integer>map("getI", _foo -> _foo.getI()), MapperS.of(1), CardinalityOperator.All).andNullSafe(greaterThanEquals(MapperS.of(foo).<Integer>map("getI", _foo -> _foo.getI()).<BigDecimal>map("Type coercion", integer -> integer == null ? null : BigDecimal.valueOf(integer)), MapperS.of(new BigDecimal("1.5")), CardinalityOperator.All)).orNullSafe(lessThan(MapperS.of(foo).<Integer>map("getI", _foo -> _foo.getI()), (l == null ? MapperS.<Integer>ofNull() : MapperS.of(Math.toIntExact(l))), CardinalityOperator.All)).orNullSafe(lessThanEquals(MapperS.of(foo).<Integer>map("getI", _foo -> _foo.getI()), (b == null ? MapperS.<Integer>ofNull() : MapperS.of(b.intValueExact())), CardinalityOperator.All)).asMapper();
        }

        @Override
        protected MapperS<Boolean> equality(Foo foo, List<? extends Foo> foos, Long l, BigInteger b, Boolean flag, List<Boolean> flags) {
            return areEqual(MapperC.<Foo>of(foos).<Integer>map("getI", _foo -> _foo.getI()), MapperS.of(1), CardinalityOperator.All).andNullSafe(notEqual(MapperC.<Foo>of(foos).<Integer>map("getI", _foo -> _foo.getI()), MapperS.of(1), CardinalityOperator.Any)).andNullSafe(areEqual(MapperS.of(foo).<Integer>map("getI", _foo -> _foo.getI()), MapperS.of(1), CardinalityOperator.All)).andNullSafe(notEqual(MapperS.of(foo).<Integer>map("getI", _foo -> _foo.getI()), MapperS.of(1), CardinalityOperator.Any)).asMapper();
        }

        @Override
        protected MapperS<Boolean> existence(Foo foo, List<? extends Foo> foos, Long l, BigInteger b, Boolean flag, List<Boolean> flags) {
            return exists(MapperS.of(foo)).andNullSafe(notExists(MapperS.of(foo).<Integer>map("getI", _foo -> _foo.getI()))).andNullSafe(singleExists(MapperC.<Foo>of(foos))).andNullSafe(multipleExists(MapperC.<Foo>of(foos))).andNullSafe(onlyExists(MapperS.of(foo), Arrays.asList("i", "s", "code", "ref", "refs"), Arrays.asList("i"))).asMapper();
        }

        @Override
        protected MapperS<Boolean> membership(Foo foo, List<? extends Foo> foos, Long l, BigInteger b, Boolean flag, List<Boolean> flags) {
            return contains(MapperC.<Foo>of(foos).<Integer>map("getI", _foo -> _foo.getI()).<BigDecimal>map("Type coercion", integer -> BigDecimal.valueOf(integer)), MapperS.of(new BigDecimal("1.5"))).andNullSafe(disjoint(MapperC.<Foo>of(foos).<Integer>map("getI", _foo -> _foo.getI()), MapperC.<Integer>of(MapperS.of(2), MapperS.of(3)))).asMapper();
        }

        @Override
        protected MapperC<Integer> defaults(Foo foo, List<? extends Foo> foos, Long l, BigInteger b, Boolean flag, List<Boolean> flags) {
            return MapperC.<Integer>of(MapperS.of(MapperS.of(foo).<Integer>map("getI", _foo -> _foo.getI()).getOrDefault(0)), (MapperC.<Foo>of(foos).<Integer>map("getI", _foo -> _foo.getI()).getMulti().isEmpty() ? MapperC.<Integer>of(MapperS.of(1)) : MapperC.<Foo>of(foos).<Integer>map("getI", _foo -> _foo.getI())));
        }
    }
}
