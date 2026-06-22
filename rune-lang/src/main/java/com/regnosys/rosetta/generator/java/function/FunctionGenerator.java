package com.regnosys.rosetta.generator.java.function;

import static com.regnosys.rosetta.generator.java.enums.EnumHelper.formatEnumName;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.uncapitalize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.ImplementedBy;
import com.regnosys.rosetta.codegen.api.CodeRenderer;
import com.regnosys.rosetta.codegen.api.CodeWriter;
import com.regnosys.rosetta.generator.GeneratedIdentifier;
import com.regnosys.rosetta.generator.java.FluentRObjectJavaClassGenerator;
import com.regnosys.rosetta.generator.java.expression.ExpressionGenerator;
import com.regnosys.rosetta.generator.java.expression.JavaDependencyProvider;
import com.regnosys.rosetta.generator.java.expression.TypeCoercionService;
import com.regnosys.rosetta.generator.java.scoping.JavaClassScope;
import com.regnosys.rosetta.generator.java.scoping.JavaIdentifierRepresentationService;
import com.regnosys.rosetta.generator.java.scoping.JavaMethodScope;
import com.regnosys.rosetta.generator.java.scoping.JavaStatementScope;
import com.regnosys.rosetta.generator.java.statement.JavaStatement;
import com.regnosys.rosetta.generator.java.statement.builder.JavaExpression;
import com.regnosys.rosetta.generator.java.statement.builder.JavaStatementBuilder;
import com.regnosys.rosetta.generator.java.statement.builder.JavaVariable;
import com.regnosys.rosetta.generator.java.types.JavaPojoInterface;
import com.regnosys.rosetta.generator.java.types.JavaPojoProperty;
import com.regnosys.rosetta.generator.java.types.JavaPojoPropertyOperationType;
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator;
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil;
import com.regnosys.rosetta.generator.java.types.RGeneratedJavaClass;
import com.regnosys.rosetta.generator.java.types.RJavaFieldWithMeta;
import com.regnosys.rosetta.generator.java.types.RJavaPojoInterface;
import com.regnosys.rosetta.generator.java.types.RJavaReferenceWithMeta;
import com.regnosys.rosetta.generator.java.types.RJavaWithMetaValue;
import com.regnosys.rosetta.generator.java.util.CodeWriterTargetStringConcatenation;
import com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil;
import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions;
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs;
import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.rosetta.RosettaFeature;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaSymbol;
import com.regnosys.rosetta.rosetta.expression.AsKeyOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaFeatureCall;
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference;
import com.regnosys.rosetta.rosetta.expression.RosettaUnaryOperation;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Condition;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.rosetta.simple.FunctionDispatch;
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration;
import com.regnosys.rosetta.rosetta.simple.TransformAnnotation;
import com.regnosys.rosetta.types.CardinalityProvider;
import com.regnosys.rosetta.types.RAttribute;
import com.regnosys.rosetta.types.RChoiceType;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.RFeature;
import com.regnosys.rosetta.types.RFunction;
import com.regnosys.rosetta.types.RFunctionOrigin;
import com.regnosys.rosetta.types.RMetaAnnotatedType;
import com.regnosys.rosetta.types.RMetaAttribute;
import com.regnosys.rosetta.types.RObjectFactory;
import com.regnosys.rosetta.types.ROperation;
import com.regnosys.rosetta.types.ROperationType;
import com.regnosys.rosetta.types.RShortcut;
import com.regnosys.rosetta.types.RType;
import com.regnosys.rosetta.types.RosettaTypeProvider;
import com.regnosys.rosetta.utils.ExpressionHelper;
import com.regnosys.rosetta.utils.ImplicitVariableUtil;
import com.regnosys.rosetta.utils.ModelIdProvider;
import com.regnosys.rosetta.utils.PojoPropertyUtil;
import com.regnosys.rosetta.utils.TransformAnnotationHelper;
import com.rosetta.model.lib.ModelSymbolId;
import com.rosetta.model.lib.annotations.RuneLabelProvider;
import com.rosetta.model.lib.functions.ConditionValidator;
import com.rosetta.model.lib.functions.IQualifyFunctionExtension;
import com.rosetta.model.lib.functions.ModelObjectValidator;
import com.rosetta.model.lib.functions.RosettaFunction;
import com.rosetta.model.lib.transform.SerializationFormat;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaParameterizedType;
import com.rosetta.util.types.JavaPrimitiveType;
import com.rosetta.util.types.JavaReferenceType;
import com.rosetta.util.types.JavaType;

import jakarta.inject.Inject;

public class FunctionGenerator extends FluentRObjectJavaClassGenerator<RFunction, RGeneratedJavaClass<? extends RosettaFunction>> {

	@Inject
	private ExpressionGenerator expressionGenerator;
	@Inject
	private JavaDependencyProvider dependencyProvider;
	@Inject
	private RosettaTypeProvider typeProvider;
	@Inject
	private RosettaFunctionExtensions functionExtensions;
	@Inject
	private ModelGeneratorUtil modelGeneratorUtil;
	@Inject
	private ExpressionHelper exprHelper;
	@Inject
	private CardinalityProvider cardinality;
	@Inject
	private JavaIdentifierRepresentationService identifierService;
	@Inject
	private JavaTypeTranslator typeTranslator;
	@Inject
	private RObjectFactory rObjectFactory;
	@Inject
	private ImplicitVariableUtil implicitVariableUtil;
	@Inject
	private JavaTypeUtil typeUtil;
	@Inject
	private TypeCoercionService coercionService;
	@Inject
	private ModelIdProvider modelIdProvider;
	@Inject
	private LabelProviderGeneratorUtil labelProviderUtil;
	@Inject
	private AliasUtil aliasUtil;
	@Inject
	private TransformAnnotationHelper transformAnnotationHelper;

	@Override
	protected Stream<? extends RFunction> streamObjects(RosettaModel model) {
		return model.getElements().stream()
				.filter(e -> e instanceof Function && !(e instanceof FunctionDispatch))
				.map(Function.class::cast)
				.map(rObjectFactory::buildRFunction);
	}

