package com.regnosys.rosetta.generator.java.function

import com.google.inject.ImplementedBy
import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.expression.ExpressionGenerator
import com.regnosys.rosetta.generator.java.expression.ExpressionGenerator.ParamMap
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.generator.java.util.JavaNames
import com.regnosys.rosetta.generator.java.util.JavaType
import com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil
import com.regnosys.rosetta.generator.java.util.ParameterizedType
import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions
import com.regnosys.rosetta.generator.util.Util
import com.regnosys.rosetta.rosetta.RosettaCallable
import com.regnosys.rosetta.rosetta.expression.RosettaCallableCall
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs
import com.regnosys.rosetta.rosetta.expression.RosettaCallableWithArgsCall
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.expression.RosettaExpression
import com.regnosys.rosetta.rosetta.RosettaFeature
import com.regnosys.rosetta.rosetta.expression.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.RosettaNamed
import com.regnosys.rosetta.rosetta.simple.Annotated
import com.regnosys.rosetta.rosetta.simple.AssignOutputOperation
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.Condition
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.simple.FunctionDispatch
import com.regnosys.rosetta.rosetta.simple.Operation
import com.regnosys.rosetta.rosetta.simple.OutputOperation
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration
import com.regnosys.rosetta.types.RAnnotateType
import com.regnosys.rosetta.types.RType
import com.regnosys.rosetta.types.RosettaTypeProvider
import com.regnosys.rosetta.utils.ExpressionHelper
import com.rosetta.model.lib.functions.ConditionValidator
import com.rosetta.model.lib.functions.IQualifyFunctionExtension
import com.rosetta.model.lib.functions.ModelObjectValidator
import com.rosetta.model.lib.functions.RosettaFunction
import com.rosetta.model.lib.mapper.Mapper
import java.util.ArrayList
import java.util.List
import java.util.Map
import java.util.Optional
import java.util.stream.Collectors
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.naming.QualifiedName

import static com.regnosys.rosetta.generator.java.enums.EnumHelper.*
import static com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil.*
import com.regnosys.rosetta.rosetta.expression.RosettaUnaryOperation

class FunctionGenerator {

	@Inject ExpressionGenerator expressionGenerator
	@Inject FunctionDependencyProvider functionDependencyProvider
	@Inject RosettaTypeProvider typeProvider
	@Inject extension RosettaFunctionExtensions
	@Inject extension RosettaExtensions
	@Inject ExpressionHelper exprHelper
	@Inject extension ImportManagerExtension
	@Inject CardinalityProvider cardinality
	@Inject JavaNames.Factory factory
	@Inject extension Util

	def void generate(JavaNames javaNames, IFileSystemAccess2 fsa, Function func, String version) {
		val fileName = javaNames.packages.model.functions.directoryName + '/' + func.name + '.java'

		val dependencies = collectFunctionDependencies(func)

		val classBody = if (func.handleAsEnumFunction) {
				tracImports(func.dispatchClassBody(func.name, dependencies, javaNames, version), func.name)
			} else {
				tracImports(func.classBody(func.name, dependencies, javaNames, version, false), func.name)
			}
        
        dependencies.getFuncOutputTypes(javaNames).toSet.forEach[classBody.addImport(it, it)]
        
		val content = '''
			package «javaNames.packages.model.functions.name»;
			
			«FOR imp : classBody.imports»
				import «imp»;
			«ENDFOR»
			
			«FOR imp : classBody.staticImports»
				import static «imp»;
			«ENDFOR»
			
			«classBody.toString»
		'''
		fsa.generateFile(fileName, content)
	}
	
	private def collectFunctionDependencies(Function func) {
		val deps = func.shortcuts.flatMap[functionDependencyProvider.functionDependencies(it.expression)] +
			func.operations.flatMap[functionDependencyProvider.functionDependencies(it.expression)]
		val condDeps = (func.conditions + func.postConditions).map[expression].flatMap [
			functionDependencyProvider.functionDependencies(it)
		]
		return Util.distinctBy(deps + condDeps, [name]).sortBy[it.name]
	}

