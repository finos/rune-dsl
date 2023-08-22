package com.regnosys.rosetta.generator.java.function

import com.google.inject.ImplementedBy
import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.GeneratedIdentifier
import com.regnosys.rosetta.generator.java.JavaIdentifierRepresentationService
import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.regnosys.rosetta.generator.java.expression.ExpressionGenerator
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil
import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions
import com.regnosys.rosetta.generator.util.Util
import com.regnosys.rosetta.rosetta.RosettaBlueprint
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.RosettaFeature
import com.regnosys.rosetta.rosetta.RosettaSymbol
import com.regnosys.rosetta.rosetta.expression.AsKeyOperation
import com.regnosys.rosetta.rosetta.expression.RosettaExpression
import com.regnosys.rosetta.rosetta.expression.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference
import com.regnosys.rosetta.rosetta.expression.RosettaUnaryOperation
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.Condition
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.simple.FunctionDispatch
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration
import com.regnosys.rosetta.types.CardinalityProvider
import com.regnosys.rosetta.types.RAttribute
import com.regnosys.rosetta.types.RFunction
import com.regnosys.rosetta.types.RFunctionOrigin
import com.regnosys.rosetta.types.RObjectFactory
import com.regnosys.rosetta.types.ROperation
import com.regnosys.rosetta.types.ROperationType
import com.regnosys.rosetta.types.RShortcut
import com.regnosys.rosetta.types.RosettaTypeProvider
import com.regnosys.rosetta.utils.ExpressionHelper
import com.rosetta.model.lib.functions.ConditionValidator
import com.rosetta.model.lib.functions.IQualifyFunctionExtension
import com.rosetta.model.lib.functions.ModelObjectValidator
import com.rosetta.model.lib.functions.RosettaFunction
import com.rosetta.model.lib.mapper.Mapper
import com.rosetta.util.DottedPath
import com.rosetta.util.types.JavaClass
import com.rosetta.util.types.JavaPrimitiveType
import com.rosetta.util.types.JavaType
import java.util.ArrayList
import java.util.List
import java.util.Map
import java.util.Optional
import java.util.stream.Collectors
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.naming.QualifiedName

import static com.regnosys.rosetta.generator.java.enums.EnumHelper.*
import static com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil.*
import com.regnosys.rosetta.utils.ImplicitVariableUtil
import com.rosetta.util.types.JavaParameterizedType

class FunctionGenerator {

	@Inject ExpressionGenerator expressionGenerator
	@Inject FunctionDependencyProvider functionDependencyProvider
	@Inject RosettaTypeProvider typeProvider
	@Inject extension RosettaFunctionExtensions
	@Inject extension RosettaExtensions
	@Inject ExpressionHelper exprHelper
	@Inject extension ImportManagerExtension
	@Inject CardinalityProvider cardinality
	@Inject extension JavaIdentifierRepresentationService
	@Inject extension JavaTypeTranslator
	@Inject RObjectFactory rTypeBuilderFactory
	@Inject ImplicitVariableUtil implicitVariableUtil

	def void generate(RootPackage root, IFileSystemAccess2 fsa, Function func, String version) {
		val fileName = root.functions.withForwardSlashes + '/' + func.name + '.java'

		val topScope = new JavaScope(root.functions)

		val StringConcatenationClient classBody = if (func.handleAsEnumFunction) {
				val dependencies = collectFunctionDependencies(func)
				topScope.createIdentifier(func)
				func.dispatchClassBody(topScope, dependencies, version, root)
			} else {
				val rFunction = rTypeBuilderFactory.buildRFunction(func)
				var overridesEvaluate = false
				val List<JavaType> functionInterfaces = newArrayList(JavaClass.from(RosettaFunction))
				if (isQualifierFunction(rFunction)) {
					overridesEvaluate = true
					functionInterfaces.add(getQualifyingFunctionInterface(rFunction.inputs))
				}
				rBuildClass(rFunction, false, functionInterfaces, overridesEvaluate, topScope)
			}

		val content = buildClass(root.functions, classBody, topScope)
		fsa.generateFile(fileName, content)
	}
	
	def rBuildClass(RFunction rFunction, boolean isStatic, List<JavaType> functionInterfaces, boolean overridesEvaluate, JavaScope topScope) {
		val dependencies = collectFunctionDependencies(rFunction)
		rFunction.classBody(isStatic, overridesEvaluate, dependencies, functionInterfaces , topScope)
	}
	
