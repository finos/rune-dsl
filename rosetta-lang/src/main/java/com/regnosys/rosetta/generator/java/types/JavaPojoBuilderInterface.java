package com.regnosys.rosetta.generator.java.types;

import java.util.ArrayList;
import java.util.List;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaTypeDeclaration;

public class JavaPojoBuilderInterface extends RGeneratedJavaClass<RosettaModelObjectBuilder> {
	private final JavaPojoInterface pojoInterface;
	
	private final JavaTypeUtil typeUtil;

	protected JavaPojoBuilderInterface(JavaPojoInterface pojoInterface, JavaTypeUtil typeUtil) {
		super(pojoInterface.getEscapedPackageName(), pojoInterface.getNestedTypeName().child(pojoInterface.getSimpleName() + "Builder"));
		this.pojoInterface = pojoInterface;
		
		this.typeUtil = typeUtil;
	}

	@Override
	public JavaClass<? super RosettaModelObject> getSuperclassDeclaration() {
		return JavaClass.OBJECT;
	}
	
	@Override
	public JavaClass<? super RosettaModelObject> getSuperclass() {
		return getSuperclassDeclaration();
	}

	@Override
	public List<? extends JavaTypeDeclaration<?>> getInterfaceDeclarations() {
		List<? extends JavaTypeDeclaration<?>> baseInterfaces = pojoInterface.getInterfaceDeclarations();
		List<JavaTypeDeclaration<?>> interfaces = new ArrayList<>(baseInterfaces.size() + 1);
		
		interfaces.add(pojoInterface);
		for (var baseInterface : baseInterfaces) {
			interfaces.add(typeUtil.toBuilder(baseInterface));
		}
		
		return interfaces;
	}

	@Override
	public List<JavaClass<?>> getInterfaces() {
		List<JavaClass<?>> baseInterfaces = pojoInterface.getInterfaces();
		List<JavaClass<?>> interfaces = new ArrayList<>(baseInterfaces.size() + 1);
		
		interfaces.add(pojoInterface);
		for (var baseInterface : baseInterfaces) {
			interfaces.add(typeUtil.toBuilder(baseInterface));
		}
		
		return interfaces;
	}
}
