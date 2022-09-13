package com.regnosys.rosetta.tests.util

import com.google.inject.Inject
import com.regnosys.rosetta.generator.RosettaGenerator
import com.regnosys.rosetta.generator.RosettaInternalGenerator
import com.regnosys.rosetta.generator.java.RosettaJavaPackages
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.regnosys.rosetta.rosetta.RosettaModel
import com.rosetta.model.lib.meta.FieldWithMeta
import java.io.File
import java.lang.reflect.Method
import java.nio.file.Files
import java.nio.file.Paths
import java.util.List
import java.util.Map
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.GeneratorContext
import org.eclipse.xtext.util.CancelIndicator
import org.eclipse.xtext.util.JavaVersion
import org.eclipse.xtext.xbase.testing.InMemoryJavaCompiler
import org.eclipse.xtext.xbase.testing.JavaSource
import org.eclipse.xtext.xbase.testing.RegisteringFileSystemAccess

import static com.google.common.collect.ImmutableMap.*

class CodeGeneratorTestHelper {

	@Inject extension RosettaGenerator
	@Inject extension ModelHelper
	
	def generateCode(CharSequence model, RosettaInternalGenerator generator) {
		val fsa = new RegisteringFileSystemAccess()
		val eResource = model.parseRosettaWithNoErrors.eResource;
		
		eResource.contents.filter(RosettaModel).forEach[
			val packages = new RosettaJavaPackages(it)
			val version = version
			generator.generate(packages, fsa, elements, version)	
		]
		
		fsa.generatedFiles
			.filter[javaClassName !== null]
			.toMap([javaClassName], [contents.toString])		
	}
	
	def generateCode(CharSequence... models) {
		val eResources = models.parseRosettaWithNoErrors.map[it.eResource];
		generateCode(eResources)
	}
	
