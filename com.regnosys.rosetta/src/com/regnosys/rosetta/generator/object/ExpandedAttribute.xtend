package com.regnosys.rosetta.generator.object

import org.eclipse.xtend.lib.annotations.Data
import java.util.List
import com.regnosys.rosetta.rosetta.RosettaSynonymSource
import com.regnosys.rosetta.rosetta.RosettaMapping
import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.rosetta.RosettaClass
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import java.util.Collections
import com.regnosys.rosetta.rosetta.RosettaFactory

@Data
final class ExpandedAttribute {
	
	def static RosettaClass referenceType(String genType) {
		val res = RosettaFactory.eINSTANCE.createRosettaClass()
		res.setName("ReferenceWithMeta"+genType.toFirstUpper)
		res.definition = genType
		return res
	}
	
	def static RosettaClass basicReferenceType(String genType) {
		val res = RosettaFactory.eINSTANCE.createRosettaClass()
		res.setName("BasicReferenceWithMeta"+genType.toFirstUpper)
		res.definition = genType
		return res
	}
	
	def static RosettaClass metadType(String genType) {
		val res = RosettaFactory.eINSTANCE.createRosettaClass()
		res.setName("FieldWithMeta"+genType.toFirstUpper)
		res.definition = genType
		return res
	}
	final static RosettaClass META_TYPE = {
		val res = RosettaFactory.eINSTANCE.createRosettaClass()
		res.setName("MetaFields")
		return res
	}
	
	RosettaType enclosingType
	String name
	RosettaType type
	String typeName
	int inf
	int sup
	boolean isUnbound
	List<ExpandedSynonym> synonyms
	String definition
	boolean hasCalculation
	boolean isEnum
	boolean isQualified
	List<ExpandedAttribute> metas;
	
	static def ExpandedAttribute referenceTo(ExpandedAttribute att) {
		if (att.getType instanceof RosettaClass)
			new ExpandedAttribute(att.enclosingType, att.getName(), referenceType(att.type.name), 
				"referenceWithMeta" + att.type.name.toFirstUpper, att.getInf(), att.getSup(), att.isUnbound, 
				Collections.emptyList(), null, false, false, false, Collections.emptyList())
		else {
			new ExpandedAttribute(att.enclosingType, att.getName(), basicReferenceType(att.type.name), 
				"basicReferenceWithMeta" + att.type.name.toFirstUpper, att.getInf(), att.getSup(), att.isUnbound, 
				Collections.emptyList(), null, false, false, false, Collections.emptyList())
		}
	}
	
	static def ExpandedAttribute refField() {
		new ExpandedAttribute(referenceType(""),"externalReference", null, "string", 1, 1, false, Collections.emptyList(), null, false, false, false, Collections.emptyList())
	}
	
	static def ExpandedAttribute metadField(ExpandedAttribute att) {
		new ExpandedAttribute(att.enclosingType, att.getName(), metadType(att.type.name), "fieldWithMeta", att.getInf(), att.getSup(), att.isUnbound, Collections.emptyList(), null, false, false, false, Collections.emptyList())
	}
	static def ExpandedAttribute metadFieldValue(ExpandedAttribute att) {
		new ExpandedAttribute(att.enclosingType, "value", att.type, att.type.getName, 1, 1, false, Collections.emptyList(), null, false, false, false, Collections.emptyList())
	}
	static def ExpandedAttribute metaDataField(ExpandedAttribute att) {
		new ExpandedAttribute(att.enclosingType, att.getName, att.getType, "string", 1, 1, false, Collections.emptyList(), null, false, false, false, Collections.emptyList())
	}
	static def ExpandedAttribute metaAtt() {
		new ExpandedAttribute(null, "meta", META_TYPE, "meta", 1, 1, false, Collections.emptyList(), null, false, false, false, Collections.emptyList())
	}
	static def ExpandedAttribute externalKeyAtt() {
		new ExpandedAttribute(null, "externalKey", null, "string", 1, 1, false, Collections.emptyList(), null, false, false, false, Collections.emptyList())
	}
	
	def RosettaType getType() {
		return type
	}	
		
 	def String getTypeName() {	
		return typeName
	}
	
	def isMultiple() {
		return unbound || inf>1 || sup!=1;//sup of 0 is counted as multiple
	}
	
	def isSingleOptional() {
		return inf==0 && !multiple
	}
	
	def refIndex() {
		for (var i=0;i<metas.size;i++) {
			if (metas.get(i).getName=="reference") return i;
		}
		return -1;
	}
	
	def hasMetas() {
		!metas.empty
	}
	
	def isRosettaClass() {
		getType() instanceof RosettaClass
	}
	
	def builtInType() {
		!(type instanceof RosettaClass || type instanceof RosettaEnumeration)
	}
	
	def shouldCopy() {
		!#['rosettaKey', 'rosettaKeyValue'].contains(name)
	}
	
}

@Data
final class ExpandedSynonym {
	List<RosettaSynonymSource> sources
	List<ExpandedSynonymValue> values
	List<String> hints
	List<ExpandedSynonymValue> metaValues
	RosettaMapping mappingLogic
	String mapperName
}

@Data
final class ExpandedSynonymValue {
	String name
	String path
	int maps
	boolean isMeta
}