package com.regnosys.rosetta.generator.java.blueprints

import com.google.inject.Inject
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.regnosys.rosetta.tests.util.ModelHelper
import com.regnosys.rosetta.validation.RosettaIssueCodes
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.HashMap
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.eclipse.xtext.testing.validation.ValidationTestHelper
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*
import static org.hamcrest.MatcherAssert.*
import static org.junit.jupiter.api.Assertions.*

@InjectWith(RosettaInjectorProvider)
@ExtendWith(InjectionExtension)
//@Disabled("This test fails after the 2.15 xtext upgrade because the generated code does not compile with the eclipse compiler using mvn.")
class RosettaBlueprintTest {

	@Inject extension CodeGeneratorTestHelper
	@Inject extension ModelHelper
	@Inject extension ValidationTestHelper

	@Test
	def void parseSimpleRule() {
		val r = '''
			eligibility rule ReportableTransation
			return "y"
		'''
		parseRosettaWithNoErrors(r)
	}

	@Test
	def void parseSimpleReport() {
		val r = '''
			body Authority TEST_REG
			corpus MiFIR
			
			report TEST_REG MiFIR in T+1
			when FooRule
			with fields
				BarField
			
			type Bar:
				field string (1..1)
			
			eligibility rule FooRule
				return "true"
			
			reporting rule BarField
				extract Bar->field
		'''
		// println(r)
		parseRosettaWithNoErrors(r)
	}

	@Test
	@Disabled // we don't currently support hand written blueprint nodes
	def void custom() {
		val blueprint = '''
			reporting rule SimpleBlueprint 
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
			
				Fish <string, string, string, string>
		'''.generateCode
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.SimpleBlueprintRule")
		// writeOutClasses(blueprint, "custom")
		assertThat(blueprintJava, CoreMatchers.notNullValue())
		blueprint.compileToClasses
	}

	@Test
	@Disabled
	def void genSimpleMerge() {
		val blueprint = '''
			reporting rule SimpleBlueprint 
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
				merge output Output 
				output Output
			
			type Input:
				traderef string (1..1)
			
			type Output:
				transactionReferenceNumber string (1..1)
				[regulatoryReference ESMA MiFIR RTS_22 annex "1 table2 #28" provision "" reportedField]
		'''.generateCode
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.SimpleBlueprint")
		// writeOutClasses(blueprint, "genSimpleMerge");
		try {
			assertThat(blueprintJava, CoreMatchers.notNullValue())
			val expected = '''
				package com.rosetta.test.model.blueprint;
				
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.regnosys.rosetta.blueprints.runner.actions.Merger;
				import com.regnosys.rosetta.blueprints.runner.data.DataIdentifier;
				import com.regnosys.rosetta.blueprints.runner.data.RosettaIdentifier;
				import com.regnosys.rosetta.blueprints.runner.data.StringIdentifier;
				import com.rosetta.model.lib.functions.Converter;
				import com.rosetta.test.model.Output;
				import java.util.HashMap;
				import java.util.Map;
				import java.util.function.BiConsumer;
				import java.util.function.Function;
				import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
				
				public abstract class SimpleBlueprint<IN, INKEY extends Comparable<INKEY>> implements Blueprint<IN, Output, INKEY, INKEY> {
					@Override
					public String getName() {
						return "SimpleBlueprint"; 
					}
					
					@Override
					public String getURI() {
						return "__synthetic1.rosetta#SimpleBlueprint";
					}
					
					
					@Override
					public BlueprintInstance<IN, Output, INKEY, INKEY> blueprint() { 
						return 
							startsWith(new Merger<>("__synthetic1.rosetta#//@elements.0/@nodes/@node", "Create Output", mergeOutput(), this::mergeOutputSupplier, Output.OutputBuilder::build, 
												new StringIdentifier("Output"), false))
							.toBlueprint(getURI(), getName());
					}
					
					protected abstract Function<DataIdentifier, BiConsumer<Output.OutputBuilder, ? extends IN>> mergeOutput();
					
					protected Map<DataIdentifier, BiConsumer<Output.OutputBuilder, ? extends IN>> simpleMergeOutput() {
						Map<DataIdentifier, BiConsumer<Output.OutputBuilder, ? extends IN>> result = new HashMap<>();
						result.put(new RosettaIdentifier("annex 1 table2 #28"), (builder, input) -> builder.setTransactionReferenceNumber(Converter.convert(String.class, input)));
						result.put(new StringIdentifier("transactionReferenceNumber"), (builder, input) -> builder.setTransactionReferenceNumber(Converter.convert(String.class, input)));
						return result;
					}
					
					protected abstract Output.OutputBuilder mergeOutputSupplier(INKEY k);
				}
			'''
			blueprint.compileToClasses
			assertEquals(expected, blueprintJava)
		} finally {
		}
	}

	@Test
	@Disabled
	def void genMappingGroup() {
		val blueprint = '''
			blueprint SimpleBlueprint 
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
			{
				SimpleMapping input Input then
				merge output Output 
				output Output
			}
			
			type Input:
				traderef string (1..1)
						[regulatoryReference ESMA MiFIR RTS_22 annex "1 table2 #28" provision "" reportedField]
			
			type Output:
				transactionReferenceNumber string (1..1)
				[regulatoryReference ESMA MiFIR RTS_22 annex "1 table2 #28" provision "" reportedField]
			
		'''.generateCode

		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.SimpleBlueprint")
		// writeOutClasses(blueprint, "genMappingGroup");
		try {
			assertThat(blueprintJava, CoreMatchers.notNullValue())
			val expected = '''
				package com.rosetta.test.model.blueprint;
				
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.regnosys.rosetta.blueprints.runner.actions.Merger;
				import com.regnosys.rosetta.blueprints.runner.data.DataIdentifier;
				import com.regnosys.rosetta.blueprints.runner.data.RosettaIdentifier;
				import com.regnosys.rosetta.blueprints.runner.data.StringIdentifier;
				import com.rosetta.model.lib.functions.Converter;
				import com.rosetta.model.lib.functions.Mapper;
				import com.rosetta.model.lib.functions.MapperS;
				import com.rosetta.model.lib.functions.MappingGroup;
				import com.rosetta.test.model.Input;
				import com.rosetta.test.model.Output;
				import java.util.Collection;
				import java.util.HashMap;
				import java.util.List;
				import java.util.Map;
				import java.util.function.BiConsumer;
				import java.util.function.Function;
				import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
				
				public abstract class SimpleBlueprint<INKEY extends Comparable<INKEY>> implements Blueprint<Input, Output, INKEY, INKEY> {
					@Override
					public String getName() {
						return "SimpleBlueprint"; 
					}
					
					@Override
					public String getURI() {
						return "__synthetic1.rosetta#SimpleBlueprint";
					}
					
					
					@Override
					public BlueprintInstance<Input, Output, INKEY, INKEY> blueprint() { 
						return 
							startsWith(BlueprintBuilder.<Input, INKEY>doSimpleMappings("__synthetic1.rosetta#//@elements.0/@nodes/@node", "simpleMappingsInput", simpleMappingsInput()))
							.then(new Merger<>("__synthetic1.rosetta#//@elements.0/@nodes/@next/@node", "Create Output", mergeOutput(), this::mergeOutputSupplier, Output.OutputBuilder::build, 
												new StringIdentifier("Output"), false))
							.toBlueprint(getURI(), getName());
					}
					
					protected Collection<MappingGroup<Input, ?>> simpleMappingsInput() {
						return Blueprint.of(
						new MappingGroup<>("annex 1 table2 #28", "__synthetic1.rosetta#//@elements.0/@nodes/@node", ANNEX_1_TABLE2_28_MAPPINGS));
					}
					
					protected abstract Function<DataIdentifier, BiConsumer<Output.OutputBuilder, ?>> mergeOutput();
					
					protected Map<DataIdentifier, BiConsumer<Output.OutputBuilder, ?>> simpleMergeOutput() {
						Map<DataIdentifier, BiConsumer<Output.OutputBuilder, ?>> result = new HashMap<>();
						result.put(new RosettaIdentifier("annex 1 table2 #28"), (builder, input) -> builder.setTransactionReferenceNumber(Converter.convert(String.class, input)));
						result.put(new StringIdentifier("transactionReferenceNumber"), (builder, input) -> builder.setTransactionReferenceNumber(Converter.convert(String.class, input)));
						return result;
					}
					
					private static final List<Function<Input, Mapper<String>>> ANNEX_1_TABLE2_28_MAPPINGS = Blueprint.of(
						i -> MapperS.of(i).map("getTraderef", Input::getTraderef)
					);
					
					protected abstract Output.OutputBuilder mergeOutputSupplier(INKEY k);
				}
			'''
			blueprint.compileToClasses
			assertEquals(expected, blueprintJava)
		} finally {
		}
	}

