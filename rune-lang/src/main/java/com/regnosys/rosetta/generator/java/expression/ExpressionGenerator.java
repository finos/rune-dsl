/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.regnosys.rosetta.generator.java.expression;

import static com.regnosys.rosetta.generator.java.types.JavaPojoPropertyOperationType.SET;
import static com.regnosys.rosetta.generator.java.types.JavaPojoPropertyOperationType.SET_VALUE;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.uncapitalize;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.regnosys.rosetta.RosettaEcoreUtil;
import com.regnosys.rosetta.codegen.api.CodeRenderer;
import com.regnosys.rosetta.codegen.api.CodeWriter;
import com.regnosys.rosetta.generator.GeneratedIdentifier;
import com.regnosys.rosetta.generator.GenerationException;
import com.regnosys.rosetta.generator.java.enums.EnumHelper;
import com.regnosys.rosetta.generator.java.function.AliasUtil;
import com.regnosys.rosetta.generator.java.scoping.JavaIdentifierRepresentationService;
import com.regnosys.rosetta.generator.java.scoping.JavaStatementScope;
import com.regnosys.rosetta.generator.java.statement.JavaLocalVariableDeclarationStatement;
import com.regnosys.rosetta.generator.java.statement.builder.JavaConditionalExpression;
import com.regnosys.rosetta.generator.java.statement.builder.JavaExpression;
import com.regnosys.rosetta.generator.java.statement.builder.JavaIfThenElseBuilder;
import com.regnosys.rosetta.generator.java.statement.builder.JavaLiteral;
import com.regnosys.rosetta.generator.java.statement.builder.JavaStatementBuilder;
import com.regnosys.rosetta.generator.java.statement.builder.JavaVariable;
import com.regnosys.rosetta.generator.java.types.JavaPojoInterface;
import com.regnosys.rosetta.generator.java.types.JavaPojoProperty;
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator;
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil;
import com.regnosys.rosetta.generator.java.types.RJavaFieldWithMeta;
import com.regnosys.rosetta.generator.java.types.RJavaPojoInterface;
import com.regnosys.rosetta.generator.java.types.RJavaReferenceWithMeta;
import com.regnosys.rosetta.generator.java.types.RJavaWithMetaValue;
import com.regnosys.rosetta.generator.java.util.CodeWriterTargetStringConcatenation;
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension;
import com.regnosys.rosetta.generator.java.util.PreferWildcardImportMethod;
import com.regnosys.rosetta.generator.java.util.RecordJavaUtil;
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs;
import com.regnosys.rosetta.rosetta.RosettaEnumValue;
import com.regnosys.rosetta.rosetta.RosettaExternalFunction;
import com.regnosys.rosetta.rosetta.RosettaFeature;
import com.regnosys.rosetta.rosetta.RosettaMetaType;
import com.regnosys.rosetta.rosetta.RosettaNamed;
import com.regnosys.rosetta.rosetta.RosettaRecordFeature;
import com.regnosys.rosetta.rosetta.RosettaRule;
import com.regnosys.rosetta.rosetta.RosettaSymbol;
import com.regnosys.rosetta.rosetta.RosettaTypeWithConditions;
import com.regnosys.rosetta.rosetta.TypeParameter;
import com.regnosys.rosetta.rosetta.expression.ArithmeticOperation;
import com.regnosys.rosetta.rosetta.expression.AsKeyOperation;
import com.regnosys.rosetta.rosetta.expression.AsOperation;
import com.regnosys.rosetta.rosetta.expression.CardinalityModifier;
import com.regnosys.rosetta.rosetta.expression.ChoiceOperation;
import com.regnosys.rosetta.rosetta.expression.ClosureParameter;
import com.regnosys.rosetta.rosetta.expression.ComparisonOperation;
import com.regnosys.rosetta.rosetta.expression.ConstructorKeyValuePair;
import com.regnosys.rosetta.rosetta.expression.DefaultOperation;
import com.regnosys.rosetta.rosetta.expression.DistinctOperation;
import com.regnosys.rosetta.rosetta.expression.EqualityOperation;
import com.regnosys.rosetta.rosetta.expression.ExistsModifier;
import com.regnosys.rosetta.rosetta.expression.FilterOperation;
import com.regnosys.rosetta.rosetta.expression.FirstOperation;
import com.regnosys.rosetta.rosetta.expression.FlattenOperation;
import com.regnosys.rosetta.rosetta.expression.InlineFunction;
import com.regnosys.rosetta.rosetta.expression.JoinOperation;
import com.regnosys.rosetta.rosetta.expression.LastOperation;
import com.regnosys.rosetta.rosetta.expression.ListLiteral;
import com.regnosys.rosetta.rosetta.expression.LogicalOperation;
import com.regnosys.rosetta.rosetta.expression.MapOperation;
import com.regnosys.rosetta.rosetta.expression.MaxOperation;
import com.regnosys.rosetta.rosetta.expression.MinOperation;
import com.regnosys.rosetta.rosetta.expression.ModifiableBinaryOperation;
import com.regnosys.rosetta.rosetta.expression.Necessity;
import com.regnosys.rosetta.rosetta.expression.OneOfOperation;
import com.regnosys.rosetta.rosetta.expression.ReduceOperation;
import com.regnosys.rosetta.rosetta.expression.ReverseOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaAbsentExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaBinaryOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaBooleanLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaConditionalExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaConstructorExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaContainsExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaCountOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaDeepFeatureCall;
import com.regnosys.rosetta.rosetta.expression.RosettaDisjointExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaExistsExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaFeatureCall;
import com.regnosys.rosetta.rosetta.expression.RosettaFunctionalOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaImplicitVariable;
import com.regnosys.rosetta.rosetta.expression.RosettaIntLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaNumberLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaOnlyElement;
import com.regnosys.rosetta.rosetta.expression.RosettaOnlyExistsExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaStringLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaSuperCall;
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference;
import com.regnosys.rosetta.rosetta.expression.RosettaUnaryOperation;
import com.regnosys.rosetta.rosetta.expression.SortOperation;
import com.regnosys.rosetta.rosetta.expression.SumOperation;
import com.regnosys.rosetta.rosetta.expression.SwitchCaseOrDefault;
import com.regnosys.rosetta.rosetta.expression.SwitchOperation;
import com.regnosys.rosetta.rosetta.expression.ThenOperation;
import com.regnosys.rosetta.rosetta.expression.ToDateOperation;
import com.regnosys.rosetta.rosetta.expression.ToDateTimeOperation;
import com.regnosys.rosetta.rosetta.expression.ToEnumOperation;
import com.regnosys.rosetta.rosetta.expression.ToIntOperation;
import com.regnosys.rosetta.rosetta.expression.ToNumberOperation;
import com.regnosys.rosetta.rosetta.expression.ToStringOperation;
import com.regnosys.rosetta.rosetta.expression.ToTimeOperation;
import com.regnosys.rosetta.rosetta.expression.ToZonedDateTimeOperation;
import com.regnosys.rosetta.rosetta.expression.WithMetaEntry;
import com.regnosys.rosetta.rosetta.expression.WithMetaOperation;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.ChoiceOption;
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration;
import com.regnosys.rosetta.types.CardinalityProvider;
import com.regnosys.rosetta.types.RAttribute;
import com.regnosys.rosetta.types.RChoiceOption;
import com.regnosys.rosetta.types.RChoiceType;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.REnumType;
import com.regnosys.rosetta.types.RFunction;
import com.regnosys.rosetta.types.RMetaAnnotatedType;
import com.regnosys.rosetta.types.RObjectFactory;
import com.regnosys.rosetta.types.RShortcut;
import com.regnosys.rosetta.types.RType;
import com.regnosys.rosetta.types.RosettaTypeProvider;
import com.regnosys.rosetta.types.TypeSystem;
import com.regnosys.rosetta.types.builtin.RBasicType;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;
import com.regnosys.rosetta.types.builtin.RRecordType;
import com.regnosys.rosetta.utils.ExpressionHelper;
import com.regnosys.rosetta.utils.ImplicitVariableUtil;
import com.regnosys.rosetta.utils.PojoPropertyUtil;
import com.regnosys.rosetta.utils.RosettaExpressionSwitch;
import com.rosetta.model.lib.expression.CardinalityOperator;
import com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe;
import com.rosetta.model.lib.expression.MapperMaths;
import com.rosetta.model.lib.mapper.MapperC;
import com.rosetta.model.lib.mapper.MapperS;
import com.rosetta.model.lib.meta.Reference;
import com.rosetta.model.lib.records.Date;
import com.rosetta.model.lib.validation.ChoiceRuleValidationMethod;
import com.rosetta.model.metafields.MetaFields;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaGenericTypeDeclaration;
import com.rosetta.util.types.JavaPrimitiveType;
import com.rosetta.util.types.JavaType;

import jakarta.inject.Inject;

public class ExpressionGenerator extends RosettaExpressionSwitch<JavaStatementBuilder, ExpressionGenerator.Context> {

	public static class Context {
		public JavaType expectedType;
		public JavaStatementScope scope;

		private Context copy() {
			Context copy = new Context();
			copy.expectedType = this.expectedType;
			copy.scope = this.scope;
			return copy;
		}

		public Context withExpected(JavaType newExpectedType) {
			Context copy = copy();
			copy.expectedType = newExpectedType;
			return copy;
		}

		public Context withScope(JavaStatementScope newScope) {
			Context copy = copy();
			copy.scope = newScope;
			return copy;
		}
	}

	private record InlineFunctionCode(CodeRenderer code, JavaType bodyType) {}

	private record MetaEntry(String name, JavaStatementBuilder value) {}

	@FunctionalInterface
	private interface SwitchCaseFold {
		JavaStatementBuilder apply(JavaStatementBuilder acc, SwitchCaseOrDefault switchCase, JavaExpression switchArg);
	}

	@Inject
	protected RosettaTypeProvider typeProvider;
	@Inject
	private CardinalityProvider cardinalityProvider;
	@Inject
	private AliasUtil aliasUtil;
	@Inject
	private RosettaEcoreUtil ecoreUtil;
	@Inject
	private ImportManagerExtension importManager;
	@Inject
	private ExpressionHelper exprHelper;
	@Inject
	private ImplicitVariableUtil implicitVariableUtil;
	@Inject
	private JavaIdentifierRepresentationService identifierService;
	@Inject
	private RecordJavaUtil recordUtil;
	@Inject
	private JavaTypeTranslator typeTranslator;
	@Inject
	private TypeSystem typeSystem;
	@Inject
	private RObjectFactory rObjectFactory;
	@Inject
	private TypeCoercionService typeCoercionService;
	@Inject
	private JavaTypeUtil typeUtil;
	@Inject
	private RBuiltinTypeService builtinTypeService;

	/**
	 * convert a rosetta expression to code
	 * ParamMpa params  - a map keyed by classname or positional index that provides variable names for expression parameters
	 */
	public JavaStatementBuilder javaCode(RosettaExpression expr, JavaType expectedType, JavaStatementScope scope) {
		Context context = new Context();
		context.expectedType = expectedType;
		context.scope = scope;
		return javaCode(expr, context);
	}

	public JavaStatementBuilder javaCode(RosettaExpression expr, Context context) {
		try {
			JavaStatementBuilder rawResult = doSwitch(expr, context);
			return typeCoercionService.addCoercions(rawResult, context.expectedType, context.scope);
		} catch (GenerationException e) {
			throw e;
		} catch (Exception e) {
			throw new GenerationException(e.getMessage(), expr.eResource().getURI(), expr, e);
		}
	}

	private PreferWildcardImportMethod runtimeMethod(String methodName) {
		return importManager.importWildcard(importManager.method(ExpressionOperatorsNullSafe.class, methodName));
	}

	private JavaStatementBuilder applyRuntimeMethod(JavaStatementBuilder expr, String methodName, JavaType resultType) {
		return expr.mapExpression(it -> JavaExpression.from(out -> out.write(runtimeMethod(methodName), "(", it, ")"), resultType));
	}

