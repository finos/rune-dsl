package test.pojo.validation;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import com.rosetta.model.metafields.FieldWithMetaString;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;
import test.pojo.Child;
import test.pojo.Foo2;
import test.pojo.metafields.ReferenceWithMetaChild;


public class Foo2Validator implements Validator<Foo2> {
    private List<ComparisonResult> getComparisonResults(Foo2 o) {
        return Lists.<ComparisonResult>newArrayList(
            ExpressionOperatorsNullSafe.checkCardinality("attr", (Integer) o.getAttr() != null ? 1 : 0, 1, 1),
            ExpressionOperatorsNullSafe.checkCardinality("numberAttr", (BigInteger) o.getNumberAttrOverriddenAsBigInteger() != null ? 1 : 0, 1, 1),
            ExpressionOperatorsNullSafe.checkCardinality("parent", (Child) o.getParent() != null ? 1 : 0, 1, 1),
            ExpressionOperatorsNullSafe.checkCardinality("parentList", (ReferenceWithMetaChild) o.getParentListOverriddenAsSingleReferenceWithMetaChild() != null ? 1 : 0, 1, 1),
            ExpressionOperatorsNullSafe.checkCardinality("stringAttr", (FieldWithMetaString) o.getStringAttr() != null ? 1 : 0, 1, 1)
        );
    }

    @Override
    public List<ValidationResult<?>> getValidationResults(RosettaPath path, Foo2 o) {
        return getComparisonResults(o)
            .stream()
            .map(res -> {
                if (!Strings.isNullOrEmpty(res.getError())) {
                    return ValidationResult.failure("Foo2", ValidationResult.ValidationType.CARDINALITY, "Foo2", path, "", res.getError());
                }
                return ValidationResult.success("Foo2", ValidationResult.ValidationType.CARDINALITY, "Foo2", path, "");
            })
            .collect(Collectors.toList());
    }
}
