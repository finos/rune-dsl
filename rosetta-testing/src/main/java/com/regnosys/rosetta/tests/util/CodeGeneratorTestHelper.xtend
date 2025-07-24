package com.regnosys.rosetta.tests.util

import com.regnosys.rosetta.generator.RosettaGenerator
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
import org.eclipse.xtext.xbase.testing.RegisteringFileSystemAccess

import static com.google.common.collect.ImmutableMap.*
import com.rosetta.model.lib.RosettaModelObject
import jakarta.inject.Inject
import com.regnosys.rosetta.tests.compiler.InMemoryJavacCompiler
import com.rosetta.model.lib.RosettaModelObjectBuilder
import com.rosetta.model.metafields.MetaFields
import com.rosetta.util.DottedPath

class CodeGeneratorTestHelper {

	@Inject extension RosettaGenerator
	@Inject extension ModelHelper
	
	def generateCode(CharSequence... models) {
		val eResources = models.parseRosettaWithNoErrors.map[it.eResource];
		generateCode(eResources)
	}
	
	def generateCode(CharSequence model) {
		val eResource = model.parseRosettaWithNoErrors.eResource;
		generateCode(#[eResource])
	}
	
	def Map<String, String> generateCode(List<Resource> resources) {
		val fsa = generateCodeWithFSA(resources)
		
		val generatedCode = newLinkedHashMap
		fsa.generatedFiles.forEach [
			if (it.getJavaClassName() !== null) {
				generatedCode.put(it.getJavaClassName(), it.getContents().toString());
			}
		]
		
		return generatedCode
	}
	
	def generateCodeWithFSA(List<Resource> resources) {
		val fsa = new RegisteringFileSystemAccess()
		val ctx = new GeneratorContext()=> [
			cancelIndicator = CancelIndicator.NullImpl
		]
		val resourceSet = resources.head.resourceSet
		try {
			resourceSet.beforeAllGenerate(fsa, ctx)
			resources.forEach[
				try {
					beforeGenerate(fsa, ctx)
					doGenerate(fsa, ctx)
				} finally {
					afterGenerate(fsa, ctx)
				}
			]
		} finally {
			resourceSet.afterAllGenerate(fsa, ctx)
		}
		
		return fsa
	}
	
	def generateCode(RosettaModel model) {
		val eResource = model.eResource
		generateCode(#[eResource])
	}

	def compileToClasses(Map<String, String> code) {
		code.inMemoryCompileToClasses(this.class.classLoader);
	}

	def compileJava8(CharSequence model) {
		val code = generateCode(model)
		code.inMemoryCompileToClasses(this.class.classLoader);
	}
	
	def createEnumInstance(Map<String, Class<?>> classes, String className, String enumValue) {
        val myEnumClass = classes.get(rootPackage + '.' + className) as Class<? extends Enum>
		Enum.valueOf(myEnumClass, enumValue) as Enum<?>
	}

	def createInstanceUsingBuilder(Map<String, Class<?>> classes, String className, Map<String, Object> itemsToSet) {
		classes.createInstanceUsingBuilder(rootPackage, className, itemsToSet)
	}

	def createInstanceUsingBuilder(Map<String, Class<?>> classes, DottedPath namespace, String className, Map<String, Object> itemsToSet) {
		classes.createInstanceUsingBuilder(namespace, className, itemsToSet, of())
	}

	def createInstanceUsingBuilder(Map<String, Class<?>> classes, String className, Map<String, Object> itemsToSet, Map<String, List<?>> itemsToAddToList) {
		classes.createInstanceUsingBuilder(rootPackage, className, itemsToSet, itemsToAddToList)
	}

	def createBuilderInstance(Map<String, Class<?>> classes, DottedPath namespace, String className) {
		classes.get(namespace + '.' + className).getMethod("builder").invoke(null) as RosettaModelObjectBuilder
	}

	def createBuilderInstance(Map<String, Class<?>> classes, String className) {
		createBuilderInstance(classes, rootPackage, className)
	}

	def setAttribute(RosettaModelObjectBuilder rosettaClassBuilderInstance, String name, Object value) {
    val setter = rosettaClassBuilderInstance.class.getMatchingMethod('set' + name.toFirstUpper, #[value?.class])
    if (setter === null) {
      throw new RuntimeException('''No method #«'set' + name.toFirstUpper»(«value?.class?.simpleName») in «rosettaClassBuilderInstance.class»''')
    }
		rosettaClassBuilderInstance.class.getMatchingMethod('set' + name.toFirstUpper, #[value?.class]).invoke(
				rosettaClassBuilderInstance, value);
	}

	def createInstanceUsingBuilder(Map<String, Class<?>> classes, DottedPath namespace, String className, Map<String, Object> itemsToSet, Map<String, List<?>> itemsToAddToList) {
    val clazz = classes.get(namespace + '.' + className)
    if (clazz === null) {
      throw new RuntimeException('''Class «namespace + '.' + className» not found''')
    }
		val rosettaClassBuilderInstance = createBuilderInstance(classes, namespace, className)
		itemsToSet.forEach [ name, value |
			setAttribute(rosettaClassBuilderInstance, name, value)
		]
		itemsToAddToList.forEach [ name, objectsToAdd |
			objectsToAdd.forEach [ value |
				val builderClazz = rosettaClassBuilderInstance.class
				val meth = getMatchingMethod(builderClazz, 'add' + name.toFirstUpper, #[value].map[class])
				meth.invoke(
					rosettaClassBuilderInstance, value);
			]
		]
		return rosettaClassBuilderInstance.class.getMethod('build').invoke(rosettaClassBuilderInstance) as RosettaModelObject;
	}

	def FieldWithMeta<String> createFieldWithMetaString(Map<String, Class<?>> classes, String value, String scheme) {
		val metaFieldsBuilder = MetaFields.builder().setScheme(scheme)
		
		val fieldWithMetaStringBuilder = classes.get('com.rosetta.model.metafields.FieldWithMetaString').getMethod("builder").invoke(null);
		fieldWithMetaStringBuilder.class.getMatchingMethod('setValue', #[value.class]).invoke(fieldWithMetaStringBuilder, value);
		fieldWithMetaStringBuilder.class.getMatchingMethod('setMeta', #[MetaFields]).invoke(fieldWithMetaStringBuilder, metaFieldsBuilder);
		
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
			if (clazz !== null && !p.isAssignableFrom(clazz)) return false
		}
		return true
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

	private def Map<String, Class<?>> inMemoryCompileToClasses(Map<String, String> sources, ClassLoader scope) {
		val inMemoryCompiler = InMemoryJavacCompiler
			.newInstance
			.useParentClassLoader(scope)
			.useOptions("--release", "8", "-Xlint:all", "-Xdiags:verbose")
		
		sources.forEach[className, sourceCode| inMemoryCompiler.addSource(className, sourceCode)]

		inMemoryCompiler.compileAll
	}
}
