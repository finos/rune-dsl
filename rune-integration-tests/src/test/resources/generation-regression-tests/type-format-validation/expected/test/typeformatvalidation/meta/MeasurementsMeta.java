package test.typeformatvalidation.meta;

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
import test.typeformatvalidation.Measurements;
import test.typeformatvalidation.validation.MeasurementsTypeFormatValidator;
import test.typeformatvalidation.validation.MeasurementsValidator;
import test.typeformatvalidation.validation.exists.MeasurementsOnlyExistsValidator;


/**
 * @version 0.0.0
 */
@RosettaMeta(model=Measurements.class)
public class MeasurementsMeta implements RosettaMetaData<Measurements> {

    @Override
    public List<Validator<? super Measurements>> dataRules(ValidatorFactory factory) {
        return Arrays.asList(
        );
    }

    @Override
    public List<Function<? super Measurements, QualifyResult>> getQualifyFunctions(QualifyFunctionFactory factory) {
        return Collections.emptyList();
    }

    @Override
    public Validator<? super Measurements> validator(ValidatorFactory factory) {
        return factory.<Measurements>create(MeasurementsValidator.class);
    }

    @Override
    public Validator<? super Measurements> typeFormatValidator(ValidatorFactory factory) {
        return factory.<Measurements>create(MeasurementsTypeFormatValidator.class);
    }

    @Override
    public ValidatorWithArg<? super Measurements, Set<String>> onlyExistsValidator() {
        return new MeasurementsOnlyExistsValidator();
    }
}
