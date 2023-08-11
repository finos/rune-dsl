package com.regnosys.rosetta.generator.java.expression

import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.JavaIdentifierRepresentationService
import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.generator.java.util.RecordFeatureMap
import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions
import com.regnosys.rosetta.rosetta.RosettaBlueprint
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
import com.regnosys.rosetta.rosetta.expression.RosettaReference
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
import com.regnosys.rosetta.types.RFunction
import com.regnosys.rosetta.types.RShortcut
import com.regnosys.rosetta.types.RType
import com.regnosys.rosetta.types.RosettaOperators
import com.regnosys.rosetta.types.RosettaTypeProvider
import com.regnosys.rosetta.types.TypeSystem
import com.regnosys.rosetta.utils.ExpressionHelper
import com.regnosys.rosetta.utils.ImplicitVariableUtil
import com.regnosys.rosetta.utils.RosettaExpressionSwitch
import com.rosetta.model.lib.expression.CardinalityOperator
import com.rosetta.model.lib.expression.ComparisonResult
import com.rosetta.model.lib.expression.ExpressionOperators
import com.rosetta.model.lib.expression.MapperMaths
import com.rosetta.model.lib.mapper.MapperC
import com.rosetta.model.lib.mapper.MapperS
import com.rosetta.model.lib.mapper.MapperUtils
import com.rosetta.model.lib.validation.ValidationResult.ChoiceRuleValidationMethod
import java.math.BigDecimal
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Arrays
import java.util.Collections
import java.util.List
import java.util.Optional
import org.apache.commons.text.StringEscapeUtils
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.EcoreUtil2

import static extension com.regnosys.rosetta.generator.java.enums.EnumHelper.convertValues
import com.regnosys.rosetta.types.RObjectFactory

class ExpressionGenerator extends RosettaExpressionSwitch<StringConcatenationClient, JavaScope> {

	@Inject protected RosettaTypeProvider typeProvider
	@Inject RosettaOperators operators
	@Inject extension CardinalityProvider cardinalityProvider
	@Inject RosettaFunctionExtensions funcExt
	@Inject extension RosettaExtensions
	@Inject extension ImportManagerExtension
	@Inject ExpressionHelper exprHelper
	@Inject extension ImplicitVariableUtil
	@Inject extension JavaIdentifierRepresentationService
	@Inject RecordFeatureMap recordFeatureMap
	@Inject extension JavaTypeTranslator
	@Inject extension TypeSystem
	@Inject RObjectFactory rObjectFactory;

	/**
	 * convert a rosetta expression to code
	 * ParamMpa params  - a map keyed by classname or positional index that provides variable names for expression parameters
	 */
	def StringConcatenationClient javaCode(RosettaExpression expr, JavaScope scope) {
		doSwitch(expr, scope)
	}

	private def runtimeMethod(String methodName) {
		return importWildcard(method(ExpressionOperators, methodName))
	}

	private def StringConcatenationClient ensureMapperJavaCode(RosettaExpression expr, JavaScope scope, boolean multi) {
		if (expr.isEmpty) {
			'''«IF multi»«MapperC»«ELSE»«MapperS»«ENDIF».ofNull()'''
		} else if (expr instanceof RosettaConditionalExpression && !multi) {
			if (expr.evaluatesToComparisonResult) {
				'''((«ComparisonResult»)«expr.javaCode(scope)»).asMapper()'''
			} else {
				'''((MapperS<«typeProvider.getRType(expr).toJavaReferenceType»>)«expr.javaCode(scope)»)'''
			}
		} else if (expr.evaluatesToComparisonResult && !multi) {
			'''«expr.javaCode(scope)».asMapper()'''
		} else {
			expr.javaCode(scope)
		}
	}

	private def boolean isEmpty(RosettaExpression e) { // TODO: temporary workaround while transitioning from old to new type system
		if (e instanceof ListLiteral) {
			e.elements.size === 0
		} else {
			false
		}
	}

