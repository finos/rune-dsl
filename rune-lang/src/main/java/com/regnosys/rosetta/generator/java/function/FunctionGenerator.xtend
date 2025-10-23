package com.regnosys.rosetta.generator.java.function

import com.fasterxml.jackson.core.type.TypeReference
import com.google.inject.ImplementedBy
import com.regnosys.rosetta.generator.GeneratedIdentifier
import com.regnosys.rosetta.generator.java.expression.ExpressionGenerator
import com.regnosys.rosetta.generator.java.expression.JavaDependencyProvider
import com.regnosys.rosetta.generator.java.expression.TypeCoercionService
import com.regnosys.rosetta.generator.java.statement.JavaStatement
import com.regnosys.rosetta.generator.java.statement.builder.JavaExpression
import com.regnosys.rosetta.generator.java.statement.builder.JavaStatementBuilder
import com.regnosys.rosetta.generator.java.statement.builder.JavaVariable
import com.regnosys.rosetta.generator.java.types.JavaPojoInterface
import com.regnosys.rosetta.generator.java.types.JavaPojoProperty
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil
import com.regnosys.rosetta.generator.java.types.RJavaFieldWithMeta
import com.regnosys.rosetta.generator.java.types.RJavaPojoInterface
import com.regnosys.rosetta.generator.java.types.RJavaReferenceWithMeta
import com.regnosys.rosetta.generator.java.types.RJavaWithMetaValue
import com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil
import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions
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
import com.regnosys.rosetta.types.RFeature
import com.regnosys.rosetta.types.RFunction
import com.regnosys.rosetta.types.RFunctionOrigin
import com.regnosys.rosetta.types.RMetaAttribute
import com.regnosys.rosetta.types.RObjectFactory
import com.regnosys.rosetta.types.ROperation
import com.regnosys.rosetta.types.ROperationType
import com.regnosys.rosetta.types.RShortcut
import com.regnosys.rosetta.types.RosettaTypeProvider
import com.regnosys.rosetta.utils.ExpressionHelper
import com.regnosys.rosetta.utils.ImplicitVariableUtil
import com.regnosys.rosetta.utils.ModelIdProvider
import com.rosetta.model.lib.ModelSymbolId
import com.rosetta.model.lib.annotations.RuneLabelProvider
import com.rosetta.model.lib.functions.ConditionValidator
import com.rosetta.model.lib.functions.IQualifyFunctionExtension
import com.rosetta.model.lib.functions.ModelObjectValidator
import com.rosetta.model.lib.functions.RosettaFunction
import com.rosetta.util.types.JavaClass
import com.rosetta.util.types.JavaParameterizedType
import com.rosetta.util.types.JavaPrimitiveType
import com.rosetta.util.types.JavaReferenceType
import com.rosetta.util.types.JavaType
import java.util.ArrayList
import java.util.Collections
import java.util.List
import java.util.Map
import java.util.Optional
import java.util.stream.Collectors
import jakarta.inject.Inject
import org.eclipse.xtend2.lib.StringConcatenationClient

import static com.regnosys.rosetta.generator.java.enums.EnumHelper.*

import static extension com.regnosys.rosetta.types.RMetaAnnotatedType.withNoMeta
import static extension com.regnosys.rosetta.utils.PojoPropertyUtil.*
import com.regnosys.rosetta.generator.java.scoping.JavaIdentifierRepresentationService
import com.regnosys.rosetta.generator.java.scoping.JavaStatementScope
import com.regnosys.rosetta.generator.java.types.RGeneratedJavaClass
import com.regnosys.rosetta.generator.java.RObjectJavaClassGenerator
import com.regnosys.rosetta.generator.java.scoping.JavaClassScope
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.generator.java.scoping.JavaMethodScope
import static com.regnosys.rosetta.generator.java.types.JavaPojoPropertyOperationType.*
import com.rosetta.model.lib.context.RuneScope

class FunctionGenerator extends RObjectJavaClassGenerator<RFunction, RGeneratedJavaClass<? extends RosettaFunction>> {

	@Inject ExpressionGenerator expressionGenerator
	@Inject JavaDependencyProvider dependencyProvider
	@Inject RosettaTypeProvider typeProvider
	@Inject extension RosettaFunctionExtensions
	@Inject extension ModelGeneratorUtil
	
	@Inject ExpressionHelper exprHelper
	@Inject CardinalityProvider cardinality
	@Inject extension JavaIdentifierRepresentationService
	@Inject extension JavaTypeTranslator
	@Inject RObjectFactory rTypeBuilderFactory
	@Inject ImplicitVariableUtil implicitVariableUtil
	@Inject extension JavaTypeUtil
	@Inject TypeCoercionService coercionService
	@Inject extension ModelIdProvider
	@Inject LabelProviderGeneratorUtil labelProviderUtil
	@Inject AliasUtil aliasUtil
	