	@Override
	protected RGeneratedJavaClass<? extends RosettaFunction> createTypeRepresentation(RFunction rFunction) {
		return typeTranslator.toFunctionJavaClass(rFunction);
	}

	@Override
	protected CodeRenderer generateClass(RFunction rFunction, RGeneratedJavaClass<? extends RosettaFunction> javaFunctionClass, String version, JavaClassScope scope) {
		if (rFunction.getEObject() instanceof Function origin && functionExtensions.handleAsEnumFunction(origin)) {
			return dispatchClassBody(origin, javaFunctionClass, scope, version);
		}
		boolean overridesEvaluate = false;
		List<JavaType> functionInterfaces = new ArrayList<>();
		functionInterfaces.add(JavaClass.from(RosettaFunction.class));
		if (functionExtensions.isQualifierFunction(rFunction)) {
			overridesEvaluate = true;
			functionInterfaces.add(getQualifyingFunctionInterface(rFunction.getInputs()));
		}
		Map<Class<?>, CodeRenderer> annotations = new LinkedHashMap<>();
		if (rFunction.getEObject() instanceof Function origin && labelProviderUtil.shouldGenerateLabelProvider(origin)) {
			JavaClass<?> labelProviderClass = typeTranslator.toLabelProviderJavaClass(rFunction);
			annotations.put(RuneLabelProvider.class, out -> out.write("labelProvider=", labelProviderClass, ".class"));
		}
		if (rFunction.getEObject() instanceof Function origin && !origin.getTransform().isEmpty()) {
			addTransformAnnotation(origin.getTransform().get(0), annotations);
		}
		return rBuildClass(rFunction, javaFunctionClass, false, functionInterfaces, annotations, overridesEvaluate, scope);
	}

	public CodeRenderer rBuildClass(RFunction rFunction, RGeneratedJavaClass<? extends RosettaFunction> javaFunctionClass, boolean isStatic, List<JavaType> functionInterfaces, Map<Class<?>, CodeRenderer> annotations, boolean overridesEvaluate, JavaClassScope classScope) {
		return out -> {
			List<JavaClass<?>> dependencies = collectFunctionDependencies(rFunction);
			renderClassBody(out, rFunction, javaFunctionClass, isStatic, overridesEvaluate, dependencies, functionInterfaces, annotations, classScope);
		};
	}

	private void addTransformAnnotation(TransformAnnotation transform, Map<Class<?>, CodeRenderer> annotations) {
		Class<?> annotationClass = typeTranslator.toTransformAnnotationClass(transform.getKind());
		String id = transformAnnotationHelper.getSchemaId(transform).orElse(null);
		SerializationFormat format = transformAnnotationHelper.getFormat(transform).orElse(null);
		String configPath = transformAnnotationHelper.getConfigPath(transform).orElse(null);
		annotations.put(annotationClass, out -> {
			boolean needsSeparator = false;
			if (id != null) {
				out.write("id = \"", id, "\"");
				needsSeparator = true;
			}
			if (format != null) {
				if (needsSeparator) {
					out.write(", ");
				}
				out.write("format = ", SerializationFormat.class, ".", format.name());
			}
			if (configPath != null) {
				out.write(", configPath = \"", configPath, "\"");
			}
		});
	}

	private JavaType getQualifyingFunctionInterface(List<RAttribute> inputs) {
		RAttribute first = inputs.get(0);
		JavaClass<?> parameterVariable = typeTranslator.toListOrSingleJavaType(first.getRMetaAnnotatedType(), first.isMulti());
		return JavaParameterizedType.from(new TypeReference<IQualifyFunctionExtension<?>>() {}, parameterVariable);
	}

	private List<JavaClass<?>> collectFunctionDependencies(RFunction func) {
		List<RosettaExpression> expressions = new ArrayList<>();
		func.getPreConditions().forEach(c -> expressions.add(c.getExpression()));
		func.getPostConditions().forEach(c -> expressions.add(c.getExpression()));
		func.getOperations().forEach(o -> expressions.add(o.getExpression()));
		func.getShortcuts().forEach(s -> expressions.add(s.getExpression()));
		return dependencyProvider.javaDependencies(expressions);
	}

