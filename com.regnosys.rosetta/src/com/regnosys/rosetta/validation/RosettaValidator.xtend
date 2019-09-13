/*
 * generated by Xtext 2.10.0
 */
package com.regnosys.rosetta.validation

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.HashMultimap
import com.google.common.collect.LinkedHashMultimap
import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions
import com.regnosys.rosetta.rosetta.RosettaAlias
import com.regnosys.rosetta.rosetta.RosettaArguments
import com.regnosys.rosetta.rosetta.RosettaBlueprint
import com.regnosys.rosetta.rosetta.RosettaCalculation
import com.regnosys.rosetta.rosetta.RosettaCallableCall
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgsCall
import com.regnosys.rosetta.rosetta.RosettaChoiceRule
import com.regnosys.rosetta.rosetta.RosettaClass
import com.regnosys.rosetta.rosetta.RosettaDataRule
import com.regnosys.rosetta.rosetta.RosettaEnumValueReference
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.RosettaEvent
import com.regnosys.rosetta.rosetta.RosettaExternalFunction
import com.regnosys.rosetta.rosetta.RosettaFeature
import com.regnosys.rosetta.rosetta.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.RosettaFeatureOwner
import com.regnosys.rosetta.rosetta.RosettaFunction
import com.regnosys.rosetta.rosetta.RosettaGroupByFeatureCall
import com.regnosys.rosetta.rosetta.RosettaMapping
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.rosetta.RosettaNamed
import com.regnosys.rosetta.rosetta.RosettaParameter
import com.regnosys.rosetta.rosetta.RosettaProduct
import com.regnosys.rosetta.rosetta.RosettaQualifiable
import com.regnosys.rosetta.rosetta.RosettaRegularAttribute
import com.regnosys.rosetta.rosetta.RosettaTreeNode
import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.rosetta.RosettaWorkflowRule
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.simple.FunctionDispatch
import com.regnosys.rosetta.types.RBuiltinType
import com.regnosys.rosetta.types.RErrorType
import com.regnosys.rosetta.types.RRecordType
import com.regnosys.rosetta.types.RType
import com.regnosys.rosetta.types.RosettaExpectedTypeProvider
import com.regnosys.rosetta.types.RosettaTypeCompatibility
import com.regnosys.rosetta.types.RosettaTypeProvider
import com.regnosys.rosetta.utils.RosettaQualifiableExtension
import com.regnosys.rosetta.validation.RosettaBlueprintTypeResolver.BlueprintUnresolvedTypeException
import java.util.List
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EReference
import org.eclipse.xtext.naming.IQualifiedNameConverter
import org.eclipse.xtext.naming.IQualifiedNameProvider
import org.eclipse.xtext.resource.impl.ResourceDescriptionsProvider
import org.eclipse.xtext.validation.Check

import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*
import static com.regnosys.rosetta.rosetta.simple.SimplePackage.Literals.*
import static org.eclipse.xtext.EcoreUtil2.*
import static org.eclipse.xtext.nodemodel.util.NodeModelUtils.*

import static extension org.eclipse.emf.ecore.util.EcoreUtil.*
import com.regnosys.rosetta.rosetta.simple.Data

/**
 * This class contains custom validation rules. 
 * 
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#validation
 */
class RosettaValidator extends AbstractRosettaValidator implements RosettaIssueCodes {

	@Inject extension RosettaExtensions
	@Inject extension RosettaExpectedTypeProvider
	@Inject extension RosettaTypeProvider
	@Inject extension RosettaQualifiableExtension qualifiableExtension
	@Inject extension RosettaTypeCompatibility
	@Inject extension IQualifiedNameConverter
	@Inject extension IQualifiedNameProvider
	@Inject extension ResourceDescriptionsProvider
	@Inject extension RosettaBlueprintTypeResolver
	@Inject extension RosettaFunctionExtensions

	@Check
	def void checkClassNameStartsWithCapital(RosettaClass classe) {
		if (!Character.isUpperCase(classe.name.charAt(0))) {
			warning("Class name should start with a capital", ROSETTA_NAMED__NAME, INVALID_CASE)
		}
	}

	@Check
	def void checkEnumerationNameStartsWithCapital(RosettaEnumeration enumeration) {
		if (!Character.isUpperCase(enumeration.name.charAt(0))) {
			warning("Enumeration name should start with a capital", ROSETTA_NAMED__NAME, INVALID_CASE)
		}
	}

