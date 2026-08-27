package test.escaping.gettype.validation;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.List;
import java.util.stream.Collectors;
import test.escaping.gettype.GetTypeEscaping;


public class GetTypeEscapingValidator implements Validator<GetTypeEscaping> {
    private List<ComparisonResult> getComparisonResults(GetTypeEscaping o) {
        return Lists.<ComparisonResult>newArrayList(
            ExpressionOperatorsNullSafe.checkCardinality("type", (Integer) o._getType() != null ? 1 : 0, 1, 1)
        );
    }

    @Override
    public List<ValidationResult<?>> getValidationResults(RosettaPath path, GetTypeEscaping o) {
        return getComparisonResults(o)
            .stream()
            .map(res -> {
                if (!Strings.isNullOrEmpty(res.getError())) {
                    return ValidationResult.failure("GetTypeEscaping", ValidationResult.ValidationType.CARDINALITY, "GetTypeEscaping", path, "", res.getError());
                }
                return ValidationResult.success("GetTypeEscaping", ValidationResult.ValidationType.CARDINALITY, "GetTypeEscaping", path, "");
            })
            .collect(Collectors.toList());
    }
}
