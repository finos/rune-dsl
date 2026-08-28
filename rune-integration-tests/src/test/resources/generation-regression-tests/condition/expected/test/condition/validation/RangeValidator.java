package test.condition.validation;

import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.List;
import test.condition.Range;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.checkCardinality;
import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;
import static java.util.stream.Collectors.toList;

public class RangeValidator implements Validator<Range> {
    private List<ComparisonResult> getComparisonResults(Range o) {
        return Lists.<ComparisonResult>newArrayList(
            checkCardinality("quantity", (Integer) o.getQuantity() != null ? 1 : 0, 1, 1)
        );
    }

    @Override
    public List<ValidationResult<?>> getValidationResults(RosettaPath path, Range o) {
        return getComparisonResults(o)
            .stream()
            .map(res -> {
                if (!isNullOrEmpty(res.getError())) {
                    return failure("Range", ValidationResult.ValidationType.CARDINALITY, "Range", path, "", res.getError());
                }
                return success("Range", ValidationResult.ValidationType.CARDINALITY, "Range", path, "");
            })
            .collect(toList());
    }
}
