package com.regnosys.rosetta.generator.util

import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.object.ExpandedAttribute
import com.regnosys.rosetta.generator.object.ExpandedSynonym
import com.regnosys.rosetta.generator.object.ExpandedSynonymValue
import com.regnosys.rosetta.generator.object.ExpandedType
import com.regnosys.rosetta.rosetta.RosettaClassSynonym
import com.regnosys.rosetta.rosetta.RosettaEnumSynonym
import com.regnosys.rosetta.rosetta.RosettaEnumValue
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.RosettaExternalClass
import com.regnosys.rosetta.rosetta.RosettaExternalClassSynonym
import com.regnosys.rosetta.rosetta.RosettaExternalRegularAttribute
import com.regnosys.rosetta.rosetta.RosettaExternalSynonym
import com.regnosys.rosetta.rosetta.RosettaExternalSynonymSource
import com.regnosys.rosetta.rosetta.RosettaFactory
import com.regnosys.rosetta.rosetta.RosettaMetaType
import com.regnosys.rosetta.rosetta.RosettaSynonym
import com.regnosys.rosetta.rosetta.RosettaSynonymBase
import com.regnosys.rosetta.rosetta.RosettaSynonymSource
import com.regnosys.rosetta.rosetta.RosettaSynonymValueBase
import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.rosetta.RosettaTypedFeature
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.scoping.RosettaScopeProvider
import java.util.ArrayList
import java.util.Collections
import java.util.List
import org.eclipse.xtext.util.SimpleCache

class RosettaAttributeExtensions {

	static def boolean cardinalityIsSingleValue(ExpandedAttribute attribute) {
		attribute.sup === 1
	}
	
	static def boolean cardinalityIsListValue(ExpandedAttribute attribute) {
		attribute.cardinalityIsSingleValue != true
	}
	
	/**
	 * Note that these methods will add a "meta" attribute if the data type has annotations
	 */
	static def List<ExpandedAttribute> getExpandedAttributes(Data data) {
		(data.attributes.map[toExpandedAttribute()].toList + data.additionalAttributes).toList
	}
	
