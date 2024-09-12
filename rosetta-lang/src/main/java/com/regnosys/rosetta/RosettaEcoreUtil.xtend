package com.regnosys.rosetta

import com.google.common.base.CaseFormat
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.RosettaFeature
import com.regnosys.rosetta.rosetta.RosettaRecordType
import com.regnosys.rosetta.rosetta.RosettaSynonym
import com.regnosys.rosetta.rosetta.expression.ChoiceOperation
import com.regnosys.rosetta.rosetta.expression.OneOfOperation
import com.regnosys.rosetta.rosetta.simple.Annotated
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.Condition
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.types.RAttribute
import com.regnosys.rosetta.types.RDataType
import com.regnosys.rosetta.types.REnumType
import com.regnosys.rosetta.types.RType
import java.util.Collection
import java.util.List
import java.util.Set
import javax.inject.Inject
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.ResourceSet

import com.regnosys.rosetta.types.builtin.RRecordType
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService
import javax.inject.Singleton
import com.regnosys.rosetta.utils.PositiveIntegerInterval
import org.eclipse.xtext.util.SimpleCache
import com.regnosys.rosetta.rosetta.RosettaFactory
import com.regnosys.rosetta.scoping.RosettaScopeProvider
import com.regnosys.rosetta.rosetta.simple.SimpleFactory
import com.regnosys.rosetta.types.RObjectFactory
import java.util.LinkedHashSet
import com.regnosys.rosetta.types.RAliasType
import com.regnosys.rosetta.types.TypeSystem

@Singleton // see `metaFieldsCache`
class RosettaEcoreUtil {

	@Inject RBuiltinTypeService builtins
	@Inject RObjectFactory objectFactory
	@Inject extension TypeSystem

	def boolean isResolved(EObject obj) {
		obj !== null && !obj.eIsProxy
	}
	
	def Iterable<? extends RosettaFeature> allFeatures(RType t, EObject context) {
		allFeatures(t, context?.eResource?.resourceSet)
	}
	def Iterable<? extends RosettaFeature> allFeatures(RType t, ResourceSet resourceSet) {
		switch t {
			RDataType:
				t.allNonOverridenAttributes.map[EObject]
			REnumType:
				t.EObject.allEnumValues
			RRecordType: {
				if (resourceSet !== null) {
					builtins.toRosettaType(t, RosettaRecordType, resourceSet).features
				} else {
					#[]
				}
			}
			default:
				#[]
		}
	}
	
	@Deprecated // Use RDataType#getAllSuperTypes instead
	def List<Data> getAllSuperTypes(Data data) {
		val reversedResult = newLinkedHashSet
		doGetAllSuperTypes(data, reversedResult);
		reversedResult.toList.reverse
	}
	private def void doGetAllSuperTypes(Data current, LinkedHashSet<Data> superTypes) {
		if (superTypes.add(current)) {
			val s = current.superType.type
			if (s !== null && s instanceof Data) {
				doGetAllSuperTypes(s as Data, superTypes);
			}
		}
	}

  @Deprecated // Use RDataType#getAllAttributes instead
	private def Set<RDataType> doGetSuperTypes(RType t, Set<RDataType> seenTypes) {
		if(t !== null) {
			if (t instanceof RDataType) {
				if (seenTypes.add(t)) {
					val s = t.superType
					doGetSuperTypes(s, seenTypes)
				}
			} else if (t instanceof RAliasType) {
				val s = t.refersTo
				doGetSuperTypes(s, seenTypes)
			}
		}
		return seenTypes
	}

  	@Deprecated // Use RDataType#getAllAttributes instead
	def getAllAttributes(RDataType t) {
		t.allSuperTypes.filter(RDataType).flatMap[EObject.attributes]
	}

	@Deprecated // Use RDataType#getAllNonOverridenAttributes instead
	def Collection<Attribute> getAllNonOverridenAttributes(Data data) {
		val result = newLinkedHashMap
		data.attributes.forEach[result.put(name, it)]
		return result.values();
	}
	
