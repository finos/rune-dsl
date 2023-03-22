package com.regnosys.rosetta.generator.java.expression

import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.function.CardinalityProvider
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.generator.java.util.JavaNames
import com.regnosys.rosetta.generator.java.types.JavaType
import com.regnosys.rosetta.generator.util.RosettaAttributeExtensions
import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions
import com.regnosys.rosetta.rosetta.expression.RosettaAbsentExpression
import com.regnosys.rosetta.rosetta.expression.RosettaBigDecimalLiteral
import com.regnosys.rosetta.rosetta.expression.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.expression.RosettaBooleanLiteral
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs
import com.regnosys.rosetta.rosetta.expression.RosettaConditionalExpression
import com.regnosys.rosetta.rosetta.expression.RosettaCountOperation
import com.regnosys.rosetta.rosetta.RosettaEnumValue
import com.regnosys.rosetta.rosetta.RosettaEnumValueReference
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.expression.RosettaExistsExpression
import com.regnosys.rosetta.rosetta.expression.RosettaExpression
import com.regnosys.rosetta.rosetta.RosettaExternalFunction
import com.regnosys.rosetta.rosetta.RosettaFeature
import com.regnosys.rosetta.rosetta.expression.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.expression.RosettaIntLiteral
import com.regnosys.rosetta.rosetta.expression.RosettaLiteral
import com.regnosys.rosetta.rosetta.RosettaMetaType
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.rosetta.expression.RosettaOnlyExistsExpression
import com.regnosys.rosetta.rosetta.expression.RosettaStringLiteral
import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.expression.ClosureParameter
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.expression.ListLiteral
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration
import com.regnosys.rosetta.types.RosettaOperators
import com.regnosys.rosetta.types.RosettaTypeProvider
import com.regnosys.rosetta.utils.ExpressionHelper
import com.rosetta.model.lib.expression.CardinalityOperator
import com.rosetta.model.lib.expression.ComparisonResult
import com.rosetta.model.lib.expression.ExpressionOperators
import com.rosetta.model.lib.expression.MapperMaths
import com.rosetta.model.lib.mapper.MapperC
import com.rosetta.model.lib.mapper.MapperS
import java.math.BigDecimal
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.EcoreUtil2

import static extension com.regnosys.rosetta.generator.java.enums.EnumHelper.convertValues
import static extension com.regnosys.rosetta.generator.java.util.JavaClassTranslator.toJavaClass
import java.util.Arrays
import com.regnosys.rosetta.rosetta.expression.FilterOperation
import com.regnosys.rosetta.rosetta.expression.NamedFunctionReference
import com.regnosys.rosetta.rosetta.expression.InlineFunction
import com.regnosys.rosetta.rosetta.expression.MapOperation
import com.regnosys.rosetta.rosetta.expression.FlattenOperation
import com.regnosys.rosetta.rosetta.expression.DistinctOperation
import com.regnosys.rosetta.rosetta.expression.SumOperation
import com.regnosys.rosetta.rosetta.expression.MinOperation
import com.regnosys.rosetta.rosetta.expression.MaxOperation
import com.regnosys.rosetta.rosetta.expression.SortOperation
import com.regnosys.rosetta.rosetta.expression.ReverseOperation
import com.regnosys.rosetta.rosetta.expression.ReduceOperation
import com.regnosys.rosetta.rosetta.expression.RosettaUnaryOperation
import com.regnosys.rosetta.rosetta.expression.FirstOperation
import com.regnosys.rosetta.rosetta.expression.LastOperation
import com.regnosys.rosetta.rosetta.expression.RosettaFunctionalOperation
import com.regnosys.rosetta.rosetta.expression.ExistsModifier
import com.regnosys.rosetta.rosetta.expression.RosettaOnlyElement
import com.regnosys.rosetta.rosetta.expression.ModifiableBinaryOperation
import com.regnosys.rosetta.rosetta.expression.CardinalityModifier
import com.regnosys.rosetta.rosetta.expression.LogicalOperation
import com.regnosys.rosetta.rosetta.expression.RosettaContainsExpression
import com.regnosys.rosetta.rosetta.expression.RosettaDisjointExpression
import com.regnosys.rosetta.rosetta.expression.ComparisonOperation
import com.regnosys.rosetta.rosetta.expression.EqualityOperation
import com.regnosys.rosetta.rosetta.expression.ExtractAllOperation
import org.eclipse.emf.ecore.EObject
import com.regnosys.rosetta.rosetta.expression.RosettaReference
import com.regnosys.rosetta.utils.ImplicitVariableUtil
import com.regnosys.rosetta.rosetta.expression.RosettaImplicitVariable
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference
import java.util.List
import com.regnosys.rosetta.rosetta.RosettaRecordFeature
import com.regnosys.rosetta.generator.util.RecordFeatureMap
import com.regnosys.rosetta.rosetta.expression.AsKeyOperation
import com.regnosys.rosetta.rosetta.expression.OneOfOperation
import com.regnosys.rosetta.rosetta.expression.ChoiceOperation
import com.regnosys.rosetta.rosetta.expression.Necessity
import com.rosetta.model.lib.validation.ValidationResult.ChoiceRuleValidationMethod
import com.regnosys.rosetta.types.RDataType
import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.types.JavaClass
import com.regnosys.rosetta.generator.java.JavaIdentifierRepresentationService

class ExpressionGenerator {
	
