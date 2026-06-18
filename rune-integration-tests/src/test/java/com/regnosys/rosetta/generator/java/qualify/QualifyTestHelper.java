package com.regnosys.rosetta.generator.java.qualify;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.functions.ConditionValidator;
import com.rosetta.model.lib.functions.DefaultConditionValidator;
import com.rosetta.model.lib.functions.ModelObjectValidator;
import com.rosetta.model.lib.functions.NoOpModelObjectValidator;
import com.rosetta.model.lib.meta.RosettaMetaDataBuilder;
import com.rosetta.model.lib.qualify.QualifyFunctionFactory;
import com.rosetta.model.lib.qualify.QualifyResult;
import com.rosetta.model.lib.qualify.QualifyResultsExtractor;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class QualifyTestHelper {

    private final QualifyFunctionFactory funcFactory;

    public QualifyTestHelper() {
        // don't use the Language Injector. This is the Test env for the model.
        funcFactory = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(ConditionValidator.class).toInstance(new DefaultConditionValidator());
                bind(ModelObjectValidator.class).toInstance(new NoOpModelObjectValidator());
            }
        }).getInstance(QualifyFunctionFactory.Default.class);
    }

    public QualifyResult getQualifyResult(List<QualifyResult> results, String isEventName) {
        List<QualifyResult> resultsMatchingIsEventName = results.stream()
                .filter(result -> result.getName().equals(isEventName))
                .toList();
        assertThat("Expected single isEvent function with name " + isEventName, resultsMatchingIsEventName.size(), is(1));
        return resultsMatchingIsEventName.get(0);
    }

    public List<QualifyResult> createUtilAndGetAllResults(RosettaModelObject model) {
        QualifyResultsExtractor<RosettaModelObject> util = new QualifyResultsExtractor<>(
                RosettaMetaDataBuilder.getMetaData(model).getQualifyFunctions(funcFactory),
                model);
        return util.getAllResults();
    }
}
