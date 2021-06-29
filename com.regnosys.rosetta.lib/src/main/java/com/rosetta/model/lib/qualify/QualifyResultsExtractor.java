package com.rosetta.model.lib.qualify;

import com.rosetta.model.lib.RosettaModelObject;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class QualifyResultsExtractor<T extends RosettaModelObject> {

    private final List<Function<? super T, QualifyResult>> qualifyFunctions;
    private final T model;

    public QualifyResultsExtractor(List<Function<? super T, QualifyResult>> qualifyFunctions, T model) {
        this.qualifyFunctions = qualifyFunctions;
        this.model = model;
    }

    public T getModelObject() {
    	return model;
    }
    
    public List<QualifyResult> getAllResults() {
        List<QualifyResult> allResults = qualifyFunctions.stream().map(x -> x.apply(model)).collect(Collectors.toList());
        Collections.sort(allResults);
        return allResults;
    }

    public Optional<QualifyResult> getOnlySuccessResult() {
        List<QualifyResult> matchedResults = getAllResults().stream().filter(x -> x.isSuccess()).collect(Collectors.toList());
        return matchedResults.size() == 1 ? Optional.of(matchedResults.get(0)) : Optional.empty();
    }
}
