package com.regnosys.rosetta.generator.java.expression

import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.function.CardinalityProvider
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.generator.java.util.JavaNames
import com.regnosys.rosetta.generator.java.util.JavaType
import com.regnosys.rosetta.generator.util.RosettaAttributeExtensions
import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions
import com.regnosys.rosetta.generator.util.Util
import com.regnosys.rosetta.rosetta.expression.RosettaAbsentExpression
import com.regnosys.rosetta.rosetta.expression.RosettaBigDecimalLiteral
import com.regnosys.rosetta.rosetta.expression.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.expression.RosettaBooleanLiteral
import com.regnosys.rosetta.rosetta.expression.RosettaCallableCall
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs
import com.regnosys.rosetta.rosetta.expression.RosettaCallableWithArgsCall
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
import java.util.HashMap
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.EcoreUtil2

import static extension com.regnosys.rosetta.generator.java.enums.EnumHelper.convertValues
import static extension com.regnosys.rosetta.generator.java.util.JavaClassTranslator.toJavaClass
import static extension com.regnosys.rosetta.generator.java.util.JavaClassTranslator.toJavaType
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

class ExpressionGenerator {
	
	@Inject protected RosettaTypeProvider typeProvider
	@Inject RosettaOperators operators
	@Inject extension CardinalityProvider cardinalityProvider
	@Inject JavaNames.Factory factory 
	@Inject RosettaFunctionExtensions funcExt
	@Inject extension RosettaExtensions
	@Inject extension ImportManagerExtension
	@Inject ExpressionHelper exprHelper
	@Inject extension Util
	@Inject extension ListOperationExtensions
	
	/**
	 * convert a rosetta expression to code
	 * ParamMpa params  - a map keyed by classname or positional index that provides variable names for expression parameters
	 */
	def StringConcatenationClient javaCode(RosettaExpression expr, ParamMap params) {
		switch (expr) {
			RosettaFeatureCall : {
				var autoValue = true //if the attribute being referenced is WithMeta and we aren't accessing the meta fields then access the value by default
				if (expr.eContainer!==null && expr.eContainer instanceof RosettaFeatureCall && (expr.eContainer as RosettaFeatureCall).feature instanceof RosettaMetaType) {
					autoValue=false;
				}
				featureCall(expr, params, autoValue)
			}
			RosettaOnlyExistsExpression : {
				onlyExistsExpr(expr, params)
			}
			RosettaExistsExpression : {
				existsExpr(expr, params)
			}
			RosettaBinaryOperation : {
				binaryExpr(expr, null, params)
			}
			RosettaCountOperation : {
				countExpr(expr, null, params)
			}
			RosettaAbsentExpression : {
				absentExpr(expr, expr.argument, params)
			}
			RosettaCallableCall : {
				callableCall(expr, params) 
			}
			RosettaCallableWithArgsCall: {
				callableWithArgsCall(expr, params)
			}
			RosettaBigDecimalLiteral : {
				'''«MapperS».of(«BigDecimal».valueOf(«expr.value»))'''
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
				'''«expr.genConditionalMapper(params)»'''
			}
			ListLiteral : {
				listLiteral(expr, params)
			}
			DistinctOperation : {
				distinctOperation(expr, params)
			}
			FirstOperation : {
				firstOperation(expr, params)
			}
			FlattenOperation : {
				flattenOperation(expr, params)
			}
			LastOperation : {
				lastOperation(expr, params)
			}
			MaxOperation : {
				maxOperation(expr, params)
			}
			MinOperation : {
				minOperation(expr, params)
			}
			SumOperation : {
				sumOperation(expr, params)
			}
			ReverseOperation : {
				reverseOperation(expr, params)
			}
			RosettaOnlyElement : {
				onlyElement(expr, params)
			}
			ReduceOperation : {
				reduceOperation(expr, params)
			}
			FilterOperation : {
				filterOperation(expr, params)
			}
			MapOperation : {
				mapOperation(expr, params)
			}
			ExtractAllOperation : {
				extractAllOperation(expr, params)
			}
			SortOperation : {
				sortOperation(expr, params)
			}
			default: 
				throw new UnsupportedOperationException("Unsupported expression type of " + expr?.class?.simpleName)
		}
	}
	
