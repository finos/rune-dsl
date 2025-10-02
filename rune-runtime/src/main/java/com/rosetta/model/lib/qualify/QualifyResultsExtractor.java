/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