	@Check
	def void checkAttributeNameStartsWithLowerCase(RosettaRegularAttribute attribute) {
		if (!Character.isLowerCase(attribute.name.charAt(0))) {
			warning("Attribute name should start with a lower case", ROSETTA_NAMED__NAME, INVALID_CASE)
		}
	}

	@Check
	def void checkDataRuleThen(RosettaDataRule rule) {
		if (rule.then === null) {
			error('''Add missing 'then' expression.''', rule, ROSETTA_NAMED__NAME)
		}
	}

	@Check
	def void checkDataRuleNameStartsWithUpperCase(RosettaDataRule rule) {
		if (!Character.isUpperCase(rule.name.charAt(0))) {
			warning("Data rule name should start with a capital", ROSETTA_NAMED__NAME, INVALID_CASE)
		}
	}

	@Check
	def void checkWorkflowRuleNameStartsWithUpperCase(RosettaWorkflowRule rule) {
		if (!Character.isUpperCase(rule.name.charAt(0))) {
			warning("Workflow rule name should start with a capital", ROSETTA_NAMED__NAME, INVALID_CASE)
		}
	}

	@Check
	def void checkWorkflowRuleCommonIdentifier(RosettaWorkflowRule rule) {
		val identifier = rule.commonIdentifier
		if (identifier !== null) {
			rule.root.checkAttributeExists(identifier.name, identifier.type)
		}
	}

	@Check
	def void checkEventQualifierNameStartsWithUpperCase(RosettaEvent event) {
		if (!Character.isUpperCase(event.name.charAt(0))) {
			warning("Event qualifier name should start with a capital", ROSETTA_NAMED__NAME, INVALID_CASE)
		}
	}

	@Check
	def void checkProductQualifierNameStartsWithUpperCase(RosettaProduct product) {
		if (!Character.isUpperCase(product.name.charAt(0))) {
			warning("Product qualifier name should start with a capital", ROSETTA_NAMED__NAME, INVALID_CASE)
		}
	}

	@Check
	def void checkAliasNameStartsWithLowerCase(RosettaAlias alias) {
		if (!Character.isLowerCase(alias.name.charAt(0))) {
			warning("Alias name should start with a lower case", ROSETTA_NAMED__NAME, INVALID_CASE)
		}
	}

	private def void checkAttributeExists(RosettaTreeNode node, String expectedName, RosettaType expectedType) {
		node.children.forEach [
			if (parent !== null && !parent.eIsProxy) {
				val attribute = parent.allAttributes.findFirst[name == expectedName]
				if (attribute === null)
					error('''Class '«parent.name»' does not have an attribute '«expectedName»'«»''', it,
						ROSETTA_TREE_NODE__PARENT, MISSING_ATTRIBUTE)
				else if (attribute.getType != expectedType)
					error('''Attribute '«attribute.name»' of class '«parent.name»' is of type '«attribute.type.name»' (expected '«expectedType.name»')''',
						it, ROSETTA_TREE_NODE__PARENT, TYPE_ERROR)
				checkAttributeExists(expectedName, expectedType)
			}
		]
	}

	@Check
	def checkTypeExpectation(EObject owner) {
		owner.eClass.EAllReferences.filter[ROSETTA_EXPRESSION.isSuperTypeOf(EReferenceType)].filter[owner.eIsSet(it)].
			forEach [ ref |
				val referenceValue = owner.eGet(ref)
				if (ref.isMany) {
					(referenceValue as List<? extends EObject>).forEach [ it, i |
						val expectedType = owner.getExpectedType(ref, i)
						checkType(expectedType, it, owner, ref, i)
					]
				} else {
					val expectedType = owner.getExpectedType(ref)
					checkType(expectedType, referenceValue as EObject, owner, ref, INSIGNIFICANT_INDEX)
				}
			]
	}

