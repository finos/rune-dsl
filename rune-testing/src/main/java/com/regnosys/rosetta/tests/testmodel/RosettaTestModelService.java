package com.regnosys.rosetta.tests.testmodel;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.rosetta.model.lib.context.RuneContextFactory;
import com.rosetta.util.types.JavaType;
import jakarta.inject.Inject;

import org.apache.commons.lang3.stream.Streams;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.resource.IResourceDescriptionsProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;

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
	private RuneContextFactory contextFactory;
	@Inject
	private Injector injector;
	@Inject
	private IResourceDescriptionsProvider indexAccess;
	
	/**
	 * Load a test model from a character sequence. It will assert that there are no issues in the model.
	 */
	public RosettaTestModel toTestModel(CharSequence source, CharSequence... other) {
		return toTestModel(source, true, other);
	}
	/**
	 * Load a test model from a character sequence, optionally asserting that there are no issues in the model.
	 */
	public RosettaTestModel toTestModel(CharSequence source, boolean assertNoIssues, CharSequence... other) {
		RosettaModel model;
		CharSequence[] sources = new CharSequence[other.length + 1];
		sources[0] = source;
		System.arraycopy(other, 0, sources, 1, other.length);
		if (assertNoIssues) {
			model = modelHelper.parseRosettaWithNoIssues(sources).getFirst();
		} else {
			model = modelHelper.parseRosetta(sources).getFirst();
		}
		return new RosettaTestModel(source, model, indexAccess, expressionParser);
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
	    XtextResource resource = (XtextResource) resources.getFirst();
	    
	    String source = CharStreams.toString(new InputStreamReader(resourceSet.getURIConverter().createInputStream(resource.getURI(), resourceSet.getLoadOptions()), StandardCharsets.UTF_8));
	    RosettaModel model = (RosettaModel) resource.getContents().getFirst();
	    return new RosettaTestModel(source, model, indexAccess, expressionParser);
	}
	
	/**
	 * Load a test model from a character sequence, and generate Java code.
	 */
	public JavaTestModel toJavaTestModel(CharSequence source, CharSequence... other) {
		RosettaTestModel rosettaModel = toTestModel(source, other);
		Map<String, String> javaCode = codeGeneratorHelper.generateCode(rosettaModel.getResourceSet().getResources());
		return new JavaTestModel(rosettaModel, javaCode, rObjectFactory, typeTranslator, evaluatorService, contextFactory, injector);
	}

    public RosettaExpression parseExpression(CharSequence expressionSource, String... attributes) {
        return expressionParser.parseExpression(expressionSource, List.of(attributes));
    }
	
	/**
	 * Load a test model from a file or folder on the classpath, and generate Java code.
	 */
	public JavaTestModel loadJavaTestModelFromResources(String resourceFolderOrFile) throws IOException {
		RosettaTestModel rosettaModel = loadTestModelFromResources(resourceFolderOrFile);
		Map<String, String> javaCode = codeGeneratorHelper.generateCode(rosettaModel.getModel());
		return new JavaTestModel(rosettaModel, javaCode, rObjectFactory, typeTranslator, evaluatorService, contextFactory, injector);
	}

    public <T> T evaluateExpression(Class<T> resultType, CharSequence expr) {
        return resultType.cast(evaluateExpression(JavaType.from(resultType), expr));
    }
    public Object evaluateExpression(JavaType resultType, CharSequence expr) {
        return evaluatorService.evaluate(expressionParser.parseExpression(expr), resultType, null, this.getClass().getClassLoader());
    }
}
