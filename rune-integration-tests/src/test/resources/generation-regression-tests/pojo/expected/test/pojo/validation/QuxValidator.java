package test.pojo.validation;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import com.rosetta.model.metafields.FieldWithMetaString;
import java.util.List;
import java.util.stream.Collectors;
import test.pojo.Qux;


public class QuxValidator implements Validator<Qux> {
    private List<ComparisonResult> getComparisonResults(Qux o) {
        return Lists.<ComparisonResult>newArrayList(
            ExpressionOperatorsNullSafe.checkCardinality("qux", (FieldWithMetaString) o.getQux() != null ? 1 : 0, 1, 1)
        );
    }

    @Override
    public List<ValidationResult<?>> getValidationResults(RosettaPath path, Qux o) {
        return getComparisonResults(o)
            .stream()
            .map(res -> {
                if (!Strings.isNullOrEmpty(res.getError())) {
                    return ValidationResult.failure("Qux", ValidationResult.ValidationType.CARDINALITY, "Qux", path, "", res.getError());
                }
                return ValidationResult.success("Qux", ValidationResult.ValidationType.CARDINALITY, "Qux", path, "");
            })
            .collect(Collectors.toList());
    }
}
