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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.eclipse.xtext.EcoreUtil2;

import com.regnosys.rosetta.RosettaExtensions;
import com.regnosys.rosetta.generator.java.RosettaJavaPackages;
import com.regnosys.rosetta.generator.object.ExpandedAttribute;
import com.regnosys.rosetta.generator.object.ExpandedType;
import com.regnosys.rosetta.generator.util.RosettaAttributeExtensions;
import com.regnosys.rosetta.rosetta.RegulatoryDocumentReference;
import com.regnosys.rosetta.rosetta.RosettaExternalFunction;
import com.regnosys.rosetta.rosetta.RosettaExternalRuleSource;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaNamed;
import com.regnosys.rosetta.rosetta.RosettaReport;
import com.regnosys.rosetta.rosetta.RosettaRootElement;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.rosetta.simple.Operation;
import com.regnosys.rosetta.rosetta.simple.Segment;
import com.regnosys.rosetta.types.RAliasType;
import com.regnosys.rosetta.types.RAttribute;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.REnumType;
import com.regnosys.rosetta.types.RErrorType;
import com.regnosys.rosetta.types.RFunction;
import com.regnosys.rosetta.types.ROperation;
import com.regnosys.rosetta.types.RType;
import com.regnosys.rosetta.types.RosettaTypeProvider;
import com.regnosys.rosetta.types.TypeSystem;
import com.regnosys.rosetta.types.builtin.RBasicType;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;
import com.regnosys.rosetta.types.builtin.RDateTimeType;
import com.regnosys.rosetta.types.builtin.RDateType;
import com.regnosys.rosetta.types.builtin.RNumberType;
import com.regnosys.rosetta.types.builtin.RStringType;
import com.regnosys.rosetta.types.builtin.RZonedDateTimeType;
import com.regnosys.rosetta.utils.RosettaTypeSwitch;
import com.rosetta.model.lib.ModelReportId;
import com.rosetta.model.lib.ModelSymbolId;
import com.rosetta.model.lib.functions.RosettaFunction;
import com.rosetta.model.lib.reports.ReportFunction;
import com.rosetta.model.lib.reports.Tabulator;
import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaParameterizedType;
import com.rosetta.util.types.JavaPrimitiveType;
import com.rosetta.util.types.JavaReferenceType;
import com.rosetta.util.types.JavaType;
import com.rosetta.util.types.generated.GeneratedJavaClass;
import com.rosetta.util.types.generated.GeneratedJavaClassService;

public class JavaTypeTranslator extends RosettaTypeSwitch<JavaType, Void> {
	private RBuiltinTypeService builtins;
	@Inject
	public JavaTypeTranslator(RBuiltinTypeService builtins) {
		super(builtins);
		this.builtins = builtins;
	}
	@Inject
	private RosettaJavaPackages packages;
	@Inject
	private RosettaExtensions extensions;
	@Inject
	private RosettaTypeProvider typeProvider;
	@Inject
	private TypeSystem typeSystem;
	@Inject
	private GeneratedJavaClassService generatedJavaClassService;
	@Inject
	private JavaTypeUtil typeUtil;
	
	private DottedPath getModelPackage(RosettaNamed object) {
		RosettaRootElement rootElement = EcoreUtil2.getContainerOfType(object, RosettaRootElement.class);
		RosettaModel model = rootElement.getModel();
		if (model == null)
			// Artificial attributes
			throw new IllegalArgumentException("Can not compute package name for " + object.eClass().getName() + " " + object.getName() + ". Element is not attached to a RosettaModel.");
		return modelPackage(model);
	}
	
