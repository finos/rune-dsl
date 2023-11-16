package com.regnosys.rosetta.generator.java.expression

import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.JavaIdentifierRepresentationService
import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs
import com.regnosys.rosetta.rosetta.RosettaEnumValue
import com.regnosys.rosetta.rosetta.RosettaEnumValueReference
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.RosettaExternalFunction
import com.regnosys.rosetta.rosetta.RosettaFeature
import com.regnosys.rosetta.rosetta.RosettaMetaType
import com.regnosys.rosetta.rosetta.RosettaRecordFeature
import com.regnosys.rosetta.rosetta.expression.ArithmeticOperation
import com.regnosys.rosetta.rosetta.expression.AsKeyOperation
import com.regnosys.rosetta.rosetta.expression.CardinalityModifier
import com.regnosys.rosetta.rosetta.expression.ChoiceOperation
import com.regnosys.rosetta.rosetta.expression.ClosureParameter
import com.regnosys.rosetta.rosetta.expression.ComparisonOperation
import com.regnosys.rosetta.rosetta.expression.DistinctOperation
import com.regnosys.rosetta.rosetta.expression.EqualityOperation
import com.regnosys.rosetta.rosetta.expression.ExistsModifier
import com.regnosys.rosetta.rosetta.expression.FilterOperation
import com.regnosys.rosetta.rosetta.expression.FirstOperation
import com.regnosys.rosetta.rosetta.expression.FlattenOperation
import com.regnosys.rosetta.rosetta.expression.InlineFunction
import com.regnosys.rosetta.rosetta.expression.JoinOperation
import com.regnosys.rosetta.rosetta.expression.LastOperation
import com.regnosys.rosetta.rosetta.expression.ListLiteral
import com.regnosys.rosetta.rosetta.expression.LogicalOperation
import com.regnosys.rosetta.rosetta.expression.MapOperation
import com.regnosys.rosetta.rosetta.expression.MaxOperation
import com.regnosys.rosetta.rosetta.expression.MinOperation
import com.regnosys.rosetta.rosetta.expression.ModifiableBinaryOperation
import com.regnosys.rosetta.rosetta.expression.Necessity
import com.regnosys.rosetta.rosetta.expression.OneOfOperation
import com.regnosys.rosetta.rosetta.expression.ReduceOperation
import com.regnosys.rosetta.rosetta.expression.ReverseOperation
import com.regnosys.rosetta.rosetta.expression.RosettaAbsentExpression
import com.regnosys.rosetta.rosetta.expression.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.expression.RosettaBooleanLiteral
import com.regnosys.rosetta.rosetta.expression.RosettaConditionalExpression
import com.regnosys.rosetta.rosetta.expression.RosettaContainsExpression
import com.regnosys.rosetta.rosetta.expression.RosettaCountOperation
import com.regnosys.rosetta.rosetta.expression.RosettaDisjointExpression
import com.regnosys.rosetta.rosetta.expression.RosettaExistsExpression
import com.regnosys.rosetta.rosetta.expression.RosettaExpression
import com.regnosys.rosetta.rosetta.expression.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.expression.RosettaFunctionalOperation
import com.regnosys.rosetta.rosetta.expression.RosettaImplicitVariable
import com.regnosys.rosetta.rosetta.expression.RosettaIntLiteral
import com.regnosys.rosetta.rosetta.expression.RosettaLiteral
import com.regnosys.rosetta.rosetta.expression.RosettaNumberLiteral
import com.regnosys.rosetta.rosetta.expression.RosettaOnlyElement
import com.regnosys.rosetta.rosetta.expression.RosettaOnlyExistsExpression
import com.regnosys.rosetta.rosetta.expression.RosettaStringLiteral
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference
import com.regnosys.rosetta.rosetta.expression.RosettaUnaryOperation
import com.regnosys.rosetta.rosetta.expression.SortOperation
import com.regnosys.rosetta.rosetta.expression.SumOperation
import com.regnosys.rosetta.rosetta.expression.ThenOperation
import com.regnosys.rosetta.rosetta.expression.ToEnumOperation
import com.regnosys.rosetta.rosetta.expression.ToIntOperation
import com.regnosys.rosetta.rosetta.expression.ToNumberOperation
import com.regnosys.rosetta.rosetta.expression.ToStringOperation
import com.regnosys.rosetta.rosetta.expression.ToTimeOperation
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration
import com.regnosys.rosetta.types.CardinalityProvider
import com.regnosys.rosetta.types.RDataType
import com.regnosys.rosetta.types.REnumType
import com.regnosys.rosetta.types.RType
import com.regnosys.rosetta.types.RosettaOperators
import com.regnosys.rosetta.types.RosettaTypeProvider
import com.regnosys.rosetta.types.TypeSystem
import com.regnosys.rosetta.utils.ExpressionHelper
import com.regnosys.rosetta.utils.ImplicitVariableUtil
import com.regnosys.rosetta.utils.RosettaExpressionSwitch
import com.rosetta.model.lib.expression.CardinalityOperator
import com.rosetta.model.lib.expression.ExpressionOperators
import com.rosetta.model.lib.expression.MapperMaths
import com.rosetta.model.lib.mapper.MapperC
import com.rosetta.model.lib.mapper.MapperS
import com.rosetta.model.lib.validation.ValidationResult.ChoiceRuleValidationMethod
import java.math.BigDecimal
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Arrays
import java.util.List
import java.util.Optional
import org.apache.commons.text.StringEscapeUtils
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.EcoreUtil2

import static extension com.regnosys.rosetta.generator.java.enums.EnumHelper.convertValues
import com.regnosys.rosetta.types.RObjectFactory
import javax.inject.Inject
import com.regnosys.rosetta.rosetta.expression.RosettaConstructorExpression
import com.regnosys.rosetta.generator.java.util.RecordJavaUtil
import com.regnosys.rosetta.types.builtin.RRecordType
import java.util.stream.Collectors
import com.regnosys.rosetta.rosetta.RosettaRule
import com.rosetta.util.types.JavaType
import com.rosetta.model.lib.mapper.Mapper
import com.rosetta.util.types.JavaPrimitiveType
import com.rosetta.model.lib.mapper.MapperListOfLists
import com.regnosys.rosetta.types.RShortcut
import com.regnosys.rosetta.types.RFunction
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil
import java.math.BigInteger
import com.regnosys.rosetta.generator.java.statement.builder.JavaStatementBuilder
import com.regnosys.rosetta.generator.java.statement.builder.JavaExpression
import com.regnosys.rosetta.generator.java.statement.builder.JavaVariable
import com.regnosys.rosetta.generator.java.statement.builder.JavaIfThenElseBuilder

