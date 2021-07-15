package com.regnosys.rosetta.generator.object

import com.regnosys.rosetta.rosetta.RosettaMapping
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.rosetta.RosettaSynonymSource
import com.regnosys.rosetta.rosetta.RosettaType
import java.util.List
import org.eclipse.xtend.lib.annotations.Data
import com.regnosys.rosetta.rosetta.RosettaRegulatoryReference

@Data
final class ExpandedAttribute {
	
	String name
	String enclosingType
	ExpandedType type
	
	RosettaType rosettaType // used in translator only
	
	boolean overriding
	int inf
	int sup
	boolean isUnbound

	List<ExpandedSynonym> synonyms

	String definition
	
	List<RosettaRegulatoryReference> docReferences
	boolean hasCalculation
	boolean isEnum
	boolean isQualified
	List<ExpandedAttribute> metas;
	
	
	def ExpandedType getType() {
		return type
	}	
	
	
	def isMultiple() {
		return unbound || inf>1 || sup!=1;//sup of 0 is counted as multiple
	}
	
	def isSingleOptional() {
		return inf==0 && !multiple
	}
	
	def refIndex() {
		for (var i=0;i<metas.size;i++) {
			switch metas.get(i).getName {
				case "reference", case "address": return i
			}
		}
		return -1;
	}
	
	def hasMetas() {
		!metas.empty
	}
	
	def hasIdAnnotation() {
		metas.map[name].contains("id")
	}
	
	
	def isDataType() {
		getType.isType
	}
	
	def builtInType() {
		getType.isBuiltInType
	}
}

@Data
final class ExpandedType {
	RosettaModel model
	String name

	boolean type // type is instance of  Data
	boolean enumeration // type is instance of  Enumeration
	boolean metaType // type is instance of  RosettaMetaType

	def isBuiltInType() {
		!(type || enumeration)
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
	String format
	String patternMatcher
	String patternReplace
}

@Data
final class ExpandedSynonymValue {
	String name
	String path
	int maps
	boolean isMeta
}