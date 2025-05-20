package com.regnosys.rosetta.generator.java.types;

import static com.rosetta.model.lib.SerializedNameConstants.*;

import java.util.Collection;
import java.util.List;

import com.rosetta.model.lib.meta.ReferenceWithMeta;
import com.rosetta.model.lib.process.AttributeMeta;
import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaParameterizedType;
import com.rosetta.util.types.JavaReferenceType;
import com.rosetta.util.types.JavaType;
import com.rosetta.util.types.JavaTypeDeclaration;

public class RJavaReferenceWithMeta extends RJavaWithMetaValue {
	private final DottedPath namespace;
	private final JavaTypeUtil javaTypeUtil;
	private final JavaParameterizedType<ReferenceWithMeta<?>> referenceWithMetaParameterisedType;

	
	public RJavaReferenceWithMeta(JavaReferenceType valueType, DottedPath namespace, JavaTypeUtil javaTypeUtil) {
		super(valueType);
		this.namespace = namespace;
		this.javaTypeUtil = javaTypeUtil;
		referenceWithMetaParameterisedType = javaTypeUtil.wrap(javaTypeUtil.REFERENCE_WITH_META, valueType);
	}

	@Override
	public boolean isSubtypeOf(JavaType other) {
		if (referenceWithMetaParameterisedType.isSubtypeOf(other)) {
			return true;
		}
		if (javaTypeUtil.ROSETTA_MODEL_OBJECT.isSubtypeOf(other)) {
			return true;
		}
		if (javaTypeUtil.GLOBAL_KEY.isSubtypeOf(other)) {
			return true;
		}
		return false;
	}

	@Override
	public String getSimpleName() {
		return "ReferenceWithMeta" + valueType.getSimpleName();
	}

	@Override
	public List<? extends JavaTypeDeclaration<?>> getInterfaceDeclarations() {
		return List.of(javaTypeUtil.ROSETTA_MODEL_OBJECT, javaTypeUtil.REFERENCE_WITH_META);
	}

	@Override
	public DottedPath getPackageName() {
		return namespace;
	}

	@Override
	public List<JavaClass<?>> getInterfaces() {
		return List.of(javaTypeUtil.ROSETTA_MODEL_OBJECT, referenceWithMetaParameterisedType);
	}

	@Override
	public Collection<JavaPojoProperty> getOwnProperties() {
		return List.of(
				new JavaPojoProperty("value", null, DATA, "value", "value", valueType, null, null, false, List.of()),
				new JavaPojoProperty("globalReference", null, REFERENCE, "globalReference", "globalReference", javaTypeUtil.STRING, null, AttributeMeta.META, false, List.of()),
				new JavaPojoProperty("externalReference", "reference", EXTERNAL_REFERENCE, "externalReference", "externalReference", javaTypeUtil.STRING, null, AttributeMeta.META, false, List.of()),
				new JavaPojoProperty("reference", "address", null, "reference", "reference", javaTypeUtil.REFERENCE, null, null, false, List.of())
			);
	}
}
