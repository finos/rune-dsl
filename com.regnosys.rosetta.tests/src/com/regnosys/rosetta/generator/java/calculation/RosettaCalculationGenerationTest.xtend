package com.regnosys.rosetta.generator.java.calculation

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
class RosettaCalculationGenerationTest {

	@Inject extension FuncGeneratorHelper
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
				import com.rosetta.model.lib.functions.MapperMaths;
				import com.rosetta.model.lib.functions.MapperS;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.test.model.Period;
				import com.rosetta.test.model.PeriodEnum;
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
							
							BigDecimal outHolder = doEvaluate(in1, in2);
							BigDecimal out = assignOutput(outHolder, in1, in2);
							
							return out;
						}
						
						private BigDecimal assignOutput(BigDecimal outHolder, PeriodEnum in1, Period in2) {
							outHolder = MapperMaths.<BigDecimal, Integer, BigDecimal>multiply(MapperS.of(i(in1, in2).get()), MapperS.of(BigDecimal.valueOf(30.0))).get();
							return outHolder;
						}
					
						protected abstract BigDecimal doEvaluate(PeriodEnum in1, Period in2);
						
						
						protected Mapper<Integer> i(PeriodEnum in1, Period in2) {
							return MapperS.of(in2).<Integer>map("getFrequency", _period -> _period.getFrequency());
						}
						public static final class MONTHDefault extends MONTH {
							@Override
							protected  BigDecimal doEvaluate(PeriodEnum in1, Period in2) {
								return null;
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
			import com.rosetta.model.lib.functions.MapperMaths;
			import com.rosetta.model.lib.functions.MapperS;
			import com.rosetta.model.lib.functions.RosettaFunction;
			
			
			@ImplementedBy(Calc.CalcDefault.class)
			public abstract class Calc implements RosettaFunction {
			
				/**
				* @param one 
				* @return out 
				*/
				public Integer evaluate(Integer one) {
					
					Integer outHolder = doEvaluate(one);
					Integer out = assignOutput(outHolder, one);
					
					return out;
				}
				
				private Integer assignOutput(Integer outHolder, Integer one) {
					outHolder = MapperMaths.<Integer, Integer, Integer>add(MapperS.of(oneA(one).get()), MapperS.of(oneA(one).get())).get();
					return outHolder;
				}
			
				protected abstract Integer doEvaluate(Integer one);
				
				
				protected Mapper<Integer> oneA(Integer one) {
					return MapperS.of(Integer.valueOf(1));
				}
				public static final class CalcDefault extends Calc {
					@Override
					protected  Integer doEvaluate(Integer one) {
						return null;
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
			import com.rosetta.model.lib.functions.MapperMaths;
			import com.rosetta.model.lib.functions.MapperS;
			import com.rosetta.model.lib.functions.Max;
			import com.rosetta.model.lib.functions.Min;
			import com.rosetta.model.lib.functions.RosettaFunction;
			
			
			@ImplementedBy(Calc.CalcDefault.class)
			public abstract class Calc implements RosettaFunction {
			
				/**
				* @param arg1 
				* @param arg2 
				* @return res 
				*/
				public Integer evaluate(Integer arg1, Integer arg2) {
					
					Integer resHolder = doEvaluate(arg1, arg2);
					Integer res = assignOutput(resHolder, arg1, arg2);
					
					return res;
				}
				
				private Integer assignOutput(Integer resHolder, Integer arg1, Integer arg2) {
					resHolder = MapperMaths.<Integer, Integer, Integer>add(MapperS.of(a1(arg1, arg2).get()), MapperMaths.<Integer, Integer, Integer>multiply(MapperS.of(a2(arg1, arg2).get()), MapperS.of(Integer.valueOf(215)))).get();
					return resHolder;
				}
			
				protected abstract Integer doEvaluate(Integer arg1, Integer arg2);
				
				
				protected Mapper<Integer> a1(Integer arg1, Integer arg2) {
					return MapperS.of(new Min().execute(MapperS.of(Integer.valueOf(1)).get(), MapperS.of(Integer.valueOf(2)).get()));
				}
				
				protected Mapper<Integer> a2(Integer arg1, Integer arg2) {
					return MapperS.of(new Max().execute(MapperS.of(Integer.valueOf(1)).get(), MapperS.of(Integer.valueOf(2)).get()));
				}
				public static final class CalcDefault extends Calc {
					@Override
					protected  Integer doEvaluate(Integer arg1, Integer arg2) {
						return null;
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
		import java.time.LocalTime;
		
		
		@ImplementedBy(Calc.CalcDefault.class)
		public abstract class Calc implements RosettaFunction {
			
			@Inject protected ModelObjectValidator objectValidator;
		
			/**
			* @param funIn 
			* @return res 
			*/
			public FoncOut evaluate(FuncIn funIn) {
				
				FoncOut.FoncOutBuilder resHolder = doEvaluate(funIn);
				FoncOut res = assignOutput(resHolder, funIn).build();
				
				objectValidator.validateAndFailOnErorr(FoncOut.class, res);
				return res;
			}
			
			private FoncOut.FoncOutBuilder assignOutput(FoncOut.FoncOutBuilder resHolder, FuncIn funIn) {
				@SuppressWarnings("unused") FoncOut res = resHolder.build();
				resHolder
					.setRes1(MapperMaths.<String, Date, LocalTime>add(MapperS.of(arg1(funIn).get()), MapperS.of(arg2(funIn).get())).get())
				;
				res = resHolder.build();
				resHolder
					.setRes2(MapperMaths.<String, Date, LocalTime>add(MapperS.of(arg1(funIn).get()), MapperS.of(arg2(funIn).get())).get())
				;
				return resHolder;
			}
		
			protected abstract FoncOut.FoncOutBuilder doEvaluate(FuncIn funIn);
			
			
			protected Mapper<Date> arg1(FuncIn funIn) {
				return MapperS.of(funIn).<Date>map("getVal1", _funcIn -> _funcIn.getVal1());
			}
			
			protected Mapper<LocalTime> arg2(FuncIn funIn) {
				return MapperS.of(funIn).<LocalTime>map("getVal2", _funcIn -> _funcIn.getVal2());
			}
			public static final class CalcDefault extends Calc {
				@Override
				protected  FoncOut.FoncOutBuilder doEvaluate(FuncIn funIn) {
					return FoncOut.builder();
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
				tradingDateTime string (1..1)
			
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
		import java.time.LocalTime;
		
		
		@ImplementedBy(RTS_22_Fields.RTS_22_FieldsDefault.class)
		public abstract class RTS_22_Fields implements RosettaFunction {
			
			@Inject protected ModelObjectValidator objectValidator;
		
			/**
			* @param funcIn 
			* @return out 
			*/
			public FuncOut evaluate(FuncIn funcIn) {
				
				FuncOut.FuncOutBuilder outHolder = doEvaluate(funcIn);
				FuncOut out = assignOutput(outHolder, funcIn).build();
				
				objectValidator.validateAndFailOnErorr(FuncOut.class, out);
				return out;
			}
			
			private FuncOut.FuncOutBuilder assignOutput(FuncOut.FuncOutBuilder outHolder, FuncIn funcIn) {
				@SuppressWarnings("unused") FuncOut out = outHolder.build();
				outHolder
					.setTransactionReferenceNumber(MapperMaths.<String, String, String>add(MapperS.of("SPH"), MapperS.of(linkId(funcIn).get())).get())
				;
				out = outHolder.build();
				outHolder
					.setTradingDateTime(MapperMaths.<String, Date, LocalTime>add(MapperS.of(tradeDate(funcIn).get()), MapperS.of(tradeTime(funcIn).get())).get())
				;
				return outHolder;
			}
		
			protected abstract FuncOut.FuncOutBuilder doEvaluate(FuncIn funcIn);
			
			
			protected Mapper<String> linkId(FuncIn funcIn) {
				return MapperS.of(funcIn).<String>map("getValS", _funcIn -> _funcIn.getValS());
			}
			
			protected Mapper<Date> tradeDate(FuncIn funcIn) {
				return MapperS.of(funcIn).<Date>map("getVal1", _funcIn -> _funcIn.getVal1());
			}
			
			protected Mapper<LocalTime> tradeTime(FuncIn funcIn) {
				return MapperS.of(funcIn).<LocalTime>map("getVal2", _funcIn -> _funcIn.getVal2());
			}
			public static final class RTS_22_FieldsDefault extends RTS_22_Fields {
				@Override
				protected  FuncOut.FuncOutBuilder doEvaluate(FuncIn funcIn) {
					return FuncOut.builder();
				}
			}
		}
		'''
		assertEquals(expected, calcJava)
	}

	@Test
	def void testAsKeyGeneration() {
		'''
		type WithMeta:
			[metadata key]
		
		type OtherType:
			attrSingle WithMeta (0..1)
			[metadata reference]
			attrMulti WithMeta (0..*)
			[metadata reference]
			
		func asKeyUsage:
			inputs: withMeta WithMeta(0..1)
			output: out OtherType (0..1)
			assign-output out -> attrMulti:
				withMeta as-key
			assign-output out -> attrMulti[1]:
				withMeta as-key
			assign-output out -> attrSingle:
				withMeta as-key
		'''.assertToGeneratedCalculation(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.google.inject.Inject;
				import com.rosetta.model.lib.functions.MapperS;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.validation.ModelObjectValidator;
				import com.rosetta.test.model.OtherType;
				import com.rosetta.test.model.WithMeta;
				import com.rosetta.test.model.metafields.ReferenceWithMetaWithMeta;
				import java.util.Optional;
				
				
				@ImplementedBy(asKeyUsage.asKeyUsageDefault.class)
				public abstract class asKeyUsage implements RosettaFunction {
					
					@Inject protected ModelObjectValidator objectValidator;
				
					/**
					* @param withMeta 
					* @return out 
					*/
					public OtherType evaluate(WithMeta withMeta) {
						
						OtherType.OtherTypeBuilder outHolder = doEvaluate(withMeta);
						OtherType out = assignOutput(outHolder, withMeta).build();
						
						objectValidator.validateAndFailOnErorr(OtherType.class, out);
						return out;
					}
					
					private OtherType.OtherTypeBuilder assignOutput(OtherType.OtherTypeBuilder outHolder, WithMeta withMeta) {
						@SuppressWarnings("unused") OtherType out = outHolder.build();
						outHolder
							.addAttrMulti(ReferenceWithMetaWithMeta.builder().setGlobalReference(
									Optional.ofNullable(MapperS.of(withMeta).get())
										.map(r -> r.getMeta())
										.map(m -> m.getGlobalKey())
										.orElse(null)
								).build()
							)
						;
						out = outHolder.build();
						outHolder
							.addAttrMulti(ReferenceWithMetaWithMeta.builder().setGlobalReference(
									Optional.ofNullable(MapperS.of(withMeta).get())
										.map(r -> r.getMeta())
										.map(m -> m.getGlobalKey())
										.orElse(null)
								).build()
							, 1)
						;
						out = outHolder.build();
						outHolder
							.setAttrSingle(ReferenceWithMetaWithMeta.builder().setGlobalReference(
									Optional.ofNullable(MapperS.of(withMeta).get())
										.map(r -> r.getMeta())
										.map(m -> m.getGlobalKey())
										.orElse(null)
								).build()
							)
						;
						return outHolder;
					}
				
					protected abstract OtherType.OtherTypeBuilder doEvaluate(WithMeta withMeta);
					
					public static final class asKeyUsageDefault extends asKeyUsage {
						@Override
						protected  OtherType.OtherTypeBuilder doEvaluate(WithMeta withMeta) {
							return OtherType.builder();
						}
					}
				}
			'''
		)
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
			assign-output out -> attrMulti:
				withMeta as-key
			assign-output out -> attrSingle:
				withMeta only-element as-key
		'''.assertToGeneratedCalculation(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.google.inject.Inject;
				import com.rosetta.model.lib.functions.MapperC;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.validation.ModelObjectValidator;
				import com.rosetta.test.model.OtherType;
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
					public OtherType evaluate(List<WithMeta> withMeta) {
						
						OtherType.OtherTypeBuilder outHolder = doEvaluate(withMeta);
						OtherType out = assignOutput(outHolder, withMeta).build();
						
						objectValidator.validateAndFailOnErorr(OtherType.class, out);
						return out;
					}
					
					private OtherType.OtherTypeBuilder assignOutput(OtherType.OtherTypeBuilder outHolder, List<WithMeta> withMeta) {
						@SuppressWarnings("unused") OtherType out = outHolder.build();
						outHolder
							.addAttrMulti(MapperC.of(withMeta)
							.getItems().map(
									(item) -> ReferenceWithMetaWithMeta.builder().setGlobalReference(item.getMappedObject().getMeta().getGlobalKey()).build()
								).collect(Collectors.toList())
							)
						;
						out = outHolder.build();
						outHolder
							.setAttrSingle(ReferenceWithMetaWithMeta.builder().setGlobalReference(
									Optional.ofNullable(MapperC.of(withMeta).get())
										.map(r -> r.getMeta())
										.map(m -> m.getGlobalKey())
										.orElse(null)
								).build()
							)
						;
						return outHolder;
					}
				
					protected abstract OtherType.OtherTypeBuilder doEvaluate(List<WithMeta> withMeta);
					
					public static final class asKeyUsageDefault extends asKeyUsage {
						@Override
						protected  OtherType.OtherTypeBuilder doEvaluate(List<WithMeta> withMeta) {
							return OtherType.builder();
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
			
			
			@ImplementedBy(Adder.AdderDefault.class)
			public abstract class Adder implements RosettaFunction {
				
				// RosettaFunction dependencies
				//
				@Inject protected AddOne addOne;
			
				/**
				* @return res 
				*/
				public Integer evaluate() {
					
					Integer resHolder = doEvaluate();
					Integer res = assignOutput(resHolder);
					
					return res;
				}
				
				private Integer assignOutput(Integer resHolder) {
					resHolder = MapperS.of(arg1().get()).get();
					return resHolder;
				}
			
				protected abstract Integer doEvaluate();
				
				
				protected Mapper<Integer> arg1() {
					return MapperS.of(addOne.evaluate(MapperS.of(Integer.valueOf(1)).get()));
				}
				public static final class AdderDefault extends Adder {
					@Override
					protected  Integer doEvaluate() {
						return null;
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
			import com.rosetta.model.lib.functions.MapperS;
			import com.rosetta.model.lib.functions.RosettaFunction;
			import com.rosetta.test.model.Math;
			import com.rosetta.test.model.MathInput;
			import com.rosetta.test.model.functions.AddOne;
			import com.rosetta.test.model.functions.SubOne;
			
			
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
						
						String arg1Holder = doEvaluate(in1, in2);
						String arg1 = assignOutput(arg1Holder, in1, in2);
						
						return arg1;
					}
					
					private String assignOutput(String arg1Holder, Math in1, MathInput in2) {
						arg1Holder = MapperS.of(addOne.evaluate(MapperS.of(in2).<String>map("getMathInput", _mathInput -> _mathInput.getMathInput()).get())).get();
						return arg1Holder;
					}
				
					protected abstract String doEvaluate(Math in1, MathInput in2);
					
					public static final class INCRDefault extends INCR {
						@Override
						protected  String doEvaluate(Math in1, MathInput in2) {
							return null;
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
						
						String arg1Holder = doEvaluate(in1, in2);
						String arg1 = assignOutput(arg1Holder, in1, in2);
						
						return arg1;
					}
					
					private String assignOutput(String arg1Holder, Math in1, MathInput in2) {
						arg1Holder = MapperS.of(subOne.evaluate(MapperS.of(in2).<String>map("getMathInput", _mathInput -> _mathInput.getMathInput()).get())).get();
						return arg1Holder;
					}
				
					protected abstract String doEvaluate(Math in1, MathInput in2);
					
					public static final class DECRDefault extends DECR {
						@Override
						protected  String doEvaluate(Math in1, MathInput in2) {
							return null;
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
					
					Integer resHolder = doEvaluate(arg1);
					Integer res = assignOutput(resHolder, arg1);
					
					return res;
				}
				
				private Integer assignOutput(Integer resHolder, Integer arg1) {
					resHolder = MapperS.of(addedOne(arg1).get()).get();
					return resHolder;
				}
			
				protected abstract Integer doEvaluate(Integer arg1);
				
				
				protected Mapper<Integer> addedOne(Integer arg1) {
					return MapperS.of(addOne.evaluate(MapperS.of(Integer.valueOf(1)).get()));
				}
				public static final class AdderDefault extends Adder {
					@Override
					protected  Integer doEvaluate(Integer arg1) {
						return null;
					}
				}
			}
			'''
		)
	}
}
