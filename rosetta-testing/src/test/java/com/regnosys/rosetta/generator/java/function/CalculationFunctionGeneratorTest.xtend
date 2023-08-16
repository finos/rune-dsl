package com.regnosys.rosetta.generator.java.function

import com.google.inject.Inject
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static org.junit.jupiter.api.Assertions.*

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class CalculationFunctionGeneratorTest {

	@Inject extension FunctionGeneratorHelper
	@Inject extension CodeGeneratorTestHelper
	
	@Test
	def void testSimpleTransDep() {
		val genereated = '''
			type Period:
				frequency int (1..1)
				periodEnum PeriodEnum (1..1)
				period number (1..1)
			
			enum PeriodEnum:MONTH
			
			func DayFraction :
				inputs: in2 Period( 1..1 )
				output: res number (1..1)
				alias p: PeriodEnumFunc(in2 -> periodEnum, in2)
				set res: p / 360
				
			func PeriodEnumFunc :
				inputs:
					in1 PeriodEnum( 1..1 )
						in2 Period( 1..1 )
				output: out number( 1..1 )
			
			func PeriodEnumFunc(in1: PeriodEnum -> MONTH ):
				alias i: in2 -> frequency
				set out: i * 30.0
		'''.generateCode.get("com.rosetta.test.model.functions.PeriodEnumFunc")

		assertEquals(
			'''
			package com.rosetta.test.model.functions;
			
			import com.google.inject.ImplementedBy;
			import com.google.inject.Inject;
			import com.rosetta.model.lib.expression.MapperMaths;
			import com.rosetta.model.lib.functions.RosettaFunction;
			import com.rosetta.model.lib.mapper.Mapper;
			import com.rosetta.model.lib.mapper.MapperS;
			import com.rosetta.test.model.Period;
			import com.rosetta.test.model.PeriodEnum;
			import java.math.BigDecimal;
			
			
			/**
			 * @version test
			 */
			public class PeriodEnumFunc {
				
				@Inject protected PeriodEnumFunc.mONTH_ mONTH_;
				
				public BigDecimal evaluate(PeriodEnum in1, Period in2) {
					switch (in1) {
						case MONTH:
							return mONTH_.evaluate(in1, in2);
						default:
							throw new IllegalArgumentException("Enum value not implemented: " + in1);
					}
				}
				
				@ImplementedBy(PeriodEnumFuncMONTH.PeriodEnumFuncMONTHDefault.class)
				public abstract class PeriodEnumFuncMONTH implements RosettaFunction {
				
					/**
					* @param in1 
					* @param in2 
					* @return out 
					*/
					public BigDecimal evaluate(PeriodEnum in1, Period in2) {
						BigDecimal out = doEvaluate(in1, in2);
						
						return out;
					}
				
					protected abstract BigDecimal doEvaluate(PeriodEnum in1, Period in2);
				
					protected abstract Mapper<Integer> i(PeriodEnum in1, Period in2);
				
					public static class PeriodEnumFuncMONTHDefault extends PeriodEnumFuncMONTH {
						@Override
						protected BigDecimal doEvaluate(PeriodEnum in1, Period in2) {
							BigDecimal out = null;
							return assignOutput(out, in1, in2);
						}
						
						protected BigDecimal assignOutput(BigDecimal out, PeriodEnum in1, Period in2) {
							out = MapperMaths.<BigDecimal, Integer, BigDecimal>multiply(MapperS.of(i(in1, in2).get()), MapperS.of(new BigDecimal("30.0"))).get();
							
							return out;
						}
						
						@Override
						protected Mapper<Integer> i(PeriodEnum in1, Period in2) {
							return MapperS.of(in2).<Integer>map("getFrequency", period -> period.getFrequency());
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
				set out: oneA + oneA
		'''.assertToGeneratedCalculation(
			'''
			package com.rosetta.test.model.functions;
			
			import com.google.inject.ImplementedBy;
			import com.rosetta.model.lib.expression.MapperMaths;
			import com.rosetta.model.lib.functions.RosettaFunction;
			import com.rosetta.model.lib.mapper.Mapper;
			import com.rosetta.model.lib.mapper.MapperS;
			
			
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
			
				protected abstract Integer doEvaluate(Integer one);
			
				protected abstract Mapper<Integer> oneA(Integer one);
			
				public static class CalcDefault extends Calc {
					@Override
					protected Integer doEvaluate(Integer one) {
						Integer out = null;
						return assignOutput(out, one);
					}
					
					protected Integer assignOutput(Integer out, Integer one) {
						out = MapperMaths.<Integer, Integer, Integer>add(MapperS.of(oneA(one).get()), MapperS.of(oneA(one).get())).get();
						
						return out;
					}
					
					@Override
					protected Mapper<Integer> oneA(Integer one) {
						return MapperS.of(Integer.valueOf(1));
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
			
				set res: a1 + a2 * 215
		'''.assertToGeneratedCalculation(
			'''
			package com.rosetta.test.model.functions;
			
			import com.google.inject.ImplementedBy;
			import com.rosetta.model.lib.expression.MapperMaths;
			import com.rosetta.model.lib.functions.Max;
			import com.rosetta.model.lib.functions.Min;
			import com.rosetta.model.lib.functions.RosettaFunction;
			import com.rosetta.model.lib.mapper.Mapper;
			import com.rosetta.model.lib.mapper.MapperS;
			
			
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
			
				protected abstract Integer doEvaluate(Integer arg1, Integer arg2);
			
				protected abstract Mapper<Integer> a1(Integer arg1, Integer arg2);
			
				protected abstract Mapper<Integer> a2(Integer arg1, Integer arg2);
			
				public static class CalcDefault extends Calc {
					@Override
					protected Integer doEvaluate(Integer arg1, Integer arg2) {
						Integer res = null;
						return assignOutput(res, arg1, arg2);
					}
					
					protected Integer assignOutput(Integer res, Integer arg1, Integer arg2) {
						res = MapperMaths.<Integer, Integer, Integer>add(MapperS.of(a1(arg1, arg2).get()), MapperMaths.<Integer, Integer, Integer>multiply(MapperS.of(a2(arg1, arg2).get()), MapperS.of(Integer.valueOf(215)))).get();
						
						return res;
					}
					
					@Override
					protected Mapper<Integer> a1(Integer arg1, Integer arg2) {
						return MapperS.of(new Min().execute(MapperS.of(Integer.valueOf(1)).get(), MapperS.of(Integer.valueOf(2)).get()));
					}
					
					@Override
					protected Mapper<Integer> a2(Integer arg1, Integer arg2) {
						return MapperS.of(new Max().execute(MapperS.of(Integer.valueOf(1)).get(), MapperS.of(Integer.valueOf(2)).get()));
					}
				}
			}
			'''
		)
	}

	@Test
	def void testDateTimeAdd() {
		val calculation = '''
			type FuncIn:
				val1 date (1..1)
				val2 time (1..1)

			type FoncOut: 
				res1 dateTime (1..1)
				res2 dateTime (1..1)
			
			func Calc:
				inputs:
					funIn FuncIn(1..1)
			
				output:
					res FoncOut(1..1)
				alias arg1: funIn-> val1
				alias arg2: funIn-> val2
				set res -> res1:  arg1 + arg2 
				set res -> res2:  arg1 + arg2 
		'''.generateCode
		val calcJava = calculation.get("com.rosetta.test.model.functions.Calc")
		//RosettaBlueprintTest.writeOutClasses(calculation, "testDateTimeAdd")
		calculation.compileToClasses
		val expected = '''
			package com.rosetta.test.model.functions;
			
			import com.google.inject.ImplementedBy;
			import com.google.inject.Inject;
			import com.rosetta.model.lib.expression.MapperMaths;
			import com.rosetta.model.lib.functions.ModelObjectValidator;
			import com.rosetta.model.lib.functions.RosettaFunction;
			import com.rosetta.model.lib.mapper.Mapper;
			import com.rosetta.model.lib.mapper.MapperS;
			import com.rosetta.model.lib.records.Date;
			import com.rosetta.test.model.FoncOut;
			import com.rosetta.test.model.FoncOut.FoncOutBuilder;
			import com.rosetta.test.model.FuncIn;
			import java.time.LocalDateTime;
			import java.time.LocalTime;
			import java.util.Optional;
			
			
			@ImplementedBy(Calc.CalcDefault.class)
			public abstract class Calc implements RosettaFunction {
				
				@Inject protected ModelObjectValidator objectValidator;
			
				/**
				* @param funIn 
				* @return res 
				*/
				public FoncOut evaluate(FuncIn funIn) {
					FoncOut.FoncOutBuilder res = doEvaluate(funIn);
					
					if (res != null) {
						objectValidator.validate(FoncOut.class, res);
					}
					return res;
				}
			
				protected abstract FoncOut.FoncOutBuilder doEvaluate(FuncIn funIn);
			
				protected abstract Mapper<Date> arg1(FuncIn funIn);
			
				protected abstract Mapper<LocalTime> arg2(FuncIn funIn);
			
				public static class CalcDefault extends Calc {
					@Override
					protected FoncOut.FoncOutBuilder doEvaluate(FuncIn funIn) {
						FoncOut.FoncOutBuilder res = FoncOut.builder();
						return assignOutput(res, funIn);
					}
					
					protected FoncOut.FoncOutBuilder assignOutput(FoncOut.FoncOutBuilder res, FuncIn funIn) {
						res
							.setRes1(MapperMaths.<LocalDateTime, Date, LocalTime>add(MapperS.of(arg1(funIn).get()), MapperS.of(arg2(funIn).get())).get());
						
						res
							.setRes2(MapperMaths.<LocalDateTime, Date, LocalTime>add(MapperS.of(arg1(funIn).get()), MapperS.of(arg2(funIn).get())).get());
						
						return Optional.ofNullable(res)
							.map(o -> o.prune())
							.orElse(null);
					}
					
					@Override
					protected Mapper<Date> arg1(FuncIn funIn) {
						return MapperS.of(funIn).<Date>map("getVal1", funcIn -> funcIn.getVal1());
					}
					
					@Override
					protected Mapper<LocalTime> arg2(FuncIn funIn) {
						return MapperS.of(funIn).<LocalTime>map("getVal2", funcIn -> funcIn.getVal2());
					}
				}
			}
			'''
		assertEquals(expected, calcJava)
	}

	@Test
	def void testWierdness() {
		val calculation = '''
			type FuncIn:
				valS string (1..1)
				val1 date (1..1)
				val2 time (1..1)

			type FuncOut:
				transactionReferenceNumber string (1..1)
				tradingDateTime dateTime (1..1)
			
			func RTS_22_Fields :
				[calculation]
				inputs: funcIn FuncIn (1..1)
			
				output: out FuncOut (1..1)
				alias linkId: funcIn -> valS
				alias tradeDate: funcIn -> val1
				alias tradeTime: funcIn -> val2
				set out -> transactionReferenceNumber: "SPH"+linkId
				set out -> tradingDateTime:
					tradeDate + tradeTime
		'''.generateCode
		val calcJava = calculation.get("com.rosetta.test.model.functions.RTS_22_Fields")
		//RosettaBlueprintTest.writeOutClasses(calculation, "testWierdness")
		calculation.compileToClasses
		val expected = '''
			package com.rosetta.test.model.functions;
			
			import com.google.inject.ImplementedBy;
			import com.google.inject.Inject;
			import com.rosetta.model.lib.expression.MapperMaths;
			import com.rosetta.model.lib.functions.ModelObjectValidator;
			import com.rosetta.model.lib.functions.RosettaFunction;
			import com.rosetta.model.lib.mapper.Mapper;
			import com.rosetta.model.lib.mapper.MapperS;
			import com.rosetta.model.lib.records.Date;
			import com.rosetta.test.model.FuncIn;
			import com.rosetta.test.model.FuncOut;
			import com.rosetta.test.model.FuncOut.FuncOutBuilder;
			import java.time.LocalDateTime;
			import java.time.LocalTime;
			import java.util.Optional;
			
			
			@ImplementedBy(RTS_22_Fields.RTS_22_FieldsDefault.class)
			public abstract class RTS_22_Fields implements RosettaFunction {
				
				@Inject protected ModelObjectValidator objectValidator;
			
				/**
				* @param funcIn 
				* @return out 
				*/
				public FuncOut evaluate(FuncIn funcIn) {
					FuncOut.FuncOutBuilder out = doEvaluate(funcIn);
					
					if (out != null) {
						objectValidator.validate(FuncOut.class, out);
					}
					return out;
				}
			
				protected abstract FuncOut.FuncOutBuilder doEvaluate(FuncIn funcIn);
			
				protected abstract Mapper<String> linkId(FuncIn funcIn);
			
				protected abstract Mapper<Date> tradeDate(FuncIn funcIn);
			
				protected abstract Mapper<LocalTime> tradeTime(FuncIn funcIn);
			
				public static class RTS_22_FieldsDefault extends RTS_22_Fields {
					@Override
					protected FuncOut.FuncOutBuilder doEvaluate(FuncIn funcIn) {
						FuncOut.FuncOutBuilder out = FuncOut.builder();
						return assignOutput(out, funcIn);
					}
					
					protected FuncOut.FuncOutBuilder assignOutput(FuncOut.FuncOutBuilder out, FuncIn funcIn) {
						out
							.setTransactionReferenceNumber(MapperMaths.<String, String, String>add(MapperS.of("SPH"), MapperS.of(linkId(funcIn).get())).get());
						
						out
							.setTradingDateTime(MapperMaths.<LocalDateTime, Date, LocalTime>add(MapperS.of(tradeDate(funcIn).get()), MapperS.of(tradeTime(funcIn).get())).get());
						
						return Optional.ofNullable(out)
							.map(o -> o.prune())
							.orElse(null);
					}
					
					@Override
					protected Mapper<String> linkId(FuncIn funcIn) {
						return MapperS.of(funcIn).<String>map("getValS", _funcIn -> _funcIn.getValS());
					}
					
					@Override
					protected Mapper<Date> tradeDate(FuncIn funcIn) {
						return MapperS.of(funcIn).<Date>map("getVal1", _funcIn -> _funcIn.getVal1());
					}
					
					@Override
					protected Mapper<LocalTime> tradeTime(FuncIn funcIn) {
						return MapperS.of(funcIn).<LocalTime>map("getVal2", _funcIn -> _funcIn.getVal2());
					}
				}
			}
			'''
		assertEquals(expected, calcJava)
	}

	@Test
	def void testAsKeyGenerationMultiValue() {
		'''
		type WithMeta:
			[metadata key]
		
		type OtherType:
			attrSingle WithMeta (0..1)
			[metadata reference]
			attrMulti WithMeta (0..*)
			[metadata reference]
			
		func asKeyUsage:
			inputs: withMeta WithMeta(0..*)
			output: out OtherType (0..1)
			add out -> attrMulti:
				withMeta as-key
			set out -> attrSingle:
				withMeta only-element as-key
		'''.assertToGeneratedCalculation(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.google.inject.Inject;
				import com.rosetta.model.lib.functions.ModelObjectValidator;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.test.model.OtherType;
				import com.rosetta.test.model.OtherType.OtherTypeBuilder;
				import com.rosetta.test.model.WithMeta;
				import com.rosetta.test.model.metafields.ReferenceWithMetaWithMeta;
				import java.util.List;
				import java.util.Optional;
				import java.util.stream.Collectors;
				
				
				@ImplementedBy(asKeyUsage.asKeyUsageDefault.class)
				public abstract class asKeyUsage implements RosettaFunction {
					
					@Inject protected ModelObjectValidator objectValidator;
				
					/**
					* @param withMeta 
					* @return out 
					*/
					public OtherType evaluate(List<? extends WithMeta> withMeta) {
						OtherType.OtherTypeBuilder out = doEvaluate(withMeta);
						
						if (out != null) {
							objectValidator.validate(OtherType.class, out);
						}
						return out;
					}
				
					protected abstract OtherType.OtherTypeBuilder doEvaluate(List<? extends WithMeta> withMeta);
				
					public static class asKeyUsageDefault extends asKeyUsage {
						@Override
						protected OtherType.OtherTypeBuilder doEvaluate(List<? extends WithMeta> withMeta) {
							OtherType.OtherTypeBuilder out = OtherType.builder();
							return assignOutput(out, withMeta);
						}
						
						protected OtherType.OtherTypeBuilder assignOutput(OtherType.OtherTypeBuilder out, List<? extends WithMeta> withMeta) {
							out
								.addAttrMulti(MapperC.<WithMeta>of(withMeta)
									.getItems()
									.map(item -> ReferenceWithMetaWithMeta.builder()
										.setExternalReference(item.getMappedObject().getMeta().getExternalKey())
										.setGlobalReference(item.getMappedObject().getMeta().getGlobalKey())
										.build())
									.collect(Collectors.toList())
								);
							
							out
								.setAttrSingle(ReferenceWithMetaWithMeta.builder()
									.setGlobalReference(Optional.ofNullable(MapperS.of(MapperC.<WithMeta>of(withMeta).get()).get())
										.map(r -> r.getMeta())
										.map(m -> m.getGlobalKey())
										.orElse(null))
									.setExternalReference(Optional.ofNullable(MapperS.of(MapperC.<WithMeta>of(withMeta).get()).get())
										.map(r -> r.getMeta())
										.map(m -> m.getExternalKey())
										.orElse(null))
									.build()
								);
							
							return Optional.ofNullable(out)
								.map(o -> o.prune())
								.orElse(null);
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
				set res : arg1
			
			func AddOne:
				inputs:  arg int (1..1)
				output: out int(1..1)
		'''.assertToGeneratedCalculation(
			'''
			package com.rosetta.test.model.functions;
			
			import com.google.inject.ImplementedBy;
			import com.google.inject.Inject;
			import com.rosetta.model.lib.functions.RosettaFunction;
			import com.rosetta.model.lib.mapper.Mapper;
			import com.rosetta.model.lib.mapper.MapperS;
			
			
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
			
				protected abstract Integer doEvaluate();
			
				protected abstract Mapper<Integer> arg1();
			
				public static class AdderDefault extends Adder {
					@Override
					protected Integer doEvaluate() {
						Integer res = null;
						return assignOutput(res);
					}
					
					protected Integer assignOutput(Integer res) {
						res = MapperS.of(arg1().get()).get();
						
						return res;
					}
					
					@Override
					protected Mapper<Integer> arg1() {
						return MapperS.of(addOne.evaluate(MapperS.of(Integer.valueOf(1)).get()));
					}
				}
			}
			'''
		)
	}

	
	@Test
	def void shouldResolveExternalFunctionDependenciesWhenEnumCalculation() {
		val generated = '''
			type MathInput:
				mathInput string (1..1)
				math Math (1..1)
			
			func AddOne:
				inputs: arg string (1..1)
				output: out string (1..1)
						
			func SubOne:
				inputs: arg string (1..1)
				output: out string (1..1)
			
			
			enum Math:
				INCR
				DECR
			
			func MathFunc:
				inputs:
					in1 Math (1..1)
					in2 MathInput (1..1)
				output: arg1 string (1..1)
			
			func MathFunc (in1 : Math -> INCR ):
				set arg1: AddOne(in2 -> mathInput)
				
			func MathFunc (in1 : Math -> DECR ):
				set arg1: SubOne(in2 -> mathInput)
		'''.generateCode
		.get("com.rosetta.test.model.functions.MathFunc")
		assertEquals(
			'''
			package com.rosetta.test.model.functions;
			
			import com.google.inject.ImplementedBy;
			import com.google.inject.Inject;
			import com.rosetta.model.lib.functions.RosettaFunction;
			import com.rosetta.model.lib.mapper.MapperS;
			import com.rosetta.test.model.Math;
			import com.rosetta.test.model.MathInput;
			
			
			/**
			 * @version test
			 */
			public class MathFunc {
				
				@Inject protected MathFunc.iNCR_ iNCR_;
				@Inject protected MathFunc.dECR_ dECR_;
				
				public String evaluate(Math in1, MathInput in2) {
					switch (in1) {
						case INCR:
							return iNCR_.evaluate(in1, in2);
						case DECR:
							return dECR_.evaluate(in1, in2);
						default:
							throw new IllegalArgumentException("Enum value not implemented: " + in1);
					}
				}
				
				@ImplementedBy(MathFuncINCR.MathFuncINCRDefault.class)
				public abstract class MathFuncINCR implements RosettaFunction {
					
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
				
					protected abstract String doEvaluate(Math in1, MathInput in2);
				
					public static class MathFuncINCRDefault extends MathFuncINCR {
						@Override
						protected String doEvaluate(Math in1, MathInput in2) {
							String arg1 = null;
							return assignOutput(arg1, in1, in2);
						}
						
						protected String assignOutput(String arg1, Math in1, MathInput in2) {
							arg1 = MapperS.of(addOne.evaluate(MapperS.of(in2).<String>map("getMathInput", mathInput -> mathInput.getMathInput()).get())).get();
							
							return arg1;
						}
					}
				}
				@ImplementedBy(MathFuncDECR.MathFuncDECRDefault.class)
				public abstract class MathFuncDECR implements RosettaFunction {
					
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
				
					protected abstract String doEvaluate(Math in1, MathInput in2);
				
					public static class MathFuncDECRDefault extends MathFuncDECR {
						@Override
						protected String doEvaluate(Math in1, MathInput in2) {
							String arg1 = null;
							return assignOutput(arg1, in1, in2);
						}
						
						protected String assignOutput(String arg1, Math in1, MathInput in2) {
							arg1 = MapperS.of(subOne.evaluate(MapperS.of(in2).<String>map("getMathInput", mathInput -> mathInput.getMathInput()).get())).get();
							
							return arg1;
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
				set res: addedOne
			
			func AddOne:
				inputs: arg int (1..1)
				output: out int (1..1)
		'''.assertToGeneratedCalculation(
			'''
			package com.rosetta.test.model.functions;
			
			import com.google.inject.ImplementedBy;
			import com.google.inject.Inject;
			import com.rosetta.model.lib.functions.RosettaFunction;
			import com.rosetta.model.lib.mapper.Mapper;
			import com.rosetta.model.lib.mapper.MapperS;
			
			
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
			
				protected abstract Integer doEvaluate(Integer arg1);
			
				protected abstract Mapper<Integer> addedOne(Integer arg1);
			
				public static class AdderDefault extends Adder {
					@Override
					protected Integer doEvaluate(Integer arg1) {
						Integer res = null;
						return assignOutput(res, arg1);
					}
					
					protected Integer assignOutput(Integer res, Integer arg1) {
						res = MapperS.of(addedOne(arg1).get()).get();
						
						return res;
					}
					
					@Override
					protected Mapper<Integer> addedOne(Integer arg1) {
						return MapperS.of(addOne.evaluate(MapperS.of(Integer.valueOf(1)).get()));
					}
				}
			}
			'''
		)
	}
}