	private def getQualifyingFunctionInterface(List<RAttribute> inputs) {
		val parameterVariable = inputs.head.RType.toListOrSingleJavaType(inputs.head.multi)
		new JavaParameterizedType(JavaClass.from(IQualifyFunctionExtension), parameterVariable)
	}

	private def collectFunctionDependencies(Function func) {
		val deps = func.shortcuts.flatMap[functionDependencyProvider.functionDependencies(it.expression)] +
			func.operations.flatMap[functionDependencyProvider.functionDependencies(it.expression)]
		val condDeps = (func.conditions + func.postConditions).map[expression].flatMap [
			functionDependencyProvider.functionDependencies(it)
		]
		return Util.distinctBy(deps + condDeps, [name]).sortBy[it.name]
	}

	private def collectFunctionDependencies(RFunction func) {
		val expressions = func.preConditions.map[it.expression] + 
				func.postConditions.map[it.expression] + 
				func.operations.map[it.expression] +
				func.shortcuts.map[it.expression]
				
		expressions.flatMap[
			val rosettaSymbols = EcoreUtil2.eAllOfType(it, RosettaSymbolReference).map[it.symbol]
			rosettaSymbols.filter(Function).map[rTypeBuilderFactory.buildRFunction(it)] +
			rosettaSymbols.filter(RosettaBlueprint).map[rTypeBuilderFactory.buildRFunction(it)]
		].toSet.sortBy[it.name]
	}

