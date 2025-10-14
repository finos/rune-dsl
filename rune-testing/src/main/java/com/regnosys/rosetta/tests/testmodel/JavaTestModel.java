package com.regnosys.rosetta.tests.testmodel;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import com.google.inject.Injector;
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator;
import com.regnosys.rosetta.generator.java.types.RJavaEnum;
import com.regnosys.rosetta.generator.java.types.RJavaEnumValue;
import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.rosetta.RosettaReport;
import com.regnosys.rosetta.rosetta.RosettaRule;
import com.regnosys.rosetta.rosetta.simple.Condition;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.tests.compiler.InMemoryJavacCompiler;
import com.regnosys.rosetta.tests.util.ExpressionJavaEvaluatorService;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.REnumType;
import com.regnosys.rosetta.types.RFunction;
import com.regnosys.rosetta.types.RObjectFactory;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.context.RuneScope;
import com.rosetta.model.lib.functions.LabelProvider;
import com.rosetta.model.lib.functions.RosettaFunction;
import com.rosetta.util.types.JavaType;

/**
 * A test utility for integration tests related to Java code generation.
 * 
 * It allows access to generated Java source files and compiled classes
 * based on their original Rune name in the model. Additionally, it allows
 * creating instances of such classes, either by injecting (functions) or
 * by evaluating expressions (types and values).
 */
public class JavaTestModel {
	private final RosettaTestModel rosettaModel;
	private final Map<String, String> javaSourceCode;
	
	private InMemoryJavacCompiler inMemoryCompiler = null;
	private Map<String, Class<?>> javaClasses = null;
	
	private final RObjectFactory rObjectFactory;
	private final JavaTypeTranslator typeTranslator;
	private final ExpressionJavaEvaluatorService evaluatorService;
	private final Injector injector;
	
	public JavaTestModel(RosettaTestModel rosettaModel, Map<String, String> javaSourceCode, RObjectFactory rObjectFactory, JavaTypeTranslator typeTranslator, ExpressionJavaEvaluatorService evaluatorService, Injector injector) {
		this.rosettaModel = rosettaModel;
		this.javaSourceCode = javaSourceCode;
		
		this.rObjectFactory = rObjectFactory;
		this.typeTranslator = typeTranslator;
		this.evaluatorService = evaluatorService;
		this.injector = injector;
	}
	
	private String getSource(JavaType javaType) {
		String source = javaSourceCode.get(javaType.toString());
		if (source == null) {
			throw new NoSuchElementException("No Java source found for " + javaType + ".\n\n" + javaSourceCode.keySet().stream().collect(Collectors.joining("\n")));
		}
		return source;
	}
	private void assertCompiled() {
		if (javaClasses == null) {
			throw new IllegalStateException("Model is not compiled yet. Please call `JavaTestModel::compile` before loading classes.");
		}
	}
	private <T> Class<? extends T> getClass(Class<T> superClass, JavaType javaType) {
		assertCompiled();
		
		Class<?> javaClass = javaClasses.get(javaType.toString());
		if (javaClass == null) {
			throw new NoSuchElementException("No Java class found for " + javaType + ".\n\n" + javaClasses.keySet().stream().collect(Collectors.joining("\n")));
		}
		return javaClass.asSubclass(superClass);
	}
	
	public JavaTestModel compile() {
		if (javaClasses != null) {
			throw new IllegalStateException("Model was already compiled!");
		}
		
		inMemoryCompiler = InMemoryJavacCompiler
			.newInstance()
			.useParentClassLoader(this.getClass().getClassLoader())
			.useOptions("--release", "8", "-Xlint:all", "-Xdiags:verbose");
		
		javaSourceCode.forEach((className, sourceCode) -> inMemoryCompiler.addSource(className, sourceCode));

		javaClasses = inMemoryCompiler.compileAll();
		
		return this;
	}
	
    public <T> T evaluateExpression(Class<T> resultType, CharSequence expr) {
    	return evaluateExpression(resultType, expr, null);
    }
	public <T> T evaluateExpression(Class<T> resultType, CharSequence expr, RuneScope scope) {		
		return resultType.cast(evaluateExpression(JavaType.from(resultType), expr));
	}
    public Object evaluateExpression(JavaType resultType, CharSequence expr) {
        return evaluateExpression(resultType, expr, null);
    }
	public Object evaluateExpression(JavaType resultType, CharSequence expr, RuneScope scope) {
		assertCompiled();
		
		return evaluatorService.evaluate(expr, rosettaModel.getModel(), resultType, inMemoryCompiler.getClassloader());
	}
	
	public RosettaTestModel getRosettaModel() {
		return rosettaModel;
	}
    
    private JavaType getScopeJavaType(String name) {
        return null;
    }
    public Class<? extends RuneScope> getScopeClass(String name) {
    	return getClass(RuneScope.class, getScopeJavaType(name));
    }
    public RuneScope getScopeInstance(String name) {
    	return injector.getInstance(getScopeClass(name));
    }
	