	private JavaStatementBuilder callableWithArgsCall(RosettaCallableWithArgs callable, List<RosettaExpression> arguments, Context context) {
		JavaStatementScope scope = context.scope;
		// Qualified because of the name clash with `java.util.function.Function`.
		if (callable instanceof com.regnosys.rosetta.rosetta.simple.Function || callable instanceof RosettaRule) {
			RFunction rCallable = callable instanceof com.regnosys.rosetta.rosetta.simple.Function function
					? rObjectFactory.buildRFunction(function)
					: rObjectFactory.buildRFunction((RosettaRule) callable);
			return evaluateCall(rCallable, scope.getIdentifierOrThrow(identifierService.toDependencyInstance(typeTranslator.toFunctionJavaClass(rCallable))), arguments, context);
		} else if (callable instanceof RosettaExternalFunction externalFunction) {
			RMetaAnnotatedType returnRType = typeProvider.getRTypeOfSymbol(externalFunction);
			if (arguments.isEmpty()) {
				return JavaExpression.from(out -> out.write("new ", typeTranslator.toFunctionJavaClass(externalFunction), "().execute()"), typeTranslator.toJavaReferenceType(returnRType));
			}
			// First evaluate all arguments
			List<RMetaAnnotatedType> argRTypes = arguments.stream().map(typeProvider::getRMetaAnnotatedType).toList();
			if (argRTypes.stream().allMatch(argRType -> typeSystem.isSubtypeOf(argRType, returnRType))) {
				// TODO: this is a hack
				// Generic return type for number type e.g. Min(1,2) or Max(2,6)
				JavaType argAndReturnType = typeTranslator.toJavaReferenceType(typeSystem.joinMetaAnnotatedTypes(argRTypes));
				JavaStatementBuilder argCode = javaCode(arguments.get(0), context.withExpected(argAndReturnType));
				for (int i = 1; i < arguments.size(); i++) {
					argCode = argCode.then(
						javaCode(arguments.get(i), context.withExpected(argAndReturnType)),
						(argList, newArg) -> JavaExpression.from(out -> out.write(argList, ", ", newArg), null),
						scope
					);
				}
				return argCode
					.mapExpressionIfNotNull(it -> JavaExpression.from(out -> out.write("new ", typeTranslator.toFunctionJavaClass(externalFunction), "().execute(", it, ")"), argAndReturnType));
			} else {
				JavaStatementBuilder argCode = javaCode(arguments.get(0), context.withExpected(externalFunctionParameterType(externalFunction, 0)));
				for (int i = 1; i < arguments.size(); i++) {
					argCode = argCode.then(
						javaCode(arguments.get(i), context.withExpected(externalFunctionParameterType(externalFunction, i))),
						(argList, newArg) -> JavaExpression.from(out -> out.write(argList, ", ", newArg), null),
						scope
					);
				}
				return argCode
					.mapExpressionIfNotNull(it -> JavaExpression.from(out -> out.write("new ", typeTranslator.toFunctionJavaClass(externalFunction), "().execute(", it, ")"), typeTranslator.toJavaReferenceType(returnRType)));
			}
		}
		throw new UnsupportedOperationException("Unsupported callable with args of type " +
				(callable == null ? null : callable.eClass().getName()));
	}

	private JavaType externalFunctionParameterType(RosettaExternalFunction function, int index) {
		return typeTranslator.toJavaReferenceType(RMetaAnnotatedType.withNoMeta(typeSystem.typeCallToRType(function.getParameters().get(index).getTypeCall())));
	}

	private JavaStatementBuilder evaluateCall(RFunction rCallable, GeneratedIdentifier dependencyId, List<RosettaExpression> arguments, Context context) {
		JavaType outputType = typeTranslator.toMetaJavaType(rCallable.getOutput());
		List<JavaStatementBuilder> args = new ArrayList<>();
		for (int i = 0; i < arguments.size(); i++) {
			args.add(javaCode(arguments.get(i), context.withExpected(typeTranslator.toMetaJavaType(rCallable.getInputs().get(i)))));
		}
		return JavaStatementBuilder.invokeMethod(
			args,
			it -> JavaExpression.from(out -> out.write(dependencyId, ".evaluate(", it, ")"), outputType),
			context.scope
		);
	}

	private JavaStatementBuilder implicitVariable(EObject context, JavaStatementScope scope) {
		JavaType itemType = typeTranslator.toJavaReferenceType(typeProvider.typeOfImplicitVariable(context));
		EObject definingContainer = implicitVariableUtil.findContainerDefiningImplicitVariable(context).get();
		JavaType actualType;
		if (definingContainer instanceof RosettaTypeWithConditions || definingContainer instanceof RosettaRule) {
			// For conditions and rules
			actualType = itemType;
		} else if (definingContainer instanceof SwitchCaseOrDefault switchCase) {
			// For choice and data switch cases
			if (switchCase.getGuard().getChoiceOptionGuard() != null) {
				actualType = typeUtil.wrap(typeUtil.MAPPER_S, itemType);
			} else {
				actualType = itemType;
			}
		} else {
			// For inline functions
			RosettaFunctionalOperation f = (RosettaFunctionalOperation) definingContainer;
			if (f instanceof ThenOperation && cardinalityProvider.isOutputListOfLists(f.getArgument())) {
				actualType = typeUtil.wrap(typeUtil.MAPPER_LIST_OF_LISTS, itemType);
			} else if (cardinalityProvider.isImplicitVariableMulti(context)) {
				actualType = typeUtil.wrap(typeUtil.MAPPER_C, itemType);
			} else {
				actualType = typeUtil.wrap(typeUtil.MAPPER_S, itemType);
			}
		}
		return new JavaVariable(scope.getIdentifierOrThrow(identifierService.getImplicitVarInContext(context)), actualType);
	}

	public CodeRenderer aliasCallArgs(RShortcut alias, RFunction function, JavaStatementScope scope) {
		RAttribute output = function.getOutput();
		List<RAttribute> inputs = function.getInputs();
		return out -> {
			if (exprHelper.usesOutputParameter(alias.getExpression())) {
				out.write(scope.getIdentifierOrThrow(output), ".toBuilder()");
				if (!inputs.isEmpty()) {
					out.write(", ");
				}
			}
			out.join(inputs, ", ", input -> out.write(scope.getIdentifierOrThrow(input)));
		};
	}

	private JavaStatementBuilder enumCall(RosettaEnumValue feature, JavaType expectedType) {
		JavaType itemType = typeUtil.getItemValueType(expectedType);
		return JavaExpression.from(out -> out.write(itemType, ".", EnumHelper.convertValue(feature)), itemType);
	}

	private JavaStatementBuilder metaCall(JavaStatementBuilder receiverCode, RMetaAnnotatedType receiverType, RosettaMetaType feature, boolean isDeepFeature, JavaStatementScope scope) {
		JavaType resultItemType = typeTranslator.toJavaReferenceType(typeProvider.getRTypeOfFeature(feature, null));
		if (isDeepFeature) {
			if (!"key".equals(feature.getName())) {
				throw new UnsupportedOperationException("Unsupported meta type for deep feature call: " + feature.getName());
			}
			return choiceFeatureCall(receiverCode, receiverType, false,
				t -> resultItemType,
				(t, lambdaParam) -> out -> out.write(JavaLiteral.STRING("metaChooseKey"), ", ", lambdaParam, " -> ", scope.getIdentifierOrThrow(identifierService.toDependencyInstance(typeTranslator.toDeepPathUtilJavaClass(t))), ".metaChooseKey(", lambdaParam, ")"),
				scope);
		} else {
			CodeRenderer right = buildMetaChain(feature.getName(), scope);
			JavaStatementBuilder mapperReceiverCode = typeCoercionService.addCoercions(receiverCode, typeUtil.wrapExtends(typeUtil.MAPPER, typeUtil.getItemType(receiverCode.getExpressionType())), scope);
			return featureCall(mapperReceiverCode, resultItemType, right, cardinalityProvider.isFeatureMulti(feature), scope);
		}
	}

	private JavaStatementBuilder recordCall(JavaStatementBuilder receiverCode, RMetaAnnotatedType receiverType, RosettaRecordFeature feature, JavaStatementScope scope) {
		JavaType resultItemType = typeTranslator.toJavaReferenceType(typeProvider.getRTypeOfFeature(feature, null));
		CodeRenderer right = out -> out.write(".<", resultItemType, ">map(", JavaLiteral.STRING(capitalize(feature.getName())), ", ", recordUtil.recordFeatureToLambda((RRecordType) receiverType.getRType(), feature, scope), ")");
		JavaStatementBuilder mapperReceiverCode = typeCoercionService.addCoercions(receiverCode, typeUtil.wrapExtendsWithoutMeta(typeUtil.MAPPER, typeUtil.getItemType(receiverCode.getExpressionType())), scope);
		return featureCall(mapperReceiverCode, resultItemType, right, cardinalityProvider.isFeatureMulti(feature), scope);
	}

	public JavaStatementBuilder attributeCall(JavaStatementBuilder receiverCode, RMetaAnnotatedType receiverType, RAttribute attr, boolean isDeepFeature, JavaType expectedType, JavaStatementScope scope) {
		if (isDeepFeature) {
			return choiceFeatureCall(receiverCode, receiverType, attr.isMulti(),
				t -> typeTranslator.toMetaItemJavaType(attr),
				(t, lambdaParam) -> out -> out.write(JavaLiteral.STRING("choose" + capitalize(attr.getName())), ", ", lambdaParam, " -> ", scope.getIdentifierOrThrow(identifierService.toDependencyInstance(typeTranslator.toDeepPathUtilJavaClass(t))), ".choose", capitalize(attr.getName()), "(", lambdaParam, ")"),
				scope);
		} else {
			return choiceFeatureCall(receiverCode, receiverType, attr.isMulti(),
				t -> typeUtil.getItemType(typeTranslator.toJavaReferenceType(t).findProperty(attr.getName(), expectedType).getType()),
				(t, lambdaParam) -> {
					JavaPojoProperty prop = typeTranslator.toJavaReferenceType(t).findProperty(attr.getName(), expectedType);
					return out -> out.write(JavaLiteral.STRING("get" + capitalize(prop.getName())), ", ", lambdaParam, " -> ", prop.applyGetter(lambdaParam));
				},
				scope);
		}
	}

	private JavaStatementBuilder choiceFeatureCall(JavaStatementBuilder receiverCode, RMetaAnnotatedType receiverType, boolean isMulti, Function<RDataType, JavaType> getResultItemType, BiFunction<RDataType, JavaVariable, CodeRenderer> getMappingCode, JavaStatementScope scope) {
		RDataType t = toDataType(typeSystem.stripFromTypeAliases(receiverType.getRType()));
		JavaPojoInterface javaType = typeTranslator.toJavaReferenceType(t);
		JavaStatementScope lambdaScope = scope.lambdaScope();
		JavaVariable lambdaParam = new JavaVariable(lambdaScope.createUniqueIdentifier(uncapitalize(javaType.getRosettaName())), javaType);
		JavaType resultItemType = getResultItemType.apply(t);
		CodeRenderer mappingCode = getMappingCode.apply(t, lambdaParam);
		CodeRenderer right = isMulti
				? out -> out.write(".<", resultItemType, ">mapC(", mappingCode, ")")
				: out -> out.write(".<", resultItemType, ">map(", mappingCode, ")");
		JavaStatementBuilder mapperReceiverCode = typeCoercionService.addCoercions(receiverCode, typeUtil.wrapExtendsWithoutMeta(typeUtil.MAPPER, typeUtil.getItemType(receiverCode.getExpressionType())), scope);
		return featureCall(mapperReceiverCode, resultItemType, right, isMulti, scope);
	}

	private RDataType toDataType(RType type) {
		if (type instanceof RChoiceType choiceType) {
			return choiceType.asRDataType();
		}
		return (RDataType) type;
	}

	private JavaStatementBuilder featureCall(JavaStatementBuilder mapperReceiverCode, JavaType resultItemType, CodeRenderer right, boolean isMulti, JavaStatementScope scope) {
		JavaGenericTypeDeclaration<?> resultWrapper = typeUtil.isMapperS(mapperReceiverCode.getExpressionType()) && !isMulti
				? typeUtil.MAPPER_S
				: typeUtil.MAPPER_C;
		JavaType resultType = typeUtil.wrap(resultWrapper, resultItemType);
		return mapperReceiverCode
			.collapseToSingleExpression(scope)
			.mapExpression(it -> JavaExpression.from(out -> out.write(it, right), resultType));
	}