	private def StringConcatenationClient classBody(
		RFunction function,
		boolean isStatic,
		boolean overridesEvaluate,
		List<RFunction> dependencies,
		List<JavaType> functionInterfaces,
		JavaScope scope
	) {
		val className = scope.createIdentifier(function, function.toFunctionJavaClass.simpleName)
		val inputs = function.inputs
		val output = function.output
		val shortcuts = function.shortcuts
		val operations = function.operations
		val preConditions = function.preConditions
		val postConditions = function.postConditions
		
		val classScope = scope.classScope(className.desiredName)
		dependencies.forEach[classScope.createIdentifier(it.toFunctionInstance, it.name.toFirstLower)]
		
		val defaultClassScope = classScope.classScope(className.desiredName + "Default")
		val defaultClassName = defaultClassScope.createUniqueIdentifier(className.desiredName + "Default")
		val outputType = output.attributeToJavaType
		val aliasOut = shortcuts.toMap([it], [exprHelper.usesOutputParameter(it.expression)])

		
		val conditionValidatorId = classScope.createUniqueIdentifier("conditionValidator")
		val objectValidatorId = classScope.createUniqueIdentifier("objectValidator")
		
		val evaluateScope = classScope.methodScope("evaluate")
		inputs.forEach[evaluateScope.createIdentifier(it, it.name)]
		evaluateScope.createIdentifier(output, output.name)
		
		val doEvaluateScope = defaultClassScope.methodScope("doEvaluate")
		inputs.forEach[doEvaluateScope.createIdentifier(it, it.name)]
		doEvaluateScope.createIdentifier(output, output.name)
		
		val assignOutputScope = defaultClassScope.methodScope("assignOutput")
		inputs.forEach[assignOutputScope.createIdentifier(it, it.name)]
		assignOutputScope.createIdentifier(output, output.name)
		function.operations
			.map[it.expression]
			.filter[implicitVariableUtil.implicitVariableExistsInContext(it)]
			.map[it.implicitVarInContext]
			.toSet
			.forEach[assignOutputScope.createKeySynonym(it, inputs.head)]
		
		val aliasScopes = newHashMap
		shortcuts.forEach [
			classScope.createIdentifier(it, it.name)
			val aliasScope = defaultClassScope.methodScope(it.name)
			inputs.forEach[aliasScope.createIdentifier(it, it.name)]
			if (aliasOut.get(it)) {
				aliasScope.createIdentifier(output, output.name)
			}
			aliasScopes.put(it, aliasScope)
		]

		'''
			@«ImplementedBy»(«className».«defaultClassName».class)
			public «IF isStatic»static «ENDIF»abstract class «className» implements «FOR fInterface : functionInterfaces SEPARATOR ","»«fInterface»«ENDFOR» {
				«IF !preConditions.empty || !postConditions.empty»
					
					@«Inject» protected «ConditionValidator» «conditionValidatorId»;
				«ENDIF»
				«IF output.needsBuilder»
					
					@«Inject» protected «ModelObjectValidator» «objectValidatorId»;
				«ENDIF»
				«IF !dependencies.empty»
					
					// RosettaFunction dependencies
					//
				«ENDIF»
				«FOR dep : dependencies»
					@«Inject» protected «dep.toFunctionJavaClass» «classScope.getIdentifierOrThrow(dep.toFunctionInstance)»;
				«ENDFOR»
			
				/**
				«FOR input : inputs»
					* @param «evaluateScope.getIdentifierOrThrow(input)» «ModelGeneratorUtil.escape(input.definition)»
				«ENDFOR»
				* @return «evaluateScope.getIdentifierOrThrow(output)» «ModelGeneratorUtil.escape(output.definition)»
				*/
				«IF overridesEvaluate»
				@Override
				«ENDIF»
				public «outputType» evaluate(«inputs.inputsAsParameters(evaluateScope)») {
					«IF !preConditions.empty»
						// pre-conditions
						«FOR cond:preConditions»
							«cond.contributeCondition(conditionValidatorId, evaluateScope)»
							
						«ENDFOR»
					«ENDIF»
					«output.toBuilderType» «evaluateScope.getIdentifierOrThrow(output)» = doEvaluate(«inputs.inputsAsArguments(evaluateScope)»);
					
					«IF !postConditions.empty»
						// post-conditions
						«FOR cond:postConditions»
							«cond.contributeCondition(conditionValidatorId, evaluateScope)»
							
						«ENDFOR»
					«ENDIF»
					«IF output.needsBuilder»
						if («evaluateScope.getIdentifierOrThrow(output)» != null) {
							«objectValidatorId».validate(«output.RType.toJavaType».class, «evaluateScope.getIdentifierOrThrow(output)»);
						}
					«ENDIF»
					return «evaluateScope.getIdentifierOrThrow(output)»;
				}
			
				protected abstract «output.toBuilderType» doEvaluate(«inputs.inputsAsParameters(doEvaluateScope)»);
			«FOR alias : shortcuts»
				«val aliasScope = aliasScopes.get(alias)»
				«IF aliasOut.get(alias)»
					«val multi = cardinality.isMulti(alias.expression)»
					«val returnType = shortcutJavaType(alias)»
					
						protected abstract «IF multi»«List»<«returnType»>«ELSE»«returnType»«ENDIF» «classScope.getIdentifierOrThrow(alias)»(«output.toBuilderType» «aliasScope.getIdentifierOrThrow(output)», «IF !inputs.empty»«inputs.inputsAsParameters(aliasScope)»«ENDIF»);
				«ELSE»
					
						protected abstract «IF needsBuilder(alias)»«Mapper»<? extends «toJavaReferenceType(typeProvider.getRType(alias.expression))»>«ELSE»«Mapper»<«toJavaReferenceType(typeProvider.getRType(alias.expression))»>«ENDIF» «classScope.getIdentifierOrThrow(alias)»(«inputs.inputsAsParameters(aliasScope)»);
				«ENDIF»
			«ENDFOR»

				public static class «defaultClassName» extends «className» {
					@Override
					protected «output.toBuilderType» doEvaluate(«inputs.inputsAsParameters(doEvaluateScope)») {
						«output.toBuilderType» «doEvaluateScope.getIdentifierOrThrow(output)» = «IF output.multi»new «ArrayList»<>()«ELSEIF output.needsBuilder»«output.RType.toListOrSingleJavaType(output.multi)».builder()«ELSE»null«ENDIF»;
						return assignOutput(«doEvaluateScope.getIdentifierOrThrow(output)»«IF !inputs.empty», «ENDIF»«inputs.inputsAsArguments(doEvaluateScope)»);
					}
					
					protected «output.toBuilderType» assignOutput(«output.toBuilderType» «assignOutputScope.getIdentifierOrThrow(output)»«IF !inputs.empty», «ENDIF»«inputs.inputsAsParameters(assignOutputScope)») {
						«FOR operation : operations»
							«assign(assignOutputScope, operation, function, aliasOut, output)»
							
						«ENDFOR»
						return «IF !needsBuilder(output)»«assignOutputScope.getIdentifierOrThrow(output)»«ELSE»«Optional».ofNullable(«assignOutputScope.getIdentifierOrThrow(output)»)
							.map(«IF output.multi»o -> o.stream().map(i -> i.prune()).collect(«Collectors».toList())«ELSE»o -> o.prune()«ENDIF»)
							.orElse(null)«ENDIF»;
					}
					«FOR alias : shortcuts»
						«val aliasScope = aliasScopes.get(alias)»
						«IF aliasOut.get(alias)»
							«val multi = cardinality.isMulti(alias.expression)»
							«val returnType = shortcutJavaType(alias)»
							
							@Override
							protected «IF multi»«List»<«returnType»>«ELSE»«returnType»«ENDIF» «classScope.getIdentifierOrThrow(alias)»(«output.toBuilderType» «aliasScope.getIdentifierOrThrow(output)», «IF !inputs.empty»«inputs.inputsAsParameters(aliasScope)»«ENDIF») {
								return toBuilder(«expressionGenerator.javaCode(alias.expression, aliasScope)»«IF multi».getMulti()«ELSE».get()«ENDIF»);
							}
						«ELSE»
							
							@Override
							protected «IF needsBuilder(alias)»«Mapper»<? extends «toJavaReferenceType(typeProvider.getRType(alias.expression))»>«ELSE»«Mapper»<«toJavaReferenceType(typeProvider.getRType(alias.expression))»>«ENDIF» «classScope.getIdentifierOrThrow(alias)»(«inputs.inputsAsParameters(aliasScope)») {
								return «expressionGenerator.javaCode(alias.expression, aliasScope)»;
							}
						«ENDIF»
					«ENDFOR»
				}
					«IF isQualifierFunction(function)»
						
						@Override
						public String getNamePrefix() {
							return "«getQualifierAnnotations(function.annotations).head.annotation.prefix»";
						}
					«ENDIF»
			}
		'''
	}