	private void renderClassBody(
			CodeWriter out,
			RFunction function,
			RGeneratedJavaClass<?> javaFunctionClass,
			boolean isStatic,
			boolean overridesEvaluate,
			List<JavaClass<?>> dependencies,
			List<JavaType> functionInterfaces,
			Map<Class<?>, CodeRenderer> annotations,
			JavaClassScope classScope) {
		List<RAttribute> inputs = function.getInputs();
		RAttribute output = function.getOutput();
		List<RShortcut> shortcuts = function.getShortcuts();
		List<ROperation> operations = function.getOperations();
		List<Condition> preConditions = function.getPreConditions();
		List<Condition> postConditions = function.getPostConditions();

		dependencies.forEach(dep -> classScope.createIdentifier(identifierService.toDependencyInstance(dep), uncapitalize(dep.getSimpleName())));

		RGeneratedJavaClass<?> defaultClass = javaFunctionClass.createNestedClassWithSuperclass(javaFunctionClass.getSimpleName() + "Default", javaFunctionClass);
		JavaClassScope defaultClassScope = classScope.createNestedClassScopeAndRegisterIdentifier(defaultClass);
		JavaType outputType = typeTranslator.toMetaJavaType(output);
		Map<RShortcut, Boolean> aliasOut = shortcuts.stream()
				.collect(Collectors.toMap(s -> s, s -> exprHelper.usesOutputParameter(s.getExpression())));

		GeneratedIdentifier conditionValidatorId = classScope.createUniqueIdentifier("conditionValidator");
		GeneratedIdentifier objectValidatorId = classScope.createUniqueIdentifier("objectValidator");

		JavaMethodScope evaluateScope = classScope.createMethodScope("evaluate");
		inputs.forEach(input -> evaluateScope.createIdentifier(input, input.getName()));
		JavaStatementScope evaluateBodyScope = evaluateScope.getBodyScope();
		evaluateBodyScope.createIdentifier(output, output.getName());
		GeneratedIdentifier outputBuilderId = needsBuilder(output)
				? evaluateBodyScope.createUniqueIdentifier(output.getName() + "Builder")
				: null;

		JavaMethodScope doEvaluateScope = classScope.createMethodScope("doEvaluate");
		inputs.forEach(input -> doEvaluateScope.createIdentifier(input, input.getName()));
		doEvaluateScope.createIdentifier(output, output.getName());

		JavaMethodScope defaultDoEvaluateScope = defaultClassScope.createMethodScope("doEvaluate");
		inputs.forEach(input -> defaultDoEvaluateScope.createIdentifier(input, input.getName()));
		defaultDoEvaluateScope.createIdentifier(output, output.getName());
		JavaStatementScope defaultDoEvaluateBodyScope = defaultDoEvaluateScope.getBodyScope();

		JavaMethodScope assignOutputScope = defaultClassScope.createMethodScope("assignOutput");
		inputs.forEach(input -> assignOutputScope.createIdentifier(input, input.getName()));
		assignOutputScope.createIdentifier(output, output.getName());
		operations.stream()
				.map(ROperation::getExpression)
				.filter(e -> implicitVariableUtil.implicitVariableExistsInContext(e))
				.map(e -> identifierService.getImplicitVarInContext(e))
				.distinct()
				.forEach(v -> assignOutputScope.createKeySynonym(v, inputs.get(0)));
		JavaStatementScope assignOutputBodyScope = assignOutputScope.getBodyScope();

		Map<RShortcut, JavaMethodScope> aliasScopes = new java.util.HashMap<>();
		Map<RShortcut, JavaMethodScope> defaultClassAliasScopes = new java.util.HashMap<>();
		shortcuts.forEach(alias -> {
			classScope.createIdentifier(alias, alias.getName());

			JavaMethodScope aliasScope = classScope.createMethodScope(alias.getName());
			aliasScopes.put(alias, aliasScope);

			JavaMethodScope defaultClassAliasScope = defaultClassScope.createMethodScope(alias.getName());
			defaultClassAliasScopes.put(alias, defaultClassAliasScope);

			if (aliasUtil.requiresOutput(alias)) {
				aliasScope.createIdentifier(output, output.getName());
				defaultClassAliasScope.createIdentifier(output, output.getName());
			}
			inputs.forEach(input -> {
				aliasScope.createIdentifier(input, input.getName());
				defaultClassAliasScope.createIdentifier(input, input.getName());
			});
		});

		annotations.forEach((annotationClass, annotationBody) -> {
			out.write("@", annotationClass, "(");
			out.write(annotationBody);
			out.writeln(")");
		});
		out.writeln("@", ImplementedBy.class, "(", defaultClass, ".class)");
		out.write("public ", isStatic ? "static " : "", "abstract class ", javaFunctionClass.getSimpleName());
		if (!functionInterfaces.isEmpty()) {
			out.write(" implements ");
			out.join(functionInterfaces, ",");
		}
		out.writeln(" {");
		out.indented(() -> {
			if (!preConditions.isEmpty() || !postConditions.isEmpty()) {
				out.newline();
				out.writeln("@", javax.inject.Inject.class, " protected ", ConditionValidator.class, " ", conditionValidatorId, ";");
			}
			if (needsBuilder(output)) {
				out.newline();
				out.writeln("@", javax.inject.Inject.class, " protected ", ModelObjectValidator.class, " ", objectValidatorId, ";");
			}
			if (!dependencies.isEmpty()) {
				out.newline();
				out.writeln("// RosettaFunction dependencies");
				out.writeln("//");
				dependencies.forEach(dep -> out.writeln("@", javax.inject.Inject.class, " protected ", dep, " ", classScope.getIdentifierOrThrow(identifierService.toDependencyInstance(dep)), ";"));
			}
			out.newline();

			out.writeln("/**");
			inputs.forEach(input -> renderJavadocTag(out, "@param", evaluateScope.getIdentifierOrThrow(input), input.getDefinition()));
			renderJavadocTag(out, "@return", evaluateBodyScope.getIdentifierOrThrow(output), output.getDefinition());
			out.writeln(" */");
			if (overridesEvaluate) {
				out.writeln("@Override");
			}
			out.write("public ", outputType, " evaluate(");
			renderInputsAsParameters(out, inputs, evaluateScope);
			out.writeln(") {");
			out.indented(() -> {
				if (!preConditions.isEmpty()) {
					out.writeln("// pre-conditions");
					preConditions.forEach(cond -> {
						renderContributeCondition(out, cond, conditionValidatorId, evaluateBodyScope);
						out.newline();
					});
				}
				out.write(toBuilderType(output), " ", needsBuilder(output) ? outputBuilderId : evaluateBodyScope.getIdentifierOrThrow(output), " = doEvaluate(");
				renderInputsAsArguments(out, inputs, evaluateBodyScope);
				out.writeln(");");
				if (needsBuilder(output)) {
					out.newline();
					out.writeln("final ", outputType, " ", evaluateBodyScope.getIdentifierOrThrow(output), ";");
					out.writeln("if (", outputBuilderId, " == null) {");
					out.indented(() -> out.writeln(evaluateBodyScope.getIdentifierOrThrow(output), " = null;"));
					out.writeln("} else {");
					out.indented(() -> {
						out.write(evaluateBodyScope.getIdentifierOrThrow(output), " = ", outputBuilderId);
						if (output.isMulti()) {
							out.write(".stream().map(", typeTranslator.toJavaReferenceType(output.getRMetaAnnotatedType()), "::build).collect(", Collectors.class, ".toList())");
						} else {
							out.write(".build()");
						}
						out.writeln(";");
						out.writeln(objectValidatorId, ".validate(", typeTranslator.toJavaReferenceType(output.getRMetaAnnotatedType()), ".class, ", evaluateBodyScope.getIdentifierOrThrow(output), ");");
					});
					out.writeln("}");
				}
				if (!postConditions.isEmpty()) {
					out.newline();
					out.writeln("// post-conditions");
					postConditions.forEach(cond -> {
						renderContributeCondition(out, cond, conditionValidatorId, evaluateBodyScope);
						out.newline();
					});
				}
				out.writeln("return ", evaluateBodyScope.getIdentifierOrThrow(output), ";");
			});
			out.writeln("}");
			out.newline();

			out.write("protected abstract ", toBuilderType(output), " doEvaluate(");
			renderInputsAsParameters(out, inputs, doEvaluateScope);
			out.writeln(");");

			shortcuts.forEach(alias -> {
				JavaMethodScope aliasScope = aliasScopes.get(alias);
				out.newline();
				out.write("protected abstract ", aliasUtil.getReturnType(alias), " ", classScope.getIdentifierOrThrow(alias), "(");
				out.write(CodeWriterTargetStringConcatenation.asCodeRenderer(aliasUtil.getParameters(alias, aliasScope)));
				out.writeln(");");
			});
			out.newline();

			out.write("public static ");
			out.write(CodeWriterTargetStringConcatenation.asCodeRenderer(defaultClass.asClassDeclaration()));
			out.writeln(" {");
			out.indented(() -> {
				out.writeln("@Override");
				out.write("protected ", toBuilderType(output), " doEvaluate(");
				renderInputsAsParameters(out, inputs, defaultDoEvaluateScope);
				out.writeln(") {");
				out.indented(() -> {
					inputs.stream().filter(RAttribute::isMulti).forEach(input -> {
						out.writeln("if (", defaultDoEvaluateBodyScope.getIdentifierOrThrow(input), " == null) {");
						out.indented(() -> out.writeln(defaultDoEvaluateBodyScope.getIdentifierOrThrow(input), " = ", Collections.class, ".emptyList();"));
						out.writeln("}");
					});
					out.write(toBuilderType(output), " ", defaultDoEvaluateBodyScope.getIdentifierOrThrow(output), " = ");
					if (output.isMulti()) {
						out.write("new ", ArrayList.class, "<>()");
					} else if (needsBuilder(output)) {
						out.write(typeTranslator.toListOrSingleJavaType(output.getRMetaAnnotatedType(), output.isMulti()), ".builder()");
					} else {
						out.write("null");
					}
					out.writeln(";");
					out.write("return assignOutput(", defaultDoEvaluateBodyScope.getIdentifierOrThrow(output));
					if (!inputs.isEmpty()) {
						out.write(", ");
					}
					renderInputsAsArguments(out, inputs, defaultDoEvaluateBodyScope);
					out.writeln(");");
				});
				out.writeln("}");
				out.newline();

				out.write("protected ", toBuilderType(output), " assignOutput(", toBuilderType(output), " ", assignOutputScope.getIdentifierOrThrow(output));
				if (!inputs.isEmpty()) {
					out.write(", ");
				}
				renderInputsAsParameters(out, inputs, assignOutputScope);
				out.writeln(") {");
				boolean functionHasDeepOperations = operations.stream().anyMatch(o -> o.getPathTail().size() > 0);
				out.indented(() -> {
					operations.forEach(operation -> {
						out.write(assign(assignOutputBodyScope, operation, function, aliasOut, output, functionHasDeepOperations).asStatementList());
						out.newline();
					});
					if (!needsBuilder(output)) {
						out.writeln("return ", assignOutputBodyScope.getIdentifierOrThrow(output), ";");
					} else {
						out.write("return ", Optional.class, ".ofNullable(", assignOutputBodyScope.getIdentifierOrThrow(output), ")");
						out.newline();
						if (output.isMulti()) {
							out.write("    .map(o -> o.stream().map(i -> i.prune()).collect(", Collectors.class, ".toList()))");
						} else {
							out.write("    .map(o -> o.prune())");
						}
						out.newline();
						out.writeln("    .orElse(null);");
					}
				});
				out.writeln("}");

				shortcuts.forEach(alias -> {
					JavaMethodScope aliasScope = defaultClassAliasScopes.get(alias);
					JavaType returnType = aliasUtil.getReturnType(alias);
					JavaStatementBuilder body = expressionGenerator.javaCode(alias.getExpression(), returnType, aliasScope.getBodyScope());
					JavaStatementBuilder safeBody = aliasUtil.requiresOutput(alias)
							? body.mapExpressionIfNotNull(it -> JavaExpression.from(o -> o.write("toBuilder(", it, ")"), returnType))
							: body;
					out.newline();
					out.writeln("@Override");
					out.write("protected ", returnType, " ", defaultClassScope.getIdentifierOrThrow(alias), "(");
					out.write(CodeWriterTargetStringConcatenation.asCodeRenderer(aliasUtil.getParameters(alias, aliasScope)));
					out.write(") ");
					out.writeln(safeBody.completeAsReturn().toBlock());
				});
			});
			out.writeln("}");

			if (functionExtensions.isQualifierFunction(function)) {
				out.newline();
				out.writeln("@Override");
				out.writeln("public String getNamePrefix() {");
				out.indented(() -> out.writeln("return \"", functionExtensions.getQualifierAnnotations(function.getAnnotations()).get(0).getAnnotation().getPrefix(), "\";"));
				out.writeln("}");
			}
		});
		out.write("}");
	}

