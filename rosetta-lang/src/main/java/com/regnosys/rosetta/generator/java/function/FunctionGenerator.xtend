package com.regnosys.rosetta.generator.java.function

import com.fasterxml.jackson.core.type.TypeReference
import com.google.inject.ImplementedBy
import com.regnosys.rosetta.generator.GeneratedIdentifier
import com.regnosys.rosetta.generator.java.JavaIdentifierRepresentationService
import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.regnosys.rosetta.generator.java.expression.ExpressionGenerator
import com.regnosys.rosetta.generator.java.expression.JavaDependencyProvider
import com.regnosys.rosetta.generator.java.expression.TypeCoercionService
import com.regnosys.rosetta.generator.java.statement.JavaStatement
import com.regnosys.rosetta.generator.java.statement.builder.JavaExpression
import com.regnosys.rosetta.generator.java.statement.builder.JavaStatementBuilder
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
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
import com.regnosys.rosetta.types.RFunction
import com.regnosys.rosetta.types.RFunctionOrigin
import com.regnosys.rosetta.types.RObjectFactory
import com.regnosys.rosetta.types.ROperation
import com.regnosys.rosetta.types.ROperationType
import com.regnosys.rosetta.types.RShortcut
import com.regnosys.rosetta.types.RosettaTypeProvider
import com.regnosys.rosetta.utils.ExpressionHelper
import com.regnosys.rosetta.utils.ImplicitVariableUtil
import com.regnosys.rosetta.utils.ModelIdProvider
import com.rosetta.model.lib.ModelSymbolId
import com.rosetta.model.lib.functions.ConditionValidator
import com.rosetta.model.lib.functions.IQualifyFunctionExtension
import com.rosetta.model.lib.functions.ModelObjectValidator
import com.rosetta.model.lib.functions.RosettaFunction
import com.rosetta.util.types.JavaClass
import com.rosetta.util.types.JavaGenericTypeDeclaration
import com.rosetta.util.types.JavaParameterizedType
import com.rosetta.util.types.JavaPrimitiveType
import com.rosetta.util.types.JavaReferenceType
import com.rosetta.util.types.JavaType
import com.rosetta.util.types.generated.GeneratedJavaClass
import java.util.ArrayList
import java.util.Collections
import java.util.List
import java.util.Map
import java.util.Optional
import java.util.stream.Collectors
import javax.inject.Inject
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.generator.IFileSystemAccess2

import static com.regnosys.rosetta.generator.java.enums.EnumHelper.*
import static com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil.*

import static extension com.regnosys.rosetta.types.RMetaAnnotatedType.withNoMeta
import com.regnosys.rosetta.generator.java.statement.builder.JavaVariable
import com.regnosys.rosetta.generator.java.types.JavaPojoInterface
import com.regnosys.rosetta.generator.java.types.RJavaWithMetaValue

class FunctionGenerator {

	@Inject ExpressionGenerator expressionGenerator
	@Inject JavaDependencyProvider dependencyProvider
	@Inject RosettaTypeProvider typeProvider
	@Inject extension RosettaFunctionExtensions
	
