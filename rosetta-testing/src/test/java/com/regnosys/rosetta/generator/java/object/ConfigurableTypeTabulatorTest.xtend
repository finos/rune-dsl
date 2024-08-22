package com.regnosys.rosetta.generator.java.object

import org.junit.jupiter.api.Test
import com.regnosys.rosetta.config.file.RosettaConfigurationFileProvider

import static extension com.regnosys.rosetta.tests.util.CustomConfigTestHelper.*
import java.net.URL
import static org.junit.jupiter.api.Assertions.*
import static org.hamcrest.MatcherAssert.*
import org.hamcrest.CoreMatchers
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.eclipse.xtext.testing.InjectWith
import org.junit.jupiter.api.^extension.ExtendWith
import javax.inject.Inject
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.rosetta.util.types.generated.GeneratedJavaClassService
import com.regnosys.rosetta.generator.java.reports.TabulatorTestUtil

@InjectWith(RosettaInjectorProvider)
@ExtendWith(InjectionExtension)
class ConfigurableTypeTabulatorTest {
		
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
			import model1.Foo;
			
			
			@ImplementedBy(FooTypeTabulator.Impl.class)
			public interface FooTypeTabulator extends Tabulator<Foo> {
				public class Impl implements FooTypeTabulator {
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
	
	private static class Model1FileConfigProvider extends RosettaConfigurationFileProvider {
		override URL get() {
			Thread.currentThread.contextClassLoader.getResource("rosetta-tabulator-type-config-model1.yml")
		}
	}
}