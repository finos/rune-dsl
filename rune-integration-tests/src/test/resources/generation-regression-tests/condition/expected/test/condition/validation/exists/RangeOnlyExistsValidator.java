package test.condition.validation.exists;

import com.google.common.collect.ImmutableMap;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ExistenceChecker;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.ValidatorWithArg;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import test.condition.Range;

import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;

public class RangeOnlyExistsValidator implements ValidatorWithArg<Range, Set<String>> {

    /* Casting is required to ensure types are output to ensure recompilation in Rosetta */
    @Override
    public <T2 extends Range> ValidationResult<Range> validate(RosettaPath path, T2 o, Set<String> fields) {
        Map<String, Boolean> fieldExistenceMap = ImmutableMap.<String, Boolean>builder()
            .put("quantity", ExistenceChecker.isSet((Integer) o.getQuantity()))
            .build();

        // Find the fields that are set
        Set<String> setFields = fieldExistenceMap.entrySet().stream()
            .filter(Map.Entry::getValue)
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());

        if (setFields.equals(fields)) {
            return success("Range", ValidationResult.ValidationType.ONLY_EXISTS, "Range", path, "");
        }
        return failure("Range", ValidationResult.ValidationType.ONLY_EXISTS, "Range", path, "",
            String.format("[%s] should only be set.  Set fields: %s", fields, setFields));
    }
}