	@Inject protected RosettaTypeProvider typeProvider
	@Inject RosettaOperators operators
	@Inject extension CardinalityProvider cardinalityProvider
	@Inject JavaNames.Factory factory 
	@Inject RosettaFunctionExtensions funcExt
	@Inject extension RosettaExtensions
	@Inject extension ImportManagerExtension
	@Inject ExpressionHelper exprHelper
	@Inject extension ImplicitVariableUtil
	@Inject extension JavaIdentifierRepresentationService
	@Inject RecordFeatureMap recordFeatureMap
	
	/**
	 * convert a rosetta expression to code
	 * ParamMpa params  - a map keyed by classname or positional index that provides variable names for expression parameters
	 */
	def StringConcatenationClient javaCode(RosettaExpression expr, JavaScope scope, JavaNames names) {
		switch (expr) {
			RosettaFeatureCall: {
				featureCall(expr, scope, names)
			}
			RosettaOnlyExistsExpression: {
				onlyExistsExpr(expr, scope, names)
			}
			RosettaExistsExpression: {
				existsExpr(expr, scope, names)
			}
			RosettaBinaryOperation: {
				binaryExpr(expr, null, scope, names)
			}
			RosettaCountOperation: {
				countExpr(expr, null, scope, names)
			}
			RosettaAbsentExpression: {
				absentExpr(expr, expr.argument, scope, names)
			}
			RosettaReference: {
				reference(expr, scope, names)
			}
			RosettaBigDecimalLiteral : {
				'''«MapperS».of(new «BigDecimal»("«expr.value»"))'''
			}
			RosettaBooleanLiteral : {
				'''«MapperS».of(Boolean.valueOf(«expr.value»))'''
			}
			RosettaIntLiteral : {
				'''«MapperS».of(Integer.valueOf(«expr.value»))'''
			}
			RosettaStringLiteral : {
				'''«MapperS».of("«expr.value»")'''
			}
			RosettaEnumValueReference : {
				'''«MapperS».of(«expr.enumeration.toJavaType».«expr.value.convertValues»)'''
			}
			RosettaConditionalExpression : {
				'''«expr.genConditionalMapper(scope, names)»'''
			}
			ListLiteral : {
				listLiteral(expr, scope, names)
			}
			DistinctOperation : {
				distinctOperation(expr, scope, names)
			}
			FirstOperation : {
				firstOperation(expr, scope, names)
			}
			FlattenOperation : {
				flattenOperation(expr, scope, names)
			}
			LastOperation : {
				lastOperation(expr, scope, names)
			}
			MaxOperation : {
				maxOperation(expr, scope, names)
			}
			MinOperation : {
				minOperation(expr, scope, names)
			}
			SumOperation : {
				sumOperation(expr, scope, names)
			}
			ReverseOperation : {
				reverseOperation(expr, scope, names)
			}
			RosettaOnlyElement : {
				onlyElement(expr, scope, names)
			}
			ReduceOperation : {
				reduceOperation(expr, scope, names)
			}
			FilterOperation : {
				filterOperation(expr, scope, names)
			}
			MapOperation : {
				mapOperation(expr, scope, names)
			}
			ExtractAllOperation : {
				extractAllOperation(expr, scope, names)
			}
			SortOperation : {
				sortOperation(expr, scope, names)
			}
			AsKeyOperation: {
				// this operation is currently handled by the `FunctionGenerator`
				expr.argument.javaCode(scope, names)
			}
			OneOfOperation: {
				oneOfOperation(expr, scope, names)
			}
			ChoiceOperation: {
				choiceOperation(expr, scope, names)
			}
			default: 
				throw new UnsupportedOperationException("Unsupported expression type of " + expr?.class?.simpleName)
		}
	}
	
	private def runtimeMethod(String methodName) {
		return importWildcard(method(ExpressionOperators, methodName))
	}
	
	private def StringConcatenationClient emptyToMapperJavaCode(RosettaExpression expr, JavaScope scope, JavaNames names, boolean multi) {
		if (expr.isEmpty) {
			'''«IF multi»«MapperC»«ELSE»«MapperS»«ENDIF».ofNull()'''
		} else {
			expr.javaCode(scope, names)
		}
	}
	
	def StringConcatenationClient listLiteral(ListLiteral e, JavaScope scope, extension JavaNames names) {
	    if (e.isEmpty) {
	        '''null'''
	    } else {
	       '''«MapperC».<«typeProvider.getRType(e).toJavaType»>of(«FOR ele: e.elements SEPARATOR ', '»«ele.javaCode(scope, names)»«ENDFOR»)'''
	    }
	}
	
	private def boolean isEmpty(RosettaExpression e) { // TODO: temporary workaround while transitioning from old to new type system
	    if (e instanceof ListLiteral) {
	        e.elements.size === 0
	    } else {
	        false
	    }
	}

	private def StringConcatenationClient genConditionalMapper(RosettaConditionalExpression expr, JavaScope scope, JavaNames names)'''
		«IF expr.ifthen.evaluatesToComparisonResult»com.rosetta.model.lib.mapper.MapperUtils.toComparisonResult(«ENDIF»com.rosetta.model.lib.mapper.MapperUtils.«IF funcExt.needsBuilder(expr.ifthen)»fromDataType«ELSE»fromBuiltInType«ENDIF»(() -> {
			«expr.genConditional(scope, names)»
		})«IF expr.ifthen.evaluatesToComparisonResult»)«ENDIF»'''



	private def StringConcatenationClient genConditional(RosettaConditionalExpression expr, JavaScope scope, JavaNames names) {
		return  '''
			if («expr.^if.javaCode(scope, names)».get()) {
				return «expr.ifthen.javaCode(scope, names)»;
			}
			«IF expr.childElseThen !== null»
				«expr.childElseThen.genElseIf(scope, names)»
			«ELSE»
				else {
					return «expr.elsethen.emptyToMapperJavaCode(scope, names, cardinalityProvider.isMulti(expr.ifthen))»;
				}
			«ENDIF»
			'''
	}

