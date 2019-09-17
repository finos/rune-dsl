package com.regnosys.rosetta.generator.java.function

import com.google.inject.ImplementedBy
import com.google.inject.Inject
import com.regnosys.rosetta.generator.java.calculation.RosettaFunctionDependencyProvider
import com.regnosys.rosetta.generator.java.expression.Context
import com.regnosys.rosetta.generator.java.expression.ExpressionGeneratorWithBuilder
import com.regnosys.rosetta.generator.java.expression.RosettaExpressionJavaGeneratorForFunctions
import com.regnosys.rosetta.generator.java.expression.RosettaExpressionJavaGeneratorForFunctions.ParamMap
import com.regnosys.rosetta.generator.java.util.ImportingStringConcatination
import com.regnosys.rosetta.generator.java.util.JavaNames
import com.regnosys.rosetta.generator.java.util.JavaType
import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions
import com.regnosys.rosetta.generator.util.Util
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs
import com.regnosys.rosetta.rosetta.RosettaClass
import com.regnosys.rosetta.rosetta.RosettaFeature
import com.regnosys.rosetta.rosetta.RosettaRegularAttribute
import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.rosetta.simple.AssignPathRoot
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.Condition
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.simple.Operation
import com.regnosys.rosetta.rosetta.simple.Segment
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration
import com.regnosys.rosetta.types.RClassType
import com.regnosys.rosetta.types.RDataType
import com.regnosys.rosetta.types.RType
import com.regnosys.rosetta.types.RosettaTypeProvider
import com.regnosys.rosetta.utils.ExpressionHelper
import com.rosetta.model.lib.functions.MapperBuilder
import com.rosetta.model.lib.functions.RosettaFunction
import java.util.List
import java.util.Map
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.generator.IFileSystemAccess2

class FuncGenerator {

	@Inject RosettaExpressionJavaGeneratorForFunctions expressionGenerator
	@Inject ExpressionGeneratorWithBuilder expressionWithBuilder
	@Inject RosettaFunctionDependencyProvider functionDependencyProvider
	@Inject RosettaTypeProvider typeProvider
	@Inject extension RosettaFunctionExtensions
	@Inject ExpressionHelper exprHelper

	def void generate(JavaNames javaNames, IFileSystemAccess2 fsa, Function func, String version) {
		val fileName = javaNames.packages.functions.directoryName + '/' + func.name + '.java'

		try {
			val concatenator = new ImportingStringConcatination()
			val deps = func.shortcuts.flatMap[functionDependencyProvider.functionDependencies(it.expression)] +
				func.operations.flatMap[functionDependencyProvider.functionDependencies(it.expression)]
			val condDeps = (func.conditions + func.postConditions).flatMap[expressions].flatMap [
				functionDependencyProvider.functionDependencies(it)
			]
			val dependencies = Util.distinctBy(deps + condDeps, [name]).sortBy[it.name]

			concatenator.append(functionClass(func, dependencies, javaNames))
			val content = '''
				package «javaNames.packages.functions.packageName»;
				
				«FOR _import : concatenator.imports»
					import «_import»;
				«ENDFOR»
				«FOR staticImport : concatenator.staticImports»
					import static «staticImport»;
				«ENDFOR»
				«IF (!func.conditions.nullOrEmpty || !func.postConditions.nullOrEmpty) /*FIXME add static imports */»
					
					import java.math.BigDecimal;
					import org.isda.cdm.*;
					import com.rosetta.model.lib.meta.*;
					import static com.rosetta.model.lib.validation.ValidatorHelper.*;
					
				«ENDIF»
				«concatenator.toString»
			'''
			fsa.generateFile(fileName, content)
		} catch (Exception e) {
			throw new UnsupportedOperationException('Unable to generate code for: ' + fileName, e)
		}
	}