	/**
	 * Writes {@code receiver} followed by a method call chained on the next, indented line.
	 * Continuation lines of a multi-line argument, such as a lambda body, are indented along with it.
	 */
	private static void writeChainedCall(CodeWriter out, Object receiver, Object... call) {
		out.writeln(receiver);
		out.indented(() -> out.write(call));
	}

	private JavaStatementBuilder binaryExpr(RosettaBinaryOperation expr, Context context) {
		RosettaExpression left = expr.getLeft();
		RosettaExpression right = expr.getRight();

		switch (expr.getOperator()) {
			case "and", "or": {
				JavaStatementBuilder leftCode = javaCode(left, context.withExpected(typeUtil.COMPARISON_RESULT));
				JavaStatementBuilder rightCode = javaCode(right, context.withExpected(typeUtil.COMPARISON_RESULT));
				return leftCode
					.then(rightCode, (l, r) -> JavaExpression.from(out -> out.write(l, ".", expr.getOperator(), "NullSafe(", r, ")"), typeUtil.COMPARISON_RESULT), context.scope);
			}
			case "+", "-", "*", "/": {
				RType leftRtype = typeProvider.getRMetaAnnotatedType(expr.getLeft()).getRType();
				RType rightRtype = typeProvider.getRMetaAnnotatedType(expr.getRight()).getRType();
				JavaType leftType = typeTranslator.toJavaReferenceType(leftRtype);
				JavaType rightType = typeTranslator.toJavaReferenceType(rightRtype);
				JavaType joinedWithoutMeta = typeTranslator.toJavaReferenceType(typeSystem.join(leftRtype, rightRtype));
				JavaType resultType = typeTranslator.toJavaReferenceType(typeProvider.getRMetaAnnotatedType(expr));
				String method = switch (expr.getOperator()) {
					case "+" -> "add";
					case "-" -> "subtract";
					case "*" -> "multiply";
					case "/" -> "divide";
					default -> null;
				};
				if (typeUtil.extendsNumber(leftType) && typeUtil.extendsNumber(rightType)) {
					JavaStatementBuilder leftCode = javaCode(left, context.withExpected(typeUtil.wrapExtends(typeUtil.MAPPER, joinedWithoutMeta)));
					JavaStatementBuilder rightCode = javaCode(right, context.withExpected(typeUtil.wrapExtends(typeUtil.MAPPER, joinedWithoutMeta)));
					return leftCode
						.then(rightCode, (l, r) -> JavaExpression.from(out -> out.write(MapperMaths.class, ".<", resultType, ", ", joinedWithoutMeta, ", ", joinedWithoutMeta, ">", method, "(", l, ", ", r, ")"), typeUtil.wrap(typeUtil.MAPPER_S, resultType)), context.scope);
				} else {
					JavaStatementBuilder leftCode = javaCode(left, context.withExpected(typeUtil.wrapExtends(typeUtil.MAPPER, leftType)));
					JavaStatementBuilder rightCode = javaCode(right, context.withExpected(typeUtil.wrapExtends(typeUtil.MAPPER, rightType)));
					return leftCode
						.then(rightCode, (l, r) -> JavaExpression.from(out -> out.write(MapperMaths.class, ".<", resultType, ", ", leftType, ", ", rightType, ">", method, "(", l, ", ", r, ")"), typeUtil.wrap(typeUtil.MAPPER_S, resultType)), context.scope);
				}
			}
			case "contains", "disjoint": {
				RMetaAnnotatedType leftRMetaType = typeProvider.getRMetaAnnotatedType(expr.getLeft());
				RMetaAnnotatedType rightRMetaType = typeProvider.getRMetaAnnotatedType(expr.getRight());
				JavaType joined = typeTranslator.toJavaReferenceType(typeSystem.joinMetaAnnotatedTypes(leftRMetaType, rightRMetaType));

				JavaStatementBuilder leftCode = javaCode(left, context.withExpected(typeUtil.wrapExtends(typeUtil.MAPPER, joined)));
				JavaStatementBuilder rightCode = javaCode(right, context.withExpected(typeUtil.wrapExtends(typeUtil.MAPPER, joined)));
				return leftCode
					.then(rightCode, (l, r) -> JavaExpression.from(out -> out.write(runtimeMethod(expr.getOperator()), "(", l, ", ", r, ")"), typeUtil.COMPARISON_RESULT), context.scope);
			}
			case "default": {
				RMetaAnnotatedType leftRMetaType = typeProvider.getRMetaAnnotatedType(expr.getLeft());
				RMetaAnnotatedType rightRMetaType = typeProvider.getRMetaAnnotatedType(expr.getRight());
				JavaType joined = typeTranslator.toJavaReferenceType(typeSystem.joinMetaAnnotatedTypes(leftRMetaType, rightRMetaType));

				JavaStatementBuilder leftCode = javaCode(left, context.withExpected(typeUtil.wrapExtends(typeUtil.MAPPER, joined)));
				if (cardinalityProvider.isMulti(left)) {
					JavaStatementBuilder rightCode = javaCode(right, context.withExpected(typeUtil.wrapExtends(typeUtil.MAPPER, joined)));

					return leftCode
						.then(rightCode, (l, r) -> new JavaConditionalExpression(JavaExpression.from(out -> out.write(l, ".getMulti().isEmpty()"), JavaPrimitiveType.BOOLEAN), r, l, typeUtil), context.scope);
				} else {
					JavaType resultType = typeTranslator.toJavaReferenceType(typeProvider.getRMetaAnnotatedType(expr));
					JavaStatementBuilder rightCode = javaCode(right, context.withExpected(joined));

					return leftCode
						.then(rightCode, (l, r) -> JavaExpression.from(out -> out.write(l, ".getOrDefault(", r, ")"), resultType), context.scope);
				}
			}
			case "join": {
				JavaStatementBuilder leftCode = javaCode(left, context.withExpected(typeUtil.wrapExtends(typeUtil.MAPPER_C, typeUtil.STRING)));
				JavaStatementBuilder rightCode = expr.getRight() == null
						? JavaExpression.from(out -> out.write(MapperS.class, ".of(\"\")"), typeUtil.wrap(typeUtil.MAPPER_S, typeUtil.STRING))
						: javaCode(right, context.withExpected(typeUtil.wrap(typeUtil.MAPPER_S, typeUtil.STRING)));
				return leftCode
					.then(rightCode, (l, r) -> JavaExpression.from(out -> out.write(l, ".join(", r, ")"), typeUtil.wrap(typeUtil.MAPPER_S, typeUtil.STRING)), context.scope);
			}
			case "=", "<>", "<", "<=", ">", ">=": {
				RType leftRtype = typeProvider.getRMetaAnnotatedType(expr.getLeft()).getRType();
				RType rightRtype = typeProvider.getRMetaAnnotatedType(expr.getRight()).getRType();
				JavaType joinedWithoutMeta = typeTranslator.toJavaReferenceType(typeSystem.join(leftRtype, rightRtype));
				String method = switch (expr.getOperator()) {
					case "=" -> "areEqual";
					case "<>" -> "notEqual";
					case "<" -> "lessThan";
					case "<=" -> "lessThanEquals";
					case ">" -> "greaterThan";
					case ">=" -> "greaterThanEquals";
					default -> null;
				};
				CardinalityModifier modifier = ((ModifiableBinaryOperation) expr).getCardMod();
				CardinalityModifier defaultModifier = "<>".equals(expr.getOperator())
						? CardinalityModifier.ANY
						: CardinalityModifier.ALL;
				JavaStatementBuilder leftCode = javaCode(left, context.withExpected(typeUtil.wrapExtends(typeUtil.MAPPER, joinedWithoutMeta)));
				JavaStatementBuilder rightCode = javaCode(right, context.withExpected(typeUtil.wrapExtends(typeUtil.MAPPER, joinedWithoutMeta)));
				return leftCode
					.then(rightCode, (l, r) -> JavaExpression.from(out -> out.write(runtimeMethod(method), "(", l, ", ", r, ", ", toCardinalityOperator(modifier, defaultModifier), ")"), typeUtil.COMPARISON_RESULT), context.scope);
			}
			default:
				throw new UnsupportedOperationException("Unsupported binary operation of " + expr.getOperator());
		}
	}

	private CodeRenderer toCardinalityOperator(CardinalityModifier cardOp, CardinalityModifier defaultOp) {
		CardinalityModifier op = cardOp == CardinalityModifier.NONE ? defaultOp : cardOp;
		return out -> out.write(CardinalityOperator.class, ".", capitalize(op.toString()));
	}

	public CodeRenderer buildMetaChain(String metaName, JavaStatementScope scope) {
		if ("reference".equals(metaName)) {
			GeneratedIdentifier lambdaParam = scope.lambdaScope().createUniqueIdentifier("a");
			return out -> out.write(".map(", JavaLiteral.STRING("get" + capitalize(metaName)), ", ", lambdaParam, "->", lambdaParam, ".getExternalReference())");
		} else {
			GeneratedIdentifier lambdaParam1 = scope.lambdaScope().createUniqueIdentifier("a");
			GeneratedIdentifier lambdaParam2 = scope.lambdaScope().createUniqueIdentifier("a");
			return out -> out.write(".map(", JavaLiteral.STRING("getMeta"), ", ", lambdaParam1, "->", lambdaParam1, ".getMeta()).map(", JavaLiteral.STRING("get" + capitalize(metaName)), ", ", lambdaParam2, "->", lambdaParam2, ".get", capitalize(PojoPropertyUtil.toPojoPropertyName(metaName)), "())");
		}
	}

	private InlineFunctionCode inlineFunction(InlineFunction ref, JavaType expectedBodyType, Context context) {
		JavaStatementScope lambdaScope = context.scope.lambdaScope();
		List<GeneratedIdentifier> paramIds = ref.getParameters().isEmpty()
				? List.of(lambdaScope.createIdentifier(identifierService.getImplicitVarInContext(ref), implicitVariableUtil.getDefaultImplicitVariable().getName()))
				: ref.getParameters().stream().map(lambdaScope::createIdentifier).toList();

		JavaStatementBuilder body = javaCode(ref.getBody(), context.withExpected(expectedBodyType).withScope(lambdaScope));
		CodeRenderer code = paramIds.size() == 1
				? out -> out.write(paramIds.get(0), " -> ", body.toLambdaBody())
				: out -> {
					out.write("(");
					out.join(paramIds, ", ");
					out.write(") -> ", body.toLambdaBody());
				};
		return new InlineFunctionCode(code, body.getExpressionType());
	}

	private JavaStatementBuilder buildConstraint(RosettaExpression arg, Collection<RAttribute> usedAttributes, Necessity validationType, Context context) {
		JavaType argItemType = typeTranslator.toJavaReferenceType(typeProvider.getRMetaAnnotatedType(arg).getRType());
		return javaCode(arg, context.withExpected(typeUtil.wrapExtendsWithoutMeta(typeUtil.MAPPER, argItemType)))
			.collapseToSingleExpression(context.scope)
			.mapExpression(it -> JavaExpression.from(out -> {
				out.write(runtimeMethod("choice"), "(", it, ", ", Arrays.class, ".asList(");
				out.join(usedAttributes, ", ", attr -> out.write(JavaLiteral.STRING(attr.getName())));
				out.write("), ", ChoiceRuleValidationMethod.class, ".", validationType.name(), ")");
			}, typeUtil.COMPARISON_RESULT));
	}

	private JavaStatementBuilder buildListOperationNoBody(RosettaUnaryOperation op, String name, JavaType expectedArgumentType, UnaryOperator<JavaType> argumentTypeToReturnType, Context context) {
		JavaStatementBuilder argCode = javaCode(op.getArgument(), context.withExpected(expectedArgumentType))
			.collapseToSingleExpression(context.scope);
		return argCode
			.mapExpression(it -> JavaExpression.from(
				out -> writeChainedCall(out, it, ".", name, "()"),
				argumentTypeToReturnType.apply(argCode.getExpressionType())
			));
	}

