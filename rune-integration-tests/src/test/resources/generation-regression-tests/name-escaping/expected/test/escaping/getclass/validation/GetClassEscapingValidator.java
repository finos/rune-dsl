package test.escaping.getclass.validation;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.List;
import java.util.stream.Collectors;
import test.escaping.getclass.GetClassEscaping;


public class GetClassEscapingValidator implements Validator<GetClassEscaping> {
    private List<ComparisonResult> getComparisonResults(GetClassEscaping o) {
        return Lists.<ComparisonResult>newArrayList(
            ExpressionOperatorsNullSafe.checkCardinality("class", (Integer) o._getClass() != null ? 1 : 0, 1, 1)
        );
    }

    @Override
    public List<ValidationResult<?>> getValidationResults(RosettaPath path, GetClassEscaping o) {
        return getComparisonResults(o)
            .stream()
            .map(res -> {
                if (!Strings.isNullOrEmpty(res.getError())) {
                    return ValidationResult.failure("GetClassEscaping", ValidationResult.ValidationType.CARDINALITY, "GetClassEscaping", path, "", res.getError());
                }
                return ValidationResult.success("GetClassEscaping", ValidationResult.ValidationType.CARDINALITY, "GetClassEscaping", path, "");
            })
            .collect(Collectors.toList());
    }
}