	@Deprecated // TODO: move to REnumType, similar to RDataType
	def Set<RosettaEnumeration> getAllSuperEnumerations(RosettaEnumeration e) {
		doGetSuperEnumerations(e, newLinkedHashSet)
	}

	@Deprecated
	private def Set<RosettaEnumeration> doGetSuperEnumerations(RosettaEnumeration e, Set<RosettaEnumeration> seenEnums) {
		if(e !== null && seenEnums.add(e)) 
			doGetSuperEnumerations(e.superType, seenEnums)
		return seenEnums
	}

	@Deprecated // TODO: move to REnumType, similar to RDataType
	def getAllEnumValues(RosettaEnumeration e) {
		e.allSuperEnumerations.map[enumValues].flatten
	}
	
	def Set<RosettaSynonym> getAllSynonyms(RosettaSynonym s) {
		doGetSynonyms(s, newLinkedHashSet)
	}
		
	private def Set<RosettaSynonym> doGetSynonyms(RosettaSynonym s, Set<RosettaSynonym> seenSynonyms) {
		if(s !== null && seenSynonyms.add(s)) 
			doGetSynonyms(s, seenSynonyms)
		return seenSynonyms		
	}

	def metaAnnotations(Annotated it) {
		allAnnotations.filter[annotation?.name == "metadata"]
	}
	
	def hasKeyedAnnotation(Annotated it) {
		metaAnnotations.exists[attribute?.name == "key"]
	}
	
	def hasTemplateAnnotation(Annotated it) {
		metaAnnotations.exists[attribute?.name == "template"]
	}
	
	def boolean hasMetaDataAnnotations(RAttribute attribute) {
		attribute.metaAnnotations.exists[name == "reference" || name == "location" || name == "scheme" || name == "id"]
	}
	
	def boolean hasMetaDataAnnotations(Annotated it) {
		metaAnnotations.exists[attribute?.name == "reference" || attribute?.name == "location" || attribute?.name == "scheme" || attribute?.name == "id"]
	}
	
	def boolean hasMetaFieldAnnotations(Annotated it) {
		metaAnnotations.exists[attribute?.name != "reference" && attribute?.name != "address"]
	}
	
	def boolean hasMetaDataAddress(RAttribute attribute) {
		attribute.metaAnnotations.exists[name == "address"]
	}
	
	def boolean hasMetaDataAddress(Annotated it) {
		metaAnnotations.exists[attribute?.name == "address"]
	}
	
	def boolean hasIdAnnotation(Annotated it) {
		metaAnnotations.exists[attribute?.name == "id"]
	}
	def boolean hasIdAnnotation(RAttribute it) {
		metaAnnotations.exists[name == "id"]
	}
	def boolean hasReferenceAnnotation(Annotated it) {
		metaAnnotations.exists[attribute?.name == "reference"]
	}
	def hasCalculationAnnotation(Annotated it) {
		allAnnotations.exists[annotation?.name == "calculation"]
	}
	
	def boolean isReference(Attribute attribute) {
		return attribute.hasMetaDataAnnotations || attribute.hasMetaDataAddress
	}
	def boolean isReference(RAttribute attribute) {
		return attribute.hasMetaDataAnnotations || attribute.hasMetaDataAddress
	}
	
	def private allAnnotations(Annotated withAnnotations) {
		withAnnotations?.annotations?.filter[annotation.isResolved]
	}
	
	@Deprecated
	def String conditionName(Condition cond, RDataType t) {
		conditionName(cond, t.EObject)
	}

	@Deprecated
	def String conditionName(Condition cond, Data data) {
		return cond.conditionName(data.name, data.conditions)
	}

	@Deprecated
	def String conditionName(Condition cond, Function func) {
		return cond.conditionName(func.name, func.conditions)
	}
	
	@Deprecated
	def boolean isConstraintCondition(Condition cond) {
		return cond.isOneOf || cond.isChoice
	}
	