	@Test
	@Disabled
	def void getNestedMappings() {
		val blueprint = '''
			blueprint NotSoSimpleBlueprint 
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
			{
				SimpleMapping input Input
			}
			
			type Input:
				child Child (1..1)
			
			type Child:
				traderef string (1..1)
						[regulatoryReference ESMA MiFIR RTS_22 annex "1 table2 #28" provision "" reportedField]
						[regulatoryReference ESMA MiFIR RTS_22 annex "1 table2 #29" provision "" reportedField]
				tradeid int (1..1)
						[regulatoryReference ESMA MiFIR RTS_22 annex "1 table2 #30" provision ""]
						[regulatoryReference ESMA MiFIR RTS_22 annex "1 table2 #31" provision "" reportedField]
			
			type Output:
				transactionReferenceNumber string (1..1)
				[regulatoryReference ESMA MiFIR RTS_22 annex "1 table2 #28" provision "" reportedField]
			
		'''.generateCode

		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.NotSoSimpleBlueprint")
		// writeOutClasses(blueprint, "getNestedMappings");
		try {
			assertThat(blueprintJava, CoreMatchers.notNullValue())
			val expected = '''
				package com.rosetta.test.model.blueprint;
				
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.rosetta.model.lib.functions.Mapper;
				import com.rosetta.model.lib.functions.MapperS;
				import com.rosetta.model.lib.functions.MappingGroup;
				import com.rosetta.test.model.Child;
				import com.rosetta.test.model.Input;
				import java.util.Collection;
				import java.util.List;
				import java.util.function.Function;
				import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
				
				public abstract class NotSoSimpleBlueprint<INKEY extends Comparable<INKEY>> implements Blueprint<Input, Object, INKEY, INKEY> {
					@Override
					public String getName() {
						return "NotSoSimpleBlueprint"; 
					}
					
					@Override
					public String getURI() {
						return "__synthetic1.rosetta#NotSoSimpleBlueprint";
					}
					
					
					@Override
					public BlueprintInstance<Input, Object, INKEY, INKEY> blueprint() { 
						return 
							startsWith(BlueprintBuilder.<Input, INKEY>doSimpleMappings("__synthetic1.rosetta#//@elements.0/@nodes/@node", "simpleMappingsInput", simpleMappingsInput()))
							.toBlueprint(getURI(), getName());
					}
					
					protected Collection<MappingGroup<Input, ?>> simpleMappingsInput() {
						return Blueprint.of(
						new MappingGroup<>("annex 1 table2 #28", "__synthetic1.rosetta#//@elements.0/@nodes/@node", ANNEX_1_TABLE2_28_MAPPINGS),
						new MappingGroup<>("annex 1 table2 #29", "__synthetic1.rosetta#//@elements.0/@nodes/@node", ANNEX_1_TABLE2_29_MAPPINGS),
						new MappingGroup<>("annex 1 table2 #31", "__synthetic1.rosetta#//@elements.0/@nodes/@node", ANNEX_1_TABLE2_31_MAPPINGS));
					}
					
					private static final List<Function<Input, Mapper<String>>> ANNEX_1_TABLE2_28_MAPPINGS = Blueprint.of(
						i -> MapperS.of(i).map("getChild", Input::getChild).map("getTraderef", Child::getTraderef)
					);
					private static final List<Function<Input, Mapper<String>>> ANNEX_1_TABLE2_29_MAPPINGS = Blueprint.of(
						i -> MapperS.of(i).map("getChild", Input::getChild).map("getTraderef", Child::getTraderef)
					);
					private static final List<Function<Input, Mapper<Integer>>> ANNEX_1_TABLE2_31_MAPPINGS = Blueprint.of(
						i -> MapperS.of(i).map("getChild", Input::getChild).map("getTradeid", Child::getTradeid)
					);
				}
			'''
			blueprint.compileToClasses
			assertEquals(expected, blueprintJava)
		} finally {
		}

	}

	@Test
	@Disabled
	def void getNestedMappingsWithMultiplePaths() {
		val blueprint = '''
			blueprint NotSoSimpleBlueprint 
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
			{
				SimpleMapping input Input
			}
			
			type Input:
				childA Child (1..1)
				childB Child (1..1)
			
			type Child:
				traderef string (1..1)
					[regulatoryReference ESMA MiFIR RTS_22 annex "1 table2 #28" provision "" reportedField]
			
			type Output:
				transactionReferenceNumber string (1..1)
				[regulatoryReference ESMA MiFIR RTS_22 annex "1 table2 #28" provision "" reportedField]
			
		'''.generateCode

		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.NotSoSimpleBlueprint")
		// writeOutClasses(blueprint, "getNestedMappingsWithMultiplePaths");
		try {
			assertThat(blueprintJava, CoreMatchers.notNullValue())
			val expected = '''
				package com.rosetta.test.model.blueprint;
				
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.rosetta.model.lib.functions.Mapper;
				import com.rosetta.model.lib.functions.MapperS;
				import com.rosetta.model.lib.functions.MappingGroup;
				import com.rosetta.test.model.Child;
				import com.rosetta.test.model.Input;
				import java.util.Collection;
				import java.util.List;
				import java.util.function.Function;
				import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
				
				public abstract class NotSoSimpleBlueprint<INKEY extends Comparable<INKEY>> implements Blueprint<Input, Object, INKEY, INKEY> {
					@Override
					public String getName() {
						return "NotSoSimpleBlueprint"; 
					}
					
					@Override
					public String getURI() {
						return "__synthetic1.rosetta#NotSoSimpleBlueprint";
					}
					
					
					@Override
					public BlueprintInstance<Input, Object, INKEY, INKEY> blueprint() { 
						return 
							startsWith(BlueprintBuilder.<Input, INKEY>doSimpleMappings("__synthetic1.rosetta#//@elements.0/@nodes/@node", "simpleMappingsInput", simpleMappingsInput()))
							.toBlueprint(getURI(), getName());
					}
					
					protected Collection<MappingGroup<Input, ?>> simpleMappingsInput() {
						return Blueprint.of(
						new MappingGroup<>("annex 1 table2 #28", "__synthetic1.rosetta#//@elements.0/@nodes/@node", ANNEX_1_TABLE2_28_MAPPINGS));
					}
					
					private static final List<Function<Input, Mapper<String>>> ANNEX_1_TABLE2_28_MAPPINGS = Blueprint.of(
						i -> MapperS.of(i).map("getChildA", Input::getChildA).map("getTraderef", Child::getTraderef),
						i -> MapperS.of(i).map("getChildB", Input::getChildB).map("getTraderef", Child::getTraderef)
					);
				}
			'''
			blueprint.compileToClasses
			assertEquals(expected, blueprintJava)
		} finally {
		}

	}

	@Test
	@Disabled
	def void genMappingCollection() {
		val blueprint = '''
			blueprint SimpleBlueprint 
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
			{
				SimpleMapping input Input then
				merge output Output 
			}
			
			type Input:
				traderefs string (1..*)
						[regulatoryReference ESMA MiFIR RTS_22 annex "1 table2 #28" provision "" reportedField]
			
			type Output:
				transactionId string (1..1)
				[regulatoryReference ESMA MiFIR RTS_22 annex "1 table2 #28" provision "" reportedField]
				transactionNumber number (1..1)
				[regulatoryReference ESMA MiFIR RTS_22 annex "1 table2 #29" provision "" reportedField]
			
		'''.generateCode

		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.SimpleBlueprint")
		assertThat(blueprintJava, CoreMatchers.notNullValue())
		val expected = '''
			package com.rosetta.test.model.blueprint;
			
			import com.regnosys.rosetta.blueprints.Blueprint;
			import com.regnosys.rosetta.blueprints.BlueprintBuilder;
			import com.regnosys.rosetta.blueprints.BlueprintInstance;
			import com.regnosys.rosetta.blueprints.runner.actions.Merger;
			import com.regnosys.rosetta.blueprints.runner.data.DataIdentifier;
			import com.regnosys.rosetta.blueprints.runner.data.RosettaIdentifier;
			import com.regnosys.rosetta.blueprints.runner.data.StringIdentifier;
			import com.rosetta.model.lib.functions.Converter;
			import com.rosetta.model.lib.functions.Mapper;
			import com.rosetta.model.lib.functions.MapperS;
			import com.rosetta.model.lib.functions.MappingGroup;
			import com.rosetta.test.model.Input;
			import com.rosetta.test.model.Output;
			import java.math.BigDecimal;
			import java.util.Collection;
			import java.util.HashMap;
			import java.util.List;
			import java.util.Map;
			import java.util.function.BiConsumer;
			import java.util.function.Function;
			import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
			
			public abstract class SimpleBlueprint<INKEY extends Comparable<INKEY>> implements Blueprint<Input, Output, INKEY, INKEY> {
				@Override
				public String getName() {
					return "SimpleBlueprint"; 
				}
				
				@Override
				public String getURI() {
					return "__synthetic1.rosetta#SimpleBlueprint";
				}
				
				
				@Override
				public BlueprintInstance<Input, Output, INKEY, INKEY> blueprint() { 
					return 
						startsWith(BlueprintBuilder.<Input, INKEY>doSimpleMappings("__synthetic1.rosetta#//@elements.0/@nodes/@node", "simpleMappingsInput", simpleMappingsInput()))
						.then(new Merger<>("__synthetic1.rosetta#//@elements.0/@nodes/@next/@node", "Create Output", mergeOutput(), this::mergeOutputSupplier, Output.OutputBuilder::build, 
											new StringIdentifier("Output"), false))
						.toBlueprint(getURI(), getName());
				}
				
				protected Collection<MappingGroup<Input, ?>> simpleMappingsInput() {
					return Blueprint.of(
					new MappingGroup<>("annex 1 table2 #28", "__synthetic1.rosetta#//@elements.0/@nodes/@node", ANNEX_1_TABLE2_28_MAPPINGS));
				}
				
				protected abstract Function<DataIdentifier, BiConsumer<Output.OutputBuilder, ?>> mergeOutput();
				
				protected Map<DataIdentifier, BiConsumer<Output.OutputBuilder, ?>> simpleMergeOutput() {
					Map<DataIdentifier, BiConsumer<Output.OutputBuilder, ?>> result = new HashMap<>();
					result.put(new RosettaIdentifier("annex 1 table2 #28"), (builder, input) -> builder.setTransactionId(Converter.convert(String.class, input)));
					result.put(new RosettaIdentifier("annex 1 table2 #29"), (builder, input) -> builder.setTransactionNumber(Converter.convert(BigDecimal.class, input)));
					result.put(new StringIdentifier("transactionId"), (builder, input) -> builder.setTransactionId(Converter.convert(String.class, input)));
					result.put(new StringIdentifier("transactionNumber"), (builder, input) -> builder.setTransactionNumber(Converter.convert(BigDecimal.class, input)));
					return result;
				}
				
				private static final List<Function<Input, Mapper<String>>> ANNEX_1_TABLE2_28_MAPPINGS = Blueprint.of(
					i -> MapperS.of(i).mapC("getTraderefs", Input::getTraderefs)
				);
				
				protected abstract Output.OutputBuilder mergeOutputSupplier(INKEY k);
			}
		'''
		blueprint.compileToClasses
		assertEquals(expected, blueprintJava)
	}

