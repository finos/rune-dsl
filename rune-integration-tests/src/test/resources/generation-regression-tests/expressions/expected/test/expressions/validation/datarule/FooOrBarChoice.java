package test.expressions.validation.datarule;

import com.google.inject.ImplementedBy;
import com.rosetta.model.lib.annotations.RosettaDataRule;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.mapper.MapperS;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ChoiceRuleValidationMethod;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import test.expressions.FooOrBar;

import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.*;

/**
 * @version 0.0.0
 */
@RosettaDataRule("FooOrBarChoice")
@ImplementedBy(FooOrBarChoice.Default.class)
public interface FooOrBarChoice extends Validator<FooOrBar> {

    String NAME = "FooOrBarChoice";
    String DEFINITION = "";

    class Default implements FooOrBarChoice {

        @Override
        public List<ValidationResult<?>> getValidationResults(RosettaPath path, FooOrBar fooOrBar) {
            ComparisonResult result = executeDataRule(fooOrBar);
            if (result.getOrDefault(true)) {
                return Arrays.asList(ValidationResult.success(NAME, ValidationResult.ValidationType.DATA_RULE, "FooOrBar", path, DEFINITION));
            }

            String failureMessage = result.getError();
            if (failureMessage == null || failureMessage.contains("Null") || failureMessage == "") {
                failureMessage = "Condition has failed.";
            }
            return Arrays.asList(ValidationResult.failure(NAME, ValidationResult.ValidationType.DATA_RULE, "FooOrBar", path, DEFINITION, failureMessage));
        }

        private ComparisonResult executeDataRule(FooOrBar fooOrBar) {
            try {
                return choice(MapperS.of(fooOrBar), Arrays.asList("Foo", "Bar"), ChoiceRuleValidationMethod.REQUIRED);
            }
            catch (Exception ex) {
                return ComparisonResult.failure(ex.getMessage());
            }
        }
    }

    @SuppressWarnings("unused")
    class NoOp implements FooOrBarChoice {

        @Override
        public List<ValidationResult<?>> getValidationResults(RosettaPath path, FooOrBar fooOrBar) {
            return Collections.emptyList();
        }
    }
}