	def generateCode(CharSequence model) {
		val eResource = model.parseRosettaWithNoErrors.eResource;
		generateCode(#[eResource])
	}
	
	protected def generateCode(List<Resource> eResources) {
		val fsa = new RegisteringFileSystemAccess()
		val ctx = new GeneratorContext()=> [
			cancelIndicator =  CancelIndicator.NullImpl
		]
		eResources.forEach[
			beforeGenerate(fsa, ctx)
			doGenerate(fsa, ctx)
			afterGenerate(fsa, ctx)
		]
		
		val generatedCode = newHashMap
		fsa.generatedFiles.forEach [
			if (it.getJavaClassName() !== null) {
				generatedCode.put(it.getJavaClassName(), it.getContents().toString());
			}
		]
		
		return generatedCode
	}
	
	def generateCode(RosettaModel model) {
		val eResource = model.eResource
		generateCode(#[eResource])
	}

	def compileToClasses(Map<String, String> code) {
		code.inMemoryCompileToClasses(this.class.classLoader, JavaVersion.JAVA8);
	}

	def compileJava8(CharSequence model) {
		val code = generateCode(model)
		code.inMemoryCompileToClasses(this.class.classLoader, JavaVersion.JAVA8);
	}

	def createInstanceUsingBuilder(Map<String, Class<?>> classes, String className, Map<String, Object> itemsToSet) {
		classes.createInstanceUsingBuilder(rootPackage, className, itemsToSet)
	}

	def createInstanceUsingBuilder(Map<String, Class<?>> classes, RootPackage namespace, String className, Map<String, Object> itemsToSet) {
		classes.createInstanceUsingBuilder(namespace, className, itemsToSet, of())
	}

	def createInstanceUsingBuilder(Map<String, Class<?>> classes, String className, Map<String, Object> itemsToSet, Map<String, List<?>> itemsToAddToList) {
		classes.createInstanceUsingBuilder(rootPackage, className, itemsToSet, itemsToAddToList)
	}

	def createInstanceUsingBuilder(Map<String, Class<?>> classes, RootPackage namespace, String className, Map<String, Object> itemsToSet, Map<String, List<?>> itemsToAddToList) {
		val rosettaClassBuilderInstance = classes.get(namespace.name + '.' + className).getMethod(
			"builder").invoke(null);
		itemsToSet.forEach [ name, value |
			rosettaClassBuilderInstance.class.getMatchingMethod('set' + name.toFirstUpper, #[value.class]).invoke(
				rosettaClassBuilderInstance, value);
		]
		itemsToAddToList.forEach [ name, objectsToAdd |
			objectsToAdd.forEach [ value |
				val clazz = rosettaClassBuilderInstance.class
				val meth = getMatchingMethod(clazz, 'add' + name.toFirstUpper, #[value].map[class])
				meth.invoke(
					rosettaClassBuilderInstance, value);
			]
		]
		return rosettaClassBuilderInstance.class.getMethod('build').invoke(rosettaClassBuilderInstance);
	}

	def FieldWithMeta<String> createFieldWithMetaString(Map<String, Class<?>> classes, String value, String scheme) {
		val metaFieldsBuilder = classes.get('com.rosetta.model.metafields.MetaFields').getMethod("builder").invoke(null);
		metaFieldsBuilder.class.getMatchingMethod('setScheme', #[scheme.class]).invoke(metaFieldsBuilder, scheme);
		
		val fieldWithMetaStringBuilder = classes.get('com.rosetta.model.metafields.FieldWithMetaString').getMethod("builder").invoke(null);
		fieldWithMetaStringBuilder.class.getMatchingMethod('setValue', #[value.class]).invoke(fieldWithMetaStringBuilder, value);
		fieldWithMetaStringBuilder.class.getMatchingMethod('setMeta', #[metaFieldsBuilder.class]).invoke(fieldWithMetaStringBuilder, metaFieldsBuilder);
		
		return fieldWithMetaStringBuilder.class.getMethod('build').invoke(fieldWithMetaStringBuilder) as FieldWithMeta<String>;
	}

	def Method getMatchingMethod(Class<?> clazz, String name, List<Class<?>> values) {
		var methods = clazz.methods.filter[m|m.name==name]
		val size = values === null ? 0 : values.size
		methods = methods
			.filter[m|m.parameterCount==size]
		val meth = methods.findFirst[m|m.paramsMatch(values)]
		meth
	}
	
	static def paramsMatch(Method m, List<Class<?>> value) {
		for (var i=0;i<m.parameterTypes.size;i++) {
			val clazz = value.get(i)
			val p = m.parameterTypes.get(i)
			if (!p.isAssignableFrom(clazz)) return false
		}
		return true
	}

	def createCalculationInstance(Map<String, Class<?>> classes, String className) {
		val fqn = rootPackage.functions.name + '.' + className
		val foundClazz = classes.get(fqn)
		if(foundClazz === null)
			throw new IllegalStateException('''No generated class '«fqn»' found''')
		return foundClazz.declaredConstructor.newInstance
	}

	@Deprecated
	def writeClasses(Map<String, String> code, String directory) {
		for (entry : code.entrySet) {
			val name = entry.key;
			val pathName = name.replace('.', File.separator)
			if (!pathName.endsWith("Factory")) {
				val path = Paths.get("target/" + directory + "/java", pathName + ".java")
				Files.createDirectories(path.parent)
				Files.write(path, entry.value.bytes)
			}
		}
		return code;
	}
	
	
	private def toJavaFile(String string) {
		string.replace('.', '/') + ".java"
	}

	private def Map<String, Class<?>> inMemoryCompileToClasses(Map<String, String> sources, ClassLoader scope, JavaVersion version) {
		val InMemoryJavaCompiler inMemoryCompiler = new InMemoryJavaCompiler(scope, version);

		val InMemoryJavaCompiler.Result result = inMemoryCompiler.compile(sources.entrySet.map [
			new JavaSource(key.toJavaFile, value)
		])
		try {
			if (result.compilationProblems.exists[error]) {
				throw new IllegalArgumentException('''
					Java code compiled with errors:
					«FOR error : result.compilationProblems.filter[error]»
						«new String(error.originatingFileName)» : «error.sourceLineNumber»
						«error.message» 
					
						Problem line is around:						
						«val sourceLines=sources.get(new String(error.originatingFileName).replace('.java','').replace('/', '.').replace('\\','.')).split("\n")»
						«sourceLines.subList(Math.max(error.sourceLineNumber - 3, 0) , Math.min(error.sourceLineNumber + 3, sourceLines.size)).join('\n')»
					
						«new String(error.originatingFileName)»
						=========
						«sources.get(new String(error.originatingFileName).replace('.java','').replace('/', '.'))»
						=========
						
					«ENDFOR»
					
					All files generated: «sources.keySet.join('\n')»
					
				''')
			}
			val classLoader = result.getClassLoader()
			return sources.keySet.map[classLoader.loadClass(it)].toMap[name]
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException('''
				«e.message» 
				source :
					«sources»
				
				PROBLEMS : 
					«result.getCompilationProblems().join('\n')»
			''', e)
		}
	}
}
