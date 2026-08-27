package test.pojo.validation;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.List;
import java.util.stream.Collectors;
import test.pojo.Bar;
import test.pojo.Qux;


public class BarValidator implements Validator<Bar> {
    private List<ComparisonResult> getComparisonResults(Bar o) {
        return Lists.<ComparisonResult>newArrayList(
            ExpressionOperatorsNullSafe.checkCardinality("bar", (Qux) o.getBar() != null ? 1 : 0, 1, 1)
        );
    }

    @Override
    public List<ValidationResult<?>> getValidationResults(RosettaPath path, Bar o) {
        return getComparisonResults(o)
            .stream()
            .map(res -> {
                if (!Strings.isNullOrEmpty(res.getError())) {
                    return ValidationResult.failure("Bar", ValidationResult.ValidationType.CARDINALITY, "Bar", path, "", res.getError());
                }
                return ValidationResult.success("Bar", ValidationResult.ValidationType.CARDINALITY, "Bar", path, "");
            })
            .collect(Collectors.toList());
    }
}