	private def checkType(RType expectedType, EObject object, EObject owner, EReference ref, int index) {
		val actualType = object.RType
		if (actualType === null || actualType == RBuiltinType.ANY) {
			return
		}
		if (actualType instanceof RErrorType)
			error('''«actualType.name»''', owner, ref, index, TYPE_ERROR)
		else if (actualType == RBuiltinType.MISSING)
			error('''Couldn't infer actual type for '«getTokenText(findActualNodeFor(object))»'«»''', owner, ref, index,
				TYPE_ERROR)
		else if (expectedType instanceof RErrorType)
			error('''«expectedType.name»''', owner, ref, index, TYPE_ERROR)
		else if (expectedType !== null && !actualType.isUseableAs(expectedType) && expectedType != RBuiltinType.MISSING)
			error('''Expected type '«expectedType.name»' but was '«actualType?.name ?: 'null'»'«»''', owner, ref, index,
				TYPE_ERROR)
	}

	@Check
	def void checkFeatureCallGroupByAttribute(RosettaGroupByFeatureCall featureCallGroupBy) {
		val groupByExp = featureCallGroupBy.groupBy
		if (groupByExp !== null) {
			val featureCall = featureCallGroupBy.call
			switch (featureCall) {
				RosettaFeatureCall: {
					val parentType = featureCall.feature.type
					switch (parentType) {
						RosettaClass: {
							// must have single cardinality in group by function
							var gbe = groupByExp
							while (gbe !== null) {
								if (gbe.attribute.card.isIsMany) {
									error('''attribute «gbe.attribute.name» of «(gbe.attribute.eContainer as RosettaClass).name» has multiple cardinality. Group by expressions must be single''',
										featureCallGroupBy, ROSETTA_GROUP_BY_FEATURE_CALL__GROUP_BY, CARDINALITY_ERROR)
									return
								}
								gbe = gbe.right
							}
						}
						default: {
							error('''Parent of group by «featureCall.feature.type.name» by must be a class''',
								featureCallGroupBy, ROSETTA_GROUP_BY_FEATURE_CALL__GROUP_BY, INVALID_TYPE)
						}
					}
				}
			}
		}
	}

	@Check
	def checkCollectionTypeCall(RosettaFeatureCall element) {
		val feature = element.feature
		switch (feature) {
			RosettaRegularAttribute case feature.card.isIsMany,
			RosettaParameter case feature.isIsArray: {
				if (getContainerOfType(element, RosettaArguments) !== null) {
					error('''Can't map from collection of '«feature.type.name»' to a single value''', element,
						ROSETTA_FEATURE_CALL__FEATURE)
				}
			}
		}
	}

	@Check
	def checkAttributeNamesAreUnique(RosettaClass clazz) {
		val name2attr = HashMultimap.create
		clazz.allAttributes.forEach [
			name2attr.put(name, it)
		]
		for (name : clazz.regularAttributes.map[name]) {
			val attrByName = name2attr.get(name)
			if (attrByName.size > 1) {
				val fromSuperClasses = attrByName.filter[eContainer != clazz]
				val messageExtension = if (fromSuperClasses.empty)
						''
					else
						' (overrides ' + fromSuperClasses.map[(eContainer as RosettaNamed).name].join(', ') + ')'
				attrByName.filter[eContainer == clazz].forEach [
					error('''Duplicate attribute '«name»'«messageExtension»''', it, ROSETTA_NAMED__NAME,
						DUPLICATE_ATTRIBUTE)
				]
			}
		}
	}

	@Check
	def checkEnumValuesAreUnique(RosettaEnumeration enumeration) {
		val name2attr = HashMultimap.create
		enumeration.allEnumValues.forEach [
			name2attr.put(name, it)
		]
		for (value : enumeration.enumValues) {
			val valuesByName = name2attr.get(value.name)
			if (valuesByName.size > 1) {
				error('''Duplicate enum value '«value.name»'«»''', value, ROSETTA_NAMED__NAME, DUPLICATE_ENUM_VALUE)
			}
		}
	}

	@Check
	def checkChoiceRuleAttributesAreUnique(RosettaChoiceRule choiceRule) {
		val name2attr = ArrayListMultimap.create
		choiceRule.thatOnes.forEach [
			name2attr.put(name, it)
		]
		for (value : choiceRule.thatOnes) {
			val attributeByName = name2attr.get(value.name)
			if (attributeByName.size > 1) {
				error('''Duplicate attribute '«value.name»'«»''', ROSETTA_NAMED__NAME, DUPLICATE_CHOICE_RULE_ATTRIBUTE)
			}
			if (value.name == choiceRule.thisOne.name) {
				error('''Duplicate attribute '«value.name»'«»''', ROSETTA_NAMED__NAME, DUPLICATE_CHOICE_RULE_ATTRIBUTE)
			}
		}
	}

