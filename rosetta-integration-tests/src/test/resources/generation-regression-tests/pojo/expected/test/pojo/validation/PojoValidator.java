package test.pojo.validation;

import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import com.rosetta.model.metafields.FieldWithMetaString;
import java.util.List;
import test.pojo.Foo;
import test.pojo.Pojo;
import test.pojo.metafields.ReferenceWithMetaFoo;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.rosetta.model.lib.expression.ExpressionOperators.checkCardinality;
import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;
import static java.util.stream.Collectors.toList;

public class PojoValidator implements Validator<Pojo> {

	private List<ComparisonResult> getComparisonResults(Pojo o) {
		return Lists.<ComparisonResult>newArrayList(
				checkCardinality("simpleAttr", (String) o.getSimpleAttr() != null ? 1 : 0, 1, 1), 
				checkCardinality("simpleAttrWithMeta", (FieldWithMetaString) o.getSimpleAttrWithMeta() != null ? 1 : 0, 1, 1), 
				checkCardinality("simpleAttrWithId", (FieldWithMetaString) o.getSimpleAttrWithId() != null ? 1 : 0, 1, 1), 
				checkCardinality("complexAttr", (Foo) o.getComplexAttr() != null ? 1 : 0, 1, 1), 
				checkCardinality("complexAttrWithRef", (ReferenceWithMetaFoo) o.getComplexAttrWithRef() != null ? 1 : 0, 1, 1)
			);
	}

	@Override
	public List<ValidationResult<?>> getValidationResults(RosettaPath path, Pojo o) {
		return getComparisonResults(o)
			.stream()
			.map(res -> {
				if (!isNullOrEmpty(res.getError())) {
					return failure("Pojo", ValidationResult.ValidationType.CARDINALITY, "Pojo", path, "", res.getError());
				}
				return success("Pojo", ValidationResult.ValidationType.CARDINALITY, "Pojo", path, "");
			})
			.collect(toList());
	}

}
