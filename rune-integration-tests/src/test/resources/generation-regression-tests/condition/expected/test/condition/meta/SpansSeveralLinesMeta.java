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
import test.condition.SpansSeveralLines;
import test.condition.validation.SpansSeveralLinesTypeFormatValidator;
import test.condition.validation.SpansSeveralLinesValidator;
import test.condition.validation.datarule.SpansSeveralLinesNotAMultiLineText;
import test.condition.validation.exists.SpansSeveralLinesOnlyExistsValidator;


/**
 * @version 0.0.0
 */
@RosettaMeta(model=SpansSeveralLines.class)
public class SpansSeveralLinesMeta implements RosettaMetaData<SpansSeveralLines> {

	@Override
	public List<Validator<? super SpansSeveralLines>> dataRules(ValidatorFactory factory) {
		return Arrays.asList(
			factory.<SpansSeveralLines>create(SpansSeveralLinesNotAMultiLineText.class)
		);
	}
	
	@Override
	public List<Function<? super SpansSeveralLines, QualifyResult>> getQualifyFunctions(QualifyFunctionFactory factory) {
		return Collections.emptyList();
	}
	
	@Override
	public Validator<? super SpansSeveralLines> validator(ValidatorFactory factory) {
		return factory.<SpansSeveralLines>create(SpansSeveralLinesValidator.class);
	}

	@Override
	public Validator<? super SpansSeveralLines> typeFormatValidator(ValidatorFactory factory) {
		return factory.<SpansSeveralLines>create(SpansSeveralLinesTypeFormatValidator.class);
	}

	@Override
	public ValidatorWithArg<? super SpansSeveralLines, Set<String>> onlyExistsValidator() {
		return new SpansSeveralLinesOnlyExistsValidator();
	}
}
