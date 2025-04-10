package com.regnosys.rosetta.tests.generator;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import com.regnosys.rosetta.generator.external.ExternalGenerator;
import com.regnosys.rosetta.generator.external.ExternalOutputConfiguration;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.rosetta.util.DemandableLock;

public class FailingGenerator implements ExternalGenerator {
    @Override
    public void beforeAllGenerate(ResourceSet set, Collection<? extends RosettaModel> models, String version,
            Consumer<Map<String, ? extends CharSequence>> processResults, DemandableLock generateLock) {
        // TODO Auto-generated method stub

    }

    @Override
    public void beforeGenerate(Resource resource, RosettaModel model, String version,
            Consumer<Map<String, ? extends CharSequence>> processResults, DemandableLock generateLock) {
        // TODO Auto-generated method stub

    }

    @Override
    public void generate(Resource resource, RosettaModel model, String version,
            Consumer<Map<String, ? extends CharSequence>> processResults, DemandableLock generateLock) {
        throw new RuntimeException("Test generator failure");
    }

    @Override
    public void afterGenerate(Resource resource, RosettaModel model, String version,
            Consumer<Map<String, ? extends CharSequence>> processResults, DemandableLock generateLock) {
        // TODO Auto-generated method stub

    }

    @Override
    public void afterAllGenerate(ResourceSet set, Collection<? extends RosettaModel> models, String version,
            Consumer<Map<String, ? extends CharSequence>> processResults, DemandableLock generateLock) {
        // TODO Auto-generated method stub

    }

    @Override
    public ExternalOutputConfiguration getOutputConfiguration() {
        // TODO Auto-generated method stub
        return null;
    }

}
