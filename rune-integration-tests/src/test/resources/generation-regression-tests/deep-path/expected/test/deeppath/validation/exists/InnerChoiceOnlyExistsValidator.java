package test.deeppath.validation.exists;

import com.google.common.collect.ImmutableMap;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ExistenceChecker;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.ValidatorWithArg;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import test.deeppath.InnerChoice;
import test.deeppath.Option1;
import test.deeppath.Option2;

import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;

public class InnerChoiceOnlyExistsValidator implements ValidatorWithArg<InnerChoice, Set<String>> {

	/* Casting is required to ensure types are output to ensure recompilation in Rosetta */
	@Override
	public <T2 extends InnerChoice> ValidationResult<InnerChoice> validate(RosettaPath path, T2 o, Set<String> fields) {
		Map<String, Boolean> fieldExistenceMap = ImmutableMap.<String, Boolean>builder()
				.put("Option1", ExistenceChecker.isSet((Option1) o.getOption1()))
				.put("Option2", ExistenceChecker.isSet((Option2) o.getOption2()))
				.build();
		
		// Find the fields that are set
		Set<String> setFields = fieldExistenceMap.entrySet().stream()
				.filter(Map.Entry::getValue)
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());
		
		if (setFields.equals(fields)) {
			return success("InnerChoice", ValidationResult.ValidationType.ONLY_EXISTS, "InnerChoice", path, "");
		}
		return failure("InnerChoice", ValidationResult.ValidationType.ONLY_EXISTS, "InnerChoice", path, "",
				String.format("[%s] should only be set.  Set fields: %s", fields, setFields));
	}
}
