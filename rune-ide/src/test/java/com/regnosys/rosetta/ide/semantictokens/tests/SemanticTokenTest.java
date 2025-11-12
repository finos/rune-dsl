package com.regnosys.rosetta.ide.semantictokens.tests;

import com.regnosys.rosetta.ide.tests.AbstractRosettaLanguageServerTest;
import org.junit.jupiter.api.Test;

public class SemanticTokenTest extends AbstractRosettaLanguageServerTest {
	@Test
	public void testAttributeTypesAreMarked() {
		testSemanticToken(cfg -> {
			String model = """
				namespace test

				enum Bar:
					V

				type Foo:
					a int (1..1)
					b Bar (1..1)
					c Foo (0..1)
					d date (0..*)
				""";
			cfg.setModel(model);
			cfg.setExpectedSemanticTokenItems("""
				property.singleCardinality: 6:1:1
				typeAlias: 6:3:3
				property.singleCardinality: 7:1:1
				enum: 7:3:3
				property.singleCardinality: 8:1:1
				type: 8:3:3
				property.multiCardinality: 9:1:1
				recordType: 9:3:4
				""");
		});
	}

	@Test
	public void testUndefinedTypesAreNotMarked() {
		testSemanticToken(cfg -> {
			String model = """
				namespace test

				type Foo:
					a int (1..1)
					b Bar (1..1)
					c Foo (0..1)
					d date (0..*)
				""";
			cfg.setModel(model);
			cfg.setAssertNoIssues(false);
			cfg.setExpectedSemanticTokenItems("""
				property.singleCardinality: 3:1:1
				typeAlias: 3:3:3
				property.singleCardinality: 4:1:1
				property.singleCardinality: 5:1:1
				type: 5:3:3
				property.multiCardinality: 6:1:1
				recordType: 6:3:4
				""");
		});
	}

	@Test
	public void testTypedStdFeaturesAreMarked() {
		testSemanticToken(cfg -> {
			String model = """
				namespace test

				metaType foo string

				library function bar(param int) date
				""";
			cfg.setModel(model);
			cfg.setExpectedSemanticTokenItems("""
				basicType: 2:13:6
				typeAlias: 4:27:3
				recordType: 4:32:4
				""");
		});
	}

	@Test
	public void testDocAnnotationsAreMarked() {
		testSemanticToken(cfg -> {
			String model = """
				namespace test

				body Authority CFTC

				corpus Regulation "CFTC 17 CFR Parts 45" Part45

				segment appendix
				segment dataElement
				segment field

				reporting rule Cleared from number:
					[regulatoryReference CFTC Part45 appendix "1" dataElement "1" field "Cleared"
						provision "Indicator of whether the transaction has been cleared, or is intended to be cleared, by a central counterparty."]
					"Y"
				""";
			cfg.setModel(model);
			cfg.setExpectedSemanticTokenItems("""
				rule.singleCardinality: 10:15:7
				basicType: 10:28:6
				documentCorpus: 11:27:6
				documentSegment: 11:34:8
				documentSegment: 11:47:11
				documentSegment: 11:63:5
				""");
		});
	}

	@Test
	public void testPartialDocAnnotationsDoNotCrashSemanticTokens() {
		testSemanticToken(cfg -> {
			String model = """
				namespace test

				body Authority CFTC

				corpus Regulation "CFTC 17 CFR Parts 45" Part45

				segment appendix
				segment dataElement
				segment field

				type A:
					a int (1..1)

				reporting rule Cleared from number:
					[regulatoryReference CFTC Pa

				type B:
					a string (1..1)
				""";
			cfg.setModel(model);
			cfg.setAssertNoIssues(false);
			cfg.setExpectedSemanticTokenItems("""
				property.singleCardinality: 11:1:1
				typeAlias: 11:3:3
				rule.singleCardinality: 13:15:7
				basicType: 13:28:6
				property.singleCardinality: 17:1:1
				basicType: 17:3:6
				""");
		});
	}

	@Test
	public void testMetadataAnnotationIsMarked() {
		testSemanticToken(cfg -> {
			String model = """
				namespace test

				metaType scheme string

				type Foo:
					bar string (1..1)
						[metadata scheme]
				""";
			cfg.setModel(model);
			cfg.setExpectedSemanticTokenItems("""
				basicType: 2:16:6
				property.singleCardinality: 5:1:3
				basicType: 5:5:6
				metaMember: 6:12:6
				""");
		});
	}

	@Test
	public void testPartialMetadataAnnotationDoesNotCrashSemanticTokens() {
		testSemanticToken(cfg -> {
			String model = """
				namespace test

				metaType scheme string

				type Foo:
					bar string (1..1)
						[metadata ]
				""";
			cfg.setModel(model);
			cfg.setAssertNoIssues(false);
			cfg.setExpectedSemanticTokenItems("""
				basicType: 2:16:6
				property.singleCardinality: 5:1:3
				basicType: 5:5:6
				""");
		});
	}

	@Test
	public void testExpressionsAreMarked() {
		testSemanticToken(cfg -> {
			String model = """
				namespace test

				metaType scheme string

				enum A:
					V

				type Foo:
					bar string (1..1)
						[metadata scheme]
					a A (1..1)

				func Bar:
					inputs:
						foo Foo (1..1)
					output:
						result string (0..1)
					set result:
						if foo -> a = A -> V
						then foo -> bar -> scheme
				""";
			cfg.setModel(model);
			cfg.setExpectedSemanticTokenItems("""
				basicType: 2:16:6
				property.singleCardinality: 8:1:3
				basicType: 8:5:6
				metaMember: 9:12:6
				property.singleCardinality: 10:1:1
				enum: 10:3:1
				function.singleCardinality: 12:5:3
				parameter.singleCardinality: 14:2:3
				type: 14:6:3
				output.singleCardinality: 16:2:6
				basicType: 16:9:6
				output.singleCardinality: 17:5:6
				parameter.singleCardinality: 18:5:3
				property.singleCardinality: 18:12:1
				enumMember: 18:21:1
				parameter.singleCardinality: 19:7:3
				property.singleCardinality: 19:14:3
				metaMember: 19:21:6
				""");
		});
	}

	@Test
	public void testGeneratedImplicitVariableDoesNotCauseSemanticToken() {
		testSemanticToken(cfg -> {
			String model = """
				namespace test

				reporting rule Test from string:
					/* item */ extract (["a", "b"])
					then /* item */ only-element
				""";
			cfg.setModel(model);
			cfg.setExpectedSemanticTokenItems("""
				rule.singleCardinality: 2:15:4
				basicType: 2:25:6
				""");
		});
	}
}
