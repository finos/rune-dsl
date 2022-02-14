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
import com.regnosys.rosetta.rosetta.RosettaAbsentExpression
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
import com.regnosys.rosetta.rosetta.RosettaIntLiteral
import com.regnosys.rosetta.rosetta.RosettaLiteral
import com.regnosys.rosetta.rosetta.RosettaMetaType
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.rosetta.RosettaOnlyExistsExpression
import com.regnosys.rosetta.rosetta.RosettaParenthesisCalcExpression
import com.regnosys.rosetta.rosetta.RosettaStringLiteral
import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.ClosureParameter
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.simple.EmptyLiteral
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.simple.ListLiteral
import com.regnosys.rosetta.rosetta.simple.ListOperation
import com.regnosys.rosetta.rosetta.simple.ListOperationKind
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
import java.util.Arrays
import java.util.HashMap
import java.util.Optional
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.EcoreUtil2

import static extension com.regnosys.rosetta.generator.java.enums.EnumHelper.convertValues
import static extension com.regnosys.rosetta.generator.java.util.JavaClassTranslator.toJavaClass
import static extension com.regnosys.rosetta.generator.java.util.JavaClassTranslator.toJavaType

class ExpressionGenerator {
	
	@Inject protected RosettaTypeProvider typeProvider
	@Inject RosettaOperators operators
	@Inject CardinalityProvider cardinalityProvider
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
				'''«expr.genConditionalMapper(params)»'''
			}
			RosettaContainsExpression : {
				'''«importMethod(ExpressionOperators,"contains")»(«expr.container.javaCode(params)», «expr.contained.javaCode(params)»)'''
			}
			RosettaDisjointExpression : {
				'''«importMethod(ExpressionOperators,"disjoint")»(«expr.container.javaCode(params)», «expr.disjoint.javaCode(params)»)'''
			}
			RosettaParenthesisCalcExpression : {
				expr.expression.javaCode(params)
			}
			EmptyLiteral : {
				'''null'''
			}
			ListLiteral : {
				'''«MapperC».of(«FOR ele: expr.elements SEPARATOR ', '»«ele.javaCode(params)»«ENDFOR»)'''
			}
			ListOperation : {
				listOperation(expr, params)
			}
			default: 
				throw new UnsupportedOperationException("Unsupported expression type of " + expr?.class?.simpleName)
		}
	}

	private def StringConcatenationClient genConditionalMapper(RosettaConditionalExpression expr, ParamMap params)'''
		«IF !expr.ifthen.evalulatesToMapper»com.rosetta.model.lib.mapper.MapperUtils.toComparisonResult(«ENDIF»com.rosetta.model.lib.mapper.MapperUtils.«IF funcExt.needsBuilder(expr.ifthen)»fromDataType«ELSE»fromBuiltInType«ENDIF»(() -> {
		«expr.genConditional(params)»
		})«IF !expr.ifthen.evalulatesToMapper»)«ENDIF»'''



	private def StringConcatenationClient genConditional(RosettaConditionalExpression expr, ParamMap params) {
		return  '''
			if («expr.^if.javaCode(params)».get()) {
				return «expr.ifthen.javaCode(params)»;
			}
			«IF expr.childElseThen !== null»
				«expr.childElseThen.genElseIf(params)»
			«ELSEIF expr.elsethen !== null»
				else {
					return «expr.elsethen.javaCode(params)»;
				}
			«ELSE»
				else {
					return «IF cardinalityProvider.isMulti(expr.ifthen)»«MapperC»«ELSE»«MapperS».ofNull()«ENDIF».ofNull();
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
			«ELSEIF next.elsethen !== null»
				else {
					return «next.elsethen.javaCode(params)»;
				}
			«ELSEIF next.elsethen === null»
				else {
					return «IF cardinalityProvider.isMulti(next.ifthen)»«MapperC»«ELSE»«MapperS».ofNull()«ENDIF».ofNull();
				}
			«ENDIF»
		«ENDIF»
		'''
	}

	private def RosettaConditionalExpression childElseThen(RosettaConditionalExpression expr) {
		if (expr.elsethen instanceof RosettaConditionalExpression)
			expr.elsethen as RosettaConditionalExpression
	}
	
	def StringConcatenationClient callableWithArgs(RosettaCallableWithArgsCall expr, ParamMap params) {
		val callable = expr.callable
		
		return switch (callable) {
			Function: {
				val multi = funcExt.getOutput(callable).card.isMany
				val implicitArg = funcExt.implicitFirstArgument(expr)
				'''«IF multi»«MapperC»«ELSE»«MapperS»«ENDIF».of(«callable.name.toFirstLower».evaluate(«IF implicitArg !== null»«implicitArg.name.toFirstLower»«ENDIF»«args(expr, params)»))'''
			}
			RosettaExternalFunction: {
				'''«MapperS».of(new «factory.create(callable.model).toJavaType(callable as RosettaCallableWithArgs)»().execute(«args(expr, params)»))'''
			}
			default: 
				throw new UnsupportedOperationException("Unsupported callable with args type of " + expr?.eClass?.name)
		}
		
	}
	
	private def StringConcatenationClient args(RosettaCallableWithArgsCall expr, ParamMap params) {
		'''«FOR argExpr : expr.args SEPARATOR ', '»«arg(argExpr, params)»«ENDFOR»'''
	}
	
	private def StringConcatenationClient arg(RosettaExpression expr, ParamMap params) {
		'''«expr.javaCode(params)»«IF !(expr instanceof EmptyLiteral)»«IF cardinalityProvider.isMulti(expr)».getMulti()«ELSE».get()«ENDIF»«ENDIF»'''
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
			RosettaParenthesisCalcExpression: expression.expression.findBinaryOperation
			default: null
		}
	}
	
	private def StringConcatenationClient doExistsExpr(RosettaExistsExpression exists, StringConcatenationClient arg) {
		if(exists.single)
			'''singleExists(«arg»)'''
		else if(exists.multiple)
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
			return '''«EcoreUtil2.getContainerOfType(expr, ListOperation).firstOrImplicit.getNameOrDefault.toDecoratedName»'''
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
					'''«MapperS».of(«EcoreUtil2.getContainerOfType(expr, Data).getName.toFirstLower»)«buildMapFunc(call, true)»'''
				else
					distinctOrOnlyElement('''«if (call.card.isIsMany) MapperC else MapperS».of(«call.name»)''', false, expr.onlyElement)
			}
			ShortcutDeclaration : {
				val multi = cardinalityProvider.isMulti(call)
				distinctOrOnlyElement('''«IF multi»«MapperC»«ELSE»«MapperS»«ENDIF».of(«call.name»(«aliasCallArgs(call)»).«IF exprHelper.usesOutputParameter(call.expression)»build()«ELSE»«IF multi»getMulti()«ELSE»get()«ENDIF»«ENDIF»)''', false, expr.onlyElement)
			}
			RosettaEnumeration: '''«call.toJavaType»'''
			ClosureParameter: '''«call.getNameOrDefault.toDecoratedName»'''
			default: 
				throw new UnsupportedOperationException("Unsupported callable type of " + call?.class?.simpleName)
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
				feature.buildMapFunc(autoValue)
			RosettaMetaType: 
				'''«feature.buildMapFunc»'''
			RosettaEnumValue: 
				return '''«MapperS».of(«feature.enumeration.toJavaType».«feature.convertValues»)'''
			RosettaFeature: 
				'''.map("get«feature.name.toFirstUpper»", «feature.containerType.toJavaType»::get«feature.name.toFirstUpper»)'''
			default:
				throw new UnsupportedOperationException("Unsupported expression type of " + feature.eClass.name)
		}
		
		return distinctOrOnlyElement('''«javaCode(call.receiver, params)»«right»''', false, call.onlyElement)
	}
	
	private def StringConcatenationClient distinctOrOnlyElement(StringConcatenationClient code, boolean distinct, boolean onlyElement) {
		return '''«IF onlyElement»«MapperS».of(«ENDIF»«IF distinct»«importWildCard(ExpressionOperators)»distinct(«ENDIF»«code»«IF distinct»)«ENDIF»«IF onlyElement».get())«ENDIF»'''
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
			default: {
				toComparisonOp('''«expr.left.javaCode(params)»''', expr.operator, '''«expr.right.javaCode(params)»''', expr.cardOp)
			}
		}
	}

	private def StringConcatenationClient toComparisonResult(RosettaExpression expr, ParamMap params) {
		val wrap = expr.evalulatesToMapper
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
	private def boolean evalulatesToMapper(RosettaExpression expr) {
		val exprs = newHashSet
		collectExpressions(expr, [exprs.add(it)])

		return !exprs.empty && 
			exprs.stream.allMatch[it instanceof RosettaFeatureCall ||
									it instanceof RosettaCallableCall ||
									it instanceof RosettaFeatureCall ||
									it instanceof RosettaCallableWithArgsCall ||
									it instanceof RosettaLiteral ||
									it instanceof RosettaConditionalExpression ||
									it instanceof RosettaCountOperation ||
									it instanceof ListOperation ||
									isArithmeticOperation(it)
			]
	}
	
	private def StringConcatenationClient toComparisonOp(StringConcatenationClient left, String operator, StringConcatenationClient right, String cardOp) {
		switch operator {
			case ("="):
				'''«importWildCard(ExpressionOperators)»areEqual(«left», «right», «toCardinalityOperator(cardOp, "All")»)'''
			case ("<>"):
				'''«importWildCard(ExpressionOperators)»notEqual(«left», «right», «toCardinalityOperator(cardOp, "Any")»)'''
			case ("<") : 
				'''«importWildCard(ExpressionOperators)»lessThan(«left», «right», «toCardinalityOperator(cardOp, "All")»)'''
			case ("<=") : 
				'''«importWildCard(ExpressionOperators)»lessThanEquals(«left», «right», «toCardinalityOperator(cardOp, "All")»)'''
			case (">") : 
				'''«importWildCard(ExpressionOperators)»greaterThan(«left», «right», «toCardinalityOperator(cardOp, "All")»)'''
			case (">=") : 
				'''«importWildCard(ExpressionOperators)»greaterThanEquals(«left», «right», «toCardinalityOperator(cardOp, "All")»)'''
			default: 
				throw new UnsupportedOperationException("Unsupported binary operation of " + operator)
		}
	}
	
	private def StringConcatenationClient toCardinalityOperator(String cardOp, String defaultOp) {
		'''«CardinalityOperator».«Optional.ofNullable(cardOp).map[toFirstUpper].orElse(defaultOp)»'''
	}
	
	/**
	 * Builds the expression of mapping functions to extract a path of attributes
	 */
	def StringConcatenationClient buildMapFunc(Attribute attribute, boolean autoValue) {
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
	
	def static StringConcatenationClient buildMapFunc(RosettaMetaType meta) {
		if (meta.name=="reference") {
			'''.map("get«meta.name.toFirstUpper»", a->a.getGlobalReference())'''
		}
		else {
			'''.map("getMeta", a->a.getMeta()).map("get«meta.name.toFirstUpper»", a->a.get«meta.name.toFirstUpper»())'''
		}
	}

	def StringConcatenationClient listOperation(ListOperation op, ParamMap params) {
		switch (op.operationKind) {
			case FILTER: {
				'''
				«op.receiver.javaCode(params)»
					.«IF op.isItemMulti»filterList«ELSE»filterItem«ENDIF»(«op.itemName» -> «op.body.javaCode(params)».get())'''
			}
			case MAP: {
				val itemType =  op.inputType
				val itemName =  op.itemName
				val isBodyMulti =  op.isBodyExpressionMulti
				val outputType =  op.outputType
				val bodyExpr = op.body.javaCode(params)
				
				if (!op.isPreviousOperationMulti) {
					if (isBodyMulti) {
						'''
						«op.receiver.javaCode(params)»
							.mapSingleToList((/*«MapperS»<«itemType»>*/ «itemName») -> («MapperC»<«outputType»>) «bodyExpr»)'''
					} else {
						buildSingleItemListOperationOptionalBody(op, "mapSingleToItem", params)
					}
				} else {
					if (op.isItemMulti) {
						if (isBodyMulti) {
							'''
							«op.receiver.javaCode(params)»
								.mapListToList((/*«MapperC»<«itemType»>*/ «itemName») -> («MapperC»<«outputType»>) «bodyExpr»)'''
						} else {
							'''
							«op.receiver.javaCode(params)»
								.mapListToItem((/*«MapperC»<«itemType»>*/ «itemName») -> («MapperS»<«outputType»>) «bodyExpr»)'''
						}
					} else {
						if (isBodyMulti) {
							'''
							«op.receiver.javaCode(params)»
								.mapItemToList((/*«MapperS»<«itemType»>*/ «itemName») -> («MapperC»<«outputType»>) «bodyExpr»)'''
						} else {
							buildSingleItemListOperationOptionalBody(op, "mapItem", params)
						}
					}
				}
			}
			case FLATTEN: {
				buildListOperationNoBody(op, "flattenList", params)
			}
			case DISTINCT, case ONLY_ELEMENT: {
				distinctOrOnlyElement('''«op.receiver.javaCode(params)»''', op.operationKind === ListOperationKind.DISTINCT, op.operationKind === ListOperationKind.ONLY_ELEMENT)
			}
			case SUM: {
				buildListOperationNoBody(op, "sum" + op.inputRawType, params)
			}
			case MIN: {
				buildSingleItemListOperationOptionalBody(op, "min", params)
			}
			case MAX: {
				buildSingleItemListOperationOptionalBody(op, "max", params)
			}
			case SORT: {
				buildSingleItemListOperationOptionalBody(op, "sort", params)
			}
			case REVERSE: {
				buildListOperationNoBody(op, "reverse", params)
			}
			case REDUCE: {
				val item1 = op.parameters.head.name.toDecoratedName
				val item2 = op.parameters.last.name.toDecoratedName
				val outputType =  op.outputRawType
				val bodyExpr = op.body.javaCode(params)
				'''
				«op.receiver.javaCode(params)»
					.<«outputType»>reduce((«item1», «item2») -> («MapperS»<«outputType»>) «bodyExpr»)'''
			}
			case JOIN: {
				'''
				«op.receiver.javaCode(params)»
					.join(«IF op.body !== null»«op.body.javaCode(params)»«ELSE»«MapperS».of("")«ENDIF»)'''
			}
			case FIRST: {
				buildListOperationNoBody(op, "first", params)
			}
			case LAST: {
				buildListOperationNoBody(op, "last", params)
			}
			case GET_INDEX: {
				'''
				«op.receiver.javaCode(params)»
					.index(«op.body.javaCode(params)»)'''
			}
			default:
				throw new UnsupportedOperationException("Unsupported operationKind of " + op.operationKind)
		}
	}
	
	private def StringConcatenationClient buildListOperationNoBody(ListOperation op, String name, ParamMap params) {
		'''
		«op.receiver.javaCode(params)»
			.«name»()'''	
	}
	
	private def StringConcatenationClient buildSingleItemListOperationOptionalBody(ListOperation op, String name, ParamMap params) {
		if (op.body === null) {
			buildListOperationNoBody(op, name, params)
		} else {
			buildSingleItemListOperation(op, name, params)
		}
	}
	
	private def StringConcatenationClient buildSingleItemListOperation(ListOperation op, String name, ParamMap params) {
		val itemType =  op.inputType
		val itemName =  op.itemName
		val outputType = op.outputType
		val bodyExpr = op.body.javaCode(params)
		'''
		«op.receiver.javaCode(params)»
			.«name»(/*«MapperS»<«itemType»>*/ «itemName» -> («MapperS»<«outputType»>) «bodyExpr»«IF !op.body.evalulatesToMapper».asMapper()«ENDIF»)'''	
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
}
