package com.regnosys.rosetta.generator.java.function

import com.google.inject.Inject
import com.google.inject.Injector
import com.regnosys.rosetta.generator.java.RosettaJavaPackages
import com.regnosys.rosetta.generator.java.util.JavaClassTranslator
import com.regnosys.rosetta.generator.java.util.JavaType
import com.regnosys.rosetta.rosetta.RosettaBasicType
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs
import com.regnosys.rosetta.rosetta.RosettaClass
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.RosettaExternalFunction
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.rosetta.RosettaRecordType
import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.simple.Function
import java.util.List
import org.eclipse.xtend.lib.annotations.Accessors
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.naming.QualifiedName

class JavaQualifiedTypeProvider {

	@Accessors(PUBLIC_GETTER)
	RosettaJavaPackages packages

	def StringConcatenationClient toJavaQualifiedType(RosettaCallableWithArgs ele) {
		switch (ele) {
			RosettaType: toJavaQualifiedType(ele as RosettaType)
			Function: '''«JavaType.create(packages.functions.packageName + '.' + ele.name)»'''
			default: '''«ele.name»'''
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
			RosettaClass, Data: {
				val builderSuffix = if (asBuilder) '''.«type.name»Builder''' else ''
				'''«JavaType.create(packages.model.packageName+'.'+ type.name + builderSuffix)»'''
			}
			RosettaEnumeration: '''«JavaType.create(packages.model.packageName+'.'+ type.name)»'''
			RosettaRecordType: {
				'''«JavaType.create(packages.libRecords.packageName + '.' +type.name.toFirstUpper)»'''	
			}
			RosettaExternalFunction: '''«JavaType.create(if(type.isLibrary) packages.libFunctions.packageName + "." + type.name.toFirstUpper else packages.functions.packageName + "." + type.name.toFirstUpper)»'''
			default:
				throw new UnsupportedOperationException("Not implemented for type " + type?.class?.name)
		}
	}
	
	def StringConcatenationClient toJavaQualifiedType(Attribute attribute, boolean asBuilder) {
		if (attribute.card.isIsMany) {
			'''«List»<«attribute.type.toJavaQualifiedType(asBuilder)»>'''
		}
		else
		'''«attribute.type.toJavaQualifiedType(asBuilder)»'''
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
			if(packages === null) 
				throw new IllegalArgumentException('''RosettaJavaPackages may not be null''')
			val result = new JavaQualifiedTypeProvider
			injector.injectMembers(result)
			result.packages = packages
			return result
		}
	}
}
