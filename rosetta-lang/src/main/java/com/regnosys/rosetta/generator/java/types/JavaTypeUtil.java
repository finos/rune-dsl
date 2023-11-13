package com.regnosys.rosetta.generator.java.types;

import java.math.BigDecimal;
import java.math.BigInteger;
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
import com.rosetta.util.types.JavaInterface;
import com.rosetta.util.types.JavaParameterizedType;
import com.rosetta.util.types.JavaPrimitiveType;
import com.rosetta.util.types.JavaReferenceType;
import com.rosetta.util.types.JavaType;
import com.rosetta.util.types.JavaTypeArgument;
import com.rosetta.util.types.JavaWildcardTypeArgument;

public class JavaTypeUtil {
	@Inject
	private RosettaTypeProvider typeProvider;
	@Inject
	private JavaTypeTranslator typeTranslator;
	
	public final JavaClass NUMBER = JavaClass.from(Number.class);
	public final JavaClass INTEGER = JavaPrimitiveType.INT.toReferenceType();
	public final JavaClass LONG = JavaPrimitiveType.LONG.toReferenceType();
	public final JavaClass BIG_INTEGER = JavaClass.from(BigInteger.class);
	public final JavaClass BIG_DECIMAL = JavaClass.from(BigDecimal.class);
	public final JavaClass BOOLEAN = JavaPrimitiveType.BOOLEAN.toReferenceType();
	public final JavaClass STRING = JavaClass.from(String.class);
	public final JavaClass VOID = JavaPrimitiveType.VOID.toReferenceType();
	
	public final JavaClass LIST = JavaClass.from(List.class);
	public final JavaInterface MAPPER = JavaInterface.from(Mapper.class);
	public final JavaClass MAPPER_S = JavaClass.from(MapperS.class);
	public final JavaClass MAPPER_C = JavaClass.from(MapperC.class);
	public final JavaClass COMPARISON_RESULT = JavaClass.from(ComparisonResult.class);
	public final JavaClass MAPPER_LIST_OF_LISTS = JavaClass.from(MapperListOfLists.class);

	public JavaParameterizedType wrap(JavaClass wrapperType, JavaType itemType) {
		return new JavaParameterizedType(wrapperType, itemType.toReferenceType());
	}
	public JavaParameterizedType wrap(Class<?> wrapperType, JavaType itemType) {
		return wrap(JavaClass.from(wrapperType), itemType);
	}
	public JavaParameterizedType wrap(Class<?> wrapperType, RosettaExpression item) {
		return wrap(wrapperType, typeTranslator.toJavaReferenceType(typeProvider.getRType(item)));
	}
	
	public JavaParameterizedType wrapExtends(JavaClass wrapperType, JavaType itemType) {
		return new JavaParameterizedType(wrapperType, JavaWildcardTypeArgument.extendsBound(itemType.toReferenceType()));
	}
	public JavaParameterizedType wrapExtends(Class<?> wrapperType, JavaType itemType) {
		return wrapExtends(JavaClass.from(wrapperType), itemType);
	}
	public JavaParameterizedType wrapExtends(Class<?> wrapperType, RosettaExpression item) {
		return wrapExtends(wrapperType, typeTranslator.toJavaReferenceType(typeProvider.getRType(item)));
	}
	
	public JavaParameterizedType wrapExtendsIfNotPrimitive(JavaClass wrapperType, JavaType itemType) {
		if (itemType instanceof JavaPrimitiveType || itemType instanceof JavaClass && JavaPrimitiveType.fromWrapper((JavaClass)itemType) != null) {
			return wrapExtends(wrapperType, itemType);
		} else {
			return wrap(wrapperType, itemType);
		}
	}
	public JavaParameterizedType wrapExtendsIfNotPrimitive(Class<?> wrapperType, JavaType itemType) {
		return wrapExtendsIfNotPrimitive(JavaClass.from(wrapperType), itemType);
	}
	public JavaParameterizedType wrapExtendsIfNotPrimitive(Class<?> wrapperType, RosettaExpression item) {
		return wrapExtendsIfNotPrimitive(wrapperType, typeTranslator.toJavaReferenceType(typeProvider.getRType(item)));
	}
	
	public boolean hasWildcardArgument(JavaType t) {
		return t instanceof JavaParameterizedType && ((JavaParameterizedType) t).getArguments().get(0) instanceof JavaWildcardTypeArgument;
	}
	
	public JavaType getItemType(JavaType t) {
		if (isWrapper(t)) {
			if (isComparisonResult(t)) {
				return BOOLEAN;
			} else {
				JavaTypeArgument arg = ((JavaParameterizedType)t).getArguments().get(0);
				if (arg instanceof JavaWildcardTypeArgument) {
					return ((JavaWildcardTypeArgument)arg).getBound().orElseThrow();
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
				JavaClass base = ((JavaParameterizedType)t).getBaseType();
				return wrap(base, newItemType);
			}
		}
		return newItemType;
	}
	
	public boolean extendsNumber(JavaType t) {
		return NUMBER.isAssignableFrom(t);
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
			return LIST.equals(((JavaParameterizedType) t).getBaseType());
		}
		return LIST.equals(t);
	}
	
	public boolean extendsMapper(JavaType t) {
		if (MAPPER.isAssignableFrom(t)) {
			return true;
		} else if (t instanceof JavaParameterizedType) {
			return MAPPER.isAssignableFrom(((JavaParameterizedType) t).getBaseType());
		}
		return false;
	}
	public boolean isMapper(JavaType t) {
		if (t instanceof JavaParameterizedType) {
			return MAPPER.equals(((JavaParameterizedType) t).getBaseType());
		}
		return MAPPER.equals(t);
	}
	public boolean isMapperS(JavaType t) {
		if (t instanceof JavaParameterizedType) {
			return MAPPER_S.equals(((JavaParameterizedType) t).getBaseType());
		}
		return MAPPER_S.equals(t);
	}
	public boolean isMapperC(JavaType t) {
		if (t instanceof JavaParameterizedType) {
			return MAPPER_C.equals(((JavaParameterizedType) t).getBaseType());
		}
		return MAPPER_C.equals(t);
	}
	public boolean isComparisonResult(JavaType t) {
		return COMPARISON_RESULT.equals(t);
	}
	public boolean isMapperListOfLists(JavaType t) {
		if (t instanceof JavaParameterizedType) {
			return MAPPER_LIST_OF_LISTS.equals(((JavaParameterizedType) t).getBaseType());
		}
		return MAPPER_LIST_OF_LISTS.equals(t);
	}
	
	public boolean isWrapper(JavaType t) {
		return isList(t) || extendsMapper(t) || isMapperListOfLists(t);
	}
	
	public JavaType join(JavaType t1, JavaType t2) {
		if (t1.equals(t2)) {
			return t1;
		} else if (t1.toReferenceType().equals(t2.toReferenceType())) {
			return t1.toReferenceType();
		}
		
		if (JavaPrimitiveType.VOID.equals(t1)) {
			return t2;
		} else if (JavaPrimitiveType.VOID.equals(t2)) {
			return t1;
		} else if (VOID.equals(t1)) {
			return t2.toReferenceType();
		} else if (VOID.equals(t2)) {
			return t1.toReferenceType();
		}
		
		if (t1 instanceof JavaClass && ((JavaClass) t1).isAssignableFrom(t2)) {
			return t1;
		} else if (t2 instanceof JavaClass && ((JavaClass) t2).isAssignableFrom(t1)) {
			return t2;
		}
		
		throw new IllegalArgumentException("Joining Java types `" + t1 + "` and `" + t2 + "` is not supported yet.");
	}
}
