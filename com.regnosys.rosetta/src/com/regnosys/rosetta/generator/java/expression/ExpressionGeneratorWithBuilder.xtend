package com.regnosys.rosetta.generator.java.expression

import com.google.common.base.Objects
import com.google.inject.Inject
import com.regnosys.rosetta.generator.java.function.CardinalityProvider
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.generator.java.util.JavaNames
import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions
import com.regnosys.rosetta.rosetta.RosettaAbsentExpression
import com.regnosys.rosetta.rosetta.RosettaBigDecimalLiteral
import com.regnosys.rosetta.rosetta.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.RosettaCallableCall
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgsCall
import com.regnosys.rosetta.rosetta.RosettaConditionalExpression
import com.regnosys.rosetta.rosetta.RosettaContainsExpression
import com.regnosys.rosetta.rosetta.RosettaCountOperation
import com.regnosys.rosetta.rosetta.RosettaEnumValue
import com.regnosys.rosetta.rosetta.RosettaEnumValueReference
import com.regnosys.rosetta.rosetta.RosettaExistsExpression
import com.regnosys.rosetta.rosetta.RosettaExpression
import com.regnosys.rosetta.rosetta.RosettaExternalFunction
import com.regnosys.rosetta.rosetta.RosettaFeature
import com.regnosys.rosetta.rosetta.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.RosettaGroupByFeatureCall
import com.regnosys.rosetta.rosetta.RosettaLiteral
import com.regnosys.rosetta.rosetta.RosettaNamed
import com.regnosys.rosetta.rosetta.RosettaParenthesisCalcExpression
import com.regnosys.rosetta.rosetta.RosettaRecordType
import com.regnosys.rosetta.rosetta.RosettaRegularAttribute
import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.EmptyLiteral
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.simple.ListLiteral
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration
import com.regnosys.rosetta.types.RBuiltinType
import com.regnosys.rosetta.types.RosettaTypeCompatibility
import com.regnosys.rosetta.types.RosettaTypeProvider
import com.rosetta.model.lib.functions.ExpressionOperators
import com.rosetta.model.lib.math.BigDecimalExtensions
import com.rosetta.model.lib.records.Date
import java.util.Arrays
import org.eclipse.xtend.lib.annotations.Accessors
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor
import org.eclipse.xtend2.lib.StringConcatenationClient

import static extension com.regnosys.rosetta.generator.java.enums.EnumHelper.convertValues
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.rosetta.model.lib.functions.MapperS

class ExpressionGeneratorWithBuilder {

	@Inject extension RosettaTypeCompatibility
	@Inject RosettaTypeProvider typeProvider
	@Inject CardinalityProvider cardinalityProvider
	@Inject RosettaFunctionExtensions funcExt
	@Inject extension ImportManagerExtension

	dispatch def StringConcatenationClient toJava(RosettaExpression ele, Context ctx) {
		throw new UnsupportedOperationException('Not supported expression: ' + ele.eClass.name)
	}

	dispatch def StringConcatenationClient toJava(RosettaFeatureCall ele, Context ctx) {
		val feature = ele.feature
		val StringConcatenationClient right = if (feature instanceof RosettaRegularAttribute)
				feature.attributeAccess(ele.toOne, ctx)
			else if (feature instanceof Attribute)
				feature.attributeAccess(ele.toOne,ctx)
			else if (feature instanceof RosettaEnumValue)
				return '''«ctx.names.toJavaType(ele.receiver as RosettaEnumeration)».«feature.convertValues»'''
			else
				throw new UnsupportedOperationException("Unsupported expression type of " + feature.class.simpleName)
		val left = ele.receiver.toJava(ctx)
		'''«left».«right»'''
	}

	def dispatch StringConcatenationClient toJava(Function ele, Context ctx) {
		'''«ele.name.toFirstLower»'''
	}

	def dispatch StringConcatenationClient toJava(RosettaGroupByFeatureCall ele, Context ctx) {
		toJava(ele.call, ctx)
	}

	def dispatch StringConcatenationClient toJava(RosettaEnumValueReference ele, Context ctx) {
		'''«ctx.names.toJavaType(ele.enumeration)».«ele.value.convertValues»'''
	}

