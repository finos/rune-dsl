package test.escaping.getclass.validation.exists;

import com.google.common.collect.ImmutableMap;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ExistenceChecker;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.ValidatorWithArg;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import test.escaping.getclass.GetClassEscaping;

import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;

public class GetClassEscapingOnlyExistsValidator implements ValidatorWithArg<GetClassEscaping, Set<String>> {

	/* Casting is required to ensure types are output to ensure recompilation in Rosetta */
	@Override
	public <T2 extends GetClassEscaping> ValidationResult<GetClassEscaping> validate(RosettaPath path, T2 o, Set<String> fields) {
		Map<String, Boolean> fieldExistenceMap = ImmutableMap.<String, Boolean>builder()
				.put("class", ExistenceChecker.isSet((Integer) o._getClass()))
				.build();
		
		// Find the fields that are set
		Set<String> setFields = fieldExistenceMap.entrySet().stream()
				.filter(Map.Entry::getValue)
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());
		
		if (setFields.equals(fields)) {
			return success("GetClassEscaping", ValidationResult.ValidationType.ONLY_EXISTS, "GetClassEscaping", path, "");
		}
		return failure("GetClassEscaping", ValidationResult.ValidationType.ONLY_EXISTS, "GetClassEscaping", path, "",
				String.format("[%s] should only be set.  Set fields: %s", fields, setFields));
	}
}
