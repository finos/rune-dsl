package com.regnosys.rosetta.tests.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.generator.GeneratorContext;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.xbase.testing.RegisteringFileSystemAccess;

import com.google.common.collect.ImmutableMap;
import com.regnosys.rosetta.generator.RosettaGenerator;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.tests.compiler.InMemoryJavacCompiler;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.meta.FieldWithMeta;
import com.rosetta.model.metafields.MetaFields;
import com.rosetta.util.DottedPath;

import jakarta.inject.Inject;

public class CodeGeneratorTestHelper {

	@Inject
	private RosettaGenerator generator;
	@Inject
	private ModelHelper modelHelper;

	public Map<String, String> generateCode(CharSequence... models) {
		List<Resource> eResources = modelHelper.parseRosettaWithNoErrors(models).stream()
				.map(RosettaModel::eResource)
				.toList();
		return generateCode(eResources);
	}

	public Map<String, String> generateCode(CharSequence model) {
		Resource eResource = modelHelper.parseRosettaWithNoErrors(model).eResource();
		return generateCode(List.of(eResource));
	}

	public Map<String, String> generateCode(List<Resource> resources) {
		RegisteringFileSystemAccess fsa = generateCodeWithFSA(resources);

		Map<String, String> generatedCode = new LinkedHashMap<>();
		fsa.getGeneratedFiles().forEach(it -> {
			if (it.getJavaClassName() != null) {
				generatedCode.put(it.getJavaClassName(), it.getContents().toString());
			}
		});

		return generatedCode;
	}

	public RegisteringFileSystemAccess generateCodeWithFSA(List<Resource> resources) {
		RegisteringFileSystemAccess fsa = new RegisteringFileSystemAccess();
		GeneratorContext ctx = new GeneratorContext();
		ctx.setCancelIndicator(CancelIndicator.NullImpl);
		ResourceSet resourceSet = resources.get(0).getResourceSet();
		try {
			generator.beforeAllGenerate(resourceSet, fsa, ctx);
			resources.forEach(resource -> {
				try {
					generator.beforeGenerate(resource, fsa, ctx);
					generator.doGenerate(resource, fsa, ctx);
				} finally {
					generator.afterGenerate(resource, fsa, ctx);
				}
			});
		} finally {
			generator.afterAllGenerate(resourceSet, fsa, ctx);
		}

		return fsa;
	}

	public Map<String, String> generateCode(RosettaModel model) {
		Resource eResource = model.eResource();
		return generateCode(List.of(eResource));
	}

	public Map<String, Class<?>> compileToClasses(Map<String, String> code) {
		return inMemoryCompileToClasses(code, this.getClass().getClassLoader());
	}

