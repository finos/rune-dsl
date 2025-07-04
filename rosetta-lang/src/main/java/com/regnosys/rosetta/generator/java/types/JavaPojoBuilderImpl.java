package com.regnosys.rosetta.generator.java.types;

import java.util.List;

import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.util.types.JavaClass;

public class JavaPojoBuilderImpl extends RGeneratedJavaClass<RosettaModelObjectBuilder> {
private final JavaPojoInterface pojoInterface;

	protected JavaPojoBuilderImpl(JavaPojoInterface pojoInterface) {
		super(pojoInterface.getEscapedPackageName(), pojoInterface.getNestedTypeName().child(pojoInterface.getSimpleName() + "BuilderImpl"));
		this.pojoInterface = pojoInterface;
	}

	@Override
	public JavaClass<? super RosettaModelObjectBuilder> getSuperclassDeclaration() {
		JavaPojoInterface superPojo = pojoInterface.getSuperPojo();
		if (superPojo != null && pojoInterface.getOwnProperties().stream().allMatch(JavaPojoProperty::isSameTypeAsParent)) {
			return superPojo.toBuilderImplClass();
		}
		return JavaClass.OBJECT;
	}

	@Override
	public JavaClass<? super RosettaModelObjectBuilder> getSuperclass() {
		return getSuperclassDeclaration();
	}

	@Override
	public List<JavaClass<?>> getInterfaceDeclarations() {
		return List.of(pojoInterface.toBuilderInterface());
	}

	@Override
	public List<JavaClass<?>> getInterfaces() {
		return getInterfaceDeclarations();
	}
}