	public JavaParameterizedType<List<?>> toPolymorphicList(JavaReferenceType t) {
		return typeUtil.wrapExtends(typeUtil.LIST, t);
	}
	public JavaClass<? extends RosettaFunction> toFunctionJavaClass(RFunction func) {
		switch (func.getOrigin()) {
		case FUNCTION:
			return generatedJavaClassService.toJavaFunction(func.getSymbolId());
		case REPORT:
			return generatedJavaClassService.toJavaReportFunction(func.getReportId());
		case RULE:
			return generatedJavaClassService.toJavaRule(func.getSymbolId());
		default:
			throw new IllegalStateException("Unknown origin of RFunction: " + func.getOrigin());
		}			 
	}
	public JavaClass<RosettaFunction> toFunctionJavaClass(Function func) {
		return generatedJavaClassService.toJavaFunction(getSymbolId(func));
	}
	public JavaClass<RosettaFunction> toFunctionJavaClass(RosettaExternalFunction func) {
		return new GeneratedJavaClass<>(packages.defaultLibFunctions(), func.getName(), RosettaFunction.class);
	}
	public JavaClass<ReportFunction<?, ?>> toReportFunctionJavaClass(RosettaReport report) {
		return generatedJavaClassService.toJavaReportFunction(getReportId(report));
	}
	public JavaClass<Tabulator<?>> toReportTabulatorJavaClass(RosettaReport report) {
		return generatedJavaClassService.toJavaReportTabulator(getReportId(report));
	}
	public JavaClass<Tabulator<?>> toTabulatorJavaClass(Data type, Optional<RosettaExternalRuleSource> ruleSource) {
		ModelSymbolId typeId = getSymbolId(type);
		Optional<RosettaExternalRuleSource> containingRuleSource = ruleSource.flatMap((rs) -> findContainingSuperRuleSource(type, rs));
		if (containingRuleSource.isEmpty()) {
			DottedPath packageName = typeId.getNamespace().child("reports");
			String simpleName = typeId.getName() + "TypeTabulator";
			return new GeneratedJavaClass<>(packageName, simpleName, new com.fasterxml.jackson.core.type.TypeReference<Tabulator<?>>() {});
		}
		ModelSymbolId sourceId = getSymbolId(containingRuleSource.get());
		DottedPath packageName = sourceId.getNamespace().child("reports");
		String simpleName = typeId.getName() + sourceId.getName() + "TypeTabulator";
		return new GeneratedJavaClass<>(packageName, simpleName, new com.fasterxml.jackson.core.type.TypeReference<Tabulator<?>>() {});
	}
	private Optional<RosettaExternalRuleSource> findContainingSuperRuleSource(Data type, RosettaExternalRuleSource ruleSource) {
		if (ruleSource.getExternalClasses().stream().filter(c -> c.getData().equals(type)).findAny().isPresent()) {
			return Optional.of(ruleSource);
		}
		return Optional.ofNullable(ruleSource.getSuperRuleSource()).flatMap(s -> findContainingSuperRuleSource(type, s));
	}
	public JavaClass<Tabulator<?>> toProjectionTabulatorJavaClass(Function projection) {
		return generatedJavaClassService.toJavaProjectionTabulator(getSymbolId(projection));
	}
	public JavaClass<Tabulator<?>> toTabulatorJavaClass(Data type, Function projection) {
		ModelSymbolId typeId = getSymbolId(type);
		ModelSymbolId projectionId = getSymbolId(projection);
		DottedPath packageName = projectionId.getNamespace().child("projections");
		String simpleName = typeId.getName() + projection.getName() + "TypeTabulator";
		return new GeneratedJavaClass<>(packageName, simpleName, new com.fasterxml.jackson.core.type.TypeReference<Tabulator<?>>() {});
	}
	public JavaReferenceType toMetaJavaType(Attribute attribute) {
		JavaReferenceType attrType = toJavaReferenceType(typeProvider.getRTypeOfSymbol(attribute));
		DottedPath namespace = getModelPackage(attribute.getTypeCall().getType());
		return toMetaJavaType(attrType, extensions.hasMetaFieldAnnotations(attribute), namespace);
	}
	public JavaReferenceType toMetaOrRegularJavaType(ExpandedAttribute expAttr) {
		JavaReferenceType attrType;
		if (expAttr.getRosettaType() != null) {
			attrType = toJavaReferenceType(typeSystem.typeCallToRType(expAttr.getRosettaType()));
		} else {
			attrType = expandedTypeToJavaType(expAttr.getType());
		}
		if (!expAttr.hasMetas()) {
			return attrType;
		}
		DottedPath namespace = getModelPackage(expAttr.getRosettaType().getType());
		return toMetaJavaType(attrType, expAttr.refIndex() < 0, namespace);
	}
	public JavaReferenceType toMultiMetaOrRegularJavaType(ExpandedAttribute expAttr) {
		JavaReferenceType singleType = toMetaOrRegularJavaType(expAttr);
		if (expAttr.isMultiple()) {
			if (expAttr.isDataType() || expAttr.hasMetas()) {
				return toPolymorphicList(singleType);
			} else {
				return typeUtil.wrap(typeUtil.LIST, singleType);
			}
		}
		return singleType;
	}
	public JavaClass<?> toMetaJavaType(ExpandedAttribute expAttr) {
		JavaReferenceType attrType;
		if (expAttr.getRosettaType() != null) {
			attrType = toJavaReferenceType(typeSystem.typeCallToRType(expAttr.getRosettaType()));
		} else {
			attrType = expandedTypeToJavaType(expAttr.getType());
		}
		DottedPath namespace = getModelPackage(expAttr.getRosettaType().getType());
		return toMetaJavaType(attrType, expAttr.refIndex() < 0, namespace);
	}
	public JavaReferenceType expandedTypeToJavaType(ExpandedType type) {
		if (type.getName().equals(RosettaAttributeExtensions.METAFIELDS_CLASS_NAME) || type.getName().equals(RosettaAttributeExtensions.META_AND_TEMPLATE_FIELDS_CLASS_NAME)) {
			return new GeneratedJavaClass<>(packages.basicMetafields(), type.getName(), Object.class);
		}
		if (type.isMetaType()) {//TODO ExpandedType needs to store the underlying type for meta types if we want them to be anything other than strings
			return typeUtil.STRING;
		}
		if (type.isBuiltInType()) {
			return toJavaReferenceType(builtins.getType(type.getName(), Collections.emptyMap()));
		}
		return new GeneratedJavaClass<>(modelPackage(type.getModel()), type.getName(), Object.class);
	}
	private JavaClass<?> toMetaJavaType(JavaReferenceType base, boolean hasMetaFieldAnnotations, DottedPath namespace) {
		String attributeTypeName = base.getSimpleName();
		String name;
		if (hasMetaFieldAnnotations) {
			name = "FieldWithMeta" + attributeTypeName;
		} else {
			name = "ReferenceWithMeta" + attributeTypeName;
		}
		DottedPath pkg = metaField(namespace);
		return new GeneratedJavaClass<>(pkg, name, Object.class);
	}
	public JavaClass<?> operationToReferenceWithMetaType(Operation op) {
		Attribute attr;
		if (op.getPath() == null) {
			attr = (Attribute)op.getAssignRoot(); // TODO: this won't work when assigning to an alias
		} else {
			List<Segment> segments = op.pathAsSegmentList();
			attr = segments.get(segments.size() - 1).getAttribute();
		}
		DottedPath namespace = getModelPackage(attr.getTypeCall().getType());
		return toMetaJavaType(toJavaReferenceType(typeProvider.getRTypeOfSymbol(attr)), false, namespace);
	}
	
