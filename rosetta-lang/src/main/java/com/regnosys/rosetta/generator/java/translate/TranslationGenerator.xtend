package com.regnosys.rosetta.generator.java.translate

import javax.inject.Inject
import org.eclipse.xtext.generator.IFileSystemAccess2
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.types.RObjectFactory
import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.function.FunctionGenerator
import com.regnosys.rosetta.rosetta.translate.Translation
import com.rosetta.util.types.JavaClass
import com.rosetta.model.lib.functions.RosettaFunction

class TranslationGenerator {
	@Inject extension JavaTypeTranslator
	@Inject extension RObjectFactory
	@Inject extension ImportManagerExtension
	@Inject FunctionGenerator functionGenerator

	
	def generate(IFileSystemAccess2 fsa, Translation translation) {
		val rFunction = buildRFunction(translation)
		val clazz = rFunction.toFunctionJavaClass
		val baseInterface = JavaClass.from(RosettaFunction)
		val topScope = new JavaScope(clazz.packageName)
		val classBody = functionGenerator.rBuildClass(rFunction, false, #[baseInterface], emptyMap, false, topScope)
		
		val content = buildClass(clazz.packageName, classBody, topScope)
		fsa.generateFile(clazz.canonicalName.withForwardSlashes + ".java", content)
	}
}