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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.xtend2.lib.StringConcatenationClient;

import com.regnosys.rosetta.generator.java.statement.builder.JavaExpression;
import com.rosetta.model.lib.process.AttributeMeta;
import com.rosetta.util.types.JavaType;

public class JavaPojoProperty {
	private final String name;
	private final String runeName;
	private final String serializedName;
	private final String compatibilityName;
	private final JavaType type;
	private final String javadoc;
	private final JavaPojoProperty parentProperty;
	private final AttributeMeta meta; // used in `process` method
	private final boolean hasLocation; // used in builder `getOrCreate`
	private final List<AttributeMetaType> attributeMetaTypes;

	public JavaPojoProperty(String name, String runeName, String serializedName, String compatibilityName, JavaType type, String javadoc, AttributeMeta meta, boolean hasLocation, List<AttributeMetaType> attributeMetaTypes) {
		this(name, runeName, serializedName, compatibilityName, type, javadoc, meta, hasLocation, attributeMetaTypes, null);
	}
	private JavaPojoProperty(String name, String runeName, String serializedName, String compatibilityName, JavaType type, String javadoc, AttributeMeta meta, boolean hasLocation, List<AttributeMetaType> attributeMetaTypes, JavaPojoProperty parentProperty) {
		this.name = name;
		this.runeName = runeName;
		this.serializedName = serializedName;
		this.compatibilityName = compatibilityName;
		this.type = type;
		this.javadoc = javadoc;
		this.meta = meta;
		this.hasLocation = hasLocation;
		this.attributeMetaTypes = attributeMetaTypes;
		this.parentProperty = parentProperty;
	}
	public JavaPojoProperty specialize(String compatibilityName, JavaType newType, String newJavadoc, AttributeMeta newMeta, boolean newHasLocation, List<AttributeMetaType> attributeMetaTypes) {
		return new JavaPojoProperty(name, runeName, serializedName, compatibilityName, newType, newJavadoc, newMeta, newHasLocation, attributeMetaTypes, this);
	}
	
	public boolean isCompatibleWithParent() {
		return parentProperty == null || type.isSubtypeOf(parentProperty.type);
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
	public String getGetterName() {
		return "get" + StringUtils.capitalize(compatibilityName);
	}
	public String getGetOrCreateName() {
		return "getOrCreate" + StringUtils.capitalize(compatibilityName);
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
	
	public JavaExpression applyGetter(JavaExpression expr) {
		return JavaExpression.from(new StringConcatenationClient() {
			@Override
			protected void appendTo(TargetStringConcatenation target) {
				target.append(expr);
				target.append('.');
				target.append(getGetterName());
				target.append("()");
			}
		}, type);
	}
	
	@Override
	public String toString() {
		return JavaPojoProperty.class.getSimpleName() + "[" + type.getSimpleName() + " " + getGetterName() + "()]";
	}
	@Override
	public int hashCode() {
		return Objects.hash(compatibilityName, hasLocation, javadoc, meta, name, runeName, serializedName, parentProperty, type, attributeMetaTypes);
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
		return Objects.equals(compatibilityName, other.compatibilityName) && hasLocation == other.hasLocation
				&& Objects.equals(javadoc, other.javadoc) && meta == other.meta && Objects.equals(name, other.name) 
				&& Objects.equals(runeName, other.runeName) && Objects.equals(serializedName, other.serializedName)
				&& Objects.equals(attributeMetaTypes, other.attributeMetaTypes) && Objects.equals(parentProperty, other.parentProperty) 
				&& Objects.equals(type, other.type);
	}
}
