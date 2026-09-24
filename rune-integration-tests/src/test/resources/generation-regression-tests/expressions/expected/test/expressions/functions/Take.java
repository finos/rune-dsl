package test.expressions.functions;

import com.google.inject.ImplementedBy;
import com.rosetta.model.lib.functions.RosettaFunction;
import com.rosetta.model.metafields.FieldWithMetaString;
import java.math.BigDecimal;
import java.math.BigInteger;


@ImplementedBy(Take.TakeDefault.class)
public abstract class Take implements RosettaFunction {

    /**
     * @param i
     * @param l
     * @param b
     * @param n
     * @param s
     * @param code
     * @return result
     */
    public Integer evaluate(Integer i, Long l, BigInteger b, BigDecimal n, String s, FieldWithMetaString code) {
        Integer result = doEvaluate(i, l, b, n, s, code);
        return result;
    }

    protected abstract Integer doEvaluate(Integer i, Long l, BigInteger b, BigDecimal n, String s, FieldWithMetaString code);

    public static class TakeDefault extends Take {
        @Override
        protected Integer doEvaluate(Integer i, Long l, BigInteger b, BigDecimal n, String s, FieldWithMetaString code) {
            Integer result = null;
            return assignOutput(result, i, l, b, n, s, code);
        }

        protected Integer assignOutput(Integer result, Integer i, Long l, BigInteger b, BigDecimal n, String s, FieldWithMetaString code) {
            return result;
        }
    }
}
