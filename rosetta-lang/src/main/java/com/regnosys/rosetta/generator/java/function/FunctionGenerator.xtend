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
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.RosettaFeature
import com.regnosys.rosetta.rosetta.RosettaNamed
import com.regnosys.rosetta.rosetta.RosettaSymbol
import com.regnosys.rosetta.rosetta.expression.AsKeyOperation
import com.regnosys.rosetta.rosetta.expression.RosettaExpression
import com.regnosys.rosetta.rosetta.expression.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference
import com.regnosys.rosetta.rosetta.expression.RosettaUnaryOperation
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
import com.regnosys.rosetta.types.CardinalityProvider
import com.rosetta.util.types.JavaType
import com.rosetta.util.types.JavaParametrizedType
import com.rosetta.util.types.JavaClass
import com.rosetta.util.types.JavaPrimitiveType
import com.regnosys.rosetta.types.RAttribute

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

	def void generate(RootPackage root, IFileSystemAccess2 fsa, Function func, String version) {
		val fileName = root.functions.withForwardSlashes + '/' + func.name + '.java'

		val topScope = new JavaScope(root.functions)
		topScope.createIdentifier(func)

		val dependencies = collectFunctionDependencies(func)

		val StringConcatenationClient classBody = if (func.handleAsEnumFunction) {
				func.dispatchClassBody(topScope, dependencies, version, root)
			} else {
				func.classBody(topScope, dependencies, version, false, root)
			}

		val content = buildClass(root.functions, classBody, topScope)
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

	private def StringConcatenationClient classBody(
		List<RAttribute> inputs,
		RAttribute output,
		boolean isStatic,
		boolean overridesEvaluate,
		List<JavaType> functionInterfaces,
		GeneratedIdentifier className,
		List<Condition> preConditions,
		List<Condition> postConditions,
		JavaScope scope,
		List<JavaClass> dependencies
	) {
		val classScope = scope.classScope(className.desiredName)
		val defaultClassScope = classScope.classScope(className.desiredName + "Default")
		val defaultClassName = defaultClassScope.createUniqueIdentifier(className.desiredName + "Default")
		val outputType = output.attributeToJavaType
		
		
		val conditionValidatorId = classScope.createUniqueIdentifier("conditionValidator")
		val objectValidatorId = classScope.createUniqueIdentifier("objectValidator")
		val evaluateScope = classScope.methodScope("evaluate")

		'''
			@«ImplementedBy»(«className».«defaultClassName».class)
			public «IF isStatic»static «ENDIF»abstract class«className» implements «FOR fInterface : functionInterfaces SEPARATOR ","»«fInterface»«ENDFOR» {
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
					@«Inject» protected «dep» «classScope.getIdentifierOrThrow(dep.toFunctionInstance)»;
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
			}
		'''
	}

	private def StringConcatenationClient classBody(Function func, JavaScope scope,
		Iterable<? extends Function> dependencies, String version, boolean isStatic, RootPackage root) {

		val output = getOutput(func)
		val inputs = getInputs(func)
		val outputType = func.outputTypeOrVoid
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
		func.shortcuts.forEach [
			val aliasScope = defaultClassScope.methodScope(it.name)
			inputs.forEach[aliasScope.createIdentifier(it)]
			if (aliasOut.get(it)) {
				aliasScope.createIdentifier(output)
			}
			aliasScopes.put(it, aliasScope)
		]

		'''
			@«ImplementedBy»(«className».«defaultClassName».class)
			public «IF isStatic»static «ENDIF»abstract class «className» implements «RosettaFunction»«IF func.isQualifierFunction()», «IQualifyFunctionExtension»<«typeProvider.getRTypeOfSymbol(inputs.head).toListOrSingleJavaType(inputs.head.card.isMany)»>«ENDIF» {
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
					@«Inject» protected «dep.toFunctionJavaClass» «classScope.getIdentifierOrThrow(dep.toFunctionInstance)»;
				«ENDFOR»
			
				/**
				«FOR input : inputs»
					* @param «evaluateScope.getIdentifierOrThrow(input)» «ModelGeneratorUtil.escape(input.definition)»
				«ENDFOR»
				«IF output !== null»
					* @return «evaluateScope.getIdentifierOrThrow(output)» «ModelGeneratorUtil.escape(output.definition)»
				«ENDIF»
				*/
				public «outputType» evaluate(«func.inputsAsParameters(evaluateScope)») {
					«IF !func.conditions.empty»
						// pre-conditions
						«FOR cond:func.conditions»
							«cond.contributeCondition(conditionValidatorId, evaluateScope)»
							
						«ENDFOR»
					«ENDIF»
					«output.toBuilderType» «evaluateScope.getIdentifierOrThrow(output)» = doEvaluate(«func.inputsAsArguments(evaluateScope)»);
					
					«IF !func.postConditions.empty»
						// post-conditions
						«FOR cond:func.postConditions»
							«cond.contributeCondition(conditionValidatorId, evaluateScope)»
							
						«ENDFOR»
					«ENDIF»
					«IF outNeedsBuilder»
						if («evaluateScope.getIdentifierOrThrow(output)» != null) {
							«objectValidatorId».validate(«typeProvider.getRTypeOfSymbol(output).toJavaType».class, «evaluateScope.getIdentifierOrThrow(output)»);
						}
					«ENDIF»
					return «evaluateScope.getIdentifierOrThrow(output)»;
				}
			
				protected abstract «output.toBuilderType» doEvaluate(«func.inputsAsParameters(doEvaluateScope)»);
			«FOR alias : func.shortcuts»
				«val aliasScope = aliasScopes.get(alias)»
				«IF aliasOut.get(alias)»
					«val multi = cardinality.isMulti(alias.expression)»
					«val returnType = shortcutJavaType(alias)»
					
						protected abstract «IF multi»«List»<«returnType»>«ELSE»«returnType»«ENDIF» «classScope.getIdentifierOrThrow(alias)»(«output.toBuilderType» «aliasScope.getIdentifierOrThrow(output)», «IF !inputs.empty»«func.inputsAsParameters(aliasScope)»«ENDIF»);
				«ELSE»
					
						protected abstract «IF needsBuilder(alias)»«Mapper»<? extends «toJavaReferenceType(typeProvider.getRType(alias.expression))»>«ELSE»«Mapper»<«toJavaReferenceType(typeProvider.getRType(alias.expression))»>«ENDIF» «classScope.getIdentifierOrThrow(alias)»(«func.inputsAsParameters(aliasScope)»);
				«ENDIF»
			«ENDFOR»
			
				public static class «defaultClassName» extends «className» {
					@Override
					protected «output.toBuilderType» doEvaluate(«func.inputsAsParameters(doEvaluateScope)») {
						«output.toBuilderType» «doEvaluateScope.getIdentifierOrThrow(output)» = «IF output.card.isMany»new «ArrayList»<>()«ELSEIF outNeedsBuilder»«typeProvider.getRTypeOfSymbol(output).toListOrSingleJavaType(output.card.isMany)».builder()«ELSE»null«ENDIF»;
						return assignOutput(«doEvaluateScope.getIdentifierOrThrow(output)»«IF !inputs.empty», «ENDIF»«func.inputsAsArguments(doEvaluateScope)»);
					}
					
					protected «output.toBuilderType» assignOutput(«output.toBuilderType» «assignOutputScope.getIdentifierOrThrow(output)»«IF !inputs.empty», «ENDIF»«func.inputsAsParameters(assignOutputScope)») {
						«FOR indexed : func.operations.filter(Operation).indexed»
							«IF indexed.value instanceof AssignOutputOperation»
								«assign(assignOutputScope, indexed.value as AssignOutputOperation, aliasOut, output, root)»
								
							«ELSEIF indexed.value instanceof OutputOperation»
								«assign(assignOutputScope, indexed.value as OutputOperation, aliasOut, output, indexed.key, root)»
								
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
								«val returnType = shortcutJavaType(alias)»
								
								@Override
								protected «IF multi»«List»<«returnType»>«ELSE»«returnType»«ENDIF» «classScope.getIdentifierOrThrow(alias)»(«output.toBuilderType» «aliasScope.getIdentifierOrThrow(output)», «IF !inputs.empty»«func.inputsAsParameters(aliasScope)»«ENDIF») {
									return toBuilder(«expressionGenerator.javaCode(alias.expression, aliasScope)»«IF multi».getMulti()«ELSE».get()«ENDIF»);
								}
							«ELSE»
								
								@Override
								protected «IF needsBuilder(alias)»«Mapper»<? extends «toJavaReferenceType(typeProvider.getRType(alias.expression))»>«ELSE»«Mapper»<«toJavaReferenceType(typeProvider.getRType(alias.expression))»>«ENDIF» «classScope.getIdentifierOrThrow(alias)»(«func.inputsAsParameters(aliasScope)») {
									return «expressionGenerator.javaCode(alias.expression, aliasScope)»;
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

	def private StringConcatenationClient dispatchClassBody(Function function, JavaScope topScope,
		Iterable<? extends Function> dependencies, String version, RootPackage root) {
		val dispatchingFuncs = function.dispatchingFunctions.sortBy[name].toList
		val enumParam = function.inputs.filter[typeCall.type instanceof RosettaEnumeration].head.name
		val outputType = function.outputTypeOrVoid
		val className = topScope.getIdentifierOrThrow(function)

		val classScope = topScope.classScope(className.desiredName)
		dispatchingFuncs.forEach[classScope.createIdentifier(it, toTargetClassName.lastSegment)]

		val evaluateScope = classScope.methodScope("evaluate")
		function.inputs.forEach[evaluateScope.createIdentifier(it)]
		'''
		«javadoc(function, version)»
		public class «className» {
			«FOR dep : dependencies»
				@«Inject» protected «dep.toFunctionJavaClass» «dep.name.toFirstLower»;
			«ENDFOR»
			
			«FOR enumFunc : dispatchingFuncs»
				@«Inject» protected «toTargetClassName(enumFunc)» «toTargetClassName(enumFunc).lastSegment»;
			«ENDFOR»
			
			public «outputType» evaluate(«function.inputsAsParameters(evaluateScope)») {
				switch («enumParam») {
					«FOR enumFunc : dispatchingFuncs»
						case «toEnumClassName(enumFunc).lastSegment»:
							return «classScope.getIdentifierOrThrow(enumFunc)».evaluate(«function.inputsAsArguments(evaluateScope)»);
					«ENDFOR»
					default:
						throw new IllegalArgumentException("Enum value not implemented: " + «enumParam»);
				}
			}
			
			«FOR enumFunc : dispatchingFuncs»
				
				«enumFunc.classBody(classScope, collectFunctionDependencies(enumFunc), version, true, root)»
			«ENDFOR»
		}'''
	}

	private def QualifiedName toTargetClassName(FunctionDispatch ele) {
		return QualifiedName.create(ele.name).append(ele.value.value.name.toFirstLower + "_") // to avoid name clashes
	}

	private def QualifiedName toEnumClassName(FunctionDispatch ele) {
		return QualifiedName.create(ele.name).append(formatEnumName(ele.value.value.name))
	}

	private def StringConcatenationClient assign(JavaScope scope, AssignOutputOperation op,
		Map<ShortcutDeclaration, Boolean> outs, Attribute type, RootPackage root) {
		val pathAsList = op.pathAsSegmentList
		if (pathAsList.isEmpty)
			'''
			«IF needsBuilder(op.assignRoot)»
				«op.assignTarget(outs, scope)» = toBuilder(«assignPlainValue(scope, op, type.card.isMany)»);
			«ELSE»
				«op.assignTarget(outs, scope)» = «assignPlainValue(scope, op, type.card.isMany)»;«ENDIF»'''
		else {
			'''
				«op.assignTarget(outs, scope)»
					«FOR seg : pathAsList»«IF seg.next !== null».getOrCreate«seg.attribute.name.toFirstUpper»(«IF seg.attribute.card.isMany»0«ENDIF»)
					«IF isReference(seg.attribute)».getOrCreateValue()«ENDIF»«ELSE»
					.«IF seg.attribute.card.isMany»add«ELSE»set«ENDIF»«seg.attribute.name.toFirstUpper»«IF seg.attribute.isReference && !op.assignAsKey»Value«ENDIF»(«assignValue(scope, op, op.assignAsKey, root)»)«ENDIF»«ENDFOR»;
			'''
		}
	}

	private def boolean assignAsKey(Operation op) {
		return op.expression instanceof AsKeyOperation
	}

	private def StringConcatenationClient assign(JavaScope scope, OutputOperation op,
		Map<ShortcutDeclaration, Boolean> outs, Attribute type, int index, RootPackage root) {
		val pathAsList = op.pathAsSegmentList

		if (pathAsList.isEmpty) {
			// assign function output object
			if (op.add) {
				val addVarName = scope.createUniqueIdentifier("addVar")
				'''
				«IF needsBuilder(op.assignRoot)»
					«type.toBuilderType» «addVarName» = toBuilder(«assignPlainValue(scope, op, type.card.isMany)»);
				«ELSE»
					«type.toBuilderType» «addVarName» = «assignPlainValue(scope, op, type.card.isMany)»;«ENDIF»
				«op.assignTarget(outs, scope)».addAll(«addVarName»);'''
			} else {
				'''
				«IF needsBuilder(op.assignRoot)»
					«op.assignTarget(outs, scope)» = toBuilder(«assignPlainValue(scope, op, type.card.isMany)»);
				«ELSE»
					«op.assignTarget(outs, scope)» = «assignPlainValue(scope, op, type.card.isMany)»;«ENDIF»'''
			}
		} else { // assign an attribute of the function output object
			'''
				«op.assignTarget(outs, scope)»
					«FOR seg : pathAsList»
						«IF seg.next !== null».getOrCreate«seg.attribute.name.toFirstUpper»(«IF seg.attribute.card.isMany»0«ENDIF»)«IF isReference(seg.attribute)».getOrCreateValue()«ENDIF»
					«ELSE».«IF op.add»add«ELSE»set«ENDIF»«seg.attribute.name.toFirstUpper»«IF seg.attribute.isReference && !op.assignAsKey»Value«ENDIF»(«assignValue(scope, op, op.assignAsKey, seg.attribute.card.isMany, root)»);«ENDIF»
					«ENDFOR»
			'''
		}
	}

	private def StringConcatenationClient assignValue(JavaScope scope, Operation op, boolean assignAsKey,
		RootPackage root) {
		assignValue(scope, op, assignAsKey, cardinality.isMulti(op.expression), root)
	}

	private def StringConcatenationClient assignValue(JavaScope scope, Operation op, boolean assignAsKey,
		boolean isAssigneeMulti, RootPackage root) {
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

	private def StringConcatenationClient assignPlainValue(JavaScope scope, Operation operation,
		boolean isAssigneeMulti) {
		'''«expressionGenerator.javaCode(operation.expression, scope)»«IF isAssigneeMulti».getMulti()«ELSE».get()«ENDIF»'''
	}

	def boolean hasMeta(RType type) {
		if (type instanceof RAnnotateType) {
			type.hasMeta
		}

		false
	}

	private def boolean isReference(RosettaNamed ele) {
		switch (ele) {
			Annotated: hasMetaDataAnnotations(ele) || hasMetaDataAddress(ele)
			default: false
		}
	}

	private def StringConcatenationClient assignTarget(Operation operation, Map<ShortcutDeclaration, Boolean> outs,
		JavaScope scope) {
		val root = operation.assignRoot
		switch (root) {
			Attribute: '''«scope.getIdentifierOrThrow(root)»'''
			ShortcutDeclaration:
				unfoldLHSShortcut(root, scope)
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
		if (f.card.isMany) '''getOrCreate«f.name.toFirstUpper»(0)''' else '''getOrCreate«f.name.toFirstUpper»()'''
	}

	private def dispatch StringConcatenationClient lhsExpand(RosettaSymbol c, JavaScope scope) {
		throw new IllegalStateException("No implementation for lhsExpand for " + c.class)
	}

	private def dispatch StringConcatenationClient lhsExpand(Attribute c,
		JavaScope scope) '''«scope.getIdentifierOrThrow(c)»'''

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

	def private StringConcatenationClient shortcutJavaType(ShortcutDeclaration feature) {
		val rType = typeProvider.getRType(feature.expression)
		val javaType = rType.toJavaReferenceType
		'''«javaType»«IF needsBuilder(rType)».«javaType»Builder«ENDIF»'''
	}

	private def JavaType toBuilderType(Attribute attr) {
		var javaType = typeProvider.getRTypeOfSymbol(attr).toJavaReferenceType as JavaClass
		if(needsBuilder(attr)) javaType = javaType.toBuilderType
		if (attr.card.isMany) {
			return new JavaParametrizedType(JavaClass.from(List), javaType)
		} else {
			return javaType
		}
	}
	
	private def JavaType toBuilderType(RAttribute rAttribute) {
		var javaType = rAttribute.RType.toJavaReferenceType as JavaClass
		if(rAttribute.needsBuilder) javaType = javaType.toBuilderType
		if (rAttribute.multi) {
			return new JavaParametrizedType(JavaClass.from(List), javaType)
		} else {
			return javaType
		}
	}
}