class ExpressionGenerator extends RosettaExpressionSwitch<JavaStatementBuilder, ExpressionGenerator.Context> {
	
	static class Context {
		public JavaType expectedType;
		public JavaScope scope;
	}

	@Inject protected RosettaTypeProvider typeProvider
	@Inject RosettaOperators operators
	@Inject extension CardinalityProvider cardinalityProvider
	@Inject RosettaFunctionExtensions funcExt
	@Inject extension RosettaExtensions
	@Inject extension ImportManagerExtension
	@Inject ExpressionHelper exprHelper
	@Inject extension ImplicitVariableUtil
	@Inject extension JavaIdentifierRepresentationService
	@Inject RecordJavaUtil recordUtil
	@Inject extension JavaTypeTranslator
	@Inject extension TypeSystem
	@Inject RObjectFactory rObjectFactory;
	@Inject TypeCoercionService typeCoercionService
	@Inject extension JavaTypeUtil typeUtil
	
	/**
	 * convert a rosetta expression to code
	 * ParamMpa params  - a map keyed by classname or positional index that provides variable names for expression parameters
	 */
	def JavaStatementBuilder javaCode(RosettaExpression expr, JavaType expectedType, JavaScope scope) {
		val rawResult = doSwitch(expr, new Context => [{
			it.expectedType = expectedType
			it.scope = scope
		}])
		return typeCoercionService.addCoercions(rawResult, expectedType, scope)
	}

	private def StringConcatenationClient runtimeMethod(String methodName) {
		'''«importWildcard(method(ExpressionOperators, methodName))»'''
	}
	private def JavaStatementBuilder applyRuntimeMethod(JavaStatementBuilder expr, String methodName, JavaType resultType) {
		expr.mapExpression[JavaExpression.from('''«runtimeMethod(methodName)»(«it»)''', resultType)]
	}	

	private def JavaStatementBuilder callableWithArgsCall(RosettaCallableWithArgs callable, List<RosettaExpression> arguments, JavaScope scope) {
		return switch (callable) {
			Function,
			RosettaRule: {
				val rCallable = if (callable instanceof Function)
						rObjectFactory.buildRFunction(callable)
					else
						rObjectFactory.buildRFunction(callable as RosettaRule)
				val outputType = rCallable.output.attributeToJavaType
				if (arguments.empty) {
					JavaExpression.from('''«scope.getIdentifierOrThrow(rCallable.toFunctionInstance)».evaluate()''', outputType)
				} else {
					// First evaluate all arguments
					var argCode = arguments.head.javaCode(rCallable.inputs.head.attributeToJavaType, scope)
					for (var i = 1; i < arguments.size; i++) {
						argCode = argCode.then(
							arguments.get(i).javaCode(rCallable.inputs.get(i).attributeToJavaType, scope),
							[argList, newArg|JavaExpression.from('''«argList», «newArg»''', null)],
							scope
						)
					}
					argCode
						.collapseToSingleExpression(scope)
						.mapExpression[JavaExpression.from('''«scope.getIdentifierOrThrow(rCallable.toFunctionInstance)».evaluate(«it»)''', outputType)]
				}
			}
			RosettaExternalFunction: {
				val returnRType = typeProvider.getRTypeOfSymbol(callable)
				if (arguments.empty) {
					JavaExpression.from('''new «callable.toFunctionJavaClass»().execute()''', returnRType.toJavaReferenceType)
				} else {
					// First evaluate all arguments
					var JavaStatementBuilder argCode
					val argRTypes = arguments.map[typeProvider.getRType(it)]
					if (argRTypes.forall[isSubtypeOf(returnRType)]) {
						// TODO: this is a hack
						// Generic return type for number type e.g. Min(1,2) or Max(2,6)
						val argAndReturnType = argRTypes.join.toJavaReferenceType
						argCode = arguments.head.javaCode(argAndReturnType, scope)
						for (var i = 1; i < arguments.size; i++) {
							argCode = argCode.then(
								arguments.get(i).javaCode(argAndReturnType, scope),
								[argList, newArg|JavaExpression.from('''«argList», «newArg»''', null)],
								scope
							)
						}
						argCode
							.mapExpressionIfNotNull[JavaExpression.from('''new «callable.toFunctionJavaClass»().execute(«it»)''', argAndReturnType)]
					} else {
						argCode = arguments.head.javaCode(callable.parameters.head.typeCall.typeCallToRType.toJavaReferenceType, scope)
						for (var i = 1; i < arguments.size; i++) {
							argCode = argCode.then(
								arguments.get(i).javaCode(callable.parameters.get(i).typeCall.typeCallToRType.toJavaReferenceType, scope),
								[argList, newArg|JavaExpression.from('''«argList», «newArg»''', null)],
								scope
							)
						}
						argCode
							.mapExpressionIfNotNull[JavaExpression.from('''new «callable.toFunctionJavaClass»().execute(«it»)''', returnRType.toJavaReferenceType)]
					}
				}
			}
			default:
				throw new UnsupportedOperationException("Unsupported callable with args of type " +
					callable?.eClass?.name)
		}
	}

	private def JavaStatementBuilder implicitVariable(EObject context, JavaScope scope) {
		val itemType = typeProvider.typeOfImplicitVariable(context).toJavaReferenceType
		val definingContainer = context.findContainerDefiningImplicitVariable.get
		val actualType = if (definingContainer instanceof Data || definingContainer instanceof RosettaRule) {
			// For conditions and rules
			itemType
		} else {
			// For inline functions
			val f = definingContainer as RosettaFunctionalOperation
			if (f instanceof ThenOperation && f.argument.isOutputListOfLists) {
				MapperListOfLists.wrap(itemType)
			} else if (context.implicitVariableMulti) {
				MapperC.wrap(itemType)
			} else {
				MapperS.wrap(itemType)
			}
		}
		new JavaVariable(scope.getIdentifierOrThrow(context.implicitVarInContext), actualType)
	}
	
	def StringConcatenationClient aliasCallArgs(RShortcut alias, RFunction function, JavaScope scope) {
		val output = function.output
		val inputs = function.inputs
		'''
			«IF exprHelper.usesOutputParameter(alias.expression)»«scope.getIdentifierOrThrow(output)».toBuilder()«IF !inputs.empty», «ENDIF»«ENDIF
			»«FOR input : inputs SEPARATOR ", "»«scope.getIdentifierOrThrow(input)»«ENDFOR»'''
	}

