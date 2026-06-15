package com.regnosys.rosetta.validation;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class SchemaParsingTest extends AbstractValidatorTest {

	@Test
	void testSchemaReferencingBuiltInFormatsParses() {
		assertNoIssues("""
				namespace test
				version "1"

				schema fixml XML
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

				schema fixml XML
				schema fixml JSON
				""", """
				ERROR (null) 'Duplicate schema 'fixml' in namespace 'test'' at 4:8, length 5, on Schema
				ERROR (null) 'Duplicate schema 'fixml' in namespace 'test'' at 5:8, length 5, on Schema
				""");
	}
}
