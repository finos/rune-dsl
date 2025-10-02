package com.regnosys.rosetta.tools.minimalexampleproducer;

import com.regnosys.rosetta.builtin.RosettaBuiltinsService;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaRule;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import jakarta.inject.Inject;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.serializer.impl.Serializer;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class UnnecessaryElementsRemoverTest {

    @Inject
    private UnnecessaryElementsRemover service;

    @Inject
    private Serializer serializer;

    @Inject
    private ParseHelper<RosettaModel> parseHelper;

    @Inject
    private ValidationTestHelper validationTestHelper;

    @Inject
    private RosettaBuiltinsService builtins;

    @Inject
    private XtextResourceSet resourceSet;

    @Test
    public void testElementsRemover() throws Exception {
        resourceSet.getResource(builtins.basicTypesURI, true);
        resourceSet.getResource(builtins.annotationsURI, true);

        RosettaModel model1 = parseHelper.parse("""
            namespace a
            
            enum MyEnum:
            	VALUE1
            	VALUE2
            
            type Foo:
            	attr1 int (0..1)
            	attr2 int (0..1)
            	attr3 string (0..1)
            """, resourceSet);

        RosettaModel model2 = parseHelper.parse("""
            namespace b
            
            import a.*
            import c.*
            
            func F: <"Definition of F">
            	inputs:
            		a string (1..1)
            	output:
            		result MyEnum (1..1)
            
            	set result:
            		if Unnecessary {} = Unnecessary {}
            		then MyEnum -> VALUE1
            		else MyEnum -> VALUE2
            
            reporting rule R from string:
            	if F = MyEnum -> VALUE1
            	then Foo { attr1: 0, ... }
            	else Foo { attr2: 42, ... }
            """, resourceSet);

        RosettaModel model3 = parseHelper.parse("""
            namespace c
            
            type Unnecessary:
            """, resourceSet);

        RosettaRule rule = model2.getElements().stream()
            .filter(e -> e instanceof RosettaRule)
            .map(e -> (RosettaRule) e)
            .filter(r -> Objects.equals(r.getName(), "R"))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Rule R not found"));

        // Validate initial state
        resourceSet.getResources().forEach(res -> validationTestHelper.assertNoIssues(res));
        assertEquals(5, resourceSet.getResources().size());
        assertTrue(resourceSet.getResources().contains(model3.eResource()));

        // Perform removal
        service.removeUnnecessaryElementsFromResourceSet(rule, false);

        // Validate final state
        resourceSet.getResources().forEach(res -> validationTestHelper.assertNoErrors(res));
        assertEquals(4, resourceSet.getResources().size());
        assertFalse(resourceSet.getResources().contains(model3.eResource()));

        String expectedModel1 = """
            namespace a
            
            enum MyEnum:
            	VALUE1
            
            type Foo:
            	attr1 int (0..1)
            	attr2 int (0..1)
            """;
        assertEquals(normalize(expectedModel1), normalize(serializer.serialize(model1)));

        String expectedModel2 = """
            namespace b
            
            import a.*
            
            func F:
                inputs:
                    a string (1..1)
            	output:
            		result MyEnum (1..1)
            
            reporting rule R from string:
            	if F = MyEnum -> VALUE1
            	then Foo { attr1: 0, ... }
            	else Foo { attr2: 42, ... }
            """;
        assertEquals(normalize(expectedModel2), normalize(serializer.serialize(model2)));
    }

    private static String normalize(String s) {
        if (s == null) return null;
        // Normalize CRLF -> LF, and drop trailing whitespace to ignore final newline differences
        return s.replace("\r\n", "\n").replaceAll("\\s+$", "");
    }

}
