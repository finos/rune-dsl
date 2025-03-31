package com.regnosys.rosetta.generator.java.types;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.xtext.EcoreUtil2;

import com.regnosys.rosetta.rosetta.ParametrizedRosettaType;
import com.regnosys.rosetta.rosetta.RosettaSymbol;
import com.regnosys.rosetta.rosetta.RosettaTypeWithConditions;
import com.regnosys.rosetta.rosetta.expression.ChoiceOperation;
import com.regnosys.rosetta.rosetta.expression.OneOfOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference;
import com.regnosys.rosetta.rosetta.simple.Condition;
import com.regnosys.rosetta.types.RType;
import com.regnosys.rosetta.types.RosettaTypeProvider;
import com.regnosys.rosetta.types.TypeSystem;
import com.regnosys.rosetta.utils.ModelIdProvider;
import com.rosetta.model.lib.validation.Validator;
import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaGenericTypeDeclaration;
import com.rosetta.util.types.JavaParameterizedType;
import com.rosetta.util.types.JavaReferenceType;
import com.rosetta.util.types.JavaType;
import com.rosetta.util.types.JavaTypeDeclaration;

public class JavaConditionInterface extends JavaClass<Object> {
	private final Condition condition;
	
	private final String simpleName;
	private final RType instanceType;
	private final JavaReferenceType instanceClass;
	private Map<String, JavaType> parameters = null;
	
	private final ModelIdProvider modelIdProvider;
	private final RosettaTypeProvider typeProvider;
	private final JavaTypeUtil typeUtil;
	private final JavaTypeTranslator typeTranslator;
	
	public JavaConditionInterface(Condition condition, ModelIdProvider modelIdProvider, RosettaTypeProvider typeProvider, TypeSystem typeSystem, JavaTypeUtil typeUtil, JavaTypeTranslator typeTranslator) {
		this.condition = condition;
		
		this.simpleName = computeConditionClassName(condition);
		this.instanceType = typeSystem.typeWithUnknownArgumentsToRType(condition.getEnclosingType());
		this.instanceClass = typeTranslator.toJavaReferenceType(instanceType);
		
		this.modelIdProvider = modelIdProvider;
		this.typeProvider = typeProvider;
		this.typeUtil = typeUtil;
		this.typeTranslator = typeTranslator;
	}
	
	public Map<String, JavaType> getParameters() {
		if (parameters == null) {
			RosettaTypeWithConditions enclosingType = condition.getEnclosingType();
			if (enclosingType instanceof ParametrizedRosettaType) {
				Set<RosettaSymbol> usedSymbols = EcoreUtil2.eAllOfType(condition.getExpression(), RosettaSymbolReference.class).stream().map(ref -> ref.getSymbol()).collect(Collectors.toSet());
				parameters = new LinkedHashMap<>();
				((ParametrizedRosettaType) enclosingType).getParameters().forEach(param -> {
					if (usedSymbols.contains(param)) {
						JavaType paramType = typeTranslator.toJavaReferenceType(typeProvider.getRTypeOfSymbol(param));
						parameters.put(param.getName(), paramType);
					}
				});
			} else {
				parameters = Collections.emptyMap();
			}
		}
		return parameters;
	}
	public RType getInstanceType() {
		return instanceType;
	}
	public JavaReferenceType getInstanceClass() {
		return instanceClass;
	}
	
	private static String computeConditionClassName(Condition cond) {
		String conditionName;
		if (cond.getName() != null) {
			conditionName = cond.getName();
		} else {
			long index = cond.getEnclosingType().getConditions().stream()
					.filter(c -> c.getName() != null)
					.takeWhile(c -> c != cond)
					.count();
			String type;
			if (isOneOf(cond)) {
				type = "OneOf";
			} else if (isChoice(cond)) {
				type = "Choice";
			} else {
				type = "DataRule";
			}
			conditionName = type + index;
		}
		return cond.getEnclosingType().getName() + conditionName;
	}
	private static boolean isOneOf(Condition cond) {
		return cond.getExpression() instanceof OneOfOperation;
	}
	private static boolean isChoice(Condition cond) {
		return cond.getExpression() instanceof ChoiceOperation;
	}
	
	public boolean implementsValidatorInterface() {
		return getParameters().isEmpty();
	}
	public JavaParameterizedType<Validator<?>> getValidatorInterface() {
		return JavaParameterizedType.from(typeUtil.VALIDATOR, instanceClass);
	}
	
	@Override
	public boolean isSubtypeOf(JavaType other) {
		if (implementsValidatorInterface()) {
			if (getValidatorInterface().isSubtypeOf(other)) {
				return true;
			}
		}
		if (other instanceof JavaConditionInterface) {
			return condition.equals(((JavaConditionInterface)other).condition);
		}
		return false;
	}

	@Override
	public String getSimpleName() {
		return simpleName;
	}

	@Override
	public List<? extends JavaGenericTypeDeclaration<?>> getInterfaceDeclarations() {
		if (implementsValidatorInterface()) {
			return Collections.singletonList(typeUtil.VALIDATOR);
		}
		return Collections.emptyList();
	}
	
	@Override
	public List<JavaClass<?>> getInterfaces() {
		if (implementsValidatorInterface()) {
			return Collections.singletonList(getValidatorInterface());
		}
		return Collections.emptyList();
	}

	@Override
	public DottedPath getPackageName() {
		return modelIdProvider.toDottedPath(condition.getEnclosingType().getModel()).child("validation").child("datarule");
	}
	
	@Override
	public JavaClass<? super Object> getSuperclassDeclaration() {
		return JavaClass.OBJECT;
	}
	
	@Override
	public JavaClass<? super Object> getSuperclass() {
		return getSuperclassDeclaration();
	}

	@Override
	public boolean extendsDeclaration(JavaTypeDeclaration<?> other) {
		if (other instanceof JavaType) {
			return this.isSubtypeOf((JavaType)other);
		}
		return false;
	}

	@Override
	public boolean isFinal() {
		return false;
	}

	@Override
	public Class<?> loadClass(ClassLoader classLoader) throws ClassNotFoundException {
		return Class.forName(getCanonicalName().toString(), true, classLoader);
	}
}