	@Test
	@Disabled
	def void genMappingExtension() {
		val blueprint = '''
			blueprint SimpleBlueprint 
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
			{
				SimpleMapping input Input2 then
				merge output Output
			}
						
			type Input:
				traderef string (1..1)
						[regulatoryReference ESMA MiFIR RTS_22 annex "1 table2 #28" provision "" reportedField]
			
			type Input2 extends Input:
			 	first string (1..1)
			
			type Output:
				transactionReferenceNumber string (1..1)
				[regulatoryReference ESMA MiFIR RTS_22 annex "1 table2 #28" provision "" reportedField]
			
		'''.generateCode

		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.SimpleBlueprint")
		assertThat(blueprintJava, CoreMatchers.notNullValue())
		val expected = '''
			package com.rosetta.test.model.blueprint;
			
			import com.regnosys.rosetta.blueprints.Blueprint;
			import com.regnosys.rosetta.blueprints.BlueprintBuilder;
			import com.regnosys.rosetta.blueprints.BlueprintInstance;
			import com.regnosys.rosetta.blueprints.runner.actions.Merger;
			import com.regnosys.rosetta.blueprints.runner.data.DataIdentifier;
			import com.regnosys.rosetta.blueprints.runner.data.RosettaIdentifier;
			import com.regnosys.rosetta.blueprints.runner.data.StringIdentifier;
			import com.rosetta.model.lib.functions.Converter;
			import com.rosetta.model.lib.functions.Mapper;
			import com.rosetta.model.lib.functions.MapperS;
			import com.rosetta.model.lib.functions.MappingGroup;
			import com.rosetta.test.model.Input;
			import com.rosetta.test.model.Input2;
			import com.rosetta.test.model.Output;
			import java.util.Collection;
			import java.util.HashMap;
			import java.util.List;
			import java.util.Map;
			import java.util.function.BiConsumer;
			import java.util.function.Function;
			import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
			
			public abstract class SimpleBlueprint<INKEY extends Comparable<INKEY>> implements Blueprint<Input2, Output, INKEY, INKEY> {
				@Override
				public String getName() {
					return "SimpleBlueprint"; 
				}
				
				@Override
				public String getURI() {
					return "__synthetic1.rosetta#SimpleBlueprint";
				}
				
				
				@Override
				public BlueprintInstance<Input2, Output, INKEY, INKEY> blueprint() { 
					return 
						startsWith(BlueprintBuilder.<Input2, INKEY>doSimpleMappings("__synthetic1.rosetta#//@elements.0/@nodes/@node", "simpleMappingsInput2", simpleMappingsInput2()))
						.then(new Merger<>("__synthetic1.rosetta#//@elements.0/@nodes/@next/@node", "Create Output", mergeOutput(), this::mergeOutputSupplier, Output.OutputBuilder::build, 
											new StringIdentifier("Output"), false))
						.toBlueprint(getURI(), getName());
				}
				
				protected Collection<MappingGroup<Input2, ?>> simpleMappingsInput2() {
					return Blueprint.of(
					new MappingGroup<>("annex 1 table2 #28", "__synthetic1.rosetta#//@elements.0/@nodes/@node", ANNEX_1_TABLE2_28_MAPPINGS));
				}
				
				protected abstract Function<DataIdentifier, BiConsumer<Output.OutputBuilder, ?>> mergeOutput();
				
				protected Map<DataIdentifier, BiConsumer<Output.OutputBuilder, ?>> simpleMergeOutput() {
					Map<DataIdentifier, BiConsumer<Output.OutputBuilder, ?>> result = new HashMap<>();
					result.put(new RosettaIdentifier("annex 1 table2 #28"), (builder, input) -> builder.setTransactionReferenceNumber(Converter.convert(String.class, input)));
					result.put(new StringIdentifier("transactionReferenceNumber"), (builder, input) -> builder.setTransactionReferenceNumber(Converter.convert(String.class, input)));
					return result;
				}
				
				private static final List<Function<Input2, Mapper<String>>> ANNEX_1_TABLE2_28_MAPPINGS = Blueprint.of(
					i -> MapperS.of(i).map("getTraderef", Input::getTraderef)
				);
				
				protected abstract Output.OutputBuilder mergeOutputSupplier(INKEY k);
			}
		'''
		// writeOutClasses(blueprint);
		blueprint.compileToClasses
		assertEquals(expected, blueprintJava)
	}

	@Test
	def void validPath() {
		val blueprint = '''
			type Input:
				input2 Input2 (1..1)
			
			type Input2:
				colour string (1..1)
			
			reporting rule Blueprint1
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
				extract Input->input2->colour
		'''.generateCode
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.Blueprint1Rule")
		// writeOutClasses(blueprint, "validPath");
		try {
			assertThat(blueprintJava, CoreMatchers.notNullValue())
			val expected = '''
				package com.rosetta.test.model.blueprint;
				
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.test.model.Input2;
				// manual imports
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory;
				import com.regnosys.rosetta.blueprints.runner.data.StringIdentifier;
				import com.regnosys.rosetta.blueprints.runner.nodes.SinkNode;
				import com.regnosys.rosetta.blueprints.runner.nodes.SourceNode;
				import com.rosetta.test.model.Input;
				import com.rosetta.test.model.Input2;
				import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
				
				/**
				 * @version test
				 */
				public class Blueprint1Rule<INKEY> implements Blueprint<Input, String, INKEY, INKEY> {
				
					private final RosettaActionFactory actionFactory;
				
					public Blueprint1Rule(RosettaActionFactory actionFactory) {
						this.actionFactory = actionFactory;
					}
					
					@Override
					public String getName() {
						return "Blueprint1"; 
					}
					
					@Override
					public String getURI() {
						return "__synthetic1.rosetta#com.rosetta.test.model.Blueprint1";
					}
					
					
					@Override
					public BlueprintInstance<Input, String, INKEY, INKEY> blueprint() { 
						return 
							startsWith(actionFactory, actionFactory.<Input, String, INKEY>newRosettaSingleMapper("__synthetic1.rosetta#//@elements.2/@nodes/@node", "->input2->colour", new StringIdentifier("->input2->colour"), input -> MapperS.of(input).<Input2>map("getInput2", _input -> _input.getInput2()).<String>map("getColour", _input2 -> _input2.getColour())))
							.toBlueprint(getURI(), getName());
					}
				}
			'''
			blueprint.compileToClasses
			assertEquals(expected, blueprintJava)
		} finally {
		}
	}

	@Test
	@Disabled
	def void multipleExtract() {
		val model = '''
			type Input:
				input2 int (1..*)
			
			blueprint Blueprint1
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
			{
				extract multiple Input->input2 as "inputs"
			}
		'''.parseRosetta
		
		model.assertWarning(BLUEPRINT_EXTRACT, null, "multiple keyword is redundant and deprecated")
		
		val blueprint = model.generateCode
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.Blueprint1")
		// writeOutClasses(blueprint, "multipleExtract");
		try {
			assertThat(blueprintJava, CoreMatchers.notNullValue())
			val expected = '''
				package com.rosetta.test.model.blueprint;
				
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.regnosys.rosetta.blueprints.runner.data.StringIdentifier;
				import com.rosetta.model.lib.functions.MapperS;
				import com.rosetta.test.model.Input;
				import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
				
				public abstract class Blueprint1<INKEY extends Comparable<INKEY>> implements Blueprint<Input, Integer, INKEY, INKEY> {
					@Override
					public String getName() {
						return "Blueprint1"; 
					}
					
					@Override
					public String getURI() {
						return "__synthetic1.rosetta#com.rosetta.test.model.com.rosetta.test.model.Blueprint1";
					}
					
					
					@Override
					public BlueprintInstance<Input, Integer, INKEY, INKEY> blueprint() { 
						return 
							startsWith(getRosettaActionFactory().<Input, Integer, INKEY>newRosettaMultipleMapper("__synthetic1.rosetta#//@elements.1/@nodes/@node", "->input2", new StringIdentifier("inputs"), input -> MapperS.of(input).mapC("getInput2", Input::getInput2)))
							.toBlueprint(getURI(), getName());
					}
				}
			'''
			blueprint.compileToClasses
			assertEquals(expected, blueprintJava)
		} finally {
		}
	}

	@Test
	def void invalidPath() {
		'''
			type Input:
				input2 Input2 (1..1)
			
			type Input2:
				colour string (1..1)
			
			reporting rule Blueprint1
				extract Input->input2->name
		'''.parseRosetta.assertError(ROSETTA_FEATURE_CALL, RosettaIssueCodes.MISSING_ATTRIBUTE,
			"attempted to reference unknown field of Input2")
	}

