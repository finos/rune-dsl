package com.regnosys.rosetta.generator.java.expression

import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.RosettaJavaPackages
import com.regnosys.rosetta.generator.java.function.CardinalityProvider
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.generator.java.util.JavaNames
import com.regnosys.rosetta.generator.java.util.JavaType
import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions
import com.regnosys.rosetta.rosetta.RosettaAbsentExpression
import com.regnosys.rosetta.rosetta.RosettaAlias
import com.regnosys.rosetta.rosetta.RosettaBigDecimalLiteral
import com.regnosys.rosetta.rosetta.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.RosettaBooleanLiteral
import com.regnosys.rosetta.rosetta.RosettaCallableCall
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgsCall
import com.regnosys.rosetta.rosetta.RosettaClass
import com.regnosys.rosetta.rosetta.RosettaConditionalExpression
import com.regnosys.rosetta.rosetta.RosettaContainsExpression
import com.regnosys.rosetta.rosetta.RosettaCountOperation
import com.regnosys.rosetta.rosetta.RosettaEnumValueReference
import com.regnosys.rosetta.rosetta.RosettaExistsExpression
import com.regnosys.rosetta.rosetta.RosettaExpression
import com.regnosys.rosetta.rosetta.RosettaExternalFunction
import com.regnosys.rosetta.rosetta.RosettaFeature
import com.regnosys.rosetta.rosetta.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.RosettaGroupByExpression
import com.regnosys.rosetta.rosetta.RosettaGroupByFeatureCall
import com.regnosys.rosetta.rosetta.RosettaIntLiteral
import com.regnosys.rosetta.rosetta.RosettaLiteral
import com.regnosys.rosetta.rosetta.RosettaMetaType
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.rosetta.RosettaParenthesisCalcExpression
import com.regnosys.rosetta.rosetta.RosettaRegularAttribute
import com.regnosys.rosetta.rosetta.RosettaStringLiteral
import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.rosetta.RosettaWhenPresentExpression
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.EmptyLiteral
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.simple.ListLiteral
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration
import com.regnosys.rosetta.types.RosettaOperators
import com.regnosys.rosetta.types.RosettaTypeProvider
import com.regnosys.rosetta.utils.ExpressionHelper
import com.rosetta.model.lib.functions.MapperC
import com.rosetta.model.lib.functions.MapperMaths
import com.rosetta.model.lib.functions.MapperS
import com.rosetta.model.lib.functions.MapperTree
import com.rosetta.model.lib.meta.FieldWithMeta
import com.rosetta.model.lib.validation.ValidatorHelper
import java.math.BigDecimal
import java.util.HashMap
import org.eclipse.xtend.lib.annotations.Data
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.util.Wrapper

import static extension com.regnosys.rosetta.generator.java.enums.EnumHelper.convertValues
import static extension com.regnosys.rosetta.generator.java.util.JavaClassTranslator.toJavaType
import static extension com.regnosys.rosetta.generator.util.RosettaAttributeExtensions.cardinalityIsListValue

class ExpressionGenerator {
	
	@Inject protected RosettaTypeProvider typeProvider
	@Inject RosettaOperators operators
	@Inject CardinalityProvider cardinalityProvider
	@Inject JavaNames.Factory factory 
	@Inject RosettaFunctionExtensions funcExt
	@Inject extension RosettaExtensions
	@Inject extension ImportManagerExtension
	@Inject ExpressionHelper exprHelper
	
	def StringConcatenationClient javaCode(RosettaExpression expr, ParamMap params) {
		expr.javaCode(params, true);
	}
	
