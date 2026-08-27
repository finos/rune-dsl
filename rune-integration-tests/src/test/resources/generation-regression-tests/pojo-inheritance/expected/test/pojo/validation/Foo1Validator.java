package test.pojo.validation;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import com.rosetta.model.metafields.FieldWithMetaString;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import test.pojo.Foo1;
import test.pojo.Parent;


public class Foo1Validator implements Validator<Foo1> {
    private List<ComparisonResult> getComparisonResults(Foo1 o) {
        return Lists.<ComparisonResult>newArrayList(
            ExpressionOperatorsNullSafe.checkCardinality("attr", (Integer) o.getAttr() != null ? 1 : 0, 1, 1),
            ExpressionOperatorsNullSafe.checkCardinality("numberAttr", (BigDecimal) o.getNumberAttr() != null ? 1 : 0, 0, 1),
            ExpressionOperatorsNullSafe.checkCardinality("parent", (Parent) o.getParent() != null ? 1 : 0, 1, 1),
            ExpressionOperatorsNullSafe.checkCardinality("parentList", (List<? extends Parent>) o.getParentList() == null ? 0 : o.getParentList().size(), 0, 10),
            ExpressionOperatorsNullSafe.checkCardinality("stringAttr", (FieldWithMetaString) o.getStringAttr() != null ? 1 : 0, 1, 1)
        );
    }

    @Override
    public List<ValidationResult<?>> getValidationResults(RosettaPath path, Foo1 o) {
        return getComparisonResults(o)
            .stream()
            .map(res -> {
                if (!Strings.isNullOrEmpty(res.getError())) {
                    return ValidationResult.failure("Foo1", ValidationResult.ValidationType.CARDINALITY, "Foo1", path, "", res.getError());
                }
                return ValidationResult.success("Foo1", ValidationResult.ValidationType.CARDINALITY, "Foo1", path, "");
            })
            .collect(Collectors.toList());
    }
}