	private def StringConcatenationClient aliasCallArgs(ShortcutDeclaration alias, JavaScope scope) {
		val func = EcoreUtil2.getContainerOfType(alias, Function)
		val output = rObjectFactory.buildRAttribute(funcExt.getOutput(func))
		val inputs = funcExt.getInputs(func).map[rObjectFactory.buildRAttribute(it)]
		'''
			«IF exprHelper.usesOutputParameter(alias.expression)»«scope.getIdentifierOrThrow(output)».toBuilder()«IF !inputs.empty», «ENDIF»«ENDIF
			»«FOR input : inputs SEPARATOR ", "»«scope.getIdentifierOrThrow(input)»«ENDFOR»'''
	}

	private def JavaStatementBuilder featureCall(JavaStatementBuilder receiverCode, RType receiverType, RosettaFeature feature, JavaScope scope, boolean autoValue) {
		val resultItemType = typeProvider.getRTypeOfFeature(feature).toJavaReferenceType
		val StringConcatenationClient right = switch (feature) {
			Attribute:
				feature.buildMapFunc(autoValue, scope)
			RosettaMetaType: 
				feature.buildMapFunc(scope)
			RosettaEnumValue:
				return JavaExpression.from('''«resultItemType».«feature.convertValues»''', resultItemType)
			RosettaRecordFeature:
				'''.<«feature.typeCall.typeCallToRType.toJavaReferenceType»>map("«feature.name.toFirstUpper»", «recordUtil.recordFeatureToLambda(receiverType as RRecordType, feature, scope)»)'''
			default:
				throw new UnsupportedOperationException("Unsupported feature type of " + feature?.class?.name)
		}
		val mapperReceiverCode = typeCoercionService.addCoercions(receiverCode, Mapper.wrapExtends(resultItemType), scope)
		val resultWrapper = if (mapperReceiverCode.expressionType.isMapperS && !cardinalityProvider.isFeatureMulti(feature)) {
			MapperS
		} else {
			MapperC
		}
		val resultType = resultWrapper.wrap(resultItemType)
		return mapperReceiverCode
			.collapseToSingleExpression(scope)
			.mapExpression[JavaExpression.from('''«it»«right»''', resultType)]
	}

	private def binaryExpr(RosettaBinaryOperation expr, Context context) {
		val left = expr.left
		val right = expr.right
		val leftRtype = typeProvider.getRType(expr.left)
		val rightRtype = typeProvider.getRType(expr.right)
		val joined = leftRtype.join(rightRtype).toJavaReferenceType
		val resultType = operators.resultType(expr.operator, leftRtype, rightRtype).toJavaReferenceType
		val leftType = leftRtype.toJavaReferenceType
		val rightType = rightRtype.toJavaReferenceType

		switch expr.operator {
			case "and",
			case "or": {
				val leftCode = javaCode(left, COMPARISON_RESULT, context.scope)
				val rightCode = javaCode(right, COMPARISON_RESULT, context.scope)
				leftCode
					.then(rightCode, [l, r|JavaExpression.from('''«l».«expr.operator»(«r»)''', COMPARISON_RESULT)], context.scope)
			}
			case "+",
			case "-",
			case "*",
			case "/": {
				val method = switch expr.operator {
					case "+": "add"
					case "-": "subtract"
					case "*": "multiply"
					case "/": "divide"
				}
				if (leftType.extendsNumber && rightType.extendsNumber) {
					val leftCode = javaCode(left, Mapper.wrapExtends(joined), context.scope)
					val rightCode = javaCode(right, Mapper.wrapExtends(joined), context.scope)
					leftCode
						.then(rightCode, [l, r|JavaExpression.from('''«MapperMaths».<«resultType», «joined», «joined»>«method»(«l», «r»)''', MapperS.wrap(resultType))], context.scope)
				} else {
					val leftCode = javaCode(left, Mapper.wrapExtends(leftType), context.scope)
					val rightCode = javaCode(right, Mapper.wrapExtends(rightType), context.scope)
					leftCode
						.then(rightCode, [l, r|JavaExpression.from('''«MapperMaths».<«resultType», «leftType», «rightType»>«method»(«l», «r»)''', MapperS.wrap(resultType))], context.scope)
				}
			}
			case "contains",
			case "disjoint": {
				val leftCode = javaCode(left, Mapper.wrapExtends(joined), context.scope)
				val rightCode = javaCode(right, Mapper.wrapExtends(joined), context.scope)
				leftCode
					.then(rightCode, [l, r|JavaExpression.from('''«runtimeMethod(expr.operator)»(«l», «r»)''', COMPARISON_RESULT)], context.scope)
			}
			case "join": {
				val leftCode = javaCode(left, MapperC.wrapExtends(STRING), context.scope)
				val rightCode = expr.right === null ? JavaExpression.from('''«MapperS».of("")''', resultType) : javaCode(right, MapperS.wrap(STRING), context.scope)
				leftCode
					.then(rightCode, [l, r|JavaExpression.from('''«l».join(«r»)''', MapperS.wrap(STRING))], context.scope)
			}
			case "=",
			case "<>",
			case "<",
			case "<=",
			case ">",
			case ">=": {
				val method = switch expr.operator {
					case "=": 'areEqual'
					case "<>": 'notEqual'
					case "<": 'lessThan'
					case "<=": 'lessThanEquals'
					case ">": 'greaterThan'
					case ">=": 'greaterThanEquals'
				}
				val modifier = (expr as ModifiableBinaryOperation).cardMod
				val defaultModifier = if (expr.operator == '<>') {
					CardinalityModifier.ANY
				} else {
					CardinalityModifier.ALL
				}
				val leftCode = javaCode(left, Mapper.wrapExtends(joined), context.scope)
				val rightCode = javaCode(right, Mapper.wrapExtends(joined), context.scope)
				leftCode
					.then(rightCode, [l, r|JavaExpression.from('''«runtimeMethod(method)»(«l», «r», «toCardinalityOperator(modifier, defaultModifier)»)''', COMPARISON_RESULT)], context.scope)
			}
			default:
				throw new UnsupportedOperationException("Unsupported binary operation of " + expr.operator)
		}
	}

	private def StringConcatenationClient toCardinalityOperator(CardinalityModifier cardOp,
		CardinalityModifier defaultOp) {
		'''«CardinalityOperator».«if (cardOp === CardinalityModifier.NONE) defaultOp.toString.toFirstUpper else cardOp.toString.toFirstUpper»'''
	}