	private def StringConcatenationClient genElseIf(RosettaConditionalExpression next, JavaScope scope, JavaNames names) {
		'''
		«IF next !== null»
			else if («next.^if.javaCode(scope, names)».get()) {
				return «next.ifthen.javaCode(scope, names)»;
			}
			«IF next.childElseThen !== null»
				«next.childElseThen.genElseIf(scope, names)»
			«ELSE»
				else {
					return «next.elsethen.emptyToMapperJavaCode(scope, names, cardinalityProvider.isMulti(next.ifthen))»;
				}
			«ENDIF»
		«ENDIF»
		'''
	}

	private def RosettaConditionalExpression childElseThen(RosettaConditionalExpression expr) {
		if (expr.elsethen instanceof RosettaConditionalExpression)
			expr.elsethen as RosettaConditionalExpression
	}
	
	private def StringConcatenationClient callableWithArgs(RosettaCallableWithArgs callable, JavaScope scope, extension JavaNames names, StringConcatenationClient argsCode, boolean needsMapper) {		
		return switch (callable) {
			Function: {
				val multi = funcExt.getOutput(callable).card.isMany
				'''«IF needsMapper»«IF multi»«MapperC».<«typeProvider.getRType(callable.output).toJavaType»>«ELSE»«MapperS».«ENDIF»of(«ENDIF»«scope.getIdentifierOrThrow(callable.toFunctionInstance)».evaluate(«argsCode»)«IF needsMapper»)«ENDIF»'''
			}
			RosettaExternalFunction: {
				'''«IF needsMapper»«MapperS».of(«ENDIF»new «factory.create(callable.model).toJavaType(callable)»().execute(«argsCode»)«IF needsMapper»)«ENDIF»'''
			}
			default: 
				throw new UnsupportedOperationException("Unsupported callable with args of type " + callable?.eClass?.name)
		}
		
	}
	
	def StringConcatenationClient callableWithArgsCall(RosettaCallableWithArgs func, List<RosettaExpression> arguments, JavaScope scope, JavaNames names) {
		callableWithArgs(func, scope, names, '''«args(arguments, scope, names)»''', true)
	}
	
	private def StringConcatenationClient args(List<RosettaExpression> arguments, JavaScope scope, JavaNames names) {
		'''«FOR argExpr : arguments SEPARATOR ', '»«arg(argExpr, scope, names)»«ENDFOR»'''
	}
	
	private def StringConcatenationClient arg(RosettaExpression expr, JavaScope scope, JavaNames names) {
		'''«expr.javaCode(scope, names)»«IF expr.evalulatesToMapper»«IF cardinalityProvider.isMulti(expr)».getMulti()«ELSE».get()«ENDIF»«ENDIF»'''
	}
	
	def StringConcatenationClient onlyExistsExpr(RosettaOnlyExistsExpression onlyExists, JavaScope scope, JavaNames names) {
		'''«runtimeMethod('onlyExists')»(«Arrays».asList(«FOR arg : onlyExists.args SEPARATOR ', '»«arg.javaCode(scope, names)»«ENDFOR»))'''
	}
	
	def StringConcatenationClient existsExpr(RosettaExistsExpression exists, JavaScope scope, JavaNames names) {
		val arg = exists.argument
		val binary = arg.findBinaryOperation
		if (binary !== null) {
			if(binary.isLogicalOperation)
				'''«doExistsExpr(exists, arg.javaCode(scope, names))»'''
			else 
				//if the argument is a binary expression then the exists needs to be pushed down into it
				binary.binaryExpr(exists, scope, names)
		}
		else {
			'''«doExistsExpr(exists, arg.javaCode(scope, names))»'''
		}
	}
	
	def RosettaBinaryOperation findBinaryOperation(RosettaExpression expression) {
		switch(expression) {
			RosettaBinaryOperation: expression
			default: null
		}
	}
	
	private def StringConcatenationClient doExistsExpr(RosettaExistsExpression exists, StringConcatenationClient arg) {
		if(exists.modifier === ExistsModifier.SINGLE)
			'''«runtimeMethod('singleExists')»(«arg»)'''
		else if(exists.modifier === ExistsModifier.MULTIPLE)
			'''«runtimeMethod('multipleExists')»(«arg»)'''
		else
			'''«runtimeMethod('exists')»(«arg»)'''
	}

	def StringConcatenationClient absentExpr(RosettaAbsentExpression notSet, RosettaExpression argument, JavaScope scope, JavaNames names) {
		val arg = argument
		val binary = arg.findBinaryOperation
		if (binary !== null) {
			if(binary.isLogicalOperation)
				'''«runtimeMethod('notExists')»(«binary.binaryExpr(notSet, scope, names)»)'''
			else
				//if the arg is binary then the operator needs to be pushed down
				binary.binaryExpr(notSet, scope, names)
		}
		else {
			'''«runtimeMethod('notExists')»(«arg.javaCode(scope, names)»)'''
		}
	}
	
	private def StringConcatenationClient implicitVariable(EObject context, JavaScope scope) {
		val definingContainer = context.findContainerDefiningImplicitVariable.get
		if (definingContainer instanceof Data) {
			// For conditions
			return '''«MapperS».of(«scope.getIdentifierOrThrow(context.implicitVarInContext)»)'''
		} else {
			// For inline functions
			return '''«scope.getIdentifierOrThrow(context.implicitVarInContext)»'''
		}
	}
	
