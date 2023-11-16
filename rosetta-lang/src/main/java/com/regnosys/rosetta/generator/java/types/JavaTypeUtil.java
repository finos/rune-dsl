package com.regnosys.rosetta.generator.java.types;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;

import javax.inject.Inject;

import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.types.RosettaTypeProvider;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.mapper.Mapper;
import com.rosetta.model.lib.mapper.MapperC;
import com.rosetta.model.lib.mapper.MapperListOfLists;
import com.rosetta.model.lib.mapper.MapperS;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaGenericTypeDeclaration;
import com.rosetta.util.types.JavaParameterizedType;
import com.rosetta.util.types.JavaReferenceType;
import com.rosetta.util.types.JavaType;
import com.rosetta.util.types.JavaTypeArgument;
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
	
	public final JavaClass<Object> OBJECT = JavaClass.OBJECT;
	public final JavaClass<Cloneable> CLONEABLE = JavaClass.CLONEABLE;
	public final JavaClass<Serializable> SERIALIZABLE = JavaClass.SERIALIZABLE;
	
	@SuppressWarnings("rawtypes")
	public final JavaClass<List> LIST = JavaClass.from(List.class);
	@SuppressWarnings("rawtypes")
	public final JavaClass<Mapper> MAPPER = JavaClass.from(Mapper.class);
	@SuppressWarnings("rawtypes")
	public final JavaClass<MapperS> MAPPER_S = JavaClass.from(MapperS.class);
	@SuppressWarnings("rawtypes")
	public final JavaClass<MapperC> MAPPER_C = JavaClass.from(MapperC.class);
	public final JavaClass<ComparisonResult> COMPARISON_RESULT = JavaClass.from(ComparisonResult.class);
	@SuppressWarnings("rawtypes")
	public final JavaClass<MapperListOfLists> MAPPER_LIST_OF_LISTS = JavaClass.from(MapperListOfLists.class);

	public <T> JavaParameterizedType<T> wrap(Class<T> wrapperType, JavaType itemType) {
		return JavaParameterizedType.from(wrapperType, itemType.toReferenceType());
	}
	public <T> JavaParameterizedType<T> wrap(Class<T> wrapperType, Class<?> itemType) {
		return wrap(wrapperType, JavaType.from(itemType));
	}
	public <T> JavaParameterizedType<T> wrap(Class<T> wrapperType, RosettaExpression item) {
		return wrap(wrapperType, typeTranslator.toJavaReferenceType(typeProvider.getRType(item)));
	}
	
	public <T> JavaParameterizedType<T> wrapExtends(Class<T> wrapperType, JavaType itemType) {
		return JavaParameterizedType.from(wrapperType, JavaWildcardTypeArgument.extendsBound(itemType.toReferenceType()));
	}
	public <T> JavaParameterizedType<T> wrapExtends(Class<T> wrapperType, Class<?> itemType) {
		return wrapExtends(wrapperType, JavaType.from(itemType));
	}
	public <T> JavaParameterizedType<T> wrapExtends(Class<T> wrapperType, RosettaExpression item) {
		return wrapExtends(wrapperType, typeTranslator.toJavaReferenceType(typeProvider.getRType(item)));
	}
	
	public <T> JavaParameterizedType<T> wrapExtendsIfNotFinal(Class<T> wrapperType, JavaType itemType) {
		if (itemType instanceof RJavaPojoInterface) {
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
	public <T> JavaParameterizedType<T> wrapExtendsIfNotFinal(Class<T> wrapperType, Class<?> itemType) {
		return wrapExtendsIfNotFinal(wrapperType, JavaType.from(itemType));
	}
	public <T> JavaParameterizedType<T> wrapExtendsIfNotFinal(Class<T> wrapperType, RosettaExpression item) {
		return wrapExtendsIfNotFinal(wrapperType, typeTranslator.toJavaReferenceType(typeProvider.getRType(item)));
	}
	
	public boolean hasWildcardArgument(JavaType t) {
		return t instanceof JavaParameterizedType && ((JavaParameterizedType<?>) t).getArguments().get(0) instanceof JavaWildcardTypeArgument;
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
			return LIST.equals(((JavaParameterizedType<?>) t).getGenericTypeDeclaration().getBaseType());
		}
		return LIST.equals(t);
	}
	
	public boolean extendsMapper(JavaType t) {
		return t.isSubtypeOf(MAPPER);
	}
	public boolean isMapper(JavaType t) {
		if (t instanceof JavaParameterizedType) {
			return MAPPER.equals(((JavaParameterizedType<?>) t).getGenericTypeDeclaration().getBaseType());
		}
		return MAPPER.equals(t);
	}
	public boolean isMapperS(JavaType t) {
		if (t instanceof JavaParameterizedType) {
			return MAPPER_S.equals(((JavaParameterizedType<?>) t).getGenericTypeDeclaration().getBaseType());
		}
		return MAPPER_S.equals(t);
	}
	public boolean isMapperC(JavaType t) {
		if (t instanceof JavaParameterizedType) {
			return MAPPER_C.equals(((JavaParameterizedType<?>) t).getGenericTypeDeclaration().getBaseType());
		}
		return MAPPER_C.equals(t);
	}
	public boolean isComparisonResult(JavaType t) {
		return COMPARISON_RESULT.equals(t);
	}
	public boolean isMapperListOfLists(JavaType t) {
		if (t instanceof JavaParameterizedType) {
			return MAPPER_LIST_OF_LISTS.equals(((JavaParameterizedType<?>) t).getGenericTypeDeclaration().getBaseType());
		}
		return MAPPER_LIST_OF_LISTS.equals(t);
	}
	
	public boolean isWrapper(JavaType t) {
		return t != JavaReferenceType.NULL_TYPE && (isList(t) || extendsMapper(t) || isMapperListOfLists(t));
	}
	
	public JavaType join(JavaType left, JavaType right) {
		return typeJoiner.visitTypes(left, right);
	}
}