	private def StringConcatenationClient classBody(Function func, String className,
		Iterable<? extends RosettaCallableWithArgs> dependencies, extension JavaNames names, String version, boolean isStatic) {
		val output = getOutput(func)
		val inputs = getInputs(func)
		val outputName = output?.name
		val outputType = func.outputTypeOrVoid(names)
		val aliasOut = func.shortcuts.toMap([it], [exprHelper.usesOutputParameter(it.expression)])
		val outNeedsBuilder = needsBuilder(output)
		'''
			@«ImplementedBy»(«className».«className»Default.class)
			public «IF isStatic»static «ENDIF»abstract class «className» implements «RosettaFunction»«IF func.isQualifierFunction()», «IQualifyFunctionExtension»<«inputs.head.toListOrSingleJavaType»>«ENDIF» {
				«IF !func.conditions.empty || !func.postConditions.empty»
					
					@«Inject» protected «ConditionValidator» conditionValidator;
				«ENDIF»
				«IF outNeedsBuilder»
					
					@«Inject» protected «ModelObjectValidator» objectValidator;
				«ENDIF»
				«IF !dependencies.empty»
					
					// RosettaFunction dependencies
					//
				«ENDIF»
				«FOR dep : dependencies»
					@«Inject» protected «dep.toJavaType» «dep.name.toFirstLower»;
				«ENDFOR»
			
				/**
				«FOR input : inputs»
					* @param «input.name» «ModelGeneratorUtil.escape(input.definition)»
				«ENDFOR»
				«IF output !== null»
					* @return «outputName» «ModelGeneratorUtil.escape(output.definition)»
				«ENDIF»
				*/
				public «IF outNeedsBuilder»«outputType.extendedParam»«ELSE»«outputType»«ENDIF» evaluate(«func.inputsAsParameters(names)») {
					«IF !func.conditions.empty»
						// pre-conditions
						«FOR cond:func.conditions»
							«cond.contributeCondition»
							
						«ENDFOR»
					«ENDIF»
					«output.toBuilderType(names)» «outputName» = doEvaluate(«func.inputsAsArguments(names)»);
					
					«IF !func.postConditions.empty»
						// post-conditions
						«FOR cond:func.postConditions»
							«cond.contributeCondition»
							
						«ENDFOR»
					«ENDIF»
					«IF outNeedsBuilder»
					if («outputName» != null) {
						objectValidator.validate(«names.toJavaType(output.type)».class, «outputName»);
					}
					«ENDIF»
					return «outputName»;
				}
			
				protected abstract «output.toBuilderType(names)» doEvaluate(«func.inputsAsParameters(names)»);
			«FOR alias : func.shortcuts»
				«IF aliasOut.get(alias)»
					«val multi = cardinality.isMulti(alias.expression)»
					«val returnType = names.shortcutJavaType(alias)»
				
					protected abstract «IF multi»«List»<«returnType»>«ELSE»«returnType»«ENDIF» «alias.name»(«output.toBuilderType(names)» «outputName», «IF !inputs.empty»«func.inputsAsParameters(names)»«ENDIF»);
				«ELSE»
				
					protected abstract «IF needsBuilder(alias)»«Mapper»<? extends «toJavaType(typeProvider.getRType(alias.expression))»>«ELSE»«Mapper»<«toJavaType(typeProvider.getRType(alias.expression))»>«ENDIF» «alias.name»(«func.inputsAsParameters(names)»);
				«ENDIF»
			«ENDFOR»
			
				public static class «className»Default extends «className» {
					@Override
					protected «output.toBuilderType(names)» doEvaluate(«func.inputsAsParameters(names)») {
						«output.toBuilderType(names)» «outputName» = «IF output.isMany»new «ArrayList»<>()«ELSEIF outNeedsBuilder»«output.toListOrSingleJavaType».builder()«ELSE»null«ENDIF»;
						return assignOutput(«outputName»«IF !inputs.empty», «ENDIF»«func.inputsAsArguments(names)»);
					}
					
					protected «output.toBuilderType(names)» assignOutput(«output.toBuilderType(names)» «outputName»«IF !inputs.empty», «ENDIF»«func.inputsAsParameters(names)») {
						«FOR indexed : func.operations.filter(Operation).indexed»
							«IF indexed.value instanceof AssignOutputOperation»
								«assign(indexed.value as AssignOutputOperation, aliasOut, names, output)»
								
							«ELSEIF indexed.value instanceof OutputOperation»
								«assign(indexed.value as OutputOperation, aliasOut, names, output, indexed.key)»
								
							«ENDIF»
						«ENDFOR»
						return «IF !needsBuilder(output)»«outputName»«ELSE»«Optional».ofNullable(«outputName»)
							.map(«IF output.isMany»o -> o.stream().map(i -> i.prune()).collect(«Collectors».toList())«ELSE»o -> o.prune()«ENDIF»)
							.orElse(null)«ENDIF»;
					}
					«FOR alias : func.shortcuts»
						«IF aliasOut.get(alias)»
							«val multi = cardinality.isMulti(alias.expression)»
							«val returnType = names.shortcutJavaType(alias)»
							
							@Override
							protected «IF multi»«List»<«returnType»>«ELSE»«returnType»«ENDIF» «alias.name»(«output.toBuilderType(names)» «outputName», «IF !inputs.empty»«func.inputsAsParameters(names)»«ENDIF») {
								return toBuilder(«expressionGenerator.javaCode(alias.expression, new ParamMap)»«IF multi».getMulti()«ELSE».get()«ENDIF»);
							}
						«ELSE»
							
							@Override
							protected «IF needsBuilder(alias)»«Mapper»<? extends «toJavaType(typeProvider.getRType(alias.expression))»>«ELSE»«Mapper»<«toJavaType(typeProvider.getRType(alias.expression))»>«ENDIF» «alias.name»(«func.inputsAsParameters(names)») {
								return «expressionGenerator.javaCode(alias.expression, new ParamMap)»;
							}
						«ENDIF»
					«ENDFOR»
				}
				«IF func.isQualifierFunction()»
				
				@Override
				public String getNamePrefix() {
					return "«getQualifierAnnotations(func).head.annotation.prefix»";
				}
				«ENDIF»
			}
		'''
	}
	
