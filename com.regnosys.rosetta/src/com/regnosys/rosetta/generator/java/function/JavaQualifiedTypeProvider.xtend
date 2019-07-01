package com.regnosys.rosetta.generator.java.function

import com.google.inject.Inject
import com.google.inject.Injector
import com.regnosys.rosetta.generator.java.RosettaJavaPackages
import com.regnosys.rosetta.generator.java.util.JavaClassTranslator
import com.regnosys.rosetta.rosetta.RosettaBasicType
import com.regnosys.rosetta.rosetta.RosettaCalculation
import com.regnosys.rosetta.rosetta.RosettaCalculationFeature
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs
import com.regnosys.rosetta.rosetta.RosettaClass
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.RosettaExternalFunction
import com.regnosys.rosetta.rosetta.RosettaFeature
import com.regnosys.rosetta.rosetta.RosettaFunction
import com.regnosys.rosetta.rosetta.RosettaFunctionInput
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.rosetta.RosettaRecordType
import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.types.RUnionType
import com.regnosys.rosetta.types.RosettaTypeProvider
import java.util.List
import org.eclipse.xtend.lib.annotations.Accessors
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.naming.QualifiedName
import com.regnosys.rosetta.generator.java.calculation.JavaType

class JavaQualifiedTypeProvider {

	@Accessors(PUBLIC_GETTER)
	RosettaJavaPackages packages

	@Inject RosettaTypeProvider typeProvider

	def StringConcatenationClient toJavaQualifiedType(RosettaCallableWithArgs ele) {
		if (ele instanceof RosettaType) {
			toJavaQualifiedType(ele as RosettaType)
		} else {
			'''«ele.name»'''
		}
	}


	def StringConcatenationClient toJavaQualifiedType(String typeName) {
		return '''«JavaType.create(JavaClassTranslator.toJavaFullType(typeName)?:"missing built-in type " + typeName)»'''
	}

	def StringConcatenationClient toJavaQualifiedType(RosettaType type) {
		type.toJavaQualifiedType(false)
	}
	
	def StringConcatenationClient toJavaQualifiedType(RosettaType type, boolean asBuilder) {
		switch (type) {
			RosettaBasicType:
				toJavaQualifiedType(type.name)
			RosettaClass: {
				val builderSuffix = if (asBuilder) '''.«type.name»Builder''' else ''
				'''«JavaType.create(packages.model.packageName+'.'+ type.name + builderSuffix)»'''
			}
			RosettaEnumeration: '''«JavaType.create(packages.model.packageName+'.'+ type.name)»'''
			RosettaCalculation: '''«JavaType.create(packages.calculation.packageName+'.'+ type.name)»'''
			RosettaRecordType: {
				if (type.name == 'date') {
					type.name.toJavaQualifiedType
				} else {
					'''«JavaType.create(packages.libRecords.packageName + '.' +type.name.toFirstUpper)»'''	
				}
			}
			RosettaExternalFunction: '''«JavaType.create(if(type.isLibrary) packages.libFunctions.packageName + "." + type.name.toFirstUpper else packages.functions.packageName + "." + type.name.toFirstUpper)»'''
			RosettaFunction: '''«JavaType.create(packages.functions.packageName + '.' + type.name)»'''
			default:
				throw new UnsupportedOperationException("Not implemented for type " + type?.class?.name)
		}
	}
	
	def StringConcatenationClient toJavaQualifiedType(RosettaFunctionInput attribute, boolean asBuilder) {
		if (attribute.card.isIsMany) '''«List»<«attribute.type.toJavaQualifiedType(asBuilder)»>''' else '''«attribute.type.toJavaQualifiedType(asBuilder)»'''
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


	def QualifiedName toTargetClassName(RosettaCalculation ele) {
		return QualifiedName.create(ele.name.split('\\.'))
	}

	def QualifiedName toTargetClassName(RosettaExternalFunction ele) {
		return QualifiedName.create(ele.name)
	}

	static class Factory {
		@Inject Injector injector

		def create(RosettaModel model) {
			create(new RosettaJavaPackages(model.header.namespace))
		}
		
		def create(RosettaJavaPackages packages) {
			val result = new JavaQualifiedTypeProvider
			injector.injectMembers(result)
			result.packages = packages
			return result
		}
	}
}
