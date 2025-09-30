package com.regnosys.rosetta.generator.java.function;

import com.google.common.collect.Lists;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.meta.FieldWithMeta;
import com.rosetta.model.lib.meta.Reference;
import com.rosetta.model.lib.meta.ReferenceWithMeta;
import com.rosetta.model.metafields.MetaAndTemplateFields;
import com.rosetta.model.metafields.MetaFields;
import com.rosetta.util.DottedPath;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class FunctionGeneratorMetaTest {

    @Inject
    private FunctionGeneratorHelper functionGeneratorHelper;
    @Inject
    private CodeGeneratorTestHelper generatorTestHelper;

    @Test
    void canGetMetaTemplateFromSuperType() {
        var model = """
                metaType key string
                metaType template string
        
                type Foo:
                    [metadata key]
                    [metadata template]
                
                type Bar extends Foo:
        
                func MyFunc:
        		    inputs:
        				bar Bar (1..1)
                    output:
                        result string (1..1)
        
                    set result: bar -> template
        """;

        var code = generatorTestHelper.generateCode(model);
        var classes = generatorTestHelper.compileToClasses(code);
        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var input = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model"), "Bar", Map.of(
                        "meta", MetaAndTemplateFields.builder().setTemplate("someTemplate").build()
                )
        );

        var result = functionGeneratorHelper.invokeFunc(myFunc, String.class, input);

        assertEquals("someTemplate", result);
    }

    @Test
    void canGetMetaTemplate() {
        var model = """
                metaType key string
                metaType template string
        
                type Foo:
                    [metadata key]
                    [metadata template]
        
                func MyFunc:
        		    inputs:
        				foo Foo (1..1)
                    output:
                        result string (1..1)
        
                    set result: foo -> template
        """;

        var code = generatorTestHelper.generateCode(model);
        var classes = generatorTestHelper.compileToClasses(code);
        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var input = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model"), "Foo", Map.of(
                        "meta", MetaAndTemplateFields.builder().setTemplate("someTemplate").build()
                )
        );

        var result = functionGeneratorHelper.invokeFunc(myFunc, String.class, input);

        assertEquals("someTemplate", result);
    }

    @Test
    void canSetMetaTemplate() {
        var model = """
                metaType key string
                metaType template string
        
                type Foo:
                    [metadata key]
                    [metadata template]
        
                func MyFunc:
                    output:
                        result Foo (1..1)
        
                    set result: Foo {} with-meta { template: "someTemplate" }
        """;

        var code = generatorTestHelper.generateCode(model);
        var classes = generatorTestHelper.compileToClasses(code);
        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model"), "Foo", Map.of(
                        "meta", MetaAndTemplateFields.builder().setTemplate("someTemplate").build()
                )
        );

        var result = functionGeneratorHelper.invokeFunc(myFunc, RosettaModelObject.class);

        assertEquals(expected, result);
    }

    @Test
    void canAccessMetaKeyOnSuperType() {
        var model = """
                metaType key string
        
                type Foo:
                    [metadata key]
                    someFooField string (1..1)
        
                type Bar extends Foo:
                   someBarField string (1..1)
        
                func MyFunc:
                    inputs:
                        inBar Bar (1..1)
        
                   output:
                        result string (1..1)
        
                    set result: inBar -> key
        """;

        var code = generatorTestHelper.generateCode(model);

        var classes = generatorTestHelper.compileToClasses(code);

        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var input = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model"), "Bar", Map.of(
                        "someBarField", "someBarFieldValue",
                        "someFooField", "someFooFieldValue",
                        "meta", MetaFields.builder().setExternalKey("someKey").build()
                )
        );

        var result = functionGeneratorHelper.invokeFunc(myFunc, String.class, input);

        assertEquals("someKey", result);
    }

    @Test
    void canSetUsingSettersKeyOnSuperType() {
        var model = """
                metaType key string
        
                type Foo:
                   [metadata key]
                   someFooField string (1..1)
        
                type Bar extends Foo:
                   someBarField string (1..1)
        
                func MyFunc:
                    inputs:
                        inBarField string (1..1)
                        inFooField string (1..1)
                        inKey string (1..1)
        
                    output:
                        result Bar (1..1)
        
                    set result -> someBarField: inBarField
                    set result -> someFooField: inFooField
                    set result -> key: inKey
        """;

        var code = generatorTestHelper.generateCode(model);

        var classes = generatorTestHelper.compileToClasses(code);

        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var result = functionGeneratorHelper.invokeFunc(myFunc, RosettaModelObject.class, "someBarFieldValue", "someFooFieldValue", "someKey");

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model"), "Bar", Map.of(
                        "someBarField", "someBarFieldValue",
                        "someFooField", "someFooFieldValue",
                        "meta", MetaFields.builder().setExternalKey("someKey").build()
                )
        );

        assertEquals(expected, result);
    }

    @Test
    void canSetUsingWithMetaKeyOnSuperType() {
        var model = """
                metaType key string
        
                type Foo:
                   [metadata key]
                   someFooField string (1..1)
        
                type Bar extends Foo:
                   someBarField string (1..1)
        
                func MyFunc:
                    inputs:
                        inBarField string (1..1)
                        inFooField string (1..1)
                        inKey string (1..1)
        
                    output:
                        result Bar (1..1)
        
                    set result:
                        Bar {
                                someBarField: inBarField,
                                someFooField: inFooField
                            } with-meta {
                                  key: inKey
                              }
        """;

        var code = generatorTestHelper.generateCode(model);

        var classes = generatorTestHelper.compileToClasses(code);

        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var result = functionGeneratorHelper.invokeFunc(myFunc, RosettaModelObject.class, "someBarFieldValue", "someFooFieldValue", "someKey");

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model"), "Bar", Map.of(
                        "someBarField", "someBarFieldValue",
                        "someFooField", "someFooFieldValue",
                        "meta", MetaFields.builder().setExternalKey("someKey").build()
                )
        );

        assertEquals(expected, result);
    }

    @Test
    void canAccessKeyUsingWithMeta() {
        var model = """
                metaType key string
        
                type Foo:
                  [metadata key]
                   someField string (1..1)
        
                func MyFunc:
                    inputs:
                        inFoo Foo (1..1)
        
                    output:
                        result string (1..1)
        
                    set result: inFoo -> key
        """;

        var code = generatorTestHelper.generateCode(model);

        var classes = generatorTestHelper.compileToClasses(code);

        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var input = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model"), "Foo", Map.of(
                        "someField", "someFieldValue",
                        "meta", MetaFields.builder().setExternalKey("someKey").build()
                )
        );

        var result = functionGeneratorHelper.invokeFunc(myFunc, String.class, input);

        assertEquals("someKey", result);
    }

    @Test
    void canSetUsingWithMetaSameTypeNameAcrossNamespaces() {
        var model1 = """
                namespace other
                
                type Baz:
                  bazField string (1..1)
                """;

        var model2 = """
                import other.* as testOther
                
                type Baz:
                  bazField string (1..1)
                
                func MyFunc:
                    inputs:
                        inBaz testOther.Baz (1..1)
                    output:
                        outBaz Baz (1..1)
                        [metadata scheme]
                
                    set outBaz: Baz {
                        bazField: inBaz -> bazField
                    } with-meta {
                              scheme: "someScheme"
                          }
                """;

        var code = generatorTestHelper.generateCode(new String[]{model1, model2});

        var classes = generatorTestHelper.compileToClasses(code);

        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var input = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("other"), "Baz", Map.of(
                        "bazField", "bazFieldValue"
                )
        );

        var result = functionGeneratorHelper.invokeFunc(myFunc, RosettaModelObject.class, input);

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model.metafields"), "FieldWithMetaBaz", Map.of(
                "value", generatorTestHelper.createInstanceUsingBuilder(classes, "Baz", Map.of(
                        "bazField", "bazFieldValue"
                )),
                "meta", MetaFields.builder().setScheme("someScheme").build()
        ));

        assertEquals(expected, result);

    }

    @Test
    void canSetUsingArrowMetaSameTypeNameAcrossNamespaces() {
        var model1 = """
                namespace other
                
                type Baz:
                  bazField string (1..1)
                """;

        var model2 = """
                import other.* as testOther
                
                type Baz:
                  bazField string (1..1)
                
                func MyFunc:
                    inputs:
                        inBaz testOther.Baz (1..1)
                    output:
                        outBaz Baz (1..1)
                        [metadata scheme]
                
                    set outBaz -> bazField: inBaz -> bazField
                    set outBaz -> scheme: "someScheme"
                """;

        var code = generatorTestHelper.generateCode(new String[]{model1, model2});

        var classes = generatorTestHelper.compileToClasses(code);

        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var input = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("other"), "Baz", Map.of(
                        "bazField", "bazFieldValue"
                )
        );

        var result = functionGeneratorHelper.invokeFunc(myFunc, RosettaModelObject.class, input);

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model.metafields"), "FieldWithMetaBaz", Map.of(
                "value", generatorTestHelper.createInstanceUsingBuilder(classes, "Baz", Map.of(
                        "bazField", "bazFieldValue"
                )),
                "meta", MetaFields.builder().setScheme("someScheme").build()
        ));

        assertEquals(expected, result);
    }

    @Test
    void canSetMetaWithMulticardinalityOutput() {
        var model = """
                type Foo:
                
                func MyFunc:
                    output:
                        result Foo (0..*)
                            [metadata location]
                    add result:
                        empty with-meta { location: "loc" }
                """;

        var code = generatorTestHelper.generateCode(model);

        var classes = generatorTestHelper.compileToClasses(code);

        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var result = functionGeneratorHelper.invokeFunc(myFunc, List.class);

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model.metafields"), "FieldWithMetaFoo", Map.of(
                "meta", MetaFields.builder().setScopedKey("loc").build()
        ));

        assertEquals(List.of(expected), result);
    }

    @Test
    void canSetMetaOnEmptyPassedArgumentUsingWithMeta() {
        var model = """
                type Foo:
                
                func MyFunc:
                    inputs:
                         inputFoo Foo (0..1)
                         keyValue string (0..1)
                     output:
                         fooWithLocation Foo (0..1)
                             [metadata location]
                
                     set fooWithLocation:
                         inputFoo
                             with-meta {
                                 location: keyValue
                             }
                """;

        var code = generatorTestHelper.generateCode(model);

        var classes = generatorTestHelper.compileToClasses(code);

        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var result = functionGeneratorHelper.invokeFunc(myFunc, RosettaModelObject.class, null, "someLocation");

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model.metafields"), "FieldWithMetaFoo", Map.of(
                "meta", MetaFields.builder().setScopedKey("someLocation").build()
        ));

        assertEquals(expected, result);
    }

    @Test
    void canAccessMetaLocationCorrectly() {
        var model = """
                metaType location string
                
                func MyFunc:
                    inputs:
                        inputField string (1..1)
                          [metadata location]
                    output:
                        outString string (1..1)
                
                    set outString: inputField -> location
                """;

        var code = generatorTestHelper.generateCode(model);

        var classes = generatorTestHelper.compileToClasses(code);

        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var input = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.model.metafields"), "FieldWithMetaString", Map.of(
                "value", "someValue",
                "meta", MetaFields.builder().setScopedKey("someLocationValue")
        ));

        var result = functionGeneratorHelper.invokeFunc(myFunc, String.class, input);

        assertEquals("someLocationValue", result);

    }

    @Test
    void canSetMetaLocationUsingWithMeta() {
        var model = """
                metaType location string
                
                func MyFunc:
                    inputs:
                        inputField string (1..1)
                        keyValue string (1..1)
                    output:
                        outString string (1..1)
                          [metadata location]
                
                    set outString:
                        inputField with-meta { location: keyValue }
                """;

        var code = generatorTestHelper.generateCode(model);

        var classes = generatorTestHelper.compileToClasses(code);

        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var result = functionGeneratorHelper.invokeFunc(myFunc, FieldWithMeta.class, "someValue", "someLocationValue");

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.model.metafields"), "FieldWithMetaString", Map.of(
                "value", "someValue",
                "meta", MetaFields.builder().setScopedKey("someLocationValue")
        ));

        assertEquals(expected, result);
    }

    @Test
    void canCreateMetaTypeUsingConstructorAndEmptyFieldWithMeta() {
        var model = """
                type Bar:
                    barField string (1..1)
                
                
                func MyFunc:
                    output:
                        result Bar (0..1)
                         [metadata scheme]
                
                    set result:
                            empty with-meta {
                                scheme: "someScheme"
                            }
                """;

        var code = generatorTestHelper.generateCode(model);

        var classes = generatorTestHelper.compileToClasses(code);

        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var result = functionGeneratorHelper.invokeFunc(myFunc, RosettaModelObject.class);

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model.metafields"), "FieldWithMetaBar", Map.of(
                "meta", MetaFields.builder().setScheme("someScheme").build()

        ));


        assertEquals(expected, result);
    }


    @Test
    void canCreateMetaTypeUsingConstructorAndEmptyReferenceWithMeta() {
        var model = """
                metaType key string
                metaType reference string
                
                type Bar:
                    fooReference Foo (0..1)
                    [metadata reference]
                
                type Foo:
                    [metadata key]
                    someField string (1..1)
                
                func MyFunc:
                    output:
                        result Bar (1..1)
                
                    set result:
                        Bar {
                            fooReference:
                                empty with-meta {
                                    reference: "someRef"
                                }
                        }
                """;

        var code = generatorTestHelper.generateCode(model);

        var classes = generatorTestHelper.compileToClasses(code);

        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var result = functionGeneratorHelper.invokeFunc(myFunc, RosettaModelObject.class);

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model"), "Bar", Map.of(
                "fooReference", generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model.metafields"), "ReferenceWithMetaFoo", Map.of(
                        "externalReference", "someRef"
                ))));

        assertEquals(expected, result);
    }

    @Test
    void canCreateMetaTypeUsingConstructorSyntaxWithIfStatement() {
        var model = """
                metaType key string
                metaType reference string
                
                type Bar:
                    fooReference Foo (0..1)
                    [metadata reference]
                
                type Foo:
                    [metadata key]
                    someField string (1..1)
                
                func GetRefFunc:
                    output:
                        fooReference Foo (0..1)
                        [metadata reference]
                
                    set fooReference -> reference: "someRef"
                
                func MyFunc:
                    output:
                        result Bar (1..1)
                
                    set result:
                        Bar {
                            fooReference:
                               if True
                               then GetRefFunc()
                        }
                """;

        var code = generatorTestHelper.generateCode(model);

        var classes = generatorTestHelper.compileToClasses(code);

        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var result = functionGeneratorHelper.invokeFunc(myFunc, RosettaModelObject.class);

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model"), "Bar", Map.of(
                "fooReference", generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model.metafields"), "ReferenceWithMetaFoo", Map.of(
                        "externalReference", "someRef"
                ))));

        assertEquals(expected, result);
    }

    @Test
    void canReadRererenceOnObjectWithReference() {
        var model = """
                metaType reference string
                metaType key string
                
                
                type Foo:
                  [metadata key]
                  fooField string (1..1)
                
                func MyFunc:
                    inputs:
                        fooReference Foo (1..1)
                        [metadata reference]
                
                    output:
                        result string (1..1)
                
                    set result: fooReference -> reference
                """;

        var code = generatorTestHelper.generateCode(model);

        var classes = generatorTestHelper.compileToClasses(code);

        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var myInput = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model.metafields"), "ReferenceWithMetaFoo", Map.of(
                "value", generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model"), "Foo", Map.of(
                        "fooField", "someValue"
                )),
                "externalReference", "someReference"
        ));

        var result = functionGeneratorHelper.invokeFunc(myFunc, String.class, myInput);

        assertEquals("someReference", result);

    }

    @Test
    void canSetMetaOnEnumUsingWithMetaSyntax() {
        var model = """
                metaType scheme string
                
                enum MyEnum:
                		A
                		B
                		C
                
                func MyFunc:
                    output:
                        result MyEnum (1..1)
                		  [metadata scheme]
                
                	alias myEnum: MyEnum -> B
                
                    set result: myEnum with-meta {
                                                scheme: "someScheme"
                                            }
                """;

        var code = generatorTestHelper.generateCode(model);

        var classes = generatorTestHelper.compileToClasses(code);

        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var result = functionGeneratorHelper.invokeFunc(myFunc, RosettaModelObject.class);

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model.metafields"), "FieldWithMetaMyEnum", Map.of(
                "value", generatorTestHelper.createEnumInstance(classes, "MyEnum", "B"),
                "meta", MetaFields.builder().setScheme("someScheme").build()
        ));

        assertEquals(expected, result);
    }

    @Test
    void canAddKeyToExistingMetaObjectUsingWithMetaSyntax() {
        var model = """
                metaType key string
                metaType scheme string
                metaType location string
                
                type Foo:
                  [metadata key]
                   someField string (1..1)
                
                func MyFunc:
                	inputs:
                	    myInput Foo (1..1)
                	      [metadata location]
                
                    output:
                        result Foo (1..1)
                		  [metadata location]
                
                    set result: myInput with-meta {
                								key: "someKey"
                                            }
                """;

        var code = generatorTestHelper.generateCode(model);

        var classes = generatorTestHelper.compileToClasses(code);

        var myInput = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model.metafields"), "FieldWithMetaFoo", Map.of(
                "value", generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model"), "Foo", Map.of(
                        "someField", "someValue",
                        "meta", MetaFields.builder().setExternalKey("someOtherKey").build()
                )),
                "meta", MetaFields.builder().setLocation("someLocation").build()
        ));

        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var result = functionGeneratorHelper.invokeFunc(myFunc, RosettaModelObject.class, myInput);

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model.metafields"), "FieldWithMetaFoo", Map.of(
                "value", generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model"), "Foo", Map.of(
                        "someField", "someValue",
                        "meta", MetaFields.builder().setExternalKey("someKey").build()
                )),
                "meta", MetaFields.builder().setLocation("someLocation").build()
        ));

        assertEquals(expected, result);
    }

    @Test
    void canAddKeyAndSchemeToExistingMetaObjectUsingWithMetaSyntax() {
        var model = """
                metaType key string
                metaType scheme string
                metaType location string
                
                type Foo:
                  [metadata key]
                   someField string (1..1)
                
                func MyFunc:
                	inputs:
                	    myInput Foo (1..1)
                	      [metadata location]
                
                    output:
                        result Foo (1..1)
                		  [metadata scheme]
                		  [metadata location]
                
                    set result: myInput with-meta {
                								key: "someKey",
                                                scheme: "someScheme"
                                            }
                """;

        var code = generatorTestHelper.generateCode(model);

        var classes = generatorTestHelper.compileToClasses(code);

        var myInput = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model.metafields"), "FieldWithMetaFoo", Map.of(
                "value", generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model"), "Foo", Map.of(
                        "someField", "someValue",
                        "meta", MetaFields.builder().setExternalKey("someOtherKey").build()
                )),
                "meta", MetaFields.builder().setLocation("someLocation").build()
        ));

        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var result = functionGeneratorHelper.invokeFunc(myFunc, RosettaModelObject.class, myInput);

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model.metafields"), "FieldWithMetaFoo", Map.of(
                "value", generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model"), "Foo", Map.of(
                        "someField", "someValue",
                        "meta", MetaFields.builder().setExternalKey("someKey").build()
                )),
                "meta", MetaFields.builder().setScheme("someScheme").setLocation("someLocation").build()
        ));

        assertEquals(expected, result);
    }

    @Test
    void canAddSchemeToExistingNonMetaObjectUsingWithMetaSyntax() {
        var model = """
                metaType key string
                metaType scheme string
                
                type Foo:
                  [metadata key]
                   someField string (1..1)
                
                func MyFunc:
                	inputs:
                	    myInput Foo (1..1)
                
                    output:
                        result Foo (1..1)
                		  [metadata scheme]
                
                    set result: myInput with-meta {
                                                scheme: "someScheme"
                                            }
                """;

        var code = generatorTestHelper.generateCode(model);

        var classes = generatorTestHelper.compileToClasses(code);

        var myInput = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model"), "Foo", Map.of(
                "someField", "someValue",
                "meta", MetaFields.builder().setExternalKey("someKey").build()
        ));

        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var result = functionGeneratorHelper.invokeFunc(myFunc, RosettaModelObject.class, myInput);

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model.metafields"), "FieldWithMetaFoo", Map.of(
                "value", generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model"), "Foo", Map.of(
                        "someField", "someValue",
                        "meta", MetaFields.builder().setExternalKey("someKey").build()
                )),
                "meta", MetaFields.builder().setScheme("someScheme").build()
        ));

        assertEquals(expected, result);
    }

    @Test
    void canAddSchemeToExistingMetaObjectUsingWithMetaSyntax() {
        var model = """
                metaType key string
                metaType scheme string
                metaType location string
                
                type Foo:
                  [metadata key]
                   someField string (1..1)
                
                func MyFunc:
                	inputs:
                	    myInput Foo (1..1)
                	      [metadata location]
                
                    output:
                        result Foo (1..1)
                		  [metadata scheme]
                
                    set result: myInput with-meta {
                                                scheme: "someScheme"
                                            }
                """;

        var code = generatorTestHelper.generateCode(model);

        var classes = generatorTestHelper.compileToClasses(code);

        var myInput = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model.metafields"), "FieldWithMetaFoo", Map.of(
                "value", generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model"), "Foo", Map.of(
                        "someField", "someValue",
                        "meta", MetaFields.builder().setExternalKey("someKey").build()
                )),
                "meta", MetaFields.builder().setLocation("someLocation").build()
        ));

        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var result = functionGeneratorHelper.invokeFunc(myFunc, RosettaModelObject.class, myInput);

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model.metafields"), "FieldWithMetaFoo", Map.of(
                "value", generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model"), "Foo", Map.of(
                        "someField", "someValue",
                        "meta", MetaFields.builder().setExternalKey("someKey").build()
                )),
                "meta", MetaFields.builder().setScheme("someScheme").setLocation("someLocation").build()
        ));

        assertEquals(expected, result);
    }

    @Test
    void canSetMetaAdressAndReferenceUsingWithMetaSyntax() {
        var model = """
                metaType address string
                metaType reference string
                
                type Foo:
                   someField string (1..1)
                
                func MyFunc:
                    output:
                        result Foo (1..1)
                		  [metadata address]
                  		  [metadata reference]
                
                
                     alias foo: Foo {
                		someField: "someValue"
                     }
                
                    set result: foo with-meta {
                                                address: "someAddress",
                                                reference: "someReference"
                                            }
                """;

        var code = generatorTestHelper.generateCode(model);


        var classes = generatorTestHelper.compileToClasses(code);

        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var result = functionGeneratorHelper.invokeFunc(myFunc, RosettaModelObject.class);

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model.metafields"), "ReferenceWithMetaFoo", Map.of(
                "value", generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model"), "Foo", Map.of(
                        "someField", "someValue"
                )),
                "reference", Reference.builder().setReference("someAddress"),
                "externalReference", "someReference"
        ));

        assertEquals(expected, result);

    }

    @Test
    void canSetMetaAdressUsingWithMetaSyntax() {
        var model = """
                metaType address string
                
                type Foo:
                   someField string (1..1)
                
                func MyFunc:
                    output:
                        result Foo (1..1)
                		  [metadata address]
                
                     alias foo: Foo {
                		someField: "someValue"
                     }
                
                    set result: foo with-meta {
                                                address: "someAddress"
                                            }
                """;

        var code = generatorTestHelper.generateCode(model);


        var classes = generatorTestHelper.compileToClasses(code);

        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var result = functionGeneratorHelper.invokeFunc(myFunc, RosettaModelObject.class);

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model.metafields"), "ReferenceWithMetaFoo", Map.of(
                "value", generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model"), "Foo", Map.of(
                        "someField", "someValue"
                )),
                "reference", Reference.builder().setReference("someAddress")
        ));

        assertEquals(expected, result);

    }

    @Test
    void canSetMetaReferenceUsingWithMetaSyntax() {
        var model = """
                metaType reference string
                
                type Foo:
                   someField string (1..1)
                
                func MyFunc:
                    output:
                        result Foo (1..1)
                		  [metadata reference]
                
                     alias foo: Foo {
                		someField: "someValue"
                     }
                
                    set result: foo with-meta {
                                                reference: "someReference"
                                            }
                """;

        var code = generatorTestHelper.generateCode(model);


        var classes = generatorTestHelper.compileToClasses(code);

        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var result = functionGeneratorHelper.invokeFunc(myFunc, RosettaModelObject.class);

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model.metafields"), "ReferenceWithMetaFoo", Map.of(
                "value", generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model"), "Foo", Map.of(
                        "someField", "someValue"
                )),
                "externalReference", "someReference"
        ));

        assertEquals(expected, result);

    }

    @Test
    void canSetMetaKeyAndSchemeUsingWithMetaSyntax() {
        var model = """
                metaType key string
                metaType scheme string
                
                type Foo:
                  [metadata key]
                   someField string (1..1)
                
                func MyFunc:
                    output:
                        result Foo (1..1)
                		  [metadata scheme]
                
                     alias foo: Foo {
                		someField: "someValue"
                     }
                
                    set result: foo with-meta {
                                                key: "someKey",
                                                scheme: "someScheme"
                                            }
                """;

        var code = generatorTestHelper.generateCode(model);

        var classes = generatorTestHelper.compileToClasses(code);

        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var result = functionGeneratorHelper.invokeFunc(myFunc, RosettaModelObject.class);

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model.metafields"), "FieldWithMetaFoo", Map.of(
                "value", generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model"), "Foo", Map.of(
                        "someField", "someValue",
                        "meta", MetaFields.builder().setExternalKey("someKey").build()
                )),
                "meta", MetaFields.builder().setScheme("someScheme").build()
        ));

        assertEquals(expected, result);
    }

    @Test
    void canSetMetaKeyUsingWithMetaSyntax() {
        var model = """
                metaType key string
                metaType scheme string
                
                type Foo:
                  [metadata key]
                   someField string (1..1)
                
                func MyFunc:
                    output:
                        result Foo (1..1)
                
                     alias foo: Foo {
                		someField: "someValue"
                     }
                
                    set result: foo with-meta {
                                                key: "someKey"
                                            }
                """;

        var code = generatorTestHelper.generateCode(model);


        var classes = generatorTestHelper.compileToClasses(code);

        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var result = functionGeneratorHelper.invokeFunc(myFunc, RosettaModelObject.class);

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model"), "Foo", Map.of(
                "someField", "someValue",
                "meta", MetaFields.builder().setExternalKey("someKey").build()
        ));

        assertEquals(expected, result);
    }

    @Test
    void canSetMetaFieldsUsingWithMetaSyntax() {
        var model = """
                metaType id string
                metaType scheme string
                
                func MyFunc:
                    output:
                        result string (1..1)
                          [metadata scheme]
                          [metadata id]
                    set result: "someValue" with-meta {
                                                scheme: "someScheme",
                                                id: "someId"
                                            }
                """;

        var code = generatorTestHelper.generateCode(model);

        var classes = generatorTestHelper.compileToClasses(code);

        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var result = functionGeneratorHelper.invokeFunc(myFunc, RosettaModelObject.class);

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.model.metafields"), "FieldWithMetaString", Map.of(
                "value", "someValue",
                "meta", MetaFields.builder().setScheme("someScheme").setExternalKey("someId").build()

        ));

        assertEquals(expected, result);
    }

    @Test
    void canSetEnumOnReferenceWithMeta() {
        var model = """
                      metaType reference string
                
                      enum MyEnum:
                          A
                          B
                          C
                
                type Foo:
                    myEnumField MyEnum (1..1)
                     [metadata reference]
                
                func MyFunc:
                    output:
                        foo Foo (1..1)
                
                    set foo -> myEnumField: MyEnum -> B
                """;


        var code = generatorTestHelper.generateCode(model);

        var classes = generatorTestHelper.compileToClasses(code);

        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var result = functionGeneratorHelper.invokeFunc(myFunc, RosettaModelObject.class);

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model"), "Foo", Map.of(
                "myEnumField", generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model.metafields"), "ReferenceWithMetaMyEnum", Map.of(
                        "value", generatorTestHelper.createEnumInstance(classes, "MyEnum", "B")
                ))

        ));

        assertEquals(expected, result);
    }

    @Test
    void canSetEnumOnFieldWithMeta() {
        var model = """
                      enum MyEnum:
                          A
                          B
                          C
                
                type Foo:
                    myEnumField MyEnum (1..1)
                     [metadata scheme]
                
                func MyFunc:
                    output:
                        foo Foo (1..1)
                
                    set foo -> myEnumField: MyEnum -> B
                """;


        var code = generatorTestHelper.generateCode(model);

        var classes = generatorTestHelper.compileToClasses(code);

        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var result = functionGeneratorHelper.invokeFunc(myFunc, RosettaModelObject.class);

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model"), "Foo", Map.of(
                "myEnumField", generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model.metafields"), "FieldWithMetaMyEnum", Map.of(
                        "value", generatorTestHelper.createEnumInstance(classes, "MyEnum", "B")
                ))

        ));

        assertEquals(expected, result);
    }

    @Test
    void canSetSingleCardinalityMetaToListOfMetaUsingConstructor() {
        var model = """
              metaType reference string
              metaType key string
        
              type Foo:
                  barReferences Bar (0..*)
                  [metadata reference]
        
              type Bar:
                  [metadata key]
                  barField string (0..1)
        
              func MyFunc:
                  inputs:
                      bar Bar (0..1)
                  output:
                      foo Foo (0..1)
        
                  set foo: Foo {
                      barReferences: bar
                  }
              """;

        var code = generatorTestHelper.generateCode(model);

        var classes = generatorTestHelper.compileToClasses(code);

        var bar = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model"), "Bar", Map.of(
                "barField", "barFieldValue",
                "meta", MetaFields.builder().setExternalKey("someExternalKey").build()
        ));

        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var result = functionGeneratorHelper.invokeFunc(myFunc, RosettaModelObject.class, bar);

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model"), "Foo", Map.of(
                "barReferences", Lists.newArrayList(generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model.metafields"), "ReferenceWithMetaBar", Map.of(
                        "value", generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model"), "Bar", Map.of(
                                "barField", "barFieldValue",
                                "meta", MetaFields.builder().setExternalKey("someExternalKey").build()
                        ))

                )))

        ));

        assertEquals(expected, result);
    }

    @Test
    void canSetSingleCardinalityMetaToListOfMeta() {
        var model = """
                metaType reference string
                metaType key string
                
                type Foo:
                  barReferences Bar (0..*)
                  [metadata reference]
                
                type Bar:
                  [metadata key]
                  barField string (0..1)
                
                func MyFunc:
                  inputs:
                      bar Bar (0..1)
                  output:
                      foo Foo (0..1)
                
                  set foo -> barReferences: bar
                """;

        var code = generatorTestHelper.generateCode(model);

        var classes = generatorTestHelper.compileToClasses(code);

        var bar = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model"), "Bar", Map.of(
                "barField", "barFieldValue",
                "meta", MetaFields.builder().setExternalKey("someExternalKey").build()
        ));

        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var result = functionGeneratorHelper.invokeFunc(myFunc, RosettaModelObject.class, bar);

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model"), "Foo", Map.of(
                "barReferences", Lists.newArrayList(generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model.metafields"), "ReferenceWithMetaBar", Map.of(
                        "value", generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model"), "Bar", Map.of(
                                "barField", "barFieldValue",
                                "meta", MetaFields.builder().setExternalKey("someExternalKey").build()
                        ))

                )))

        ));

        assertEquals(expected, result);
    }

    @Test
    void canSetNestedCombinedFieldWithMetaUsingConstructor() {
        var model = """
                type Foo:
                   fooField string (1..1)
                       [metadata scheme]
                
                func MyFunc:
                    inputs:
                      myInput string (1..1)
                         [metadata scheme]
                
                    output:
                        result Foo (1..1)
                
                    set result:
                        Foo {
                          fooField: myInput
                        }
                """;

        var code = generatorTestHelper.generateCode(model);

        var classes = generatorTestHelper.compileToClasses(code);
        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var result = functionGeneratorHelper.invokeFunc(myFunc, RosettaModelObject.class, generatorTestHelper.createFieldWithMetaString(classes, "someValue", "someScheme"));

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model"), "Foo", Map.of(
                "fooField", generatorTestHelper.createFieldWithMetaString(classes, "someValue", "someScheme")
        ));

        assertEquals(expected, result);
    }

    @Test
    void canSetNestedCombinedFieldWithMeta() {
        var model = """
                type Foo:
                   fooField string (1..1)
                       [metadata scheme]
                
                func MyFunc:
                    inputs:
                      myInput string (1..1)
                         [metadata scheme]
                
                    output:
                        result Foo (1..1)
                
                    set result -> fooField: myInput
                """;

        var code = generatorTestHelper.generateCode(model);

        var classes = generatorTestHelper.compileToClasses(code);
        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var result = functionGeneratorHelper.invokeFunc(myFunc, RosettaModelObject.class, generatorTestHelper.createFieldWithMetaString(classes, "someValue", "someScheme"));

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model"), "Foo", Map.of(
                "fooField", generatorTestHelper.createFieldWithMetaString(classes, "someValue", "someScheme")
        ));

        assertEquals(expected, result);
    }

    @Test
    void canSetMetaOutputWhereInputArgumentIsNull() {
        var model = """
              func MyFunc:
                  inputs:
                      value string (0..1)
                      scheme string (0..1)
                  output:
                      result string (0..1)
                          [metadata scheme]
        
                  set result: value
                  set result -> scheme: scheme
              """;

        var code = generatorTestHelper.generateCode(model);

        var classes = generatorTestHelper.compileToClasses(code);
        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var result = functionGeneratorHelper.invokeFunc(myFunc, FieldWithMeta.class, null, null);

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.model.metafields"), "FieldWithMetaString", Map.of());

        assertEquals(expected, result);
    }

    @Test
    void canSetMetaLocationOnFunctionObjectOutput() {
        var model = """
             metaType location string
            
             type Foo:
                 field string (1..1)
            
             func MyFunc:
                 output:
                     result Foo (1..1)
                         [metadata location]
                 set result -> field:  "someValue"
                 set result -> location: "someAddress"
             """;

        var code = generatorTestHelper.generateCode(model);

        var classes = generatorTestHelper.compileToClasses(code);
        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var result = functionGeneratorHelper.invokeFunc(myFunc, FieldWithMeta.class);

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model.metafields"), "FieldWithMetaFoo", Map.of(
                "value", generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model"), "Foo", Map.of(
                        "field", "someValue"
                )),
                "meta", MetaFields.builder().setScopedKey("someAddress")
        ));

        assertEquals(expected, result);
    }

    @Test
    void canSetMetaLocationOnFunctionBasicOutput() {
        var model = """
             metaType location string
            
             func MyFunc:
                 output:
                     result string (1..1)
                         [metadata location]
                 set result:  "someValue"
                 set result -> location: "someAddress"
             """;

        var code = generatorTestHelper.generateCode(model);

        var classes = generatorTestHelper.compileToClasses(code);
        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var result = functionGeneratorHelper.invokeFunc(myFunc, FieldWithMeta.class);

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.model.metafields"), "FieldWithMetaString", Map.of(
                "value", "someValue",
                "meta", MetaFields.builder().setScopedKey("someAddress")
        ));

        assertEquals(expected, result);
    }

    @Test
    void canSetMetaAddressOnFunctionObjectOutput() {
        var model = """
             metaType address string
            
             type Foo:
                 field string (1..1)
            
             func MyFunc:
                 output:
                     result Foo (1..1)
                         [metadata address]
                 set result -> field:  "someValue"
                 set result -> address: "someLocation"
             """;

        var code = generatorTestHelper.generateCode(model);

        var classes = generatorTestHelper.compileToClasses(code);

        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var result = functionGeneratorHelper.invokeFunc(myFunc, FieldWithMeta.class);

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model.metafields"), "ReferenceWithMetaFoo", Map.of(
                "value", generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model"), "Foo", Map.of(
                        "field", "someValue"
                )),
                "reference", Reference.builder().setReference("someLocation")
        ));

        assertEquals(expected, result);
    }

    @Test
    void canSetAddressOnFunctionBasicOutput() {
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

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.model.metafields"), "ReferenceWithMetaString", Map.of(
                "value", "someValue",
                "reference", Reference.builder().setReference("someLocation")
        ));

        assertEquals(expected, result);
    }


    @Test
    void canSetExternalIdOnFunctionBasicOutput() {
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

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.model.metafields"), "FieldWithMetaString", Map.of(
                "value", "someValue",
                "meta", MetaFields.builder().setExternalKey("someExternalKey")
        ));

        assertEquals(expected, result);
    }

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
                    set result -> a: "someA"
                    set result -> key: myReference
                """;

        var code = generatorTestHelper.generateCode(model);

        var classes = generatorTestHelper.compileToClasses(code);
        var myFunc = functionGeneratorHelper.createFunc(classes, "MyFunc");

        var result = functionGeneratorHelper.invokeFunc(myFunc, RosettaModelObject.class, "someExternalReference");

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model"), "Foo", Map.of(
                "a", "someA",
                "meta", MetaFields.builder().setExternalKey("someExternalReference")
        ));

        assertEquals(expected, result);
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

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model.metafields"), "ReferenceWithMetaFoo", Map.of(
                "externalReference", "someExternalReference"
        ));

        assertEquals(expected, result);
    }

    @Test
    void canSetMetaOnFunctionObjectOutputAndNestedBasicMetaField() {
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

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model.metafields"), "FieldWithMetaFoo", Map.of(
                "value", generatorTestHelper.createInstanceUsingBuilder(classes, "Foo", Map.of(
                        "a", "someValueA",
                        "b", generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.model.metafields"), "FieldWithMetaString", Map.of(
                                "value", "someValueB",
                                "meta", MetaFields.builder().setScheme("innerScheme")
                        ))
                )),
                "meta", MetaFields.builder().setScheme("outerScheme")
        ));

        assertEquals(expected, result);
    }

    @Test
    void canSetSchemeOnFunctionObjectOutput() {
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

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model.metafields"), "FieldWithMetaFoo", Map.of(
                "value", generatorTestHelper.createInstanceUsingBuilder(classes, "Foo", Map.of(
                        "a", "someValueA",
                        "b", "someValueB"
                )),
                "meta", MetaFields.builder().setScheme("outerScheme")
        ));

        assertEquals(expected, result);
    }

    @Test
    void canSetSchemeOnFunctionBasicOutput() {
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

        var result = functionGeneratorHelper.invokeFunc(myFunc, FieldWithMeta.class);

        var expected = generatorTestHelper.createFieldWithMetaString(classes, "someValue", "someScheme");

        assertEquals(expected, result);
    }
}