	private def StringConcatenationClient genConditionalMapper(RosettaConditionalExpression expr, JavaScope scope) '''
	«IF expr.ifthen.evaluatesToComparisonResult»«MapperUtils».toComparisonResult(«ENDIF»«MapperUtils».run«IF expr.isMulti»Multi«ELSE»Single«ENDIF»«IF funcExt.needsBuilder(expr)»Polymorphic«ENDIF»(() -> {
		«expr.genConditional(scope)»
	})«IF expr.ifthen.evaluatesToComparisonResult»)«ENDIF»'''

	private def StringConcatenationClient genConditional(RosettaConditionalExpression expr, JavaScope scope) {
		return '''
			if («expr.^if.javaCode(scope)».getOrDefault(false)) {
				return «expr.ifthen.javaCode(scope)»;
			}
			«IF expr.childElseThen !== null»
				«expr.childElseThen.genElseIf(scope)»
			«ELSE»
				else {
					return «expr.elsethen.ensureMapperJavaCode(scope, cardinalityProvider.isMulti(expr.ifthen))»;
				}
			«ENDIF»
		'''
	}

	private def StringConcatenationClient genElseIf(RosettaConditionalExpression next, JavaScope scope) {
		'''
			«IF next !== null»
				else if («next.^if.javaCode(scope)».getOrDefault(false)) {
					return «next.ifthen.javaCode(scope)»;
				}
				«IF next.childElseThen !== null»
					«next.childElseThen.genElseIf(scope)»
				«ELSE»
					else {
						return «next.elsethen.ensureMapperJavaCode(scope, cardinalityProvider.isMulti(next.ifthen))»;
					}
				«ENDIF»
			«ENDIF»
		'''
	}

	private def RosettaConditionalExpression childElseThen(RosettaConditionalExpression expr) {
		if (expr.elsethen instanceof RosettaConditionalExpression)
			expr.elsethen as RosettaConditionalExpression
	}

	private def StringConcatenationClient callableWithArgs(RosettaCallableWithArgs callable, JavaScope scope,
		StringConcatenationClient argsCode, boolean needsMapper) {
		return switch (callable) {
			Function,
			RosettaBlueprint: {
				val rCallable = if (callable instanceof Function)
						rObjectFactory.buildRFunction(callable)
					else
						rObjectFactory.buildRFunction(callable as RosettaBlueprint)
				val multi = rCallable.output.multi
				'''«IF needsMapper»«IF multi»«MapperC».<«rCallable.output.RType.toJavaReferenceType»>«ELSE»«MapperS».«ENDIF»of(«ENDIF»«scope.getIdentifierOrThrow(rCallable.toFunctionInstance)».evaluate(«argsCode»)«IF needsMapper»)«ENDIF»'''
			}
			RosettaExternalFunction: {
				'''«IF needsMapper»«MapperS».of(«ENDIF»new «callable.toFunctionJavaClass»().execute(«argsCode»)«IF needsMapper»)«ENDIF»'''
			}
			default:
				throw new UnsupportedOperationException("Unsupported callable with args of type " +
					callable?.eClass?.name)
		}

	}

	private def StringConcatenationClient callableWithArgsCall(RosettaCallableWithArgs func,
		List<RosettaExpression> arguments, JavaScope scope) {
		callableWithArgs(func, scope, '''«args(func, arguments, scope)»''', true)
	}

	private def StringConcatenationClient args(RosettaCallableWithArgs func, List<RosettaExpression> arguments,
		JavaScope scope) {
		if (func instanceof Function) {
			'''«FOR i : 0 ..< arguments.size SEPARATOR ', '»«arg(arguments.get(i), typeProvider.getRTypeOfSymbol(func.inputs.get(i)), func.inputs.get(i).isMulti, scope)»«ENDFOR»'''
		} else {
			'''«FOR argExpr : arguments SEPARATOR ', '»«arg(argExpr, null, false, scope)»«ENDFOR»'''
		}
	}

	private def StringConcatenationClient arg(RosettaExpression expr, RType expectedType, boolean needsToBeMulti,
		JavaScope scope) {
		if (expr.evalulatesToMapper) {
			'''«expr.javaCode(scope)»«IF needsToBeMulti».getMulti()«ELSE».get()«ENDIF»'''
		} else {
			val isMulti = expr.isMulti
			if (!isMulti && needsToBeMulti) {
				'''«Optional».«IF expectedType !== null»<«expectedType.toJavaReferenceType»>«ENDIF»ofNullable(«expr.javaCode(scope)»).map(«Arrays»::asList).orElse(«Collections».emptyList())'''
			} else if (isMulti && !needsToBeMulti) {
				'''«expr.javaCode(scope)».get(0)'''
			} else {
				'''«expr.javaCode(scope)»'''
			}
		}
	}

