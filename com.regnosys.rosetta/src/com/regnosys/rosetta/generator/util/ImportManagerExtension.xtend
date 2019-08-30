package com.regnosys.rosetta.generator.util

import com.regnosys.rosetta.generator.java.calculation.ImportingStringConcatination
import org.eclipse.xtend2.lib.StringConcatenationClient

class ImportManagerExtension {

	def importMethod(Class<?> clazz, String methodName) {
		clazz.methods.findFirst[name == methodName]
	}

	def ImportingStringConcatination tracImports(StringConcatenationClient scc) {
		val isc = new ImportingStringConcatination()
		isc.append(scc)
		isc
	}
}
