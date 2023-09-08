package com.regnosys.rosetta.generator.java.reports

import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
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
import com.regnosys.rosetta.types.RAttribute
import com.rosetta.util.types.JavaClass
import com.rosetta.util.types.JavaParameterizedType
import java.util.List
import com.regnosys.rosetta.blueprints.BlueprintInstance
import com.regnosys.rosetta.blueprints.runner.data.DataItemIdentifier
import com.regnosys.rosetta.blueprints.runner.nodes.SingleItemSource
import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory
import com.regnosys.rosetta.blueprints.BlueprintReport
import com.regnosys.rosetta.blueprints.runner.nodes.MapMerger
import com.regnosys.rosetta.blueprints.runner.nodes.ListSink
import java.util.Map
import com.regnosys.rosetta.blueprints.runner.data.DataIdentifier
import com.regnosys.rosetta.blueprints.runner.data.GroupableData
import java.util.Optional
import com.regnosys.rosetta.blueprints.BlueprintBuilder
import com.regnosys.rosetta.types.RDataType
import com.regnosys.rosetta.blueprints.runner.data.RuleIdentifier
import java.util.concurrent.ExecutionException
import com.rosetta.util.types.JavaInterface
import com.regnosys.rosetta.generator.java.function.FunctionGenerator
import com.rosetta.util.types.JavaReferenceType
import java.util.Collections
import java.util.Arrays

