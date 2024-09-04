package com.regnosys.rosetta

import com.google.common.base.CaseFormat
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.RosettaFeature
import com.regnosys.rosetta.rosetta.RosettaSynonym
import com.regnosys.rosetta.rosetta.expression.ChoiceOperation
import com.regnosys.rosetta.rosetta.expression.OneOfOperation
import com.regnosys.rosetta.rosetta.expression.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.expression.RosettaConditionalExpression
import com.regnosys.rosetta.rosetta.expression.RosettaExpression
import com.regnosys.rosetta.rosetta.simple.Annotated
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.Condition
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.types.RDataType
import com.regnosys.rosetta.types.REnumType
import com.regnosys.rosetta.types.RType
import java.util.Collection
import java.util.List
import java.util.Set
import javax.inject.Inject
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject

import com.regnosys.rosetta.types.builtin.RRecordType
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService
import org.eclipse.emf.ecore.resource.ResourceSet
import com.regnosys.rosetta.rosetta.RosettaRecordType
import com.regnosys.rosetta.types.RAttribute
import com.regnosys.rosetta.generator.object.ExpandedAttribute
import org.eclipse.xtext.util.SimpleCache
import com.regnosys.rosetta.generator.object.ExpandedType
import javax.inject.Singleton
import com.regnosys.rosetta.rosetta.RosettaFactory
import com.regnosys.rosetta.scoping.RosettaScopeProvider
import com.regnosys.rosetta.rosetta.RosettaEnumValue
import com.regnosys.rosetta.generator.object.ExpandedSynonym
import com.regnosys.rosetta.rosetta.RosettaEnumSynonym
import java.util.Collections
import com.regnosys.rosetta.generator.object.ExpandedSynonymValue
import com.regnosys.rosetta.rosetta.RosettaSynonymSource
import com.regnosys.rosetta.rosetta.RosettaExternalSynonym
import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.rosetta.RosettaSynonymValueBase
import com.regnosys.rosetta.rosetta.RosettaSynonymBase
import com.regnosys.rosetta.rosetta.RosettaExternalRegularAttribute
import com.regnosys.rosetta.rosetta.RosettaExternalClass
import com.regnosys.rosetta.rosetta.RosettaExternalSynonymSource
import com.regnosys.rosetta.rosetta.RosettaExternalClassSynonym
import com.regnosys.rosetta.rosetta.RosettaClassSynonym
import com.regnosys.rosetta.rosetta.RosettaMetaType

@Singleton // see `metaFieldsCache`
class RosettaExtensions {
	
	@Inject RBuiltinTypeService builtins
	
	def boolean isResolved(EObject obj) {
		obj !== null && !obj.eIsProxy
	}
	