	public JavaReferenceType operationToJavaType(ROperation op) {
		RAttribute attr;
		if (op.getPathTail().isEmpty()) {
			attr = (RAttribute)op.getPathHead(); // TODO: this won't work when assigning to an alias
		} else {
			List<RAttribute> segments = op.getPathTail();
			attr = segments.get(segments.size() - 1);
		}
		return attributeToJavaType(attr);
	}
	public JavaClass<?> operationToReferenceWithMetaType(ROperation op) {
		RAttribute attr;
		if (op.getPathTail().isEmpty()) {
			attr = (RAttribute)op.getPathHead(); // TODO: this won't work when assigning to an alias
		} else {
			List<RAttribute> segments = op.getPathTail();
			attr = segments.get(segments.size() - 1);
		}
		return toMetaJavaType(toJavaReferenceType(attr.getRType()), false, attr.getRType().getNamespace());
	}
	
	private String getTypeDebugInfo(RType type) {
		return type.toString() + " (" + type.getClass().getSimpleName() + ")";
	}
	public JavaReferenceType toJavaReferenceType(RType type) {
		JavaType jt = toJavaType(type);
		if (jt instanceof JavaPrimitiveType) {
			return ((JavaPrimitiveType)jt).toReferenceType();
		} else if (jt instanceof JavaReferenceType) {
			return (JavaReferenceType)jt;
		} else {
			throw new UnsupportedOperationException("Cannot convert type " + getTypeDebugInfo(type) + " to a Java reference type.");
		}
	}
	public JavaReferenceType toJavaReferenceType(Optional<RType> type) {
		return type.map(t -> toJavaReferenceType(t)).orElse(typeUtil.OBJECT);
	}
	public JavaReferenceType attributeToJavaType(RAttribute rAttribute) {
		JavaReferenceType itemType = toJavaReferenceType(rAttribute.getRType());
		if (rAttribute.isMulti()) {
			return typeUtil.wrapExtendsIfNotFinal(typeUtil.LIST, itemType);
		} else {
			return itemType;
		}
	}
	public JavaType toJavaType(RType type) {
		return doSwitch(type, null);
	}
	public JavaType toJavaType(Optional<RType> type) {
		return type.map(t -> toJavaType(t)).orElse(typeUtil.OBJECT);
	}
	public JavaClass<?> toJavaType(RDataType type) {
		return caseDataType(type, null);
	}
	
