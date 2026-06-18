package com.regnosys.rosetta.generator.java.function;

import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.testmodel.JavaTestModel;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class TransformAnnotationGeneratorTest {

	@Inject
	private RosettaTestModelService modelService;

	@Test
	void generatesTransformAnnotationsOnFunctionClasses() {
		JavaTestModel model = modelService.toJavaTestModel("""
				namespace test

				schema myXmlSchema XML

				type Foo:
					a string (1..1)

				func IngestBareFormat:
					[ingest XML]
					inputs:
						input string (1..1)
					output:
						result Foo (1..1)
					set result: Foo { a: input }

				func IngestSchema:
					[ingest myXmlSchema]
					inputs:
						input string (1..1)
					output:
						result Foo (1..1)
					set result: Foo { a: input }

				func Project:
					[projection JSON]
					inputs:
						input Foo (1..1)
					output:
						result string (1..1)
					set result: input -> a

				func Enricher:
					[enrich]
					inputs:
						input Foo (1..1)
					output:
						result Foo (1..1)
					set result: input
				""");

		assertTrue(model.getFunctionJavaSource("IngestBareFormat").contains("@Ingest(format = SerializationFormat.XML)"),
				"bare-format ingest should emit @Ingest with just the format");
		assertTrue(model.getFunctionJavaSource("IngestSchema").contains("@Ingest(id = \"myXmlSchema\", format = SerializationFormat.XML)"),
				"schema-based ingest should emit @Ingest with id and the schema's format");
		assertTrue(model.getFunctionJavaSource("Project").contains("@Projection(format = SerializationFormat.JSON)"),
				"projection should emit @Projection with the format");
		assertTrue(model.getFunctionJavaSource("Enricher").contains("@Enrich"),
				"enrich should emit @Enrich");
	}
}