	public Map<String, Class<?>> compileJava8(CharSequence model) {
		Map<String, String> code = generateCode(model);
		return inMemoryCompileToClasses(code, this.getClass().getClassLoader());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Enum<?> createEnumInstance(Map<String, Class<?>> classes, String className, String enumValue) {
		Class<? extends Enum> myEnumClass = (Class<? extends Enum>) classes.get(modelHelper.rootPackage() + "." + className);
		return Enum.valueOf(myEnumClass, enumValue);
	}

	public RosettaModelObject createInstanceUsingBuilder(Map<String, Class<?>> classes, String className,
			Map<String, Object> itemsToSet) {
		return createInstanceUsingBuilder(classes, modelHelper.rootPackage(), className, itemsToSet);
	}

	public RosettaModelObject createInstanceUsingBuilder(Map<String, Class<?>> classes, DottedPath namespace,
			String className, Map<String, Object> itemsToSet) {
		return createInstanceUsingBuilder(classes, namespace, className, itemsToSet, ImmutableMap.of());
	}

	public RosettaModelObject createInstanceUsingBuilder(Map<String, Class<?>> classes, String className,
			Map<String, Object> itemsToSet, Map<String, List<?>> itemsToAddToList) {
		return createInstanceUsingBuilder(classes, modelHelper.rootPackage(), className, itemsToSet, itemsToAddToList);
	}

	public RosettaModelObjectBuilder createBuilderInstance(Map<String, Class<?>> classes, DottedPath namespace,
			String className) {
		try {
			return (RosettaModelObjectBuilder) classes.get(namespace + "." + className).getMethod("builder")
					.invoke(null);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	public RosettaModelObjectBuilder createBuilderInstance(Map<String, Class<?>> classes, String className) {
		return createBuilderInstance(classes, modelHelper.rootPackage(), className);
	}

	public void setAttribute(RosettaModelObjectBuilder rosettaClassBuilderInstance, String name, Object value) {
		Class<?> valueClass = value == null ? null : value.getClass();
		Method setter = getMatchingMethod(rosettaClassBuilderInstance.getClass(), "set" + StringUtils.capitalize(name),
				Arrays.asList(valueClass));
		if (setter == null) {
			throw new RuntimeException("No method #" + "set" + StringUtils.capitalize(name) + "("
					+ (valueClass == null ? null : valueClass.getSimpleName()) + ") in "
					+ rosettaClassBuilderInstance.getClass());
		}
		try {
			setter.invoke(rosettaClassBuilderInstance, value);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	public RosettaModelObject createInstanceUsingBuilder(Map<String, Class<?>> classes, DottedPath namespace,
			String className, Map<String, Object> itemsToSet, Map<String, List<?>> itemsToAddToList) {
		Class<?> clazz = classes.get(namespace + "." + className);
		if (clazz == null) {
			throw new RuntimeException("Class " + namespace + "." + className + " not found");
		}
		RosettaModelObjectBuilder rosettaClassBuilderInstance = createBuilderInstance(classes, namespace, className);
		itemsToSet.forEach((name, value) -> setAttribute(rosettaClassBuilderInstance, name, value));
		itemsToAddToList.forEach((name, objectsToAdd) -> objectsToAdd.forEach(value -> {
			Class<?> builderClazz = rosettaClassBuilderInstance.getClass();
			Method meth = getMatchingMethod(builderClazz, "add" + StringUtils.capitalize(name),
					List.of(value.getClass()));
			try {
				meth.invoke(rosettaClassBuilderInstance, value);
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		}));
		try {
			return (RosettaModelObject) rosettaClassBuilderInstance.getClass().getMethod("build")
					.invoke(rosettaClassBuilderInstance);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public FieldWithMeta<String> createFieldWithMetaString(Map<String, Class<?>> classes, String value, String scheme) {
		try {
			MetaFields.MetaFieldsBuilder metaFieldsBuilder = MetaFields.builder().setScheme(scheme);

			Object fieldWithMetaStringBuilder = classes.get("com.rosetta.model.metafields.FieldWithMetaString")
					.getMethod("builder").invoke(null);
			getMatchingMethod(fieldWithMetaStringBuilder.getClass(), "setValue", List.of(value.getClass()))
					.invoke(fieldWithMetaStringBuilder, value);
			getMatchingMethod(fieldWithMetaStringBuilder.getClass(), "setMeta", List.of(MetaFields.class))
					.invoke(fieldWithMetaStringBuilder, metaFieldsBuilder);

			return (FieldWithMeta<String>) fieldWithMetaStringBuilder.getClass().getMethod("build")
					.invoke(fieldWithMetaStringBuilder);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	public Method getMatchingMethod(Class<?> clazz, String name, List<Class<?>> values) {
		int size = values == null ? 0 : values.size();
		return Arrays.stream(clazz.getMethods())
				.filter(m -> m.getName().equals(name))
				.filter(m -> m.getParameterCount() == size)
				.filter(m -> paramsMatch(m, values))
				.findFirst()
				.orElse(null);
	}

	public static boolean paramsMatch(Method m, List<Class<?>> value) {
		for (int i = 0; i < m.getParameterTypes().length; i++) {
			Class<?> clazz = value.get(i);
			Class<?> p = m.getParameterTypes()[i];
			if (clazz != null && !p.isAssignableFrom(clazz)) {
				return false;
			}
		}
		return true;
	}

	@Deprecated
	public Map<String, String> writeClasses(Map<String, String> code, String directory) {
		for (Map.Entry<String, String> entry : code.entrySet()) {
			String name = entry.getKey();
			String pathName = name.replace(".", File.separator);
			if (!pathName.endsWith("Factory")) {
				Path path = Paths.get("target/" + directory + "/java", pathName + ".java");
				try {
					Files.createDirectories(path.getParent());
					Files.write(path, entry.getValue().getBytes(StandardCharsets.UTF_8));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return code;
	}

	private Map<String, Class<?>> inMemoryCompileToClasses(Map<String, String> sources, ClassLoader scope) {
		InMemoryJavacCompiler inMemoryCompiler = InMemoryJavacCompiler
				.newInstance()
				.useParentClassLoader(scope)
				.useOptions("--release", "8", "-Xlint:all", "-Xdiags:verbose");

		sources.forEach(inMemoryCompiler::addSource);

		return inMemoryCompiler.compileAll();
	}
}
