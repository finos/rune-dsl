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

import com.fasterxml.jackson.core.type.TypeReference;
import com.regnosys.rosetta.cache.caches.RJavaPojoInterfaceCache;
import com.regnosys.rosetta.generator.java.RosettaJavaPackages;
import com.regnosys.rosetta.generator.java.scoping.JavaPackageName;
import com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil;
import com.regnosys.rosetta.rosetta.RosettaExternalFunction;
import com.regnosys.rosetta.rosetta.RosettaFeature;
import com.regnosys.rosetta.rosetta.RosettaReport;
import com.regnosys.rosetta.rosetta.simple.*;
import com.regnosys.rosetta.types.*;
import com.regnosys.rosetta.types.builtin.*;
import com.regnosys.rosetta.utils.ModelIdProvider;
import com.regnosys.rosetta.utils.RosettaTypeSwitch;
import com.rosetta.model.lib.ModelReportId;
import com.rosetta.model.lib.ModelSymbolId;
import com.rosetta.model.lib.functions.LabelProvider;
import com.rosetta.model.lib.functions.RosettaFunction;
import com.rosetta.model.lib.meta.RosettaMetaData;
import com.rosetta.model.lib.reports.ReportFunction;
import com.rosetta.model.lib.validation.Validator;
import com.rosetta.util.DottedPath;
import com.rosetta.util.types.*;

