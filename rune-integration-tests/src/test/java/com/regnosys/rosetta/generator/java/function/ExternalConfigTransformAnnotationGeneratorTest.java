package com.regnosys.rosetta.generator.java.function;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.tests.testmodel.JavaTestModel;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;
import com.regnosys.rosetta.tests.util.RosettaCustomConfigInjectorProvider;

/**
 * Verifies that a {@code configPath} is generated into the transform annotation only when the
 * referenced schema is marked {@code [externalConfig]}. Runs against {@code rune-custom-config.yml},
 * which defines a {@code serializationConfig} for id {@code externalXmlSchema}.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaCustomConfigInjectorProvider.class)
public class ExternalConfigTransformAnnotationGeneratorTest {

	@Inject
	private RosettaTestModelService modelService;

	@Test
	void generatesConfigPathOnlyForExternalConfigSchema() {
		JavaTestModel model = modelService.toJavaTestModel("""
				namespace test

				schema externalXmlSchema XML
					[externalConfig]

				type Foo:
					a string (1..1)

				func IngestExternal:
					[ingest externalXmlSchema]
					inputs:
						input string (1..1)
					output:
						result Foo (1..1)
					set result: Foo { a: input }
				""");

		assertTrue(model.getFunctionJavaSource("IngestExternal").contains(
						"@Ingest(id = \"externalXmlSchema\", format = SerializationFormat.XML, configPath = \"xml-config/external-xml-schema-config.json\")"),
				"an [externalConfig] schema should emit @Ingest with id, format and configPath");
	}

	@Test
	void omitsConfigPathWhenSchemaIsNotExternalConfig() {
		// 'externalXmlSchema' is the configured id, but a differently-named schema without
		// [externalConfig] must not pull in any configPath, even when a configuration is present.
		JavaTestModel model = modelService.toJavaTestModel("""
				namespace test

				schema plainXmlSchema XML

				type Foo:
					a string (1..1)

				func IngestPlain:
					[ingest plainXmlSchema]
					inputs:
						input string (1..1)
					output:
						result Foo (1..1)
					set result: Foo { a: input }
				""");

		String source = model.getFunctionJavaSource("IngestPlain");
		assertTrue(source.contains("@Ingest(id = \"plainXmlSchema\", format = SerializationFormat.XML)"),
				"a schema without [externalConfig] should emit @Ingest with id and format only");
		assertFalse(source.contains("configPath"),
				"a schema without [externalConfig] must not emit a configPath");
	}
}
