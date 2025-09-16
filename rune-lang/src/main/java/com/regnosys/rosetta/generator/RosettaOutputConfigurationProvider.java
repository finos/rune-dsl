package com.regnosys.rosetta.generator;

import com.regnosys.rosetta.generator.external.ExternalGenerator;
import com.regnosys.rosetta.generator.external.ExternalGenerators;
import com.regnosys.rosetta.generator.external.ExternalOutputConfiguration;
import jakarta.inject.Inject;
import org.eclipse.xtext.generator.IFileSystemAccess;
import org.eclipse.xtext.generator.OutputConfiguration;
import org.eclipse.xtext.generator.OutputConfigurationProvider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RosettaOutputConfigurationProvider extends OutputConfigurationProvider {

    @Inject
    private ExternalGenerators externalGeneratorsProvider;

    public static final String SRC_GEN_JAVA_OUTPUT = IFileSystemAccess.DEFAULT_OUTPUT;
    public static final String SRC_TEST_GEN_JAVA_OUTPUT = "SRC_TEST_GEN_JAVA_OUTPUT";

    @Override
    public Set<OutputConfiguration> getOutputConfigurations() {
        Set<OutputConfiguration> result = new HashSet<>(getOutConfigMap().values());
        // Inflate external outputs and add them
        for (ExternalGenerator gen : externalGeneratorsProvider) {
            ExternalOutputConfiguration ext = gen.getOutputConfiguration();
            if (ext != null) {
                result.add(inflate(ext));
            }
        }
        return result;
    }

    public Map<String, OutputConfiguration> getOutConfigMap() {
        OutputConfiguration srcGenJava = new OutputConfiguration(SRC_GEN_JAVA_OUTPUT);
        srcGenJava.setOutputDirectory("./src/generated/java");
        srcGenJava.setDescription("Generated Java Output Folder");
        srcGenJava.setOverrideExistingResources(true);
        srcGenJava.setCanClearOutputDirectory(true);
        srcGenJava.setCreateOutputDirectory(true);
        srcGenJava.setCleanUpDerivedResources(true);
        srcGenJava.setSetDerivedProperty(true);
        srcGenJava.setKeepLocalHistory(true);

        OutputConfiguration srcTestJava = new OutputConfiguration(SRC_TEST_GEN_JAVA_OUTPUT);
        srcTestJava.setOutputDirectory("./src/test/generated/java");
        srcTestJava.setDescription("Java Tests Output Folder");
        srcTestJava.setOverrideExistingResources(true);
        srcTestJava.setCanClearOutputDirectory(true);
        srcTestJava.setCreateOutputDirectory(true);
        srcTestJava.setCleanUpDerivedResources(true);
        srcTestJava.setSetDerivedProperty(true);
        srcTestJava.setKeepLocalHistory(false);

        Map<String, OutputConfiguration> result = new HashMap<>();
        result.put(SRC_GEN_JAVA_OUTPUT, srcGenJava);
        result.put(SRC_TEST_GEN_JAVA_OUTPUT, srcTestJava);
        return result;
    }

    private OutputConfiguration inflate(ExternalOutputConfiguration minimalConfig) {
        OutputConfiguration config = new OutputConfiguration(minimalConfig.getName());
        config.setOutputDirectory("./src/generated/" + minimalConfig.getDirectory());
        config.setDescription(minimalConfig.getDescription());
        config.setOverrideExistingResources(true);
        config.setCanClearOutputDirectory(true);
        config.setCreateOutputDirectory(true);
        config.setCleanUpDerivedResources(true);
        config.setSetDerivedProperty(true);
        config.setKeepLocalHistory(true);
        return config;
    }
}