	@Check
	def checkClassWithChoiceRuleAndOneOfRule(RosettaChoiceRule choiceRule) {
		if (choiceRule.scope.oneOf) {
			error('''Class «choiceRule.scope.name» has both choice rule («choiceRule.name») and one of rule.''',
				ROSETTA_NAMED__NAME, CLASS_WITH_CHOICE_RULE_AND_ONE_OF_RULE)
		}
	}

	@Check
	def checkFeatureNamesAreUnique(RosettaFeatureOwner ele) {
		ele.features.groupBy[name].forEach [ k, v |
			if (v.size > 1) {
				v.forEach [
					error('''Duplicate feature "«k»"''', it, ROSETTA_NAMED__NAME)
				]
			}
		]
	}

	// TODO This probably should be made namespace aware
	@Check(FAST) // switch to NORMAL if it becomes slow
	def checkTypeNamesAreUnique(RosettaModel model) {
		val name2attr = HashMultimap.create
		model.elements.filter(RosettaNamed).filter[!(it instanceof FunctionDispatch)].forEach [ // TODO better FunctionDispatch handling
			name2attr.put(name, it)
		]
		val resources = getResourceDescriptions(model.eResource)
		for (name : name2attr.keySet) {
			val valuesByName = name2attr.get(name)
			if (valuesByName.size > 1) {
				valuesByName.forEach [
					if (it.name !== null)
						error('''Duplicate element named '«name»'«»''', it, ROSETTA_NAMED__NAME, DUPLICATE_ELEMENT_NAME)
				]
			} else if (valuesByName.size == 1 && model.eResource.URI.isPlatformResource) {
				val EObject toCheck = valuesByName.get(0)
				val sameNamed = resources.getExportedObjects(toCheck.eClass(), toCheck.fullyQualifiedName, false).filter [
					isProjectLocal(model.eResource.URI, it.EObjectURI) && getEClass() !== FUNCTION_DISPATCH
				].map[EObjectURI]
				if (sameNamed.size > 1) {
					error('''Duplicate element named '«name»' in «sameNamed.filter[toCheck.URI != it].join(', ',[it.lastSegment])»''',
						toCheck, ROSETTA_NAMED__NAME, DUPLICATE_ELEMENT_NAME)
				}
			}
		}
	}

	@Check
	def checkForMulipleClassReferencesDefinedForDataRule(RosettaDataRule model) {
		val classRefs = newLinkedHashSet
		model.when.eAllContents.filter(RosettaCallableCall).forEach[collectRootCalls[classRefs.add(it)]]
		model.then.eAllContents.filter(RosettaCallableCall).forEach[collectRootCalls[classRefs.add(it)]]
		if (classRefs.size > 1) {
			warning('''Data rule "«model.name»" has multiple class references «classRefs.join(', ')[name]». Data rules when/then should always start from the same class''',
				model, ROSETTA_NAMED__NAME, MULIPLE_CLASS_REFERENCES_DEFINED_FOR_DATA_RULE)
		}
	}

	@Check
	def checkUniqueRootClassForRosettaQualifiable(RosettaQualifiable ele) {
		val usedClasses = collectRootCalls(ele)

		val qualifiableType = switch (ele) { RosettaProduct: 'isProduct' RosettaEvent: 'isEvent' default: 'unknown Qualifiable' }
		if (usedClasses.size > 1) {
			error('''«qualifiableType» "«ele.name»" has multiple class references «usedClasses.join(', ',[name])». isProduct expressions should always start from the same class''',
				ele, ROSETTA_NAMED__NAME, MULIPLE_CLASS_REFERENCES_DEFINED_FOR_ROSETTA_QUALIFIABLE)
		}
		if (usedClasses.size == 1) {
			val allowedClass = switch (ele) { RosettaProduct: findProductRootName(ele) RosettaEvent: findEventRootName(
				ele) default: null }
			if (allowedClass !== null && usedClasses.head.name != allowedClass) {
				error('''«qualifiableType» expressions should always start from the '«allowedClass»' class. But found '«usedClasses.head.name»'.''',
					ele, ROSETTA_NAMED__NAME, MULIPLE_CLASS_REFERENCES_DEFINED_FOR_ROSETTA_QUALIFIABLE)
			}
		}
	}