	@Test
	def void brokenAndInputTypes() {
		'''
			reporting rule Blueprint1
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
				( extract Input->traderef , extract Input2->colour)
						
			type Input:
				traderef string (1..1)
			
			type Input2:
				colour string (1..1)
			
		'''.parseRosetta.assertError(BLUEPRINT_EXTRACT, RosettaIssueCodes.TYPE_ERROR,
			"Input type of Input2 is not assignable from type Input of previous node ")
	}
	
	@Test
	def void multiplicationExpression() {
		val model = '''
			reporting rule Blueprint1
				extract Input->traderef * Input2->colour
						
			type Input:
				traderef int (1..1)
			
			type Input2:
				colour int (1..1)
			
		'''.parseRosetta
		model.assertError(BLUEPRINT_NODE_EXP, RosettaIssueCodes.TYPE_ERROR, 
			"Input types must be the same but were Input and Input2")
	}
	
	@Test
	def void brokenExpressionInputTypes() {
		val model = '''
			reporting rule Blueprint1
				extract Input->traderef * Input->colour
						
			type Input:
				traderef int (1..1)
				colour int (1..1)
			
		'''.parseRosetta
		model.generateCode.compileToClasses
	}

	@Test
	def void andInputTypesExtends() {
		val code = '''
			reporting rule Blueprint1
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
				( extract Input->traderef , extract Input2->colour)
						
			type Input:
				traderef string (1..1)
			
			type Input2 extends Input:
				colour string (1..1)
			
		'''.generateCode
		code.compileToClasses
	}

	@Test
	def void complexAnd() {
		val blueprint = '''
			reporting rule Blueprint1
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
				(filter when Input->traderef="3" then extract Input->traderef , extract Input->colour)
						
			type Input:
				traderef string (1..1)
				colour string (1..1)
		'''.generateCode

		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.Blueprint1Rule")
		 //writeOutClasses(blueprint, "complexAnd");
		try {
			assertThat(blueprintJava, CoreMatchers.notNullValue())
			val expected = '''
			package com.rosetta.test.model.blueprint;
			
			import com.rosetta.model.lib.mapper.MapperS;
			import static com.rosetta.model.lib.expression.ExpressionOperators.*;
			// manual imports
			import com.regnosys.rosetta.blueprints.Blueprint;
			import com.regnosys.rosetta.blueprints.BlueprintBuilder;
			import com.regnosys.rosetta.blueprints.BlueprintInstance;
			import com.regnosys.rosetta.blueprints.runner.actions.Filter;
			import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory;
			import com.regnosys.rosetta.blueprints.runner.data.StringIdentifier;
			import com.regnosys.rosetta.blueprints.runner.nodes.SinkNode;
			import com.regnosys.rosetta.blueprints.runner.nodes.SourceNode;
			import com.rosetta.test.model.Input;
			import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
			
			/**
			 * @version test
			 */
			public class Blueprint1Rule<INKEY> implements Blueprint<Input, String, INKEY, INKEY> {
			
				private final RosettaActionFactory actionFactory;
			
				public Blueprint1Rule(RosettaActionFactory actionFactory) {
					this.actionFactory = actionFactory;
				}
				
				@Override
				public String getName() {
					return "Blueprint1"; 
				}
				
				@Override
				public String getURI() {
					return "__synthetic1.rosetta#com.rosetta.test.model.Blueprint1";
				}
				
				
				@Override
				public BlueprintInstance<Input, String, INKEY, INKEY> blueprint() { 
					return 
						startsWith(actionFactory, BlueprintBuilder.<Input, String, INKEY, INKEY>and(actionFactory,
							startsWith(actionFactory, new Filter<Input, INKEY>("__synthetic1.rosetta#//@elements.0/@nodes/@node/@bps.0/@node", "->traderef=\"3\"", input -> areEqual(MapperS.of(input).<String>map("getTraderef", _input -> _input.getTraderef()), MapperS.of("3")).get(), null))
							.then(actionFactory.<Input, String, INKEY>newRosettaSingleMapper("__synthetic1.rosetta#//@elements.0/@nodes/@node/@bps.0/@next/@node", "->traderef", new StringIdentifier("->traderef"), input -> MapperS.of(input).<String>map("getTraderef", _input -> _input.getTraderef()))),
							startsWith(actionFactory, actionFactory.<Input, String, INKEY>newRosettaSingleMapper("__synthetic1.rosetta#//@elements.0/@nodes/@node/@bps.1/@node", "->colour", new StringIdentifier("->colour"), input -> MapperS.of(input).<String>map("getColour", _input -> _input.getColour())))
							)
						)
						.toBlueprint(getURI(), getName());
				}
			}
			'''
			blueprint.compileToClasses
			assertEquals(expected, blueprintJava)
		} finally {
		}
	}

	@Test
	def void numberAnd() {
		val blueprint = '''
			reporting rule Blueprint1
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
				(extract Input->a , extract Input->b)
						
			type Input:
				a int (1..1)
				b number (1..1)
		'''.generateCode

		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.Blueprint1Rule")
		// writeOutClasses(blueprint, "numberAnd");
		try {
			assertThat(blueprintJava, CoreMatchers.notNullValue())
			val expected = '''
				package com.rosetta.test.model.blueprint;
				
				import com.rosetta.model.lib.mapper.MapperS;
				import java.math.BigDecimal;
				// manual imports
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory;
				import com.regnosys.rosetta.blueprints.runner.data.StringIdentifier;
				import com.regnosys.rosetta.blueprints.runner.nodes.SinkNode;
				import com.regnosys.rosetta.blueprints.runner.nodes.SourceNode;
				import com.rosetta.test.model.Input;
				import java.math.BigDecimal;
				import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
				
				/**
				 * @version test
				 */
				public class Blueprint1Rule<INKEY> implements Blueprint<Input, Number, INKEY, INKEY> {
				
					private final RosettaActionFactory actionFactory;
				
					public Blueprint1Rule(RosettaActionFactory actionFactory) {
						this.actionFactory = actionFactory;
					}
					
					@Override
					public String getName() {
						return "Blueprint1"; 
					}
					
					@Override
					public String getURI() {
						return "__synthetic1.rosetta#com.rosetta.test.model.Blueprint1";
					}
					
					
					@Override
					public BlueprintInstance<Input, Number, INKEY, INKEY> blueprint() { 
						return 
							startsWith(actionFactory, BlueprintBuilder.<Input, Number, INKEY, INKEY>and(actionFactory,
								startsWith(actionFactory, actionFactory.<Input, Integer, INKEY>newRosettaSingleMapper("__synthetic1.rosetta#//@elements.0/@nodes/@node/@bps.0/@node", "->a", new StringIdentifier("->a"), input -> MapperS.of(input).<Integer>map("getA", _input -> _input.getA()))),
								startsWith(actionFactory, actionFactory.<Input, BigDecimal, INKEY>newRosettaSingleMapper("__synthetic1.rosetta#//@elements.0/@nodes/@node/@bps.1/@node", "->b", new StringIdentifier("->b"), input -> MapperS.of(input).<BigDecimal>map("getB", _input -> _input.getB())))
								)
							)
							.toBlueprint(getURI(), getName());
					}
				}
			'''
			blueprint.compileToClasses
			assertEquals(expected, blueprintJava)
		} finally {
		}
	}

	@Test
	def void complexAnd2() {
		val blueprint = '''
			reporting rule Blueprint1
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
				(
					extract Input -> foo
					,
					extract Input -> bar
				)
			
			type Input:
				foo Foo (1..1)
				bar Bar (1..1)
			
			type Foo:
				traderef string (1..1)
				colour string (1..1)
			
			type Bar:
				traderef string (1..1)
				colour string (1..1)
			
		'''.generateCode

		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.Blueprint1Rule")
		// writeOutClasses(blueprint, "complexAnd2");
		try {
			assertThat(blueprintJava, CoreMatchers.notNullValue())
			val expected = '''
				package com.rosetta.test.model.blueprint;
				
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.test.model.Bar;
				import com.rosetta.test.model.Foo;
				// manual imports
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory;
				import com.regnosys.rosetta.blueprints.runner.data.StringIdentifier;
				import com.regnosys.rosetta.blueprints.runner.nodes.SinkNode;
				import com.regnosys.rosetta.blueprints.runner.nodes.SourceNode;
				import com.rosetta.test.model.Bar;
				import com.rosetta.test.model.Foo;
				import com.rosetta.test.model.Input;
				import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
				
				/**
				 * @version test
				 */
				public class Blueprint1Rule<INKEY> implements Blueprint<Input, Object, INKEY, INKEY> {
				
					private final RosettaActionFactory actionFactory;
				
					public Blueprint1Rule(RosettaActionFactory actionFactory) {
						this.actionFactory = actionFactory;
					}
					
					@Override
					public String getName() {
						return "Blueprint1"; 
					}
					
					@Override
					public String getURI() {
						return "__synthetic1.rosetta#com.rosetta.test.model.Blueprint1";
					}
					
					
					@Override
					public BlueprintInstance<Input, Object, INKEY, INKEY> blueprint() { 
						return 
							startsWith(actionFactory, BlueprintBuilder.<Input, Object, INKEY, INKEY>and(actionFactory,
								startsWith(actionFactory, actionFactory.<Input, Foo, INKEY>newRosettaSingleMapper("__synthetic1.rosetta#//@elements.0/@nodes/@node/@bps.0/@node", "->foo", new StringIdentifier("->foo"), input -> MapperS.of(input).<Foo>map("getFoo", _input -> _input.getFoo()))),
								startsWith(actionFactory, actionFactory.<Input, Bar, INKEY>newRosettaSingleMapper("__synthetic1.rosetta#//@elements.0/@nodes/@node/@bps.1/@node", "->bar", new StringIdentifier("->bar"), input -> MapperS.of(input).<Bar>map("getBar", _input -> _input.getBar())))
								)
							)
							.toBlueprint(getURI(), getName());
					}
				}
			'''
			blueprint.compileToClasses
			assertEquals(expected, blueprintJava)
		} finally {
		}
	}