	private def StringConcatenationClient dispatchClassBody(Function function, JavaScope topScope,
		Iterable<? extends Function> dependencies, String version, RootPackage root) {
		val dispatchingFuncs = function.dispatchingFunctions.sortBy[name].toList
		val enumParam = function.inputs.filter[typeCall.type instanceof RosettaEnumeration].head.name
		val outputType = function.outputTypeOrVoid
		val className = topScope.getIdentifierOrThrow(function)

		val classScope = topScope.classScope(className.desiredName)
		dispatchingFuncs.forEach[classScope.createIdentifier(it, (function.name + value.value.name.toFirstUpper).toFirstLower)]

		val evaluateScope = classScope.methodScope("evaluate")
		function.inputs.forEach[evaluateScope.createIdentifier(it)]
		'''
		«javadoc(function, version)»
		public class «className» implements «RosettaFunction» {
			«FOR dep : dependencies»
				@«Inject» protected «dep.toFunctionJavaClass» «dep.name.toFirstLower»;
			«ENDFOR»
			
			«FOR enumFunc : dispatchingFuncs»
				@«Inject» protected «toDispatchClass(enumFunc)» «classScope.getIdentifierOrThrow(enumFunc)»;
			«ENDFOR»
			
			public «outputType» evaluate(«function.inputsAsParameters(evaluateScope)») {
				switch («enumParam») {
					«FOR enumFunc : dispatchingFuncs»
						case «formatEnumName(enumFunc.value.value.name)»:
							return «classScope.getIdentifierOrThrow(enumFunc)».evaluate(«function.inputsAsArguments(evaluateScope)»);
					«ENDFOR»
					default:
						throw new IllegalArgumentException("Enum value not implemented: " + «enumParam»);
				}
			}
			
			«FOR enumFunc : dispatchingFuncs»
				«val rFunction = new RFunction(
					function.name + enumFunc.value.value.name.toFirstUpper, 
					DottedPath.splitOnDots(function.model.name), 
					enumFunc.definition, 
					function.inputs.map[rTypeBuilderFactory.buildRAttribute(it)], 
					rTypeBuilderFactory.buildRAttribute(function.output), 
					RFunctionOrigin.FUNCTION,
					enumFunc.conditions, 
					enumFunc.postConditions,
					(function.shortcuts + enumFunc.shortcuts).toList.map[rTypeBuilderFactory.buildRShortcut(it)],
					enumFunc.operations.map[rTypeBuilderFactory.buildROperation(it)],
					enumFunc.annotations
				)»
				«rFunction.rBuildClass(true, #[JavaClass.from(RosettaFunction)], false, classScope)»
			«ENDFOR»
		}'''
	}

	private def JavaClass toDispatchClass(FunctionDispatch ele) {
		return new JavaClass(DottedPath.splitOnDots(ele.model.name).child("functions"), ele.name + "." + ele.name + formatEnumName(ele.value.value.name))
	}

//	private def QualifiedName toEnumClassName(FunctionDispatch ele) {
//		return QualifiedName.create(ele.name).append(formatEnumName(ele.value.value.name))
//	}

