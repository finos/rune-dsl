package test.condition.meta;

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
import test.condition.Simple;
import test.condition.validation.SimpleTypeFormatValidator;
import test.condition.validation.SimpleValidator;
import test.condition.validation.datarule.SimpleNotForbidden;
import test.condition.validation.exists.SimpleOnlyExistsValidator;


/**
 * @version 0.0.0
 */
@RosettaMeta(model=Simple.class)
public class SimpleMeta implements RosettaMetaData<Simple> {

	@Override
	public List<Validator<? super Simple>> dataRules(ValidatorFactory factory) {
		return Arrays.asList(
			factory.<Simple>create(SimpleNotForbidden.class)
		);
	}
	
	@Override
	public List<Function<? super Simple, QualifyResult>> getQualifyFunctions(QualifyFunctionFactory factory) {
		return Collections.emptyList();
	}
	
	@Override
	public Validator<? super Simple> validator(ValidatorFactory factory) {
		return factory.<Simple>create(SimpleValidator.class);
	}

	@Override
	public Validator<? super Simple> typeFormatValidator(ValidatorFactory factory) {
		return factory.<Simple>create(SimpleTypeFormatValidator.class);
	}

	@Override
	public ValidatorWithArg<? super Simple, Set<String>> onlyExistsValidator() {
		return new SimpleOnlyExistsValidator();
	}
}
