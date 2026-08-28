package com.regnosys.rosetta.generator.java.condition;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.ecore.EObject;

import com.google.inject.ImplementedBy;
import com.regnosys.rosetta.generator.GeneratedIdentifier;
import com.regnosys.rosetta.generator.ImplicitVariableRepresentation;
import com.regnosys.rosetta.codegen.api.CodeRenderer;
import com.regnosys.rosetta.generator.java.FluentJavaClassGenerator;
import com.regnosys.rosetta.generator.java.expression.ExpressionGenerator;
import com.regnosys.rosetta.generator.java.expression.JavaDependencyProvider;
import com.regnosys.rosetta.generator.java.scoping.JavaClassScope;
import com.regnosys.rosetta.generator.java.scoping.JavaIdentifierRepresentationService;
import com.regnosys.rosetta.generator.java.scoping.JavaMethodScope;
import com.regnosys.rosetta.generator.java.scoping.JavaStatementScope;
import com.regnosys.rosetta.generator.java.types.JavaConditionInterface;
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator;
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil;
import com.regnosys.rosetta.generator.java.types.RGeneratedJavaClass;
import com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil;
import com.regnosys.rosetta.generator.java.util.RosettaGrammarUtil;
import com.regnosys.rosetta.rosetta.ParametrizedRosettaType;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaTypeWithConditions;
import com.regnosys.rosetta.rosetta.TypeParameter;
import com.regnosys.rosetta.rosetta.simple.Condition;
import com.regnosys.rosetta.rosetta.simple.SimplePackage;
import com.rosetta.model.lib.annotations.RosettaDataRule;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaType;

import java.util.Arrays;
import java.util.Collections;

import jakarta.inject.Inject;

public class ConditionGenerator extends FluentJavaClassGenerator<Condition, JavaConditionInterface> {
	@Inject
	private ExpressionGenerator expressionHandler;
	@Inject
	private JavaDependencyProvider dependencyProvider;
	@Inject
	private JavaIdentifierRepresentationService identifierRepresentationService;
	@Inject
	private JavaTypeTranslator typeTranslator;
	@Inject
	private JavaTypeUtil typeUtil;
	@Inject
	private ModelGeneratorUtil modelGeneratorUtil;

	@Override
	protected EObject getSource(Condition object) {
		return object;
	}

	@Override
	protected Stream<? extends Condition> streamObjects(RosettaModel model) {
		return model.getElements().stream()
				.filter(RosettaTypeWithConditions.class::isInstance)
				.map(RosettaTypeWithConditions.class::cast)
				.flatMap(type -> type.getConditions().stream());
	}

	@Override
	protected JavaConditionInterface createTypeRepresentation(Condition object) {
		return typeTranslator.toConditionJavaClass(object);
	}

