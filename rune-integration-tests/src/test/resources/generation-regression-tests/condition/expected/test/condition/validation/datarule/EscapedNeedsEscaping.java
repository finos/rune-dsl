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
import test.condition.Escaped;

import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.*;

/**
 * @version 0.0.0
 */
@RosettaDataRule("EscapedNeedsEscaping")
@ImplementedBy(EscapedNeedsEscaping.Default.class)
public interface EscapedNeedsEscaping extends Validator<Escaped> {

    String NAME = "EscapedNeedsEscaping";
    String DEFINITION = "val <> \"C:\\\\dir \\\"quoted\\\" \\\\u0041\"";

    class Default implements EscapedNeedsEscaping {

        @Override
        public List<ValidationResult<?>> getValidationResults(RosettaPath path, Escaped escaped) {
            ComparisonResult result = executeDataRule(escaped);
            if (result.getOrDefault(true)) {
                return Arrays.asList(ValidationResult.success(NAME, ValidationResult.ValidationType.DATA_RULE, "Escaped", path, DEFINITION));
            }

            String failureMessage = result.getError();
            if (failureMessage == null || failureMessage.contains("Null") || failureMessage == "") {
                failureMessage = "Condition has failed.";
            }
            return Arrays.asList(ValidationResult.failure(NAME, ValidationResult.ValidationType.DATA_RULE, "Escaped", path, DEFINITION, failureMessage));
        }

        private ComparisonResult executeDataRule(Escaped escaped) {
            try {
                return notEqual(MapperS.of(escaped).<String>map("getVal", _escaped -> _escaped.getVal()), MapperS.of("C:\\dir \"quoted\" \\u0041"), CardinalityOperator.Any);
            }
            catch (Exception ex) {
                return ComparisonResult.failure(ex.getMessage());
            }
        }
    }

    @SuppressWarnings("unused")
    class NoOp implements EscapedNeedsEscaping {

        @Override
        public List<ValidationResult<?>> getValidationResults(RosettaPath path, Escaped escaped) {
            return Collections.emptyList();
        }
    }
}
