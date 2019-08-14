package com.regnosys.rosetta.generator.java.calculation

import com.google.inject.Inject
import com.regnosys.rosetta.rosetta.RosettaAlias
import com.regnosys.rosetta.rosetta.RosettaArgumentFeature
import com.regnosys.rosetta.rosetta.RosettaBigDecimalLiteral
import com.regnosys.rosetta.rosetta.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.RosettaCallableCall
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgsCall
import com.regnosys.rosetta.rosetta.RosettaClass
import com.regnosys.rosetta.rosetta.RosettaConditionalExpression
import com.regnosys.rosetta.rosetta.RosettaEnumValueReference
import com.regnosys.rosetta.rosetta.RosettaExistsExpression
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
import com.regnosys.rosetta.types.RBuiltinType
import com.regnosys.rosetta.types.RClassType
import com.regnosys.rosetta.types.RDataType
import com.regnosys.rosetta.types.REnumType
import com.regnosys.rosetta.types.RFeatureCallType
import com.regnosys.rosetta.types.RRecordType
import com.regnosys.rosetta.types.RType
import com.regnosys.rosetta.types.RUnionType
import com.regnosys.rosetta.types.RosettaTypeCompatibility
import com.regnosys.rosetta.types.RosettaTypeProvider
import com.rosetta.model.lib.functions.ExpressionOperators
import com.rosetta.model.lib.math.BigDecimalExtensions
import java.lang.reflect.Modifier
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Objects
import org.eclipse.xtend2.lib.StringConcatenationClient

import static extension com.regnosys.rosetta.generator.java.enums.EnumGenerator.convertValues

class RosettaToJavaExtensions {
	@Inject RosettaTypeProvider typeProvider
	@Inject extension RosettaTypeCompatibility


	def dispatch StringConcatenationClient toJava(extension JavaNames it, Object ele) {
		'''not implemented: «ele.class»'''
	}

	def dispatch StringConcatenationClient toJava(extension JavaNames it, RosettaNamed ele) {
		'''not implemented named «ele.class»'''
	}

	def dispatch StringConcatenationClient toJava(extension JavaNames it, RosettaGroupByFeatureCall ele) {
		toJava(ele.call)	
	}
	
	def dispatch StringConcatenationClient toJava(extension JavaNames it, RosettaEnumValueReference ele) {
		'''«toJavaQualifiedType(ele.enumeration)».«ele.value.convertValues»'''
	}

	def dispatch StringConcatenationClient toJava(extension JavaNames it, RosettaFeatureCall ele) {
		val recieverType = typeProvider.getRType(ele.receiver)
		if (recieverType instanceof RUnionType) {
			'''«wrapInToConverter(recieverType, toJava(ele.receiver))».«toJava(ele.feature)»'''
		} else {
			'''«toJava(ele.receiver)».«toJava(ele.feature)»'''
		}
	}

	def StringConcatenationClient wrapInToConverter(extension JavaNames it, RUnionType ele, StringConcatenationClient toWrap) {
		'''new «ele.converter.toTargetClassName.firstSegment»().calculate(inputParam, «toWrap»)'''
	}

	def dispatch StringConcatenationClient toJava(extension JavaNames it, RosettaRegularAttribute ele) {
		if (ele.metaTypes===null || ele.metaTypes.isEmpty)
			'''get«ele.name.toFirstUpper»()'''
		else {
			'''get«ele.name.toFirstUpper»().getValue()'''
		}
	}

	def dispatch StringConcatenationClient toJava(extension JavaNames it, RosettaFeature ele) {
		'''get«ele.name.toFirstUpper»()'''
	}

	def dispatch StringConcatenationClient toJava(extension JavaNames it, RosettaCallableWithArgsCall ele) {
		'''«toJava(ele.callable)».execute(«FOR arg : ele.args SEPARATOR ','»«toJava(arg)»«ENDFOR»)'''
	}
	
	def dispatch StringConcatenationClient toJava(extension JavaNames it, RosettaExternalFunction ele) {
		if (ele.isLibrary) {
			'''new «toJavaQualifiedType(ele as RosettaType)»()'''
		} else {
			'''«ele.name.toFirstLower»'''	
		}
	}
	
