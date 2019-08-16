package com.regnosys.rosetta.generator.java.function

import com.google.common.collect.ClassToInstanceMap
import com.google.inject.Inject
import com.regnosys.rosetta.generator.RosettaInternalGenerator
import com.regnosys.rosetta.generator.java.RosettaJavaPackages
import com.regnosys.rosetta.generator.java.calculation.ImportingStringConcatination
import com.regnosys.rosetta.generator.java.calculation.RosettaFunctionDependencyProvider
import com.regnosys.rosetta.rosetta.RosettaDefinable
import com.regnosys.rosetta.rosetta.RosettaFuncitonCondition
import com.regnosys.rosetta.rosetta.RosettaFunction
import com.regnosys.rosetta.rosetta.RosettaNamed
import com.regnosys.rosetta.rosetta.RosettaRootElement
import com.regnosys.rosetta.rosetta.simple.Condition
import com.regnosys.rosetta.rosetta.simple.Function
import java.util.List
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.generator.IFileSystemAccess2
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs

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
	

	def void generate(RosettaJavaPackages packages, IFileSystemAccess2 fsa, Function func, String version) {
		val javaNames = factory.create(packages)
		val name = javaNames.packages.functions.directoryName + '/' + func.name + '.java'
			
			try {
				val concatenator = new ImportingStringConcatination()
				concatenator.append(functionClass(func, javaNames))
				val content = 
				'''
				package «javaNames.packages.functions.packageName»;
				
				«FOR _import : concatenator.imports»
					import «_import»;
				«ENDFOR»
				«FOR staticImport : concatenator.staticImports»
					import static «staticImport»;
				«ENDFOR»
				«IF !(func.conditions.nullOrEmpty || func.postConditions.nullOrEmpty) /*FIXME add static imports */»
				import static com.rosetta.model.lib.validation.ValidatorHelper.*;
				«ENDIF»
				«concatenator.toString»
				'''
				fsa.generateFile(name, content)	
			} catch (Exception e) {
				throw new UnsupportedOperationException('Unable to generate code for: ' + name, e)
			}
	}
	
	private def String generate(RosettaFunction function, JavaQualifiedTypeProvider javaNames) {
		val concatenator = new ImportingStringConcatination()
		concatenator.append(functionClass(function, javaNames))
		
		return '''
			package «javaNames.packages.functions.packageName»;
			
«««			(DONE) Make RosettaExpression support StringConcatClient to add these imports
«««			Now have RosettaExpressionToJava support actually use types (not just strings)
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
	
	private def dispatch StringConcatenationClient functionClass(Function function, JavaQualifiedTypeProvider javaNames) {
		val dependencies = (function.conditions + function.postConditions)
			.flatMap[expressions]
			.flatMap[functionDependencyProvider.functionDependencies(it)]
			.sortBy[name]
			.toSet
		
		'''
			«function.contributeJavaDoc»
			public abstract class «function.name» implements «com.rosetta.model.lib.functions.RosettaFunction» {
				«contributeFields(javaNames)»
				«contributeConstructor(function, javaNames)»
				«contributeEvaluateMethod(function, javaNames, dependencies)»
				«contributeEnrichMethod(function, javaNames)»
			}
		'''
	}
	
	private def dispatch StringConcatenationClient functionClass(RosettaFunction function, JavaQualifiedTypeProvider javaNames) {
		val dependencies = (function.preConditions + function.postConditions)
			.flatMap[expressions]
			.flatMap[functionDependencyProvider.functionDependencies(it)]
			.sortBy[name]
			.toSet
		
		'''
			«function.contributeJavaDoc»
			public abstract class «function.name» implements «com.rosetta.model.lib.functions.RosettaFunction» {
				«contributeFields(javaNames)»
				«contributeConstructor(function, javaNames)»
				«contributeEvaluateMethod(function, javaNames, dependencies)»
				«contributeEnrichMethod(function, javaNames)»
			}
		'''
	}
	
	
	def StringConcatenationClient contributeJavaDoc(extension RosettaDefinable function) {
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
	
	
	def dispatch StringConcatenationClient contributeEnrichMethod(extension RosettaFunction function, extension JavaQualifiedTypeProvider names) '''
			
		protected abstract «output.toJavaQualifiedType(false)» doEvaluate(«function.inputsAsParameters(names)»);
	'''
	
	def dispatch StringConcatenationClient contributeEnrichMethod(extension Function function, extension JavaQualifiedTypeProvider names) '''
			
		protected abstract «function.outputTypeOrVoid(names)» doEvaluate(«function.inputsAsParameters(names)»);
	'''
	

	dispatch def StringConcatenationClient contributeEvaluateMethod(extension RosettaFunction function, extension JavaQualifiedTypeProvider names, Iterable<? extends RosettaCallableWithArgs> dependencies) '''
			
		/**
		 «FOR input : inputs»
		 * @param «input.name» «input.definition»
		 «ENDFOR»
		 * @return «output.name» «output.definition»
		 */
		public «output.toJavaQualifiedType(false)» evaluate(«function.inputsAsParameters(names)») {
			«contributeDependencies(names, dependencies)»
			«function.contributePreConditions(names)»
			
			// Delegate to implementation
			//
			«output.toJavaQualifiedType(false)» «output.name» = doEvaluate(«function.inputsAsArguments(names)»);
			«function.contributePostConditions(names)»
			
			return «output.name»;
		}
	'''
	
	dispatch def StringConcatenationClient contributeEvaluateMethod(extension Function function, extension JavaQualifiedTypeProvider names, Iterable<? extends RosettaCallableWithArgs> dependencies) '''
			
		/**
		 «FOR input : inputs»
		 * @param «input.name» «input.definition»
		 «ENDFOR»
		 «IF output !== null»
		 * @return «output.name» «output.definition»
		 «ENDIF»
		 */
		public «function.outputTypeOrVoid(names)» evaluate(«function.inputsAsParameters(names)») {
			«contributeDependencies(names, dependencies)»
			
			«IF !conditions.empty»
			// pre-conditions
			//
			«FOR cond:conditions»
			«cond.contributeCondition»
			«ENDFOR»
			«ENDIF»
			// Delegate to implementation
			//
			«IF output !== null»«output.toJavaQualifiedType(false)» «output.name» = «ENDIF»doEvaluate(«function.inputsAsArguments(names)»);
			«IF !postConditions.empty»
			// post-conditions
			//
			«FOR cond:postConditions»
			«cond.contributeCondition»
			«ENDFOR»
			«ENDIF»
			«IF output !== null»
			return «output.name»;
			«ENDIF»
		}
	'''
	
	def outputTypeOrVoid(Function function,  extension JavaQualifiedTypeProvider names){
		if(function.output === null) {
			'void'
		} else {
			function.output.toJavaQualifiedType(false)
		}
	}
	
	private def StringConcatenationClient contributeDependencies(extension JavaQualifiedTypeProvider provider, Iterable<? extends RosettaCallableWithArgs> dependencies) {
		if (!dependencies.empty) {
			'''
			
			// RosettaFunction dependencies
			//
			«FOR dep : dependencies»
			final «dep.name» «dep.name.toFirstLower» = classRegistry.getInstance(«dep.toJavaQualifiedType()».class);
			«ENDFOR»
			'''
		}
	}
	
	def StringConcatenationClient contributePostConditions(extension RosettaFunction function, extension JavaQualifiedTypeProvider names) {
		if (!postConditions.empty) {
			'''
				
			// post-conditions
			//
			«FOR cond:postConditions»
			«cond.contributeCondition»
			«ENDFOR»
			'''
		}
	}
	
	def StringConcatenationClient contributePreConditions(extension RosettaFunction function, extension JavaQualifiedTypeProvider names) {
		if (!preConditions.empty) {
			'''
				
			// pre-conditions
			//
			«FOR cond:preConditions»
			«cond.contributeCondition»
			«ENDFOR»
			'''
		}
	}
	
	private dispatch def StringConcatenationClient contributeCondition(Condition condition) {
		'''
		assert
«««			«rosettaExpressionGenerator.javaCode(condition.expressions.head, null)»
			«FOR expr : condition.expressions SEPARATOR ' &&'» 
				«rosettaExpressionGenerator.javaCode(expr, null)».get()
			«ENDFOR»
				: "«condition.definition»";
		'''
	}
	
	
	private dispatch def StringConcatenationClient contributeCondition(RosettaFuncitonCondition condition)
		'''
		assert
«««			«rosettaExpressionGenerator.javaCode(condition.expressions.head, null)»
			«FOR expr : condition.expressions SEPARATOR ' &&'» 
				«rosettaExpressionGenerator.javaCode(expr, null)».get()
			«ENDFOR»
				: "«condition.definition»";
		'''
	
	
	def StringConcatenationClient contributeConstructor(extension RosettaNamed function, extension JavaQualifiedTypeProvider names) {
		'''
		
		protected «name.toFirstUpper»(«ClassToInstanceMap»<«com.rosetta.model.lib.functions.RosettaFunction»> classRegistry) {
			
			// On concrete instantiation, register implementation with function to implementation container
			//
			classRegistry.putInstance(«name».class, this);
			this.classRegistry = classRegistry;	
		}
		'''
	}
	
	def StringConcatenationClient contributeFields(extension JavaQualifiedTypeProvider names) {
		'''
		
		protected final «ClassToInstanceMap»<«com.rosetta.model.lib.functions.RosettaFunction»> classRegistry;
		'''	
	}
	
	
	private def StringConcatenationClient inputsAsParameters(extension RosettaFunction function, extension JavaQualifiedTypeProvider names) {
		'''«FOR input : inputs SEPARATOR ', '»«input.toJavaQualifiedType(false)» «input.name»«ENDFOR»'''
	}
	private def StringConcatenationClient inputsAsParameters(extension Function function, extension JavaQualifiedTypeProvider names) {
		'''«FOR input : inputs SEPARATOR ', '»«input.toJavaQualifiedType(false)» «input.name»«ENDFOR»'''
	}
	
	private dispatch def StringConcatenationClient inputsAsArguments(extension RosettaFunction function, extension JavaQualifiedTypeProvider names) {
		'''«FOR input : inputs SEPARATOR ', '»«input.name»«ENDFOR»'''
	}
	private dispatch def StringConcatenationClient inputsAsArguments(extension Function function, extension JavaQualifiedTypeProvider names) {
		'''«FOR input : inputs SEPARATOR ', '»«input.name»«ENDFOR»'''
	}

}
