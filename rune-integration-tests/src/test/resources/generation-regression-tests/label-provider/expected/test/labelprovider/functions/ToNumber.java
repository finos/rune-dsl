package test.labelprovider.functions;

import com.google.inject.ImplementedBy;
import com.rosetta.model.lib.annotations.RuneLabelProvider;
import com.rosetta.model.lib.functions.RosettaFunction;
import com.rosetta.model.lib.transform.Ingest;
import com.rosetta.model.lib.transform.SerializationFormat;
import test.labelprovider.labels.ToNumberLabelProvider;


@RuneLabelProvider(labelProvider=ToNumberLabelProvider.class)
@Ingest(format = SerializationFormat.JSON)
@ImplementedBy(ToNumber.ToNumberDefault.class)
public abstract class ToNumber implements RosettaFunction {

    /**
     * @param input
     * @return result
     */
    public Integer evaluate(Integer input) {
        Integer result = doEvaluate(input);
        return result;
    }

    protected abstract Integer doEvaluate(Integer input);

    public static class ToNumberDefault extends ToNumber {
        @Override
        protected Integer doEvaluate(Integer input) {
            Integer result = null;
            return assignOutput(result, input);
        }

        protected Integer assignOutput(Integer result, Integer input) {
            return result;
        }
    }
}