	private def boolean isOneOf(Condition cond) {
		return cond.expression instanceof OneOfOperation
	}
	
	private def boolean isChoice(Condition cond) {
		return cond.expression instanceof ChoiceOperation
	}
	
	//Name convention: <type name>(<condition name>|<condition type><#>) where condition type should be 'choice' or 'oneof'.
	private def String conditionName(Condition cond, String containerName, Collection<Condition> conditions) {
		val name = if (!cond.name.nullOrEmpty)
				cond.name
			else {
				val idx = conditions.filter[name.nullOrEmpty].toList.indexOf(cond)
				val type = if (cond.isOneOf) {
						'OneOf' 
					} else if (cond.isChoice) {
						 'Choice'
					} else 'DataRule'
				'''«type»«idx»'''
			}
		return '''«containerName»«name»'''
	}
	
	@Deprecated
	def String toConditionJavaType(String conditionName) {
		val allUnderscore = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, conditionName)
		val camel = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, allUnderscore)
		return camel
	}
	
	@Deprecated
	def String javaAnnotation(RAttribute attr) {
		if (attr.name == "key" && attr.RType.name == "Key" && attr.RType.namespace.toString == "com.rosetta.model.lib.meta") {
			return 'location'
		} else if (attr.name == "reference" && attr.RType.name == "Reference" && attr.RType.namespace.toString == "com.rosetta.model.lib.meta") {
			return 'address'
		} else
			return attr.name
	}
	// Copied over from RosettaAttributeExtensions.
	@Deprecated
	private def List<RAttribute> additionalAttributes(RDataType t) {
		val res = newArrayList
		if(hasKeyedAnnotation(t.EObject)){
			res.add(new RAttribute(
				'meta',
				null,
				emptyList,
				provideMetaFieldsType(t),
				emptyList,
				PositiveIntegerInterval.bounded(0, 1),
				null,
				null
			))
		}
		return res
	}
	private def List<RAttribute> valueAttribute(RDataType t) {
		val res = newArrayList
		val valueType = t.superType.stripFromTypeAliases
		if (valueType !== null && !(valueType instanceof RDataType)) {
			res.add(objectFactory.createArtificialAttribute("value", valueType, false))
		}
		return res
	}
	def List<RAttribute> javaAttributes(RDataType t) {
		(t.valueAttribute + t.ownAttributes + t.additionalAttributes).toList
	}
	def List<RAttribute> allJavaAttributes(RDataType t) {
		val atts = t.javaAttributes
		if (t.superType !== null && t.superType.stripFromTypeAliases instanceof RDataType) {
			val attsWithSuper = (t.superType.stripFromTypeAliases as RDataType).allJavaAttributes
			val result = newArrayList
			attsWithSuper.forEach[
				val overridenAtt = atts.findFirst[att| att.name == name]
				if (overridenAtt !== null) {
					result.add(overridenAtt)
				} else {
					result.add(it)
				}
			]
			result.addAll(atts.filter[att| !result.contains(att)].toList)
			return result
		}
		return atts
	}

	@Deprecated
	String METAFIELDS_CLASS_NAME = 'MetaFields'
	@Deprecated
	String META_AND_TEMPLATE_FIELDS_CLASS_NAME = 'MetaAndTemplateFields'

	@Deprecated
	SimpleCache<RDataType, RDataType> metaFieldsCache = new SimpleCache[RDataType t|
		val rosModel = RosettaFactory.eINSTANCE.createRosettaModel()
		rosModel.name = RosettaScopeProvider.LIB_NAMESPACE + ".metafields"
		val name = if (hasTemplateAnnotation(t.EObject)) META_AND_TEMPLATE_FIELDS_CLASS_NAME else METAFIELDS_CLASS_NAME
		val data = SimpleFactory.eINSTANCE.createData
		data.namespace = rosModel
		data.name = name
		return objectFactory.buildRDataType(data)
	]
	private def RType provideMetaFieldsType(RDataType t) {
		metaFieldsCache.get(t)
	}
}