	def private StringConcatenationClient dispatchClassBody(Function function, String className, Iterable<? extends RosettaCallableWithArgs> dependencies, extension JavaNames names, String version) {
		val dispatchingFuncs = function.dispatchingFunctions.sortBy[name].toList
		val enumParam = function.inputs.filter[type instanceof RosettaEnumeration].head.name
		val outputType = function.outputTypeOrVoid(names)
		'''
		«javadoc(function, version)»
		public class «className» {
			«FOR dep : dependencies»
				@«Inject» protected «dep.toJavaType» «dep.name.toFirstLower»;
			«ENDFOR»
			
			«FOR enumFunc : dispatchingFuncs»
				@«Inject» protected «toTargetClassName(enumFunc)» «toTargetClassName(enumFunc).lastSegment»;
			«ENDFOR»
			
			public «outputType.extendedParam» evaluate(«function.inputsAsParameters(names)») {
				switch («enumParam») {
					«FOR enumFunc : dispatchingFuncs»
						case «toEnumClassName(enumFunc).lastSegment»:
							return «toTargetClassName(enumFunc).lastSegment».evaluate(«function.inputsAsArguments(names)»);
					«ENDFOR»
					default:
						throw new IllegalArgumentException("Enum value not implemented: " + «enumParam»);
				}
			}
			
			«FOR enumFunc : dispatchingFuncs»
			
			«val enumValClass = toTargetClassName(enumFunc).lastSegment»
			«enumFunc.classBody(enumValClass, collectFunctionDependencies(enumFunc), names,  version, true)»
			«ENDFOR»
		}'''
	}
	
	
	private def QualifiedName toTargetClassName(FunctionDispatch ele) {
		return QualifiedName.create(ele.name).append(ele.value.value.name.toFirstLower + "_") // to avoid name clashes
	}
	
	private def QualifiedName toEnumClassName(FunctionDispatch ele) {
		return QualifiedName.create(ele.name).append(formatEnumName(ele.value.value.name))
	}
	
	private def StringConcatenationClient assign(AssignOutputOperation op, Map<ShortcutDeclaration, Boolean> outs, JavaNames names, Attribute type) {
		val pathAsList = op.pathAsSegmentList
		if (pathAsList.isEmpty)
			'''
			«IF needsBuilder(op.assignRoot)»
				«op.assignTarget(outs, names)» = toBuilder(«assignPlainValue(op, type.isMany)»);
			«ELSE»
				«op.assignTarget(outs, names)» = «assignPlainValue(op, type.isMany)»;«ENDIF»'''
		else {
			'''
				«op.assignTarget(outs, names)»
					«FOR seg : pathAsList»«IF seg.next !== null».getOrCreate«seg.attribute.name.toFirstUpper»(«IF seg.attribute.many»0«ENDIF»)
					«IF isReference(seg.attribute)».getOrCreateValue()«ENDIF»«ELSE»
					.«IF seg.attribute.isMany»add«ELSE»set«ENDIF»«seg.attribute.name.toFirstUpper»«IF seg.attribute.isReference && !op.assignAsKey»Value«ENDIF»(«op.assignValue(op.assignAsKey, names)»)«ENDIF»«ENDFOR»;
			'''
		}
	}
	
