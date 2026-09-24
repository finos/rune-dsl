package test.condition.functions;

import com.google.inject.ImplementedBy;
import com.rosetta.model.lib.functions.IQualifyFunctionExtension;
import com.rosetta.model.lib.functions.RosettaFunction;
import com.rosetta.model.lib.mapper.MapperS;
import test.condition.Simple;

import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.*;

@ImplementedBy(Qualify_Simple.Qualify_SimpleDefault.class)
public abstract class Qualify_Simple implements RosettaFunction,IQualifyFunctionExtension<Simple> {

    /**
     * @param simple
     * @return is_product
     */
    @Override
    public Boolean evaluate(Simple simple) {
        Boolean is_product = doEvaluate(simple);
        return is_product;
    }

    protected abstract Boolean doEvaluate(Simple simple);

    public static class Qualify_SimpleDefault extends Qualify_Simple {
        @Override
        protected Boolean doEvaluate(Simple simple) {
            Boolean is_product = null;
            return assignOutput(is_product, simple);
        }

        protected Boolean assignOutput(Boolean is_product, Simple simple) {
            is_product = exists(MapperS.of(simple).<String>map("getVal", _simple -> _simple.getVal())).get();
            return is_product;
        }
    }

    @Override
    public String getNamePrefix() {
        return "Qualify";
    }
}