	private CodeRenderer dispatchClassBody(Function function, RGeneratedJavaClass<? extends RosettaFunction> javaFunctionClass, JavaClassScope classScope, String version) {
		return out -> {
			RFunction rfunc = rObjectFactory.buildRFunction(function);
			List<FunctionDispatch> dispatchingFuncs = new ArrayList<>();
			functionExtensions.getDispatchingFunctions(function).forEach(dispatchingFuncs::add);
			dispatchingFuncs.sort(java.util.Comparator.comparing(FunctionDispatch::getName));
			String enumParam = function.getInputs().stream()
					.filter(i -> i.getTypeCall().getType() instanceof RosettaEnumeration)
					.findFirst().orElseThrow().getName();
			JavaType outputType = outputTypeOrVoid(function);

			dispatchingFuncs.forEach(f -> classScope.createIdentifier(f, uncapitalize(function.getName() + capitalize(f.getValue().getValue().getName()))));

			Map<FunctionDispatch, RFunction> enumFuncToRFunc = new java.util.HashMap<>();
			Map<FunctionDispatch, RGeneratedJavaClass<? extends RosettaFunction>> enumFuncToClass = new java.util.HashMap<>();
			Map<FunctionDispatch, JavaClassScope> enumFuncToScope = new java.util.HashMap<>();
			dispatchingFuncs.forEach(enumFunc -> {
				List<RShortcut> allShortcuts = new ArrayList<>();
				function.getShortcuts().forEach(s -> allShortcuts.add(rObjectFactory.buildRShortcut(s)));
				enumFunc.getShortcuts().forEach(s -> allShortcuts.add(rObjectFactory.buildRShortcut(s)));
				RFunction rFunction = new RFunction(
						enumFunc,
						null,
						null,
						new ModelSymbolId(modelIdProvider.toDottedPath(function.getModel()), function.getName() + formatEnumName(enumFunc.getValue().getValue().getName())),
						enumFunc.getDefinition(),
						function.getInputs().stream().map(i -> rObjectFactory.buildRAttributeWithEnclosingType(null, i)).collect(Collectors.toList()),
						rObjectFactory.buildRAttributeWithEnclosingType(null, function.getOutput()),
						RFunctionOrigin.FUNCTION,
						enumFunc.getConditions(),
						enumFunc.getPostConditions(),
						allShortcuts,
						enumFunc.getOperations().stream().map(o -> rObjectFactory.buildROperation(o)).collect(Collectors.toList()),
						enumFunc.getAnnotations());
				RGeneratedJavaClass<? extends RosettaFunction> nestedClass = javaFunctionClass.createNestedClass(rFunction.getAlphanumericName(), RosettaFunction.class);
				JavaClassScope scope = classScope.createNestedClassScopeAndRegisterIdentifier(nestedClass);
				enumFuncToRFunc.put(enumFunc, rFunction);
				enumFuncToClass.put(enumFunc, nestedClass);
				enumFuncToScope.put(enumFunc, scope);
			});

			JavaMethodScope evaluateScope = classScope.createMethodScope("evaluate");
			rfunc.getInputs().forEach(input -> evaluateScope.createIdentifier(input, input.getName()));
			JavaStatementScope evaluateBodyScope = evaluateScope.getBodyScope();

			out.write(modelGeneratorUtil.javadoc(function.getDefinition(), function.getReferences(), version));
			out.writeln("public class ", javaFunctionClass, " implements ", RosettaFunction.class, " {");
			out.indented(() -> {
				out.newline();
				dispatchingFuncs.forEach(enumFunc -> out.writeln("@", javax.inject.Inject.class, " protected ", enumFuncToClass.get(enumFunc), " ", classScope.getIdentifierOrThrow(enumFunc), ";"));
				out.newline();
				out.write("public ", outputType, " evaluate(");
				renderInputsAsParameters(out, rfunc.getInputs(), evaluateScope);
				out.writeln(") {");
				out.indented(() -> {
					out.writeln("switch (", enumParam, ") {");
					out.indented(() -> {
						dispatchingFuncs.forEach(enumFunc -> {
							out.writeln("case ", formatEnumName(enumFunc.getValue().getValue().getName()), ":");
							out.indented(() -> {
								out.write("return ", evaluateBodyScope.getIdentifierOrThrow(enumFunc), ".evaluate(");
								renderInputsAsArguments(out, rfunc.getInputs(), evaluateBodyScope);
								out.writeln(");");
							});
						});
						out.writeln("default:");
						out.indented(() -> out.writeln("throw new IllegalArgumentException(\"Enum value not implemented: \" + ", enumParam, ");"));
					});
					out.writeln("}");
				});
				out.writeln("}");

				dispatchingFuncs.forEach(enumFunc -> {
					RFunction rFunction = enumFuncToRFunc.get(enumFunc);
					RGeneratedJavaClass<? extends RosettaFunction> nestedClass = enumFuncToClass.get(enumFunc);
					JavaClassScope scope = enumFuncToScope.get(enumFunc);
					out.newline();
					out.write(rBuildClass(rFunction, nestedClass, true, List.of(JavaClass.from(RosettaFunction.class)), Collections.emptyMap(), false, scope));
				});
			});
			out.write("}");
		};
	}