	private def StringConcatenationClient assign(OutputOperation op, Map<ShortcutDeclaration, Boolean> outs, JavaNames names, Attribute type, int index) {
		val pathAsList = op.pathAsSegmentList
		
		if (pathAsList.isEmpty) {
			// assign function output object
			if (op.add) {
				val addVarName = ("addVar" + index).toDecoratedName
				'''
				«IF needsBuilder(op.assignRoot)»
					«type.toBuilderType(names)» «addVarName» = toBuilder(«assignPlainValue(op, type.isMany)»);
				«ELSE»
					«type.toBuilderType(names)» «addVarName» = «assignPlainValue(op, type.isMany)»;«ENDIF»
				«op.assignTarget(outs, names)».addAll(«addVarName»);'''	
			} else {
				'''
				«IF needsBuilder(op.assignRoot)»
					«op.assignTarget(outs, names)» = toBuilder(«assignPlainValue(op, type.isMany)»);
				«ELSE»
					«op.assignTarget(outs, names)» = «assignPlainValue(op, type.isMany)»;«ENDIF»'''	
			}
		} else { // assign an attribute of the function output object
			'''
			«op.assignTarget(outs, names)»
				«FOR seg : pathAsList»
					«IF seg.next !== null».getOrCreate«seg.attribute.name.toFirstUpper»(«IF seg.attribute.many»0«ENDIF»)«IF isReference(seg.attribute)».getOrCreateValue()«ENDIF»
					«ELSE».«IF op.add»add«ELSE»set«ENDIF»«seg.attribute.name.toFirstUpper»«IF seg.attribute.isReference && !op.assignAsKey»Value«ENDIF»(«op.assignValue(op.assignAsKey, names, seg.attribute.many)»);«ENDIF»
				«ENDFOR»
			'''
		}
	}
	
	private def JavaType referenceWithMetaJavaType(Operation op, JavaNames names) {
			if (op.path === null) {
				val valueRType = typeProvider.getRType(op.assignRoot)
			 	names.createJavaType(names.packages.model.metaField, "ReferenceWithMeta" + valueRType.name.toFirstUpper)
			} else {
				val attr = op.pathAsSegmentList.last.attribute
				val valueRType = typeProvider.getRType(attr)
			 	names.createJavaType(factory.create(attr.type.model).packages.model.metaField, "ReferenceWithMeta" + valueRType.name.toFirstUpper)
			}
	}
	
	private def StringConcatenationClient assignValue(Operation op, boolean assignAsKey, JavaNames names) {
		assignValue(op, assignAsKey, names, cardinality.isMulti(op.expression))
	}
	
	private def StringConcatenationClient assignValue(Operation op, boolean assignAsKey, JavaNames names, boolean isAssigneeMulti) {
		if (assignAsKey) {
			val metaClass = referenceWithMetaJavaType(op, names)
			if (cardinality.isMulti(op.expression)) {
				'''
					«expressionGenerator.javaCode(op.expression, new ParamMap)»
						.getItems()
						.map(_item -> «metaClass».builder()
							.setExternalReference(_item.getMappedObject().getMeta().getExternalKey())
							.setGlobalReference(_item.getMappedObject().getMeta().getGlobalKey())
							.build())
						.collect(«Collectors».toList())
				'''
			} else {
				'''
					«metaClass».builder()
						.setGlobalReference(«Optional».ofNullable(«expressionGenerator.javaCode(op.expression, new ParamMap)».get())
							.map(_r -> _r.getMeta())
							.map(_m -> _m.getGlobalKey())
							.orElse(null))
						.setExternalReference(«Optional».ofNullable(«expressionGenerator.javaCode(op.expression, new ParamMap)».get())
							.map(_r -> _r.getMeta())
							.map(_m -> _m.getExternalKey())
							.orElse(null))
						.build()
				'''
			}
		} else {
			assignPlainValue(op, isAssigneeMulti)
		}
	}
	
	private def StringConcatenationClient assignPlainValue(Operation operation, boolean isAssigneeMulti) {
		'''«expressionGenerator.javaCode(operation.expression,  new ParamMap)»«IF isAssigneeMulti».getMulti()«ELSE».get()«ENDIF»'''
	}
	
	def boolean hasMeta(RType type) {
		if(type instanceof RAnnotateType) {
			type.hasMeta
		}
		
		false
	}
	
	private def boolean isReference(RosettaNamed ele) {
		switch(ele) {
			Annotated: hasMetaDataAnnotations(ele) || hasMetaDataAddress(ele)
			default:false
		}
	}
	