	private def RosettaBinaryOperation findBinaryOperation(RosettaExpression expression) {
		switch (expression) {
			RosettaBinaryOperation: expression
			default: null
		}
	}

	private def StringConcatenationClient implicitVariable(EObject context, JavaScope scope) {
		val definingContainer = context.findContainerDefiningImplicitVariable.get
		if (definingContainer instanceof Data || definingContainer instanceof RosettaBlueprint) {
			// For conditions and rules
			return '''«MapperS».of(«scope.getIdentifierOrThrow(context.implicitVarInContext)»)'''
		} else {
			// For inline functions
			return '''«scope.getIdentifierOrThrow(context.implicitVarInContext)»'''
		}
	}

	def StringConcatenationClient aliasCallArgs(RShortcut alias, RFunction function, JavaScope scope) {
		val output = function.output
		val inputs = function.inputs
		'''
			«IF exprHelper.usesOutputParameter(alias.expression)»«scope.getIdentifierOrThrow(output)».toBuilder()«IF !inputs.empty», «ENDIF»«ENDIF»
			«FOR input : inputs SEPARATOR ", "»«scope.getIdentifierOrThrow(input)»«ENDFOR»
		'''
	}

	def aliasCallArgs(ShortcutDeclaration alias) {
		val func = EcoreUtil2.getContainerOfType(alias, Function)
		val attrs = <String>newArrayList
		attrs.addAll(funcExt.getInputs(func).map[name].toList)
		if (exprHelper.usesOutputParameter(alias.expression)) {
			attrs.add(0, funcExt.getOutput(func)?.name + '.toBuilder()')
		}
		attrs.join(', ')
	}

	def StringConcatenationClient featureCall(StringConcatenationClient receiverCode, RosettaFeature feature,
		JavaScope scope, boolean autoValue) {
		val StringConcatenationClient right = switch (feature) {
			Attribute:
				feature.buildMapFunc(autoValue, scope)
			RosettaMetaType: '''«feature.buildMapFunc(scope)»'''
			RosettaEnumValue:
				return '''«MapperS».of(«new REnumType(feature.enumeration).toJavaType».«feature.convertValues»)'''
			RosettaRecordFeature: '''.<«feature.typeCall.typeCallToRType.toJavaReferenceType»>map("«feature.name.toFirstUpper»", «recordFeatureMap.recordFeatureToLambda(feature)»)'''
			default:
				throw new UnsupportedOperationException("Unsupported feature type of " + feature?.class?.name)
		}

		return '''«receiverCode»«right»'''
	}

	private def StringConcatenationClient binaryExpr(RosettaBinaryOperation expr, JavaScope scope) {
		val left = expr.left
		val right = expr.right
		val leftRtype = typeProvider.getRType(expr.left)
		val rightRtype = typeProvider.getRType(expr.right)
		val resultType = operators.resultType(expr.operator, leftRtype, rightRtype).toJavaReferenceType
		val leftType = leftRtype.toJavaReferenceType
		val rightType = rightRtype.toJavaReferenceType

		switch expr.operator {
			case ("and"): {
				'''«left.toComparisonResult(scope)».and(«right.toComparisonResult(scope)»)'''
			}
			case ("or"): {
				'''«left.toComparisonResult(scope)».or(«right.toComparisonResult(scope)»)'''
			}
			case ("+"): {
				'''«MapperMaths».<«resultType», «leftType», «rightType»>add(«expr.left.javaCode(scope)», «expr.right.javaCode(scope)»)'''
			}
			case ("-"): {
				'''«MapperMaths».<«resultType», «leftType», «rightType»>subtract(«expr.left.javaCode(scope)», «expr.right.javaCode(scope)»)'''
			}
			case ("*"): {
				'''«MapperMaths».<«resultType», «leftType», «rightType»>multiply(«expr.left.javaCode(scope)», «expr.right.javaCode(scope)»)'''
			}
			case ("/"): {
				'''«MapperMaths».<«resultType», «leftType», «rightType»>divide(«expr.left.javaCode(scope)», «expr.right.javaCode(scope)»)'''
			}
			case ("contains"): {
				'''«runtimeMethod("contains")»(«expr.left.javaCode(scope)», «expr.right.javaCode(scope)»)'''
			}
			case ("disjoint"): {
				'''«runtimeMethod("disjoint")»(«expr.left.javaCode(scope)», «expr.right.javaCode(scope)»)'''
			}
			case ("join"): {
				'''
				«expr.left.javaCode(scope)»
					.join(«IF expr.right !== null»«expr.right.javaCode(scope)»«ELSE»«MapperS».of("")«ENDIF»)'''
			}
			default: {
				toComparisonOp('''«expr.left.ensureMapperJavaCode(scope, false)»''',
					expr.operator, '''«expr.right.ensureMapperJavaCode(scope, false)»''',
					(expr as ModifiableBinaryOperation).cardMod)
			}
		}
	}