import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class JavaTypeTranslator extends RosettaTypeSwitch<JavaType, Void> {
	@Inject
	public JavaTypeTranslator(RBuiltinTypeService builtins) {
		super(builtins);
	}
	@Inject
	private RosettaJavaPackages packages;
	@Inject
	private RosettaTypeProvider typeProvider;
	@Inject
	private TypeSystem typeSystem;
	@Inject
	private JavaTypeUtil typeUtil;
	@Inject
	private ModelIdProvider modelIdProvider;
	@Inject
	private ModelGeneratorUtil generatorUtil;
	@Inject
	private RJavaPojoInterfaceCache rJavaPojoInterfaceCache;
	
	@Deprecated
	public boolean isRosettaModelObject(RAttribute attr) {
		RMetaAnnotatedType rMetaAnnotatedType = attr.getRMetaAnnotatedType();
		return isValueRosettaModelObject(attr) || rMetaAnnotatedType.hasAttributeMeta();
	}
	@Deprecated
	public boolean isValueRosettaModelObject(RAttribute attr) {
		RType t = attr.getRMetaAnnotatedType().getRType();
		return t instanceof RDataType || t instanceof RChoiceType;
	}
	public boolean isRosettaModelObject(JavaType type) {
		return typeUtil.getItemType(type).isSubtypeOf(typeUtil.ROSETTA_MODEL_OBJECT);
	}
	public boolean isValueRosettaModelObject(JavaType type) {
		JavaType itemType = typeUtil.getItemType(type);
		if (itemType instanceof RJavaWithMetaValue) {
			return isValueRosettaModelObject((RJavaWithMetaValue)itemType);
		}
		return itemType.isSubtypeOf(typeUtil.ROSETTA_MODEL_OBJECT);
	}
	public boolean isValueRosettaModelObject(RJavaWithMetaValue t) {
		return t.getValueType().isSubtypeOf(typeUtil.ROSETTA_MODEL_OBJECT);
	}
	
	public JavaParameterizedType<List<?>> toPolymorphicList(JavaReferenceType t) {
		return typeUtil.wrapExtends(typeUtil.LIST, t);
	}
	public RGeneratedJavaClass<? extends RosettaFunction> toFunctionJavaClass(RFunction func) {
		switch (func.getOrigin()) {
		case FUNCTION:
			return toJavaFunctionClass(func.getSymbolId());
		case REPORT:
			return toJavaReportClass(func.getReportId());
		case RULE:
			return toJavaRuleClass(func.getSymbolId());
		default:
			throw new IllegalStateException("Unknown origin of RFunction: " + func.getOrigin());
		}			 
	}
	public RGeneratedJavaClass<? extends RosettaFunction> toFunctionJavaClass(Function func) {
		return toJavaFunctionClass(modelIdProvider.getSymbolId(func));
	}
	public RGeneratedJavaClass<? extends RosettaFunction> toFunctionJavaClass(RosettaExternalFunction func) {
		return RGeneratedJavaClass.create(JavaPackageName.escape(packages.defaultLibFunctions()), func.getName(), RosettaFunction.class);
	}
	public RGeneratedJavaClass<? extends ReportFunction<?, ?>> toReportFunctionJavaClass(RosettaReport report) {
		return toJavaReportClass(modelIdProvider.getReportId(report));
	}
	private RGeneratedJavaClass<? extends RosettaFunction> toJavaFunctionClass(ModelSymbolId functionId) {
		DottedPath funcPackageName = functionId.getNamespace().child("functions");
		String funcSimpleName = functionId.getName();
		return RGeneratedJavaClass.create(JavaPackageName.escape(funcPackageName), funcSimpleName, RosettaFunction.class);
	}
	private RGeneratedJavaClass<? extends ReportFunction<?, ?>> toJavaReportClass(ModelReportId reportId) {
		DottedPath reportPackageName = reportId.getNamespace().child("reports");
		String reportSimpleName = reportId.joinRegulatoryReference() + "ReportFunction";
		return RGeneratedJavaClass.create(JavaPackageName.escape(reportPackageName), reportSimpleName, new TypeReference<ReportFunction<?, ?>>() {});
	}
	private RGeneratedJavaClass<? extends RosettaFunction> toJavaRuleClass(ModelSymbolId ruleId) {
		DottedPath rulePackageName = ruleId.getNamespace().child("reports");
		String ruleSimpleName = ruleId.getName() + "Rule";
		return RGeneratedJavaClass.create(JavaPackageName.escape(rulePackageName), ruleSimpleName, new TypeReference<ReportFunction<?, ?>>() {});
	}
	
	public RGeneratedJavaClass<? extends RosettaMetaData<?>> toJavaMetaDataClass(JavaPojoInterface pojoClass) {
		JavaParameterizedType<RosettaMetaData<?>> superType = JavaParameterizedType.from(typeUtil.ROSETTA_META_DATA, pojoClass);
		return RGeneratedJavaClass.createImplementingInterface(JavaPackageName.escape(pojoClass.getPackageName().child("meta")), pojoClass.getSimpleName() + "Meta", superType);
	}
	
	public RGeneratedJavaClass<? extends LabelProvider> toLabelProviderJavaClass(RFunction function) {
		DottedPath packageName = function.getNamespace().child("labels");
		String simpleName = function.getAlphanumericName() + "LabelProvider";
		return RGeneratedJavaClass.create(JavaPackageName.escape(packageName), simpleName, LabelProvider.class);
	}
	
	public JavaConditionInterface toConditionJavaClass(Condition condition) {
		return JavaConditionInterface.create(condition, modelIdProvider, typeProvider, typeSystem, typeUtil, this);
	}
	public JavaClass<?> toDeepPathUtilJavaClass(RDataType choiceType) {
		ModelSymbolId typeId = modelIdProvider.getSymbolId(choiceType.getEObject());
		DottedPath packageName = typeId.getNamespace().child("util");
		String simpleName = typeId.getName() + "DeepPathUtil";
		return RGeneratedJavaClass.create(JavaPackageName.escape(packageName), simpleName, Object.class);
	}
	public JavaClass<?> toItemJavaType(RMetaAttribute attr) {
		return toJavaReferenceType(attr.getRType());
	}
	public JavaClass<?> toItemJavaType(RAttribute attr) {
		return toJavaReferenceType(attr.getRMetaAnnotatedType().getRType());
	}
	public JavaClass<?> toMetaItemJavaType(RAttribute attr) {
		return toJavaReferenceType(attr.getRMetaAnnotatedType());
	}
	public JavaClass<?> toForcedMetaItemJavaType(RAttribute attr) {
		JavaClass<?> metaItemJavaType = toMetaItemJavaType(attr);
		if (!attr.getRMetaAnnotatedType().hasAttributeMeta()) {
			RType rType = typeSystem.stripFromTypeAliases(attr.getRMetaAnnotatedType().getRType());
			DottedPath namespace = metaField(rType.getNamespace());
			return new RJavaFieldWithMeta(metaItemJavaType, JavaPackageName.escape(namespace), typeUtil);
		}
		return metaItemJavaType;
	}
	public JavaClass<?> toMetaJavaType(RAttribute attr) {
		JavaClass<?> itemType = toMetaItemJavaType(attr);
		if (attr.isMulti()) {
			if (isRosettaModelObject(attr)) {
				return toPolymorphicList(itemType);
			} else {
				return typeUtil.wrap(typeUtil.LIST, itemType);
			}
		}
		return itemType;
	}
	public JavaClass<?> toJavaType(RAttribute attr) {
		JavaClass<?> itemType = toItemJavaType(attr);
		if (attr.isMulti()) {
			if (isRosettaModelObject(attr)) {
				return toPolymorphicList(itemType);
			} else {
				return typeUtil.wrap(typeUtil.LIST, itemType);
			}
		}
		return itemType;
	}
	public JavaClass<?> toMetaJavaType(RFeature feature) {
		if (feature instanceof RAttribute) {
			return toMetaJavaType((RAttribute) feature);
		} else if (feature instanceof RMetaAttribute) {
			return toItemJavaType((RMetaAttribute) feature);
		} else {
			throw new UnsupportedOperationException("No JavaType exists for feature: " + feature.getName());
		}
	}	
	public JavaClass<?> toJavaType(RFeature feature) {
		if (feature instanceof RAttribute) {
			return toJavaType((RAttribute) feature);
		} else if (feature instanceof RMetaAttribute) {
			return toItemJavaType((RMetaAttribute) feature);
		} else {
			throw new UnsupportedOperationException("No JavaType exists for feature: " + feature.getName());
		}
	}
	public JavaClass<?> operationToReferenceWithMetaType(Operation op) {
		RosettaFeature feature;
		if (op.getPath() == null) {
			feature = (RosettaFeature)op.getAssignRoot(); // TODO: this won't work when assigning to an alias
		} else {
			List<Segment> segments = op.pathAsSegmentList();
			feature = segments.get(segments.size() - 1).getFeature();
		}
		return toJavaReferenceType(typeProvider.getRTypeOfFeature(feature, null));
	}
	public JavaReferenceType operationToMetaJavaType(ROperation op) {
		RFeature feature;
		if (op.getPathTail().isEmpty()) {
			feature = (RFeature)op.getPathHead(); // TODO: this won't work when assigning to an alias
		} else {
			List<? extends RFeature> segments = op.getPathTail();
			feature = segments.get(segments.size() - 1);
		}
		return toMetaJavaType(feature);
	}	
	public JavaReferenceType operationToJavaType(ROperation op) {
		RFeature feature;
		if (op.getPathTail().isEmpty()) {
			feature = (RFeature)op.getPathHead(); // TODO: this won't work when assigning to an alias
		} else {
			List<? extends RFeature> segments = op.getPathTail();
			feature = segments.get(segments.size() - 1);
		}
		return toJavaType(feature);
	}
	public JavaClass<?> operationToReferenceWithMetaType(ROperation op) {
		RFeature feature;
		if (op.getPathTail().isEmpty()) {
			feature = (RFeature)op.getPathHead(); // TODO: this won't work when assigning to an alias
		} else {
			List<? extends RFeature> segments = op.getPathTail();
			feature = segments.get(segments.size() - 1);
		}
		if (feature instanceof RAttribute) {
			return toJavaReferenceType(((RAttribute)feature).getRMetaAnnotatedType());
		} else if (feature instanceof RMetaAttribute) {
			return toJavaReferenceType(((RMetaAttribute)feature).getRType());
		} else {
			throw new UnsupportedOperationException("No JavaReferenceType exists for feature: " + feature.getName());
		}
	}
	
	private String getTypeDebugInfo(RType type) {
		return type.toString() + " (" + type.getClass().getSimpleName() + ")";
	}
	private String getTypeDebugInfo(RMetaAnnotatedType type) {
		return type.toString() + " (" + type.getClass().getSimpleName() + ")";
	}
	public JavaClass<?> toJavaReferenceType(RMetaAnnotatedType type) {
		JavaType jt = toJavaType(type);
		if (jt instanceof JavaPrimitiveType) {
			return ((JavaPrimitiveType)jt).toReferenceType();
		} else if (jt instanceof JavaClass<?>) {
			return (JavaClass<?>)jt;
		} else {
			throw new UnsupportedOperationException("Cannot convert type " + getTypeDebugInfo(type) + " to a Java reference type.");
		}
	}
	public JavaClass<?> toJavaReferenceType(RType type) {
		JavaType jt = toJavaType(type);
		if (jt instanceof JavaPrimitiveType) {
			return ((JavaPrimitiveType)jt).toReferenceType();
		} else if (jt instanceof JavaClass<?>) {
			return (JavaClass<?>)jt;
		} else {
			throw new UnsupportedOperationException("Cannot convert type " + getTypeDebugInfo(type) + " to a Java reference type.");
		}
	}
	public JavaPojoInterface toJavaReferenceType(RDataType type) {
		return toJavaType(type);
	}
	public RJavaEnum toJavaReferenceType(REnumType type) {
		return toJavaType(type);
	}
	public JavaClass<?> toJavaReferenceType(Optional<RType> type) {
		if (type.isPresent()) {
			return toJavaReferenceType(type.orElseThrow());
		}
		return typeUtil.OBJECT;
	}
	public JavaType toJavaType(RMetaAnnotatedType type) {
		JavaReferenceType javaType = toJavaReferenceType(type.getRType());
		
		if (type.hasAttributeMeta()) {
			RType rType = typeSystem.stripFromTypeAliases(type.getRType());
			JavaPackageName packageName = JavaPackageName.escape(metaField(rType.getNamespace()));
			return hasReferenceOrAddressMetadata(type) ? 
					new RJavaReferenceWithMeta(javaType, packageName, typeUtil):
						new RJavaFieldWithMeta(javaType, packageName, typeUtil);
		}
		return javaType;
	}
	private JavaType toJavaType(RType type) {
		return doSwitch(type, null);
	}
	public JavaPojoInterface toJavaType(RDataType type) {
		return caseDataType(type, null);
	}
	public RJavaEnum toJavaType(REnumType type) {
		return caseEnumType(type, null);
	}
	
	public JavaClass<?> toPolymorphicListOrSingleJavaType(RMetaAnnotatedType type, boolean isMany) {
		if (isMany) {
			return toPolymorphicList(toJavaReferenceType(type));
		} else
			return toJavaReferenceType(type);
	}
	
	public JavaClass<?> toPolymorphicListOrSingleJavaType(RType type, boolean isMany) {
		if (isMany) {
			return toPolymorphicList(toJavaReferenceType(type));
		} else
			return toJavaReferenceType(type);
	}
	
	public JavaClass<?> toListOrSingleJavaType(RMetaAnnotatedType type, boolean isMany) {
		if (isMany) {
			return typeUtil.wrap(typeUtil.LIST, toJavaReferenceType(type));
		} else
			return toJavaReferenceType(type);
	}
	
	public JavaClass<?> toListOrSingleJavaType(RType type, boolean isMany) {
		if (isMany) {
			return typeUtil.wrap(typeUtil.LIST, toJavaReferenceType(type));
		} else
			return toJavaReferenceType(type);
	}
	
	public RGeneratedJavaClass<? extends Validator<?>> toValidatorClass(JavaPojoInterface t) {
		return RGeneratedJavaClass.createImplementingInterface(JavaPackageName.escape(validation(t.getPackageName())), t.getSimpleName() + "Validator", JavaParameterizedType.from(typeUtil.VALIDATOR, t));
	}
	public RGeneratedJavaClass<? extends Validator<?>> toTypeFormatValidatorClass(JavaPojoInterface t) {
		return RGeneratedJavaClass.createImplementingInterface(JavaPackageName.escape(validation(t.getPackageName())), t.getSimpleName() + "TypeFormatValidator", JavaParameterizedType.from(typeUtil.VALIDATOR, t));
	}
	public RGeneratedJavaClass<? extends Validator<?>> toOnlyExistsValidatorClass(JavaPojoInterface t) {
		var argType = JavaParameterizedType.from(JavaGenericTypeDeclaration.from(new TypeReference<Set<?>>() {}), typeUtil.STRING);
		return RGeneratedJavaClass.createImplementingInterface(JavaPackageName.escape(existsValidation(t.getPackageName())), t.getSimpleName() + "OnlyExistsValidator", JavaParameterizedType.from(typeUtil.VALIDATOR_WITH_ARG, t, argType));
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
	
	public RJavaPojoInterface createPojoInterface(RDataType type) {
		return rJavaPojoInterfaceCache.get(type, () -> new RJavaPojoInterface(type, this, typeUtil, generatorUtil));
	}
	
	@Override
	protected JavaPojoInterface caseDataType(RDataType type, Void context) {
		return createPojoInterface(type);
	}
	@Override
	protected JavaPojoInterface caseChoiceType(RChoiceType type, Void context) {
		return caseDataType(type.asRDataType(), context);
	}
	@Override
	protected RJavaEnum caseEnumType(REnumType type, Void context) {
		return new RJavaEnum(type);
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
	
	private boolean hasReferenceOrAddressMetadata(RMetaAnnotatedType rMetaAnnotatedType) {
		return rMetaAnnotatedType.getMetaAttributes()
				.stream()
				.anyMatch(a -> a.getName().equals("reference") || a.getName().equals("address"));
	}
}