class RuleGenerator {
	@Inject extension JavaTypeTranslator
	@Inject extension RObjectFactory
	@Inject extension ImportManagerExtension
	@Inject extension RosettaFunctionExtensions
	@Inject FunctionGenerator functionGenerator

	
	def generate(RootPackage root, IFileSystemAccess2 fsa, RosettaBlueprint rule, String version) {
		if (rule.isLegacy) {			
			val rFunctionRule = buildRFunction(rule)
			val clazz = rFunctionRule.toFunctionJavaClass
			val topScope = new JavaScope(clazz.packageName)
			val classBody = generateWrapperForLegacyRule(root, rule, topScope)
			
			val content = buildClass(clazz.packageName, classBody, topScope)
			fsa.generateFile(clazz.canonicalName.withForwardSlashes + ".java", content)
		} else {
			val rFunctionRule = buildRFunction(rule)
			val clazz = rFunctionRule.toFunctionJavaClass
			val baseInterface = new JavaParameterizedType(JavaInterface.from(ReportFunction), rFunctionRule.inputs.head.attributeToJavaType, rFunctionRule.output.attributeToJavaType)
			val topScope = new JavaScope(clazz.packageName)
			val classBody = functionGenerator.rBuildClass(rFunctionRule, false, #[baseInterface], true, topScope)
			
			val content = buildClass(clazz.packageName, classBody, topScope)
			fsa.generateFile(clazz.canonicalName.withForwardSlashes + ".java", content)
		}
	}
	
	private def StringConcatenationClient generateWrapperForLegacyRule(RootPackage root, RosettaBlueprint rule, JavaScope topScope) {
		val rFunctionRule = buildRFunction(rule)
		val input = rFunctionRule.inputs.head
		val output = rFunctionRule.output
		val inputJavaType = input.attributeToJavaType
		val outputJavaType = output.attributeToJavaType
		val clazz = rFunctionRule.toFunctionJavaClass
		val blueprintClass = new JavaClass(root.legacyBlueprint, clazz.simpleName)
		
		val classScope = topScope.classScope(clazz.simpleName)
		val objectValidatorId = classScope.createUniqueIdentifier("objectValidator")
		
		val evaluateScope = classScope.methodScope("evaluate")
		evaluateScope.createIdentifier(input, input.name)
		evaluateScope.createIdentifier(output, output.name)
		
		val doEvaluateScope = classScope.methodScope("evaluate")
		doEvaluateScope.createIdentifier(input, input.name)
		doEvaluateScope.createIdentifier(output, output.name)
		val bpInstanceId = doEvaluateScope.createUniqueIdentifier("blueprint")
		val bpReportId = doEvaluateScope.createUniqueIdentifier("blueprintReport")
		val reportDataId = doEvaluateScope.createUniqueIdentifier("reportData")
		val singleReportDataId = doEvaluateScope.createUniqueIdentifier("singleReportData")
		val doEvaluateLambdaScope1 = doEvaluateScope.lambdaScope
		val doEvaluateLambdaParam1 = doEvaluateLambdaScope1.createUniqueIdentifier("b")
		val doEvaluateLambdaScope2 = doEvaluateScope.lambdaScope
		val doEvaluateLambdaParam2 = doEvaluateLambdaScope2.createUniqueIdentifier("e")
		
		val defaultClassScope = classScope.classScope(rule.name + "Default")
		val defaultClassName = defaultClassScope.createUniqueIdentifier(rule.name + "Default")
		val blueprintId = defaultClassScope.createUniqueIdentifier(blueprintClass.simpleName.toFirstLower)
		val actionFactoryId = defaultClassScope.createUniqueIdentifier("actionFactory")
		val sampleId = classScope.createUniqueIdentifier("sampleId")
		val sourceId = classScope.createUniqueIdentifier("source")
		'''
			@«ImplementedBy»(«clazz».«defaultClassName».class)
			public abstract class «clazz» implements «ReportFunction»<«inputJavaType», «outputJavaType»> {
				«IF output.needsBuilder»
					
					@«Inject» protected «ModelObjectValidator» «objectValidatorId»;
				«ENDIF»
			
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
					@«Inject» private «blueprintClass» «blueprintId»;
					@«Inject» private «RosettaActionFactory» «actionFactoryId»;
					
					private final «DataItemIdentifier» «sampleId» = new «DataItemIdentifier»("«input.RType.name»");
					private final «SingleItemSource»<«inputJavaType»> «sourceId» = new «SingleItemSource»("«input.RType.namespace.child(input.RType.name)»", "«input.RType.name»", «sampleId»);
					
					@Override
					protected «output.toBuilderType» doEvaluate(«inputJavaType» «doEvaluateScope.getIdentifierOrThrow(input)») {
						«BlueprintInstance»<«inputJavaType», «outputJavaType», «String», «String»> «bpInstanceId» = «blueprintId».blueprint();
						«sourceId».setItem(«doEvaluateScope.getIdentifierOrThrow(input)»);
						
						«BlueprintReport» «bpReportId»;
						try {
							«bpReportId» = «BlueprintBuilder».startsWith(«actionFactoryId», «sourceId»)
				                .then(«bpInstanceId»)
				                .then(new «MapMerger»<>("Table", "Table", false, «sampleId»))
				                .andSink(new «ListSink»<>("sink", "sink", null))
				                .addDataItemReportBuilder(«bpInstanceId».getDataItemReportBuilder())
				                .toBlueprint(«bpInstanceId».getURI(), «bpInstanceId».getLabel()).runBlueprint();
						} catch («InterruptedException» | «ExecutionException» e) {
						    throw new «RuntimeException»(e);
						}
						«List»<«Map»<«DataIdentifier», «GroupableData»<?, «String»>>> «reportDataId» = («List»<«Map»<«DataIdentifier», «GroupableData»<?, «String»>>>)«bpReportId».getReportData();
				        «Map»<«DataIdentifier», «GroupableData»<?, «String»>> «singleReportDataId»;
				        if («reportDataId».size() == 1) {
				        	«singleReportDataId» = «reportDataId».get(0);
				        } else if («reportDataId».size() == 0) {
				        	«singleReportDataId» = «Collections».emptyMap();
				        } else {
				        	throw new «RuntimeException»("Expected a single report output, but found " + «reportDataId».size() + ".");
				        }
				        «IF output.RType instanceof RDataType»
				        return «Optional».ofNullable(«bpReportId».getDataItemReportBuilder())
				                .map(«doEvaluateLambdaParam1» -> («output.toBuilderType»)«doEvaluateLambdaParam1».buildReport«IF output.isMulti»List«ENDIF»(«singleReportDataId».values()))
				                .orElse(null);
				        «ELSE»
				        return «singleReportDataId».entrySet().stream()
				        		.filter(«doEvaluateLambdaParam2» -> «doEvaluateLambdaParam2».getKey() instanceof «RuleIdentifier»)
				        		.findAny()
				                .map(«doEvaluateLambdaParam2» -> «doEvaluateLambdaParam2».getValue().getData())
				                .map(«doEvaluateLambdaParam2» -> («output.toBuilderType»)«IF output.isMulti»(«doEvaluateLambdaParam2» instanceof «List» ? «doEvaluateLambdaParam2» : «Arrays».asList(«doEvaluateLambdaParam2»))«ELSE»«doEvaluateLambdaParam2»«ENDIF»)
				                .orElse(null);
				        «ENDIF»
					}
				}
			}
		'''
	}
	private def JavaReferenceType toBuilderType(RAttribute rAttribute) {
		var javaType = rAttribute.RType.toJavaReferenceType as JavaClass
		if(rAttribute.needsBuilder) javaType = javaType.toBuilderType
		if (rAttribute.multi) {
			return new JavaParameterizedType(JavaClass.from(List), javaType)
		} else {
			return javaType
		}
	}
}