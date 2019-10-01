package com.regnosys.rosetta.generator.java.calculation

import com.google.inject.Inject
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static org.junit.jupiter.api.Assertions.*

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class RosettaCalculationGenerationTest {

	@Inject extension CalculationGeneratorHelper
	@Inject extension CodeGeneratorTestHelper
	
	@Test
	def void testSimpleTransDep() {
		val genereated = '''
			class Period {
				frequency int (1..1);
				periodEnum PeriodEnum (1..1);
				period number (1..1);
			}
			
			enum PeriodEnum {
				MONTH
			}
			
			func DayFraction :
				inputs: in2 Period( 1..1 )
				output: res number (1..1)
				alias p: PeriodEnumFunc(in2 -> periodEnum, in2)
				assign-output res: p / 360
				
			func PeriodEnumFunc :
				inputs:
					in1 PeriodEnum( 1..1 )
						in2 Period( 1..1 )
				output: out number( 1..1 )
			
			func PeriodEnumFunc(in1: PeriodEnum -> MONTH ):
				alias i: in2 -> frequency
				assign-output out: i * 30.0
		'''.generateCode.get("com.rosetta.test.model.functions.PeriodEnumFunc")

		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.google.inject.Inject;
				import com.rosetta.model.lib.functions.Mapper;
				import com.rosetta.model.lib.functions.MapperS;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.math.BigDecimalExtensions;
				import com.rosetta.test.model.Period;
				import com.rosetta.test.model.PeriodEnum;
				import java.lang.Integer;
				import java.lang.UnsupportedOperationException;
				import java.math.BigDecimal;
				
				
				/**
				 * @version test
				 */
				public class PeriodEnumFunc {
					
					@Inject protected PeriodEnumFunc.MONTH MONTH;
					
					public BigDecimal evaluate(PeriodEnum in1, Period in2) {
						switch (in1) {
							case MONTH:
								return MONTH.evaluate(in1, in2);
							default:
								throw new IllegalArgumentException("Enum value not implemented: " + in1);
						}
					}
					
					
					@ImplementedBy(MONTH.MONTHDefault.class)
					public static abstract class MONTH implements RosettaFunction {
					
						/**
						* @param in1 
						* @param in2 
						* @return out 
						*/
						public BigDecimal evaluate(PeriodEnum in1, Period in2) {
							
							BigDecimal out = doEvaluate(in1, in2);
							
							return out;
						}
						
						protected BigDecimal doEvaluate(PeriodEnum in1, Period in2) {
							Mapper<BigDecimal> outHolder = null;
							outHolder = MapperS.of(BigDecimalExtensions.multiply(BigDecimalExtensions.valueOf(i(in1, in2).get()), BigDecimalExtensions.valueOf(30.0)));
							return outHolder.get();
						}
						
						
						protected Mapper<Integer> i(PeriodEnum in1, Period in2) {
							return MapperS.of(in2).<Integer>map("getFrequency", Period::getFrequency);
						}
						public static final class MONTHDefault extends MONTH {
							@Override
							protected  BigDecimal doEvaluate(PeriodEnum in1, Period in2) {
								throw new UnsupportedOperationException("Function PeriodEnumFunc has operation implementation but is not annotated with 'calculation' annotation");
							}
						}
					}
				}
			'''.toString,
			genereated
		)

	}


	@Test
	def void testOnePlusOneGeneration() {
		'''
			func Calc:
				inputs:
					one int (1..1)
				output: out int (1..1)
				alias oneA : 1
				assign-output out: oneA + oneA
		'''.assertToGeneratedCalculation(
			'''
			package com.rosetta.test.model.functions;
			
			import com.google.inject.ImplementedBy;
			import com.rosetta.model.lib.functions.Mapper;
			import com.rosetta.model.lib.functions.MapperS;
			import com.rosetta.model.lib.functions.RosettaFunction;
			import java.lang.Integer;
			import java.lang.UnsupportedOperationException;
			
			
			@ImplementedBy(Calc.CalcDefault.class)
			public abstract class Calc implements RosettaFunction {
			
				/**
				* @param one 
				* @return out 
				*/
				public Integer evaluate(Integer one) {
					
					Integer out = doEvaluate(one);
					
					return out;
				}
				
				protected Integer doEvaluate(Integer one) {
					Mapper<Integer> outHolder = null;
					outHolder = MapperS.of((oneA(one).get() + oneA(one).get()));
					return outHolder.get();
				}
				
				
				protected Mapper<Integer> oneA(Integer one) {
					return MapperS.of(Integer.valueOf(1));
				}
				public static final class CalcDefault extends Calc {
					@Override
					protected  Integer doEvaluate(Integer one) {
						throw new UnsupportedOperationException("Function Calc has operation implementation but is not annotated with 'calculation' annotation");
					}
				}
			}
			'''
		)
	}
	
	@Test
	def void testSimpleCalculationGeneration() {
		'''
			func Calc:
				[calculation]
				inputs:
					arg1 int  (1..1)
					arg2 int  (1..1)
				output: res int (1..1)
				alias a1 : Min(1,2)
				alias a2 :  Max(1,2)
			
				assign-output res: a1 + a2 * 215
		'''.assertToGeneratedCalculation(
			'''
			package com.rosetta.test.model.functions;
			
			import com.google.inject.ImplementedBy;
			import com.rosetta.model.lib.functions.Mapper;
			import com.rosetta.model.lib.functions.MapperS;
			import com.rosetta.model.lib.functions.Max;
			import com.rosetta.model.lib.functions.Min;
			import com.rosetta.model.lib.functions.RosettaFunction;
			import java.lang.Integer;
			
			
			@ImplementedBy(Calc.CalcDefault.class)
			public abstract class Calc implements RosettaFunction {
			
				/**
				* @param arg1 
				* @param arg2 
				* @return res 
				*/
				public Integer evaluate(Integer arg1, Integer arg2) {
					
					Integer res = doEvaluate(arg1, arg2);
					
					return res;
				}
				
				protected Integer doEvaluate(Integer arg1, Integer arg2) {
					Mapper<Integer> resHolder = null;
					resHolder = MapperS.of((a1(arg1, arg2).get() + (a2(arg1, arg2).get() * 215)));
					return resHolder.get();
				}
				
				
				protected Mapper<Integer> a1(Integer arg1, Integer arg2) {
					return MapperS.of(new Min().execute(MapperS.of(Integer.valueOf(1)).get(), MapperS.of(Integer.valueOf(2)).get()));
				}
				
				protected Mapper<Integer> a2(Integer arg1, Integer arg2) {
					return MapperS.of(new Max().execute(MapperS.of(Integer.valueOf(1)).get(), MapperS.of(Integer.valueOf(2)).get()));
				}
				public static final class CalcDefault extends Calc {
					@Override
					protected  Integer doEvaluate(Integer arg1, Integer arg2) {
						return super.doEvaluate(arg1, arg2);
					}
				}
			}
			'''
		)
	}

	@Test
	def void testDateTimeAdd() {
		val calculation = '''
			class FuncIn {
				val1 date (1..1);
				val2 time (1..1);
			}
			data FoncOut: 
				res1 string (1..1)
				res2 string (1..1)
			
			func Calc:
				inputs:
					funIn FuncIn(1..1)
			
				output:
					res FoncOut(1..1)
				alias arg1: funIn-> val1
				alias arg2: funIn-> val2
				assign-output res -> res1:  arg1 + arg2 
				assign-output res -> res2:  arg1 + arg2 
		'''.generateCode
		val calcJava = calculation.get("com.rosetta.test.model.functions.Calc")
		//RosettaBlueprintTest.writeOutClasses(calculation, "testDateTimeAdd")
		calculation.compileToClasses
		val expected = '''
		package com.rosetta.test.model.functions;
		
		import com.google.inject.ImplementedBy;
		import com.google.inject.Inject;
		import com.rosetta.model.lib.functions.Mapper;
		import com.rosetta.model.lib.functions.MapperMaths;
		import com.rosetta.model.lib.functions.MapperS;
		import com.rosetta.model.lib.functions.RosettaFunction;
		import com.rosetta.model.lib.records.Date;
		import com.rosetta.model.lib.validation.ModelObjectValidator;
		import com.rosetta.test.model.FoncOut;
		import com.rosetta.test.model.FuncIn;
		import java.lang.SuppressWarnings;
		import java.lang.UnsupportedOperationException;
		import java.time.LocalTime;
		
		
		@ImplementedBy(Calc.CalcDefault.class)
		public abstract class Calc implements RosettaFunction {
			
			@Inject protected ModelObjectValidator objectValidator;
		
			/**
			* @param funIn 
			* @return res 
			*/
			public FoncOut evaluate(FuncIn funIn) {
				
				FoncOut res = doEvaluate(funIn).build();
				
				objectValidator.validateAndFailOnErorr(FoncOut.class, res);
				return res;
			}
			
			protected FoncOut.FoncOutBuilder doEvaluate(FuncIn funIn) {
				FoncOut.FoncOutBuilder resHolder = FoncOut.builder();
				@SuppressWarnings("unused") FoncOut res = resHolder.build();
				resHolder
					.setRes1(MapperMaths.<String, Date, LocalTime>add(MapperS.of(arg1(funIn).get()), MapperS.of(arg2(funIn).get())).get());
				;
				res = resHolder.build();
				resHolder
					.setRes2(MapperMaths.<String, Date, LocalTime>add(MapperS.of(arg1(funIn).get()), MapperS.of(arg2(funIn).get())).get());
				;
				return resHolder;
			}
			
			
			protected Mapper<Date> arg1(FuncIn funIn) {
				return MapperS.of(funIn).<Date>map("getVal1", FuncIn::getVal1);
			}
			
			protected Mapper<LocalTime> arg2(FuncIn funIn) {
				return MapperS.of(funIn).<LocalTime>map("getVal2", FuncIn::getVal2);
			}
			public static final class CalcDefault extends Calc {
				@Override
				protected  FoncOut.FoncOutBuilder doEvaluate(FuncIn funIn) {
					throw new UnsupportedOperationException("Function Calc has operation implementation but is not annotated with 'calculation' annotation");
				}
			}
		}
		'''
		assertEquals(expected, calcJava)
	}

	@Test
	def void testWierdness() {
		val calculation = '''			
			class FuncIn {
				valS string (1..1);
				val1 date (1..1);
				val2 time (1..1);
			}
			class FuncOut {
				transactionReferenceNumber string (1..1);
				tradingDateTime string (1..1);
			}
			
			func RTS_22_Fields :
				[calculation]
				inputs: funcIn FuncIn (1..1)
			
				output: out FuncOut (1..1)
				alias linkId: funcIn -> valS
				alias tradeDate: funcIn -> val1
				alias tradeTime: funcIn -> val2
				assign-output out -> transactionReferenceNumber: "SPH"+linkId
				assign-output out -> tradingDateTime:
					tradeDate + tradeTime
		'''.generateCode
		val calcJava = calculation.get("com.rosetta.test.model.functions.RTS_22_Fields")
		//RosettaBlueprintTest.writeOutClasses(calculation, "testWierdness")
		calculation.compileToClasses
		val expected = '''
		package com.rosetta.test.model.functions;
		
		import com.google.inject.ImplementedBy;
		import com.google.inject.Inject;
		import com.rosetta.model.lib.functions.Mapper;
		import com.rosetta.model.lib.functions.MapperMaths;
		import com.rosetta.model.lib.functions.MapperS;
		import com.rosetta.model.lib.functions.RosettaFunction;
		import com.rosetta.model.lib.records.Date;
		import com.rosetta.model.lib.validation.ModelObjectValidator;
		import com.rosetta.test.model.FuncIn;
		import com.rosetta.test.model.FuncOut;
		import java.lang.String;
		import java.lang.SuppressWarnings;
		import java.time.LocalTime;
		
		
		@ImplementedBy(RTS_22_Fields.RTS_22_FieldsDefault.class)
		public abstract class RTS_22_Fields implements RosettaFunction {
			
			@Inject protected ModelObjectValidator objectValidator;
		
			/**
			* @param funcIn 
			* @return out 
			*/
			public FuncOut evaluate(FuncIn funcIn) {
				
				FuncOut out = doEvaluate(funcIn).build();
				
				objectValidator.validateAndFailOnErorr(FuncOut.class, out);
				return out;
			}
			
			protected FuncOut.FuncOutBuilder doEvaluate(FuncIn funcIn) {
				FuncOut.FuncOutBuilder outHolder = FuncOut.builder();
				@SuppressWarnings("unused") FuncOut out = outHolder.build();
				outHolder
					.setTransactionReferenceNumber(MapperMaths.<String, String, String>add(MapperS.of("SPH"), MapperS.of(linkId(funcIn).get())).get());
				;
				out = outHolder.build();
				outHolder
					.setTradingDateTime(MapperMaths.<String, Date, LocalTime>add(MapperS.of(tradeDate(funcIn).get()), MapperS.of(tradeTime(funcIn).get())).get());
				;
				return outHolder;
			}
			
			
			protected Mapper<String> linkId(FuncIn funcIn) {
				return MapperS.of(funcIn).<String>map("getValS", FuncIn::getValS);
			}
			
			protected Mapper<Date> tradeDate(FuncIn funcIn) {
				return MapperS.of(funcIn).<Date>map("getVal1", FuncIn::getVal1);
			}
			
			protected Mapper<LocalTime> tradeTime(FuncIn funcIn) {
				return MapperS.of(funcIn).<LocalTime>map("getVal2", FuncIn::getVal2);
			}
			public static final class RTS_22_FieldsDefault extends RTS_22_Fields {
				@Override
				protected  FuncOut.FuncOutBuilder doEvaluate(FuncIn funcIn) {
					return super.doEvaluate(funcIn);
				}
			}
		}
		'''
		assertEquals(expected, calcJava)
	}

	@Disabled
	@Test
	def void testSimpleCalculationGeneration2() {
		'''			
			calculation Calc {
				res defined by: arg1 + arg2 * 215
			}
			 
			arguments Calc {
				arg1 int : is FuncIn->val1
				arg2 int : is FuncIn->val2
			}
			
			class FuncIn {
				val1 int (1..1);
				val2 int (1..1);
			}
		'''.assertToGeneratedCalculation(
			'''
				package com.rosetta.test.model.calculation;
				
				import com.rosetta.model.lib.functions.IResult;
				import com.rosetta.test.model.FuncIn;
				import java.util.Arrays;
				import java.util.List;
				
				public class Calc {
					
					public Result calculate(FuncIn paramFuncIn) {
						Input input = new Input().create(paramFuncIn);
						Result result = new Result();
						result.res = input.arg1 + input.arg2 * 215;
						return result;
					}
						
					public static class Input {
						private Integer arg1;
						private Integer arg2;
						
						public Input create(FuncIn inputParam) {
							this.arg1 = MapperS.of(inputParam).map("FuncIn->val1", FuncIn::getVal1).get();
							this.arg2 = MapperS.of(inputParam).map("FuncIn->val1", FuncIn::getVal2).get();
							return this;
						}
					}
					
					public static class Result implements IResult {
						private Integer res;
						
						public Integer getRes() {
							return this.res;
						}
						
						public Result setRes(Integer res) {
							this.res = res;
							return this;
						}
						
						private static final List<Attribute<?>> ATTRIBUTES =  Arrays.asList(
							new Attribute<>("res", Integer.class, (IResult res) -> ((Result) res).getRes())
						);
					
						@Override
						public List<Attribute<?>> getAttributes() {
							return ATTRIBUTES;
						}
						
						@Override
						public boolean equals(Object o) {
							if (this == o) return true;
							if (o == null || getClass() != o.getClass()) return false;
						
							Result _that = (Result) o;
						
							if (res != null ? !res.equals(_that.res) : _that.res != null) return false;
							return true;
						}
						
						@Override
						public int hashCode() {
							int _result = 0;
							_result = 31 * _result + (res != null ? res.hashCode() : 0);
							return _result;
						}
						
						@Override
						public String toString() {
							return "Result {" +
								"res=" + this.res +
							'}';
						}
					}
				}
			'''
		)
	}


	@Test
	def void shouldResolveFunctionDependencies() {
		'''
			func Adder:
				output: res int (1..1)
				alias arg1 : AddOne( 1 )
				assign-output res : arg1
			
			func AddOne:
				inputs:  arg int (1..1)
				output: out int(1..1)
		'''.assertToGeneratedCalculation(
			'''
			package com.rosetta.test.model.functions;
			
			import com.google.inject.ImplementedBy;
			import com.google.inject.Inject;
			import com.rosetta.model.lib.functions.Mapper;
			import com.rosetta.model.lib.functions.MapperS;
			import com.rosetta.model.lib.functions.RosettaFunction;
			import com.rosetta.test.model.functions.AddOne;
			import java.lang.Integer;
			import java.lang.UnsupportedOperationException;
			
			
			@ImplementedBy(Adder.AdderDefault.class)
			public abstract class Adder implements RosettaFunction {
				
				// RosettaFunction dependencies
				//
				@Inject protected AddOne addOne;
			
				/**
				* @return res 
				*/
				public Integer evaluate() {
					
					Integer res = doEvaluate();
					
					return res;
				}
				
				protected Integer doEvaluate() {
					Mapper<Integer> resHolder = null;
					resHolder = MapperS.of(arg1().get());
					return resHolder.get();
				}
				
				
				protected Mapper<Integer> arg1() {
					return MapperS.of(addOne.evaluate(MapperS.of(Integer.valueOf(1)).get()));
				}
				public static final class AdderDefault extends Adder {
					@Override
					protected  Integer doEvaluate() {
						throw new UnsupportedOperationException("Function Adder has operation implementation but is not annotated with 'calculation' annotation");
					}
				}
			}
			'''
		)
	}

	
	@Test
	def void shouldResolveExternalFunctionDependenciesWhenEnumCalculation() {
		val generated = '''
			class MathInput
			{
				mathInput string (1..1);
				math Math (1..1);
			}
			
			func AddOne:
				inputs: arg string (1..1)
				output: out string (1..1)
						
			func SubOne:
				inputs: arg string (1..1)
				output: out string (1..1)
			
			
			enum Math
			{
				INCR,
				DECR
			}
			
			func MathFunc:
				inputs:
					in1 Math (1..1)
					in2 MathInput (1..1)
				output: arg1 string (1..1)
			
			func MathFunc (in1 : Math -> INCR ):
				assign-output arg1: AddOne(in2 -> mathInput)
				
			func MathFunc (in1 : Math -> DECR ):
				assign-output arg1: SubOne(in2 -> mathInput)
		'''.generateCode
		.get("com.rosetta.test.model.functions.MathFunc")
		assertEquals(
			'''
			package com.rosetta.test.model.functions;
			
			import com.google.inject.ImplementedBy;
			import com.google.inject.Inject;
			import com.rosetta.model.lib.functions.Mapper;
			import com.rosetta.model.lib.functions.MapperS;
			import com.rosetta.model.lib.functions.RosettaFunction;
			import com.rosetta.test.model.Math;
			import com.rosetta.test.model.MathInput;
			import com.rosetta.test.model.functions.AddOne;
			import com.rosetta.test.model.functions.SubOne;
			import java.lang.String;
			import java.lang.UnsupportedOperationException;
			
			
			/**
			 * @version test
			 */
			public class MathFunc {
				
				@Inject protected MathFunc.INCR INCR;
				@Inject protected MathFunc.DECR DECR;
				
				public String evaluate(Math in1, MathInput in2) {
					switch (in1) {
						case INCR:
							return INCR.evaluate(in1, in2);
						case DECR:
							return DECR.evaluate(in1, in2);
						default:
							throw new IllegalArgumentException("Enum value not implemented: " + in1);
					}
				}
				
				
				@ImplementedBy(INCR.INCRDefault.class)
				public static abstract class INCR implements RosettaFunction {
					
					// RosettaFunction dependencies
					//
					@Inject protected AddOne addOne;
				
					/**
					* @param in1 
					* @param in2 
					* @return arg1 
					*/
					public String evaluate(Math in1, MathInput in2) {
						
						String arg1 = doEvaluate(in1, in2);
						
						return arg1;
					}
					
					protected String doEvaluate(Math in1, MathInput in2) {
						Mapper<String> arg1Holder = null;
						arg1Holder = MapperS.of(addOne.evaluate(in2.getMathInput()));
						return arg1Holder.get();
					}
					
					public static final class INCRDefault extends INCR {
						@Override
						protected  String doEvaluate(Math in1, MathInput in2) {
							throw new UnsupportedOperationException("Function MathFunc has operation implementation but is not annotated with 'calculation' annotation");
						}
					}
				}
				
				@ImplementedBy(DECR.DECRDefault.class)
				public static abstract class DECR implements RosettaFunction {
					
					// RosettaFunction dependencies
					//
					@Inject protected SubOne subOne;
				
					/**
					* @param in1 
					* @param in2 
					* @return arg1 
					*/
					public String evaluate(Math in1, MathInput in2) {
						
						String arg1 = doEvaluate(in1, in2);
						
						return arg1;
					}
					
					protected String doEvaluate(Math in1, MathInput in2) {
						Mapper<String> arg1Holder = null;
						arg1Holder = MapperS.of(subOne.evaluate(in2.getMathInput()));
						return arg1Holder.get();
					}
					
					public static final class DECRDefault extends DECR {
						@Override
						protected  String doEvaluate(Math in1, MathInput in2) {
							throw new UnsupportedOperationException("Function MathFunc has operation implementation but is not annotated with 'calculation' annotation");
						}
					}
				}
			}
			'''.toString, generated)
	}
	
	@Test
	def void shouldResolveFunctionDependenciesWhenReferencedInAlias() {
		'''	
			func Adder :
				inputs: arg1 int (1..1)
				output: res int (1..1)
				
				alias addedOne: AddOne( 1 )
				assign-output res: addedOne
			
			func AddOne:
				inputs: arg int (1..1)
				output: out int (1..1)
		'''.assertToGeneratedCalculation(
			'''
			package com.rosetta.test.model.functions;
			
			import com.google.inject.ImplementedBy;
			import com.google.inject.Inject;
			import com.rosetta.model.lib.functions.Mapper;
			import com.rosetta.model.lib.functions.MapperS;
			import com.rosetta.model.lib.functions.RosettaFunction;
			import com.rosetta.test.model.functions.AddOne;
			import java.lang.Integer;
			import java.lang.UnsupportedOperationException;
			
			
			@ImplementedBy(Adder.AdderDefault.class)
			public abstract class Adder implements RosettaFunction {
				
				// RosettaFunction dependencies
				//
				@Inject protected AddOne addOne;
			
				/**
				* @param arg1 
				* @return res 
				*/
				public Integer evaluate(Integer arg1) {
					
					Integer res = doEvaluate(arg1);
					
					return res;
				}
				
				protected Integer doEvaluate(Integer arg1) {
					Mapper<Integer> resHolder = null;
					resHolder = MapperS.of(addedOne(arg1).get());
					return resHolder.get();
				}
				
				
				protected Mapper<Integer> addedOne(Integer arg1) {
					return MapperS.of(addOne.evaluate(MapperS.of(Integer.valueOf(1)).get()));
				}
				public static final class AdderDefault extends Adder {
					@Override
					protected  Integer doEvaluate(Integer arg1) {
						throw new UnsupportedOperationException("Function Adder has operation implementation but is not annotated with 'calculation' annotation");
					}
				}
			}
			'''
		)
	}

}
