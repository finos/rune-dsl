package com.regnosys.rosetta.generator.java.util

import com.google.inject.Inject
import com.google.inject.Injector
import com.regnosys.rosetta.generator.java.RosettaJavaPackages
import com.regnosys.rosetta.rosetta.RosettaBasicType
import com.regnosys.rosetta.rosetta.RosettaCalculation
import com.regnosys.rosetta.rosetta.RosettaCalculationFeature
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs
import com.regnosys.rosetta.rosetta.RosettaClass
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.RosettaExternalFunction
import com.regnosys.rosetta.rosetta.RosettaFeature
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.rosetta.RosettaRecordType
import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.simple.FunctionDispatch
import com.regnosys.rosetta.types.RUnionType
import com.regnosys.rosetta.types.RosettaTypeProvider
import org.eclipse.xtend.lib.annotations.Accessors
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.naming.QualifiedName
import com.regnosys.rosetta.rosetta.simple.Attribute
import java.util.List

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


	def StringConcatenationClient toJavaQualifiedType(String typeName) {
		return '''«JavaType.create(JavaClassTranslator.toJavaFullType(typeName)?:"missing builtin type " + typeName)»'''
	}

	def StringConcatenationClient toJavaQualifiedType(RosettaType type) {
		switch (type) {
			RosettaBasicType:
				toJavaQualifiedType(type.name)
			RosettaClass,
			Data,
			RosettaEnumeration: '''«JavaType.create(packages.model.packageName+'.'+ type.name)»'''
			RosettaCalculation: '''«JavaType.create(packages.calculation.packageName+'.'+ type.name)»'''
			RosettaRecordType: '''«JavaType.create(packages.libRecords.packageName + '.' +type.name.toFirstUpper)»'''
			RosettaExternalFunction: '''«JavaType.create(if(type.isLibrary) packages.libFunctions.packageName + "." + type.name.toFirstUpper else packages.functions.packageName + "." + type.name.toFirstUpper)»'''
			default:
				throw new UnsupportedOperationException("Not implemented for type " + type?.class?.name)
		}
	}
	
	def JavaType toJavaType(RosettaCallableWithArgs func) {
		switch (func) {
			Function case !func.operations.nullOrEmpty:
				JavaType.create(packages.calculation.packageName+'.'+ func.name)
			Function:
				JavaType.create(packages.functions.packageName+'.'+ func.name)
			default:
				throw new UnsupportedOperationException("Not implemented for type " + func?.class?.name)
		}
	}
	
	def JavaType toJavaType(RosettaType type) {
		switch (type) {
			RosettaBasicType:
				createForBasicType(type.name)
			RosettaClass,
			Data,
			RosettaEnumeration: JavaType.create(packages.model.packageName+'.'+ type.name)
			RosettaCalculation: JavaType.create(packages.calculation.packageName+'.'+ type.name)
			RosettaRecordType: JavaType.create(packages.libRecords.packageName + '.' +type.name.toFirstUpper)
			RosettaExternalFunction: JavaType.create(if(type.isLibrary) packages.libFunctions.packageName + "." + type.name.toFirstUpper else packages.functions.packageName + "." + type.name.toFirstUpper)
			default:
				throw new UnsupportedOperationException("Not implemented for type " + type?.class?.name)
		}
	}
	
	private def JavaType createForBasicType(String typeName) {
		return  JavaType.create(JavaClassTranslator.toJavaFullType(typeName)?:"missing builtin type " + typeName)
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
