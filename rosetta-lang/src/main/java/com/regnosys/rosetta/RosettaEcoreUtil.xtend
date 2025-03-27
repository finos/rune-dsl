package com.regnosys.rosetta

import com.google.common.base.CaseFormat
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.RosettaFeature
import com.regnosys.rosetta.rosetta.RosettaRecordType
import com.regnosys.rosetta.rosetta.RosettaSynonym
import com.regnosys.rosetta.rosetta.expression.ChoiceOperation
import com.regnosys.rosetta.rosetta.expression.OneOfOperation
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.Condition
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.types.RChoiceType
import com.regnosys.rosetta.types.RDataType
import com.regnosys.rosetta.types.REnumType
import com.regnosys.rosetta.types.RMetaAnnotatedType
import com.regnosys.rosetta.types.RType
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService
import com.regnosys.rosetta.types.builtin.RRecordType
import com.regnosys.rosetta.utils.RosettaConfigExtension
import java.util.Collection
import java.util.LinkedHashSet
import java.util.List
import java.util.Set
import javax.inject.Inject
import javax.inject.Singleton
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.ResourceSet

import org.eclipse.emf.ecore.util.EcoreUtil
import com.regnosys.rosetta.rosetta.simple.Annotated
import java.util.function.Predicate
import com.regnosys.rosetta.types.RMetaAttribute

@Singleton // see `metaFieldsCache`
class RosettaEcoreUtil {
	
	@Inject RBuiltinTypeService builtins
	@Inject extension RosettaConfigExtension configs
	
	def boolean isResolved(EObject obj) {
		obj !== null && !obj.eIsProxy
	}
	
	def Iterable<? extends RosettaFeature> allFeatures(RMetaAnnotatedType t, EObject context) {
		val List<RosettaFeature>  metas = t === builtins.NOTHING_WITH_ANY_META ? newArrayList : getMetaDescriptions(t, context)
		allFeatures(t.RType, context?.eResource?.resourceSet) + metas
	}
	
	def Iterable<? extends RosettaFeature> allFeatures(RMetaAnnotatedType t, EObject context, Predicate<RType> restrictType) {
		val List<RosettaFeature>  metas = getMetaDescriptions(t, context)
		allFeatures(t.RType, context, restrictType) + metas
	}
	
	def Iterable<? extends RosettaFeature> allFeatures(RType t, EObject context) {
		allFeatures(t, context?.eResource?.resourceSet)
	}
	
	def Iterable<? extends RosettaFeature> allFeatures(RType t, EObject context, Predicate<RType> restrictType) {
		restrictType.test(t) ? allFeatures(t, context?.eResource?.resourceSet) : #[]
	}
	
