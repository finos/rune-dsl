package com.regnosys.rosetta.validation;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.tests.util.RosettaCustomConfigInjectorProvider;

/**
 * Validation of the {@code schema} construct and its annotations. Runs against
 * {@code rune-custom-config.yml}, which configures an external serialization config for the id
 * {@code externalXmlSchema} (and nothing for the other schema names used here).
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaCustomConfigInjectorProvider.class)
public class SchemaValidationTest extends AbstractValidatorTest {

	@Test
	void testSchemaReferencingBuiltInFormatsParses() {
		assertNoIssues("""
				namespace test
				version "1"

				schema myXmlSchema XML
				schema myJson JSON
				schema runeJson RUNE_JSON
				schema csvSchema CSV
				""");
	}

	@Test
	void testDuplicateSchemaNamesAreNotAllowed() {
		assertIssues("""
				namespace test
				version "1"

				schema myXmlSchema XML
				schema myXmlSchema JSON
				""", """
				ERROR (null) 'Duplicate schema 'myXmlSchema' in namespace 'test'' at 4:8, length 11, on Schema
				ERROR (null) 'Duplicate schema 'myXmlSchema' in namespace 'test'' at 5:8, length 11, on Schema
				""");
	}

	@Test
	void testDeprecatedSchemaIsAllowed() {
		// [deprecated] is a generic annotation and parses on a schema.
		assertNoIssues("""
				namespace test
				version "1"

				schema myXmlSchema XML
					[deprecated]
				""");
	}

	@Test
	void testExternalConfigWithoutConfigurationIsAnError() {
		// 'myXmlSchema' has no external serialization configuration configured; the error points at the annotation.
		assertIssues("""
				namespace test
				version "1"

				schema myXmlSchema XML
					[externalConfig]
				""", """
				ERROR (null) 'Schema 'myXmlSchema' is marked [externalConfig] but no external serialization configuration is configured for it' at 5:3, length 14, on AnnotationRef
				""");
	}

	@Test
	void testExternalConfigWithMatchingConfigurationIsValid() {
		// 'externalXmlSchema' has an external serialization configuration configured.
		assertNoIssues("""
				namespace test
				version "1"

				schema externalXmlSchema XML
					[externalConfig]
				""");
	}

	@Test
	void testConfigurationWithoutExternalConfigWarns() {
		// 'externalXmlSchema' is configured externally, but the schema does not declare [externalConfig].
		assertIssues("""
				namespace test
				version "1"

				schema externalXmlSchema XML
				""", """
				WARNING (null) 'An external serialization configuration is configured for schema 'externalXmlSchema', but the schema is not marked [externalConfig], so it will be ignored' at 4:8, length 17, on Schema
				""");
	}

	@Test
	void testCsvLabelledSchemaFormatWarns() {
		assertIssues("""
				namespace test
				version "1"

				schema labelledCsvSchema CSV_LABELLED
				""", """
				WARNING (null) 'CSV_LABELLED is deprecated. Use CSV instead, with "headerStyle": "LABEL" in the CSV serialization configuration. The CSV format honours the whole configuration and resolves the label provider by the same rules.' at 4:26, length 12, on Schema
				""");
	}

	@Test
	void testCsvLabelledTransformFormatWarns() {
		assertIssues("""
				namespace test
				version "1"

				type Thing:
					attr string (1..1)

				func MyFunc:
					[projection CSV_LABELLED]
					inputs:
						inp string (1..1)
					output:
						result Thing (1..1)

					set result: Thing { attr: inp }
				""", """
				WARNING (null) 'CSV_LABELLED is deprecated. Use CSV instead, with "headerStyle": "LABEL" in the CSV serialization configuration. The CSV format honours the whole configuration and resolves the label provider by the same rules.' at 8:14, length 12, on TransformAnnotation
				""");
	}

	@Test
	void testAFunctionUsingACsvLabelledSchemaWarnsOnlyAtTheSchema() {
		// The schema declaration is the one place the format is written, so it carries the single warning.
		assertIssues("""
				namespace test
				version "1"

				schema labelledCsvSchema CSV_LABELLED

				type Thing:
					attr string (1..1)

				func MyFunc:
					[projection labelledCsvSchema]
					inputs:
						inp string (1..1)
					output:
						result Thing (1..1)

					set result: Thing { attr: inp }
				""", """
				WARNING (null) 'CSV_LABELLED is deprecated. Use CSV instead, with "headerStyle": "LABEL" in the CSV serialization configuration. The CSV format honours the whole configuration and resolves the label provider by the same rules.' at 4:26, length 12, on Schema
				""");
	}

	@Test
	void testCsvSchemaFormatDoesNotWarn() {
		assertNoIssues("""
				namespace test
				version "1"

				schema plainCsvSchema CSV
				""");
	}

	@Test
	void testExternalConfigOnNonSchemaIsAnError() {
		assertIssues("""
				namespace test
				version "1"

				type Foo:
					[externalConfig]
					a string (1..1)
				""", """
				ERROR (null) '[externalConfig] can only be applied to a schema' at 5:3, length 14, on AnnotationRef
				""");
	}
}