	private boolean assignAsKey(ROperation op) {
		return op.getExpression() instanceof AsKeyOperation;
	}

	private JavaStatement assign(JavaStatementScope scope, ROperation op, RFunction function, Map<RShortcut, Boolean> outs, RAttribute attribute, boolean functionHasDeepOperations) {
		if (op.getPathTail().isEmpty()) {
			// assign function output object
			JavaType expressionType = typeTranslator.toMetaJavaType(attribute);
			JavaStatementBuilder javaExpr = expressionGenerator.javaCode(op.getExpression(), expressionType, scope);
			JavaType effectiveExprType = javaExpr.getExpressionType();
			if (needsBuilder(attribute)) {
				javaExpr = javaExpr.mapExpressionIfNotNull(it -> JavaExpression.from(o -> {
					o.write("toBuilder(", it);
					if (functionHasDeepOperations) {
						o.write(", () -> ", effectiveExprType, ".builder()");
					}
					o.write(")");
				}, toBuilderType(attribute)));
			} else {
				boolean needsToCopy =
						op.getROperationType() == ROperationType.SET
						&& typeUtil.isList(effectiveExprType)
						&& function.getOperations().stream().anyMatch(o -> o.getROperationType() == ROperationType.ADD);
				if (needsToCopy) {
					javaExpr = javaExpr.mapExpressionIfNotNull(it -> JavaExpression.from(
							o -> o.write("new ", ArrayList.class, "<>(", it, ")"),
							typeUtil.wrap(typeUtil.LIST, typeUtil.getItemType(effectiveExprType))));
				}
			}
			switch (op.getROperationType()) {
				case ADD: {
					JavaStatementBuilder coerced = coercionService.addCoercions(javaExpr, attribute.isMulti() ? typeUtil.wrapExtends(typeUtil.LIST, toBuilderItemType(attribute)) : toBuilderItemType(attribute), scope);
					return coerced
							.mapExpression(it -> JavaExpression.from(
									o -> { o.write(assignTarget(op, function, outs, scope)); o.write(".addAll(", it, ")"); },
									JavaPrimitiveType.VOID))
							.completeAsExpressionStatement();
				}
				case SET: {
					JavaStatementBuilder coerced = coercionService.addCoercions(javaExpr, toBuilderType(attribute), scope);
					return coerced
							.mapExpression(it -> JavaExpression.from(
									o -> { o.write(assignTarget(op, function, outs, scope)); o.write(" = ", it); },
									JavaPrimitiveType.VOID))
							.completeAsExpressionStatement();
				}
				default:
					throw new IllegalStateException("Unexpected operation type: " + op.getROperationType());
			}
		}
		// assign an attribute of the function output object
		return assignValue(scope, op, assignAsKey(op))
				.collapseToSingleExpression(scope)
				.mapExpression(it -> {
					JavaExpression expr = assignTarget(op, function, outs, scope);

					// path intermediary
					int intermediarySegmentSize = op.getPathTail().size() - 1;
					for (int pathIndex = 0; pathIndex < intermediarySegmentSize; pathIndex++) {
						RFeature seg = op.getPathTail().get(pathIndex);

						if (typeUtil.getItemType(expr.getExpressionType()) instanceof RJavaWithMetaValue withMeta) {
							JavaExpression metaExpr = expr;
							expr = JavaExpression.from(o -> o.write(metaExpr, ".getOrCreateValue()"), withMeta.getValueType());
						}

						JavaPojoProperty prop = getPojoProperty(seg, typeUtil.getItemType(expr.getExpressionType()));
						JavaExpression oldExpr = expr;
						JavaType itemType = typeUtil.getItemType(prop.getType());
						expr = JavaExpression.from(o -> {
							o.write(oldExpr);
							o.newline();
							o.write("    .", prop.getOperationName(JavaPojoPropertyOperationType.GET_OR_CREATE), "(", typeUtil.isList(prop.getType()) ? "0" : "", ")");
						}, itemType);
					}

					// end of path
					RFeature seg = op.getPathTail().get(op.getPathTail().size() - 1);
					JavaExpression oldExpr = expr;
					JavaType outputExpressionType = typeUtil.getItemType(expr.getExpressionType());
					JavaPojoProperty prop = getPojoProperty(seg, outputExpressionType);

					boolean requiresValueSetter = requiresValueSetter(outputExpressionType, prop, seg, op);
					String propertySetterName = getPropertySetterName(outputExpressionType, prop, seg, op.getROperationType(), requiresValueSetter);
					expr = JavaExpression.from(o -> {
						o.write(oldExpr);
						o.newline();
						o.write("    ");
						o.write(generateMetaWrapperCreator(seg, prop, outputExpressionType));
						o.write(".", propertySetterName, "(", it, ")");
					}, JavaPrimitiveType.VOID);

					return expr;
				})
				.completeAsExpressionStatement();
	}

