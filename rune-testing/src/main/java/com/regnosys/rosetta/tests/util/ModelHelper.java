package com.regnosys.rosetta.tests.util;

import com.regnosys.rosetta.builtin.RosettaBuiltinsService;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.rosetta.util.DottedPath;
import jakarta.inject.Inject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.eclipse.xtext.testing.util.ResourceHelper;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;

import java.util.ArrayList;
import java.util.List;

public class ModelHelper {

    @Inject
    private ParseHelper<RosettaModel> parseHelper;

    @Inject
    private ValidationTestHelper validationTestHelper;

    @Inject
    private RosettaBuiltinsService builtins;

    @Inject
    private ResourceHelper resourceHelper;

    public static final String commonTestTypes = getVersionInfo() + """
        metaType scheme string
        """;

    private static String getVersionInfo() {
        return """
            namespace "com.rosetta.test.model"
            version "test"
            """;
    }

    private final DottedPath rootpack = DottedPath.splitOnDots("com.rosetta.test.model");

    public final DottedPath rootPackage() {
        return rootpack;
    }

    public RosettaModel parseRosetta(CharSequence model) {
        CharSequence m = model;
        if (!model.toString().trim().startsWith("namespace")) {
            m = getVersionInfo() + "\n" + m;
        }
        ResourceSet resourceSet = testResourceSet();

        RosettaModel parsed = safeParse(m, resourceSet);
        EcoreUtil2.resolveAll(parsed);
        return parsed;
    }

    /*
     * This method parses all the models up front using the ResourceHelper.
     * This is necessary to ensure that all indexes are populated ready for cross linking.
     * At the end the model contents are returned using RosettaModel.getContents(), this call triggers
     * the RosettaDerivedStateComputer which needs the indexes to be populated.
     */
    public List<RosettaModel> parseRosetta(CharSequence... models) {
        ResourceSet resourceSet = testResourceSet();
        List<RosettaModel> result = new ArrayList<>();
        List<Resource> resources = new ArrayList<>();
        for (CharSequence it : models) {
            CharSequence content = it.toString().trim().startsWith("namespace")
                    ? it
                    : (getVersionInfo() + "\n" + it);
            try {
                resources.add(resourceHelper.resource(content, resourceSet));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        for (Resource r : resources) {
            result.add((RosettaModel) r.getContents().getFirst());
        }
        return result;
    }

    public RosettaModel parseRosettaWithNoErrors(CharSequence model) {
        RosettaModel parsed = parseRosetta(model);
        validationTestHelper.assertNoErrors(parsed);
        return parsed;
    }

    public List<RosettaModel> parseRosettaWithNoErrors(CharSequence... models) {
        List<RosettaModel> parsed = parseRosetta(models);
        for (RosettaModel rm : parsed) {
            validationTestHelper.assertNoErrors(rm);
        }
        return parsed;
    }

    public RosettaModel parseRosettaWithNoIssues(CharSequence model) {
        RosettaModel parsed = parseRosetta(model);
        validationTestHelper.assertNoIssues(parsed);
        return parsed;
    }

    public List<RosettaModel> parseRosettaWithNoIssues(CharSequence... models) {
        List<RosettaModel> parsed = parseRosetta(models);
        for (RosettaModel rm : parsed) {
            validationTestHelper.assertNoIssues(rm);
        }
        return parsed;
    }

    public RosettaModel combineAndParseRosetta(CharSequence... models) {
        StringBuilder m = new StringBuilder(getVersionInfo());
        for (CharSequence model : models) {
            m.append("\n").append(model);
        }
        ResourceSet resourceSet = testResourceSet();

        RosettaModel parsed = safeParse(m, resourceSet);
        validationTestHelper.assertNoErrors(parsed);
        return parsed;
    }

    public ResourceSet testResourceSet() {
        RosettaModel parsed = safeParse(commonTestTypes, null);
        ResourceSet resourceSet = parsed.eResource().getResourceSet();
        resourceSet.getResource(builtins.basicTypesURI, true);
        resourceSet.getResource(builtins.annotationsURI, true);
        return resourceSet;
    }

    private RosettaModel safeParse(CharSequence content, ResourceSet resourceSet) {
        try {
            if (resourceSet != null) {
                return parseHelper.parse(content, resourceSet);
            } else {
                return parseHelper.parse(content);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Rosetta model content.", e);
        }
    }
}
