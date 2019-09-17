package com.regnosys.rosetta.generator.java.expression

import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.util.JavaNames
import com.regnosys.rosetta.generator.java.util.JavaType
import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions
import com.regnosys.rosetta.rosetta.RosettaAbsentExpression
import com.regnosys.rosetta.rosetta.RosettaAlias
import com.regnosys.rosetta.rosetta.RosettaArgumentFeature
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
import com.regnosys.rosetta.rosetta.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.RosettaFunction
import com.regnosys.rosetta.rosetta.RosettaFunctionInput
import com.regnosys.rosetta.rosetta.RosettaGroupByExpression
import com.regnosys.rosetta.rosetta.RosettaGroupByFeatureCall
import com.regnosys.rosetta.rosetta.RosettaIntLiteral
import com.regnosys.rosetta.rosetta.RosettaLiteral
import com.regnosys.rosetta.rosetta.RosettaMetaType
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.rosetta.RosettaNamed
import com.regnosys.rosetta.rosetta.RosettaParenthesisCalcExpression
import com.regnosys.rosetta.rosetta.RosettaRegularAttribute
import com.regnosys.rosetta.rosetta.RosettaStringLiteral
import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.rosetta.RosettaWhenPresentExpression
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.EmptyLiteral
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration
import com.regnosys.rosetta.types.RosettaTypeProvider
import com.rosetta.model.lib.functions.MapperMaths
import com.rosetta.model.lib.functions.MapperS
import com.rosetta.model.lib.functions.MapperTree
import com.rosetta.model.lib.validation.ComparisonResult
import java.math.BigDecimal
import java.util.HashMap
import org.eclipse.xtend.lib.annotations.Data
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.EcoreUtil2

import static extension com.regnosys.rosetta.generator.java.enums.EnumGenerator.convertValues
import static extension com.regnosys.rosetta.generator.java.util.JavaClassTranslator.toJavaType
import static extension com.regnosys.rosetta.generator.util.RosettaAttributeExtensions.cardinalityIsListValue

class RosettaExpressionJavaGeneratorForFunctions {
	
	@Inject RosettaTypeProvider typeProvider
	@Inject com.regnosys.rosetta.generator.java.function.ConvertableCardinalityProvider cardinalityProvider
	@Inject JavaNames.Factory factory 
	@Inject RosettaFunctionExtensions funcExt
	@Inject extension RosettaExtensions
	
	def StringConcatenationClient javaCode(RosettaExpression expr, ParamMap params) {
		expr.javaCode(params, true);
	}
	
