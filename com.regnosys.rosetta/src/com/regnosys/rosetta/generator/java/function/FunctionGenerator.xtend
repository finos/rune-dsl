package com.regnosys.rosetta.generator.java.function

import com.google.common.collect.ClassToInstanceMap
import com.google.inject.Inject
import com.regnosys.rosetta.generator.RosettaInternalGenerator
import com.regnosys.rosetta.generator.java.RosettaJavaPackages
import com.regnosys.rosetta.generator.java.calculation.ImportingStringConcatination
import com.regnosys.rosetta.generator.java.calculation.RosettaFunctionDependencyProvider
import com.regnosys.rosetta.rosetta.RosettaFuncitonCondition
import com.regnosys.rosetta.rosetta.RosettaFunction
import com.regnosys.rosetta.rosetta.RosettaRootElement
import java.util.List
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.generator.IFileSystemAccess2

class FunctionGenerator implements RosettaInternalGenerator {

	@Inject JavaQualifiedTypeProvider.Factory factory
	@Inject RosettaExpressionJavaGeneratorForFunctions rosettaExpressionGenerator
	@Inject RosettaFunctionDependencyProvider functionDependencyProvider

	override generate(RosettaJavaPackages packages, IFileSystemAccess2 fsa, List<RosettaRootElement> elements, String version) {
		val javaNames = factory.create(packages)
		
		elements.filter(RosettaFunction).forEach [
			val name = javaNames.packages.functions.directoryName + '/' + name + '.java'
			
			try {
				val content = generate(it, javaNames)
				fsa.generateFile(name, content)	
			} catch (Exception e) {
				throw new UnsupportedOperationException('Unable to generate code for: ' + name)
			}
			
		]
	}

	private def String generate(RosettaFunction function, JavaQualifiedTypeProvider javaNames) {
		val concatenator = new ImportingStringConcatination()
		concatenator.append(functionClass(function, javaNames))
		
		return '''
			package «javaNames.packages.functions.packageName»;
			
«««			(DONE) Make RosettaExpression support StringConcatClient to add these imports
«««			Now have RosettaExpressionToJava support actually use types (not just strings)
			import com.rosetta.model.lib.functions.MapperS;
			import com.rosetta.model.lib.functions.MapperTree;
			import com.rosetta.model.lib.meta.FieldWithMeta;
			import java.time.LocalDate;
			import java.math.BigDecimal;
			
			import org.isda.cdm.*;
						
			import static com.rosetta.model.lib.validation.ValidatorHelper.*;
			
			«FOR _import : concatenator.imports»
				import «_import»;
			«ENDFOR»
			«FOR staticImport : concatenator.staticImports»
				import static «staticImport»;
			«ENDFOR»
			
			«concatenator.toString»
		'''
	}
	
	private def StringConcatenationClient functionClass(RosettaFunction function, JavaQualifiedTypeProvider javaNames) {
		val dependencies = (function.preConditions + function.postConditions)
			.flatMap[expressions]
			.flatMap[functionDependencyProvider.functionDependencies(it)]
			.sortBy[name]
			.toSet
		
		'''
			«function.contributeJavaDoc»
			public abstract class «function.name» implements «com.rosetta.model.lib.functions.RosettaFunction» {
				«contributeFields(function, javaNames)»
				«contributeConstructor(function, javaNames)»
				«contributeEvaluateMethod(function, javaNames, dependencies)»
				«contributeEnrichMethod(function, javaNames)»
			}
		'''
	}
	
	
	def StringConcatenationClient contributeJavaDoc(extension RosettaFunction function) {
		if (definition !== null) {
			'''
				/**
				 «IF definition !== null»
				 * «definition»
				 «ENDIF»
				 */
			'''
		}
	}
	
	
	def StringConcatenationClient contributeEnrichMethod(extension RosettaFunction function, extension JavaQualifiedTypeProvider names) '''
			
		abstract «output.toJavaQualifiedType(false)» doEvaluate(«function.inputsAsParameters(names)»);
	'''
	
	def StringConcatenationClient contributeEvaluateMethod(extension RosettaFunction function, extension JavaQualifiedTypeProvider names, Iterable<RosettaFunction> dependencies) '''
			
		/**
		 «FOR input : inputs»
		 * @param «input.name» «input.definition»
		 «ENDFOR»
		 * @return «output.name» «output.definition»
		 */
		public «output.toJavaQualifiedType(false)» evaluate(«function.inputsAsParameters(names)») {
			«function.contributeDependencies(names, dependencies)»
			«function.contributePreConditions(names)»
			
			// Delegate to implementation
			//
			«output.toJavaQualifiedType(false)» «output.name» = doEvaluate(«function.inputsAsArguments(names)»);
			«function.contributePostConditions(names)»
			
			return «output.name»;
		}
	'''
	
	def StringConcatenationClient contributeDependencies(extension RosettaFunction function, extension JavaQualifiedTypeProvider provider, Iterable<RosettaFunction> dependencies) {
		if (!dependencies.empty) {
			'''
			
			// RosettaFunction dependencies
			//
			«FOR dep : dependencies»
			final «dep.name» «dep.name.toFirstLower» = classRegistry.getInstance(«dep.toJavaQualifiedType(false)».class);
			«ENDFOR»
			'''
		}
	}
	
	def StringConcatenationClient contributePostConditions(extension RosettaFunction function, extension JavaQualifiedTypeProvider names) {
		if (!postConditions.empty) {
			'''
				
			// post-conditions
			//
			«postConditions.contributeConditionBlock»
			'''
		}
	}
	
	def StringConcatenationClient contributePreConditions(extension RosettaFunction function, extension JavaQualifiedTypeProvider names) {
		if (!preConditions.empty) {
			'''
				
			// pre-conditions
			//
			«preConditions.contributeConditionBlock»
			'''
		}
	}
	
	def CharSequence contributeConditionBlock(Iterable<RosettaFuncitonCondition> conditions)
		'''«FOR condition : conditions»
		assert
«««			«rosettaExpressionGenerator.javaCode(condition.expressions.head, null)»
			«FOR expr : condition.expressions SEPARATOR ' &&'» 
				«rosettaExpressionGenerator.javaCode(expr, null)».get()
			«ENDFOR»
				: "«condition.definition»";
		«ENDFOR»
		'''
	
	
	def StringConcatenationClient contributeConstructor(extension RosettaFunction function, extension JavaQualifiedTypeProvider names) {
		'''
		
		«name.toFirstUpper»(«ClassToInstanceMap»<«com.rosetta.model.lib.functions.RosettaFunction»> classRegistry) {
			
			// On concrete instantiation, register implementation with function to implementation container
			//
			classRegistry.putInstance(«name».class, this);
			this.classRegistry = classRegistry;	
		}
		'''
	}
	
	def StringConcatenationClient contributeFields(extension RosettaFunction function, extension JavaQualifiedTypeProvider names) {
		'''
		
		private final «ClassToInstanceMap»<«com.rosetta.model.lib.functions.RosettaFunction»> classRegistry;
		'''	
	}
	
	
	private def StringConcatenationClient inputsAsParameters(extension RosettaFunction function, extension JavaQualifiedTypeProvider names) {
		'''«FOR input : inputs SEPARATOR ', '»«input.toJavaQualifiedType(false)» «input.name»«ENDFOR»'''
	}
	
	private def StringConcatenationClient inputsAsArguments(extension RosettaFunction function, extension JavaQualifiedTypeProvider names) {
		'''«FOR input : inputs SEPARATOR ', '»«input.name»«ENDFOR»'''
	}

}
