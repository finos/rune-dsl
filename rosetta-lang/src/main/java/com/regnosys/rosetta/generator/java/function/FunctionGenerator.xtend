package com.regnosys.rosetta.generator.java.function

import com.google.inject.ImplementedBy
import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.expression.ExpressionGenerator
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.generator.java.util.JavaNames
import com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil
import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions
import com.regnosys.rosetta.generator.util.Util
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs
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
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference
import com.regnosys.rosetta.rosetta.RosettaSymbol
import com.regnosys.rosetta.rosetta.expression.AsKeyOperation
import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.JavaIdentifierRepresentationService
import com.regnosys.rosetta.generator.java.types.JavaType
import com.regnosys.rosetta.generator.java.types.JavaPrimitiveType
import com.regnosys.rosetta.generator.GeneratedIdentifier
import com.regnosys.rosetta.generator.java.types.JavaClass
import com.regnosys.rosetta.generator.java.types.JavaParameterizedType

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
	@Inject extension JavaIdentifierRepresentationService

	def void generate(JavaNames javaNames, IFileSystemAccess2 fsa, Function func, String version) {
		val fileName = javaNames.packages.model.functions.withForwardSlashes + '/' + func.name + '.java'
		
		val topScope = new JavaScope(javaNames.packages.model.functions)
		topScope.createIdentifier(func)
				
		val dependencies = collectFunctionDependencies(func)

		val StringConcatenationClient classBody = if (func.handleAsEnumFunction) {
				func.dispatchClassBody(topScope, dependencies, javaNames, version)
			} else {
				func.classBody(topScope, dependencies, javaNames, version, false)
			}
        
		val content = buildClass(javaNames.packages.model.functions, classBody, topScope)
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

	private def StringConcatenationClient classBody(Function func, JavaScope scope,
		Iterable<? extends Function> dependencies, extension JavaNames names, String version, boolean isStatic) {
		
		val output = getOutput(func)
		val inputs = getInputs(func)
		val outputType = func.outputTypeOrVoid(names)
		val aliasOut = func.shortcuts.toMap([it], [exprHelper.usesOutputParameter(it.expression)])
		val outNeedsBuilder = needsBuilder(output)
		val className = scope.getIdentifierOrThrow(func)
		
		val classScope = scope.classScope(className.desiredName)
		dependencies.forEach[classScope.createIdentifier(it.toFunctionInstance, it.name.toFirstLower)]
		val conditionValidatorId = classScope.createUniqueIdentifier("conditionValidator")
		val objectValidatorId = classScope.createUniqueIdentifier("objectValidator")
		func.shortcuts.forEach[classScope.createIdentifier(it)]
		
		val evaluateScope = classScope.methodScope("evaluate")
		inputs.forEach[evaluateScope.createIdentifier(it)]
		evaluateScope.createIdentifier(output)
		
		val defaultClassScope = classScope.classScope(className.desiredName + "Default")
		val defaultClassName = defaultClassScope.createUniqueIdentifier(className.desiredName + "Default")
		
		val doEvaluateScope = defaultClassScope.methodScope("doEvaluate")
		inputs.forEach[doEvaluateScope.createIdentifier(it)]
		doEvaluateScope.createIdentifier(output)
		
		val assignOutputScope = defaultClassScope.methodScope("assignOutput")
		inputs.forEach[assignOutputScope.createIdentifier(it)]
		assignOutputScope.createIdentifier(output)
		
		val aliasScopes = newHashMap
		func.shortcuts.forEach[
			val aliasScope = defaultClassScope.methodScope(it.name)
			inputs.forEach[aliasScope.createIdentifier(it)]
			if (aliasOut.get(it)) {
				aliasScope.createIdentifier(output)
			}
			aliasScopes.put(it, aliasScope)
		]
		
		'''
			@«ImplementedBy»(«className».«defaultClassName».class)
			public «IF isStatic»static «ENDIF»abstract class «className» implements «RosettaFunction»«IF func.isQualifierFunction()», «IQualifyFunctionExtension»<«typeProvider.getRType(inputs.head.type).toListOrSingleJavaType(inputs.head.card.isMany)»>«ENDIF» {
				«IF !func.conditions.empty || !func.postConditions.empty»
					
					@«Inject» protected «ConditionValidator» «conditionValidatorId»;
				«ENDIF»
				«IF outNeedsBuilder»
					
					@«Inject» protected «ModelObjectValidator» «objectValidatorId»;
				«ENDIF»
				«IF !dependencies.empty»
					
					// RosettaFunction dependencies
					//
				«ENDIF»
				«FOR dep : dependencies»
					@«Inject» protected «dep.toJavaType» «classScope.getIdentifierOrThrow(dep.toFunctionInstance)»;
				«ENDFOR»
			
				/**
				«FOR input : inputs»
					* @param «evaluateScope.getIdentifierOrThrow(input)» «ModelGeneratorUtil.escape(input.definition)»
				«ENDFOR»
				«IF output !== null»
					* @return «evaluateScope.getIdentifierOrThrow(output)» «ModelGeneratorUtil.escape(output.definition)»
				«ENDIF»
				*/
				public «outputType» evaluate(«func.inputsAsParameters(evaluateScope, names)») {
					«IF !func.conditions.empty»
						// pre-conditions
						«FOR cond:func.conditions»
							«cond.contributeCondition(conditionValidatorId, evaluateScope, names)»
							
						«ENDFOR»
					«ENDIF»
					«output.toBuilderType(names)» «evaluateScope.getIdentifierOrThrow(output)» = doEvaluate(«func.inputsAsArguments(evaluateScope, names)»);
					
					«IF !func.postConditions.empty»
						// post-conditions
						«FOR cond:func.postConditions»
							«cond.contributeCondition(conditionValidatorId, evaluateScope, names)»
							
						«ENDFOR»
					«ENDIF»
					«IF outNeedsBuilder»
					if («evaluateScope.getIdentifierOrThrow(output)» != null) {
						«objectValidatorId».validate(«names.toJavaType(typeProvider.getRType(output.type))».class, «evaluateScope.getIdentifierOrThrow(output)»);
					}
					«ENDIF»
					return «evaluateScope.getIdentifierOrThrow(output)»;
				}
			
				protected abstract «output.toBuilderType(names)» doEvaluate(«func.inputsAsParameters(doEvaluateScope, names)»);
			«FOR alias : func.shortcuts»
				«val aliasScope = aliasScopes.get(alias)»
				«IF aliasOut.get(alias)»
					«val multi = cardinality.isMulti(alias.expression)»
					«val returnType = names.shortcutJavaType(alias)»
				
					protected abstract «IF multi»«List»<«returnType»>«ELSE»«returnType»«ENDIF» «classScope.getIdentifierOrThrow(alias)»(«output.toBuilderType(names)» «aliasScope.getIdentifierOrThrow(output)», «IF !inputs.empty»«func.inputsAsParameters(aliasScope, names)»«ENDIF»);
				«ELSE»
				
					protected abstract «IF needsBuilder(alias)»«Mapper»<? extends «toJavaType(typeProvider.getRType(alias.expression))»>«ELSE»«Mapper»<«toJavaType(typeProvider.getRType(alias.expression))»>«ENDIF» «classScope.getIdentifierOrThrow(alias)»(«func.inputsAsParameters(aliasScope, names)»);
				«ENDIF»
			«ENDFOR»
			
				public static class «defaultClassName» extends «className» {
					@Override
					protected «output.toBuilderType(names)» doEvaluate(«func.inputsAsParameters(doEvaluateScope, names)») {
						«output.toBuilderType(names)» «doEvaluateScope.getIdentifierOrThrow(output)» = «IF output.card.isMany»new «ArrayList»<>()«ELSEIF outNeedsBuilder»«typeProvider.getRType(output.type).toListOrSingleJavaType(output.card.isMany)».builder()«ELSE»null«ENDIF»;
						return assignOutput(«doEvaluateScope.getIdentifierOrThrow(output)»«IF !inputs.empty», «ENDIF»«func.inputsAsArguments(doEvaluateScope, names)»);
					}
					
					protected «output.toBuilderType(names)» assignOutput(«output.toBuilderType(names)» «assignOutputScope.getIdentifierOrThrow(output)»«IF !inputs.empty», «ENDIF»«func.inputsAsParameters(assignOutputScope, names)») {
						«FOR indexed : func.operations.filter(Operation).indexed»
							«IF indexed.value instanceof AssignOutputOperation»
								«assign(assignOutputScope, indexed.value as AssignOutputOperation, aliasOut, names, output)»
								
							«ELSEIF indexed.value instanceof OutputOperation»
								«assign(assignOutputScope, indexed.value as OutputOperation, aliasOut, names, output, indexed.key)»
								
							«ENDIF»
						«ENDFOR»
						return «IF !needsBuilder(output)»«assignOutputScope.getIdentifierOrThrow(output)»«ELSE»«Optional».ofNullable(«assignOutputScope.getIdentifierOrThrow(output)»)
							.map(«IF output.card.isMany»o -> o.stream().map(i -> i.prune()).collect(«Collectors».toList())«ELSE»o -> o.prune()«ENDIF»)
							.orElse(null)«ENDIF»;
					}
					«FOR alias : func.shortcuts»
						«val aliasScope = aliasScopes.get(alias)»
						«IF aliasOut.get(alias)»
							«val multi = cardinality.isMulti(alias.expression)»
							«val returnType = names.shortcutJavaType(alias)»
							
							@Override
							protected «IF multi»«List»<«returnType»>«ELSE»«returnType»«ENDIF» «classScope.getIdentifierOrThrow(alias)»(«output.toBuilderType(names)» «aliasScope.getIdentifierOrThrow(output)», «IF !inputs.empty»«func.inputsAsParameters(aliasScope, names)»«ENDIF») {
								return toBuilder(«expressionGenerator.javaCode(alias.expression, aliasScope, names)»«IF multi».getMulti()«ELSE».get()«ENDIF»);
							}
						«ELSE»
							
							@Override
							protected «IF needsBuilder(alias)»«Mapper»<? extends «toJavaType(typeProvider.getRType(alias.expression))»>«ELSE»«Mapper»<«toJavaType(typeProvider.getRType(alias.expression))»>«ENDIF» «classScope.getIdentifierOrThrow(alias)»(«func.inputsAsParameters(aliasScope, names)») {
								return «expressionGenerator.javaCode(alias.expression, aliasScope, names)»;
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
	
	def private StringConcatenationClient dispatchClassBody(Function function, JavaScope topScope, Iterable<? extends Function> dependencies, extension JavaNames names, String version) {
		val dispatchingFuncs = function.dispatchingFunctions.sortBy[name].toList
		val enumParam = function.inputs.filter[type instanceof RosettaEnumeration].head.name
		val outputType = function.outputTypeOrVoid(names)
		val className = topScope.getIdentifierOrThrow(function)
		
		val classScope = topScope.classScope(className.desiredName)
		dispatchingFuncs.forEach[classScope.createIdentifier(it, toTargetClassName.lastSegment)]
		
		val evaluateScope = classScope.methodScope("evaluate")
		function.inputs.forEach[evaluateScope.createIdentifier(it)]
		'''
		«javadoc(function, version)»
		public class «className» {
			«FOR dep : dependencies»
				@«Inject» protected «dep.toJavaType» «dep.name.toFirstLower»;
			«ENDFOR»
			
			«FOR enumFunc : dispatchingFuncs»
				@«Inject» protected «toTargetClassName(enumFunc)» «toTargetClassName(enumFunc).lastSegment»;
			«ENDFOR»
			
			public «outputType» evaluate(«function.inputsAsParameters(evaluateScope, names)») {
				switch («enumParam») {
					«FOR enumFunc : dispatchingFuncs»
						case «toEnumClassName(enumFunc).lastSegment»:
							return «classScope.getIdentifierOrThrow(enumFunc)».evaluate(«function.inputsAsArguments(evaluateScope, names)»);
					«ENDFOR»
					default:
						throw new IllegalArgumentException("Enum value not implemented: " + «enumParam»);
				}
			}
			
			«FOR enumFunc : dispatchingFuncs»
			
			«enumFunc.classBody(classScope, collectFunctionDependencies(enumFunc), names,  version, true)»
			«ENDFOR»
		}'''
	}
	
	
	private def QualifiedName toTargetClassName(FunctionDispatch ele) {
		return QualifiedName.create(ele.name).append(ele.value.value.name.toFirstLower + "_") // to avoid name clashes
	}
	
	private def QualifiedName toEnumClassName(FunctionDispatch ele) {
		return QualifiedName.create(ele.name).append(formatEnumName(ele.value.value.name))
	}
	
	private def StringConcatenationClient assign(JavaScope scope, AssignOutputOperation op, Map<ShortcutDeclaration, Boolean> outs, JavaNames names, Attribute type) {
		val pathAsList = op.pathAsSegmentList
		if (pathAsList.isEmpty)
			'''
			«IF needsBuilder(op.assignRoot)»
				«op.assignTarget(outs, scope, names)» = toBuilder(«assignPlainValue(scope, names, op, type.card.isMany)»);
			«ELSE»
				«op.assignTarget(outs, scope, names)» = «assignPlainValue(scope, names, op, type.card.isMany)»;«ENDIF»'''
		else {
			'''
				«op.assignTarget(outs, scope, names)»
					«FOR seg : pathAsList»«IF seg.next !== null».getOrCreate«seg.attribute.name.toFirstUpper»(«IF seg.attribute.card.isMany»0«ENDIF»)
					«IF isReference(seg.attribute)».getOrCreateValue()«ENDIF»«ELSE»
					.«IF seg.attribute.card.isMany»add«ELSE»set«ENDIF»«seg.attribute.name.toFirstUpper»«IF seg.attribute.isReference && !op.assignAsKey»Value«ENDIF»(«assignValue(scope, op, op.assignAsKey, names)»)«ENDIF»«ENDFOR»;
			'''
		}
	}
	
	private def boolean assignAsKey(Operation op) {
		return op.expression instanceof AsKeyOperation
	}
	
	private def StringConcatenationClient assign(JavaScope scope, OutputOperation op, Map<ShortcutDeclaration, Boolean> outs, JavaNames names, Attribute type, int index) {
		val pathAsList = op.pathAsSegmentList
		
		if (pathAsList.isEmpty) {
			// assign function output object
			if (op.add) {
				val addVarName = scope.createUniqueIdentifier("addVar")
				'''
				«IF needsBuilder(op.assignRoot)»
					«type.toBuilderType(names)» «addVarName» = toBuilder(«assignPlainValue(scope, names, op, type.card.isMany)»);
				«ELSE»
					«type.toBuilderType(names)» «addVarName» = «assignPlainValue(scope, names, op, type.card.isMany)»;«ENDIF»
				«op.assignTarget(outs, scope, names)».addAll(«addVarName»);'''	
			} else {
				'''
				«IF needsBuilder(op.assignRoot)»
					«op.assignTarget(outs, scope, names)» = toBuilder(«assignPlainValue(scope, names, op, type.card.isMany)»);
				«ELSE»
					«op.assignTarget(outs, scope, names)» = «assignPlainValue(scope, names, op, type.card.isMany)»;«ENDIF»'''	
			}
		} else { // assign an attribute of the function output object
			'''
			«op.assignTarget(outs, scope, names)»
				«FOR seg : pathAsList»
					«IF seg.next !== null».getOrCreate«seg.attribute.name.toFirstUpper»(«IF seg.attribute.card.isMany»0«ENDIF»)«IF isReference(seg.attribute)».getOrCreateValue()«ENDIF»
					«ELSE».«IF op.add»add«ELSE»set«ENDIF»«seg.attribute.name.toFirstUpper»«IF seg.attribute.isReference && !op.assignAsKey»Value«ENDIF»(«assignValue(scope, op, op.assignAsKey, names, seg.attribute.card.isMany)»);«ENDIF»
				«ENDFOR»
			'''
		}
	}
	
	private def JavaType referenceWithMetaJavaType(Operation op, JavaNames names) {
			if (op.path === null) {
				val valueRType = typeProvider.getRType(op.assignRoot)
			 	new JavaClass(names.packages.model.metaField, "ReferenceWithMeta" + valueRType.name.toFirstUpper)
			} else {
				val attr = op.pathAsSegmentList.last.attribute
				val valueRType = typeProvider.getRType(attr)
			 	new JavaClass(factory.create(attr.type.model).packages.model.metaField, "ReferenceWithMeta" + valueRType.name.toFirstUpper)
			}
	}
	
	private def StringConcatenationClient assignValue(JavaScope scope, Operation op, boolean assignAsKey, JavaNames names) {
		assignValue(scope, op, assignAsKey, names, cardinality.isMulti(op.expression))
	}
	
	private def StringConcatenationClient assignValue(JavaScope scope, Operation op, boolean assignAsKey, JavaNames names, boolean isAssigneeMulti) {
		if (assignAsKey) {
			val metaClass = referenceWithMetaJavaType(op, names)
			if (cardinality.isMulti(op.expression)) {
				val lambdaScope = scope.lambdaScope
				val item = lambdaScope.createUniqueIdentifier("item")
				'''
					«expressionGenerator.javaCode(op.expression, scope, names)»
						.getItems()
						.map(«item» -> «metaClass».builder()
							.setExternalReference(«item».getMappedObject().getMeta().getExternalKey())
							.setGlobalReference(«item».getMappedObject().getMeta().getGlobalKey())
							.build())
						.collect(«Collectors».toList())
				'''
			} else {
				val lambdaScope = scope.lambdaScope
				val r = lambdaScope.createUniqueIdentifier("r")
				val m = lambdaScope.createUniqueIdentifier("m")
				'''
					«metaClass».builder()
						.setGlobalReference(«Optional».ofNullable(«expressionGenerator.javaCode(op.expression, scope, names)».get())
							.map(«r» -> «r».getMeta())
							.map(«m» -> «m».getGlobalKey())
							.orElse(null))
						.setExternalReference(«Optional».ofNullable(«expressionGenerator.javaCode(op.expression, scope, names)».get())
							.map(«r» -> «r».getMeta())
							.map(«m» -> «m».getExternalKey())
							.orElse(null))
						.build()
				'''
			}
		} else {
			assignPlainValue(scope, names, op, isAssigneeMulti)
		}
	}
	
	private def StringConcatenationClient assignPlainValue(JavaScope scope, JavaNames names, Operation operation, boolean isAssigneeMulti) {
		'''«expressionGenerator.javaCode(operation.expression, scope, names)»«IF isAssigneeMulti».getMulti()«ELSE».get()«ENDIF»'''
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
	
	private def StringConcatenationClient assignTarget(Operation operation, Map<ShortcutDeclaration, Boolean> outs, JavaScope scope, JavaNames names) {
		val root = operation.assignRoot
		switch (root) {
			Attribute: '''«scope.getIdentifierOrThrow(root)»'''
			ShortcutDeclaration: unfoldLHSShortcut(root, scope)
		}
	}
	
	private def StringConcatenationClient unfoldLHSShortcut(ShortcutDeclaration shortcut, JavaScope scope) {
		val e = shortcut.expression
		if (e instanceof RosettaSymbolReference) {
			if (e.symbol instanceof RosettaCallableWithArgs) {
				// assign-output for an alias
				return '''«scope.getIdentifierOrThrow(shortcut)»(«expressionGenerator.aliasCallArgs(shortcut)»)'''
			}
		}
		return '''«lhsExpand(e, scope)»'''	
	}
	
	private def dispatch StringConcatenationClient lhsExpand(RosettaExpression f, JavaScope scope) {
		throw new IllegalStateException("No implementation for lhsExpand for "+f.class)
	}
	
	private def dispatch StringConcatenationClient lhsExpand(RosettaFeatureCall f, JavaScope scope) 
	'''«lhsExpand(f.receiver, scope)».«f.feature.lhsFeature»'''
	
	private def dispatch StringConcatenationClient lhsExpand(RosettaSymbolReference f, JavaScope scope) 
	'''«f.symbol.lhsExpand(scope)»'''
	
	private def dispatch StringConcatenationClient lhsExpand(ShortcutDeclaration f, JavaScope scope) 
	'''«f.expression.lhsExpand(scope)»'''
	
	private def dispatch StringConcatenationClient lhsExpand(RosettaUnaryOperation f, JavaScope scope) 
	'''«f.argument.lhsExpand(scope)»'''
	
	private def dispatch StringConcatenationClient lhsFeature(RosettaFeature f){
		throw new IllegalStateException("No implementation for lhsFeature for "+f.class)
	}
	private def dispatch StringConcatenationClient lhsFeature(Attribute f){
		if (f.card.isMany) '''getOrCreate«f.name.toFirstUpper»(0)'''
		else '''getOrCreate«f.name.toFirstUpper»()'''
	}
	
	private def dispatch StringConcatenationClient lhsExpand(RosettaSymbol c, JavaScope scope) {
		throw new IllegalStateException("No implementation for lhsExpand for "+c.class)
	}
	private def dispatch StringConcatenationClient lhsExpand(Attribute c, JavaScope scope) '''«scope.getIdentifierOrThrow(c)»'''
	
	private def StringConcatenationClient contributeCondition(Condition condition, GeneratedIdentifier conditionValidator, JavaScope scope, JavaNames names) {
		'''
			«conditionValidator».validate(() -> 
				«expressionGenerator.javaCode(condition.expression, scope.lambdaScope, names)», 
					"«condition.definition»");
		'''
	}

	private def JavaType outputTypeOrVoid(Function function, extension JavaNames names) {
		val out = getOutput(function)
		if (out === null) {
			JavaPrimitiveType.VOID
		} else {
			if (out.type.needsBuilder) {
				typeProvider.getRType(out.type).toPolymorphicListOrSingleJavaType(out.card.isMany)
			} else {
				typeProvider.getRType(out.type).toListOrSingleJavaType(out.card.isMany)
			}
		}
	}

	private def StringConcatenationClient inputsAsArguments(extension Function function, JavaScope scope, extension JavaNames names) {
		'''«FOR input : getInputs(function) SEPARATOR ', '»«scope.getIdentifierOrThrow(input)»«ENDFOR»'''
	}

	private def StringConcatenationClient inputsAsParameters(extension Function function, JavaScope scope, extension JavaNames names) {
		'''«FOR input : getInputs(function) SEPARATOR ', '»«IF input.type.needsBuilder»«typeProvider.getRType(input.type).toPolymorphicListOrSingleJavaType(input.card.isMany)»«ELSE»«typeProvider.getRType(input.type).toListOrSingleJavaType(input.card.isMany)»«ENDIF» «scope.getIdentifierOrThrow(input)»«ENDFOR»'''
	}

	def private StringConcatenationClient shortcutJavaType(JavaNames names, ShortcutDeclaration feature) {
		val rType = typeProvider.getRType(feature.expression)
		val javaType = names.toJavaType(rType)
		'''«javaType»«IF needsBuilder(rType)».«javaType»Builder«ENDIF»'''
	}

	private def JavaType toBuilderType(Attribute attr, JavaNames names) {
		var javaType = names.toJavaType(typeProvider.getRType(attr.type)) as JavaClass
		if (needsBuilder(attr)) javaType = names.toBuilderType(javaType)
		if (attr.card.isMany) {
			return new JavaParameterizedType(names.toJavaType(List), javaType)
		} else {
			return javaType
		}
	}
}
