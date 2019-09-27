package com.regnosys.rosetta.types

import com.google.inject.Inject
import com.regnosys.rosetta.rosetta.RosettaAbsentExpression
import com.regnosys.rosetta.rosetta.RosettaAlias
import com.regnosys.rosetta.rosetta.RosettaBasicType
import com.regnosys.rosetta.rosetta.RosettaBigDecimalLiteral
import com.regnosys.rosetta.rosetta.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.RosettaBooleanLiteral
import com.regnosys.rosetta.rosetta.RosettaCalculationType
import com.regnosys.rosetta.rosetta.RosettaCallableCall
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgsCall
import com.regnosys.rosetta.rosetta.RosettaClass
import com.regnosys.rosetta.rosetta.RosettaConditionalExpression
import com.regnosys.rosetta.rosetta.RosettaContainsExpression
import com.regnosys.rosetta.rosetta.RosettaCountOperation
import com.regnosys.rosetta.rosetta.RosettaEnumValueReference
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.RosettaExistsExpression
import com.regnosys.rosetta.rosetta.RosettaExpression
import com.regnosys.rosetta.rosetta.RosettaExternalFunction
import com.regnosys.rosetta.rosetta.RosettaFeature
import com.regnosys.rosetta.rosetta.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.RosettaGroupByFeatureCall
import com.regnosys.rosetta.rosetta.RosettaIntLiteral
import com.regnosys.rosetta.rosetta.RosettaMapPath
import com.regnosys.rosetta.rosetta.RosettaMapPathValue
import com.regnosys.rosetta.rosetta.RosettaMapRosettaPath
import com.regnosys.rosetta.rosetta.RosettaParenthesisCalcExpression
import com.regnosys.rosetta.rosetta.RosettaQualifiedType
import com.regnosys.rosetta.rosetta.RosettaRecordType
import com.regnosys.rosetta.rosetta.RosettaStringLiteral
import com.regnosys.rosetta.rosetta.RosettaWhenPresentExpression
import com.regnosys.rosetta.rosetta.simple.Condition
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.simple.EmptyLiteral
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.simple.ListLiteral
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration
import java.util.Map
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.conversion.impl.IDValueConverter
import org.eclipse.xtext.naming.IQualifiedNameProvider
import org.eclipse.xtext.nodemodel.util.NodeModelUtils

class RosettaTypeProvider {

	@Inject extension RosettaOperators
	@Inject IQualifiedNameProvider qNames
	@Inject IDValueConverter idConverter
	@Inject RosettaTypeCompatibility compatibility

	def RType getRType(EObject expression) {
		expression.safeRType(newHashMap)
	}
	
