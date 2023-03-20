package com.regnosys.rosetta.generator.java.util

import org.eclipse.xtend2.lib.StringConcatenationClient
import com.regnosys.rosetta.generator.java.types.JavaClass
import java.lang.reflect.Method
import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.utils.DottedPath

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
	
	/**
	 * Given the body of a Java class represented as a StringConcatenationClient,
	 * generate a full Java class file by adding imports and resolving identifiers.
	 */
	def String buildClass(DottedPath packageName, StringConcatenationClient classCode, JavaScope topScope) {
		val isc = new ImportingStringConcatenation(topScope)
		val resolvedCode = isc.resolveIdentifiers(classCode)
		isc.append(resolvedCode)
		'''
		package «packageName»;
		
		«FOR imp : isc.imports»
			import «imp»;
		«ENDFOR»
		
		«FOR imp : isc.staticImports»
			import static «imp»;
		«ENDFOR»
		
		«resolvedCode»
		'''
	}
}
