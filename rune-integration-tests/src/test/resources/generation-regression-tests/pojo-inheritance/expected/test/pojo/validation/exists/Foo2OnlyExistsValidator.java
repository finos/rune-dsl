package test.pojo.validation.exists;

import com.google.common.collect.ImmutableMap;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ExistenceChecker;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.ValidatorWithArg;
import com.rosetta.model.metafields.FieldWithMetaString;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import test.pojo.Child;
import test.pojo.Foo2;
import test.pojo.metafields.ReferenceWithMetaChild;

import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;

public class Foo2OnlyExistsValidator implements ValidatorWithArg<Foo2, Set<String>> {

	/* Casting is required to ensure types are output to ensure recompilation in Rosetta */
	@Override
	public <T2 extends Foo2> ValidationResult<Foo2> validate(RosettaPath path, T2 o, Set<String> fields) {
		Map<String, Boolean> fieldExistenceMap = ImmutableMap.<String, Boolean>builder()
				.put("attr", ExistenceChecker.isSet((Integer) o.getAttr()))
				.put("numberAttr", ExistenceChecker.isSet((BigInteger) o.getNumberAttrOverriddenAsBigInteger()))
				.put("parent", ExistenceChecker.isSet((Child) o.getParent()))
				.put("parentList", ExistenceChecker.isSet((ReferenceWithMetaChild) o.getParentListOverriddenAsSingleReferenceWithMetaChild()))
				.put("otherParentList", ExistenceChecker.isSet((List<? extends Child>) o.getOtherParentList()))
				.put("stringAttr", ExistenceChecker.isSet((FieldWithMetaString) o.getStringAttr()))
				.build();
		
		// Find the fields that are set
		Set<String> setFields = fieldExistenceMap.entrySet().stream()
				.filter(Map.Entry::getValue)
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());
		
		if (setFields.equals(fields)) {
			return success("Foo2", ValidationResult.ValidationType.ONLY_EXISTS, "Foo2", path, "");
		}
		return failure("Foo2", ValidationResult.ValidationType.ONLY_EXISTS, "Foo2", path, "",
				String.format("[%s] should only be set.  Set fields: %s", fields, setFields));
	}
}
