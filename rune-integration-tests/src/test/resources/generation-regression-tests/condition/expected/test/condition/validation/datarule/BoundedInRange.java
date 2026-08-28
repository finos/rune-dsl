package test.condition.validation.datarule;

import com.google.inject.ImplementedBy;
import com.rosetta.model.lib.annotations.RosettaDataRule;
import com.rosetta.model.lib.expression.CardinalityOperator;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.mapper.MapperS;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.*;

/**
 * @version 0.0.0
 */
@RosettaDataRule("BoundedInRange")
@ImplementedBy(BoundedInRange.Default.class)
public interface BoundedInRange {

    String NAME = "BoundedInRange";
    String DEFINITION = "item >= lowerBound and item <= upperBound";

    List<ValidationResult<?>> getValidationResults(RosettaPath path, Integer bounded, Integer lowerBound, Integer upperBound);

    class Default implements BoundedInRange {

        @Override
        public List<ValidationResult<?>> getValidationResults(RosettaPath path, Integer bounded, Integer lowerBound, Integer upperBound) {
            ComparisonResult result = executeDataRule(bounded, lowerBound, upperBound);
            if (result.getOrDefault(true)) {
                return Arrays.asList(ValidationResult.success(NAME, ValidationResult.ValidationType.DATA_RULE, "Bounded", path, DEFINITION));
            }

            String failureMessage = result.getError();
            if (failureMessage == null || failureMessage.contains("Null") || failureMessage == "") {
                failureMessage = "Condition has failed.";
            }
            return Arrays.asList(ValidationResult.failure(NAME, ValidationResult.ValidationType.DATA_RULE, "Bounded", path, DEFINITION, failureMessage));
        }

        private ComparisonResult executeDataRule(Integer bounded, Integer lowerBound, Integer upperBound) {
            try {
                return greaterThanEquals(MapperS.of(bounded), MapperS.of(lowerBound), CardinalityOperator.All).andNullSafe(lessThanEquals(MapperS.of(bounded), MapperS.of(upperBound), CardinalityOperator.All));
            }
            catch (Exception ex) {
                return ComparisonResult.failure(ex.getMessage());
            }
        }
    }

    @SuppressWarnings("unused")
    class NoOp implements BoundedInRange {

        @Override
        public List<ValidationResult<?>> getValidationResults(RosettaPath path, Integer bounded, Integer lowerBound, Integer upperBound) {
            return Collections.emptyList();
        }
    }
}
