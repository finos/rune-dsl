/*

 * generated by Xtext 2.10.0
 */
package com.regnosys.rosetta.generator

import com.regnosys.rosetta.generator.external.ExternalGenerators
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.regnosys.rosetta.generator.java.enums.EnumGenerator
import com.regnosys.rosetta.generator.java.function.FunctionGenerator
import com.regnosys.rosetta.generator.java.object.JavaPackageInfoGenerator
import com.regnosys.rosetta.generator.java.object.MetaFieldGenerator
import com.regnosys.rosetta.generator.java.object.ModelMetaGenerator
import com.regnosys.rosetta.generator.java.object.ModelObjectGenerator
import com.regnosys.rosetta.generator.java.object.ValidatorsGenerator
import com.regnosys.rosetta.generator.java.reports.TabulatorGenerator
import com.regnosys.rosetta.generator.resourcefsa.ResourceAwareFSAFactory
import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions
import com.regnosys.rosetta.rosetta.RosettaExternalRuleSource
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.simple.Function
import com.rosetta.util.DemandableLock
import java.util.Map
import java.util.Optional
import java.util.concurrent.CancellationException
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGenerator2
import org.eclipse.xtext.generator.IGeneratorContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.regnosys.rosetta.generator.java.reports.RuleGenerator
import com.regnosys.rosetta.generator.java.condition.ConditionGenerator
import com.regnosys.rosetta.generator.java.reports.ReportGenerator
import javax.inject.Inject
import com.regnosys.rosetta.rosetta.RosettaRule
import com.regnosys.rosetta.rosetta.RosettaReport
import com.regnosys.rosetta.generator.java.validator.ValidatorGenerator
import com.regnosys.rosetta.config.RosettaGeneratorsConfiguration
import com.regnosys.rosetta.generator.java.expression.DeepPathUtilGenerator
import com.regnosys.rosetta.utils.DeepFeatureCallUtil
import com.regnosys.rosetta.types.RDataType

/**
 * Generates code from your model files on save.
 * 
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#code-generation
 */
class RosettaGenerator implements IGenerator2 {
	static Logger LOGGER = LoggerFactory.getLogger(RosettaGenerator)

	@Inject EnumGenerator enumGenerator
	@Inject ModelMetaGenerator metaGenerator
	@Inject ConditionGenerator conditionGenerator
	@Inject TabulatorGenerator tabulatorGenerator
	@Inject MetaFieldGenerator metaFieldGenerator
	@Inject ExternalGenerators externalGenerators
	@Inject JavaPackageInfoGenerator javaPackageInfoGenerator
	@Inject RuleGenerator ruleGenerator

	@Inject ModelObjectGenerator dataGenerator
	@Inject ValidatorsGenerator validatorsGenerator
	@Inject ValidatorGenerator validatorGenerator
	@Inject extension RosettaFunctionExtensions
	@Inject FunctionGenerator funcGenerator
	@Inject ReportGenerator reportGenerator
	@Inject DeepPathUtilGenerator deepPathUtilGenerator
	
	@Inject DeepFeatureCallUtil deepFeatureCallUtil

	@Inject
	ResourceAwareFSAFactory fsaFactory;

	@Inject
	RosettaGeneratorsConfiguration config;

	// For files that are
	val ignoredFiles = #{'model-no-code-gen.rosetta', 'basictypes.rosetta', 'annotations.rosetta'}

	val Map<ResourceSet, DemandableLock> locks = newHashMap