	/**
	 * Builds the expression of mapping functions to extract a path of attributes
	 */
	private def StringConcatenationClient buildMapFunc(Attribute attribute, boolean autoValue, JavaScope scope) {
		val mapFunc = attribute.buildMapFuncAttribute(scope)
		val resultType = if (attribute.metaAnnotations.nullOrEmpty) {
				typeProvider.getRTypeOfSymbol(attribute).toJavaReferenceType
			} else {
				attribute.toMetaJavaType
			}
		if (attribute.card.isIsMany) {
			if (attribute.metaAnnotations.nullOrEmpty || !autoValue)
				'''.<«resultType»>mapC(«mapFunc»)'''
			else {
				'''.<«resultType»>mapC(«mapFunc»).<«typeProvider.getRTypeOfSymbol(attribute).toJavaReferenceType»>map("getValue", _f->_f.getValue())'''
			}
		} else {
			if (attribute.metaAnnotations.nullOrEmpty || !autoValue) {
				'''.<«resultType»>map(«mapFunc»)'''
			} else
				'''.<«resultType»>map(«mapFunc»).<«typeProvider.getRTypeOfSymbol(attribute).toJavaReferenceType»>map("getValue", _f->_f.getValue())'''
		}
	}

	private def static StringConcatenationClient buildMapFunc(RosettaMetaType meta, JavaScope scope) {
		if (meta.name == "reference") {
			val lambdaScope = scope.lambdaScope
			val lambdaParam = lambdaScope.createUniqueIdentifier("a")
			'''.map("get«meta.name.toFirstUpper»", «lambdaParam»->«lambdaParam».getGlobalReference())'''
		} else {
			val lambdaScope1 = scope.lambdaScope
			val lambdaParam1 = lambdaScope1.createUniqueIdentifier("a")
			val lambdaScope2 = scope.lambdaScope
			val lambdaParam2 = lambdaScope2.createUniqueIdentifier("a")
			'''.map("getMeta", «lambdaParam1»->«lambdaParam1».getMeta()).map("get«meta.name.toFirstUpper»", «lambdaParam2»->«lambdaParam2».get«meta.name.toFirstUpper»())'''
		}
	}

	private def Pair<StringConcatenationClient, JavaType> inlineFunction(InlineFunction ref, JavaType expectedType, JavaScope scope) {
		val lambdaScope = scope.lambdaScope
		val paramIds = if (ref.parameters.size == 0) {
				#[lambdaScope.createIdentifier(ref.implicitVarInContext, defaultImplicitVariable.name)]
			} else {
				ref.parameters.stream.map[lambdaScope.createIdentifier(it)].collect(Collectors.toList)
			}
		
		val body = ref.body.javaCode(expectedType, lambdaScope)
		new Pair<StringConcatenationClient, JavaType>(
			if (paramIds.size == 1) {
				'''«paramIds.head» -> «body.toLambdaBody»'''
			} else {
				'''(«FOR id : paramIds SEPARATOR ', '»«id»«ENDFOR») -> «body.toLambdaBody»'''
			},
			body.expressionType
		)
	}

	private def buildConstraint(RosettaExpression arg, Iterable<Attribute> usedAttributes,
		Necessity validationType, Context context) {
		val argItemType = typeProvider.getRType(arg).toJavaReferenceType
		arg.javaCode(Mapper.wrapExtends(argItemType), context.scope)
			.collapseToSingleExpression(context.scope)
			.mapExpression[JavaExpression.from('''«runtimeMethod('choice')»(«it», «Arrays».asList(«usedAttributes.join(", ")['"' + name + '"']»), «ChoiceRuleValidationMethod».«validationType.name()»)''', COMPARISON_RESULT)]
	}

	private def JavaStatementBuilder buildListOperationNoBody(RosettaUnaryOperation op, String name, JavaType expectedArgumentType, (JavaType) => JavaType argumentTypeToReturnType, JavaScope scope) {
		val argCode = op.argument.javaCode(expectedArgumentType, scope)
			.collapseToSingleExpression(scope)
		argCode
			.mapExpression[JavaExpression.from(
				'''
				«it»
					.«name»()''',
				argumentTypeToReturnType.apply(argCode.expressionType)
			)]
	}

	private def JavaStatementBuilder buildSingleItemListOperationOptionalBody(RosettaFunctionalOperation op, String name, JavaType expectedArgumentType, JavaType expectedBodyType, (JavaType, JavaType) => JavaType argumentAndBodyTypeToReturnType, JavaScope scope) {
		if (op.function === null) {
			buildListOperationNoBody(op, name, expectedArgumentType, [argumentAndBodyTypeToReturnType.apply(it, null)], scope)
		} else {
			buildSingleItemListOperation(op, name, expectedArgumentType, expectedBodyType, argumentAndBodyTypeToReturnType, scope)
		}
	}

	private def JavaStatementBuilder buildSingleItemListOperation(RosettaFunctionalOperation op, String name, JavaType expectedArgumentType, JavaType expectedBodyType, (JavaType, JavaType) => JavaType argumentAndBodyTypeToReturnType, JavaScope scope) {
		val argCode = op.argument.javaCode(expectedArgumentType, scope)
			.collapseToSingleExpression(scope)
		val inlineFunctionCodeAndBodyType = op.function.inlineFunction(expectedBodyType, scope)
		val StringConcatenationClient inlineFunctionCode = inlineFunctionCodeAndBodyType.key
		val inlineFunctionBodyType = inlineFunctionCodeAndBodyType.value
		argCode
			.mapExpression[JavaExpression.from(
				'''
				«it»
					.«name»(«inlineFunctionCode»)''',
				argumentAndBodyTypeToReturnType.apply(argCode.expressionType, inlineFunctionBodyType)
			)]
	}

	private def StringConcatenationClient buildMapFuncAttribute(Attribute attribute, JavaScope scope) {
		if (attribute.eContainer instanceof Data) {
			val lambdaScope = scope.lambdaScope
			val lambdaParam = lambdaScope.createUniqueIdentifier(attribute.attributeTypeVariableName)
			'''"get«attribute.name.toFirstUpper»", «lambdaParam» -> «IF attribute.override»(«typeProvider.getRTypeOfSymbol(attribute).toJavaReferenceType») «ENDIF»«lambdaParam».get«attribute.name.toFirstUpper»()'''
		}
	}

	private def attributeTypeVariableName(Attribute attribute) {
		new RDataType(attribute.eContainer as Data).toJavaType.simpleName.toFirstLower
	}