	public JavaReferenceType toPolymorphicListOrSingleJavaType(RType type, boolean isMany) {
		if (isMany) {
			return toPolymorphicList(toJavaReferenceType(type));
		} else
			return toJavaReferenceType(type);
	}
	public JavaReferenceType toListOrSingleJavaType(RType type, boolean isMany) {
		if (isMany) {
			return typeUtil.wrap(typeUtil.LIST, toJavaReferenceType(type));
		} else
			return toJavaReferenceType(type);
	}
	
	public JavaClass<?> toImplType(JavaClass<?> type) {
		return new GeneratedJavaClass<>(type.getPackageName(), type.getSimpleName() + "." + type.getSimpleName() + "Impl", Object.class);
	}
	public JavaClass<?> toBuilderType(JavaClass<?> type) {
		return new GeneratedJavaClass<>(type.getPackageName(), type.getSimpleName() + "." + type.getSimpleName() + "Builder", Object.class);
	}
	public JavaClass<?> toBuilderImplType(JavaClass<?> type) {
		return new GeneratedJavaClass<>(type.getPackageName(), type.getSimpleName() + "." + type.getSimpleName() + "BuilderImpl", Object.class);
	}
	
	public JavaClass<?> toValidatorClass(RDataType t) {
		return new GeneratedJavaClass<>(validation(getModelPackage(t.getData())), t.getName() + "Validator", Object.class);
	}
	public JavaClass<?> toTypeFormatValidatorClass(RDataType t) {
		return new GeneratedJavaClass<>(validation(getModelPackage(t.getData())), t.getName() + "TypeFormatValidator", Object.class);
	}
	public JavaClass<?> toOnlyExistsValidatorClass(RDataType t) {
		return new GeneratedJavaClass<>(existsValidation(getModelPackage(t.getData())), t.getName() + "OnlyExistsValidator", Object.class);
	}
	
