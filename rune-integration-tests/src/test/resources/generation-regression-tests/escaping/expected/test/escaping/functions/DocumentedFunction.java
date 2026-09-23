package test.escaping.functions;

import com.google.inject.ImplementedBy;
import com.rosetta.model.lib.functions.ConditionValidator;
import com.rosetta.model.lib.functions.RosettaFunction;
import com.rosetta.model.lib.mapper.MapperS;
import javax.inject.Inject;

import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.*;

@ImplementedBy(DocumentedFunction.DocumentedFunctionDefault.class)
public abstract class DocumentedFunction implements RosettaFunction {

    @Inject protected ConditionValidator conditionValidator;

    /**
     * @param input An input stored in C:&#92;users, which *&#47; ends a comment.
     * @return result An output stored in C:&#92;users, which *&#47; ends a comment.
     */
    public String evaluate(String input) {
        // pre-conditions
        conditionValidator.validate(() -> exists(MapperS.of(input)),
            "A condition stored in C:\\users, which is \"quoted\".");

        String result = doEvaluate(input);
        return result;
    }

    protected abstract String doEvaluate(String input);

    public static class DocumentedFunctionDefault extends DocumentedFunction {
        @Override
        protected String doEvaluate(String input) {
            String result = null;
            return assignOutput(result, input);
        }

        protected String assignOutput(String result, String input) {
            result = input;
            return result;
        }
    }
}