	/**
	 * Create a string representation of a rosetta function  
	 * mainly used to give human readable names to the mapping functions used to extract attributes
	 */
	def StringConcatenationClient toNodeLabel(RosettaExpression expr) {
		switch (expr) {
			RosettaFeatureCall: {
				toNodeLabel(expr)
			}
			RosettaBinaryOperation: {
				toNodeLabel(expr)
			}
			RosettaStringLiteral: {
				'''\"«expr.value»\"'''
			}
			RosettaConditionalExpression: {
				'''choice'''
			}
			RosettaEnumValueReference: {
				'''«expr.enumeration.name»'''
			}
			RosettaEnumValue: {
				'''«expr.name»'''
			}
			ListLiteral: {
				'''[«FOR el : expr.elements SEPARATOR ", "»«el.toNodeLabel»«ENDFOR»]'''
			}
			RosettaLiteral: {
				'''«expr.stringValue»'''
			}
			RosettaSymbolReference: {
				'''«expr.symbol.name»«IF expr.explicitArguments»(«FOR arg:expr.args SEPARATOR ", "»«arg.toNodeLabel»«ENDFOR»)«ENDIF»'''
			}
			RosettaImplicitVariable: {
				'''«defaultImplicitVariable.name»'''
			}
			RosettaFunctionalOperation: {
				'''«toNodeLabel(expr.argument)» «expr.operator»«IF expr.function !== null» [«toNodeLabel(expr.function.body)»]«ENDIF»'''
			}
			RosettaUnaryOperation: {
				'''«toNodeLabel(expr.argument)» «expr.operator»'''
			}
			default: '''Unsupported expression type of «expr?.class?.name»'''
		}
	}

	def StringConcatenationClient toNodeLabel(RosettaFeatureCall call) {
		val feature = call.feature
		val right = switch feature {
			RosettaMetaType,
			Attribute,
			RosettaEnumValue:
				feature.name
			default:
				throw new UnsupportedOperationException("Unsupported expression type (feature) " + feature?.getClass)
		}

		val receiver = call.receiver
		val left = receiver.toNodeLabel

		'''«left»->«right»'''
	}

	def StringConcatenationClient toNodeLabel(RosettaBinaryOperation binOp) {
		'''«binOp.left.toNodeLabel» «binOp.operator» «binOp.right.toNodeLabel»'''
	}

	override protected caseAbsentOperation(RosettaAbsentExpression expr, Context context) {
		expr.argument.javaCode(Mapper.wrapExtends(expr.argument), context.scope)
			.applyRuntimeMethod('notExists', COMPARISON_RESULT)
	}

	override protected caseAddOperation(ArithmeticOperation expr, Context context) {
		binaryExpr(expr, context)
	}

	override protected caseAndOperation(LogicalOperation expr, Context context) {
		binaryExpr(expr, context)
	}

	override protected caseAsKeyOperation(AsKeyOperation expr, Context context) {
		// this operation is currently handled by the `FunctionGenerator`
		doSwitch(expr.argument, context)
	}

	override protected caseBooleanLiteral(RosettaBooleanLiteral expr, Context context) {
		JavaExpression.from('''«expr.value»''', JavaPrimitiveType.BOOLEAN)
	}

	override protected caseChoiceOperation(ChoiceOperation expr, Context context) {
		buildConstraint(expr.argument, expr.attributes, expr.necessity, context)
	}

	override protected caseConditionalExpression(RosettaConditionalExpression expr, Context context) {
		val condition = expr.^if.javaCode(JavaPrimitiveType.BOOLEAN, context.scope)
		val thenBranch = expr.ifthen.javaCode(context.expectedType, context.scope)
		val elseBranch = expr.elsethen.javaCode(context.expectedType, context.scope)
		
		// TODO: fix result type (should join both types)
		condition
			.collapseToSingleExpression(context.scope)
			.mapExpression[new JavaIfThenElseBuilder(it, thenBranch, elseBranch, typeUtil)]
	}

	override protected caseContainsOperation(RosettaContainsExpression expr, Context context) {
		binaryExpr(expr, context)
	}

	override protected caseCountOperation(RosettaCountOperation expr, Context context) {
		expr.argument.javaCode(Mapper.wrapExtends(expr.argument), context.scope)
			.mapExpression[JavaExpression.from('''«it».resultCount()''', JavaPrimitiveType.INT)]
	}

	override protected caseDisjointOperation(RosettaDisjointExpression expr, Context context) {
		binaryExpr(expr, context)
	}

	override protected caseDistinctOperation(DistinctOperation expr, Context context) {
		val argItemType = typeProvider.getRType(expr.argument).toJavaReferenceType
		val argCode = expr.argument.javaCode(Mapper.wrapExtends(argItemType), context.scope)
		val argType = argCode.expressionType
		argCode
			.applyRuntimeMethod('distinct', argType.hasWildcardArgument ? MapperC.wrapExtends(argItemType) : MapperC.wrap(argItemType))
	}

	override protected caseDivideOperation(ArithmeticOperation expr, Context context) {
		binaryExpr(expr, context)
	}

	override protected caseEqualsOperation(EqualityOperation expr, Context context) {
		binaryExpr(expr, context)
	}

	override protected caseExistsOperation(RosettaExistsExpression expr, Context context) {
		val methodName = if (expr.modifier === ExistsModifier.SINGLE)
				'singleExists'
			else if (expr.modifier === ExistsModifier.MULTIPLE)
				'multipleExists'
			else
				'exists'
		expr.argument.javaCode(Mapper.wrapExtends(expr.argument), context.scope)
			.applyRuntimeMethod(methodName, COMPARISON_RESULT)
	}

	override protected caseFeatureCall(RosettaFeatureCall expr, Context context) {
		var autoValue = true // if the attribute being referenced is WithMeta and we aren't accessing the meta fields then access the value by default
		if (expr.eContainer instanceof RosettaFeatureCall &&
			(expr.eContainer as RosettaFeatureCall).feature instanceof RosettaMetaType) {
			autoValue = false;
		}
		return featureCall(expr.receiver.javaCode(Mapper.wrapExtends(expr.receiver), context.scope), typeProvider.getRType(expr.receiver), expr.feature, context.scope, autoValue)
	}

	override protected caseFilterOperation(FilterOperation expr, Context context) {
		val StringConcatenationClient inlineFunctionCode = expr.function.inlineFunction(JavaPrimitiveType.BOOLEAN.toReferenceType, context.scope).key
		if (!expr.isPreviousOperationMulti) {
			// Case MapperS
			val argCode = expr.argument.javaCode(MapperS.wrapExtends(expr.argument), context.scope)
				.collapseToSingleExpression(context.scope)
			argCode
				.mapExpression[JavaExpression.from(
					'''
					«it»
						.filterSingleNullSafe(«inlineFunctionCode»)''',
					argCode.expressionType
				)]
		} else {
			if (expr.argument.isOutputListOfLists) {
				// Case MapperListOfLists
				val argCode = expr.argument.javaCode(MapperListOfLists.wrapExtends(expr.argument), context.scope)
					.collapseToSingleExpression(context.scope)
				argCode
					.mapExpression[JavaExpression.from(
						'''
						«it»
							.filterListNullSafe(«inlineFunctionCode»)''',
						argCode.expressionType
					)]
			} else {
				// Case MapperC
				val argCode = expr.argument.javaCode(MapperC.wrapExtends(expr.argument), context.scope)
					.collapseToSingleExpression(context.scope)
				argCode
					.mapExpression[JavaExpression.from(
						'''
						«it»
							.filterItemNullSafe(«inlineFunctionCode»)''',
						argCode.expressionType
					)]
			}
		}
	}

