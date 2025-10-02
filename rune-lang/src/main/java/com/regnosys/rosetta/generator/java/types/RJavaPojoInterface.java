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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.regnosys.rosetta.generator.java.scoping.JavaPackageName;
import com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil;
import com.regnosys.rosetta.types.RAttribute;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.TypeSystem;
import com.rosetta.model.lib.process.AttributeMeta;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaType;

public class RJavaPojoInterface extends JavaPojoInterface {	
	private final RDataType type;
	private RJavaPojoInterface superPojo = null;
	private Map<String, JavaPojoProperty> ownProperties = null;
	private Map<String, JavaPojoProperty> allProperties = null;
	
	private final TypeSystem typeSystem;
	private final JavaTypeTranslator typeTranslator;
	private final JavaTypeUtil typeUtil;
	private final ModelGeneratorUtil generatorUtil;

	public RJavaPojoInterface(RDataType type, TypeSystem typeSystem, JavaTypeTranslator typeTranslator, JavaTypeUtil typeUtil, ModelGeneratorUtil generatorUtil) {
		super(JavaPackageName.escape(type.getNamespace()), type.getName(), typeUtil);
		this.type = type;
		
		this.typeSystem = typeSystem;
		this.typeTranslator = typeTranslator;
		this.typeUtil = typeUtil;
		this.generatorUtil = generatorUtil;
	}
	
	@Override
	public String getJavadoc() {
		return generatorUtil.javadoc(type.getEObject().getDefinition(), type.getEObject().getReferences(), getVersion());
	}
	@Override
	public String getRosettaName() {
		return type.getName();
	}
	@Override
	public String getVersion() {
		return type.getEObject().getModel().getVersion();
	}
	
