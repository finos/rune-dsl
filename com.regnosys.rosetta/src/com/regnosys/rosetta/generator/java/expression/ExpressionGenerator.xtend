package com.regnosys.rosetta.generator.java.expression

import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
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
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgsCall
import com.regnosys.rosetta.rosetta.RosettaConditionalExpression
import com.regnosys.rosetta.rosetta.RosettaContainsExpression
import com.regnosys.rosetta.rosetta.RosettaCountOperation
import com.regnosys.rosetta.rosetta.RosettaDisjointExpression
import com.regnosys.rosetta.rosetta.RosettaEnumValue
import com.regnosys.rosetta.rosetta.RosettaEnumValueReference
import com.regnosys.rosetta.rosetta.RosettaEnumeration
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
import com.regnosys.rosetta.rosetta.RosettaStringLiteral
import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.simple.EmptyLiteral
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.simple.ListLiteral
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration
import com.regnosys.rosetta.types.RosettaOperators
import com.regnosys.rosetta.types.RosettaTypeProvider
import com.regnosys.rosetta.utils.ExpressionHelper
import com.rosetta.model.lib.mapper.MapperC
import com.rosetta.model.lib.expression.MapperMaths
import com.rosetta.model.lib.mapper.MapperS
import com.rosetta.model.lib.mapper.MapperTree
import com.rosetta.model.lib.expression.ExpressionOperators
import java.math.BigDecimal
import java.util.HashMap
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.util.Wrapper

