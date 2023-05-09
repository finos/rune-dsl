package com.regnosys.rosetta.generator

import com.google.inject.Inject
import com.regnosys.rosetta.generator.external.ExternalGenerators
import com.regnosys.rosetta.generator.java.blueprints.BlueprintGenerator
import com.regnosys.rosetta.generator.java.enums.EnumGenerator
import com.regnosys.rosetta.generator.java.object.JavaPackageInfoGenerator
import com.regnosys.rosetta.generator.java.object.MetaFieldGenerator
import com.regnosys.rosetta.generator.java.object.ModelMetaGenerator
import com.regnosys.rosetta.generator.java.object.ModelObjectGenerator
import com.regnosys.rosetta.generator.java.object.NamespaceHierarchyGenerator
import com.regnosys.rosetta.generator.java.object.ValidatorsGenerator
import com.regnosys.rosetta.generator.java.rule.DataRuleGenerator
import com.regnosys.rosetta.generator.java.util.ModelNamespaceUtil
import com.regnosys.rosetta.generator.resourcefsa.ResourceAwareFSAFactory
import com.regnosys.rosetta.generator.resourcefsa.TestResourceAwareFSAFactory.TestFolderAwareFsa
import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.simple.Function
import com.rosetta.util.DemandableLock
import java.util.Map
import java.util.concurrent.CancellationException
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.xtext.generator.AbstractGenerator
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext
import com.regnosys.rosetta.generator.java.function.FunctionGenerator
import com.regnosys.rosetta.generator.java.util.BackwardCompatibilityGenerator
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Generates code from your model files on save.
 * 
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#code-generation
 */
class RosettaGenerator extends AbstractGenerator {
	static Logger LOGGER = LoggerFactory.getLogger(RosettaGenerator)

	@Inject EnumGenerator enumGenerator
	@Inject ModelMetaGenerator metaGenerator
	@Inject DataRuleGenerator dataRuleGenerator
	@Inject BlueprintGenerator blueprintGenerator
	@Inject MetaFieldGenerator metaFieldGenerator
	@Inject ExternalGenerators externalGenerators
	@Inject JavaPackageInfoGenerator javaPackageInfoGenerator
	@Inject NamespaceHierarchyGenerator namespaceHierarchyGenerator

	@Inject ModelObjectGenerator dataGenerator
	@Inject ValidatorsGenerator validatorsGenerator
	@Inject extension RosettaFunctionExtensions
	@Inject FunctionGenerator funcGenerator
	@Inject BackwardCompatibilityGenerator backwardCompatibilityGenerator

	@Inject
	ResourceAwareFSAFactory fsaFactory;

	@Inject ModelNamespaceUtil modelNamespaceUtil

	// For files that are
	val ignoredFiles = #{'model-no-code-gen.rosetta'}

	val Map<ResourceSet, DemandableLock> locks = newHashMap
	
	override void beforeGenerate(Resource resource, IFileSystemAccess2 fsa2, IGeneratorContext context) {
		LOGGER.debug("Starting the main generate method for " + resource.URI.toString)
		fsaFactory.beforeGenerate(resource)
	}

	override void doGenerate(Resource resource, IFileSystemAccess2 fsa2, IGeneratorContext context) {
		LOGGER.trace("Starting the main generate method for " + resource.URI.toString)
		val fsa = fsaFactory.resourceAwareFSA(resource, fsa2, false)
		val lock = locks.computeIfAbsent(resource.resourceSet, [new DemandableLock]);
		try {
			lock.getWriteLock(true);
			if (!ignoredFiles.contains(resource.URI.segments.last)) {
				// all models
				val models = if (resource.resourceSet?.resources === null) {
					LOGGER.warn("No resource set found for " + resource.URI.toString)
					newHashSet
				} else resource.resourceSet.resources.flatMap[contents].filter(RosettaModel).toSet
				
				// generate for each model object
				resource.contents.filter(RosettaModel).forEach [
					val version = version
					val packages = new RootPackage(it)

					elements.forEach [
						if (context.cancelIndicator.canceled) {
							return // throw exception instead
						}
						switch (it) {
							Data: {
								dataGenerator.generate(packages, fsa, it, version)
								metaGenerator.generate(packages, fsa, it, version, models)
								validatorsGenerator.generate(packages, fsa, it, version)
								it.conditions.forEach [ cond |
									dataRuleGenerator.generate(packages, fsa, it, cond, version)
								]
							}
							Function: {
								if (!isDispatchingFunction) {
									funcGenerator.generate(packages, fsa, it, version)
								}
							}
						}
					]
					enumGenerator.generate(packages, fsa, elements, version)
					blueprintGenerator.generate(packages, fsa, elements, version)

					// Invoke externally defined code generators
					externalGenerators.forEach [ generator |
						generator.generate(packages, elements, version, [ map |
							map.entrySet.forEach[fsa.generateFile(key, generator.outputConfiguration.getName, value)]
						], resource, lock)
					]
				]

				if (!resource.contents.filter(RosettaModel).isEmpty) {
					metaFieldGenerator.generate(resource, fsa, context)
				}
			}
		} catch (CancellationException e) {
			LOGGER.trace("Code generation cancelled, this is expected")
		} catch (Exception e) {
			LOGGER.warn(
				"Unexpected calling standard generate for rosetta -" + e.message + " - see debug logging for more")
			LOGGER.info("Unexpected calling standard generate for rosetta", e);
		} finally {
			LOGGER.debug("ending the main generate method")
			lock.releaseWriteLock
		}
	}

	override void afterGenerate(Resource resource, IFileSystemAccess2 fsa2, IGeneratorContext context) {
		try {
			val lock = locks.computeIfAbsent(resource.resourceSet, [new DemandableLock]);
			val fsa = fsaFactory.resourceAwareFSA(resource, fsa2, true)
			
			backwardCompatibilityGenerator.generate(fsa)
			
			val models = if (resource.resourceSet?.resources === null) {
							LOGGER.warn("No resource set found for " + resource.URI.toString)
							newArrayList
						} else resource.resourceSet.resources.flatMap[contents]
								.filter[!TestFolderAwareFsa.isTestResource(it.eResource)]
								.filter(RosettaModel).toList

			val namespaceDescriptionMap = modelNamespaceUtil.namespaceToDescriptionMap(models).asMap
			val namespaceUrilMap = modelNamespaceUtil.namespaceToModelUriMap(models).asMap
			
			javaPackageInfoGenerator.generatePackageInfoClasses(fsa, namespaceDescriptionMap)
			namespaceHierarchyGenerator.generateNamespacePackageHierarchy(fsa, namespaceDescriptionMap, namespaceUrilMap)
			
			externalGenerators.forEach [ generator |
				generator.afterGenerate(models, [ map |
					map.entrySet.forEach[fsa.generateFile(key, generator.outputConfiguration.getName, value)]
				], resource, lock)
			]
			fsaFactory.afterGenerate(resource)
		} catch (Exception e) {
			LOGGER.warn("Unexpected calling after generate for rosetta -" + e.message + " - see debug logging for more")
			LOGGER.debug("Unexpected calling after generate for rosetta", e);
		}

	}
}
