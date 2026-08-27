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
import test.pojo.Foo;
import test.pojo.Pojo;
import test.pojo.metafields.ReferenceWithMetaFoo;


public class PojoValidator implements Validator<Pojo> {
    private List<ComparisonResult> getComparisonResults(Pojo o) {
        return Lists.<ComparisonResult>newArrayList(
            ExpressionOperatorsNullSafe.checkCardinality("simpleAttr", (String) o.getSimpleAttr() != null ? 1 : 0, 1, 1),
            ExpressionOperatorsNullSafe.checkCardinality("simpleAttrWithMeta", (FieldWithMetaString) o.getSimpleAttrWithMeta() != null ? 1 : 0, 1, 1),
            ExpressionOperatorsNullSafe.checkCardinality("simpleAttrWithId", (FieldWithMetaString) o.getSimpleAttrWithId() != null ? 1 : 0, 1, 1),
            ExpressionOperatorsNullSafe.checkCardinality("complexAttr", (Foo) o.getComplexAttr() != null ? 1 : 0, 1, 1),
            ExpressionOperatorsNullSafe.checkCardinality("complexAttrWithRef", (ReferenceWithMetaFoo) o.getComplexAttrWithRef() != null ? 1 : 0, 1, 1)
        );
    }

    @Override
    public List<ValidationResult<?>> getValidationResults(RosettaPath path, Pojo o) {
        return getComparisonResults(o)
            .stream()
            .map(res -> {
                if (!Strings.isNullOrEmpty(res.getError())) {
                    return ValidationResult.failure("Pojo", ValidationResult.ValidationType.CARDINALITY, "Pojo", path, "", res.getError());
                }
                return ValidationResult.success("Pojo", ValidationResult.ValidationType.CARDINALITY, "Pojo", path, "");
            })
            .collect(Collectors.toList());
    }
}
