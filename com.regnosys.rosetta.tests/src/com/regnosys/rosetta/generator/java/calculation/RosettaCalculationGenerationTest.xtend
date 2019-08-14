package com.regnosys.rosetta.generator.java.calculation

import com.google.inject.Inject
import com.regnosys.rosetta.generator.java.blueprints.RosettaBlueprintTest
import com.regnosys.rosetta.rosetta.RosettaPackage
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.regnosys.rosetta.tests.util.ModelHelper
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.eclipse.xtext.testing.validation.ValidationTestHelper
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static org.junit.jupiter.api.Assertions.*

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class RosettaCalculationGenerationTest {

	@Inject extension CalculationGeneratorHelper
	@Inject extension CodeGeneratorTestHelper
	@Inject extension ModelHelper
	@Inject extension ValidationTestHelper

	@Test
	def void testSimpleTransDep() {
		'''
			class Period {
				frequency int (1..1);
				periodEnum PeriodEnum (1..1);
				period number (1..1);
			}
			
			enum PeriodEnum {
				MONTH
			}

			calculation DayFraction {
				res defined by: p / 360
				
				where
					p: is Period -> periodEnum
			}
			
			calculation PeriodEnum.MONTH {
				defined by: i * 30.0
				
				where
					i: is Period -> frequency
			}
		'''.assertToGeneratedCalculation(
			'''
			package com.rosetta.test.model.calculation;
			
			import com.rosetta.model.lib.functions.Formula;
			import com.rosetta.model.lib.functions.ICalculationInput;
			import com.rosetta.model.lib.functions.ICalculationResult;
			import com.rosetta.model.lib.functions.IResult;
			import com.rosetta.model.lib.math.BigDecimalExtensions;
			import com.rosetta.test.model.Period;
			import java.math.BigDecimal;
			import java.util.ArrayList;
			import java.util.Arrays;
			import java.util.List;
			
			public class DayFraction {
				
				public CalculationResult calculate(Period paramPeriod) {
					CalculationInput input = new CalculationInput().create(paramPeriod);
					CalculationResult result = new CalculationResult(input);
					result.res = BigDecimalExtensions.divide(input.p, BigDecimal.valueOf(360));
					return result;
				}
				
				public static class CalculationInput implements ICalculationInput {
					private CalculationInput input = this;  // For when arguments need to reference other arguments
					private final List<ICalculationResult> calculationResults = new ArrayList<>();
					private BigDecimal p;
					
					public CalculationInput create(Period inputParam) {
						PeriodEnum.CalculationResult periodEnumCalculationResult = new PeriodEnum().calculate(inputParam, inputParam.getPeriodEnum());
						this.calculationResults.add(periodEnumCalculationResult);
						this.p = periodEnumCalculationResult.getValue();
						return this;
					}
				
					@Override
					public List<Formula> getFormulas() {
						return Arrays.asList(
						new Formula("DayFraction", "res defined by: p / 360", this));
					}
					
					@Override
					public List<ICalculationResult> getCalculationResults() {
						return calculationResults;
					}
					
					public BigDecimal getP() {
						return p;
					}
				
					private static final List<Attribute<?>> ATTRIBUTES =  Arrays.asList(
						new Attribute<>("p", BigDecimal.class, (IResult res) -> ((CalculationInput) res).getP())
					);
				
					@Override
					public List<Attribute<?>> getAttributes() {
						return ATTRIBUTES;
					}
					
				}
				
				public static class CalculationResult implements ICalculationResult {
				
					private CalculationInput calculationInput;
				
					private BigDecimal res;
					
					public CalculationResult(CalculationInput calculationInput) {
						this.calculationInput = calculationInput;
					}
					public BigDecimal getRes() {
						return this.res;
					}
					
					public CalculationResult setRes(BigDecimal res) {
						this.res = res;
						return this;
					}
					
					@Override
					public CalculationInput getCalculationInput() {
						return calculationInput;
					}
					
					private static final List<Attribute<?>> ATTRIBUTES =  Arrays.asList(
						new Attribute<>("res", BigDecimal.class, (IResult res) -> ((CalculationResult) res).getRes())
					);
				
					@Override
					public List<Attribute<?>> getAttributes() {
						return ATTRIBUTES;
					}
					
					@Override
					public boolean equals(Object o) {
						if (this == o) return true;
						if (o == null || getClass() != o.getClass()) return false;
					
						CalculationResult _that = (CalculationResult) o;
					
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
						return "CalculationResult {" +
							"res=" + this.res +
						'}';
					}
				}
			}
			'''
		)

	}


	@Test
	def void testOnePlusOneGeneration() {
		'''
			calculation Calc {
				defined by: one + one
				
				where
					one int : is 1
			}
		'''.assertToGeneratedCalculation(
			'''
			package com.rosetta.test.model.calculation;
			
			import com.rosetta.model.lib.functions.Formula;
			import com.rosetta.model.lib.functions.ICalculationInput;
			import com.rosetta.model.lib.functions.ICalculationResult;
			import com.rosetta.model.lib.functions.IResult;
			import java.lang.Integer;
			import java.util.Arrays;
			import java.util.List;
			
			public class Calc {
				
				public CalculationResult calculate() {
					CalculationInput input = new CalculationInput().create();
					CalculationResult result = new CalculationResult(input);
					result.value = (input.one + input.one);
					return result;
				}
				
				public static class CalculationInput implements ICalculationInput {
					private CalculationInput input = this;  // For when arguments need to reference other arguments
					private Integer one;
					
					public CalculationInput create() {
						this.one = 1;
						return this;
					}
				
					@Override
					public List<Formula> getFormulas() {
						return Arrays.asList(
						new Formula("Calc", "defined by: one + one", this));
					}
					
					public Integer getOne() {
						return one;
					}
				
					private static final List<Attribute<?>> ATTRIBUTES =  Arrays.asList(
						new Attribute<>("one", Integer.class, (IResult res) -> ((CalculationInput) res).getOne())
					);
				
					@Override
					public List<Attribute<?>> getAttributes() {
						return ATTRIBUTES;
					}
					
				}
				
				public static class CalculationResult implements ICalculationResult {
				
					private CalculationInput calculationInput;
				
					private Integer value;
					
					public CalculationResult(CalculationInput calculationInput) {
						this.calculationInput = calculationInput;
					}
					public Integer getValue() {
						return this.value;
					}
					
					public CalculationResult setValue(Integer value) {
						this.value = value;
						return this;
					}
					
					@Override
					public CalculationInput getCalculationInput() {
						return calculationInput;
					}
					
					private static final List<Attribute<?>> ATTRIBUTES =  Arrays.asList(
						new Attribute<>("value", Integer.class, (IResult res) -> ((CalculationResult) res).getValue())
					);
				
					@Override
					public List<Attribute<?>> getAttributes() {
						return ATTRIBUTES;
					}
					
					@Override
					public boolean equals(Object o) {
						if (this == o) return true;
						if (o == null || getClass() != o.getClass()) return false;
					
						CalculationResult _that = (CalculationResult) o;
					
						if (value != null ? !value.equals(_that.value) : _that.value != null) return false;
						return true;
					}
					
					@Override
					public int hashCode() {
						int _result = 0;
						_result = 31 * _result + (value != null ? value.hashCode() : 0);
						return _result;
					}
					
					@Override
					public String toString() {
						return "CalculationResult {" +
							"value=" + this.value +
						'}';
					}
				}
			}
			'''
		)

	}
	
	@Test
	def void testSimpleCalculationGeneration() {
		'''
			calculation Calc {
				res defined by: arg1 + arg2 * 215
				
				where
					arg1 int : is Min(1,2)
					arg2 int : is Max(1,2)
			}
		'''.assertToGeneratedCalculation(
			'''
			package com.rosetta.test.model.calculation;
			
			import com.rosetta.model.lib.functions.Formula;
			import com.rosetta.model.lib.functions.ICalculationInput;
			import com.rosetta.model.lib.functions.ICalculationResult;
			import com.rosetta.model.lib.functions.IResult;
			import com.rosetta.model.lib.functions.Max;
			import com.rosetta.model.lib.functions.Min;
			import java.lang.Integer;
			import java.util.Arrays;
			import java.util.List;
			
			public class Calc {
				
				public CalculationResult calculate() {
					CalculationInput input = new CalculationInput().create();
					CalculationResult result = new CalculationResult(input);
					result.res = (input.arg1 + (input.arg2 * 215));
					return result;
				}
				
				public static class CalculationInput implements ICalculationInput {
					private CalculationInput input = this;  // For when arguments need to reference other arguments
					private Integer arg1;
					private Integer arg2;
					
					public CalculationInput create() {
						this.arg1 = new Min().execute(1,2);
						this.arg2 = new Max().execute(1,2);
						return this;
					}
				
					@Override
					public List<Formula> getFormulas() {
						return Arrays.asList(
						new Formula("Calc", "res defined by: arg1 + arg2 * 215", this));
					}
					
					public Integer getArg1() {
						return arg1;
					}
				
					public Integer getArg2() {
						return arg2;
					}
				
					private static final List<Attribute<?>> ATTRIBUTES =  Arrays.asList(
						new Attribute<>("arg1", Integer.class, (IResult res) -> ((CalculationInput) res).getArg1()),
						new Attribute<>("arg2", Integer.class, (IResult res) -> ((CalculationInput) res).getArg2())
					);
				
					@Override
					public List<Attribute<?>> getAttributes() {
						return ATTRIBUTES;
					}
					
				}
				
				public static class CalculationResult implements ICalculationResult {
				
					private CalculationInput calculationInput;
				
					private Integer res;
					
					public CalculationResult(CalculationInput calculationInput) {
						this.calculationInput = calculationInput;
					}
					public Integer getRes() {
						return this.res;
					}
					
					public CalculationResult setRes(Integer res) {
						this.res = res;
						return this;
					}
					
					@Override
					public CalculationInput getCalculationInput() {
						return calculationInput;
					}
					
					private static final List<Attribute<?>> ATTRIBUTES =  Arrays.asList(
						new Attribute<>("res", Integer.class, (IResult res) -> ((CalculationResult) res).getRes())
					);
				
					@Override
					public List<Attribute<?>> getAttributes() {
						return ATTRIBUTES;
					}
					
					@Override
					public boolean equals(Object o) {
						if (this == o) return true;
						if (o == null || getClass() != o.getClass()) return false;
					
						CalculationResult _that = (CalculationResult) o;
					
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
						return "CalculationResult {" +
							"res=" + this.res +
						'}';
					}
				}
			}
			'''
		)
	}

	@Test
	def void testDateTimeAdd() {
		val calculation = '''			
			calculation Calc {
				res defined by: arg1 + arg2 
				string res2 defined by: arg1 + arg2
				
				where
					arg1 date : is FuncIn->val1
					arg2 time : is FuncIn->val2
			}
			
			class FuncIn {
				val1 date (1..1);
				val2 time (1..1);
			}
		'''.generateCode
		val calcJava = calculation.get("com.rosetta.test.model.calculation.Calc")
		//RosettaBlueprintTest.writeOutClasses(calculation, "testDateTimeAdd")
		calculation.compileToClasses
		val expected = '''
		package com.rosetta.test.model.calculation;
		
		import com.rosetta.model.lib.functions.Formula;
		import com.rosetta.model.lib.functions.ICalculationInput;
		import com.rosetta.model.lib.functions.ICalculationResult;
		import com.rosetta.model.lib.functions.IResult;
		import com.rosetta.test.model.FuncIn;
		import java.lang.String;
		import java.time.LocalDate;
		import java.time.LocalDateTime;
		import java.time.LocalTime;
		import java.util.Arrays;
		import java.util.List;
		
		public class Calc {
			
			public CalculationResult calculate(FuncIn paramFuncIn) {
				CalculationInput input = new CalculationInput().create(paramFuncIn);
				CalculationResult result = new CalculationResult(input);
				result.res = LocalDateTime.of(input.arg1, input.arg2);
				result.res2 = LocalDateTime.of(input.arg1, input.arg2).toString();
				return result;
			}
			
			public static class CalculationInput implements ICalculationInput {
				private CalculationInput input = this;  // For when arguments need to reference other arguments
				private LocalDate arg1;
				private LocalTime arg2;
				
				public CalculationInput create(FuncIn inputParam) {
					this.arg1 = inputParam.getVal1();
					this.arg2 = inputParam.getVal2();
					return this;
				}
			
				@Override
				public List<Formula> getFormulas() {
					return Arrays.asList(
					new Formula("Calc", "res defined by: arg1 + arg2", this),
					new Formula("Calc", "string res2 defined by: arg1 + arg2", this));
				}
				
				public LocalDate getArg1() {
					return arg1;
				}
			
				public LocalTime getArg2() {
					return arg2;
				}
			
				private static final List<Attribute<?>> ATTRIBUTES =  Arrays.asList(
					new Attribute<>("arg1", LocalDate.class, (IResult res) -> ((CalculationInput) res).getArg1()),
					new Attribute<>("arg2", LocalTime.class, (IResult res) -> ((CalculationInput) res).getArg2())
				);
			
				@Override
				public List<Attribute<?>> getAttributes() {
					return ATTRIBUTES;
				}
				
			}
			
			public static class CalculationResult implements ICalculationResult {
			
				private CalculationInput calculationInput;
			
				private LocalDateTime res;
				private String res2;
				
				public CalculationResult(CalculationInput calculationInput) {
					this.calculationInput = calculationInput;
				}
				public LocalDateTime getRes() {
					return this.res;
				}
				
				public CalculationResult setRes(LocalDateTime res) {
					this.res = res;
					return this;
				}
				
				public String getRes2() {
					return this.res2;
				}
				
				public CalculationResult setRes2(String res2) {
					this.res2 = res2;
					return this;
				}
				
				@Override
				public CalculationInput getCalculationInput() {
					return calculationInput;
				}
				
				private static final List<Attribute<?>> ATTRIBUTES =  Arrays.asList(
					new Attribute<>("res", LocalDateTime.class, (IResult res) -> ((CalculationResult) res).getRes()),
					new Attribute<>("res2", String.class, (IResult res) -> ((CalculationResult) res).getRes2())
				);
			
				@Override
				public List<Attribute<?>> getAttributes() {
					return ATTRIBUTES;
				}
				
				@Override
				public boolean equals(Object o) {
					if (this == o) return true;
					if (o == null || getClass() != o.getClass()) return false;
				
					CalculationResult _that = (CalculationResult) o;
				
					if (res != null ? !res.equals(_that.res) : _that.res != null) return false;
					if (res2 != null ? !res2.equals(_that.res2) : _that.res2 != null) return false;
					return true;
				}
				
				@Override
				public int hashCode() {
					int _result = 0;
					_result = 31 * _result + (res != null ? res.hashCode() : 0);
					_result = 31 * _result + (res2 != null ? res2.hashCode() : 0);
					return _result;
				}
				
				@Override
				public String toString() {
					return "CalculationResult {" +
						"res=" + this.res + ", " +
						"res2=" + this.res2 +
					'}';
				}
			}
		}
		'''
		assertEquals(expected, calcJava)
	}

	@Test
	def void testWierdness() {
		val calculation = '''			
			calculation RTS_22_Fields {
				string transactionReferenceNumber defined by: "SPH"+linkId
				string tradingDateTime defined by: tradeDate + tradeTime
				
				where 
					linkId string : is FuncIn->valS
					tradeDate date : is FuncIn->val1
					tradeTime time : is FuncIn->val2
			}
			class FuncIn {
				valS string (1..1);
				val1 date (1..1);
				val2 time (1..1);
			}
		'''.generateCode
		val calcJava = calculation.get("com.rosetta.test.model.calculation.RTS_22_Fields")
		//RosettaBlueprintTest.writeOutClasses(calculation, "testWierdness")
		calculation.compileToClasses
		val expected = '''
		package com.rosetta.test.model.calculation;
		
		import com.rosetta.model.lib.functions.Formula;
		import com.rosetta.model.lib.functions.ICalculationInput;
		import com.rosetta.model.lib.functions.ICalculationResult;
		import com.rosetta.model.lib.functions.IResult;
		import com.rosetta.test.model.FuncIn;
		import java.lang.String;
		import java.time.LocalDate;
		import java.time.LocalDateTime;
		import java.time.LocalTime;
		import java.util.Arrays;
		import java.util.List;
		
		public class RTS_22_Fields {
			
			public CalculationResult calculate(FuncIn paramFuncIn) {
				CalculationInput input = new CalculationInput().create(paramFuncIn);
				CalculationResult result = new CalculationResult(input);
				result.transactionReferenceNumber = ("SPH" + input.linkId);
				result.tradingDateTime = LocalDateTime.of(input.tradeDate, input.tradeTime).toString();
				return result;
			}
			
			public static class CalculationInput implements ICalculationInput {
				private CalculationInput input = this;  // For when arguments need to reference other arguments
				private String linkId;
				private LocalDate tradeDate;
				private LocalTime tradeTime;
				
				public CalculationInput create(FuncIn inputParam) {
					this.linkId = inputParam.getValS();
					this.tradeDate = inputParam.getVal1();
					this.tradeTime = inputParam.getVal2();
					return this;
				}
			
				@Override
				public List<Formula> getFormulas() {
					return Arrays.asList(
					new Formula("RTS_22_Fields", "string transactionReferenceNumber defined by: 'SPH'+linkId", this),
					new Formula("RTS_22_Fields", "string tradingDateTime defined by: tradeDate + tradeTime", this));
				}
				
				public String getLinkId() {
					return linkId;
				}
			
				public LocalDate getTradeDate() {
					return tradeDate;
				}
			
				public LocalTime getTradeTime() {
					return tradeTime;
				}
			
				private static final List<Attribute<?>> ATTRIBUTES =  Arrays.asList(
					new Attribute<>("linkId", String.class, (IResult res) -> ((CalculationInput) res).getLinkId()),
					new Attribute<>("tradeDate", LocalDate.class, (IResult res) -> ((CalculationInput) res).getTradeDate()),
					new Attribute<>("tradeTime", LocalTime.class, (IResult res) -> ((CalculationInput) res).getTradeTime())
				);
			
				@Override
				public List<Attribute<?>> getAttributes() {
					return ATTRIBUTES;
				}
				
			}
			
			public static class CalculationResult implements ICalculationResult {
			
				private CalculationInput calculationInput;
			
				private String transactionReferenceNumber;
				private String tradingDateTime;
				
				public CalculationResult(CalculationInput calculationInput) {
					this.calculationInput = calculationInput;
				}
				public String getTransactionReferenceNumber() {
					return this.transactionReferenceNumber;
				}
				
				public CalculationResult setTransactionReferenceNumber(String transactionReferenceNumber) {
					this.transactionReferenceNumber = transactionReferenceNumber;
					return this;
				}
				
				public String getTradingDateTime() {
					return this.tradingDateTime;
				}
				
				public CalculationResult setTradingDateTime(String tradingDateTime) {
					this.tradingDateTime = tradingDateTime;
					return this;
				}
				
				@Override
				public CalculationInput getCalculationInput() {
					return calculationInput;
				}
				
				private static final List<Attribute<?>> ATTRIBUTES =  Arrays.asList(
					new Attribute<>("transactionReferenceNumber", String.class, (IResult res) -> ((CalculationResult) res).getTransactionReferenceNumber()),
					new Attribute<>("tradingDateTime", String.class, (IResult res) -> ((CalculationResult) res).getTradingDateTime())
				);
			
				@Override
				public List<Attribute<?>> getAttributes() {
					return ATTRIBUTES;
				}
				
				@Override
				public boolean equals(Object o) {
					if (this == o) return true;
					if (o == null || getClass() != o.getClass()) return false;
				
					CalculationResult _that = (CalculationResult) o;
				
					if (transactionReferenceNumber != null ? !transactionReferenceNumber.equals(_that.transactionReferenceNumber) : _that.transactionReferenceNumber != null) return false;
					if (tradingDateTime != null ? !tradingDateTime.equals(_that.tradingDateTime) : _that.tradingDateTime != null) return false;
					return true;
				}
				
				@Override
				public int hashCode() {
					int _result = 0;
					_result = 31 * _result + (transactionReferenceNumber != null ? transactionReferenceNumber.hashCode() : 0);
					_result = 31 * _result + (tradingDateTime != null ? tradingDateTime.hashCode() : 0);
					return _result;
				}
				
				@Override
				public String toString() {
					return "CalculationResult {" +
						"transactionReferenceNumber=" + this.transactionReferenceNumber + ", " +
						"tradingDateTime=" + this.tradingDateTime +
					'}';
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

	@Disabled
	@Test
	def void testBrokenArgs() {
		'''
			function Min(x number, y number) number
			function Max(x number, y number) number
			
			calculation Calc {
				res defined by: arg1 + arg2 * 215
			}
			 
			arguments Calc {
				arg1 int : is FuncIn1->val1
				arg2 int : is FuncIn2->val2
			}
			
			class FuncIn1 {
				val1 int (1..1);
			}
			class FuncIn2 {
				val2 int (1..1);
			}
		'''.parseRosetta.assertError(RosettaPackage.Literals.ROSETTA_CALCULATION, "")
	}

	@Test
	def void shouldResolveFunctionDependencies() {
		'''			
			calculation Adder {
				res defined by: arg1
				
				where
					arg1 int : is AddOne( 1 ) -> out
			}
			
			function AddOne( arg int ) {
				out int;
			}
		'''.assertToGeneratedCalculation(
			'''
			package com.rosetta.test.model.calculation;
			
			import com.rosetta.model.lib.functions.Formula;
			import com.rosetta.model.lib.functions.ICalculationInput;
			import com.rosetta.model.lib.functions.ICalculationResult;
			import com.rosetta.model.lib.functions.IResult;
			import com.rosetta.test.model.functions.AddOne;
			import java.lang.Integer;
			import java.util.Arrays;
			import java.util.List;
			
			public class Adder {
				
				private final AddOne addOne;
				
				public Adder(AddOne addOne) {
					this.addOne = addOne;
				}
				
				public CalculationResult calculate() {
					CalculationInput input = new CalculationInput().create(addOne);
					CalculationResult result = new CalculationResult(input);
					result.res = input.arg1;
					return result;
				}
				
				public static class CalculationInput implements ICalculationInput {
					private CalculationInput input = this;  // For when arguments need to reference other arguments
					private Integer arg1;
					
					public CalculationInput create(AddOne addOne) {
						this.arg1 = addOne.execute(1).getOut();
						return this;
					}
				
					@Override
					public List<Formula> getFormulas() {
						return Arrays.asList(
						new Formula("Adder", "res defined by: arg1", this));
					}
					
					public Integer getArg1() {
						return arg1;
					}
				
					private static final List<Attribute<?>> ATTRIBUTES =  Arrays.asList(
						new Attribute<>("arg1", Integer.class, (IResult res) -> ((CalculationInput) res).getArg1())
					);
				
					@Override
					public List<Attribute<?>> getAttributes() {
						return ATTRIBUTES;
					}
					
				}
				
				public static class CalculationResult implements ICalculationResult {
				
					private CalculationInput calculationInput;
				
					private Integer res;
					
					public CalculationResult(CalculationInput calculationInput) {
						this.calculationInput = calculationInput;
					}
					public Integer getRes() {
						return this.res;
					}
					
					public CalculationResult setRes(Integer res) {
						this.res = res;
						return this;
					}
					
					@Override
					public CalculationInput getCalculationInput() {
						return calculationInput;
					}
					
					private static final List<Attribute<?>> ATTRIBUTES =  Arrays.asList(
						new Attribute<>("res", Integer.class, (IResult res) -> ((CalculationResult) res).getRes())
					);
				
					@Override
					public List<Attribute<?>> getAttributes() {
						return ATTRIBUTES;
					}
					
					@Override
					public boolean equals(Object o) {
						if (this == o) return true;
						if (o == null || getClass() != o.getClass()) return false;
					
						CalculationResult _that = (CalculationResult) o;
					
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
						return "CalculationResult {" +
							"res=" + this.res +
						'}';
					}
				}
			}
			'''
		)
	}

	@Test
	def void shouldResolveTransitiveFunctionDependencies() {
		'''
			class MathInput
			{
			    mathInput string (1..1);
			    math Math (1..1);
			}
			
			calculation AddOrSubtract {
			    res defined by: arg1
			    
			    where
			    	arg1 string : is MathInput -> math
				    arg2 string : is AddThree( '3' ) -> out
			}
			
			function AddOne( arg string ) {
			    out string;
			}
			
			function SubOne( arg string ) {
			    out string;
			}
			
			function AddThree( arg string ) {
				out string;
			}
			
			enum Math
			{
			    INCR,
			    DECR
			}
			
			calculation Math.INCR {
			    defined by: arg1
			    
			    where
			    	arg1 string : is AddOne(MathInput -> mathInput) -> out
			}
			
			calculation Math.DECR {
			    defined by: arg1
			    
			    where
			        arg1 string : is SubOne(MathInput -> mathInput) -> out
			}
		'''.assertToGeneratedCalculation(
			'''
			package com.rosetta.test.model.calculation;
			
			import com.rosetta.model.lib.functions.Formula;
			import com.rosetta.model.lib.functions.ICalculationInput;
			import com.rosetta.model.lib.functions.ICalculationResult;
			import com.rosetta.model.lib.functions.IResult;
			import com.rosetta.test.model.MathInput;
			import com.rosetta.test.model.functions.AddOne;
			import com.rosetta.test.model.functions.AddThree;
			import com.rosetta.test.model.functions.SubOne;
			import java.lang.String;
			import java.util.ArrayList;
			import java.util.Arrays;
			import java.util.List;
			
			public class AddOrSubtract {
				
				private final AddOne addOne;
				private final SubOne subOne;
				private final AddThree addThree;
				
				public AddOrSubtract(AddOne addOne, SubOne subOne, AddThree addThree) {
					this.addOne = addOne;
					this.subOne = subOne;
					this.addThree = addThree;
				}
				
				public CalculationResult calculate(MathInput paramMathInput) {
					CalculationInput input = new CalculationInput().create(paramMathInput, addOne, subOne, addThree);
					CalculationResult result = new CalculationResult(input);
					result.res = input.arg1;
					return result;
				}
				
				public static class CalculationInput implements ICalculationInput {
					private CalculationInput input = this;  // For when arguments need to reference other arguments
					private final List<ICalculationResult> calculationResults = new ArrayList<>();
					private String arg1;
					private String arg2;
					
					public CalculationInput create(MathInput inputParam, AddOne addOne, SubOne subOne, AddThree addThree) {
						Math.CalculationResult mathCalculationResult = new Math(addOne, subOne).calculate(inputParam, inputParam.getMath());
						this.calculationResults.add(mathCalculationResult);
						this.arg1 = mathCalculationResult.getValue();
						this.arg2 = addThree.execute("3").getOut();
						return this;
					}
				
					@Override
					public List<Formula> getFormulas() {
						return Arrays.asList(
						new Formula("AddOrSubtract", "res defined by: arg1", this));
					}
					
					@Override
					public List<ICalculationResult> getCalculationResults() {
						return calculationResults;
					}
					
					public String getArg1() {
						return arg1;
					}
				
					public String getArg2() {
						return arg2;
					}
				
					private static final List<Attribute<?>> ATTRIBUTES =  Arrays.asList(
						new Attribute<>("arg1", String.class, (IResult res) -> ((CalculationInput) res).getArg1()),
						new Attribute<>("arg2", String.class, (IResult res) -> ((CalculationInput) res).getArg2())
					);
				
					@Override
					public List<Attribute<?>> getAttributes() {
						return ATTRIBUTES;
					}
					
				}
				
				public static class CalculationResult implements ICalculationResult {
				
					private CalculationInput calculationInput;
				
					private String res;
					
					public CalculationResult(CalculationInput calculationInput) {
						this.calculationInput = calculationInput;
					}
					public String getRes() {
						return this.res;
					}
					
					public CalculationResult setRes(String res) {
						this.res = res;
						return this;
					}
					
					@Override
					public CalculationInput getCalculationInput() {
						return calculationInput;
					}
					
					private static final List<Attribute<?>> ATTRIBUTES =  Arrays.asList(
						new Attribute<>("res", String.class, (IResult res) -> ((CalculationResult) res).getRes())
					);
				
					@Override
					public List<Attribute<?>> getAttributes() {
						return ATTRIBUTES;
					}
					
					@Override
					public boolean equals(Object o) {
						if (this == o) return true;
						if (o == null || getClass() != o.getClass()) return false;
					
						CalculationResult _that = (CalculationResult) o;
					
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
						return "CalculationResult {" +
							"res=" + this.res +
						'}';
					}
				}
			}
			'''
		)
	}

	@Test
	def void shouldResolveExternalFunctionDependenciesWhenEnumCalculation() {
		'''
			class MathInput
			{
			    mathInput string (1..1);
			    math Math (1..1);
			}
			
			function AddOne( arg string ) {
			    out string;
			}
			
			function SubOne( arg string ) {
			    out string;
			}
			
			enum Math
			{
			    INCR,
			    DECR
			}
			
			calculation Math.INCR {
			    defined by: arg1
			    
			    where 
			    	arg1 string : is AddOne(MathInput -> mathInput) -> out
			}
			
			calculation Math.DECR {
			    defined by: arg1
			    
			    where
			    	arg1 string : is SubOne(MathInput -> mathInput) -> out
			}
		'''.assertToGeneratedCalculation(
			'''
			package com.rosetta.test.model.calculation;
			
			import com.rosetta.model.lib.functions.Formula;
			import com.rosetta.model.lib.functions.ICalculationInput;
			import com.rosetta.model.lib.functions.ICalculationResult;
			import com.rosetta.model.lib.functions.IResult;
			import com.rosetta.test.model.MathInput;
			import com.rosetta.test.model.functions.AddOne;
			import com.rosetta.test.model.functions.SubOne;
			import java.lang.String;
			import java.util.Arrays;
			import java.util.List;
			
			/**
			 * @version test
			 */
			public class Math {
				
				private final AddOne addOne;
				private final SubOne subOne;
				
				public Math(AddOne addOne, SubOne subOne) {
					this.addOne = addOne;
					this.subOne = subOne;
				}
				
				public CalculationResult calculate(MathInput mathInput, com.rosetta.test.model.Math enumValue) {
					switch (enumValue) {
						case INCR:
							return new INCR(addOne).calculate(mathInput);
						case DECR:
							return new DECR(subOne).calculate(mathInput);
						default:
							throw new IllegalArgumentException("Enum value not implemented: " + enumValue);
					}
				}
				
				public static class INCR {
					
					private final AddOne addOne;
					
					public INCR(AddOne addOne) {
						this.addOne = addOne;
					}
					
					public CalculationResult calculate(MathInput paramMathInput) {
						CalculationInput input = new CalculationInput().create(paramMathInput, addOne);
						CalculationResult result = new CalculationResult(input);
						result.value = input.arg1;
						return result;
					}
					
					public static class CalculationInput implements ICalculationInput {
						private CalculationInput input = this;  // For when arguments need to reference other arguments
						private String arg1;
						
						public CalculationInput create(MathInput inputParam, AddOne addOne) {
							this.arg1 = addOne.execute(inputParam.getMathInput()).getOut();
							return this;
						}
					
						@Override
						public List<Formula> getFormulas() {
							return Arrays.asList(
							new Formula("INCR", "defined by: arg1", this));
						}
						
						public String getArg1() {
							return arg1;
						}
					
						private static final List<Attribute<?>> ATTRIBUTES =  Arrays.asList(
							new Attribute<>("arg1", String.class, (IResult res) -> ((CalculationInput) res).getArg1())
						);
					
						@Override
						public List<Attribute<?>> getAttributes() {
							return ATTRIBUTES;
						}
						
					}
				}
				public static class DECR {
					
					private final SubOne subOne;
					
					public DECR(SubOne subOne) {
						this.subOne = subOne;
					}
					
					public CalculationResult calculate(MathInput paramMathInput) {
						CalculationInput input = new CalculationInput().create(paramMathInput, subOne);
						CalculationResult result = new CalculationResult(input);
						result.value = input.arg1;
						return result;
					}
					
					public static class CalculationInput implements ICalculationInput {
						private CalculationInput input = this;  // For when arguments need to reference other arguments
						private String arg1;
						
						public CalculationInput create(MathInput inputParam, SubOne subOne) {
							this.arg1 = subOne.execute(inputParam.getMathInput()).getOut();
							return this;
						}
					
						@Override
						public List<Formula> getFormulas() {
							return Arrays.asList(
							new Formula("DECR", "defined by: arg1", this));
						}
						
						public String getArg1() {
							return arg1;
						}
					
						private static final List<Attribute<?>> ATTRIBUTES =  Arrays.asList(
							new Attribute<>("arg1", String.class, (IResult res) -> ((CalculationInput) res).getArg1())
						);
					
						@Override
						public List<Attribute<?>> getAttributes() {
							return ATTRIBUTES;
						}
						
					}
				}
				public static class CalculationResult implements ICalculationResult {
				
					private ICalculationInput calculationInput;
				
					private String value;
					
					public CalculationResult(ICalculationInput calculationInput) {
						this.calculationInput = calculationInput;
					}
					public String getValue() {
						return this.value;
					}
					
					public CalculationResult setValue(String value) {
						this.value = value;
						return this;
					}
					
					@Override
					public ICalculationInput getCalculationInput() {
						return calculationInput;
					}
					
					private static final List<Attribute<?>> ATTRIBUTES =  Arrays.asList(
						new Attribute<>("value", String.class, (IResult res) -> ((CalculationResult) res).getValue())
					);
				
					@Override
					public List<Attribute<?>> getAttributes() {
						return ATTRIBUTES;
					}
					
					@Override
					public boolean equals(Object o) {
						if (this == o) return true;
						if (o == null || getClass() != o.getClass()) return false;
					
						CalculationResult _that = (CalculationResult) o;
					
						if (value != null ? !value.equals(_that.value) : _that.value != null) return false;
						return true;
					}
					
					@Override
					public int hashCode() {
						int _result = 0;
						_result = 31 * _result + (value != null ? value.hashCode() : 0);
						return _result;
					}
					
					@Override
					public String toString() {
						return "CalculationResult {" +
							"value=" + this.value +
						'}';
					}
				}
			}
			''')
	}
	
	@Test
	def void shouldResolveFunctionDependenciesWhenReferencedInAlias() {
		'''	
			calculation Adder {
				res defined by: arg1
				
				where
					alias addedOne AddOne( 1 )
					arg1 int : is addedOne -> out
			}
			
			function AddOne( arg int ) {
				out int;
			}
		'''.assertToGeneratedCalculation(
			'''
			package com.rosetta.test.model.calculation;
			
			import com.rosetta.model.lib.functions.Formula;
			import com.rosetta.model.lib.functions.ICalculationInput;
			import com.rosetta.model.lib.functions.ICalculationResult;
			import com.rosetta.model.lib.functions.IResult;
			import com.rosetta.test.model.functions.AddOne;
			import java.lang.Integer;
			import java.util.Arrays;
			import java.util.List;
			
			public class Adder {
				
				private final AddOne addOne;
				
				public Adder(AddOne addOne) {
					this.addOne = addOne;
				}
				
				public CalculationResult calculate() {
					CalculationInput input = new CalculationInput().create(addOne);
					CalculationResult result = new CalculationResult(input);
					result.res = input.arg1;
					return result;
				}
				
				public static class CalculationInput implements ICalculationInput {
					private CalculationInput input = this;  // For when arguments need to reference other arguments
					private Integer arg1;
					
					public CalculationInput create(AddOne addOne) {
						AddOne.CalculationResult addedOneAlias = addOne.execute(1);
						this.arg1 = addedOneAlias.getOut();
						return this;
					}
				
					@Override
					public List<Formula> getFormulas() {
						return Arrays.asList(
						new Formula("Adder", "res defined by: arg1", this));
					}
					
					public Integer getArg1() {
						return arg1;
					}
				
					private static final List<Attribute<?>> ATTRIBUTES =  Arrays.asList(
						new Attribute<>("arg1", Integer.class, (IResult res) -> ((CalculationInput) res).getArg1())
					);
				
					@Override
					public List<Attribute<?>> getAttributes() {
						return ATTRIBUTES;
					}
					
				}
				
				public static class CalculationResult implements ICalculationResult {
				
					private CalculationInput calculationInput;
				
					private Integer res;
					
					public CalculationResult(CalculationInput calculationInput) {
						this.calculationInput = calculationInput;
					}
					public Integer getRes() {
						return this.res;
					}
					
					public CalculationResult setRes(Integer res) {
						this.res = res;
						return this;
					}
					
					@Override
					public CalculationInput getCalculationInput() {
						return calculationInput;
					}
					
					private static final List<Attribute<?>> ATTRIBUTES =  Arrays.asList(
						new Attribute<>("res", Integer.class, (IResult res) -> ((CalculationResult) res).getRes())
					);
				
					@Override
					public List<Attribute<?>> getAttributes() {
						return ATTRIBUTES;
					}
					
					@Override
					public boolean equals(Object o) {
						if (this == o) return true;
						if (o == null || getClass() != o.getClass()) return false;
					
						CalculationResult _that = (CalculationResult) o;
					
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
						return "CalculationResult {" +
							"res=" + this.res +
						'}';
					}
				}
			}
			'''
		)
	}

}