	protected def StringConcatenationClient reference(RosettaReference expr, JavaScope scope, extension JavaNames names) {
		switch (expr) {
			RosettaImplicitVariable: {
				implicitVariable(expr, scope)
			}
			RosettaSymbolReference: {
				val s = expr.symbol
				switch (s)  {
					Data: { // -------> replace with call to implicit variable?
						'''«MapperS».of(«scope.getIdentifierOrThrow(new RDataType(s).toBlueprintImplicitVar)»)'''
					}
					Attribute: {
						// Data attributes can only be called if there is an implicit variable present.
						// The current container (Data) is stored in Params, but we need also look for superTypes
						// so we could also do: (s.eContainer as Data).allSuperTypes.map[it|params.getClass(it)].filterNull.head
						val implicitType = typeProvider.typeOfImplicitVariable(expr)
						val implicitFeatures = implicitType.allFeatures
						if(implicitFeatures.contains(s)) {
							var autoValue = true //if the attribute being referenced is WithMeta and we aren't accessing the meta fields then access the value by default
							if (expr.eContainer instanceof RosettaFeatureCall && (expr.eContainer as RosettaFeatureCall).feature instanceof RosettaMetaType) {
								autoValue = false;
							}
							featureCall(implicitVariable(expr, scope), s, scope, names, autoValue)
						}
						else
							'''«IF s.card.isIsMany»«MapperC».<«typeProvider.getRType(s).toJavaType»>«ELSE»«MapperS».«ENDIF»of(«scope.getIdentifierOrThrow(s)»)'''
					}
					ShortcutDeclaration: {
						val multi = cardinalityProvider.isMulti(s)
						'''«IF multi»«MapperC».<«typeProvider.getRType(s).toJavaType»>«ELSE»«MapperS».«ENDIF»of(«scope.getIdentifierOrThrow(s)»(«aliasCallArgs(s)»).«IF exprHelper.usesOutputParameter(s.expression)»build()«ELSE»«IF multi»getMulti()«ELSE»get()«ENDIF»«ENDIF»)'''
					}
					RosettaEnumeration: '''«s.toJavaType»'''
					ClosureParameter: '''«scope.getIdentifierOrThrow(s)»'''
					RosettaCallableWithArgs: {
						callableWithArgsCall(s, expr.args, scope, names)
					}
					default: 
						throw new UnsupportedOperationException("Unsupported symbol type of " + s?.class?.name)
				}
			}
			default: 
				throw new UnsupportedOperationException("Unsupported reference type of " + expr?.class?.name)
		}
	}
	
	def aliasCallArgs(ShortcutDeclaration alias) {
		val func = EcoreUtil2.getContainerOfType(alias, Function)
		val attrs = <String>newArrayList
		attrs.addAll(funcExt.getInputs(func).map[name].toList)
		if(exprHelper.usesOutputParameter(alias.expression)) {
			attrs.add(0, funcExt.getOutput(func)?.name + '.toBuilder()')
		}
		attrs.join(', ')
	}
	
	/**
	 * feature call is a call to get an attribute of an object e.g. Quote->amount
	 */
	private def StringConcatenationClient featureCall(RosettaFeatureCall call, JavaScope scope, JavaNames names) {
		var autoValue = true //if the attribute being referenced is WithMeta and we aren't accessing the meta fields then access the value by default
		if (call.eContainer instanceof RosettaFeatureCall && (call.eContainer as RosettaFeatureCall).feature instanceof RosettaMetaType) {
			autoValue = false;
		}
		return featureCall(javaCode(call.receiver, scope, names), call.feature, scope, names, autoValue)
	}
	private def StringConcatenationClient featureCall(StringConcatenationClient receiverCode, RosettaFeature feature, JavaScope scope, JavaNames names, boolean autoValue) {
		val StringConcatenationClient right = switch (feature) {
			Attribute:
				feature.buildMapFunc(autoValue, scope, names)
			RosettaMetaType: 
				'''«feature.buildMapFunc(scope)»'''
			RosettaEnumValue: 
				return '''«MapperS».of(«feature.enumeration.toJavaType».«feature.convertValues»)'''
			RosettaRecordFeature:
				'''.map("«feature.name.toFirstUpper»", «recordFeatureMap.recordFeatureToLambda(feature)»)'''
			default: 
				'''.map("get«feature.name.toFirstUpper»", «feature.containerType.toJavaType»::get«feature.name.toFirstUpper»)'''
		}
		
		return '''«receiverCode»«right»'''
	}
	
	private def StringConcatenationClient distinct(StringConcatenationClient code) {
		return '''«runtimeMethod('distinct')»(«code»)'''
	}
	
	def private RosettaType containerType(RosettaFeature feature) {
		EcoreUtil2.getContainerOfType(feature, RosettaType)
	}
	
	def StringConcatenationClient countExpr(RosettaCountOperation expr, RosettaExpression test, JavaScope scope, JavaNames names) {
		'''«MapperS».of(«expr.argument.javaCode(scope, names)».resultCount())'''
	}
	
