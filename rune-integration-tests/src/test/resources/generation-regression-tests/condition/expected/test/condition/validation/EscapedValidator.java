package test.condition.validation;

import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.List;
import test.condition.Escaped;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.checkCardinality;
import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;
import static java.util.stream.Collectors.toList;

public class EscapedValidator implements Validator<Escaped> {
    private List<ComparisonResult> getComparisonResults(Escaped o) {
        return Lists.<ComparisonResult>newArrayList(
            checkCardinality("val", (String) o.getVal() != null ? 1 : 0, 1, 1)
        );
    }

    @Override
    public List<ValidationResult<?>> getValidationResults(RosettaPath path, Escaped o) {
        return getComparisonResults(o)
            .stream()
            .map(res -> {
                if (!isNullOrEmpty(res.getError())) {
                    return failure("Escaped", ValidationResult.ValidationType.CARDINALITY, "Escaped", path, "", res.getError());
                }
                return success("Escaped", ValidationResult.ValidationType.CARDINALITY, "Escaped", path, "");
            })
            .collect(toList());
    }
}