	private def boolean assignAsKey(ROperation op) {
		return op.expression instanceof AsKeyOperation
	}

	private def StringConcatenationClient assign(JavaScope scope, ROperation op, RFunction function,
		Map<RShortcut, Boolean> outs, RAttribute attribute) {

		if (op.pathTail.isEmpty) {
			// assign function output object
			switch(op.ROperationType) {
				case ADD: {
					val addVarName = scope.createUniqueIdentifier("addVar")
					'''
					«IF needsBuilder(op.pathHead)»
						«attribute.toBuilderType» «addVarName» = toBuilder(«assignPlainValue(scope, op, attribute.multi)»);
					«ELSE»
						«attribute.toBuilderType» «addVarName» = «assignPlainValue(scope, op, attribute.multi)»;«ENDIF»
					«op.assignTarget(function, outs, scope)».addAll(«addVarName»);'''
				}
				case SET: {
					'''
					«IF needsBuilder(op.pathHead)»
						«op.assignTarget(function, outs, scope)» = toBuilder(«assignPlainValue(scope, op, attribute.multi)»);
					«ELSE»
						«op.assignTarget(function, outs, scope)» = «assignPlainValue(scope, op, attribute.multi)»;«ENDIF»'''
				} 	
			}

		} else { // assign an attribute of the function output object
			'''
				«op.assignTarget(function, outs, scope)»
					«FOR seg : op.pathTail.indexed»
						«IF seg.key < op.pathTail.size - 1».getOrCreate«seg.value.name.toFirstUpper»(«IF seg.value.multi»0«ENDIF»)«IF isReference(seg.value)».getOrCreateValue()«ENDIF»
					«ELSE».«IF op.ROperationType == ROperationType.ADD»add«ELSE»set«ENDIF»«seg.value.name.toFirstUpper»«IF seg.value.isReference && !op.assignAsKey»Value«ENDIF»(«assignValue(scope, op, op.assignAsKey, seg.value.multi)»);«ENDIF»
					«ENDFOR»
			'''
		}
	}

	private def StringConcatenationClient assignValue(JavaScope scope, ROperation op, boolean assignAsKey,
		boolean isAssigneeMulti) {
		if (assignAsKey) {
			val metaClass = op.operationToReferenceWithMetaType
			if (cardinality.isMulti(op.expression)) {
				val lambdaScope = scope.lambdaScope
				val item = lambdaScope.createUniqueIdentifier("item")
				'''
					«expressionGenerator.javaCode(op.expression, scope)»
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
						.setGlobalReference(«Optional».ofNullable(«expressionGenerator.javaCode(op.expression, scope)».get())
							.map(«r» -> «r».getMeta())
							.map(«m» -> «m».getGlobalKey())
							.orElse(null))
						.setExternalReference(«Optional».ofNullable(«expressionGenerator.javaCode(op.expression, scope)».get())
							.map(«r» -> «r».getMeta())
							.map(«m» -> «m».getExternalKey())
							.orElse(null))
						.build()
				'''
			}
		} else {
			assignPlainValue(scope, op, isAssigneeMulti)
		}
	}

	private def StringConcatenationClient assignPlainValue(JavaScope scope, ROperation operation,
		boolean isAssigneeMulti) {
		'''«expressionGenerator.javaCode(operation.expression, scope)»«IF isAssigneeMulti».getMulti()«ELSE».get()«ENDIF»'''
	}

	private def boolean isReference(RAttribute attribute) {
		return attribute.hasMetaDataAnnotations || attribute.hasMetaDataAddress
	}

	private def StringConcatenationClient assignTarget(ROperation operation, RFunction function, Map<RShortcut, Boolean> outs,
		JavaScope scope) {
		val root = operation.pathHead
		switch (root) {
			RAttribute: '''«scope.getIdentifierOrThrow(root)»'''
			RShortcut:
				unfoldLHSShortcut(root, function, scope)
		}
	}

	private def StringConcatenationClient unfoldLHSShortcut(RShortcut shortcut, RFunction function, JavaScope scope) {
		val e = shortcut.expression
		if (e instanceof RosettaSymbolReference) {
			if (e.symbol instanceof RosettaCallableWithArgs) {
				// assign-output for an alias
				return '''«scope.getIdentifierOrThrow(shortcut)»(«expressionGenerator.aliasCallArgs(shortcut, function, scope)»)'''
			}
		}
		return '''«lhsExpand(e, scope)»'''
	}

	private def dispatch StringConcatenationClient lhsExpand(RosettaExpression f, JavaScope scope) {
		throw new IllegalStateException("No implementation for lhsExpand for " + f.class)
	}