	override protected caseFirstOperation(FirstOperation expr, Context context) {
		buildListOperationNoBody(expr, "first", MapperC.wrapExtends(expr.argument), [MapperS.wrap(it.itemType)], context.scope)
	}

	override protected caseFlattenOperation(FlattenOperation expr, Context context) {
		buildListOperationNoBody(expr, "flattenList", MapperListOfLists.wrapExtends(expr.argument), [it.hasWildcardArgument ? MapperC.wrapExtends(expr.argument) : MapperC.wrap(expr.argument)], context.scope)
	}

	override protected caseGreaterThanOperation(ComparisonOperation expr, Context context) {
		binaryExpr(expr, context)
	}

	override protected caseGreaterThanOrEqualOperation(ComparisonOperation expr, Context context) {
		binaryExpr(expr, context)
	}

	override protected caseImplicitVariable(RosettaImplicitVariable expr, Context context) {
		implicitVariable(expr, context.scope)
	}

	override protected caseIntLiteral(RosettaIntLiteral expr, Context context) {
		val intValue = expr.value.intValue
		if (BigInteger.valueOf(intValue) == expr.value) {
			return JavaExpression.from('''«intValue»''', JavaPrimitiveType.INT)
		}
		val longValue = expr.value.longValue
		if (BigInteger.valueOf(intValue) == expr.value) {
			return JavaExpression.from('''«longValue»l''', JavaPrimitiveType.LONG)
		}
		return JavaExpression.from('''new «BigInteger»("«expr.value»")''', BIG_INTEGER)
	}

	override protected caseJoinOperation(JoinOperation expr, Context context) {
		binaryExpr(expr, context)
	}

	override protected caseLastOperation(LastOperation expr, Context context) {
		buildListOperationNoBody(expr, "last", MapperC.wrapExtends(expr.argument), [MapperS.wrap(it.itemType)], context.scope)
	}

	override protected caseLessThanOperation(ComparisonOperation expr, Context context) {
		binaryExpr(expr, context)
	}

	override protected caseLessThanOrEqualOperation(ComparisonOperation expr, Context context) {
		binaryExpr(expr, context)
	}

	override protected caseListLiteral(ListLiteral expr, Context context) {
		if (expr.elements.empty) {
			return JavaExpression.NULL
		}
		val itemType = typeProvider.getRType(expr).toJavaReferenceType
		val first = expr.elements.head
		var elementsCode = first.javaCode(first.isMulti ? MapperC.wrapExtends(itemType) : MapperS.wrapExtends(itemType), context.scope)
		for (var i = 1; i < expr.elements.size; i++) {
			val elem = expr.elements.get(i)
			elementsCode = elementsCode.then(
				elem.javaCode(elem.isMulti ? MapperC.wrapExtends(itemType) : MapperS.wrapExtends(itemType), context.scope),
				[elemList, newElem|JavaExpression.from('''«elemList», «newElem»''', null)],
				context.scope
			)
		}
		elementsCode
			.collapseToSingleExpression(context.scope)
			.mapExpression[
				JavaExpression.from(
					'''«MapperC».<«itemType»>of(«it»)''',
					MapperC.wrap(itemType)
				)
			]
	}

	override protected caseMapOperation(MapOperation expr, Context context) {
		val bodyItemType = typeProvider.getRType(expr.function.body).toJavaReferenceType
		val isBodyMulti = expr.function.isBodyExpressionMulti

		if (!expr.isPreviousOperationMulti) {
			if (isBodyMulti) {
				// Case MapperS to MapperC
				val inlineFunctionCodeAndBodyType = expr.function.inlineFunction(MapperC.wrapExtends(bodyItemType), context.scope)
				val StringConcatenationClient inlineFunctionCode = inlineFunctionCodeAndBodyType.key
				val inlineFunctionBodyType = inlineFunctionCodeAndBodyType.value
				expr.argument.javaCode(MapperS.wrapExtends(expr.argument), context.scope)
					.collapseToSingleExpression(context.scope)
					.mapExpression[JavaExpression.from(
						'''
						«it»
							.mapSingleToList(«inlineFunctionCode»)''',
						inlineFunctionBodyType
					)]
			} else {
				// Case MapperS to MapperS
				buildSingleItemListOperationOptionalBody(expr, "mapSingleToItem", MapperS.wrapExtends(expr.argument), MapperS.wrapExtends(bodyItemType), [a,b|b], context.scope)
			}
		} else {
			if (expr.argument.isOutputListOfLists) {
				if (isBodyMulti) {
					// Case MapperListOfLists to MapperListOfLists
					val inlineFunctionCodeAndBodyType = expr.function.inlineFunction(MapperC.wrapExtends(bodyItemType), context.scope)
					val StringConcatenationClient inlineFunctionCode = inlineFunctionCodeAndBodyType.key
					val inlineFunctionBodyType = inlineFunctionCodeAndBodyType.value
					expr.argument.javaCode(MapperListOfLists.wrapExtends(expr.argument), context.scope)
						.collapseToSingleExpression(context.scope)
						.mapExpression[JavaExpression.from(
							'''
							«it»
								.mapListToList(«inlineFunctionCode»)''',
							inlineFunctionBodyType.hasWildcardArgument ? MapperListOfLists.wrapExtends(bodyItemType) : MapperListOfLists.wrap(bodyItemType)
						)]
				} else {
					// Case MapperListOfLists to MapperC
					val inlineFunctionCodeAndBodyType = expr.function.inlineFunction(MapperS.wrapExtends(bodyItemType), context.scope)
					val StringConcatenationClient inlineFunctionCode = inlineFunctionCodeAndBodyType.key
					val inlineFunctionBodyType = inlineFunctionCodeAndBodyType.value
					expr.argument.javaCode(MapperListOfLists.wrapExtends(expr.argument), context.scope)
						.collapseToSingleExpression(context.scope)
						.mapExpression[JavaExpression.from(
							'''
							«it»
								.mapListToItem(«inlineFunctionCode»)''',
							inlineFunctionBodyType.hasWildcardArgument ? MapperC.wrapExtends(bodyItemType) : MapperC.wrap(bodyItemType)
						)]
				}
			} else {
				if (isBodyMulti) {
					// MapperC to MapperListOfLists
					val inlineFunctionCodeAndBodyType = expr.function.inlineFunction(MapperC.wrapExtends(bodyItemType), context.scope)
					val StringConcatenationClient inlineFunctionCode = inlineFunctionCodeAndBodyType.key
					val inlineFunctionBodyType = inlineFunctionCodeAndBodyType.value
					expr.argument.javaCode(MapperC.wrapExtends(expr.argument), context.scope)
						.collapseToSingleExpression(context.scope)
						.mapExpression[JavaExpression.from(
							'''
							«it»
								.mapItemToList(«inlineFunctionCode»)''',
							inlineFunctionBodyType.hasWildcardArgument ? MapperListOfLists.wrapExtends(bodyItemType) : MapperListOfLists.wrap(bodyItemType)
						)]
				} else {
					// MapperC to MapperC
					buildSingleItemListOperationOptionalBody(expr, "mapItem", MapperC.wrapExtends(expr.argument), MapperS.wrapExtends(bodyItemType), [a,b|b.hasWildcardArgument ? MapperC.wrapExtends(bodyItemType) : MapperC.wrap(bodyItemType)], context.scope)
				}
			}
		}
	}

