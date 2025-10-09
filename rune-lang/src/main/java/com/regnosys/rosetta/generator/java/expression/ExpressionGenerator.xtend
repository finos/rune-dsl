package com.regnosys.rosetta.generator.java.expression

import com.regnosys.rosetta.RosettaEcoreUtil
import com.regnosys.rosetta.generator.GenerationException
import com.regnosys.rosetta.generator.java.scoping.JavaIdentifierRepresentationService
import com.regnosys.rosetta.generator.java.scoping.JavaStatementScope
import com.regnosys.rosetta.generator.java.statement.JavaLocalVariableDeclarationStatement
import com.regnosys.rosetta.generator.java.statement.builder.JavaConditionalExpression
import com.regnosys.rosetta.generator.java.statement.builder.JavaExpression
import com.regnosys.rosetta.generator.java.statement.builder.JavaIfThenElseBuilder
import com.regnosys.rosetta.generator.java.statement.builder.JavaLiteral
import com.regnosys.rosetta.generator.java.statement.builder.JavaStatementBuilder
import com.regnosys.rosetta.generator.java.statement.builder.JavaVariable
import com.regnosys.rosetta.generator.java.types.JavaPojoInterface
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil
import com.regnosys.rosetta.generator.java.types.RJavaFieldWithMeta
import com.regnosys.rosetta.generator.java.types.RJavaPojoInterface
import com.regnosys.rosetta.generator.java.types.RJavaReferenceWithMeta
import com.regnosys.rosetta.generator.java.types.RJavaWithMetaValue
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.generator.java.util.RecordJavaUtil
import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs
import com.regnosys.rosetta.rosetta.RosettaEnumValue
import com.regnosys.rosetta.rosetta.RosettaEnumValueReference
import com.regnosys.rosetta.rosetta.RosettaExternalFunction
import com.regnosys.rosetta.rosetta.RosettaFeature
import com.regnosys.rosetta.rosetta.RosettaMetaType
import com.regnosys.rosetta.rosetta.RosettaRecordFeature
import com.regnosys.rosetta.rosetta.RosettaRule
import com.regnosys.rosetta.rosetta.RosettaTypeWithConditions
import com.regnosys.rosetta.rosetta.TypeParameter
import com.regnosys.rosetta.rosetta.expression.ArithmeticOperation
import com.regnosys.rosetta.rosetta.expression.AsKeyOperation
import com.regnosys.rosetta.rosetta.expression.CardinalityModifier
import com.regnosys.rosetta.rosetta.expression.ChoiceOperation
import com.regnosys.rosetta.rosetta.expression.ClosureParameter
import com.regnosys.rosetta.rosetta.expression.ComparisonOperation
import com.regnosys.rosetta.rosetta.expression.DefaultOperation
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
import com.regnosys.rosetta.rosetta.expression.RosettaConstructorExpression
import com.regnosys.rosetta.rosetta.expression.RosettaContainsExpression
import com.regnosys.rosetta.rosetta.expression.RosettaCountOperation
import com.regnosys.rosetta.rosetta.expression.RosettaDeepFeatureCall
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
import com.regnosys.rosetta.rosetta.expression.SwitchCaseOrDefault
import com.regnosys.rosetta.rosetta.expression.SwitchOperation
import com.regnosys.rosetta.rosetta.expression.ThenOperation
import com.regnosys.rosetta.rosetta.expression.ToDateOperation
import com.regnosys.rosetta.rosetta.expression.ToDateTimeOperation
import com.regnosys.rosetta.rosetta.expression.ToEnumOperation
import com.regnosys.rosetta.rosetta.expression.ToIntOperation
import com.regnosys.rosetta.rosetta.expression.ToNumberOperation
import com.regnosys.rosetta.rosetta.expression.ToStringOperation
import com.regnosys.rosetta.rosetta.expression.ToTimeOperation
import com.regnosys.rosetta.rosetta.expression.ToZonedDateTimeOperation
import com.regnosys.rosetta.rosetta.expression.WithMetaOperation
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.ChoiceOption
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration
import com.regnosys.rosetta.types.CardinalityProvider
import com.regnosys.rosetta.types.RAttribute
import com.regnosys.rosetta.types.RChoiceOption
import com.regnosys.rosetta.types.RChoiceType
import com.regnosys.rosetta.types.RDataType
import com.regnosys.rosetta.types.REnumType
import com.regnosys.rosetta.types.RFunction
import com.regnosys.rosetta.types.RMetaAnnotatedType
import com.regnosys.rosetta.types.RObjectFactory
import com.regnosys.rosetta.types.RShortcut
import com.regnosys.rosetta.types.RosettaTypeProvider
import com.regnosys.rosetta.types.TypeSystem
import com.regnosys.rosetta.types.builtin.RBasicType
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService
import com.regnosys.rosetta.types.builtin.RRecordType
import com.regnosys.rosetta.utils.ExpressionHelper
import com.regnosys.rosetta.utils.ImplicitVariableUtil
import com.regnosys.rosetta.utils.RosettaExpressionSwitch
import com.rosetta.model.lib.expression.CardinalityOperator
import com.rosetta.model.lib.expression.ExpressionOperators
import com.rosetta.model.lib.expression.MapperMaths
import com.rosetta.model.lib.mapper.MapperC
import com.rosetta.model.lib.mapper.MapperS
import com.rosetta.model.lib.meta.Reference
import com.rosetta.model.lib.records.Date
import com.rosetta.model.lib.validation.ChoiceRuleValidationMethod
import com.rosetta.model.metafields.MetaFields
import com.rosetta.util.types.JavaClass
import com.rosetta.util.types.JavaGenericTypeDeclaration
import com.rosetta.util.types.JavaPrimitiveType
import com.rosetta.util.types.JavaReferenceType
import com.rosetta.util.types.JavaType
import jakarta.inject.Inject
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Arrays
import java.util.Collection
import java.util.List
import java.util.Optional
import java.util.stream.Collectors
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.xbase.lib.Functions.Function3

import static com.regnosys.rosetta.generator.java.types.JavaPojoPropertyOperationType.*

import static extension com.regnosys.rosetta.generator.java.enums.EnumHelper.convertValue
import static extension com.regnosys.rosetta.types.RMetaAnnotatedType.withNoMeta
import static extension com.regnosys.rosetta.utils.PojoPropertyUtil.*

class ExpressionGenerator extends RosettaExpressionSwitch<JavaStatementBuilder, ExpressionGenerator.Context> {
	
	static class Context {
		public JavaType expectedType;
		public JavaStatementScope scope;
	}

	@Inject protected RosettaTypeProvider typeProvider
	@Inject extension CardinalityProvider cardinalityProvider
	@Inject RosettaFunctionExtensions funcExt
	@Inject extension RosettaEcoreUtil
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
	@Inject extension RObjectFactory
	@Inject RBuiltinTypeService builtinTypeService

	/**
	 * convert a rosetta expression to code
	 * ParamMpa params  - a map keyed by classname or positional index that provides variable names for expression parameters
	 */
	def JavaStatementBuilder javaCode(RosettaExpression expr, JavaType expectedType, JavaStatementScope scope) {
		try {
			val rawResult = doSwitch(expr, new Context => [{
				it.expectedType = expectedType
				it.scope = scope
			}])
			return typeCoercionService.addCoercions(rawResult, expectedType, scope)			
		} catch (GenerationException e) {
			throw e
		} catch (Exception e) {
			throw new GenerationException(e.message, expr.eResource.URI, expr, e)
		}

	}

	private def StringConcatenationClient runtimeMethod(String methodName) {
		'''«importWildcard(method(ExpressionOperators, methodName))»'''
	}
	private def JavaStatementBuilder applyRuntimeMethod(JavaStatementBuilder expr, String methodName, JavaType resultType) {
		expr.mapExpression[JavaExpression.from('''«runtimeMethod(methodName)»(«it»)''', resultType)]
	}	

