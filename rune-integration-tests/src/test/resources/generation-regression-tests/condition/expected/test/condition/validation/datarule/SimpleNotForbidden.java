package test.condition.validation.datarule;

import com.google.inject.ImplementedBy;
import com.rosetta.model.lib.annotations.RosettaDataRule;
import com.rosetta.model.lib.expression.CardinalityOperator;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.mapper.MapperS;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import test.condition.Simple;

import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.*;

/**
 * @version 0.0.0
 */
@RosettaDataRule("SimpleNotForbidden")
@ImplementedBy(SimpleNotForbidden.Default.class)
public interface SimpleNotForbidden extends Validator<Simple> {

    String NAME = "SimpleNotForbidden";
    String DEFINITION = "val <> \"forbidden\"";

    class Default implements SimpleNotForbidden {

        @Override
        public List<ValidationResult<?>> getValidationResults(RosettaPath path, Simple simple) {
            ComparisonResult result = executeDataRule(simple);
            if (result.getOrDefault(true)) {
                return Arrays.asList(ValidationResult.success(NAME, ValidationResult.ValidationType.DATA_RULE, "Simple", path, DEFINITION));
            }

            String failureMessage = result.getError();
            if (failureMessage == null || failureMessage.contains("Null") || failureMessage == "") {
                failureMessage = "Condition has failed.";
            }
            return Arrays.asList(ValidationResult.failure(NAME, ValidationResult.ValidationType.DATA_RULE, "Simple", path, DEFINITION, failureMessage));
        }

        private ComparisonResult executeDataRule(Simple simple) {
            try {
                return notEqual(MapperS.of(simple).<String>map("getVal", _simple -> _simple.getVal()), MapperS.of("forbidden"), CardinalityOperator.Any);
            }
            catch (Exception ex) {
                return ComparisonResult.failure(ex.getMessage());
            }
        }
    }

    @SuppressWarnings("unused")
    class NoOp implements SimpleNotForbidden {

        @Override
        public List<ValidationResult<?>> getValidationResults(RosettaPath path, Simple simple) {
            return Collections.emptyList();
        }
    }
}