	private def StringConcatenationClient emptyToMapperJavaCode(RosettaExpression expr, ParamMap params, boolean multi) {
		if (expr.isEmpty) {
			'''«IF multi»«MapperC»«ELSE»«MapperS»«ENDIF».ofNull()'''
		} else {
			expr.javaCode(params)
		}
	}
	
	def StringConcatenationClient listLiteral(ListLiteral e, ParamMap params) {
	    if (e.isEmpty) {
	        '''null'''
	    } else {
	       '''«MapperC».of(«FOR ele: e.elements SEPARATOR ', '»«ele.javaCode(params)»«ENDFOR»)'''
	    }
	}
	
	private def boolean isEmpty(RosettaExpression e) { // TODO: temporary workaround while transitioning from old to new type system
	    if (e instanceof ListLiteral) {
	        e.elements.size === 0
	    } else {
	        false
	    }
	}

	private def StringConcatenationClient genConditionalMapper(RosettaConditionalExpression expr, ParamMap params)'''
		«IF expr.ifthen.evaluatesToComparisonResult»com.rosetta.model.lib.mapper.MapperUtils.toComparisonResult(«ENDIF»com.rosetta.model.lib.mapper.MapperUtils.«IF funcExt.needsBuilder(expr.ifthen)»fromDataType«ELSE»fromBuiltInType«ENDIF»(() -> {
			«expr.genConditional(params)»
		})«IF expr.ifthen.evaluatesToComparisonResult»)«ENDIF»'''



	private def StringConcatenationClient genConditional(RosettaConditionalExpression expr, ParamMap params) {
		return  '''
			if («expr.^if.javaCode(params)».get()) {
				return «expr.ifthen.javaCode(params)»;
			}
			«IF expr.childElseThen !== null»
				«expr.childElseThen.genElseIf(params)»
			«ELSE»
				else {
					return «expr.elsethen.emptyToMapperJavaCode(params, cardinalityProvider.isMulti(expr.ifthen))»;
				}
			«ENDIF»
			'''
	}

	private def StringConcatenationClient genElseIf(RosettaConditionalExpression next, ParamMap params) {
		'''
		«IF next !== null»
			else if («next.^if.javaCode(params)».get()) {
				return «next.ifthen.javaCode(params)»;
			}
			«IF next.childElseThen !== null»
				«next.childElseThen.genElseIf(params)»
			«ELSE»
				else {
					return «next.elsethen.emptyToMapperJavaCode(params, cardinalityProvider.isMulti(next.ifthen))»;
				}
			«ENDIF»
		«ENDIF»
		'''
	}

	private def RosettaConditionalExpression childElseThen(RosettaConditionalExpression expr) {
		if (expr.elsethen instanceof RosettaConditionalExpression)
			expr.elsethen as RosettaConditionalExpression
	}
	
	private def StringConcatenationClient callableWithArgs(RosettaCallableWithArgs callable, ParamMap params, StringConcatenationClient argsCode, boolean needsMapper) {		
		return switch (callable) {
			Function: {
				val multi = funcExt.getOutput(callable).card.isMany
				'''«IF needsMapper»«IF multi»«MapperC»«ELSE»«MapperS»«ENDIF».of(«ENDIF»«callable.name.toFirstLower».evaluate(«argsCode»)«IF needsMapper»)«ENDIF»'''
			}
			RosettaExternalFunction: {
				'''«IF needsMapper»«MapperS».of(«ENDIF»new «factory.create(callable.model).toJavaType(callable as RosettaCallableWithArgs)»().execute(«argsCode»)«IF needsMapper»)«ENDIF»'''
			}
			default: 
				throw new UnsupportedOperationException("Unsupported callable with args of type " + callable?.eClass?.name)
		}
		
	}
	
