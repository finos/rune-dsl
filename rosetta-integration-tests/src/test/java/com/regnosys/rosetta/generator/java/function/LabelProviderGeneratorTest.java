package com.regnosys.rosetta.generator.java.function;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.xbase.testing.RegisteringFileSystemAccess;
import org.eclipse.xtext.xbase.testing.RegisteringFileSystemAccess.GeneratedFile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.common.base.Charsets;
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
	private void generateLabelProviderForFunction(RosettaTestModel model, String functionName) {
		labelProviderGenerator.generateForFunctionIfApplicable(fsa, model.getFunction(functionName));
	}
	private void generateLabelProviderForReport(RosettaTestModel model, String body, String... corpusList) {
		labelProviderGenerator.generateForReport(fsa, model.getReport(body, corpusList));
	}
	
	private List<String> getGeneratedFileNames() {
		return fsa.getGeneratedFiles().stream().map(f -> f.getPath()).collect(Collectors.toList());
	}
	private void assertNoGeneratedFiles() {
		List<String> fileNames = getGeneratedFileNames();
		Assertions.assertTrue(
				fileNames.isEmpty(),
				"The following files were generated:" + fileNames.stream().collect(Collectors.joining("\n", "\n", "\n"))
		);
	}
	private GeneratedFile getSingleGeneratedFile() {
		List<String> fileNames = getGeneratedFileNames();
		Assertions.assertEquals(
				1,
				fileNames.size(),
				"The following files were generated:" + fileNames.stream().collect(Collectors.joining("\n", "\n", "\n"))
		);
		String actualGeneratedPath = fileNames.get(0);
		return fsa.getGeneratedFiles().stream().filter(f -> f.getPath().equals(actualGeneratedPath)).findAny().orElseThrow();
	}
	private LabelProvider getLabelProviderInstance() {
		GeneratedFile file = getSingleGeneratedFile();
		Map<String, Class<?>> compiled = generatorTestHelper.compileToClasses(Map.of(file.getJavaClassName(), file.getContents().toString()));
		Class<? extends LabelProvider> clazz = compiled.get(file.getJavaClassName()).asSubclass(LabelProvider.class);
		return injector.getInstance(clazz);
	}
	private void assertSingleGeneratedFile(String expectationFileName, String expectedGeneratedPath) throws IOException {
		GeneratedFile file = getSingleGeneratedFile();
		Assertions.assertEquals(expectedGeneratedPath, file.getPath().replace("/test-project/src-gen/main/java", ""));
		String actualSource = file.getContents().toString();
		String expectedSource = Resources.toString(getClass().getResource("/label-annotations/" + expectationFileName), Charsets.UTF_8);
		Assertions.assertEquals(expectedSource, actualSource);
	}
	private void assertLabels(String... pathLabelExpectations) {
		LabelProvider provider = getLabelProviderInstance();
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
						[label as "My attribute"]
				
				annotation myAnn:
				
				func MyFunc:
					[myAnn]
					output:
						foo Foo (1..1)
				""");
		
		generateLabelProviderForFunction(model, "MyFunc");
		
		assertNoGeneratedFiles();
	}
	
	@Test
	void testFunctionWithIngestAnnotationGeneratesLabelProvider() throws IOException {
		RosettaTestModel model = loadModel("""
				namespace test
				
				type Foo:
					attr string (1..1)
						[label as "My attribute"]
					other int (1..1)
				
				func MyFunc:
					[ingest JSON]
					output:
						foo Foo (1..1)
				""");
		
		generateLabelProviderForFunction(model, "MyFunc");
		
		assertSingleGeneratedFile("func-ingest/MyFuncLabelProvider.java", "/test/labels/MyFuncLabelProvider.java");
		assertLabels(
			"attr:My attribute",
			"other:null"
		);
	}
	
	@Test
	void testReportLabelOverridesRuleReferenceLabel() throws IOException {
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
				
				type Foo:
					attr string (1..1)
						[ruleReference FooAttr]
						[label as "My attribute"]
					other int (1..1)
						[ruleReference FooOther]
				
				
				reporting rule FooAttr from int:
					to-string
					as "My attribute from rule"
				
				reporting rule FooOther from int:
					item
					as "Other from rule"
				""");
		
		generateLabelProviderForReport(model, "Body", "Corpus");
		
		assertSingleGeneratedFile("report-with-rule-references/BodyCorpusLabelProvider.java", "/test/labels/BodyCorpusLabelProvider.java");
		assertLabels(
			"attr:My attribute",
			"other:Other from rule"
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
						[label as "My Label"]
					qux Qux (1..1)
						[label Opt1 -> opt1Attr as "Super option 1 Attribute"]
				
				type Foo extends SuperFoo:
					override attr1 string (1..1)
						[metadata scheme]
						[label as "My Overridden Label"]
					override qux Qux (1..1)
						[label item ->> id as "Deep path ID"]
					attr2 string (1..1)
						[label item as "Label with item"]
					bar Bar (1..1)
						[label barAttr as "Bar attribute using path"]
						[label item -> nestedBarList -> nestedAttr as "Nested bar attribute $"]
				
				type Bar:
					barAttr string (1..1)
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
						[label as "Option 1 Attribute"]
				
				type Opt2:
					id string (1..1)
					opt2Attribute int (1..1)
				
				
				reporting rule BarAttr from int:
					to-string
					as "Bar attribute from rule"
				""");
		
		generateLabelProviderForReport(model, "Body", "Corpus");
		
		assertSingleGeneratedFile("report-with-complex-labels/BodyCorpusLabelProvider.java", "/test/labels/BodyCorpusLabelProvider.java");
		assertLabels(
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
}
