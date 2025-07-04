package com.regnosys.rosetta.generator.java.types;

import java.util.List;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.util.types.JavaClass;

public class JavaPojoImpl extends RGeneratedJavaClass<RosettaModelObject> {
	private final JavaPojoInterface pojoInterface;

	protected JavaPojoImpl(JavaPojoInterface pojoInterface) {
		super(pojoInterface.getEscapedPackageName(), pojoInterface.getNestedTypeName().child(pojoInterface.getSimpleName() + "Impl"));
		this.pojoInterface = pojoInterface;
	}

	@Override
	public JavaClass<? super RosettaModelObject> getSuperclassDeclaration() {
		JavaPojoInterface superPojo = pojoInterface.getSuperPojo();
		if (superPojo != null && pojoInterface.getOwnProperties().stream().allMatch(JavaPojoProperty::isCompatibleTypeWithParent)) {
			return superPojo.toImplClass();
		}
		return JavaClass.OBJECT;
	}
	
	@Override
	public JavaClass<? super RosettaModelObject> getSuperclass() {
		return getSuperclassDeclaration();
	}

	@Override
	public List<JavaClass<?>> getInterfaceDeclarations() {
		return List.of(pojoInterface);
	}

	@Override
	public List<JavaClass<?>> getInterfaces() {
		return getInterfaceDeclarations();
	}

}