	static def List<ExpandedAttribute> expandedAttributesPlus(Data data) {
		val atts = data.expandedAttributes;
		if (data.hasSuperType) {
			val attsWithSuper = data.superType.expandedAttributesPlus
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
	
	private static def List<ExpandedAttribute> additionalAttributes(Data data) {
		val res = newArrayList
		val rosExt = new RosettaExtensions // Can't inject as used in rosetta-translate and daml directly
		if(rosExt.hasKeyedAnnotation(data)){
			res.add(new ExpandedAttribute(
				'meta',
				data.name,
				provideMetaFieldsType(data),
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
	
	public static val METAFIELDS_CLASS_NAME = 'MetaFields'
	public static val META_AND_TEMPLATE_FIELDS_CLASS_NAME = 'MetaAndTemplateFields'
	
	static SimpleCache<Data, ExpandedType> metaFieldsCache = new SimpleCache[Data data|
		val rosModel = RosettaFactory.eINSTANCE.createRosettaModel()
		rosModel.name = RosettaScopeProvider.LIB_NAMESPACE
		val rosExt = new RosettaExtensions // Can't inject as used in rosetta-translate and daml directly
		val name = if (rosExt.hasTemplateAnnotation(data)) META_AND_TEMPLATE_FIELDS_CLASS_NAME else METAFIELDS_CLASS_NAME
		return new ExpandedType(rosModel, name, true, false, false)
	]
	private static def ExpandedType provideMetaFieldsType(Data data) {
		metaFieldsCache.get(data)
	}
	
	static def List<ExpandedAttribute> getExpandedAttributes(RosettaEnumeration rosettaEnum) {
		rosettaEnum.enumValues.map[expandedEnumAttribute]
	}
	
	def static ExpandedAttribute expandedEnumAttribute(RosettaEnumValue value) {
		new ExpandedAttribute(value.name,value.enumeration.name, null, null, false, 0,0, false, value.enumSynonyms.map[toExpandedSynonym], 
			value.definition, value.references, true, Collections.emptyList
		)
	}
	
	def static ExpandedSynonym toExpandedSynonym(RosettaEnumSynonym syn) {
		new ExpandedSynonym(syn.sources, Collections.singletonList(new ExpandedSynonymValue(syn.synonymValue, null, 0, false)), newArrayList, null, Collections.emptyList, null, null,
			null, syn.patternMatch, syn.patternReplace, syn.removeHtml
		)
	}

	static def boolean isList(ExpandedAttribute a) {
		return a.cardinalityIsListValue
	}

	static def toRosettaExpandedSynonym(Attribute attr, int index) {
		val s= attr.synonyms.filter[body.metaValues.size > index].map[
			s|new ExpandedSynonym(s.sources, metaSynValue(s.body.values,s.body.metaValues.get(index))
				//new ExpandedSynonymValue(s.metaValues.get(index), path+"."+value, maps, true)
			.toList, s.body.hints, s.body.merge, s.body.metaValues.map[new ExpandedSynonymValue(it, null, 1, true)], s.body.mappingLogic, 
				s.body.mapper, s.body.format, s.body.patternMatch, s.body.patternReplace, s.body.removeHtml)
		]
		s.toList
	}

	static def toRosettaExpandedSynonym(List<RosettaSynonymSource> sources, List<RosettaExternalSynonym> externalSynonyms, int index) {		
		externalSynonyms.filter[body.metaValues.size > index].map[
			s|new ExpandedSynonym(sources, metaSynValue(s.body.values, s.body.metaValues.get(index))
				//new ExpandedSynonymValue(s.metaValues.get(index), path+"."+value, maps, true)
			.toList, s.body.hints, s.body.merge, s.body.metaValues.map[new ExpandedSynonymValue(it, null, 1, true)], s.body.mappingLogic, 
				s.body.mapper, s.body.format, s.body.patternMatch, s.body.patternReplace, s.body.removeHtml)
		]
		.filter[!values.isEmpty]
		.toList
	}

	static def toExpandedAttribute(Attribute attr) {
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
			attr.isEnumeration,
			metas
		)
	}
	
	static def ExpandedType toExpandedType(RosettaType type) {
		return new ExpandedType(type.model, type.name,type instanceof Data, type instanceof RosettaEnumeration, type instanceof RosettaMetaType)
	}
	
	static def toRosettaExpandedSynonyms(List<RosettaSynonym> synonyms, int meta) {
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
	
	def static List<ExpandedSynonymValue> metaSynValue(RosettaSynonymValueBase[] values, String meta) {
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
	
	static dispatch def toRosettaExpandedSynonym(RosettaSynonymBase synonym) {
	}
	
	static dispatch def toRosettaExpandedSynonym(RosettaSynonym syn) {
		new ExpandedSynonym(syn.sources, syn.body.values?.map[new ExpandedSynonymValue(name, path, maps, false)], 
			syn.body.hints, syn.body.merge, syn.body.metaValues.map[new ExpandedSynonymValue(it, null, 1, true)], syn.body.mappingLogic, 
			syn.body.mapper, syn.body.format, syn.body.patternMatch, syn.body.patternReplace, syn.body.removeHtml
		)
	}
	
	static dispatch def toRosettaExpandedSynonym(RosettaExternalSynonym syn) {
		val externalAttr = syn.eContainer as RosettaExternalRegularAttribute;
		val externalClass = externalAttr.eContainer as RosettaExternalClass
		val externalSynonymSource = externalClass.eContainer as RosettaExternalSynonymSource
		val superSynonyms = externalSynonymSource.superSynonymSources;
		
		val sources = new ArrayList
		sources.add(externalSynonymSource)
		if  (superSynonyms !== null) {
			sources.addAll(superSynonyms)
		}
		
		new ExpandedSynonym(sources, syn.body.values?.map[new ExpandedSynonymValue(name, path, maps, false)], syn.body.hints, syn.body.merge, 
			syn.body.metaValues.map[new ExpandedSynonymValue(it, null, 1, true)], syn.body.mappingLogic, syn.body.mapper,
			syn.body.format, syn.body.patternMatch, syn.body.patternReplace, syn.body.removeHtml
		)
	}
	
	static dispatch def toRosettaExpandedSynonym(RosettaExternalClassSynonym syn) {
		val synVals = if (syn.value===null) Collections.emptyList else newArrayList(new ExpandedSynonymValue(syn.value.name, syn.value.path, syn.value.maps, false))
		val synMetaVals = if (syn.metaValue!==null) newArrayList(new ExpandedSynonymValue(syn.metaValue.name, syn.metaValue.path, syn.metaValue.maps, true)) else Collections.emptyList
		
		val externalClass = syn.eContainer as RosettaExternalClass
		val externalSynonymSource = externalClass.eContainer as RosettaExternalSynonymSource
		val superSynonyms = externalSynonymSource.superSynonymSources;
		
		val sources = new ArrayList
		sources.add(externalSynonymSource)
		if  (superSynonyms !== null) {
			sources.addAll(superSynonyms)
		}
		
		new ExpandedSynonym(sources, synVals, newArrayList, null, synMetaVals, null, null, null, null, null, false)	
	}
	
	static dispatch def toRosettaExpandedSynonym(RosettaClassSynonym syn) {
		val synVals = if (syn.value===null) Collections.emptyList else newArrayList(new ExpandedSynonymValue(syn.value.name, syn.value.path, syn.value.maps, false))
		val synMetaVals = if (syn.metaValue!==null) newArrayList(new ExpandedSynonymValue(syn.metaValue.name, syn.metaValue.path, syn.metaValue.maps, true)) else Collections.emptyList
		new ExpandedSynonym(syn.sources, synVals, newArrayList, null, synMetaVals, null, null, null, null, null, false)
	}

	private def static boolean isEnumeration(RosettaTypedFeature a) {
		return a.typeCall?.type instanceof RosettaEnumeration
	}
}