	def StringConcatenationClient binaryExpr(RosettaBinaryOperation expr, RosettaExpression test, JavaScope scope, extension JavaNames names) {
		val left = expr.left
		val right = expr.right
		val leftRtype = typeProvider.getRType(expr.left)
		val rightRtype = typeProvider.getRType(expr.right)
		val resultType = operators.resultType(expr.operator, leftRtype,rightRtype)
		val leftType = leftRtype.toJavaType
		val rightType = rightRtype.toJavaType
		
		switch expr.operator {
			case ("and"): {
				'''«left.toComparisonResult(scope, names)».and(«right.toComparisonResult(scope, names)»)'''
			}
			case ("or"): {
				'''«left.toComparisonResult(scope, names)».or(«right.toComparisonResult(scope, names)»)'''
			}
			case ("+"): {
				'''«MapperMaths».<«resultType.name.toJavaClass», «leftType», «rightType»>add(«expr.left.javaCode(scope, names)», «expr.right.javaCode(scope, names)»)'''
			}
			case ("-"): {
				'''«MapperMaths».<«resultType.name.toJavaClass», «leftType», «rightType»>subtract(«expr.left.javaCode(scope, names)», «expr.right.javaCode(scope, names)»)'''
			}
			case ("*"): {
				'''«MapperMaths».<«resultType.name.toJavaClass», «leftType», «rightType»>multiply(«expr.left.javaCode(scope, names)», «expr.right.javaCode(scope, names)»)'''
			}
			case ("/"): {
				'''«MapperMaths».<«resultType.name.toJavaClass», «leftType», «rightType»>divide(«expr.left.javaCode(scope, names)», «expr.right.javaCode(scope, names)»)'''
			}
			case ("contains"): {
				'''«runtimeMethod("contains")»(«expr.left.javaCode(scope, names)», «expr.right.javaCode(scope, names)»)'''
			}
			case ("disjoint"): {
				'''«runtimeMethod("disjoint")»(«expr.left.javaCode(scope, names)», «expr.right.javaCode(scope, names)»)'''
			}
			case ("join"): {
				'''
				«expr.left.javaCode(scope, names)»
					.join(«IF expr.right !== null»«expr.right.javaCode(scope, names)»«ELSE»«MapperS».of("")«ENDIF»)'''
			}
			default: {
				toComparisonOp('''«expr.left.emptyToMapperJavaCode(scope, names, false)»''', expr.operator, '''«expr.right.emptyToMapperJavaCode(scope, names, false)»''', (expr as ModifiableBinaryOperation).cardMod)
			}
		}
	}

	def StringConcatenationClient toComparisonResult(RosettaExpression expr, JavaScope scope, JavaNames names) {
		val wrap = !expr.evaluatesToComparisonResult
		'''«IF wrap»«ComparisonResult».of(«ENDIF»«expr.javaCode(scope, names)»«IF wrap»)«ENDIF»'''
	}

	private def boolean isLogicalOperation(RosettaExpression expr) {
		if(expr instanceof RosettaBinaryOperation) return expr.operator == "and" || expr.operator == "or"
		return false
	}
	
	private def boolean isArithmeticOperation(RosettaExpression expr) {
		if(expr instanceof RosettaBinaryOperation) 
			return RosettaOperators.ARITHMETIC_OPS.contains(expr.operator)
		return false
	}
		
	/**
	 * Collects all expressions down the tree, and checks that they're all either FeatureCalls or CallableCalls (or anything that resolves to a Mapper)
	 */
	private def boolean evalulatesToMapper(RosettaExpression expr) { // TODO: this function is faulty, I think
		val exprs = newHashSet
		collectExpressions(expr, [exprs.add(it)])

		return expr.evaluatesToComparisonResult
			|| !exprs.empty
			&& exprs.stream.allMatch[it instanceof RosettaFeatureCall ||
									it instanceof RosettaReference ||
									it instanceof RosettaLiteral ||
									it instanceof ListLiteral && !(it.isEmpty && !(it.eContainer instanceof RosettaConditionalExpression)) ||
									it instanceof RosettaCountOperation ||
									it instanceof RosettaFunctionalOperation ||
									it instanceof RosettaOnlyElement ||
									it instanceof RosettaConditionalExpression ||
									isArithmeticOperation(it)
			]
	}
	private def boolean evaluatesToComparisonResult(RosettaExpression expr) {
		return expr instanceof LogicalOperation
			|| expr instanceof ComparisonOperation
			|| expr instanceof EqualityOperation
			|| expr instanceof RosettaContainsExpression
			|| expr instanceof RosettaDisjointExpression
			|| expr instanceof RosettaOnlyExistsExpression
			|| expr instanceof RosettaExistsExpression
			|| expr instanceof RosettaAbsentExpression
			|| expr instanceof RosettaConditionalExpression && (expr as RosettaConditionalExpression).ifthen.evaluatesToComparisonResult
			|| expr instanceof OneOfOperation
			|| expr instanceof ChoiceOperation
	}
	
	private def StringConcatenationClient toComparisonOp(StringConcatenationClient left, String operator, StringConcatenationClient right, CardinalityModifier cardMod) {
		switch operator {
			case ("="): {
				'''«runtimeMethod('areEqual')»(«left», «right», «toCardinalityOperator(cardMod, CardinalityModifier.ALL)»)'''
			}
			case ("<>"):
				'''«runtimeMethod('notEqual')»(«left», «right», «toCardinalityOperator(cardMod, CardinalityModifier.ANY)»)'''
			case ("<") : 
				'''«runtimeMethod('lessThan')»(«left», «right», «toCardinalityOperator(cardMod, CardinalityModifier.ALL)»)'''
			case ("<=") : 
				'''«runtimeMethod('lessThanEquals')»(«left», «right», «toCardinalityOperator(cardMod, CardinalityModifier.ALL)»)'''
			case (">") : 
				'''«runtimeMethod('greaterThan')»(«left», «right», «toCardinalityOperator(cardMod, CardinalityModifier.ALL)»)'''
			case (">=") : 
				'''«runtimeMethod('greaterThanEquals')»(«left», «right», «toCardinalityOperator(cardMod, CardinalityModifier.ALL)»)'''
			default: 
				throw new UnsupportedOperationException("Unsupported binary operation of " + operator)
		}
	}
	
