package test.expressions.functions;

import com.google.inject.ImplementedBy;
import com.rosetta.model.lib.expression.CardinalityOperator;
import com.rosetta.model.lib.expression.MapperMaths;
import com.rosetta.model.lib.functions.RosettaFunction;
import com.rosetta.model.lib.mapper.MapperC;
import com.rosetta.model.lib.mapper.MapperListOfLists;
import com.rosetta.model.lib.mapper.MapperS;
import com.rosetta.model.metafields.FieldWithMetaString;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import test.expressions.Foo;

import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.*;

@ImplementedBy(ListOperations.ListOperationsDefault.class)
public abstract class ListOperations implements RosettaFunction {

    /**
     * @param foos
     * @param ints
     * @return result
     */
    public List<BigDecimal> evaluate(List<? extends Foo> foos, List<Integer> ints) {
        List<BigDecimal> result = doEvaluate(foos, ints);
        return result;
    }

    protected abstract List<BigDecimal> doEvaluate(List<? extends Foo> foos, List<Integer> ints);

    protected abstract MapperS<? extends Foo> filtered(List<? extends Foo> foos, List<Integer> ints);

    protected abstract MapperC<String> listOfLists(List<? extends Foo> foos, List<Integer> ints);

    protected abstract MapperC<Integer> mapped(List<? extends Foo> foos, List<Integer> ints);

    protected abstract MapperS<Integer> reduced(List<? extends Foo> foos, List<Integer> ints);

    protected abstract MapperC<Integer> sorted(List<? extends Foo> foos, List<Integer> ints);

    protected abstract MapperC<Integer> extremes(List<? extends Foo> foos, List<Integer> ints);

    protected abstract MapperC<Integer> singles(List<? extends Foo> foos, List<Integer> ints);

    protected abstract MapperC<Integer> aggregates(List<? extends Foo> foos, List<Integer> ints);

    protected abstract MapperC<String> joined(List<? extends Foo> foos, List<Integer> ints);

    public static class ListOperationsDefault extends ListOperations {
        @Override
        protected List<BigDecimal> doEvaluate(List<? extends Foo> foos, List<Integer> ints) {
            if (foos == null) {
                foos = Collections.emptyList();
            }
            if (ints == null) {
                ints = Collections.emptyList();
            }
            List<BigDecimal> result = new ArrayList<>();
            return assignOutput(result, foos, ints);
        }

        protected List<BigDecimal> assignOutput(List<BigDecimal> result, List<? extends Foo> foos, List<Integer> ints) {
            result = new ArrayList<>(mapped(foos, ints).<BigDecimal>map("Type coercion", integer -> BigDecimal.valueOf(integer)).getMulti());
            result.addAll(ints.stream()
            	.<BigDecimal>map(integer -> BigDecimal.valueOf(integer))
            	.collect(Collectors.toList())
            );
            result.addAll(Collections.singletonList(BigDecimal.valueOf(1)));
            return result;
        }

        @Override
        protected MapperS<? extends Foo> filtered(List<? extends Foo> foos, List<Integer> ints) {
            final MapperC<Foo> thenArg = MapperC.<Foo>of(foos)
            	.filterItemNullSafe(item -> exists(item.map("getMeta", a->a.getMeta()).map("getKey", a->a.getExternalKey())).get());
            return MapperS.of(thenArg.get())
            	.filterSingleNullSafe(item -> exists(item.<Integer>map("getI", foo -> foo.getI())).get());
        }

        @Override
        protected MapperC<String> listOfLists(List<? extends Foo> foos, List<Integer> ints) {
            final MapperListOfLists<String> thenArg0 = MapperC.<Foo>of(foos)
            	.mapItemToList(item -> item.<String>mapC("getS", foo -> foo.getS()));
            final MapperListOfLists<String> thenArg1 = thenArg0
            	.filterListNullSafe(item -> greaterThan(MapperS.of(item.resultCount()), MapperS.of(0), CardinalityOperator.All).get());
            return thenArg1
            	.flattenList();
        }

        @Override
        protected MapperC<Integer> mapped(List<? extends Foo> foos, List<Integer> ints) {
            final MapperListOfLists<String> thenArg0 = MapperC.<Foo>of(foos)
            	.mapItemToList(item -> item.<String>mapC("getS", foo -> foo.getS()));
            final MapperListOfLists<String> thenArg1 = MapperC.<Foo>of(foos)
            	.mapItemToList(item -> item.<String>mapC("getS", foo -> foo.getS()));
            final MapperListOfLists<String> thenArg2 = thenArg1
            	.mapListToList(item -> distinctIgnoringPrecision(item));
            return MapperC.<Integer>of(MapperC.<Foo>of(foos)
            	.mapItem(f -> f.<Integer>map("getI", foo -> foo.getI())), thenArg0
            	.mapListToItem(item -> MapperS.of(item.resultCount())), MapperS.of(thenArg2
            	.flattenList().resultCount()), MapperS.of(MapperC.of(foos).get())
            	.mapSingleToItem(item -> item.<Integer>map("getI", foo -> foo.getI())));
        }

