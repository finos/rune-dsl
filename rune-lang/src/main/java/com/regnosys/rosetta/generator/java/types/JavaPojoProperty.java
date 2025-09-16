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

package com.regnosys.rosetta.generator.java.types;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.xtend2.lib.StringConcatenationClient;

import com.regnosys.rosetta.generator.java.statement.builder.JavaExpression;
import com.rosetta.model.lib.process.AttributeMeta;
import com.rosetta.util.types.JavaType;

public class JavaPojoProperty {
	private static final Set<String> OPERATION_NAMES_TO_ESCAPE =
			Set.of(
					"getClass", // from java.lang.Object
					"getType" // from com.rosetta.model.lib.RosettaModelObject
				);
	
	private final JavaPojoInterface pojo;
	private final String name;
	private final String runeName;
	private final String serializedName;
	private final String getterCompatibilityName;
	private final String setterCompatibilityName;
	private final JavaType type;
	private final String javadoc;
	private final JavaPojoProperty parentProperty;
    private final boolean isRequired;
    private final AttributeMeta meta; // used in `process` method
	private final boolean hasLocation; // used in builder `getOrCreate`
	private final List<AttributeMetaType> attributeMetaTypes;

	public JavaPojoProperty(JavaPojoInterface pojo, String name, String runeName, String serializedName, String getterCompatibilityName, String setterCompatibilityName, JavaType type, String javadoc, AttributeMeta meta, boolean hasLocation, List<AttributeMetaType> attributeMetaTypes, boolean isRequired) {
		this(pojo, name, runeName, serializedName, getterCompatibilityName, setterCompatibilityName, type, javadoc, meta, hasLocation, attributeMetaTypes, isRequired, null);
	}
	private JavaPojoProperty(JavaPojoInterface pojo, String name, String runeName, String serializedName, String getterCompatibilityName, String setterCompatibilityName, JavaType type, String javadoc, AttributeMeta meta, boolean hasLocation, List<AttributeMetaType> attributeMetaTypes, boolean isRequired, JavaPojoProperty parentProperty) {
		this.pojo = pojo;
        this.name = name;
		this.runeName = runeName;
		this.serializedName = serializedName;
		this.getterCompatibilityName = getterCompatibilityName;
		this.setterCompatibilityName = setterCompatibilityName;
		this.type = type;
		this.javadoc = javadoc;
		this.meta = meta;
		this.hasLocation = hasLocation;
		this.attributeMetaTypes = attributeMetaTypes;
		this.isRequired = isRequired;
		this.parentProperty = parentProperty;
	}
	public JavaPojoProperty specialize(JavaPojoInterface pojo, String getterCompatibilityName, String setterCompatibilityName, JavaType newType, String newJavadoc, AttributeMeta newMeta, boolean newHasLocation, List<AttributeMetaType> attributeMetaTypes, boolean isRequired) {
		return new JavaPojoProperty(pojo, name, runeName, serializedName, getterCompatibilityName, setterCompatibilityName, newType, newJavadoc, newMeta, newHasLocation, attributeMetaTypes, isRequired, this);
	}
	
	public String getOperationName(JavaPojoPropertyOperationType operationType) {
		String compatibilityName;
		if (operationType == JavaPojoPropertyOperationType.GET || operationType == JavaPojoPropertyOperationType.GET_OR_CREATE) {
			compatibilityName = this.getterCompatibilityName;
		} else {
			compatibilityName = this.setterCompatibilityName;
		}
		return escapeOperationName(operationType.getPrefix() + StringUtils.capitalize(compatibilityName) + operationType.getPostfix());
	}
	private String escapeOperationName(String opName) {
		if (OPERATION_NAMES_TO_ESCAPE.contains(opName)) {
			return "_" + opName;
		}
		return opName;
	}
	
	public boolean isCompatibleTypeWithParent() {
		return parentProperty == null || type.isSubtypeOf(parentProperty.type);
	}
	public boolean isSameTypeAsParent() {
		return parentProperty == null || type.equals(parentProperty.type);
	}
	public boolean getterOverridesParentGetter() {
		return parentProperty != null && getterCompatibilityName.equals(parentProperty.getterCompatibilityName);
	}
	
	public String getName() {
		return name;
	}
	public String getRuneName() {
		return runeName;
	}
	public String getSerializedName() {
		return serializedName;
	}
	public String getGetterCompatibilityName() {
		return getterCompatibilityName;
	}
	public String getSetterCompatibilityName() {
		return setterCompatibilityName;
	}
	public JavaType getType() {
		return type;
	}
	public String getJavadoc() {
		return javadoc;
	}
	public AttributeMeta getMeta() {
		return meta;
	}
	public boolean hasLocation() {
		return hasLocation;
	}
	public List<AttributeMetaType> getAttributeMetaTypes() {
        return attributeMetaTypes;
    }
    public JavaPojoProperty getParentProperty() {
		return parentProperty;
	}

	public boolean isRequired() {
		return isRequired;
	}

	public JavaExpression applyGetter(JavaExpression expr) {
		return JavaExpression.from(new StringConcatenationClient() {
			@Override
			protected void appendTo(TargetStringConcatenation target) {
				target.append(expr);
				target.append('.');
				target.append(getOperationName(JavaPojoPropertyOperationType.GET));
				target.append("()");
			}
		}, type);
	}
	
	@Override
	public String toString() {
		return JavaPojoProperty.class.getSimpleName() + "[" + type.getSimpleName() + " " + getterCompatibilityName + "]";
	}
	@Override
	public int hashCode() {
		return Objects.hash(pojo, getterCompatibilityName, setterCompatibilityName, hasLocation, javadoc, meta, name, runeName, serializedName, parentProperty, type, attributeMetaTypes, isRequired);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JavaPojoProperty other = (JavaPojoProperty) obj;
		return Objects.equals(pojo, other.pojo)
                && Objects.equals(getterCompatibilityName, other.getterCompatibilityName) && Objects.equals(setterCompatibilityName, other.setterCompatibilityName) && hasLocation == other.hasLocation
				&& Objects.equals(javadoc, other.javadoc) && meta == other.meta && Objects.equals(name, other.name) 
				&& Objects.equals(runeName, other.runeName) && Objects.equals(serializedName, other.serializedName)
				&& Objects.equals(attributeMetaTypes, other.attributeMetaTypes) && Objects.equals(parentProperty, other.parentProperty) 
				&& Objects.equals(type, other.type) && Objects.equals(isRequired, other.isRequired) ;
	}
}
