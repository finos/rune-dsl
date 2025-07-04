package test.escaping.gettype.meta;

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
import test.escaping.gettype.GetTypeEscaping;
import test.escaping.gettype.validation.GetTypeEscapingTypeFormatValidator;
import test.escaping.gettype.validation.GetTypeEscapingValidator;
import test.escaping.gettype.validation.exists.GetTypeEscapingOnlyExistsValidator;


/**
 * @version 0.0.0
 */
@RosettaMeta(model=GetTypeEscaping.class)
public class GetTypeEscapingMeta implements RosettaMetaData<GetTypeEscaping> {

	@Override
	public List<Validator<? super GetTypeEscaping>> dataRules(ValidatorFactory factory) {
		return Arrays.asList(
		);
	}
	
	@Override
	public List<Function<? super GetTypeEscaping, QualifyResult>> getQualifyFunctions(QualifyFunctionFactory factory) {
		return Collections.emptyList();
	}
	
	@Override
	public Validator<? super GetTypeEscaping> validator(ValidatorFactory factory) {
		return factory.<GetTypeEscaping>create(GetTypeEscapingValidator.class);
	}

	@Override
	public Validator<? super GetTypeEscaping> typeFormatValidator(ValidatorFactory factory) {
		return factory.<GetTypeEscaping>create(GetTypeEscapingTypeFormatValidator.class);
	}

	@Deprecated
	@Override
	public Validator<? super GetTypeEscaping> validator() {
		return new GetTypeEscapingValidator();
	}

	@Deprecated
	@Override
	public Validator<? super GetTypeEscaping> typeFormatValidator() {
		return new GetTypeEscapingTypeFormatValidator();
	}
	
	@Override
	public ValidatorWithArg<? super GetTypeEscaping, Set<String>> onlyExistsValidator() {
		return new GetTypeEscapingOnlyExistsValidator();
	}
}
