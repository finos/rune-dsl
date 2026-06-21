package com.regnosys.rosetta.tests.util;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;

import com.google.inject.Injector;
import com.regnosys.rosetta.generator.java.expression.ExpressionGenerator;
import com.regnosys.rosetta.generator.java.expression.JavaDependencyProvider;
import com.regnosys.rosetta.generator.java.object.MetaFieldGenerator;
import com.regnosys.rosetta.generator.java.scoping.JavaClassScope;
import com.regnosys.rosetta.generator.java.scoping.JavaIdentifierRepresentationService;
import com.regnosys.rosetta.generator.java.scoping.JavaPackageName;
import com.regnosys.rosetta.generator.java.scoping.JavaStatementScope;
import com.regnosys.rosetta.generator.java.statement.builder.JavaStatementBuilder;
import com.regnosys.rosetta.generator.java.types.RGeneratedJavaClass;
import com.regnosys.rosetta.generator.java.types.RJavaWithMetaValue;
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.tests.compiler.CompilationException;
import com.regnosys.rosetta.tests.compiler.InMemoryJavacCompiler;
import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaType;

import jakarta.inject.Inject;

public class ExpressionJavaEvaluatorService {
	@Inject
	private ExpressionParser expressionParser;
	@Inject
	private MetaFieldGenerator metaFieldGenerator;
	@Inject
	private ExpressionGenerator expressionGenerator;
	@Inject
	private JavaDependencyProvider dependencyProvider;
	@Inject
	private JavaIdentifierRepresentationService javaIdentifierRepresentationService;
	@Inject
	private ValidationTestHelper validationHelper;
	@Inject
	private Injector injector;
	@Inject
	private ImportManagerExtension importManagerExtension;

	public Object evaluate(CharSequence rosettaExpression, RosettaModel context, JavaType expectedType,
			ClassLoader classLoader) {
		RosettaExpression expr = expressionParser.parseExpression(rosettaExpression, List.of(context));
		return evaluate(expr, expectedType, classLoader);
	}

	public Object evaluate(RosettaExpression expr, JavaType expectedType, ClassLoader classLoader) {
		validationHelper.assertNoIssues(expr);

		DottedPath packageName = DottedPath.splitOnDots("com.regnosys.rosetta.tests.testexpression");
		String className = "TestExpressionEvaluator";
		String methodName = "evaluate";

		JavaClassScope classScope = JavaClassScope.createAndRegisterIdentifier(
				RGeneratedJavaClass.create(JavaPackageName.escape(packageName), className, Object.class));
		JavaStatementScope evaluateBodyScope = classScope.createMethodScope(methodName).getBodyScope();

		List<JavaClass<?>> dependencies = dependencyProvider.javaDependencies(expr);
		dependencies.forEach(dep -> classScope.createIdentifier(
				javaIdentifierRepresentationService.toDependencyInstance(dep),
				StringUtils.uncapitalize(dep.getSimpleName())));

		JavaStatementBuilder javaCode = expressionGenerator.javaCode(expr, expectedType, evaluateBodyScope);
		StringConcatenationClient content = new StringConcatenationClient() {
			@Override
			protected void appendTo(TargetStringConcatenation target) {
				target.append("public class ");
				target.append(className);
				target.append(" {");
				target.newLine();
				for (JavaClass<?> dep : dependencies) {
					target.append("\t@");
					target.append(Inject.class);
					target.newLine();
					target.append("\tprivate ");
					target.append(dep);
					target.append(" ");
					target.append(classScope.getIdentifierOrThrow(
							javaIdentifierRepresentationService.toDependencyInstance(dep)));
					target.append(";");
					target.newLine();
				}
				target.newLine();
				target.append("\tpublic ");
				target.append(expectedType);
				target.append(" ");
				target.append(methodName);
				target.append("() ");
				target.append(javaCode.completeAsReturn().toBlock(), "\t");
				target.newLine();
				target.append("}");
				target.newLine();
			}
		};

		String sourceCode = importManagerExtension.buildClass(packageName, content, classScope.getFileScope());
		InMemoryJavacCompiler expressionCompiler = InMemoryJavacCompiler
				.newInstance()
				.useParentClassLoader(classLoader)
				.useOptions("--release", "8", "-Xlint:all", "-Xdiags:verbose");
		generateMetaFieldClasses(expr, expressionCompiler);
		Class<?> evaluatorClass;
		try {
			evaluatorClass = expressionCompiler.compile(packageName.child(className).withDots(), sourceCode);
		} catch (CompilationException e) {
			throw new RuntimeException("Failed to compile expression.\n" + sourceCode, e);
		}
		Object instance = injector.getInstance(evaluatorClass);

		try {
			return evaluatorClass.getDeclaredMethod(methodName).invoke(instance);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	private void generateMetaFieldClasses(RosettaExpression expr, InMemoryJavacCompiler compiler) {
		metaFieldGenerator.streamObjects(expr).forEach(object -> {
			RJavaWithMetaValue typeRepresentation = metaFieldGenerator.createTypeRepresentation(object);
			JavaClassScope classScope = JavaClassScope.createAndRegisterIdentifier(typeRepresentation);
			StringConcatenationClient classCode = metaFieldGenerator.generateClass(object, typeRepresentation, "0",
					classScope);
			String javaFileCode = importManagerExtension.buildClass(typeRepresentation.getPackageName(), classCode,
					classScope.getFileScope());
			compiler.addSource(typeRepresentation.getCanonicalName().withDots(), javaFileCode);
		});
	}
}
