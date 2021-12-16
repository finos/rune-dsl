package com.regnosys.rosetta.types

import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.rosetta.RosettaAbsentExpression
import com.regnosys.rosetta.rosetta.RosettaBasicType
import com.regnosys.rosetta.rosetta.RosettaBigDecimalLiteral
import com.regnosys.rosetta.rosetta.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.RosettaBooleanLiteral
import com.regnosys.rosetta.rosetta.RosettaCalculationType
import com.regnosys.rosetta.rosetta.RosettaCallableCall
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
import com.regnosys.rosetta.rosetta.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.RosettaIntLiteral
import com.regnosys.rosetta.rosetta.RosettaMapPath
import com.regnosys.rosetta.rosetta.RosettaMapPathValue
import com.regnosys.rosetta.rosetta.RosettaMapRosettaPath
import com.regnosys.rosetta.rosetta.RosettaOnlyExistsExpression
import com.regnosys.rosetta.rosetta.RosettaParenthesisCalcExpression
import com.regnosys.rosetta.rosetta.RosettaQualifiedType
import com.regnosys.rosetta.rosetta.RosettaRecordType
import com.regnosys.rosetta.rosetta.RosettaStringLiteral
import com.regnosys.rosetta.rosetta.RosettaTyped
import com.regnosys.rosetta.rosetta.RosettaTypedFeature
import com.regnosys.rosetta.rosetta.simple.Annotated
import com.regnosys.rosetta.rosetta.simple.ClosureParameter
import com.regnosys.rosetta.rosetta.simple.Condition
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.simple.EmptyLiteral
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.simple.ListLiteral
import com.regnosys.rosetta.rosetta.simple.ListOperation
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration
import java.util.List
import java.util.Map
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.conversion.impl.IDValueConverter
import org.eclipse.xtext.naming.IQualifiedNameProvider
import org.eclipse.xtext.nodemodel.util.NodeModelUtils

class RosettaTypeProvider {

	@Inject extension RosettaOperators
	@Inject IQualifiedNameProvider qNames
	@Inject IDValueConverter idConverter
	@Inject RosettaTypeCompatibility compatibility
	@Inject RosettaExtensions extensions
	@Inject extension RosettaTypeCompatibility
	
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
		if (expression === null || expression.eIsProxy) {
			return null
		}
		switch expression {
			RosettaCallableCall: {
				if(expression.implicitReceiver)
					safeRType(EcoreUtil2.getContainerOfType(expression, ListOperation).firstOrImplicit, cycleTracker)
				else
					safeRType(expression.callable, cycleTracker)
			}
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
					expression.callable.safeRType(cycleTracker)
				}
			}
			Data:
				new RDataType(expression)
			ShortcutDeclaration: {
				cycleTracker.put(expression, null)
				val type = expression.expression.safeRType(cycleTracker)
				cycleTracker.put(expression, type)
				type
			} 
			RosettaFeatureCall: {
				val feature = expression.feature
				if(feature === null || feature.eIsProxy) {
					return null
				}
				switch (feature) {
					RosettaTypedFeature: {
						val featureType = if (feature.isTypeInferred) {
								feature.safeRType(cycleTracker)
							} else {
								val type = feature?.type
								type.safeRType(cycleTracker)
							}
						if (feature instanceof Annotated) {
							if (featureType instanceof RAnnotateType) {
								featureType.withMeta = extensions.hasMetaDataAnnotations(feature)
							}
						}
						featureType
					}
					RosettaEnumValue:
						feature.type.safeRType(cycleTracker)
					default:
						RBuiltinType.ANY
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
					return RBuiltinType.ANY
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
				RBuiltinType.INT
			}
			RosettaContainsExpression,
			RosettaDisjointExpression,
			RosettaOnlyExistsExpression,
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
				listType(expression.elements)
			RosettaExternalFunction: {
				expression.type.safeRType(cycleTracker)
			}
			RosettaEnumValue:
				expression.eContainer.safeRType(cycleTracker)
			RosettaTyped:
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
						if(expression.name !== null)
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
			ClosureParameter: {
				val setOp = EcoreUtil2.getContainerOfType(expression.operation, ListOperation) // TODO get container with opposite in xcore and use receiver
				if(setOp !== null) {
					setOp.receiver.safeRType(cycleTracker)
				} else
					RBuiltinType.MISSING
			}
			ListOperation:
				switch(expression.operationKind) {
					case FILTER:
						expression.firstOrImplicit.safeRType(cycleTracker)
					case MAP:
						expression.body.safeRType(cycleTracker)
					case FLATTEN:
						expression.receiver.safeRType(cycleTracker)
					case DISTINCT:
						expression.receiver.safeRType(cycleTracker)
					case ONLY_ELEMENT:
						expression.receiver.safeRType(cycleTracker)
					default: 
						RBuiltinType.MISSING
				}
			default:
				RBuiltinType.MISSING
		}
	}
	
	private def listType(List<RosettaExpression> exp) {
		val types = exp.map[RType]
		val result = types.reduce[p1, p2| parent(p1,p2)]
		if (result===null) return new RErrorType(types.groupBy[name].keySet.join(', '));
		return result;
	}
	
	private def RType parent(RType type1, RType type2) {
		if (type1===null || type2===null) {
			return null;
		}
		if (type1==type2) {
			return type1
		}
		if (type1.isUseableAs(type2)) return type2
		if (type2.isUseableAs(type1)) return type1
	}

}