	private def RType safeRType(EObject expression, Map<EObject,RType> cycleTracker) {
		if (cycleTracker.containsKey(expression)) {
			val computed = cycleTracker.get(expression)
			if (computed === null) {
				return new RErrorType('''Can't infer type due to a cyclic reference of «qNames.getFullyQualifiedName(expression)»''')
			} else {
				return computed
			}
		}
		switch expression {
			RosettaCallableCall:
				safeRType(expression.callable, cycleTracker).wrapInFeatureCallType(expression)
			RosettaCallableWithArgsCall: {
				if (expression.callable instanceof RosettaExternalFunction) {
					val fun = expression.callable as RosettaExternalFunction
					val returnType = fun.safeRType(cycleTracker)
					// Generic return type for number type e.g. Min(1,2) or Max(2,6)
					if (returnType == RBuiltinType.NUMBER && expression.args.forall[it.safeRType(cycleTracker) == RBuiltinType.INT]) {
						RBuiltinType.INT
					} else {
						returnType
					}
				} else {
					expression.callable.safeRType(cycleTracker).wrapInFeatureCallType(expression)
				}
			}
			RosettaClass:
				new RClassType(expression)
			Data:
				new RDataType(expression)
			RosettaAlias: {
				val exp = expression.expression
				if (exp !== null && exp.eAllContents.filter(RosettaCallableCall).findFirst[expression == it.callable] === null) {
					val expressionType = exp.safeRType(cycleTracker)
					if (expressionType instanceof RFeatureCallType)
						return expressionType
					else
						new RFeatureCallType(expressionType)
				} else {
					new RErrorType('Can not compute type for ' + expression.name + " because of recursive call.")
				}
			}
			ShortcutDeclaration: {
				cycleTracker.put(expression, null)
				val type = expression.expression.safeRType(cycleTracker)
				cycleTracker.put(expression, type)
				type
			} 
			RosettaGroupByFeatureCall: {
				expression.call.safeRType(cycleTracker).wrapInFeatureCallType(expression)
			}
			RosettaFeatureCall: {
				if (expression.feature.isTypeInferred) {
					expression.feature.safeRType(cycleTracker).wrapInFeatureCallType(expression)
				} else {
					val type = expression.feature?.type
					type.safeRType(cycleTracker).wrapInFeatureCallType(expression)
				}
			}
			RosettaRecordType:
				new RRecordType(expression)
			RosettaEnumValueReference: {
				new REnumType(expression.enumeration)
			}
			RosettaEnumeration: {
				new REnumType(expression)
			}
			RosettaBinaryOperation: {
				// Synonym path expressions refer to external documents so type checking is not possible
				if (expression.left instanceof RosettaMapPathValue) {
					return null
				}
				val left = expression.left
				var leftType = left.safeRType(cycleTracker)
				if (leftType instanceof RErrorType) {
					return leftType
				}
				val right = expression.right
				var rightType = right.safeRType(cycleTracker)
				if (rightType instanceof RErrorType) {
					return rightType
				}
				expression.operator.resultType(leftType, rightType)
			}
			RosettaCountOperation: {
				val leftType = expression.left.safeRType(cycleTracker)
				if (leftType instanceof RErrorType) {
					return leftType
				}
				val rightType = expression.right.safeRType(cycleTracker)
				if (rightType instanceof RErrorType) {
					return rightType
				}
				expression.operator.resultType(RBuiltinType.INT, rightType)
			}
			RosettaWhenPresentExpression: {
				val leftType = expression.left.safeRType(cycleTracker)
				if (leftType instanceof RErrorType) {
					return leftType
				}
				val rightType = expression.right.safeRType(cycleTracker)
				if (rightType instanceof RErrorType) {
					return rightType
				}
				expression.operator.resultType(leftType, rightType)
			}
			RosettaContainsExpression,
			RosettaExistsExpression,
			RosettaAbsentExpression,
			RosettaBooleanLiteral:
				RBuiltinType.BOOLEAN
			RosettaStringLiteral:
				RBuiltinType.STRING
			RosettaIntLiteral:
				RBuiltinType.INT
			RosettaBigDecimalLiteral:
				RBuiltinType.NUMBER
			EmptyLiteral:
				RBuiltinType.ANY
			ListLiteral:
				expression.elements.head.RType
			RosettaExternalFunction: {
				expression.type.safeRType(cycleTracker)
			}
			RosettaFeature:
				expression.type.safeRType(cycleTracker)
			RosettaCalculationType:
				switch expression.name {
					case RCalculationType.CALCULATION.calculationType:
						RCalculationType.CALCULATION
					default:
						new RErrorType(
							'No such calculation type: ' + expression.name + " '" +
								NodeModelUtils.findActualNodeFor(expression)?.text + "'")
				}
			RosettaQualifiedType:
				switch expression.name {
					case RQualifiedType.PRODUCT_TYPE.qualifiedType:
						RQualifiedType.PRODUCT_TYPE
					case RQualifiedType.EVENT_TYPE.qualifiedType:
						RQualifiedType.EVENT_TYPE
					default:
						new RErrorType(
							'No such qualified type: ' + expression.name + " '" +
								NodeModelUtils.findActualNodeFor(expression)?.text + "'")
				}
			RosettaBasicType:{
				val typeName = idConverter.toValue(expression.name, null)
				switch typeName {
					case 'boolean':
						RBuiltinType.BOOLEAN
					case 'string':
						RBuiltinType.STRING
					case 'int':
						RBuiltinType.INT
					case 'number':
						RBuiltinType.NUMBER
					case 'time':
						RBuiltinType.TIME
					case 'date':
						RBuiltinType.DATE
					case 'dateTime':
						RBuiltinType.DATE_TIME
					case 'zonedDateTime':
						RBuiltinType.ZONED_DATE_TIME
					default:
						new RErrorType(
							'No such built-in type: ' + expression.name + " '" +
								NodeModelUtils.findActualNodeFor(expression)?.text + "'")
				}
			}
			RosettaParenthesisCalcExpression:
				expression.expression.safeRType(cycleTracker)
			RosettaConditionalExpression: {
				val ifT = expression.ifthen.safeRType(cycleTracker)
				if (expression.elsethen === null) {
					ifT
				} else {
					val elseT = expression.elsethen.safeRType(cycleTracker)
					if (ifT instanceof RErrorType) {
						elseT
					} else if (elseT instanceof RErrorType) {
						ifT
					} else if (compatibility.isUseableAs(ifT, elseT)) {
						elseT
					} else if (compatibility.isUseableAs(elseT, ifT)) {
						ifT
					} else {
						new RErrorType("Can not infer common type for '" + ifT.name + "' and " + elseT.name + "'.")
					}
				}
			}
			RosettaMapPathValue:
				RBuiltinType.STRING
			RosettaMapPath:
				expression.path.safeRType(cycleTracker)
			RosettaMapRosettaPath:
				expression.path.safeRType(cycleTracker)
			Function:
				if(expression.output !== null) expression.output.safeRType(cycleTracker) else RBuiltinType.MISSING
			Condition:
				expression.expression.safeRType(cycleTracker)
			default:
				RBuiltinType.MISSING
		}
	}
	
	
	private def wrapInFeatureCallType(RType expressionType, RosettaExpression expression) {
		if (!(expressionType instanceof RFeatureCallType) && isFeatureCallTypeContext(expression))
			new RFeatureCallType(expressionType)
		else
			expressionType
	}

	private def boolean isFeatureCallTypeContext(EObject expression) {
		switch expression.eContainer {
			RosettaExistsExpression,
			RosettaAbsentExpression,
			RosettaAlias:
				true
			RosettaBinaryOperation:
				expression.eContainer.featureCallTypeContext
			default:
				false
		}
	}

}