	private JavaStatementBuilder buildSingleItemListOperationOptionalBody(RosettaFunctionalOperation op, String name, JavaType expectedArgumentType, JavaType expectedBodyType, BinaryOperator<JavaType> argumentAndBodyTypeToReturnType, boolean autoUnwrapMeta, Context context) {
		if (op.getFunction() == null) {
			if (autoUnwrapMeta && typeUtil.getItemType(expectedArgumentType) instanceof RJavaFieldWithMeta expectedItemType) {
				return buildUnwrappingListOperation(op, name, expectedArgumentType, expectedItemType, argumentAndBodyTypeToReturnType, context);
			} else {
				return buildListOperationNoBody(op, name, expectedArgumentType, it -> argumentAndBodyTypeToReturnType.apply(it, null), context);
			}
		} else {
			return buildSingleItemListOperation(op, name, expectedArgumentType, expectedBodyType, argumentAndBodyTypeToReturnType, context);
		}
	}

	private JavaStatementBuilder buildSingleItemListOperation(RosettaFunctionalOperation op, String name, JavaType expectedArgumentType, JavaType expectedBodyType, BinaryOperator<JavaType> argumentAndBodyTypeToReturnType, Context context) {
		JavaStatementBuilder argCode = javaCode(op.getArgument(), context.withExpected(expectedArgumentType))
			.collapseToSingleExpression(context.scope);
		InlineFunctionCode inlineFunction = inlineFunction(op.getFunction(), expectedBodyType, context);
		return argCode
			.mapExpression(it -> JavaExpression.from(
				out -> writeChainedCall(out, it, ".", name, "(", inlineFunction.code(), ")"),
				argumentAndBodyTypeToReturnType.apply(argCode.getExpressionType(), inlineFunction.bodyType())
			));
	}

	private JavaStatementBuilder buildUnwrappingListOperation(RosettaFunctionalOperation op, String name, JavaType expectedArgumentType, RJavaWithMetaValue expectedItemType, BinaryOperator<JavaType> argumentAndBodyTypeToReturnType, Context context) {
		JavaStatementScope scope = context.scope;
		JavaStatementBuilder argCode = javaCode(op.getArgument(), context.withExpected(expectedArgumentType))
			.collapseToSingleExpression(scope);
		JavaVariable lambdaParam = new JavaVariable(scope.createUniqueIdentifier("lambdaParam"), typeUtil.wrap(typeUtil.MAPPER_S, expectedItemType));
		JavaStatementBuilder unwrapCoercion = typeCoercionService.addCoercions(lambdaParam, typeUtil.wrap(typeUtil.MAPPER_S, expectedItemType.getValueType()), scope);

		return argCode
			.mapExpression(it -> JavaExpression.from(
				out -> writeChainedCall(out, it, ".", name, "(", lambdaParam, " -> ", unwrapCoercion.toLambdaBody(), ")"),
				argumentAndBodyTypeToReturnType.apply(argCode.getExpressionType(), expectedItemType.getValueType())
			));
	}

	@Override
	protected JavaStatementBuilder caseAbsentOperation(RosettaAbsentExpression expr, Context context) {
		return applyRuntimeMethod(javaCode(expr.getArgument(), context.withExpected(typeUtil.wrapExtends(typeUtil.MAPPER, expr.getArgument()))),
				"notExists", typeUtil.COMPARISON_RESULT);
	}

	@Override
	protected JavaStatementBuilder caseAddOperation(ArithmeticOperation expr, Context context) {
		return binaryExpr(expr, context);
	}

	@Override
	protected JavaStatementBuilder caseAndOperation(LogicalOperation expr, Context context) {
		return binaryExpr(expr, context);
	}

	@Override
	protected JavaStatementBuilder caseAsKeyOperation(AsKeyOperation expr, Context context) {
		// this operation is currently handled by the `FunctionGenerator`
		return doSwitch(expr.getArgument(), context);
	}

	@Override
	protected JavaStatementBuilder caseBooleanLiteral(RosettaBooleanLiteral expr, Context context) {
		return expr.isValue() ? JavaLiteral.TRUE : JavaLiteral.FALSE;
	}

	@Override
	protected JavaStatementBuilder caseChoiceOperation(ChoiceOperation expr, Context context) {
		List<RAttribute> attributes = expr.getAttributes().stream().map(rObjectFactory::buildRAttribute).toList();
		return buildConstraint(expr.getArgument(), attributes, expr.getNecessity(), context);
	}

	@Override
	protected JavaStatementBuilder caseConditionalExpression(RosettaConditionalExpression expr, Context context) {
		JavaStatementBuilder condition = javaCode(expr.getIf(), context.withExpected(JavaPrimitiveType.BOOLEAN));
		JavaStatementBuilder thenBranch = javaCode(expr.getIfthen(), context);
		JavaStatementBuilder elseBranch = javaCode(expr.getElsethen(), context);

		return condition
			.collapseToSingleExpression(context.scope)
			.mapExpression(it -> new JavaIfThenElseBuilder(it, thenBranch, elseBranch, typeUtil));
	}

	@Override
	protected JavaStatementBuilder caseContainsOperation(RosettaContainsExpression expr, Context context) {
		return binaryExpr(expr, context);
	}

	@Override
	protected JavaStatementBuilder caseDefaultOperation(DefaultOperation expr, Context context) {
		return binaryExpr(expr, context);
	}

	@Override
	protected JavaStatementBuilder caseCountOperation(RosettaCountOperation expr, Context context) {
		return javaCode(expr.getArgument(), context.withExpected(typeUtil.wrapExtends(typeUtil.MAPPER, expr.getArgument())))
			.mapExpression(it -> JavaExpression.from(out -> out.write(it, ".resultCount()"), JavaPrimitiveType.INT));
	}

	@Override
	protected JavaStatementBuilder caseDisjointOperation(RosettaDisjointExpression expr, Context context) {
		return binaryExpr(expr, context);
	}

	@Override
	protected JavaStatementBuilder caseDistinctOperation(DistinctOperation expr, Context context) {
		JavaType argItemType = typeTranslator.toJavaReferenceType(typeProvider.getRMetaAnnotatedType(expr.getArgument()));
		JavaStatementBuilder argCode = javaCode(expr.getArgument(), context.withExpected(typeUtil.wrapExtends(typeUtil.MAPPER, argItemType)));
		JavaType argType = argCode.getExpressionType();
		return applyRuntimeMethod(argCode, "distinctIgnoringPrecision",
				typeUtil.hasWildcardArgument(argType) ? typeUtil.wrapExtends(typeUtil.MAPPER_C, argItemType) : typeUtil.wrap(typeUtil.MAPPER_C, argItemType));
	}

	@Override
	protected JavaStatementBuilder caseDivideOperation(ArithmeticOperation expr, Context context) {
		return binaryExpr(expr, context);
	}

	@Override
	protected JavaStatementBuilder caseEqualsOperation(EqualityOperation expr, Context context) {
		return binaryExpr(expr, context);
	}

	public JavaStatementBuilder exists(JavaStatementBuilder arg, ExistsModifier modifier, JavaStatementScope scope) {
		String methodName;
		if (modifier == ExistsModifier.SINGLE) {
			methodName = "singleExists";
		} else if (modifier == ExistsModifier.MULTIPLE) {
			methodName = "multipleExists";
		} else {
			methodName = "exists";
		}
		return applyRuntimeMethod(
				typeCoercionService.addCoercions(arg, typeUtil.wrapExtends(typeUtil.MAPPER, typeUtil.getItemType(arg.getExpressionType())), scope),
				methodName, typeUtil.COMPARISON_RESULT);
	}

	@Override
	protected JavaStatementBuilder caseExistsOperation(RosettaExistsExpression expr, Context context) {
		return exists(javaCode(expr.getArgument(), context.withExpected(typeUtil.wrapExtends(typeUtil.MAPPER, expr.getArgument()))), expr.getModifier(), context.scope);
	}

	@Override
	protected JavaStatementBuilder caseFeatureCall(RosettaFeatureCall expr, Context context) {
		RosettaFeature feature = expr.getFeature();
		if (feature instanceof RosettaEnumValue enumValue) {
			return enumCall(enumValue, context.expectedType);
		} else if (feature instanceof Attribute attribute) {
			return attributeCall(javaCode(expr.getReceiver(), context.withExpected(typeUtil.wrapExtendsWithoutMeta(typeUtil.MAPPER, expr.getReceiver()))), typeProvider.getRMetaAnnotatedType(expr.getReceiver()), rObjectFactory.buildRAttribute(attribute), false, context.expectedType, context.scope);
		} else if (feature instanceof RosettaMetaType metaType) {
			return metaCall(javaCode(expr.getReceiver(), context.withExpected(typeUtil.wrapExtends(typeUtil.MAPPER, expr.getReceiver()))), typeProvider.getRMetaAnnotatedType(expr.getReceiver()), metaType, false, context.scope);
		} else if (feature instanceof RosettaRecordFeature recordFeature) {
			return recordCall(javaCode(expr.getReceiver(), context.withExpected(typeUtil.wrapExtends(typeUtil.MAPPER, expr.getReceiver()))), typeProvider.getRMetaAnnotatedType(expr.getReceiver()), recordFeature, context.scope);
		} else {
			throw new UnsupportedOperationException("Unsupported feature type of " + (feature == null ? null : feature.getClass().getName()));
		}
	}

	@Override
	protected JavaStatementBuilder caseDeepFeatureCall(RosettaDeepFeatureCall expr, Context context) {
		RosettaFeature feature = expr.getFeature();
		if (feature instanceof RosettaMetaType metaType) {
			return metaCall(javaCode(expr.getReceiver(), context.withExpected(typeUtil.wrapExtendsWithoutMeta(typeUtil.MAPPER, expr.getReceiver()))), typeProvider.getRMetaAnnotatedType(expr.getReceiver()), metaType, true, context.scope);
		}
		return attributeCall(javaCode(expr.getReceiver(), context.withExpected(typeUtil.wrapExtendsWithoutMeta(typeUtil.MAPPER, expr.getReceiver()))), typeProvider.getRMetaAnnotatedType(expr.getReceiver()), rObjectFactory.buildRAttribute((Attribute) feature), true, context.expectedType, context.scope);
	}

	@Override
	protected JavaStatementBuilder caseFilterOperation(FilterOperation expr, Context context) {
		CodeRenderer inlineFunctionCode = inlineFunction(expr.getFunction(), JavaPrimitiveType.BOOLEAN.toReferenceType(), context).code();
		if (!cardinalityProvider.isPreviousOperationMulti(expr)) {
			// Case MapperS
			JavaStatementBuilder argCode = javaCode(expr.getArgument(), context.withExpected(typeUtil.wrapExtends(typeUtil.MAPPER_S, expr.getArgument())))
				.collapseToSingleExpression(context.scope);
			return argCode
				.mapExpression(it -> JavaExpression.from(
					out -> writeChainedCall(out, it, ".filterSingleNullSafe(", inlineFunctionCode, ")"),
					argCode.getExpressionType()
				));
		} else if (cardinalityProvider.isOutputListOfLists(expr.getArgument())) {
			// Case MapperListOfLists
			JavaStatementBuilder argCode = javaCode(expr.getArgument(), context.withExpected(typeUtil.wrapExtends(typeUtil.MAPPER_LIST_OF_LISTS, expr.getArgument())))
				.collapseToSingleExpression(context.scope);
			return argCode
				.mapExpression(it -> JavaExpression.from(
					out -> writeChainedCall(out, it, ".filterListNullSafe(", inlineFunctionCode, ")"),
					argCode.getExpressionType()
				));
		} else {
			// Case MapperC
			JavaStatementBuilder argCode = javaCode(expr.getArgument(), context.withExpected(typeUtil.wrapExtends(typeUtil.MAPPER_C, expr.getArgument())))
				.collapseToSingleExpression(context.scope);
			return argCode
				.mapExpression(it -> JavaExpression.from(
					out -> writeChainedCall(out, it, ".filterItemNullSafe(", inlineFunctionCode, ")"),
					argCode.getExpressionType()
				));
		}
	}

