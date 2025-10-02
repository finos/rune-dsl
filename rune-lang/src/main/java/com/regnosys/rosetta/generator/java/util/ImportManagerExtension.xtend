package com.regnosys.rosetta.generator.java.util

import org.eclipse.xtend2.lib.StringConcatenationClient
import java.lang.reflect.Method
import com.rosetta.util.types.JavaClass
import com.rosetta.util.DottedPath
import com.regnosys.rosetta.generator.java.scoping.JavaFileScope

class ImportManagerExtension {
	def method(Class<?> clazz, String methodName) {
		clazz.methods.findFirst[name == methodName]
	}

	def importWildcard(Class<?> clazz) {
		importWildcard(JavaClass.from(clazz))
	}
	def importWildcard(JavaClass<?> t) {
		new PreferWildcardImportClass(t)
	}
	def importWildcard(Method method) {
		new PreferWildcardImportMethod(method)
	}
	
	/**
	 * Given the body of a Java class represented as a StringConcatenationClient,
	 * generate a full Java class file by adding imports and resolving identifiers.
	 */
	def String buildClass(DottedPath packageName, StringConcatenationClient classCode, JavaFileScope fileScope) {
		if (fileScope.isClosed) {
			throw new IllegalStateException("The top scope may not be closed, as imports will be added to it.")
		}
		val isc = new ImportingStringConcatenation(fileScope)
		val resolvedCode = isc.preprocess(classCode)
		val StringConcatenationClient fullClass = '''
			package «packageName»;
			
			«FOR imp : isc.imports»
				import «imp»;
			«ENDFOR»
			
			«FOR imp : isc.staticImports»
				import static «imp»;
			«ENDFOR»
			
			«resolvedCode»
		'''
		isc.append(fullClass)
		isc.toString
	}
}