	private def JavaStatementBuilder callableWithArgsCall(RosettaCallableWithArgs callable, List<RosettaExpression> arguments, JavaStatementScope scope) {
		return switch (callable) {
			Function,
			RosettaRule: {
				val rCallable = if (callable instanceof Function)
						rObjectFactory.buildRFunction(callable)
					else
						rObjectFactory.buildRFunction(callable as RosettaRule)
				val outputType = rCallable.output.toMetaJavaType
				val args = newArrayList
				for (var i = 0; i < arguments.size; i++) {
					args.add(arguments.get(i).javaCode(rCallable.inputs.get(i).toMetaJavaType, scope))
				}
				JavaStatementBuilder.invokeMethod(
					args,
					[JavaExpression.from('''«scope.getIdentifierOrThrow(rCallable.toFunctionJavaClass.toDependencyInstance)».evaluate(«it»)''', outputType)],
					scope
				)
			}
			RosettaExternalFunction: {
				val returnRType = typeProvider.getRTypeOfSymbol(callable)
				if (arguments.empty) {
					JavaExpression.from('''new «callable.toFunctionJavaClass»().execute()''', returnRType.toJavaReferenceType)
				} else {
					// First evaluate all arguments
					var JavaStatementBuilder argCode
					val argRTypes = arguments.map[typeProvider.getRMetaAnnotatedType(it)]
					if (argRTypes.forall[isSubtypeOf(returnRType)]) {
						// TODO: this is a hack
						// Generic return type for number type e.g. Min(1,2) or Max(2,6)
						val argAndReturnType = argRTypes.joinMetaAnnotatedTypes.toJavaReferenceType
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
						argCode = arguments.head.javaCode(callable.parameters.head.typeCall.typeCallToRType.withNoMeta.toJavaReferenceType, scope)
						for (var i = 1; i < arguments.size; i++) {
							argCode = argCode.then(
								arguments.get(i).javaCode(callable.parameters.get(i).typeCall.typeCallToRType.withNoMeta.toJavaReferenceType, scope),
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

	private def JavaStatementBuilder implicitVariable(EObject context, JavaStatementScope scope) {
		val itemType = typeProvider.typeOfImplicitVariable(context).toJavaReferenceType
		val definingContainer = context.findContainerDefiningImplicitVariable.get
		val JavaType actualType = if (definingContainer instanceof RosettaTypeWithConditions || definingContainer instanceof RosettaRule) {
			// For conditions and rules
			itemType
		} else if (definingContainer instanceof SwitchCaseOrDefault) {
			// For choice and data switch cases
			if (definingContainer.guard.choiceOptionGuard !== null) {
				MAPPER_S.wrap(itemType)
			} else {
				itemType
			}
		} else {
			// For inline functions
			val f = definingContainer as RosettaFunctionalOperation
			if (f instanceof ThenOperation && f.argument.isOutputListOfLists) {
				MAPPER_LIST_OF_LISTS.wrap(itemType) as JavaType
			} else if (context.implicitVariableMulti) {
				MAPPER_C.wrap(itemType) as JavaType
			} else {
				MAPPER_S.wrap(itemType) as JavaType
			}
		}
		new JavaVariable(scope.getIdentifierOrThrow(context.implicitVarInContext), actualType)
	}
	
	def StringConcatenationClient aliasCallArgs(RShortcut alias, RFunction function, JavaStatementScope scope) {
		val output = function.output
		val inputs = function.inputs
		'''
			«IF exprHelper.usesOutputParameter(alias.expression)»«scope.getIdentifierOrThrow(output)».toBuilder()«IF !inputs.empty», «ENDIF»«ENDIF
			»«FOR input : inputs SEPARATOR ", "»«scope.getIdentifierOrThrow(input)»«ENDFOR»'''
	}

	private def StringConcatenationClient aliasCallArgs(ShortcutDeclaration alias, JavaStatementScope scope) {
		val func = EcoreUtil2.getContainerOfType(alias, Function)
		val output = rObjectFactory.buildRAttributeWithEnclosingType(null, funcExt.getOutput(func))
		val inputs = funcExt.getInputs(func).map[rObjectFactory.buildRAttributeWithEnclosingType(null, it)]
		'''
			«IF exprHelper.usesOutputParameter(alias.expression)»«scope.getIdentifierOrThrow(output)».toBuilder()«IF !inputs.empty», «ENDIF»«ENDIF
			»«FOR input : inputs SEPARATOR ", "»«scope.getIdentifierOrThrow(input)»«ENDFOR»'''
	}

	def JavaStatementBuilder enumCall(RosettaEnumValue feature, JavaType expectedType) {
		val itemType = expectedType.getItemValueType
		JavaExpression.from('''«itemType».«feature.convertValue»''', itemType)	
	}
	
	def JavaStatementBuilder metaCall(JavaStatementBuilder receiverCode, RMetaAnnotatedType receiverType, RosettaMetaType feature, JavaStatementScope scope) {
		val resultItemType = typeProvider.getRTypeOfFeature(feature, null).toJavaReferenceType
		val StringConcatenationClient right = feature.buildMapFunc(scope)
		val mapperReceiverCode = typeCoercionService.addCoercions(receiverCode, MAPPER.wrapExtends(receiverCode.expressionType.itemType), scope)
		featureCall(mapperReceiverCode, resultItemType, right, receiverCode, receiverType, cardinalityProvider.isFeatureMulti(feature), scope)
	}
	
	def JavaStatementBuilder recordCall(JavaStatementBuilder receiverCode, RMetaAnnotatedType receiverType, RosettaRecordFeature feature, JavaStatementScope scope) {
		val resultItemType = typeProvider.getRTypeOfFeature(feature, null).toJavaReferenceType
		val StringConcatenationClient right = '''.<«resultItemType»>map("«feature.name.toFirstUpper»", «recordUtil.recordFeatureToLambda(receiverType.RType as RRecordType, feature, scope)»)'''
		val mapperReceiverCode = typeCoercionService.addCoercions(receiverCode, MAPPER.wrapExtendsWithoutMeta(receiverCode.expressionType.itemType), scope)
		featureCall(mapperReceiverCode, resultItemType, right, receiverCode, receiverType, cardinalityProvider.isFeatureMulti(feature), scope)
	}
	
	def JavaStatementBuilder attributeCall(JavaStatementBuilder receiverCode, RMetaAnnotatedType receiverType, RAttribute attr, boolean isDeepFeature, JavaType expectedType, JavaStatementScope scope) {
		val receiverRType = receiverType.RType
		val t = if (receiverRType instanceof RChoiceType) {
			receiverRType.asRDataType
		} else {
			receiverRType as RDataType
		}
		val javaType = t.toJavaReferenceType
		val lambdaScope = scope.lambdaScope
		val lambdaParam = new JavaVariable(lambdaScope.createUniqueIdentifier(javaType.rosettaName.toFirstLower), javaType)
		var JavaType resultItemType
		val StringConcatenationClient mappingCode = if (isDeepFeature) {
			resultItemType = attr.toMetaItemJavaType
			'''"choose«attr.name.toFirstUpper»", «lambdaParam» -> «scope.getIdentifierOrThrow(t.toDeepPathUtilJavaClass.toDependencyInstance)».choose«attr.name.toFirstUpper»(«lambdaParam»)'''
		} else {
			val prop = javaType.findProperty(attr.name, expectedType)
			resultItemType = prop.type.itemType
			'''"get«prop.name.toFirstUpper»", «lambdaParam» -> «prop.applyGetter(lambdaParam)»'''
		}
		val r = resultItemType
		val StringConcatenationClient right = if (attr.isMulti) {
			'''.<«r»>mapC(«mappingCode»)'''
		} else {
			'''.<«r»>map(«mappingCode»)'''
		}

		val mapperReceiverCode = typeCoercionService.addCoercions(receiverCode, MAPPER.wrapExtendsWithoutMeta(receiverCode.expressionType.itemType), scope)
		featureCall(mapperReceiverCode, resultItemType, right, receiverCode, receiverType, attr.isMulti, scope)
	}
	
	private def JavaStatementBuilder featureCall(JavaStatementBuilder mapperReceiverCode, JavaType resultItemType, StringConcatenationClient right, JavaStatementBuilder receiverCode, RMetaAnnotatedType receiverType, boolean isMulti, JavaStatementScope scope) {
		val resultWrapper = if (mapperReceiverCode.expressionType.isMapperS && !isMulti) {
			MAPPER_S as JavaGenericTypeDeclaration<?>
		} else {
			MAPPER_C as JavaGenericTypeDeclaration<?>
		}
		val resultType = resultWrapper.wrap(resultItemType)
		return mapperReceiverCode
			.collapseToSingleExpression(scope)
			.mapExpression[JavaExpression.from('''«it»«right»''', resultType)]
	}

	private def binaryExpr(RosettaBinaryOperation expr, Context context) {
		val left = expr.left
		val right = expr.right

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
				val leftRtype = typeProvider.getRMetaAnnotatedType(expr.left).RType
				val rightRtype = typeProvider.getRMetaAnnotatedType(expr.right).RType
				val leftType = leftRtype.toJavaReferenceType
				val rightType = rightRtype.toJavaReferenceType
				val joinedWithoutMeta = leftRtype.join(rightRtype).toJavaReferenceType
				val resultType = typeProvider.getRMetaAnnotatedType(expr).toJavaReferenceType
				val method = switch expr.operator {
					case "+": "add"
					case "-": "subtract"
					case "*": "multiply"
					case "/": "divide"
				}
				if (leftType.extendsNumber && rightType.extendsNumber) {
					val leftCode = javaCode(left, MAPPER.wrapExtends(joinedWithoutMeta), context.scope)
					val rightCode = javaCode(right, MAPPER.wrapExtends(joinedWithoutMeta), context.scope)
					leftCode
						.then(rightCode, [l, r|JavaExpression.from('''«MapperMaths».<«resultType», «joinedWithoutMeta», «joinedWithoutMeta»>«method»(«l», «r»)''', MAPPER_S.wrap(resultType))], context.scope)
				} else {
					val leftCode = javaCode(left, MAPPER.wrapExtends(leftType), context.scope)
					val rightCode = javaCode(right, MAPPER.wrapExtends(rightType), context.scope)
					leftCode
						.then(rightCode, [l, r|JavaExpression.from('''«MapperMaths».<«resultType», «leftType», «rightType»>«method»(«l», «r»)''', MAPPER_S.wrap(resultType))], context.scope)
				}
			}
			case "contains",
			case "disjoint": {
				val leftRMetaType = typeProvider.getRMetaAnnotatedType(expr.left)
				val rightRMetaType = typeProvider.getRMetaAnnotatedType(expr.right)
				val joined = leftRMetaType.joinMetaAnnotatedTypes(rightRMetaType).toJavaReferenceType
				
				val leftCode = javaCode(left, MAPPER.wrapExtends(joined), context.scope)
				val rightCode = javaCode(right, MAPPER.wrapExtends(joined), context.scope)
				leftCode
					.then(rightCode, [l, r|JavaExpression.from('''«runtimeMethod(expr.operator)»(«l», «r»)''', COMPARISON_RESULT)], context.scope)
			}
			case "default": {
				val leftRMetaType = typeProvider.getRMetaAnnotatedType(expr.left)
				val rightRMetaType = typeProvider.getRMetaAnnotatedType(expr.right)
				val joined = leftRMetaType.joinMetaAnnotatedTypes(rightRMetaType).toJavaReferenceType
				
				val leftCode = javaCode(left, MAPPER.wrapExtends(joined), context.scope)
				if (left.isMulti) {
					val rightCode = javaCode(right, MAPPER.wrapExtends(joined), context.scope)
					
					leftCode
					.then(rightCode, [l, r| new JavaConditionalExpression(JavaExpression.from('''«l».getMulti().isEmpty()''', JavaPrimitiveType.BOOLEAN),r ,l , typeUtil)], context.scope)
				} else {
					val resultType = typeProvider.getRMetaAnnotatedType(expr).toJavaReferenceType
					val rightCode = javaCode(right, joined, context.scope)
					
					leftCode
					.then(rightCode, [l, r|JavaExpression.from('''«l».getOrDefault(«r»)''', resultType)], context.scope)
				}
			}
			case "join": {
				val leftCode = javaCode(left, MAPPER_C.wrapExtends(STRING), context.scope)
				val rightCode = expr.right === null ? JavaExpression.from('''«MapperS».of("")''', MAPPER_S.wrap(STRING)) : javaCode(right, MAPPER_S.wrap(STRING), context.scope)
				leftCode
					.then(rightCode, [l, r|JavaExpression.from('''«l».join(«r»)''', MAPPER_S.wrap(STRING))], context.scope)
			}
			case "=",
			case "<>",
			case "<",
			case "<=",
			case ">",
			case ">=": {
				val leftRtype = typeProvider.getRMetaAnnotatedType(expr.left).RType
				val rightRtype = typeProvider.getRMetaAnnotatedType(expr.right).RType
				val joinedWithoutMeta = leftRtype.join(rightRtype).toJavaReferenceType
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
				val leftCode = javaCode(left, MAPPER.wrapExtends(joinedWithoutMeta), context.scope)
				val rightCode = javaCode(right, MAPPER.wrapExtends(joinedWithoutMeta), context.scope)
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

	private def static StringConcatenationClient buildMapFunc(RosettaMetaType meta, JavaStatementScope scope) {
		if (meta.name == "reference") {
			val lambdaScope = scope.lambdaScope
			val lambdaParam = lambdaScope.createUniqueIdentifier("a")
			'''.map("get«meta.name.toFirstUpper»", «lambdaParam»->«lambdaParam».getExternalReference())'''
		} else {
			val lambdaScope1 = scope.lambdaScope
			val lambdaParam1 = lambdaScope1.createUniqueIdentifier("a")
			val lambdaScope2 = scope.lambdaScope
			val lambdaParam2 = lambdaScope2.createUniqueIdentifier("a")
			'''.map("getMeta", «lambdaParam1»->«lambdaParam1».getMeta()).map("get«meta.name.toFirstUpper»", «lambdaParam2»->«lambdaParam2».get«meta.name.toPojoPropertyName.toFirstUpper»())'''
		}
	}

	private def Pair<StringConcatenationClient, JavaType> inlineFunction(InlineFunction ref, JavaType expectedType, JavaStatementScope scope) {
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

	private def buildConstraint(RosettaExpression arg, Collection<RAttribute> usedAttributes,
		Necessity validationType, Context context) {
		val argItemType = typeProvider.getRMetaAnnotatedType(arg).RType.toJavaReferenceType
		arg.javaCode(MAPPER.wrapExtendsWithoutMeta(argItemType), context.scope)
			.collapseToSingleExpression(context.scope)
			.mapExpression[JavaExpression.from('''«runtimeMethod('choice')»(«it», «Arrays».asList(«usedAttributes.join(", ")['"' + name + '"']»), «ChoiceRuleValidationMethod».«validationType.name()»)''', COMPARISON_RESULT)]
	}

	private def JavaStatementBuilder buildListOperationNoBody(RosettaUnaryOperation op, String name, JavaType expectedArgumentType, (JavaType) => JavaType argumentTypeToReturnType, JavaStatementScope scope) {
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
	

	private def JavaStatementBuilder buildSingleItemListOperationOptionalBody(RosettaFunctionalOperation op, String name, JavaType expectedArgumentType, JavaType expectedBodyType, (JavaType, JavaType) => JavaType argumentAndBodyTypeToReturnType, boolean autoUnwrapMeta, JavaStatementScope scope) {
		if (op.function === null) {
			if (autoUnwrapMeta && expectedArgumentType.itemType instanceof RJavaFieldWithMeta) {
				buildUnwrappingListOperation(op, name, expectedArgumentType, expectedArgumentType.itemType as RJavaFieldWithMeta, expectedBodyType, argumentAndBodyTypeToReturnType, scope)
			} else {
				buildListOperationNoBody(op, name, expectedArgumentType, [argumentAndBodyTypeToReturnType.apply(it, null)], scope)
			}
		} else {
			buildSingleItemListOperation(op, name, expectedArgumentType, expectedBodyType, argumentAndBodyTypeToReturnType, scope)
		}
	}

	private def JavaStatementBuilder buildSingleItemListOperation(RosettaFunctionalOperation op, String name, JavaType expectedArgumentType, JavaType expectedBodyType, (JavaType, JavaType) => JavaType argumentAndBodyTypeToReturnType, JavaStatementScope scope) {
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
	
	
	private def JavaStatementBuilder buildUnwrappingListOperation(RosettaFunctionalOperation op, String name, JavaType expectedArgumentType, RJavaWithMetaValue expectedItemType,  JavaType expectedBodyType, (JavaType, JavaType) => JavaType argumentAndBodyTypeToReturnType, JavaStatementScope scope) {
		val argCode = op.argument.javaCode(expectedArgumentType, scope)
			.collapseToSingleExpression(scope)
		val lambdaPara = new JavaVariable(scope.createUniqueIdentifier("lambdaParam"), MAPPER_S.wrap(expectedItemType))
		val unwrapCoerceon = typeCoercionService.addCoercions(lambdaPara, MAPPER_S.wrap(expectedItemType.valueType), scope)
		
		argCode
			.mapExpression[JavaExpression.from(
				'''
				«it»
					.«name»(«lambdaPara» -> «unwrapCoerceon.toLambdaBody»)''',
				argumentAndBodyTypeToReturnType.apply(argCode.expressionType, expectedItemType.valueType)
			)]
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
		expr.argument.javaCode(MAPPER.wrapExtends(expr.argument), context.scope)
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
		if (expr.value) {
			JavaLiteral.TRUE
		} else {
			JavaLiteral.FALSE
		}
	}

	override protected caseChoiceOperation(ChoiceOperation expr, Context context) {
		buildConstraint(expr.argument, expr.attributes.map[buildRAttribute], expr.necessity, context)
	}

	override protected caseConditionalExpression(RosettaConditionalExpression expr, Context context) {
		val condition = expr.^if.javaCode(JavaPrimitiveType.BOOLEAN, context.scope)
		val thenBranch = trueOnEmptyExpression(expr.ifthen, context)
		val elseBranch = trueOnEmptyExpression(expr.elsethen, context)
		
		
		condition
			.collapseToSingleExpression(context.scope)
			.mapExpression[new JavaIfThenElseBuilder(it, thenBranch, elseBranch, typeUtil)]
	}
	
	private def JavaStatementBuilder trueOnEmptyExpression(RosettaExpression expr, Context context) {
		val rawResult = doSwitch(expr, new Context => [{
			it.expectedType = context.expectedType
			it.scope = context.scope
		}])
		val actual = rawResult.expressionType
		if (actual.itemType == JavaReferenceType.NULL_TYPE || actual.itemType.isVoid) {
			return JavaExpression.from('''true''', JavaPrimitiveType.BOOLEAN)
		}
		return expr.javaCode(context.expectedType, context.scope)
	}

	override protected caseContainsOperation(RosettaContainsExpression expr, Context context) {
		binaryExpr(expr, context)
	}
	
	override protected caseDefaultOperation(DefaultOperation expr, Context context) {
		binaryExpr(expr, context)
	}

	override protected caseCountOperation(RosettaCountOperation expr, Context context) {
		expr.argument.javaCode(MAPPER.wrapExtends(expr.argument), context.scope)
			.mapExpression[JavaExpression.from('''«it».resultCount()''', JavaPrimitiveType.INT)]
	}

	override protected caseDisjointOperation(RosettaDisjointExpression expr, Context context) {
		binaryExpr(expr, context)
	}

	override protected caseDistinctOperation(DistinctOperation expr, Context context) {
		val argItemType = typeProvider.getRMetaAnnotatedType(expr.argument).toJavaReferenceType
		val argCode = expr.argument.javaCode(MAPPER.wrapExtends(argItemType), context.scope)
		val argType = argCode.expressionType
		argCode
			.applyRuntimeMethod('distinct', argType.hasWildcardArgument ? MAPPER_C.wrapExtends(argItemType) : MAPPER_C.wrap(argItemType))
	}

	override protected caseDivideOperation(ArithmeticOperation expr, Context context) {
		binaryExpr(expr, context)
	}

	override protected caseEqualsOperation(EqualityOperation expr, Context context) {
		binaryExpr(expr, context)
	}
	
	def JavaStatementBuilder exists(JavaStatementBuilder arg, ExistsModifier modifier, JavaStatementScope scope) {
		val methodName = if (modifier === ExistsModifier.SINGLE)
				'singleExists'
			else if (modifier === ExistsModifier.MULTIPLE)
				'multipleExists'
			else
				'exists'
		typeCoercionService
			.addCoercions(arg, MAPPER.wrapExtends(arg.expressionType.itemType), scope)
			.applyRuntimeMethod(methodName, COMPARISON_RESULT)
	}

	override protected caseExistsOperation(RosettaExistsExpression expr, Context context) {
		exists(expr.argument.javaCode(MAPPER.wrapExtends(expr.argument), context.scope), expr.modifier, context.scope)
	}

	override protected caseFeatureCall(RosettaFeatureCall expr, Context context) {
		val feature = expr.feature
		if (feature instanceof RosettaEnumValue) {
			return enumCall(feature, context.expectedType)
		} else if (feature instanceof Attribute) {
			return attributeCall(expr.receiver.javaCode(MAPPER.wrapExtendsWithoutMeta(expr.receiver), context.scope), typeProvider.getRMetaAnnotatedType(expr.receiver), feature.buildRAttribute, false, context.expectedType, context.scope)
		} else if (feature instanceof RosettaMetaType) {
			return metaCall(expr.receiver.javaCode(MAPPER.wrapExtends(expr.receiver), context.scope), typeProvider.getRMetaAnnotatedType(expr.receiver), feature, context.scope)
		} else if (feature instanceof RosettaRecordFeature) {
			return recordCall(expr.receiver.javaCode(MAPPER.wrapExtends(expr.receiver), context.scope), typeProvider.getRMetaAnnotatedType(expr.receiver), feature, context.scope)
		} else {
			throw new UnsupportedOperationException("Unsupported feature type of " + feature?.class?.name)
		}
	}
	
	override protected caseDeepFeatureCall(RosettaDeepFeatureCall expr, Context context) {
		return attributeCall(expr.receiver.javaCode(MAPPER.wrapExtendsWithoutMeta(expr.receiver), context.scope), typeProvider.getRMetaAnnotatedType(expr.receiver), expr.feature.buildRAttribute, true, context.expectedType, context.scope)
	}

	override protected caseFilterOperation(FilterOperation expr, Context context) {
		val StringConcatenationClient inlineFunctionCode = expr.function.inlineFunction(JavaPrimitiveType.BOOLEAN.toReferenceType, context.scope).key
		if (!expr.isPreviousOperationMulti) {
			// Case MapperS
			val argCode = expr.argument.javaCode(MAPPER_S.wrapExtends(expr.argument), context.scope)
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
				val argCode = expr.argument.javaCode(MAPPER_LIST_OF_LISTS.wrapExtends(expr.argument), context.scope)
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
				val argCode = expr.argument.javaCode(MAPPER_C.wrapExtends(expr.argument), context.scope)
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
		buildListOperationNoBody(expr, "first", MAPPER_C.wrapExtends(expr.argument), [MAPPER_S.wrap(it.itemType)], context.scope)
	}

	override protected caseFlattenOperation(FlattenOperation expr, Context context) {
		buildListOperationNoBody(expr, "flattenList", MAPPER_LIST_OF_LISTS.wrapExtends(expr.argument), [it.hasWildcardArgument ? MAPPER_C.wrapExtends(expr.argument) : MAPPER_C.wrap(expr.argument)], context.scope)
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
			return JavaLiteral.INT(intValue)
		}
		val longValue = expr.value.longValue
		if (BigInteger.valueOf(longValue) == expr.value) {
			return JavaLiteral.LONG(longValue)
		}
		return JavaExpression.from('''new «BigInteger»("«expr.value»")''', BIG_INTEGER)
	}

	override protected caseJoinOperation(JoinOperation expr, Context context) {
		binaryExpr(expr, context)
	}

	override protected caseLastOperation(LastOperation expr, Context context) {
		buildListOperationNoBody(expr, "last", MAPPER_C.wrapExtends(expr.argument), [MAPPER_S.wrap(it.itemType)], context.scope)
	}

	override protected caseLessThanOperation(ComparisonOperation expr, Context context) {
		binaryExpr(expr, context)
	}

	override protected caseLessThanOrEqualOperation(ComparisonOperation expr, Context context) {
		binaryExpr(expr, context)
	}

	override protected caseListLiteral(ListLiteral expr, Context context) {
		if (expr.elements.empty) {
			return JavaLiteral.NULL
		}
		val itemType = typeProvider.getRMetaAnnotatedType(expr).toJavaReferenceType
		val elements = newArrayList
		for (var i = 0; i < expr.elements.size; i++) {
			val elem = expr.elements.get(i)
			elements.add(elem.javaCode(elem.isMulti ? MAPPER_C.wrapExtends(itemType) as JavaType : MAPPER_S.wrapExtends(itemType), context.scope))
		}
		JavaStatementBuilder.invokeMethod(
			elements,
			[JavaExpression.from('''«MapperC».<«itemType»>of(«it»)''', MAPPER_C.wrap(itemType))],
			context.scope
		)
	}

	override protected caseMapOperation(MapOperation expr, Context context) {
		val bodyItemType = typeProvider.getRMetaAnnotatedType(expr.function.body).toJavaReferenceType
		val isBodyMulti = expr.function.isBodyExpressionMulti

		if (!expr.isPreviousOperationMulti) {
			if (isBodyMulti) {
				// Case MapperS to MapperC
				val inlineFunctionCodeAndBodyType = expr.function.inlineFunction(MAPPER_C.wrapExtends(bodyItemType), context.scope)
				val StringConcatenationClient inlineFunctionCode = inlineFunctionCodeAndBodyType.key
				val inlineFunctionBodyType = inlineFunctionCodeAndBodyType.value
				expr.argument.javaCode(MAPPER_S.wrapExtends(expr.argument), context.scope)
					.collapseToSingleExpression(context.scope)
					.mapExpression[JavaExpression.from(
						'''
						«it»
							.mapSingleToList(«inlineFunctionCode»)''',
						inlineFunctionBodyType
					)]
			} else {
				// Case MapperS to MapperS
				buildSingleItemListOperation(expr, "mapSingleToItem", MAPPER_S.wrapExtends(expr.argument), MAPPER_S.wrapExtends(bodyItemType), [a,b|b], context.scope)
			}
		} else {
			if (expr.argument.isOutputListOfLists) {
				if (isBodyMulti) {
					// Case MapperListOfLists to MapperListOfLists
					val inlineFunctionCodeAndBodyType = expr.function.inlineFunction(MAPPER_C.wrapExtends(bodyItemType), context.scope)
					val StringConcatenationClient inlineFunctionCode = inlineFunctionCodeAndBodyType.key
					val inlineFunctionBodyType = inlineFunctionCodeAndBodyType.value
					expr.argument.javaCode(MAPPER_LIST_OF_LISTS.wrapExtends(expr.argument), context.scope)
						.collapseToSingleExpression(context.scope)
						.mapExpression[JavaExpression.from(
							'''
							«it»
								.mapListToList(«inlineFunctionCode»)''',
							inlineFunctionBodyType.hasWildcardArgument ? MAPPER_LIST_OF_LISTS.wrapExtends(bodyItemType) : MAPPER_LIST_OF_LISTS.wrap(bodyItemType)
						)]
				} else {
					// Case MapperListOfLists to MapperC
					val inlineFunctionCodeAndBodyType = expr.function.inlineFunction(MAPPER_S.wrapExtends(bodyItemType), context.scope)
					val StringConcatenationClient inlineFunctionCode = inlineFunctionCodeAndBodyType.key
					val inlineFunctionBodyType = inlineFunctionCodeAndBodyType.value
					expr.argument.javaCode(MAPPER_LIST_OF_LISTS.wrapExtends(expr.argument), context.scope)
						.collapseToSingleExpression(context.scope)
						.mapExpression[JavaExpression.from(
							'''
							«it»
								.mapListToItem(«inlineFunctionCode»)''',
							inlineFunctionBodyType.hasWildcardArgument ? MAPPER_C.wrapExtends(bodyItemType) : MAPPER_C.wrap(bodyItemType)
						)]
				}
			} else {
				if (isBodyMulti) {
					// MapperC to MapperListOfLists
					val inlineFunctionCodeAndBodyType = expr.function.inlineFunction(MAPPER_C.wrapExtends(bodyItemType), context.scope)
					val StringConcatenationClient inlineFunctionCode = inlineFunctionCodeAndBodyType.key
					val inlineFunctionBodyType = inlineFunctionCodeAndBodyType.value
					expr.argument.javaCode(MAPPER_C.wrapExtends(expr.argument), context.scope)
						.collapseToSingleExpression(context.scope)
						.mapExpression[JavaExpression.from(
							'''
							«it»
								.mapItemToList(«inlineFunctionCode»)''',
							inlineFunctionBodyType.hasWildcardArgument ? MAPPER_LIST_OF_LISTS.wrapExtends(bodyItemType) : MAPPER_LIST_OF_LISTS.wrap(bodyItemType)
						)]
				} else {
					// MapperC to MapperC
					buildSingleItemListOperation(expr, "mapItem", MAPPER_C.wrapExtends(expr.argument), MAPPER_S.wrapExtends(bodyItemType), [a,b|b.hasWildcardArgument ? MAPPER_C.wrapExtends(bodyItemType) : MAPPER_C.wrap(bodyItemType)], context.scope)
				}
			}
		}
	}

	override protected caseMaxOperation(MaxOperation expr, Context context) {
		val bodyType = if (expr.function !== null) MAPPER_S.wrapExtendsWithoutMeta(expr.function.body)
		buildSingleItemListOperationOptionalBody(expr, "max", MAPPER_C.wrapExtendsWithoutMeta(expr.argument), bodyType, [a,b|MAPPER_S.wrap(a.itemType)], true, context.scope)
	}

	override protected caseMinOperation(MinOperation expr, Context context) {
		val bodyType = if (expr.function !== null) MAPPER_S.wrapExtendsWithoutMeta(expr.function.body)
		buildSingleItemListOperationOptionalBody(expr, "min", MAPPER_C.wrapExtendsWithoutMeta(expr.argument), bodyType, [a,b|MAPPER_S.wrap(a.itemType)], true, context.scope)
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
		val type = typeProvider.getRMetaAnnotatedType(expr.argument).RType
		val t = if (type instanceof RChoiceType) {
			type.asRDataType
		} else {
			type as RDataType
		}
		buildConstraint(expr.argument, t.allAttributes, Necessity.REQUIRED, context)
	}

	override protected caseOnlyElementOperation(RosettaOnlyElement expr, Context context) {
		val itemType = typeProvider.getRMetaAnnotatedType(expr.argument).toJavaReferenceType
		expr.argument.javaCode(itemType, context.scope)
	}

	override protected caseOnlyExists(RosettaOnlyExistsExpression expr, Context context) {
		val first = expr.args.head
		var RDataType parentType
		val parent = if (first instanceof RosettaFeatureCall) {
			val t = typeProvider.getRMetaAnnotatedType(first.receiver).RType
			parentType = if (t instanceof RChoiceType) {
				t.asRDataType
			} else {
				t as RDataType
			}
			first.receiver.javaCode(MAPPER.wrapExtends(parentType.toJavaReferenceType), context.scope)
		} else {
			val t = typeProvider.typeOfImplicitVariable(expr).RType
			parentType = if (t instanceof RChoiceType) {
				t.asRDataType
			} else {
				t as RDataType
			}
			typeCoercionService.addCoercions(implicitVariable(expr, context.scope), MAPPER.wrapExtends(parentType.toJavaReferenceType), context.scope)
		}
		val requiredAttributes = expr.args.map[
			if (it instanceof RosettaFeatureCall) {
				return it.feature
			} else if (it instanceof RosettaSymbolReference) {
				return it.symbol
			}
			throw new UnsupportedOperationException("Unsupported parent in `only exists` expression of type " + it?.class?.name)
		]
		val allAttrs = parentType.allAttributes
		parent
			.collapseToSingleExpression(context.scope)
			.mapExpression[JavaExpression.from('''«runtimeMethod('onlyExists')»(«it», «Arrays».asList(«allAttrs.join(", ")['"' + name + '"']»), «Arrays».asList(«requiredAttributes.join(", ")['"' + name + '"']»))''', COMPARISON_RESULT)]
	}

	override protected caseOrOperation(LogicalOperation expr, Context context) {
		binaryExpr(expr, context)
	}

	override protected caseReduceOperation(ReduceOperation expr, Context context) {
		val outputType = typeProvider.getRMetaAnnotatedType(expr.function.body).toJavaReferenceType
		val inlineFunctionCodeAndBodyType = expr.function.inlineFunction(MAPPER_S.wrapExtends(outputType), context.scope)
		val StringConcatenationClient inlineFunctionCode = inlineFunctionCodeAndBodyType.key
		val inlineFunctionBodyType = inlineFunctionCodeAndBodyType.value
		expr.argument.javaCode(MAPPER_C.wrapExtends(expr.argument), context.scope)
			.collapseToSingleExpression(context.scope)
			.mapExpression[JavaExpression.from(
				'''
				«it»
					.<«outputType»>reduce(«inlineFunctionCode»)''',
				inlineFunctionBodyType
			)]
	}

	override protected caseReverseOperation(ReverseOperation expr, Context context) {
		buildListOperationNoBody(expr, "reverse", MAPPER_C.wrapExtends(expr.argument), [it], context.scope)
	}

	override protected caseSortOperation(SortOperation expr, Context context) {
		val bodyType = if (expr.function !== null) MAPPER_S.wrapExtendsWithoutMeta(expr.function.body)
		buildSingleItemListOperationOptionalBody(expr, "sort", MAPPER_C.wrapExtends(expr.argument), bodyType, [a,b|a], true, context.scope)
	}

	override protected caseStringLiteral(RosettaStringLiteral expr, Context context) {
		JavaLiteral.STRING(expr.value)
	}

	override protected caseSubtractOperation(ArithmeticOperation expr, Context context) {
		binaryExpr(expr, context)
	}

	override protected caseSumOperation(SumOperation expr, Context context) {
		val itemType = typeProvider.getRMetaAnnotatedType(expr.argument).RType.toJavaReferenceType
		buildListOperationNoBody(expr, "sum" + itemType.simpleName, MAPPER_C.wrapExtendsWithoutMeta(itemType), [MAPPER_S.wrap(itemType)], context.scope)
	}

	override protected caseSymbolReference(RosettaSymbolReference expr, Context context) {
		val s = expr.symbol
		switch (s) {
			Attribute: {
				val attribute = rObjectFactory.buildRAttribute(s)
				// Data attributes can only be called if there is an implicit variable present.
				val implicitType = typeProvider.typeOfImplicitVariable(expr)
				val implicitFeatures = implicitType.RType.allFeatures(expr)
				if (implicitFeatures.contains(s)) {
					attributeCall(implicitVariable(expr, context.scope), implicitType, s.buildRAttribute, false, context.expectedType, context.scope)
				} else
					new JavaVariable(context.scope.getIdentifierOrThrow(attribute), attribute.toMetaJavaType)
			}
			ShortcutDeclaration: {
				val isMulti = s.isSymbolMulti
				val shortcut = rObjectFactory.buildRShortcut(s)
				val itemType = typeProvider.getRTypeOfSymbol(s).toJavaReferenceType
				if (exprHelper.usesOutputParameter(s.expression)) {
					val aliasType = isMulti ? LIST.wrap(itemType) : itemType
					JavaExpression.from('''«context.scope.getIdentifierOrThrow(shortcut)»(«aliasCallArgs(s, context.scope)»).build()''', aliasType)
				} else {
					val aliasType = isMulti ? MAPPER_C.wrapExtendsIfNotFinal(itemType) as JavaType : MAPPER_S.wrapExtendsIfNotFinal(itemType)
					JavaExpression.from('''«context.scope.getIdentifierOrThrow(shortcut)»(«aliasCallArgs(s, context.scope)»)''', aliasType)
				}

			}
			RosettaEnumValue: {
				enumCall(s, context.expectedType)
			}
			ClosureParameter: {
				new JavaVariable(context.scope.getIdentifierOrThrow(s), expr.isMulti ? MAPPER_C.wrap(expr) as JavaType : MAPPER_S.wrap(expr))
			}
			RosettaCallableWithArgs: {
				callableWithArgsCall(s, expr.args, context.scope)
			}
			RosettaMetaType: {
				val implicitType = typeProvider.typeOfImplicitVariable(expr)
				metaCall(implicitVariable(expr, context.scope), implicitType, s, context.scope)
			}
			TypeParameter: {
				val type = typeProvider.getRTypeOfSymbol(s)
				new JavaVariable(context.scope.getIdentifierOrThrow(s), type.toJavaReferenceType)
			}
			default:
				throw new UnsupportedOperationException("Unsupported symbol type of " + s?.class?.name)
		}
	}

	override protected caseThenOperation(ThenOperation expr, Context context) {
		val thenArgCode = expr.argument.javaCode(expr.argument.isMulti ? MAPPER_C.wrapExtends(expr.argument) as JavaType : MAPPER_S.wrapExtends(expr.argument), context.scope)
		val thenAsVarCode = thenArgCode.declareAsVariable(true, "thenArg", context.scope)
		if (expr.function.parameters.size == 0) {
			context.scope.createKeySynonym(expr.function.implicitVarInContext, thenArgCode)
		} else {
			context.scope.createKeySynonym(expr.function.parameters.head, thenArgCode)
		}
		thenAsVarCode
			.then(
				expr.function.body.javaCode(expr.isMulti ? MAPPER_C.wrapExtends(expr) as JavaType : MAPPER_S.wrapExtends(expr), context.scope),
				[a, b| b],
				context.scope
			)
	}

	private def JavaStatementBuilder conversionOperation(RosettaUnaryOperation expr, Context context, StringConcatenationClient conversion, Class<? extends Exception> errorClass) {
		val argumentJavaType = typeProvider.getRMetaAnnotatedType(expr.argument).RType.toJavaReferenceType
		expr.argument.javaCode(MAPPER_S.wrapExtends(argumentJavaType), context.scope)
			.collapseToSingleExpression(context.scope)
			.mapExpression[JavaExpression.from('''«it».checkedMap("«expr.operator»", «conversion», «errorClass».class)''', MAPPER_S.wrap(expr))]
	}

	override protected caseToEnumOperation(ToEnumOperation expr, Context context) {
		val javaEnum = expr.enumeration.buildREnumType.toJavaType
		val argIsEnum = typeProvider.getRMetaAnnotatedType(expr.argument).RType.stripFromTypeAliases instanceof REnumType
		val StringConcatenationClient conversion = argIsEnum ? '''e -> «javaEnum».valueOf(e.name())''' : '''«javaEnum»::fromDisplayName'''
		conversionOperation(expr, context, conversion, IllegalArgumentException)
	}

	override protected caseToIntOperation(ToIntOperation expr, Context context) {
		conversionOperation(expr, context, '''«Integer»::parseInt''', NumberFormatException)
	}

	override protected caseToNumberOperation(ToNumberOperation expr, Context context) {
		conversionOperation(expr, context, '''«BigDecimal»::new''', NumberFormatException)
	}

	override protected caseToStringOperation(ToStringOperation expr, Context context) {
		val rType = typeProvider.getRMetaAnnotatedType(expr.argument).RType
		val StringConcatenationClient toStringMethod = if (rType.stripFromTypeAliases instanceof REnumType) {
			'''«rType.toJavaReferenceType»::toDisplayString'''
		} else {
			'''«Object»::toString'''
		}
		expr.argument.javaCode(MAPPER_S.wrapExtendsWithoutMeta(expr.argument), context.scope)
			.collapseToSingleExpression(context.scope)
			.mapExpression[JavaExpression.from('''«it».map("«expr.operator»", «toStringMethod»)''', MAPPER_S.wrap(expr))]
	}

	override protected caseToTimeOperation(ToTimeOperation expr, Context context) {
		val lambdaScope = context.scope.lambdaScope
		val lambdaParam = lambdaScope.createUniqueIdentifier("s")
		conversionOperation(expr,
			context, '''«lambdaParam» -> «LocalTime».parse(«lambdaParam», «DateTimeFormatter».ISO_LOCAL_TIME)''',
			DateTimeParseException)
	}
	
	override protected caseConstructorExpression(RosettaConstructorExpression expr, Context context) {
		val metaAnnotatedType = typeProvider.getRMetaAnnotatedType(expr)
		val clazz = metaAnnotatedType.toJavaReferenceType
		if (clazz instanceof JavaPojoInterface) {
			if (expr.values.empty) {
				JavaExpression.from('''
					«clazz».builder()
						.build()''',
					clazz
				)
			} else {
				expr.values.map[pair|
					val attr = pair.key as Attribute
					val attrExpr = pair.value
					
					val prop = clazz.findProperty(attr.name)
					val assignAsKey = attrExpr instanceof AsKeyOperation
					val requiresValueAssignment = requiresValueAssignment(assignAsKey, attr, attrExpr)
					
					val setterName = prop.getOperationName(requiresValueAssignment ? SET_VALUE : SET)
					
					evaluateConstructorValue(attr, attrExpr, cardinalityProvider.isFeatureMulti(attr), assignAsKey, context.scope)
						.collapseToSingleExpression(context.scope)
						.mapExpression[JavaExpression.from('''.«setterName»(«it»)''', null)]
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
							.build()''',
						clazz
					)
				]
			}
		} else { // type instanceof RRecordType
			val featureMap = expr.values.toMap([key.name], [evaluateConstructorValue(key, value, false, false, context.scope)])
			recordUtil.recordConstructor(metaAnnotatedType.RType as RRecordType, featureMap, context.scope)
		}
	}
	
	private def boolean requiresValueAssignment(boolean assignAsKey, Attribute attr, RosettaExpression attrExpr) {
		if (assignAsKey) {
			return false
		}
		val attrHasMeta = attr.buildRAttribute.RMetaAnnotatedType.hasAttributeMeta
		val attrExprHasMeta = typeProvider.getRMetaAnnotatedType(attrExpr).hasAttributeMeta
		return attrHasMeta && !attrExprHasMeta
	}
	
	
	private def JavaStatementBuilder evaluateConstructorValue(RosettaFeature feature, RosettaExpression value, boolean isMulti, boolean assignAsKey, JavaStatementScope scope) {
		if (assignAsKey) {
			val metaClass = (feature as Attribute).buildRAttribute.toMetaJavaType.itemType
			if (isMulti) {
				val lambdaScope = scope.lambdaScope
				val item = lambdaScope.createUniqueIdentifier("item")
				value.javaCode(MAPPER_C.wrapExtendsWithoutMeta(value), scope)
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
							LIST.wrap(metaClass)
						)
					]
			} else {
				val lambdaScope = scope.lambdaScope
				val r = lambdaScope.createUniqueIdentifier("r")
				val m = lambdaScope.createUniqueIdentifier("m")
				value.javaCode(typeProvider.getRMetaAnnotatedType(value).RType.withNoMeta.toJavaReferenceType, scope)
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
			var clazz = typeProvider.getRTypeOfFeature(feature, value).RType.withNoMeta.toJavaReferenceType
			if (feature instanceof Attribute) {
				if (!requiresValueAssignment(assignAsKey, feature, value)) {
					clazz = typeProvider.getRTypeOfFeature(feature, value).toJavaReferenceType
				}
			}
			
			value.javaCode(isMulti ? LIST.wrap(clazz) : clazz, scope)
		}
	}
	
	override protected caseToDateOperation(ToDateOperation expr, Context context) {
		conversionOperation(expr, context, '''«Date»::parse''', DateTimeParseException)
	}
	
	override protected caseToDateTimeOperation(ToDateTimeOperation expr, Context context) {
		conversionOperation(expr, context, '''«LocalDateTime»::parse''', DateTimeParseException)
	}
	
	override protected caseToZonedDateTimeOperation(ToZonedDateTimeOperation expr, Context context) {
		conversionOperation(expr, context, '''«ZonedDateTime»::parse''', DateTimeParseException)
	}

	override protected caseSwitchOperation(SwitchOperation expr, Context context) {
		val inputRType = typeProvider.getRMetaAnnotatedType(expr.argument).RType.stripFromTypeAliases
		if (inputRType instanceof RChoiceType) {
			val switchArgument = expr.argument.javaCode(MAPPER.wrap(inputRType.toJavaReferenceType), context.scope)
			
			createSwitchJavaExpression(expr, switchArgument, [acc,switchCase,switchArg|
				val choiceOption = new RChoiceOption(switchCase.guard.choiceOptionGuard, inputRType, typeProvider)
				val optionPath = findOptionPath(inputRType, choiceOption)
				
				val itemVar = context.scope.createIdentifier(switchCase.expression.implicitVarInContext, choiceOption.type.RType.name.toFirstLower)
				val optionExpr = optionPath.fold(switchArg as JavaStatementBuilder, [pathAcc,opt|
					pathAcc.attributeCall(opt.choiceType.withNoMeta, (opt.EObject as ChoiceOption).buildRAttribute, false, context.expectedType, context.scope)
				])
				optionExpr
					.collapseToSingleExpression(context.scope)
					.mapExpression[
						new JavaIfThenElseBuilder(
							JavaExpression.from('''«it».get() != null''', JavaPrimitiveType.BOOLEAN),
							new JavaLocalVariableDeclarationStatement(true, it.expressionType, itemVar, it)
								.append(switchCase.expression.javaCode(context.expectedType, context.scope)),
		 					acc,
		 					typeUtil
		 				)
					]
			], context)
		} else if (inputRType instanceof RDataType) {
			val switchArgument = expr.argument.javaCode(inputRType.toJavaReferenceType, context.scope)
			
			createSwitchJavaExpression(expr, switchArgument, [acc,switchCase,switchArg|
				val caseType = switchCase.guard.dataGuard.buildRDataType
				val caseJavaType = caseType.toJavaReferenceType
				
				val itemVar = context.scope.createIdentifier(switchCase.expression.implicitVarInContext, caseType.name.toFirstLower)
				val castExpression = JavaExpression.from('''(«caseJavaType») «switchArg»''', caseJavaType)
				new JavaIfThenElseBuilder(
					JavaExpression.from('''«switchArg» instanceof «caseType»''', JavaPrimitiveType.BOOLEAN),
					new JavaLocalVariableDeclarationStatement(true, caseJavaType, itemVar, castExpression)
								.append(switchCase.expression.javaCode(context.expectedType, context.scope)),
					acc,
					typeUtil
				)
			], context)
		} else if (inputRType instanceof REnumType) {
			val switchArgument = expr.argument.javaCode(inputRType.toJavaReferenceType, context.scope)
			
			createSwitchJavaExpression(expr, switchArgument, [acc,switchCase,switchArg|
				val enumCaseToCheck = enumCall(switchCase.guard.enumGuard, switchArgument.expressionType)
				new JavaIfThenElseBuilder(
						JavaExpression.from('''«switchArg» == «enumCaseToCheck»''', JavaPrimitiveType.BOOLEAN),
						switchCase.expression.javaCode(context.expectedType, context.scope),
	 					acc,
	 					typeUtil
	 				)
			], context)
		} else if (inputRType instanceof RBasicType) {
			val switchArgument = expr.argument.javaCode(MAPPER.wrap(inputRType.toJavaReferenceType), context.scope)
			val mapperSConditionType = MAPPER_S.wrap(switchArgument.expressionType.itemType)
			
			createSwitchJavaExpression(expr, switchArgument, [acc,switchCase,switchArg|
				val literalCaseToCheck = switchCase.guard.literalGuard.javaCode(mapperSConditionType, context.scope)
				new JavaIfThenElseBuilder(
						JavaExpression.from('''«runtimeMethod("areEqual")»(«switchArg», «literalCaseToCheck», «CardinalityOperator».All).get()''', JavaPrimitiveType.BOOLEAN),
						switchCase.expression.javaCode(context.expectedType, context.scope),
	 					acc,
	 					typeUtil
	 				)
			], context)
		}
 	}
 	private def List<RChoiceOption> findOptionPath(RChoiceType from, RChoiceOption goal) {
 		val result = newArrayList
 		
 		var currentChoice = from
 		var currentOption = currentChoice.ownOptions.findFirst[goal.type.isSubtypeOf(it.type, false)]
 		result.add(currentOption)
 		while (currentOption != goal) {
 			if (currentOption === null || !(currentOption.type.RType instanceof RChoiceType)) {
				throw new IllegalStateException("Did not find an option path from " + from + " to " + goal + ". " + currentOption)
			}
 			currentChoice = currentOption.type.RType as RChoiceType
 			currentOption = currentChoice.ownOptions.findFirst[goal.type.isSubtypeOf(it.type, false)]
 			result.add(currentOption)
 		}
 		
 		result
 	}

 	private def JavaStatementBuilder createSwitchJavaExpression(SwitchOperation expr, JavaStatementBuilder switchArgument, Function3<JavaStatementBuilder, SwitchCaseOrDefault, JavaExpression, JavaStatementBuilder> fold, Context context) {
 		val defaultExpr = expr.^default === null ? JavaLiteral.NULL : expr.^default.javaCode(context.expectedType, context.scope)
 		switchArgument
				.declareAsVariable(true, "switchArgument", context.scope)
				.mapExpression[switchArg|
					val javaSwitchExpr = expr.cases.filter[!isDefault].toList.reverseView
						.fold(defaultExpr, [acc,^case|fold.apply(acc, ^case, switchArg)])
					typeCoercionService.addCoercions(switchArg, switchArg.expressionType.itemType, context.scope)
						.mapExpression[JavaExpression.from('''«it» == null''', JavaPrimitiveType.BOOLEAN)]
						.mapExpression[
							new JavaIfThenElseBuilder(
								it,
								JavaLiteral.NULL,
								javaSwitchExpr,
								typeUtil
							)
						]
				]
 	}
 	
 	override protected caseWithMetaOperation(WithMetaOperation expr, Context context) {
 		val withMetaRMetaType = typeProvider.getRMetaAnnotatedType(expr)	
		val withMetaJavaType = deriveJavaTypeWithDefault(withMetaRMetaType, context.expectedType.itemType)
		val argumentrMetaType = typeProvider.getRMetaAnnotatedType(expr.argument)
		val argumentJavaType = deriveJavaTypeWithDefault(argumentrMetaType, withMetaJavaType instanceof RJavaWithMetaValue ? withMetaJavaType.valueType : withMetaJavaType)
		
		val metaEntries = expr.entries.map [ entry |
			{
				val entryType = typeProvider.getRTypeOfFeature(entry.key, expr).RType.toJavaReferenceType
				return entry.key.name ->
					entry.value.javaCode(entryType, context.scope).collapseToSingleExpression(context.scope)
			}
		].toList

		val argumentExpression = expr.argument.javaCode(argumentJavaType, context.scope)
				.mapExpression[JavaExpression.from('''«it»«IF it.needsBuilder» == null ? null : «it».toBuilder()«ENDIF»''', argumentJavaType.toBuilder)]
				.collapseToSingleExpression(context.scope)

		if (withMetaJavaType instanceof RJavaFieldWithMeta || withMetaJavaType instanceof RJavaPojoInterface) {
			val attributeMetaEntries = metaEntries.filter[key.isAttributeMeta].toList
			val typeMetaEntries = metaEntries.filter[key.isTypeMeta].toList
			val setMeta = !attributeMetaEntries.empty
			val setMetafields = !typeMetaEntries.empty
			
			val withMetaArgument = argumentExpression
					.declareAsVariable(true, "withMetaArgument", context.scope)				
			val withMetaAgumentVar = context.scope.getIdentifierOrThrow(argumentExpression)
			
			if (setMetafields && !setMeta) {
				if (argumentJavaType instanceof RJavaWithMetaValue) {
					return withMetaArgument
						.mapExpression[JavaExpression.from('''«it».getOrCreateValue().getOrCreateMeta()«FOR m : typeMetaEntries».set«m.key.toPojoSetter»(«m.value»)«ENDFOR»''', withMetaJavaType.itemType)]
						.completeAsExpressionStatement
						.append(new JavaVariable(withMetaAgumentVar, argumentJavaType))	
				} else {
					return withMetaArgument
						.mapExpression[JavaExpression.from('''«it».getOrCreateMeta()«FOR m : typeMetaEntries».set«m.key.toPojoSetter»(«m.value»)«ENDFOR»''',  withMetaJavaType.itemType)]
						.completeAsExpressionStatement
						.append(new JavaVariable(withMetaAgumentVar, argumentJavaType))
				}
			} else if (!setMetafields && setMeta) {
				if (argumentJavaType instanceof RJavaWithMetaValue) {
					return withMetaArgument
							.mapExpression[JavaExpression.from('''«it».getOrCreateMeta()«FOR m : attributeMetaEntries».set«m.key.toPojoSetter»(«m.value»)«ENDFOR»''', withMetaJavaType.itemType)]
							.completeAsExpressionStatement
							.append(new JavaVariable(withMetaAgumentVar, withMetaJavaType))
				} else {
					return withMetaArgument
							.mapExpression[JavaExpression.from('''«withMetaJavaType».builder().setValue(«it»).setMeta(«MetaFields».builder()«FOR m : attributeMetaEntries».set«m.key.toPojoSetter»(«m.value»)«ENDFOR»)''', withMetaJavaType.itemType)]
				}
				
			} else if (setMetafields && setMeta) {
				if (argumentJavaType instanceof RJavaWithMetaValue) {
					return withMetaArgument
							.mapExpression[JavaExpression.from('''«it».getOrCreateValue().getOrCreateMeta()«FOR m : typeMetaEntries».set«m.key.toPojoSetter»(«m.value»)«ENDFOR»''', JavaPrimitiveType.VOID)]
							.completeAsExpressionStatement
							.append(new JavaVariable(withMetaAgumentVar, argumentJavaType))
							.mapExpression[JavaExpression.from('''«it».getOrCreateMeta()«FOR m : attributeMetaEntries».set«m.key.toPojoSetter»(«m.value»)«ENDFOR»''', withMetaJavaType.itemType)]
							.completeAsExpressionStatement 
							.append(new JavaVariable(withMetaAgumentVar, withMetaJavaType))				
				} else {
					return withMetaArgument
							.mapExpression[JavaExpression.from('''«it».getOrCreateMeta()«FOR m : typeMetaEntries».set«m.key.toPojoSetter»(«m.value»)«ENDFOR»''', JavaPrimitiveType.VOID)]
							.completeAsExpressionStatement
							.append(new JavaVariable(withMetaAgumentVar, argumentJavaType))
							.mapExpression[JavaExpression.from('''«withMetaJavaType».builder().setValue(«it»).setMeta(«MetaFields».builder()«FOR m : attributeMetaEntries».set«m.key.toPojoSetter»(«m.value»)«ENDFOR»)''', withMetaJavaType.itemType)]
				}
			} else {
				return withMetaArgument
			}

		}

		if (withMetaJavaType instanceof RJavaReferenceWithMeta) {
			val metaEntriesWithoutAddress = metaEntries.filter[key != "address"].toList
			val metaAdressEntry = metaEntries.findFirst[key == "address"]
			
			return argumentExpression.mapExpression [
				JavaExpression.from('''«withMetaJavaType».builder().setValue(«it»)«FOR m : metaEntriesWithoutAddress».set«m.key.toPojoPropertyName.toFirstUpper»(«m.value»)«ENDFOR»«IF metaAdressEntry !== null».set«metaAdressEntry.key.toPojoPropertyName.toFirstUpper»(«Reference».builder().set«metaAdressEntry.key.toPojoPropertyName.toFirstUpper»(«metaAdressEntry.value»))«ENDIF».build()''', withMetaJavaType)
			]
		}

		throw new IllegalStateException("Unsupported type: " + withMetaJavaType)
	}
	
	private def JavaClass<?> deriveJavaTypeWithDefault(RMetaAnnotatedType withMetaRMetaType, JavaType defaultType) {
		if (withMetaRMetaType.RType == builtinTypeService.NOTHING && defaultType instanceof JavaClass) {
			return defaultType as JavaClass<?>
		}
		return withMetaRMetaType.toJavaReferenceType
	}

	private def String toPojoSetter(String metaEntryName) {
		metaEntryName.toPojoPropertyName.toFirstUpper
	}

	private def boolean needsBuilder(JavaExpression expr) {
		expr != JavaLiteral.NULL && expr.expressionType.hasBuilderType
	}
}