	@Override
	protected JavaStatementBuilder caseFirstOperation(FirstOperation expr, Context context) {
		return buildListOperationNoBody(expr, "first", typeUtil.wrapExtends(typeUtil.MAPPER_C, expr.getArgument()), it -> typeUtil.wrap(typeUtil.MAPPER_S, typeUtil.getItemType(it)), context);
	}

	@Override
	protected JavaStatementBuilder caseFlattenOperation(FlattenOperation expr, Context context) {
		return buildListOperationNoBody(expr, "flattenList", typeUtil.wrapExtends(typeUtil.MAPPER_LIST_OF_LISTS, expr.getArgument()),
				it -> typeUtil.hasWildcardArgument(it) ? typeUtil.wrapExtends(typeUtil.MAPPER_C, expr.getArgument()) : typeUtil.wrap(typeUtil.MAPPER_C, expr.getArgument()),
				context);
	}

	@Override
	protected JavaStatementBuilder caseGreaterThanOperation(ComparisonOperation expr, Context context) {
		return binaryExpr(expr, context);
	}

	@Override
	protected JavaStatementBuilder caseGreaterThanOrEqualOperation(ComparisonOperation expr, Context context) {
		return binaryExpr(expr, context);
	}

	@Override
	protected JavaStatementBuilder caseImplicitVariable(RosettaImplicitVariable expr, Context context) {
		return implicitVariable(expr, context.scope);
	}

	@Override
	protected JavaStatementBuilder caseIntLiteral(RosettaIntLiteral expr, Context context) {
		BigInteger value = expr.getValue();
		int intValue = value.intValue();
		if (BigInteger.valueOf(intValue).equals(value)) {
			return JavaLiteral.INT(intValue);
		}
		long longValue = value.longValue();
		if (BigInteger.valueOf(longValue).equals(value)) {
			return JavaLiteral.LONG(longValue);
		}
		return JavaExpression.from(out -> out.write("new ", BigInteger.class, "(", JavaLiteral.STRING(value.toString()), ")"), typeUtil.BIG_INTEGER);
	}

	@Override
	protected JavaStatementBuilder caseJoinOperation(JoinOperation expr, Context context) {
		return binaryExpr(expr, context);
	}

	@Override
	protected JavaStatementBuilder caseLastOperation(LastOperation expr, Context context) {
		return buildListOperationNoBody(expr, "last", typeUtil.wrapExtends(typeUtil.MAPPER_C, expr.getArgument()), it -> typeUtil.wrap(typeUtil.MAPPER_S, typeUtil.getItemType(it)), context);
	}

	@Override
	protected JavaStatementBuilder caseLessThanOperation(ComparisonOperation expr, Context context) {
		return binaryExpr(expr, context);
	}

	@Override
	protected JavaStatementBuilder caseLessThanOrEqualOperation(ComparisonOperation expr, Context context) {
		return binaryExpr(expr, context);
	}

	@Override
	protected JavaStatementBuilder caseListLiteral(ListLiteral expr, Context context) {
		if (expr.getElements().isEmpty()) {
			return JavaLiteral.NULL;
		}
		JavaType itemType = typeTranslator.toJavaReferenceType(typeProvider.getRMetaAnnotatedType(expr));
		List<JavaStatementBuilder> elements = new ArrayList<>();
		for (RosettaExpression elem : expr.getElements()) {
			JavaType expectedElementType = cardinalityProvider.isMulti(elem)
					? typeUtil.wrapExtends(typeUtil.MAPPER_C, itemType)
					: typeUtil.wrapExtends(typeUtil.MAPPER_S, itemType);
			elements.add(javaCode(elem, context.withExpected(expectedElementType)));
		}
		return JavaStatementBuilder.invokeMethod(
			elements,
			it -> JavaExpression.from(out -> out.write(MapperC.class, ".<", itemType, ">of(", it, ")"), typeUtil.wrap(typeUtil.MAPPER_C, itemType)),
			context.scope
		);
	}

	@Override
	protected JavaStatementBuilder caseMapOperation(MapOperation expr, Context context) {
		JavaType bodyItemType = typeTranslator.toJavaReferenceType(typeProvider.getRMetaAnnotatedType(expr.getFunction().getBody()));
		boolean isBodyMulti = cardinalityProvider.isBodyExpressionMulti(expr.getFunction());

		if (!cardinalityProvider.isPreviousOperationMulti(expr)) {
			if (isBodyMulti) {
				// Case MapperS to MapperC
				InlineFunctionCode inlineFunction = inlineFunction(expr.getFunction(), typeUtil.wrapExtends(typeUtil.MAPPER_C, bodyItemType), context);
				return javaCode(expr.getArgument(), context.withExpected(typeUtil.wrapExtends(typeUtil.MAPPER_S, expr.getArgument())))
					.collapseToSingleExpression(context.scope)
					.mapExpression(it -> JavaExpression.from(
						out -> writeChainedCall(out, it, ".mapSingleToList(", inlineFunction.code(), ")"),
						inlineFunction.bodyType()
					));
			} else {
				// Case MapperS to MapperS
				return buildSingleItemListOperation(expr, "mapSingleToItem", typeUtil.wrapExtends(typeUtil.MAPPER_S, expr.getArgument()), typeUtil.wrapExtends(typeUtil.MAPPER_S, bodyItemType), (a, b) -> b, context);
			}
		} else if (cardinalityProvider.isOutputListOfLists(expr.getArgument())) {
			if (isBodyMulti) {
				// Case MapperListOfLists to MapperListOfLists
				InlineFunctionCode inlineFunction = inlineFunction(expr.getFunction(), typeUtil.wrapExtends(typeUtil.MAPPER_C, bodyItemType), context);
				return javaCode(expr.getArgument(), context.withExpected(typeUtil.wrapExtends(typeUtil.MAPPER_LIST_OF_LISTS, expr.getArgument())))
					.collapseToSingleExpression(context.scope)
					.mapExpression(it -> JavaExpression.from(
						out -> writeChainedCall(out, it, ".mapListToList(", inlineFunction.code(), ")"),
						typeUtil.hasWildcardArgument(inlineFunction.bodyType()) ? typeUtil.wrapExtends(typeUtil.MAPPER_LIST_OF_LISTS, bodyItemType) : typeUtil.wrap(typeUtil.MAPPER_LIST_OF_LISTS, bodyItemType)
					));
			} else {
				// Case MapperListOfLists to MapperC
				InlineFunctionCode inlineFunction = inlineFunction(expr.getFunction(), typeUtil.wrapExtends(typeUtil.MAPPER_S, bodyItemType), context);
				return javaCode(expr.getArgument(), context.withExpected(typeUtil.wrapExtends(typeUtil.MAPPER_LIST_OF_LISTS, expr.getArgument())))
					.collapseToSingleExpression(context.scope)
					.mapExpression(it -> JavaExpression.from(
						out -> writeChainedCall(out, it, ".mapListToItem(", inlineFunction.code(), ")"),
						typeUtil.hasWildcardArgument(inlineFunction.bodyType()) ? typeUtil.wrapExtends(typeUtil.MAPPER_C, bodyItemType) : typeUtil.wrap(typeUtil.MAPPER_C, bodyItemType)
					));
			}
		} else {
			if (isBodyMulti) {
				// MapperC to MapperListOfLists
				InlineFunctionCode inlineFunction = inlineFunction(expr.getFunction(), typeUtil.wrapExtends(typeUtil.MAPPER_C, bodyItemType), context);
				return javaCode(expr.getArgument(), context.withExpected(typeUtil.wrapExtends(typeUtil.MAPPER_C, expr.getArgument())))
					.collapseToSingleExpression(context.scope)
					.mapExpression(it -> JavaExpression.from(
						out -> writeChainedCall(out, it, ".mapItemToList(", inlineFunction.code(), ")"),
						typeUtil.hasWildcardArgument(inlineFunction.bodyType()) ? typeUtil.wrapExtends(typeUtil.MAPPER_LIST_OF_LISTS, bodyItemType) : typeUtil.wrap(typeUtil.MAPPER_LIST_OF_LISTS, bodyItemType)
					));
			} else {
				// MapperC to MapperC
				return buildSingleItemListOperation(expr, "mapItem", typeUtil.wrapExtends(typeUtil.MAPPER_C, expr.getArgument()), typeUtil.wrapExtends(typeUtil.MAPPER_S, bodyItemType),
						(a, b) -> typeUtil.hasWildcardArgument(b) ? typeUtil.wrapExtends(typeUtil.MAPPER_C, bodyItemType) : typeUtil.wrap(typeUtil.MAPPER_C, bodyItemType),
						context);
			}
		}
	}

	@Override
	protected JavaStatementBuilder caseMaxOperation(MaxOperation expr, Context context) {
		JavaType bodyType = expr.getFunction() != null ? typeUtil.wrapExtendsWithoutMeta(typeUtil.MAPPER_S, expr.getFunction().getBody()) : null;
		return buildSingleItemListOperationOptionalBody(expr, "max", typeUtil.wrapExtendsWithoutMeta(typeUtil.MAPPER_C, expr.getArgument()), bodyType, (a, b) -> typeUtil.wrap(typeUtil.MAPPER_S, typeUtil.getItemType(a)), true, context);
	}

	@Override
	protected JavaStatementBuilder caseMinOperation(MinOperation expr, Context context) {
		JavaType bodyType = expr.getFunction() != null ? typeUtil.wrapExtendsWithoutMeta(typeUtil.MAPPER_S, expr.getFunction().getBody()) : null;
		return buildSingleItemListOperationOptionalBody(expr, "min", typeUtil.wrapExtendsWithoutMeta(typeUtil.MAPPER_C, expr.getArgument()), bodyType, (a, b) -> typeUtil.wrap(typeUtil.MAPPER_S, typeUtil.getItemType(a)), true, context);
	}

	@Override
	protected JavaStatementBuilder caseMultiplyOperation(ArithmeticOperation expr, Context context) {
		return binaryExpr(expr, context);
	}

	@Override
	protected JavaStatementBuilder caseNotEqualsOperation(EqualityOperation expr, Context context) {
		return binaryExpr(expr, context);
	}

	@Override
	protected JavaStatementBuilder caseNumberLiteral(RosettaNumberLiteral expr, Context context) {
		return JavaExpression.from(out -> out.write("new ", BigDecimal.class, "(", JavaLiteral.STRING(expr.getValue().toString()), ")"), typeUtil.BIG_DECIMAL);
	}

	@Override
	protected JavaStatementBuilder caseOneOfOperation(OneOfOperation expr, Context context) {
		RDataType t = toDataType(typeProvider.getRMetaAnnotatedType(expr.getArgument()).getRType());
		return buildConstraint(expr.getArgument(), t.getAllAttributes(), Necessity.REQUIRED, context);
	}

	@Override
	protected JavaStatementBuilder caseOnlyElementOperation(RosettaOnlyElement expr, Context context) {
		JavaType itemType = typeTranslator.toJavaReferenceType(typeProvider.getRMetaAnnotatedType(expr.getArgument()));
		return javaCode(expr.getArgument(), context.withExpected(itemType));
	}

