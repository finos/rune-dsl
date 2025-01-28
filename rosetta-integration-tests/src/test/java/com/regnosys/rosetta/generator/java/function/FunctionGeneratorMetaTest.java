package com.regnosys.rosetta.generator.java.function;

import com.regnosys.rosetta.generator.java.RosettaJavaPackages;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.meta.FieldWithMeta;
import com.rosetta.model.lib.meta.Reference;
import com.rosetta.model.lib.meta.ReferenceWithMeta;
import com.rosetta.model.metafields.MetaFields;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class FunctionGeneratorMetaTest {

    @Inject
    FunctionGeneratorHelper functionGeneratorHelper;
    @Inject
    CodeGeneratorTestHelper generatorTestHelper;
    
    //TODO:canSetMetaLocationOnFunctionBasicOutput
    
    
    @Test
    void canSetMetaAddressOnFunctionBasicOutput() {
        var model = """
        metaType address string

        func MyFunc:
            output:
                result string (1..1)
                    [metadata address]
            set result:  "someValue"
            set result -> address: "someLocation"
       """;

        var code = generatorTestHelper.generateCode(model);

        var classes = generatorTestHelper.compileToClasses(code);
        
        
        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var result = functionGeneratorHelper.invokeFunc(myFunc, ReferenceWithMeta.class);

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, new RosettaJavaPackages.RootPackage("com.rosetta.model.metafields"), "ReferenceWithMetaString", Map.of(
                "value", "someValue",
                "reference", Reference.builder().setReference("someLocation")
        ));

        assertEquals(expected, result);
    }    
    
    
    @Test
    void canSetExternalIdOnFunctionObjectOutput() {
        var model = """
        metaType id string
        
        func MyFunc:
            inputs:
        		myValue string (1..1)
                myReference string (1..1)

            output:
                result string (1..1)
                [metadata id]
            set result: myValue
            set result -> id: myReference
        """;
        
        var code = generatorTestHelper.generateCode(model);
        var classes = generatorTestHelper.compileToClasses(code);         
        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var result = functionGeneratorHelper.invokeFunc(myFunc, FieldWithMeta.class, "someValue", "someExternalKey");
        
        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, new RosettaJavaPackages.RootPackage("com.rosetta.model.metafields"), "FieldWithMetaString", Map.of(
                "value", "someValue",
                "meta", MetaFields.builder().setExternalKey("someExternalKey")
        ));

        assertEquals(expected, result);
    }
   

    @Disabled //TODO: implement nested setting of meta and then complete this
    @Test
    void canSetExternalKeyOnFunctionObjectOutput() {
        var model = """
        	
        metaType key string
        metaType reference string
        
        type Foo:
          [metadata key]
            a string (1..1)

        func MyFunc:
            inputs:
                myReference string (1..1)

            output:
                result Foo (1..1)
            set result -> key: myReference
        """;
        
      var code = generatorTestHelper.generateCode(model);
    }

    @Test
    void canSetExternalReferenceOnFunctionObjectOutput() {
        var model = """
        metaType key string
        metaType reference string
        
        type Foo:
          [metadata key]
            a string (1..1)

        func MyFunc:
            inputs:
                myKey string (1..1)

            output:
                result Foo (1..1)
                  [metadata reference]
            set result -> reference: myKey
        """;

        var code = generatorTestHelper.generateCode(model);
        
        var classes = generatorTestHelper.compileToClasses(code);
        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var result = functionGeneratorHelper.invokeFunc(myFunc, ReferenceWithMeta.class, "someExternalReference");
        
        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, new RosettaJavaPackages.RootPackage("com.rosetta.test.model.metafields"), "ReferenceWithMetaFoo", Map.of(
                "externalReference", "someExternalReference"
        ));

        assertEquals(expected, result);
    }

    @Disabled  //TODO: implement setting nested meta
    @Test
    void canSetMetaOnFunctionObjectOutputAndNestedMetaField() {
        var model = """
        type Foo:
            a string (1..1)
            b string (1..1)
                [metadata scheme]

        func MyFunc:
            output:
                result Foo (1..1)
                    [metadata scheme]
            set result -> scheme: "outerScheme"
            set result -> a:  "someValueA"
            set result -> b: "someValueB"
            set result -> b -> scheme: "innerScheme"
        """;

        var code = generatorTestHelper.generateCode(model);
        var classes = generatorTestHelper.compileToClasses(code);
        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var result = functionGeneratorHelper.invokeFunc(myFunc, RosettaModelObject.class);

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, new RosettaJavaPackages.RootPackage("com.rosetta.test.model.metafields"), "FieldWithMetaFoo", Map.of(
                "value", generatorTestHelper.createInstanceUsingBuilder(classes, "Foo", Map.of(
                        "a", "someValueA",
                        "b", generatorTestHelper.createInstanceUsingBuilder(classes, new RosettaJavaPackages.RootPackage("com.rosetta.model.metafields"), "FieldWithMetaString", Map.of(
                                "value", "someValueB",
                                "meta", MetaFields.builder().setScheme("innerScheme")
                        ))
                )),
                "meta", MetaFields.builder().setScheme("outerScheme")
        ));

        assertEquals(expected, result);
    }

    @Test
    void canSetMetaSchemeOnFunctionObjectOutput() {
        var model = """
        type Foo:
            a string (1..1)
            b string (1..1)

        func MyFunc:
            output:
                result Foo (1..1)
                    [metadata scheme]
            set result -> scheme: "outerScheme"
            set result -> a:  "someValueA"
            set result -> b: "someValueB"
        """;

        var code = generatorTestHelper.generateCode(model);
        var classes = generatorTestHelper.compileToClasses(code);
        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var result = functionGeneratorHelper.invokeFunc(myFunc, RosettaModelObject.class);

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, new RosettaJavaPackages.RootPackage("com.rosetta.test.model.metafields"), "FieldWithMetaFoo", Map.of(
                "value", generatorTestHelper.createInstanceUsingBuilder(classes, "Foo", Map.of(
                        "a", "someValueA",
                        "b", "someValueB"
                )),
                "meta", MetaFields.builder().setScheme("outerScheme")
        ));

        assertEquals(expected, result);
    }

    @Test
    void canSetMetaSchemeOnFunctionBasicOutput() {
        var model = """
        func MyFunc:
            output:
                result string (1..1)
                  [metadata scheme]
            set result:  "someValue"
            set result -> scheme: "someScheme"
        """;

        var code = generatorTestHelper.generateCode(model);
        var classes = generatorTestHelper.compileToClasses(code);

        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var result =  functionGeneratorHelper.invokeFunc(myFunc, FieldWithMeta.class);

        var expected = generatorTestHelper.createFieldWithMetaString(classes, "someValue", "someScheme");

        assertEquals(expected, result);
    }
}