	private def StringConcatenationClient assignTarget(Operation operation, Map<ShortcutDeclaration, Boolean> outs, JavaNames names) {
		val root = operation.assignRoot
		switch (root) {
			Attribute: '''«root.name»'''
			ShortcutDeclaration: unfoldLHSShortcut(root)
		}
	}
	
	private def StringConcatenationClient unfoldLHSShortcut(ShortcutDeclaration shortcut) {
		switch (shortcut.expression) {
			RosettaCallableWithArgsCall: 
				// assign-output for an alias
				'''«shortcut.name»(«expressionGenerator.aliasCallArgs(shortcut)»)'''
			default: 
				'''«lhsExpand(shortcut.expression)»'''
		}		
	}
	
	private def dispatch StringConcatenationClient lhsExpand(RosettaExpression f) {
		throw new IllegalStateException("No implementation for lhsExpand for "+f.class)
	}
	
	private def dispatch StringConcatenationClient lhsExpand(RosettaFeatureCall f) 
	'''«lhsExpand(f.receiver)».«f.feature.lhsFeature»'''
	
	private def dispatch StringConcatenationClient lhsExpand(RosettaCallableCall f) 
	'''«f.callable.lhsExpand»'''
	
	private def dispatch StringConcatenationClient lhsExpand(ShortcutDeclaration f) 
	'''«f.expression.lhsExpand»'''
	
	private def dispatch StringConcatenationClient lhsExpand(RosettaUnaryOperation f) 
	'''«f.argument.lhsExpand»'''
	
	private def dispatch StringConcatenationClient lhsFeature(RosettaFeature f){
		throw new IllegalStateException("No implementation for lhsFeature for "+f.class)
	}
	private def dispatch StringConcatenationClient lhsFeature(Attribute f){
		if (f.many) '''getOrCreate«f.name.toFirstUpper»(0)'''
		else '''getOrCreate«f.name.toFirstUpper»()'''
	}
	
	private def dispatch StringConcatenationClient lhsExpand(RosettaCallable c) {
		throw new IllegalStateException("No implementation for lhsExpand for "+c.class)
	}
	private def dispatch StringConcatenationClient lhsExpand(Attribute c) '''«c.name»'''
	
	private def StringConcatenationClient contributeCondition(Condition condition) {
		'''
			conditionValidator.validate(() -> 
				«expressionGenerator.javaCode(condition.expression, null)», 
					"«condition.definition»");
		'''
	}

	private def ParameterizedType outputTypeOrVoid(Function function, extension JavaNames names) {
		val out = getOutput(function)
		if (out === null) {
			new ParameterizedType(names.voidType(),#[])
		} else {
			out.toListOrSingleJavaType()
		}
	}

	private def StringConcatenationClient inputsAsArguments(extension Function function, extension JavaNames names) {
		'''«FOR input : getInputs(function) SEPARATOR ', '»«input.name»«ENDFOR»'''
	}

	private def StringConcatenationClient inputsAsParameters(extension Function function, extension JavaNames names) {
		'''«FOR input : getInputs(function) SEPARATOR ', '»«IF  input.needsBuilder»«input.toListOrSingleJavaType.extendedParam»«ELSE»«input.toListOrSingleJavaType»«ENDIF» «input.name»«ENDFOR»'''
	}

	def private StringConcatenationClient shortcutJavaType(JavaNames names, ShortcutDeclaration feature) {
		val rType = typeProvider.getRType(feature.expression)
		val javaType = names.toJavaType(rType)
		'''«javaType»«IF needsBuilder(rType)».«javaType»Builder«ENDIF»'''
	}

	private def ParameterizedType toBuilderType(Attribute attr, JavaNames names) {
		var javaType = names.toJavaType(attr.type)
		if (needsBuilder(attr)) javaType = javaType.toBuilderType
		if (attr.isMany) {
			new ParameterizedType(new JavaType(List.name), #[new ParameterizedType(javaType,#[])])
		}
		else {
			new ParameterizedType(javaType, #[])
		}
	}

	private def isMany(RosettaFeature feature) {
		switch (feature) {
			Attribute: feature.card.isMany
			default: throw new IllegalStateException('Unsupported type passed ' + feature?.eClass?.name)
		}
	}
	
	private def getFuncOutputTypes(List<RosettaCallableWithArgs> dependencies, JavaNames javaNames) {
		dependencies
        	.filter[it instanceof Function]
        	.map[it as Function]
        	.map[it.output.type]
        	.map[javaNames.toJavaType(it).name]
	}
}
