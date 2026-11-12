package test.escaping.javalang.meta;

import com.rosetta.model.lib.annotations.RosettaMeta;
import com.rosetta.model.lib.meta.RosettaMetaData;
import com.rosetta.model.lib.qualify.QualifyFunctionFactory;
import com.rosetta.model.lib.qualify.QualifyResult;
import com.rosetta.model.lib.validation.Validator;
import com.rosetta.model.lib.validation.ValidatorFactory;
import com.rosetta.model.lib.validation.ValidatorWithArg;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import test.escaping.javalang.validation.ObjectTypeFormatValidator;
import test.escaping.javalang.validation.ObjectValidator;
import test.escaping.javalang.validation.exists.ObjectOnlyExistsValidator;


/**
 * @version 0.0.0
 */
@RosettaMeta(model=test.escaping.javalang.Object.class)
public class ObjectMeta implements RosettaMetaData<test.escaping.javalang.Object> {

    @Override
    public List<Validator<? super test.escaping.javalang.Object>> dataRules(ValidatorFactory factory) {
        return Arrays.asList(
        );
    }

    @Override
    public List<Function<? super test.escaping.javalang.Object, QualifyResult>> getQualifyFunctions(QualifyFunctionFactory factory) {
        return Collections.emptyList();
    }

    @Override
    public Validator<? super test.escaping.javalang.Object> validator(ValidatorFactory factory) {
        return factory.<test.escaping.javalang.Object>create(ObjectValidator.class);
    }

    @Override
    public Validator<? super test.escaping.javalang.Object> typeFormatValidator(ValidatorFactory factory) {
        return factory.<test.escaping.javalang.Object>create(ObjectTypeFormatValidator.class);
    }

    @Override
    public ValidatorWithArg<? super test.escaping.javalang.Object, Set<String>> onlyExistsValidator() {
        return new ObjectOnlyExistsValidator();
    }
}
