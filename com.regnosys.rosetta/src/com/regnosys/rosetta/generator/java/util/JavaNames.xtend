package com.regnosys.rosetta.generator.java.util

import com.google.inject.Inject
import com.google.inject.Injector
import com.regnosys.rosetta.generator.java.RosettaJavaPackages
import com.regnosys.rosetta.rosetta.RosettaBasicType
import com.regnosys.rosetta.rosetta.RosettaCalculation
import com.regnosys.rosetta.rosetta.RosettaCalculationFeature
import com.regnosys.rosetta.rosetta.RosettaCalculationType
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs
import com.regnosys.rosetta.rosetta.RosettaClass
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.RosettaExternalFunction
import com.regnosys.rosetta.rosetta.RosettaFeature
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.rosetta.RosettaRecordType
import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.rosetta.simple.AssignPathRoot
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.simple.FunctionDispatch
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration
import com.regnosys.rosetta.types.RBuiltinType
import com.regnosys.rosetta.types.RClassType
import com.regnosys.rosetta.types.RDataType
import com.regnosys.rosetta.types.REnumType
import com.regnosys.rosetta.types.RFeatureCallType
import com.regnosys.rosetta.types.RRecordType
import com.regnosys.rosetta.types.RType
import com.regnosys.rosetta.types.RUnionType
import com.regnosys.rosetta.types.RosettaTypeProvider
import java.util.List
import org.eclipse.xtend.lib.annotations.Accessors
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.naming.QualifiedName
import com.regnosys.rosetta.rosetta.RosettaQualifiedType
import com.regnosys.rosetta.generator.util.RosettaAttributeExtensions

class JavaNames {

	@Accessors(PUBLIC_GETTER)
	RosettaJavaPackages packages

	@Inject RosettaTypeProvider typeProvider

	def StringConcatenationClient toJavaQualifiedType(RosettaCallableWithArgs ele) {
		switch (ele) {
			RosettaType:
				toJavaQualifiedType(ele as RosettaType)
			Function: '''«ele.toJavaType()»'''
			default: '''«ele.name»'''
		}
	}

	def StringConcatenationClient toJavaQualifiedType(AssignPathRoot ele) {
		switch(ele) {
			Attribute: toJavaQualifiedType(ele.type)
			ShortcutDeclaration: '''«toJavaType(typeProvider.getRType(ele.expression))»'''
		}
	}

	def StringConcatenationClient toJavaQualifiedType(String typeName) {
		return '''«JavaType.create(JavaClassTranslator.toJavaFullType(typeName)?:"missing builtin type " + typeName)»'''
	}

	def StringConcatenationClient toJavaQualifiedType(RosettaType type) {
		switch (type) {
			RosettaBasicType:
				toJavaQualifiedType(type.name)
			RosettaClass,
			Data,
			RosettaQualifiedType,
			RosettaEnumeration: '''«toJavaType(type)»'''
			RosettaRecordType: '''«toJavaType(type as RosettaType)»'''
			RosettaExternalFunction: '''«toJavaType(type as RosettaType)»'''
			RosettaCalculationType: '''«toJavaType(type as RosettaType)»'''
			default:
				throw new UnsupportedOperationException("Not implemented for type " + type?.class?.name)
		}
	}
		
	def StringConcatenationClient toJavaQualifiedType(RosettaFeature feature) {
		if (feature.isTypeInferred) {
			switch (feature) {
				RosettaCalculationFeature: {
					val rType = typeProvider.getRType(feature)
					val rTypeName = if (rType instanceof RUnionType) rType.toName else rType.name
					val javaType = toJavaQualifiedType(rTypeName)
					javaType
				}
				default:
					toJavaQualifiedType(feature.type)
			}
		} else {
			toJavaQualifiedType(feature.type)
		}
	}

	def StringConcatenationClient toJavaQualifiedType(Attribute attribute) {
		if (attribute.card.isIsMany) {
			'''«List»<«attribute.type.toJavaQualifiedType()»>'''
		}
		else
		'''«attribute.type.toJavaQualifiedType()»'''
	}
	
	def JavaType toJavaType(RosettaCallableWithArgs func) {
		switch (func) {
			Function:
				packages.functions.javaType(func.name)
			default:
				throw new UnsupportedOperationException("Not implemented for type " + func?.class?.name)
		}
	}
	
	def JavaType toJavaType(RosettaType type) {
		switch (type) {
			RosettaBasicType:
				createForBasicType(type.name)
			RosettaClass case type.name == RosettaAttributeExtensions.METAFIELDSCLASSNAME: {
				packages.metaField.javaType(type.name)
			}
			RosettaClass,
			Data,
			RosettaEnumeration: packages.model.javaType(type.name)
			RosettaRecordType: JavaType.create(JavaClassTranslator.toJavaFullType(type.name))?:JavaType.create(packages.libRecords.packageName + '.' +type.name.toFirstUpper)
			RosettaExternalFunction:
					if(type.isLibrary)
						packages.libFunctions.javaType(type.name.toFirstUpper)
					else 
						packages.functions.javaType(type.name.toFirstUpper)
			RosettaCalculationType,
			RosettaQualifiedType: JavaType.create('java.lang.String')
			default:
				throw new UnsupportedOperationException("Not implemented for type " + type?.class?.name)
		}
	}
		
	private def JavaType createForBasicType(String typeName) {
		return  JavaType.create(JavaClassTranslator.toJavaFullType(typeName)?:"missing builtin type " + typeName)
	}

	def  JavaType toJavaType(RType rType) {
		switch (rType) {
			RBuiltinType:
				rType.name.createForBasicType
			REnumType:
				rType.enumeration.toJavaType
			RClassType:
				rType.clazz.toJavaType
			RDataType:
				rType.data.toJavaType
			RFeatureCallType:
				rType.featureType.toJavaType
			RRecordType:
				(rType.record as RosettaType).toJavaType
			default:
				JavaType.create(rType.name)
		}
	}
	
	def QualifiedName toTargetClassName(RosettaCalculation ele) {
		return QualifiedName.create(ele.name.split('\\.'))
	}

	def QualifiedName toTargetClassName(RosettaCallableWithArgs ele) {
		return QualifiedName.create(ele.name)
	}
	
	def QualifiedName toTargetClassName(FunctionDispatch ele) {
		return QualifiedName.create(ele.name).append(ele.value.value.name)
	}

	static class Factory {
		@Inject Injector injector

		def create(RosettaModel model) {
			create(new RosettaJavaPackages(model.header.namespace))
		}
		
		def create(RosettaJavaPackages packages) {
			val result = new JavaNames
			injector.injectMembers(result)
			result.packages = packages
			return result
		}
	}
}
