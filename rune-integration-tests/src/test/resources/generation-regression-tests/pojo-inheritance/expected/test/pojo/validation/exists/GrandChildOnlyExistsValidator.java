package test.pojo.validation.exists;

import com.google.common.collect.ImmutableMap;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.ValidatorWithArg;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import test.pojo.GrandChild;

import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;

public class GrandChildOnlyExistsValidator implements ValidatorWithArg<GrandChild, Set<String>> {

	/* Casting is required to ensure types are output to ensure recompilation in Rosetta */
	@Override
	public <T2 extends GrandChild> ValidationResult<GrandChild> validate(RosettaPath path, T2 o, Set<String> fields) {
		Map<String, Boolean> fieldExistenceMap = ImmutableMap.<String, Boolean>builder()
				.build();
		
		// Find the fields that are set
		Set<String> setFields = fieldExistenceMap.entrySet().stream()
				.filter(Map.Entry::getValue)
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());
		
		if (setFields.equals(fields)) {
			return success("GrandChild", ValidationResult.ValidationType.ONLY_EXISTS, "GrandChild", path, "");
		}
		return failure("GrandChild", ValidationResult.ValidationType.ONLY_EXISTS, "GrandChild", path, "",
				String.format("[%s] should only be set.  Set fields: %s", fields, setFields));
	}
}