	private def StringConcatenationClient toCardinalityOperator(CardinalityModifier cardOp, CardinalityModifier defaultOp) {
		'''«CardinalityOperator».«if (cardOp === CardinalityModifier.NONE) defaultOp.toString.toFirstUpper else cardOp.toString.toFirstUpper»'''
	}
	
	/**
	 * Builds the expression of mapping functions to extract a path of attributes
	 */
	def StringConcatenationClient buildMapFunc(Attribute attribute, boolean autoValue, JavaScope scope, JavaNames names) {
		val mapFunc = attribute.buildMapFuncAttribute(scope, names)
		if (attribute.card.isIsMany) {
			if (attribute.metaAnnotations.nullOrEmpty)
				'''.<«attribute.type.toJavaType»>mapC(«mapFunc»)'''
			else if (!autoValue) {
				'''.<«attribute.metaClass»>mapC(«mapFunc»)'''
			}
			else {
				'''.<«attribute.metaClass»>mapC(«mapFunc»).<«attribute.type.toJavaType»>map("getValue", _f->_f.getValue())'''
			}
		}
		else
		{
			if (attribute.metaAnnotations.nullOrEmpty){
				if(attribute.type instanceof Data) 
				'''.<«attribute.type.toJavaType»>map(«mapFunc»)'''
				else
				'''.<«attribute.type.toJavaType»>map(«mapFunc»)'''
			}
			else if (!autoValue) {
				'''.<«attribute.metaClass»>map(«mapFunc»)'''
			}
			else
				'''.<«attribute.metaClass»>map(«mapFunc»).<«attribute.type.toJavaType»>map("getValue", _f->_f.getValue())'''
		}
	}
	
	private def JavaType toJavaType(RosettaType rosType) {
		val model = rosType.model
		if(model === null)
			throw new IllegalArgumentException('''Can not create type reference. «rosType.eClass?.name» «rosType.name» is not attached to a «RosettaModel.name»''')
		factory.create(model).toJavaType(typeProvider.getRType(rosType))
	}
	
	private def javaNames(Attribute attr) {
		val model = EcoreUtil2.getContainerOfType(attr, RosettaModel)
		if(model === null)
			throw new IllegalArgumentException('''Can not create type reference. «attr.eClass?.name» «attr.name» is not attached to a «RosettaModel.name»''')
		factory.create(model)
	}
	
	def JavaType metaClass(Attribute attribute) {
		val names = attribute.javaNames
		val name = if (!attribute.hasMetaFieldAnnotations)  
						if (RosettaAttributeExtensions.isBuiltInType(attribute.type))
							"BasicReferenceWithMeta"+attribute.type.name.toFirstUpper
						else
							"ReferenceWithMeta"+attribute.type.name.toFirstUpper
				   else 
				   		"FieldWithMeta"+attribute.type.name.toFirstUpper
		return names.toMetaType(attribute, name)
	}
	
	def static StringConcatenationClient buildMapFunc(RosettaMetaType meta, JavaScope scope) {
		if (meta.name=="reference") {
			val lambdaScope = scope.lambdaScope
			val lambdaParam = lambdaScope.createUniqueIdentifier("a")
			'''.map("get«meta.name.toFirstUpper»", «lambdaParam»->«lambdaParam».getGlobalReference())'''
		}
		else {
			val lambdaScope1 = scope.lambdaScope
			val lambdaParam1 = lambdaScope1.createUniqueIdentifier("a")
			val lambdaScope2 = scope.lambdaScope
			val lambdaParam2 = lambdaScope2.createUniqueIdentifier("a")
			'''.map("getMeta", «lambdaParam1»->«lambdaParam1».getMeta()).map("get«meta.name.toFirstUpper»", «lambdaParam2»->«lambdaParam2».get«meta.name.toFirstUpper»())'''
		}
	}
	
	def dispatch StringConcatenationClient functionReference(NamedFunctionReference ref, JavaScope scope, JavaNames names, boolean doCast, boolean needsMapper) {		
		val callable = ref.function
		val inputs = switch (callable) {
			Function: {
				callable.inputs
			}
			RosettaExternalFunction: {
				callable.parameters
			}
			default: 
				throw new UnsupportedOperationException("Unsupported callable with args of type " + callable?.eClass?.name)
		}
		val lambdaScope = scope.lambdaScope
		val inputIds = inputs.map[lambdaScope.createIdentifier(it)]
		val StringConcatenationClient inputExprs = '''«FOR input: inputs SEPARATOR ', '»«lambdaScope.getIdentifierOrThrow(input)»«IF cardinalityProvider.isMulti(input)».getMulti()«ELSE».get()«ENDIF»«ENDFOR»'''
		val body = callableWithArgs(callable, lambdaScope, names, inputExprs, needsMapper)
		'''(«FOR id: inputIds SEPARATOR ', '»«id»«ENDFOR») -> «body»'''
	}
	
