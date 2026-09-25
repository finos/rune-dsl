package test.condition.validation;

import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.List;
import test.condition.SpansSeveralLines;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.checkCardinality;
import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;
import static java.util.stream.Collectors.toList;

public class SpansSeveralLinesValidator implements Validator<SpansSeveralLines> {
    private List<ComparisonResult> getComparisonResults(SpansSeveralLines o) {
        return Lists.<ComparisonResult>newArrayList(
            checkCardinality("val", (String) o.getVal() != null ? 1 : 0, 1, 1)
        );
    }

    @Override
    public List<ValidationResult<?>> getValidationResults(RosettaPath path, SpansSeveralLines o) {
        return getComparisonResults(o)
            .stream()
            .map(res -> {
                if (!isNullOrEmpty(res.getError())) {
                    return failure("SpansSeveralLines", ValidationResult.ValidationType.CARDINALITY, "SpansSeveralLines", path, "", res.getError());
                }
                return success("SpansSeveralLines", ValidationResult.ValidationType.CARDINALITY, "SpansSeveralLines", path, "");
            })
            .collect(toList());
    }
}
