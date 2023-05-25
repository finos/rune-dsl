package com.regnosys.rosetta.generator.java.util

import org.eclipse.xtext.generator.IFileSystemAccess2

/**
 * Generator provides backward compatibility for models stuck on earlier model versions.
 */
class BackwardCompatibilityGenerator {
	
	def generate(IFileSystemAccess2 fsa) {
		fsa.generateFile('com/rosetta/model/lib/meta/Key.java',
			'''
			package com.rosetta.model.lib.meta;
			
			import com.rosetta.model.lib.RosettaModelObject;
			import com.rosetta.model.lib.RosettaModelObjectBuilder;
			import com.rosetta.model.lib.path.RosettaPath;
			import com.rosetta.model.lib.process.BuilderMerger;
			import com.rosetta.model.lib.process.BuilderProcessor;
			import com.rosetta.model.lib.process.Processor;
			import com.rosetta.model.lib.qualify.QualifyFunctionFactory;
			import com.rosetta.model.lib.qualify.QualifyResult;
			import com.rosetta.model.lib.validation.ValidationResult;
			import com.rosetta.model.lib.validation.Validator;
			import com.rosetta.model.lib.validation.ValidatorFactory;
			import com.rosetta.model.lib.validation.ValidatorWithArg;
			import java.util.Collections;
			import java.util.List;
			import java.util.Set;
			import java.util.function.Function;
			import com.rosetta.model.lib.validation.ValidationResult.ValidationType;
			
			/**
			 * @author TomForwood
			 * This class represents a value that can be references elsewhere to link to the object the key is associated with
			 * The keyValue is required to be unique within the scope defined by "scope"
			 * 
			 * Scope can be 
			 *  - global - the key must be universally unique
			 * 	- document - the key must be unique in this document
			 *  - the name of the rosetta class e.g. TradeableProduct- the object bearing this key is inside a TradeableProduct and the key is only unique inside that TradeableProduct
			 */
			public interface Key extends RosettaModelObject{
			
				public String getScope();
				public String getKeyValue();
				
				Key build();
				KeyBuilder toBuilder();
				
				final static KeyMeta meta = new KeyMeta();
				@Override
				default RosettaMetaData<? extends RosettaModelObject> metaData() {
					return meta;
				}
				
				default Class<? extends RosettaModelObject> getType() {
					return Key.class;
				}
				
				default void process(RosettaPath path, Processor processor) {
				}
				
				static KeyBuilder builder() {
					return new KeyBuilderImpl();
				}
				
				interface KeyBuilder extends Key, RosettaModelObjectBuilder {
					KeyBuilder setScope(String scope);
					KeyBuilder setKeyValue(String keyValue);
					
					default void process(RosettaPath path, BuilderProcessor processor) {
					}
				}
				
				class KeyImpl implements Key {
					
					private final String scope;
					private final String keyValue;
					public KeyImpl(KeyBuilder builder) {
						super();
						this.scope = builder.getScope();
						this.keyValue = builder.getKeyValue();
					}
					public String getScope() {
						return scope;
					}
					public String getKeyValue() {
						return keyValue;
					}
				
					public KeyBuilder toBuilder() {
						KeyBuilder key = builder();
						key.setKeyValue(keyValue);
						key.setScope(scope);
						return key;
					}
					
					public Key build() {
						return this;
					}
					@Override
					public int hashCode() {
						final int prime = 31;
						int result = 1;
						result = prime * result + ((keyValue == null) ? 0 : keyValue.hashCode());
						result = prime * result + ((scope == null) ? 0 : scope.hashCode());
						return result;
					}
					@Override
					public boolean equals(Object obj) {
						if (this == obj)
							return true;
						if (obj == null)
							return false;
						if (getClass() != obj.getClass())
							return false;
						KeyImpl other = (KeyImpl) obj;
						if (keyValue == null) {
							if (other.keyValue != null)
								return false;
						} else if (!keyValue.equals(other.keyValue))
							return false;
						if (scope == null) {
							if (other.scope != null)
								return false;
						} else if (!scope.equals(other.scope))
							return false;
						return true;
					}
				}
				
				public static class KeyBuilderImpl implements KeyBuilder{
					private String scope;
					private String keyValue;
					
					public Key build() {
						return new KeyImpl(this);
					}
			
					public String getScope() {
						return scope;
					}
			
					public KeyBuilder setScope(String scope) {
						this.scope = scope;
						return this;
					}
			
					public String getKeyValue() {
						return keyValue;
					}
			
					public KeyBuilder setKeyValue(String keyValue) {
						this.keyValue = keyValue;
						return this;
					}
					
					public boolean hasData() {
						return keyValue!=null;
					}
			
					@Override
					public KeyBuilder toBuilder() {
						return this;
					}
			
					@SuppressWarnings("unchecked")
					@Override
					public KeyBuilder prune() {
						return this;
					}
			
					@SuppressWarnings("unchecked")
					@Override
					public KeyBuilder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
						KeyBuilder otherKey = (KeyBuilder) other;
						merger.mergeBasic(getKeyValue(), otherKey.getKeyValue(), this::setKeyValue);
						merger.mergeBasic(getScope(), otherKey.getScope(), this::setScope);
						return this;
					}
			
					@Override
					public int hashCode() {
						final int prime = 31;
						int result = 1;
						result = prime * result + ((keyValue == null) ? 0 : keyValue.hashCode());
						result = prime * result + ((scope == null) ? 0 : scope.hashCode());
						return result;
					}
			
					@Override
					public boolean equals(Object obj) {
						if (this == obj)
							return true;
						if (obj == null)
							return false;
						if (getClass() != obj.getClass())
							return false;
						KeyBuilderImpl other = (KeyBuilderImpl) obj;
						if (keyValue == null) {
							if (other.keyValue != null)
								return false;
						} else if (!keyValue.equals(other.keyValue))
							return false;
						if (scope == null) {
							if (other.scope != null)
								return false;
						} else if (!scope.equals(other.scope))
							return false;
						return true;
					}
				}
				
				class KeyMeta implements RosettaMetaData<Key> {
			
			
					@Override
					public List<Validator<? super Key>> dataRules(ValidatorFactory factory) {
						return Collections.emptyList();
					}
			
					@Override
					public List<Validator<? super Key>> choiceRuleValidators() {
						return Collections.emptyList();
					}
			
					@Override
					public List<Function<? super Key, QualifyResult>> getQualifyFunctions(QualifyFunctionFactory factory) {
						return Collections.emptyList();
					}
			
					@Override
					public Validator<? super Key> validator() {
						return new Validator<Key>() {
			
							@Override
							public ValidationResult<Key> validate(RosettaPath path, Key key) {
								if (key.getKeyValue()==null) {
									return ValidationResult.failure("Key.value", ValidationType.KEY, "Key", path, "", "Key value must be set");
								}
								if (key.getScope()==null) {
									return ValidationResult.failure("Key.scope", ValidationType.KEY, "Key", path, "", "Key scope must be set");
								}
								return ValidationResult.success("Key", ValidationType.KEY, "Key", path, "");
							}
						};
					}
					
					@Override
					public Validator<? super Key> typeFormatValidator() {
						return null;
					}
			
					@Override
					public ValidatorWithArg<? super Key, Set<String>> onlyExistsValidator() {
						return null;
					}
				}
			}
			'''
		)
		
		fsa.generateFile('com/rosetta/model/lib/expression/MapperMaths.java',
			'''
			package com.rosetta.model.lib.expression;
			
			import com.rosetta.model.lib.mapper.Mapper;
			import com.rosetta.model.lib.mapper.MapperS;
			import java.math.BigDecimal;
			import java.math.MathContext;
			import java.time.LocalDate;
			import java.time.LocalDateTime;
			import java.time.LocalTime;
			import java.time.temporal.ChronoUnit;
			import java.util.Arrays;
			import org.eclipse.xtend2.lib.StringConcatenation;
			
			@SuppressWarnings("all")
			public class MapperMaths {
			  public static <R extends Object, A extends Object, B extends Object> MapperS<R> add(final Mapper<A> in1, final Mapper<B> in2) {
			    if (((in1.resultCount() == 1) && (in2.resultCount() == 1))) {
			      final A arg1 = in1.get();
			      final B arg2 = in2.get();
			      Object _plus = MapperMaths.operator_plus(arg1, arg2);
			      return MapperS.<R>of(((R) _plus));
			    }
			    return MapperS.<R>ofNull();
			  }
			
			  public static <R extends Object, A extends Object, B extends Object> MapperS<R> subtract(final Mapper<A> in1, final Mapper<B> in2) {
			    if (((in1.resultCount() == 1) && (in2.resultCount() == 1))) {
			      final A arg1 = in1.get();
			      final B arg2 = in2.get();
			      Object _minus = MapperMaths.operator_minus(arg1, arg2);
			      return MapperS.<R>of(((R) _minus));
			    }
			    return null;
			  }
			
			  public static <R extends Object, A extends Object, B extends Object> MapperS<R> multiply(final Mapper<A> in1, final Mapper<B> in2) {
			    if (((in1.resultCount() == 1) && (in2.resultCount() == 1))) {
			      final A arg1 = in1.get();
			      final B arg2 = in2.get();
			      Object _multiply = MapperMaths.operator_multiply(arg1, arg2);
			      return MapperS.<R>of(((R) _multiply));
			    }
			    return MapperS.<R>ofNull();
			  }
			
			  public static <R extends Object, A extends Object, B extends Object> MapperS<R> divide(final Mapper<A> in1, final Mapper<B> in2) {
			    if (((in1.resultCount() == 1) && (in2.resultCount() == 1))) {
			      final A arg1 = in1.get();
			      final B arg2 = in2.get();
			      Object _divide = MapperMaths.operator_divide(arg1, arg2);
			      return MapperS.<R>of(((R) _divide));
			    }
			    return MapperS.<R>ofNull();
			  }
			
			  private static Object _operator_plus(final Object a, final Object b) {
			    StringConcatenation _builder = new StringConcatenation();
			    _builder.append("Cant add two random (");
			    String _simpleName = a.getClass().getSimpleName();
			    _builder.append(_simpleName);
			    _builder.append(", ");
			    String _simpleName_1 = b.getClass().getSimpleName();
			    _builder.append(_simpleName_1);
			    _builder.append(") together");
			    throw new RuntimeException(_builder.toString());
			  }
			
			  private static BigDecimal _operator_plus(final LocalDate d1, final LocalDate d2) {
			    StringConcatenation _builder = new StringConcatenation();
			    _builder.append("Cant add two dates together");
			    throw new RuntimeException(_builder.toString());
			  }
			
			  private static LocalDateTime _operator_plus(final LocalDate d, final LocalTime t) {
			    return LocalDateTime.of(d, t);
			  }
			
			  private static String _operator_plus(final String a, final String b) {
			    return (a + b);
			  }
			
			  private static Integer _operator_plus(final Integer a, final Integer b) {
			    int _intValue = a.intValue();
			    int _intValue_1 = b.intValue();
			    return Integer.valueOf((_intValue + _intValue_1));
			  }
			
			  private static BigDecimal _operator_plus(final Number a, final Number b) {
			    final BigDecimal bigA = MapperMaths.toBigD(a);
			    final BigDecimal bigB = MapperMaths.toBigD(b);
			    return bigA.add(bigB);
			  }
			
			  private static Object _operator_minus(final Object a, final Object b) {
			    StringConcatenation _builder = new StringConcatenation();
			    _builder.append("Cant subtract two random (");
			    String _simpleName = a.getClass().getSimpleName();
			    _builder.append(_simpleName);
			    _builder.append(", ");
			    String _simpleName_1 = b.getClass().getSimpleName();
			    _builder.append(_simpleName_1);
			    _builder.append(") together");
			    throw new RuntimeException(_builder.toString());
			  }
			
			  private static Integer _operator_minus(final LocalDate d1, final LocalDate d2) {
			    return Integer.valueOf(Long.valueOf(ChronoUnit.DAYS.between(d2, d1)).intValue());
			  }
			
			  private static LocalDateTime _operator_minus(final LocalDate d, final LocalTime t) {
			    StringConcatenation _builder = new StringConcatenation();
			    _builder.append("Cant subtract time from date");
			    throw new RuntimeException(_builder.toString());
			  }
			
			  private static String _operator_minus(final String a, final String b) {
			    StringConcatenation _builder = new StringConcatenation();
			    _builder.append("Cant subtract two strings together");
			    throw new RuntimeException(_builder.toString());
			  }
			
			  private static Integer _operator_minus(final Integer a, final Integer b) {
			    int _intValue = a.intValue();
			    int _intValue_1 = b.intValue();
			    return Integer.valueOf((_intValue - _intValue_1));
			  }
			
			  private static BigDecimal _operator_minus(final Number a, final Number b) {
			    final BigDecimal bigA = MapperMaths.toBigD(a);
			    final BigDecimal bigB = MapperMaths.toBigD(b);
			    return bigA.subtract(bigB);
			  }
			
			  private static Object _operator_multiply(final Object a, final Object b) {
			    StringConcatenation _builder = new StringConcatenation();
			    _builder.append("Cant multiply two random (");
			    String _simpleName = a.getClass().getSimpleName();
			    _builder.append(_simpleName);
			    _builder.append(", ");
			    String _simpleName_1 = b.getClass().getSimpleName();
			    _builder.append(_simpleName_1);
			    _builder.append(") together");
			    throw new RuntimeException(_builder.toString());
			  }
			
			  private static BigDecimal _operator_multiply(final LocalDate d1, final LocalDate d2) {
			    StringConcatenation _builder = new StringConcatenation();
			    _builder.append("Cant multiply date and date");
			    throw new RuntimeException(_builder.toString());
			  }
			
			  private static LocalDateTime _operator_multiply(final LocalDate d, final LocalTime t) {
			    StringConcatenation _builder = new StringConcatenation();
			    _builder.append("Cant multiply time and date");
			    throw new RuntimeException(_builder.toString());
			  }
			
			  private static String _operator_multiply(final String a, final String b) {
			    StringConcatenation _builder = new StringConcatenation();
			    _builder.append("Cant multiply two strings together");
			    throw new RuntimeException(_builder.toString());
			  }
			
			  private static Integer _operator_multiply(final Integer a, final Integer b) {
			    int _intValue = a.intValue();
			    int _intValue_1 = b.intValue();
			    return Integer.valueOf((_intValue * _intValue_1));
			  }
			
			  private static BigDecimal _operator_multiply(final Number a, final Number b) {
			    final BigDecimal bigA = MapperMaths.toBigD(a);
			    final BigDecimal bigB = MapperMaths.toBigD(b);
			    return bigA.multiply(bigB);
			  }
			
			  private static Object _operator_divide(final Object a, final Object b) {
			    StringConcatenation _builder = new StringConcatenation();
			    _builder.append("Cant divide two random (");
			    String _simpleName = a.getClass().getSimpleName();
			    _builder.append(_simpleName);
			    _builder.append(", ");
			    String _simpleName_1 = b.getClass().getSimpleName();
			    _builder.append(_simpleName_1);
			    _builder.append(")");
			    throw new RuntimeException(_builder.toString());
			  }
			
			  private static BigDecimal _operator_divide(final LocalDate d1, final LocalDate d2) {
			    StringConcatenation _builder = new StringConcatenation();
			    _builder.append("Cant divide date and date");
			    throw new RuntimeException(_builder.toString());
			  }
			
			  private static LocalDateTime _operator_divide(final LocalDate d, final LocalTime t) {
			    StringConcatenation _builder = new StringConcatenation();
			    _builder.append("Cant divide time and date");
			    throw new RuntimeException(_builder.toString());
			  }
			
			  private static String _operator_divide(final String a, final String b) {
			    StringConcatenation _builder = new StringConcatenation();
			    _builder.append("Cant divide two strings");
			    throw new RuntimeException(_builder.toString());
			  }
			
			  private static BigDecimal _operator_divide(final Integer a, final Integer b) {
			    final BigDecimal bigA = MapperMaths.toBigD(a);
			    final BigDecimal bigB = MapperMaths.toBigD(b);
			    return bigA.divide(bigB, MathContext.DECIMAL128);
			  }
			
			  private static BigDecimal _operator_divide(final Number a, final Number b) {
			    final BigDecimal bigA = MapperMaths.toBigD(a);
			    final BigDecimal bigB = MapperMaths.toBigD(b);
			    return bigA.divide(bigB, MathContext.DECIMAL128);
			  }
			
			  public static BigDecimal toBigD(final Number n) {
			    boolean _matched = false;
			    if (n instanceof BigDecimal) {
			      _matched=true;
			      return ((BigDecimal)n);
			    }
			    if (!_matched) {
			      if (n instanceof Long) {
			        _matched=true;
			        long _longValue = ((Long)n).longValue();
			        return new BigDecimal(_longValue);
			      }
			    }
			    if (!_matched) {
			      if (n instanceof Integer) {
			        _matched=true;
			        int _intValue = ((Integer)n).intValue();
			        return new BigDecimal(_intValue);
			      }
			    }
			    return null;
			  }
			
			  private static Object operator_plus(final Object d1, final Object d2) {
			    if (d1 instanceof LocalDate
			         && d2 instanceof LocalDate) {
			      return _operator_plus((LocalDate)d1, (LocalDate)d2);
			    } else if (d1 instanceof LocalDate
			         && d2 instanceof LocalTime) {
			      return _operator_plus((LocalDate)d1, (LocalTime)d2);
			    } else if (d1 instanceof Integer
			         && d2 instanceof Integer) {
			      return _operator_plus((Integer)d1, (Integer)d2);
			    } else if (d1 instanceof Number
			         && d2 instanceof Number) {
			      return _operator_plus((Number)d1, (Number)d2);
			    } else if (d1 instanceof String
			         && d2 instanceof String) {
			      return _operator_plus((String)d1, (String)d2);
			    } else if (d1 != null
			         && d2 != null) {
			      return _operator_plus(d1, d2);
			    } else {
			      throw new IllegalArgumentException("Unhandled parameter types: " +
			        Arrays.<Object>asList(d1, d2).toString());
			    }
			  }
			
			  private static Object operator_minus(final Object d1, final Object d2) {
			    if (d1 instanceof LocalDate
			         && d2 instanceof LocalDate) {
			      return _operator_minus((LocalDate)d1, (LocalDate)d2);
			    } else if (d1 instanceof LocalDate
			         && d2 instanceof LocalTime) {
			      return _operator_minus((LocalDate)d1, (LocalTime)d2);
			    } else if (d1 instanceof Integer
			         && d2 instanceof Integer) {
			      return _operator_minus((Integer)d1, (Integer)d2);
			    } else if (d1 instanceof Number
			         && d2 instanceof Number) {
			      return _operator_minus((Number)d1, (Number)d2);
			    } else if (d1 instanceof String
			         && d2 instanceof String) {
			      return _operator_minus((String)d1, (String)d2);
			    } else if (d1 != null
			         && d2 != null) {
			      return _operator_minus(d1, d2);
			    } else {
			      throw new IllegalArgumentException("Unhandled parameter types: " +
			        Arrays.<Object>asList(d1, d2).toString());
			    }
			  }
			
			  private static Object operator_multiply(final Object d1, final Object d2) {
			    if (d1 instanceof LocalDate
			         && d2 instanceof LocalDate) {
			      return _operator_multiply((LocalDate)d1, (LocalDate)d2);
			    } else if (d1 instanceof LocalDate
			         && d2 instanceof LocalTime) {
			      return _operator_multiply((LocalDate)d1, (LocalTime)d2);
			    } else if (d1 instanceof Integer
			         && d2 instanceof Integer) {
			      return _operator_multiply((Integer)d1, (Integer)d2);
			    } else if (d1 instanceof Number
			         && d2 instanceof Number) {
			      return _operator_multiply((Number)d1, (Number)d2);
			    } else if (d1 instanceof String
			         && d2 instanceof String) {
			      return _operator_multiply((String)d1, (String)d2);
			    } else if (d1 != null
			         && d2 != null) {
			      return _operator_multiply(d1, d2);
			    } else {
			      throw new IllegalArgumentException("Unhandled parameter types: " +
			        Arrays.<Object>asList(d1, d2).toString());
			    }
			  }
			
			  private static Object operator_divide(final Object d1, final Object d2) {
			    if (d1 instanceof LocalDate
			         && d2 instanceof LocalDate) {
			      return _operator_divide((LocalDate)d1, (LocalDate)d2);
			    } else if (d1 instanceof LocalDate
			         && d2 instanceof LocalTime) {
			      return _operator_divide((LocalDate)d1, (LocalTime)d2);
			    } else if (d1 instanceof Integer
			         && d2 instanceof Integer) {
			      return _operator_divide((Integer)d1, (Integer)d2);
			    } else if (d1 instanceof Number
			         && d2 instanceof Number) {
			      return _operator_divide((Number)d1, (Number)d2);
			    } else if (d1 instanceof String
			         && d2 instanceof String) {
			      return _operator_divide((String)d1, (String)d2);
			    } else if (d1 != null
			         && d2 != null) {
			      return _operator_divide(d1, d2);
			    } else {
			      throw new IllegalArgumentException("Unhandled parameter types: " +
			        Arrays.<Object>asList(d1, d2).toString());
			    }
			  }
			}
			'''
		)
		
		fsa.generateFile('com/rosetta/model/lib/mapper/MapperUtils.java',
			'''
			package com.rosetta.model.lib.mapper;
			
			import java.util.function.Supplier;
			import com.rosetta.model.lib.expression.ComparisonResult;
			
			public class MapperUtils {
				
				/**
				 * Used when generating code for nested if statements
				 */
				public static <T> Mapper<T> fromBuiltInType(Supplier<Mapper<T>> supplier) {
					return supplier.get();
				}
			
				/**
				 * Used when generating code for nested if statements
				 */
				public static <T> Mapper<? extends T> fromDataType(Supplier<Mapper<? extends T>> supplier) {
					return supplier.get();
				}
			
				public static ComparisonResult toComparisonResult(Mapper<Boolean> mapper) {
					if (mapper instanceof ComparisonResult) {
						return (ComparisonResult) mapper;
					} else {
						return mapper.getMulti().stream().allMatch(Boolean::booleanValue) ? ComparisonResult.success() : ComparisonResult.failure("");
					}
				}
			}
			''')
			
		fsa.generateFile('com/rosetta/model/lib/expression/CardinalityOperator.java',
			'''
			package com.rosetta.model.lib.expression;
			
			/**
			 * Generated by com.regnosys.rosetta.generator.util.BackwardCompatabilityGenerator.java.
			 * Provide compatibility for CDM versions before 2.111.0 (which uses DSL version 4.9.1).
			 * Can be removed once all model CDM versions have been updated to 2.111.0.
			 */
			public enum CardinalityOperator {
				All,
				Any
			}
			''')
					
		fsa.generateFile('com/rosetta/model/lib/functions/ModelObjectValidator.java',
			'''
			package com.rosetta.model.lib.functions;
			
			import java.util.List;
			
			import com.google.inject.ImplementedBy;
			import com.rosetta.model.lib.RosettaModelObject;
			
			@ImplementedBy(NoOpModelObjectValidator.class)
			public interface ModelObjectValidator {
			
				/**
				 * Runs validation and collects errors. Implementation may throw an exception if validation fails.
				 * 
				 * @param <T>
				 * @param clazz
				 * @param object
				 * @throws ModelObjectValidationException if validation fails
				 */
				<T extends RosettaModelObject> void validate(Class<T> clazz, T object);
			
				/**
				 * Runs validation and collects errors. Implementation may throw an exception if validation fails.
				 * 
				 * @param <T>
				 * @param clazz
				 * @param objects
				 * @throws ModelObjectValidationException if validation fails
				 */
				<T extends RosettaModelObject> void validate(Class<T> clazz, List<? extends T> objects);
			
			
				class ModelObjectValidationException extends RuntimeException {
					
					private final String errors;
			
					public ModelObjectValidationException(String errors) {
						super(errors);
						this.errors = errors;
					}
			
					public String getErrors() {
						return errors;
					}
				}
			}
			''')
			
		fsa.generateFile('com/rosetta/model/lib/functions/NoOpModelObjectValidator.java',
			'''
			package com.rosetta.model.lib.functions;
			
			import java.util.List;
			
			import com.rosetta.model.lib.RosettaModelObject;
			
			public class NoOpModelObjectValidator implements ModelObjectValidator {
			
				@Override
				public <T extends RosettaModelObject> void validate(Class<T> clazz, T object) {
					// do nothing
				}
			
				@Override
				public <T extends RosettaModelObject> void validate(Class<T> clazz, List<? extends T> objects) {
					// do nothing
				}
			
			}

			''')
			
		fsa.generateFile('com/rosetta/model/lib/functions/ConditionValidator.java',
			'''
			package com.rosetta.model.lib.functions;
			
			import java.util.function.Supplier;
			
			import com.google.inject.ImplementedBy;
			import com.rosetta.model.lib.expression.ComparisonResult;
			
			/**
			 * Generated by com.regnosys.rosetta.generator.util.BackwardCompatabilityGenerator.java.
			 * Provide compatibility for CDM versions before 2.175.0 (which uses DSL version 4.42.0).
			 * Can be removed once all model CDM versions have been updated to 2.175.0.
			 */
			@ImplementedBy(DefaultConditionValidator.class)
			public interface ConditionValidator {
			
				/**
				 * Evaluates conditions. Implementation may throw an exception if condition fails.
				 * 
				 * @param condition
				 * @param description
				 * @throws ConditionException if condition fails
				 */
			    void validate(Supplier<ComparisonResult> condition, String description);
			
			
			    class ConditionException extends RuntimeException {
			
			        public ConditionException(String message) {
			            super(message);
			        }
			
			        public ConditionException(String message, Throwable cause) {
			            super(message, cause);
			        }
			    }
			}
			''')
			
		fsa.generateFile('com/rosetta/model/lib/functions/DefaultConditionValidator.java',
			'''
			package com.rosetta.model.lib.functions;
			
			import java.util.function.Supplier;
			
			import com.rosetta.model.lib.expression.ComparisonResult;
			
			/**
			 * Generated by com.regnosys.rosetta.generator.util.BackwardCompatabilityGenerator.java.
			 * Provide compatibility for CDM versions before 2.175.0 (which uses DSL version 4.42.0).
			 * Can be removed once all model CDM versions have been updated to 2.175.0.
			 */
			public class DefaultConditionValidator implements ConditionValidator {
			    @Override
			    public void validate(Supplier<ComparisonResult> condition, String description) {
			        if (!condition.get().get()) {
			            throw new ConditionException(description);
			        }
			    }
			}
			''')
			
		fsa.generateFile('com/rosetta/model/lib/mapper/MapperS.java',
			'''
			package com.rosetta.model.lib.mapper;
			
			import static com.rosetta.model.lib.mapper.MapperItem.getMapperItem;
			import static com.rosetta.model.lib.mapper.MapperItem.getMapperItems;
			
			import java.util.ArrayList;
			import java.util.Arrays;
			import java.util.Collections;
			import java.util.List;
			import java.util.Optional;
			import java.util.function.Function;
			import java.util.stream.Stream;
			
			import com.rosetta.model.lib.RosettaModelObject;
			
			
			public class MapperS<T> implements MapperBuilder<T> {
			
				private final MapperItem<T,?> item;
				private final boolean identity;
				
				public MapperS(MapperItem<T,?> item) {
					this(item, false);
				}
				
				public MapperS(MapperItem<T,?> item, boolean identity) {
					this.item = item;
					this.identity = identity;
				}
				
				public static <T> MapperS<T> identity() {
					return new MapperS<>(new MapperItem<>(null, MapperPath.builder().addNull(), true, Optional.empty()), true);
				}
				
				public static <T> MapperS<T> ofNull() {
					return new MapperS<>(new MapperItem<>(null, MapperPath.builder().addNull(), true, Optional.empty()));
				}
			
				public static <T> MapperS<T> of(T t) {
					if (t==null) {
						return new MapperS<>(new MapperItem<>(t, MapperPath.builder().addNull(), true, Optional.empty()));
					}
					if (t instanceof RosettaModelObject) {
						return new MapperS<>(new MapperItem<>(t, MapperPath.builder().addRoot(((RosettaModelObject)t).getType()), false, Optional.empty()));
					}
					return new MapperS<>(new MapperItem<>(t, MapperPath.builder().addRoot(t.getClass()), false, Optional.empty()));
				}
				
				public static <T,P> MapperS<T> of(T t, MapperPath path, MapperItem<P, ?> parent) {
					if (t==null) { 
						return new MapperS<>(new MapperItem<>(t, path, true, Optional.ofNullable(parent)));
					}
					return new MapperS<>(new MapperItem<>(t, path, false, Optional.ofNullable(parent)));
				}
				
				@Override
				public <F> MapperS<F> map(String name, Function<T, F> mappingFunc) {
					return map(new NamedFunctionImpl<>(name, mappingFunc));
				}
				
				/**
				 * Maps single parent item to single child item.
				 */
				@Override
				public <F> MapperS<F> map(NamedFunction<T, F> mappingFunc) {
					return new MapperS<>(getMapperItem(item, mappingFunc));
				}
				
				@Override
				public <F> MapperC<F> mapC(String name, Function<T, List<? extends F>> mappingFunc) {
					return mapC(new NamedFunctionImpl<T, List<? extends F>>(name, mappingFunc));
				}
				
				/**
				 * Maps single parent item to list child item.
				 */
				@Override
				public <F> MapperC<F> mapC(NamedFunction<T, List<? extends F>> mappingFunc) {
					return new MapperC<>(getMapperItems(item, mappingFunc));
				}
				
				@Override
				public T get() {
					return item.getMappedObject();
				}
				
				@Override
				public T getOrDefault(T defaultValue) {
					return Optional.ofNullable(item.getMappedObject()).orElse(defaultValue);
				}
				
				@Override
				public List<T> getMulti() {
					return Optional.ofNullable(get())
							.map(Arrays::asList)
							.orElseGet(ArrayList::new);
				}
				
				@Override
				public Optional<?> getParent() {
					return findParent(item)
							.map(MapperItem::getMappedObject);
				}
			
				@Override
				public List<?> getParentMulti() {
					return findParent(item)
							.map(MapperItem::getMappedObject)
							.map(Arrays::asList)
							.orElseGet(ArrayList::new);
				}
			
				@Override
				public int resultCount() {
					return item.getMappedObject()!=null?1:0;
				}
				
				public boolean isIdentity() {
					return identity;
				}
				
				/**
				 * Map a single value into an item of a list based on the given mapping function.
				 * 
				 * @param <F>
				 * @param mappingFunc
				 * @return mapped list
				 */
				public <F> MapperS<F> mapSingleToItem(Function<MapperS<T>, MapperS<F>> mappingFunc) {
					return mappingFunc.apply(this);
				}
				
				/**
				 * Map a single value into an item of a list based on the given mapping function.
				 * 
				 * @param <F>
				 * @param mappingFunc
				 * @return mapped list
				 */
				public <F> MapperC<F> mapSingleToList(Function<MapperS<T>, MapperC<F>> mappingFunc) {
					return mappingFunc.apply(this);
				}
				
				/**
				 * Apply a function to this mapper
				 */
				public <F> F apply(Function<MapperS<T>, F> f) {
					return f.apply(this);
				}
				
				/* (non-Javadoc)
				 * @see com.rosetta.model.lib.blueprint.Mapper#getPath()
				 */
				@Override
				public List<Path> getPaths() {
					return !item.isError() ? Collections.singletonList(item.getPath()) : Collections.emptyList();
				}
				
				/* (non-Javadoc)
				 * @see com.rosetta.model.lib.blueprint.Mapper#getPath()
				 */
				@Override
				public List<Path> getErrorPaths() {
					return item.isError() ? Collections.singletonList(item.getPath()) : Collections.emptyList();
				}
				
				/* (non-Javadoc)
				 * @see com.rosetta.model.lib.blueprint.Mapper#getError()
				 */
				@Override
				public List<String> getErrors() {
					return item.isError() ? Collections.singletonList(item.getPath().toString() +" was null") : Collections.emptyList();
				}
				
				@Override
				public String toString() {
					return item.getPath().toString();
				}
				
				@Override
				public MapperC<T> unionSame(MapperBuilder<T> other) {
					if(other instanceof MapperS) {
						MapperS<T> otherMapperS = (MapperS<T>) other;
						return new MapperC<>(Arrays.asList(this.item, otherMapperS.item));
					}
					else if(other instanceof MapperC) {
						return new MapperC<>(Collections.singletonList(this.item)).unionSame(other);
					}
					else {
						throw new IllegalArgumentException("Unsupported Mapper type: " + other.getClass().getName());
					}
				}
				
				@Override
				public MapperC<Object> unionDifferent(MapperBuilder<?> other) {
					if(other instanceof MapperS) {
						MapperS<?> otherMapperS = (MapperS<?>) other;
						return new MapperC<>(Arrays.asList(this.item.upcast(), otherMapperS.item.upcast()));
					}
					else if(other instanceof MapperC) {
						return new MapperC<>(Collections.singletonList(this.item.upcast())).unionDifferent(other);
					}
					else {
						throw new IllegalArgumentException("Unsupported Mapper type: " + other.getClass().getName());
					}
				}
			
				@Override
				public int hashCode() {
					final int prime = 31;
					int result = 1;
					result = prime * result + ((item == null) ? 0 : item.hashCode());
					return result;
				}
			
				@Override
				public boolean equals(Object obj) {
					if (this == obj)
						return true;
					if (obj == null)
						return false;
					if (getClass() != obj.getClass())
						return false;
					MapperS<?> other = (MapperS<?>) obj;
					if (item == null) {
						if (other.item != null)
							return false;
					} else if (!item.equals(other.item))
						return false;
					return true;
				}
			
				@Override
				public Stream<MapperItem<T, ?>> getItems() {
					return Stream.of(item);
				}
			}
			''')
		
		fsa.generateFile('com/rosetta/model/lib/mapper/MapperC.java', '''
			package com.rosetta.model.lib.mapper;
			
			import static com.rosetta.model.lib.mapper.MapperItem.getMapperItem;
			import static com.rosetta.model.lib.mapper.MapperItem.getMapperItems;
			
			import java.math.BigDecimal;
			import java.util.ArrayList;
			import java.util.Collections;
			import java.util.Comparator;
			import java.util.List;
			import java.util.Optional;
			import java.util.function.BinaryOperator;
			import java.util.function.Function;
			import java.util.function.Predicate;
			import java.util.stream.Collectors;
			import java.util.stream.Stream;
			
			public class MapperC<T> implements MapperBuilder<T> {
				
				private final List<MapperItem<T,?>> items;
				
				protected MapperC(List<MapperItem<T,?>> items) {
					this.items = items;
				}
				
				public static <T> MapperC<T> ofNull() {
					return new MapperC<>(new ArrayList<>());
				}
				
				@SafeVarargs
				public static <T> MapperC<T> of(MapperBuilder<T>... ts) {
					List<MapperItem<T, ?>> items = new ArrayList<>();
					if (ts != null) {
						for (MapperBuilder<T> ele : ts) {
							if (ele != null) {
								ele.getItems().forEach(item -> items.add(item));
							}
						}
					}
					return new MapperC<T>(items);
				}
			
				public static <T> MapperC<T> of(List<? extends T> ts) {
					List<MapperItem<T, ?>> items = new ArrayList<>();
					if (ts != null) {
						for (T ele : ts) {
							if (ele == null) {
								items.add(new MapperItem<>(ele, MapperPath.builder().addNull(), true, Optional.empty()));
							} else {
								items.add(new MapperItem<>(ele, MapperPath.builder().addRoot(ele.getClass()), false, Optional.empty()));
							}
						}
					}
					return new MapperC<T>(items);
				}
				
				@Override
				public <F> MapperC<F> map(String name, Function<T, F> mappingFunc) {
					return map(new NamedFunctionImpl<>(name, mappingFunc));
				}
				
				/**
				 * Maps list parent item to single child item.
				 */
				@Override
				public <F> MapperC<F> map(NamedFunction<T, F> mappingFunc) {
					List<MapperItem<F,?>> results = new ArrayList<>();
					
					for (int i=0; i<items.size(); i++) {
						results.add(getMapperItem(items.get(i), mappingFunc));
					}
					return new MapperC<>(results);
				}
				
				@Override
				public <F> MapperC<F> mapC(String name, Function<T, List<? extends F>> mappingFunc) {
					return mapC(new NamedFunctionImpl<T, List<? extends F>>(name, mappingFunc));
				}
				
				/**
				 * Maps list parent item to list child item.
				 */
				@Override
				public <F> MapperC<F> mapC(NamedFunction<T, List<? extends F>> mappingFunc) {
					List<MapperItem<F,?>> results = new ArrayList<>();
					
					for (int i=0; i<items.size(); i++) {
						results.addAll(getMapperItems(items.get(i), mappingFunc));
					}
					return new MapperC<>(results);
				}
				
				/**
				 * Filter items of list based on the given predicate.
				 * 
				 * @param predicate - test that determines whether to filter list item. True to include in list, and false to exclude.
				 * @return filtered list 
				 */
				public MapperC<T> filterItem(Predicate<MapperS<T>> predicate) {
					return new MapperC<>(nonErrorItems()
							.filter(item -> predicate.test(new MapperS<>(item)))
							.collect(Collectors.toList()));
				}
				
				/**
				 * Map items of a list based on the given mapping function.
				 * 
				 * @param <F>
				 * @param mappingFunc
				 * @return mapped list
				 */
				public <F> MapperC<F> mapItem(Function<MapperS<T>, MapperS<F>> mappingFunc) {
					return MapperC.of(nonErrorItems()
							.map(item -> new MapperS<>(item))
							.map(m -> mappingFunc.apply(m))
							.map(MapperS::get)
							.collect(Collectors.toList()));
				}
				
				/**
				 * Map items of a list based on the given mapping function.
				 * 
				 * @param <F>
				 * @param mappingFunc
				 * @return mapped list
				 */
				public <F> MapperListOfLists<F> mapItemToList(Function<MapperS<T>, MapperC<F>> mappingFunc) {
					return MapperListOfLists.of(nonErrorItems()
							.map(item -> new MapperS<>(item))
							.map(m -> mappingFunc.apply(m))
							.map(MapperC::getMulti)
							.collect(Collectors.toList()));
				}
				
				/**
				 * Apply a function to this mapper
				 */
				public <F> F apply(Function<MapperC<T>, F> f) {
					return f.apply(this);
				}
			
				/**
				 * Reduce list items to single item based on the given reduce function.
				 * 
				 * @param <F>
				 * @param reduceFunc
				 * @return reduced item
				 */
				public <F> MapperS<F> reduce(BinaryOperator<MapperS<F>> reduceFunc) {
					return reduce(MapperS.identity(), reduceFunc);
				}
				
				/**
				 * Reduce list items to single item based on the given reduce function.
				 * 
				 * @param <F>
				 * @param reduceFunc
				 * @return reduced item
				 */
				@SuppressWarnings("unchecked")
				public <F> MapperS<F> reduce(MapperS<F> initial, BinaryOperator<MapperS<F>> reduceFunc) {
					return nonErrorItems()
							.map(item -> new MapperS<>((MapperItem<F, ?>) item))
							.reduce(initial, (m1, m2) -> {
									if (m1.isIdentity())
										return m2;
									else if (m2.isIdentity())
										return m1;
									else
										return reduceFunc.apply(m1, m2);
								});
				}
				
				/**
				 * Sum list of integers.
				 * 
				 * @return total of summed integers.
				 */
				public MapperS<Integer> sumInteger() {
					return MapperS.of(nonErrorItems()
							.map(MapperItem::getMappedObject)
							.map(Integer.class::cast)
							.reduce(0, Integer::sum));
				}
				
				/**
				 * Sum list of numbers.
				 * 
				 * @return total of summed numbers.
				 */
				public MapperS<BigDecimal> sumBigDecimal() {
					return MapperS.of(nonErrorItems()
							.map(MapperItem::getMappedObject)
							.map(BigDecimal.class::cast)
							.reduce(BigDecimal.ZERO, BigDecimal::add));
				}
				
				/**
				 * Concatenate list of strings, separating each item with delimiter.
				 * 
				 * @param delimiter - item separator
				 * @return concatenated string
				 */
				public MapperS<String> join(MapperS<String> delimiter) {
					return MapperS.of(nonErrorItems()
							.map(MapperItem::getMappedObject)
							.map(String.class::cast)
							.collect(Collectors.joining(delimiter.getOrDefault(""))));
				}
				
				/**
				 * Get minimum item from a list of comparable items.
				 * 
				 * @return minimum
				 */
				@SuppressWarnings("unchecked")
				public <F extends Comparable<F>> MapperS<T> min() {
					return min(x -> (MapperS<F>) x);
				}
				
				/**
				 * Get item from list based on minimum item attribute (provided by comparableGetter)
				 * 
				 * @param <F>
				 * @param comparableGetter - getter for comparable attribute
				 * @return minimum
				 */
				public <F extends Comparable<F>> MapperS<T> min(Function<MapperS<T>, MapperS<F>> comparableGetter) {
					return nonErrorItems()
							.map(item -> new MapperS<>(item))
							.filter(item -> comparableGetter.apply(item).get() != null)
							.min(Comparator.comparing(item -> comparableGetter.apply(item).get()))
							.orElse(MapperS.ofNull());
							
				}
			
				/**
				 * Get maximum item from a list of comparable items.
				 * 
				 * @return maximum
				 */
				@SuppressWarnings("unchecked")
				public <F extends Comparable<F>> MapperS<T> max() {
					return max(x -> (MapperS<F>) x);
				}
				
				/**
				 * Get item from list based on maximum item attribute (provided by comparableGetter)
				 * 
				 * @param <F>
				 * @param comparableGetter - getter for comparable attribute
				 * @return maximum
				 */
				public <F extends Comparable<F>> MapperS<T> max(Function<MapperS<T>, MapperS<F>> comparableGetter) {
					return nonErrorItems()
							.map(item -> new MapperS<>(item))
							.filter(item -> comparableGetter.apply(item).get() != null)
							.max(Comparator.comparing(item -> comparableGetter.apply(item).get()))
							.orElse(MapperS.ofNull());
				}
				
				/**
				 * Sort list of comparable items.
				 * 
				 * @return sorted list
				 */
				public MapperC<T> sort() {
					return MapperC.of(nonErrorItems()
							.map(MapperItem::getMappedObject)
							.sorted()
							.collect(Collectors.toList()));
				}
				
				/**
				 * Sort list of items based on comparable attribute.
				 * 
				 * @param <F> comparable type
				 * @param comparableGetter to get comparable item to sort by
				 * @return sorted list
				 */
				public <F extends Comparable<F>> MapperC<T> sort(Function<MapperS<T>, MapperS<F>> comparableGetter) {
					return new MapperC<>(nonErrorItems()
							.sorted(Comparator.comparing(item -> comparableGetter.apply(new MapperS<>(item)).get()))
							.collect(Collectors.toList()));
				}
				
				/**
				 * Reverse items of a list.
				 * 
				 * @return reversed list
				 */
				public MapperC<T> reverse() {
					List<MapperItem<T, ?>> nonErrorItems = nonErrorItems().collect(Collectors.toList());
					Collections.reverse(nonErrorItems);
					return new MapperC<>(nonErrorItems);
				}
				
				/**
				 * Gets first item of the list.
				 * 
				 * @return first list item
				 */
				public MapperS<T> first() {
					return nonErrorItems()
							.findFirst()
							.map(MapperS::new)
							.orElse(MapperS.ofNull());
				}
				
				/**
				 * Gets last item of the list.
				 * 
				 * @return last list item
				 */
				public MapperS<T> last() {
					return nonErrorItems()
							.reduce((first, second) -> second)
							.map(MapperS::new)
							.orElse(MapperS.ofNull());
				}
				
				/**
				 * Get item at specified index, returns null if index out of bounds.
				 * 
				 * @return list item at index
				 */
				public MapperS<T> getItem(MapperS<Integer> indexGetter) {
					List<MapperItem<T, ?>> nonErrorItems = nonErrorItems().collect(Collectors.toList());
					Integer index = indexGetter.get();
					if (index != null && index < nonErrorItems.size()) {
						return new MapperS<>(nonErrorItems.get(index));
					}
					return MapperS.ofNull();
				}
				
				/**
				 * Remove item at specified index, returns list without removed item.
				 * 
				 * @return list without specified item
				 */
				public MapperC<T> removeItem(MapperS<Integer> indexGetter) {
					List<MapperItem<T, ?>> nonErrorItems = nonErrorItems().collect(Collectors.toList());
					Integer index = indexGetter.get();
					if (index != null && index < nonErrorItems.size()) {
						nonErrorItems.remove(index.intValue());
					}
					return new MapperC<>(nonErrorItems);
				}
				
				protected Stream<MapperItem<T,?>> nonErrorItems() {
					return items.stream().filter(i->!i.isError());
				}
			
				private Stream<MapperItem<T,?>> errorItems() {
					return items.stream().filter(MapperItem::isError);
				}
				
				@Override
				public T get() {
					List<T> collect = nonErrorItems()
							.map(i->i.getMappedObject())
							.collect(Collectors.toList());
					return collect.size()!=1 ? null : collect.get(0);
				}
				
				@Override
				public T getOrDefault(T defaultValue) {
					return Optional.ofNullable(get()).orElse(defaultValue);
				}
				
				@Override
				public List<T> getMulti() {
					return nonErrorItems()
							.map(i->i.getMappedObject())
							.collect(Collectors.toList());
				}
			
				@Override
				public Optional<?> getParent() {
					List<?> collect = nonErrorItems()
						.map(this::findParent)
						.filter(Optional::isPresent)
						.map(Optional::get)
						.map(MapperItem::getMappedObject)
						.collect(Collectors.toList());
					return collect.size()==1 ? Optional.of(collect.get(0)) : Optional.empty();
				}
				
				@Override
				public List<?> getParentMulti() {
					return nonErrorItems()
							.map(this::findParent)
							.filter(Optional::isPresent)
							.map(Optional::get)
							.map(MapperItem::getMappedObject)
							.collect(Collectors.toList());
				}
				
				@Override
				public int resultCount() {
					return (int) nonErrorItems().count();
				}
			
				@Override
				public List<Path> getPaths() {
					return nonErrorItems()
							.map(MapperItem::getPath)
							.collect(Collectors.toList());
				}
				
				@Override
				public List<Path> getErrorPaths() {
					return errorItems()
							.map(MapperItem::getPath)
							.collect(Collectors.toList());
				}
			
				@Override
				public List<String> getErrors() {
					return errorItems()
							.map(MapperItem::getPath)
							.map(p -> String.format("[%s] is not set", p.getFullPath()))
							.collect(Collectors.toList());
				}
				
				@Override
				public String toString() {
					return String.join(",", items.stream().map(i -> i.getPath().getFullPath()).collect(Collectors.toList()));
				}
				
				@Override
				public MapperC<T> unionSame(MapperBuilder<T> other) {
					if(other instanceof MapperC) {
						MapperC<T> otherMapperC = (MapperC<T>) other;
						List<MapperItem<T,?>> unionItems = new ArrayList<>();
						unionItems.addAll(this.items);
						unionItems.addAll(otherMapperC.items);
						return new MapperC<>(unionItems);
					}
					else if(other instanceof MapperS) {
						return ((MapperS<T>) other).unionSame(this);
					}
					else {
						throw new IllegalArgumentException("Unsupported Mapper type: " + other.getClass().getName());
					}
				}
				
				@Override
				public MapperC<Object> unionDifferent(MapperBuilder<?> other) {
					if(other instanceof MapperC) {
						MapperC<?> otherMapperC = (MapperC<?>) other;
						List<MapperItem<Object,?>> unionItems = new ArrayList<>();
						unionItems.addAll(upcast(this));
						unionItems.addAll(upcast(otherMapperC));
						return new MapperC<>(unionItems);
					}
					else if(other instanceof MapperS) {
						return ((MapperS<?>) other).unionDifferent(this);
					}
					else {
						throw new IllegalArgumentException("Unsupported Mapper type: " + other.getClass().getName());
					}
				}
			
				private List<MapperItem<Object,?>> upcast(MapperC<?> mapper) {
					return mapper.items.stream().map(MapperItem::upcast).collect(Collectors.toList());
				}
			
				@Override
				public int hashCode() {
					final int prime = 31;
					int result = 1;
					result = prime * result + ((items == null) ? 0 : items.hashCode());
					return result;
				}
			
				@Override
				public boolean equals(Object obj) {
					if (this == obj)
						return true;
					if (obj == null)
						return false;
					if (getClass() != obj.getClass())
						return false;
					MapperC<?> other = (MapperC<?>) obj;
					if (items == null) {
						if (other.items != null)
							return false;
					} else if (!items.equals(other.items))
						return false;
					return true;
				}
			
				@Override
				public Stream<MapperItem<T, ?>> getItems() {
					return items.stream();
				}
			}
		''')
		
		fsa.generateFile('com/rosetta/model/lib/expression/ExpressionOperators.java',
			'''
			package com.rosetta.model.lib.expression;
			
			import java.lang.reflect.InvocationTargetException;
			import java.lang.reflect.Method;
			import java.math.BigDecimal;
			import java.math.BigInteger;
			import java.util.ArrayList;
			import java.util.Collection;
			import java.util.Collections;
			import java.util.LinkedList;
			import java.util.List;
			import java.util.Optional;
			import java.util.Set;
			import java.util.function.Supplier;
			import java.util.regex.Matcher;
			import java.util.regex.Pattern;
			import java.util.stream.Collectors;
			import java.util.stream.Stream;
			
			import org.apache.commons.lang3.StringUtils;
			
			import com.rosetta.model.lib.RosettaModelObject;
			import com.rosetta.model.lib.mapper.Mapper;
			import com.rosetta.model.lib.mapper.MapperC;
			import com.rosetta.model.lib.mapper.Mapper.Path;
			import com.rosetta.model.lib.mapper.MapperS;
			import com.rosetta.model.lib.meta.RosettaMetaData;
			import com.rosetta.model.lib.validation.ExistenceChecker;
			import com.rosetta.model.lib.validation.ValidationResult;
			import com.rosetta.model.lib.validation.ValidatorWithArg;
			
			public class ExpressionOperators {
				
				// notExists
				
				public static <T> ComparisonResult notExists(Mapper<T> o) {
					if (o.resultCount()==0) {
						return ComparisonResult.success();
					}
					return ComparisonResult.failure(o.getPaths() + " does exist and is " + formatMultiError(o));
				}
				
				// exists
				
				public static <T> ComparisonResult exists(Mapper<T> o) {
					if (o.resultCount()>0) {
						return ComparisonResult.success();
					}
					return ComparisonResult.failure(o.getErrorPaths() + " does not exist");
				}
				
				// singleExists
				
				public static <T> ComparisonResult singleExists(Mapper<T> o) {
					if (o.resultCount()==1) {
						return  ComparisonResult.success();
					}
					
					String error = o.resultCount() > 0 ?
							String.format("Expected single %s but found %s [%s]", o.getPaths(), o.resultCount(), formatMultiError(o)) :
							String.format("Expected single %s but found zero", o.getErrorPaths());
					
					return ComparisonResult.failure(error);
				}
				
				// multipleExists
				
				public static <T> ComparisonResult multipleExists(Mapper<T> o) {
					if (o.resultCount()>1) {
						return ComparisonResult.success();
					}
					
					String error = o.resultCount() > 0 ?
							String.format("Expected multiple %s but only one [%s]", o.getPaths(), formatMultiError(o)) :
							String.format("Expected multiple %s but found zero", o.getErrorPaths());
							
					return ComparisonResult.failure(error);
				}
				
				// onlyExists
				
				public static ComparisonResult onlyExists(List<? extends Mapper<?>> o) {
					// Validation rule checks that all parents match
					Set<RosettaModelObject> parents = o.stream()
							.map(Mapper::getParentMulti)
							.flatMap(Collection::stream)
							.map(RosettaModelObject.class::cast)
						    .collect(Collectors.toSet());
					
					if (parents.size() == 0) {
						return ComparisonResult.failure("No fields set.");
					}
			
					// Find attributes to check
					Set<String> fields = o.stream()
							.flatMap(m -> Stream.concat(m.getPaths().stream(), m.getErrorPaths().stream()))
							.map(ExpressionOperators::getAttributeName)
							.collect(Collectors.toSet());
					
					// The number of attributes to check, should equal the number of mappers
					if (fields.size() != o.size()) {
						return ComparisonResult.failure("All required fields not set.");
					}
					
					// Run validation then and results together 
					return parents.stream()
						.map(p -> validateOnlyExists(p, fields))
						.reduce(ComparisonResult.success(), (a, b) -> a.and(b));
				}
			
				/**
				 * @return attributeName - get the attribute name which is the path leaf node, unless attribute has metadata (scheme/reference etc), where it is the paths penultimate node. 
				 */
				private static String getAttributeName(Path p) {
					String attr = p.getLastName();
					return "value".equals(attr) || "reference".equals(attr) || "globalReference".equals(attr) ? 
							p.getNames().get(p.getNames().size() - 2) : 
							attr;
				}
				
				private static <T extends RosettaModelObject> ComparisonResult validateOnlyExists(T parent, Set<String> fields) {
					@SuppressWarnings("unchecked")
					RosettaMetaData<T> meta = (RosettaMetaData<T>) parent.metaData();
					ValidatorWithArg<? super T, Set<String>> onlyExistsValidator = meta.onlyExistsValidator();
					if (onlyExistsValidator != null) {
						ValidationResult<? extends RosettaModelObject> validationResult = onlyExistsValidator.validate(null, parent, fields);
						// Translate validationResult into comparisonResult
						return validationResult.isSuccess() ? 
								ComparisonResult.success() : 
								ComparisonResult.failure(validationResult.getFailureReason().orElse(""));
					} else {
						return ComparisonResult.success();
					}
				}
				
				/**
				 * DoIf implementation for Mappers
				 */
				public static <T, A extends Mapper<T>> A doIf(Mapper<Boolean> test, Supplier<A> ifthen, Supplier<A> elsethen) {
					boolean testResult = test.getMulti().stream().allMatch(Boolean::booleanValue);
					if (testResult) return ifthen.get();
					else return elsethen.get();
				}
				@SuppressWarnings("unchecked")
				public static <T, A extends Mapper<T>> A doIf(Mapper<Boolean> test, Supplier<A> ifthen) {
					return doIf(test, ifthen, () -> (A) MapperS.of((T) null));
				}
				
				
				/**
				 * DoIf implementation for ComparisonResult.
				 */
				public static ComparisonResult resultDoIf(Mapper<Boolean> test, Supplier<Mapper<Boolean>> ifthen, Supplier<Mapper<Boolean>> elsethen) {
					boolean testResult = test.getMulti().stream().allMatch(Boolean::booleanValue);
					if (testResult) {
						return toComparisonResult(ifthen.get());
					} else {
						return toComparisonResult(elsethen.get());
					}
				}
				
				public static ComparisonResult resultDoIf(Mapper<Boolean> test, Supplier<Mapper<Boolean>> ifthen) {
					return resultDoIf(test, ifthen, () -> ComparisonResult.success());
				}
				
				private static ComparisonResult toComparisonResult(Mapper<Boolean> mapper) {
					if (mapper instanceof ComparisonResult) {
						return (ComparisonResult) mapper;
					} else {
						return mapper.getMulti().stream().allMatch(Boolean::booleanValue) ? ComparisonResult.success() : ComparisonResult.failure("");
					}
				}
				
				interface CompareFunction<T, U> {
				    ComparisonResult apply(T t, U u, CardinalityOperator o);
				}
				
				// areEqual
				
				public static <T, U> ComparisonResult areEqual(Mapper<T> m1, Mapper<U> m2, CardinalityOperator o) {
					return ExpressionEqualityUtil.evaluate(m1, m2, o, ExpressionEqualityUtil::areEqual);
				}
				
				// notEqual
					
				public static <T, U> ComparisonResult notEqual(Mapper<T> m1, Mapper<U> m2, CardinalityOperator o) {
					return ExpressionEqualityUtil.evaluate(m1, m2, o, ExpressionEqualityUtil::notEqual);
				}
				
				public static <T extends Comparable<? super T>> ComparisonResult notEqual(ComparisonResult r1, ComparisonResult r2) {
					return r1.get() != r2.get() ? ComparisonResult.success() : ComparisonResult.failure("Results are not equal");
				}
				
				// greaterThan
					
				public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult greaterThan(Mapper<T> m1, Mapper<U> m2, CardinalityOperator o) {
					return ExpressionCompareUtil.evaluate(m1, m2, o, ExpressionCompareUtil::greaterThan);
				}
				
				// greaterThanEquals
				
				public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult greaterThanEquals(Mapper<T> m1, Mapper<U> m2, CardinalityOperator o) {
					return ExpressionCompareUtil.evaluate(m1, m2, o, ExpressionCompareUtil::greaterThanEquals);
				}
				
				// lessThan
			
				public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult lessThan(Mapper<T> m1, Mapper<U> m2, CardinalityOperator o)  {
					return ExpressionCompareUtil.evaluate(m1, m2, o, ExpressionCompareUtil::lessThan);
				}
				
				// lessThanEquals
			
				public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult lessThanEquals(Mapper<T> m1, Mapper<U> m2, CardinalityOperator o)  {
					return ExpressionCompareUtil.evaluate(m1, m2, o, ExpressionCompareUtil::lessThanEquals);
				}
			
				// contains
				
				public static <T> ComparisonResult contains(Mapper<? extends T> o1, Mapper<? extends T> o2) {
					if (o1.getMulti().isEmpty()) {
						return ComparisonResult.failure("Empty list does not contain all of " +formatMultiError(o2));
					}
					if (o2.getMulti().isEmpty()) {
						return ComparisonResult.failure(formatMultiError(o1) + " does not contain empty list");
					}
					boolean result =  o1.getMulti().containsAll(o2.getMulti());
					if (result) {
						return ComparisonResult.success();
					}
					else {
						return ComparisonResult.failure(formatMultiError(o1) + " does not contain all of " +formatMultiError(o2));
					}
				}
				
				// disjoint
				
				public static <T> ComparisonResult disjoint(Mapper<T> o1, Mapper<T> o2) {
					List<T> multi2 = o2.getMulti();
					List<T> multi1 = o1.getMulti();
					boolean result =  Collections.disjoint(multi1, multi2);
					if (result) {
						return ComparisonResult.success();
					}
					else {
						Collection<T> common = multi1.stream().filter(multi2::contains).collect(Collectors.toSet());
						return ComparisonResult.failure(formatMultiError(o1) + " is not disjoint from " +formatMultiError(o2) + "common items are " + common);
					}
				}
				
				// distinct
				
				public static <T> MapperC<T> distinct(Mapper<T> o) {
					return MapperC.of(o.getMulti()
							.stream()
							.distinct()
							.collect(Collectors.toList()));
				}
				
				public static ComparisonResult checkCardinality(String msgPrefix, int actual, int min, int max) {
					if (actual < min) {
						return ComparisonResult
								.failure("Minimum of " + min + " '" + msgPrefix + "' is expected but found " + actual + ".");
					} else if (max > 0 && actual > max) {
						return ComparisonResult
								.failure("Maximum of " + max + " '" + msgPrefix + "' are expected but found " + actual + ".");
					}
					return ComparisonResult.success();
				}
				
				public static ComparisonResult checkString(String msgPrefix, String value, int minLength, Optional<Integer> maxLength, Optional<Pattern> pattern) {
					if (value == null) {
						return ComparisonResult.success();
					}
					List<String> failures = new ArrayList<>();
					if (value.length() < minLength) {
						failures.add("Expected a minimum of " + minLength + " characters for '" + msgPrefix + "', but found '" + value + "' (" + value.length() + " characters).");
					}
					if (maxLength.isPresent()) {
						int m = maxLength.get();
						if (value.length() > m) {
							failures.add("Expected a maximum of " + m + " characters for '" + msgPrefix + "', but found '" + value + "' (" + value.length() + " characters).");
						}
					}
					if (pattern.isPresent()) {
						Pattern p = pattern.get();
						Matcher match = p.matcher(value);
						if (!match.matches()) {
							failures.add("'" + value + "' does not match the pattern /" + p.toString() + "/ of '" + msgPrefix + "'.");
						}
					}
					if (failures.isEmpty()) {
						return ComparisonResult.success();
					}
					return ComparisonResult.failure(
								failures.stream().collect(Collectors.joining(" "))
							);
				}
				public static ComparisonResult checkString(String msgPrefix, List<String> values, int minLength, Optional<Integer> maxLength, Optional<Pattern> pattern) {
					if (values == null) {
						return ComparisonResult.success();
					}
					List<String> failures = values.stream()
							.map(v -> checkString(msgPrefix, v, minLength, maxLength, pattern))
							.filter(r -> !r.get())
							.map(r -> r.getError())
							.collect(Collectors.toList());
					if (failures.isEmpty()) {
						return ComparisonResult.success();
					}
					return ComparisonResult.failure(
							failures.stream().collect(Collectors.joining(" - "))
						);
				}
				public static ComparisonResult checkNumber(String msgPrefix, BigDecimal value, Optional<Integer> digits, Optional<Integer> fractionalDigits, Optional<BigDecimal> min, Optional<BigDecimal> max) {
					if (value == null) {
						return ComparisonResult.success();
					}
					List<String> failures = new ArrayList<>();
					if (digits.isPresent()) {
						int d = digits.get();
						BigDecimal normalized = value.stripTrailingZeros();
						int actual = normalized.precision();
						if (normalized.scale() < 0) {
							actual -= normalized.scale(); // add unsignificant zeros
						}
						if (actual > d) {
							failures.add("Expected a maximum of " + d + " digits for '" + msgPrefix + "', but the number " + value + " has " + actual + ".");
						}
					}
					if (fractionalDigits.isPresent()) {
						int f = fractionalDigits.get();
						BigDecimal normalized = value.stripTrailingZeros();
						int actual = normalized.scale();
						if (normalized.scale() < 0) {
							actual = 0;
						}
						if (actual > f) {
							failures.add("Expected a maximum of " + f + " fractional digits for '" + msgPrefix + "', but the number " + value + " has " + actual + ".");
						}
					}
					if (min.isPresent()) {
						BigDecimal m = min.get();
						if (value.compareTo(m) < 0) {
							failures.add("Expected a number greater than or equal to " + m.toPlainString()+ " for '" + msgPrefix + "', but found " + value + ".");
						}
					}
					if (max.isPresent()) {
						BigDecimal m = max.get();
						if (value.compareTo(m) > 0) {
							failures.add("Expected a number less than or equal to " + m.toPlainString() + " for '" + msgPrefix + "', but found " + value + ".");
						}
					}
					if (failures.isEmpty()) {
						return ComparisonResult.success();
					}
					return ComparisonResult.failure(
								failures.stream().collect(Collectors.joining(" "))
							);
				}
				public static ComparisonResult checkNumber(String msgPrefix, Integer value, Optional<Integer> digits, Optional<Integer> fractionalDigits, Optional<BigDecimal> min, Optional<BigDecimal> max) {
					if (value == null) {
						return ComparisonResult.success();
					}
					return checkNumber(msgPrefix, BigDecimal.valueOf(value), digits, fractionalDigits, min, max);
				}
				public static ComparisonResult checkNumber(String msgPrefix, Long value, Optional<Integer> digits, Optional<Integer> fractionalDigits, Optional<BigDecimal> min, Optional<BigDecimal> max) {
					if (value == null) {
						return ComparisonResult.success();
					}
					return checkNumber(msgPrefix, BigDecimal.valueOf(value), digits, fractionalDigits, min, max);
				}
				public static ComparisonResult checkNumber(String msgPrefix, BigInteger value, Optional<Integer> digits, Optional<Integer> fractionalDigits, Optional<BigDecimal> min, Optional<BigDecimal> max) {
					if (value == null) {
						return ComparisonResult.success();
					}
					return checkNumber(msgPrefix, new BigDecimal(value), digits, fractionalDigits, min, max);
				}
				public static ComparisonResult checkNumber(String msgPrefix, List<? extends Number> values, Optional<Integer> digits, Optional<Integer> fractionalDigits, Optional<BigDecimal> min, Optional<BigDecimal> max) {
					if (values == null) {
						return ComparisonResult.success();
					}
					List<String> failures = values.stream()
							.map(v -> {
								if (v instanceof BigDecimal) {
									return checkNumber(msgPrefix, (BigDecimal)v, digits, fractionalDigits, min, max);
								}
								return checkNumber(msgPrefix, v.longValue(), digits, fractionalDigits, min, max);
							})
							.filter(r -> !r.get())
							.map(r -> r.getError())
							.collect(Collectors.toList());
					if (failures.isEmpty()) {
						return ComparisonResult.success();
					}
					return ComparisonResult.failure(
							failures.stream().collect(Collectors.joining(" - "))
						);
				}
				
				private static <T> String formatMultiError(Mapper<T> o) {
					T t = o.getMulti().stream().findAny().orElse(null);
					return t instanceof RosettaModelObject  ? 
							t.getClass().getSimpleName() : // for rosettaModelObjects only log class name otherwise error messages are way too long
							o.getMulti().toString();
				}
				
				// one-of and choice
			
				public static <T> ComparisonResult choice(Mapper<T> mapper, List<String> choiceFieldNames, ValidationResult.ChoiceRuleValidationMethod necessity) {
					T object = mapper.get();
					List<String> populatedFieldNames = new LinkedList<>();
					for (String a: choiceFieldNames) {
						try {
							Method getter = object.getClass().getMethod("get" + StringUtils.capitalize(a));
							if (ExistenceChecker.isSet(getter.invoke(object))) {
								populatedFieldNames.add(a);
							}
						} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
							throw new IllegalArgumentException(e);
						}
					}
							
					if (necessity.check(populatedFieldNames.size())) {
						return ComparisonResult.success();
					}
					String definition = choiceFieldNames.stream()
						.collect(Collectors.joining("', '", necessity.getDescription() + " of '", "'. "));
					String errorMessage = definition + (populatedFieldNames.isEmpty() ? "No fields are set." :
						populatedFieldNames.stream().collect(Collectors.joining("', '", "Set fields are '", "'.")));
					return ComparisonResult.failure(errorMessage);
				}
			}
			''')
		
		fsa.generateFile('com/rosetta/model/lib/meta/RosettaMetaData.java',
			'''
			package com.rosetta.model.lib.meta;
			
			import java.util.Collections;
			import java.util.List;
			import java.util.Set;
			import java.util.function.Function;
			
			import com.rosetta.model.lib.RosettaModelObject;
			import com.rosetta.model.lib.qualify.QualifyFunctionFactory;
			import com.rosetta.model.lib.qualify.QualifyResult;
			import com.rosetta.model.lib.validation.Validator;
			import com.rosetta.model.lib.validation.ValidatorFactory;
			import com.rosetta.model.lib.validation.ValidatorWithArg;
			
			public interface RosettaMetaData<T extends RosettaModelObject> {
			
				List<Validator<? super T>> dataRules(ValidatorFactory factory);
				
				// @Compat. This will be empty, as choice rules are now all data rules.
				@Deprecated
				default List<Validator<? super T>> choiceRuleValidators() {
					return Collections.emptyList();
				}
				
				List<Function<? super T, QualifyResult>> getQualifyFunctions(QualifyFunctionFactory factory);
				
				Validator<? super T> validator();
				
				Validator<? super T> typeFormatValidator();
				
				ValidatorWithArg<? super T, Set<String>> onlyExistsValidator();
			}
			''')
		
		fsa.generateFile('com/rosetta/model/lib/validation/ExistenceChecker.java',
			'''
			package com.rosetta.model.lib.validation;
			
			import java.util.List;
			import java.util.Objects;
			
			import com.rosetta.model.lib.RosettaModelObjectBuilder;
			
			public class ExistenceChecker {
				public static boolean isSet(Object field) {
					if (field == null) {
						return false;
					}
					if (field instanceof List) {
						@SuppressWarnings("unchecked")
						List<? extends Object> l = (List<? extends Object>)field;
						return l.size() > 0 && l.stream().anyMatch(Objects::nonNull);
					} else if (field instanceof RosettaModelObjectBuilder) {
						return ((RosettaModelObjectBuilder)field).hasData();
					}
					return true;
				}
				
				public static boolean isSet(RosettaModelObjectBuilder field) {
					return isSet((Object)field);
				}
				
				public static boolean isSet(List<? extends Object> field) {
					return isSet((Object)field);
				}
			}
			''')
		
		fsa.generateFile('com/rosetta/model/lib/validation/ValidationResult.java',
			'''
			package com.rosetta.model.lib.validation;
			
			import java.util.Optional;
			import java.util.List;
			import java.util.stream.Collectors;
			
			import com.rosetta.model.lib.path.RosettaPath;
			
			import java.util.function.Function;
			
			import static com.rosetta.model.lib.validation.ValidationResult.ValidationType.CHOICE_RULE;
			
			public interface ValidationResult<T> {
			
				boolean isSuccess();
			
				String getModelObjectName();
			
				String getName();
				
				ValidationType getValidationType();
			
				String getDefinition();
				
				Optional<String> getFailureReason();
				
				RosettaPath getPath();
			
				static <T> ValidationResult<T> success(String name, ValidationType validationType, String modelObjectName, RosettaPath path, String definition) {
					return new ModelValidationResult<>(name, validationType, modelObjectName, path, definition, Optional.empty());
				}
				
				static <T> ValidationResult<T> failure(String name, ValidationType validationType, String modelObjectName, RosettaPath path, String definition, String failureMessage) {
					return new ModelValidationResult<>(name, validationType, modelObjectName, path, definition, Optional.of(failureMessage));
				}
			
				enum ValidationType {
					DATA_RULE, CHOICE_RULE, CARDINALITY, TYPE_FORMAT, KEY, ONLY_EXISTS, POST_PROCESS_EXCEPTION
				}
			
				class ModelValidationResult<T> implements ValidationResult<T> {
			
					private final String modelObjectName;
					private final String name;
					private final String definition;
					private final Optional<String> failureReason;
					private final ValidationType validationType;
					private final RosettaPath path;
			
					public ModelValidationResult(String name, ValidationType validationType, String modelObjectName, RosettaPath path, String definition, Optional<String> failureReason) {
						this.name = name;
						this.validationType = validationType;
						this.path = path;
						this.modelObjectName = modelObjectName;
						this.definition = definition;
						this.failureReason = failureReason;
					}
			
					@Override
					public boolean isSuccess() {
						return !failureReason.isPresent();
					}
			
					@Override
					public String getModelObjectName() {
						return modelObjectName;
					}
			
					@Override
					public String getName() {
						return name;
					}
					
					public RosettaPath getPath() {
						return path;
					}
			
					@Override
					public String getDefinition() {
						return definition;
					}
					
					@Override
					public Optional<String> getFailureReason() {
						if (failureReason.isPresent() && modelObjectName.endsWith("Report") && failureReason.get().contains(modelObjectName)) {
							return getUpdatedFailureReason();
						}
						return failureReason;
					}
			
					@Override
					public ValidationType getValidationType() {
						return validationType;
					}
			
					@Override
					public String toString() {
						return String.format("Validation %s on [%s] for [%s] [%s] %s",
								isSuccess() ? "SUCCESS" : "FAILURE",
								path.buildPath(),
								validationType,
								name,
								failureReason.map(s -> "because [" + s + "]").orElse(""));
					}
			
					// TODO: refactor this method. This is an ugly hack.
					private Optional<String> getUpdatedFailureReason() {
			
						String conditionName = name.replaceFirst(modelObjectName, "");
						String failReason = failureReason.get();
			
						failReason = failReason.replaceAll(modelObjectName, "");
						failReason = failReason.replaceAll("->get", " ");
						failReason = failReason.replaceAll("[^\\w-]+", " ");
			
						return Optional.of(conditionName + ":- " + failReason);
					}
				}
			
				// @Compat. Choice rules are now obsolete in favor of data rules.
				@Deprecated
				class ChoiceRuleFailure<T> implements ValidationResult<T> {
			
					private final String name;
					private final String modelObjectName;
					private final List<String> populatedFields;
					private final List<String> choiceFieldNames;
					private final ChoiceRuleValidationMethod validationMethod;
					private final RosettaPath path;
			
					public ChoiceRuleFailure(String name, String modelObjectName, List<String> choiceFieldNames, RosettaPath path, List<String> populatedFields,
											 ChoiceRuleValidationMethod validationMethod) {
						this.name = name;
						this.path = path;
						this.modelObjectName = modelObjectName;
						this.populatedFields = populatedFields;
						this.choiceFieldNames = choiceFieldNames;
						this.validationMethod = validationMethod;
					}
			
					@Override
					public boolean isSuccess() {
						return false;
					}
			
					@Override
					public String getName() {
						return name;
					}
					
					public RosettaPath getPath() {
						return path;
					}
			
					@Override
					public String getModelObjectName() {
						return modelObjectName;
					}
			
					public List<String> populatedFields() {
						return populatedFields;
					}
			
					public List<String> choiceFieldNames() {
						return choiceFieldNames;
					}
			
					public ChoiceRuleValidationMethod validationMethod() {
						return validationMethod;
					}
			
					@Override
					public String getDefinition() {
						return choiceFieldNames.stream()
							.collect(Collectors.joining("', '", validationMethod.desc + " of '", "'. "));
					}
					
					@Override
					public Optional<String> getFailureReason() {
						return Optional.of(getDefinition() + (populatedFields.isEmpty() ? "No fields are set." :
								populatedFields.stream().collect(Collectors.joining("', '", "Set fields are '", "'."))));
					}
			
					@Override
					public ValidationType getValidationType() {
						return CHOICE_RULE;
					}
			
					@Override
					public String toString() {
						return String.format("Validation %s on [%s] for [%s] [%s] %s",
								isSuccess() ? "SUCCESS" : "FAILURE",
								path.buildPath(),
								CHOICE_RULE + ":" + validationMethod,
								name,
								getFailureReason().map(reason -> "because " + reason).orElse(""));
					}
				}
			
				enum ChoiceRuleValidationMethod {
			
					OPTIONAL("Zero or one field must be set", fieldCount -> fieldCount == 1 || fieldCount == 0),
					REQUIRED("One and only one field must be set", fieldCount -> fieldCount == 1);
			
					private final String desc;
					private final Function<Integer, Boolean> check;
			
					ChoiceRuleValidationMethod(String desc, Function<Integer, Boolean> check) {
						this.desc = desc;
						this.check = check;
					}
			
					public boolean check(int fields) {
						return check.apply(fields);
					}
					
					public String getDescription() {
						return this.desc;
					}
				}
				
				class ProcessValidationResult<T> implements ValidationResult<T> {
					private String message;
					private String modelObjectName;
					private String processorName;
					private RosettaPath path;
			
					public ProcessValidationResult(String message, String modelObjectName, String processorName, RosettaPath path) {
						this.message = message;
						this.modelObjectName = modelObjectName;
						this.processorName = processorName;
						this.path = path;
					}
			
					@Override
					public boolean isSuccess() {
						return false;
					}
			
					@Override
					public String getModelObjectName() {
						return modelObjectName;
					}
			
					@Override
					public String getName() {
						return processorName;
					}
			
					@Override
					public ValidationType getValidationType() {
						return ValidationType.POST_PROCESS_EXCEPTION;
					}
			
					@Override
					public String getDefinition() {
						return "";
					}
			
					@Override
					public Optional<String> getFailureReason() {
						return Optional.of(message);
					}
			
					@Override
					public RosettaPath getPath() {
						return path;
					}
				}
			}
			''')
	}
}