	private String getPropertySetterName(JavaType outputExpressionType, JavaPojoProperty prop, RFeature segment, ROperationType operationType, boolean requiresValueSetter) {
		if (outputExpressionType instanceof RJavaWithMetaValue || (segment instanceof RMetaAttribute && outputExpressionType instanceof RJavaPojoInterface)) {
			String prefix = operationType == ROperationType.ADD ? "add" : "set";
			String segmentPropName = capitalize(PojoPropertyUtil.toPojoPropertyName(segment));
			if (requiresValueSetter) {
				return prefix + segmentPropName + "Value";
			}
			return prefix + segmentPropName;
		}
		if (requiresValueSetter) {
			if (operationType == ROperationType.ADD) {
				return prop.getOperationName(JavaPojoPropertyOperationType.ADD_VALUE);
			}
			return prop.getOperationName(JavaPojoPropertyOperationType.SET_VALUE);
		}
		if (operationType == ROperationType.ADD) {
			return prop.getOperationName(JavaPojoPropertyOperationType.ADD);
		}
		return prop.getOperationName(JavaPojoPropertyOperationType.SET);
	}

	private boolean requiresValueSetter(JavaType outputExpressionType, JavaPojoProperty outerPojoProperty, RFeature segment, ROperation op) {
		JavaType outerPropertyType = typeUtil.getItemType(outerPojoProperty.getType());
		JavaPojoProperty innerProp = (outputExpressionType instanceof RJavaWithMetaValue && outerPropertyType instanceof JavaPojoInterface)
				? getPojoProperty(segment, outerPropertyType)
				: outerPojoProperty;

		JavaType innerPropType = typeUtil.getItemType(innerProp.getType());

		boolean isMetaSegment = segment instanceof RAttribute attr && attr.getRMetaAnnotatedType().hasAttributeMeta();

		return innerPropType instanceof RJavaWithMetaValue && !isMetaSegment && !assignAsKey(op);
	}

	private CodeRenderer generateMetaWrapperCreator(RFeature seg, JavaPojoProperty prop, JavaType expressionType) {
		if (expressionType instanceof RJavaFieldWithMeta) {
			return out -> {
				if (seg instanceof RMetaAttribute) {
					out.write(".getOrCreateMeta()");
				} else {
					out.write(".", prop.getOperationName(JavaPojoPropertyOperationType.GET_OR_CREATE), "()");
				}
			};
		}
		if (expressionType instanceof RJavaReferenceWithMeta && seg instanceof RMetaAttribute && "address".equals(seg.getName())) {
			return out -> out.write(".", prop.getOperationName(JavaPojoPropertyOperationType.GET_OR_CREATE), "()");
		}
		if (expressionType instanceof RJavaReferenceWithMeta && !(seg instanceof RMetaAttribute)) {
			return out -> out.write(".getOrCreateValue()");
		}
		if (expressionType instanceof RJavaPojoInterface && seg instanceof RMetaAttribute) {
			return out -> out.write(".", prop.getOperationName(JavaPojoPropertyOperationType.GET_OR_CREATE), "()");
		}
		return out -> {};
	}

