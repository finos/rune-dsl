package com.regnosys.rosetta.generator.java.util

import org.eclipse.xtend2.lib.StringConcatenationClient
import com.regnosys.rosetta.generator.java.types.JavaClass
import java.lang.reflect.Method
import com.regnosys.rosetta.generator.java.JavaScope

class ImportManagerExtension {
	def method(Class<?> clazz, String methodName) {
		clazz.methods.findFirst[name == methodName]
	}

	def importWildcard(Class<?> clazz) {
		importWildcard(JavaClass.from(clazz))
	}
	def importWildcard(JavaClass t) {
		new PreferWildcardImportClass(t)
	}
	def importWildcard(Method method) {
		new PreferWildcardImportMethod(method)
	}

	def ImportingStringConcatenation trackImports(StringConcatenationClient scc, JavaScope scope) {
		val isc = new ImportingStringConcatenation(scope)
		isc.append(scc)
		isc
	}
}