	def Iterable<? extends RosettaFeature> allFeatures(RType t, ResourceSet resourceSet) {
		switch t {
			RDataType:
				t.allAttributes.map[EObject]
			RChoiceType:
				t.asRDataType.allFeatures(resourceSet)
			REnumType:
				t.allEnumValues
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
	
	/*
	 * This method is resolving references during scoping which is not an advised approach.
	 * It could lead to poor performance as it is possible that it could be called upon to
	 * resolve across multiple files. For now this is acceptable as in reality it's not going
	 * going to get called to run across multiple files.
	 * 
	 * TODO: find an alternative approach to this.
	 * 
	 */
    def List<RosettaFeature> getMetaDescriptions(List<RMetaAttribute> metaAttributes, EObject context) {
 		val metas = metaAttributes.map[it.name].toList
 		if (!metas.isEmpty) {
 			configs.findMetaTypes(context).filter[
 				metas.contains(it.name.lastSegment.toString)
 			]
 			.map[it.EObjectOrProxy]
			.map[EcoreUtil.resolve(it, context)]
 			.filter(RosettaFeature)
 			.toList
 		} else {
 			emptyList
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
			val s = current.getSuperType();
			if (s !== null) {
				doGetAllSuperTypes(s, superTypes);
			}
		}
	}
	
	@Deprecated // Use RDataType#getAllAttributes instead
	def Iterable<Attribute> getAllAttributes(Data data) {
		return data.allSuperTypes.flatMap[attributes]
	}
	
	@Deprecated // Use RDataType#getAllNonOverridenAttributes instead
	def Collection<Attribute> getAllNonOverridenAttributes(Data data) {
		val result = newLinkedHashMap
		data.allAttributes.forEach[result.put(name, it)]
		return result.values();
	}
	
	@Deprecated // Use REnumType#getAllParents instead
	def Set<RosettaEnumeration> getAllSuperEnumerations(RosettaEnumeration e) {
		doGetSuperEnumerations(e, newLinkedHashSet)
	}
	private def Set<RosettaEnumeration> doGetSuperEnumerations(RosettaEnumeration e, Set<RosettaEnumeration> seenEnums) {
		if(e !== null && seenEnums.add(e)) 
			doGetSuperEnumerations(e.parent, seenEnums)
		return seenEnums
	}
	
	@Deprecated // Use REnumType#getAllEnumValues instead
	def getAllEnumValues(RosettaEnumeration e) {
		e.allSuperEnumerations.map[enumValues].flatten
	}
	
	def Attribute getParentAttribute(Attribute attr) {
		val t = attr.eContainer
		if (t instanceof Data) {
			val visited = newHashSet
			visited.add(t)
			var st = t.superType
			while (st !== null) {
				val p = st.attributes.findFirst[name == attr.name]
				if (p !== null) {
					return p
				}
				st = st.superType
				if (!visited.add(st)) {
					return null
				}
			}
		}
		return null
	}
	
	def Set<RosettaSynonym> getAllSynonyms(RosettaSynonym s) {
		doGetSynonyms(s, newLinkedHashSet)
	}
		
	private def Set<RosettaSynonym> doGetSynonyms(RosettaSynonym s, Set<RosettaSynonym> seenSynonyms) {
		if(s !== null && seenSynonyms.add(s)) 
			doGetSynonyms(s, seenSynonyms)
		return seenSynonyms		
	}
	
	@Deprecated
	def metaAnnotations(Annotated it) {
		allAnnotations.filter[annotation?.name == "metadata"]
	}
	
	@Deprecated
	def hasKeyedAnnotation(Annotated it) {
		metaAnnotations.exists[attribute?.name == "key"]
	}
	
	@Deprecated
	def hasTemplateAnnotation(Annotated it) {
		metaAnnotations.exists[attribute?.name == "template"]
	}
	
	@Deprecated
	def boolean hasMetaDataAnnotations(Annotated it) {
		metaAnnotations.exists[attribute?.name == "reference" || attribute?.name == "location" || attribute?.name == "scheme" || attribute?.name == "id"]
	}
	
	@Deprecated
	def boolean hasMetaFieldAnnotations(Annotated it) {
		metaAnnotations.exists[attribute?.name != "reference" && attribute?.name != "address"]
	}
	
	@Deprecated
	def boolean hasMetaDataAddress(Annotated it) {
		metaAnnotations.exists[attribute?.name == "address"]
	}
	
	@Deprecated
	def boolean hasIdAnnotation(Annotated it) {
		metaAnnotations.exists[attribute?.name == "id"]
	}
	@Deprecated
	def boolean hasReferenceAnnotation(Annotated it) {
		metaAnnotations.exists[attribute?.name == "reference"]
	}
	@Deprecated
	def hasCalculationAnnotation(Annotated it) {
		allAnnotations.exists[annotation?.name == "calculation"]
	}
	@Deprecated
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
	
 	private def List<RosettaFeature> getMetaDescriptions(RMetaAnnotatedType type, EObject context) {
 		type.metaAttributes.getMetaDescriptions(context)
 	}
	
	@Deprecated
	def String toConditionJavaType(String conditionName) {
		val allUnderscore = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, conditionName)
		val camel = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, allUnderscore)
		return camel
	}
}
