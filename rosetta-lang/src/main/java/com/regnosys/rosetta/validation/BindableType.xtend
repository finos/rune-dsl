package com.regnosys.rosetta.validation

import com.regnosys.rosetta.generator.java.types.JavaClass
import com.regnosys.rosetta.generator.java.util.JavaNames
import com.regnosys.rosetta.utils.DottedPath
import com.regnosys.rosetta.types.RType
import com.regnosys.rosetta.generator.java.types.JavaType

class BindableType {
	public RType type
	public JavaType genericType

	def isBound() {
		type !== null || genericType !== null
	}

	def JavaType getEither(JavaNames names) {
		if(type !== null) return names.toJavaType(type)
		if(genericType !== null) return genericType else return new JavaClass(DottedPath.of("java", "lang"), "?")
	}

	def void setGenericType(JavaType genericType) {
		type = null
		this.genericType = genericType
	}
	def void setGenericType(Class<?> genericType) {
		type = null
		this.genericType = JavaType.from(genericType)
	}
}