	def StringConcatenationClient toComparisonResult(RosettaExpression expr, JavaScope scope) {
		val wrap = !expr.evaluatesToComparisonResult
		'''«IF wrap»«ComparisonResult».of(«ENDIF»«expr.javaCode(scope)»«IF wrap»)«ENDIF»'''
	}

	private def boolean isLogicalOperation(RosettaExpression expr) {
		if(expr instanceof RosettaBinaryOperation) return expr.operator == "and" || expr.operator == "or"
		return false
	}

	private def boolean isArithmeticOperation(RosettaExpression expr) {
		if (expr instanceof RosettaBinaryOperation)
			return RosettaOperators.ARITHMETIC_OPS.contains(expr.operator)
		return false
	}

	/**
	 * Collects all expressions down the tree, and checks that they're all either FeatureCalls or CallableCalls (or anything that resolves to a Mapper)
	 */
	private def boolean evalulatesToMapper(RosettaExpression expr) { // TODO: this function is faulty, I think
		val exprs = newHashSet
		collectExpressions(expr, [exprs.add(it)])

		return expr.evaluatesToComparisonResult || !exprs.empty && exprs.stream.allMatch [
			it instanceof RosettaFeatureCall || it instanceof RosettaReference || it instanceof RosettaLiteral ||
				it instanceof ListLiteral &&
					!(it.isEmpty && !(it.eContainer instanceof RosettaConditionalExpression)) ||
				it instanceof RosettaCountOperation || it instanceof RosettaFunctionalOperation ||
				it instanceof RosettaOnlyElement || it instanceof RosettaConditionalExpression ||
				isArithmeticOperation(it)
		]
	}

	private def boolean evaluatesToComparisonResult(RosettaExpression expr) {
		return expr instanceof LogicalOperation || expr instanceof ComparisonOperation ||
			expr instanceof EqualityOperation || expr instanceof RosettaContainsExpression ||
			expr instanceof RosettaDisjointExpression || expr instanceof RosettaOnlyExistsExpression ||
			expr instanceof RosettaExistsExpression || expr instanceof RosettaAbsentExpression ||
			expr instanceof RosettaConditionalExpression &&
				(expr as RosettaConditionalExpression).ifthen.evaluatesToComparisonResult ||
			expr instanceof OneOfOperation || expr instanceof ChoiceOperation
	}

