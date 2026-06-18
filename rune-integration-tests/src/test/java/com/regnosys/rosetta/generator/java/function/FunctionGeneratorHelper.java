package com.regnosys.rosetta.generator.java.function;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.regnosys.rosetta.generator.java.scoping.JavaClassScope;
import com.regnosys.rosetta.generator.java.types.RGeneratedJavaClass;
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper;
import com.regnosys.rosetta.tests.util.ModelHelper;
import com.regnosys.rosetta.types.RFunction;
import com.rosetta.model.lib.functions.ConditionValidator;
import com.rosetta.model.lib.functions.DefaultConditionValidator;
import com.rosetta.model.lib.functions.ModelObjectValidator;
import com.rosetta.model.lib.functions.NoOpModelObjectValidator;
import com.rosetta.model.lib.functions.RosettaFunction;
import com.rosetta.util.DottedPath;
import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtext.xbase.testing.RegisteringFileSystemAccess;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FunctionGeneratorHelper {

	@Inject
	private FunctionGenerator generator;
	@Inject
	private ModelHelper modelHelper;
	@Inject
	private CodeGeneratorTestHelper codeGeneratorTestHelper;
	@Inject
	private RegisteringFileSystemAccess fsa;
	@Inject
	private ImportManagerExtension importManager;

	private final Injector injector;

	public FunctionGeneratorHelper() {
		injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bind(ConditionValidator.class).toInstance(new DefaultConditionValidator());
				bind(ModelObjectValidator.class).toInstance(new NoOpModelObjectValidator());
			}
		});
	}

	public RosettaFunction createFunc(Map<String, Class<?>> classes, String funcName) {
		return createFunc(classes, funcName, modelHelper.rootPackage().child("functions"));
	}

	public RosettaFunction createFunc(Map<String, Class<?>> classes, String funcName, DottedPath packageName) {
		return (RosettaFunction) injector.getInstance(classes.get(packageName + "." + funcName));
	}

	@SuppressWarnings("unchecked")
	public <T> T invokeFunc(RosettaFunction func, Class<T> resultClass, Object... inputs) {
		List<Class<?>> inputTypes = Arrays.stream(inputs)
			.map(it -> it == null ? null : it.getClass())
			.collect(Collectors.toList());
		Method evaluateMethod = codeGeneratorTestHelper.getMatchingMethod(func.getClass(), "evaluate", inputTypes);
		try {
			return (T) evaluateMethod.invoke(func, inputs);
		} catch (InvocationTargetException e) {
			throw sneakyThrow(e.getCause());
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private static <E extends Throwable> RuntimeException sneakyThrow(Throwable e) throws E {
		throw (E) e;
	}

	public void assertToGeneratedFunction(CharSequence actualModel, CharSequence expected) throws AssertionError {
		assertToGenerated(actualModel, expected, model -> {
			RFunction func = generator.streamObjects(model)
				.filter(it -> it.getOperations() == null || it.getOperations().isEmpty())
				.findAny()
				.orElseThrow();
			generate(func);
		});
	}

	public void assertToGeneratedFunctionWithOperations(CharSequence actualModel, CharSequence expected) throws AssertionError {
		assertToGenerated(actualModel, expected, model -> {
			RFunction func = generator.streamObjects(model)
				.filter(it -> it.getOperations() != null && !it.getOperations().isEmpty())
				.findAny()
				.orElseThrow();
			generate(func);
		});
	}

	private void generate(RFunction func) {
		RGeneratedJavaClass<? extends RosettaFunction> typeRepresentation = generator.createTypeRepresentation(func);
		JavaClassScope classScope = JavaClassScope.createAndRegisterIdentifier(typeRepresentation);
		StringConcatenationClient classCode = generator.generateClass(func, typeRepresentation, "test", classScope);
		String javaFileCode = importManager.buildClass(typeRepresentation.getPackageName(), classCode, classScope.getFileScope());
		fsa.generateFile(typeRepresentation.getCanonicalName().withForwardSlashes() + ".java", javaFileCode);
	}

	protected void assertToGenerated(CharSequence actualModel, CharSequence expected,
			Consumer<RosettaModel> genCall) throws AssertionError {
		RosettaModel model = modelHelper.parseRosettaWithNoErrors(actualModel);
		genCall.accept(model);
		String actual = fsa.getTextFiles().entrySet().iterator().next().getValue().toString();
		assertEquals(expected.toString(), actual.replace("\r\n", "\n"));
	}
}