	@Test
	def void complexAnd3() {
		val blueprint = '''
			reporting rule Blueprint1
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
				(
					extract Input1->i1
					,
					extract Input1->i2
				) then
				extract Input2->traderef
						
			type Input1:
				i1 Input2 (1..1)
				i2 Input2 (1..1)
			
			type Input2:
				traderef string (1..1)
		'''.generateCode

		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.Blueprint1Rule")
		// writeOutClasses(blueprint, "complexAnd3");
		try {
			assertThat(blueprintJava, CoreMatchers.notNullValue())
			val expected = '''
				package com.rosetta.test.model.blueprint;
				
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.test.model.Input2;
				// manual imports
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory;
				import com.regnosys.rosetta.blueprints.runner.data.StringIdentifier;
				import com.regnosys.rosetta.blueprints.runner.nodes.SinkNode;
				import com.regnosys.rosetta.blueprints.runner.nodes.SourceNode;
				import com.rosetta.test.model.Input1;
				import com.rosetta.test.model.Input2;
				import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
				
				/**
				 * @version test
				 */
				public class Blueprint1Rule<INKEY> implements Blueprint<Input1, String, INKEY, INKEY> {
				
					private final RosettaActionFactory actionFactory;
				
					public Blueprint1Rule(RosettaActionFactory actionFactory) {
						this.actionFactory = actionFactory;
					}
					
					@Override
					public String getName() {
						return "Blueprint1"; 
					}
					
					@Override
					public String getURI() {
						return "__synthetic1.rosetta#com.rosetta.test.model.Blueprint1";
					}
					
					
					@Override
					public BlueprintInstance<Input1, String, INKEY, INKEY> blueprint() { 
						return 
							startsWith(actionFactory, BlueprintBuilder.<Input1, Input2, INKEY, INKEY>and(actionFactory,
								startsWith(actionFactory, actionFactory.<Input1, Input2, INKEY>newRosettaSingleMapper("__synthetic1.rosetta#//@elements.0/@nodes/@node/@bps.0/@node", "->i1", new StringIdentifier("->i1"), input1 -> MapperS.of(input1).<Input2>map("getI1", _input1 -> _input1.getI1()))),
								startsWith(actionFactory, actionFactory.<Input1, Input2, INKEY>newRosettaSingleMapper("__synthetic1.rosetta#//@elements.0/@nodes/@node/@bps.1/@node", "->i2", new StringIdentifier("->i2"), input1 -> MapperS.of(input1).<Input2>map("getI2", _input1 -> _input1.getI2())))
								)
							)
							.then(actionFactory.<Input2, String, INKEY>newRosettaSingleMapper("__synthetic1.rosetta#//@elements.0/@nodes/@next/@node", "->traderef", new StringIdentifier("->traderef"), input2 -> MapperS.of(input2).<String>map("getTraderef", _input2 -> _input2.getTraderef())))
							.toBlueprint(getURI(), getName());
					}
				}
			'''
			blueprint.compileToClasses
			assertEquals(expected, blueprintJava)
		} finally {
		}
	}

	@Test
	@Disabled
	def void genNestedBlueprints() {
		val blueprint = '''
			reporting rule Blueprint1
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
				( extract Input->traderef , extract Input->input2->colour)
				then Blueprint2
			
			reporting rule Blueprint2
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
				merge output Output
						
			type Input:
				traderef string (1..1)
				input2 Input2 (1..1)
			
			type Input2:
				colour string (1..1)
			
			type Output:
				transactionReferenceNumber string (1..1)
				colour string (1..1)
		'''.generateCode

		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.Blueprint1Rule")
		// writeOutClasses(blueprint, "genNestedBlueprints");
		try {
			assertThat(blueprintJava, CoreMatchers.notNullValue())
			val expected = '''
				package com.rosetta.test.model.blueprint;
				
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.regnosys.rosetta.blueprints.runner.data.StringIdentifier;
				import com.rosetta.model.lib.functions.MapperS;
				import com.rosetta.test.model.Input;
				import com.rosetta.test.model.Input2;
				import com.rosetta.test.model.Output;
				import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
				
				public abstract class Blueprint1<INKEY> implements Blueprint<Input, Output, INKEY, INKEY> {
					@Override
					public String getName() {
						return "Blueprint1"; 
					}
					
					@Override
					public String getURI() {
						return "__synthetic1.rosetta#com.rosetta.test.model.Blueprint1";
					}
					
					
					@Override
					public BlueprintInstance<Input, Output, INKEY, INKEY> blueprint() { 
						return 
							startsWith(BlueprintBuilder.<Input, String, INKEY, INKEY>and(
								startsWith(getRosettaActionFactory().<Input, String, INKEY>newRosettaSingleMapper("__synthetic1.rosetta#//@elements.0/@nodes/@node/@bps.0/@node", "->traderef", new StringIdentifier("->traderef"), input -> MapperS.of(input).map("getTraderef", Input::getTraderef))),
								startsWith(getRosettaActionFactory().<Input, String, INKEY>newRosettaSingleMapper("__synthetic1.rosetta#//@elements.0/@nodes/@node/@bps.1/@node", "->input2->colour", new StringIdentifier("->input2->colour"), input -> MapperS.of(input).map("getInput2", Input::getInput2).map("getColour", Input2::getColour)))
								)
							)
							.then(getBlueprint2())
							.toBlueprint(getURI(), getName());
					}
					
					protected abstract BlueprintInstance<String, Output, INKEY, INKEY> getBlueprint2();
				}
			'''
			blueprint.compileToClasses
			assertEquals(expected, blueprintJava)
		} finally {
		}
	}

	@Test
	@Disabled
	def void ingest() {
		val blueprint = '''
			blueprint SimpleBlueprint 
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
			{
				bpsource ISource <string, string> then
				ingest to Input
			}
						
			type Input:
				traderef string (1..1)
		'''.generateCode
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.SimpleBlueprint")
		// writeOutClasses(blueprint, "ingest");
		blueprint.compileToClasses
		try {
			assertThat(blueprintJava, CoreMatchers.notNullValue())
			val expected = '''
				package com.rosetta.test.model.blueprint;
				
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.regnosys.rosetta.blueprints.runner.nodes.SourceNode;
				import com.rosetta.test.model.Input;
				import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
				
				public abstract class SimpleBlueprint implements Blueprint<Void, Input, String, String> {
					@Override
					public String getName() {
						return "SimpleBlueprint"; 
					}
					
					@Override
					public String getURI() {
						return "__synthetic1.rosetta#SimpleBlueprint";
					}
					
					
					@Override
					public BlueprintInstance<Void, Input, String, String> blueprint() { 
						return 
							startsWith(getISource())
							.then(getRosettaActionFactory().<Input, String>newRosettaIngester("__synthetic1.rosetta#//@elements.0/@nodes/@next/@node", "Map to Rosetta", Input.class))
							.toBlueprint(getURI(), getName());
					}
					
					protected abstract SourceNode<String, String> getISource();
				}
			'''
			assertEquals(expected, blueprintJava)
		} finally {
		}
	}

	@Test
	@Disabled
	def void validate() {
		val blueprint = '''
			blueprint SimpleBlueprint 
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
			{
				validate as Input
			}
									
			type Input:
				traderef string (1..1)
			
		'''.generateCode
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.SimpleBlueprint")
		// writeOutClasses(blueprint, "validate");
		try {
			assertThat(blueprintJava, CoreMatchers.notNullValue())
			val expected = '''
				package com.rosetta.test.model.blueprint;
				
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.rosetta.test.model.Input;
				import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
				
				public abstract class SimpleBlueprint<INKEY extends Comparable<INKEY>> implements Blueprint<Input, Input, INKEY, INKEY> {
					@Override
					public String getName() {
						return "SimpleBlueprint"; 
					}
					
					@Override
					public String getURI() {
						return "__synthetic1.rosetta#SimpleBlueprint";
					}
					
					
					@Override
					public BlueprintInstance<Input, Input, INKEY, INKEY> blueprint() { 
						return 
							startsWith(getRosettaActionFactory().<Input, INKEY>newRosettaValidator("__synthetic1.rosetta#//@elements.0/@nodes/@node", "Validate Rosetta", Input.class))
							.toBlueprint(getURI(), getName());
					}
				}
			'''
			blueprint.compileToClasses
			assertEquals(expected, blueprintJava)
		} finally {
		}
	}

	@Test
	def void ruleRef() {
		'''
			type Foo:
				bar Bar (1..1)
			
			type Bar:
				val string (1..1)
			
			reporting rule Rule1
				Rule2 then
				extract Bar->val
			
			reporting rule Rule2
				extract Foo->bar
			
		'''.parseRosettaWithNoErrors
	}

