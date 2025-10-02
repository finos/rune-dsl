package test.pojo.validation.exists;

import com.google.common.collect.ImmutableMap;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ExistenceChecker;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.ValidatorWithArg;
import com.rosetta.model.metafields.FieldWithMetaString;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import test.pojo.Foo1;
import test.pojo.Parent;

import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;

public class Foo1OnlyExistsValidator implements ValidatorWithArg<Foo1, Set<String>> {

	/* Casting is required to ensure types are output to ensure recompilation in Rosetta */
	@Override
	public <T2 extends Foo1> ValidationResult<Foo1> validate(RosettaPath path, T2 o, Set<String> fields) {
		Map<String, Boolean> fieldExistenceMap = ImmutableMap.<String, Boolean>builder()
				.put("attr", ExistenceChecker.isSet((Integer) o.getAttr()))
				.put("numberAttr", ExistenceChecker.isSet((BigDecimal) o.getNumberAttr()))
				.put("parent", ExistenceChecker.isSet((Parent) o.getParent()))
				.put("parentList", ExistenceChecker.isSet((List<? extends Parent>) o.getParentList()))
				.put("otherParentList", ExistenceChecker.isSet((List<? extends Parent>) o.getOtherParentList()))
				.put("stringAttr", ExistenceChecker.isSet((FieldWithMetaString) o.getStringAttr()))
				.build();
		
		// Find the fields that are set
		Set<String> setFields = fieldExistenceMap.entrySet().stream()
				.filter(Map.Entry::getValue)
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());
		
		if (setFields.equals(fields)) {
			return success("Foo1", ValidationResult.ValidationType.ONLY_EXISTS, "Foo1", path, "");
		}
		return failure("Foo1", ValidationResult.ValidationType.ONLY_EXISTS, "Foo1", path, "",
				String.format("[%s] should only be set.  Set fields: %s", fields, setFields));
	}
}