	// The type of the output expression to be set and the pojo property type are not the same when working with meta
	private JavaPojoProperty getPojoProperty(RFeature seg, JavaType outputExpressionType) {
		if (seg instanceof RMetaAttribute && (outputExpressionType instanceof RJavaFieldWithMeta || outputExpressionType instanceof RJavaPojoInterface)) {
			return ((JavaPojoInterface) outputExpressionType).findProperty("meta");
		} else if (seg instanceof RMetaAttribute && outputExpressionType instanceof RJavaReferenceWithMeta) {
			return ((JavaPojoInterface) outputExpressionType).findProperty("reference");
		} else if (outputExpressionType instanceof RJavaWithMetaValue) {
			return ((JavaPojoInterface) outputExpressionType).findProperty("value");
		} else {
			return ((JavaPojoInterface) outputExpressionType).findProperty(seg.getName());
		}
	}

	private JavaStatementBuilder assignValue(JavaStatementScope scope, ROperation op, boolean assignAsKey) {
		if (assignAsKey) {
			JavaClass<?> metaClass = typeTranslator.operationToReferenceWithMetaType(op);
			if (cardinality.isMulti(op.getExpression())) {
				JavaStatementScope lambdaScope = scope.lambdaScope();
				GeneratedIdentifier item = lambdaScope.createUniqueIdentifier("item");
				return expressionGenerator.javaCode(op.getExpression(), typeUtil.wrapExtendsWithoutMeta(typeUtil.MAPPER_C, op.getExpression()), scope)
						.collapseToSingleExpression(scope)
						.mapExpression(it -> JavaExpression.from(o -> {
							o.write(it);
							o.newline();
							o.write("    .getItems()");
							o.newline();
							o.write("    .map(", item, " -> ", metaClass, ".builder()");
							o.newline();
							o.write("        .setExternalReference(", item, ".getMappedObject().getMeta().getExternalKey())");
							o.newline();
							o.write("        .setGlobalReference(", item, ".getMappedObject().getMeta().getGlobalKey())");
							o.newline();
							o.write("        .build())");
							o.newline();
							o.write("    .collect(", Collectors.class, ".toList())");
						}, typeUtil.wrap(typeUtil.LIST, metaClass)));
			}
			RType exprRType = typeProvider.getRMetaAnnotatedType(op.getExpression()).getRType();
			if (exprRType instanceof RChoiceType choiceType) {
				JavaClass<?> deepPathUtil = typeTranslator.toDeepPathUtilJavaClass(choiceType.asRDataType());
				JavaStatementScope lambdaScope = scope.lambdaScope();
				GeneratedIdentifier c = lambdaScope.createUniqueIdentifier(uncapitalize(choiceType.asRDataType().getName()));
				GeneratedIdentifier gc = lambdaScope.createUniqueIdentifier(uncapitalize(choiceType.asRDataType().getName()));
				return expressionGenerator.javaCode(op.getExpression(), typeTranslator.toJavaReferenceType(RMetaAnnotatedType.withNoMeta(typeProvider.getRMetaAnnotatedType(op.getExpression()).getRType())), scope)
						.declareAsVariable(true, op.getPathHead().getName() + pathTailSuffix(op), scope)
						.mapExpression(it -> JavaExpression.from(o -> {
							o.write(metaClass, ".builder()");
							o.newline();
							o.write("    .setExternalReference(", Optional.class, ".ofNullable(", it, ")");
							o.newline();
							o.write("        .map(", c, " -> ", scope.getIdentifierOrThrow(identifierService.toDependencyInstance(deepPathUtil)), ".metaChooseKey(", c, "))");
							o.newline();
							o.write("        .orElse(null))");
							o.newline();
							o.write("    .setGlobalReference(", Optional.class, ".ofNullable(", it, ")");
							o.newline();
							o.write("        .map(", gc, " -> ", scope.getIdentifierOrThrow(identifierService.toDependencyInstance(deepPathUtil)), ".metaChooseGlobalKey(", gc, "))");
							o.newline();
							o.write("        .orElse(null))");
							o.newline();
							o.write("    .build()");
						}, metaClass));
			}
			JavaStatementScope lambdaScope = scope.lambdaScope();
			GeneratedIdentifier r = lambdaScope.createUniqueIdentifier("r");
			GeneratedIdentifier m = lambdaScope.createUniqueIdentifier("m");
			return expressionGenerator.javaCode(op.getExpression(), typeTranslator.toJavaReferenceType(RMetaAnnotatedType.withNoMeta(typeProvider.getRMetaAnnotatedType(op.getExpression()).getRType())), scope)
					.declareAsVariable(true, op.getPathHead().getName() + pathTailSuffix(op), scope)
					.mapExpression(it -> JavaExpression.from(o -> {
						o.write(metaClass, ".builder()");
						o.newline();
						o.write("    .setGlobalReference(", Optional.class, ".ofNullable(", it, ")");
						o.newline();
						o.write("        .map(", r, " -> ", r, ".getMeta())");
						o.newline();
						o.write("        .map(", m, " -> ", m, ".getGlobalKey())");
						o.newline();
						o.write("        .orElse(null))");
						o.newline();
						o.write("    .setExternalReference(", Optional.class, ".ofNullable(", it, ")");
						o.newline();
						o.write("        .map(", r, " -> ", r, ".getMeta())");
						o.newline();
						o.write("        .map(", m, " -> ", m, ".getExternalKey())");
						o.newline();
						o.write("        .orElse(null))");
						o.newline();
						o.write("    .build()");
					}, metaClass));
		}
		return expressionGenerator.javaCode(op.getExpression(), typeTranslator.operationToMetaJavaType(op), scope);
	}

	private String pathTailSuffix(ROperation op) {
		return op.getPathTail().stream().map(f -> capitalize(f.getName())).collect(Collectors.joining());
	}

