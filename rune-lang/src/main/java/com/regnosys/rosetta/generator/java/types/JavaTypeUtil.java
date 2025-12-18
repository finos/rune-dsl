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

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import com.rosetta.model.lib.context.RuneContextFactory;
import com.rosetta.model.lib.context.RuneScope;

import jakarta.inject.Inject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.types.RosettaTypeProvider;
import com.rosetta.model.lib.GlobalKey;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.Templatable;
import com.rosetta.model.lib.context.RuneContext;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.mapper.Mapper;
import com.rosetta.model.lib.mapper.MapperC;
import com.rosetta.model.lib.mapper.MapperListOfLists;
import com.rosetta.model.lib.mapper.MapperS;
import com.rosetta.model.lib.meta.FieldWithMeta;
import com.rosetta.model.lib.meta.Reference;
import com.rosetta.model.lib.meta.ReferenceWithMeta;
import com.rosetta.model.lib.meta.RosettaMetaData;
import com.rosetta.model.lib.validation.Validator;
import com.rosetta.model.lib.validation.ValidatorWithArg;
import com.rosetta.model.metafields.MetaAndTemplateFields;
import com.rosetta.model.metafields.MetaFields;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaGenericTypeDeclaration;
import com.rosetta.util.types.JavaParameterizedType;
import com.rosetta.util.types.JavaReferenceType;
import com.rosetta.util.types.JavaType;
import com.rosetta.util.types.JavaTypeArgument;
import com.rosetta.util.types.JavaTypeDeclaration;
import com.rosetta.util.types.JavaWildcardTypeArgument;

public class JavaTypeUtil {
	@Inject
	private RosettaTypeProvider typeProvider;
	@Inject
	private JavaTypeTranslator typeTranslator;
	@Inject
	private JavaTypeJoiner typeJoiner;
	
	public final JavaClass<Number> NUMBER = JavaClass.from(Number.class);
	public final JavaClass<Integer> INTEGER = JavaClass.from(Integer.class);
	public final JavaClass<Long> LONG = JavaClass.from(Long.class);
	public final JavaClass<BigInteger> BIG_INTEGER = JavaClass.from(BigInteger.class);
	public final JavaClass<BigDecimal> BIG_DECIMAL = JavaClass.from(BigDecimal.class);
	public final JavaClass<Boolean> BOOLEAN = JavaClass.from(Boolean.class);
	public final JavaClass<String> STRING = JavaClass.from(String.class);
	public final JavaClass<Void> VOID = JavaClass.from(Void.class);
	public final JavaClass<LocalTime> LOCAL_TIME = JavaClass.from(LocalTime.class);
	public final JavaClass<com.rosetta.model.lib.records.Date> DATE = JavaClass.from(com.rosetta.model.lib.records.Date.class);
	public final JavaClass<LocalDateTime> LOCAL_DATE_TIME = JavaClass.from(LocalDateTime.class);
	public final JavaClass<ZonedDateTime> ZONED_DATE_TIME = JavaClass.from(ZonedDateTime.class);
	
	public final JavaClass<GlobalKey> GLOBAL_KEY = JavaClass.from(GlobalKey.class);
	public final JavaClass<GlobalKey.GlobalKeyBuilder> GLOBAL_KEY_BUILDER = JavaClass.from(GlobalKey.GlobalKeyBuilder.class);
	public final JavaClass<Templatable> TEMPLATABLE = JavaClass.from(Templatable.class);
	public final JavaClass<Templatable.TemplatableBuilder> TEMPLATABLE_BUILDER = JavaClass.from(Templatable.TemplatableBuilder.class);
	public final JavaClass<Reference> REFERENCE = JavaClass.from(Reference.class);
	public final JavaClass<Reference.ReferenceBuilder> REFERENCE_BUILDER = JavaClass.from(Reference.ReferenceBuilder.class);
	public final JavaClass<MetaFields> META_FIELDS = JavaClass.from(MetaFields.class);
	public final JavaClass<MetaFields.MetaFieldsBuilder> META_FIELDS_BUILDER = JavaClass.from(MetaFields.MetaFieldsBuilder.class);
	public final JavaClass<MetaAndTemplateFields> META_AND_TEMPLATE_FIELDS = JavaClass.from(MetaAndTemplateFields.class);
	public final JavaClass<MetaAndTemplateFields.MetaAndTemplateFieldsBuilder> META_AND_TEMPLATE_FIELDS_BUILDER = JavaClass.from(MetaAndTemplateFields.MetaAndTemplateFieldsBuilder.class);
	public final JavaGenericTypeDeclaration<FieldWithMeta<?>> FIELD_WITH_META = JavaGenericTypeDeclaration.from(new TypeReference<>() {});
	public final JavaGenericTypeDeclaration<FieldWithMeta.FieldWithMetaBuilder<?>> FIELD_WITH_META_BUILDER = JavaGenericTypeDeclaration.from(new TypeReference<>() {});
	public final JavaGenericTypeDeclaration<ReferenceWithMeta<?>> REFERENCE_WITH_META = JavaGenericTypeDeclaration.from(new TypeReference<>() {});
	public final JavaGenericTypeDeclaration<ReferenceWithMeta.ReferenceWithMetaBuilder<?>> REFERENCE_WITH_META_BUILDER = JavaGenericTypeDeclaration.from(new TypeReference<>() {});
	
