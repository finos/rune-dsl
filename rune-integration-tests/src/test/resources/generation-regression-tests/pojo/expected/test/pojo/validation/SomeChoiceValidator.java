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
import test.pojo.Foo;
import test.pojo.SomeChoice;


public class SomeChoiceValidator implements Validator<SomeChoice> {
    private List<ComparisonResult> getComparisonResults(SomeChoice o) {
        return Lists.<ComparisonResult>newArrayList(
            ExpressionOperatorsNullSafe.checkCardinality("Foo", (Foo) o.getFoo() != null ? 1 : 0, 0, 1),
            ExpressionOperatorsNullSafe.checkCardinality("Bar", (Bar) o.getBar() != null ? 1 : 0, 0, 1)
        );
    }

    @Override
    public List<ValidationResult<?>> getValidationResults(RosettaPath path, SomeChoice o) {
        return getComparisonResults(o)
            .stream()
            .map(res -> {
                if (!Strings.isNullOrEmpty(res.getError())) {
                    return ValidationResult.failure("SomeChoice", ValidationResult.ValidationType.CARDINALITY, "SomeChoice", path, "", res.getError());
                }
                return ValidationResult.success("SomeChoice", ValidationResult.ValidationType.CARDINALITY, "SomeChoice", path, "");
            })
            .collect(Collectors.toList());
    }
}