	def void beforeAllGenerate(ResourceSet resourceSet, IFileSystemAccess2 fsa2, IGeneratorContext context) {
		LOGGER.trace("Starting the before all generate method")
		val lock = locks.computeIfAbsent(resourceSet, [new DemandableLock]);
		try {
			lock.getWriteLock(true);
			val models = resourceSet.resources.filter[!ignoredFiles.contains(URI.segments.last)].map [
				contents.head as RosettaModel
			].filter[it.shouldGenerate].toList
			val version = models.head?.version // TODO: find a way to access the version of a project directly
			externalGenerators.forEach [ generator |
				generator.beforeAllGenerate(resourceSet, models, version, [ map |
					map.entrySet.forEach[fsa2.generateFile(key, generator.outputConfiguration.getName, value)]
				], lock)
			]
		} catch (CancellationException e) {
			LOGGER.trace("Code generation cancelled, this is expected")
		} catch (Exception e) {
			LOGGER.warn("Unexpected calling before all generate for rosetta -" + e.message +
				" - see debug logging for more")
			LOGGER.debug("Unexpected calling before all generate for rosetta", e);
		} finally {
			lock.releaseWriteLock
		}
	}

	override void beforeGenerate(Resource resource, IFileSystemAccess2 fsa2, IGeneratorContext context) {
		if (!ignoredFiles.contains(resource.URI.segments.last)) {
			LOGGER.trace("Starting the before generate method for " + resource.URI.toString)
			val lock = locks.computeIfAbsent(resource.resourceSet, [new DemandableLock]);
			val fsa = fsaFactory.resourceAwareFSA(resource, fsa2, true)
			try {
				lock.getWriteLock(true);

				fsaFactory.beforeGenerate(resource)

				val model = resource.contents.head as RosettaModel
				if (!model.shouldGenerate) {
					return
				}
				val version = model.version

				externalGenerators.forEach [ generator |
					generator.beforeGenerate(resource, model, version, [ map |
						map.entrySet.forEach[fsa.generateFile(key, generator.outputConfiguration.getName, value)]
					], lock)
				]
			} catch (CancellationException e) {
				LOGGER.trace("Code generation cancelled, this is expected")
			} catch (Exception e) {
				LOGGER.warn("Unexpected calling before generate for rosetta -" + e.message +
					" - see debug logging for more")
				LOGGER.debug("Unexpected calling before generate for rosetta", e);
			} finally {
				lock.releaseWriteLock
			}
		}
	}

	override void doGenerate(Resource resource, IFileSystemAccess2 fsa2, IGeneratorContext context) {
		if (!ignoredFiles.contains(resource.URI.segments.last)) {
			LOGGER.trace("Starting the main generate method for " + resource.URI.toString)
			val fsa = fsaFactory.resourceAwareFSA(resource, fsa2, false)
			val lock = locks.computeIfAbsent(resource.resourceSet, [new DemandableLock]);
			try {
				lock.getWriteLock(true);

				val model = resource.contents.head as RosettaModel
				if (!model.shouldGenerate) {
					return
				}
				val version = model.version

				// generate
				val packages = new RootPackage(model)

				model.elements.forEach [
					if (context.cancelIndicator.canceled) {
						return // throw exception instead
					}
					switch (it) {
						Data: {
							dataGenerator.generate(packages, fsa, it, version)
							metaGenerator.generate(packages, fsa, it, version)
							// Legacy
							validatorsGenerator.generate(packages, fsa, it, version)
							it.conditions.forEach [ cond |
								conditionGenerator.generate(packages, fsa, it, cond, version)
							]
							// new
							// validatorGenerator.generate(packages, fsa, it, version)
							tabulatorGenerator.generate(fsa, it, Optional.empty)
							if (deepFeatureCallUtil.isEligibleForDeepFeatureCall(new RDataType(it))) {
								deepPathUtilGenerator.generate(fsa, it, version)
							}
							tabulatorGenerator.generate(fsa, it)
						}
						Function: {
							if (!isDispatchingFunction) {
								funcGenerator.generate(packages, fsa, it, version)
							}
							tabulatorGenerator.generate(fsa, it)
						}
						RosettaRule: {
							ruleGenerator.generate(packages, fsa, it, version)
						}
						RosettaReport: {
							reportGenerator.generate(packages, fsa, it, version)
							tabulatorGenerator.generate(fsa, it)
						}
						RosettaExternalRuleSource: {
							it.externalClasses.forEach [ externalClass |
								tabulatorGenerator.generate(fsa, externalClass.data, Optional.of(it))
							]
						}
					}
				]
				enumGenerator.generate(packages, fsa, model.elements, version)

				// Invoke externally defined code generators
				externalGenerators.forEach [ generator |
					generator.generate(resource, model, version, [ map |
						map.entrySet.forEach[fsa.generateFile(key, generator.outputConfiguration.getName, value)]
					], lock)
				]

				metaFieldGenerator.generate(resource, fsa, context)
			} catch (CancellationException e) {
				LOGGER.trace("Code generation cancelled, this is expected")
			} catch (Exception e) {
				LOGGER.warn(
					"Unexpected calling standard generate for rosetta -" + e.message + " - see debug logging for more")
				LOGGER.info("Unexpected calling standard generate for rosetta", e);
			} finally {
				LOGGER.trace("ending the main generate method")
				lock.releaseWriteLock
			}
		}
	}