	def Iterable<? extends RosettaFeature> allFeatures(RType t, EObject context) {
		allFeatures(t, context?.eResource?.resourceSet)
	}
	def Iterable<? extends RosettaFeature> allFeatures(RType t, ResourceSet resourceSet) {
		switch t {
			RDataType:
				t.allAttributes
			REnumType:
				t.enumeration.allEnumValues
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
	
//	def Set<RDataType> getAllSuperTypes(RDataType t) {
//		doGetSuperTypes(t, newLinkedHashSet)
//	}
//	
//	private def Set<RDataType> doGetSuperTypes(RDataType t, Set<RDataType> seenTypes) {
//		if(t !== null && seenTypes.add(t)) 
//			doGetSuperTypes(t.superType, seenTypes)
//		return seenTypes
//	}
//
//	def getAllAttributes(RDataType t) {
//		t.allSuperTypes.map[data.attributes].flatten
//	}
	
	def Set<RosettaEnumeration> getAllSuperEnumerations(RosettaEnumeration e) {
		doGetSuperEnumerations(e, newLinkedHashSet)
	}
	
//	 def List<Attribute>allNonOverridesAttributes(RDataType t) {
//		val atts = newArrayList;
//		atts.addAll(t.data.attributes)
//		if (t.superType !== null) {
//			val attsWithSuper = t.superType.allNonOverridesAttributes
//			val result = newArrayList
//			attsWithSuper.forEach[
//				val overridenAtt = atts.findFirst[att| att.name == name]
//				if (overridenAtt !== null) {
//					result.add(overridenAtt)
//				} else {
//					result.add(it)
//				}
//			]
//			result.addAll(atts.filter[att| !result.contains(att)].toList)
//			return result
//		}
//		return atts
//	}
	
	private def Set<RosettaEnumeration> doGetSuperEnumerations(RosettaEnumeration e, Set<RosettaEnumeration> seenEnums) {
		if(e !== null && seenEnums.add(e)) 
			doGetSuperEnumerations(e.superType, seenEnums)
		return seenEnums
	}

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

	/**
	 * Collect all expressions
	 */
	def void collectExpressions(RosettaExpression expr, (RosettaExpression) => void visitor) {
		if(expr instanceof RosettaBinaryOperation) {
			if(expr.operator.equals("or") || expr.operator.equals("and")) {
				expr.left.collectExpressions(visitor)
				expr.right.collectExpressions(visitor)
			}
			else {
				visitor.apply(expr)
			}	
		}
		if(expr instanceof RosettaConditionalExpression) {
			expr.ifthen.collectExpressions(visitor)
			expr.elsethen.collectExpressions(visitor)
		}
		else {
			visitor.apply(expr)
		}
	}
	
	def boolean isProjectLocal(URI platformResourceURI, URI candidateUri) {
		if (!platformResourceURI.isPlatformResource) {
			// synthetic tests URI
			return true
		}
		val projectName = platformResourceURI.segment(1)
		if (candidateUri.isPlatformResource) {
			return projectName == candidateUri.segment(1)
		}
		return false
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
	
	def String conditionName(Condition cond, Data data) {
		return cond.conditionName(data.name, data.conditions)
	}

	def String conditionName(Condition cond, Function func) {
		return cond.conditionName(func.name, func.conditions)
	}
	
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
	
	def String toConditionJavaType(String conditionName) {
		val allUnderscore = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, conditionName)
		val camel = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, allUnderscore)
		return camel
	}
	
	
	def String javaAnnotation(RAttribute attr) {
		if (attr.name == "key" && attr.RType.name == "Key" && attr.RType.namespace.toString == "com.rosetta.model.lib.meta") {
			return 'location'
		} else if (attr.name == "reference" && attr.RType.name == "Reference" && attr.RType.namespace.toString == "com.rosetta.model.lib.meta") {
			return 'address'
		} else
			return attr.name
	}
	// Copied over from RosettaAttributeExtensions. TODO: get rid of ExpandedAttribute, and use RAttribute instead.
	def List<ExpandedAttribute> getExpandedAttributes(RDataType t) {
		(t.data.attributes.map[toExpandedAttribute()].toList + t.additionalAttributes).toList
	}
	
	def List<ExpandedAttribute> expandedAttributesPlus(RDataType t) {
		val atts = t.expandedAttributes
		val s = t.superType
		if (s !== null) {
			val attsWithSuper = s.expandedAttributesPlus
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
	
	private def List<ExpandedAttribute> additionalAttributes(RDataType t) {
		val res = newArrayList
		if(hasKeyedAnnotation(t.data)){
			res.add(new ExpandedAttribute(
				'meta',
				t.name,
				provideMetaFieldsType(t),
				null,
				false,
				0,
				1,
				false,
				emptyList,
				"",
				emptyList,
				false,
				emptyList
			))
		}
		return res
	}
	
	String METAFIELDS_CLASS_NAME = 'MetaFields'
	String META_AND_TEMPLATE_FIELDS_CLASS_NAME = 'MetaAndTemplateFields'
	
	SimpleCache<RDataType, ExpandedType> metaFieldsCache = new SimpleCache[RDataType t|
		val rosModel = RosettaFactory.eINSTANCE.createRosettaModel()
		rosModel.name = RosettaScopeProvider.LIB_NAMESPACE
		val name = if (hasTemplateAnnotation(t.data)) META_AND_TEMPLATE_FIELDS_CLASS_NAME else METAFIELDS_CLASS_NAME
		return new ExpandedType(rosModel, name, true, false, false)
	]
	private def ExpandedType provideMetaFieldsType(RDataType t) {
		metaFieldsCache.get(t)
	}
	
	def List<ExpandedAttribute> getExpandedAttributes(RosettaEnumeration rosettaEnum) {
		rosettaEnum.enumValues.map[expandedEnumAttribute]
	}
	
	def ExpandedAttribute expandedEnumAttribute(RosettaEnumValue value) {
		new ExpandedAttribute(value.name,value.enumeration.name, null, null, false, 0,0, false, value.enumSynonyms.map[toExpandedSynonym], 
			value.definition, value.references, true, emptyList
		)
	}
	
	def ExpandedSynonym toExpandedSynonym(RosettaEnumSynonym syn) {
		new ExpandedSynonym(syn.sources, Collections.singletonList(new ExpandedSynonymValue(syn.synonymValue, null, 0, false)), newArrayList, null, Collections.emptyList, null, null,
			null, syn.patternMatch, syn.patternReplace, syn.removeHtml
		)
	}
	
	def toRosettaExpandedSynonym(Attribute attr, int index) {
		val s= attr.synonyms.filter[body.metaValues.size > index].map[
			s|new ExpandedSynonym(s.sources, metaSynValue(s.body.values,s.body.metaValues.get(index))
				//new ExpandedSynonymValue(s.metaValues.get(index), path+"."+value, maps, true)
			.toList, s.body.hints, s.body.merge, s.body.metaValues.map[new ExpandedSynonymValue(it, null, 1, true)], s.body.mappingLogic, 
				s.body.mapper, s.body.format, s.body.patternMatch, s.body.patternReplace, s.body.removeHtml)
		]
		s.toList
	}

	def toRosettaExpandedSynonym(List<RosettaSynonymSource> sources, List<RosettaExternalSynonym> externalSynonyms, int index) {		
		externalSynonyms.filter[body.metaValues.size > index].map[
			s|new ExpandedSynonym(sources, metaSynValue(s.body.values, s.body.metaValues.get(index))
				//new ExpandedSynonymValue(s.metaValues.get(index), path+"."+value, maps, true)
			.toList, s.body.hints, s.body.merge, s.body.metaValues.map[new ExpandedSynonymValue(it, null, 1, true)], s.body.mappingLogic, 
				s.body.mapper, s.body.format, s.body.patternMatch, s.body.patternReplace, s.body.removeHtml)
		]
		.filter[!values.isEmpty]
		.toList
	}

	def toExpandedAttribute(Attribute attr) {
		val metas = <ExpandedAttribute>newArrayList
		attr.annotations.forEach [ annoRef, i |
			val annoAttr = annoRef?.attribute
			if(annoAttr !== null && annoAttr.typeCall?.type !== null) {
				metas.add(new ExpandedAttribute(
					annoAttr.name,
					annoRef.annotation.name,
					annoAttr.typeCall.type.toExpandedType,
					annoAttr.typeCall,
					annoAttr.override,
					0,
					1,
					false,
					attr.toRosettaExpandedSynonym(i),
					attr.definition,
					attr.references,
					false,
					Collections.emptyList
				))
			}
		]
		new ExpandedAttribute(
			attr.name,
			(attr.eContainer as RosettaType).name,
			attr.typeCall?.type?.toExpandedType,
			attr.typeCall,
			attr.override,
			attr.card.inf,
			attr.card.sup,
			attr.card.unbounded,
			attr.synonyms.toRosettaExpandedSynonyms(-1),
			attr.definition,
			attr.references,
			attr.typeCall?.type instanceof RosettaEnumeration,
			metas
		)
	}
	
	def ExpandedType toExpandedType(RosettaType type) {
		return new ExpandedType(type.model, type.name,type instanceof Data, type instanceof RosettaEnumeration, type instanceof RosettaMetaType)
	}
	
	def toRosettaExpandedSynonyms(List<RosettaSynonym> synonyms, int meta) {
		if (meta<0) {
			synonyms.map[new ExpandedSynonym(sources, body.values?.map[new ExpandedSynonymValue(name, path, maps, false)], body.hints, body.merge,
				body.metaValues.map[new ExpandedSynonymValue(it, null, 1, true)], body.mappingLogic, 
				body.mapper, body.format, body.patternMatch, body.patternReplace, body.removeHtml
			)]
		} else {
			synonyms.filter[body.metaValues.size>meta]
			.map[s|new ExpandedSynonym(s.sources, metaSynValue(s.body.values,s.body.metaValues.get(meta)), s.body.hints, s.body.merge,
				s.body.metaValues.map[new ExpandedSynonymValue(it, null, 1, true)], s.body.mappingLogic, 
				s.body.mapper, s.body.format, s.body.patternMatch, s.body.patternReplace, s.body.removeHtml
			)]
			.toList
		}
		
	}
	
	def List<ExpandedSynonymValue> metaSynValue(RosettaSynonymValueBase[] values, String meta) {
		if (values===null || values.isEmpty) {
			#[new ExpandedSynonymValue(meta, null,2, true)];
		}
		else {
			values.map[value|{
				val path = if (value.path===null) value.name else value.path+"->"+value.name
				val name = meta
				new ExpandedSynonymValue(name, path, value.maps, true)
			}]
		}
	}
	
	dispatch def toRosettaExpandedSynonym(RosettaSynonymBase synonym) {
	}
	
	dispatch def toRosettaExpandedSynonym(RosettaSynonym syn) {
		new ExpandedSynonym(syn.sources, syn.body.values?.map[new ExpandedSynonymValue(name, path, maps, false)], 
			syn.body.hints, syn.body.merge, syn.body.metaValues.map[new ExpandedSynonymValue(it, null, 1, true)], syn.body.mappingLogic, 
			syn.body.mapper, syn.body.format, syn.body.patternMatch, syn.body.patternReplace, syn.body.removeHtml
		)
	}
	
	dispatch def toRosettaExpandedSynonym(RosettaExternalSynonym syn) {
		val externalAttr = syn.eContainer as RosettaExternalRegularAttribute;
		val externalClass = externalAttr.eContainer as RosettaExternalClass
		val externalSynonymSource = externalClass.eContainer as RosettaExternalSynonymSource
		val superSynonyms = externalSynonymSource.superSynonymSources;
		
		val sources = newArrayList
		sources.add(externalSynonymSource)
		if  (superSynonyms !== null) {
			sources.addAll(superSynonyms)
		}
		
		new ExpandedSynonym(sources, syn.body.values?.map[new ExpandedSynonymValue(name, path, maps, false)], syn.body.hints, syn.body.merge, 
			syn.body.metaValues.map[new ExpandedSynonymValue(it, null, 1, true)], syn.body.mappingLogic, syn.body.mapper,
			syn.body.format, syn.body.patternMatch, syn.body.patternReplace, syn.body.removeHtml
		)
	}
	
	dispatch def toRosettaExpandedSynonym(RosettaExternalClassSynonym syn) {
		val synVals = if (syn.value===null) Collections.emptyList else newArrayList(new ExpandedSynonymValue(syn.value.name, syn.value.path, syn.value.maps, false))
		val synMetaVals = if (syn.metaValue!==null) newArrayList(new ExpandedSynonymValue(syn.metaValue.name, syn.metaValue.path, syn.metaValue.maps, true)) else Collections.emptyList
		
		val externalClass = syn.eContainer as RosettaExternalClass
		val externalSynonymSource = externalClass.eContainer as RosettaExternalSynonymSource
		val superSynonyms = externalSynonymSource.superSynonymSources;
		
		val sources = newArrayList
		sources.add(externalSynonymSource)
		if  (superSynonyms !== null) {
			sources.addAll(superSynonyms)
		}
		
		new ExpandedSynonym(sources, synVals, newArrayList, null, synMetaVals, null, null, null, null, null, false)	
	}
	
	dispatch def toRosettaExpandedSynonym(RosettaClassSynonym syn) {
		val synVals = if (syn.value===null) Collections.emptyList else newArrayList(new ExpandedSynonymValue(syn.value.name, syn.value.path, syn.value.maps, false))
		val synMetaVals = if (syn.metaValue!==null) newArrayList(new ExpandedSynonymValue(syn.metaValue.name, syn.metaValue.path, syn.metaValue.maps, true)) else Collections.emptyList
		new ExpandedSynonym(syn.sources, synVals, newArrayList, null, synMetaVals, null, null, null, null, null, false)
	}
}