import static extension com.regnosys.rosetta.generator.java.enums.EnumHelper.convertValues
import static extension com.regnosys.rosetta.generator.java.util.JavaClassTranslator.toJavaType
import static extension com.regnosys.rosetta.generator.java.util.JavaClassTranslator.toJavaClass
import org.eclipse.xtend.lib.annotations.Accessors
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor
import com.regnosys.rosetta.generator.util.RosettaAttributeExtensions

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
				'''«importMethod(ExpressionOperators, expr.doIfName)»(«expr.^if.javaCode(params)», ()->«expr.ifthen.javaCode(params)»«IF expr.elsethen !== null», ()->«expr.elsethen.javaCode(params)»«ENDIF»)'''
			}
			RosettaContainsExpression : {
				'''«importMethod(ExpressionOperators,"contains")»(«expr.container.javaCode(params)», «expr.contained.javaCode(params)»)'''
			}
			RosettaDisjointExpression : {
				'''«importMethod(ExpressionOperators,"disjoint")»(«expr.container.javaCode(params)», «expr.disjoint.javaCode(params)»)'''
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
	
	private def String doIfName(RosettaConditionalExpression expr) {
		if (expr.ifthen.evalulatesToMapper) "doIf"
		else "resultDoIf"
	}
	
	/**
	 * group by only occurs in alias expression and should be deprecated
	 */
	private def StringConcatenationClient groupByFeatureCall(RosettaGroupByFeatureCall groupByCall, ParamMap params, boolean isLast, boolean autoValue) {
		val call = groupByCall.call
		switch(call) {
			RosettaFeatureCall: {
				val feature = call.feature
				val groupByFeature = groupByCall.groupBy
				val StringConcatenationClient right =
				switch (feature) {
					Attribute: {
						'''«feature.buildMapFunc(isLast, autoValue)»«IF groupByFeature!==null»«buildGroupBy(groupByFeature, isLast)»«ENDIF»'''
					}
					RosettaMetaType: {
						'''«feature.buildMapFunc(isLast)»«IF groupByFeature!==null»«buildGroupBy(groupByFeature, isLast)»«ENDIF»'''
					}
					RosettaEnumValue: 
						return '''«MapperS».of(«feature.enumeration.toJavaType».«feature.convertValues»)'''
					default: 
						throw new UnsupportedOperationException("Unsupported expression type of "+feature.class.simpleName)
				}
				'''«MapperC».of(«javaCode(call.receiver, params, false)»«right».getMulti())'''
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
				val multi = funcExt.getOutput(callable).card.isMany
				val implicitArg = funcExt.implicitFirstArgument(expr)
				'''«IF multi»«MapperC»«ELSE»«MapperS»«ENDIF».of(«callable.name.toFirstLower».evaluate(«IF implicitArg !== null»«implicitArg.name.toFirstLower»«ENDIF»«args(expr, params)»))'''
			}
			RosettaExternalFunction:
				'''«MapperS».of(new «factory.create(callable.model).toJavaType(callable as RosettaCallableWithArgs)»().execute(«args(expr, params)»))'''
			default: 
				throw new UnsupportedOperationException("Unsupported callable with args type of " + expr.eClass.name)
		}
		
	}
	
	private def StringConcatenationClient args(RosettaCallableWithArgsCall expr, ParamMap params) {
		'''«FOR arg : expr.args SEPARATOR ', '»«arg.javaCode(params)»«IF !(arg instanceof EmptyLiteral)»«IF cardinalityProvider.isMulti(arg)».getMulti()«ELSE».get()«ENDIF»«ENDIF»«ENDFOR»'''
	}
	
	def StringConcatenationClient existsExpr(RosettaExistsExpression exists, RosettaExpression argument, ParamMap params) {
		val arg = getAliasExpressionIfPresent(argument)
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
			RosettaParenthesisCalcExpression: expression.expression.findBinaryOperation
			default: null
		}
	}
	
	private def StringConcatenationClient doExistsExpr(RosettaExistsExpression exists, StringConcatenationClient arg) {
		if(exists.single)
			'''singleExists(«arg», «exists.only»)'''
		else if(exists.multiple)
			'''multipleExists(«arg», «exists.only»)'''
		else 
			'''exists(«arg», «exists.only»)'''
	}
	
	def StringConcatenationClient absentExpr(RosettaAbsentExpression notSet, RosettaExpression argument, ParamMap params) {
		val arg = getAliasExpressionIfPresent(argument)
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
		val call = expr.callable
		switch (call)  {
			Data : {
				'''«MapperS».of(«params.getClass(call)»)'''
			}
			RosettaAlias : {
				call.expression.javaCode(params)
			}
			Attribute : {
				// Data Attributes can only be called from their conditions
				// The current container (Data) is stored in Params, but we need also look for superTypes
				// so we could also do: (call.eContainer as Data).allSuperTypes.map[it|params.getClass(it)].filterNull.head
				if(call.eContainer instanceof Data)
				'''«MapperS».of(«EcoreUtil2.getContainerOfType(expr, Data).getName.toFirstLower»)«buildMapFunc(call, false, true)»'''
				else
				'''«if (call.card.isIsMany) MapperC else MapperS».of(«call.name»)'''
			}
			ShortcutDeclaration : {
				'''«MapperS».of(«call.name»(«aliasCallArgs(call)»).«IF exprHelper.usesOutputParameter(call.expression)»build()«ELSE»get()«ENDIF»)'''
			}
			RosettaEnumeration: '''«call.toJavaType»'''
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
	private def StringConcatenationClient featureCall(RosettaFeatureCall call, ParamMap params, boolean isLast, boolean autoValue) {
		val feature = call.feature
		val StringConcatenationClient right = switch (feature) {
			Attribute:
				feature.buildMapFunc(isLast, autoValue)
			RosettaMetaType: 
				'''«feature.buildMapFunc(isLast)»'''
			RosettaEnumValue: 
				return '''«MapperS».of(«feature.enumeration.toJavaType».«feature.convertValues»)'''
			RosettaFeature: 
				'''.map("get«feature.name.toFirstUpper»", «feature.containerType.toJavaType»::get«feature.name.toFirstUpper»)'''
			default:
				throw new UnsupportedOperationException("Unsupported expression type of " + feature.eClass.name)
		}
		if(call.toOne)
			return '''«MapperS».of(«javaCode(call.receiver, params, false)»«right».get())'''
		else
			return '''«javaCode(call.receiver, params, false)»«right»'''
	}
	
	def private RosettaType containerType(RosettaFeature feature) {
		EcoreUtil2.getContainerOfType(feature, RosettaType)
	}
	
	def StringConcatenationClient countExpr(RosettaCountOperation expr, RosettaExpression test, ParamMap params) {
		'''«MapperS».of(«expr.argument.javaCode(params)».resultCount())'''
	}
	
	def StringConcatenationClient binaryExpr(RosettaBinaryOperation expr, RosettaExpression test, ParamMap params) {
		val left = getAliasExpressionIfPresent(expr.left)
		val right = getAliasExpressionIfPresent(expr.right)
		val leftRtype = typeProvider.getRType(expr.left)
		val rightRtype = typeProvider.getRType(expr.right)
		val resultType = operators.resultType(expr.operator, leftRtype,rightRtype)
		val leftType = '''«leftRtype.name.toJavaType»'''
		val rightType = '''«rightRtype.name.toJavaType»'''
		
		switch expr.operator {
			case ("and"): {
				if (evalulatesToMapper(left)) {
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
				if (evalulatesToMapper(left)) {
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
	private def getAliasExpressionIfPresent(RosettaExpression expr) {
		if (expr instanceof RosettaCallableCall) {
			val callable = expr.callable
			if(callable instanceof RosettaAlias) {
				return callable.expression
			}
		}
		return expr
	}
	
	/**
	 * Collects all expressions down the tree, and checks that they're all either FeatureCalls or CallableCalls (or anything that resolves to a Mapper)
	 */
	private def boolean evalulatesToMapper(RosettaExpression expr) {
		val exprs = newHashSet
		collectExpressions(expr, [exprs.add(it)])

		return !exprs.empty && 
			exprs.stream.allMatch[it instanceof RosettaGroupByFeatureCall || it instanceof RosettaFeatureCall || it instanceof RosettaCallableCall || it instanceof RosettaFeatureCall || it instanceof RosettaCallableWithArgsCall || it instanceof RosettaLiteral]
	}
	
	/**
	 * Search leaf node objects to determine whether this is a comparison of matching objects types
	 */
	private def isComparableTypes(RosettaBinaryOperation binaryExpr) {
		// get list of the object type at each leaf node
		val rosettaTypes = newHashSet
		collectLeafTypes(binaryExpr, [rosettaTypes.add(it)])
		
		// check whether they're all the same type
		val type = rosettaTypes.stream.findAny
		return type.isPresent && rosettaTypes.stream.allMatch[it.equals(type.get)]
	}
		
	private def StringConcatenationClient toComparisonOp(StringConcatenationClient left, String operator, StringConcatenationClient right) {
		switch operator {
			case ("="):
				'''«importWildCard(ExpressionOperators)»areEqual(«left», «right»)'''
			case ("<>"):
				'''«importWildCard(ExpressionOperators)»notEqual(«left», «right»)'''
			case ("<") : 
				'''«importWildCard(ExpressionOperators)»lessThan(«left», «right»)'''
			case ("<=") : 
				'''«importWildCard(ExpressionOperators)»lessThanEquals(«left», «right»)'''
			case (">") : 
				'''«importWildCard(ExpressionOperators)»greaterThan(«left», «right»)'''
			case (">=") : 
				'''«importWildCard(ExpressionOperators)»greaterThanEquals(«left», «right»)'''
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
	def StringConcatenationClient buildMapFunc(Attribute attribute, boolean isLast, boolean autoValue) {
		val mapFunc = attribute.buildMapFuncAttribute
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
			throw new IllegalArgumentException('''Can not create type reference. «rosType.eClass?.name» «rosType.name» is not attached to a «RosettaModel.simpleName»''')
		factory.create(model).toJavaType(rosType)
	}
	
	private def javaNames(Attribute attr) {
		val model = EcoreUtil2.getContainerOfType(attr, RosettaModel)
		if(model === null)
			throw new IllegalArgumentException('''Can not create type reference. «attr.eClass?.name» «attr.name» is not attached to a «RosettaModel.simpleName»''')
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
		'''.<«expr.get.attribute.type.name.toJavaClass»>groupBy(g->new «MapperS»<>(g)«FOR ex:exprs»«buildMapFunc(ex.attribute as Attribute, isLast, true)»«ENDFOR»)'''
	}
	
	private def StringConcatenationClient buildMapFuncAttribute(Attribute attribute) {
		if(attribute.eContainer instanceof Data) 
			'''"get«attribute.name.toFirstUpper»", «attribute.attributeTypeVariableName» -> «IF attribute.override»(«attribute.type.toJavaType») «ENDIF»«attribute.attributeTypeVariableName».get«attribute.name.toFirstUpper»()'''
	}

	private def attributeTypeVariableName(Attribute attribute) 
		'''_«(attribute.eContainer as Data).toJavaType.simpleName.toFirstLower»'''
	
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
				put(new ParamID(c,-1, null), c.name.toFirstLower);
		}
		
		new(RosettaType c, String name) {
			put(new ParamID(c,-1, null), name);
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
			RosettaContainsExpression : {
				'''«expr.container.toNodeLabel» contains «expr.contained.toNodeLabel»'''
			}
			RosettaParenthesisCalcExpression : {
				'''(«expr.expression.toNodeLabel»)'''
			}
			RosettaCallableWithArgsCall :{
				'''«expr.callable.name»(«FOR arg:expr.args SEPARATOR ", "»«arg.toNodeLabel»«ENDFOR»)'''
			}

			default :
				'''Unsupported expression type of «expr.class.simpleName»'''
		}
	}
	
	def StringConcatenationClient toNodeLabel(RosettaFeatureCall call) {
		val feature = call.feature
		val right = switch feature {
			RosettaMetaType, Attribute, RosettaEnumValue: feature.name
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

@Accessors
@FinalFieldsConstructor
class Context {
	final JavaNames names
	boolean inFunctionCall
	static def create(JavaNames names) {
		new Context(names)
	}
}
