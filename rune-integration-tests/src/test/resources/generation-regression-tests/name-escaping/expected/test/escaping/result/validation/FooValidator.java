package test.escaping.result.validation;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.List;
import java.util.stream.Collectors;
import test.escaping.result.Foo;


public class FooValidator implements Validator<Foo> {
    private List<ComparisonResult> getComparisonResults(Foo o) {
        return Lists.<ComparisonResult>newArrayList(
        );
    }

    @Override
    public List<ValidationResult<?>> getValidationResults(RosettaPath path, Foo o) {
        return getComparisonResults(o)
            .stream()
            .map(res -> {
                if (!Strings.isNullOrEmpty(res.getError())) {
                    return ValidationResult.failure("Foo", ValidationResult.ValidationType.CARDINALITY, "Foo", path, "", res.getError());
                }
                return ValidationResult.success("Foo", ValidationResult.ValidationType.CARDINALITY, "Foo", path, "");
            })
            .collect(Collectors.toList());
    }
}
