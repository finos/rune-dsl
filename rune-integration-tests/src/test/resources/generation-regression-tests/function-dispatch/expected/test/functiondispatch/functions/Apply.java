package test.functiondispatch.functions;

import com.google.inject.ImplementedBy;
import com.rosetta.model.lib.expression.MapperMaths;
import com.rosetta.model.lib.functions.RosettaFunction;
import com.rosetta.model.lib.mapper.MapperS;
import javax.inject.Inject;
import test.functiondispatch.Operation;


/**
 * @version 0.0.0
 */
public class Apply implements RosettaFunction {

    @Inject protected Apply.ApplyINCREMENT applyINCREMENT;
    @Inject protected Apply.ApplyDECREMENT applyDECREMENT;

    public Integer evaluate(Operation operation, Integer value) {
        switch (operation) {
            case INCREMENT:
                return applyINCREMENT.evaluate(operation, value);
            case DECREMENT:
                return applyDECREMENT.evaluate(operation, value);
            default:
                throw new IllegalArgumentException("Enum value not implemented: " + operation);
        }
    }

    @ImplementedBy(Apply.ApplyINCREMENT.ApplyINCREMENTDefault.class)
    public static abstract class ApplyINCREMENT implements RosettaFunction {

        /**
         * @param operation
         * @param value
         * @return result
         */
        public Integer evaluate(Operation operation, Integer value) {
            Integer result = doEvaluate(operation, value);
            return result;
        }

        protected abstract Integer doEvaluate(Operation operation, Integer value);

        public static class ApplyINCREMENTDefault extends Apply.ApplyINCREMENT {
            @Override
            protected Integer doEvaluate(Operation operation, Integer value) {
                Integer result = null;
                return assignOutput(result, operation, value);
            }

            protected Integer assignOutput(Integer result, Operation operation, Integer value) {
                result = MapperMaths.<Integer, Integer, Integer>add(MapperS.of(value), MapperS.of(1)).get();
                return result;
            }
        }
    }
    @ImplementedBy(Apply.ApplyDECREMENT.ApplyDECREMENTDefault.class)
    public static abstract class ApplyDECREMENT implements RosettaFunction {

        /**
         * @param operation
         * @param value
         * @return result
         */
        public Integer evaluate(Operation operation, Integer value) {
            Integer result = doEvaluate(operation, value);
            return result;
        }

        protected abstract Integer doEvaluate(Operation operation, Integer value);

        public static class ApplyDECREMENTDefault extends Apply.ApplyDECREMENT {
            @Override
            protected Integer doEvaluate(Operation operation, Integer value) {
                Integer result = null;
                return assignOutput(result, operation, value);
            }

            protected Integer assignOutput(Integer result, Operation operation, Integer value) {
                result = MapperMaths.<Integer, Integer, Integer>subtract(MapperS.of(value), MapperS.of(1)).get();
                return result;
            }
        }
    }
}