	override protected streamObjects(RosettaModel model) {
		model.elements.stream.filter[it instanceof Function && !(it instanceof FunctionDispatch)].map[it as Function].map[rTypeBuilderFactory.buildRFunction(it)]
	}
	override protected createTypeRepresentation(RFunction rFunction) {
		rFunction.toFunctionJavaClass
	}
	override protected generateClass(RFunction rFunction, RGeneratedJavaClass<? extends RosettaFunction> javaFunctionClass, String version, JavaClassScope scope) {
		val origin = rFunction.EObject
		if (origin instanceof Function && (origin as Function).handleAsEnumFunction) {
			val dependencies = collectFunctionDependencies(origin as Function)
			(origin as Function).dispatchClassBody(javaFunctionClass, scope, dependencies, version)
		} else {
			var overridesEvaluate = false
			var List<JavaType> functionInterfaces
			if (rFunction.superFunction !== null) {
				functionInterfaces = emptyList
			} else {
				functionInterfaces = newArrayList(JavaClass.from(RosettaFunction))
				if (isQualifierFunction(rFunction)) {
					overridesEvaluate = true
					functionInterfaces.add(getQualifyingFunctionInterface(rFunction.inputs))
				}
			}
			val Map<Class<?>, StringConcatenationClient> annotations = newLinkedHashMap
			if (origin instanceof Function && labelProviderUtil.shouldGenerateLabelProvider(origin as Function)) {
				val labelProviderClass = rFunction.toLabelProviderJavaClass
				annotations.put(RuneLabelProvider, '''labelProvider=«labelProviderClass».class''' )
			}
			rBuildClass(rFunction, javaFunctionClass, false, functionInterfaces, annotations, overridesEvaluate, scope)
		}
	}
	
	def rBuildClass(RFunction rFunction, RGeneratedJavaClass<? extends RosettaFunction> javaFunctionClass, boolean isStatic, List<JavaType> functionInterfaces, Map<Class<?>, StringConcatenationClient> annotations, boolean overridesEvaluate, JavaClassScope classScope) {
		val dependencies = collectFunctionDependencies(rFunction)
		rFunction.classBody(javaFunctionClass, isStatic, overridesEvaluate, dependencies, functionInterfaces, annotations, classScope)
	}
	
	private def getQualifyingFunctionInterface(List<RAttribute> inputs) {
		val parameterVariable = inputs.head.RMetaAnnotatedType.toListOrSingleJavaType(inputs.head.multi)
		JavaParameterizedType.from(new TypeReference<IQualifyFunctionExtension<?>>() {}, parameterVariable)
	}

	private def collectFunctionDependencies(Function func) {
		val expressions = 
			func.shortcuts.map[expression] +
			func.operations.map[expression] +
			(func.conditions + func.postConditions).map[expression]
		return dependencyProvider.javaDependencies(expressions)
	}

	private def collectFunctionDependencies(RFunction func) {
		val expressions = func.preConditions.map[it.expression] + 
				func.postConditions.map[it.expression] + 
				func.operations.map[it.expression] +
				func.shortcuts.map[it.expression]
		return dependencyProvider.javaDependencies(expressions)
	}

