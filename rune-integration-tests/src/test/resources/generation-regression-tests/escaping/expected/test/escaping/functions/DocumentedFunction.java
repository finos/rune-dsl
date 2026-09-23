package test.escaping.functions;

import com.google.inject.ImplementedBy;
import com.rosetta.model.lib.functions.RosettaFunction;


@ImplementedBy(DocumentedFunction.DocumentedFunctionDefault.class)
public abstract class DocumentedFunction implements RosettaFunction {

    /**
     * @param input An input stored in C:&#92;users, which *&#47; ends a comment.
     * @return result An output stored in C:&#92;users, which *&#47; ends a comment.
     */
    public String evaluate(String input) {
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