        @Override
        protected MapperS<Integer> reduced(List<? extends Foo> foos, List<Integer> ints) {
            final MapperC<Integer> thenArg = MapperC.<Foo>of(foos)
            	.mapItem(item -> item.<Integer>map("getI", foo -> foo.getI()));
            return thenArg
            	.<Integer>reduce((a, b) -> MapperMaths.<Integer, Integer, Integer>add(a, b));
        }

        @Override
        protected MapperC<Integer> sorted(List<? extends Foo> foos, List<Integer> ints) {
            final MapperC<Integer> thenArg0 = MapperC.<Foo>of(foos)
            	.mapItem(item -> item.<Integer>map("getI", foo -> foo.getI()));
            final MapperC<Foo> thenArg1 = MapperC.<Foo>of(foos)
            	.sort(item -> item.<Integer>map("getI", foo -> foo.getI()));
            final MapperC<FieldWithMetaString> thenArg2 = MapperC.<Foo>of(foos)
            	.mapItem(item -> item.<FieldWithMetaString>map("getCode", foo -> foo.getCode()));
            final MapperC<FieldWithMetaString> thenArg3 = thenArg2
            	.sort(lambdaParam -> lambdaParam.<String>map("Type coercion", fieldWithMetaString -> fieldWithMetaString == null ? null : fieldWithMetaString.getValue()));
            return MapperC.<Integer>of(thenArg0
            	.sort(), thenArg1
            	.mapItem(item -> item.<Integer>map("getI", foo -> foo.getI())), thenArg3
            	.mapItem(item -> item.<String>map("Type coercion", _fieldWithMetaString -> _fieldWithMetaString == null ? null : _fieldWithMetaString.getValue()).checkedMap("to-int", Integer::parseInt, NumberFormatException.class)));
        }

        @Override
        protected MapperC<Integer> extremes(List<? extends Foo> foos, List<Integer> ints) {
            final MapperC<Integer> thenArg0 = MapperC.<Foo>of(foos)
            	.mapItem(item -> item.<Integer>map("getI", foo -> foo.getI()));
            final MapperC<FieldWithMetaString> thenArg1 = MapperC.<Foo>of(foos)
            	.mapItem(item -> item.<FieldWithMetaString>map("getCode", foo -> foo.getCode()));
            return MapperC.<Integer>of(thenArg0
            	.max(), MapperC.<Foo>of(foos)
            	.min(item -> item.<Integer>map("getI", foo -> foo.getI())).<Integer>map("getI", foo -> foo.getI()), thenArg1.<String>map("Type coercion", fieldWithMetaString -> fieldWithMetaString.getValue())
            	.max().checkedMap("to-int", Integer::parseInt, NumberFormatException.class));
        }

        @Override
        protected MapperC<Integer> singles(List<? extends Foo> foos, List<Integer> ints) {
            return MapperC.<Foo>of(MapperC.<Foo>of(foos)
            	.first(), MapperC.<Foo>of(foos)
            	.last())
            	.mapItem(item -> item.<Integer>map("getI", foo -> foo.getI()));
        }

        @Override
        protected MapperC<Integer> aggregates(List<? extends Foo> foos, List<Integer> ints) {
            final MapperC<Integer> thenArg = MapperC.<Foo>of(foos)
            	.mapItem(item -> item.<Integer>map("getI", foo -> foo.getI()));
            return MapperC.<Integer>of(MapperS.of(MapperC.<Foo>of(foos)
            	.reverse().resultCount()), MapperS.of(distinctIgnoringPrecision(MapperC.<Foo>of(foos)).resultCount()), thenArg
            	.sumInteger());
        }

        @Override
        protected MapperC<String> joined(List<? extends Foo> foos, List<Integer> ints) {
            final MapperListOfLists<String> thenArg0 = MapperC.<Foo>of(foos)
            	.mapItemToList(item -> item.<String>mapC("getS", foo -> foo.getS()));
            final MapperC<String> thenArg1 = MapperS.of(MapperC.of(foos).get())
            	.mapSingleToList(item -> item.<String>mapC("getS", foo -> foo.getS()));
            return MapperC.<String>of(thenArg0
            	.flattenList().join(MapperS.of(", ")), thenArg1.join(MapperS.of("")));
        }
    }
}