	public final JavaClass<Object> OBJECT = JavaClass.OBJECT;
	public final JavaClass<Cloneable> CLONEABLE = JavaClass.CLONEABLE;
	public final JavaClass<Serializable> SERIALIZABLE = JavaClass.SERIALIZABLE;
	
	public final JavaGenericTypeDeclaration<List<?>> LIST = JavaGenericTypeDeclaration.from(new TypeReference<>() {});
	public final JavaGenericTypeDeclaration<Mapper<?>> MAPPER = JavaGenericTypeDeclaration.from(new TypeReference<>() {});
	public final JavaGenericTypeDeclaration<MapperS<?>> MAPPER_S = JavaGenericTypeDeclaration.from(new TypeReference<>() {});
	public final JavaGenericTypeDeclaration<MapperC<?>> MAPPER_C = JavaGenericTypeDeclaration.from(new TypeReference<>() {});
	public final JavaClass<ComparisonResult> COMPARISON_RESULT = JavaClass.from(ComparisonResult.class);
	public final JavaGenericTypeDeclaration<MapperListOfLists<?>> MAPPER_LIST_OF_LISTS = JavaGenericTypeDeclaration.from(new TypeReference<>() {});
	
	public final JavaClass<RosettaModelObject> ROSETTA_MODEL_OBJECT = JavaClass.from(RosettaModelObject.class);
	public final JavaClass<RosettaModelObjectBuilder> ROSETTA_MODEL_OBJECT_BUILDER = JavaClass.from(RosettaModelObjectBuilder.class);
	public final JavaGenericTypeDeclaration<RosettaMetaData<?>> ROSETTA_META_DATA = JavaGenericTypeDeclaration.from(new TypeReference<>() {});
	public final JavaGenericTypeDeclaration<Validator<?>> VALIDATOR = JavaGenericTypeDeclaration.from(new TypeReference<>() {});
	public final JavaGenericTypeDeclaration<ValidatorWithArg<?, ?>> VALIDATOR_WITH_ARG = JavaGenericTypeDeclaration.from(new TypeReference<>() {});
	
    public final JavaClass<RuneContext> RUNE_CONTEXT = JavaClass.from(RuneContext.class);
    public final JavaClass<RuneContextFactory> RUNE_CONTEXT_FACTORY = JavaClass.from(RuneContextFactory.class);
    public final JavaClass<RuneScope> RUNE_SCOPE = JavaClass.from(RuneScope.class);
	