	private def dispatch StringConcatenationClient lhsExpand(RosettaFeatureCall f,
		JavaScope scope) '''«lhsExpand(f.receiver, scope)».«f.feature.lhsFeature»'''

	private def dispatch StringConcatenationClient lhsExpand(RosettaSymbolReference f,
		JavaScope scope) '''«f.symbol.lhsExpand(scope)»'''

	private def dispatch StringConcatenationClient lhsExpand(ShortcutDeclaration f,
		JavaScope scope) '''«f.expression.lhsExpand(scope)»'''

	private def dispatch StringConcatenationClient lhsExpand(RosettaUnaryOperation f,
		JavaScope scope) '''«f.argument.lhsExpand(scope)»'''

	private def dispatch StringConcatenationClient lhsFeature(RosettaFeature f) {
		throw new IllegalStateException("No implementation for lhsFeature for " + f.class)
	}

	private def dispatch StringConcatenationClient lhsFeature(Attribute f) {
		val rAttribute = rTypeBuilderFactory.buildRAttribute(f)
		if (rAttribute.multi) '''getOrCreate«rAttribute.name.toFirstUpper»(0)''' else '''getOrCreate«rAttribute.name.toFirstUpper»()'''
	}

	private def dispatch StringConcatenationClient lhsExpand(RosettaSymbol c, JavaScope scope) {
		throw new IllegalStateException("No implementation for lhsExpand for " + c.class)
	}

	private def dispatch StringConcatenationClient lhsExpand(Attribute c, JavaScope scope) {
		val rAttribute = rTypeBuilderFactory.buildRAttribute(c)
		'''«scope.getIdentifierOrThrow(rAttribute)»'''
	}

	private def StringConcatenationClient contributeCondition(Condition condition,
		GeneratedIdentifier conditionValidator, JavaScope scope) {
		'''
			«conditionValidator».validate(() -> 
				«expressionGenerator.javaCode(condition.expression, scope.lambdaScope)», 
					"«condition.definition»");
		'''
	}

	private def JavaType outputTypeOrVoid(Function function) {
		val out = getOutput(function)
		if (out === null) {
			JavaPrimitiveType.VOID
		} else {
			if (out.typeCall.type.needsBuilder) {
				typeProvider.getRTypeOfSymbol(out).toPolymorphicListOrSingleJavaType(out.card.isMany)
			} else {
				typeProvider.getRTypeOfSymbol(out).toListOrSingleJavaType(out.card.isMany)
			}
		}
	}
	
	private def JavaType attributeToJavaType(RAttribute rAttribute) {
		if (rAttribute.needsBuilder) {
			rAttribute.RType.toPolymorphicListOrSingleJavaType(rAttribute.multi)
		} else {
			rAttribute.RType.toListOrSingleJavaType(rAttribute.multi)
		}
	}
	

	private def StringConcatenationClient inputsAsArguments(extension Function function, JavaScope scope) {
		'''«FOR input : getInputs(function) SEPARATOR ', '»«scope.getIdentifierOrThrow(input)»«ENDFOR»'''
	}
	
	private def StringConcatenationClient inputsAsArguments(List<RAttribute> inputs, JavaScope scope) {
		'''«FOR input : inputs SEPARATOR ', '»«scope.getIdentifierOrThrow(input)»«ENDFOR»'''
	}

	private def StringConcatenationClient inputsAsParameters(extension Function function, JavaScope scope) {
		'''«FOR input : getInputs(function) SEPARATOR ', '»«IF input.typeCall.type.needsBuilder»«typeProvider.getRTypeOfSymbol(input).toPolymorphicListOrSingleJavaType(input.card.isMany)»«ELSE»«typeProvider.getRTypeOfSymbol(input).toListOrSingleJavaType(input.card.isMany)»«ENDIF» «scope.getIdentifierOrThrow(input)»«ENDFOR»'''
	}
	
	private def StringConcatenationClient inputsAsParameters(List<RAttribute> inputs, JavaScope scope) {
		'''«FOR input : inputs SEPARATOR ', '»«input.attributeToJavaType» «scope.getIdentifierOrThrow(input)»«ENDFOR»'''
	}

	private def StringConcatenationClient shortcutJavaType(RShortcut feature) {
		val rType = typeProvider.getRType(feature.expression)
		val javaType = rType.toJavaReferenceType
		'''«javaType»«IF needsBuilder(rType)».«javaType»Builder«ENDIF»'''
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
