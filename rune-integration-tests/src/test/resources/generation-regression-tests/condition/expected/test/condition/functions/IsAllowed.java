package test.condition.functions;

import com.google.inject.ImplementedBy;
import com.rosetta.model.lib.expression.CardinalityOperator;
import com.rosetta.model.lib.functions.RosettaFunction;
import com.rosetta.model.lib.mapper.MapperS;
import test.condition.WithDependency;

import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.*;

@ImplementedBy(IsAllowed.IsAllowedDefault.class)
public abstract class IsAllowed implements RosettaFunction {

    /**
     * @param input
     * @return result
     */
    public Boolean evaluate(WithDependency input) {
        Boolean result = doEvaluate(input);
        return result;
    }

    protected abstract Boolean doEvaluate(WithDependency input);

    public static class IsAllowedDefault extends IsAllowed {
        @Override
        protected Boolean doEvaluate(WithDependency input) {
            Boolean result = null;
            return assignOutput(result, input);
        }

        protected Boolean assignOutput(Boolean result, WithDependency input) {
            result = notEqual(MapperS.of(input).<String>map("getVal", withDependency -> withDependency.getVal()), MapperS.of("forbidden"), CardinalityOperator.Any).get();
            return result;
        }
    }
}