	/**
	 * convert a rosetta expression to code
	 * ParamMpa params  - a map keyed by classname or positional index that provides variable names for expression parameters
	 */
	def StringConcatenationClient javaCode(RosettaExpression expr, ParamMap params, boolean isLast) {
		switch (expr) {
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
				'''«MapperS».of(«expr.enumeration.name».«expr.value.convertValues»)'''
			}
			RosettaConditionalExpression : {
				'''doIf(«expr.^if.javaCode(params)»,«expr.ifthen.javaCode(params)»,«IF expr.elsethen !== null»«expr.elsethen.javaCode(params)»«ELSE»«ComparisonResult».success()«ENDIF»)'''
			}
			RosettaContainsExpression : {
				'''contains(«expr.container.javaCode(params)», «expr.contained.javaCode(params)»)'''
			}
			RosettaParenthesisCalcExpression : {
				expr.expression.javaCode(params, isLast)
			}
			EmptyLiteral : {
				'''null'''
			}
			default: 
				throw new UnsupportedOperationException("Unsupported expression type of " + expr.class.simpleName)
		}
	}
	
	def StringConcatenationClient callableWithArgs(RosettaCallableWithArgsCall expr, ParamMap params) {
		val callable = expr.callable
		
		val many = switch (callable) {
			Function:
				funcExt.getOutput(callable).card.isMany
			RosettaFunction:
				callable.output.card.isMany
			default:
				null
		}
		if (many !== null)
			if (!many) {
				'''«MapperS».of(«callable.name.toFirstLower».evaluate(«FOR arg : expr.args SEPARATOR ', '»«arg.javaCode(params)»«IF !(arg instanceof EmptyLiteral)»«IF cardinalityProvider.isMulti(arg)».getMulti()«ELSE».get()«ENDIF»«ENDIF»«ENDFOR»))'''
			} else {
				throw new IllegalArgumentException(
					'Calling Functions with multiple cardinality return types not yet supported')
			}
	}
	
	def StringConcatenationClient existsExpr(RosettaExistsExpression exists, RosettaExpression argument, ParamMap params) {
		val arg = getAliasExpressionIfPresent(argument)
		
		if (arg instanceof RosettaBinaryOperation) {
			if(arg.operator.equals("or") || arg.operator.equals("and"))
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
			'''singleExists(«arg», «exists.only»)'''
		else if(exists.multiple)
			'''multipleExists(«arg», «exists.only»)'''
		else
			'''exists(«arg», «exists.only»)'''
	}
	
	def StringConcatenationClient absentExpr(RosettaAbsentExpression notSet, RosettaExpression argument, ParamMap params) {
		val arg = getAliasExpressionIfPresent(argument)
		
		if (arg instanceof RosettaBinaryOperation) {
			if(arg.operator.equals("or") || arg.operator.equals("and"))
				'''notExists(«arg.binaryExpr(notSet, params)»)'''
			else
				//if the arg is binary then the operator needs to be pushed down
				arg.binaryExpr(notSet, params)
		}
		else {
			'''notExists(«arg.javaCode(params)»)'''
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
			RosettaArgumentFeature : {
				'''args.get("«call.name»")'''
			}
			RosettaAlias : {
				call.expression.javaCode(params)
			}
			RosettaFunctionInput : {
				'''«MapperS».of(«call.name»)'''
			}
			Attribute : {
				'''«MapperS».of(«call.name»)'''
			}
			ShortcutDeclaration : {
				'''«call.name»(«inputsAsArgs(call)»)'''
			}
			default: 
				throw new UnsupportedOperationException("Unsupported callable type of "+call.class.simpleName)
		}
	}
	
	def inputsAsArgs(ShortcutDeclaration alias) {
		val func = EcoreUtil2.getContainerOfType(alias, Function)
		funcExt.getInputs(func).join(', ')[name]
	}
	
	/**
	 * feature call is a call to get an attribute of an object e.g. Quote->amount
	 */
	def StringConcatenationClient featureCall(RosettaFeatureCall call, ParamMap params, boolean isLast, boolean autoValue) {
		val feature = call.feature
		val StringConcatenationClient right = 
			if (feature instanceof RosettaRegularAttribute)
				feature.buildMapFunc(isLast, autoValue)
			else if (feature instanceof Attribute)
				feature.buildMapFunc(isLast, autoValue)
			else
				throw new UnsupportedOperationException("Unsupported expression type of "+feature.class.simpleName)
		'''«javaCode(call.receiver, params, false)»«right»'''
	}
	
	def StringConcatenationClient countExpr(RosettaCountOperation expr, RosettaExpression test, ParamMap params) {
		toComparisonOp('''«MapperS».of(«expr.left.javaCode(params)».resultCount())''', expr.operator, expr.right.javaCode(params))
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
						'''.andDifferent(«left.booleanize(test, params)», «right.booleanize(test, params)»)'''
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
				val leftType = '''«typeProvider.getRType(expr.left).name.toJavaType»'''
				val rightType = '''«typeProvider.getRType(expr.right).name.toJavaType»'''
				'''«MapperMaths».<«BigDecimal», «leftType», «rightType»>add(«expr.left.javaCode(params)», «expr.right.javaCode(params)»)'''
			}
			case ("-"): {
				val leftType = '''«typeProvider.getRType(expr.left).name.toJavaType»'''
				val rightType = '''«typeProvider.getRType(expr.right).name.toJavaType»'''
				'''«MapperMaths».<«BigDecimal», «leftType», «rightType»>subtract(«expr.left.javaCode(params)», «expr.right.javaCode(params)»)'''
			}			
			default: {
				toComparisonOp('''«expr.left.javaCode(params)»''', expr.operator, '''«expr.right.javaCode(params)»''')
			}
		}
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
	 * Collects all expressions down the tree, and checks that they're all either FeatureCalls or CallableCalls
	 */
	private def boolean containsFeatureCallOrCallableCall(RosettaExpression expr) {
		val exprs = newHashSet
		val extensions = new RosettaExtensions
		extensions.collectExpressions(expr, [exprs.add(it)])

		return !exprs.empty && exprs.stream.allMatch[it instanceof RosettaGroupByFeatureCall || it instanceof RosettaFeatureCall || it instanceof RosettaCallableCall]
	}
	
	/**
	 * Search leaf node objects to determine whether this is a comparison of matching objects types
	 */
	private def isComparableTypes(RosettaBinaryOperation binaryExpr) {
		// get list of the object type at each leaf node
		val rosettaTypes = newHashSet
		val extensions = new RosettaExtensions
		extensions.collectLeafTypes(binaryExpr, [rosettaTypes.add(it)])
		
		// check whether they're all the same type
		val type = rosettaTypes.stream.findAny
		return type.isPresent && rosettaTypes.stream.allMatch[it.equals(type.get)]
	}
		
	private def StringConcatenationClient toComparisonOp(StringConcatenationClient leftExpr, String operator, StringConcatenationClient rightExpr) {
		val left = toCommonType(leftExpr, rightExpr)
		val right = toCommonType(rightExpr, leftExpr)
		
		switch operator {
			case ("="):
				'''areEqual(«left», «right»)'''
			case ("<>"):
				'''notEqual(«left», «right»)'''
			case ("<") : 
				'''lessThan(«left», «right»)'''
			case ("<=") : 
				'''lessThanEquals(«left», «right»)'''
			case (">") : 
				'''greaterThan(«left», «right»)'''
			case (">=") : 
				'''greaterThanEquals(«left», «right»)'''
			default: 
				throw new UnsupportedOperationException("Unsupported binary operation of "+operator)
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
				'''.<«attribute.type.name.toJavaType»>mapC(«mapFunc»)'''
			else if (!autoValue) {
				'''.<«attribute.metaClass»>mapC(«mapFunc»)'''
			}
			else {
				'''.mapC(«mapFunc»).<«attribute.type.toJavaType»>map("getValue", FieldWithMeta::getValue)'''
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
				'''.map(«mapFunc»).<«attribute.type.toJavaType»>map("getValue", FieldWithMeta::getValue)'''
		}
	}
	/**
	 * Builds the expression of mapping functions to extract a path of attributes
	 */
	def StringConcatenationClient buildMapFunc(Attribute attribute, boolean isLast, boolean autoValue) {
		val mapFunc = attribute.buildMapFuncAttribute
		if (attribute.card.isIsMany) {
			if (attribute.metaAnnotations.nullOrEmpty)
				'''.<«attribute.type.name.toJavaType»>mapC(«mapFunc»)'''
			else if (!autoValue) {
				'''.<«attribute.metaClass»>mapC(«mapFunc»)'''
			}
			else {
				'''.mapC(«mapFunc»).<«attribute.type.toJavaType»>map("getValue", FieldWithMeta::getValue)'''
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
				'''.map(«mapFunc»).<«attribute.type.toJavaType»>map("getValue", FieldWithMeta::getValue)'''
		}
	}
	
	def JavaType toJavaType(RosettaType rosType) {
		val model = EcoreUtil2.getContainerOfType(rosType, RosettaModel)
		if (model !== null)
			factory.create(model).toJavaType(rosType)
		else
			JavaType.create(rosType.name.toJavaType)
	}
	
	def static metaClass(RosettaRegularAttribute attribute) {
		if (attribute.metaTypes.exists[m|m.name=="reference"]) "ReferenceWithMeta"+attribute.type.name.toJavaType.toFirstUpper
		else "FieldWithMeta"+attribute.type.name.toJavaType.toFirstUpper
	}
	def static metaClass(Attribute attribute) {
		if (attribute.annotations.exists[a|a.annotation?.name=="metadata" && a.attribute?.name=="reference"]) "ReferenceWithMeta"+attribute.type.name.toJavaType.toFirstUpper
		else "FieldWithMeta"+attribute.type.name.toJavaType.toFirstUpper
	}
	
	def static buildMapFunc(RosettaMetaType meta, boolean isLast) {
		if (meta.name=="reference") {
			'''.map("get«meta.name.toFirstUpper»", a->a.getGlobalReference())'''
		}
		else {
			'''.map("getMeta", a->a.getMeta()).map("get«meta.name.toFirstUpper»", a->a.get«meta.name.toFirstUpper»())'''
		}
	}

	def String buildGroupBy(RosettaGroupByExpression expression, boolean isLast) {
		var exprs = newArrayList
		var expr = expression
		exprs.add(expr)
		while (expr.right!==null) {
			expr = expr.right;
			exprs.add(expr)
		}
		'''.<«expr.attribute.type.name.toJavaType»>groupBy(g->new «MapperS»<>(g)«FOR ex:exprs»«buildMapFunc(ex.attribute, isLast, true)»«ENDFOR»)'''
	}
	
	private def static String buildMapFuncAttribute(RosettaRegularAttribute attribute)
		'''"get«attribute.name.toFirstUpper»", «(attribute.eContainer as RosettaClass).name»::get«attribute.name.toFirstUpper»'''

	private def static String buildMapFuncAttribute(Attribute attribute)
		'''"get«attribute.name.toFirstUpper»", «(attribute.eContainer as RosettaNamed).name»::get«attribute.name.toFirstUpper»'''

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
			RosettaRegularAttribute, RosettaMetaType: feature.name
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
	
	private def toCommonType(StringConcatenationClient code, StringConcatenationClient otherCode) {
		if(!isMapperTree(code) && isMapperTree(otherCode)) 
			return code.toMapperTree
		else
			return code
	}
	
	protected def StringConcatenationClient toMapperTree(StringConcatenationClient code) {
		return '''«MapperTree».of(«code»)'''
	}
	
	private def isMapperTree(StringConcatenationClient code) {
		return code.toString.startsWith('MapperTree')
	}
}
