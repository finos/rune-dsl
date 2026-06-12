package test.deeppath.validation.exists;

import com.google.common.collect.ImmutableMap;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ExistenceChecker;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.ValidatorWithArg;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import test.deeppath.Leaf;

import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;

public class LeafOnlyExistsValidator implements ValidatorWithArg<Leaf, Set<String>> {

	/* Casting is required to ensure types are output to ensure recompilation in Rosetta */
	@Override
	public <T2 extends Leaf> ValidationResult<Leaf> validate(RosettaPath path, T2 o, Set<String> fields) {
		Map<String, Boolean> fieldExistenceMap = ImmutableMap.<String, Boolean>builder()
				.put("common", ExistenceChecker.isSet((String) o.getCommon()))
				.put("items", ExistenceChecker.isSet((List<String>) o.getItems()))
				.build();
		
		// Find the fields that are set
		Set<String> setFields = fieldExistenceMap.entrySet().stream()
				.filter(Map.Entry::getValue)
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());
		
		if (setFields.equals(fields)) {
			return success("Leaf", ValidationResult.ValidationType.ONLY_EXISTS, "Leaf", path, "");
		}
		return failure("Leaf", ValidationResult.ValidationType.ONLY_EXISTS, "Leaf", path, "",
				String.format("[%s] should only be set.  Set fields: %s", fields, setFields));
	}
}
