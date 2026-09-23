package test.typeformatvalidation.validation.exists;

import com.google.common.collect.ImmutableMap;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ExistenceChecker;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.ValidatorWithArg;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import test.typeformatvalidation.Measurements;

import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;

public class MeasurementsOnlyExistsValidator implements ValidatorWithArg<Measurements, Set<String>> {

    /* Casting is required to ensure types are output to ensure recompilation in Rosetta */
    @Override
    public <T2 extends Measurements> ValidationResult<Measurements> validate(RosettaPath path, T2 o, Set<String> fields) {
        Map<String, Boolean> fieldExistenceMap = ImmutableMap.<String, Boolean>builder()
            .put("amount", ExistenceChecker.isSet((Integer) o.getAmount()))
            .put("note", ExistenceChecker.isSet((String) o.getNote()))
            .put("amounts", ExistenceChecker.isSet((List<Integer>) o.getAmounts()))
            .build();

        // Find the fields that are set
        Set<String> setFields = fieldExistenceMap.entrySet().stream()
            .filter(Map.Entry::getValue)
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());

        if (setFields.equals(fields)) {
            return success("Measurements", ValidationResult.ValidationType.ONLY_EXISTS, "Measurements", path, "");
        }
        return failure("Measurements", ValidationResult.ValidationType.ONLY_EXISTS, "Measurements", path, "",
            String.format("[%s] should only be set.  Set fields: %s", fields, setFields));
    }
}