	def StringConcatenationClient callableWithArgsCall(RosettaCallableWithArgsCall expr, ParamMap params) {
		val callable = expr.callable
		val implicitArg = funcExt.implicitFirstArgument(expr)
		callableWithArgs(callable, params, '''«IF implicitArg !== null»«implicitArg.name.toFirstLower»«ENDIF»«args(expr, params)»''', true)
	}
	
	private def StringConcatenationClient args(RosettaCallableWithArgsCall expr, ParamMap params) {
		'''«FOR argExpr : expr.args SEPARATOR ', '»«arg(argExpr, params)»«ENDFOR»'''
	}
	
	private def StringConcatenationClient arg(RosettaExpression expr, ParamMap params) {
		'''«expr.javaCode(params)»«IF expr.evalulatesToMapper»«IF cardinalityProvider.isMulti(expr)».getMulti()«ELSE».get()«ENDIF»«ENDIF»'''
	}
	
	def StringConcatenationClient onlyExistsExpr(RosettaOnlyExistsExpression onlyExists, ParamMap params) {
		'''«importWildCard(ExpressionOperators)»onlyExists(«Arrays».asList(«FOR arg : onlyExists.args SEPARATOR ', '»«arg.javaCode(params)»«ENDFOR»))'''
	}
	
	def StringConcatenationClient existsExpr(RosettaExistsExpression exists, ParamMap params) {
		val arg = exists.argument
		val binary = arg.findBinaryOperation
		if (binary !== null) {
			if(binary.isLogicalOperation)
				'''«importWildCard(ExpressionOperators)»«doExistsExpr(exists, arg.javaCode(params))»'''
			else 
				//if the argument is a binary expression then the exists needs to be pushed down into it
				binary.binaryExpr(exists, params)
		}
		else {
			'''«importWildCard(ExpressionOperators)»«doExistsExpr(exists, arg.javaCode(params))»'''
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
			'''singleExists(«arg»)'''
		else if(exists.modifier === ExistsModifier.MULTIPLE)
			'''multipleExists(«arg»)'''
		else
			'''exists(«arg»)'''
	}

	def StringConcatenationClient absentExpr(RosettaAbsentExpression notSet, RosettaExpression argument, ParamMap params) {
		val arg = argument
		val binary = arg.findBinaryOperation
		if (binary !== null) {
			if(binary.isLogicalOperation)
				'''«ExpressionOperators».notExists(«binary.binaryExpr(notSet, params)»)'''
			else
				//if the arg is binary then the operator needs to be pushed down
				binary.binaryExpr(notSet, params)
		}
		else {
			'''«importMethod(ExpressionOperators,"notExists")»(«arg.javaCode(params)»)'''
		}
	}
	
	protected def StringConcatenationClient callableCall(RosettaCallableCall expr, ParamMap params) {
		if (expr.implicitReceiver) {
			return '''«EcoreUtil2.getContainerOfType(expr, InlineFunction).itemName»'''
		}
		val call = expr.callable
		switch (call)  {
			Data : {
				'''«MapperS».of(«params.getClass(call)»)'''
			}
			Attribute : {
				// Data Attributes can only be called from their conditions
				// The current container (Data) is stored in Params, but we need also look for superTypes
				// so we could also do: (call.eContainer as Data).allSuperTypes.map[it|params.getClass(it)].filterNull.head
				if(call.eContainer instanceof Data)
					'''«MapperS».of(«EcoreUtil2.getContainerOfType(expr, Data).getName.toFirstLower»)«buildMapFunc(call, true, expr)»'''
				else
					'''«if (call.card.isIsMany) MapperC else MapperS».of(«call.name»)'''
			}
			ShortcutDeclaration : {
				val multi = cardinalityProvider.isMulti(call)
				'''«IF multi»«MapperC»«ELSE»«MapperS»«ENDIF».of(«call.name»(«aliasCallArgs(call)»).«IF exprHelper.usesOutputParameter(call.expression)»build()«ELSE»«IF multi»getMulti()«ELSE»get()«ENDIF»«ENDIF»)'''
			}
			RosettaEnumeration: '''«call.toJavaType»'''
			ClosureParameter: '''«call.getNameOrDefault.toDecoratedName(call.function)»'''
			default: 
				throw new UnsupportedOperationException("Unsupported callable type of " + call?.class?.name)
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
	private def StringConcatenationClient featureCall(RosettaFeatureCall call, ParamMap params, boolean autoValue) {
		val feature = call.feature
		val StringConcatenationClient right = switch (feature) {
			Attribute:
				feature.buildMapFunc(autoValue, call)
			RosettaMetaType: 
				'''«feature.buildMapFunc»'''
			RosettaEnumValue: 
				return '''«MapperS».of(«feature.enumeration.toJavaType».«feature.convertValues»)'''
			default: 
				'''.map("get«feature.name.toFirstUpper»", «feature.containerType.toJavaType»::get«feature.name.toFirstUpper»)'''
		}
		
		return '''«javaCode(call.receiver, params)»«right»'''
	}
	
	private def StringConcatenationClient distinct(StringConcatenationClient code) {
		return '''«importWildCard(ExpressionOperators)»distinct(«code»)'''
	}
	
	def private RosettaType containerType(RosettaFeature feature) {
		EcoreUtil2.getContainerOfType(feature, RosettaType)
	}
	
	def StringConcatenationClient countExpr(RosettaCountOperation expr, RosettaExpression test, ParamMap params) {
		'''«MapperS».of(«expr.argument.javaCode(params)».resultCount())'''
	}
	
	def StringConcatenationClient binaryExpr(RosettaBinaryOperation expr, RosettaExpression test, ParamMap params) {
		val left = expr.left
		val right = expr.right
		val leftRtype = typeProvider.getRType(expr.left)
		val rightRtype = typeProvider.getRType(expr.right)
		val resultType = operators.resultType(expr.operator, leftRtype,rightRtype)
		val leftType = '''«leftRtype.name.toJavaType»'''
		val rightType = '''«rightRtype.name.toJavaType»'''
		
		switch expr.operator {
			case ("and"): {
				'''«left.toComparisonResult(params)».and(«right.toComparisonResult(params)»)'''
			}
			case ("or"): {
				'''«left.toComparisonResult(params)».or(«right.toComparisonResult(params)»)'''
			}
			case ("+"): {
				'''«MapperMaths».<«resultType.name.toJavaClass», «leftType», «rightType»>add(«expr.left.javaCode(params)», «expr.right.javaCode(params)»)'''
			}
			case ("-"): {
				'''«MapperMaths».<«resultType.name.toJavaClass», «leftType», «rightType»>subtract(«expr.left.javaCode(params)», «expr.right.javaCode(params)»)'''
			}
			case ("*"): {
				'''«MapperMaths».<«resultType.name.toJavaClass», «leftType», «rightType»>multiply(«expr.left.javaCode(params)», «expr.right.javaCode(params)»)'''
			}
			case ("/"): {
				'''«MapperMaths».<«resultType.name.toJavaClass», «leftType», «rightType»>divide(«expr.left.javaCode(params)», «expr.right.javaCode(params)»)'''
			}
			case ("contains"): {
				'''«importMethod(ExpressionOperators,"contains")»(«expr.left.javaCode(params)», «expr.right.javaCode(params)»)'''
			}
			case ("disjoint"): {
				'''«importMethod(ExpressionOperators,"disjoint")»(«expr.left.javaCode(params)», «expr.right.javaCode(params)»)'''
			}
			case ("join"): {
				'''
				«expr.left.javaCode(params)»
					.join(«IF expr.right !== null»«expr.right.javaCode(params)»«ELSE»«MapperS».of("")«ENDIF»)'''
			}
			default: {
				toComparisonOp('''«expr.left.emptyToMapperJavaCode(params, false)»''', expr.operator, '''«expr.right.emptyToMapperJavaCode(params, false)»''', (expr as ModifiableBinaryOperation).cardMod)
			}
		}
	}

	def StringConcatenationClient toComparisonResult(RosettaExpression expr, ParamMap params) {
		val wrap = !expr.evaluatesToComparisonResult
		'''«IF wrap»«ComparisonResult».of(«ENDIF»«expr.javaCode(params)»«IF wrap»)«ENDIF»'''
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
									it instanceof RosettaCallableCall ||
									it instanceof RosettaCallableWithArgsCall ||
									it instanceof RosettaLiteral && !(it.isEmpty && !(it.eContainer instanceof RosettaConditionalExpression)) ||
									it instanceof RosettaCountOperation ||
									it instanceof RosettaFunctionalOperation ||
									it instanceof RosettaOnlyElement ||
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
	}
	
	private def StringConcatenationClient toComparisonOp(StringConcatenationClient left, String operator, StringConcatenationClient right, CardinalityModifier cardMod) {
		switch operator {
			case ("="): {
				'''«importWildCard(ExpressionOperators)»areEqual(«left», «right», «toCardinalityOperator(cardMod, CardinalityModifier.ALL)»)'''
			}
			case ("<>"):
				'''«importWildCard(ExpressionOperators)»notEqual(«left», «right», «toCardinalityOperator(cardMod, CardinalityModifier.ANY)»)'''
			case ("<") : 
				'''«importWildCard(ExpressionOperators)»lessThan(«left», «right», «toCardinalityOperator(cardMod, CardinalityModifier.ALL)»)'''
			case ("<=") : 
				'''«importWildCard(ExpressionOperators)»lessThanEquals(«left», «right», «toCardinalityOperator(cardMod, CardinalityModifier.ALL)»)'''
			case (">") : 
				'''«importWildCard(ExpressionOperators)»greaterThan(«left», «right», «toCardinalityOperator(cardMod, CardinalityModifier.ALL)»)'''
			case (">=") : 
				'''«importWildCard(ExpressionOperators)»greaterThanEquals(«left», «right», «toCardinalityOperator(cardMod, CardinalityModifier.ALL)»)'''
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
	def StringConcatenationClient buildMapFunc(Attribute attribute, boolean autoValue, EObject container) {
		val mapFunc = attribute.buildMapFuncAttribute(container)
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
		factory.create(model).toJavaType(rosType)
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
	
	def static StringConcatenationClient buildMapFunc(RosettaMetaType meta) {
		if (meta.name=="reference") {
			'''.map("get«meta.name.toFirstUpper»", a->a.getGlobalReference())'''
		}
		else {
			'''.map("getMeta", a->a.getMeta()).map("get«meta.name.toFirstUpper»", a->a.get«meta.name.toFirstUpper»())'''
		}
	}
	
	def dispatch StringConcatenationClient functionReference(NamedFunctionReference ref, ParamMap params, boolean doCast, boolean needsMapper) {		
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
		val StringConcatenationClient inputExprs = '''«FOR input: inputs SEPARATOR ', '»«input.name.toDecoratedName(ref)»«IF cardinalityProvider.isMulti(input)».getMulti()«ELSE».get()«ENDIF»«ENDFOR»'''
		val body = callableWithArgs(callable, params, inputExprs, needsMapper)
		'''(«FOR input: inputs SEPARATOR ', '»«input.name.toDecoratedName(ref)»«ENDFOR») -> «body»'''
	}
	
	def dispatch StringConcatenationClient functionReference(InlineFunction ref, ParamMap params, boolean doCast, boolean needsMapper) {
		val isBodyMulti =  ref.isBodyExpressionMulti
		val StringConcatenationClient bodyExpr = '''«ref.body.javaCode(params)»«IF needsMapper»«IF ref.body.evaluatesToComparisonResult».asMapper()«ENDIF»«ELSE»«IF ref.body.evalulatesToMapper»«IF isBodyMulti».getMulti()«ELSE».get()«ENDIF»«ENDIF»«ENDIF»'''
		val StringConcatenationClient cast = if (doCast) {
			val outputType =  ref.bodyRawType
			'''(«IF needsMapper»«IF isBodyMulti»«MapperC»<«outputType»>«ELSE»«MapperS»<«outputType»>«ENDIF»«ELSE»«outputType»«ENDIF»)'''
		} else {
			''''''
		}
		if (ref.parameters.size <= 1) {
			val item = ref.itemName
			'''«item» -> «cast»«bodyExpr»'''
		} else {
			val items = ref.parameters.map[name.toDecoratedName(ref)]
			'''(«FOR item : items SEPARATOR ', '»«item»«ENDFOR») -> «cast»«bodyExpr»'''
		}
	}
	
	def StringConcatenationClient onlyElement(RosettaOnlyElement expr, ParamMap params) {
		return '''«MapperS».of(«expr.argument.javaCode(params)».get())'''
	}
	
	def StringConcatenationClient filterOperation(FilterOperation op, ParamMap params) {
		'''
		«op.argument.emptyToMapperJavaCode(params, true)»
			.«IF op.functionRef.isItemMulti»filterList«ELSE»filterItem«ENDIF»(«op.functionRef.functionReference(params, true, false)»)'''
	}
	
	def StringConcatenationClient mapOperation(MapOperation op, ParamMap params) {
		val isBodyMulti =  op.functionRef.isBodyExpressionMulti
		val funcExpr = op.functionRef.functionReference(params, true, true)
		
		if (!op.isPreviousOperationMulti) {
			if (isBodyMulti) {
				'''
				«op.argument.emptyToMapperJavaCode(params, false)»
					.mapSingleToList(«funcExpr»)'''
			} else {
				buildSingleItemListOperationOptionalBody(op, "mapSingleToItem", params)
			}
		} else {
			if (op.argument.isOutputListOfLists) {
				if (isBodyMulti) {
					'''
					«op.argument.emptyToMapperJavaCode(params, false)»
						.mapListToList(«funcExpr»)'''
				} else {
					'''
					«op.argument.emptyToMapperJavaCode(params, false)»
						.mapListToItem(«funcExpr»)'''
				}
			} else {
				if (isBodyMulti) {
					'''
					«op.argument.emptyToMapperJavaCode(params, false)»
						.mapItemToList(«funcExpr»)'''
				} else {
					buildSingleItemListOperationOptionalBody(op, "mapItem", params)
				}
			}
		}
	}
	
	def StringConcatenationClient extractAllOperation(ExtractAllOperation op, ParamMap params) {
		val funcExpr = op.functionRef.functionReference(params, false, true)
		'''
		«op.argument.emptyToMapperJavaCode(params, false)»
			.apply(«funcExpr»)'''
	}
	
	def StringConcatenationClient flattenOperation(FlattenOperation op, ParamMap params) {
		buildListOperationNoBody(op, "flattenList", params)
	}
	
	def StringConcatenationClient distinctOperation(DistinctOperation op, ParamMap params) {
		distinct(op.argument.javaCode(params))
	}
	
	def StringConcatenationClient sumOperation(SumOperation op, ParamMap params) {
		buildListOperationNoBody(op, "sum" + op.inputRawType, params)
	}
	
	def StringConcatenationClient minOperation(MinOperation op, ParamMap params) {
		buildSingleItemListOperationOptionalBody(op, "min", params)
	}
	
	def StringConcatenationClient maxOperation(MaxOperation op, ParamMap params) {
		buildSingleItemListOperationOptionalBody(op, "max", params)
	}
	
	def StringConcatenationClient sortOperation(SortOperation op, ParamMap params) {
		buildSingleItemListOperationOptionalBody(op, "sort", params)
	}
	
	def StringConcatenationClient reverseOperation(ReverseOperation op, ParamMap params) {
		buildListOperationNoBody(op, "reverse", params)
	}
	
	def StringConcatenationClient reduceOperation(ReduceOperation op, ParamMap params) {
		val outputType =  op.functionRef.bodyRawType
		'''
		«op.argument.javaCode(params)»
			.<«outputType»>reduce(«op.functionRef.functionReference(params, true, true)»)'''
	}
	
	def StringConcatenationClient firstOperation(FirstOperation op, ParamMap params) {
		buildListOperationNoBody(op, "first", params)
	}
	
	def StringConcatenationClient lastOperation(LastOperation op, ParamMap params) {
		buildListOperationNoBody(op, "last", params)
	}
	
	private def StringConcatenationClient buildListOperationNoBody(RosettaUnaryOperation op, String name, ParamMap params) {
		'''
		«op.argument.emptyToMapperJavaCode(params, true)»
			.«name»()'''	
	}
	
	private def StringConcatenationClient buildSingleItemListOperationOptionalBody(RosettaFunctionalOperation op, String name, ParamMap params) {
		if (op.functionRef === null) {
			buildListOperationNoBody(op, name, params)
		} else {
			buildSingleItemListOperation(op, name, params)
		}
	}
	
	private def StringConcatenationClient buildSingleItemListOperation(RosettaFunctionalOperation op, String name, ParamMap params) {
		'''
		«op.argument.emptyToMapperJavaCode(params, true)»
			.«name»(«op.functionRef.functionReference(params, true, true)»)'''	
	}
	
	private def StringConcatenationClient buildMapFuncAttribute(Attribute attribute, EObject container) {
		if(attribute.eContainer instanceof Data) 
			'''"get«attribute.name.toFirstUpper»", «attribute.attributeTypeVariableName(container)» -> «IF attribute.override»(«attribute.type.toJavaType») «ENDIF»«attribute.attributeTypeVariableName(container)».get«attribute.name.toFirstUpper»()'''
	}

	private def attributeTypeVariableName(Attribute attribute, EObject container) {
		(attribute.eContainer as Data).toJavaType.simpleName.toFirstLower.toDecoratedName(container)
	}
	
	/**
	 * The id for a parameter - either a Class name or a positional index
	 */
	@org.eclipse.xtend.lib.annotations.Data static class ParamID {
		RosettaType c
		int index
		String name;
	}
	
	//Class mapping from class name or positional index to the name of a variable defined in the containing code
	static class ParamMap extends HashMap<ParamID, String> {
		new(RosettaType c) {
			if (null !== c)
				put(new ParamID(c, -1, null), c.name.toFirstLower);
		}
		
		new(RosettaType c, String name) {
			put(new ParamID(c, -1, null), name);
		}
		
		new(){
		}
		
		def dispatch String getClass(RosettaType c) {
			return get(new ParamID(c, -1, null))
		}
		
		def dispatch String getClass(Data c) {
			entrySet.findFirst[e|
				val type  = e.key.c
				if (type instanceof Data) {
					if (type.isSub(c)) return true
				}
				false	
			]?.value
		}
		
		def boolean isSub(Data d1, Data d2) {
			if (d1==d2) return true
			return d1.hasSuperType && d1.superType.isSub(d2)
		}
	}
	
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
			RosettaCallableWithArgsCall :{
				'''«expr.callable.name»(«FOR arg:expr.args SEPARATOR ", "»«arg.toNodeLabel»«ENDFOR»)'''
			}
			RosettaCallableCall : {
				'''«expr.callable.name»'''
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
			RosettaCallableCall, 
			RosettaCallableWithArgsCall, 
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
