package com.regnosys.rosetta.tests.testmodel;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.stream.Streams;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.inject.Injector;
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper;
import com.regnosys.rosetta.tests.util.ExpressionJavaEvaluatorService;
import com.regnosys.rosetta.tests.util.ExpressionParser;
import com.regnosys.rosetta.tests.util.ModelHelper;
import com.regnosys.rosetta.types.RObjectFactory;

/**
 * A utility to create or load Rosetta models.
 * 
 * The result is wrapped in a `RosettaTestModel` or `JavaTestModel`
 * to allow easy access to Ecore objects or generated Java based on
 * the name of the object in the model.
 */
public class RosettaTestModelService {
	@Inject
	private ModelHelper modelHelper;
	@Inject 
    private ValidationTestHelper validationHelper;
	@Inject
	private ExpressionParser expressionParser;
	@Inject
	private CodeGeneratorTestHelper codeGeneratorHelper;
	@Inject
	private RObjectFactory rObjectFactory;
	@Inject
	private JavaTypeTranslator typeTranslator;
	@Inject
	private ExpressionJavaEvaluatorService evaluatorService;
	@Inject
	private Injector injector;
	
	/**
	 * Load a test model from a character sequence. It will assert that there are no issues in the model.
	 */
	public RosettaTestModel toTestModel(CharSequence source) {
		return toTestModel(source, true);
	}
	/**
	 * Load a test model from a character sequence, optionally asserting that there are no issues in the model.
	 */
	public RosettaTestModel toTestModel(CharSequence source, boolean assertNoIssues) {
		RosettaModel model;
		if (assertNoIssues) {
			model = modelHelper.parseRosettaWithNoIssues(source);
		} else {
			model = modelHelper.parseRosetta(source);
		}
		return new RosettaTestModel(source, model, expressionParser);
	}
	/**
	 * Load a test model from a file or folder on the classpath. It will assert that there are no issues in the model.
	 */
	public RosettaTestModel loadTestModelFromResources(String resourceFolderOrFile) throws IOException {
		ResourceSet resourceSet = modelHelper.testResourceSet();
		
		List<Resource> resources = new ArrayList<>();
		URL resourceURL = getClass().getResource(resourceFolderOrFile);
	    Path resourcePath;
		try {
			resourcePath = Path.of(resourceURL.toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		(Files.isDirectory(resourcePath) ? Files.walk(resourcePath, 1) : Streams.of(new Path[] {resourcePath}))
    		.filter(p -> !Files.isDirectory(p))
    		.map(p -> URI.createURI(p.toUri().toString()))
    		.filter(uri -> uri.fileExtension().equals("rosetta"))
    		.forEach(uri -> {
    			Resource res = resourceSet.getResource(uri, true);
    			resources.add(res);
    		});
	    resources.forEach(res -> {
	    	EcoreUtil2.resolveAll(res);
	    	validationHelper.assertNoIssues(res);
	    });
	    
	    if (resources.size() != 1) {
	    	throw new IllegalArgumentException("Expecting 1 rosetta file at " + resourceFolderOrFile + ", but found " + resources.size());
	    }
	    XtextResource resource = (XtextResource) resources.get(0);
	    
	    String source = CharStreams.toString(new InputStreamReader(resourceSet.getURIConverter().createInputStream(resource.getURI(), resourceSet.getLoadOptions()), Charsets.UTF_8));
	    RosettaModel model = (RosettaModel) resource.getContents().get(0);
	    return new RosettaTestModel(source, model, expressionParser);
	}
	
	/**
	 * Load a test model from a character sequence, and generate Java code.
	 */
	public JavaTestModel toJavaTestModel(CharSequence source) {
		RosettaTestModel rosettaModel = toTestModel(source);
		Map<String, String> javaCode = codeGeneratorHelper.generateCode(rosettaModel.getModel());
		return new JavaTestModel(rosettaModel, javaCode, rObjectFactory, typeTranslator, evaluatorService, injector);
	}
	
	/**
	 * Load a test model from a file or folder on the classpath, and generate Java code.
	 */
	public JavaTestModel loadJavaTestModelFromResources(String resourceFolderOrFile) throws IOException {
		RosettaTestModel rosettaModel = loadTestModelFromResources(resourceFolderOrFile);
		Map<String, String> javaCode = codeGeneratorHelper.generateCode(rosettaModel.getModel());
		return new JavaTestModel(rosettaModel, javaCode, rObjectFactory, typeTranslator, evaluatorService, injector);
	}
}
