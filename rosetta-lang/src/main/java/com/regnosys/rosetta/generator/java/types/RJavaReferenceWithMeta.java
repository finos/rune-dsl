package com.regnosys.rosetta.generator.java.types;

import static com.rosetta.model.lib.SerializedNameConstants.*;

import java.util.Collection;
import java.util.List;

import com.regnosys.rosetta.generator.java.scoping.JavaPackageName;
import com.rosetta.model.lib.meta.ReferenceWithMeta;
import com.rosetta.model.lib.process.AttributeMeta;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaParameterizedType;
import com.rosetta.util.types.JavaReferenceType;
import com.rosetta.util.types.JavaTypeDeclaration;

public class RJavaReferenceWithMeta extends RJavaWithMetaValue {
	private final JavaTypeUtil javaTypeUtil;
	private final JavaParameterizedType<ReferenceWithMeta<?>> referenceWithMetaParameterisedType;

	public RJavaReferenceWithMeta(JavaReferenceType valueType, JavaPackageName packageName, JavaTypeUtil javaTypeUtil) {
		super(valueType, packageName, "ReferenceWithMeta" + valueType.getSimpleName());
		this.javaTypeUtil = javaTypeUtil;
		this.referenceWithMetaParameterisedType = javaTypeUtil.wrap(javaTypeUtil.REFERENCE_WITH_META, valueType);
	}

	@Override
	public List<? extends JavaTypeDeclaration<?>> getInterfaceDeclarations() {
		return List.of(javaTypeUtil.ROSETTA_MODEL_OBJECT, javaTypeUtil.REFERENCE_WITH_META);
	}

	@Override
	public List<JavaClass<?>> getInterfaces() {
		return List.of(javaTypeUtil.ROSETTA_MODEL_OBJECT, referenceWithMetaParameterisedType);
	}

	@Override
	public Collection<JavaPojoProperty> getOwnProperties() {
		return List.of(
				new JavaPojoProperty(this, "value", null, DATA, "value", "value", valueType, null, null, false, List.of()),
				new JavaPojoProperty(this, "globalReference", null, REFERENCE, "globalReference", "globalReference", javaTypeUtil.STRING, null, AttributeMeta.META, false, List.of()),
				new JavaPojoProperty(this, "externalReference", "reference", EXTERNAL_REFERENCE, "externalReference", "externalReference", javaTypeUtil.STRING, null, AttributeMeta.META, false, List.of()),
				new JavaPojoProperty(this, "reference", "address", null, "reference", "reference", javaTypeUtil.REFERENCE, null, null, false, List.of())
			);
	}
}