	override void afterGenerate(Resource resource, IFileSystemAccess2 fsa2, IGeneratorContext context) {
		if (!ignoredFiles.contains(resource.URI.segments.last)) {
			LOGGER.trace("Starting the after generate method for " + resource.URI.toString)
			val lock = locks.computeIfAbsent(resource.resourceSet, [new DemandableLock]);
			val fsa = fsaFactory.resourceAwareFSA(resource, fsa2, true)
			try {
				lock.getWriteLock(true)

				val model = resource.contents.head as RosettaModel
				if (!model.shouldGenerate) {
					return
				}
				val version = model.version

				externalGenerators.forEach [ generator |
					generator.afterGenerate(resource, model, version, [ map |
						map.entrySet.forEach[fsa.generateFile(key, generator.outputConfiguration.getName, value)]
					], lock)
				]
				fsaFactory.afterGenerate(resource)

				// TODO: move this over to `afterAllGenerate` once the language supports that method as well.
				val models = resource.resourceSet.resources.filter[!ignoredFiles.contains(URI.segments.last)].map [
					contents.head as RosettaModel
				].filter[shouldGenerate].toList
				javaPackageInfoGenerator.generatePackageInfoClasses(fsa2, models)
			} catch (CancellationException e) {
				LOGGER.trace("Code generation cancelled, this is expected")
			} catch (Exception e) {
				LOGGER.warn("Unexpected calling after generate for rosetta -" + e.message +
					" - see debug logging for more")
				LOGGER.debug("Unexpected calling after generate for rosetta", e);
			} finally {
				lock.releaseWriteLock
			}
		}
	}

	def void afterAllGenerate(ResourceSet resourceSet, IFileSystemAccess2 fsa2, IGeneratorContext context) {
		LOGGER.trace("Starting the after all generate method")
		val lock = locks.computeIfAbsent(resourceSet, [new DemandableLock]);
		try {
			lock.getWriteLock(true)

			val models = resourceSet.resources.filter[!ignoredFiles.contains(URI.segments.last)].map [
				contents.head as RosettaModel
			].filter[shouldGenerate].toList
			val version = models.head?.version // TODO: find a way to access the version of a project directly
			externalGenerators.forEach [ generator |
				generator.afterAllGenerate(resourceSet, models, version, [ map |
					map.entrySet.forEach[fsa2.generateFile(key, generator.outputConfiguration.getName, value)]
				], lock)
			]
		} catch (CancellationException e) {
			LOGGER.trace("Code generation cancelled, this is expected")
		} catch (Exception e) {
			LOGGER.warn("Unexpected calling after all generate for rosetta -" + e.message +
				" - see debug logging for more")
			LOGGER.debug("Unexpected calling after all generate for rosetta", e);
		} finally {
			lock.releaseWriteLock
		}
	}
	
	private def boolean shouldGenerate(RosettaModel model) {
		config.namespaceFilter.test(model.name) || model.overridden
	}
	
}