	def dispatch StringConcatenationClient toJava(RosettaRegularAttribute ele, Context ctx) {
		if (ele.metaTypes === null || ele.metaTypes.isEmpty)
			'''get«ele.name.toFirstUpper»()'''
		else {
			'''get«ele.name.toFirstUpper»().getValue()'''
		}
	}

	def dispatch StringConcatenationClient toJava(RosettaFeature ele, Context ctx) {
		'''get«ele.name.toFirstUpper»()'''
	}

	def dispatch StringConcatenationClient toJava(RosettaCallableWithArgsCall ele, Context ctx) {
		val callable = ele.callable
		switch (callable) {
			Function: {
				val returnVal = funcExt.getOutput(callable)
				if (returnVal !== null) {
					val toBuilder = if(funcExt.needsBuilder(returnVal)) '.toBuilder()' else ''
					val StringConcatenationClient result = 
					'''«toJava(ele.callable, ctx)».evaluate(«ctx.setInFunctionCall = true»«FOR arg : ele.args SEPARATOR ', '»«toJava(arg, ctx)».get()«ENDFOR»«ctx.setInFunctionCall = false»)«toBuilder»'''
					return result
				}
			}
			default: '''«toJava(ele.callable, ctx)».execute(«FOR arg : ele.args SEPARATOR ','»«toJava(arg, ctx)»«ENDFOR»)'''
		}
	}

	def dispatch StringConcatenationClient toJava(RosettaExternalFunction ele, Context ctx) {
		'''new «ctx.names.toJavaType(ele as RosettaType)»()'''
	}

	def dispatch StringConcatenationClient toJava(RosettaRecordType ele, Context ctx) {
		'''new «ctx.names.toJavaType(ele as RosettaType)»()'''
	}

	def dispatch StringConcatenationClient toJava(RosettaLiteral ele, Context ctx) {
		'''«ele.stringValue»'''
	}
	
	def dispatch StringConcatenationClient toJava(ListLiteral ele, Context ctx) {
		'''«importMethod(Arrays,"asList")»(«FOR entry: ele.elements SEPARATOR ', '»«entry.toJava(ctx)»«ENDFOR»)'''
	}

	def dispatch StringConcatenationClient toJava(EmptyLiteral ele, Context ctx) {
		'''null'''
	}

	def dispatch StringConcatenationClient toJava(RosettaBigDecimalLiteral ele, Context ctx) {
		toBigDecimal('''«ele.stringValue»''')
	}

	def dispatch StringConcatenationClient toJava(RosettaParenthesisCalcExpression ele, Context ctx) {
		'''(«toJava(ele.expression, ctx)»)'''
	}

	def dispatch StringConcatenationClient toJava(RosettaBinaryOperation ele, Context ctx) {
		val leftType = typeProvider.getRType(ele.left)
		val rightType = typeProvider.getRType(ele.right)

		val leftIsBD = RBuiltinType.NUMBER.isUseableAs(leftType)
		val rightIsBD = RBuiltinType.NUMBER.isUseableAs(rightType)

		val leftStr = if(leftIsBD) toJava(ele.left, ctx) else toBigDecimal(toJava(ele.left, ctx))
		val rightStr = if(rightIsBD) toJava(ele.right, ctx) else toBigDecimal(toJava(ele.right, ctx))

		if (ele.operator == '/') {
			'''«BigDecimalExtensions».divide(«leftStr», «rightStr»)'''
		} else if (leftIsBD || rightIsBD) {
			switch (ele.operator) {
				case "*": '''«BigDecimalExtensions».multiply(«leftStr», «rightStr»)'''
				case "+": '''«BigDecimalExtensions».add(«leftStr», «rightStr»)'''
				case "-": '''«BigDecimalExtensions».subtract(«leftStr», «rightStr»)'''
				case ">": '''«leftStr».compareTo(«rightStr») > 0'''
				case ">=": '''«leftStr».compareTo(«rightStr») >= 0'''
				case "<": '''«leftStr».compareTo(«rightStr») < 0'''
				case "<=": '''«leftStr».compareTo(«rightStr») <= 0'''
				case "=": '''«leftStr».compareTo(«rightStr») == 0'''
				default: '''«toJava(ele.left, ctx)» «ele.operator» «toJava(ele.right, ctx)»'''
			}
		} else if (ele.operator == '+' && leftType == RBuiltinType.DATE && rightType == RBuiltinType.TIME) {
			'''«Date».of(«toJava(ele.left, ctx)», «toJava(ele.right, ctx)»)'''
		} else {
			switch (ele.operator) {
				case "=": '''«Objects».equal(«toJava(ele.left, ctx)», «toJava(ele.right, ctx)»)'''
				case "and": '''(«toJava(ele.left, ctx)») && («toJava(ele.right, ctx)»)'''
				case "or": '''(«toJava(ele.left, ctx)») || («toJava(ele.right, ctx)»)'''
				default: '''(«toJava(ele.left, ctx)» «ele.operator» «toJava(ele.right, ctx)»)'''
			}

		}
	}

