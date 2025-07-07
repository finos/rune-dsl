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
import test.pojo.Child;
import test.pojo.Foo3;
import test.pojo.metafields.ReferenceWithMetaGrandChild;

import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;

public class Foo3OnlyExistsValidator implements ValidatorWithArg<Foo3, Set<String>> {

	/* Casting is required to ensure types are output to ensure recompilation in Rosetta */
	@Override
	public <T2 extends Foo3> ValidationResult<Foo3> validate(RosettaPath path, T2 o, Set<String> fields) {
		Map<String, Boolean> fieldExistenceMap = ImmutableMap.<String, Boolean>builder()
				.put("attr", ExistenceChecker.isSet((Integer) o.getAttr()))
				.put("numberAttr", ExistenceChecker.isSet((Integer) o.getNumberAttrOverriddenAsInteger()))
				.put("parent", ExistenceChecker.isSet((Child) o.getParent()))
				.put("parentList", ExistenceChecker.isSet((ReferenceWithMetaGrandChild) o.getParentListOverriddenAsReferenceWithMetaGrandChild()))
				.put("otherParentList", ExistenceChecker.isSet((List<? extends Child>) o.getOtherParentList()))
				.put("stringAttr", ExistenceChecker.isSet((FieldWithMetaString) o.getStringAttr()))
				.build();
		
		// Find the fields that are set
		Set<String> setFields = fieldExistenceMap.entrySet().stream()
				.filter(Map.Entry::getValue)
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());
		
		if (setFields.equals(fields)) {
			return success("Foo3", ValidationResult.ValidationType.ONLY_EXISTS, "Foo3", path, "");
		}
		return failure("Foo3", ValidationResult.ValidationType.ONLY_EXISTS, "Foo3", path, "",
				String.format("[%s] should only be set.  Set fields: %s", fields, setFields));
	}
}
