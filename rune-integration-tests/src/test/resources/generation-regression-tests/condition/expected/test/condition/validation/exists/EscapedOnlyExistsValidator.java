package test.condition.validation.exists;

import com.google.common.collect.ImmutableMap;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ExistenceChecker;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.ValidatorWithArg;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import test.condition.Escaped;

import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;

public class EscapedOnlyExistsValidator implements ValidatorWithArg<Escaped, Set<String>> {

    /* Casting is required to ensure types are output to ensure recompilation in Rosetta */
    @Override
    public <T2 extends Escaped> ValidationResult<Escaped> validate(RosettaPath path, T2 o, Set<String> fields) {
        Map<String, Boolean> fieldExistenceMap = ImmutableMap.<String, Boolean>builder()
            .put("val", ExistenceChecker.isSet((String) o.getVal()))
            .build();

        // Find the fields that are set
        Set<String> setFields = fieldExistenceMap.entrySet().stream()
            .filter(Map.Entry::getValue)
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());

        if (setFields.equals(fields)) {
            return success("Escaped", ValidationResult.ValidationType.ONLY_EXISTS, "Escaped", path, "");
        }
        return failure("Escaped", ValidationResult.ValidationType.ONLY_EXISTS, "Escaped", path, "",
            String.format("[%s] should only be set.  Set fields: %s", fields, setFields));
    }
}
