package test.pojo.validation.exists;

import com.google.common.collect.ImmutableMap;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ExistenceChecker;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.ValidatorWithArg;
import com.rosetta.model.metafields.FieldWithMetaString;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import test.pojo.Level2;
import test.pojo.metafields.ReferenceWithMetaChild;

import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;

public class Level2OnlyExistsValidator implements ValidatorWithArg<Level2, Set<String>> {

    /* Casting is required to ensure types are output to ensure recompilation in Rosetta */
    @Override
    public <T2 extends Level2> ValidationResult<Level2> validate(RosettaPath path, T2 o, Set<String> fields) {
        Map<String, Boolean> fieldExistenceMap = ImmutableMap.<String, Boolean>builder()
            .put("attr", ExistenceChecker.isSet((Integer) o.getAttr()))
            .put("metaSingle", ExistenceChecker.isSet((FieldWithMetaString) o.getMetaSingle()))
            .put("otherMetaList", ExistenceChecker.isSet((List<? extends FieldWithMetaString>) o.getOtherMetaList()))
            .put("singleParent", ExistenceChecker.isSet((ReferenceWithMetaChild) o.getSingleParentOverriddenAsReferenceWithMetaChild()))
            .put("metaList", ExistenceChecker.isSet((FieldWithMetaString) o.getMetaListOverriddenAsSingle()))
            .build();

        // Find the fields that are set
        Set<String> setFields = fieldExistenceMap.entrySet().stream()
            .filter(Map.Entry::getValue)
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());

        if (setFields.equals(fields)) {
            return success("Level2", ValidationResult.ValidationType.ONLY_EXISTS, "Level2", path, "");
        }
        return failure("Level2", ValidationResult.ValidationType.ONLY_EXISTS, "Level2", path, "",
            String.format("[%s] should only be set.  Set fields: %s", fields, setFields));
    }
}