	private def StringConcatenationClient toComparisonOp(StringConcatenationClient left, String operator,
		StringConcatenationClient right, CardinalityModifier cardMod) {
		switch operator {
			case ("="): {
				'''«runtimeMethod('areEqual')»(«left», «right», «toCardinalityOperator(cardMod, CardinalityModifier.ALL)»)'''
			}
			case ("<>"): '''«runtimeMethod('notEqual')»(«left», «right», «toCardinalityOperator(cardMod, CardinalityModifier.ANY)»)'''
			case ("<"): '''«runtimeMethod('lessThan')»(«left», «right», «toCardinalityOperator(cardMod, CardinalityModifier.ALL)»)'''
			case ("<="): '''«runtimeMethod('lessThanEquals')»(«left», «right», «toCardinalityOperator(cardMod, CardinalityModifier.ALL)»)'''
			case (">"): '''«runtimeMethod('greaterThan')»(«left», «right», «toCardinalityOperator(cardMod, CardinalityModifier.ALL)»)'''
			case (">="): '''«runtimeMethod('greaterThanEquals')»(«left», «right», «toCardinalityOperator(cardMod, CardinalityModifier.ALL)»)'''
			default:
				throw new UnsupportedOperationException("Unsupported binary operation of " + operator)
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

	private def StringConcatenationClient inlineFunction(InlineFunction ref, JavaScope scope, boolean doCast,
		boolean needsMapper) {
		val lambdaScope = scope.lambdaScope
		val paramIds = if (ref.parameters.size == 0) {
				#[lambdaScope.createIdentifier(ref.implicitVarInContext, defaultImplicitVariable.name)]
			} else {
				ref.parameters.map[lambdaScope.createIdentifier(it)]
			}

		val isBodyMulti = ref.isBodyExpressionMulti
		val StringConcatenationClient bodyExpr = '''«ref.body.javaCode(lambdaScope)»«IF needsMapper»«IF ref.body.evaluatesToComparisonResult».asMapper()«ENDIF»«ELSE»«IF ref.body.evalulatesToMapper»«IF isBodyMulti».getMulti()«ELSE».get()«ENDIF»«ENDIF»«ENDIF»'''
		val StringConcatenationClient cast = if (doCast) {
				val outputType = typeProvider.getRType(ref.body).toJavaReferenceType
				'''(«IF needsMapper»«IF isBodyMulti»«MapperC»<«outputType»>«ELSE»«MapperS»<«outputType»>«ENDIF»«ELSE»«outputType»«ENDIF»)'''
			} else {
				''''''
			}
		if (paramIds.size == 1) {
			'''«paramIds.head» -> «cast»«bodyExpr»'''
		} else {
			'''(«FOR id : paramIds SEPARATOR ', '»«id»«ENDFOR») -> «cast»«bodyExpr»'''
		}
	}

	private def StringConcatenationClient buildConstraint(RosettaExpression arg, Iterable<Attribute> usedAttributes,
		Necessity validationType, JavaScope scope) {
		'''«runtimeMethod('choice')»(«arg.javaCode(scope)», «Arrays».asList(«usedAttributes.join(", ")['"' + name + '"']»), «ChoiceRuleValidationMethod».«validationType.name()»)'''
	}

	private def StringConcatenationClient buildListOperationNoBody(RosettaUnaryOperation op, String name,
		JavaScope scope) {
		'''
		«op.argument.ensureMapperJavaCode(scope, true)»
			.«name»()'''
	}

	private def StringConcatenationClient buildSingleItemListOperationOptionalBody(RosettaFunctionalOperation op,
		String name, JavaScope scope) {
		if (op.function === null) {
			buildListOperationNoBody(op, name, scope)
		} else {
			buildSingleItemListOperation(op, name, scope)
		}
	}

	private def StringConcatenationClient buildSingleItemListOperation(RosettaFunctionalOperation op, String name,
		JavaScope scope) {
		'''
		«op.argument.ensureMapperJavaCode(scope, false)»
			.«name»(«op.function.inlineFunction(scope, true, true)»)'''
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

	override protected caseAbsentOperation(RosettaAbsentExpression expr, JavaScope context) {
		val arg = expr.argument
		val binary = arg.findBinaryOperation
		if (binary !== null) {
			if (binary.isLogicalOperation)
				'''«runtimeMethod('notExists')»(«binary.binaryExpr(context)»)'''
			else
				// if the arg is binary then the operator needs to be pushed down
				binary.binaryExpr(context)
		} else {
			'''«runtimeMethod('notExists')»(«arg.javaCode(context)»)'''
		}
	}

	override protected caseAddOperation(ArithmeticOperation expr, JavaScope context) {
		binaryExpr(expr, context)
	}

	override protected caseAndOperation(LogicalOperation expr, JavaScope context) {
		binaryExpr(expr, context)
	}

	override protected caseAsKeyOperation(AsKeyOperation expr, JavaScope context) {
		// this operation is currently handled by the `FunctionGenerator`
		expr.argument.javaCode(context)
	}

	override protected caseBooleanLiteral(RosettaBooleanLiteral expr, JavaScope context) {
		'''«MapperS».of(«Boolean».valueOf(«expr.value»))'''
	}

	override protected caseChoiceOperation(ChoiceOperation expr, JavaScope context) {
		buildConstraint(expr.argument, expr.attributes, expr.necessity, context)
	}

	override protected caseConditionalExpression(RosettaConditionalExpression expr, JavaScope context) {
		expr.genConditionalMapper(context)
	}

	override protected caseContainsOperation(RosettaContainsExpression expr, JavaScope context) {
		binaryExpr(expr, context)
	}

	override protected caseCountOperation(RosettaCountOperation expr, JavaScope context) {
		'''«MapperS».of(«expr.argument.javaCode(context)».resultCount())'''
	}

	override protected caseDisjointOperation(RosettaDisjointExpression expr, JavaScope context) {
		binaryExpr(expr, context)
	}

	override protected caseDistinctOperation(DistinctOperation expr, JavaScope context) {
		'''«runtimeMethod('distinct')»(«expr.argument.javaCode(context)»)'''
	}

	override protected caseDivideOperation(ArithmeticOperation expr, JavaScope context) {
		binaryExpr(expr, context)
	}

	override protected caseEqualsOperation(EqualityOperation expr, JavaScope context) {
		binaryExpr(expr, context)
	}

	override protected caseExistsOperation(RosettaExistsExpression expr, JavaScope context) {
		val arg = expr.argument.javaCode(context)
		if (expr.modifier === ExistsModifier.SINGLE)
			'''«runtimeMethod('singleExists')»(«arg»)'''
		else if (expr.modifier === ExistsModifier.MULTIPLE)
			'''«runtimeMethod('multipleExists')»(«arg»)'''
		else
			'''«runtimeMethod('exists')»(«arg»)'''
	}

	override protected caseFeatureCall(RosettaFeatureCall expr, JavaScope context) {
		var autoValue = true // if the attribute being referenced is WithMeta and we aren't accessing the meta fields then access the value by default
		if (expr.eContainer instanceof RosettaFeatureCall &&
			(expr.eContainer as RosettaFeatureCall).feature instanceof RosettaMetaType) {
			autoValue = false;
		}
		return featureCall(javaCode(expr.receiver, context), expr.feature, context, autoValue)
	}

	override protected caseFilterOperation(FilterOperation expr, JavaScope context) {
		if (!expr.isPreviousOperationMulti) {
			'''
			«expr.argument.ensureMapperJavaCode(context, false)»
				.filterSingleNullSafe(«expr.function.inlineFunction(context, true, false)»)'''
		} else {
			if (expr.argument.isOutputListOfLists) {
				'''
				«expr.argument.ensureMapperJavaCode(context, true)»
					.filterListNullSafe(«expr.function.inlineFunction(context, true, false)»)'''
			} else {
				'''
				«expr.argument.ensureMapperJavaCode(context, true)»
					.filterItemNullSafe(«expr.function.inlineFunction(context, true, false)»)'''
			}
		}
	}

	override protected caseFirstOperation(FirstOperation expr, JavaScope context) {
		buildListOperationNoBody(expr, "first", context)
	}

	override protected caseFlattenOperation(FlattenOperation expr, JavaScope context) {
		buildListOperationNoBody(expr, "flattenList", context)
	}

	override protected caseGreaterThanOperation(ComparisonOperation expr, JavaScope context) {
		binaryExpr(expr, context)
	}

	override protected caseGreaterThanOrEqualOperation(ComparisonOperation expr, JavaScope context) {
		binaryExpr(expr, context)
	}

	override protected caseImplicitVariable(RosettaImplicitVariable expr, JavaScope context) {
		implicitVariable(expr, context)
	}

	override protected caseIntLiteral(RosettaIntLiteral expr, JavaScope context) {
		'''«MapperS».of(«Integer».valueOf(«expr.value»))'''
	}

	override protected caseJoinOperation(JoinOperation expr, JavaScope context) {
		binaryExpr(expr, context)
	}

	override protected caseLastOperation(LastOperation expr, JavaScope context) {
		buildListOperationNoBody(expr, "last", context)
	}

	override protected caseLessThanOperation(ComparisonOperation expr, JavaScope context) {
		binaryExpr(expr, context)
	}

	override protected caseLessThanOrEqualOperation(ComparisonOperation expr, JavaScope context) {
		binaryExpr(expr, context)
	}

	override protected caseListLiteral(ListLiteral expr, JavaScope context) {
		if (expr.isEmpty) {
			'''null'''
		} else {
			'''«MapperC».<«typeProvider.getRType(expr).toJavaReferenceType»>of(«FOR ele : expr.elements SEPARATOR ', '»«ele.javaCode(context)»«ENDFOR»)'''
		}
	}

	override protected caseMapOperation(MapOperation expr, JavaScope context) {
		val isBodyMulti = expr.function.isBodyExpressionMulti
		val funcExpr = expr.function.inlineFunction(context, true, true)

		if (!expr.isPreviousOperationMulti) {
			if (isBodyMulti) {
				'''
				«expr.argument.ensureMapperJavaCode(context, false)»
					.mapSingleToList(«funcExpr»)'''
			} else {
				buildSingleItemListOperationOptionalBody(expr, "mapSingleToItem", context)
			}
		} else {
			if (expr.argument.isOutputListOfLists) {
				if (isBodyMulti) {
					'''
					«expr.argument.ensureMapperJavaCode(context, false)»
						.mapListToList(«funcExpr»)'''
				} else {
					'''
					«expr.argument.ensureMapperJavaCode(context, false)»
						.mapListToItem(«funcExpr»)'''
				}
			} else {
				if (isBodyMulti) {
					'''
					«expr.argument.ensureMapperJavaCode(context, false)»
						.mapItemToList(«funcExpr»)'''
				} else {
					buildSingleItemListOperationOptionalBody(expr, "mapItem", context)
				}
			}
		}
	}

	override protected caseMaxOperation(MaxOperation expr, JavaScope context) {
		buildSingleItemListOperationOptionalBody(expr, "max", context)
	}

	override protected caseMinOperation(MinOperation expr, JavaScope context) {
		buildSingleItemListOperationOptionalBody(expr, "min", context)
	}

	override protected caseMultiplyOperation(ArithmeticOperation expr, JavaScope context) {
		binaryExpr(expr, context)
	}

	override protected caseNotEqualsOperation(EqualityOperation expr, JavaScope context) {
		binaryExpr(expr, context)
	}

	override protected caseNumberLiteral(RosettaNumberLiteral expr, JavaScope context) {
		'''«MapperS».of(new «BigDecimal»("«expr.value»"))'''
	}

	override protected caseOneOfOperation(OneOfOperation expr, JavaScope context) {
		val type = typeProvider.getRType(expr.argument) as RDataType
		buildConstraint(expr.argument, type.data.allAttributes, Necessity.REQUIRED, context)
	}

	override protected caseOnlyElementOperation(RosettaOnlyElement expr, JavaScope context) {
		return '''«MapperS».of(«expr.argument.javaCode(context)».get())'''
	}

	override protected caseOnlyExists(RosettaOnlyExistsExpression expr, JavaScope context) {
		'''«runtimeMethod('onlyExists')»(«Arrays».asList(«FOR arg : expr.args SEPARATOR ', '»«arg.javaCode(context)»«ENDFOR»))'''
	}

	override protected caseOrOperation(LogicalOperation expr, JavaScope context) {
		binaryExpr(expr, context)
	}

	override protected caseReduceOperation(ReduceOperation expr, JavaScope context) {
		val outputType = typeProvider.getRType(expr.function.body).toJavaReferenceType
		'''
		«expr.argument.javaCode(context)»
			.<«outputType»>reduce(«expr.function.inlineFunction(context, true, true)»)'''
	}

	override protected caseReverseOperation(ReverseOperation expr, JavaScope context) {
		buildListOperationNoBody(expr, "reverse", context)
	}

	override protected caseSortOperation(SortOperation expr, JavaScope context) {
		buildSingleItemListOperationOptionalBody(expr, "sort", context)
	}

	override protected caseStringLiteral(RosettaStringLiteral expr, JavaScope context) {
		'''«MapperS».of("«StringEscapeUtils.escapeJava(expr.value)»")'''
	}

	override protected caseSubtractOperation(ArithmeticOperation expr, JavaScope context) {
		binaryExpr(expr, context)
	}

	override protected caseSumOperation(SumOperation expr, JavaScope context) {
		buildListOperationNoBody(expr, "sum" + typeProvider.getRType(expr.argument).toJavaReferenceType.simpleName,
			context)
	}

	override protected caseSymbolReference(RosettaSymbolReference expr, JavaScope context) {
		val s = expr.symbol
		switch (s) {
			Data: { // -------> replace with call to implicit variable?
				'''«MapperS».of(«context.getIdentifierOrThrow(new RDataType(s).toBlueprintImplicitVar)»)'''
			}
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
					featureCall(implicitVariable(expr, context), s, context, autoValue)
				} else
					'''«IF s.card.isIsMany»«MapperC».<«attribute.RType.toJavaReferenceType»>«ELSE»«MapperS».«ENDIF»of(«context.getIdentifierOrThrow(attribute)»)'''
			}
			ShortcutDeclaration: {
				val shortcut = rObjectFactory.buildRShortcut(s);
				val multi = cardinalityProvider.isMulti(s)
				'''«IF multi»«MapperC».<«typeProvider.getRTypeOfSymbol(s).toJavaReferenceType»>«ELSE»«MapperS».«ENDIF»of(«context.getIdentifierOrThrow(shortcut)»(«aliasCallArgs(s)»).«IF exprHelper.usesOutputParameter(s.expression)»build()«ELSE»«IF multi»getMulti()«ELSE»get()«ENDIF»«ENDIF»)'''
			}
			RosettaEnumeration: '''«new REnumType(s).toJavaType»'''
			ClosureParameter: '''«context.getIdentifierOrThrow(s)»'''
			RosettaCallableWithArgs: {
				callableWithArgsCall(s, expr.args, context)
			}
			default:
				throw new UnsupportedOperationException("Unsupported symbol type of " + s?.class?.name)
		}
	}

	override protected caseThenOperation(ThenOperation expr, JavaScope context) {
		val funcExpr = expr.function.inlineFunction(context, false, true)
		'''
		«expr.argument.ensureMapperJavaCode(context, false)»
			.apply(«funcExpr»)'''
	}

	private def StringConcatenationClient conversionOperation(RosettaUnaryOperation expr, JavaScope context,
		StringConcatenationClient conversion, Class<? extends Exception> errorClass) {
		'''«expr.argument.ensureMapperJavaCode(context, false)».checkedMap("«expr.operator»", «conversion», «errorClass».class)'''
	}

	override protected caseToEnumOperation(ToEnumOperation expr, JavaScope context) {
		val javaEnum = new REnumType(expr.enumeration).toJavaType
		conversionOperation(expr, context, '''«javaEnum»::fromDisplayName''', IllegalArgumentException)
	}

	override protected caseToIntOperation(ToIntOperation expr, JavaScope context) {
		conversionOperation(expr, context, '''«Integer»::parseInt''', NumberFormatException)
	}

	override protected caseToNumberOperation(ToNumberOperation expr, JavaScope context) {
		conversionOperation(expr, context, '''«BigDecimal»::new''', NumberFormatException)
	}

	override protected caseToStringOperation(ToStringOperation expr, JavaScope context) {
		'''«expr.argument.ensureMapperJavaCode(context, false)».map("«expr.operator»", «Object»::toString)'''
	}

	override protected caseToTimeOperation(ToTimeOperation expr, JavaScope context) {
		val lambdaScope = context.lambdaScope
		val lambdaParam = lambdaScope.createUniqueIdentifier("s")
		conversionOperation(expr,
			context, '''«lambdaParam» -> «LocalTime».parse(s, «DateTimeFormatter».ISO_LOCAL_TIME)''',
			DateTimeParseException)
	}

}
