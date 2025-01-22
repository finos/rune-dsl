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
	
	
	private RosettaTestModel loadModel(String rootFolderName) throws IOException {
		return testModelService.loadTestModelFromResources("/label-annotations/" + rootFolderName);
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
		RosettaTestModel model = loadModel("func-without-transform-annotation");
		
		generateLabelProviderForFunction(model, "MyFunc");
		
		assertNoGeneratedFiles();
	}
	
	@Test
	void testFunctionWithIngestAnnotationGeneratesLabelProvider() throws IOException {
		RosettaTestModel model = loadModel("func-ingest");
		
		generateLabelProviderForFunction(model, "MyFunc");
		
		assertSingleGeneratedFile("func-ingest/MyFuncLabelProvider.java", "/test/labels/MyFuncLabelProvider.java");
		assertLabels(
			"attr:My attribute",
			"other:null"
		);
	}
	
	@Test
	void testReportLabelOverridesRuleReferenceLabel() throws IOException {
		RosettaTestModel model = loadModel("report-with-rule-references");
		
		generateLabelProviderForReport(model, "Body", "Corpus");
		
		assertSingleGeneratedFile("report-with-rule-references/BodyCorpusLabelProvider.java", "/test/labels/BodyCorpusLabelProvider.java");
		assertLabels(
			"attr:My attribute",
			"other:Other from rule"
		);
	}
	
	@Test
	void testComplexReportLabels() throws IOException {
		RosettaTestModel model = loadModel("report-with-complex-labels");
		
		generateLabelProviderForReport(model, "Body", "Corpus");
		
		assertSingleGeneratedFile("report-with-complex-labels/BodyCorpusLabelProvider.java", "/test/labels/BodyCorpusLabelProvider.java");
		assertLabels(
			"attr1:My Label",
			"attr2:Label with item",
			"bar.barAttr:Bar attribute using path",
			"bar.nestedBarList(0).nestedAttr:Nested bar attribute $",
			"bar.nestedBarList(1).nestedAttr:Nested bar attribute $",
			"qux.Opt1.id:Deep path ID",
			"qux.Opt2.id:Deep path ID"
		);
	}
}
