package test.pojo.meta;

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
import test.pojo.GrandChild;
import test.pojo.validation.GrandChildTypeFormatValidator;
import test.pojo.validation.GrandChildValidator;
import test.pojo.validation.exists.GrandChildOnlyExistsValidator;


/**
 * @version 0.0.0
 */
@RosettaMeta(model=GrandChild.class)
public class GrandChildMeta implements RosettaMetaData<GrandChild> {

	@Override
	public List<Validator<? super GrandChild>> dataRules(ValidatorFactory factory) {
		return Arrays.asList(
		);
	}
	
	@Override
	public List<Function<? super GrandChild, QualifyResult>> getQualifyFunctions(QualifyFunctionFactory factory) {
		return Collections.emptyList();
	}
	
	@Override
	public Validator<? super GrandChild> validator(ValidatorFactory factory) {
		return factory.<GrandChild>create(GrandChildValidator.class);
	}

	@Override
	public Validator<? super GrandChild> typeFormatValidator(ValidatorFactory factory) {
		return factory.<GrandChild>create(GrandChildTypeFormatValidator.class);
	}

	@Deprecated
	@Override
	public Validator<? super GrandChild> validator() {
		return new GrandChildValidator();
	}

	@Deprecated
	@Override
	public Validator<? super GrandChild> typeFormatValidator() {
		return new GrandChildTypeFormatValidator();
	}
	
	@Override
	public ValidatorWithArg<? super GrandChild, Set<String>> onlyExistsValidator() {
		return new GrandChildOnlyExistsValidator();
	}
}
