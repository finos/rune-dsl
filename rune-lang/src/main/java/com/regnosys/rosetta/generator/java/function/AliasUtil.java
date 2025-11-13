package com.regnosys.rosetta.generator.java.function;

import org.eclipse.xtend2.lib.StringConcatenationClient;

import com.regnosys.rosetta.generator.GeneratedIdentifier;
import com.regnosys.rosetta.generator.java.expression.JavaDependencyProvider;
import com.regnosys.rosetta.generator.java.scoping.JavaIdentifierRepresentationService;
import com.regnosys.rosetta.generator.java.scoping.JavaMethodScope;
import com.regnosys.rosetta.generator.java.scoping.JavaStatementScope;
import com.regnosys.rosetta.generator.java.types.JavaPojoInterface;
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator;
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil;
import com.regnosys.rosetta.types.CardinalityProvider;
import com.regnosys.rosetta.types.RAttribute;
import com.regnosys.rosetta.types.RFunction;
import com.regnosys.rosetta.types.RMetaAnnotatedType;
import com.regnosys.rosetta.types.RShortcut;
import com.regnosys.rosetta.types.RosettaTypeProvider;
import com.regnosys.rosetta.utils.ExpressionHelper;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaGenericTypeDeclaration;
import com.rosetta.util.types.JavaReferenceType;

import jakarta.inject.Inject;

public class AliasUtil {
	@Inject
	private ExpressionHelper expressionHelper;
	@Inject
	private RosettaTypeProvider typeProvider;
	@Inject
	private CardinalityProvider cardinalityProvider;
	@Inject
	private JavaTypeTranslator typeTranslator;
	@Inject
	private JavaTypeUtil typeUtil;
	@Inject
	private JavaDependencyProvider dependencyProvider;
	@Inject
	private JavaIdentifierRepresentationService identifierService;
	
	public JavaReferenceType getReturnType(RShortcut alias) {
		if (requiresOutput(alias)) {
			var rtype = typeProvider.getRMetaAnnotatedType(alias.getExpression());
			return toMultiBuilderType(rtype, cardinalityProvider.isMulti(alias.getExpression()));
		} else {
			JavaGenericTypeDeclaration<?> wrapper = typeUtil.MAPPER_S;
			if (cardinalityProvider.isMulti(alias.getExpression())) {
				wrapper = typeUtil.MAPPER_C;
			}
			var result = typeUtil.wrapExtendsIfNotFinal(wrapper, alias.getExpression());
			return result;
		}
	}
	
	public StringConcatenationClient getParameters(RShortcut alias, GeneratedIdentifier runtimeContextId, JavaMethodScope scope) {
		RFunction func = alias.getFunction();
		return new StringConcatenationClient() {
			@Override
			protected void appendTo(TargetStringConcatenation target) {
				if (requiresOutput(alias)) {
                    RAttribute output = func.getOutput();
                    JavaReferenceType outputParameterType = toMultiBuilderType(output.getRMetaAnnotatedType(), output.isMulti());

                    target.append(outputParameterType);
                    target.append(" ");
                    target.append(scope.getIdentifierOrThrow(output));
                    target.append(", ");
                }
                for (RAttribute input : func.getInputs()) {
                    JavaReferenceType inputParameterType = typeTranslator.toMetaJavaType(input);
					
					target.append(inputParameterType);
					target.append(" ");
					target.append(scope.getIdentifierOrThrow(input));
					target.append(", ");
				}
				for (JavaClass<?> dependency : dependencyProvider.javaDependencies(alias.getExpression())) {
					target.append(dependency);
					target.append(" ");
					target.append(scope.getIdentifierOrThrow(identifierService.toDependencyInstance(dependency)));
					target.append(", ");
				}
				target.append(typeUtil.RUNE_CONTEXT);
				target.append(" ");
				target.append(runtimeContextId);
			}
		};
	}
	
	public StringConcatenationClient getArguments(RShortcut alias, GeneratedIdentifier runtimeContextId, JavaStatementScope scope) {
		RFunction func = alias.getFunction();
		return new StringConcatenationClient() {
			@Override
			protected void appendTo(TargetStringConcatenation target) {
				if (requiresOutput(alias)) {
					RAttribute output = func.getOutput();
					target.append(scope.getIdentifierOrThrow(output));
					target.append(".toBuilder(), ");
				}
				for (RAttribute input : func.getInputs()) {
					target.append(scope.getIdentifierOrThrow(input));
					target.append(", ");
				}
				for (JavaClass<?> dependency : dependencyProvider.javaDependencies(alias.getExpression())) {
					target.append(scope.getIdentifierOrThrow(identifierService.toDependencyInstance(dependency)));
					target.append(", ");
				}
				target.append(runtimeContextId);
			}
		};
	}
	
	public boolean requiresOutput(RShortcut alias) {
		return expressionHelper.usesOutputParameter(alias.getExpression());
	}
	
	private JavaReferenceType toBuilderType(RMetaAnnotatedType type) {
		var javaType = typeTranslator.toJavaReferenceType(type);
		if (javaType instanceof JavaPojoInterface pojoInterface) {
			return pojoInterface.toBuilderInterface();
		}
		return javaType;
	}
	private JavaReferenceType toMultiBuilderType(RMetaAnnotatedType type, boolean isMulti) {
		var builderType = toBuilderType(type);
		if (isMulti) {
			return typeUtil.wrap(typeUtil.LIST, builderType);
		}
		return builderType;
	}
}