	public JavaType getTypeJavaType(String name) {
		Data type = rosettaModel.getType(name);
		RDataType t = rObjectFactory.buildRDataType(type);
		return typeTranslator.toJavaReferenceType(t);
	}
	public String getTypeJavaSource(String name) {
		return getSource(getTypeJavaType(name));
	}
	public Class<? extends RosettaModelObject> getTypeJavaClass(String name) {
		return getClass(RosettaModelObject.class, getTypeJavaType(name));
	}
	
	private RJavaEnum getEnumJavaType(String name) {
		RosettaEnumeration enumeration = rosettaModel.getEnum(name);
		REnumType t = rObjectFactory.buildREnumType(enumeration);
		return typeTranslator.toJavaReferenceType(t);
	}
	public String getEnumJavaSource(String name) {
		return getSource(getEnumJavaType(name));
	}
	@SuppressWarnings("unchecked")
	public Class<? extends Enum<?>> getEnumJavaClass(String name) {
		return (Class<? extends Enum<?>>) getClass(Enum.class, getEnumJavaType(name));
	}
	public Object getEnumJavaValue(String enumName, String valueName) {
		RJavaEnum enumJavaType = getEnumJavaType(enumName);
		Class<?> enumJavaClass = getClass(Enum.class, enumJavaType);
		RJavaEnumValue enumValueRepr = enumJavaType.getEnumValues().stream()
			.filter(v -> valueName.equals(v.getRosettaName()))
			.findAny()
			.orElseThrow(() -> new NoSuchElementException("The enum " + enumName + " does not have a value named " + valueName));
		for (Object enumConst : enumJavaClass.getEnumConstants()) {
			if (enumConst.toString().equals(enumValueRepr.getName()) || enumConst.toString().equals(enumValueRepr.getDisplayName())) {
				return enumConst;
			}
		}
		return null;
	}
	
	private JavaType getFunctionJavaType(String name) {
		Function func = rosettaModel.getFunction(name);
		RFunction f = rObjectFactory.buildRFunction(func);
		return typeTranslator.toFunctionJavaClass(f);
	}
	public String getFunctionJavaSource(String name) {
		return getSource(getFunctionJavaType(name));
	}
	public Class<? extends RosettaFunction> getFunctionJavaClass(String name) {
		return getClass(RosettaFunction.class, getFunctionJavaType(name));
	}
	public RosettaFunction getFunctionJavaInstance(String name) {
		return injector.getInstance(getFunctionJavaClass(name));
	}
	
	private JavaType getFunctionJavaLabelProviderType(String name) {
		Function func = rosettaModel.getFunction(name);
		RFunction f = rObjectFactory.buildRFunction(func);
		return typeTranslator.toLabelProviderJavaClass(f);
	}
	public String getFunctionJavaLabelProviderSource(String name) {
		return getSource(getFunctionJavaLabelProviderType(name));
	}
	public Class<? extends LabelProvider> getFunctionJavaLabelProviderClass(String name) {
		return getClass(LabelProvider.class, getFunctionJavaLabelProviderType(name));
	}
	public LabelProvider getFunctionJavaLabelProviderInstance(String name) {
		return injector.getInstance(getFunctionJavaLabelProviderClass(name));
	}
	
	private JavaType getRuleJavaType(String name) {
		RosettaRule rule = rosettaModel.getRule(name);
		RFunction f = rObjectFactory.buildRFunction(rule);
		return typeTranslator.toFunctionJavaClass(f);
	}
	public String getRuleJavaSource(String name) {
		return getSource(getRuleJavaType(name));
	}
	public Class<? extends RosettaFunction> getRuleJavaClass(String name) {
		return getClass(RosettaFunction.class, getRuleJavaType(name));
	}
	public RosettaFunction getRuleJavaInstance(String name) {
		return injector.getInstance(getRuleJavaClass(name));
	}
	
	private JavaType getReportJavaType(String body, String... corpusList) {
		RosettaReport report = rosettaModel.getReport(body, corpusList);
		RFunction f = rObjectFactory.buildRFunction(report);
		return typeTranslator.toFunctionJavaClass(f);
	}
	public String getReportJavaSource(String body, String... corpusList) {
		return getSource(getReportJavaType(body, corpusList));
	}
	public Class<? extends RosettaFunction> getReportJavaClass(String body, String... corpusList) {
		return getClass(RosettaFunction.class, getReportJavaType(body, corpusList));
	}
	public RosettaFunction getReportJavaInstance(String body, String... corpusList) {
		return injector.getInstance(getReportJavaClass(body, corpusList));
	}
	
	private JavaType getConditionJavaType(String typeName, String conditionName) {
		Condition condition = rosettaModel.getCondition(typeName, conditionName);
		return typeTranslator.toConditionJavaClass(condition);
	}
	public String getConditionJavaSource(String typeName, String conditionName) {
		return getSource(getConditionJavaType(typeName, conditionName));
	}
	public Class<?> getConditionJavaClass(String typeName, String conditionName) {
		return getClass(Object.class, getConditionJavaType(typeName, conditionName));
	}
	public Object getConditionJavaInstance(String typeName, String conditionName) {
		return injector.getInstance(getConditionJavaClass(typeName, conditionName));
	}
}
