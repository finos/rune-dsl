package com.regnosys.rosetta.generator.java.qualify

import com.google.common.base.CaseFormat
import com.google.inject.Inject
import com.google.inject.Provider
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.RosettaJavaPackages
import com.regnosys.rosetta.generator.java.qualify.RosettaExpressionJavaGenerator.ParamMap
import com.regnosys.rosetta.generator.java.rule.DataRuleGenerator
import com.regnosys.rosetta.generator.java.util.ImportGenerator
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.generator.java.util.RosettaGrammarUtil
import com.regnosys.rosetta.rosetta.RosettaCallableCall
import com.regnosys.rosetta.rosetta.RosettaClass
import com.regnosys.rosetta.rosetta.RosettaQualifiable
import com.regnosys.rosetta.rosetta.RosettaRootElement
import java.util.List
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.generator.IFileSystemAccess2

import static com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil.*

class QualifyFunctionGenerator<T extends RosettaQualifiable> {
	@Inject extension ImportManagerExtension
	@Inject Provider<RosettaExpressionJavaGenerator> expressionHandlerProvider
	
	def generate(RosettaJavaPackages packages, IFileSystemAccess2 fsa, List<RosettaRootElement> elements, RosettaJavaPackages.Package javaPackage, Class<T> qualifiableClassType, String version) {
		// create is function classes (e.g. isFooEvent or isBarProduct)
		elements.filter(qualifiableClassType).forEach[ 
			val isFunctionClassName = getIsFunctionClassName(it.name)
			
			fsa.generateFile('''«javaPackage.directoryName»/«isFunctionClassName».java''', toIsFunctionJava(packages, javaPackage, isFunctionClassName, it, version))
		]
	}

	def static String getIsFunctionClassName(String className) {
		val allUnderscore = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, className)
		return 'Is' + CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, allUnderscore)
	}

	private def getRosettaClass(RosettaRootElement element) {
		val rosettaClasses = newHashSet
		val extensions = new RosettaExtensions
		element.eAllContents.filter(RosettaCallableCall).forEach[
				extensions.collectRootCalls(it, [if(it instanceof RosettaClass) rosettaClasses.add(it)])
		]
		
		if (rosettaClasses.size > 1) {
			throw new IllegalStateException('QualifyFunction compile failed for ' + element + '. Found more then one class reference ' + rosettaClasses.map[name])
		}
		if (rosettaClasses.size < 1) {
			throw new IllegalStateException('QualifyFunction compile failed for ' + element + '. Found no class references')
		}
		
		return rosettaClasses.get(0)
	}

	private def  toIsFunctionJava(RosettaJavaPackages packages, RosettaJavaPackages.Package javaPackage, String className, RosettaQualifiable qualifiableClass, String version) { 
		val classBody = tracImports(toIsFunctionJava(packages, javaPackage, className, qualifiableClass, expressionHandlerProvider.get(), version))
		'''«classBody.toString»'''
	}
	
	private def StringConcatenationClient toIsFunctionJava(RosettaJavaPackages packages, RosettaJavaPackages.Package javaPackage, String className, RosettaQualifiable qualifiableClass, RosettaExpressionJavaGenerator expressionHandler, String version) { 
		val rosettaClass = getRosettaClass(qualifiableClass)
		
		val imports = new ImportGenerator(packages) 
		imports.addQualifyClass(qualifiableClass.expression, qualifiableClass.andDataRules, qualifiableClass.andDataRules, rosettaClass)
		
		val definition = RosettaGrammarUtil.grammarQualifiable(qualifiableClass)
		
		'''
		package «javaPackage.packageName»;
		
		«FOR importClass : imports.imports.filter[imports.isImportable(it)]»
		import «importClass»;
		«ENDFOR»
		
		«FOR importClass : imports.staticImports»
		import static «importClass».*;
		«ENDFOR»
		
		«emptyJavadocWithVersion(version)»
		@RosettaQualifiable("«qualifiableClass.name»")
		public class «className» implements Function<«rosettaClass.name», QualifyResult>{
			
			private static final String QUALIFY_DEFINITION = «RosettaGrammarUtil.quote(definition)»;
			
			private static final String EXPR_DEFINITION = «RosettaGrammarUtil.quote(RosettaGrammarUtil.grammarText(qualifiableClass.expression))»;
			
			@Override
			public QualifyResult apply(«rosettaClass.name» «rosettaClass.name.toFirstLower») {
				ComparisonResult exprResult = «expressionHandler.javaCode(qualifiableClass.expression, new ParamMap(rosettaClass))»;
				
				return QualifyResult.builder()
						.setName("«qualifiableClass.name»")
						.setDefinition(QUALIFY_DEFINITION)
						.setExpressionResult(EXPR_DEFINITION, exprResult)
						«FOR dataRule : qualifiableClass.andDataRules»
						.addAndDataRuleResult(new «DataRuleGenerator.dataRuleClassName(dataRule.name)»().validate(null, «rosettaClass.name.toFirstLower»))
						«ENDFOR»
						«FOR dataRule : qualifiableClass.orDataRules»
						.addOrDataRuleResult(new «DataRuleGenerator.dataRuleClassName(dataRule.name)»().validate(null, «rosettaClass.name.toFirstLower»))
						«ENDFOR»
						.build();
			}
		}
		'''
	}
}
