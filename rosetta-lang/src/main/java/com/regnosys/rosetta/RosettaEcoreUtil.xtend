package com.regnosys.rosetta

import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.RosettaFeature
import com.regnosys.rosetta.rosetta.RosettaRecordType
import com.regnosys.rosetta.rosetta.RosettaSynonym
import com.regnosys.rosetta.rosetta.expression.ChoiceOperation
import com.regnosys.rosetta.rosetta.expression.OneOfOperation
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.Condition
import com.regnosys.rosetta.rosetta.simple.Data
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
import com.regnosys.rosetta.rosetta.RosettaExternalRegularAttribute
import org.eclipse.xtext.EcoreUtil2
import com.regnosys.rosetta.rosetta.ExternalAnnotationSource
import com.regnosys.rosetta.rosetta.RosettaExternalClass
import com.regnosys.rosetta.rosetta.RosettaExternalSynonymSource
import java.util.stream.Collectors
import com.regnosys.rosetta.rosetta.RosettaExternalRuleSource

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
	def List<EObject> getParentsOfExternalType(RosettaExternalClass externalType) {
		val source = EcoreUtil2.getContainerOfType(externalType, ExternalAnnotationSource)
		if (source === null) {
			return emptyList
		}
		
		val type = externalType.data
		val parents = newLinkedHashSet
		var RosettaExternalClass superTypeInSource = null
		if (type.superType !== null) {
			superTypeInSource = findSuperTypeInSource(type.superType, null, source)
			if (superTypeInSource !== null) {
				parents.add(superTypeInSource)
			}
		}
		
		val superSources = getSuperSources(source)
		if (superSources.isEmpty) {
			parents.add(type)
		} else {
			val visitedSources = newHashSet
			visitedSources.add(source)
			val stop = superTypeInSource?.data
			superSources.forEach[
				collectParentsOfTypeInSource(parents, type, stop, it, visitedSources)
			]
		}
				
		return parents.toList
	}
	private def void collectParentsOfTypeInSource(Set<EObject> parents, Data type, Data stop, ExternalAnnotationSource currentSource, Set<ExternalAnnotationSource> visitedSources) {
		if (!visitedSources.add(currentSource)) {
			return
		}
		
		val superTypeInSource = findSuperTypeInSource(type, stop, currentSource)
		if (superTypeInSource !== null) {
			parents.add(superTypeInSource)
		}
		val superSources = getSuperSources(currentSource)
		if (superSources.isEmpty) {
			parents.add(type)
		} else {
			superSources.forEach[
				collectParentsOfTypeInSource(parents, type, superTypeInSource?.data ?: stop, it, visitedSources)
			]
		}
	}
	private def RosettaExternalClass findSuperTypeInSource(Data type, Data stop, ExternalAnnotationSource source) {
		val visitedTypes = newHashSet
		var current = type
		
		while (current !== null && current != stop && !visitedTypes.add(current)) {
			val d = current
			val externalType = source.externalClasses.findFirst[data == d]
			if (externalType !== null) {
				return externalType
			}
			current = current.superType
		}
		return null
	}
	private def List<ExternalAnnotationSource> getSuperSources(ExternalAnnotationSource source) {
		if (source instanceof RosettaExternalSynonymSource) {
			return source.getSuperSources().stream()
					.filter[s| s instanceof ExternalAnnotationSource]
					.map[s| s as ExternalAnnotationSource]
					.collect(Collectors.toList());
		} else if (source instanceof RosettaExternalRuleSource) {
			return source.getSuperSources();
		} else {
			return emptyList
		}
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
	def boolean isConstraintCondition(Condition cond) {
		return cond.isOneOf || cond.isChoice
	}
	
	private def boolean isOneOf(Condition cond) {
		return cond.expression instanceof OneOfOperation
	}
	
	private def boolean isChoice(Condition cond) {
		return cond.expression instanceof ChoiceOperation
	}
	
 	private def List<RosettaFeature> getMetaDescriptions(RMetaAnnotatedType type, EObject context) {
 		type.metaAttributes.getMetaDescriptions(context)
 	}
}