	private final Map<JavaTypeDeclaration<?>, JavaTypeDeclaration<?>> builderMap = Map.of(
				ROSETTA_MODEL_OBJECT, ROSETTA_MODEL_OBJECT_BUILDER,
				GLOBAL_KEY, GLOBAL_KEY_BUILDER,
				TEMPLATABLE, TEMPLATABLE_BUILDER,
				REFERENCE, REFERENCE_BUILDER,
				META_FIELDS, META_FIELDS_BUILDER,
				META_AND_TEMPLATE_FIELDS, META_AND_TEMPLATE_FIELDS_BUILDER,
				FIELD_WITH_META, FIELD_WITH_META_BUILDER,
				REFERENCE_WITH_META, REFERENCE_WITH_META_BUILDER
			);
	public boolean hasBuilderType(JavaType type) {
		return !toBuilder(type).equals(type);
	}
	public JavaType toBuilder(JavaType type) {
		if (type instanceof JavaClass<?> c) {
			return toBuilder(c);
		}
		// No builder type found
		return type;
	}
	public JavaClass<?> toBuilder(JavaClass<?> type) {
		return (JavaClass<?>)toBuilder((JavaTypeDeclaration<?>) type);
	}
	public JavaTypeDeclaration<?> toBuilder(JavaTypeDeclaration<?> type) {
		if (type instanceof JavaPojoInterface pojo) {
			return pojo.toBuilderInterface();
		}
		
		JavaTypeDeclaration<?> base = type;
		if (type instanceof JavaParameterizedType<?> paramType) {
			base = paramType.getGenericTypeDeclaration();
		}
		
		var baseBuilder = builderMap.get(base);
		if (baseBuilder == null) {
			// No builder type found
			return type;
		}
		
		if (type instanceof JavaParameterizedType<?> paramType) {
			return JavaParameterizedType.from((JavaGenericTypeDeclaration<?>)baseBuilder, paramType.getArguments());
		}
		return baseBuilder;
	}

	public <T> JavaParameterizedType<T> wrap(JavaGenericTypeDeclaration<T> wrapperType, JavaType itemType) {
		return JavaParameterizedType.from(wrapperType, itemType.toReferenceType());
	}
	public <T> JavaParameterizedType<T> wrap(JavaGenericTypeDeclaration<T> wrapperType, Class<?> itemType) {
		return wrap(wrapperType, JavaType.from(itemType));
	}
	public <T> JavaParameterizedType<T> wrap(JavaGenericTypeDeclaration<T> wrapperType, RosettaExpression item) {
		return wrap(wrapperType, typeTranslator.toJavaReferenceType(typeProvider.getRMetaAnnotatedType(item)));
	}
	public <T> JavaParameterizedType<T> wrapExtendsWithoutMeta(JavaGenericTypeDeclaration<T> wrapperType, JavaType itemType) {
		if (itemType instanceof RJavaWithMetaValue) {
			RJavaWithMetaValue metaItemType = (RJavaWithMetaValue) itemType;
			return JavaParameterizedType.from(wrapperType, JavaWildcardTypeArgument.extendsBound(metaItemType.getValueType()));
		}
		return JavaParameterizedType.from(wrapperType, JavaWildcardTypeArgument.extendsBound(itemType.toReferenceType()));
	}
	public <T> JavaParameterizedType<T> wrapExtends(JavaGenericTypeDeclaration<T> wrapperType, JavaType itemType) {
		return JavaParameterizedType.from(wrapperType, JavaWildcardTypeArgument.extendsBound(itemType.toReferenceType()));
	}
	public <T> JavaParameterizedType<T> wrapExtends(JavaGenericTypeDeclaration<T> wrapperType, Class<?> itemType) {
		return wrapExtends(wrapperType, JavaType.from(itemType));
	}
	public <T> JavaParameterizedType<T> wrapExtendsWithoutMeta(JavaGenericTypeDeclaration<T> wrapperType, RosettaExpression item) {
		return wrapExtends(wrapperType, typeTranslator.toJavaReferenceType(typeProvider.getRMetaAnnotatedType(item).getRType()));
	}
	
	public <T> JavaParameterizedType<T> wrapExtends(JavaGenericTypeDeclaration<T> wrapperType, RosettaExpression item) {
		return wrapExtends(wrapperType, typeTranslator.toJavaReferenceType(typeProvider.getRMetaAnnotatedType(item)));
	}
	
	public <T> JavaParameterizedType<T> wrapExtendsIfNotFinal(JavaGenericTypeDeclaration<T> wrapperType, JavaType itemType) {
		if (itemType instanceof JavaPojoInterface) {
			return wrapExtends(wrapperType, itemType);
		} else {
			return wrap(wrapperType, itemType);
		}
		// TODO: change this back
//		if (!(itemType instanceof JavaClass) || ((JavaClass<?>)itemType).isFinal()) {
//			return wrap(wrapperType, itemType);
//		} else {
//			return wrapExtends(wrapperType, itemType);
//		}
	}
	public <T> JavaParameterizedType<T> wrapExtendsIfNotFinal(JavaGenericTypeDeclaration<T> wrapperType, Class<?> itemType) {
		return wrapExtendsIfNotFinal(wrapperType, JavaType.from(itemType));
	}
	public <T> JavaParameterizedType<T> wrapExtendsIfNotFinal(JavaGenericTypeDeclaration<T> wrapperType, RosettaExpression item) {
		return wrapExtendsIfNotFinal(wrapperType, typeTranslator.toJavaReferenceType(typeProvider.getRMetaAnnotatedType(item)));
	}
	