	@Test
	def void filter() {
		val blueprint = '''
			reporting rule SimpleBlueprint 
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
				filter when Input->traderef="Hello"
			
			type Input:
				traderef string (1..1)
			
		'''.generateCode
		blueprint.writeOutClasses("blueprint.filter")
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.SimpleBlueprintRule")
		// writeOutClasses(blueprint, "filter");
		assertThat(blueprintJava, CoreMatchers.notNullValue())
		val expected = '''
		package com.rosetta.test.model.blueprint;
		
		import com.rosetta.model.lib.mapper.MapperS;
		import static com.rosetta.model.lib.expression.ExpressionOperators.*;
		// manual imports
		import com.regnosys.rosetta.blueprints.Blueprint;
		import com.regnosys.rosetta.blueprints.BlueprintBuilder;
		import com.regnosys.rosetta.blueprints.BlueprintInstance;
		import com.regnosys.rosetta.blueprints.runner.actions.Filter;
		import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory;
		import com.regnosys.rosetta.blueprints.runner.nodes.SinkNode;
		import com.regnosys.rosetta.blueprints.runner.nodes.SourceNode;
		import com.rosetta.test.model.Input;
		import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
		
		/**
		 * @version test
		 */
		public class SimpleBlueprintRule<INKEY> implements Blueprint<Input, Input, INKEY, INKEY> {
		
			private final RosettaActionFactory actionFactory;
		
			public SimpleBlueprintRule(RosettaActionFactory actionFactory) {
				this.actionFactory = actionFactory;
			}
			
			@Override
			public String getName() {
				return "SimpleBlueprint"; 
			}
			
			@Override
			public String getURI() {
				return "__synthetic1.rosetta#com.rosetta.test.model.SimpleBlueprint";
			}
			
			
			@Override
			public BlueprintInstance<Input, Input, INKEY, INKEY> blueprint() { 
				return 
					startsWith(actionFactory, new Filter<Input, INKEY>("__synthetic1.rosetta#//@elements.0/@nodes/@node", "->traderef=\"Hello\"", input -> areEqual(MapperS.of(input).<String>map("getTraderef", _input -> _input.getTraderef()), MapperS.of("Hello")).get(), null))
					.toBlueprint(getURI(), getName());
			}
		}
		'''
		blueprint.compileToClasses
		assertEquals(expected, blueprintJava)
	}

	@Test
	def void filter2() {
		val blueprint = '''
			reporting rule SimpleBlueprint 
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
				filter when Input->traderef exists
									
			type Input:
				traderef string (1..1)
			
		'''.generateCode
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.SimpleBlueprintRule")
		// writeOutClasses(blueprint, "filter2");
		assertThat(blueprintJava, CoreMatchers.notNullValue())

		blueprint.compileToClasses
	}

	@Test
	def void filterWhenRule() {
		val blueprint = '''
			reporting rule TestRule
				extract Input->flag
						
			reporting rule FilterRule
				filter when rule TestRule then extract Input->traderef
			
			
			type Input:
				traderef string (1..1)
				flag boolean (1..1)
			
		'''.generateCode
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.FilterRuleRule")
		// writeOutClasses(blueprint, "filterWhenRule");
		assertThat(blueprintJava, CoreMatchers.notNullValue())

		blueprint.compileToClasses
	}

	@Test
	def void lookupRule() {
		val blueprint = '''
			reporting rule WorthyAvenger
				filter when rule CanWieldMjolnir 
					then extract Avengers -> heros
					then filter when Hero -> name <> 'Thor'
					then extract Hero -> name
			
			eligibility rule CanWieldMjolnir
				extract Avengers -> heros then 
				lookup CanWieldMjolnir boolean
			
			type Avengers:
				heros Hero (0..*)
			
			type Hero:
				name string (1..1)
			
		'''.generateCode
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.WorthyAvengerRule")
		blueprint.compileToClasses

		val expected = '''
			package com.rosetta.test.model.blueprint;
			
			import com.rosetta.model.lib.mapper.MapperS;
			import com.rosetta.test.model.Hero;
			import static com.rosetta.model.lib.expression.ExpressionOperators.*;
			// manual imports
			import com.regnosys.rosetta.blueprints.Blueprint;
			import com.regnosys.rosetta.blueprints.BlueprintBuilder;
			import com.regnosys.rosetta.blueprints.BlueprintInstance;
			import com.regnosys.rosetta.blueprints.runner.actions.Filter;
			import com.regnosys.rosetta.blueprints.runner.actions.FilterByRule;
			import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory;
			import com.regnosys.rosetta.blueprints.runner.data.StringIdentifier;
			import com.regnosys.rosetta.blueprints.runner.nodes.SinkNode;
			import com.regnosys.rosetta.blueprints.runner.nodes.SourceNode;
			import com.rosetta.test.model.Avengers;
			import com.rosetta.test.model.Hero;
			import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
			
			/**
			 * @version test
			 */
			public class WorthyAvengerRule<INKEY> implements Blueprint<Avengers, String, INKEY, INKEY> {
			
				private final RosettaActionFactory actionFactory;
			
				public WorthyAvengerRule(RosettaActionFactory actionFactory) {
					this.actionFactory = actionFactory;
				}
				
				@Override
				public String getName() {
					return "WorthyAvenger"; 
				}
				
				@Override
				public String getURI() {
					return "__synthetic1.rosetta#com.rosetta.test.model.WorthyAvenger";
				}
				
				
				@Override
				public BlueprintInstance<Avengers, String, INKEY, INKEY> blueprint() { 
					return 
						startsWith(actionFactory, new FilterByRule<Avengers, INKEY>("__synthetic1.rosetta#//@elements.0/@nodes/@node", "CanWieldMjolnir", new CanWieldMjolnirRule<INKEY>(actionFactory).blueprint(), null))
						.then(actionFactory.<Avengers, Hero, INKEY>newRosettaMultipleMapper("__synthetic1.rosetta#//@elements.0/@nodes/@next/@node", "->heros", new StringIdentifier("->heros"), avengers -> MapperS.of(avengers).<Hero>mapC("getHeros", _avengers -> _avengers.getHeros())))
						.then(new Filter<Hero, INKEY>("__synthetic1.rosetta#//@elements.0/@nodes/@next/@next/@node", "->name<>\"Thor\"", hero -> notEqual(MapperS.of(hero).<String>map("getName", _hero -> _hero.getName()), MapperS.of("Thor")).get(), null))
						.then(actionFactory.<Hero, String, INKEY>newRosettaSingleMapper("__synthetic1.rosetta#//@elements.0/@nodes/@next/@next/@next/@node", "->name", new StringIdentifier("->name"), hero -> MapperS.of(hero).<String>map("getName", _hero -> _hero.getName())))
						.toBlueprint(getURI(), getName());
				}
			}
		'''
		assertEquals(expected, blueprintJava)

	}

	@Test
	def void filterWhenRuleBrokenType() {
		'''
			reporting rule TestRule
				extract Input->flag
						
			reporting rule FilterRule
				filter when rule TestRule then extract Input->traderef
			
			
			type Input:
				traderef string (1..1)
				flag number (1..1)
			
		'''.parseRosetta.assertError(BLUEPRINT_EXTRACT, RosettaIssueCodes.TYPE_ERROR,
			"output type of node BigDecimal does not match required type of Boolean")

	}

	@Test
	def void filterWhenCount() {
		val blueprint = '''
			reporting rule IsFixedFloat
			extract Foo->fixed count = 12
			
			type Foo:
				fixed string (0..*)
				floating string (0..*)
			
		'''.generateCode
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.IsFixedFloatRule")
		// writeOutClasses(blueprint, "filterWhenCount");
		assertThat(blueprintJava, CoreMatchers.notNullValue())
		val expected = '''
			package com.rosetta.test.model.blueprint;
			
			import com.rosetta.model.lib.mapper.MapperS;
			import static com.rosetta.model.lib.expression.ExpressionOperators.*;
			// manual imports
			import com.regnosys.rosetta.blueprints.Blueprint;
			import com.regnosys.rosetta.blueprints.BlueprintBuilder;
			import com.regnosys.rosetta.blueprints.BlueprintInstance;
			import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory;
			import com.regnosys.rosetta.blueprints.runner.data.StringIdentifier;
			import com.regnosys.rosetta.blueprints.runner.nodes.SinkNode;
			import com.regnosys.rosetta.blueprints.runner.nodes.SourceNode;
			import com.rosetta.test.model.Foo;
			import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
			
			/**
			 * @version test
			 */
			public class IsFixedFloatRule<INKEY> implements Blueprint<Foo, Boolean, INKEY, INKEY> {
			
				private final RosettaActionFactory actionFactory;
			
				public IsFixedFloatRule(RosettaActionFactory actionFactory) {
					this.actionFactory = actionFactory;
				}
				
				@Override
				public String getName() {
					return "IsFixedFloat"; 
				}
				
				@Override
				public String getURI() {
					return "__synthetic1.rosetta#com.rosetta.test.model.IsFixedFloat";
				}
				
				
				@Override
				public BlueprintInstance<Foo, Boolean, INKEY, INKEY> blueprint() { 
					return 
						startsWith(actionFactory, actionFactory.<Foo, Boolean, INKEY>newRosettaSingleMapper("__synthetic1.rosetta#//@elements.0/@nodes/@node", "->fixed count=12", new StringIdentifier("->fixed count=12"), foo -> areEqual(MapperS.of(MapperS.of(foo).<String>mapC("getFixed", _foo -> _foo.getFixed()).resultCount()), MapperS.of(Integer.valueOf(12)))))
						.toBlueprint(getURI(), getName());
				}
			}
		'''
		blueprint.compileToClasses
		assertEquals(expected, blueprintJava)

	}

	@Test
	def void oneOf() {
		val blueprint = '''
			reporting rule FixedFloat
				if extract Foo -> fixed = "Wood" do extract Foo -> floating
					else if extract Foo -> fixed do extract Foo -> sinking
					else do extract Foo -> swimming
				endif
					
			
			type Foo:
				fixed string (0..*)
				floating string (0..*)
				sinking string (1..1)
				swimming string (1..1)
			
		'''.generateCode
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.FixedFloatRule")
		// writeOutClasses(blueprint, "blueprintOneOf");
		assertThat(blueprintJava, CoreMatchers.notNullValue())
		blueprint.compileToClasses

	}