	dispatch def StringConcatenationClient toJava(RosettaCallableCall expr, Context ctx) {
		val callee = expr.callable
		switch (callee) {
			Attribute: {
				if(funcExt.needsBuilder(callee) && !funcExt.isOutput(callee) && !ctx.inFunctionCall)
				'''«MapperS».of(«callee.name»)«IF expr.toOne».map("get(0)", _m-> _m.get(0))«ENDIF».map("toBuilder()", _m-> _m.toBuilder())'''
				else
				'''«MapperS».of(«callee.name»)«IF expr.toOne».map("get(0)", _m-> _m.get(0))«ENDIF»'''
			}
			ShortcutDeclaration: {
				if(funcExt.needsBuilder(callee) && !ctx.inFunctionCall)
				'''«callee.name»(«funcExt.inputsAsArgs(callee)»)«IF expr.toOne».get(0)«ENDIF».map("toBuilder", _m-> _m.toBuilder())'''
				else
				'''«callee.name»(«funcExt.inputsAsArgs(callee)»)«IF expr.toOne».get(0)«ENDIF»'''
			}
			RosettaNamed: {
				'''«callee.name»«IF expr.toOne».get(0)«ENDIF»'''
			}
			default:
				throw new UnsupportedOperationException("Unsupported callable type of " + callee.class.simpleName)
		}
	}

	def dispatch StringConcatenationClient toJava(RosettaConditionalExpression ele, Context ctx) {
		'''«toJava(ele.^if, ctx)» ? «toJava(ele.ifthen, ctx)» : «IF ele.elsethen !== null»«toJava(ele.elsethen, ctx)»«ELSE»MapperS.ofNull()«ENDIF»'''
	}
	
	def dispatch StringConcatenationClient toJava(RosettaExistsExpression ele, Context ctx) {
		'''«importMethod(ExpressionOperators, 'exists')»(«toJava(ele.argument, ctx)»)'''
	}
	
	def dispatch StringConcatenationClient toJava(RosettaAbsentExpression ele, Context ctx) {
		'''«importMethod(ExpressionOperators, 'notExists')»(«toJava(ele.argument, ctx)»)'''
	}
	
	def dispatch StringConcatenationClient toJava(RosettaCountOperation ele, Context ctx) {
		'''«importMethod(ExpressionOperators, 'count')»(«toJava(ele.argument, ctx)»)'''
	}
	
	def dispatch StringConcatenationClient toJava(RosettaContainsExpression ele, Context ctx) {
		'''«importMethod(ExpressionOperators, 'contains')»(«toJava(ele.container, ctx)», «toJava(ele.contained, ctx)»)'''
	}
	
	private def StringConcatenationClient attributeAccess(RosettaFeature feature, boolean toOne, Context ctx) {
		if(cardinalityProvider.isMulti(feature)) {
			'''map("get«feature.name.toFirstUpper»", _m-> _m.«IF funcExt.needsBuilder(feature) && toOne»get«feature.name.toFirstUpper»().get(0)«ELSE»get«feature.name.toFirstUpper»()«ENDIF»)'''
		}
		else
			'''map("get«feature.name.toFirstUpper»", _m-> _m.get«feature.name.toFirstUpper»())'''
	}

	private def StringConcatenationClient toBigDecimal(StringConcatenationClient sequence) {
		'''«BigDecimalExtensions».valueOf(«sequence»)'''
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
