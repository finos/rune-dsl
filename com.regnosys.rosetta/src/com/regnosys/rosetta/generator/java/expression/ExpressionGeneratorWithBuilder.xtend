package com.regnosys.rosetta.generator.java.expression

import com.google.common.base.Objects
import com.google.inject.Inject
import com.regnosys.rosetta.generator.java.function.ConvertableCardinalityProvider
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.generator.java.util.JavaNames
import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions
import com.regnosys.rosetta.rosetta.RosettaBigDecimalLiteral
import com.regnosys.rosetta.rosetta.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.RosettaCallableCall
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgsCall
import com.regnosys.rosetta.rosetta.RosettaConditionalExpression
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
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration
import com.regnosys.rosetta.types.RBuiltinType
import com.regnosys.rosetta.types.RosettaTypeCompatibility
import com.regnosys.rosetta.types.RosettaTypeProvider
import com.rosetta.model.lib.functions.ExpressionOperators
import com.rosetta.model.lib.math.BigDecimalExtensions
import com.rosetta.model.lib.records.Date
import org.eclipse.xtend.lib.annotations.Accessors
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor
import org.eclipse.xtend2.lib.StringConcatenationClient

import static extension com.regnosys.rosetta.generator.java.enums.EnumHelper.convertValues

class ExpressionGeneratorWithBuilder {

	@Inject extension RosettaTypeCompatibility
	@Inject RosettaTypeProvider typeProvider
	@Inject ConvertableCardinalityProvider cardinalityProvider
	@Inject RosettaFunctionExtensions funcExt
	@Inject extension ImportManagerExtension

	dispatch def StringConcatenationClient toJava(RosettaExpression ele, Context ctx) {
		throw new UnsupportedOperationException('Not supported expression: ' + ele.eClass.name)
	}

	dispatch def StringConcatenationClient toJava(RosettaFeatureCall ele, Context ctx) {
		// if the attribute being referenced is WithMeta and we aren't accessing the meta fields then access the value by default
		val feature = ele.feature
		val StringConcatenationClient right = if (feature instanceof RosettaRegularAttribute)
				feature.attributeAccess(ctx)
			else if (feature instanceof Attribute)
				feature.attributeAccess(ctx)
			else
				throw new UnsupportedOperationException("Unsupported expression type of " + feature.class.simpleName)
		'''«ele.receiver.toJava(ctx)».«right»(«IF cardinalityProvider.isMulti(feature)»0«ENDIF»)'''
	}

	def dispatch StringConcatenationClient toJava(Function ele, Context ctx) {
		'''«ele.name.toFirstLower»'''
	}

	def dispatch StringConcatenationClient toJava(RosettaGroupByFeatureCall ele, Context ctx) {
		toJava(ele.call, ctx)
	}

	def dispatch StringConcatenationClient toJava(RosettaEnumValueReference ele, Context ctx) {
		'''«ctx.names.toJavaQualifiedType(ele.enumeration)».«ele.value.convertValues»'''
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
					'''«toJava(ele.callable, ctx)».evaluate(«ctx.setInFunctionCall = true»«FOR arg : ele.args SEPARATOR ','»«toJava(arg, ctx)»«ENDFOR»«ctx.setInFunctionCall = false»)«toBuilder»'''
					return result
				}
			}
			default: '''«toJava(ele.callable, ctx)».execute(«FOR arg : ele.args SEPARATOR ','»«toJava(arg, ctx)»«ENDFOR»)'''
		}
	}

	def dispatch StringConcatenationClient toJava(RosettaExternalFunction ele, Context ctx) {
		if (ele.isLibrary) {
			'''new «ctx.names.toJavaQualifiedType(ele as RosettaType)»()'''
		} else {
			'''«ele.name.toFirstLower»'''
		}
	}

	def dispatch StringConcatenationClient toJava(RosettaRecordType ele, Context ctx) {
		'''new «ctx.names.toJavaQualifiedType(ele as RosettaType)»()'''
	}

	def dispatch StringConcatenationClient toJava(RosettaLiteral ele, Context ctx) {
		'''«ele.stringValue»'''
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
				case "=": '''«Objects».equals(«toJava(ele.left, ctx)», «toJava(ele.right, ctx)»)'''
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
				'''«callee.name»«IF expr.toOne».get(0)«ENDIF».toBuilder()'''
				else
				'''«callee.name»«IF expr.toOne».get(0)«ENDIF»'''
			}
			ShortcutDeclaration: {
				if(funcExt.needsBuilder(callee) && !ctx.inFunctionCall)
				'''«callee.name»(«funcExt.inputsAsArgs(callee)»)«IF expr.toOne».get(0)«ENDIF».get().toBuilder()'''
				else
				'''«callee.name»(«funcExt.inputsAsArgs(callee)»)«IF expr.toOne».get(0)«ENDIF».get()'''
			}
			RosettaNamed: {
				'''«callee.name»«IF expr.toOne».get(0)«ENDIF»'''
			}
			default:
				throw new UnsupportedOperationException("Unsupported callable type of " + callee.class.simpleName)
		}
	}

	def dispatch StringConcatenationClient toJava(RosettaConditionalExpression ele, Context ctx) {
		'''«toJava(ele.^if, ctx)» ? «toJava(ele.ifthen, ctx)» : «IF ele.elsethen !== null»«toJava(ele.elsethen, ctx)»«ELSE»null«ENDIF»'''
	}
	
	def dispatch StringConcatenationClient toJava(RosettaExistsExpression ele, Context ctx) {
		'''«importMethod(ExpressionOperators, 'exists')»(«toJava(ele.argument, ctx)»)'''
	}
	
	private def StringConcatenationClient attributeAccess(RosettaFeature feature, Context ctx) {
		 '''«IF funcExt.needsBuilder(feature)»getOrCreate«ELSE»get«ENDIF»«feature.name.toFirstUpper»'''
	}

//
//	private dispatch def metaClass(RosettaRegularAttribute attribute) {
//		if (attribute.metaTypes.exists[m|m.name == "reference"])
//			"ReferenceWithMeta" + attribute.type.name.toJavaType.toFirstUpper
//		else
//			"FieldWithMeta" + attribute.type.name.toJavaType.toFirstUpper
//	}
//
//	private dispatch def metaClass(Attribute attribute) {
//		if (attribute.annotations.exists[a|a.annotation?.name == "metadata" && a.attribute?.name == "reference"])
//			"ReferenceWithMeta" + attribute.type.name.toJavaType.toFirstUpper
//		else
//			"FieldWithMeta" + attribute.type.name.toJavaType.toFirstUpper
//	}
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