	/**
	 * convert a rosetta expression to code
	 * ParamMpa params  - a map keyed by classname or positional index that provides variable names for expression parameters
	 */
	def StringConcatenationClient javaCode(RosettaExpression expr, ParamMap params, boolean isLast) {
		switch (expr) {
			RosettaGroupByFeatureCall : {
				var autoValue = true //if the attribute being referenced is WithMeta and we aren't accessing the meta fields then access the value by default
				if (expr.eContainer!==null && expr.eContainer instanceof RosettaFeatureCall && (expr.eContainer as RosettaFeatureCall).feature instanceof RosettaMetaType) {
					autoValue=false;
				}
				groupByFeatureCall(expr, params, isLast, autoValue)
			}
			RosettaFeatureCall : {
				var autoValue = true //if the attribute being referenced is WithMeta and we aren't accessing the meta fields then access the value by default
				if (expr.eContainer!==null && expr.eContainer instanceof RosettaFeatureCall && (expr.eContainer as RosettaFeatureCall).feature instanceof RosettaMetaType) {
					autoValue=false;
				}
				featureCall(expr, params, isLast, autoValue)
			}
			RosettaExistsExpression : {
				existsExpr(expr, expr.argument, params)
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
			RosettaWhenPresentExpression : {
				whenPresentExpr(expr, expr.left, params)
			}
			RosettaCallableCall : {
				callableCall(expr, params) 
			}
			RosettaCallableWithArgsCall: {
				callableWithArgs(expr, params)
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
				'''«importMethod(ValidatorHelper,"doIf")»(«expr.^if.javaCode(params)»,«expr.ifthen.javaCode(params)»«IF expr.elsethen !== null»,«expr.elsethen.javaCode(params)»«ENDIF»)'''
			}
			RosettaContainsExpression : {
				'''«importMethod(ValidatorHelper,"contains")»(«expr.container.javaCode(params)», «expr.contained.javaCode(params)»)'''
			}
			RosettaParenthesisCalcExpression : {
				expr.expression.javaCode(params, isLast)
			}
			EmptyLiteral : {
				'''null'''
			}
			ListLiteral : {
				'''«MapperC».of(«FOR ele: expr.elements SEPARATOR ', '»«ele.javaCode(params)»«ENDFOR»)'''
			}
			default: 
				throw new UnsupportedOperationException("Unsupported expression type of " + expr?.class?.simpleName)
		}
	}
	/**
	 * feature call is a call to get an attribute of an object e.g. Quote->amount
	 */
	def StringConcatenationClient groupByFeatureCall(RosettaGroupByFeatureCall groupByCall, ParamMap params, boolean isLast, boolean autoValue) {
		val call = groupByCall.call
		switch(call) {
			RosettaFeatureCall: {
				val feature = call.feature
				val groupByFeature = groupByCall.groupBy
				val StringConcatenationClient right =
				switch (feature) {
					RosettaRegularAttribute: {
						'''«feature.buildMapFunc(isLast, autoValue)»«IF groupByFeature!==null»«buildGroupBy(groupByFeature, isLast)»«ENDIF»'''
					}
					Attribute: {
						'''«feature.buildMapFunc(isLast, autoValue)»«IF groupByFeature!==null»«buildGroupBy(groupByFeature, isLast)»«ENDIF»'''
					}
					RosettaMetaType: {
						'''«feature.buildMapFunc(isLast)»«IF groupByFeature!==null»«buildGroupBy(groupByFeature, isLast)»«ENDIF»'''
					}
					default: 
						throw new UnsupportedOperationException("Unsupported expression type of "+feature.class.simpleName)
				}
				'''«javaCode(call.receiver, params, false)»«right»'''
			}
			default: {
				javaCode(groupByCall.call, params)
			}
		}
	}
	
	def StringConcatenationClient callableWithArgs(RosettaCallableWithArgsCall expr, ParamMap params) {
		val callable = expr.callable
		
		return switch (callable) {
			Function: {
				funcExt.getOutput(callable).card.isMany
				'''«MapperS».of(«callable.name.toFirstLower».evaluate(«args(expr, params)»))'''
			}
			RosettaExternalFunction:
				'''«MapperS».of(new «new RosettaJavaPackages(null).libFunctions.javaType(callable.name)»().execute(«args(expr, params)»))'''
			default: 
				throw new UnsupportedOperationException("Unsupported callable with args type of " + expr.eClass.name)
		}
		
	}
	
	private def StringConcatenationClient args(RosettaCallableWithArgsCall expr, ParamMap params) {
		'''«FOR arg : expr.args SEPARATOR ', '»«arg.javaCode(params)»«IF !(arg instanceof EmptyLiteral)»«IF cardinalityProvider.isMulti(arg)».getMulti()«ELSE».get()«ENDIF»«ENDIF»«ENDFOR»'''
	}
	
	def StringConcatenationClient existsExpr(RosettaExistsExpression exists, RosettaExpression argument, ParamMap params) {
		val arg = getAliasExpressionIfPresent(argument)
		
		if (arg instanceof RosettaBinaryOperation) {
			if(arg.isLogicalOperation)
				doExistsExpr(exists, arg.binaryExpr(exists, params))
			else 
				//if the argument is a binary expression then the exists needs to be pushed down into it
				arg.binaryExpr(exists, params)
		}
		else {
			doExistsExpr(exists, arg.javaCode(params))
		}
	}
	
	private def StringConcatenationClient doExistsExpr(RosettaExistsExpression exists, StringConcatenationClient arg) {
		if(exists.single)
			'''«importMethod(ValidatorHelper,"singleExists")»(«arg», «exists.only»)'''
		else if(exists.multiple)
			'''«importMethod(ValidatorHelper,"multipleExists")»(«arg», «exists.only»)'''
		else
			'''«importMethod(ValidatorHelper,"exists")»(«arg», «exists.only»)'''
	}
	
	def StringConcatenationClient absentExpr(RosettaAbsentExpression notSet, RosettaExpression argument, ParamMap params) {
		val arg = getAliasExpressionIfPresent(argument)
		
		if (arg instanceof RosettaBinaryOperation) {
			if(arg.isLogicalOperation)
				'''«importMethod(ValidatorHelper,"notExists")»(«arg.binaryExpr(notSet, params)»)'''
			else
				//if the arg is binary then the operator needs to be pushed down
				arg.binaryExpr(notSet, params)
		}
		else {
			'''«importMethod(ValidatorHelper,"notExists")»(«arg.javaCode(params)»)'''
		}
	}
	
	protected def StringConcatenationClient callableCall(RosettaCallableCall expr, ParamMap params) {
		val call = expr.callable
		switch (call)  {
			RosettaClass : {
				'''«MapperS».of(«params.getClass(call)»)'''
			}
			com.regnosys.rosetta.rosetta.simple.Data : {
				'''«MapperS».of(«params.getClass(call)»)'''
			}
			RosettaAlias : {
				call.expression.javaCode(params)
			}
			Attribute : {
				'''«if (call.card.isIsMany) MapperC else MapperS».of(«call.name»)'''
			}
			ShortcutDeclaration : {
				'''«MapperS».of(«call.name»(«aliasCallArgs(call)»).«IF exprHelper.usesOutputParameter(call.expression)»build()«ELSE»get()«ENDIF»)'''
			}
			default: 
				throw new UnsupportedOperationException("Unsupported callable type of "+call.class.simpleName)
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
	def StringConcatenationClient featureCall(RosettaFeatureCall call, ParamMap params, boolean isLast, boolean autoValue) {
		val feature = call.feature
		val StringConcatenationClient right = switch (feature) {
			RosettaRegularAttribute:
				feature.buildMapFunc(isLast, autoValue)
			Attribute:
				feature.buildMapFunc(isLast, autoValue)
			RosettaFeature: 
				'''.map("get«feature.name.toFirstUpper»", «EcoreUtil2.getContainerOfType(feature, RosettaType).toJavaType»::get«feature.name.toFirstUpper»)'''
			default:
				throw new UnsupportedOperationException("Unsupported expression type of " + feature.eClass.name)
		}
		'''«javaCode(call.receiver, params, false)»«right»'''
	}
	
	def StringConcatenationClient countExpr(RosettaCountOperation expr, RosettaExpression test, ParamMap params) {
		toComparisonOp('''«MapperS».of(«expr.left.javaCode(params)».resultCount())''',  expr.operator, expr.right.javaCode(params))
	}
	
	def StringConcatenationClient whenPresentExpr(RosettaWhenPresentExpression expr, RosettaExpression left, ParamMap params) {
		'''doWhenPresent(«left.javaCode(params)», «toComparisonOp(expr.left.javaCode(params), expr.operator, expr.right.javaCode(params))»)'''
	}
	
	def StringConcatenationClient binaryExpr(RosettaBinaryOperation expr, RosettaExpression test, ParamMap params) {
		val left = getAliasExpressionIfPresent(expr.left)
		val right = getAliasExpressionIfPresent(expr.right)

		switch expr.operator {
			case ("and"): {
				if (containsFeatureCallOrCallableCall(left)) {
					// Mappers
					if(isComparableTypes(expr))
						'''«MapperTree».and(«left.booleanize(test, params)», «right.booleanize(test, params)»)'''
					else
						'''«MapperTree».andDifferent(«left.booleanize(test, params)», «right.booleanize(test, params)»)'''
				}
				else {
					// ComparisonResults
					'''«left.javaCode(params)».and(«right.javaCode(params)»)'''
				}	
			}
			case ("or"): {
				if (containsFeatureCallOrCallableCall(left)) {
					// Mappers
					if(isComparableTypes(expr))
						'''«MapperTree».or(«left.booleanize(test, params)», «right.booleanize(test, params)»)'''
					else
						'''«MapperTree».orDifferent(«left.booleanize(test, params)», «right.booleanize(test, params)»)'''
				}
				else {
					// ComparisonResult
					'''«left.javaCode(params)».or(«right.javaCode(params)»)'''
				}
			}
			case ("+"): {
				val leftRtype = typeProvider.getRType(expr.left)
				val rightRtype = typeProvider.getRType(expr.right)
				val commontype = operators.resultType(expr.operator, leftRtype,rightRtype)
				val leftType = '''«leftRtype.name.toJavaType»'''
				val rightType = '''«rightRtype.name.toJavaType»'''
				'''«MapperMaths».<«commontype.name.toJavaType», «leftType», «rightType»>add(«expr.left.javaCode(params)», «expr.right.javaCode(params)»)'''
			}
			case ("-"): {
				val leftRtype = typeProvider.getRType(expr.left)
				val rightRtype = typeProvider.getRType(expr.right)
				val commontype = operators.resultType(expr.operator, leftRtype,rightRtype)
				val leftType = '''«typeProvider.getRType(expr.left).name.toJavaType»'''
				val rightType = '''«typeProvider.getRType(expr.right).name.toJavaType»'''
				'''«MapperMaths».<«commontype.name.toJavaType», «leftType», «rightType»>subtract(«expr.left.javaCode(params)», «expr.right.javaCode(params)»)'''
			}
			case ("*"): {
				val leftRtype = typeProvider.getRType(expr.left)
				val rightRtype = typeProvider.getRType(expr.right)
				val commontype = operators.resultType(expr.operator, leftRtype,rightRtype)
				val leftType = '''«typeProvider.getRType(expr.left).name.toJavaType»'''
				val rightType = '''«typeProvider.getRType(expr.right).name.toJavaType»'''
				'''«MapperMaths».<«commontype.name.toJavaType», «leftType», «rightType»>multiply(«expr.left.javaCode(params)», «expr.right.javaCode(params)»)'''
			}
			case ("/"): {
				val leftRtype = typeProvider.getRType(expr.left)
				val rightRtype = typeProvider.getRType(expr.right)
				val commontype = operators.resultType(expr.operator, leftRtype,rightRtype)
				val leftType = '''«typeProvider.getRType(expr.left).name.toJavaType»'''
				val rightType = '''«typeProvider.getRType(expr.right).name.toJavaType»'''
				'''«MapperMaths».<«commontype.name.toJavaType», «leftType», «rightType»>divide(«expr.left.javaCode(params)», «expr.right.javaCode(params)»)'''
			}
			default: {
				// FIXME isProduct isEvent stuff in QualifyFunctionGenerator. Should be removed after alias migration
				if(left.needsMapperTree && !right.needsMapperTree) {
					toComparisonOp('''«expr.left.javaCode(params)»''', expr.operator, '''«toMapperTree(expr.right.javaCode(params))»''')
				} else if(!left.needsMapperTree && right.needsMapperTree) {
					toComparisonOp('''«toMapperTree(expr.left.javaCode(params))»''', expr.operator, '''«expr.right.javaCode(params)»''')
				} else {
					toComparisonOp('''«expr.left.javaCode(params)»''', expr.operator, '''«expr.right.javaCode(params)»''')
				}
			}
		}
	}
	
	private def boolean needsMapperTree(RosettaExpression expr) {
		if (expr instanceof RosettaGroupByFeatureCall) {
			val call = expr.call
			switch (call) {
				RosettaCallableCall: {
					val callable = call.callable
					if (callable instanceof RosettaAlias) {
						return callable.expression.isLogicalOperation
					}
				}
				RosettaBinaryOperation: return call.isLogicalOperation
			}
		}
		return expr.isLogicalOperation
	}
	
	private def boolean isLogicalOperation(RosettaExpression expr) {
		if(expr instanceof RosettaBinaryOperation) return expr.operator == "and" || expr.operator == "or"
		return false
	}
	
	/**
	 * Inspect expression and return alias expression if present.  Currently, nested aliases are not supported.
	 */
	protected def getAliasExpressionIfPresent(RosettaExpression expr) {
		if (expr instanceof RosettaCallableCall) {
			val callable = expr.callable
			if(callable instanceof RosettaAlias) {
				return callable.expression
			}
		}
		return expr
	}
	
	/**
	 * Collects all expressions down the tree, and checks that they're all either FeatureCalls or CallableCalls
	 */
	protected def boolean containsFeatureCallOrCallableCall(RosettaExpression expr) {
		val exprs = newHashSet
		val extensions = new RosettaExtensions
		extensions.collectExpressions(expr, [exprs.add(it)])

		return !exprs.empty && exprs.stream.allMatch[it instanceof RosettaGroupByFeatureCall || it instanceof RosettaFeatureCall || it instanceof RosettaCallableCall]
	}
	
	/**
	 * Search leaf node objects to determine whether this is a comparison of matching objects types
	 */
	protected def isComparableTypes(RosettaBinaryOperation binaryExpr) {
		// get list of the object type at each leaf node
		val rosettaTypes = newHashSet
		val extensions = new RosettaExtensions
		extensions.collectLeafTypes(binaryExpr, [rosettaTypes.add(it)])
		
		// check whether they're all the same type
		val type = rosettaTypes.stream.findAny
		return type.isPresent && rosettaTypes.stream.allMatch[it.equals(type.get)]
	}
		
	private def StringConcatenationClient toComparisonOp(StringConcatenationClient left, String operator, StringConcatenationClient right) {
		switch operator {
			case ("="):
				'''«importMethod(ValidatorHelper, "areEqual")»(«left», «right»)'''
			case ("<>"):
				'''«importMethod(ValidatorHelper,"notEqual")»(«left», «right»)'''
			case ("<") : 
				'''«importMethod(ValidatorHelper,"lessThan")»(«left», «right»)'''
			case ("<=") : 
				'''«importMethod(ValidatorHelper,"lessThanEquals")»(«left», «right»)'''
			case (">") : 
				'''«importMethod(ValidatorHelper,"greaterThan")»(«left», «right»)'''
			case (">=") : 
				'''«importMethod(ValidatorHelper,"greaterThanEquals")»(«left», «right»)'''
			default: 
				throw new UnsupportedOperationException("Unsupported binary operation of " + operator)
		}
	}
	
	/**
	 * converts an expression into a boolean result using the test expression pushed down (see exists etc)
	 */	
	def StringConcatenationClient booleanize(RosettaExpression expr, RosettaExpression test, ParamMap params) {
		switch (expr) {
			RosettaBinaryOperation : {
				binaryExpr(expr, test, params)
			}
			default : {
				 switch (test) {
				 	RosettaExistsExpression: {
						expr.javaCode(params).toMapperTree
					}
					RosettaAbsentExpression: {
						expr.javaCode(params).toMapperTree
					}
					case null : {
						expr.javaCode(params).toMapperTree
					}
					default:
						throw new UnsupportedOperationException(
							"Unsupported expression type of " + test.class.simpleName)
				}
			}
		}
	}
	
	/**
	 * Builds the expression of mapping functions to extract a path of attributes
	 */
	def StringConcatenationClient buildMapFunc(RosettaRegularAttribute attribute, boolean isLast, boolean autoValue) {
		val mapFunc = attribute.buildMapFuncAttribute
		if (attribute.cardinalityIsListValue) {
			if (attribute.metaTypes===null || attribute.metaTypes.isEmpty)
				'''.<«attribute.type.toJavaType»>mapC(«mapFunc»)'''
			else if (!autoValue) {
				'''.<«attribute.metaClass»>mapC(«mapFunc»)'''
			}
			else {
				'''.mapC(«mapFunc»).<«attribute.type.toJavaType»>map("getValue", «FieldWithMeta»::getValue)'''
			}
		}
		else
		{
			if (attribute.metaTypes===null || attribute.metaTypes.isEmpty){
				if(attribute.type instanceof RosettaClass) 
				'''.<«attribute.type.toJavaType»>map(«mapFunc»)'''
				else
				'''.<«attribute.type.toJavaType»>map(«mapFunc»)'''
			}
			else if (!autoValue) {
				'''.<«attribute.metaClass»>map(«mapFunc»)'''
			}
			else
				'''.map(«mapFunc»).<«attribute.type.toJavaType»>map("getValue", «FieldWithMeta»::getValue)'''
		}
	}
	/**
	 * Builds the expression of mapping functions to extract a path of attributes
	 */
	def StringConcatenationClient buildMapFunc(Attribute attribute, boolean isLast, boolean autoValue) {
		val mapFunc = attribute.buildMapFuncAttribute
		if (attribute.card.isIsMany) {
			if (attribute.metaAnnotations.nullOrEmpty)
				'''.<«attribute.type.toJavaType»>mapC(«mapFunc»)'''
			else if (!autoValue) {
				'''.<«attribute.metaClass»>mapC(«mapFunc»)'''
			}
			else {
				'''.mapC(«mapFunc»).<«attribute.type.toJavaType»>map("getValue", «FieldWithMeta»::getValue)'''
			}
		}
		else
		{
			if (attribute.metaAnnotations.nullOrEmpty){
				if(attribute.type instanceof RosettaClass) 
				'''.<«attribute.type.toJavaType»>map(«mapFunc»)'''
				else
				'''.<«attribute.type.toJavaType»>map(«mapFunc»)'''
			}
			else if (!autoValue) {
				'''.<«attribute.metaClass»>map(«mapFunc»)'''
			}
			else
				'''.map(«mapFunc»).<«attribute.type.toJavaType»>map("getValue", «FieldWithMeta»::getValue)'''
		}
	}
	
	private def JavaType toJavaType(RosettaType rosType) {
		val model = EcoreUtil2.getContainerOfType(rosType, RosettaModel)
		if (model !== null)
			factory.create(model).toJavaType(rosType)
		else
			JavaType.create(rosType.name.toJavaType)
	}
	private def JavaType toJavaType(com.regnosys.rosetta.rosetta.simple.Data ele) {
		val model = EcoreUtil2.getContainerOfType(ele, RosettaModel)
		if (model !== null)
			factory.create(model).toJavaType(ele)
		else
			JavaType.create(ele.name.toJavaType)
	}
	
	def static metaClass(RosettaRegularAttribute attribute) {
		if (attribute.metaTypes.exists[m|m.name=="reference"]) "ReferenceWithMeta"+attribute.type.name.toJavaType.toFirstUpper
		else "FieldWithMeta"+attribute.type.name.toJavaType.toFirstUpper
	}
	def static metaClass(Attribute attribute) {
		if (attribute.annotations.exists[a|a.annotation?.name=="metadata" && a.attribute?.name=="reference"]) "ReferenceWithMeta"+attribute.type.name.toJavaType.toFirstUpper
		else "FieldWithMeta"+attribute.type.name.toJavaType.toFirstUpper
	}
	
	def static StringConcatenationClient buildMapFunc(RosettaMetaType meta, boolean isLast) {
		if (meta.name=="reference") {
			'''.map("get«meta.name.toFirstUpper»", a->a.getGlobalReference())'''
		}
		else {
			'''.map("getMeta", a->a.getMeta()).map("get«meta.name.toFirstUpper»", a->a.get«meta.name.toFirstUpper»())'''
		}
	}

	def StringConcatenationClient buildGroupBy(RosettaGroupByExpression expression, boolean isLast) {
		val exprs = newArrayList
		val expr = Wrapper.wrap(expression)
		exprs.add(expr.get)
		while (expr.get.right!==null) {
			expr.set(expr.get.right)
			exprs.add(expr.get)
		}
		'''.<«expr.get.attribute.type.name.toJavaType»>groupBy(g->new «MapperS»<>(g)«FOR ex:exprs»«buildMapFunc(ex.attribute, isLast, true)»«ENDFOR»)'''
	}
	
	private def StringConcatenationClient buildMapFuncAttribute(RosettaRegularAttribute attribute)
		'''"get«attribute.name.toFirstUpper»", «(attribute.eContainer as RosettaClass).toJavaType»::get«attribute.name.toFirstUpper»'''

	private def StringConcatenationClient buildMapFuncAttribute(Attribute attribute) {
		if(attribute.eContainer instanceof com.regnosys.rosetta.rosetta.simple.Data)
		'''"get«attribute.name.toFirstUpper»", «(attribute.eContainer as com.regnosys.rosetta.rosetta.simple.Data).toJavaType»::get«attribute.name.toFirstUpper»'''
	}

	/**
	 * The id for a parameter - either a Class name or a positional index
	 */
	@Data static class ParamID {
		RosettaType c
		int index
		String name;
	}
	
	//Class mapping from class name or positional index to the name of a variable defined in the containing code
	static class ParamMap extends HashMap<ParamID, String> {
		new(RosettaType c) {
			if (null !== c)
				put(new ParamID(c,-1, null), c.name.toFirstLower);
		}
		
		new(RosettaType c, String name) {
			put(new ParamID(c,-1, null), name);
		}
		
		new(){
		}
		
		def getClass(RosettaType c) {
			return get(new ParamID(c, -1, null));
		}
	}
	
	/**
	 * Create a string representation of a rossetta function  
	 * mainly used to give human readable names to the mapping functions used to extract attributes
	 */
	def StringConcatenationClient toNodeLabel(RosettaExpression expr) {
		switch (expr) {
			RosettaGroupByFeatureCall : {
				toNodeLabel(expr.call)
			}
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
			RosettaLiteral : {
				'''«expr.stringValue»'''
			}
			RosettaCountOperation : {
				'''«toNodeLabel(expr.left)» count = «toNodeLabel(expr.right)»'''
			}

			default :
				throw new UnsupportedOperationException("Unsupported expression type of "+expr.class.simpleName)
		}
	}
	
	def StringConcatenationClient toNodeLabel(RosettaFeatureCall call) {
		val feature = call.feature
		val right = switch feature {
			RosettaRegularAttribute, RosettaMetaType, Attribute: feature.name
			default: throw new UnsupportedOperationException("Unsupported expression type "+feature.getClass)
		}
		
		val receiver = call.receiver
		val left = switch receiver {
			RosettaCallableCall: '''''' //(receiver.callable as RosettaClass).name
			RosettaFeatureCall: toNodeLabel(receiver)
			default: throw new UnsupportedOperationException("Unsupported expression type")
		}
		
		'''«left»->«right»'''
	}
	
	def StringConcatenationClient toNodeLabel(RosettaBinaryOperation binOp) {
		'''«binOp.left.toNodeLabel»«binOp.operator»«binOp.right.toNodeLabel»'''
	}
	
	private def StringConcatenationClient toMapperTree(StringConcatenationClient code) {
		return '''«MapperTree».of(«code»)'''
	}
	
}