	private def StringConcatenationClient classBody(
		RFunction function,
		RGeneratedJavaClass<?> javaFunctionClass,
		boolean isStatic,
		boolean overridesEvaluate,
		List<JavaClass<?>> dependencies,
		List<JavaType> functionInterfaces,
		Map<Class<?>, StringConcatenationClient> annotations,
		JavaClassScope classScope
	) {
		val inputs = function.inputs
		val output = function.output
		val shortcuts = function.shortcuts
		val operations = function.operations
		val preConditions = function.preConditions
		val postConditions = function.postConditions
		
		val superFunc = function.superFunction
		val superClass = superFunc?.toFunctionJavaClass
		
		val defaultClass = javaFunctionClass.createNestedClassWithSuperclass(javaFunctionClass.simpleName + "Default", javaFunctionClass)
		val defaultClassScope = classScope.createNestedClassScopeAndRegisterIdentifier(defaultClass)
		val outputType = output.toMetaJavaType
		val aliasOut = shortcuts.toMap([it], [exprHelper.usesOutputParameter(it.expression)])
		
		if (superFunc !== null) {
			defaultClassScope.createIdentifier(superFunc.toSuperFunctionInstance, "superFunc")
		}

		val contextFactoryId = classScope.createUniqueIdentifier("contextFactory")
		val conditionValidatorId = classScope.createUniqueIdentifier("conditionValidator")
		val objectValidatorId = classScope.createUniqueIdentifier("objectValidator")

		val scopeClass = function.scope?.toScopeJavaClass
		val scopeId = scopeClass === null ? null : classScope.createUniqueIdentifier(scopeClass.simpleName.toFirstLower)
		val defaultScopeExpression = if (scopeId === null) {
			JavaExpression.from('''«contextFactoryId».createDefault()''', RUNE_CONTEXT)
		} else {
			JavaExpression.from('''«contextFactoryId».withScope(«scopeId»)''', RUNE_CONTEXT)
		}
		
		val evaluateScope = classScope.createMethodScope("evaluate")
		inputs.forEach[evaluateScope.createIdentifier(it, it.name)]
		evaluateScope.createIdentifier(output, output.name)
		val evaluateContextId = evaluateScope.createUniqueIdentifier("context")
		val evaluateBodyScope = evaluateScope.bodyScope
		val outputBuilderId = if (output.needsBuilder) {
			evaluateBodyScope.createUniqueIdentifier(output.name + "Builder")
		}
		
		val doEvaluateScope = classScope.createMethodScope("doEvaluate")
		inputs.forEach[doEvaluateScope.createIdentifier(it, it.name)]
		doEvaluateScope.createIdentifier(output, output.name)
		val doEvaluateContextId = doEvaluateScope.createUniqueIdentifier("context")
		
		val defaultDoEvaluateScope = defaultClassScope.createMethodScope("doEvaluate")
		inputs.forEach[defaultDoEvaluateScope.createIdentifier(it, it.name)]
		defaultDoEvaluateScope.createIdentifier(output, output.name)
		val defaultDoEvaluateContextId = defaultDoEvaluateScope.createUniqueIdentifier("context")
		val defaultDoEvaluateBodyScope = defaultDoEvaluateScope.bodyScope
		
		val assignOutputScope = defaultClassScope.createMethodScope("assignOutput")
		inputs.forEach[assignOutputScope.createIdentifier(it, it.name)]
		assignOutputScope.createIdentifier(output, output.name)
		val assignOutputContextId = assignOutputScope.createUniqueIdentifier("context")
		function.operations
			.map[it.expression]
			.filter[implicitVariableUtil.implicitVariableExistsInContext(it)]
			.map[it.implicitVarInContext]
			.toSet
			.forEach[assignOutputScope.createKeySynonym(it, inputs.head)]
		val assignOutputBodyScope = assignOutputScope.bodyScope
		dependencies.forEach[assignOutputBodyScope.createIdentifier(it.toDependencyInstance, it.simpleName.toFirstLower)]
		
		// TODO: try break things with clever alias names
		val aliasScopes = newHashMap
		val defaultClassAliasScopes = newHashMap
		val aliasContextIds = newHashMap
		val defaultClassContextIds = newHashMap
		shortcuts.forEach[
			classScope.createIdentifier(it, it.name)
						
			val aliasScope = classScope.createMethodScope(it.name)
			aliasScopes.put(it, aliasScope)
			inputs.forEach[aliasScope.createIdentifier(it, it.name)]
			
			val defaultClassAliasScope = defaultClassScope.createMethodScope(it.name)
			defaultClassAliasScopes.put(it, defaultClassAliasScope)
			inputs.forEach[defaultClassAliasScope.createIdentifier(it, it.name)]
			
			if (aliasUtil.requiresOutput(it)) {
				aliasScope.createIdentifier(output, output.name)
				defaultClassAliasScope.createIdentifier(output, output.name)
			}
			
			val aliasDependencies = dependencyProvider.javaDependencies(it.expression)
			for (dep : aliasDependencies) {
				aliasScope.createIdentifier(dep.toDependencyInstance, dep.simpleName.toFirstLower)
				defaultClassAliasScope.createIdentifier(dep.toDependencyInstance, dep.simpleName.toFirstLower)
			}
			
			aliasContextIds.put(it, aliasScope.createUniqueIdentifier("context"))
			defaultClassContextIds.put(it, defaultClassAliasScope.createUniqueIdentifier("context"))
		]

		'''
			«FOR entry: annotations.entrySet»
				@«entry.key»(«entry.value»)
			«ENDFOR»
			@«ImplementedBy»(«defaultClass».class)
			public «IF isStatic»static «ENDIF»abstract class «javaFunctionClass.simpleName»«IF superClass !== null» extends «superClass»«ENDIF»«IF !functionInterfaces.empty» implements «FOR fInterface : functionInterfaces SEPARATOR ","»«fInterface»«ENDFOR»«ENDIF» {
				
				@«javax.inject.Inject» protected «RUNE_CONTEXT_FACTORY» «contextFactoryId»;
				«IF scopeId !== null»
					
					@«javax.inject.Inject» protected «scopeClass» «scopeId»;
				«ENDIF»
				«IF !preConditions.empty || !postConditions.empty»
					
					@«javax.inject.Inject» protected «ConditionValidator» «conditionValidatorId»;
				«ENDIF»
				«IF output.needsBuilder»
					
					@«javax.inject.Inject» protected «ModelObjectValidator» «objectValidatorId»;
				«ENDIF»
				
				«IF overridesEvaluate»
				@Override
				«ENDIF»
				public «outputType» evaluate(«inputs.inputsAsParameters(evaluateScope)») {
					return evaluate(«inputs.inputsAsArguments(defaultScopeExpression, evaluateBodyScope)»);
				}
			
				/**
				«FOR input : inputs»
					* @param «evaluateScope.getIdentifierOrThrow(input)» «escape(input.definition)»
				«ENDFOR»
				* @return «evaluateScope.getIdentifierOrThrow(output)» «escape(output.definition)»
				*/
				public «outputType» evaluate(«inputs.inputsAsParameters(evaluateContextId, evaluateScope)») {
					«IF !preConditions.empty»
						// pre-conditions
						«FOR cond:preConditions»
							«cond.contributeCondition(conditionValidatorId, evaluateContextId, evaluateBodyScope)»
							
						«ENDFOR»
					«ENDIF»
					«output.toBuilderType» «IF output.needsBuilder»«outputBuilderId»«ELSE»«evaluateBodyScope.getIdentifierOrThrow(output)»«ENDIF» = doEvaluate(«inputs.inputsAsArguments(evaluateContextId, evaluateBodyScope)»);
					
					«IF output.needsBuilder»
						final «outputType» «evaluateBodyScope.getIdentifierOrThrow(output)»;
						if («outputBuilderId» == null) {
							«evaluateBodyScope.getIdentifierOrThrow(output)» = null;
						} else {
							«evaluateBodyScope.getIdentifierOrThrow(output)» = «outputBuilderId»«IF output.isMulti».stream().map(«output.RMetaAnnotatedType.toJavaReferenceType»::build).collect(«Collectors».toList())«ELSE».build()«ENDIF»;
							«objectValidatorId».validate(«output.RMetaAnnotatedType.toJavaReferenceType».class, «evaluateBodyScope.getIdentifierOrThrow(output)»);
						}
						
					«ENDIF»
					«IF !postConditions.empty»
						// post-conditions
						«FOR cond:postConditions»
							«cond.contributeCondition(conditionValidatorId, evaluateContextId, evaluateBodyScope)»
							
						«ENDFOR»
					«ENDIF»
					return «evaluateScope.getIdentifierOrThrow(output)»;
				}
			
				protected abstract «output.toBuilderType» doEvaluate(«inputs.inputsAsParameters(doEvaluateContextId, doEvaluateScope)»);
			«FOR alias : shortcuts»
				«val aliasScope = aliasScopes.get(alias)»
				«val contextId = aliasContextIds.get(alias)»
				protected abstract «aliasUtil.getReturnType(alias)» «classScope.getIdentifierOrThrow(alias)»(«aliasUtil.getParameters(alias, contextId, aliasScope)»);
			«ENDFOR»

				public static «defaultClass.asClassDeclaration» {
					«IF superFunc !== null»
					
					@«javax.inject.Inject» protected «superClass» «defaultClassScope.getIdentifierOrThrow(superFunc.toSuperFunctionInstance)»;
					
					«ENDIF»
					@Override
					protected «output.toBuilderType» doEvaluate(«inputs.inputsAsParameters(defaultDoEvaluateContextId, defaultDoEvaluateScope)») {
						«FOR input : inputs.filter[isMulti]»
						if («defaultDoEvaluateBodyScope.getIdentifierOrThrow(input)» == null) {
							«defaultDoEvaluateBodyScope.getIdentifierOrThrow(input)» = «Collections».emptyList();
						}
						«ENDFOR»
						«output.toBuilderType» «defaultDoEvaluateBodyScope.getIdentifierOrThrow(output)» = «IF output.multi»new «ArrayList»<>()«ELSEIF output.needsBuilder»«output.RMetaAnnotatedType.toListOrSingleJavaType(output.multi)».builder()«ELSE»null«ENDIF»;
						return assignOutput(«defaultDoEvaluateBodyScope.getIdentifierOrThrow(output)», «inputs.inputsAsArguments(defaultDoEvaluateContextId, defaultDoEvaluateBodyScope)»);
					}
					
					protected «output.toBuilderType» assignOutput(«output.toBuilderType» «assignOutputScope.getIdentifierOrThrow(output)», «inputs.inputsAsParameters(assignOutputContextId, assignOutputScope)») {
						«IF !dependencies.empty»
							// dependencies
							«val runtimeScopeId = assignOutputBodyScope.createUniqueIdentifier("scope")»
							final «RuneScope» «runtimeScopeId» = «assignOutputContextId».getScope();
							«FOR dep : dependencies»
							final «dep» «assignOutputBodyScope.getIdentifierOrThrow(dep.toDependencyInstance)» = «runtimeScopeId».getInstance(«dep».class);
							«ENDFOR»
							
						«ENDIF»
						«val functionHasDeepOperations = operations.filter[o|o.pathTail.size > 0].size > 0»
						«FOR operation : operations»
							«assign(assignOutputBodyScope, operation, function, aliasOut, output, functionHasDeepOperations, assignOutputContextId).asStatementList»
							
						«ENDFOR»
						return «IF !needsBuilder(output)»«assignOutputBodyScope.getIdentifierOrThrow(output)»«ELSE»«Optional».ofNullable(«assignOutputBodyScope.getIdentifierOrThrow(output)»)
							.map(«IF output.multi»o -> o.stream().map(i -> i.prune()).collect(«Collectors».toList())«ELSE»o -> o.prune()«ENDIF»)
							.orElse(null)«ENDIF»;
					}
					«FOR alias : shortcuts»
						«val aliasScope = defaultClassAliasScopes.get(alias)»
						«val returnType = aliasUtil.getReturnType(alias)»
						«val contextId = defaultClassContextIds.get(alias)»
						«val body = expressionGenerator.javaCode(alias.expression, returnType, contextId, aliasScope.bodyScope)»
						@Override
						protected «returnType» «defaultClassScope.getIdentifierOrThrow(alias)»(«aliasUtil.getParameters(alias, contextId, aliasScope)») «body.completeAsReturn.toBlock»
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

	private def StringConcatenationClient dispatchClassBody(Function function, RGeneratedJavaClass<? extends RosettaFunction> javaFunctionClass, JavaClassScope classScope, List<JavaClass<?>> dependencies, String version) {
		val dispatchingFuncs = function.dispatchingFunctions.sortBy[name].toList
		val enumParam = function.inputs.filter[typeCall.type instanceof RosettaEnumeration].head.name
		val outputType = function.outputTypeOrVoid

		dispatchingFuncs.forEach[classScope.createIdentifier(it, (function.name + value.value.name.toFirstUpper).toFirstLower)]

		val enumFuncToRFunc = newHashMap
		val enumFuncToClass = newHashMap
		val enumFuncToScope = newHashMap
		dispatchingFuncs.forEach[enumFunc|
			val rFunction = new RFunction(
				enumFunc,
				null,
				null,
				new ModelSymbolId(function.model.toDottedPath, function.name + formatEnumName(enumFunc.value.value.name)),
				enumFunc.definition,
				function.inputs.map[rTypeBuilderFactory.buildRAttributeWithEnclosingType(null, it)],
				rTypeBuilderFactory.buildRAttributeWithEnclosingType(null, function.output),
				RFunctionOrigin.FUNCTION,
				enumFunc.conditions,
				enumFunc.postConditions,
				(function.shortcuts + enumFunc.shortcuts).toList.map[rTypeBuilderFactory.buildRShortcut(it)],
				enumFunc.operations.map[rTypeBuilderFactory.buildROperation(it)],
				enumFunc.annotations
			)
			val nestedClass = javaFunctionClass.createNestedClass(rFunction.alphanumericName, RosettaFunction)
			val scope = classScope.createNestedClassScopeAndRegisterIdentifier(nestedClass)
			enumFuncToRFunc.put(enumFunc, rFunction)
			enumFuncToClass.put(enumFunc, nestedClass)
			enumFuncToScope.put(enumFunc, scope)
		]

		val evaluateScope = classScope.createMethodScope("evaluate")
		val evaluateBodyScope = evaluateScope.bodyScope
		'''
		«javadoc(function.definition, function.references, version)»
		public class «javaFunctionClass» implements «RosettaFunction» {
			«FOR dep : dependencies»
				@«javax.inject.Inject» protected «dep» «dep.simpleName.toFirstLower»;
			«ENDFOR»
			
			«FOR enumFunc : dispatchingFuncs»
				@«javax.inject.Inject» protected «enumFuncToClass.get(enumFunc)» «classScope.getIdentifierOrThrow(enumFunc)»;
			«ENDFOR»
			
			public «outputType» evaluate(«function.inputsAsParameters(evaluateScope)») {
				switch («enumParam») {
					«FOR enumFunc : dispatchingFuncs»
						case «formatEnumName(enumFunc.value.value.name)»:
							return «evaluateBodyScope.getIdentifierOrThrow(enumFunc)».evaluate(«function.inputsAsArguments(evaluateBodyScope)»);
					«ENDFOR»
					default:
						throw new IllegalArgumentException("Enum value not implemented: " + «enumParam»);
				}
			}
			
			«FOR enumFunc : dispatchingFuncs»
				«val rFunction = enumFuncToRFunc.get(enumFunc)»
				«val nestedClass = enumFuncToClass.get(enumFunc)»
				«val scope = enumFuncToScope.get(enumFunc)»
				«rFunction.rBuildClass(nestedClass, true, #[JavaClass.from(RosettaFunction)], emptyMap, false, scope)»
			«ENDFOR»
		}'''
	}

	private def boolean assignAsKey(ROperation op) {
		return op.expression instanceof AsKeyOperation
	}

	private def JavaStatement assign(JavaStatementScope scope, ROperation op, RFunction function, Map<RShortcut, Boolean> outs, RAttribute attribute, boolean functionHasDeepOperations, GeneratedIdentifier runtimeContextId) {
		if (op.pathTail.isEmpty) {
			// assign function output object
			val expressionType = attribute.toMetaJavaType
			var javaExpr = expressionGenerator.javaCode(op.expression, expressionType, runtimeContextId, scope)
			val effectiveExprType = javaExpr.expressionType
			if (needsBuilder(attribute)) {
				javaExpr = javaExpr.mapExpressionIfNotNull[JavaExpression.from('''toBuilder(«it»«IF functionHasDeepOperations», () -> «effectiveExprType».builder()«ENDIF»)''', attribute.toBuilderType)]
			} else {
				val needsToCopy = 
					op.ROperationType == ROperationType.SET
					&& effectiveExprType.isList
					&& function.operations.exists[o| o.ROperationType == ROperationType.ADD]
				if (needsToCopy) {
					javaExpr =
						javaExpr
							.mapExpressionIfNotNull[JavaExpression.from('''new «ArrayList»<>(«it»)''', LIST.wrap(effectiveExprType.itemType))]
				}
			}
			switch(op.ROperationType) {
				case ADD: {
					javaExpr = coercionService.addCoercions(javaExpr, attribute.isMulti ? LIST.wrapExtends(attribute.toBuilderItemType) : attribute.toBuilderItemType, scope)
					javaExpr
						.mapExpression[
							JavaExpression.from(
								'''«op.assignTarget(function, outs, scope)».addAll(«it»)''',
								JavaPrimitiveType.VOID
							)
						].completeAsExpressionStatement
				}
				case SET: {
					javaExpr = coercionService.addCoercions(javaExpr, attribute.toBuilderType, scope)
					javaExpr
						.mapExpression[
							JavaExpression.from(
								'''«op.assignTarget(function, outs, scope)» = «it»''',
								JavaPrimitiveType.VOID
							)
						].completeAsExpressionStatement
				} 	
			}

		} else { // assign an attribute of the function output object
			assignValue(scope, op, op.assignAsKey, runtimeContextId)
				.collapseToSingleExpression(scope)
				.mapExpression[
					var expr = op.assignTarget(function, outs, scope)

					// path intermediary
					val intermediarySegmentSize =  op.pathTail.length - 1
					for (var pathIndex=0; pathIndex < intermediarySegmentSize; pathIndex++) {
						
						val seg = op.pathTail.get(pathIndex)
						
						if (expr.expressionType.itemType instanceof RJavaWithMetaValue) {
							val metaExpr = expr
							expr = JavaExpression.from('''«metaExpr».getOrCreateValue()''', (expr.expressionType.itemType as RJavaWithMetaValue).valueType)							
						}
						
						val prop = getPojoProperty(seg, expr.expressionType.itemType)
						val oldExpr = expr
						val itemType = prop.type.itemType
						expr = JavaExpression.from(
							'''
							«oldExpr»
								.«prop.getOperationName(GET_OR_CREATE)»(«IF prop.type.isList»0«ENDIF»)''',
							itemType
						)
					}
					
					//end of path
					val seg = op.pathTail.get(op.pathTail.length - 1)
					val oldExpr = expr				
					val outputExpressionType = expr.expressionType.itemType
					val prop = getPojoProperty(seg, outputExpressionType)
					
					val requiresValueSetter = requiresValueSetter(outputExpressionType, prop, seg, op)
					val propertySetterName = getPropertySetterName(outputExpressionType, prop, seg, op.ROperationType, requiresValueSetter)
					expr = JavaExpression.from(
						'''
						«oldExpr»
							«generateMetaWrapperCreator(seg, prop, outputExpressionType)».«propertySetterName»(«it»)''',
						JavaPrimitiveType.VOID
					)
					
					expr
				].completeAsExpressionStatement
		}
	}
	
	private def String getPropertySetterName(JavaType outputExpressionType, JavaPojoProperty prop, RFeature segment, ROperationType operationType, boolean requiresValueSetter) {
		if (outputExpressionType instanceof RJavaWithMetaValue || (segment instanceof RMetaAttribute && outputExpressionType instanceof RJavaPojoInterface)) {
			val prefix = operationType == ROperationType.ADD ? "add" : "set"
			val segmentPropName = segment.toPojoPropertyName.toFirstUpper
			if (requiresValueSetter) {
				prefix + segmentPropName + "Value"
			} else {
				prefix + segmentPropName
			}
		} else {
			if (requiresValueSetter) {
				if (operationType == ROperationType.ADD) {
					prop.getOperationName(ADD_VALUE)
				} else {
					prop.getOperationName(SET_VALUE)
				}
			} else {
				if (operationType == ROperationType.ADD) {
					prop.getOperationName(ADD)
				} else {
					prop.getOperationName(SET)
				}
			}
		}
	}
	
	private def boolean requiresValueSetter(JavaType outputExpressionType, JavaPojoProperty outerPojoProperty, RFeature segment, ROperation op) {
		val outerPropertyType = outerPojoProperty.type.itemType
		val innerProp = if (outputExpressionType instanceof RJavaWithMetaValue && outerPropertyType instanceof JavaPojoInterface) {
			getPojoProperty(segment, outerPropertyType)
		} else {
			outerPojoProperty
		}
		
		val innerPropType = innerProp.type.itemType
		
		val isMetaSegment = if (segment instanceof RAttribute) {
			segment.RMetaAnnotatedType.hasAttributeMeta
		} else {
			false
		}
		
		innerPropType instanceof RJavaWithMetaValue && !isMetaSegment && !op.assignAsKey		
	}
	
	private def StringConcatenationClient generateMetaWrapperCreator(RFeature seg, JavaPojoProperty prop, JavaType expressionType) {
		switch (expressionType) {
			RJavaFieldWithMeta: '''«IF seg instanceof RMetaAttribute».getOrCreateMeta()«ELSE».«prop.getOperationName(GET_OR_CREATE)»()«ENDIF»'''
			RJavaReferenceWithMeta case seg instanceof RMetaAttribute && seg.name == "address": '''.«prop.getOperationName(GET_OR_CREATE)»()'''
			RJavaReferenceWithMeta case !(seg instanceof RMetaAttribute): '''.getOrCreateValue()'''
			RJavaPojoInterface case seg instanceof RMetaAttribute: '''.«prop.getOperationName(GET_OR_CREATE)»()'''
			default: ''''''
		}
	}
	
	//The type of the output expression to be set and the pojo property type are not the same when working with meta
	private def JavaPojoProperty getPojoProperty(RFeature seg, JavaType outputExpressionType) {
		if (seg instanceof RMetaAttribute && (outputExpressionType instanceof RJavaFieldWithMeta || outputExpressionType instanceof RJavaPojoInterface)) {
			(outputExpressionType as JavaPojoInterface).findProperty("meta")
		} else if (seg instanceof RMetaAttribute && outputExpressionType instanceof RJavaReferenceWithMeta) {
			(outputExpressionType as JavaPojoInterface).findProperty("reference")
		} else  if (outputExpressionType instanceof RJavaWithMetaValue) {
			(outputExpressionType as JavaPojoInterface).findProperty("value")
		} else {
			(outputExpressionType as JavaPojoInterface).findProperty(seg.name)
		}
	}
	


	private def JavaStatementBuilder assignValue(JavaStatementScope scope, ROperation op, boolean assignAsKey, GeneratedIdentifier runtimeContextId) {
		if (assignAsKey) {
			val metaClass = op.operationToReferenceWithMetaType
			if (cardinality.isMulti(op.expression)) {
				val lambdaScope = scope.lambdaScope
				val item = lambdaScope.createUniqueIdentifier("item")
				expressionGenerator.javaCode(op.expression, MAPPER_C.wrapExtendsWithoutMeta(op.expression), runtimeContextId, scope)
					.collapseToSingleExpression(scope)
					.mapExpression[
						JavaExpression.from(
							'''
								«it»
									.getItems()
									.map(«item» -> «metaClass».builder()
										.setExternalReference(«item».getMappedObject().getMeta().getExternalKey())
										.setGlobalReference(«item».getMappedObject().getMeta().getGlobalKey())
										.build())
									.collect(«Collectors».toList())
							''',
							LIST.wrap(metaClass)
						)
					]
			} else {
				val lambdaScope = scope.lambdaScope
				val r = lambdaScope.createUniqueIdentifier("r")
				val m = lambdaScope.createUniqueIdentifier("m")
				expressionGenerator.javaCode(op.expression, typeProvider.getRMetaAnnotatedType(op.expression).RType.withNoMeta.toJavaReferenceType, runtimeContextId, scope)
					.declareAsVariable(true, op.pathHead.name + op.pathTail.map[name.toFirstUpper].join, scope)
					.mapExpression[
						JavaExpression.from(
							'''
								«metaClass».builder()
									.setGlobalReference(«Optional».ofNullable(«it»)
										.map(«r» -> «r».getMeta())
										.map(«m» -> «m».getGlobalKey())
										.orElse(null))
									.setExternalReference(«Optional».ofNullable(«it»)
										.map(«r» -> «r».getMeta())
										.map(«m» -> «m».getExternalKey())
										.orElse(null))
									.build()
							''',
							metaClass
						)
					]
			}
		} else {
			expressionGenerator.javaCode(op.expression, op.operationToMetaJavaType, runtimeContextId, scope)
		}
	}

	private def JavaExpression assignTarget(ROperation operation, RFunction function, Map<RShortcut, Boolean> outs,
		JavaStatementScope scope) {
		val root = operation.pathHead
		switch (root) {
			RAttribute: new JavaVariable(scope.getIdentifierOrThrow(root), root.RMetaAnnotatedType.toJavaReferenceType)
			RShortcut:
				unfoldLHSShortcut(root, function, scope)
		}
	}

	private def JavaExpression unfoldLHSShortcut(RShortcut shortcut, RFunction function, JavaStatementScope scope) {
		val e = shortcut.expression
		if (e instanceof RosettaSymbolReference) {
			if (e.symbol instanceof RosettaCallableWithArgs) {
				// assign-output for an alias
				return JavaExpression.from('''«scope.getIdentifierOrThrow(shortcut)»(«expressionGenerator.aliasCallArgs(shortcut, function, scope)»)''', shortcut.shortcutExpressionJavaType)
			}
		}
		return lhsExpand(e, scope)
	}

	private def dispatch JavaExpression lhsExpand(RosettaExpression f, JavaStatementScope scope) {
		throw new IllegalStateException("No implementation for lhsExpand for " + f.class)
	}

	private def dispatch JavaExpression lhsExpand(RosettaFeatureCall f,
		JavaStatementScope scope) { lhsExpand(f.receiver, scope).lhsFeature(f.feature) }

	private def dispatch JavaExpression lhsExpand(RosettaSymbolReference f,
		JavaStatementScope scope) { f.symbol.lhsExpand(scope) }

	private def dispatch JavaExpression lhsExpand(ShortcutDeclaration f,
		JavaStatementScope scope) { f.expression.lhsExpand(scope) }

	private def dispatch JavaExpression lhsExpand(RosettaUnaryOperation f,
		JavaStatementScope scope) { f.argument.lhsExpand(scope) }

	private def dispatch JavaExpression lhsFeature(JavaExpression receiver, RosettaFeature f) {
		throw new IllegalStateException("No implementation for lhsFeature for " + f.class)
	}

	private def dispatch JavaExpression lhsFeature(JavaExpression receiver, Attribute f) {
		val t = receiver.expressionType as JavaPojoInterface
		val prop = t.findProperty(f.name)
		JavaExpression.from('''«receiver».«prop.getOperationName(GET_OR_CREATE)»(«IF prop.type.isList»0«ENDIF»)''', prop.type.itemType)
	}

	private def dispatch JavaExpression lhsExpand(RosettaSymbol c, JavaStatementScope scope) {
		throw new IllegalStateException("No implementation for lhsExpand for " + c.class)
	}

	private def dispatch JavaExpression lhsExpand(Attribute c, JavaStatementScope scope) {
		val rAttribute = rTypeBuilderFactory.buildRAttribute(c)
		new JavaVariable(scope.getIdentifierOrThrow(rAttribute), rAttribute.RMetaAnnotatedType.toJavaReferenceType)
	}

	private def StringConcatenationClient contributeCondition(Condition condition,
		GeneratedIdentifier conditionValidator, GeneratedIdentifier runtimeContextId, JavaStatementScope scope) {
		val conditionBody = expressionGenerator.javaCode(condition.expression, COMPARISON_RESULT, runtimeContextId, scope.lambdaScope).toLambdaBody
		'''
			«conditionValidator».validate(() -> «conditionBody»,
				"«condition.definition»");
		'''
	}

	private def JavaType outputTypeOrVoid(Function function) {
		val out = getOutput(function)
		if (out === null) {
			JavaPrimitiveType.VOID
		} else {
			if (out.typeCall.type.needsBuilder) {
				typeProvider.getRTypeOfSymbol(out).RType.toPolymorphicListOrSingleJavaType(out.card.isMany)
			} else {
				typeProvider.getRTypeOfSymbol(out).RType.toListOrSingleJavaType(out.card.isMany)
			}
		}
	}	

	private def StringConcatenationClient inputsAsArguments(extension Function function, JavaStatementScope scope) {
		'''«FOR input : getInputs(function) SEPARATOR ', '»«scope.getIdentifierOrThrow(input)»«ENDFOR»'''
	}
	
	private def StringConcatenationClient inputsAsArguments(List<RAttribute> inputs, GeneratedIdentifier contextId, JavaStatementScope scope) {
		inputsAsArguments(inputs, new JavaVariable(contextId, RUNE_CONTEXT), scope)
	}
	private def StringConcatenationClient inputsAsArguments(List<RAttribute> inputs, JavaExpression context, JavaStatementScope scope) {
		'''«FOR input : inputs SEPARATOR ', '»«scope.getIdentifierOrThrow(input)»«ENDFOR»«IF !inputs.isEmpty», «ENDIF»«context»'''
	}

	private def StringConcatenationClient inputsAsParameters(extension Function function, JavaMethodScope scope) {
		'''«FOR input : getInputs(function) SEPARATOR ', '»«IF input.typeCall.type.needsBuilder»«typeProvider.getRTypeOfSymbol(input).toPolymorphicListOrSingleJavaType(input.card.isMany)»«ELSE»«typeProvider.getRTypeOfSymbol(input).toListOrSingleJavaType(input.card.isMany)»«ENDIF» «scope.createIdentifier(input)»«ENDFOR»'''
	}
	
	private def StringConcatenationClient inputsAsParameters(List<RAttribute> inputs, JavaMethodScope scope) {
		inputsAsParameters(inputs, null, scope)
	}
	private def StringConcatenationClient inputsAsParameters(List<RAttribute> inputs, GeneratedIdentifier contextId, JavaMethodScope scope) {
		'''«FOR input : inputs SEPARATOR ', '»«input.toMetaJavaType» «scope.getIdentifierOrThrow(input)»«ENDFOR»«IF contextId !== null»«IF !inputs.isEmpty», «ENDIF»«RUNE_CONTEXT» «contextId»«ENDIF»'''
	}

	private def JavaReferenceType shortcutExpressionJavaType(RShortcut feature) {
		val metaRType = typeProvider.getRMetaAnnotatedType(feature.expression)
		metaRType.toJavaReferenceType
	}
	
	private def JavaType toBuilderItemType(RAttribute rAttribute) {
		var javaType = rAttribute.RMetaAnnotatedType.toJavaReferenceType as JavaClass<?>
		if(javaType.needsBuilder) javaType = (javaType as JavaPojoInterface).toBuilderInterface
		javaType
	}
	private def JavaType toBuilderType(RAttribute rAttribute) {
		val javaType = rAttribute.toBuilderItemType
		if (rAttribute.multi) {
			return LIST.wrap(javaType)
		} else {
			return javaType
		}
	}
	
	private def needsBuilder(RAttribute rAttribute) {
		var javaType = rAttribute.RMetaAnnotatedType.toJavaReferenceType as JavaClass<?>
		javaType.needsBuilder
	}
	
	private def boolean needsBuilder(JavaClass<?> javaClass) {
		switch (javaClass) {
			JavaPojoInterface: true
			default: false
		}
	}
}
