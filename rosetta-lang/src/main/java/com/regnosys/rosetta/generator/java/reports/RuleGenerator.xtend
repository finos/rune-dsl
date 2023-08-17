package com.regnosys.rosetta.generator.java.reports

import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.regnosys.rosetta.generator.java.blueprints.BlueprintGenerator
import com.regnosys.rosetta.rosetta.RosettaBlueprint
import javax.inject.Inject
import org.eclipse.xtext.generator.IFileSystemAccess2
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.types.RObjectFactory
import com.regnosys.rosetta.generator.java.JavaScope
import org.eclipse.xtend2.lib.StringConcatenationClient
import com.google.inject.ImplementedBy
import com.rosetta.model.lib.reports.ReportFunction
import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions
import com.rosetta.model.lib.functions.ModelObjectValidator
import com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil
import com.rosetta.util.types.JavaType
import com.regnosys.rosetta.types.RAttribute
import com.rosetta.util.types.JavaClass
import com.rosetta.util.types.JavaParameterizedType
import java.util.List
import com.regnosys.rosetta.blueprints.BlueprintInstance
import com.regnosys.rosetta.blueprints.runner.data.DataItemIdentifier
import com.regnosys.rosetta.blueprints.runner.nodes.SingleItemSource
import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory

class RuleGenerator {
	@Inject BlueprintGenerator blueprintGenerator
	@Inject extension JavaTypeTranslator
	@Inject extension RObjectFactory
	@Inject extension ImportManagerExtension
	@Inject extension RosettaFunctionExtensions

	
	def generate(RootPackage root, IFileSystemAccess2 fsa, RosettaBlueprint rule, String version) {
		if (rule.isLegacy) {
			blueprintGenerator.generate(root, fsa, #[rule], version)
			
			val rFunctionRule = buildRFunction(rule)
			val clazz = rFunctionRule.toFunctionJavaClass
			val topScope = new JavaScope(clazz.packageName)
			val classBody = generateWrapperForLegacyRule(root, rule, topScope)
			
			val content = buildClass(clazz.packageName, classBody, topScope)
			fsa.generateFile(clazz.canonicalName.withForwardSlashes + ".java", content)
		} else {
			// TODO for David
		}
	}
	
	private def StringConcatenationClient generateWrapperForLegacyRule(RootPackage root, RosettaBlueprint rule, JavaScope topScope) {
		val rFunctionRule = buildRFunction(rule)
		val input = rFunctionRule.inputs.head
		val output = rFunctionRule.output
		val inputJavaType = input.RType.toJavaReferenceType
		val outputJavaType = output.RType.toJavaReferenceType
		val clazz = rFunctionRule.toFunctionJavaClass
		val blueprintClass = new JavaClass(root.legacyBlueprint, clazz.simpleName)
		
		val classScope = topScope.classScope(clazz.simpleName)
		val objectValidatorId = classScope.createUniqueIdentifier("objectValidator")
		val blueprintId = classScope.createUniqueIdentifier(blueprintClass.simpleName.toFirstLower)
		val actionFactoryId = classScope.createUniqueIdentifier("actionFactory")
		val sampleId = classScope.createUniqueIdentifier("sampleId")
		val sourceId = classScope.createUniqueIdentifier("source")
		
		val evaluateScope = classScope.methodScope("evaluate")
		evaluateScope.createIdentifier(input, input.name)
		evaluateScope.createIdentifier(output, output.name)
		
		val doEvaluateScope = classScope.methodScope("evaluate")
		doEvaluateScope.createIdentifier(input, input.name)
		doEvaluateScope.createIdentifier(output, output.name)
		val bpInstanceId = doEvaluateScope.createUniqueIdentifier("blueprint")
		
		val defaultClassScope = classScope.classScope(rule.name + "Default")
		val defaultClassName = defaultClassScope.createUniqueIdentifier(rule.name + "Default")

		'''
			@«ImplementedBy»(«clazz».«defaultClassName».class)
			public abstract class «clazz» implements «ReportFunction»<«inputJavaType», «outputJavaType»> {
			
				«IF output.needsBuilder»
					@«Inject» protected «ModelObjectValidator» «objectValidatorId»;
				«ENDIF»
				@«Inject» private «blueprintClass» «blueprintId»;
				@«Inject» private «RosettaActionFactory» «actionFactoryId»;
			
				private final «DataItemIdentifier» «sampleId» = new «DataItemIdentifier»("«input.RType.name»");
				private final «SingleItemSource»<«inputJavaType»> «sourceId» = new «SingleItemSource»("«input.RType.namespace.child(input.RType.name)»", "«input.RType.name»", «sampleId»);
			
				/**
				* @param «evaluateScope.getIdentifierOrThrow(input)» «ModelGeneratorUtil.escape(input.definition)»
				* @return «evaluateScope.getIdentifierOrThrow(output)» «ModelGeneratorUtil.escape(output.definition)»
				*/
				@Override
				public «outputJavaType» evaluate(«inputJavaType» «evaluateScope.getIdentifierOrThrow(input)») {
					«output.toBuilderType» «evaluateScope.getIdentifierOrThrow(output)» = doEvaluate(«evaluateScope.getIdentifierOrThrow(input)»);
					«IF output.needsBuilder»
						if («evaluateScope.getIdentifierOrThrow(output)» != null) {
							«objectValidatorId».validate(«output.RType.toJavaType».class, «evaluateScope.getIdentifierOrThrow(output)»);
						}
					«ENDIF»
					return «evaluateScope.getIdentifierOrThrow(output)»;
				}
			
				protected abstract «output.toBuilderType» doEvaluate(«inputJavaType» «doEvaluateScope.getIdentifierOrThrow(input)»);
			
				public static class «defaultClassName» extends «clazz» {
					@Override
					protected «output.toBuilderType» doEvaluate(«inputJavaType» «doEvaluateScope.getIdentifierOrThrow(input)») {
						«BlueprintInstance»<«inputJavaType», «outputJavaType», «String», «String»> «bpInstanceId» = «blueprintId».blueprint();
						«sourceId».set(«doEvaluateScope.getIdentifierOrThrow(input)»);
						BlueprintReport blueprintReport = startsWith(actionFactory, source)
						                .then(blueprint)
						                .then(new MapMerger<>("Table", "Table", false, sampleId))
						                .andSink(new ListSink<>("sink", "sink", null))
						                .toBlueprint(blueprint.getURI(), blueprint.getLabel()).runBlueprint();
					}
				}
			}
		'''
	}
	private def JavaType toBuilderType(RAttribute rAttribute) {
		var javaType = rAttribute.RType.toJavaReferenceType as JavaClass
		if(rAttribute.needsBuilder) javaType = javaType.toBuilderType
		if (rAttribute.multi) {
			return new JavaParameterizedType(JavaClass.from(List), javaType)
		} else {
			return javaType
		}
	}
}