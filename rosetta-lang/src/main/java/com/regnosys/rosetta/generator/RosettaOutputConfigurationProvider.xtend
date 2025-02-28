package com.regnosys.rosetta.generator

import java.util.Set
import org.eclipse.xtext.generator.OutputConfiguration
import org.eclipse.xtext.generator.OutputConfigurationProvider
import org.eclipse.xtext.generator.IFileSystemAccess
import com.regnosys.rosetta.generator.external.ExternalGenerators
import com.regnosys.rosetta.generator.external.ExternalOutputConfiguration
import java.util.Map
import java.util.HashMap
import java.util.HashSet
import jakarta.inject.Inject

class RosettaOutputConfigurationProvider extends OutputConfigurationProvider {

	@Inject ExternalGenerators externalGeneratorsProvider

	public final static String SRC_GEN_JAVA_OUTPUT = IFileSystemAccess.DEFAULT_OUTPUT
	public final static String SRC_TEST_GEN_JAVA_OUTPUT = "SRC_TEST_GEN_JAVA_OUTPUT"
	public final static String SRC_MAIN_JAVA_OUTPUT = "SRC_MAIN_JAVA_OUTPUT"
	public final static String SRC_GEN_DAML_OUTPUT = "SRC_GEN_DAML_OUTPUT"

	override Set<OutputConfiguration> getOutputConfigurations() {
		val result = new HashSet(getOutConfigMap.values())
		externalGeneratorsProvider.map[outputConfiguration].map[inflate].forEach[result.add(it)]
		return result
	}
	
	def Map<String, OutputConfiguration> getOutConfigMap() {
		var srcGenJava = new OutputConfiguration(SRC_GEN_JAVA_OUTPUT)
		srcGenJava.setOutputDirectory("./src/generated/java")
		srcGenJava.setDescription("Generated Java Output Folder")
		srcGenJava.setOverrideExistingResources(true)
		srcGenJava.setCanClearOutputDirectory(true)
		srcGenJava.setCreateOutputDirectory(true)
		srcGenJava.setCleanUpDerivedResources(true)
		srcGenJava.setSetDerivedProperty(true)
		srcGenJava.setKeepLocalHistory(true)

		var srcMainJava = new OutputConfiguration(SRC_MAIN_JAVA_OUTPUT)
		srcMainJava.setOutputDirectory("./src/main/java")
		srcMainJava.setDescription("Java Main Output Folder")
		srcMainJava.setOverrideExistingResources(false)
		srcMainJava.setCanClearOutputDirectory(false)
		srcMainJava.setCreateOutputDirectory(false)
		srcMainJava.setCleanUpDerivedResources(false)
		srcMainJava.setSetDerivedProperty(false)
		srcMainJava.setKeepLocalHistory(false)
		
		var srcTestJava = new OutputConfiguration(SRC_TEST_GEN_JAVA_OUTPUT)
		srcTestJava.setOutputDirectory("./src/test/generated/java")
		srcTestJava.setDescription("Java Tests Output Folder")
		srcTestJava.setOverrideExistingResources(true)
		srcTestJava.setCanClearOutputDirectory(true)
		srcTestJava.setCreateOutputDirectory(true)
		srcTestJava.setCleanUpDerivedResources(true)
		srcTestJava.setSetDerivedProperty(true)
		srcTestJava.setKeepLocalHistory(false)

		var srcGenDaml = new OutputConfiguration(SRC_GEN_DAML_OUTPUT)
		srcGenDaml.setOutputDirectory("./src/generated/daml")
		srcGenDaml.setDescription("Generated DAML Output Folder")
		srcGenDaml.setOverrideExistingResources(true)
		srcGenDaml.setCanClearOutputDirectory(true)
		srcGenDaml.setCreateOutputDirectory(true)
		srcGenDaml.setCleanUpDerivedResources(true)
		srcGenDaml.setSetDerivedProperty(true)
		srcGenDaml.setKeepLocalHistory(true)
		
		val result = new HashMap
		result.put(SRC_GEN_JAVA_OUTPUT, srcGenJava)
		result.put(SRC_MAIN_JAVA_OUTPUT, srcMainJava)
		result.put(SRC_GEN_DAML_OUTPUT, srcGenDaml)
		result.put(SRC_TEST_GEN_JAVA_OUTPUT, srcTestJava)
		result
	}

	private def inflate(extension ExternalOutputConfiguration minimalConfig) {
		val config = new OutputConfiguration(getName)
		config.setOutputDirectory('./src/generated/' + directory)
		config.setDescription(getDescription)
		config.setOverrideExistingResources(true)
		config.setCanClearOutputDirectory(true)
		config.setCreateOutputDirectory(true)
		config.setCleanUpDerivedResources(true)
		config.setSetDerivedProperty(true)
		config.setKeepLocalHistory(true)

		return config
	}
}