	def dispatch StringConcatenationClient functionReference(InlineFunction ref, JavaScope scope, extension JavaNames names, boolean doCast, boolean needsMapper) {
		val lambdaScope = scope.lambdaScope
		val paramIds = if (ref.parameters.size == 0) {
			#[lambdaScope.createIdentifier(ref.implicitVarInContext, defaultImplicitVariable.name) ]
		} else {
			ref.parameters.map[lambdaScope.createIdentifier(it)]
		}
		
		val isBodyMulti = ref.isBodyExpressionMulti
		val StringConcatenationClient bodyExpr = '''«ref.body.javaCode(lambdaScope, names)»«IF needsMapper»«IF ref.body.evaluatesToComparisonResult».asMapper()«ENDIF»«ELSE»«IF ref.body.evalulatesToMapper»«IF isBodyMulti».getMulti()«ELSE».get()«ENDIF»«ENDIF»«ENDIF»'''
		val StringConcatenationClient cast = if (doCast) {
			val outputType = typeProvider.getRType(ref.body).toJavaType
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
	
	def StringConcatenationClient onlyElement(RosettaOnlyElement expr, JavaScope scope, JavaNames names) {
		return '''«MapperS».of(«expr.argument.javaCode(scope, names)».get())'''
	}
	
	def StringConcatenationClient filterOperation(FilterOperation op, JavaScope scope, JavaNames names) {
		'''
		«op.argument.emptyToMapperJavaCode(scope, names, true)»
			.«IF op.functionRef.isItemMulti»filterList«ELSE»filterItem«ENDIF»(«op.functionRef.functionReference(scope, names, true, false)»)'''
	}
	
	def StringConcatenationClient mapOperation(MapOperation op, JavaScope scope, JavaNames names) {
		val isBodyMulti =  op.functionRef.isBodyExpressionMulti
		val funcExpr = op.functionRef.functionReference(scope, names, true, true)
		
		if (!op.isPreviousOperationMulti) {
			if (isBodyMulti) {
				'''
				«op.argument.emptyToMapperJavaCode(scope, names, false)»
					.mapSingleToList(«funcExpr»)'''
			} else {
				buildSingleItemListOperationOptionalBody(op, "mapSingleToItem", scope, names)
			}
		} else {
			if (op.argument.isOutputListOfLists) {
				if (isBodyMulti) {
					'''
					«op.argument.emptyToMapperJavaCode(scope, names, false)»
						.mapListToList(«funcExpr»)'''
				} else {
					'''
					«op.argument.emptyToMapperJavaCode(scope, names, false)»
						.mapListToItem(«funcExpr»)'''
				}
			} else {
				if (isBodyMulti) {
					'''
					«op.argument.emptyToMapperJavaCode(scope, names, false)»
						.mapItemToList(«funcExpr»)'''
				} else {
					buildSingleItemListOperationOptionalBody(op, "mapItem", scope, names)
				}
			}
		}
	}
	
	def StringConcatenationClient extractAllOperation(ExtractAllOperation op, JavaScope scope, JavaNames names) {
		val funcExpr = op.functionRef.functionReference(scope, names, false, true)
		'''
		«op.argument.emptyToMapperJavaCode(scope, names, false)»
			.apply(«funcExpr»)'''
	}
	
	def StringConcatenationClient flattenOperation(FlattenOperation op, JavaScope scope, JavaNames names) {
		buildListOperationNoBody(op, "flattenList", scope, names)
	}
	
	def StringConcatenationClient distinctOperation(DistinctOperation op, JavaScope scope, JavaNames names) {
		distinct(op.argument.javaCode(scope, names))
	}
	
	def StringConcatenationClient sumOperation(SumOperation op, JavaScope scope, extension JavaNames names) {
		buildListOperationNoBody(op, "sum" + (typeProvider.getRType(op.argument).toJavaType as JavaClass).simpleName, scope, names)
	}
	
	def StringConcatenationClient minOperation(MinOperation op, JavaScope scope, JavaNames names) {
		buildSingleItemListOperationOptionalBody(op, "min", scope, names)
	}
	
	def StringConcatenationClient maxOperation(MaxOperation op, JavaScope scope, JavaNames names) {
		buildSingleItemListOperationOptionalBody(op, "max", scope, names)
	}
	
	def StringConcatenationClient sortOperation(SortOperation op, JavaScope scope, JavaNames names) {
		buildSingleItemListOperationOptionalBody(op, "sort", scope, names)
	}
	
	def StringConcatenationClient reverseOperation(ReverseOperation op, JavaScope scope, JavaNames names) {
		buildListOperationNoBody(op, "reverse", scope, names)
	}
	
	def StringConcatenationClient reduceOperation(ReduceOperation op, JavaScope scope, extension JavaNames names) {
		val outputType =  typeProvider.getRType(op.functionRef).toJavaType
		'''
		«op.argument.javaCode(scope, names)»
			.<«outputType»>reduce(«op.functionRef.functionReference(scope, names, true, true)»)'''
	}
	
	def StringConcatenationClient firstOperation(FirstOperation op, JavaScope scope, JavaNames names) {
		buildListOperationNoBody(op, "first", scope, names)
	}
	
	def StringConcatenationClient lastOperation(LastOperation op, JavaScope scope, JavaNames names) {
		buildListOperationNoBody(op, "last", scope, names)
	}
	
	def StringConcatenationClient oneOfOperation(OneOfOperation op, JavaScope scope, JavaNames names) {
		val type = typeProvider.getRType(op.argument) as RDataType
		buildConstraint(op.argument, type.data.allAttributes, Necessity.REQUIRED, scope, names)
	}
	
	def StringConcatenationClient choiceOperation(ChoiceOperation op, JavaScope scope, JavaNames names) {
		buildConstraint(op.argument, op.attributes, op.necessity, scope, names)
	}
	
	private def StringConcatenationClient buildConstraint(RosettaExpression arg, Iterable<Attribute> usedAttributes, Necessity validationType, JavaScope scope, JavaNames names) {
		'''«runtimeMethod('choice')»(«arg.javaCode(scope, names)», «Arrays».asList(«usedAttributes.join(", ")['"' + name + '"']»), «ChoiceRuleValidationMethod».«validationType.name()»)'''
	}
	
	private def StringConcatenationClient buildListOperationNoBody(RosettaUnaryOperation op, String name, JavaScope scope, JavaNames names) {
		'''
		«op.argument.emptyToMapperJavaCode(scope, names, true)»
			.«name»()'''	
	}
	
	private def StringConcatenationClient buildSingleItemListOperationOptionalBody(RosettaFunctionalOperation op, String name, JavaScope scope, JavaNames names) {
		if (op.functionRef === null) {
			buildListOperationNoBody(op, name, scope, names)
		} else {
			buildSingleItemListOperation(op, name, scope, names)
		}
	}
	
	private def StringConcatenationClient buildSingleItemListOperation(RosettaFunctionalOperation op, String name, JavaScope scope, JavaNames names) {
		'''
		«op.argument.emptyToMapperJavaCode(scope, names, true)»
			.«name»(«op.functionRef.functionReference(scope, names, true, true)»)'''	
	}
	
	private def StringConcatenationClient buildMapFuncAttribute(Attribute attribute, JavaScope scope, JavaNames names) {
		if(attribute.eContainer instanceof Data) {
			val lambdaScope = scope.lambdaScope
			val lambdaParam = lambdaScope.createUniqueIdentifier(attribute.attributeTypeVariableName(names))
			'''"get«attribute.name.toFirstUpper»", «lambdaParam» -> «IF attribute.override»(«attribute.type.toJavaType») «ENDIF»«lambdaParam».get«attribute.name.toFirstUpper»()'''
		}
	}

	private def attributeTypeVariableName(Attribute attribute, extension JavaNames names) {
		((attribute.eContainer as Data).toJavaType.toReferenceType as JavaClass).simpleName.toFirstLower
	}
	
	/**
	 * The id for a parameter - either a Class name or a positional index
	 */
//	@org.eclipse.xtend.lib.annotations.Data static class ParamID {
//		RosettaType c
//		int index
//		String name;
//	}
	
	//Class mapping from class name or positional index to the name of a variable defined in the containing code
//	static class ParamMap extends HashMap<ParamID, String> {
//		new(RosettaType c) {
//			if (null !== c)
//				put(new ParamID(c, -1, null), c.name.toFirstLower);
//		}
//		
//		new(RosettaType c, String name) {
//			put(new ParamID(c, -1, null), name);
//		}
//		
//		new(){
//		}
//		
//		def dispatch String getClass(RosettaType c) {
//			return get(new ParamID(c, -1, null))
//		}
//		
//		def dispatch String getClass(Data c) {
//			entrySet.findFirst[e|
//				val type  = e.key.c
//				if (type instanceof Data) {
//					if (type.isSub(c)) return true
//				}
//				false	
//			]?.value
//		}
//		
//		def boolean isSub(Data d1, Data d2) {
//			if (d1==d2) return true
//			return d1.hasSuperType && d1.superType.isSub(d2)
//		}
//	}
	
	/**
	 * Create a string representation of a rosetta function  
	 * mainly used to give human readable names to the mapping functions used to extract attributes
	 */
	def StringConcatenationClient toNodeLabel(RosettaExpression expr) {
		switch (expr) {
			RosettaFeatureCall : {
				toNodeLabel(expr)
			}
			RosettaBinaryOperation : {
				toNodeLabel(expr)
			}
			RosettaStringLiteral : {
				'''\"«expr.value»\"'''
			}
			RosettaConditionalExpression : {
				'''choice'''
			}
			RosettaExistsExpression : {
				'''«toNodeLabel(expr.argument)» exists'''
			}
			RosettaEnumValueReference : {
				'''«expr.enumeration.name»'''
			}
			RosettaEnumValue : {
				'''«expr.name»'''
			}
			ListLiteral : {
				'''[«FOR el:expr.elements SEPARATOR ", " »«el.toNodeLabel»«ENDFOR»]'''
			}
			RosettaLiteral : {
				'''«expr.stringValue»'''
			}
			RosettaCountOperation : {
				'''«toNodeLabel(expr.argument)» count'''
			}
			RosettaSymbolReference : {
				'''«expr.symbol.name»«IF expr.explicitArguments»(«FOR arg:expr.args SEPARATOR ", "»«arg.toNodeLabel»«ENDFOR»)«ENDIF»'''
			}
			RosettaImplicitVariable : {
				'''«defaultImplicitVariable.name»'''
			}
			RosettaOnlyElement : {
				toNodeLabel(expr.argument)
			}
			default :
				'''Unsupported expression type of «expr?.class?.name»'''
		}
	}
	
	def StringConcatenationClient toNodeLabel(RosettaFeatureCall call) {
		val feature = call.feature
		val right = switch feature {
			RosettaMetaType, 
			Attribute, 
			RosettaEnumValue: 
				feature.name
			default: throw new UnsupportedOperationException("Unsupported expression type (feature) " + feature?.getClass)
		}
		
		val receiver = call.receiver
		val left = switch receiver {
			RosettaReference,
			RosettaFeatureCall: {
				toNodeLabel(receiver)
			}
			RosettaOnlyElement : {
				toNodeLabel(receiver.argument)
			}
			default: throw new UnsupportedOperationException("Unsupported expression type (receiver) " + receiver?.getClass)
		}
		
		'''«left»->«right»'''
	}
	
	def StringConcatenationClient toNodeLabel(RosettaBinaryOperation binOp) {
		'''«binOp.left.toNodeLabel» «binOp.operator» «binOp.right.toNodeLabel»'''
	}
}
