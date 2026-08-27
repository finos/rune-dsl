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
import test.pojo.Child;
import test.pojo.Foo3;
import test.pojo.metafields.ReferenceWithMetaGrandChild;


public class Foo3Validator implements Validator<Foo3> {
    private List<ComparisonResult> getComparisonResults(Foo3 o) {
        return Lists.<ComparisonResult>newArrayList(
            ExpressionOperatorsNullSafe.checkCardinality("attr", (Integer) o.getAttr() != null ? 1 : 0, 1, 1),
            ExpressionOperatorsNullSafe.checkCardinality("numberAttr", (Integer) o.getNumberAttrOverriddenAsInteger() != null ? 1 : 0, 1, 1),
            ExpressionOperatorsNullSafe.checkCardinality("parent", (Child) o.getParent() != null ? 1 : 0, 1, 1),
            ExpressionOperatorsNullSafe.checkCardinality("parentList", (ReferenceWithMetaGrandChild) o.getParentListOverriddenAsReferenceWithMetaGrandChild() != null ? 1 : 0, 1, 1),
            ExpressionOperatorsNullSafe.checkCardinality("stringAttr", (FieldWithMetaString) o.getStringAttr() != null ? 1 : 0, 1, 1)
        );
    }

    @Override
    public List<ValidationResult<?>> getValidationResults(RosettaPath path, Foo3 o) {
        return getComparisonResults(o)
            .stream()
            .map(res -> {
                if (!Strings.isNullOrEmpty(res.getError())) {
                    return ValidationResult.failure("Foo3", ValidationResult.ValidationType.CARDINALITY, "Foo3", path, "", res.getError());
                }
                return ValidationResult.success("Foo3", ValidationResult.ValidationType.CARDINALITY, "Foo3", path, "");
            })
            .collect(Collectors.toList());
    }
}
