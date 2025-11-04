package com.regnosys.rosetta.generator.java.object.validators;

import java.util.stream.Stream;

import org.eclipse.xtend2.lib.StringConcatenationClient;

import com.regnosys.rosetta.generator.java.RObjectJavaClassGenerator;
import com.regnosys.rosetta.generator.java.scoping.JavaClassScope;
import com.regnosys.rosetta.generator.java.types.JavaPojoInterface;
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator;
import com.regnosys.rosetta.generator.java.types.RGeneratedJavaClass;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.RObjectFactory;

import jakarta.inject.Inject;

public abstract class AbstractValidatorGenerator extends RObjectJavaClassGenerator<RDataType, RGeneratedJavaClass<?>> {
	@Inject
	private RObjectFactory rObjectFactory;
	@Inject
	private JavaTypeTranslator translator;
	
	abstract protected RGeneratedJavaClass<?> createValidatorClass(JavaPojoInterface pojo);
	abstract protected StringConcatenationClient generateClass(RDataType type, RGeneratedJavaClass<?> validator, JavaPojoInterface pojo, String version, JavaClassScope scope);
	
	@Override
	protected Stream<RDataType> streamObjects(RosettaModel model) {
		return model.getElements()
				.stream()
				.filter(elem -> elem instanceof Data)
				.map(elem -> (Data)elem)
				.map(rObjectFactory::buildRDataType);
	}
	
	@Override
	protected RGeneratedJavaClass<?> createTypeRepresentation(RDataType type) {
		return createValidatorClass(translator.toJavaReferenceType(type));
	}

	@Override
	protected StringConcatenationClient generateClass(RDataType type, RGeneratedJavaClass<?> validator,
			String version, JavaClassScope scope) {
		return generateClass(type, validator, translator.toJavaReferenceType(type), version, scope);
	}

}