	@Test
	def void maxBy() {
		val blueprint = '''
			reporting rule IsFixedFloat
			maxBy Foo->order
			
			type Foo:
				fixed string (0..*)
				order int (0..1)
			
		'''.generateCode
		// writeOutClasses(blueprint, "maxBy");
		blueprint.compileToClasses
	}
	
	@Test
	def void maxByRule() {
		val blueprint = '''
			reporting rule IsFixedFloat
			maxBy rule MinFixed
			
			reporting rule MinFixed
				extract Foo->fixed then
				minimum
			
			type Foo:
				fixed string (0..*)
				order int (0..1)
			
		'''.parseRosetta
		val code=blueprint.generateCode
		writeOutClasses(code, "maxByRule");
		code.compileToClasses
	}
	
	@Test
	def void maxByBrokenTypeAndCardinality() {
		val model = '''
			reporting rule IsFixedFloat
			maxBy Foo->order
			
			type Bar:
				thing int (1..1)
			
			type Foo:
				fixed string (0..*)
				order Bar (0..*)
			
		'''.parseRosetta
		model.assertError(BLUEPRINT_REDUCE, RosettaIssueCodes.TYPE_ERROR,
			"The expression for maxBy must return a comparable type (e.g. number or date) the curent expression returns Bar")
		model.assertError(BLUEPRINT_REDUCE, null,
			"The expression for maxBy must return a single value the curent expression returns multiple values")
	}

	@Test
	def void group() {
		val blueprint = '''
			reporting rule SimpleBlueprint 
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
				groupby Input->traderef
									
			type Input:
				traderef string (1..1)
			
		'''.generateCode
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.SimpleBlueprintRule")
		// writeOutClasses(blueprint, "group");
		blueprint.compileToClasses
		try {
			assertThat(blueprintJava, CoreMatchers.notNullValue())
			val expected = '''
				package com.rosetta.test.model.blueprint;
				
				import com.rosetta.model.lib.mapper.MapperS;
				// manual imports
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory;
				import com.regnosys.rosetta.blueprints.runner.nodes.SinkNode;
				import com.regnosys.rosetta.blueprints.runner.nodes.SourceNode;
				import com.rosetta.test.model.Input;
				import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
				
				/**
				 * @version test
				 */
				public class SimpleBlueprintRule<INKEY> implements Blueprint<Input, Input, INKEY, String> {
				
					private final RosettaActionFactory actionFactory;
				
					public SimpleBlueprintRule(RosettaActionFactory actionFactory) {
						this.actionFactory = actionFactory;
					}
					
					@Override
					public String getName() {
						return "SimpleBlueprint"; 
					}
					
					@Override
					public String getURI() {
						return "__synthetic1.rosetta#com.rosetta.test.model.SimpleBlueprint";
					}
					
					
					@Override
					public BlueprintInstance<Input, Input, INKEY, String> blueprint() { 
						return 
							startsWith(actionFactory, actionFactory.<Input, INKEY, String>newRosettaGrouper("__synthetic1.rosetta#//@elements.0/@nodes/@node", "group by ->traderef", null, input -> MapperS.of(input).<String>map("getTraderef", _input -> _input.getTraderef())))
							.toBlueprint(getURI(), getName());
					}
				}
			'''
			assertEquals(expected, blueprintJava)
		} finally {
		}
	}

	@Test
	def void join() {
		val blueprint = '''
			reporting rule SimpleBlueprint 
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
				join key Input2->keyVal foreignKey Input1->foreign
									
			type Input1:
				foreign string (1..*)
			
			type Input2:
				keyVal string (1..1)
			
		'''.generateCode
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.SimpleBlueprintRule")
		// writeOutClasses(blueprint, "join");
		try {
			assertThat(blueprintJava, CoreMatchers.notNullValue())
			val expected = '''
				package com.rosetta.test.model.blueprint;
				
				import com.rosetta.model.lib.mapper.MapperS;
				// manual imports
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory;
				import com.regnosys.rosetta.blueprints.runner.nodes.SinkNode;
				import com.regnosys.rosetta.blueprints.runner.nodes.SourceNode;
				import com.rosetta.test.model.Input1;
				import com.rosetta.test.model.Input2;
				import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
				
				/**
				 * @version test
				 */
				public class SimpleBlueprintRule<INKEY> implements Blueprint<Object, Input2, INKEY, INKEY> {
				
					private final RosettaActionFactory actionFactory;
				
					public SimpleBlueprintRule(RosettaActionFactory actionFactory) {
						this.actionFactory = actionFactory;
					}
					
					@Override
					public String getName() {
						return "SimpleBlueprint"; 
					}
					
					@Override
					public String getURI() {
						return "__synthetic1.rosetta#com.rosetta.test.model.SimpleBlueprint";
					}
					
					
					@Override
					public BlueprintInstance<Object, Input2, INKEY, INKEY> blueprint() { 
						return 
							startsWith(actionFactory, actionFactory.<Input2, Input1, INKEY, String>newRosettaDataJoin("__synthetic1.rosetta#//@elements.0/@nodes/@node", "joinInput2", null, input2 -> MapperS.of(input2).<String>map("getKeyVal", _input2 -> _input2.getKeyVal()),
									input1 -> MapperS.of(input1).<String>mapC("getForeign", _input1 -> _input1.getForeign()),
									Input2.class, Input1.class))
							.toBlueprint(getURI(), getName());
					}
				}
			'''
			blueprint.compileToClasses
			assertEquals(expected, blueprintJava)
		} finally {
		}
	}

	@Test
	def void selfJoin() {
		val blueprint = '''
			reporting rule SimpleBlueprint 
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
				(
					LoaderBlueprint,
					LoaderBlueprint
				) then
				join key Input->keyVal foreignKey Input->foreign
			
			reporting rule LoaderBlueprint
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
				extract Top->input
			
			type Input:
				foreign string (1..*)
				keyVal string (1..1)
			
			type Top:
				input Input (1..1)
			
		'''.generateCode
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.SimpleBlueprintRule")
		 //writeOutClasses(blueprint, "selfJoin");
		assertThat(blueprintJava, CoreMatchers.notNullValue())
		blueprint.compileToClasses
	}
	
	@Test
	def void selfJoinBasic() {
		val blueprint = '''
			type MyType:
				singleString string (1..1)
				singleInt int (1..1)
			
				multiString string (0..*)
				multiInt int (0..*)
			
			type MyType2:
					multiInt int (0..*)
				
			reporting rule Rule1
				join key MyType->singleInt foreignKey MyType->multiInt'''
		.generateCode
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.Rule1Rule")
		//writeOutClasses(blueprint, "selfJoinBasic");
		assertThat(blueprintJava, CoreMatchers.notNullValue())
		blueprint.compileToClasses
	}
	
	@Test
	def void joinKeyMismatch() {
		val model = '''
			type MyType:
				singleString string (1..1)
				singleInt int (1..1)
			
				multiString string (0..*)
				multiInt int (0..*)
				
			reporting rule Rule1
				join key MyType->multiInt foreignKey MyType->singleString'''
		.parseRosetta
		model.assertError(BLUEPRINT_DATA_JOIN, RosettaIssueCodes.TYPE_ERROR,
			"Type of Key (int) and ForeignKey (string) do not match")
		model.assertError(BLUEPRINT_DATA_JOIN, null,
			"Key expression must have single cardinality")
	}

	@Test
	@Disabled
	def void source() {
		val blueprint = '''
			blueprint SimpleBlueprint 
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
			{
				bpsource asource <string, string>
			}
		'''.generateCode
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.SimpleBlueprint")
		// writeOutClasses(blueprint, "source");
		try {
			assertThat(blueprintJava, CoreMatchers.notNullValue())
			val expected = '''
				package com.rosetta.test.model.blueprint;
				
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.regnosys.rosetta.blueprints.runner.nodes.SourceNode;
				import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
				
				public abstract class SimpleBlueprint implements Blueprint<Void, String, String, String> {
					@Override
					public String getName() {
						return "SimpleBlueprint"; 
					}
					
					@Override
					public String getURI() {
						return "__synthetic1.rosetta#SimpleBlueprint";
					}
					
					
					@Override
					public BlueprintInstance<Void, String, String, String> blueprint() { 
						return 
							startsWith(getAsource())
							.toBlueprint(getURI(), getName());
					}
					
					protected abstract SourceNode<String, String> getAsource();
				}
			'''
			blueprint.compileToClasses
			assertEquals(expected, blueprintJava)
		} finally {
		}
	}
	
	@Test
	def void functionCall() {
		val blueprint = ''' 
			type Foo:
				bar Bar (1..1)
			
			type Bar:
				val number (1..1)
			
			reporting rule Rule1
				extract MyFunc(Foo->bar->val)
			
			func MyFunc:
				inputs: 
					foo number (0..1)
				output: 
					bar number (1..1)
			assign-output bar:
				foo +1
				
			'''.parseRosettaWithNoErrors
			.generateCode
			//blueprint.writeClasses("functionCall")
			blueprint.compileToClasses
	}
	
	@Test
	def void expressionBadTypes() {
		 ''' 
			type Foo:
				bar Bar (1..1)
			
			type Bar:
				val number (1..1)
			
			reporting rule Rule1
				extract Foo->bar->val + Bar->val
			'''.parseRosetta
			.assertError(BLUEPRINT_NODE_EXP, RosettaIssueCodes.TYPE_ERROR,
			"Input types must be the same but were Foo and Bar")
			
	}
	