	public boolean hasWildcardArgument(JavaType t) {
		return t instanceof JavaParameterizedType && ((JavaParameterizedType<?>) t).getArguments().get(0) instanceof JavaWildcardTypeArgument;
	}
	public JavaType getItemValueType(JavaType t) {
		JavaType itemType = getItemType(t);
		if (itemType instanceof RJavaWithMetaValue) {
			return ((RJavaWithMetaValue) itemType).valueType;
		}
		return itemType;
	}
	
	public JavaType getItemType(JavaType t) {
		if (isWrapper(t)) {
			if (isComparisonResult(t)) {
				return BOOLEAN;
			} else {
				JavaTypeArgument arg = ((JavaParameterizedType<?>)t).getArguments().get(0);
				if (arg instanceof JavaWildcardTypeArgument) {
					return ((JavaWildcardTypeArgument)arg).getBound().orElse(OBJECT);
				}
				return (JavaReferenceType) arg;
			}
		}
		return t;
	}
	public JavaType changeItemType(JavaType t, JavaType newItemType) {
		if (isWrapper(t)) {
			if (isComparisonResult(t)) {
				return t;
			} else {
				JavaGenericTypeDeclaration<?> base = ((JavaParameterizedType<?>)t).getGenericTypeDeclaration();
				return JavaParameterizedType.from(base, newItemType.toReferenceType());
			}
		}
		return newItemType;
	}
	public boolean extendsNumber(JavaType t) {
		return t.isSubtypeOf(NUMBER);
	}
	public boolean isInteger(JavaType t) {
		return INTEGER.equals(t);
	}
	public boolean isLong(JavaType t) {
		return LONG.equals(t);
	}
	public boolean isBigInteger(JavaType t) {
		return BIG_INTEGER.equals(t);
	}
	public boolean isBigDecimal(JavaType t) {
		return BIG_DECIMAL.equals(t);
	}
	public boolean isBoolean(JavaType t) {
		return BOOLEAN.equals(t);
	}
	public boolean isVoid(JavaType t) {
		return VOID.equals(t);
	}
	
	public boolean isList(JavaType t) {
		if (t instanceof JavaParameterizedType) {
			return LIST.equals(((JavaParameterizedType<?>) t).getGenericTypeDeclaration());
		}
		return LIST.equals(t);
	}
	
	public boolean extendsMapper(JavaType t) {
		if (t instanceof JavaParameterizedType) {
			return ((JavaParameterizedType<?>) t).getGenericTypeDeclaration().extendsDeclaration(MAPPER);
		} else if (t instanceof JavaClass) {
			return ((JavaClass<?>) t).extendsDeclaration(MAPPER);
		}
		return false;
	}
	public boolean isMapper(JavaType t) {
		if (t instanceof JavaParameterizedType) {
			return MAPPER.equals(((JavaParameterizedType<?>) t).getGenericTypeDeclaration());
		}
		return false;
	}
	public boolean isMapperS(JavaType t) {
		if (t instanceof JavaParameterizedType) {
			return MAPPER_S.equals(((JavaParameterizedType<?>) t).getGenericTypeDeclaration());
		}
		return false;
	}
	public boolean isMapperC(JavaType t) {
		if (t instanceof JavaParameterizedType) {
			return MAPPER_C.equals(((JavaParameterizedType<?>) t).getGenericTypeDeclaration());
		}
		return false;
	}
	public boolean isComparisonResult(JavaType t) {
		return COMPARISON_RESULT.equals(t);
	}
	public boolean isMapperListOfLists(JavaType t) {
		if (t instanceof JavaParameterizedType) {
			return MAPPER_LIST_OF_LISTS.equals(((JavaParameterizedType<?>) t).getGenericTypeDeclaration());
		}
		return false;
	}
	
	public boolean isWrapper(JavaType t) {
		return t != JavaReferenceType.NULL_TYPE && (isList(t) || extendsMapper(t) || isMapperListOfLists(t));
	}
	
	public JavaType join(JavaType left, JavaType right) {
		return typeJoiner.visitTypes(left, right);
	}
}
