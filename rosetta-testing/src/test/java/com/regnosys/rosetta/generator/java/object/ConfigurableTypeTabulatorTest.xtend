package com.regnosys.rosetta.generator.java.object

import com.regnosys.rosetta.config.file.RosettaConfigurationFileProvider
import java.net.URL
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.Test

import static org.hamcrest.MatcherAssert.*
import static org.junit.jupiter.api.Assertions.*

import static extension com.regnosys.rosetta.tests.util.CustomConfigTestHelper.*

class ConfigurableTypeTabulatorTest {

	@Test
	def void shouldGenerateTabulatorsForTypeListedInConfig_DefaultNamespace() {
		// default testing namespace is com.rosetta.test.model
		val model = '''
			type Foo:
			   bar string (1..1)
		'''

		val code = model.generateCodeForModel(DefaultNamespaceFileConfigProvider)
		val fooTabulatorCode = code.get("com.rosetta.test.model.tabulator.FooTypeTabulator")
		assertThat(fooTabulatorCode, CoreMatchers.notNullValue())
		var expected = '''
			package com.rosetta.test.model.tabulator;
			
			import com.google.inject.ImplementedBy;
			import com.rosetta.model.lib.reports.Tabulator;
			import com.rosetta.model.lib.reports.Tabulator.Field;
			import com.rosetta.model.lib.reports.Tabulator.FieldImpl;
			import com.rosetta.model.lib.reports.Tabulator.FieldValue;
			import com.rosetta.model.lib.reports.Tabulator.FieldValueImpl;
			import com.rosetta.test.model.Foo;
			import java.util.Arrays;
			import java.util.List;
			import java.util.Optional;
			import javax.inject.Singleton;
			
			
			@ImplementedBy(FooTypeTabulator.Impl.class)
			public interface FooTypeTabulator extends Tabulator<Foo> {
				@Singleton
				class Impl implements FooTypeTabulator {
					private final Field barField;
			
					public Impl() {
						this.barField = new FieldImpl(
							"bar",
							false,
							Optional.empty(),
							Optional.empty(),
							Arrays.asList()
						);
					}
			
					@Override
					public List<FieldValue> tabulate(Foo input) {
						FieldValue bar = new FieldValueImpl(barField, Optional.ofNullable(input.getBar()));
						return Arrays.asList(
							bar
						);
					}
				}
			}
		'''
		assertEquals(expected, fooTabulatorCode)
	}
		
	@Test
	def void shouldGenerateTabulatorsForTypeListedInConfig() {
		val model1 = '''
			namespace model1
			
				type Foo:
				   bar Bar (1..1)
				
				type Bar:
				   baz string (1..1)
		'''

		val model1Code = model1.generateCodeForModel(Model1FileConfigProvider)
		val fooTabulatorCode = model1Code.get("model1.tabulator.FooTypeTabulator")
		assertThat(fooTabulatorCode, CoreMatchers.notNullValue())
		var expected = '''
			package model1.tabulator;
			
			import com.google.inject.ImplementedBy;
			import com.rosetta.model.lib.reports.Tabulator;
			import com.rosetta.model.lib.reports.Tabulator.Field;
			import com.rosetta.model.lib.reports.Tabulator.FieldImpl;
			import com.rosetta.model.lib.reports.Tabulator.FieldValue;
			import com.rosetta.model.lib.reports.Tabulator.NestedFieldValueImpl;
			import java.util.Arrays;
			import java.util.List;
			import java.util.Optional;
			import javax.inject.Inject;
			import javax.inject.Singleton;
			import model1.Foo;
			
			
			@ImplementedBy(FooTypeTabulator.Impl.class)
			public interface FooTypeTabulator extends Tabulator<Foo> {
				@Singleton
				class Impl implements FooTypeTabulator {
					private final Field barField;
			
					private final BarTypeTabulator barTypeTabulator;
			
					@Inject
					public Impl(BarTypeTabulator barTypeTabulator) {
						this.barTypeTabulator = barTypeTabulator;
						this.barField = new FieldImpl(
							"bar",
							false,
							Optional.empty(),
							Optional.empty(),
							Arrays.asList()
						);
					}
			
					@Override
					public List<FieldValue> tabulate(Foo input) {
						FieldValue bar = Optional.ofNullable(input.getBar())
							.map(x -> new NestedFieldValueImpl(barField, Optional.of(barTypeTabulator.tabulate(x))))
							.orElse(new NestedFieldValueImpl(barField, Optional.empty()));
						return Arrays.asList(
							bar
						);
					}
				}
			}
		'''
		assertEquals(expected, fooTabulatorCode)
	}
	
	@Test
	def void shouldGenerateTabulatorsForTypeEngineSpecificationConfig() {
		val model2 = '''
			namespace model2
			
			type Root:
				engineSpecification EngineSpecification (1..1)
			
			type EngineSpecification:
				fuel string (1..*)
				[metadata scheme]
		'''

		val model2Code = model2.generateCodeForModel(Model2FileConfigProvider)
		val engineSpecificationTabulatorCode = model2Code.get("model2.tabulator.EngineSpecificationTypeTabulator")
		assertThat(engineSpecificationTabulatorCode, CoreMatchers.notNullValue())
		var expected = '''
			package model2.tabulator;
			
			import com.google.inject.ImplementedBy;
			import com.rosetta.model.lib.reports.Tabulator;
			import com.rosetta.model.lib.reports.Tabulator.Field;
			import com.rosetta.model.lib.reports.Tabulator.FieldImpl;
			import com.rosetta.model.lib.reports.Tabulator.FieldValue;
			import com.rosetta.model.lib.reports.Tabulator.FieldValueImpl;
			import java.util.Arrays;
			import java.util.List;
			import java.util.Objects;
			import java.util.Optional;
			import java.util.stream.Collectors;
			import javax.inject.Singleton;
			import model2.EngineSpecification;
			
			
			@ImplementedBy(EngineSpecificationTypeTabulator.Impl.class)
			public interface EngineSpecificationTypeTabulator extends Tabulator<EngineSpecification> {
				@Singleton
				class Impl implements EngineSpecificationTypeTabulator {
					private final Field fuelField;
			
					public Impl() {
						this.fuelField = new FieldImpl(
							"fuel",
							true,
							Optional.empty(),
							Optional.empty(),
							Arrays.asList()
						);
					}
			
					@Override
					public List<FieldValue> tabulate(EngineSpecification input) {
						FieldValue fuel = new FieldValueImpl(fuelField, Optional.ofNullable(input.getFuel())
							.map(x -> x.stream()
								.map(_x -> _x.getValue())
								.filter(Objects::nonNull)
								.collect(Collectors.toList())));
						return Arrays.asList(
							fuel
						);
					}
				}
			}
		'''
		assertEquals(expected, engineSpecificationTabulatorCode)
	}
	
	private static class Model1FileConfigProvider extends RosettaConfigurationFileProvider {
		override URL get() {
			Thread.currentThread.contextClassLoader.getResource("rosetta-tabulator-type-config-model1.yml")
		}
	}
	
	private static class Model2FileConfigProvider extends RosettaConfigurationFileProvider {
		override URL get() {
			Thread.currentThread.contextClassLoader.getResource("rosetta-tabulator-type-config-model2.yml")
		}
	}
	
	private static class DefaultNamespaceFileConfigProvider extends RosettaConfigurationFileProvider {
		override URL get() {
			Thread.currentThread.contextClassLoader.getResource("rosetta-tabulator-type-config-default.yml")
		}
	}
}