	private ModelSymbolId getSymbolId(RosettaNamed named) {
		RosettaRootElement rootElement = EcoreUtil2.getContainerOfType(named, RosettaRootElement.class);
		RosettaModel model = rootElement.getModel();
		if (model == null)
			// Artificial attributes
			throw new IllegalArgumentException("Can not compute package name for " + named.eClass().getName() + " " + named.getName() + ". Element is not attached to a RosettaModel.");
		DottedPath namespace = DottedPath.splitOnDots(model.getName());
		return new ModelSymbolId(namespace, named.getName());
	}
	private ModelReportId getReportId(RosettaReport report) {
		RosettaRootElement rootElement = EcoreUtil2.getContainerOfType(report, RosettaRootElement.class);
		RosettaModel model = rootElement.getModel();
		if (model == null)
			// Artificial attributes
			throw new IllegalArgumentException("Can not compute package name for " + report.eClass().getName() + " " + report.getRegulatoryId() + ". Element is not attached to a RosettaModel.");
		DottedPath namespace = DottedPath.splitOnDots(model.getName());
		
		RegulatoryDocumentReference ref = report.getRegulatoryBody();
		String body = ref.getBody().getName();
		String[] corpusList = ref.getCorpusList().stream().map(c -> c.getName()).toArray(String[]::new);
		
		return new ModelReportId(namespace, body, corpusList);
	}
	private DottedPath modelPackage(RosettaModel model) {
		return DottedPath.splitOnDots(model.getName());
	}
	private DottedPath metaField(DottedPath p) {
		return p.child("metafields");
	}
	private DottedPath validation(DottedPath p) {
		return p.child("validation");
	}
	public DottedPath existsValidation(DottedPath p) {
		return validation(p).child("exists");
	}
	
	@Override
	protected JavaType caseErrorType(RErrorType type, Void context) {
		throw new IllegalArgumentException("Cannot convert an error type to a Java type.");
	}
	@Override
	protected JavaClass<?> caseDataType(RDataType type, Void context) {
		return new RJavaPojoInterface(type.getData(), typeSystem);
	}
	@Override
	protected JavaClass<?> caseEnumType(REnumType type, Void context) {
		return new RJavaEnum(type.getEnumeration());
	}
	@Override
	protected JavaType caseAliasType(RAliasType type, Void context) {
		return toJavaType(type.getRefersTo());
	}
	@Override
	protected JavaType caseNumberType(RNumberType type, Void context) {
		if (!type.isInteger()) {
			return JavaClass.from(BigDecimal.class);
		} else {
			int digits = type.getDigits().orElse(9);
			if (digits <= 9) {
				return JavaPrimitiveType.INT;
			} else if (digits <= 18) {
				return JavaPrimitiveType.LONG;
			} else {
				return JavaClass.from(BigInteger.class);
			}
		}
	}
	@Override
	protected JavaClass<String> caseStringType(RStringType type, Void context) {
		return typeUtil.STRING;
	}
	@Override
	protected JavaPrimitiveType caseBooleanType(RBasicType type, Void context) {
		return JavaPrimitiveType.BOOLEAN;
	}
	@Override
	protected JavaClass<LocalTime> caseTimeType(RBasicType type, Void context) {
		return typeUtil.LOCAL_TIME;
	}
	@Override
	protected JavaType caseMissingType(RBasicType type, Void context) {
		throw new IllegalArgumentException("Cannot convert a missing type to a Java type.");
	}
	@Override
	protected JavaClass<Void> caseNothingType(RBasicType type, Void context) {
		return typeUtil.VOID;
	}
	@Override
	protected JavaClass<Object> caseAnyType(RBasicType type, Void context) {
		return typeUtil.OBJECT;
	}
	@Override
	protected JavaClass<com.rosetta.model.lib.records.Date> caseDateType(RDateType type, Void context) {
		return typeUtil.DATE;
	}
	@Override
	protected JavaClass<LocalDateTime> caseDateTimeType(RDateTimeType type, Void context) {
		return typeUtil.LOCAL_DATE_TIME;
	}
	@Override
	protected JavaClass<ZonedDateTime> caseZonedDateTimeType(RZonedDateTimeType type, Void context) {
		return typeUtil.ZONED_DATE_TIME;
	}
}
