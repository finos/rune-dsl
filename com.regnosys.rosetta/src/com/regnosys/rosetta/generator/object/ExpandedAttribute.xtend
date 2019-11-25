package com.regnosys.rosetta.generator.object

import com.regnosys.rosetta.rosetta.RosettaMapping
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.rosetta.RosettaSynonymSource
import com.regnosys.rosetta.rosetta.RosettaType
import java.util.List
import org.eclipse.xtend.lib.annotations.Data

@Data
final class ExpandedAttribute {
	
	String name
	String enclosingType
	ExpandedType type
	
	RosettaType rosettaType // used in translator only
	

	int inf
	int sup
	boolean isUnbound

	List<ExpandedSynonym> synonyms

	String definition
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
			if (metas.get(i).getName=="reference") return i;
		}
		return -1;
	}
	
	def hasMetas() {
		!metas.empty
	}
	
	def isRosettaClassOrData() {
		getType.isType
	}
	
	def builtInType() {
		getType.isBuiltInType
	}
	
	def shouldCopy() {
		!#['rosettaKey', 'rosettaKeyValue'].contains(name)
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
}

@Data
final class ExpandedSynonymValue {
	String name
	String path
	int maps
	boolean isMeta
}