	def dispatch StringConcatenationClient toJava(extension JavaNames it, RosettaRecordType ele) {
		'''new «toJavaQualifiedType(ele as RosettaType)»()'''
	}

	def dispatch StringConcatenationClient toJava(extension JavaNames it, RosettaLiteral ele) {
		'''«ele.stringValue»'''
	}

	def dispatch StringConcatenationClient toJava(extension JavaNames it, RosettaBigDecimalLiteral ele) {
		toBigDecimal('''«ele.stringValue»''')
	}

	def dispatch StringConcatenationClient toJava(extension JavaNames it, RosettaParenthesisCalcExpression ele) {
		'''(«toJava(ele.expression)»)'''
	}

	def dispatch StringConcatenationClient toJava(extension JavaNames it, RosettaBinaryOperation ele) {
		val leftType = typeProvider.getRType(ele.left)
		val rightType = typeProvider.getRType(ele.right)

		val leftIsBD = RBuiltinType.NUMBER.isUseableAs(leftType)
		val rightIsBD = RBuiltinType.NUMBER.isUseableAs(rightType)

		val leftStr = if(leftIsBD) toJava(ele.left) else toBigDecimal(toJava(ele.left))
		val rightStr = if(rightIsBD) toJava(ele.right) else toBigDecimal(toJava(ele.right))
	
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
				default: '''«toJava(ele.left)» «ele.operator» «toJava(ele.right)»'''
			}
		} else if (ele.operator == '+' && leftType==RBuiltinType.DATE && rightType==RBuiltinType.TIME){
			'''«LocalDateTime».of(«toJava(ele.left)», «toJava(ele.right)»)'''
		} else {
			switch (ele.operator) {
				case "=": '''«Objects».equals(«toJava(ele.left)», «toJava(ele.right)»)'''
				case "and": '''(«toJava(ele.left)») && («toJava(ele.right)»)'''
				case "or": '''(«toJava(ele.left)») || («toJava(ele.right)»)'''
				default: '''(«toJava(ele.left)» «ele.operator» «toJava(ele.right)»)'''
			}
			
		}
	}

	def dispatch StringConcatenationClient toJava(extension JavaNames it, RosettaConditionalExpression ele) {
		'''«toJava(ele.^if)» ? «toJava(ele.ifthen)» : «toJava(ele.elsethen)»'''
	}
	
	def dispatch StringConcatenationClient toJava(extension JavaNames it, RosettaExistsExpression ele) {
//		'''«expressionJavaGenerator.javaCode(ele, new ParamMap(), true)»'''
		'''«getMethod(ExpressionOperators, 'exists')»(«toJava(ele.argument)»)'''
	}
	
	private def getMethod(Class<?> clazz, String name) {
		val method = clazz.methods.filter[it.name.equals(name)].head
		
		if (Modifier.isStatic(method.modifiers)) {
			method
		} else {
			throw new IllegalArgumentException("the list must not be empty")
		}
	}

	def StringConcatenationClient toBigDecimal(StringConcatenationClient sequence) {
		'''«BigDecimal».valueOf(«sequence»)'''
	}

	def dispatch StringConcatenationClient toJava(extension JavaNames it, RType rType) {
		switch (rType) {
			RBuiltinType:
				rType.name.toJavaQualifiedType
			REnumType:
				rType.enumeration.toJavaQualifiedType
			RClassType:
				rType.clazz.toJavaQualifiedType
			RDataType:
				rType.data.toJavaQualifiedType
			RUnionType:
				toJava(rType.to)
			RFeatureCallType:
				toJava(rType.featureType)
			RRecordType:
				'''«(rType.record as RosettaType).toJavaQualifiedType».CalculationResult'''
			default: '''«rType.name»'''
		}
	}

	def dispatch StringConcatenationClient toJava(extension JavaNames names, RosettaCallableCall ele) {
		val callable = ele.callable
		switch (callable) {
			RosettaArgumentFeature: '''input.«callable.name»'''
			RosettaAlias: '''«callable.name»Alias'''
			RosettaClass: '''inputParam'''
			default: '''«callable.name»'''
		}
	}
}