	@Override
	protected CodeRenderer generateClass(Condition condition, JavaConditionInterface conditionClass, String version, JavaClassScope classScope) {
		String definition = RosettaGrammarUtil.quote(RosettaGrammarUtil.extractNodeText(condition, SimplePackage.Literals.CONDITION__EXPRESSION));
		List<JavaClass<?>> deps = dependencyProvider.javaDependencies(condition.getExpression());
		ImplicitVariableRepresentation implicitVarRepr = identifierRepresentationService.getImplicitVarInContext(condition.getExpression());
		String instanceVarName = StringUtils.uncapitalize(conditionClass.getInstanceType().getName());

		Map<String, JavaType> paramsByName = conditionClass.getParameters();
		Map<TypeParameter, JavaType> params = new LinkedHashMap<>();
		if (!paramsByName.isEmpty()) {
			List<TypeParameter> enclosingParameters = ((ParametrizedRosettaType) condition.getEnclosingType()).getParameters();
			paramsByName.forEach((name, type) -> enclosingParameters.stream()
					.filter(p -> p.getName().equals(name))
					.findFirst()
					.ifPresent(param -> params.put(param, type)));
		}

		JavaMethodScope getValidationResultsScope = classScope.createMethodScope("getValidationResults");
		GeneratedIdentifier pathId = getValidationResultsScope.createUniqueIdentifier("path");
		GeneratedIdentifier instanceId = getValidationResultsScope.createIdentifier(implicitVarRepr, instanceVarName);
		params.keySet().forEach(getValidationResultsScope::createIdentifier);

		RGeneratedJavaClass<?> defaultClass = conditionClass.createNestedClassImplementingInterface("Default", conditionClass);
		JavaClassScope defaultClassScope = classScope.createNestedClassScopeAndRegisterIdentifier(defaultClass);

		deps.forEach(dep -> defaultClassScope.createIdentifier(identifierRepresentationService.toDependencyInstance(dep), StringUtils.uncapitalize(dep.getSimpleName())));

		JavaMethodScope defaultClassGetValidationResultsScope = defaultClassScope.createMethodScope("getValidationResults");
		GeneratedIdentifier defaultClassInstanceId = defaultClassGetValidationResultsScope.createIdentifier(implicitVarRepr, instanceVarName);
		params.keySet().forEach(defaultClassGetValidationResultsScope::createIdentifier);
		JavaStatementScope defaultClassGetValidationResultsBodyScope = defaultClassGetValidationResultsScope.getBodyScope();
		GeneratedIdentifier defaultClassResultId = defaultClassGetValidationResultsBodyScope.createUniqueIdentifier("result");
		GeneratedIdentifier defaultClassFailureMessageId = defaultClassGetValidationResultsBodyScope.createUniqueIdentifier("failureMessage");

		JavaMethodScope defaultClassExecuteScope = defaultClassScope.createMethodScope("execute");
		GeneratedIdentifier defaultClassExecuteInstanceId = defaultClassExecuteScope.createIdentifier(implicitVarRepr, instanceVarName);
		params.keySet().forEach(defaultClassExecuteScope::createIdentifier);
		JavaStatementScope defaultClassExecuteBodyScope = defaultClassExecuteScope.getBodyScope();
		GeneratedIdentifier defaultClassExecuteExceptionId = defaultClassExecuteBodyScope.createUniqueIdentifier("ex");

		RGeneratedJavaClass<?> noOpClass = conditionClass.createNestedClassImplementingInterface("NoOp", conditionClass);
		classScope.createNestedClassScopeAndRegisterIdentifier(noOpClass);

		return out -> {
			out.write(modelGeneratorUtil.emptyJavadocWithVersion(version));
			out.writeln("@", RosettaDataRule.class, "(\"", conditionClass.getSimpleName(), "\")");
			out.writeln("@", ImplementedBy.class, "(", conditionClass, ".Default.class)");
			out.write("public interface ", conditionClass.getSimpleName());
			List<JavaClass<?>> conditionInterfaces = conditionClass.getInterfaces();
			if (!conditionInterfaces.isEmpty()) {
				out.write(" extends ");
				out.join(conditionInterfaces, ", ");
			}
			out.writeln(" {");
			out.indented(() -> {
				out.newline();
				out.writeln("String NAME = \"", conditionClass.getSimpleName(), "\";");
				out.writeln("String DEFINITION = ", definition, ";");
				if (!conditionClass.implementsValidatorInterface()) {
					out.newline();
					out.write(List.class, "<", ValidationResult.class, "<?>> getValidationResults(", RosettaPath.class, " ", pathId, ", ", conditionClass.getInstanceClass(), " ", instanceId);
					for (TypeParameter param : params.keySet()) {
						out.write(", ", params.get(param), " ", getValidationResultsScope.getIdentifierOrThrow(param));
					}
					out.writeln(");");
				}
				out.newline();
				out.write(defaultClass.asClassDeclaration());
				out.writeln(" {");
				out.indented(() -> {
					out.newline();
					for (JavaClass<?> dep : deps) {
						out.writeln("@", javax.inject.Inject.class, " protected ", dep, " ", defaultClassScope.getIdentifierOrThrow(identifierRepresentationService.toDependencyInstance(dep)), ";");
						out.newline();
					}
					out.writeln("@Override");
					out.write("public ", List.class, "<", ValidationResult.class, "<?>> getValidationResults(", RosettaPath.class, " ", pathId, ", ", conditionClass.getInstanceClass(), " ", defaultClassInstanceId);
					for (TypeParameter param : params.keySet()) {
						out.write(", ", params.get(param), " ", getValidationResultsScope.getIdentifierOrThrow(param));
					}
					out.writeln(") {");
					out.indented(() -> {
						out.write(ComparisonResult.class, " ", defaultClassResultId, " = executeDataRule(", defaultClassGetValidationResultsBodyScope.getIdentifierOrThrow(implicitVarRepr));
						for (TypeParameter param : params.keySet()) {
							out.write(", ", defaultClassGetValidationResultsBodyScope.getIdentifierOrThrow(param));
						}
						out.writeln(");");
						out.writeln("if (result.getOrDefault(true)) {");
						out.indented(() -> out.writeln("return ", Arrays.class, ".asList(", ValidationResult.class, ".success(NAME, ValidationResult.ValidationType.DATA_RULE, \"", conditionClass.getInstanceType().getName(), "\", ", pathId, ", DEFINITION));"));
						out.writeln("}");
						out.newline();
						out.writeln(String.class, " ", defaultClassFailureMessageId, " = ", defaultClassResultId, ".getError();");
						out.writeln("if (", defaultClassFailureMessageId, " == null || ", defaultClassFailureMessageId, ".contains(\"Null\") || ", defaultClassFailureMessageId, " == \"\") {");
						out.indented(() -> out.writeln(defaultClassFailureMessageId, " = \"Condition has failed.\";"));
						out.writeln("}");
						out.writeln("return ", Arrays.class, ".asList(", ValidationResult.class, ".failure(NAME, ", ValidationResult.ValidationType.class, ".DATA_RULE, \"", conditionClass.getInstanceType().getName(), "\", ", pathId, ", DEFINITION, ", defaultClassFailureMessageId, "));");
					});
					out.writeln("}");
					out.newline();
					out.write("private ", ComparisonResult.class, " executeDataRule(", conditionClass.getInstanceClass(), " ", defaultClassExecuteInstanceId);
					for (TypeParameter param : params.keySet()) {
						out.write(", ", params.get(param), " ", defaultClassExecuteScope.getIdentifierOrThrow(param));
					}
					out.writeln(") {");
					out.indented(() -> {
						out.write("try ");
						out.write(expressionHandler.javaCode(condition.getExpression(), typeUtil.COMPARISON_RESULT, defaultClassExecuteBodyScope).completeAsReturn().toBlock());
						out.newline();
						out.writeln("catch (", Exception.class, " ", defaultClassExecuteExceptionId, ") {");
						out.indented(() -> out.writeln("return ", ComparisonResult.class, ".failure(", defaultClassExecuteExceptionId, ".getMessage());"));
						out.writeln("}");
					});
					out.writeln("}");
				});
				out.writeln("}");
				out.newline();
				out.writeln("@SuppressWarnings(\"unused\")");
				out.write(noOpClass.asClassDeclaration());
				out.writeln(" {");
				out.indented(() -> {
					out.newline();
					out.writeln("@Override");
					out.write("public ", List.class, "<", ValidationResult.class, "<?>> getValidationResults(", RosettaPath.class, " ", pathId, ", ", conditionClass.getInstanceClass(), " ", instanceId);
					for (TypeParameter param : params.keySet()) {
						out.write(", ", params.get(param), " ", getValidationResultsScope.getIdentifierOrThrow(param));
					}
					out.writeln(") {");
					out.indented(() -> out.writeln("return ", Collections.class, ".emptyList();"));
					out.writeln("}");
				});
				out.writeln("}");
			});
			out.write("}");
		};
	}
}