	@Override
	protected JavaStatementBuilder caseOnlyExists(RosettaOnlyExistsExpression expr, Context context) {
		RosettaExpression first = expr.getArgs().isEmpty() ? null : expr.getArgs().get(0);
		RDataType parentType;
		JavaStatementBuilder parent;
		if (first instanceof RosettaFeatureCall firstFeatureCall) {
			parentType = toDataType(typeProvider.getRMetaAnnotatedType(firstFeatureCall.getReceiver()).getRType());
			parent = javaCode(firstFeatureCall.getReceiver(), context.withExpected(typeUtil.wrapExtends(typeUtil.MAPPER, typeTranslator.toJavaReferenceType(parentType))));
		} else {
			parentType = toDataType(typeProvider.typeOfImplicitVariable(expr).getRType());
			parent = typeCoercionService.addCoercions(implicitVariable(expr, context.scope), typeUtil.wrapExtends(typeUtil.MAPPER, typeTranslator.toJavaReferenceType(parentType)), context.scope);
		}
		List<RosettaNamed> requiredAttributes = expr.getArgs().stream().<RosettaNamed>map(arg -> {
			if (arg instanceof RosettaFeatureCall featureCall) {
				return featureCall.getFeature();
			} else if (arg instanceof RosettaSymbolReference symbolReference) {
				return symbolReference.getSymbol();
			}
			throw new UnsupportedOperationException("Unsupported parent in `only exists` expression of type " + (arg == null ? null : arg.getClass().getName()));
		}).toList();
		Collection<RAttribute> allAttrs = parentType.getAllAttributes();
		return parent
			.collapseToSingleExpression(context.scope)
			.mapExpression(it -> JavaExpression.from(out -> {
				out.write(runtimeMethod("onlyExists"), "(", it, ", ", Arrays.class, ".asList(");
				out.join(allAttrs, ", ", attr -> out.write(JavaLiteral.STRING(attr.getName())));
				out.write("), ", Arrays.class, ".asList(");
				out.join(requiredAttributes, ", ", attr -> out.write(JavaLiteral.STRING(attr.getName())));
				out.write("))");
			}, typeUtil.COMPARISON_RESULT));
	}

	@Override
	protected JavaStatementBuilder caseOrOperation(LogicalOperation expr, Context context) {
		return binaryExpr(expr, context);
	}

	@Override
	protected JavaStatementBuilder caseReduceOperation(ReduceOperation expr, Context context) {
		JavaType outputType = typeTranslator.toJavaReferenceType(typeProvider.getRMetaAnnotatedType(expr.getFunction().getBody()));
		InlineFunctionCode inlineFunction = inlineFunction(expr.getFunction(), typeUtil.wrapExtends(typeUtil.MAPPER_S, outputType), context);
		return javaCode(expr.getArgument(), context.withExpected(typeUtil.wrapExtends(typeUtil.MAPPER_C, expr.getArgument())))
			.collapseToSingleExpression(context.scope)
			.mapExpression(it -> JavaExpression.from(
				out -> writeChainedCall(out, it, ".<", outputType, ">reduce(", inlineFunction.code(), ")"),
				inlineFunction.bodyType()
			));
	}

	@Override
	protected JavaStatementBuilder caseReverseOperation(ReverseOperation expr, Context context) {
		return buildListOperationNoBody(expr, "reverse", typeUtil.wrapExtends(typeUtil.MAPPER_C, expr.getArgument()), it -> it, context);
	}

	@Override
	protected JavaStatementBuilder caseSortOperation(SortOperation expr, Context context) {
		JavaType bodyType = expr.getFunction() != null ? typeUtil.wrapExtendsWithoutMeta(typeUtil.MAPPER_S, expr.getFunction().getBody()) : null;
		return buildSingleItemListOperationOptionalBody(expr, "sort", typeUtil.wrapExtends(typeUtil.MAPPER_C, expr.getArgument()), bodyType, (a, b) -> a, true, context);
	}

	@Override
	protected JavaStatementBuilder caseStringLiteral(RosettaStringLiteral expr, Context context) {
		return JavaLiteral.STRING(expr.getValue());
	}

	@Override
	protected JavaStatementBuilder caseSubtractOperation(ArithmeticOperation expr, Context context) {
		return binaryExpr(expr, context);
	}

	@Override
	protected JavaStatementBuilder caseSumOperation(SumOperation expr, Context context) {
		JavaType itemType = typeTranslator.toJavaReferenceType(typeProvider.getRMetaAnnotatedType(expr.getArgument()).getRType());
		return buildListOperationNoBody(expr, "sum" + itemType.getSimpleName(), typeUtil.wrapExtendsWithoutMeta(typeUtil.MAPPER_C, itemType), it -> typeUtil.wrap(typeUtil.MAPPER_S, itemType), context);
	}

	@Override
	protected JavaStatementBuilder caseSymbolReference(RosettaSymbolReference expr, Context context) {
		RosettaSymbol s = expr.getSymbol();
		if (s instanceof Attribute attr) {
			RAttribute attribute = rObjectFactory.buildRAttribute(attr);
			// Data attributes can only be called if there is an implicit variable present.
			RMetaAnnotatedType implicitType = typeProvider.typeOfImplicitVariable(expr);
			Iterable<? extends RosettaFeature> implicitFeatures = ecoreUtil.allFeaturesExcludingEnumValues(implicitType.getRType(), expr);
			if (Iterables.contains(implicitFeatures, attr)) {
				return attributeCall(implicitVariable(expr, context.scope), implicitType, rObjectFactory.buildRAttribute(attr), false, context.expectedType, context.scope);
			} else {
				return new JavaVariable(context.scope.getIdentifierOrThrow(attribute), typeTranslator.toMetaJavaType(attribute));
			}
		} else if (s instanceof ShortcutDeclaration shortcutDeclaration) {
			boolean isMulti = cardinalityProvider.isSymbolMulti(shortcutDeclaration);
			RShortcut shortcut = rObjectFactory.buildRShortcut(shortcutDeclaration);
			JavaType itemType = typeTranslator.toJavaReferenceType(typeProvider.getRTypeOfSymbol(shortcutDeclaration));
			CodeRenderer arguments = CodeWriterTargetStringConcatenation.asCodeRenderer(aliasUtil.getArguments(shortcut, context.scope));
			if (aliasUtil.requiresOutput(shortcut)) {
				JavaType aliasType = isMulti ? typeUtil.wrap(typeUtil.LIST, itemType) : itemType;
				return JavaExpression.from(out -> out.write(context.scope.getIdentifierOrThrow(shortcut), "(", arguments, ").build()"), aliasType);
			} else {
				JavaType aliasType = isMulti ? typeUtil.wrapExtendsIfNotFinal(typeUtil.MAPPER_C, itemType) : typeUtil.wrapExtendsIfNotFinal(typeUtil.MAPPER_S, itemType);
				return JavaExpression.from(out -> out.write(context.scope.getIdentifierOrThrow(shortcut), "(", arguments, ")"), aliasType);
			}
		} else if (s instanceof RosettaEnumValue enumValue) {
			return enumCall(enumValue, context.expectedType);
		} else if (s instanceof ClosureParameter closureParameter) {
			return new JavaVariable(context.scope.getIdentifierOrThrow(closureParameter), cardinalityProvider.isMulti(expr) ? typeUtil.wrap(typeUtil.MAPPER_C, expr) : typeUtil.wrap(typeUtil.MAPPER_S, expr));
		} else if (s instanceof RosettaCallableWithArgs callable) {
			return callableWithArgsCall(callable, expr.getArgs(), context);
		} else if (s instanceof RosettaMetaType metaType) {
			RMetaAnnotatedType implicitType = typeProvider.typeOfImplicitVariable(expr);
			return metaCall(implicitVariable(expr, context.scope), implicitType, metaType, false, context.scope);
		} else if (s instanceof TypeParameter typeParameter) {
			RMetaAnnotatedType type = typeProvider.getRTypeOfSymbol(typeParameter);
			return new JavaVariable(context.scope.getIdentifierOrThrow(typeParameter), typeTranslator.toJavaReferenceType(type));
		}
		throw new UnsupportedOperationException("Unsupported symbol type of " + (s == null ? null : s.getClass().getName()));
	}

	@Override
	protected JavaStatementBuilder caseThenOperation(ThenOperation expr, Context context) {
		JavaStatementBuilder thenArgCode = javaCode(expr.getArgument(), context.withExpected(cardinalityProvider.isMulti(expr.getArgument())
				? typeUtil.wrapExtends(typeUtil.MAPPER_C, expr.getArgument())
				: typeUtil.wrapExtends(typeUtil.MAPPER_S, expr.getArgument())));
		JavaStatementBuilder thenAsVarCode = thenArgCode.declareAsVariable(true, "thenArg", context.scope);
		if (expr.getFunction().getParameters().isEmpty()) {
			context.scope.createKeySynonym(identifierService.getImplicitVarInContext(expr.getFunction()), thenArgCode);
		} else {
			context.scope.createKeySynonym(expr.getFunction().getParameters().get(0), thenArgCode);
		}
		return thenAsVarCode
			.then(
				javaCode(expr.getFunction().getBody(), context.withExpected(cardinalityProvider.isMulti(expr)
						? typeUtil.wrapExtends(typeUtil.MAPPER_C, expr)
						: typeUtil.wrapExtends(typeUtil.MAPPER_S, expr))),
				(a, b) -> b,
				context.scope
			);
	}

	private JavaStatementBuilder conversionOperation(RosettaUnaryOperation expr, Context context, CodeRenderer conversion, Class<? extends Exception> errorClass) {
		JavaType argumentJavaType = typeTranslator.toJavaReferenceType(typeProvider.getRMetaAnnotatedType(expr.getArgument()).getRType());
		return javaCode(expr.getArgument(), context.withExpected(typeUtil.wrapExtends(typeUtil.MAPPER_S, argumentJavaType)))
			.collapseToSingleExpression(context.scope)
			.mapExpression(it -> JavaExpression.from(out -> out.write(it, ".checkedMap(", JavaLiteral.STRING(expr.getOperator()), ", ", conversion, ", ", errorClass, ".class)"), typeUtil.wrap(typeUtil.MAPPER_S, expr)));
	}

	@Override
	protected JavaStatementBuilder caseToEnumOperation(ToEnumOperation expr, Context context) {
		JavaType javaEnum = typeTranslator.toJavaType(rObjectFactory.buildREnumType(expr.getEnumeration()));
		boolean argIsEnum = typeSystem.stripFromTypeAliases(typeProvider.getRMetaAnnotatedType(expr.getArgument()).getRType()) instanceof REnumType;
		CodeRenderer conversion = argIsEnum
				? out -> out.write("e -> ", javaEnum, ".valueOf(e.name())")
				: out -> out.write(javaEnum, "::fromDisplayName");
		return conversionOperation(expr, context, conversion, IllegalArgumentException.class);
	}

	@Override
	protected JavaStatementBuilder caseAsOperation(AsOperation expr, Context context) {
		boolean isMulti = cardinalityProvider.isMulti(expr);
		RType argRType = typeSystem.stripFromTypeAliases(typeProvider.getRMetaAnnotatedType(expr.getArgument()).getRType());

		if (argRType instanceof RChoiceType choiceType) {
			// Narrowing a choice to one of its options means navigating the path of option attributes
			// leading to that option - the same navigation a `switch` case performs.
			RChoiceOption choiceOption = new RChoiceOption((ChoiceOption) expr.getType(), choiceType, typeProvider);
			JavaStatementBuilder argCode = javaCode(expr.getArgument(), context.withExpected(isMulti
					? typeUtil.wrapExtends(typeUtil.MAPPER_C, typeTranslator.toJavaReferenceType(choiceType))
					: typeUtil.wrap(typeUtil.MAPPER, typeTranslator.toJavaReferenceType(choiceType))));
			return navigateToChoiceOption(argCode, choiceType, choiceOption, context);
		} else {
			// A data type is narrowed by filtering on the runtime type and casting.
			JavaType targetJavaType = typeTranslator.toJavaReferenceType(typeSystem.stripFromTypeAliases(typeProvider.getRMetaAnnotatedType(expr).getRType()));
			JavaStatementBuilder argCode = javaCode(expr.getArgument(), context.withExpected(isMulti
					? typeUtil.wrapExtends(typeUtil.MAPPER_C, typeTranslator.toJavaReferenceType(argRType))
					: typeUtil.wrapExtends(typeUtil.MAPPER_S, typeTranslator.toJavaReferenceType(argRType))));
			return narrowToSubtype(argCode, targetJavaType, isMulti, context);
		}
	}

	/**
	 * Navigate from a choice-typed mapper to the value of one of its (nested) options, by following the
	 * path of option attributes. Shared by the `as` and `switch` operators.
	 */
	private JavaStatementBuilder navigateToChoiceOption(JavaStatementBuilder choiceArg, RChoiceType choiceType, RChoiceOption goal, Context context) {
		// Do NOT strip the alias: findChoiceOptionPath uses alias identity for the leaf match so
		// that two aliases of the same base type are not confused.
		RType goalRType = goal.getType().getRType();
		// The path ends on the option whose type is exactly `goalRType`, so the navigated value is already of
		// the requested type - no downcast is needed.
		JavaStatementBuilder result = choiceArg;
		for (RChoiceOption option : typeSystem.findChoiceOptionPath(choiceType, goalRType)) {
			result = attributeCall(result, RMetaAnnotatedType.withNoMeta(option.getChoiceType()), rObjectFactory.buildRAttribute(option.getEObject()), false, context.expectedType, context.scope);
		}
		return result;
	}

