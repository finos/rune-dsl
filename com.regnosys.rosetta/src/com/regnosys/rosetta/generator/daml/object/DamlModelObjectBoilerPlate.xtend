package com.regnosys.rosetta.generator.daml.object

import com.regnosys.rosetta.generator.object.ExpandedAttribute
import com.regnosys.rosetta.rosetta.RosettaClass

import static extension com.regnosys.rosetta.generator.daml.util.DamlTranslator.toDamlType

class DamlModelObjectBoilerPlate {
		
	def toAttributeName(ExpandedAttribute attribute) {
		if (attribute.name == "type")
			'''_type'''
		else
			attribute.name.toFirstLower
	}
	
	def replaceTabsWithSpaces(CharSequence code) {
		code.toString.replace('\t', '  ')
	}
	
	def toType(ExpandedAttribute attribute) {
		if (attribute.isMultiple) 
			'''[«attribute.toRawType»]'''
		else
			attribute.toRawType
				.wrapSingleMetaInBrackets(attribute)
				.prefixSingleOptional(attribute)
	}
	
	private def toRawType(ExpandedAttribute attribute) {
		if (!attribute.hasMetas) 
			attribute.typeName.toDamlType
		else if (attribute.refIndex>=0) {
			if (attribute.type instanceof RosettaClass)
				attribute.typeName.toReferenceWithMetaTypeName
			else 
				attribute.typeName.toBasicReferenceWithMetaTypeName
		}
		else 
			attribute.typeName.toFieldWithMetaTypeName
	}
	
	private def toReferenceWithMetaTypeName(String type) {
		'''ReferenceWithMeta «type.toDamlType.toFirstUpper»'''
	}
	
	private def toBasicReferenceWithMetaTypeName(String type) {
		'''BasicReferenceWithMeta «type.toDamlType.toFirstUpper»'''
	}
	
	private def toFieldWithMetaTypeName(String type) {
		'''FieldWithMeta «type.toDamlType.toFirstUpper»'''
	}
	
	private def prefixSingleOptional(CharSequence type, ExpandedAttribute attribute) {
		if (attribute.isSingleOptional)
			'''Optional «type»'''
		else
			type
	}
	
	private def wrapSingleMetaInBrackets(CharSequence type, ExpandedAttribute attribute) {
		if (attribute.hasMetas) 
			'''(«type»)'''
		else
			type
	}
}