	private def StringConcatenationClient functionClass(Function func,
		Iterable<? extends RosettaCallableWithArgs> dependencies, extension JavaNames names) {
		val isAbstract = func.operations.nullOrEmpty
		val outputName = getOutput(func)?.name
		val outputType = func.outputTypeOrVoid(names)
		val aliasOut = func.shortcuts.toMap([it], [exprHelper.usesOutputParameter(it.expression)])
		'''
			«IF isAbstract»@«ImplementedBy»(«func.name»Impl.class)«ENDIF»
			public «IF isAbstract»abstract«ENDIF» class «func.name» implements «RosettaFunction» {
				«IF !dependencies.empty»
					
					// RosettaFunction dependencies
					//
				«ENDIF»
				«FOR dep : dependencies»
					@«Inject» protected «dep.toJavaQualifiedType» «dep.name.toFirstLower»;
				«ENDFOR»
			
				/**
				«FOR input : getInputs(func)»
					* @param «input.name» «input.definition»
				«ENDFOR»
				«IF getOutput(func) !== null»
					* @return «outputName» «getOutput(func).definition»
				«ENDIF»
				*/
				public «outputType» evaluate(«func.inputsAsParameters(names)») {
					«IF !func.conditions.empty»
						// pre-conditions
						//
						«FOR cond:func.conditions»
							«cond.contributeCondition»
						«ENDFOR»
					«ENDIF»
					
					«IF isAbstract»
						«IF getOutput(func) !== null»«getOutput(func).toBuilderType(names)» «outputName»Builder = «ENDIF»doEvaluate(«func.inputsAsArguments(names)»);
					«ELSE»
						«IF getOutput(func) !== null»
						«getOutput(func).toBuilderType(names)» «outputName»Builder = «IF getOutput(func).type.needsBuilder»«getOutput(func).toJavaQualifiedType».builder()«ELSE»null;«ENDIF»
						«ENDIF»
						«FOR indexed : func.operations.indexed»
							«val operation = indexed.value»
							«operation.assign(aliasOut, names)»;
						«ENDFOR»
					«ENDIF»
					
					«outputType» «outputName» = «outputName»Builder«IF getOutput(func).type.needsBuilder».build()«ENDIF»;
					«IF !func.postConditions.empty»
						// post-conditions
						//
						«FOR cond:func.postConditions»
							«cond.contributeCondition»
						«ENDFOR»
					«ENDIF»
					return «outputName»;
				}
				«IF isAbstract»
					protected abstract «getOutput(func).toBuilderType(names)» doEvaluate(«func.inputsAsParameters(names)»);
				«ENDIF»
				«FOR alias : func.shortcuts»
					«IF aliasOut.get(alias)»
						protected «names.shortcutJavaType(alias)» «alias.name»(«getOutput(func).toBuilderType(names)» «outputName», «IF !getInputs(func).empty»«func.inputsAsParameters(names)»«ENDIF») {
							return «expressionWithBuilder.javaCode(alias.expression, Context.create(names))»;
						}
					«ELSE»
						protected «MapperBuilder»<«toJavaType(typeProvider.getRType(alias.expression))»> «alias.name»(«func.inputsAsParameters(names)») {
							return «expressionGenerator.javaCode(alias.expression, new ParamMap)»;
						}
					«ENDIF»
				«ENDFOR»
			}
		'''
	}

	private def StringConcatenationClient assign(Operation operation, Map<ShortcutDeclaration, Boolean> outs, JavaNames names) {
		val pathAsList = operation.path.asSegmentList
		val ctx = Context.create(names)
		if(pathAsList.isEmpty)
		'''
			«IF operation.assignRoot.needsBuilder»
			«operation.assignTarget(outs, names)»
				.«IF operation.assignRoot.isMany»add«ELSE»set«ENDIF»«operation.assignRoot.name.toFirstUpper»(«expressionWithBuilder.javaCode(operation.expression, ctx)»)
			«ELSE»
			«operation.assignTarget(outs, names)» = «expressionWithBuilder.javaCode(operation.expression, ctx)»
			«ENDIF»
		'''
		else
		'''
			«operation.assignTarget(outs, names)»
				«FOR seg : pathAsList»«IF seg.next !== null».getOrCreate«seg.attribute.name.toFirstUpper»(«IF seg.attribute.many»«seg.index»«ENDIF»)«ELSE»
				.«IF seg.attribute.isMany»add«ELSE»set«ENDIF»«seg.attribute.name.toFirstUpper»(«expressionGenerator.javaCode(operation.expression, new ParamMap)»)«ENDIF»«ENDFOR»;
		'''
	}