	@Override
	public Collection<JavaPojoProperty> getOwnProperties() {
		initializeProperties();
		return ownProperties.values();
	}
	@Override
	public Collection<JavaPojoProperty> getAllProperties() {
		initializeProperties();
		return allProperties.values();
	}
	private void initializeProperties() {
		if (ownProperties == null) {
			RJavaPojoInterface superPojo = getSuperPojo();
			if (superPojo == null) {
				allProperties = new LinkedHashMap<>();
			} else {
				superPojo.initializeProperties();
				allProperties = new LinkedHashMap<>(superPojo.allProperties);
			}
			ownProperties = new LinkedHashMap<>();
			type.getOwnAttributes().forEach(attr -> {
				String name = attr.getName();
				JavaType type = typeTranslator.toMetaJavaType(attr);
				addPropertyIfNecessary(name,
						name,
						name,
						type,
						generatorUtil.javadoc(attr.getDefinition(), attr.getAllDocReferences(), null),
						attr.getRMetaAnnotatedType().hasMetaAttribute("id") ? AttributeMeta.GLOBAL_KEY_FIELD : null,
						attr.getRMetaAnnotatedType().hasMetaAttribute("location"),
						generateAttributeMetaTypes(attr),
						!attr.getCardinality().isOptional());
			});
			if (type.hasMetaAttribute("key")) {
				JavaType metaFieldsType = type.hasMetaAttribute("template") ? typeUtil.META_AND_TEMPLATE_FIELDS : typeUtil.META_FIELDS;
				addPropertyIfNecessary("meta",
						null,
						"meta",
						metaFieldsType,
						null,
						null,
						false,
						List.of(),
						false);
			}
		}
	}
	private void addPropertyIfNecessary(String name, String runeName, String serializedName, JavaType type, String javadoc, AttributeMeta meta, boolean hasLocation, List<AttributeMetaType> attributeMetaTypes, boolean isRequired) {
		JavaPojoProperty parentProperty = allProperties.get(name);
		if (parentProperty == null) {
			JavaPojoProperty newProperty = new JavaPojoProperty(
					this,
					name,
					runeName,
					serializedName,
					name,
					name,
					type,
					javadoc,
					meta,
					hasLocation,
					attributeMetaTypes,
					isRequired);
			ownProperties.put(name, newProperty);
			allProperties.put(name, newProperty);
		} else {
			JavaType parentType = parentProperty.getType();
			if (!type.equals(parentType) || isRequired != parentProperty.isRequired()) {
				String getterCompatibilityName;
				if (type.isSubtypeOf(parentType)) {
					// Specialize existing property => reuse getter of parent
					getterCompatibilityName = parentProperty.getGetterCompatibilityName();
				} else {
					// Incompatible specialization => need new getter
					getterCompatibilityName = getIncompatiblePropertyName(name, parentType, type);
				}
				String setterCompatibilityName;
				if (type.getTypeErasure().equals(parentType.getTypeErasure())) {
					// Type erasures are the same => need new setter
					setterCompatibilityName = getIncompatiblePropertyName(name, parentType, type);
				} else {
					// Type erasures are not the same => we can overload the setter
					setterCompatibilityName = parentProperty.getSetterCompatibilityName();
				}
				JavaPojoProperty newProperty = parentProperty.specialize(this, getterCompatibilityName, setterCompatibilityName, type, javadoc, meta, hasLocation, attributeMetaTypes, isRequired);
				ownProperties.put(name, newProperty);
				allProperties.put(name, newProperty);
			}
		}
	}
	private String getIncompatiblePropertyName(String propertyName, JavaType parentType, JavaType specializedType) {
		if (typeUtil.isList(parentType) && typeUtil.isList(specializedType)) {
			// List to list
			return getIncompatiblePropertyName(propertyName, typeUtil.getItemType(parentType), typeUtil.getItemType(specializedType));
		} else if (typeUtil.isList(parentType)) {
			// List to single
			JavaType parentItemType = typeUtil.getItemType(parentType);
			if (parentItemType.equals(specializedType)) {
				return propertyName + "OverriddenAsSingle";
			}
			return propertyName + "OverriddenAsSingle" + specializedType.getSimpleName();
		} else {
			// Type to other type
			return propertyName + "OverriddenAs" + specializedType.getSimpleName();
		}
	}
	private List<AttributeMetaType> generateAttributeMetaTypes(RAttribute rAttribute) {
	    return rAttribute.getRMetaAnnotatedType()
	        .getMetaAttributes()
	        .stream()
	        .filter(metaAttr -> metaAttr.getName().equals("address") || metaAttr.getName().equals("location"))
	        .map(metaAttr -> {
	            if (metaAttr.getName().equals("address")) {
	                return AttributeMetaType.SCOPED_REFERENCE;
	            } else if (metaAttr.getName().equals("location")) {
	                return AttributeMetaType.SCOPED_KEY;
	            }
	            throw new IllegalStateException("AttributeMetaType for meta attributes of " + metaAttr.getName() + " are not supported");
	        }).collect(Collectors.toList());
	}
	
	@Override
	public RJavaPojoInterface getSuperPojo() {
		if (superPojo == null) {
			RDataType superType = type.getSuperType();
			if (superType != null) {
				superPojo = new RJavaPojoInterface(superType, typeSystem, typeTranslator, typeUtil, generatorUtil);
			}
		}
		return superPojo;
	}

	@Override
	public List<JavaClass<?>> getInterfaceDeclarations() {
		List<JavaClass<?>> interfaces = new ArrayList<>();
		
		JavaClass<?> superPojo = getSuperPojo();
		if (superPojo == null) {
			interfaces.add(typeUtil.ROSETTA_MODEL_OBJECT);
		} else {
			interfaces.add(superPojo);
		}
		if (type.hasMetaAttribute("key")) {
			interfaces.add(typeUtil.GLOBAL_KEY);
		}
		if (type.hasMetaAttribute("template")) {
			interfaces.add(typeUtil.TEMPLATABLE);
		}
		return interfaces;
	}
	
	@Override
	public List<JavaClass<?>> getInterfaces() {
		return getInterfaceDeclarations();
	}
}