	@Test
	def void functionCallBadTypes() {
		 ''' 
			type Foo:
				bar Bar (1..1)
			
			type Bar:
				val number (1..1)
			
			reporting rule Rule1
				extract MyFunc(Foo->bar->val, Bar->val)
			
			func MyFunc:
				inputs: 
					a number (0..1)
					b number (0..1)
				output: 
					r number (1..1)
			assign-output r:
				a +b
				
			'''.parseRosetta
			.assertError(BLUEPRINT_NODE_EXP, RosettaIssueCodes.TYPE_ERROR,
			"Input types must be the same but were [Foo, Bar]")
	}
	
	@Test
	def void shouldUseNS() {
		val code = #['''
			namespace "ns1"
			
			
			type TestObject: <"">
				fieldOne string (0..1)
			
			reporting rule Rule1
				extract TestObject -> fieldOne	
			
		''','''
			namespace "ns12"
			
			import ns1.*
			
			reporting rule Rule2
				Rule1
		'''
		].generateCode
		//code.writeClasses("shouldUseNS")
		code.compileToClasses
		
	}
	
	@Test
	def void callRuleWithAs() {
		val code = '''
			
			
			type TestObject: <"">
				fieldOne string (0..1)
			
			reporting rule Rule1
				extract TestObject -> fieldOne
							
			reporting rule Rule2
				Rule1 as "BLAH"
			
		'''
		.generateCode
		//code.writeClasses("callRuleWithAs")
		code.compileToClasses
		
	}
	
	@Test
	def void brokenRuleTypes() {
		'''
			type Foo: <"">
				bar Bar (0..1)
			
			type Bar:
				str string (1..1)
			
			reporting rule Rule1
				extract Foo->bar then
				Rule2
							
			reporting rule Rule2
				extract Foo->bar->str
			
		'''
		.parseRosetta
			.assertError(BLUEPRINT_REF, RosettaIssueCodes.TYPE_ERROR, 168, 5,
			"Input type of Foo is not assignable from type Bar of previous node")
		
	}

	@Test
	@Disabled
	def void calculationUnifiedType() {
		val blueprint = '''
			calculation Calc {
				outputVal int : arg1 + arg2
				outputDt number : arg3 + arg4
			}
			
			arguments Calc CalcArgs{
				arg1 int : is Input->val1
				arg2 int : is Input->val2
				arg3 number : is Input->val3
				arg4 number : is Input->val4
			}			
						
			type Input:
				val1 int (1..1)
				val2 int (1..1)
				val3 number (1..1)
				val4 number (1..1)
			
			
			blueprint Blueprint1
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
			{
				calculate calc Calc args CalcArgs
			}
		'''.generateCode
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.Blueprint1")
		// writeOutClasses(blueprint, "calculationUnifiedType");
		try {
			assertThat(blueprintJava, CoreMatchers.notNullValue())
			blueprint.compileToClasses
			val expected = '''
				package com.rosetta.test.model.blueprint;
				
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.rosetta.model.lib.functions.CalculationFunction;
				import com.rosetta.model.lib.functions.CalculationFunction.CalculationArgFunctions;
				import com.rosetta.model.lib.functions.MapperS;
				import com.rosetta.model.lib.functions.MappingGroup;
				import com.rosetta.test.model.Input;
				import java.math.BigDecimal;
				import java.util.Collection;
				import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
				import static com.rosetta.model.lib.functions.MapperMaths.*;
				import static com.rosetta.model.lib.validation.ValidatorHelper.*;
				
				public abstract class Blueprint1<INKEY extends Comparable<INKEY>> implements Blueprint<Input, Number, INKEY, INKEY> {
					@Override
					public String getName() {
						return "Blueprint1"; 
					}
					
					@Override
					public String getURI() {
						return "__synthetic1.rosetta#com.rosetta.test.model.Blueprint1";
					}
					
					
					@Override
					public BlueprintInstance<Input, Number, INKEY, INKEY> blueprint() { 
						return 
							startsWith(BlueprintBuilder.<Input, Number, INKEY>doCalcMappings("__synthetic1.rosetta#//@elements.3/@nodes/@node", calcMappingsCalc()))
							.toBlueprint(getURI(), getName());
					}
					
					private Collection<MappingGroup<Input, ? extends Number>> calcMappingsCalc() {
						return Blueprint.of(
							new MappingGroup<Input, Integer>("outputVal", "__synthetic1.rosetta#Calc.outputVal", Blueprint.of(new CalculationFunction<>(args->add(args.get("arg1"), args.get("arg2")), calcArgsCalc()))),
							
							new MappingGroup<Input, BigDecimal>("outputDt", "__synthetic1.rosetta#Calc.outputDt", Blueprint.of(new CalculationFunction<>(args->add(args.get("arg3"), args.get("arg4")), calcArgsCalc())))
						);
					}
					
					private CalculationArgFunctions<Input> calcArgsCalc() {
						CalculationArgFunctions<Input> result = new CalculationArgFunctions<>();
						result.put("arg1", input->MapperS.of(input).map("getVal1", Input::getVal1));
						result.put("arg2", input->MapperS.of(input).map("getVal2", Input::getVal2));
						result.put("arg3", input->MapperS.of(input).map("getVal3", Input::getVal3));
						result.put("arg4", input->MapperS.of(input).map("getVal4", Input::getVal4));
						return result;
					}
				}
			'''
			assertEquals(expected, blueprintJava)

		} finally {
		}
	}

	@Test
	@Disabled
	def void calculation() {
		val blueprint = '''
			calculation Calc {
				outputVal int : arg1 + arg2
				outputDt string : arg3 + arg4
			}
			
			arguments Calc CalcArgs{
				arg1 int : is Input->val1
				arg2 int : is Input->val2
				arg3 date : is Input->val3
				arg4 time : is Input->val4
			}			
						
			type Input:
				val1 int (1..1)
				val2 int (1..1)
				val3 date (1..1)
				val4 time (1..1)
			
			
			reporting rule Blueprint1
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
				calculate calc Calc args CalcArgs
		'''.generateCode
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.Blueprint1")
		// writeOutClasses(blueprint, "calculation");
		try {
			assertThat(blueprintJava, CoreMatchers.notNullValue())
			blueprint.compileToClasses
			val expected = '''
				package com.rosetta.test.model.blueprint;
				
				import com.regnosys.rosetta.blueprints.Blueprint;
				import com.regnosys.rosetta.blueprints.BlueprintBuilder;
				import com.regnosys.rosetta.blueprints.BlueprintInstance;
				import com.rosetta.model.lib.functions.CalculationFunction;
				import com.rosetta.model.lib.functions.CalculationFunction.CalculationArgFunctions;
				import com.rosetta.test.model.Input;
				import java.time.LocalDate;
				import java.time.LocalTime;
				import java.util.Collection;
				import static com.regnosys.rosetta.blueprints.BlueprintBuilder.*;
				import static com.rosetta.model.lib.functions.MapperMaths.*;
				
				public abstract class Blueprint1<INKEY extends Comparable<INKEY>> implements Blueprint<Input, Object, INKEY, INKEY> {
					@Override
					public String getName() {
						return "Blueprint1"; 
					}
					
					@Override
					public String getURI() {
						return "__synthetic1.rosetta#com.rosetta.test.model.Blueprint1";
					}
					
					
					@Override
					public BlueprintInstance<Input, Object, INKEY, INKEY> blueprint() { 
						return 
							startsWith(BlueprintBuilder.<Input, Object, INKEY>doCalcMappings("__synthetic1.rosetta#//@elements.3/@nodes/@node", calcMappingsCalc()))
							.toBlueprint(getURI(), getName());
					}
					
					private Collection<MappingGroup<Input, ? extends Object>> calcMappingsCalc() {
						return Blueprint.of(
							new MappingGroup<Input, Integer>("outputVal", "__synthetic1.rosetta#Calc.outputVal", Blueprint.of(new CalculationFunction<>(args->add(args.get("arg1"), args.get("arg2")), calcArgsCalc()))),
							
							new MappingGroup<Input, String>("outputDt", "__synthetic1.rosetta#Calc.outputDt", Blueprint.of(new CalculationFunction<>(args->add(args.get("arg3"), args.get("arg4")), calcArgsCalc())))
						);
					}
					
					private CalculationArgFunctions<Input> calcArgsCalc() {
						CalculationArgFunctions<Input> result = new CalculationArgFunctions<>();
						result.put("arg1", input->MapperS.of(input).map("getVal1", Input::getVal1));
						result.put("arg2", input->MapperS.of(input).map("getVal2", Input::getVal2));
						result.put("arg3", input->MapperS.of(input).map("getVal3", Input::getVal3));
						result.put("arg4", input->MapperS.of(input).map("getVal4", Input::getVal4));
						return result;
					}
				}
			'''
			assertEquals(expected, blueprintJava)

		} finally {
		}
	}

	@Disabled
	@Test
	def void calculationInline() {
		val blueprint = '''
			
			blueprint Blueprint1
				[regulatoryReference ESMA MiFIR RTS_22 annex "" provision ""]
			{
				calculate calc quantity string : "1" args a1 int : is Input->val1
			}
			
			type Input:
				val1 int (1..1)
			
		'''.generateCode
		val blueprintJava = blueprint.get("com.rosetta.test.model.blueprint.Blueprint1")
		// writeOutClasses(blueprint, "calculationInline");
		try {
			assertThat(blueprintJava, CoreMatchers.notNullValue())
			blueprint.compileToClasses

		} finally {
		}
	}

	@Deprecated
	static def writeOutClasses(HashMap<String, String> map, String testName) {
		for (entry : map.entrySet) {
			val name = entry.key;
			val pathName = name.replace('.', File.separator)
			val path = Paths.get("target/" + testName + "/java", pathName + ".java")
			Files.createDirectories(path.parent);
			Files.write(path, entry.value.bytes)

		}
	}
}
