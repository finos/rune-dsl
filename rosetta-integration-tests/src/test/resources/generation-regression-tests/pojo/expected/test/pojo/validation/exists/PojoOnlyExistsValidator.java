package test.pojo.validation.exists;

import com.google.common.collect.ImmutableMap;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ExistenceChecker;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.ValidatorWithArg;
import com.rosetta.model.metafields.FieldWithMetaString;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import test.pojo.Foo;
import test.pojo.Pojo;
import test.pojo.metafields.ReferenceWithMetaFoo;

import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;

public class PojoOnlyExistsValidator implements ValidatorWithArg<Pojo, Set<String>> {

	/* Casting is required to ensure types are output to ensure recompilation in Rosetta */
	@Override
	public <T2 extends Pojo> ValidationResult<Pojo> validate(RosettaPath path, T2 o, Set<String> fields) {
		Map<String, Boolean> fieldExistenceMap = ImmutableMap.<String, Boolean>builder()
				.put("simpleAttr", ExistenceChecker.isSet((String) o.getSimpleAttr()))
				.put("multiSimpleAttr", ExistenceChecker.isSet((List<String>) o.getMultiSimpleAttr()))
				.put("simpleAttrWithMeta", ExistenceChecker.isSet((FieldWithMetaString) o.getSimpleAttrWithMeta()))
				.put("multiSimpleAttrWithMeta", ExistenceChecker.isSet((List<? extends FieldWithMetaString>) o.getMultiSimpleAttrWithMeta()))
				.put("simpleAttrWithId", ExistenceChecker.isSet((FieldWithMetaString) o.getSimpleAttrWithId()))
				.put("multiSimpleAttrWithId", ExistenceChecker.isSet((List<? extends FieldWithMetaString>) o.getMultiSimpleAttrWithId()))
				.put("complexAttr", ExistenceChecker.isSet((Foo) o.getComplexAttr()))
				.put("multiComplexAttr", ExistenceChecker.isSet((List<? extends Foo>) o.getMultiComplexAttr()))
				.put("complexAttrWithRef", ExistenceChecker.isSet((ReferenceWithMetaFoo) o.getComplexAttrWithRef()))
				.put("multiComplexAttrWithRef", ExistenceChecker.isSet((List<? extends ReferenceWithMetaFoo>) o.getMultiComplexAttrWithRef()))
				.build();
		
		// Find the fields that are set
		Set<String> setFields = fieldExistenceMap.entrySet().stream()
				.filter(Map.Entry::getValue)
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());
		
		if (setFields.equals(fields)) {
			return success("Pojo", ValidationResult.ValidationType.ONLY_EXISTS, "Pojo", path, "");
		}
		return failure("Pojo", ValidationResult.ValidationType.ONLY_EXISTS, "Pojo", path, "",
				String.format("[%s] should only be set.  Set fields: %s", fields, setFields));
	}
}