	@Inject ExpressionHelper exprHelper
	@Inject extension ImportManagerExtension
	@Inject CardinalityProvider cardinality
	@Inject extension JavaIdentifierRepresentationService
	@Inject extension JavaTypeTranslator
	@Inject RObjectFactory rTypeBuilderFactory
	@Inject ImplicitVariableUtil implicitVariableUtil
	@Inject extension JavaTypeUtil
	@Inject TypeCoercionService coercionService
	@Inject extension ModelIdProvider

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
				rBuildClass(rFunction, false, functionInterfaces, emptyMap, overridesEvaluate, topScope)
			}

		val content = buildClass(root.functions, classBody, topScope)
		fsa.generateFile(fileName, content)
	}
	
	def rBuildClass(RFunction rFunction, boolean isStatic, List<JavaType> functionInterfaces, Map<Class<?>, String> annotations, boolean overridesEvaluate, JavaScope topScope) {
		val dependencies = collectFunctionDependencies(rFunction)
		rFunction.classBody(isStatic, overridesEvaluate, dependencies, functionInterfaces, annotations, topScope)
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
		boolean isStatic,
		boolean overridesEvaluate,
		List<JavaClass<?>> dependencies,
		List<JavaType> functionInterfaces,
		Map<Class<?>, String> annotations,
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
		dependencies.forEach[classScope.createIdentifier(it.toDependencyInstance, it.simpleName.toFirstLower)]
		
		val defaultClassScope = classScope.classScope(className.desiredName + "Default")
		val defaultClassName = defaultClassScope.createUniqueIdentifier(className.desiredName + "Default")
		val outputType = output.toMetaJavaType
		val aliasOut = shortcuts.toMap([it], [exprHelper.usesOutputParameter(it.expression)])

		
		val conditionValidatorId = classScope.createUniqueIdentifier("conditionValidator")
		val objectValidatorId = classScope.createUniqueIdentifier("objectValidator")
		
		val evaluateScope = classScope.methodScope("evaluate")
		inputs.forEach[evaluateScope.createIdentifier(it, it.name)]
		evaluateScope.createIdentifier(output, output.name)
		val outputBuilderId = if (output.needsBuilder) {
			evaluateScope.createUniqueIdentifier(output.name + "Builder")
		}
		
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
			«FOR entry: annotations.entrySet»
				@«entry.key»(«entry.value»)
			«ENDFOR»
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
					@«Inject» protected «dep» «classScope.getIdentifierOrThrow(dep.toDependencyInstance)»;
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
					«output.toBuilderType» «IF output.needsBuilder»«outputBuilderId»«ELSE»«evaluateScope.getIdentifierOrThrow(output)»«ENDIF» = doEvaluate(«inputs.inputsAsArguments(evaluateScope)»);
					
					«IF output.needsBuilder»
						final «outputType» «evaluateScope.getIdentifierOrThrow(output)»;
						if («outputBuilderId» == null) {
							«evaluateScope.getIdentifierOrThrow(output)» = null;
						} else {
							«evaluateScope.getIdentifierOrThrow(output)» = «outputBuilderId»«IF output.isMulti».stream().map(«output.RMetaAnnotatedType.toJavaReferenceType»::build).collect(«Collectors».toList())«ELSE».build()«ENDIF»;
							«objectValidatorId».validate(«output.RMetaAnnotatedType.toJavaReferenceType».class, «evaluateScope.getIdentifierOrThrow(output)»);
						}
						
					«ENDIF»
					«IF !postConditions.empty»
						// post-conditions
						«FOR cond:postConditions»
							«cond.contributeCondition(conditionValidatorId, evaluateScope)»
							
						«ENDFOR»
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
					«val multi = cardinality.isMulti(alias.expression)»
					«val returnType = (multi ? MAPPER_C as JavaGenericTypeDeclaration<?> : MAPPER_S).wrapExtendsIfNotFinal(alias.expression)»
					
						protected abstract «returnType» «classScope.getIdentifierOrThrow(alias)»(«inputs.inputsAsParameters(aliasScope)»);
				«ENDIF»
			«ENDFOR»

				public static class «defaultClassName» extends «className» {
					@Override
					protected «output.toBuilderType» doEvaluate(«inputs.inputsAsParameters(doEvaluateScope)») {
						«FOR input : inputs.filter[isMulti]»
						if («doEvaluateScope.getIdentifierOrThrow(input)» == null) {
							«doEvaluateScope.getIdentifierOrThrow(input)» = «Collections».emptyList();
						}
						«ENDFOR»
						«output.toBuilderType» «doEvaluateScope.getIdentifierOrThrow(output)» = «IF output.multi»new «ArrayList»<>()«ELSEIF output.needsBuilder»«output.RMetaAnnotatedType.RType.toListOrSingleJavaType(output.multi)».builder()«ELSE»null«ENDIF»;
						return assignOutput(«doEvaluateScope.getIdentifierOrThrow(output)»«IF !inputs.empty», «ENDIF»«inputs.inputsAsArguments(doEvaluateScope)»);
					}
					
					protected «output.toBuilderType» assignOutput(«output.toBuilderType» «assignOutputScope.getIdentifierOrThrow(output)»«IF !inputs.empty», «ENDIF»«inputs.inputsAsParameters(assignOutputScope)») {
						«FOR operation : operations»
							«assign(assignOutputScope, operation, function, aliasOut, output).asStatementList»
							
						«ENDFOR»
						return «IF !needsBuilder(output)»«assignOutputScope.getIdentifierOrThrow(output)»«ELSE»«Optional».ofNullable(«assignOutputScope.getIdentifierOrThrow(output)»)
							.map(«IF output.multi»o -> o.stream().map(i -> i.prune()).collect(«Collectors».toList())«ELSE»o -> o.prune()«ENDIF»)
							.orElse(null)«ENDIF»;
					}
					«FOR alias : shortcuts»
						«val aliasScope = aliasScopes.get(alias)»
						«IF aliasOut.get(alias)»
							«val multi = cardinality.isMulti(alias.expression)»
							«val itemReturnType = shortcutJavaType(alias)»
							«val returnType = multi ? LIST.wrap(itemReturnType) : itemReturnType»
							«val body = expressionGenerator.javaCode(alias.expression, alias.shortcutExpressionJavaType, aliasScope)
									.mapExpressionIfNotNull[JavaExpression.from('''toBuilder(«it»)''', returnType)]
							»
							
							@Override
							protected «returnType» «classScope.getIdentifierOrThrow(alias)»(«output.toBuilderType» «aliasScope.getIdentifierOrThrow(output)», «IF !inputs.empty»«inputs.inputsAsParameters(aliasScope)»«ENDIF») «body.completeAsReturn.toBlock»
						«ELSE»
							«val multi = cardinality.isMulti(alias.expression)»
							«val returnType = (multi ? MAPPER_C as JavaGenericTypeDeclaration<?> : MAPPER_S).wrapExtendsIfNotFinal(alias.expression)»
							
							@Override
							protected «returnType» «classScope.getIdentifierOrThrow(alias)»(«inputs.inputsAsParameters(aliasScope)») «expressionGenerator.javaCode(alias.expression, returnType, aliasScope).completeAsReturn.toBlock»
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
		List<JavaClass<?>> dependencies, String version, RootPackage root) {
		val dispatchingFuncs = function.dispatchingFunctions.sortBy[name].toList
		val enumParam = function.inputs.filter[typeCall.type instanceof RosettaEnumeration].head.name
		val outputType = function.outputTypeOrVoid
		val className = topScope.getIdentifierOrThrow(function)

		val classScope = topScope.classScope(className.desiredName)
		dispatchingFuncs.forEach[classScope.createIdentifier(it, (function.name + value.value.name.toFirstUpper).toFirstLower)]

		val evaluateScope = classScope.methodScope("evaluate")
		function.inputs.forEach[evaluateScope.createIdentifier(it)]
		'''
		«javadoc(function.definition, function.references, version)»
		public class «className» implements «RosettaFunction» {
			«FOR dep : dependencies»
				@«Inject» protected «dep» «dep.simpleName.toFirstLower»;
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
					new ModelSymbolId(function.model.toDottedPath, function.name + formatEnumName(enumFunc.value.value.name)),
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
				«rFunction.rBuildClass(true, #[JavaClass.from(RosettaFunction)], emptyMap, false, classScope)»
			«ENDFOR»
		}'''
	}

	private def JavaClass<?> toDispatchClass(FunctionDispatch ele) {
		return new GeneratedJavaClass<Object>(ele.model.toDottedPath.child("functions"), ele.name + "." + ele.name + formatEnumName(ele.value.value.name), Object)
	}

	private def boolean assignAsKey(ROperation op) {
		return op.expression instanceof AsKeyOperation
	}

	private def JavaStatement assign(JavaScope scope, ROperation op, RFunction function, Map<RShortcut, Boolean> outs, RAttribute attribute) {

		if (op.pathTail.isEmpty) {
			// assign function output object
			val expressionType = attribute.toMetaJavaType
			var javaExpr = expressionGenerator.javaCode(op.expression, expressionType, scope)
			val effectiveExprType = javaExpr.expressionType
			if (needsBuilder(attribute)) {
				javaExpr = javaExpr.mapExpressionIfNotNull[JavaExpression.from('''toBuilder(«it»)''', attribute.toBuilderType)]
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
			assignValue(scope, op, op.assignAsKey)
				.collapseToSingleExpression(scope)
				.mapExpression[
					var expr = op.assignTarget(function, outs, scope)
					for (seg : op.pathTail.indexed) {
						val oldExpr = expr
						val prop = (expr.expressionType as JavaPojoInterface).findProperty(seg.value.name)
						val itemType = prop.type.itemType
						if (seg.key < op.pathTail.size - 1) {
							expr = JavaExpression.from(
								'''
								«oldExpr»
									.«prop.getOrCreateName»(«IF prop.type.isList»0«ENDIF»)''',
								itemType
							)
							if (itemType instanceof RJavaWithMetaValue) {
								val metaExpr = expr
								expr = JavaExpression.from('''«metaExpr».getOrCreateValue()''', itemType.valueType)
							}
						} else {
							expr = JavaExpression.from(
								'''
								«oldExpr»
									.«IF op.ROperationType == ROperationType.ADD»add«ELSE»set«ENDIF»«prop.name.toFirstUpper»«IF itemType instanceof RJavaWithMetaValue && !op.assignAsKey»Value«ENDIF»(«it»)''',
								JavaPrimitiveType.VOID
							)
						}
					}
					expr
				].completeAsExpressionStatement
		}
	}

	private def JavaStatementBuilder assignValue(JavaScope scope, ROperation op, boolean assignAsKey) {
		if (assignAsKey) {
			val metaClass = op.operationToReferenceWithMetaType
			if (cardinality.isMulti(op.expression)) {
				val lambdaScope = scope.lambdaScope
				val item = lambdaScope.createUniqueIdentifier("item")
				expressionGenerator.javaCode(op.expression, MAPPER_C.wrapExtendsWithoutMeta(op.expression), scope)
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
				expressionGenerator.javaCode(op.expression, typeProvider.getRMetaAnnotatedType(op.expression).RType.withNoMeta.toJavaReferenceType, scope)
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
			expressionGenerator.javaCode(op.expression, op.operationToJavaType, scope)
		}
	}

	private def JavaExpression assignTarget(ROperation operation, RFunction function, Map<RShortcut, Boolean> outs,
		JavaScope scope) {
		val root = operation.pathHead
		switch (root) {
			RAttribute: new JavaVariable(scope.getIdentifierOrThrow(root), root.RMetaAnnotatedType.toJavaReferenceType)
			RShortcut:
				unfoldLHSShortcut(root, function, scope)
		}
	}

	private def JavaExpression unfoldLHSShortcut(RShortcut shortcut, RFunction function, JavaScope scope) {
		val e = shortcut.expression
		if (e instanceof RosettaSymbolReference) {
			if (e.symbol instanceof RosettaCallableWithArgs) {
				// assign-output for an alias
				return JavaExpression.from('''«scope.getIdentifierOrThrow(shortcut)»(«expressionGenerator.aliasCallArgs(shortcut, function, scope)»)''', shortcut.shortcutExpressionJavaType)
			}
		}
		return lhsExpand(e, scope)
	}

	private def dispatch JavaExpression lhsExpand(RosettaExpression f, JavaScope scope) {
		throw new IllegalStateException("No implementation for lhsExpand for " + f.class)
	}

	private def dispatch JavaExpression lhsExpand(RosettaFeatureCall f,
		JavaScope scope) { lhsExpand(f.receiver, scope).lhsFeature(f.feature) }

	private def dispatch JavaExpression lhsExpand(RosettaSymbolReference f,
		JavaScope scope) { f.symbol.lhsExpand(scope) }

	private def dispatch JavaExpression lhsExpand(ShortcutDeclaration f,
		JavaScope scope) { f.expression.lhsExpand(scope) }

	private def dispatch JavaExpression lhsExpand(RosettaUnaryOperation f,
		JavaScope scope) { f.argument.lhsExpand(scope) }

	private def dispatch JavaExpression lhsFeature(JavaExpression receiver, RosettaFeature f) {
		throw new IllegalStateException("No implementation for lhsFeature for " + f.class)
	}

	private def dispatch JavaExpression lhsFeature(JavaExpression receiver, Attribute f) {
		val t = receiver.expressionType as JavaPojoInterface
		val prop = t.findProperty(f.name)
		JavaExpression.from('''«receiver».«prop.getOrCreateName»(«IF prop.type.isList»0«ENDIF»)''', prop.type.itemType)
	}

	private def dispatch JavaExpression lhsExpand(RosettaSymbol c, JavaScope scope) {
		throw new IllegalStateException("No implementation for lhsExpand for " + c.class)
	}

	private def dispatch JavaExpression lhsExpand(Attribute c, JavaScope scope) {
		val rAttribute = rTypeBuilderFactory.buildRAttribute(c)
		new JavaVariable(scope.getIdentifierOrThrow(rAttribute), rAttribute.RMetaAnnotatedType.toJavaReferenceType)
	}

	private def StringConcatenationClient contributeCondition(Condition condition,
		GeneratedIdentifier conditionValidator, JavaScope scope) {
		val conditionBody = expressionGenerator.javaCode(condition.expression, COMPARISON_RESULT, scope.lambdaScope).toLambdaBody
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
		'''«FOR input : inputs SEPARATOR ', '»«input.toMetaJavaType» «scope.getIdentifierOrThrow(input)»«ENDFOR»'''
	}

	private def JavaReferenceType shortcutJavaType(RShortcut feature) {
		val javaType = feature.shortcutExpressionJavaType
		if (feature.needsBuilder)
			(javaType as JavaClass<?>).toBuilderType
		else
			javaType
	}
	private def JavaReferenceType shortcutExpressionJavaType(RShortcut feature) {
		val metaRType = typeProvider.getRMetaAnnotatedType(feature.expression)
		metaRType.toJavaReferenceType
	}
	
	private def JavaType toBuilderItemType(RAttribute rAttribute) {
		var javaType = rAttribute.RMetaAnnotatedType.toJavaReferenceType as JavaClass<?>
		if(rAttribute.needsBuilder) javaType = javaType.toBuilderType
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
}
