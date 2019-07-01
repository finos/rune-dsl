package com.regnosys.rosetta.validation

import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.generator.java.util.JavaClassTranslator

class BindableType {
		public RosettaType type
		public String genericName

		def isBound() {
			type !== null || genericName !== null
		}

		def getEither() {
			if(type !== null) return JavaClassTranslator.toJavaType(type.name)
			if(genericName !== null) return genericName else return "?"
		}

		def setGenericName(String genericName) {
			type = null
			this.genericName = genericName
		}

		override toString() {
			getEither
		}
	}