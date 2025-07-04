package test.escaping.getclass.meta;

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
import test.escaping.getclass.GetClassEscaping;
import test.escaping.getclass.validation.GetClassEscapingTypeFormatValidator;
import test.escaping.getclass.validation.GetClassEscapingValidator;
import test.escaping.getclass.validation.exists.GetClassEscapingOnlyExistsValidator;


/**
 * @version 0.0.0
 */
@RosettaMeta(model=GetClassEscaping.class)
public class GetClassEscapingMeta implements RosettaMetaData<GetClassEscaping> {

	@Override
	public List<Validator<? super GetClassEscaping>> dataRules(ValidatorFactory factory) {
		return Arrays.asList(
		);
	}
	
	@Override
	public List<Function<? super GetClassEscaping, QualifyResult>> getQualifyFunctions(QualifyFunctionFactory factory) {
		return Collections.emptyList();
	}
	
	@Override
	public Validator<? super GetClassEscaping> validator(ValidatorFactory factory) {
		return factory.<GetClassEscaping>create(GetClassEscapingValidator.class);
	}

	@Override
	public Validator<? super GetClassEscaping> typeFormatValidator(ValidatorFactory factory) {
		return factory.<GetClassEscaping>create(GetClassEscapingTypeFormatValidator.class);
	}

	@Deprecated
	@Override
	public Validator<? super GetClassEscaping> validator() {
		return new GetClassEscapingValidator();
	}

	@Deprecated
	@Override
	public Validator<? super GetClassEscaping> typeFormatValidator() {
		return new GetClassEscapingTypeFormatValidator();
	}
	
	@Override
	public ValidatorWithArg<? super GetClassEscaping, Set<String>> onlyExistsValidator() {
		return new GetClassEscapingOnlyExistsValidator();
	}
}