	@Check
	def checkMappingSetToCase(RosettaMapping element) {
		if (element.instances.filter[^set !== null && when === null].size > 1) {
			error('''Only one set to with no when clause allowed.''', element, ROSETTA_MAPPING__INSTANCES)
		}
		if (element.instances.filter[^set !== null && when === null].size == 1) {
			val defaultInstance = element.instances.findFirst[^set !== null && when === null]
			val lastInstance = element.instances.last
			if (defaultInstance !== lastInstance) {
				error('''Set to without when case must be ordered last.''', element, ROSETTA_MAPPING__INSTANCES)
			}
		}
		val attribute = element.eContainer.eContainer.eContainer as RosettaRegularAttribute
		val type = attribute.getType
		if (type instanceof RosettaClass && !element.instances.filter[^set !== null].empty) {
			error('''Set to constant type does not match type of field.''', element, ROSETTA_MAPPING__INSTANCES)
		} else if (type instanceof RosettaEnumeration) {
			for (inst : element.instances.filter[^set !== null]) {
				if (!(inst.set instanceof RosettaEnumValueReference)) {
					error('''Set to constant type does not match type of field.''', element, ROSETTA_MAPPING__INSTANCES)
				} else {
					val setEnum = inst.set as RosettaEnumValueReference
					if (type.name != setEnum.enumeration.name) {
						error('''Set to constant type does not match type of field.''', element,
							ROSETTA_MAPPING__INSTANCES)
					}
				}
			}
		}
	}

	@Check
	def checkMappingDefaultCase(RosettaMapping element) {
		if (element.instances.filter[^default].size > 1) {
			error('''Only one default case allowed.''', element, ROSETTA_MAPPING__INSTANCES)
		}
		if (element.instances.filter[^default].size == 1) {
			val defaultInstance = element.instances.findFirst[^default]
			val lastInstance = element.instances.last
			if (defaultInstance !== lastInstance) {
				error('''Default case must be ordered last.''', element, ROSETTA_MAPPING__INSTANCES)
			}
		}
	}

	@Check
	def checkCalculationName(RosettaCalculation calculation) {
		val qualifiedName = calculation.name?.toQualifiedName
		if (qualifiedName !== null && qualifiedName.segmentCount == 2) {
			val index = calculation.eResource.resourceDescriptions
			val enumValues = index.getExportedObjects(ROSETTA_ENUM_VALUE, qualifiedName, false)
			if (enumValues.empty) {
				error('''Cannot find enum value '«qualifiedName.toString»'«»''', calculation, ROSETTA_NAMED__NAME, -1,
					MISSING_ENUM_VALUE)
			} else {
				val thisType = calculation.RType

				val step1 = index.getExportedObjectsByType(ROSETTA_CALCULATION).filter [ d |
					d.qualifiedName.firstSegment == qualifiedName.firstSegment
				].map[EObjectOrProxy.resolve(calculation)].filter[!eIsProxy]

				val step2 = step1.map[RType]

				val calcsWithOtherTypes = step2.filter[!typesEqual(it, thisType)].map[name].toSet
				if (!calcsWithOtherTypes.empty)
					error('''All calculations for enum '«qualifiedName.firstSegment»' must have same return type. (expected «thisType.name» but was «calcsWithOtherTypes.join(',')»)''',
						calculation, ROSETTA_NAMED__NAME, -1, TYPE_ERROR)
			}
		}
	}

	def boolean typesEqual(RType type, RType type2) {
		if (type instanceof RRecordType && type2 instanceof RRecordType) {
			val record = (type as RRecordType).record
			val record2 = (type2 as RRecordType).record
			if (record instanceof RosettaCalculation && record2 instanceof RosettaCalculation) {
				if (record.features.size != record.features.size) {
					return false
				}
				val mapper = [RosettaFeature it|it.name + it.type.name]
				return record.features.map(mapper).containsAll(record2.features.map(mapper))
			}
		}
		return type == type2
	}