	override protected caseMaxOperation(MaxOperation expr, Context context) {
		val bodyType = if (expr.function !== null) MapperS.wrapExtends(expr.function.body)
		buildSingleItemListOperationOptionalBody(expr, "max", MapperC.wrapExtends(expr.argument), bodyType, [a,b|MapperS.wrap(a.itemType)], context.scope)
	}

	override protected caseMinOperation(MinOperation expr, Context context) {
		val bodyType = if (expr.function !== null) MapperS.wrapExtends(expr.function.body)
		buildSingleItemListOperationOptionalBody(expr, "min", MapperC.wrapExtends(expr.argument), bodyType, [a,b|MapperS.wrap(a.itemType)], context.scope)
	}

	override protected caseMultiplyOperation(ArithmeticOperation expr, Context context) {
		binaryExpr(expr, context)
	}

	override protected caseNotEqualsOperation(EqualityOperation expr, Context context) {
		binaryExpr(expr, context)
	}

	override protected caseNumberLiteral(RosettaNumberLiteral expr, Context context) {
		JavaExpression.from('''new «BigDecimal»("«expr.value»")''', BIG_DECIMAL)
	}

	override protected caseOneOfOperation(OneOfOperation expr, Context context) {
		val type = typeProvider.getRType(expr.argument) as RDataType
		buildConstraint(expr.argument, type.data.allAttributes, Necessity.REQUIRED, context)
	}

	override protected caseOnlyElementOperation(RosettaOnlyElement expr, Context context) {
		val itemType = typeProvider.getRType(expr.argument).toJavaReferenceType
		expr.argument.javaCode(itemType, context.scope)
	}

	override protected caseOnlyExists(RosettaOnlyExistsExpression expr, Context context) {
		expr.args.map[it.javaCode(Mapper.wrapExtends(it), context.scope)]
			.reduce[acc, stat| acc.then(stat, [list, item| JavaExpression.from('''«list», «item»''', null)], context.scope)]
			.mapExpression[JavaExpression.from('''«runtimeMethod('onlyExists')»(«Arrays».asList(«it»))''', COMPARISON_RESULT)]
	}

	override protected caseOrOperation(LogicalOperation expr, Context context) {
		binaryExpr(expr, context)
	}

	override protected caseReduceOperation(ReduceOperation expr, Context context) {
		val outputType = typeProvider.getRType(expr.function.body).toJavaReferenceType
		val inlineFunctionCodeAndBodyType = expr.function.inlineFunction(MapperS.wrapExtends(outputType), context.scope)
		val StringConcatenationClient inlineFunctionCode = inlineFunctionCodeAndBodyType.key
		val inlineFunctionBodyType = inlineFunctionCodeAndBodyType.value
		expr.argument.javaCode(MapperC.wrapExtends(expr.argument), context.scope)
			.collapseToSingleExpression(context.scope)
			.mapExpression[JavaExpression.from(
				'''
				«it»
					.<«outputType»>reduce(«inlineFunctionCode»)''',
				inlineFunctionBodyType
			)]
	}

	override protected caseReverseOperation(ReverseOperation expr, Context context) {
		buildListOperationNoBody(expr, "reverse", MapperC.wrapExtends(expr.argument), [it], context.scope)
	}

	override protected caseSortOperation(SortOperation expr, Context context) {
		val bodyType = if (expr.function !== null) MapperS.wrapExtends(expr.function.body)
		buildSingleItemListOperationOptionalBody(expr, "sort", MapperC.wrapExtends(expr.argument), bodyType, [a,b|a], context.scope)
	}

	override protected caseStringLiteral(RosettaStringLiteral expr, Context context) {
		JavaExpression.from('''"«StringEscapeUtils.escapeJava(expr.value)»"''', STRING)
	}

	override protected caseSubtractOperation(ArithmeticOperation expr, Context context) {
		binaryExpr(expr, context)
	}

	override protected caseSumOperation(SumOperation expr, Context context) {
		val itemType = typeProvider.getRType(expr.argument).toJavaReferenceType
		buildListOperationNoBody(expr, "sum" + itemType.simpleName, MapperC.wrapExtends(itemType), [MapperS.wrap(itemType)], context.scope)
	}

	override protected caseSymbolReference(RosettaSymbolReference expr, Context context) {
		val s = expr.symbol
		switch (s) {
			Attribute: {
				val attribute = rObjectFactory.buildRAttribute(s)
				// Data attributes can only be called if there is an implicit variable present.
				// The current container (Data) is stored in Params, but we need also look for superTypes
				// so we could also do: (s.eContainer as Data).allSuperTypes.map[it|params.getClass(it)].filterNull.head
				val implicitType = typeProvider.typeOfImplicitVariable(expr)
				val implicitFeatures = implicitType.allFeatures(expr)
				if (implicitFeatures.contains(s)) {
					var autoValue = true // if the attribute being referenced is WithMeta and we aren't accessing the meta fields then access the value by default
					if (expr.eContainer instanceof RosettaFeatureCall &&
						(expr.eContainer as RosettaFeatureCall).feature instanceof RosettaMetaType) {
						autoValue = false;
					}
					featureCall(implicitVariable(expr, context.scope), implicitType, s, context.scope, autoValue)
				} else
					new JavaVariable(context.scope.getIdentifierOrThrow(attribute), attribute.attributeToJavaType)
			}
			ShortcutDeclaration: {
				val isMulti = s.isSymbolMulti
				val shortcut = rObjectFactory.buildRShortcut(s)
				val itemType = typeProvider.getRTypeOfSymbol(s).toJavaReferenceType
				if (exprHelper.usesOutputParameter(s.expression)) {
					val aliasType = isMulti ? List.wrap(itemType) : itemType
					JavaExpression.from('''«context.scope.getIdentifierOrThrow(shortcut)»(«aliasCallArgs(s, context.scope)»).build()''', aliasType)
				} else {
					val aliasType = isMulti ? MapperC.wrapExtendsIfNotFinal(itemType) : MapperS.wrapExtendsIfNotFinal(itemType)
					JavaExpression.from('''«context.scope.getIdentifierOrThrow(shortcut)»(«aliasCallArgs(s, context.scope)»)''', aliasType)
				}
				
			}
			RosettaEnumeration: {
				val t = new REnumType(s).toJavaType
				JavaExpression.from('''«t»''', t)
			}
			ClosureParameter: {
				new JavaVariable(context.scope.getIdentifierOrThrow(s), expr.isMulti ? MapperC.wrap(expr) : MapperS.wrap(expr))
			}
			RosettaCallableWithArgs: {
				callableWithArgsCall(s, expr.args, context.scope)
			}
			default:
				throw new UnsupportedOperationException("Unsupported symbol type of " + s?.class?.name)
		}
	}

