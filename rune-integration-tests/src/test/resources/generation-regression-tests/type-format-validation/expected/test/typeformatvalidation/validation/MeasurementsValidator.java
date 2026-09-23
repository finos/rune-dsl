package test.typeformatvalidation.validation;

import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.List;
import test.typeformatvalidation.Measurements;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.checkCardinality;
import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;
import static java.util.stream.Collectors.toList;

public class MeasurementsValidator implements Validator<Measurements> {
    private List<ComparisonResult> getComparisonResults(Measurements o) {
        return Lists.<ComparisonResult>newArrayList(
            checkCardinality("amount", (Integer) o.getAmount() != null ? 1 : 0, 1, 1),
            checkCardinality("note", (String) o.getNote() != null ? 1 : 0, 0, 1)
        );
    }

    @Override
    public List<ValidationResult<?>> getValidationResults(RosettaPath path, Measurements o) {
        return getComparisonResults(o)
            .stream()
            .map(res -> {
                if (!isNullOrEmpty(res.getError())) {
                    return failure("Measurements", ValidationResult.ValidationType.CARDINALITY, "Measurements", path, "", res.getError());
                }
                return success("Measurements", ValidationResult.ValidationType.CARDINALITY, "Measurements", path, "");
            })
            .collect(toList());
    }
}
