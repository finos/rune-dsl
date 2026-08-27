package test.escaping.index.validation;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.List;
import java.util.stream.Collectors;
import test.escaping.index.Foo;
import test.escaping.index.ResultEscaping;


public class ResultEscapingValidator implements Validator<ResultEscaping> {
    private List<ComparisonResult> getComparisonResults(ResultEscaping o) {
        return Lists.<ComparisonResult>newArrayList(
            ExpressionOperatorsNullSafe.checkCardinality("result", (Foo) o.getResult() != null ? 1 : 0, 1, 1)
        );
    }

    @Override
    public List<ValidationResult<?>> getValidationResults(RosettaPath path, ResultEscaping o) {
        return getComparisonResults(o)
            .stream()
            .map(res -> {
                if (!Strings.isNullOrEmpty(res.getError())) {
                    return ValidationResult.failure("ResultEscaping", ValidationResult.ValidationType.CARDINALITY, "ResultEscaping", path, "", res.getError());
                }
                return ValidationResult.success("ResultEscaping", ValidationResult.ValidationType.CARDINALITY, "ResultEscaping", path, "");
            })
            .collect(Collectors.toList());
    }
}
