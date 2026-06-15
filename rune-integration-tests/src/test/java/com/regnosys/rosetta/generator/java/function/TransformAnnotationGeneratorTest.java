package com.regnosys.rosetta.generator.java.function;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class TransformAnnotationGeneratorTest {

	@Inject
	private CodeGeneratorTestHelper generatorTestHelper;

	@Test
	void generatesTransformAnnotationsOnFunctionClasses() {
		Map<String, String> code = generatorTestHelper.generateCode("""
				namespace test

				schema fixml XML

				type Foo:
					a string (1..1)

				func IngestBareFormat:
					[ingest XML]
					inputs:
						input string (1..1)
					output:
						result Foo (1..1)

				func IngestSchema:
					[ingest fixml]
					inputs:
						input string (1..1)
					output:
						result Foo (1..1)

				func Project:
					[projection JSON]
					inputs:
						input Foo (1..1)
					output:
						result string (1..1)

				func Enricher:
					[enrich]
					inputs:
						input Foo (1..1)
					output:
						result Foo (1..1)
				""");

		String all = String.join("\n", code.values());

		assertTrue(all.contains("@Ingest(format = SerializationFormat.XML)"),
				"bare-format ingest should emit @Ingest with just the format");
		assertTrue(all.contains("@Ingest(id = \"fixml\", format = SerializationFormat.XML)"),
				"schema-based ingest should emit @Ingest with id and the schema's format");
		assertTrue(all.contains("@Projection(format = SerializationFormat.JSON)"),
				"projection should emit @Projection with the format");
		assertTrue(all.contains("@Enrich"),
				"enrich should emit @Enrich");
	}
}
