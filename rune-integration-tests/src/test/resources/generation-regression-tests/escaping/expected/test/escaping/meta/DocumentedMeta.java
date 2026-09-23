package test.escaping.meta;

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
import test.escaping.Documented;
import test.escaping.validation.DocumentedTypeFormatValidator;
import test.escaping.validation.DocumentedValidator;
import test.escaping.validation.exists.DocumentedOnlyExistsValidator;


/**
 * @version 1.0 "beta" C:&#92;users
 */
@RosettaMeta(model=Documented.class)
public class DocumentedMeta implements RosettaMetaData<Documented> {

	@Override
	public List<Validator<? super Documented>> dataRules(ValidatorFactory factory) {
		return Arrays.asList(
		);
	}
	
	@Override
	public List<Function<? super Documented, QualifyResult>> getQualifyFunctions(QualifyFunctionFactory factory) {
		return Collections.emptyList();
	}
	
	@Override
	public Validator<? super Documented> validator(ValidatorFactory factory) {
		return factory.<Documented>create(DocumentedValidator.class);
	}

	@Override
	public Validator<? super Documented> typeFormatValidator(ValidatorFactory factory) {
		return factory.<Documented>create(DocumentedTypeFormatValidator.class);
	}

	@Override
	public ValidatorWithArg<? super Documented, Set<String>> onlyExistsValidator() {
		return new DocumentedOnlyExistsValidator();
	}
}