	@Check
	def checkFunctionCall(RosettaCallableWithArgsCall element) {
		val callerSize = element.args.size
		val callable = element.callable

		val callableSize = switch callable {
			RosettaExternalFunction: callable.parameters.size
			RosettaFunction: callable.inputs.size
			Function: callable.inputs.size
			default: 0
		}
		if (callerSize !== callableSize) {
			error('''Invalid number of arguments. Expecting «callableSize» but passed «callerSize».''', element,
				ROSETTA_CALLABLE_WITH_ARGS_CALL__CALLABLE)
		} else {
			if (callable instanceof Function) {
				element.args.indexed.forEach [ indexed |
					val callerArg = indexed.value
					val param = callable.inputs.get(indexed.key)
					checkType(param.type.RType, callerArg, element, ROSETTA_CALLABLE_WITH_ARGS_CALL__ARGS, indexed.key)
				]
			}
		}
	}

	@Check
	def checkArgumentsType(RosettaArguments element) {
		val clazzUsages = element.classUsages
		val sortedUsages = clazzUsages.keySet.sortBy[clazzUsages.get(it).size].reverseView
		val calcName = element.calculation?.name
		if (calcName !== null && calcName.toQualifiedName.segmentCount == 1) { // TODO check Enum conversion calculation
			sortedUsages.filter(RosettaClass).forEach [ clazz |
				clazzUsages.get(clazz).forEach [
					if (!clazz.calculations.values.contains(element.calculation)) {
						error('''«clazz.name» doesn't refer to calculation «element.calculation.name».''', it,
							ROSETTA_CALLABLE_CALL__CALLABLE)
					}
				]
			]
		}
		if (clazzUsages.keySet.size > 0) {
			sortedUsages.tail.forEach [ clazz |
				clazzUsages.get(clazz).forEach [
					error('''Use common type in all expressions.''', it, ROSETTA_CALLABLE_CALL__CALLABLE)
				]
			]
		}

	}

	@Check
	def checkNodeTypeGraph(RosettaBlueprint bp) {
		try {
			buildTypeGraph(bp.nodes, bp.output)
		} catch (BlueprintUnresolvedTypeException e) {
			error(e.message, e.getEStructuralFeature, e.code, e.issueData)
		}
	}

	@Check
	def checkFuncDispatchAttr(FunctionDispatch ele) {
		if (ele.attribute !== null && ele.attribute.type !== null && !ele.attribute.type.eIsProxy) {
			if (!(ele.attribute.type instanceof RosettaEnumeration)) {
				error('''Dispatching function may refer to an enumeration typed attributes only. Current type is «ele.attribute.type.name»''', ele,
					FUNCTION_DISPATCH__ATTRIBUTE)
			}
		}
	}
	
	@Check
	def checkData(Data ele) {
		val onOfs = ele.conditions.groupBy[it.constraint.oneOf].get(Boolean.TRUE)
		if (onOfs.size > 1) {
			onOfs.forEach [
				error('''Only one 'oneOf' condition is allowed.''', it.constraint, null)
			]
		}
	}
	
	@Check
	def checkDispatch(Function ele) {
		if (ele instanceof FunctionDispatch)
			return
		val dispath = ele.dispatchingFunctions.toList
		if (dispath.empty)
			return
		val enumsUsed = LinkedHashMultimap.create
		dispath.forEach [
			val enumRef = it.value
			if (enumRef !== null && enumRef.enumeration !== null && enumRef.value !== null) {
				enumsUsed.put(enumRef.enumeration, enumRef.value.name -> it)
			}
		]
		val structured = enumsUsed.keys.map[it -> enumsUsed.get(it)]
		val mostUsedEnum = structured.max[$0.value.size <=> $1.value.size].key
		val toImplement = mostUsedEnum.allEnumValues.map[name].toSet
		enumsUsed.get(mostUsedEnum).forEach[
			toImplement.remove(it.key)
		]
		if (!toImplement.empty) {
			warning('''Missing implementation for «mostUsedEnum.name»: «toImplement.sort.join(', ')»''', ele,
				ROSETTA_NAMED__NAME)
		}
		structured.forEach [
			if (it.key != mostUsedEnum) {
				it.value.forEach [ entry |
					error('''Wrong «it.key.name» enumeration used. Expecting «mostUsedEnum.name».''', entry.value.value,
						ROSETTA_ENUM_VALUE_REFERENCE__ENUMERATION)
				]
			} else {
				it.value.groupBy[it.key].filter[enumVal, entries|entries.size > 1].forEach [ enumVal, entries |
					entries.forEach [
						error('''Dupplicate usage of «it.key» enumeration value.''', it.value.value,
							ROSETTA_ENUM_VALUE_REFERENCE__VALUE)
					]
				]
			}
		]
	}
}
