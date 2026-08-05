package com.regnosys.rosetta.generator.java.labels;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.xbase.testing.RegisteringFileSystemAccess;
import org.eclipse.xtext.xbase.testing.RegisteringFileSystemAccess.GeneratedFile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.common.io.Resources;
import com.google.inject.Injector;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModel;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper;
import com.rosetta.model.lib.functions.LabelProvider;
import com.rosetta.model.lib.path.RosettaPath;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class LabelProviderGeneratorTest {
	@Inject
	private RosettaTestModelService testModelService;
	@Inject
	private CodeGeneratorTestHelper generatorTestHelper;
	@Inject
	private Injector injector;

	@Inject
	private LabelProviderGenerator labelProviderGenerator;

	private RegisteringFileSystemAccess fsa;

	@BeforeEach
	void beforeEach() {
		fsa = new RegisteringFileSystemAccess();
		fsa.setProjectName("test-project");
		fsa.setOutputPath("src-gen/main/java");
	}


	private RosettaTestModel loadModel(String runeSourceCode) throws IOException {
		return testModelService.toTestModel(runeSourceCode);
	}
	private void generateLabelProvider(RosettaTestModel model) {
		labelProviderGenerator.generateClasses(model.getModel(), "test", fsa, CancelIndicator.NullImpl);
	}

	private List<String> getGeneratedFileNames() {
		return fsa.getGeneratedFiles().stream().map(f -> f.getPath()).collect(Collectors.toList());
	}
	/**
	 * Asserts that the set of generated files is exactly {@code expectedGeneratedPaths} - no more, no
	 * fewer. Order does not matter.
	 */
	private void assertGeneratedFiles(String... expectedGeneratedPaths) {
		List<String> actualPaths = getGeneratedFileNames().stream()
				.map(p -> p.replace("/test-project/src-gen/main/java", ""))
				.sorted()
				.collect(Collectors.toList());
		List<String> expectedPaths = Arrays.stream(expectedGeneratedPaths).sorted().collect(Collectors.toList());
		Assertions.assertEquals(
				expectedPaths,
				actualPaths,
				"Expected exactly the generated files " + expectedPaths + ", but got " + actualPaths);
	}
	private GeneratedFile getGeneratedFile(String expectedGeneratedPath) {
		String fullPath = "/test-project/src-gen/main/java" + expectedGeneratedPath;
		List<GeneratedFile> matches = fsa.getGeneratedFiles().stream()
				.filter(f -> f.getPath().equals(fullPath))
				.collect(Collectors.toList());
		Assertions.assertEquals(
				1,
				matches.size(),
				"Expected exactly one generated file at " + expectedGeneratedPath + ", but the generated files were:"
						+ getGeneratedFileNames().stream().collect(Collectors.joining("\n", "\n", "\n")));
		return matches.get(0);
	}
	private LabelProvider getLabelProviderInstance(String expectedGeneratedPath) {
		GeneratedFile file = getGeneratedFile(expectedGeneratedPath);
		Map<String, Class<?>> compiled = generatorTestHelper.compileToClasses(Map.of(file.getJavaClassName(), file.getContents().toString()));
		Class<? extends LabelProvider> clazz = compiled.get(file.getJavaClassName()).asSubclass(LabelProvider.class);
		return injector.getInstance(clazz);
	}
	private void assertGeneratedFileMatches(String expectationFileName, String expectedGeneratedPath) throws IOException {
		GeneratedFile file = getGeneratedFile(expectedGeneratedPath);
		// This file comes straight from the fsa, so normalise the platform line
		// separator emitted by the Xtend templates
		String actualSource = file.getContents().toString().replace("\r\n", "\n");
		String expectedSource = Resources.toString(getClass().getResource("/label-annotations/" + expectationFileName), StandardCharsets.UTF_8);
		Assertions.assertEquals(expectedSource, actualSource);
	}
	private void assertLabels(String expectedGeneratedPath, String... pathLabelExpectations) {
		LabelProvider provider = getLabelProviderInstance(expectedGeneratedPath);
		Assertions.assertAll(
				Arrays.stream(pathLabelExpectations)
					.map(expectation -> () -> {
						String[] parts = expectation.split(":");
						String rawPath = parts[0];
						RosettaPath path = RosettaPath.valueOf(rawPath);
						String expectedLabel = parts[1].equals("null") ? null : parts[1];
						String actualLabel = provider.getLabel(path);
						Assertions.assertEquals(expectedLabel, actualLabel, "Expected label \"" + expectedLabel + "\", but got \"" + actualLabel + "\" for path `" + rawPath + "`");
					})
		);
	}


	@Test
	void testFunctionWithoutAnnotationDoesNotGenerateLabelProvider() throws IOException {
		RosettaTestModel model = loadModel("""
				namespace test

				type Foo:
					attr string (1..1)
						[label "My attribute"]

				annotation myAnn:

				func MyFunc:
				    [codeImplementation]
					[myAnn]
					output:
						foo Foo (1..1)
				""");

		generateLabelProvider(model);

		// No function-rooted provider is generated because MyFunc has no transform annotation. A
		// type-rooted provider for Foo is generated regardless, because Foo carries a direct label -
		// that is the type-per-provider behaviour under test elsewhere, not what this test is pinning.
		assertGeneratedFiles("/test/labels/types/FooLabelProvider.java");
	}

	@Test
	void testFunctionWithIngestAnnotationGeneratesLabelProvider() throws IOException {
		RosettaTestModel model = loadModel("""
				namespace test

				type Foo:
					attr string (1..1)
						[label "My attribute"]
					other int (1..1)

				func MyFunc:
				    [codeImplementation]
					[ingest JSON]
					inputs:
					    inp int (1..1)
					output:
						foo Foo (1..1)
				""");

		generateLabelProvider(model);

		// Both a function-rooted and a type-rooted provider are generated: Foo carries a direct
		// label, so it gets its own provider in `labels.types`, alongside MyFunc's in `labels`.
		assertGeneratedFiles(
				"/test/labels/MyFuncLabelProvider.java",
				"/test/labels/types/FooLabelProvider.java");
		assertGeneratedFileMatches("func-ingest/MyFuncLabelProvider.java", "/test/labels/MyFuncLabelProvider.java");
		assertLabels(
			"/test/labels/MyFuncLabelProvider.java",
			"attr:My attribute",
			"other:null"
		);
	}

	@Test
	void testComplexReportLabels() throws IOException {
		RosettaTestModel model = loadModel("""
				namespace test

				body Authority Body
				corpus Regulation "Description" Corpus

				report Body Corpus in T+1
					from int
					when IsEligible
					with type Foo

				eligibility rule IsEligible from int:
					item

				type SuperFoo:
					attr1 string (1..1)
						[metadata scheme]
						[label "My Label"]
					qux Qux (1..1)
						[label for Opt1 -> opt1Attr "Super option 1 Attribute"]

				type Foo extends SuperFoo:
					override attr1 string (1..1)
						[metadata scheme]
						[label "My Overridden Label"]
					override qux Qux (1..1)
						[label for item ->> id "Deep path ID"]
					attr2 string (1..1)
						[label for item "Label with item"]
					bar Bar (1..1)
						[label for barAttr "Bar attribute using path"]
						[label for item -> nestedBarList -> nestedAttr "Nested bar attribute $"]

				type Bar:
					barAttr string (1..1)
					  [label for item "Nested Bar attribute"]
						[ruleReference BarAttr]
					nestedBarList NestedBar (0..*)

				type NestedBar:
					nestedAttr string (1..1)

				choice Qux:
					Opt1
					Opt2

				type Opt1:
					id string (1..1)
					opt1Attr int (1..1)
						[label "Option 1 Attribute"]

				type Opt2:
					id string (1..1)
					opt2Attribute int (1..1)


				reporting rule BarAttr from int:
					to-string
				""");

		generateLabelProvider(model);

		// Besides the (unchanged) report provider, a type-rooted provider is generated for every type
		// in this model that carries a *direct* label on its own/inherited/overridden attributes:
		// SuperFoo (attr1, qux), Foo (attr1 override, qux override, attr2, bar), Bar (barAttr) and
		// Opt1 (opt1Attr). NestedBar, Opt2 and Qux have none of their own, so they get nothing - note
		// that Qux is excluded only because none of its options carries a label here, not because it is
		// a choice: see testLabelOnAChoiceOptionGeneratesATypeRootedProvider.
		assertGeneratedFiles(
				"/test/labels/BodyCorpusLabelProvider.java",
				"/test/labels/types/SuperFooLabelProvider.java",
				"/test/labels/types/FooLabelProvider.java",
				"/test/labels/types/BarLabelProvider.java",
				"/test/labels/types/Opt1LabelProvider.java");
		assertGeneratedFileMatches("report-with-complex-labels/BodyCorpusLabelProvider.java", "/test/labels/BodyCorpusLabelProvider.java");
		assertLabels(
			"/test/labels/BodyCorpusLabelProvider.java",
			"attr1:My Overridden Label",
			"attr2:Label with item",
			"bar.barAttr:Bar attribute using path",
			"bar.nestedBarList(0).nestedAttr:Nested bar attribute $",
			"bar.nestedBarList(1).nestedAttr:Nested bar attribute $",
			"qux.Opt1.id:Deep path ID",
			"qux.Opt1.opt1Attr:Super option 1 Attribute",
			"qux.Opt2.id:Deep path ID"
		);
	}

	@Test
	void testCircularReferencesInTypesAreSupported() throws IOException {
		RosettaTestModel model = loadModel("""
				namespace test

				type Foo:
					bar Bar (1..1)
					fooAttr int (1..1)
						[label "Foo attribute"]
					nested A (1..1)
						[label for b -> bAttr "Overridden B attribute"]
						[label for b -> a -> b -> c -> b -> bAttr "Random path B attribute"]

				type Bar:
					foos Foo (0..*)
					barAttr int (1..1)
						[label "Bar attribute"]

				type A:
					b B (1..1)
						[label for c -> b -> bAttr "A -> B -> C -> B attribute"]

				type B:
					a A (0..1)
					c C (1..1)
					bAttr int (1..1)
						[label "Default B attribute"]

				type C:
					b B (0..1)

				func MyFunc:
				    [codeImplementation]
					[ingest JSON]
					inputs:
					    inp int (1..1)
					output:
						foo Foo (1..1)
				""");

		generateLabelProvider(model);

		// Besides the (unchanged) function provider, a type-rooted provider is generated for Foo, Bar
		// and A and B - each carries a direct label on one of its own attributes. C does not, so it
		// gets nothing of its own, even though it participates as an internal node in every other
		// type's reachable graph.
		assertGeneratedFiles(
				"/test/labels/MyFuncLabelProvider.java",
				"/test/labels/types/FooLabelProvider.java",
				"/test/labels/types/BarLabelProvider.java",
				"/test/labels/types/ALabelProvider.java",
				"/test/labels/types/BLabelProvider.java");
		assertGeneratedFileMatches("func-circular/MyFuncLabelProvider.java", "/test/labels/MyFuncLabelProvider.java");
		assertLabels(
			"/test/labels/MyFuncLabelProvider.java",
			"fooAttr:Foo attribute",
			"bar.barAttr:Bar attribute",
			"bar.foos(0).fooAttr:Foo attribute",
			"bar.foos(1).bar.barAttr:Bar attribute",
			"bar.foos(2).bar.foos(1).fooAttr:Foo attribute",
			"nested.b.bAttr:Overridden B attribute",
			"nested.b.a.b.bAttr:Default B attribute",
			"nested.b.c.b.a.b.bAttr:Default B attribute",
			"nested.b.a.b.c.b.bAttr:Random path B attribute",
			"nested.b.a.b.a.b.c.b.bAttr:A -> B -> C -> B attribute"
		);
	}

	@Test
	void testFlatLabelledTypeGeneratesTypeRootedLabelProviderWithNoFunctionOrReport() throws IOException {
		// The CSV-ingest shape: a flat type with labelled columns, and no transform or report at all
		// anywhere in the model. This is exactly the case that produced nothing before this task.
		RosettaTestModel model = loadModel("""
				namespace test

				type Foo:
					attr1 string (1..1)
						[label "Attr One"]
					attr2 int (1..1)
						[label "Attr Two"]
					attr3 number (1..1)
				""");

		generateLabelProvider(model);

		assertGeneratedFiles("/test/labels/types/FooLabelProvider.java");
		// Pins the emitted source of a type-rooted provider, not just its behaviour: the package, the
		// absence of the @Deprecated header that function/report providers carry, and the whitespace the
		// Xtend template is sensitive to. This is the parity gate for migrating the generator to Java.
		assertGeneratedFileMatches("type-flat/FooLabelProvider.java", "/test/labels/types/FooLabelProvider.java");
		assertLabels(
			"/test/labels/types/FooLabelProvider.java",
			"attr1:Attr One",
			"attr2:Attr Two",
			"attr3:null"
		);
	}

	@Test
	void testLabelOnAChoiceOptionGeneratesATypeRootedProvider() throws IOException {
		// A `choice` is a Data type like any other and its options are attributes that can carry a
		// [label ...] of their own, so a choice qualifies under exactly the same "direct labels only"
		// gate as a `type` does. Nothing special-cases choices either way.
		RosettaTestModel model = loadModel("""
				namespace test

				choice Qux:
					Opt1
						[label "Option One"]
					Opt2

				type Opt1:
					id string (1..1)

				type Opt2:
					id string (1..1)
				""");

		generateLabelProvider(model);

		assertGeneratedFiles("/test/labels/types/QuxLabelProvider.java");
		assertLabels(
			"/test/labels/types/QuxLabelProvider.java",
			"Opt1:Option One",
			"Opt2:null"
		);
	}

	@Test
	void testTypeWithNoLabelsGeneratesNoLabelProvider() throws IOException {
		RosettaTestModel model = loadModel("""
				namespace test

				type Foo:
					attr1 string (1..1)
					attr2 int (1..1)
				""");

		generateLabelProvider(model);

		assertGeneratedFiles();
	}

	@Test
	void testInheritedAndOverriddenLabelsAreBothReflectedInTypeRootedProvider() throws IOException {
		RosettaTestModel model = loadModel("""
				namespace test

				type SuperFoo:
					inheritedAttr string (1..1)
						[label "Inherited label"]
					attr string (1..1)
						[label "Super label"]

				type Foo extends SuperFoo:
					override attr string (1..1)
						[label "Overridden label"]
				""");

		generateLabelProvider(model);

		// SuperFoo has its own direct label, so it gets a provider of its own too, in addition to
		// Foo's - both qualify under the "direct labels only" gate independently.
		assertGeneratedFiles(
				"/test/labels/types/SuperFooLabelProvider.java",
				"/test/labels/types/FooLabelProvider.java");
		assertLabels(
			"/test/labels/types/FooLabelProvider.java",
			"inheritedAttr:Inherited label",
			"attr:Overridden label"
		);
		assertLabels(
			"/test/labels/types/SuperFooLabelProvider.java",
			"inheritedAttr:Inherited label",
			"attr:Super label"
		);
	}

	@Test
	void testOuterTypesDeepPathLabelDeliberatelyDisagreesWithInnerTypesOwnLabel() throws IOException {
		// Decision §2.5/§3.2: providers are not composable or substitutable. A deep-path label declared
		// on an outer type deliberately overrides what the nested type's own label would give for that
		// same relative path, so the outer type's provider and the inner type's own provider disagree on
		// purpose. This is intended behaviour, not a bug to be "fixed" by delegating one to the other.
		RosettaTestModel model = loadModel("""
				namespace test

				type Outer:
					nested Inner (1..1)
						[label for attr "Outer's view of nested attr"]

				type Inner:
					attr string (1..1)
						[label "Inner's own label"]
				""");

		generateLabelProvider(model);

		assertGeneratedFiles(
				"/test/labels/types/OuterLabelProvider.java",
				"/test/labels/types/InnerLabelProvider.java");
		assertLabels(
			"/test/labels/types/OuterLabelProvider.java",
			"nested.attr:Outer's view of nested attr"
		);
		assertLabels(
			"/test/labels/types/InnerLabelProvider.java",
			"attr:Inner's own label"
		);
	}

	@Test
	void testTypeAndFunctionSharingANameEachGetTheirOwnLabelProvider() throws IOException {
		// `type Foo` and `func Foo` are legal together in one namespace (they sit in separate
		// uniqueness clusters), which is exactly why type-rooted providers live in a `labels.types`
		// sub-package rather than alongside function-rooted providers in `labels` - a shared package
		// would collide on `FooLabelProvider`.
		RosettaTestModel model = loadModel("""
				namespace test

				type Foo:
					attr string (1..1)
						[label "Foo attribute"]

				func Foo:
				    [codeImplementation]
					[ingest JSON]
					inputs:
					    inp int (1..1)
					output:
						result Foo (1..1)
				""");

		generateLabelProvider(model);

		assertGeneratedFiles(
				"/test/labels/FooLabelProvider.java",
				"/test/labels/types/FooLabelProvider.java");
		assertLabels("/test/labels/FooLabelProvider.java", "attr:Foo attribute");
		assertLabels("/test/labels/types/FooLabelProvider.java", "attr:Foo attribute");

		// The two providers are byte-identical in content (§2.8), but only the function-rooted one is
		// deprecated (§2.9): it is a steer towards the type-rooted provider, not a removal notice, since
		// a function/report provider must stay self-contained permanently for roots defined upstream.
		String functionSource = getGeneratedFile("/test/labels/FooLabelProvider.java").getContents().toString();
		String typeSource = getGeneratedFile("/test/labels/types/FooLabelProvider.java").getContents().toString();
		Assertions.assertTrue(functionSource.contains("@Deprecated"), "Expected the function-rooted provider to be @Deprecated");
		Assertions.assertFalse(typeSource.contains("@Deprecated"), "Expected the type-rooted provider not to be @Deprecated");
	}
}
