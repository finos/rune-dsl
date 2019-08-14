package com.regnosys.rosetta.types

import com.google.inject.Inject
import com.regnosys.rosetta.rosetta.RosettaAbsentExpression
import com.regnosys.rosetta.rosetta.RosettaAlias
import com.regnosys.rosetta.rosetta.RosettaArgumentFeature
import com.regnosys.rosetta.rosetta.RosettaBasicType
import com.regnosys.rosetta.rosetta.RosettaBigDecimalLiteral
import com.regnosys.rosetta.rosetta.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.RosettaBooleanLiteral
import com.regnosys.rosetta.rosetta.RosettaCalculation
import com.regnosys.rosetta.rosetta.RosettaCalculationFeature
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
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import org.eclipse.xtext.resource.impl.ResourceDescriptionsProvider

import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*
import com.regnosys.rosetta.rosetta.RosettaExternalFunction
import com.regnosys.rosetta.rosetta.RosettaFunction
import com.regnosys.rosetta.rosetta.simple.Attribute
import org.eclipse.xtext.conversion.impl.IDValueConverter
import com.regnosys.rosetta.rosetta.simple.Function

class RosettaTypeProvider {

	@Inject extension ResourceDescriptionsProvider
	@Inject extension RosettaOperators
	@Inject IDValueConverter idConverter

	def RType getRType(EObject expression) {
		switch expression {
			RosettaCallableCall:
				expression.callable.RType.wrapInFeatureCallType(expression)
			RosettaCallableWithArgsCall: {
				if (expression.callable instanceof RosettaExternalFunction) {
					val fun = expression.callable as RosettaExternalFunction
					val returnType = fun.RType
					// Generic return type for number type e.g. Min(1,2) or Max(2,6)
					if (returnType == RBuiltinType.NUMBER && expression.args.forall[it.RType == RBuiltinType.INT]) {
						RBuiltinType.INT
					} else {
						returnType
					}
				} else if (expression.callable instanceof RosettaFunction) {
					val fun = expression.callable as RosettaFunction
					fun.RType
				} else {
					expression.callable.RType.wrapInFeatureCallType(expression)
				}
			}
			RosettaClass:
				new RClassType(expression)
			RosettaAlias: {
				if (expression.expression.eAllContents.filter(RosettaCallableCall).findFirst[expression == it.callable] === null) {
					val expressionType = expression.expression.RType
					if (expressionType instanceof RFeatureCallType)
						return expressionType
					else
						new RFeatureCallType(expressionType)
				} else {
					// TODO add better recursion detector
					new RErrorType('Can not compute type for ' + expression.name + " because of recursive call.")
				}
			}
			RosettaGroupByFeatureCall: {
				expression.call.RType.wrapInFeatureCallType(expression)
			}
			RosettaFeatureCall: {
				if (expression.feature.isTypeInferred) {
					expression.feature.RType.wrapInFeatureCallType(expression)
				} else {
					val type = expression.feature?.type
					type.RType.wrapInFeatureCallType(expression)
				}
			}
			RosettaRecordType:
				new RRecordType(expression)
			RosettaEnumValueReference: {
				new REnumType(expression.enumeration)
			}
			RosettaEnumeration: {
				val enumType = new REnumType(expression)
				val conversion = enumType.typeConversion
				val convertedType = conversion?.RType
				if (convertedType !== null) {
					new RUnionType(enumType, convertedType, conversion)
				} else {
					enumType
				}
			}
			RosettaBinaryOperation: {
				// Synonym path expressions refer to external documents so type checking is not possible
				if (expression.left instanceof RosettaMapPathValue) {
					return null
				}
				val left = expression.left
				var leftType = left.RType
				if (leftType instanceof RErrorType) {
					return null
				}
				val right = expression.right
				var rightType = right.RType
				if (rightType instanceof RErrorType) {
					return null
				}
				expression.operator.resultType(leftType, rightType)
			}
			RosettaCountOperation: {
				val leftType = expression.left.RType
				if (leftType instanceof RErrorType) {
					return null
				}
				val rightType = expression.right.RType
				if (rightType instanceof RErrorType) {
					return null
				}
				expression.operator.resultType(RBuiltinType.INT, rightType)
			}
			RosettaWhenPresentExpression: {
				val leftType = expression.left.RType
				if (leftType instanceof RErrorType) {
					return null
				}
				val rightType = expression.right.RType
				if (rightType instanceof RErrorType) {
					return null
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
			RosettaCalculation,
			RosettaExternalFunction: {
				if (expression.features.size === 1 && expression.features.head.name === null) {
					if (expression.features.head.isTypeInferred)
						expression.features.head.RType
					else 
						expression.features.head.type.RType
				}
				else if (expression instanceof RosettaExternalFunction && (expression as RosettaExternalFunction).type !== null)
					(expression as RosettaExternalFunction).type.RType
				else
					new RRecordType(expression)
			}
			RosettaFunction: {
				expression.output.type.RType
			}
			RosettaArgumentFeature:
				expression.expression.RType
			RosettaCalculationFeature: {
				if (expression.isIsTypeInferred)
					expression.expression.RType
				else
					expression.type.RType
			}
			RosettaFeature:
				expression.type.RType
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
					case 'function':
						RBuiltinType.FUNCTION
					default:
						new RErrorType(
							'No such built-in type: ' + expression.name + " '" +
								NodeModelUtils.findActualNodeFor(expression)?.text + "'")
				}
			}
			RosettaParenthesisCalcExpression:
				expression.expression.RType
			RosettaConditionalExpression:
				expression.ifthen.RType
			RosettaMapPathValue:
				RBuiltinType.STRING
			RosettaMapPath:
				expression.path.RType
			RosettaMapRosettaPath:
				expression.path.RType
			Attribute:
				expression.type.RType
			Function:
				expression.output.RType
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

	private def typeConversion(REnumType t0) {
		val index = t0.enumeration.eResource.resourceDescriptions
		val calculationDesc = index.getExportedObjectsByType(ROSETTA_CALCULATION).filter [
			qualifiedName.segments.size == 2 && qualifiedName.firstSegment == t0.enumeration.name
		].head
		if (calculationDesc !== null) {
			return EcoreUtil.resolve(calculationDesc.EObjectOrProxy, t0.enumeration) as RosettaCalculation
		}
		return null
	}
}