	private JavaStatementBuilder narrowToSubtype(JavaStatementBuilder mapperCode, JavaType targetType, boolean isMulti, Context context) {
		JavaStatementScope filterScope = context.scope.lambdaScope();
		GeneratedIdentifier filterParam = filterScope.createUniqueIdentifier("a");
		JavaStatementScope castScope = context.scope.lambdaScope();
		GeneratedIdentifier castParam = castScope.createUniqueIdentifier(uncapitalize(targetType.getSimpleName()));
		JavaStatementBuilder collapsed = mapperCode.collapseToSingleExpression(context.scope);
		String filterMethod = isMulti ? "filterItemNullSafe" : "filterSingleNullSafe";
		JavaType resultType = isMulti ? typeUtil.wrap(typeUtil.MAPPER_C, targetType) : typeUtil.wrap(typeUtil.MAPPER_S, targetType);
		return collapsed.mapExpression(it -> JavaExpression.from(
			out -> {
				out.writeln(it);
				out.indented(() -> {
					out.writeln(".", filterMethod, "(", filterParam, " -> ", filterParam, ".get() instanceof ", targetType, ")");
					out.write(".map(", JavaLiteral.STRING("as " + targetType.getSimpleName()), ", ", castParam, " -> (", targetType, ") ", castParam, ")");
				});
			},
			resultType));
	}

	@Override
	protected JavaStatementBuilder caseToIntOperation(ToIntOperation expr, Context context) {
		return conversionOperation(expr, context, out -> out.write(Integer.class, "::parseInt"), NumberFormatException.class);
	}

	@Override
	protected JavaStatementBuilder caseToNumberOperation(ToNumberOperation expr, Context context) {
		return conversionOperation(expr, context, out -> out.write(BigDecimal.class, "::new"), NumberFormatException.class);
	}

	@Override
	protected JavaStatementBuilder caseToStringOperation(ToStringOperation expr, Context context) {
		RType rType = typeProvider.getRMetaAnnotatedType(expr.getArgument()).getRType();
		CodeRenderer toStringMethod = typeSystem.stripFromTypeAliases(rType) instanceof REnumType
				? out -> out.write(typeTranslator.toJavaReferenceType(rType), "::toDisplayString")
				: out -> out.write(Object.class, "::toString");
		return javaCode(expr.getArgument(), context.withExpected(typeUtil.wrapExtendsWithoutMeta(typeUtil.MAPPER_S, expr.getArgument())))
			.collapseToSingleExpression(context.scope)
			.mapExpression(it -> JavaExpression.from(out -> out.write(it, ".map(", JavaLiteral.STRING(expr.getOperator()), ", ", toStringMethod, ")"), typeUtil.wrap(typeUtil.MAPPER_S, expr)));
	}

	@Override
	protected JavaStatementBuilder caseToTimeOperation(ToTimeOperation expr, Context context) {
		GeneratedIdentifier lambdaParam = context.scope.lambdaScope().createUniqueIdentifier("s");
		return conversionOperation(expr, context,
				out -> out.write(lambdaParam, " -> ", LocalTime.class, ".parse(", lambdaParam, ", ", DateTimeFormatter.class, ".ISO_LOCAL_TIME)"),
				DateTimeParseException.class);
	}

	@Override
	protected JavaStatementBuilder caseConstructorExpression(RosettaConstructorExpression expr, Context context) {
		RMetaAnnotatedType metaAnnotatedType = typeProvider.getRMetaAnnotatedType(expr);
		JavaType clazz = typeTranslator.toJavaReferenceType(metaAnnotatedType);
		if (clazz instanceof JavaPojoInterface pojo) {
			if (expr.getValues().isEmpty()) {
				return JavaExpression.from(out -> {
					out.writeln(pojo, ".builder()");
					out.indented(() -> out.write(".build()"));
				}, pojo);
			}
			// Each setter is evaluated right before it is combined with the previous ones.
			Iterator<ConstructorKeyValuePair> pairs = expr.getValues().iterator();
			JavaStatementBuilder allSetCode = constructorSetter(pojo, pairs.next(), context);
			while (pairs.hasNext()) {
				JavaStatementBuilder attrCode = constructorSetter(pojo, pairs.next(), context);
				allSetCode = allSetCode.then(attrCode, (previousSetters, setAttr) -> JavaExpression.from(out -> {
					out.writeln(previousSetters);
					out.write(setAttr);
				}, null), context.scope);
			}
			return allSetCode.mapExpression(it -> JavaExpression.from(out -> {
				out.writeln(pojo, ".builder()");
				out.indented(() -> {
					out.writeln(it);
					out.write(".build()");
				});
			}, pojo));
		} else { // type instanceof RRecordType
			Map<String, JavaStatementBuilder> featureMap = new LinkedHashMap<>();
			for (ConstructorKeyValuePair pair : expr.getValues()) {
				featureMap.put(pair.getKey().getName(), evaluateConstructorValue(pair.getKey(), pair.getValue(), false, false, context));
			}
			return recordUtil.recordConstructor((RRecordType) metaAnnotatedType.getRType(), featureMap, context.scope);
		}
	}

	private JavaStatementBuilder constructorSetter(JavaPojoInterface pojo, ConstructorKeyValuePair pair, Context context) {
		Attribute attr = (Attribute) pair.getKey();
		RosettaExpression attrExpr = pair.getValue();

		JavaPojoProperty prop = pojo.findProperty(attr.getName());
		boolean assignAsKey = attrExpr instanceof AsKeyOperation;
		boolean requiresValueAssignment = requiresValueAssignment(assignAsKey, attr, attrExpr);

		String setterName = prop.getOperationName(requiresValueAssignment ? SET_VALUE : SET);

		return evaluateConstructorValue(attr, attrExpr, cardinalityProvider.isFeatureMulti(attr), assignAsKey, context)
			.collapseToSingleExpression(context.scope)
			.mapExpression(it -> JavaExpression.from(out -> out.write(".", setterName, "(", it, ")"), null));
	}

	private boolean requiresValueAssignment(boolean assignAsKey, Attribute attr, RosettaExpression attrExpr) {
		if (assignAsKey) {
			return false;
		}
		boolean attrHasMeta = rObjectFactory.buildRAttribute(attr).getRMetaAnnotatedType().hasAttributeMeta();
		boolean attrExprHasMeta = typeProvider.getRMetaAnnotatedType(attrExpr).hasAttributeMeta();
		return attrHasMeta && !attrExprHasMeta;
	}

	private JavaStatementBuilder evaluateConstructorValue(RosettaFeature feature, RosettaExpression value, boolean isMulti, boolean assignAsKey, Context context) {
		if (assignAsKey) {
			JavaStatementScope scope = context.scope;
			JavaType metaClass = typeUtil.getItemType(typeTranslator.toMetaJavaType(rObjectFactory.buildRAttribute((Attribute) feature)));
			if (isMulti) {
				JavaStatementScope lambdaScope = scope.lambdaScope();
				GeneratedIdentifier item = lambdaScope.createUniqueIdentifier("item");
				return javaCode(value, context.withExpected(typeUtil.wrapExtendsWithoutMeta(typeUtil.MAPPER_C, value)))
					.collapseToSingleExpression(scope)
					.mapExpression(it -> JavaExpression.from(
						out -> {
							out.writeln(it);
							out.indented(() -> {
								out.writeln(".getItems()");
								out.writeln(".map(", item, " -> ", metaClass, ".builder()");
								out.indented(() -> {
									out.writeln(".setExternalReference(", item, ".getMappedObject().getMeta().getExternalKey())");
									out.writeln(".setGlobalReference(", item, ".getMappedObject().getMeta().getGlobalKey())");
									out.writeln(".build())");
								});
								out.write(".collect(", Collectors.class, ".toList())");
							});
						},
						typeUtil.wrap(typeUtil.LIST, metaClass)
					));
			} else {
				JavaStatementScope lambdaScope = scope.lambdaScope();
				GeneratedIdentifier r = lambdaScope.createUniqueIdentifier("r");
				GeneratedIdentifier m = lambdaScope.createUniqueIdentifier("m");
				return javaCode(value, context.withExpected(typeTranslator.toJavaReferenceType(RMetaAnnotatedType.withNoMeta(typeProvider.getRMetaAnnotatedType(value).getRType()))))
					.declareAsVariable(true, feature.getName(), scope)
					.mapExpression(it -> JavaExpression.from(
						out -> {
							out.writeln(metaClass, ".builder()");
							out.indented(() -> {
								out.writeln(".setGlobalReference(", Optional.class, ".ofNullable(", it, ")");
								out.indented(() -> {
									out.writeln(".map(", r, " -> ", r, ".getMeta())");
									out.writeln(".map(", m, " -> ", m, ".getGlobalKey())");
									out.writeln(".orElse(null))");
								});
								out.writeln(".setExternalReference(", Optional.class, ".ofNullable(", it, ")");
								out.indented(() -> {
									out.writeln(".map(", r, " -> ", r, ".getMeta())");
									out.writeln(".map(", m, " -> ", m, ".getExternalKey())");
									out.writeln(".orElse(null))");
								});
								out.write(".build()");
							});
						},
						metaClass
					));
			}
		} else {
			JavaType clazz = typeTranslator.toJavaReferenceType(RMetaAnnotatedType.withNoMeta(typeProvider.getRTypeOfFeature(feature, value).getRType()));
			if (feature instanceof Attribute attr && !requiresValueAssignment(assignAsKey, attr, value)) {
				clazz = typeTranslator.toJavaReferenceType(typeProvider.getRTypeOfFeature(feature, value));
			}
			return javaCode(value, context.withExpected(isMulti ? typeUtil.wrap(typeUtil.LIST, clazz) : clazz));
		}
	}

	@Override
	protected JavaStatementBuilder caseToDateOperation(ToDateOperation expr, Context context) {
		return conversionOperation(expr, context, out -> out.write(Date.class, "::parse"), DateTimeParseException.class);
	}

	@Override
	protected JavaStatementBuilder caseToDateTimeOperation(ToDateTimeOperation expr, Context context) {
		return conversionOperation(expr, context, out -> out.write(LocalDateTime.class, "::parse"), DateTimeParseException.class);
	}

	@Override
	protected JavaStatementBuilder caseToZonedDateTimeOperation(ToZonedDateTimeOperation expr, Context context) {
		return conversionOperation(expr, context, out -> out.write(ZonedDateTime.class, "::parse"), DateTimeParseException.class);
	}