	private JavaExpression assignTarget(ROperation operation, RFunction function, Map<RShortcut, Boolean> outs, JavaStatementScope scope) {
		Object root = operation.getPathHead();
		if (root instanceof RAttribute attr) {
			return new JavaVariable(scope.getIdentifierOrThrow(attr), typeTranslator.toJavaReferenceType(attr.getRMetaAnnotatedType()));
		}
		if (root instanceof RShortcut shortcut) {
			return unfoldLHSShortcut(shortcut, function, scope);
		}
		throw new IllegalStateException("Unexpected assign target: " + root);
	}

	private JavaExpression unfoldLHSShortcut(RShortcut shortcut, RFunction function, JavaStatementScope scope) {
		RosettaExpression e = shortcut.getExpression();
		if (e instanceof RosettaSymbolReference ref && ref.getSymbol() instanceof RosettaCallableWithArgs) {
			// assign-output for an alias
			return JavaExpression.from(
					o -> { o.write(scope.getIdentifierOrThrow(shortcut), "("); o.write(CodeWriterTargetStringConcatenation.asCodeRenderer(expressionGenerator.aliasCallArgs(shortcut, function, scope))); o.write(")"); },
					shortcutExpressionJavaType(shortcut));
		}
		return lhsExpand(e, scope);
	}

	private JavaExpression lhsExpand(Object f, JavaStatementScope scope) {
		if (f instanceof RosettaFeatureCall fc) {
			return lhsFeature(lhsExpand(fc.getReceiver(), scope), fc.getFeature());
		}
		if (f instanceof RosettaSymbolReference ref) {
			return lhsExpand(ref.getSymbol(), scope);
		}
		if (f instanceof ShortcutDeclaration sd) {
			return lhsExpand(sd.getExpression(), scope);
		}
		if (f instanceof RosettaUnaryOperation uo) {
			return lhsExpand(uo.getArgument(), scope);
		}
		if (f instanceof Attribute attribute) {
			RAttribute rAttribute = rObjectFactory.buildRAttribute(attribute);
			return new JavaVariable(scope.getIdentifierOrThrow(rAttribute), typeTranslator.toJavaReferenceType(rAttribute.getRMetaAnnotatedType()));
		}
		throw new IllegalStateException("No implementation for lhsExpand for " + f.getClass());
	}

	private JavaExpression lhsFeature(JavaExpression receiver, RosettaFeature f) {
		if (f instanceof Attribute attribute) {
			JavaPojoInterface t = (JavaPojoInterface) receiver.getExpressionType();
			JavaPojoProperty prop = t.findProperty(attribute.getName());
			return JavaExpression.from(
					o -> o.write(receiver, ".", prop.getOperationName(JavaPojoPropertyOperationType.GET_OR_CREATE), "(", typeUtil.isList(prop.getType()) ? "0" : "", ")"),
					typeUtil.getItemType(prop.getType()));
		}
		throw new IllegalStateException("No implementation for lhsFeature for " + f.getClass());
	}

	private void renderContributeCondition(CodeWriter out, Condition condition, GeneratedIdentifier conditionValidator, JavaStatementScope scope) {
		JavaStatementBuilder conditionBody = expressionGenerator.javaCode(condition.getExpression(), typeUtil.COMPARISON_RESULT, scope.lambdaScope());
		out.writeln(conditionValidator, ".validate(() -> ", conditionBody.toLambdaBody(), ",");
		out.writeln("    \"", condition.getDefinition(), "\");");
	}

	private JavaType outputTypeOrVoid(Function function) {
		Attribute out = functionExtensions.getOutput(function);
		if (out == null) {
			return JavaPrimitiveType.VOID;
		}
		if (functionExtensions.needsBuilder(out.getTypeCall().getType())) {
			return typeTranslator.toPolymorphicListOrSingleJavaType(typeProvider.getRTypeOfSymbol(out).getRType(), out.getCard().isIsMany());
		}
		return typeTranslator.toListOrSingleJavaType(typeProvider.getRTypeOfSymbol(out).getRType(), out.getCard().isIsMany());
	}

	private void renderJavadocTag(CodeWriter out, String tag, GeneratedIdentifier name, String definition) {
		String escaped = modelGeneratorUtil.escape(definition).toString();
		out.write(" * ", tag, " ", name);
		if (!escaped.isEmpty()) {
			out.write(" ", escaped);
		}
		out.newline();
	}

	private void renderInputsAsArguments(CodeWriter out, List<RAttribute> inputs, JavaStatementScope scope) {
		out.join(inputs, ", ", input -> out.write(scope.getIdentifierOrThrow(input)));
	}

	private void renderInputsAsParameters(CodeWriter out, List<RAttribute> inputs, JavaMethodScope scope) {
		out.join(inputs, ", ", input -> out.write(typeTranslator.toMetaJavaType(input), " ", scope.getIdentifierOrThrow(input)));
	}

	private JavaReferenceType shortcutExpressionJavaType(RShortcut feature) {
		RMetaAnnotatedType metaRType = typeProvider.getRMetaAnnotatedType(feature.getExpression());
		return typeTranslator.toJavaReferenceType(metaRType);
	}

	private JavaType toBuilderItemType(RAttribute rAttribute) {
		JavaClass<?> javaType = (JavaClass<?>) typeTranslator.toJavaReferenceType(rAttribute.getRMetaAnnotatedType());
		if (needsBuilder(javaType)) {
			return ((JavaPojoInterface) javaType).toBuilderInterface();
		}
		return javaType;
	}

	private JavaType toBuilderType(RAttribute rAttribute) {
		JavaType javaType = toBuilderItemType(rAttribute);
		if (rAttribute.isMulti()) {
			return typeUtil.wrap(typeUtil.LIST, javaType);
		}
		return javaType;
	}

	private boolean needsBuilder(RAttribute rAttribute) {
		JavaClass<?> javaType = (JavaClass<?>) typeTranslator.toJavaReferenceType(rAttribute.getRMetaAnnotatedType());
		return needsBuilder(javaType);
	}

	private boolean needsBuilder(JavaClass<?> javaClass) {
		return javaClass instanceof JavaPojoInterface;
	}
}
