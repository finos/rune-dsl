package test.pojo.validation.exists;

import com.google.common.collect.ImmutableMap;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ExistenceChecker;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.ValidatorWithArg;
import com.rosetta.model.metafields.ReferenceWithMetaString;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import test.pojo.Baz;

import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;

public class BazOnlyExistsValidator implements ValidatorWithArg<Baz, Set<String>> {

	/* Casting is required to ensure types are output to ensure recompilation in Rosetta */
	@Override
	public <T2 extends Baz> ValidationResult<Baz> validate(RosettaPath path, T2 o, Set<String> fields) {
		Map<String, Boolean> fieldExistenceMap = ImmutableMap.<String, Boolean>builder()
				.put("baz", ExistenceChecker.isSet((ReferenceWithMetaString) o.getBaz()))
				.build();
		
		// Find the fields that are set
		Set<String> setFields = fieldExistenceMap.entrySet().stream()
				.filter(Map.Entry::getValue)
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());
		
		if (setFields.equals(fields)) {
			return success("Baz", ValidationResult.ValidationType.ONLY_EXISTS, "Baz", path, "");
		}
		return failure("Baz", ValidationResult.ValidationType.ONLY_EXISTS, "Baz", path, "",
				String.format("[%s] should only be set.  Set fields: %s", fields, setFields));
	}
}