	@Override
	protected JavaStatementBuilder caseSwitchOperation(SwitchOperation expr, Context context) {
		RType inputRType = typeSystem.stripFromTypeAliases(typeProvider.getRMetaAnnotatedType(expr.getArgument()).getRType());
		if (inputRType instanceof RChoiceType choiceType) {
			JavaStatementBuilder switchArgument = javaCode(expr.getArgument(), context.withExpected(typeUtil.wrap(typeUtil.MAPPER, typeTranslator.toJavaReferenceType(choiceType))));

			return createSwitchJavaExpression(expr, switchArgument, (acc, switchCase, switchArg) -> {
				RChoiceOption choiceOption = new RChoiceOption(switchCase.getGuard().getChoiceOptionGuard(), choiceType, typeProvider);
				GeneratedIdentifier itemVar = context.scope.createIdentifier(identifierService.getImplicitVarInContext(switchCase.getExpression()), uncapitalize(choiceOption.getType().getRType().getName()));
				JavaStatementBuilder optionExpr = navigateToChoiceOption(switchArg, choiceType, choiceOption, context);
				return optionExpr
					.collapseToSingleExpression(context.scope)
					.mapExpression(it -> new JavaIfThenElseBuilder(
						JavaExpression.from(out -> out.write(it, ".get() != null"), JavaPrimitiveType.BOOLEAN),
						new JavaLocalVariableDeclarationStatement(true, it.getExpressionType(), itemVar, it)
							.append(javaCode(switchCase.getExpression(), context)),
						acc,
						typeUtil
					));
			}, context);
		} else if (inputRType instanceof RDataType dataType) {
			JavaStatementBuilder switchArgument = javaCode(expr.getArgument(), context.withExpected(typeTranslator.toJavaReferenceType(dataType)));

			return createSwitchJavaExpression(expr, switchArgument, (acc, switchCase, switchArg) -> {
				RDataType caseType = rObjectFactory.buildRDataType(switchCase.getGuard().getDataGuard());
				JavaType caseJavaType = typeTranslator.toJavaReferenceType(caseType);

				GeneratedIdentifier itemVar = context.scope.createIdentifier(identifierService.getImplicitVarInContext(switchCase.getExpression()), uncapitalize(caseType.getName()));
				JavaExpression castExpression = JavaExpression.from(out -> out.write("(", caseJavaType, ") ", switchArg), caseJavaType);
				return new JavaIfThenElseBuilder(
					JavaExpression.from(out -> out.write(switchArg, " instanceof ", caseJavaType), JavaPrimitiveType.BOOLEAN),
					new JavaLocalVariableDeclarationStatement(true, caseJavaType, itemVar, castExpression)
						.append(javaCode(switchCase.getExpression(), context)),
					acc,
					typeUtil
				);
			}, context);
		} else if (inputRType instanceof REnumType enumType) {
			JavaStatementBuilder switchArgument = javaCode(expr.getArgument(), context.withExpected(typeTranslator.toJavaReferenceType(enumType)));

			return createSwitchJavaExpression(expr, switchArgument, (acc, switchCase, switchArg) -> {
				JavaStatementBuilder enumCaseToCheck = enumCall(switchCase.getGuard().getEnumGuard(), switchArgument.getExpressionType());
				return new JavaIfThenElseBuilder(
					JavaExpression.from(out -> out.write(switchArg, " == ", enumCaseToCheck), JavaPrimitiveType.BOOLEAN),
					javaCode(switchCase.getExpression(), context),
					acc,
					typeUtil
				);
			}, context);
		} else if (inputRType instanceof RBasicType basicType) {
			JavaStatementBuilder switchArgument = javaCode(expr.getArgument(), context.withExpected(typeUtil.wrap(typeUtil.MAPPER, typeTranslator.toJavaReferenceType(basicType))));
			JavaType mapperSConditionType = typeUtil.wrap(typeUtil.MAPPER_S, typeUtil.getItemType(switchArgument.getExpressionType()));

			return createSwitchJavaExpression(expr, switchArgument, (acc, switchCase, switchArg) -> {
				JavaStatementBuilder literalCaseToCheck = javaCode(switchCase.getGuard().getLiteralGuard(), context.withExpected(mapperSConditionType));
				return new JavaIfThenElseBuilder(
					JavaExpression.from(out -> out.write(runtimeMethod("areEqual"), "(", switchArg, ", ", literalCaseToCheck, ", ", CardinalityOperator.class, ".All).get()"), JavaPrimitiveType.BOOLEAN),
					javaCode(switchCase.getExpression(), context),
					acc,
					typeUtil
				);
			}, context);
		}
		return null;
	}

	private JavaStatementBuilder createSwitchJavaExpression(SwitchOperation expr, JavaStatementBuilder switchArgument, SwitchCaseFold fold, Context context) {
		JavaStatementBuilder defaultExpr = expr.getDefault() == null ? JavaLiteral.NULL : javaCode(expr.getDefault(), context);
		return switchArgument
			.declareAsVariable(true, "switchArgument", context.scope)
			.mapExpression(switchArg -> {
				List<SwitchCaseOrDefault> cases = expr.getCases().stream().filter(switchCase -> !switchCase.isDefault()).toList();
				JavaStatementBuilder javaSwitchExpr = defaultExpr;
				for (SwitchCaseOrDefault switchCase : Lists.reverse(cases)) {
					javaSwitchExpr = fold.apply(javaSwitchExpr, switchCase, switchArg);
				}
				JavaStatementBuilder elseBranch = javaSwitchExpr;
				return typeCoercionService.addCoercions(switchArg, typeUtil.getItemType(switchArg.getExpressionType()), context.scope)
					.mapExpression(it -> JavaExpression.from(out -> out.write(it, " == null"), JavaPrimitiveType.BOOLEAN))
					.mapExpression(it -> new JavaIfThenElseBuilder(
						it,
						JavaLiteral.NULL,
						elseBranch,
						typeUtil
					));
			});
	}

	@Override
	protected JavaStatementBuilder caseWithMetaOperation(WithMetaOperation expr, Context context) {
		RMetaAnnotatedType withMetaRMetaType = typeProvider.getRMetaAnnotatedType(expr);
		JavaClass<?> withMetaJavaType = deriveJavaTypeWithDefault(withMetaRMetaType, typeUtil.getItemType(context.expectedType));
		RMetaAnnotatedType argumentRMetaType = typeProvider.getRMetaAnnotatedType(expr.getArgument());
		JavaClass<?> argumentJavaType = deriveJavaTypeWithDefault(argumentRMetaType, withMetaJavaType instanceof RJavaWithMetaValue withMetaValue ? withMetaValue.getValueType() : withMetaJavaType);

		List<MetaEntry> metaEntries = new ArrayList<>();
		for (WithMetaEntry entry : expr.getEntries()) {
			JavaType entryType = typeTranslator.toJavaReferenceType(typeProvider.getRTypeOfFeature(entry.getKey(), expr).getRType());
			metaEntries.add(new MetaEntry(entry.getKey().getName(),
					javaCode(entry.getValue(), context.withExpected(entryType)).collapseToSingleExpression(context.scope)));
		}

		JavaStatementBuilder argumentExpression = javaCode(expr.getArgument(), context.withExpected(argumentJavaType))
				.mapExpression(it -> JavaExpression.from(out -> {
					out.write(it);
					if (needsBuilder(it)) {
						out.write(" == null ? null : ", it, ".toBuilder()");
					}
				}, typeUtil.toBuilder(argumentJavaType)))
				.collapseToSingleExpression(context.scope);

		if (withMetaJavaType instanceof RJavaFieldWithMeta || withMetaJavaType instanceof RJavaPojoInterface) {
			List<MetaEntry> attributeMetaEntries = metaEntries.stream().filter(m -> ecoreUtil.isAttributeMeta(m.name())).toList();
			List<MetaEntry> typeMetaEntries = metaEntries.stream().filter(m -> ecoreUtil.isTypeMeta(m.name())).toList();
			boolean setMeta = !attributeMetaEntries.isEmpty();
			boolean setMetafields = !typeMetaEntries.isEmpty();

			JavaStatementBuilder withMetaArgument = argumentExpression
					.declareAsVariable(true, "withMetaArgument", context.scope);
			GeneratedIdentifier withMetaArgumentVar = context.scope.getIdentifierOrThrow(argumentExpression);

			if (setMetafields && !setMeta) {
				if (argumentJavaType instanceof RJavaWithMetaValue) {
					return withMetaArgument
						.mapExpression(it -> JavaExpression.from(out -> out.write(it, ".getOrCreateValue().getOrCreateMeta()", metaSetters(typeMetaEntries)), typeUtil.getItemType(withMetaJavaType)))
						.completeAsExpressionStatement()
						.append(new JavaVariable(withMetaArgumentVar, argumentJavaType));
				} else {
					return withMetaArgument
						.mapExpression(it -> JavaExpression.from(out -> out.write(it, ".getOrCreateMeta()", metaSetters(typeMetaEntries)), typeUtil.getItemType(withMetaJavaType)))
						.completeAsExpressionStatement()
						.append(new JavaVariable(withMetaArgumentVar, argumentJavaType));
				}
			} else if (!setMetafields && setMeta) {
				if (argumentJavaType instanceof RJavaWithMetaValue) {
					return withMetaArgument
						.mapExpression(it -> JavaExpression.from(out -> out.write(it, ".getOrCreateMeta()", metaSetters(attributeMetaEntries)), typeUtil.getItemType(withMetaJavaType)))
						.completeAsExpressionStatement()
						.append(new JavaVariable(withMetaArgumentVar, withMetaJavaType));
				} else {
					return withMetaArgument
						.mapExpression(it -> JavaExpression.from(out -> out.write(withMetaJavaType, ".builder().setValue(", it, ").setMeta(", MetaFields.class, ".builder()", metaSetters(attributeMetaEntries), ")"), typeUtil.getItemType(withMetaJavaType)));
				}
			} else if (setMetafields && setMeta) {
				if (argumentJavaType instanceof RJavaWithMetaValue) {
					return withMetaArgument
						.mapExpression(it -> JavaExpression.from(out -> out.write(it, ".getOrCreateValue().getOrCreateMeta()", metaSetters(typeMetaEntries)), JavaPrimitiveType.VOID))
						.completeAsExpressionStatement()
						.append(new JavaVariable(withMetaArgumentVar, argumentJavaType))
						.mapExpression(it -> JavaExpression.from(out -> out.write(it, ".getOrCreateMeta()", metaSetters(attributeMetaEntries)), typeUtil.getItemType(withMetaJavaType)))
						.completeAsExpressionStatement()
						.append(new JavaVariable(withMetaArgumentVar, withMetaJavaType));
				} else {
					return withMetaArgument
						.mapExpression(it -> JavaExpression.from(out -> out.write(it, ".getOrCreateMeta()", metaSetters(typeMetaEntries)), JavaPrimitiveType.VOID))
						.completeAsExpressionStatement()
						.append(new JavaVariable(withMetaArgumentVar, argumentJavaType))
						.mapExpression(it -> JavaExpression.from(out -> out.write(withMetaJavaType, ".builder().setValue(", it, ").setMeta(", MetaFields.class, ".builder()", metaSetters(attributeMetaEntries), ")"), typeUtil.getItemType(withMetaJavaType)));
				}
			} else {
				return withMetaArgument;
			}
		}

		if (withMetaJavaType instanceof RJavaReferenceWithMeta) {
			List<MetaEntry> metaEntriesWithoutAddress = metaEntries.stream().filter(m -> !"address".equals(m.name())).toList();
			MetaEntry metaAddressEntry = metaEntries.stream().filter(m -> "address".equals(m.name())).findFirst().orElse(null);

			return argumentExpression.mapExpression(it -> JavaExpression.from(out -> {
				out.write(withMetaJavaType, ".builder().setValue(", it, ")");
				for (MetaEntry m : metaEntriesWithoutAddress) {
					out.write(".set", toPojoSetter(m.name()), "(", m.value(), ")");
				}
				if (metaAddressEntry != null) {
					out.write(".set", toPojoSetter(metaAddressEntry.name()), "(", Reference.class, ".builder().set", toPojoSetter(metaAddressEntry.name()), "(", metaAddressEntry.value(), "))");
				}
				out.write(".build()");
			}, withMetaJavaType));
		}

		throw new IllegalStateException("Unsupported type: " + withMetaJavaType);
	}

	private CodeRenderer metaSetters(List<MetaEntry> entries) {
		return out -> entries.forEach(m -> out.write(".set", toPojoSetter(m.name()), "(", m.value(), ")"));
	}

	private JavaClass<?> deriveJavaTypeWithDefault(RMetaAnnotatedType withMetaRMetaType, JavaType defaultType) {
		if (withMetaRMetaType.getRType().equals(builtinTypeService.NOTHING) && defaultType instanceof JavaClass<?> defaultClass) {
			return defaultClass;
		}
		return typeTranslator.toJavaReferenceType(withMetaRMetaType);
	}

	private String toPojoSetter(String metaEntryName) {
		return capitalize(PojoPropertyUtil.toPojoPropertyName(metaEntryName));
	}

	private boolean needsBuilder(JavaExpression expr) {
		return expr != JavaLiteral.NULL && typeUtil.hasBuilderType(expr.getExpressionType());
	}

	@Override
	protected JavaStatementBuilder caseSuperCall(RosettaSuperCall expr, Context context) {
		return JavaLiteral.NULL;
	}
}
