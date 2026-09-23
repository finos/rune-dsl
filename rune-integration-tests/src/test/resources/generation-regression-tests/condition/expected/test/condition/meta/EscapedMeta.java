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
import test.condition.Escaped;
import test.condition.validation.EscapedTypeFormatValidator;
import test.condition.validation.EscapedValidator;
import test.condition.validation.datarule.EscapedNeedsEscaping;
import test.condition.validation.exists.EscapedOnlyExistsValidator;


/**
 * @version 0.0.0
 */
@RosettaMeta(model=Escaped.class)
public class EscapedMeta implements RosettaMetaData<Escaped> {

	@Override
	public List<Validator<? super Escaped>> dataRules(ValidatorFactory factory) {
		return Arrays.asList(
			factory.<Escaped>create(EscapedNeedsEscaping.class)
		);
	}
	
	@Override
	public List<Function<? super Escaped, QualifyResult>> getQualifyFunctions(QualifyFunctionFactory factory) {
		return Collections.emptyList();
	}
	
	@Override
	public Validator<? super Escaped> validator(ValidatorFactory factory) {
		return factory.<Escaped>create(EscapedValidator.class);
	}

	@Override
	public Validator<? super Escaped> typeFormatValidator(ValidatorFactory factory) {
		return factory.<Escaped>create(EscapedTypeFormatValidator.class);
	}

	@Override
	public ValidatorWithArg<? super Escaped, Set<String>> onlyExistsValidator() {
		return new EscapedOnlyExistsValidator();
	}
}