	private def StringConcatenationClient assignTarget(Operation operation, Map<ShortcutDeclaration, Boolean> outs, JavaNames names) {
		val root = operation.assignRoot
		switch (root) {
			Attribute: '''«root.name»Builder'''
			ShortcutDeclaration: 
			'''«root.name»(«IF outs.get(root)»«getOutput(operation.function)?.name»Builder«IF !getInputs(operation.function).empty», «ENDIF»«ENDIF»«inputsAsArguments(operation.function, names)»)'''
		}
	}

	private def StringConcatenationClient contributeCondition(Condition condition) {
		'''
			assert
				«FOR expr : condition.expressions SEPARATOR ' &&'» 
					«expressionGenerator.javaCode(expr, null)».get()
				«ENDFOR»
					: "«condition.definition»";
		'''
	}

	private def JavaType outputTypeOrVoid(Function function, extension JavaNames names) {
		val out = getOutput(function)
		if (out === null) {
			JavaType.create('void')
		} else {
			out.type.toJavaType()
		}
	}

	private def StringConcatenationClient inputsAsArguments(extension Function function, extension JavaNames names) {
		'''«FOR input : getInputs(function) SEPARATOR ', '»«input.name»«ENDFOR»'''
	}

	private def StringConcatenationClient inputsAsParameters(extension Function function, extension JavaNames names) {
		'''«FOR input : getInputs(function) SEPARATOR ', '»«input.toJavaQualifiedType()» «input.name»«ENDFOR»'''
	}

	def private StringConcatenationClient shortcutJavaType(JavaNames names, ShortcutDeclaration feature) {
		val rType = typeProvider.getRType(feature.expression)
		val javaType = names.toJavaType(rType)
		'''«javaType»«IF rType.needsBuilder».«javaType»Builder«ENDIF»'''
	}

	private def StringConcatenationClient toBuilderType(Attribute attr, JavaNames names) {
		val javaType = names.toJavaType(attr.type)
		'''«javaType»«IF attr.type.needsBuilder».«javaType»Builder«ENDIF»'''
	}

	private def boolean needsBuilder(AssignPathRoot root) {
		switch (root) {
			Attribute: root.type.needsBuilder
			ShortcutDeclaration: typeProvider.getRType(root.expression).needsBuilder
			default: false
		}
	}
	private def boolean needsBuilder(RosettaType type) {
		switch (type) {
			RosettaClass,
			Data: true
			default: false
		}
	}

	private def boolean needsBuilder(RType type) {
		switch (type) {
			RClassType,
			RDataType: true
			default: false
		}
	}

	private def List<Segment> asSegmentList(Segment segment) {
		val result = newArrayList
		if (segment !== null) {
			result.add(segment)
			val segmentNext = segment?.next
			if (segmentNext !== null) {
				result.addAll(asSegmentList(segmentNext))
			}
		}
		return result
	}

	private def isMany(AssignPathRoot root) {
		switch(root) {
			Attribute: root.card.isMany
		}
	}
	private def isMany(RosettaFeature feature) {
		switch (feature) {
			RosettaRegularAttribute: feature.card.isMany
			Attribute: feature.card.isMany
			default: throw new IllegalStateException('Unsupported type passed ' + feature?.eClass?.name)
		}
	}
}