	override protected caseThenOperation(ThenOperation expr, Context context) {
		val thenArgCode = expr.argument.javaCode(expr.argument.isMulti ? MapperC.wrapExtends(expr.argument) : MapperS.wrapExtends(expr.argument), context.scope)
		val thenAsVarCode = thenArgCode.declareAsVariable(true, "thenResult", context.scope)
		if (expr.function.parameters.size == 0) {
			context.scope.createKeySynonym(expr.function.implicitVarInContext, thenArgCode)
		} else {
			context.scope.createKeySynonym(expr.function.parameters.head, thenArgCode)
		}
		thenAsVarCode
			.then(
				expr.function.body.javaCode(expr.isMulti ? MapperC.wrapExtends(expr) : MapperS.wrapExtends(expr), context.scope),
				[a, b| b],
				context.scope
			)
	}

	private def JavaStatementBuilder conversionOperation(RosettaUnaryOperation expr, Context context, StringConcatenationClient conversion, Class<? extends Exception> errorClass) {
		expr.argument.javaCode(MapperS.wrapExtends(expr.argument), context.scope)
			.collapseToSingleExpression(context.scope)
			.mapExpression[JavaExpression.from('''«it».checkedMap("«expr.operator»", «conversion», «errorClass».class)''', MapperS.wrap(expr))]
	}

	override protected caseToEnumOperation(ToEnumOperation expr, Context context) {
		val javaEnum = new REnumType(expr.enumeration).toJavaType
		conversionOperation(expr, context, '''«javaEnum»::fromDisplayName''', IllegalArgumentException)
	}

	override protected caseToIntOperation(ToIntOperation expr, Context context) {
		conversionOperation(expr, context, '''«Integer»::parseInt''', NumberFormatException)
	}

	override protected caseToNumberOperation(ToNumberOperation expr, Context context) {
		conversionOperation(expr, context, '''«BigDecimal»::new''', NumberFormatException)
	}

	override protected caseToStringOperation(ToStringOperation expr, Context context) {
		val rType = typeProvider.getRType(expr.argument)
		val StringConcatenationClient toStringMethod = if (rType.stripFromTypeAliases instanceof REnumType) {
			'''«rType.toJavaReferenceType»::toDisplayString'''
		} else {
			'''«Object»::toString'''
		}
		expr.argument.javaCode(MapperS.wrapExtends(expr.argument), context.scope)
			.collapseToSingleExpression(context.scope)
			.mapExpression[JavaExpression.from('''«it».map("«expr.operator»", «toStringMethod»)''', MapperS.wrap(expr))]
	}

	override protected caseToTimeOperation(ToTimeOperation expr, Context context) {
		val lambdaScope = context.scope.lambdaScope
		val lambdaParam = lambdaScope.createUniqueIdentifier("s")
		conversionOperation(expr,
			context, '''«lambdaParam» -> «LocalTime».parse(s, «DateTimeFormatter».ISO_LOCAL_TIME)''',
			DateTimeParseException)
	}
	
	override protected caseConstructorExpression(RosettaConstructorExpression expr, Context context) {
		val type = typeProvider.getRType(expr).stripFromTypeAliases
		val clazz = type.toJavaReferenceType
		if (type instanceof RDataType) {
			expr.values.map[pair|
				val attr = pair.key as Attribute
				val attrExpr = pair.value
				val isReference = attr.isReference
				val assignAsKey = attrExpr instanceof AsKeyOperation
				evaluateConstructorValue(attr, attrExpr, cardinalityProvider.isSymbolMulti(attr), assignAsKey, context.scope)
					.collapseToSingleExpression(context.scope)
					.mapExpression[JavaExpression.from('''.set«attr.name.toFirstUpper»«IF isReference && !assignAsKey»Value«ENDIF»(«it»)''', null)]
			].reduce[acc,attrCode|
				acc.then(attrCode, [allSetCode,setAttr|
					JavaExpression.from(
						'''
						«allSetCode»
						«setAttr»
						''',
						null
					)], context.scope
				)
			].mapExpression[
				JavaExpression.from('''
					«clazz».builder()
						«it»
						.build()
					''',
					clazz
				)
			]
		} else { // type instanceof RRecordType
			val featureMap = expr.values.toMap([key.name], [evaluateConstructorValue(key, value, false, false, context.scope)])
			recordUtil.recordConstructor(type as RRecordType, featureMap, context.scope)
		}
	}
	private def JavaStatementBuilder evaluateConstructorValue(RosettaFeature feature, RosettaExpression value, boolean isMulti, boolean assignAsKey, JavaScope scope) {
		if (assignAsKey) {
			val metaClass = (feature as Attribute).toMetaJavaType
			if (isMulti) {
				val lambdaScope = scope.lambdaScope
				val item = lambdaScope.createUniqueIdentifier("item")
				value.javaCode(MapperC.wrapExtends(value), scope)
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
									.collect(«Collectors».toList())''',
							List.wrap(metaClass)
						)
					]
			} else {
				val lambdaScope = scope.lambdaScope
				val r = lambdaScope.createUniqueIdentifier("r")
				val m = lambdaScope.createUniqueIdentifier("m")
				value.javaCode(typeProvider.getRType(value).toJavaReferenceType, scope)
					.declareAsVariable(true, feature.name, scope)
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
									.build()''',
							metaClass
						)
					]
			}
		} else {
			val clazz = typeProvider.getRTypeOfFeature(feature).toJavaReferenceType
			value.javaCode(isMulti ? List.wrap(clazz) : clazz, scope)
		}
	}
}
