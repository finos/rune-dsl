package com.regnosys.rosetta.generator.java.function;

import com.google.common.collect.ImmutableList;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper;
import com.regnosys.rosetta.tests.util.ModelHelper;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.meta.Key;
import com.rosetta.model.lib.meta.Reference;
import com.rosetta.model.lib.records.Date;
import com.rosetta.model.metafields.MetaFields;
import com.rosetta.util.DottedPath;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.EQUALITY_OPERATION;
import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.LOGICAL_OPERATION;
import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.ROSETTA_DISJOINT_EXPRESSION;
import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.ROSETTA_ONLY_ELEMENT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class FunctionGeneratorTest {

    @Inject
    private FunctionGeneratorHelper functionGeneratorHelper;
    @Inject
    private CodeGeneratorTestHelper generatorTestHelper;
    @Inject
    private ModelHelper modelHelper;
    @Inject
    private ValidationTestHelper validationTestHelper;

    private static final DottedPath METAFIELDS = DottedPath.splitOnDots("com.rosetta.model.metafields");
    private static final DottedPath TEST_METAFIELDS = DottedPath.splitOnDots("com.rosetta.test.model.metafields");

    @Test
    void reportingRuleSupportsRecursion() {
        var code = generatorTestHelper.generateCode("""
                reporting rule Fac from int:
                	if item = 1
                	then 1
                	else item * Fac(item - 1)
                """);

        generatorTestHelper.compileToClasses(code);
        var classes = generatorTestHelper.compileToClasses(code);

        var facRule = functionGeneratorHelper.createFunc(classes, "FacRule", modelHelper.rootPackage().child("reports"));

        assertEquals(120, functionGeneratorHelper.invokeFunc(facRule, Integer.class, 5));
    }

    @Test
    void testCanPassMetaFromOutputOfFunctionCall() {
        var code = generatorTestHelper.generateCode("""
                func A:
                	inputs:
                		myInput string (1..1)
                			[metadata scheme]
                	output:
                	result string (1..1)
                		[metadata scheme]
                     set result: myInput

                func B:
                	inputs:
                		myInput string (1..1)
                			[metadata scheme]
                	output:
                	result string (1..1)
                	set result:
                	A(myInput) -> scheme
                """);

        generatorTestHelper.compileToClasses(code);
        var classes = generatorTestHelper.compileToClasses(code);

        var funcB = functionGeneratorHelper.createFunc(classes, "B");

        var myInput = generatorTestHelper.createFieldWithMetaString(classes, "myValue", "myScheme");

        var result = functionGeneratorHelper.invokeFunc(funcB, String.class, myInput);

        assertEquals("myScheme", result);
    }

    @Test
    void testIgnoreMetaOnChoiceTypes() {
        var code = generatorTestHelper.generateCode("""
                type Foo:
                  a string (1..1)
                  b int (0..1)
                  c number (0..1)

                func Test:
                  inputs:
                    foo Foo (1..1)
                      [metadata scheme]
                  output:
                    result boolean (1..1)
                  set result:
                    foo required choice b, c
                """);

        var classes = generatorTestHelper.compileToClasses(code);

        var test = functionGeneratorHelper.createFunc(classes, "Test");

        var myInput = generatorTestHelper.createInstanceUsingBuilder(classes, TEST_METAFIELDS, "FieldWithMetaFoo", Map.of(
                "value", generatorTestHelper.createInstanceUsingBuilder(classes, "Foo", Map.of(
                        "a", "aValue",
                        "c", BigDecimal.valueOf(20)
                )),
                "meta", MetaFields.builder().setScheme("myScheme")
        ));

        var result = functionGeneratorHelper.invokeFunc(test, Boolean.class, myInput);
        assertTrue(result);
    }

    @Test
    void testSortFunctionsOnMetaItemsInInput() {
        var code = generatorTestHelper.generateCode("""
                func Test:
                	inputs:
                		myInputs string (1..*)
                		[metadata scheme]
                	output:
                		result string (1..*)

                	set result: myInputs sort
                """);

        var classes = generatorTestHelper.compileToClasses(code);

        var test = functionGeneratorHelper.createFunc(classes, "Test");

        var myInputs = List.of(
                generatorTestHelper.createFieldWithMetaString(classes, "DDDD", "myScheme"),
                generatorTestHelper.createFieldWithMetaString(classes, "AAAA", "myScheme"),
                generatorTestHelper.createFieldWithMetaString(classes, "HHHH", "myScheme")
        );

        var result = functionGeneratorHelper.invokeFunc(test, List.class, myInputs);

        assertEquals(List.of("AAAA", "DDDD", "HHHH"), result);
    }

    @Test
    void testSumOnMetaIntegers() {
        var code = generatorTestHelper.generateCode("""
                func Test:
                  inputs:
                    n int (0..*)
                      [metadata scheme]
                  output:
                    result int (1..1)
                  set result:
                    n sum
                """);

        var classes = generatorTestHelper.compileToClasses(code);

        var test = functionGeneratorHelper.createFunc(classes, "Test");

        var myInputs = List.of(
                generatorTestHelper.createInstanceUsingBuilder(classes, METAFIELDS, "FieldWithMetaInteger", Map.of(
                        "value", 6,
                        "meta", MetaFields.builder().setScheme("myScheme")
                )),
                generatorTestHelper.createInstanceUsingBuilder(classes, METAFIELDS, "FieldWithMetaInteger", Map.of(
                        "value", 5,
                        "meta", MetaFields.builder().setScheme("myScheme")
                ))
        );

        var result = functionGeneratorHelper.invokeFunc(test, Integer.class, myInputs);

        assertEquals(11, result);
    }

    @Test
    void testMaxFunctionsOnMetaItemsInInput() {
        var code = generatorTestHelper.generateCode("""
                func Test:
                	inputs:
                		myInputs string (1..*)
                		[metadata scheme]
                	output:
                		result string (1..1)

                	set result: myInputs max
                """);

        var classes = generatorTestHelper.compileToClasses(code);

        var test = functionGeneratorHelper.createFunc(classes, "Test");

        var myInputs = List.of(
                generatorTestHelper.createFieldWithMetaString(classes, "AAAA", "myScheme"),
                generatorTestHelper.createFieldWithMetaString(classes, "BBBB", "myScheme")
        );

        var result = functionGeneratorHelper.invokeFunc(test, String.class, myInputs);

        assertEquals("BBBB", result);
    }

    @Test
    void testMinFunctionsOnMetaItemsInInput() {
        var code = generatorTestHelper.generateCode("""
                func Test:
                	inputs:
                		myInputs string (1..*)
                		[metadata scheme]
                	output:
                		result string (1..1)

                	set result: myInputs min
                """);

        var classes = generatorTestHelper.compileToClasses(code);

        var test = functionGeneratorHelper.createFunc(classes, "Test");

        var myInputs = List.of(
                generatorTestHelper.createFieldWithMetaString(classes, "AAAA", "myScheme"),
                generatorTestHelper.createFieldWithMetaString(classes, "BBBB", "myScheme")
        );

        var result = functionGeneratorHelper.invokeFunc(test, String.class, myInputs);

        assertEquals("AAAA", result);
    }

    @Test
    void testToStringOnEnumWithMeta() {
        var code = generatorTestHelper.generateCode("""
                enum MyEnum:
                	A
                	B
                	C

                type Foo:
                	myEnum MyEnum (1..1)
                	[metadata scheme]

                func Test:
                	inputs:
                		myInput Foo (1..1)

                	output:
                		result string (1..1)

                	set result: myInput -> myEnum to-string
                """);

        var classes = generatorTestHelper.compileToClasses(code);

        var test = functionGeneratorHelper.createFunc(classes, "Test");

        var myInput = generatorTestHelper.createInstanceUsingBuilder(classes, "Foo", Map.of(
                "myEnum", generatorTestHelper.createInstanceUsingBuilder(classes, TEST_METAFIELDS, "FieldWithMetaMyEnum", Map.of(
                        "value", generatorTestHelper.createEnumInstance(classes, "MyEnum", "B"),
                        "meta", MetaFields.builder().setScheme("myScheme")
                ))
        ));

        var result = functionGeneratorHelper.invokeFunc(test, String.class, myInput);

        assertEquals("B", result);
    }

    @Test
    void testSettingMetaOnOutputWithReferenceAsKey() {
        var code = generatorTestHelper.generateCode("""
                type Foo:
                	[metadata key]

                type Bar:
                	b Foo (1..1)
                	[metadata reference]

                func Test:
                	inputs:
                		myInput Foo (1..1)
                		[metadata reference]
                	output:
                		result Bar (1..1)

                	set result -> b: myInput as-key
                """);

        var classes = generatorTestHelper.compileToClasses(code);

        var test = functionGeneratorHelper.createFunc(classes, "Test");

        var myInput = generatorTestHelper.createInstanceUsingBuilder(classes, TEST_METAFIELDS, "ReferenceWithMetaFoo", Map.of(
                "value", generatorTestHelper.createInstanceUsingBuilder(classes, "Foo", Map.of(
                        "meta", MetaFields.builder().setExternalKey("myExternalKey").setGlobalKey("myGlobalKey")
                ))
        ));

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", Map.of(
                "b", generatorTestHelper.createInstanceUsingBuilder(classes, TEST_METAFIELDS, "ReferenceWithMetaFoo", Map.of(
                        "externalReference", "myExternalKey",
                        "globalReference", "myGlobalKey"
                ))
        ));

        var result = functionGeneratorHelper.invokeFunc(test, RosettaModelObject.class, myInput);

        assertEquals(expected, result);
    }

    @Test
    void testSettingMetaOnOutput() {
        var code = generatorTestHelper.generateCode("""
                type Bar:
                	b string (1..1)
                	[metadata scheme]

                func Test:
                	inputs:
                		myInput string (1..1)
                		[metadata scheme]
                	output:
                		result Bar (1..1)

                	set result -> b: myInput
                """);

        var classes = generatorTestHelper.compileToClasses(code);

        var test = functionGeneratorHelper.createFunc(classes, "Test");

        var myInput = generatorTestHelper.createInstanceUsingBuilder(classes, METAFIELDS, "FieldWithMetaString", Map.of(
                "value", "someInput",
                "meta", MetaFields.builder().setScheme("myScheme")
        ));

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", Map.of(
                "b", generatorTestHelper.createInstanceUsingBuilder(classes, METAFIELDS, "FieldWithMetaString", Map.of(
                        "value", "someInput",
                        "meta", MetaFields.builder().setScheme("myScheme")
                ))
        ));

        var result = functionGeneratorHelper.invokeFunc(test, RosettaModelObject.class, myInput);

        assertEquals(expected, result);
    }

    @Test
    void testTransitivilyPassingMetaReference() {
        var code = generatorTestHelper.generateCode("""
                type FooContainer:
                	foo Foo (1..1)
                	[metadata reference]

                type Foo:
                	[metadata key]

                type Bar:
                	b Foo (1..1)
                	[metadata reference]

                func Test:
                	inputs:
                		myInput FooContainer (1..1)

                	output:
                		result Bar (1..1)

                	set result: Bar {
                		b: myInput -> foo as-key
                	}
                """);

        var classes = generatorTestHelper.compileToClasses(code);

        var test = functionGeneratorHelper.createFunc(classes, "Test");

        var myInput = generatorTestHelper.createInstanceUsingBuilder(classes, "FooContainer", Map.of(
                "foo", generatorTestHelper.createInstanceUsingBuilder(classes, TEST_METAFIELDS, "ReferenceWithMetaFoo", Map.of(
                        "value", generatorTestHelper.createInstanceUsingBuilder(classes, "Foo", Map.of(
                                "meta", MetaFields.builder().setExternalKey("myExternalKey").setGlobalKey("myGlobalKey")
                        ))
                ))
        ));

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", Map.of(
                "b", generatorTestHelper.createInstanceUsingBuilder(classes, TEST_METAFIELDS, "ReferenceWithMetaFoo", Map.of(
                        "externalReference", "myExternalKey",
                        "globalReference", "myGlobalKey"
                ))
        ));

        var result = functionGeneratorHelper.invokeFunc(test, RosettaModelObject.class, myInput);

        assertEquals(expected, result);
    }

    @Test
    void testPassingMetaItemToConstructorWithReferenceAsKey() {
        var code = generatorTestHelper.generateCode("""
                type Foo:
                	[metadata key]

                type Bar:
                	b Foo (1..1)
                	[metadata reference]

                func Test:
                	inputs:
                		myInput Foo (1..1)
                		[metadata reference]
                	output:
                		result Bar (1..1)

                	set result: Bar {
                		b: myInput as-key
                	}
                """);

        var classes = generatorTestHelper.compileToClasses(code);

        var test = functionGeneratorHelper.createFunc(classes, "Test");

        var myInput = generatorTestHelper.createInstanceUsingBuilder(classes, TEST_METAFIELDS, "ReferenceWithMetaFoo", Map.of(
                "value", generatorTestHelper.createInstanceUsingBuilder(classes, "Foo", Map.of(
                        "meta", MetaFields.builder().setExternalKey("myExternalKey").setGlobalKey("myGlobalKey")
                ))
        ));

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", Map.of(
                "b", generatorTestHelper.createInstanceUsingBuilder(classes, TEST_METAFIELDS, "ReferenceWithMetaFoo", Map.of(
                        "externalReference", "myExternalKey",
                        "globalReference", "myGlobalKey"
                ))
        ));

        var result = functionGeneratorHelper.invokeFunc(test, RosettaModelObject.class, myInput);

        assertEquals(expected, result);
    }

    @Test
    void testPassingMetaItemToConstructor() {
        var code = generatorTestHelper.generateCode("""
                type Foo:
                	a string (1..1)
                	[metadata scheme]

                func Test:
                	inputs:
                		myInput string (1..1)
                		[metadata scheme]
                	output:
                		result Foo (1..1)

                	set result: Foo {
                		a: myInput
                	}
                """);

        var classes = generatorTestHelper.compileToClasses(code);

        var test = functionGeneratorHelper.createFunc(classes, "Test");

        var myInput = generatorTestHelper.createFieldWithMetaString(classes, "someInput", "myScheme");

        var expected = generatorTestHelper.createInstanceUsingBuilder(classes, "Foo", Map.of(
                "a", generatorTestHelper.createFieldWithMetaString(classes, "someInput", "myScheme")
        ));

        var result = functionGeneratorHelper.invokeFunc(test, RosettaModelObject.class, myInput);

        assertEquals(expected, result);
    }

    @Test
    void testCompareItemWithMetaToItemWithMeta() {
        var code = generatorTestHelper.generateCode("""
                func Test:
                	inputs:
                		a string (1..1)
                		[metadata scheme]
                		b string (1..1)
                		[metadata scheme]
                	output:
                		result boolean (1..1)
                	set result: a = b
                """);

        var classes = generatorTestHelper.compileToClasses(code);

        var test = functionGeneratorHelper.createFunc(classes, "Test");

        var a = generatorTestHelper.createFieldWithMetaString(classes, "foo", "myScheme");

        var b = generatorTestHelper.createFieldWithMetaString(classes, "foo", "myScheme");

        assertTrue(functionGeneratorHelper.invokeFunc(test, Boolean.class, a, b));
    }

    @Test
    void testCompareItemToItemWithMeta() {
        var code = generatorTestHelper.generateCode("""
                func Test:
                	inputs:
                		a string (1..1)
                		b string (1..1)
                		[metadata scheme]
                	output:
                		result boolean (1..1)
                	set result: a = b
                """);

        var classes = generatorTestHelper.compileToClasses(code);

        var test = functionGeneratorHelper.createFunc(classes, "Test");

        var a = "foo";
        var b = generatorTestHelper.createFieldWithMetaString(classes, "foo", "myScheme");

        assertTrue(functionGeneratorHelper.invokeFunc(test, Boolean.class, a, b));
    }

    @Test
    void testCompareItemWithMetaToItemWithReference() {
        var code = generatorTestHelper.generateCode("""
                func Test:
                	inputs:
                		a string (1..1)
                		[metadata scheme]
                		b string (1..1)
                		[metadata reference]
                	output:
                		result boolean (1..1)
                	set result: a = b
                """);

        var classes = generatorTestHelper.compileToClasses(code);

        var test = functionGeneratorHelper.createFunc(classes, "Test");

        var a = generatorTestHelper.createFieldWithMetaString(classes, "foo", "myScheme");
        var b = generatorTestHelper.createInstanceUsingBuilder(classes, METAFIELDS, "ReferenceWithMetaString", Map.of(
                "value", "foo",
                "reference", Reference.builder().setReference("myRef").build()
        ));

        assertTrue(functionGeneratorHelper.invokeFunc(test, Boolean.class, a, b));
    }

    @Test
    void testDeepFeatureCallWithMeta() {
        var code = generatorTestHelper.generateCode("""
                choice Foo:
                    A
                    B

                type A:
                	[metadata key]
                    attr int (1..1)

                type B:
                	[metadata key]
                    attr int (1..1)

                func Test:
                    inputs:
                        fooWithReference Foo (1..1)
                            [metadata reference]
                    output:
                        result int (1..1)
                    set result:
                        fooWithReference ->> attr
                """);

        var classes = generatorTestHelper.compileToClasses(code);

        var test = functionGeneratorHelper.createFunc(classes, "Test");

        var fooWithReference = generatorTestHelper.createInstanceUsingBuilder(classes, TEST_METAFIELDS, "ReferenceWithMetaFoo", Map.of(
                "value", generatorTestHelper.createInstanceUsingBuilder(classes, "Foo", Map.of(
                        "a", generatorTestHelper.createInstanceUsingBuilder(classes, "A", Map.of(
                                "attr", 99
                        ))
                )),
                "reference", Reference.builder().setReference("myRef").build()
        ));

        assertEquals(99, functionGeneratorHelper.invokeFunc(test, Integer.class, fooWithReference));
    }

    @Test
    void canHandleMetaCoecrion() {
        var code = generatorTestHelper.generateCode("""
                metaType reference string

                func SomeFunc:
                	inputs:
                		myInput int (0..*)
                		[metadata reference]

                	output:
                		myResult number (0..*)
                		[metadata scheme]

                	set myResult: myInput
                """);

        var classes = generatorTestHelper.compileToClasses(code);
        var someFunc = functionGeneratorHelper.createFunc(classes, "SomeFunc");

        var myInput = List.of(
                generatorTestHelper.createInstanceUsingBuilder(classes, METAFIELDS, "ReferenceWithMetaInteger", Map.of(
                        "value", 5,
                        "reference", Reference.builder().setReference("myRef").build()
                )),
                generatorTestHelper.createInstanceUsingBuilder(classes, METAFIELDS, "ReferenceWithMetaInteger", Map.of(
                        "value", 10,
                        "reference", Reference.builder().setReference("myRef2").build()
                )),
                generatorTestHelper.createInstanceUsingBuilder(classes, METAFIELDS, "ReferenceWithMetaInteger", Map.of(
                        "value", 15,
                        "reference", Reference.builder().setReference("myRef3").build()
                ))
        );

        var expected = List.of(
                generatorTestHelper.createInstanceUsingBuilder(classes, METAFIELDS, "FieldWithMetaBigDecimal", Map.of(
                        "value", BigDecimal.valueOf(5)
                )),
                generatorTestHelper.createInstanceUsingBuilder(classes, METAFIELDS, "FieldWithMetaBigDecimal", Map.of(
                        "value", BigDecimal.valueOf(10)
                )),
                generatorTestHelper.createInstanceUsingBuilder(classes, METAFIELDS, "FieldWithMetaBigDecimal", Map.of(
                        "value", BigDecimal.valueOf(15)
                ))
        );

        var result = functionGeneratorHelper.invokeFunc(someFunc, List.class, myInput);

        assertEquals(expected, result);
    }

    @Test
    void canPassMetadataToFunctionAndUseInExpression() {
        var code = generatorTestHelper.generateCode("""
                func SomeFunc:
                    inputs:
                        myInput string (1..1)
                        [metadata scheme]
                    output:
                        myResult string (1..1)

                    set myResult: myInput + myInput -> scheme
                """);

        var classes = generatorTestHelper.compileToClasses(code);
        var someFunc = functionGeneratorHelper.createFunc(classes, "SomeFunc");

        var myInput = generatorTestHelper.createFieldWithMetaString(classes, "myInputValue", "myScheme");

        var result = functionGeneratorHelper.invokeFunc(someFunc, String.class, myInput);
        assertEquals("myInputValuemyScheme", result);
    }

    @Test
    void canSetFunctionWithMetaOutput() {
        var code = generatorTestHelper.generateCode("""
                func SomeFunc:
                    inputs:
                        myInput string (1..1)
                        [metadata scheme]
                    output:
                        myResult string (1..1)
                        [metadata scheme]

                    set myResult: myInput
                """);

        var classes = generatorTestHelper.compileToClasses(code);
        var someFunc = functionGeneratorHelper.createFunc(classes, "SomeFunc");

        var myInput = generatorTestHelper.createFieldWithMetaString(classes, "myInputValue", "myScheme");

        var result = functionGeneratorHelper.invokeFunc(someFunc, RosettaModelObject.class, myInput);
        assertEquals(myInput, result);
    }

    @Test
    void canPassMetadataToFunctions() {
        var code = generatorTestHelper.generateCode("""
                func SomeFunc:
                    inputs:
                        myInput string (1..1)
                        [metadata scheme]
                    output:
                        myResult string (1..1)

                    set myResult: myInput -> scheme
                """);

        var classes = generatorTestHelper.compileToClasses(code);
        var someFunc = functionGeneratorHelper.createFunc(classes, "SomeFunc");

        var myInput = generatorTestHelper.createFieldWithMetaString(classes, "myInputValue", "myScheme");

        var result = functionGeneratorHelper.invokeFunc(someFunc, String.class, myInput);
        assertEquals("myScheme", result);
    }

    @Test
    void assignToMultiMetaFeature() {
        var code = generatorTestHelper.generateCode("""
                type A:
                    a string (0..*)
                	[metadata reference]

                func Test:
                	output:
                		result A (1..1)

                	add result -> a:
                		"Hello"
                """);

        var classes = generatorTestHelper.compileToClasses(code);
        var a = generatorTestHelper.createInstanceUsingBuilder(classes, "A", Map.of(
                "a", List.of(generatorTestHelper.createInstanceUsingBuilder(classes, METAFIELDS, "ReferenceWithMetaString", Map.of(
                        "value", "Hello"
                )))
        ));

        var testOnlyExists = functionGeneratorHelper.createFunc(classes, "Test");
        assertEquals(a, functionGeneratorHelper.invokeFunc(testOnlyExists, a.getClass()));
    }

    @Test
    void onlyExistsOnAbsentParent() {
        var code = generatorTestHelper.generateCode("""
                type A:
                    a1 string (0..1)
                    a2 string (0..1)

                func TestOnlyExists:
                	inputs:
                		a A (1..1)
                	output:
                		result boolean (1..1)

                	set result:
                		a -> a1 only exists
                """);

        var classes = generatorTestHelper.compileToClasses(code);

        var testOnlyExists = functionGeneratorHelper.createFunc(classes, "TestOnlyExists");
        assertFalse(functionGeneratorHelper.invokeFunc(testOnlyExists, Boolean.class, new Object[] {null}));
    }

    @Test
    void onlyExistsAndOneOfWorkOnStaticType() {
        var code = generatorTestHelper.generateCode("""
                type A:
                    a1 string (0..1)
                    a2 string (0..1)
                    a3 boolean (0..1)

                type B extends A:
                    b1 string (0..1)

                func TestOnlyExists:
                	inputs:
                		a A (1..1)
                	output:
                		result boolean (1..1)

                	set result:
                		a -> a1 only exists

                func TestOneOf:
                	inputs:
                		a A (1..1)
                	output:
                		result boolean (1..1)

                	set result:
                		a one-of
                """);

        var classes = generatorTestHelper.compileToClasses(code);

        var b1 = generatorTestHelper.createInstanceUsingBuilder(classes, "B", Map.of(
                "a1", "some value",
                "b1", "other value"
        ));
        var b2 = generatorTestHelper.createInstanceUsingBuilder(classes, "B", Map.of(
                "b1", "other value"
        ));

        var testOnlyExists = functionGeneratorHelper.createFunc(classes, "TestOnlyExists");
        assertTrue(functionGeneratorHelper.invokeFunc(testOnlyExists, Boolean.class, b1));
        assertFalse(functionGeneratorHelper.invokeFunc(testOnlyExists, Boolean.class, b2));

        var testOneOf = functionGeneratorHelper.createFunc(classes, "TestOneOf");
        assertTrue(functionGeneratorHelper.invokeFunc(testOneOf, Boolean.class, b1));
        assertFalse(functionGeneratorHelper.invokeFunc(testOneOf, Boolean.class, b2));
    }

    @Test
    void testDeepPathOperatorWithMeta() {
        var code = generatorTestHelper.generateCode("""
                choice A:
                	B
                		[metadata reference]
                	C
                		[metadata reference]

                type B:
                	[metadata key]
                	id string (1..1)
                		[metadata scheme]

                type C:
                	[metadata key]
                	id string (1..1)
                		[metadata scheme]

                func Test:
                	inputs:
                		a A (1..1)
                	output:
                		result string (1..1)

                	set result:
                		a ->> id -> scheme
                """);

        var classes = generatorTestHelper.compileToClasses(code);

        var test = functionGeneratorHelper.createFunc(classes, "Test");
        var aB = generatorTestHelper.createInstanceUsingBuilder(classes, "A", Map.of(
                "B", generatorTestHelper.createInstanceUsingBuilder(classes, TEST_METAFIELDS, "ReferenceWithMetaB", Map.of(
                        "value", generatorTestHelper.createInstanceUsingBuilder(classes, "B", Map.of(
                                "meta", MetaFields.builder().setKey(List.of(Key.builder().setKeyValue("myKey"))),
                                "id", generatorTestHelper.createInstanceUsingBuilder(classes, METAFIELDS, "FieldWithMetaString", Map.of(
                                        "meta", MetaFields.builder().setScheme("myScheme"),
                                        "value", "abc123"
                                ))
                        )),
                        "globalReference", "globalRef",
                        "externalReference", "externalRef"
                ))
        ));

        assertEquals("myScheme", functionGeneratorHelper.invokeFunc(test, String.class, aB));
    }

    @Test
    void testDeepPathOperatorWithMultiMeta() {
        var code = generatorTestHelper.generateCode("""
                choice A:
                	B
                	C

                type ABase:
                	prop int (0..*)
                		[metadata scheme]

                type B extends ABase:

                type C extends ABase:

                func Test:
                	inputs:
                		a A (1..1)
                	output:
                		result int (0..*)

                	set result:
                		a ->> prop
                """);

        var classes = generatorTestHelper.compileToClasses(code);

        var test = functionGeneratorHelper.createFunc(classes, "Test");
        var aB = generatorTestHelper.createInstanceUsingBuilder(classes, "A", Map.of(
                "B", generatorTestHelper.createInstanceUsingBuilder(classes, "B", Map.of(
                        "prop", List.of(
                                generatorTestHelper.createInstanceUsingBuilder(classes, METAFIELDS, "FieldWithMetaInteger", Map.of(
                                        "meta", MetaFields.builder().setScheme("myScheme"),
                                        "value", 42
                                )),
                                generatorTestHelper.createInstanceUsingBuilder(classes, METAFIELDS, "FieldWithMetaInteger", Map.of(
                                        "meta", MetaFields.builder().setScheme("otherScheme"),
                                        "value", 0
                                ))
                        )
                ))
        ));

        assertEquals(List.of(42, 0), functionGeneratorHelper.invokeFunc(test, String.class, aB));
    }

    @Test
    void testDeepPathOperator() {
        var code = generatorTestHelper.generateCode("""
                choice A:
                	B
                	C

                type B:
                	opt1 Option1 (0..1)
                	opt2 Option2 (0..1)
                	attr Foo (0..1)

                	condition Choice: one-of

                type C:
                	opt1 Option1 (0..1)

                	condition Choice: one-of

                type Option1:
                	attr Foo (1..1)

                type Option2:
                	attr Foo (1..1)
                	otherAttr string (1..1)

                type Option3:
                	attr Foo (1..1)

                type Foo:
                	id string (1..1)

                func Test:
                	inputs:
                		a A (1..1)
                		b B (1..1)
                		aList A (0..*)
                	output:
                		result Foo (0..*)

                	add result:
                		a ->> attr
                	add result:
                		a ->> opt1 -> attr
                	add result:
                		b ->> attr
                	add result:
                		aList ->> attr
                	add result:
                		aList ->> opt1 -> attr
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var test = functionGeneratorHelper.createFunc(classes, "Test");

        var foo1 = generatorTestHelper.createInstanceUsingBuilder(classes, "Foo", Map.of(
                "id", "aBOpt1"
        ));
        var bOpt1 = generatorTestHelper.createInstanceUsingBuilder(classes, "B", Map.of(
                "opt1", generatorTestHelper.createInstanceUsingBuilder(classes, "Option1", Map.of(
                        "attr", foo1
                ))
        ));
        var aBOpt1 = generatorTestHelper.createInstanceUsingBuilder(classes, "A", Map.of(
                "B", bOpt1
        ));
        var foo2 = generatorTestHelper.createInstanceUsingBuilder(classes, "Foo", Map.of(
                "id", "aBOpt2"
        ));
        var bOpt2 = generatorTestHelper.createInstanceUsingBuilder(classes, "B", Map.of(
                "opt2", generatorTestHelper.createInstanceUsingBuilder(classes, "Option2", Map.of(
                        "attr", foo2,
                        "otherAttr", "some value"
                ))
        ));
        var aBOpt2 = generatorTestHelper.createInstanceUsingBuilder(classes, "A", Map.of(
                "B", bOpt2
        ));
        var foo3 = generatorTestHelper.createInstanceUsingBuilder(classes, "Foo", Map.of(
                "id", "aBAttr"
        ));
        var bAttr = generatorTestHelper.createInstanceUsingBuilder(classes, "B", Map.of(
                "attr", foo3
        ));
        var aBAttr = generatorTestHelper.createInstanceUsingBuilder(classes, "A", Map.of(
                "B", bAttr
        ));
        var foo4 = generatorTestHelper.createInstanceUsingBuilder(classes, "Foo", Map.of(
                "id", "aCOpt1"
        ));
        var aCOpt1 = generatorTestHelper.createInstanceUsingBuilder(classes, "A", Map.of(
                "C", generatorTestHelper.createInstanceUsingBuilder(classes, "C", Map.of(
                        "opt1", generatorTestHelper.createInstanceUsingBuilder(classes, "Option1", Map.of(
                                "attr", foo4
                        ))
                ))
        ));

        assertEquals(
                List.of(foo1, foo1, foo2, foo4, foo3, foo2, foo4),
                functionGeneratorHelper.invokeFunc(test, List.class, aBOpt1, bOpt2, List.of(aCOpt1, aBAttr, aBOpt2))
        );
    }

    //TODO: remove this when deep path on choice has been set back to error
    @Test
    void testChoiceAttributeAccess() {
        var code = generatorTestHelper.generateCode("""
                type A:
                	b B (1..1)

                type B:
                	val boolean (0..1)

                choice AB:
                	A
                	B

                func Foo:
                	inputs:
                		ab AB (1..1)
                	output:
                		result boolean (1..1)

                	set result:
                		if ab -> A exists
                		then ab -> A -> b -> val
                		else if ab -> B exists
                		then ab -> B -> val
                """);
        generatorTestHelper.compileToClasses(code);
    }

    @Test
    void handlesNullWhenConstructingRecords() {
        var code = generatorTestHelper.generateCode("""
                func Foo:
                    inputs:
                        date date (0..1)
                        time time (0..1)
                        zone string (0..1)
                    output: result zonedDateTime (0..1)
                    set result:
                        zonedDateTime {
                            date: date,
                            time: time,
                            timezone: zone
                        }

                func Bar:
                    inputs:
                        day int (0..1)
                    output: result date (0..1)
                    set result:
                        date {
                            day: day,
                            year: 2024,
                            month: 2
                        }
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var foo = functionGeneratorHelper.createFunc(classes, "Foo");

        var date = Date.of(2024, 2, 26);
        var time = LocalTime.of(11, 10);
        var zone = "Europe/Paris";
        var zdt = ZonedDateTime.of(date.toLocalDate(), time, ZoneId.of(zone));
        assertEquals(zdt, functionGeneratorHelper.invokeFunc(foo, ZonedDateTime.class, date, time, zone));
        assertEquals(null, functionGeneratorHelper.invokeFunc(foo, ZonedDateTime.class, null, time, zone));
        assertEquals(null, functionGeneratorHelper.invokeFunc(foo, ZonedDateTime.class, date, null, zone));
        assertEquals(null, functionGeneratorHelper.invokeFunc(foo, ZonedDateTime.class, date, time, null));

        var bar = functionGeneratorHelper.createFunc(classes, "Bar");

        assertEquals(Date.of(2024, 2, 26), functionGeneratorHelper.invokeFunc(bar, Date.class, 26));
        assertEquals(null, functionGeneratorHelper.invokeFunc(bar, Date.class, new Object[] {null}));
    }

    @Test
    void canEscapeIdentifiers() {
        var code = generatorTestHelper.generateCode("""
                func Foo:
                	inputs: ^func int (1..1)
                	output: result int (1..1)
                	set result:
                		^func
                """);

        var funcCode = code.get("com.rosetta.test.model.functions.Foo");
        assertJavaEquals(
                """
				package com.rosetta.test.model.functions;

				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.RosettaFunction;


				@ImplementedBy(Foo.FooDefault.class)
				public abstract class Foo implements RosettaFunction {

					/**
					* @param func\s
					* @return result\s
					*/
					public Integer evaluate(Integer func) {
						Integer result = doEvaluate(func);
				\t\t
						return result;
					}

					protected abstract Integer doEvaluate(Integer func);

					public static class FooDefault extends Foo {
						@Override
						protected Integer doEvaluate(Integer func) {
							Integer result = null;
							return assignOutput(result, func);
						}
				\t\t
						protected Integer assignOutput(Integer result, Integer func) {
							result = func;
				\t\t\t
							return result;
						}
					}
				}
				""",
                funcCode
        );

        generatorTestHelper.compileToClasses(code);
    }

    @Test
    void canReturnEmptyInsideExtract() {
        var code = generatorTestHelper.generateCode("""
                func Foo:
                	output:
                		result int (0..1)
                	set result:
                		42 extract
                		if item > 0
                		then empty
                		else item
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var foo = functionGeneratorHelper.createFunc(classes, "Foo");
        assertEquals(null, functionGeneratorHelper.invokeFunc(foo, Integer.class));
    }

    @Test
    void canConstructTypeWithEmptyValue() {
        var code = generatorTestHelper.generateCode("""
                type A:
                	prop1 int (0..1)
                	prop2 int (0..*)
                	prop3 A (0..1)
                	prop4 A (0..*)

                func CreateA:
                	output: result A (1..1)
                	set result:
                		A {
                			prop1: empty,
                			prop2: empty,
                			prop3: empty,
                			prop4: empty,
                		}
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var map = new java.util.HashMap<String, Object>();
        map.put("prop1", null);
        map.put("prop2", List.of());
        map.put("prop3", null);
        map.put("prop4", List.of());
        var a = generatorTestHelper.createInstanceUsingBuilder(classes, "A", map);

        var createA = functionGeneratorHelper.createFunc(classes, "CreateA");
        assertEquals(a, functionGeneratorHelper.invokeFunc(createA, a.getClass()));
    }

    @Test
    void constructorExpression() {
        var code = generatorTestHelper.generateCode("""
                type A:
                	a int (1..1)
                	b string (0..*)
                	c A (0..1)

                func CreateA:
                	output: result A (1..1)
                	set result:
                		A {
                			c: A { a: 0, ... },
                			b: ["A", "B"],
                			a: 2*21,
                		}
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var a = generatorTestHelper.createInstanceUsingBuilder(classes, "A", Map.of(
                "a", 42,
                "b", List.of("A", "B"),
                "c", generatorTestHelper.createInstanceUsingBuilder(classes, "A", Map.of("a", 0))
        ));

        var createA = functionGeneratorHelper.createFunc(classes, "CreateA");
        assertEquals(a, functionGeneratorHelper.invokeFunc(createA, a.getClass()));
    }

    @Test
    void recordConstructorExpression() {
        var code = generatorTestHelper.generateCode("""
                func CreateDate:
                	output: result date (1..1)
                	set result:
                		date {
                			day: 4,
                			month: 11,
                			year: 1998
                		}
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var createDate = functionGeneratorHelper.createFunc(classes, "CreateDate");
        assertEquals(Date.of(1998, 11, 4), functionGeneratorHelper.invokeFunc(createDate, Date.class));
    }

    @Test
    void constructorExpressionWithReference() {
        var code = generatorTestHelper.generateCode("""
                type TypeWithKey:
                	[metadata key]

                type OtherType:
                	attrSingle TypeWithKey (1..1)
                		[metadata reference]
                	attrMulti TypeWithKey (0..*)
                		[metadata reference]

                func CreateOtherType:
                	inputs:
                		key TypeWithKey (1..1)
                	output: result OtherType (1..1)
                	set result:
                		OtherType {
                			attrSingle: key as-key,
                			attrMulti: [key, key] as-key
                		}
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var objectWithKey = generatorTestHelper.createInstanceUsingBuilder(classes, "TypeWithKey", Map.of(
                "meta", MetaFields.builder().setExternalKey("external").setGlobalKey("global")
        ));
        var objectWithKeyReference = generatorTestHelper.createInstanceUsingBuilder(classes, TEST_METAFIELDS, "ReferenceWithMetaTypeWithKey", Map.of(
                "externalReference", "external",
                "globalReference", "global"
        ));
        var otherObject = generatorTestHelper.createInstanceUsingBuilder(classes, "OtherType", Map.of(
                "attrSingle", objectWithKeyReference,
                "attrMulti", List.of(objectWithKeyReference, objectWithKeyReference)
        ));

        var createOtherType = functionGeneratorHelper.createFunc(classes, "CreateOtherType");
        assertEquals(otherObject, functionGeneratorHelper.invokeFunc(createOtherType, otherObject.getClass(), objectWithKey));
    }

    @Test
    void singularExtractWithEmptyValueReturnsEmpty() {
        var code = generatorTestHelper.generateCode("""
                func A:
                	inputs:
                		input int (0..1)
                	output:
                		result boolean (0..1)
                	set result:
                		input
                			extract
                				if item = 0
                				then True
                				else False
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var a = functionGeneratorHelper.createFunc(classes, "A");
        assertEquals(null, functionGeneratorHelper.invokeFunc(a, String.class, new Object[] {null}));
    }

    @Test
    void testDispatchFunction() throws Exception {
        var code = generatorTestHelper.generateCode("""
                enum DayCountFractionEnum:
                	ACT_360 displayName "ACT/360"
                	ACT_365L displayName "ACT/365L"
                	ACT_364 displayName "ACT/364"
                	ACT_365_fixed displayName "ACT/365.FIXED"
                	_30E_360 displayName "30E/360"
                	_30_360 displayName "30/360"

                func DayCountBasis:
                	inputs:
                		dcf DayCountFractionEnum (1..1)
                	output:
                		basis int (1..1)

                func DayCountBasis(dcf: DayCountFractionEnum -> ACT_360):
                	set basis: 360

                func DayCountBasis(dcf: DayCountFractionEnum ->_30_360):
                	set basis: 360

                func DayCountBasis(dcf: DayCountFractionEnum ->_30E_360):
                	set basis: 360

                func DayCountBasis(dcf: DayCountFractionEnum -> ACT_365L):
                	set basis: 365

                func DayCountBasis(dcf: DayCountFractionEnum -> ACT_365_fixed):
                	set basis: 365
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var dcfeLoader = classes
                .get("com.rosetta.test.model.DayCountFractionEnum")
                .getDeclaredMethod("fromDisplayName", String.class);
        var act360 = dcfeLoader.invoke(null, "ACT/360");
        var act365Fixed = dcfeLoader.invoke(null, "ACT/365.FIXED");
        var act364 = dcfeLoader.invoke(null, "ACT/364");
        var dayCountBasis = functionGeneratorHelper.createFunc(classes, "DayCountBasis");

        assertEquals(360, functionGeneratorHelper.invokeFunc(dayCountBasis, Integer.class, act360));
        assertEquals(365, functionGeneratorHelper.invokeFunc(dayCountBasis, Integer.class, act365Fixed));
        assertThrows(IllegalArgumentException.class, () -> functionGeneratorHelper.invokeFunc(dayCountBasis, Integer.class, act364));
    }

    @Test
    void conditionalThenJoin() {
        var code = generatorTestHelper.generateCode("""
                func A:
                	output:
                		result string (1..1)
                	set result:
                		if True
                	then ["Foo", "Bar"]
                	else "Bar"
                	then join ", "
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var a = functionGeneratorHelper.createFunc(classes, "A");
        assertEquals("Foo, Bar", functionGeneratorHelper.invokeFunc(a, String.class));
    }

    @Test
    void canPassEmptyToFunctionThatExpectsList() {
        var code = generatorTestHelper.generateCode("""
                func A:
                	inputs:
                		a int (0..*)
                	output:
                		result int (0..*)
                	add result:
                		a

                func B:
                	output: result int (0..*)
                	add result:
                		A(empty)

                func C:
                	inputs:
                		a int (0..1)
                	output:
                		result int (0..*)
                	add result:
                		A(a)
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var b = functionGeneratorHelper.createFunc(classes, "B");
        assertEquals(List.of(), functionGeneratorHelper.invokeFunc(b, List.class));

        var c = functionGeneratorHelper.createFunc(classes, "C");
        assertEquals(List.of(), functionGeneratorHelper.invokeFunc(c, List.class, new Object[] {null}));
    }

    @Test
    void canUseNullAsCondition() {
        var code = generatorTestHelper.generateCode("""
                func Test:
                	inputs: inp boolean (0..1)
                	output: result int (0..1)
                	set result:
                		if inp then 42
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var test = functionGeneratorHelper.createFunc(classes, "Test");

        assertEquals(null, functionGeneratorHelper.invokeFunc(test, Integer.class, new Object[] {null}));
    }

    @Test
    void canUseNullInFilter() {
        var code = generatorTestHelper.generateCode("""
                func Test:
                	inputs: inp boolean (0..1)
                	output: result int (0..1)
                	set result:
                		42
                			filter inp
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var test = functionGeneratorHelper.createFunc(classes, "Test");

        assertEquals(null, functionGeneratorHelper.invokeFunc(test, Integer.class, new Object[] {null}));
    }

    @Test
    void canChainAfterConditional() {
        var code = generatorTestHelper.generateCode("""
                func Test:
                	output: result int (0..*)
                	set result:
                		(if True then 42 else 0)
                			extract item + 1
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var test = functionGeneratorHelper.createFunc(classes, "Test");

        assertEquals(List.of(43), functionGeneratorHelper.invokeFunc(test, List.class));
    }

    @Test
    void canReturnDifferingCardinalitiesInIfThenElseBranches() {
        var code = generatorTestHelper.generateCode("""
                func Test:
                	output: result int (0..*)
                	set result:
                		42
                			extract
                				if False
                				then [1, 2]
                				else 0
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var test = functionGeneratorHelper.createFunc(classes, "Test");

        assertEquals(List.of(0), functionGeneratorHelper.invokeFunc(test, List.class));
    }

    @Test
    void passSingleItemToFunctionWhenMultiIsExpectedDoesNotResultInStaticCompilationError() {
        generatorTestHelper.compileToClasses(generatorTestHelper.generateCode("""
                func A:
                	inputs: a int (0..*)
                	output: result int (1..1)
                	set result: 42

                func Foo:
                	output: result int (0..*)
                	set result:
                		[1, 2, 3]
                			extract A(item)
                """));
    }

    @Test
    void toEnumTest() {
        var code = generatorTestHelper.generateCode("""
                enum Bar:
                	Value1
                	Value2 displayName "Value 2"

                func ToBar:
                	inputs: input string (1..1)
                	output: result Bar (1..1)
                	set result:
                		input to-enum Bar

                func ToString:
                	inputs: input Bar (1..1)
                	output: result string (1..1)
                	set result:
                		input to-string
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var barClass = classes.get("com.rosetta.test.model.Bar");
        var value1 = barClass.getEnumConstants()[0];
        var value2 = barClass.getEnumConstants()[1];

        var toBar = functionGeneratorHelper.createFunc(classes, "ToBar");

        assertEquals(value1, functionGeneratorHelper.invokeFunc(toBar, barClass, "Value1"));
        assertEquals(null, functionGeneratorHelper.invokeFunc(toBar, barClass, "Value2"));
        assertEquals(value2, functionGeneratorHelper.invokeFunc(toBar, barClass, "Value 2"));

        var toString = functionGeneratorHelper.createFunc(classes, "ToString");

        assertEquals("Value1", functionGeneratorHelper.invokeFunc(toString, String.class, value1));
        assertEquals("Value 2", functionGeneratorHelper.invokeFunc(toString, String.class, value2));
    }

    @Test
    void basicConversionTest() {
        var code = generatorTestHelper.generateCode("""
                func ToNumber:
                	inputs: input string (1..1)
                	output: result number (1..1)
                	set result:
                		input to-number

                func ToInt:
                	inputs: input string (1..1)
                	output: result int (1..1)
                	set result:
                		input to-int

                func ToTime:
                	inputs: input string (1..1)
                	output: result time (1..1)
                	set result:
                		input to-time

                func NumberToString:
                	inputs: input number (1..1)
                	output: result string (1..1)
                	set result:
                		input to-string

                func TimeToString:
                	inputs: input time (1..1)
                	output: result string (1..1)
                	set result:
                		input to-string
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var toNumber = functionGeneratorHelper.createFunc(classes, "ToNumber");
        assertEquals(BigDecimal.valueOf(3.14), functionGeneratorHelper.invokeFunc(toNumber, BigDecimal.class, "3.14"));
        assertEquals(null, functionGeneratorHelper.invokeFunc(toNumber, BigDecimal.class, "test"));
        assertEquals(BigDecimal.valueOf(-42), functionGeneratorHelper.invokeFunc(toNumber, BigDecimal.class, "-42"));

        var toInt = functionGeneratorHelper.createFunc(classes, "ToInt");
        assertEquals(3, functionGeneratorHelper.invokeFunc(toInt, Integer.class, "3"));
        assertEquals(null, functionGeneratorHelper.invokeFunc(toInt, Integer.class, "test"));
        assertEquals(-42, functionGeneratorHelper.invokeFunc(toInt, Integer.class, "-42"));

        var toTime = functionGeneratorHelper.createFunc(classes, "ToTime");
        assertEquals(LocalTime.of(15, 7, 42), functionGeneratorHelper.invokeFunc(toTime, LocalTime.class, "15:07:42"));
        assertEquals(null, functionGeneratorHelper.invokeFunc(toTime, LocalTime.class, "42:00:00"));
        assertEquals(LocalTime.of(23, 7, 0), functionGeneratorHelper.invokeFunc(toTime, LocalTime.class, "23:07"));

        var numberToString = functionGeneratorHelper.createFunc(classes, "NumberToString");
        assertEquals("3.14", functionGeneratorHelper.invokeFunc(numberToString, String.class, BigDecimal.valueOf(3.14)));
        assertEquals("-42", functionGeneratorHelper.invokeFunc(numberToString, String.class, BigDecimal.valueOf(-42)));

        var timeToString = functionGeneratorHelper.createFunc(classes, "TimeToString");
        assertEquals("15:07:42", functionGeneratorHelper.invokeFunc(timeToString, String.class, LocalTime.of(15, 7, 42)));
        assertEquals("23:07", functionGeneratorHelper.invokeFunc(timeToString, String.class, LocalTime.of(23, 7, 0)));
    }

    @Test
    void recordConversionTest() {
        var code = generatorTestHelper.generateCode("""
                func ToDate:
                	inputs: input string (1..1)
                	output: result date (1..1)
                	set result:
                		input to-date

                func ToDateTime:
                	inputs: input string (1..1)
                	output: result dateTime (1..1)
                	set result:
                		input to-date-time

                func ToZonedDateTime:
                	inputs: input string (1..1)
                	output: result zonedDateTime (1..1)
                	set result:
                		input to-zoned-date-time

                func DateToString:
                	inputs: input date (1..1)
                	output: result string (1..1)
                	set result:
                		input to-string

                func DateTimeToString:
                	inputs: input dateTime (1..1)
                	output: result string (1..1)
                	set result:
                		input to-string

                func ZonedDateTimeToString:
                	inputs: input zonedDateTime (1..1)
                	output: result string (1..1)
                	set result:
                		input to-string
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var toDate = functionGeneratorHelper.createFunc(classes, "ToDate");
        var dateStr = "2024-04-18";
        var dateRes = Date.of(2024, 4, 18);
        assertEquals(dateRes, functionGeneratorHelper.invokeFunc(toDate, Date.class, dateStr));
        assertEquals(null, functionGeneratorHelper.invokeFunc(toDate, Date.class, "test"));

        var toDateTime = functionGeneratorHelper.createFunc(classes, "ToDateTime");
        var dateTimeStr = "2024-04-18T13:06:26";
        var dateTimeRes = LocalDateTime.of(2024, 4, 18, 13, 6, 26);
        assertEquals(dateTimeRes, functionGeneratorHelper.invokeFunc(toDateTime, LocalDateTime.class, dateTimeStr));
        assertEquals(null, functionGeneratorHelper.invokeFunc(toDateTime, LocalDateTime.class, "test"));

        var toZonedDateTime = functionGeneratorHelper.createFunc(classes, "ToZonedDateTime");
        var zonedDateTimeStr1 = "2024-04-18T13:06:26+02:00[Europe/Brussels]";
        var zonedDateTimeRes1 = ZonedDateTime.of(2024, 4, 18, 13, 6, 26, 0, ZoneId.of("Europe/Brussels"));
        var zonedDateTimeStr2 = "2024-04-18T11:06:26Z";
        var zonedDateTimeRes2 = ZonedDateTime.of(2024, 4, 18, 11, 6, 26, 0, ZoneId.of("Z"));
        assertEquals(zonedDateTimeRes1, functionGeneratorHelper.invokeFunc(toZonedDateTime, ZonedDateTime.class, zonedDateTimeStr1));
        assertEquals(null, functionGeneratorHelper.invokeFunc(toZonedDateTime, ZonedDateTime.class, "test"));
        assertEquals(zonedDateTimeRes2, functionGeneratorHelper.invokeFunc(toZonedDateTime, ZonedDateTime.class, zonedDateTimeStr2));

        var dateToString = functionGeneratorHelper.createFunc(classes, "DateToString");
        assertEquals(dateStr, functionGeneratorHelper.invokeFunc(dateToString, String.class, dateRes));

        var dateTimeToString = functionGeneratorHelper.createFunc(classes, "DateTimeToString");
        assertEquals(dateTimeStr, functionGeneratorHelper.invokeFunc(dateTimeToString, String.class, dateTimeRes));

        var zonedDateTimeToString = functionGeneratorHelper.createFunc(classes, "ZonedDateTimeToString");
        assertEquals(zonedDateTimeStr1, functionGeneratorHelper.invokeFunc(zonedDateTimeToString, String.class, zonedDateTimeRes1));
        assertEquals(zonedDateTimeStr2, functionGeneratorHelper.invokeFunc(zonedDateTimeToString, String.class, zonedDateTimeRes2));
    }

    @Test
    void testSingularFilterOperation() {
        var code = generatorTestHelper.generateCode("""
                func NonZero:
                	inputs:
                		input int (0..1)
                	output:
                		result int (0..1)
                	set result:
                		input filter item <> 0
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var nonZero = functionGeneratorHelper.createFunc(classes, "NonZero");

        assertEquals(42, functionGeneratorHelper.invokeFunc(nonZero, Integer.class, 42));
        assertEquals(null, functionGeneratorHelper.invokeFunc(nonZero, Integer.class, 0));
    }

    @Test
    void testJavaLangNames() {
        var code = generatorTestHelper.generateCode("""
                func Boolean:
                	output:
                		Boolean boolean (1..1)
                	set Boolean:
                		True extract [ False ]
                """);
        generatorTestHelper.compileToClasses(code);
    }

    @Test
    void testJavaKeywordNames() {
        generatorTestHelper.compileToClasses(generatorTestHelper.generateCode("""
                func This:
                	output:
                		static int (1..1)
                	set static:
                		42
                """));
    }

    @Test
    void testAccessToDateMembers() {
        var code = generatorTestHelper.generateCode("""
                func GetDay:
                	inputs:
                		d date (1..1)
                	output:
                		result int (1..1)
                	set result:
                		d -> day

                func GetMonth:
                	inputs:
                		d date (1..1)
                	output:
                		result int (1..1)
                	set result:
                		d -> month

                func GetYear:
                	inputs:
                		d date (1..1)
                	output:
                		result int (1..1)
                	set result:
                		d -> year
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var getDay = functionGeneratorHelper.createFunc(classes, "GetDay");
        var getMonth = functionGeneratorHelper.createFunc(classes, "GetMonth");
        var getYear = functionGeneratorHelper.createFunc(classes, "GetYear");

        var d = Date.of(2023, 1, 19);

        assertEquals(19, functionGeneratorHelper.invokeFunc(getDay, Integer.class, d));
        assertEquals(1, functionGeneratorHelper.invokeFunc(getMonth, Integer.class, d));
        assertEquals(2023, functionGeneratorHelper.invokeFunc(getYear, Integer.class, d));
    }

    @Test
    void testAccessToDateTimeMembers() {
        var code = generatorTestHelper.generateCode("""
                func GetDate:
                	inputs:
                		dt dateTime (1..1)
                	output:
                		result date (1..1)
                	set result:
                		dt -> date

                func GetTime:
                	inputs:
                		dt dateTime (1..1)
                	output:
                		result time (1..1)
                	set result:
                		dt -> time
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var getDate = functionGeneratorHelper.createFunc(classes, "GetDate");
        var getTime = functionGeneratorHelper.createFunc(classes, "GetTime");

        var date = Date.of(2023, 1, 19);
        var time = LocalTime.of(11, 2);
        var dt = LocalDateTime.of(date.toLocalDate(), time);

        assertEquals(date, functionGeneratorHelper.invokeFunc(getDate, Date.class, dt));
        assertEquals(time, functionGeneratorHelper.invokeFunc(getTime, LocalTime.class, dt));
    }

    @Test
    void testAccessToZonedDateTimeMembers() {
        var code = generatorTestHelper.generateCode("""
                func GetDate:
                	inputs:
                		zdt zonedDateTime (1..1)
                	output:
                		result date (1..1)
                	set result:
                		zdt -> date

                func GetTime:
                	inputs:
                		zdt zonedDateTime (1..1)
                	output:
                		result time (1..1)
                	set result:
                		zdt -> time

                func GetZone:
                	inputs:
                		zdt zonedDateTime (1..1)
                	output:
                		result string (1..1)
                	set result:
                		zdt -> timezone
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var getDate = functionGeneratorHelper.createFunc(classes, "GetDate");
        var getTime = functionGeneratorHelper.createFunc(classes, "GetTime");
        var getZone = functionGeneratorHelper.createFunc(classes, "GetZone");

        var date = Date.of(2023, 1, 19);
        var time = LocalTime.of(11, 2);
        var zone = "Europe/Paris";
        var zdt = ZonedDateTime.of(date.toLocalDate(), time, ZoneId.of(zone));
        assertEquals(date, Date.of(zdt.toLocalDate()));
        assertEquals(time, zdt.toLocalTime());
        assertEquals(zone, zdt.getZone().getId());

        assertEquals(date, functionGeneratorHelper.invokeFunc(getDate, Date.class, zdt));
        assertEquals(time, functionGeneratorHelper.invokeFunc(getTime, LocalTime.class, zdt));
        assertEquals(zone, functionGeneratorHelper.invokeFunc(getZone, String.class, zdt));
    }

    @Test
    void mayDoRecursiveCalls() {
        var code = generatorTestHelper.generateCode("""
                func Rec:
                	output: result int (1..1)
                	alias test: Rec()
                	set result: Rec()
                """);
        generatorTestHelper.compileToClasses(code);
    }

    @Test
    void nestedInlineFunctionsTest() {
        var code = generatorTestHelper.generateCode("""
                namespace com.rosetta.test.model
                version "${project.version}"

                func F1:
                	output:
                		result int (1..1)

                	set result:
                		1 extract [
                			item then extract param1 [
                				10 extract [
                					item then extract param2 [
                						100 extract [
                							item*10
                						] then extract [
                							item + param1 + param2
                						]
                					]
                				]
                			]
                		]
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var func1 = functionGeneratorHelper.createFunc(classes, "F1");
        assertEquals(1011, functionGeneratorHelper.invokeFunc(func1, List.class));
    }

    @Test
    void directlyUseAttributesOfImplicitVariableTest() {
        var code = generatorTestHelper.generateCode("""
                namespace com.rosetta.test.model
                version "${project.version}"

                type Foo:
                	a int (1..1)
                	b string (0..*)

                func F1:
                	inputs:
                		foos Foo (0..*)
                	output:
                		result int (0..*)

                	add result:
                		foos
                			extract [ a + b count]
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var foo1 = generatorTestHelper.createInstanceUsingBuilder(classes, "Foo", Map.of("a", 42, "b", List.of()));
        var foo2 = generatorTestHelper.createInstanceUsingBuilder(classes, "Foo", Map.of("a", -5, "b", List.of("Hello", "World!")));
        var func1 = functionGeneratorHelper.createFunc(classes, "F1");
        assertEquals(List.of(42, -3), functionGeneratorHelper.invokeFunc(func1, List.class, List.of(foo1, foo2)));
    }

    @Test
    void omittedParameterInFunctionalOperationTest() {
        var code = generatorTestHelper.generateCode("""
                namespace com.rosetta.test.model
                version "${project.version}"

                func F1:
                	inputs:
                		a int (0..*)
                	output:
                		result int (0..*)

                	add result:
                		a extract [* 2]
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var func1 = functionGeneratorHelper.createFunc(classes, "F1");
        assertEquals(List.of(2, 4, 6), functionGeneratorHelper.invokeFunc(func1, List.class, List.of(1, 2, 3)));
    }

    @Test
    void namedFunctionInFunctionalOperationTest() {
        var code = generatorTestHelper.generateCode("""
                namespace com.rosetta.test.model
                version "${project.version}"

                func Incr:
                	inputs:
                		a int (1..1)
                	output:
                		result int (1..1)

                	set result:
                		a + 1

                func IsAnswerToTheUniverse:
                	inputs:
                		a int (1..1)
                	output:
                		result boolean (1..1)

                	set result:
                		a = 42

                func ClosestToTen:
                	inputs:
                		a int (1..1)
                		b int (1..1)
                	output:
                		result int (1..1)

                	set result:
                		if a < 10 then
                			if b < 10 then
                				if a > b then a else b
                			else
                				if 10 - a < b - 10 then a else b
                		else
                			if b < 10 then
                				if a - 10 < 10 - b then a else b
                			else
                				if a < b then a else b

                func F1:
                	inputs:
                		list int (0..*)
                	output:
                		res int (0..*)

                	add res:
                		list
                			extract Incr

                func F2:
                	inputs:
                		list int (0..*)
                	output:
                		res boolean (0..*)

                	add res:
                		list
                			extract IsLeapYear

                func F3:
                	inputs:
                		list int (0..*)
                	output:
                		res int (0..*)

                	add res:
                		list
                			filter IsAnswerToTheUniverse

                func F4:
                	inputs:
                		list int (0..*)
                	output:
                		res int (1..1)

                	set res:
                		list
                			reduce acc, v [ ClosestToTen(acc, v) ]

                func F5:
                	inputs:
                		list int (0..*)
                	output:
                		res int (0..*)

                	add res:
                		list
                			extract Incr
                			then extract Incr
                			then extract item + 1
                			then extract a [ a extract Incr ]
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var func1 = functionGeneratorHelper.createFunc(classes, "F1");
        assertEquals(List.of(2, 3, 4), functionGeneratorHelper.invokeFunc(func1, List.class, List.of(1, 2, 3)));

        var func2 = functionGeneratorHelper.createFunc(classes, "F2");
        assertEquals(List.of(true, false, false), functionGeneratorHelper.invokeFunc(func2, List.class, List.of(2000, 2001, 2002)));

        var func3 = functionGeneratorHelper.createFunc(classes, "F3");
        assertEquals(List.of(42, 42), functionGeneratorHelper.invokeFunc(func3, List.class, List.of(1, 2, 42, 3, 42)));

        var func4 = functionGeneratorHelper.createFunc(classes, "F4");
        assertEquals(8, functionGeneratorHelper.invokeFunc(func4, Integer.class, List.of(0, 5, 8)));
        assertEquals(11, functionGeneratorHelper.invokeFunc(func4, Integer.class, List.of(0, 5, 8, 11, 15)));

        var func5 = functionGeneratorHelper.createFunc(classes, "F5");
        assertEquals(List.of(5, 6, 7), functionGeneratorHelper.invokeFunc(func5, List.class, List.of(1, 2, 3)));
    }

    @Test
    void emptyArgumentTest() {
        var code = generatorTestHelper.generateCode("""
                namespace com.rosetta.test.model
                version "${project.version}"

                func F1:
                	output:
                		res int (1..1)
                	set res:
                		F2(empty)

                func F2:
                	inputs:
                		a int (0..1)
                	output:
                		res int (1..1)
                	set res:
                		42
                """);
        generatorTestHelper.compileToClasses(code);
    }

    @Test
    void thenOperationTest() {
        var code = generatorTestHelper.generateCode("""
                namespace com.rosetta.test.model
                version "${project.version}"

                func F1:
                	output:
                		res boolean (1..1)
                	set res:
                		empty then item = empty

                func F2:
                	output:
                		res int (1..1)
                	set res:
                		42 then item + item

                func F3:
                	output:
                		res int (2..2)
                	set res:
                		[1, 2, 3] then [ [item count, item sum] ]

                func F4:
                	output:
                		res int (2..2)
                	set res:
                		[1, 2, 3]
                			extract [ [item, item] ]
                			then extract l [ l count ]

                func F5:
                	output:
                		res int (2..2)
                	set res:
                		[1, 2, 3]
                			extract [ [item, item] ]
                			then extract l [ [ l count, l sum ] ]
                			then extract l [ l sum ]
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var func1 = functionGeneratorHelper.createFunc(classes, "F1");
        assertTrue(functionGeneratorHelper.invokeFunc(func1, Boolean.class));

        var func2 = functionGeneratorHelper.createFunc(classes, "F2");
        assertEquals(84, functionGeneratorHelper.invokeFunc(func2, Integer.class));

        var func3 = functionGeneratorHelper.createFunc(classes, "F3");
        assertEquals(List.of(3, 6), functionGeneratorHelper.invokeFunc(func3, List.class));

        var func4 = functionGeneratorHelper.createFunc(classes, "F4");
        assertEquals(List.of(2, 2, 2), functionGeneratorHelper.invokeFunc(func4, List.class));

        var func5 = functionGeneratorHelper.createFunc(classes, "F5");
        assertEquals(List.of(4, 6, 8), functionGeneratorHelper.invokeFunc(func5, List.class));
    }

    @Test
    void singularExtractTest() {
        var code = generatorTestHelper.generateCode("""
                namespace com.rosetta.test.model
                version "${project.version}"

                func F1:
                	output:
                		res int (1..1)
                	set res:
                		42
                			extract [item + 1]

                func F2:
                	output:
                		res boolean (1..1)
                	set res:
                		42
                			extract item + 1
                			then extract item = 42
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var func1 = functionGeneratorHelper.createFunc(classes, "F1");
        assertEquals(43, functionGeneratorHelper.invokeFunc(func1, Integer.class));

        var func2 = functionGeneratorHelper.createFunc(classes, "F2");
        assertFalse(functionGeneratorHelper.invokeFunc(func2, Boolean.class));
    }

    @Test
    void largeNumberTest() {
        var code = generatorTestHelper.generateCode("""
                namespace com.rosetta.test.model
                version "${project.version}"

                func F1:
                	output:
                		res number (1..1)
                	set res:
                		99999999999999999999.99999
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var func1 = functionGeneratorHelper.createFunc(classes, "F1");
        assertEquals(new BigDecimal("99999999999999999999.99999"), functionGeneratorHelper.invokeFunc(func1, Number.class));
    }

    @Test
    void testPreconditionValidGeneration() {
        generatorTestHelper.compileToClasses(generatorTestHelper.generateCode("""
                func FuncFoo:
                	inputs:
                		a int (1..1)
                	output:
                		result int (1..1)

                	condition PositiveArgument:
                		if True then a = 0

                	set result:
                		a
                """));
    }

    @Test
    void testExpressionValidGeneration() {
        generatorTestHelper.compileToClasses(generatorTestHelper.generateCode("""
                type A:
                	a int (0..1)

                func FuncFoo:
                	inputs:
                		a A (0..*)
                	output:
                		result A (0..*)

                	set result:
                		a filter [item->a exists]
                """));
    }

    @Test
    void testSimpleFunctionGeneration() {
        var code = """
                func FuncFoo:
                	inputs:
                		name string  (0..1)
                		name2 string (0..1)
                	output:
                		result string (0..1)
                """;
        var generatedCode = generatorTestHelper.generateCode(code);
        assertJavaEquals(
                """
				package com.rosetta.test.model.functions;

				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.RosettaFunction;


				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {

					/**
					* @param name\s
					* @param name2\s
					* @return result\s
					*/
					public String evaluate(String name, String name2) {
						String result = doEvaluate(name, name2);
				\t\t
						return result;
					}

					protected abstract String doEvaluate(String name, String name2);

					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected String doEvaluate(String name, String name2) {
							String result = null;
							return assignOutput(result, name, name2);
						}
				\t\t
						protected String assignOutput(String result, String name, String name2) {
							return result;
						}
					}
				}
				""",
                generatedCode.get("com.rosetta.test.model.functions.FuncFoo"));
        generatorTestHelper.compileToClasses(generatorTestHelper.generateCode(code));
    }

    @Test
    void shouldGenerateFunctionWithStringListOutput() {
        var code = """
                func FuncFoo:
                	inputs:
                		name string  (0..1)
                		name2 string (0..1)
                	output:
                		result string (0..*)
                """;
        var generatedCode = generatorTestHelper.generateCode(code);
        assertJavaEquals(
                """
				package com.rosetta.test.model.functions;

				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import java.util.ArrayList;
				import java.util.List;


				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {

					/**
					* @param name\s
					* @param name2\s
					* @return result\s
					*/
					public List<String> evaluate(String name, String name2) {
						List<String> result = doEvaluate(name, name2);
				\t\t
						return result;
					}

					protected abstract List<String> doEvaluate(String name, String name2);

					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected List<String> doEvaluate(String name, String name2) {
							List<String> result = new ArrayList<>();
							return assignOutput(result, name, name2);
						}
				\t\t
						protected List<String> assignOutput(List<String> result, String name, String name2) {
							return result;
						}
					}
				}
				""",
                generatedCode.get("com.rosetta.test.model.functions.FuncFoo"));
        generatorTestHelper.compileToClasses(generatorTestHelper.generateCode(code));
    }

    @Test
    void shouldGenerateFunctionWithNumberListOutput() {
        var code = """
                func FuncFoo:
                	inputs:
                		name string  (0..1)
                		name2 string (0..1)
                	output:
                		result number (0..*)
                """;
        var generatedCode = generatorTestHelper.generateCode(code);
        assertJavaEquals(
                """
				package com.rosetta.test.model.functions;

				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import java.math.BigDecimal;
				import java.util.ArrayList;
				import java.util.List;


				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {

					/**
					* @param name\s
					* @param name2\s
					* @return result\s
					*/
					public List<BigDecimal> evaluate(String name, String name2) {
						List<BigDecimal> result = doEvaluate(name, name2);
				\t\t
						return result;
					}

					protected abstract List<BigDecimal> doEvaluate(String name, String name2);

					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected List<BigDecimal> doEvaluate(String name, String name2) {
							List<BigDecimal> result = new ArrayList<>();
							return assignOutput(result, name, name2);
						}
				\t\t
						protected List<BigDecimal> assignOutput(List<BigDecimal> result, String name, String name2) {
							return result;
						}
					}
				}
				""",
                generatedCode.get("com.rosetta.test.model.functions.FuncFoo"));
        generatorTestHelper.compileToClasses(generatorTestHelper.generateCode(code));
    }

    @Test
    void shouldGenerateFunctionWithIntListOutput() {
        var code = """
                func FuncFoo:
                	inputs:
                		name string  (0..1)
                		name2 string (0..1)
                	output:
                		result int (0..*)
                """;
        var generatedCode = generatorTestHelper.generateCode(code);
        assertJavaEquals(
                """
				package com.rosetta.test.model.functions;

				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import java.util.ArrayList;
				import java.util.List;


				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {

					/**
					* @param name\s
					* @param name2\s
					* @return result\s
					*/
					public List<Integer> evaluate(String name, String name2) {
						List<Integer> result = doEvaluate(name, name2);
				\t\t
						return result;
					}

					protected abstract List<Integer> doEvaluate(String name, String name2);

					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected List<Integer> doEvaluate(String name, String name2) {
							List<Integer> result = new ArrayList<>();
							return assignOutput(result, name, name2);
						}
				\t\t
						protected List<Integer> assignOutput(List<Integer> result, String name, String name2) {
							return result;
						}
					}
				}
				""",
                generatedCode.get("com.rosetta.test.model.functions.FuncFoo"));
        generatorTestHelper.compileToClasses(generatorTestHelper.generateCode(code));
    }

    @Test
    void shouldGenerateFunctionWithDateListOutput() {
        var code = """
                func FuncFoo:
                	inputs:
                		name string  (0..1)
                		name2 string (0..1)
                	output:
                		result date (0..*)
                """;
        var generatedCode = generatorTestHelper.generateCode(code);
        assertJavaEquals(
                """
				package com.rosetta.test.model.functions;

				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.records.Date;
				import java.util.ArrayList;
				import java.util.List;


				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {

					/**
					* @param name\s
					* @param name2\s
					* @return result\s
					*/
					public List<Date> evaluate(String name, String name2) {
						List<Date> result = doEvaluate(name, name2);
				\t\t
						return result;
					}

					protected abstract List<Date> doEvaluate(String name, String name2);

					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected List<Date> doEvaluate(String name, String name2) {
							List<Date> result = new ArrayList<>();
							return assignOutput(result, name, name2);
						}
				\t\t
						protected List<Date> assignOutput(List<Date> result, String name, String name2) {
							return result;
						}
					}
				}
				""",
                generatedCode.get("com.rosetta.test.model.functions.FuncFoo"));
        generatorTestHelper.compileToClasses(generatorTestHelper.generateCode(code));
    }

    @Test
    void shouldGenerateFuncWithAssignOutputDoIfBooleanLiterals() {
        var code = generatorTestHelper.generateCode("""
                func Foo:
                	inputs:
                		foo int (0..1)
                	output:
                		result boolean (1..1)

                	set result:
                		if foo exists
                		then False
                		else True
                """);
        generatorTestHelper.compileToClasses(code);
    }

    @Test
    void shouldGenerateFuncWithAssignOutputDoIfBooleanLiteralsAndNoElse() {
        var code = generatorTestHelper.generateCode("""
                func Foo:
                	inputs:
                		foo int (0..1)
                	output:
                		result boolean (1..1)

                	set result:
                		if foo exists
                		then False
                """);
        generatorTestHelper.compileToClasses(code);
    }

    @Test
    void shouldGenerateFuncWithAssignOutputDoIfFuncCall() {
        var code = generatorTestHelper.generateCode("""
                func Bar:
                	inputs:
                		bar number (0..1)
                	output:
                		result number (1..1)

                func Foo:
                	inputs:
                		foo number (0..1)
                	output:
                		result number (1..1)

                	set result:
                		if foo exists
                		then Bar( foo )
                		else 0.0
                """);
        generatorTestHelper.compileToClasses(code);
    }

    @Test
    void shouldGenerateFuncWithAssignOutputDoIfFuncCallAndElseBoolean() {
        var code = generatorTestHelper.generateCode("""
                func Bar:
                	inputs:
                		bar number (0..1)
                	output:
                		result boolean (1..1)

                func Foo:
                	inputs:
                		foo number (0..1)
                	output:
                		result boolean (1..1)

                	set result:
                		if foo exists
                		then Bar( foo )
                		else True
                """);
        generatorTestHelper.compileToClasses(code);
    }

    @Test
    void shouldGenerateFuncWithAssignOutputDoIfFuncCallAndNoElse() {
        var code = generatorTestHelper.generateCode("""
                func Bar:
                	inputs:
                		bar number (0..1)
                	output:
                		result boolean (1..1)

                func Foo:
                	inputs:
                		foo number (0..1)
                	output:
                		result boolean (1..1)

                	set result:
                		if foo exists
                		then Bar( foo )
                """);
        generatorTestHelper.compileToClasses(code);
    }

    @Test
    void shouldGenerateFuncWithAssignOutputDoIfBigDecimalAndFeatureCall() {
        var code = generatorTestHelper.generateCode("""
                type Bar:
                	baz number (1..1)

                func Foo:
                	inputs:
                		bar Bar (0..1)
                	output:
                		result number (1..1)

                	set result:
                		if bar exists
                		then 30.0
                		else bar -> baz
                """);
        generatorTestHelper.compileToClasses(code);
    }

    @Test
    void shouldGenerateFuncWithAssignOutputDoIfComparisonResultAndElseBoolean() {
        var code = generatorTestHelper.generateCode("""
                type Bar:
                	baz number (1..1)

                func Foo:
                	inputs:
                		bar Bar (0..1)
                	output:
                		result boolean (1..1)

                	set result:
                		if bar -> baz exists
                		then bar -> baz > 5
                		else True
                """);
        generatorTestHelper.compileToClasses(code);
    }

    @Test
    void shouldGenerateFuncWithAssignOutputDoIfComparisonResultAndNoElse() {
        var code = generatorTestHelper.generateCode("""
                type Bar:
                	baz number (1..1)

                func Foo:
                	inputs:
                		bar Bar (0..1)
                	output:
                		result boolean (1..1)

                	set result:
                		if bar -> baz exists
                		then bar -> baz > 5
                """);
        generatorTestHelper.compileToClasses(code);
    }

    @Test
    void shouldGenerateFuncWithNestedBooleanExpressionCondition() {
        var code = generatorTestHelper.generateCode("""
                type Money:
                	amount number (1..1)
                	currency string (1..1)

                func Foo:
                	inputs:
                		m1 Money  (0..1)
                		m2 Money (0..1)
                		currency string (0..1)
                	output:
                		result string (0..1)

                	condition:
                		[ m1 -> currency , m2 -> currency ] any = currency
                """);
        generatorTestHelper.compileToClasses(code);
    }

    @Test
    void shouldGenerateFuncWithKeyReferenceFromAnotherNamespace() {
        var code = generatorTestHelper.generateCode(
                """
                        namespace com.rosetta.test.model.party
                        version "test"

                        type Party:
                        	[metadata key]
                        	id number (1..1)
                        	name string (1..1)
                        """,
                """
                        namespace com.rosetta.test.model.agreement
                        version "test"

                        import com.rosetta.test.model.party.*

                        type Agreement:
                        	id number (1..1)
                        	party Party (1..1)
                        		[metadata reference]
                        """,
                """
                        namespace "com.rosetta.test.model.func"
                        version "test"

                        import com.rosetta.test.model.party.*
                        import com.rosetta.test.model.agreement.*

                        func Create_Agreement:
                        	inputs:
                        		party Party (1..1)
                        	id number (1..1)
                        	output:
                        		agreement Agreement (1..1)

                        	set agreement -> id: id
                        	set agreement -> party: party as-key
                        """
        );

        generatorTestHelper.compileToClasses(code);
    }

    @Disabled
    @Test
    void shouldGenerateFunctionWithAssignemtnAsReference() {
        generatorTestHelper.compileToClasses(generatorTestHelper.generateCode(
                """
                        namespace com.rosetta.test.model.party
                        version "test"

                        type Party:
                        	id number (1..1)
                        	name MyData (1..1)

                        type MyData:
                        	val string (1..1)
                        """,
                """
                        namespace com.rosetta.test.model.agreement
                        version "test"

                        import com.rosetta.test.model.party.*

                        type Agreement:
                        	id number (1..1)
                        	party Party (1..1)

                        	condition AgreementValid:
                        	if Get_Party_Id() exists
                        		then id is absent

                        func Get_Party_Id:
                        	inputs:
                        		agreement Agreement (1..1)
                        	output:
                        		result MyData (1..1)

                        	set result : agreement -> party -> name
                        """
        ));
    }

    @Disabled
    @Test
    void shouldGenerateFunctionWithAssignmentAsMeta() {
        generatorTestHelper.compileToClasses(generatorTestHelper.generateCode(
                """
                        namespace com.rosetta.test.model.party
                        version "test"

                        type Party:
                        	id number (1..1)
                        	name string (1..1)

                        type MyData:
                        	val Party (1..1)
                        		[metadata id]
                        """,
                """
                        namespace com.rosetta.test.model.agreement
                        version "test"

                        import com.rosetta.test.model.party.*

                        type Agreement:
                        	id number (1..1)
                        	party Party (1..1)
                        		[metadata id]

                        	condition AgreementValid:
                        		if Get_Party_Id() exists
                        			then id is absent

                        func Get_Party_Id:
                        	inputs:
                        		agreement Agreement (1..1)
                        	output:
                        		result MyData (1..1)

                        	set result-> val : agreement -> party
                        """
        ));
    }

    @Test
    void shouldGenerateFunctionWithConditionalAssignment() {
        var code = generatorTestHelper.generateCode(
                """
                        namespace com.rosetta.test.model.agreement
                        version "test"

                        type Top:
                        	foo Foo (1..*)

                        type Foo:
                        	bar1 Bar (0..1)
                        	bar2 Bar (0..1)

                        type Bar:
                        	id number (1..1)

                        func ExtractBar: <"Extracts a bar">
                        	inputs: top Top (1..1)
                        	output: bar Bar (1..1)
                        	alias foo: top -> foo  only-element
                        	set bar:
                        		if foo -> bar1 exists then foo -> bar1
                        		//else if foo -> bar2 exists then foo -> bar2
                        """
        );
        generatorTestHelper.compileToClasses(code);
    }

    @Test
    void shouldGenerateFunctionWithCreationLHSUsingAlias() {
        var code = generatorTestHelper.generateCode(
                """
                        namespace com.rosetta.test.model.agreement
                        version "test"

                        type Top:
                        	foo Foo (1..1)

                        type Foo:
                        	bar1 Bar (0..1)
                        	bar2 Bar (0..1)

                        type Bar:
                        	id number (1..1)

                        func ExtractBar: <"Extracts a bar">
                        	inputs: top Top (1..1)
                        	output: topOut Top (1..1)
                        	alias fooAlias : topOut -> foo
                        	set fooAlias -> bar1:
                        		top -> foo -> bar1
                        	set topOut -> foo -> bar2:
                        		top -> foo -> bar2
                        """
        );

        var extractBar = code.get("com.rosetta.test.model.agreement.functions.ExtractBar");
        assertJavaEquals(
                """
				package com.rosetta.test.model.agreement.functions;

				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.ModelObjectValidator;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.test.model.agreement.Bar;
				import com.rosetta.test.model.agreement.Foo;
				import com.rosetta.test.model.agreement.Top;
				import java.util.Optional;
				import javax.inject.Inject;


				@ImplementedBy(ExtractBar.ExtractBarDefault.class)
				public abstract class ExtractBar implements RosettaFunction {
				\t
					@Inject protected ModelObjectValidator objectValidator;

					/**
					* @param top\s
					* @return topOut\s
					*/
					public Top evaluate(Top top) {
						Top.TopBuilder topOutBuilder = doEvaluate(top);
				\t\t
						final Top topOut;
						if (topOutBuilder == null) {
							topOut = null;
						} else {
							topOut = topOutBuilder.build();
							objectValidator.validate(Top.class, topOut);
						}
				\t\t
						return topOut;
					}

					protected abstract Top.TopBuilder doEvaluate(Top top);

					protected abstract Foo.FooBuilder fooAlias(Top.TopBuilder topOut, Top top);

					public static class ExtractBarDefault extends ExtractBar {
						@Override
						protected Top.TopBuilder doEvaluate(Top top) {
							Top.TopBuilder topOut = Top.builder();
							return assignOutput(topOut, top);
						}
				\t\t
						protected Top.TopBuilder assignOutput(Top.TopBuilder topOut, Top top) {
							topOut.getOrCreateFoo()
								.setBar1(MapperS.of(top).<Foo>map("getFoo", _top -> _top.getFoo()).<Bar>map("getBar1", foo -> foo.getBar1()).get());
				\t\t\t
							topOut
								.getOrCreateFoo()
								.setBar2(MapperS.of(top).<Foo>map("getFoo", _top -> _top.getFoo()).<Bar>map("getBar2", foo -> foo.getBar2()).get());
				\t\t\t
							return Optional.ofNullable(topOut)
								.map(o -> o.prune())
								.orElse(null);
						}
				\t\t
						@Override
						protected Foo.FooBuilder fooAlias(Top.TopBuilder topOut, Top top) {
							return toBuilder(MapperS.of(topOut).<Foo>map("getFoo", _top -> _top.getFoo()).get());
						}
					}
				}
				""",
                extractBar);
        generatorTestHelper.compileToClasses(code);
    }

    @Test
    void shouldGenerateFunctionWithAliasAssignOutput() {
        var code = generatorTestHelper.generateCode(
                """
                        namespace com.rosetta.test.model.agreement
                        version "test"

                        type Top:
                        	foo Foo (1..1)

                        type Foo:
                        	bar Bar (0..1)

                        type Bar:
                        	id number (1..1)

                        func UpdateBarId: <"Updates Bar.id by set on an alias">
                        	inputs:
                        		top Top (1..1)
                        		newId number (1..1)

                        	output:
                        		topOut Top (1..1)

                        	alias barAlias :
                        		topOut -> foo -> bar

                        	set barAlias -> id:
                        		newId
                        """
        );
        generatorTestHelper.compileToClasses(code);
    }

    @Test
    void shouldGenerateDisjoint() {
        var code = generatorTestHelper.generateCode(
                """
                        namespace com.rosetta.test.model.agreement
                        version "test"

                        type Top:
                        	foo Foo (1..*)

                        type Foo:
                        	bar1 number (0..1)

                        func Disjoint: <"checks disjoint">
                        	inputs:
                        		top1 Top (1..1)
                        		top2 Top (1..1)

                        	output: result boolean (1..1)
                        	set result:
                        		top1-> foo disjoint top2 -> foo
                        """
        );
        generatorTestHelper.compileToClasses(code);
    }

    @Test
    void shouldNotGenerateDisjointDifferentTypes() {
        var model = modelHelper.parseRosetta("""
                namespace com.rosetta.test.model.agreement
                version "test"

                type Top:
                	foo Foo (1..*)
                	bar string (1..*)

                type Foo:
                	bar1 number (0..1)

                func ExtractBar: <"tries disjoint differnt types">
                	inputs:
                		top1 Top (1..1)
                		top2 Top (1..1)

                	output: result boolean (1..1)
                	set result:
                		top1-> foo disjoint top2 -> bar
                """);

        validationTestHelper.assertError(model, ROSETTA_DISJOINT_EXPRESSION, null, "Types `Foo` and `string` are not comparable");
    }

    @Test
    void shouldNotAndInts() {
        var model = modelHelper.parseRosetta("""
                namespace com.rosetta.test.model.agreement
                version "test"

                type Top:
                	foo Foo (1..1)

                type Foo:
                	bar1 number (1..1)

                func ExtractBar: <"tries anding integers">
                	inputs:
                		top1 Top (1..1)
                		top2 Top (1..1)

                	output: result boolean (1..1)

                	set result:
                		top1 -> foo and top2 -> foo
                """);

        validationTestHelper.assertError(model, LOGICAL_OPERATION, null,
                "Expected type `boolean`, but got `Foo` instead. Cannot use `Foo` with operator `and`");
    }

    @Test
    void shouldReturnMultiple() {
        var model = modelHelper.parseRosettaWithNoErrors("""
                namespace com.rosetta.test.model.agreement
                version "test"

                type Top:
                	foo Foo (1..*)
                	foob Foo (1..1)

                type Foo:
                	bar1 number (1..*)

                func ExtractFoo: <"tries returning list of complex">
                	inputs:
                		top1 Top (1..1)
                	output:
                		result Foo (1..*)
                	add result:
                		top1 -> foo

                func ExtractFoowithAlias: <"tries returning list of complex">
                	inputs:
                		top1 Top (1..1)
                	output:
                		result Foo (0..*)
                	alias foos: top1 -> foo
                	set result:
                		foos

                func ExtractBar: <"tries returning list of basic">
                	inputs:
                		top1 Top (1..1)
                	output:
                		result number (1..*)
                	add result:
                		top1-> foo -> bar1
                """);
        generatorTestHelper.compileToClasses(generatorTestHelper.generateCode(model));
    }

    @Test
    void funcCallingMultipleFunc() {
        var model = """
                func F1:
                	inputs: f1Input date (1..1)
                	output: f1OutputList date (1..*)

                func F2:
                	inputs: f2InputList date (1..*)
                	output: f2Output date (1..1)

                func F3:
                	inputs: f3Input date (1..1)
                	output: f3Output date (1..1)
                	set f3Output: F2(F1(f3Input))
                """;
        var code = generatorTestHelper.generateCode(model);
        var f3 = code.get("com.rosetta.test.model.functions.F3");
        assertJavaEquals(
                """
				package com.rosetta.test.model.functions;

				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.records.Date;
				import javax.inject.Inject;


				@ImplementedBy(F3.F3Default.class)
				public abstract class F3 implements RosettaFunction {
				\t
					// RosettaFunction dependencies
					//
					@Inject protected F1 f1;
					@Inject protected F2 f2;

					/**
					* @param f3Input\s
					* @return f3Output\s
					*/
					public Date evaluate(Date f3Input) {
						Date f3Output = doEvaluate(f3Input);
				\t\t
						return f3Output;
					}

					protected abstract Date doEvaluate(Date f3Input);

					public static class F3Default extends F3 {
						@Override
						protected Date doEvaluate(Date f3Input) {
							Date f3Output = null;
							return assignOutput(f3Output, f3Input);
						}
				\t\t
						protected Date assignOutput(Date f3Output, Date f3Input) {
							f3Output = f2.evaluate(f1.evaluate(f3Input));
				\t\t\t
							return f3Output;
						}
					}
				}
				""",
                f3
        );
        generatorTestHelper.compileToClasses(code);
    }

    @Test
    void testDelegateFunctionCallWithInputAlias() {
        var model = """
                func F1:
                	inputs: f1Input string (1..1)
                	output: f1Output string (1..1)

                func F2:
                	inputs: f2Input string (1..1)
                	output: f2Output string (1..1)
                	alias foo: F1(f2Input)
                	set f2Output: foo
                """;
        var code = generatorTestHelper.generateCode(model);
        var f1 = code.get("com.rosetta.test.model.functions.F1");
        assertJavaEquals(
                """
				package com.rosetta.test.model.functions;

				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.RosettaFunction;


				@ImplementedBy(F1.F1Default.class)
				public abstract class F1 implements RosettaFunction {

					/**
					* @param f1Input\s
					* @return f1Output\s
					*/
					public String evaluate(String f1Input) {
						String f1Output = doEvaluate(f1Input);
				\t\t
						return f1Output;
					}

					protected abstract String doEvaluate(String f1Input);

					public static class F1Default extends F1 {
						@Override
						protected String doEvaluate(String f1Input) {
							String f1Output = null;
							return assignOutput(f1Output, f1Input);
						}
				\t\t
						protected String assignOutput(String f1Output, String f1Input) {
							return f1Output;
						}
					}
				}
				""",
                f1
        );
        var f2 = code.get("com.rosetta.test.model.functions.F2");
        assertJavaEquals(
                """
				package com.rosetta.test.model.functions;

				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperS;
				import javax.inject.Inject;


				@ImplementedBy(F2.F2Default.class)
				public abstract class F2 implements RosettaFunction {
				\t
					// RosettaFunction dependencies
					//
					@Inject protected F1 f1;

					/**
					* @param f2Input\s
					* @return f2Output\s
					*/
					public String evaluate(String f2Input) {
						String f2Output = doEvaluate(f2Input);
				\t\t
						return f2Output;
					}

					protected abstract String doEvaluate(String f2Input);

					protected abstract MapperS<String> foo(String f2Input);

					public static class F2Default extends F2 {
						@Override
						protected String doEvaluate(String f2Input) {
							String f2Output = null;
							return assignOutput(f2Output, f2Input);
						}
				\t\t
						protected String assignOutput(String f2Output, String f2Input) {
							f2Output = foo(f2Input).get();
				\t\t\t
							return f2Output;
						}
				\t\t
						@Override
						protected MapperS<String> foo(String f2Input) {
							return MapperS.of(f1.evaluate(f2Input));
						}
					}
				}
				""",
                f2
        );
        generatorTestHelper.compileToClasses(code);
    }

    @Test
    void funcCallingMultipleFunc2() {
        var model = """
                func F1:
                	inputs: f1Input date (1..1)
                	output: f1OutputList date (1..*)

                func F2:
                	inputs: f2InputList date (1..*)
                	output: f2Output date (1..1)

                func F3:
                	inputs: f3Input date (1..1)
                	output: f3Output date (1..1)
                	alias f1OutList: F1(f3Input)
                	set f3Output: F2(f1OutList)
                """;
        var code = generatorTestHelper.generateCode(model);
        var f1 = code.get("com.rosetta.test.model.functions.F1");
        assertJavaEquals(
                """
				package com.rosetta.test.model.functions;

				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.records.Date;
				import java.util.ArrayList;
				import java.util.List;


				@ImplementedBy(F1.F1Default.class)
				public abstract class F1 implements RosettaFunction {

					/**
					* @param f1Input\s
					* @return f1OutputList\s
					*/
					public List<Date> evaluate(Date f1Input) {
						List<Date> f1OutputList = doEvaluate(f1Input);
				\t\t
						return f1OutputList;
					}

					protected abstract List<Date> doEvaluate(Date f1Input);

					public static class F1Default extends F1 {
						@Override
						protected List<Date> doEvaluate(Date f1Input) {
							List<Date> f1OutputList = new ArrayList<>();
							return assignOutput(f1OutputList, f1Input);
						}
				\t\t
						protected List<Date> assignOutput(List<Date> f1OutputList, Date f1Input) {
							return f1OutputList;
						}
					}
				}
				""",
                f1
        );
        var f2 = code.get("com.rosetta.test.model.functions.F2");
        assertJavaEquals(
                """
				package com.rosetta.test.model.functions;

				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.records.Date;
				import java.util.Collections;
				import java.util.List;


				@ImplementedBy(F2.F2Default.class)
				public abstract class F2 implements RosettaFunction {

					/**
					* @param f2InputList\s
					* @return f2Output\s
					*/
					public Date evaluate(List<Date> f2InputList) {
						Date f2Output = doEvaluate(f2InputList);
				\t\t
						return f2Output;
					}

					protected abstract Date doEvaluate(List<Date> f2InputList);

					public static class F2Default extends F2 {
						@Override
						protected Date doEvaluate(List<Date> f2InputList) {
							if (f2InputList == null) {
								f2InputList = Collections.emptyList();
							}
							Date f2Output = null;
							return assignOutput(f2Output, f2InputList);
						}
				\t\t
						protected Date assignOutput(Date f2Output, List<Date> f2InputList) {
							return f2Output;
						}
					}
				}
				""",
                f2
        );
        var f3 = code.get("com.rosetta.test.model.functions.F3");
        assertJavaEquals(
                """
				package com.rosetta.test.model.functions;

				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import com.rosetta.model.lib.records.Date;
				import javax.inject.Inject;


				@ImplementedBy(F3.F3Default.class)
				public abstract class F3 implements RosettaFunction {
				\t
					// RosettaFunction dependencies
					//
					@Inject protected F1 f1;
					@Inject protected F2 f2;

					/**
					* @param f3Input\s
					* @return f3Output\s
					*/
					public Date evaluate(Date f3Input) {
						Date f3Output = doEvaluate(f3Input);
				\t\t
						return f3Output;
					}

					protected abstract Date doEvaluate(Date f3Input);

					protected abstract MapperC<Date> f1OutList(Date f3Input);

					public static class F3Default extends F3 {
						@Override
						protected Date doEvaluate(Date f3Input) {
							Date f3Output = null;
							return assignOutput(f3Output, f3Input);
						}
				\t\t
						protected Date assignOutput(Date f3Output, Date f3Input) {
							f3Output = f2.evaluate(f1OutList(f3Input).getMulti());
				\t\t\t
							return f3Output;
						}
				\t\t
						@Override
						protected MapperC<Date> f1OutList(Date f3Input) {
							return MapperC.<Date>of(f1.evaluate(f3Input));
						}
					}
				}
				""",
                f3
        );
        generatorTestHelper.compileToClasses(code);
    }

    @Test
    void funcCallingMultipleFuncWithAlias() {
        var model = modelHelper.parseRosettaWithNoErrors("""
                namespace "demo"
                version "${project.version}"

                type Number:
                	num number (1..1)

                func F1:
                	inputs: num number (1..1)
                	output: numbers Number (1..*)

                func F2:
                	inputs: nums number(1..*)
                	output: str string (1..1)

                func F3:
                	inputs: num number (1..1)
                	output: str string (1..1)

                	alias f1: F1(num)
                	set str: F2(f1 -> num)

                func F4:
                	inputs: num number (1..*)
                	output: str string (1..1)

                	alias f2: F2(num)

                	set str: f2
                """);
        generatorTestHelper.compileToClasses(generatorTestHelper.generateCode(model));
    }

    @Test
    void typeWithCondition() {
        var model = modelHelper.parseRosettaWithNoErrors("""
                namespace "demo"
                version "${project.version}"

                type Foo:
                	bar Bar (1..1)

                	condition XXX:
                	if bar -> num exists
                	then bar -> zap contains Zap -> A
                	and if bar -> zap contains Zap -> A
                	then bar -> num exists

                type Bar:
                	num number (0..1)
                	zap Zap (1..2)

                enum Zap:
                	A B C
                """);
        generatorTestHelper.generateCode(model);
    }

    @Test
    void funcUsingListEquals() {
        var model = modelHelper.parseRosetta("""
                namespace "demo"
                version "${project.version}"

                type T1:
                		num number (1..1)
                		nums number (1..*)

                func F1:
                	inputs: t1 T1(1..1)
                			t2 T1(1..1)
                	output: res boolean (1..1)
                	set res: t1->num = t2->nums
                """);
        validationTestHelper.assertError(model, EQUALITY_OPERATION, null,
                "Operator `=` should specify `all` or `any` when comparing a list to a single value");
    }

    @Test
    void funcUsingListEqualsAll() {
        var code = generatorTestHelper.generateCode("""
                namespace com.rosetta.test.model
                version "${project.version}"

                func F1:
                	inputs:
                		s1 string (1..*)
                		s2 string (1..1)
                	output:
                		res boolean (1..1)
                	set res: s1 all = s2
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var func = functionGeneratorHelper.createFunc(classes, "F1");
        assertTrue(functionGeneratorHelper.invokeFunc(func, Boolean.class, Arrays.asList("a", "a"), "a"));
        assertFalse(functionGeneratorHelper.invokeFunc(func, Boolean.class, Arrays.asList("a", "b"), "a"));
        assertFalse(functionGeneratorHelper.invokeFunc(func, Boolean.class, Arrays.asList("b", "b"), "a"));
    }

    @Test
    void funcUsingListEqualsAny() {
        var code = generatorTestHelper.generateCode("""
                namespace com.rosetta.test.model
                version "${project.version}"

                func F1:
                	inputs:
                		s1 string (1..*)
                		s2 string (1..1)
                	output:
                		res boolean (1..1)
                	set res: s1 any = s2
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var func = functionGeneratorHelper.createFunc(classes, "F1");
        assertTrue(functionGeneratorHelper.invokeFunc(func, Boolean.class, Arrays.asList("a", "a"), "a"));
        assertTrue(functionGeneratorHelper.invokeFunc(func, Boolean.class, Arrays.asList("a", "b"), "a"));
        assertFalse(functionGeneratorHelper.invokeFunc(func, Boolean.class, Arrays.asList("b", "b"), "a"));
    }

    @Test
    void funcUsingListComparableEqualsAll() {
        var code = generatorTestHelper.generateCode("""
                namespace com.rosetta.test.model
                version "${project.version}"

                func F1:
                	inputs:
                		n1 int (1..*)
                		n2 int (1..1)
                	output:
                		res boolean (1..1)
                	set res: n1 all = n2
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var func = functionGeneratorHelper.createFunc(classes, "F1");
        assertTrue(functionGeneratorHelper.invokeFunc(func, Boolean.class, Arrays.asList(1, 1), 1));
        assertFalse(functionGeneratorHelper.invokeFunc(func, Boolean.class, Arrays.asList(1, 2), 1));
        assertFalse(functionGeneratorHelper.invokeFunc(func, Boolean.class, Arrays.asList(1, 1), 2));
    }

    @Test
    void funcUsingListComparableEqualsAny() {
        var code = generatorTestHelper.generateCode("""
                namespace com.rosetta.test.model
                version "${project.version}"

                func F1:
                	inputs:
                		n1 int (1..*)
                		n2 int (1..1)
                	output:
                		res boolean (1..1)
                	set res: n1 any = n2
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var func = functionGeneratorHelper.createFunc(classes, "F1");
        assertTrue(functionGeneratorHelper.invokeFunc(func, Boolean.class, Arrays.asList(1, 1), 1));
        assertTrue(functionGeneratorHelper.invokeFunc(func, Boolean.class, Arrays.asList(1, 2), 1));
        assertFalse(functionGeneratorHelper.invokeFunc(func, Boolean.class, Arrays.asList(1, 1), 2));
    }

    @Test
    void funcUsingZonedDateTimeEquality() {
        var code = generatorTestHelper.generateCode("""
                namespace com.rosetta.test.model
                version "${project.version}"

                func F1:
                	inputs:
                		dt1 zonedDateTime (1..1)
                		dt2 zonedDateTime (1..1)
                	output:
                		res boolean (1..1)
                	set res: dt1 = dt2
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var func = functionGeneratorHelper.createFunc(classes, "F1");

        var dt1 = ZonedDateTime.of(2022, 10, 13, 14, 0, 0, 0, ZoneId.of("Europe/Brussels"));
        var dt2 = ZonedDateTime.of(2022, 10, 13, 14, 0, 0, 0, ZoneId.of("Europe/Brussels"));
        assertTrue(functionGeneratorHelper.invokeFunc(func, Boolean.class, dt1, dt2));

        dt1 = ZonedDateTime.of(2022, 10, 13, 14, 0, 0, 0, ZoneId.of("Europe/Brussels"));
        dt2 = ZonedDateTime.of(2022, 10, 13, 14, 0, 0, 0, ZoneId.of("Europe/London"));
        assertFalse(functionGeneratorHelper.invokeFunc(func, Boolean.class, dt1, dt2));

        dt1 = ZonedDateTime.of(2022, 10, 13, 15, 0, 0, 0, ZoneId.of("Europe/Brussels"));
        dt2 = ZonedDateTime.of(2022, 10, 13, 14, 0, 0, 0, ZoneId.of("Europe/London"));
        assertTrue(functionGeneratorHelper.invokeFunc(func, Boolean.class, dt1, dt2));
    }

    @Test
    void funcUsingListNotEqualsAll() {
        var code = generatorTestHelper.generateCode("""
                namespace com.rosetta.test.model
                version "${project.version}"

                func F1:
                	inputs:
                		s1 string (1..*)
                		s2 string (1..1)
                	output:
                		res boolean (1..1)
                	set res: s1 all <> s2
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var func = functionGeneratorHelper.createFunc(classes, "F1");
        assertFalse(functionGeneratorHelper.invokeFunc(func, Boolean.class, Arrays.asList("a", "a"), "a"));
        assertFalse(functionGeneratorHelper.invokeFunc(func, Boolean.class, Arrays.asList("a", "b"), "a"));
        assertTrue(functionGeneratorHelper.invokeFunc(func, Boolean.class, Arrays.asList("a", "a"), "b"));
    }

    @Test
    void funcUsingListNotEqualsAny() {
        var code = generatorTestHelper.generateCode("""
                namespace com.rosetta.test.model
                version "${project.version}"

                func F1:
                	inputs:
                		s1 string (1..*)
                		s2 string (1..1)
                	output:
                		res boolean (1..1)
                	set res: s1 any <> s2
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var func = functionGeneratorHelper.createFunc(classes, "F1");
        assertFalse(functionGeneratorHelper.invokeFunc(func, Boolean.class, Arrays.asList("a", "a"), "a"));
        assertTrue(functionGeneratorHelper.invokeFunc(func, Boolean.class, Arrays.asList("a", "b"), "a"));
        assertTrue(functionGeneratorHelper.invokeFunc(func, Boolean.class, Arrays.asList("a", "a"), "b"));
    }

    @Test
    void funcUsingListComparableNotEqualsAll() {
        var code = generatorTestHelper.generateCode("""
                namespace com.rosetta.test.model
                version "${project.version}"

                func F1:
                	inputs:
                		n1 int (1..*)
                		n2 int (1..1)
                	output:
                		res boolean (1..1)
                	set res: n1 all <> n2
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var func = functionGeneratorHelper.createFunc(classes, "F1");
        assertFalse(functionGeneratorHelper.invokeFunc(func, Boolean.class, Arrays.asList(1, 1), 1));
        assertFalse(functionGeneratorHelper.invokeFunc(func, Boolean.class, Arrays.asList(1, 2), 1));
        assertTrue(functionGeneratorHelper.invokeFunc(func, Boolean.class, Arrays.asList(1, 1), 2));
    }

    @Test
    void funcUsingListComparableNotEqualsAny() {
        var code = generatorTestHelper.generateCode("""
                namespace com.rosetta.test.model
                version "${project.version}"

                func F1:
                	inputs:
                		n1 int (1..*)
                		n2 int (1..1)
                	output:
                		res boolean (1..1)
                	set res: n1 any <> n2
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var func = functionGeneratorHelper.createFunc(classes, "F1");
        assertFalse(functionGeneratorHelper.invokeFunc(func, Boolean.class, Arrays.asList(1, 1), 1));
        assertTrue(functionGeneratorHelper.invokeFunc(func, Boolean.class, Arrays.asList(1, 2), 1));
        assertTrue(functionGeneratorHelper.invokeFunc(func, Boolean.class, Arrays.asList(1, 1), 2));
    }

    @Test
    void funcUsingListComparableGreaterThanAll() {
        var code = generatorTestHelper.generateCode("""
                namespace com.rosetta.test.model
                version "${project.version}"

                func F1:
                	inputs:
                		n1 int (1..*)
                		n2 int (1..1)
                	output:
                		res boolean (1..1)
                	set res: n1 all > n2
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var func = functionGeneratorHelper.createFunc(classes, "F1");
        assertFalse(functionGeneratorHelper.invokeFunc(func, Boolean.class, Arrays.asList(1, 1), 2));
        assertFalse(functionGeneratorHelper.invokeFunc(func, Boolean.class, Arrays.asList(1, 3), 2));
        assertTrue(functionGeneratorHelper.invokeFunc(func, Boolean.class, Arrays.asList(3, 3), 2));
    }

    @Test
    void funcUsingListComparableGreaterThanAny() {
        var code = generatorTestHelper.generateCode("""
                namespace com.rosetta.test.model
                version "${project.version}"

                func F1:
                	inputs:
                		n1 int (1..*)
                		n2 int (1..1)
                	output:
                		res boolean (1..1)
                	set res: n1 any > n2
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var func = functionGeneratorHelper.createFunc(classes, "F1");
        assertFalse(functionGeneratorHelper.invokeFunc(func, Boolean.class, Arrays.asList(1, 1), 2));
        assertTrue(functionGeneratorHelper.invokeFunc(func, Boolean.class, Arrays.asList(1, 3), 2));
        assertTrue(functionGeneratorHelper.invokeFunc(func, Boolean.class, Arrays.asList(3, 3), 2));
    }

    @Test
    void funcUsingZonedDateTimeGreaterThan() {
        var code = generatorTestHelper.generateCode("""
                namespace com.rosetta.test.model
                version "${project.version}"

                func F1:
                	inputs:
                		dt1 zonedDateTime (1..1)
                		dt2 zonedDateTime (1..1)
                	output:
                		res boolean (1..1)
                	set res: dt1 > dt2
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var func = functionGeneratorHelper.createFunc(classes, "F1");

        var dt1 = ZonedDateTime.of(2022, 10, 13, 14, 0, 0, 0, ZoneId.of("Europe/Brussels"));
        var dt2 = ZonedDateTime.of(2022, 10, 13, 14, 0, 0, 0, ZoneId.of("Europe/Brussels"));
        assertFalse(functionGeneratorHelper.invokeFunc(func, Boolean.class, dt1, dt2));

        dt1 = ZonedDateTime.of(2022, 10, 13, 14, 0, 0, 0, ZoneId.of("Europe/Brussels"));
        dt2 = ZonedDateTime.of(2022, 10, 13, 14, 0, 0, 0, ZoneId.of("Europe/London"));
        assertFalse(functionGeneratorHelper.invokeFunc(func, Boolean.class, dt1, dt2));

        dt1 = ZonedDateTime.of(2022, 10, 13, 15, 0, 0, 0, ZoneId.of("Europe/Brussels"));
        dt2 = ZonedDateTime.of(2022, 10, 13, 14, 0, 0, 0, ZoneId.of("Europe/London"));
        assertFalse(functionGeneratorHelper.invokeFunc(func, Boolean.class, dt1, dt2));

        dt1 = ZonedDateTime.of(2022, 10, 13, 16, 0, 0, 0, ZoneId.of("Europe/Brussels"));
        dt2 = ZonedDateTime.of(2022, 10, 13, 14, 0, 0, 0, ZoneId.of("Europe/London"));
        assertTrue(functionGeneratorHelper.invokeFunc(func, Boolean.class, dt1, dt2));
    }

    @Test
    void funcUsingZonedDateTimeGreatherThanOrEqual() {
        var code = generatorTestHelper.generateCode("""
                namespace com.rosetta.test.model
                version "${project.version}"

                func F1:
                	inputs:
                		dt1 zonedDateTime (1..1)
                		dt2 zonedDateTime (1..1)
                	output:
                		res boolean (1..1)
                	set res: dt1 >= dt2
                """);
        var classes = generatorTestHelper.compileToClasses(code);

        var func = functionGeneratorHelper.createFunc(classes, "F1");

        var dt1 = ZonedDateTime.of(2022, 10, 13, 14, 0, 0, 0, ZoneId.of("Europe/Brussels"));
        var dt2 = ZonedDateTime.of(2022, 10, 13, 14, 0, 0, 0, ZoneId.of("Europe/Brussels"));
        assertTrue(functionGeneratorHelper.invokeFunc(func, Boolean.class, dt1, dt2));

        dt1 = ZonedDateTime.of(2022, 10, 13, 14, 0, 0, 0, ZoneId.of("Europe/Brussels"));
        dt2 = ZonedDateTime.of(2022, 10, 13, 14, 0, 0, 0, ZoneId.of("Europe/London"));
        assertFalse(functionGeneratorHelper.invokeFunc(func, Boolean.class, dt1, dt2));

        dt1 = ZonedDateTime.of(2022, 10, 13, 15, 0, 0, 0, ZoneId.of("Europe/Brussels"));
        dt2 = ZonedDateTime.of(2022, 10, 13, 14, 0, 0, 0, ZoneId.of("Europe/London"));
        assertTrue(functionGeneratorHelper.invokeFunc(func, Boolean.class, dt1, dt2));

        dt1 = ZonedDateTime.of(2022, 10, 13, 16, 0, 0, 0, ZoneId.of("Europe/Brussels"));
        dt2 = ZonedDateTime.of(2022, 10, 13, 14, 0, 0, 0, ZoneId.of("Europe/London"));
        assertTrue(functionGeneratorHelper.invokeFunc(func, Boolean.class, dt1, dt2));
    }

    @Test
    void funcWithListOfIntDistinct() {
        var model = """
                namespace com.rosetta.test.model
                version "${project.version}"

                type Foo:
                	n int (0..*)

                func DistinctFunc:
                	inputs:
                		foo Foo (0..1)
                	output:
                		res int (0..*)
                	set res: foo -> n distinct
                """;
        var code = generatorTestHelper.generateCode(model);
        var f = code.get("com.rosetta.test.model.functions.DistinctFunc");
        assertJavaEquals(
                """
				package com.rosetta.test.model.functions;

				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.test.model.Foo;
				import java.util.ArrayList;
				import java.util.List;

				import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.*;

				@ImplementedBy(DistinctFunc.DistinctFuncDefault.class)
				public abstract class DistinctFunc implements RosettaFunction {

					/**
					* @param foo\s
					* @return res\s
					*/
					public List<Integer> evaluate(Foo foo) {
						List<Integer> res = doEvaluate(foo);
				\t\t
						return res;
					}

					protected abstract List<Integer> doEvaluate(Foo foo);

					public static class DistinctFuncDefault extends DistinctFunc {
						@Override
						protected List<Integer> doEvaluate(Foo foo) {
							List<Integer> res = new ArrayList<>();
							return assignOutput(res, foo);
						}
				\t\t
						protected List<Integer> assignOutput(List<Integer> res, Foo foo) {
							res = distinctIgnoringPrecision(MapperS.of(foo).<Integer>mapC("getN", _foo -> _foo.getN())).getMulti();
				\t\t\t
							return res;
						}
					}
				}
				""",
                f
        );
        var classes = generatorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "DistinctFunc");
        var foo = generatorTestHelper.createInstanceUsingBuilder(classes, "Foo", Map.of(), Map.of("n", ImmutableList.of(1, 1, 1, 2, 2, 3)));
        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, foo);
        assertEquals(3, res.size());
        assertThat(res, hasItems((Object) 1, 2, 3));
    }

    @Test
    void funcWithListOfIntDistinct2() {
        var model = """
                namespace com.rosetta.test.model
                version "${project.version}"

                func DistinctFunc:
                	inputs:
                		n int (0..*)
                	output:
                		res int (0..*)
                	set res: n distinct
                """;
        var code = generatorTestHelper.generateCode(model);
        var f = code.get("com.rosetta.test.model.functions.DistinctFunc");
        assertJavaEquals(
                """
				package com.rosetta.test.model.functions;

				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import java.util.ArrayList;
				import java.util.Collections;
				import java.util.List;

				import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.*;

				@ImplementedBy(DistinctFunc.DistinctFuncDefault.class)
				public abstract class DistinctFunc implements RosettaFunction {

					/**
					* @param n\s
					* @return res\s
					*/
					public List<Integer> evaluate(List<Integer> n) {
						List<Integer> res = doEvaluate(n);
				\t\t
						return res;
					}

					protected abstract List<Integer> doEvaluate(List<Integer> n);

					public static class DistinctFuncDefault extends DistinctFunc {
						@Override
						protected List<Integer> doEvaluate(List<Integer> n) {
							if (n == null) {
								n = Collections.emptyList();
							}
							List<Integer> res = new ArrayList<>();
							return assignOutput(res, n);
						}
				\t\t
						protected List<Integer> assignOutput(List<Integer> res, List<Integer> n) {
							res = distinctIgnoringPrecision(MapperC.<Integer>of(n)).getMulti();
				\t\t\t
							return res;
						}
					}
				}
				""",
                f
        );
        var classes = generatorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "DistinctFunc");
        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, ImmutableList.of(1, 1, 1, 2, 2, 3));
        assertEquals(3, res.size());
        assertThat(res, hasItems((Object) 1, 2, 3));
    }

    @Test
    void funcWithListOfStringDistinct() {
        var code = generatorTestHelper.generateCode("""
                namespace com.rosetta.test.model
                version "${project.version}"

                type Foo:
                	n string (0..*)

                func DistinctFunc:
                	inputs:
                		foo Foo (0..1)
                	output:
                		res string (0..*)
                	add res: foo -> n distinct
                """);
        var classes = generatorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "DistinctFunc");
        var foo = generatorTestHelper.createInstanceUsingBuilder(classes, "Foo", Map.of(), Map.of("n", ImmutableList.of("1", "1", "1", "2", "2", "3")));
        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, foo);
        assertEquals(3, res.size());
        assertThat(res, hasItems((Object) "1", "2", "3"));
    }

    @Test
    void funcWithListOfStringDistinct2() {
        var code = generatorTestHelper.generateCode("""
                namespace com.rosetta.test.model
                version "${project.version}"

                func DistinctFunc:
                	inputs:
                		n string (0..*)
                	output:
                		res string (0..*)
                	add res: n distinct
                """);
        var classes = generatorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "DistinctFunc");
        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, ImmutableList.of("1", "1", "1", "2", "2", "3"));
        assertEquals(3, res.size());
        assertThat(res, hasItems((Object) "1", "2", "3"));
    }

    @Test
    void funcWithListOfComplexTypeDistinct() {
        var model = """
                namespace com.rosetta.test.model
                version "${project.version}"

                type Foo:
                	barList Bar (0..*)

                type Bar:
                	n int (0..1)

                func DistinctFunc:
                	inputs:
                		foo Foo (0..1)
                	output:
                		res Bar (0..*)
                	add res: foo -> barList distinct
                """;
        var code = generatorTestHelper.generateCode(model);
        var f = code.get("com.rosetta.test.model.functions.DistinctFunc");
        assertJavaEquals(
                """
				package com.rosetta.test.model.functions;

				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.ModelObjectValidator;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.test.model.Bar;
				import com.rosetta.test.model.Foo;
				import java.util.ArrayList;
				import java.util.List;
				import java.util.Optional;
				import java.util.stream.Collectors;
				import javax.inject.Inject;

				import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.*;

				@ImplementedBy(DistinctFunc.DistinctFuncDefault.class)
				public abstract class DistinctFunc implements RosettaFunction {
				\t
					@Inject protected ModelObjectValidator objectValidator;

					/**
					* @param foo\s
					* @return res\s
					*/
					public List<? extends Bar> evaluate(Foo foo) {
						List<Bar.BarBuilder> resBuilder = doEvaluate(foo);
				\t\t
						final List<? extends Bar> res;
						if (resBuilder == null) {
							res = null;
						} else {
							res = resBuilder.stream().map(Bar::build).collect(Collectors.toList());
							objectValidator.validate(Bar.class, res);
						}
				\t\t
						return res;
					}

					protected abstract List<Bar.BarBuilder> doEvaluate(Foo foo);

					public static class DistinctFuncDefault extends DistinctFunc {
						@Override
						protected List<Bar.BarBuilder> doEvaluate(Foo foo) {
							List<Bar.BarBuilder> res = new ArrayList<>();
							return assignOutput(res, foo);
						}
				\t\t
						protected List<Bar.BarBuilder> assignOutput(List<Bar.BarBuilder> res, Foo foo) {
							res.addAll(toBuilder(distinctIgnoringPrecision(MapperS.of(foo).<Bar>mapC("getBarList", _foo -> _foo.getBarList())).getMulti()));
				\t\t\t
							return Optional.ofNullable(res)
								.map(o -> o.stream().map(i -> i.prune()).collect(Collectors.toList()))
								.orElse(null);
						}
					}
				}
				""",
                f
        );
        var classes = generatorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "DistinctFunc");

        var bar1 = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", Map.of("n", 1), Map.of());
        var bar2 = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", Map.of("n", 2), Map.of());
        var bar3 = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", Map.of("n", 3), Map.of());

        var barList = new java.util.ArrayList<>();
        barList.add(bar1);
        barList.add(bar1);
        barList.add(bar1);
        barList.add(bar2);
        barList.add(bar2);
        barList.add(bar3);

        var foo = generatorTestHelper.createInstanceUsingBuilder(classes, "Foo", Map.of(), Map.of("barList", barList));

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, foo);
        assertEquals(3, res.size());
        assertThat(res, hasItems((Object) bar1, bar2, bar3));
    }

    @Test
    void funcWithListOfComplexTypeDistinct2() {
        var model = """
                namespace com.rosetta.test.model
                version "${project.version}"

                type Bar:
                	n int (0..1)

                func DistinctFunc:
                	inputs:
                		barList Bar (0..*)
                	output:
                		res Bar (0..*)
                	add res: barList distinct
                """;
        var code = generatorTestHelper.generateCode(model);
        var f = code.get("com.rosetta.test.model.functions.DistinctFunc");
        assertJavaEquals(
                """
				package com.rosetta.test.model.functions;

				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.ModelObjectValidator;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import com.rosetta.test.model.Bar;
				import java.util.ArrayList;
				import java.util.Collections;
				import java.util.List;
				import java.util.Optional;
				import java.util.stream.Collectors;
				import javax.inject.Inject;

				import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.*;

				@ImplementedBy(DistinctFunc.DistinctFuncDefault.class)
				public abstract class DistinctFunc implements RosettaFunction {
				\t
					@Inject protected ModelObjectValidator objectValidator;

					/**
					* @param barList\s
					* @return res\s
					*/
					public List<? extends Bar> evaluate(List<? extends Bar> barList) {
						List<Bar.BarBuilder> resBuilder = doEvaluate(barList);
				\t\t
						final List<? extends Bar> res;
						if (resBuilder == null) {
							res = null;
						} else {
							res = resBuilder.stream().map(Bar::build).collect(Collectors.toList());
							objectValidator.validate(Bar.class, res);
						}
				\t\t
						return res;
					}

					protected abstract List<Bar.BarBuilder> doEvaluate(List<? extends Bar> barList);

					public static class DistinctFuncDefault extends DistinctFunc {
						@Override
						protected List<Bar.BarBuilder> doEvaluate(List<? extends Bar> barList) {
							if (barList == null) {
								barList = Collections.emptyList();
							}
							List<Bar.BarBuilder> res = new ArrayList<>();
							return assignOutput(res, barList);
						}
				\t\t
						protected List<Bar.BarBuilder> assignOutput(List<Bar.BarBuilder> res, List<? extends Bar> barList) {
							res.addAll(toBuilder(distinctIgnoringPrecision(MapperC.<Bar>of(barList)).getMulti()));
				\t\t\t
							return Optional.ofNullable(res)
								.map(o -> o.stream().map(i -> i.prune()).collect(Collectors.toList()))
								.orElse(null);
						}
					}
				}
				""",
                f
        );
        var classes = generatorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "DistinctFunc");

        var bar1 = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", Map.of("n", 1), Map.of());
        var bar2 = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", Map.of("n", 2), Map.of());
        var bar3 = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", Map.of("n", 3), Map.of());

        var barList = new java.util.ArrayList<>();
        barList.add(bar1);
        barList.add(bar1);
        barList.add(bar1);
        barList.add(bar2);
        barList.add(bar2);
        barList.add(bar3);

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, barList);
        assertEquals(3, res.size());
        assertThat(res, hasItems((Object) bar1, bar2, bar3));
    }

    @Test
    void funcWithListOfStringDistinctThenOnlyElement() {
        var code = generatorTestHelper.generateCode("""
                namespace com.rosetta.test.model
                version "${project.version}"

                type Foo:
                	n string (0..*)

                func DistinctFunc:
                	inputs:
                		foo Foo (0..1)
                	output:
                		res string (0..1)
                	set res: foo -> n distinct only-element
                """);
        var classes = generatorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "DistinctFunc");
        var foo = generatorTestHelper.createInstanceUsingBuilder(classes, "Foo", Map.of(), Map.of("n", ImmutableList.of("1", "1", "1")));
        var res = functionGeneratorHelper.invokeFunc(func, String.class, foo);
        assertEquals("1", res);
    }

    @Test
    void funcWithListOfStringDistinctThenOnlyElement2() {
        var code = generatorTestHelper.generateCode("""
                namespace com.rosetta.test.model
                version "${project.version}"

                func DistinctFunc:
                	inputs:
                		n string (0..*)
                	output:
                		res string (0..1)
                	set res: n distinct only-element
                """);
        var classes = generatorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "DistinctFunc");
        var res = functionGeneratorHelper.invokeFunc(func, String.class, ImmutableList.of("1", "1", "1"));
        assertEquals("1", res);
    }

    @Test
    void funcWithListOfStringDistinctThenOnlyElement3() {
        var code = generatorTestHelper.generateCode("""
                namespace com.rosetta.test.model
                version "${project.version}"

                func DistinctFunc:
                	inputs:
                		n string (0..*)
                	output:
                		res string (0..1)
                	alias x:
                		n distinct only-element
                	set res:
                		x
                """);
        var classes = generatorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "DistinctFunc");
        var res = functionGeneratorHelper.invokeFunc(func, String.class, ImmutableList.of("1", "1", "1"));
        assertEquals("1", res);
    }

    @Test
    void funcWithListOfStringDistinctThenOnlyElement4() {
        var code = generatorTestHelper.generateCode("""
                namespace com.rosetta.test.model
                version "${project.version}"

                func DistinctFunc:
                	inputs:
                		n string (0..*)
                	output:
                		res string (0..1)
                	alias x:
                		n
                	set res:
                		x distinct only-element
                """);
        var classes = generatorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "DistinctFunc");
        var res = functionGeneratorHelper.invokeFunc(func, String.class, ImmutableList.of("1", "1", "1"));
        assertEquals("1", res);
    }

    @Test
    void funcOnlyElementAnyMultiple() {
        generatorTestHelper.compileToClasses(generatorTestHelper.generateCode(modelHelper.parseRosettaWithNoErrors("""
                namespace "demo"
                version "${project.version}"

                type Type1:
                		t Type2 (1..1)
                		ts Type2 (1..*)
                type Type2:
                		num number (1..1)
                		nums number (1..*)

                func Func1:
                	inputs: t1 Type1(1..1)
                	output: res number (1..1)
                	set res: t1->ts->num only-element
                """)));
    }

    @Test
    void funcOnlyElementOnlySingle() {
        var model = modelHelper.parseRosetta("""
                namespace "demo"
                version "${project.version}"

                type T1:
                		t T2 (1..1)
                		ts T2 (1..*)
                type T2:
                		num number (1..1)
                		nums number (1..*)

                func F1:
                	inputs: t1 T1(1..1)
                	output: res number (1..1)
                	set res: t1->t->num only-element
                """);
        validationTestHelper.assertWarning(model, ROSETTA_ONLY_ELEMENT, null,
                "List only-element operation cannot be used for single cardinality expressions.");
    }

    @Test
    void nestedIfElse() {
        var model = modelHelper.parseRosettaWithNoErrors("""
                  namespace "demo"
                  version "${project.version}"

                  func IfElseTest:
                  inputs:
                  	s1 string (1..1)
                  	s2 string (1..1)
                  output: result string (1..1)

                  set result:
                  	if s1 = "1"
                  		then if s2 = "a"
                  			then "result1a"
                  		else
                  			if s2 = "b"
                  				then "result1b"
                  	else
                  		"result1"
                  	else if s1 = "2" then
                  		if s2 = "a"
                  		then "result2a"
                  		else if s2 = "b"
                  		then "result2b"
                  		else "result2"
                  		else
                  "result"
                  """);
        generatorTestHelper.compileToClasses(generatorTestHelper.generateCode(model));
    }

    @Test
    void mathOperationInsideIfStatement() {
        var model = modelHelper.parseRosettaWithNoErrors("""
                namespace "demo"
                version "${project.version}"

                func AddInsideIf:
                	inputs:
                		i1 int (1..1)
                		i2 int (1..1)
                		b boolean (1..1)
                	output: result int (1..1)

                	set result:
                		if b = True
                		then i1 + i2
                		else 0
                """);
        generatorTestHelper.compileToClasses(generatorTestHelper.generateCode(model));
    }

    @Test
    void assignOutputOnResolvedQuantity() {
        var model = modelHelper.parseRosettaWithNoErrors("""
                namespace "demo"
                version "${project.version}"

                type Quantity:
                	amount number (1..1)

                type PriceQuantity:
                	[metadata key]
                	quantity Quantity (0..*)
                	[metadata location]

                type ResolvablePayoutQuantity:
                	resolvedQuantity Quantity (0..1)
                	[metadata address "pointsTo"=PriceQuantity->quantity]

                type Cashflow:
                	payoutQuantity ResolvablePayoutQuantity (1..1)

                func InterestCashSettlementAmount:
                	inputs:
                		x number (1..1)
                	output:
                		cashflow Cashflow (1..1)

                set cashflow -> payoutQuantity -> resolvedQuantity -> amount:
                	x
                """);
        generatorTestHelper.compileToClasses(generatorTestHelper.generateCode(model));
    }

    @Test
    void ifWithSingleStringType() {
        var model = """
                func FuncFoo:
                	inputs:
                		test boolean (1..1)
                		t1 string  (1..1)
                		t2 string (1..1)
                	output:
                		result string (1..1)

                	set result:
                		if test = True
                		then t1
                		else t2
                """;
        var code = generatorTestHelper.generateCode(model);
        var f = code.get("com.rosetta.test.model.functions.FuncFoo");
        assertJavaEquals(
                """
				package com.rosetta.test.model.functions;

				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.expression.CardinalityOperator;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperS;

				import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.*;

				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {

					/**
					* @param test\s
					* @param t1\s
					* @param t2\s
					* @return result\s
					*/
					public String evaluate(Boolean test, String t1, String t2) {
						String result = doEvaluate(test, t1, t2);
				\t\t
						return result;
					}

					protected abstract String doEvaluate(Boolean test, String t1, String t2);

					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected String doEvaluate(Boolean test, String t1, String t2) {
							String result = null;
							return assignOutput(result, test, t1, t2);
						}
				\t\t
						protected String assignOutput(String result, Boolean test, String t1, String t2) {
							if (areEqual(MapperS.of(test), MapperS.of(true), CardinalityOperator.All).getOrDefault(false)) {
								result = t1;
							} else {
								result = t2;
							}
				\t\t\t
							return result;
						}
					}
				}
				""",
                f
        );
        generatorTestHelper.compileToClasses(code);
    }

    @Test
    void ifWithMultipleStringType() {
        var model = """
                func FuncFoo:
                	inputs:
                		test boolean (1..1)
                		t1 string  (1..*)
                		t2 string (1..*)
                	output:
                		result string (1..*)

                	add result:
                		if test = True
                		then t1
                		else t2
                """;
        var code = generatorTestHelper.generateCode(model);
        var f = code.get("com.rosetta.test.model.functions.FuncFoo");
        assertJavaEquals(
                """
				package com.rosetta.test.model.functions;

				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.expression.CardinalityOperator;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperS;
				import java.util.ArrayList;
				import java.util.Collections;
				import java.util.List;

				import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.*;

				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {

					/**
					* @param test\s
					* @param t1\s
					* @param t2\s
					* @return result\s
					*/
					public List<String> evaluate(Boolean test, List<String> t1, List<String> t2) {
						List<String> result = doEvaluate(test, t1, t2);
				\t\t
						return result;
					}

					protected abstract List<String> doEvaluate(Boolean test, List<String> t1, List<String> t2);

					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected List<String> doEvaluate(Boolean test, List<String> t1, List<String> t2) {
							if (t1 == null) {
								t1 = Collections.emptyList();
							}
							if (t2 == null) {
								t2 = Collections.emptyList();
							}
							List<String> result = new ArrayList<>();
							return assignOutput(result, test, t1, t2);
						}
				\t\t
						protected List<String> assignOutput(List<String> result, Boolean test, List<String> t1, List<String> t2) {
							if (areEqual(MapperS.of(test), MapperS.of(true), CardinalityOperator.All).getOrDefault(false)) {
								result.addAll(t1);
							} else {
								result.addAll(t2);
							}
				\t\t\t
							return result;
						}
					}
				}
				""",
                f
        );
        generatorTestHelper.compileToClasses(code);
    }

    @Test
    void ifWithSingleNumberType() {
        var model = """
                func FuncFoo:
                	inputs:
                		test boolean (1..1)
                		t1 number  (1..1)
                		t2 number (1..1)
                	output:
                		result number (1..1)

                	set result:
                		if test = True
                		then t1
                		else t2
                """;
        var code = generatorTestHelper.generateCode(model);
        var f = code.get("com.rosetta.test.model.functions.FuncFoo");
        assertJavaEquals(
                """
				package com.rosetta.test.model.functions;

				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.expression.CardinalityOperator;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperS;
				import java.math.BigDecimal;

				import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.*;

				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {

					/**
					* @param test\s
					* @param t1\s
					* @param t2\s
					* @return result\s
					*/
					public BigDecimal evaluate(Boolean test, BigDecimal t1, BigDecimal t2) {
						BigDecimal result = doEvaluate(test, t1, t2);
				\t\t
						return result;
					}

					protected abstract BigDecimal doEvaluate(Boolean test, BigDecimal t1, BigDecimal t2);

					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected BigDecimal doEvaluate(Boolean test, BigDecimal t1, BigDecimal t2) {
							BigDecimal result = null;
							return assignOutput(result, test, t1, t2);
						}
				\t\t
						protected BigDecimal assignOutput(BigDecimal result, Boolean test, BigDecimal t1, BigDecimal t2) {
							if (areEqual(MapperS.of(test), MapperS.of(true), CardinalityOperator.All).getOrDefault(false)) {
								result = t1;
							} else {
								result = t2;
							}
				\t\t\t
							return result;
						}
					}
				}
				""",
                f
        );
        generatorTestHelper.compileToClasses(code);
    }

    @Test
    void ifWithMultipleNumberType() {
        var model = """
                func FuncFoo:
                	inputs:
                		test boolean (1..1)
                		t1 number  (1..*)
                		t2 number (1..*)
                	output:
                		result number (1..*)

                	add result:
                		if test = True
                		then t1
                		else t2
                """;
        var code = generatorTestHelper.generateCode(model);
        var f = code.get("com.rosetta.test.model.functions.FuncFoo");
        assertJavaEquals(
                """
				package com.rosetta.test.model.functions;

				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.expression.CardinalityOperator;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperS;
				import java.math.BigDecimal;
				import java.util.ArrayList;
				import java.util.Collections;
				import java.util.List;

				import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.*;

				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {

					/**
					* @param test\s
					* @param t1\s
					* @param t2\s
					* @return result\s
					*/
					public List<BigDecimal> evaluate(Boolean test, List<BigDecimal> t1, List<BigDecimal> t2) {
						List<BigDecimal> result = doEvaluate(test, t1, t2);
				\t\t
						return result;
					}

					protected abstract List<BigDecimal> doEvaluate(Boolean test, List<BigDecimal> t1, List<BigDecimal> t2);

					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected List<BigDecimal> doEvaluate(Boolean test, List<BigDecimal> t1, List<BigDecimal> t2) {
							if (t1 == null) {
								t1 = Collections.emptyList();
							}
							if (t2 == null) {
								t2 = Collections.emptyList();
							}
							List<BigDecimal> result = new ArrayList<>();
							return assignOutput(result, test, t1, t2);
						}
				\t\t
						protected List<BigDecimal> assignOutput(List<BigDecimal> result, Boolean test, List<BigDecimal> t1, List<BigDecimal> t2) {
							if (areEqual(MapperS.of(test), MapperS.of(true), CardinalityOperator.All).getOrDefault(false)) {
								result.addAll(t1);
							} else {
								result.addAll(t2);
							}
				\t\t\t
							return result;
						}
					}
				}
				""",
                f
        );
        generatorTestHelper.compileToClasses(code);
    }

    @Test
    void ifWithSingleDataType() {
        var model = """
                func FuncFoo:
                	inputs:
                		test boolean (1..1)
                		b1 Bar (1..1)
                		b2 Bar (1..1)
                	output:
                		result Bar (1..1)

                	set result:
                		if test = True
                		then b1
                		else b2

                type Bar:
                	s1 string (1..1)
                """;
        var code = generatorTestHelper.generateCode(model);
        var f = code.get("com.rosetta.test.model.functions.FuncFoo");
        assertJavaEquals(
                """
				package com.rosetta.test.model.functions;

				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.expression.CardinalityOperator;
				import com.rosetta.model.lib.functions.ModelObjectValidator;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.test.model.Bar;
				import java.util.Optional;
				import javax.inject.Inject;

				import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.*;

				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
				\t
					@Inject protected ModelObjectValidator objectValidator;

					/**
					* @param test\s
					* @param b1\s
					* @param b2\s
					* @return result\s
					*/
					public Bar evaluate(Boolean test, Bar b1, Bar b2) {
						Bar.BarBuilder resultBuilder = doEvaluate(test, b1, b2);
				\t\t
						final Bar result;
						if (resultBuilder == null) {
							result = null;
						} else {
							result = resultBuilder.build();
							objectValidator.validate(Bar.class, result);
						}
				\t\t
						return result;
					}

					protected abstract Bar.BarBuilder doEvaluate(Boolean test, Bar b1, Bar b2);

					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected Bar.BarBuilder doEvaluate(Boolean test, Bar b1, Bar b2) {
							Bar.BarBuilder result = Bar.builder();
							return assignOutput(result, test, b1, b2);
						}
				\t\t
						protected Bar.BarBuilder assignOutput(Bar.BarBuilder result, Boolean test, Bar b1, Bar b2) {
							if (areEqual(MapperS.of(test), MapperS.of(true), CardinalityOperator.All).getOrDefault(false)) {
								result = toBuilder(b1);
							} else {
								result = toBuilder(b2);
							}
				\t\t\t
							return Optional.ofNullable(result)
								.map(o -> o.prune())
								.orElse(null);
						}
					}
				}
				""",
                f
        );
        generatorTestHelper.compileToClasses(code);
    }

    @Test
    void ifWithMultipleDataTypes() {
        var model = """
                func FuncFoo:
                	inputs:
                		test boolean (1..1)
                		b1 Bar (1..*)
                		b2 Bar (1..*)
                	output:
                		result Bar (1..*)

                	add result:
                		if test = True
                		then b1
                		else b2

                type Bar:
                	s1 string (1..1)
                """;
        var code = generatorTestHelper.generateCode(model);
        var f = code.get("com.rosetta.test.model.functions.FuncFoo");
        assertJavaEquals(
                """
				package com.rosetta.test.model.functions;

				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.expression.CardinalityOperator;
				import com.rosetta.model.lib.functions.ModelObjectValidator;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.test.model.Bar;
				import java.util.ArrayList;
				import java.util.Collections;
				import java.util.List;
				import java.util.Optional;
				import java.util.stream.Collectors;
				import javax.inject.Inject;

				import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.*;

				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
				\t
					@Inject protected ModelObjectValidator objectValidator;

					/**
					* @param test\s
					* @param b1\s
					* @param b2\s
					* @return result\s
					*/
					public List<? extends Bar> evaluate(Boolean test, List<? extends Bar> b1, List<? extends Bar> b2) {
						List<Bar.BarBuilder> resultBuilder = doEvaluate(test, b1, b2);
				\t\t
						final List<? extends Bar> result;
						if (resultBuilder == null) {
							result = null;
						} else {
							result = resultBuilder.stream().map(Bar::build).collect(Collectors.toList());
							objectValidator.validate(Bar.class, result);
						}
				\t\t
						return result;
					}

					protected abstract List<Bar.BarBuilder> doEvaluate(Boolean test, List<? extends Bar> b1, List<? extends Bar> b2);

					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected List<Bar.BarBuilder> doEvaluate(Boolean test, List<? extends Bar> b1, List<? extends Bar> b2) {
							if (b1 == null) {
								b1 = Collections.emptyList();
							}
							if (b2 == null) {
								b2 = Collections.emptyList();
							}
							List<Bar.BarBuilder> result = new ArrayList<>();
							return assignOutput(result, test, b1, b2);
						}
				\t\t
						protected List<Bar.BarBuilder> assignOutput(List<Bar.BarBuilder> result, Boolean test, List<? extends Bar> b1, List<? extends Bar> b2) {
							if (areEqual(MapperS.of(test), MapperS.of(true), CardinalityOperator.All).getOrDefault(false)) {
								result.addAll(toBuilder(b1));
							} else {
								result.addAll(toBuilder(b2));
							}
				\t\t\t
							return Optional.ofNullable(result)
								.map(o -> o.stream().map(i -> i.prune()).collect(Collectors.toList()))
								.orElse(null);
						}
					}
				}
				""",
                f
        );
        generatorTestHelper.compileToClasses(code);
    }

    @Test
    void shouldSetMathsOperation() {
        var model = """
                func FuncFoo:
                	inputs:
                		n1 number (1..1)
                		n2 number (1..1)
                	output:
                		res number (1..1)

                	set res:
                		n1 * n2
                """;
        var code = generatorTestHelper.generateCode(model);
        var f = code.get("com.rosetta.test.model.functions.FuncFoo");
        assertJavaEquals(
                """
				package com.rosetta.test.model.functions;

				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.expression.MapperMaths;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperS;
				import java.math.BigDecimal;


				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {

					/**
					* @param n1\s
					* @param n2\s
					* @return res\s
					*/
					public BigDecimal evaluate(BigDecimal n1, BigDecimal n2) {
						BigDecimal res = doEvaluate(n1, n2);
				\t\t
						return res;
					}

					protected abstract BigDecimal doEvaluate(BigDecimal n1, BigDecimal n2);

					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected BigDecimal doEvaluate(BigDecimal n1, BigDecimal n2) {
							BigDecimal res = null;
							return assignOutput(res, n1, n2);
						}
				\t\t
						protected BigDecimal assignOutput(BigDecimal res, BigDecimal n1, BigDecimal n2) {
							res = MapperMaths.<BigDecimal, BigDecimal, BigDecimal>multiply(MapperS.of(n1), MapperS.of(n2)).get();
				\t\t\t
							return res;
						}
					}
				}
				""",
                f
        );
        generatorTestHelper.compileToClasses(code);
    }

    @Test
    void shouldSetList() {
        var model = """
                type Foo:
                	outList string (0..*)

                func FuncFoo:
                	inputs:
                		inList string (0..*)
                	output:
                		foo Foo (1..1)

                	set foo -> outList:
                		inList
                """;
        var code = generatorTestHelper.generateCode(model);
        var f = code.get("com.rosetta.test.model.functions.FuncFoo");
        assertJavaEquals(
                """
				package com.rosetta.test.model.functions;

				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.ModelObjectValidator;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.test.model.Foo;
				import java.util.Collections;
				import java.util.List;
				import java.util.Optional;
				import javax.inject.Inject;


				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
				\t
					@Inject protected ModelObjectValidator objectValidator;

					/**
					* @param inList\s
					* @return foo\s
					*/
					public Foo evaluate(List<String> inList) {
						Foo.FooBuilder fooBuilder = doEvaluate(inList);
				\t\t
						final Foo foo;
						if (fooBuilder == null) {
							foo = null;
						} else {
							foo = fooBuilder.build();
							objectValidator.validate(Foo.class, foo);
						}
				\t\t
						return foo;
					}

					protected abstract Foo.FooBuilder doEvaluate(List<String> inList);

					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected Foo.FooBuilder doEvaluate(List<String> inList) {
							if (inList == null) {
								inList = Collections.emptyList();
							}
							Foo.FooBuilder foo = Foo.builder();
							return assignOutput(foo, inList);
						}
				\t\t
						protected Foo.FooBuilder assignOutput(Foo.FooBuilder foo, List<String> inList) {
							foo
								.setOutList(inList);
				\t\t\t
							return Optional.ofNullable(foo)
								.map(o -> o.prune())
								.orElse(null);
						}
					}
				}
				""",
                f
        );
        generatorTestHelper.compileToClasses(code);
    }

    @Test
    void shouldAddList() {
        var model = """
                type Foo:
                	outList string (0..*)

                func FuncFoo:
                	inputs:
                		inList string (0..*)
                	output:
                		foo Foo (1..1)

                	add foo -> outList:
                		inList
                """;
        var code = generatorTestHelper.generateCode(model);
        var f = code.get("com.rosetta.test.model.functions.FuncFoo");
        assertJavaEquals(
                """
				package com.rosetta.test.model.functions;

				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.ModelObjectValidator;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.test.model.Foo;
				import java.util.Collections;
				import java.util.List;
				import java.util.Optional;
				import javax.inject.Inject;


				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
				\t
					@Inject protected ModelObjectValidator objectValidator;

					/**
					* @param inList\s
					* @return foo\s
					*/
					public Foo evaluate(List<String> inList) {
						Foo.FooBuilder fooBuilder = doEvaluate(inList);
				\t\t
						final Foo foo;
						if (fooBuilder == null) {
							foo = null;
						} else {
							foo = fooBuilder.build();
							objectValidator.validate(Foo.class, foo);
						}
				\t\t
						return foo;
					}

					protected abstract Foo.FooBuilder doEvaluate(List<String> inList);

					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected Foo.FooBuilder doEvaluate(List<String> inList) {
							if (inList == null) {
								inList = Collections.emptyList();
							}
							Foo.FooBuilder foo = Foo.builder();
							return assignOutput(foo, inList);
						}
				\t\t
						protected Foo.FooBuilder assignOutput(Foo.FooBuilder foo, List<String> inList) {
							foo
								.addOutList(inList);
				\t\t\t
							return Optional.ofNullable(foo)
								.map(o -> o.prune())
								.orElse(null);
						}
					}
				}
				""",
                f
        );
        generatorTestHelper.compileToClasses(code);
    }

    @Test
    void shouldMergeComplexTypeList() {
        var model = """
                type Foo:
                	attr string (1..1)

                func FuncFoo:
                	inputs:
                		foos Foo (0..*)
                		newFoo Foo (1..1) <"Add single Foo">
                	output:
                		mergedFoos Foo (0..*)

                	set mergedFoos:
                		foos

                	add mergedFoos:
                		newFoo
                """;
        var code = generatorTestHelper.generateCode(model);
        var classes = generatorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");
        var foo1 = createFoo(classes, "1");
        var foo2 = createFoo(classes, "2");
        var newFoo = createFoo(classes, "3");
        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, newArrayList(foo1, foo2), newFoo);
        assertEquals(3, res.size());
        assertThat(res, hasItems((Object) foo1, foo2, newFoo));
    }

    @Test
    void shouldMergeComplexTypeList2() {
        var model = """
                type Foo:
                	attr string (1..1)

                func FuncFoo:
                	inputs:
                		foos Foo (0..*)
                		newFoo Foo (1..1) <"Add single Foo">
                	output:
                		mergedFoos Foo (0..*)

                	add mergedFoos:
                		foos

                	add mergedFoos:
                		newFoo
                """;
        var code = generatorTestHelper.generateCode(model);
        var classes = generatorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");
        var foo1 = createFoo(classes, "1");
        var foo2 = createFoo(classes, "2");
        var newFoo = createFoo(classes, "3");
        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, newArrayList(foo1, foo2), newFoo);
        assertEquals(3, res.size());
        assertThat(res, hasItems((Object) foo1, foo2, newFoo));
    }

    @Test
    void shouldMergeComplexTypeList3() {
        var model = """
                type Foo:
                	attr string (1..1)

                func FuncFoo:
                	inputs:
                		foos Foo (0..*)
                		newFoos Foo (0..*) <"Add Foo list">
                	output:
                		mergedFoos Foo (0..*)

                	add mergedFoos:
                		foos

                	add mergedFoos:
                		newFoos
                """;
        var code = generatorTestHelper.generateCode(model);
        var classes = generatorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");
        var foo1 = createFoo(classes, "1");
        var foo2 = createFoo(classes, "2");
        var foo3 = createFoo(classes, "3");
        var foo4 = createFoo(classes, "4");
        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, newArrayList(foo1, foo2), newArrayList(foo3, foo4));
        assertEquals(4, res.size());
        assertThat(res, hasItems((Object) foo1, foo2, foo3, foo4));
    }

    @Test
    void shouldMergeBasicTypeList() {
        var model = """
                func FuncFoo:
                	inputs:
                		foos string (0..*)
                		newFoo string (1..1) <"Add single Foo">
                	output:
                		mergedFoos string (0..*)

                	set mergedFoos:
                		foos

                	add mergedFoos:
                		newFoo
                """;
        var code = generatorTestHelper.generateCode(model);
        var classes = generatorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");
        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, newArrayList("1", "2"), "3");
        assertEquals(3, res.size());
        assertThat(res, hasItems((Object) "1", "2", "3"));
    }

    @Test
    void shouldMergeBasicTypeList2() {
        var model = """
                func FuncFoo:
                	inputs:
                		foos string (0..*)
                		newFoo string (1..1) <"Add single Foo">
                	output:
                		mergedFoos string (0..*)

                	add mergedFoos:
                		foos

                	add mergedFoos:
                		newFoo
                """;
        var code = generatorTestHelper.generateCode(model);
        var classes = generatorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");
        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, newArrayList("1", "2"), "3");
        assertEquals(3, res.size());
        assertThat(res, hasItems((Object) "1", "2", "3"));
    }

    @Test
    void shouldMergeBasicTypeList3() {
        var model = """
                func FuncFoo:
                	inputs:
                		foos string (0..*)
                		newFoos string (0..*) <"Add Foo list">
                	output:
                		mergedFoos string (0..*)

                	add mergedFoos:
                		foos

                	add mergedFoos:
                		newFoos
                """;
        var code = generatorTestHelper.generateCode(model);
        var classes = generatorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");
        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, newArrayList("1", "2"), newArrayList("3", "4"));
        assertEquals(4, res.size());
        assertThat(res, hasItems((Object) "1", "2", "3", "4"));
    }

    @Test
    void shouldAddComplexTypeList() throws Exception {
        var model = """
                type Bar:
                	foos Foo (0..*)

                type Foo:
                	attr string (1..1)

                func FuncFoo:
                	inputs:
                		bar Bar (1..1)
                		newFoo Foo (1..1) <"Add single Foo">
                	output:
                		updatedBar Bar (1..1)

                	set updatedBar:
                		bar

                	add updatedBar -> foos:
                		newFoo
                """;
        var code = generatorTestHelper.generateCode(model);
        var classes = generatorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, "1");
        var foo2 = createFoo(classes, "2");
        var bar = createBar(classes, newArrayList(foo1, foo2));
        var newFoo = createFoo(classes, "3");

        var res = functionGeneratorHelper.invokeFunc(func, RosettaModelObject.class, bar, newFoo);

        // reflective Bar.getFoos()
        @SuppressWarnings("unchecked")
        List<RosettaModelObject> foos = (List<RosettaModelObject>) res.getClass().getMethod("getFoos").invoke(res);

        assertEquals(3, foos.size());
        assertThat(foos, hasItems(foo1, foo2, newFoo)); // appends to existing list
    }

    @Test
    void shouldAddComplexTypeList2() throws Exception {
        var model = """
                type Bar:
                	foos Foo (0..*)

                type Foo:
                	attr string (1..1)

                func FuncFoo:
                	inputs:
                		bar Bar (1..1)
                		newFoos Foo (0..*) <"Add Foo list">
                	output:
                		updatedBar Bar (1..1)

                	set updatedBar:
                		bar

                	add updatedBar -> foos:
                		newFoos
                """;
        var code = generatorTestHelper.generateCode(model);
        var classes = generatorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, "1");
        var foo2 = createFoo(classes, "2");
        var bar = createBar(classes, newArrayList(foo1, foo2));
        var foo3 = createFoo(classes, "3");
        var foo4 = createFoo(classes, "4");

        var res = functionGeneratorHelper.invokeFunc(func, RosettaModelObject.class, bar, newArrayList(foo3, foo4));

        // reflective Bar.getFoos()
        @SuppressWarnings("unchecked")
        List<RosettaModelObject> foos = (List<RosettaModelObject>) res.getClass().getMethod("getFoos").invoke(res);

        assertEquals(4, foos.size());
        assertThat(foos, hasItems(foo1, foo2, foo3, foo4)); // appends to existing list
    }

    @Test
    void shouldSetComplexTypeList() throws Exception {
        var model = """
                type Bar:
                	foos Foo (0..*)

                type Foo:
                	attr string (1..1)

                func FuncFoo:
                	inputs:
                		bar Bar (1..1)
                		newFoo Foo (1..1) <"Add single Foo">
                	output:
                		updatedBar Bar (1..1)

                	set updatedBar:
                		bar

                	set updatedBar -> foos:
                		newFoo
                """;
        var code = generatorTestHelper.generateCode(model);
        var classes = generatorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, "1");
        var foo2 = createFoo(classes, "2");
        var bar = createBar(classes, newArrayList(foo1, foo2));
        var newFoo = createFoo(classes, "3");

        var res = functionGeneratorHelper.invokeFunc(func, RosettaModelObject.class, bar, newFoo);

        // reflective Bar.getFoos()
        @SuppressWarnings("unchecked")
        List<RosettaModelObject> foos = (List<RosettaModelObject>) res.getClass().getMethod("getFoos").invoke(res);

        assertEquals(1, foos.size());
        assertThat(foos, hasItems(newFoo)); // overwrites existing list
    }

    @Test
    void shouldSetComplexTypeList2() throws Exception {
        var model = """
                type Bar:
                	foos Foo (0..*)

                type Foo:
                	attr string (1..1)

                func FuncFoo:
                	inputs:
                		bar Bar (1..1)
                		newFoos Foo (0..*) <"Add Foo list">
                	output:
                		updatedBar Bar (1..1)

                	set updatedBar:
                		bar

                	set updatedBar -> foos:
                		newFoos
                """;
        var code = generatorTestHelper.generateCode(model);
        var classes = generatorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, "1");
        var foo2 = createFoo(classes, "2");
        var bar = createBar(classes, newArrayList(foo1, foo2));
        var foo3 = createFoo(classes, "3");
        var foo4 = createFoo(classes, "4");

        var res = functionGeneratorHelper.invokeFunc(func, RosettaModelObject.class, bar, newArrayList(foo3, foo4));

        // reflective Bar.getFoos()
        @SuppressWarnings("unchecked")
        List<RosettaModelObject> foos = (List<RosettaModelObject>) res.getClass().getMethod("getFoos").invoke(res);

        assertEquals(2, foos.size());
        assertThat(foos, hasItems(foo3, foo4)); // overwrites existing list
    }

    @Test
    void shouldAddBasicTypeList() throws Exception {
        var model = """
                type Baz:
                	attrList string (0..*)

                func FuncFoo:
                	inputs:
                		baz Baz (1..1)
                		s string (1..1) <"Add single">
                	output:
                		updatedBaz Baz (1..1)

                	set updatedBaz:
                		baz

                	add updatedBaz -> attrList:
                		s
                """;
        var code = generatorTestHelper.generateCode(model);
        var classes = generatorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");
        var baz = createBaz(classes, newArrayList("1", "2"));
        var res = functionGeneratorHelper.invokeFunc(func, RosettaModelObject.class, baz, "3");

        // reflective Baz.getAttrList()
        @SuppressWarnings("unchecked")
        List<String> attrList = (List<String>) res.getClass().getMethod("getAttrList").invoke(res);

        assertEquals(3, attrList.size());
        assertThat(attrList, hasItems("1", "2", "3")); // appends to existing list
    }

    @Test
    void shouldAddBasicTypeList2() throws Exception {
        var model = """
                type Baz:
                	attrList string (0..*)

                func FuncFoo:
                	inputs:
                		baz Baz (1..1)
                		sList string (0..*) <"Add list">
                	output:
                		updatedBaz Baz (1..1)

                	set updatedBaz:
                		baz

                	add updatedBaz -> attrList:
                		sList
                """;
        var code = generatorTestHelper.generateCode(model);
        var classes = generatorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");
        var baz = createBaz(classes, newArrayList("1", "2"));
        var res = functionGeneratorHelper.invokeFunc(func, RosettaModelObject.class, baz, newArrayList("3", "4"));

        // reflective Baz.getAttrList()
        @SuppressWarnings("unchecked")
        List<String> attrList = (List<String>) res.getClass().getMethod("getAttrList").invoke(res);

        assertEquals(4, attrList.size());
        assertThat(attrList, hasItems("1", "2", "3", "4")); // appends to existing list
    }

    @Test
    void shouldSetBasicTypeList() throws Exception {
        var model = """
                type Baz:
                	attrList string (0..*)

                func FuncFoo:
                	inputs:
                		baz Baz (1..1)
                		s string (1..1) <"Add single">
                	output:
                		updatedBaz Baz (1..1)

                	set updatedBaz:
                		baz

                	set updatedBaz -> attrList:
                		s
                """;
        var code = generatorTestHelper.generateCode(model);
        var classes = generatorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");
        var baz = createBaz(classes, newArrayList("1", "2"));
        var res = functionGeneratorHelper.invokeFunc(func, RosettaModelObject.class, baz, "3");

        // reflective Baz.getAttrList()
        @SuppressWarnings("unchecked")
        List<String> attrList = (List<String>) res.getClass().getMethod("getAttrList").invoke(res);

        assertEquals(1, attrList.size());
        assertThat(attrList, hasItems("3")); // overwrites existing list
    }

    @Test
    void shouldSetBasicTypeList2() throws Exception {
        var model = """
                type Baz:
                	attrList string (0..*)

                func FuncFoo:
                	inputs:
                		baz Baz (1..1)
                		sList string (0..*) <"Add list">
                	output:
                		updatedBaz Baz (1..1)

                	set updatedBaz:
                		baz

                	set updatedBaz -> attrList:
                		sList
                """;
        var code = generatorTestHelper.generateCode(model);
        var classes = generatorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");
        var baz = createBaz(classes, newArrayList("1", "2"));
        var res = functionGeneratorHelper.invokeFunc(func, RosettaModelObject.class, baz, newArrayList("3", "4"));

        // reflective Baz.getAttrList()
        @SuppressWarnings("unchecked")
        List<String> attrList = (List<String>) res.getClass().getMethod("getAttrList").invoke(res);

        assertEquals(2, attrList.size());
        assertThat(attrList, hasItems("3", "4")); // overwrites existing list
    }

    @Test
    void shouldCallFuncTwiceInCondition() {
        var model = """
                type Foo:
                	test boolean (1..1)
                	attr string (1..1)

                	condition Bar:
                		if test = True then
                			FuncFoo( attr, "x" )
                		else
                			FuncFoo( attr, "y" )

                func FuncFoo:
                	inputs:
                		a string (1..1)
                		b string (1..1)
                	output:
                		result boolean (1..1)
                """;
        var code = generatorTestHelper.generateCode(model);
        var f = code.get("com.rosetta.test.model.validation.datarule.FooBar");
        assertJavaEquals(
                """
				package com.rosetta.test.model.validation.datarule;

				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.annotations.RosettaDataRule;
				import com.rosetta.model.lib.expression.CardinalityOperator;
				import com.rosetta.model.lib.expression.ComparisonResult;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.model.lib.path.RosettaPath;
				import com.rosetta.model.lib.validation.ValidationResult;
				import com.rosetta.model.lib.validation.Validator;
				import com.rosetta.test.model.Foo;
				import com.rosetta.test.model.functions.FuncFoo;
				import java.util.Arrays;
				import java.util.Collections;
				import java.util.List;
				import javax.inject.Inject;

				import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.*;

				/**
				 * @version test
				 */
				@RosettaDataRule("FooBar")
				@ImplementedBy(FooBar.Default.class)
				public interface FooBar extends Validator<Foo> {
				\t
					String NAME = "FooBar";
					String DEFINITION = "if test = True then FuncFoo( attr, \\"x\\" ) else FuncFoo( attr, \\"y\\" )";
				\t
					class Default implements FooBar {
				\t
						@Inject protected FuncFoo funcFoo;
				\t\t
						@Override
						public List<ValidationResult<?>> getValidationResults(RosettaPath path, Foo foo) {
							ComparisonResult result = executeDataRule(foo);
							if (result.getOrDefault(true)) {
								return Arrays.asList(ValidationResult.success(NAME, ValidationResult.ValidationType.DATA_RULE, "Foo", path, DEFINITION));
							}
				\t\t\t
							String failureMessage = result.getError();
							if (failureMessage == null || failureMessage.contains("Null") || failureMessage == "") {
								failureMessage = "Condition has failed.";
							}
							return Arrays.asList(ValidationResult.failure(NAME, ValidationResult.ValidationType.DATA_RULE, "Foo", path, DEFINITION, failureMessage));
						}
				\t\t
						private ComparisonResult executeDataRule(Foo foo) {
							try {
								if (areEqual(MapperS.of(foo).<Boolean>map("getTest", _foo -> _foo.getTest()), MapperS.of(true), CardinalityOperator.All).getOrDefault(false)) {
									return ComparisonResult.ofNullSafe(MapperS.of(funcFoo.evaluate(MapperS.of(foo).<String>map("getAttr", _foo -> _foo.getAttr()).get(), "x")));
								}
								return ComparisonResult.ofNullSafe(MapperS.of(funcFoo.evaluate(MapperS.of(foo).<String>map("getAttr", _foo -> _foo.getAttr()).get(), "y")));
							}
							catch (Exception ex) {
								return ComparisonResult.failure(ex.getMessage());
							}
						}
					}
				\t
					@SuppressWarnings("unused")
					class NoOp implements FooBar {
				\t
						@Override
						public List<ValidationResult<?>> getValidationResults(RosettaPath path, Foo foo) {
							return Collections.emptyList();
						}
					}
				}
				""",
                f
        );
        generatorTestHelper.compileToClasses(code);
    }

    @Test
    void canUseNestedIfThenElseInsideFunctionCall() {
        var model = """
                func A:
                    inputs:
                        a boolean (1..1)
                    output:
                        result boolean (1..1)

                func B:
                    output:
                        result boolean (1..1)

                    set result:
                        A(if True then True else if False then True)
                """;
        var code = generatorTestHelper.generateCode(model);
        var f = code.get("com.rosetta.test.model.functions.B");
        assertJavaEquals(
                """
				package com.rosetta.test.model.functions;

				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import javax.inject.Inject;


				@ImplementedBy(B.BDefault.class)
				public abstract class B implements RosettaFunction {
				\t
					// RosettaFunction dependencies
					//
					@Inject protected A a;

					/**
					* @return result\s
					*/
					public Boolean evaluate() {
						Boolean result = doEvaluate();
				\t\t
						return result;
					}

					protected abstract Boolean doEvaluate();

					public static class BDefault extends B {
						@Override
						protected Boolean doEvaluate() {
							Boolean result = null;
							return assignOutput(result);
						}
				\t\t
						protected Boolean assignOutput(Boolean result) {
							final Boolean ifThenElseResult;
							if (true) {
								ifThenElseResult = true;
							} else if (false) {
								ifThenElseResult = true;
							} else {
								ifThenElseResult = null;
							}
							result = a.evaluate(ifThenElseResult);
				\t\t\t
							return result;
						}
					}
				}
				""",
                f
        );
        generatorTestHelper.compileToClasses(code);
    }

    @Test
    void shouldCompareDateExtractedFromZonedDateTime() {
        var model = """
                func IsDateGreaterThan:
                	inputs:
                        date date (1..1)
                        zonedDateTime zonedDateTime (1..1)
                	output:
                		result boolean (1..1)

                    set result:
                        date <= zonedDateTime -> date
                """;
        var code = generatorTestHelper.generateCode(model);
        var f = code.get("com.rosetta.test.model.functions.IsDateGreaterThan");
        assertJavaEquals(
                """
				package com.rosetta.test.model.functions;

				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.expression.CardinalityOperator;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.model.lib.records.Date;
				import java.time.ZonedDateTime;

				import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.*;

				@ImplementedBy(IsDateGreaterThan.IsDateGreaterThanDefault.class)
				public abstract class IsDateGreaterThan implements RosettaFunction {

					/**
					* @param date\s
					* @param zonedDateTime\s
					* @return result\s
					*/
					public Boolean evaluate(Date date, ZonedDateTime zonedDateTime) {
						Boolean result = doEvaluate(date, zonedDateTime);
				\t\t
						return result;
					}

					protected abstract Boolean doEvaluate(Date date, ZonedDateTime zonedDateTime);

					public static class IsDateGreaterThanDefault extends IsDateGreaterThan {
						@Override
						protected Boolean doEvaluate(Date date, ZonedDateTime zonedDateTime) {
							Boolean result = null;
							return assignOutput(result, date, zonedDateTime);
						}
				\t\t
						protected Boolean assignOutput(Boolean result, Date date, ZonedDateTime zonedDateTime) {
							result = lessThanEquals(MapperS.of(date), MapperS.of(zonedDateTime).<Date>map("Date", zdt -> Date.of(zdt.toLocalDate())), CardinalityOperator.All).get();
				\t\t\t
							return result;
						}
					}
				}
				""",
                f
        );
        generatorTestHelper.compileToClasses(code);
    }

    private RosettaModelObject createFoo(Map<String, Class<?>> classes, String attr) {
        return (RosettaModelObject) generatorTestHelper.createInstanceUsingBuilder(classes, "Foo", Map.of("attr", attr), Map.of());
    }

    private RosettaModelObject createBar(Map<String, Class<?>> classes, List<RosettaModelObject> foos) {
        return (RosettaModelObject) generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", Map.of(), Map.of("foos", foos));
    }

    private RosettaModelObject createBaz(Map<String, Class<?>> classes, List<String> attrList) {
        return (RosettaModelObject) generatorTestHelper.createInstanceUsingBuilder(classes, "Baz", Map.of(), Map.of("attrList", attrList));
    }

    @SafeVarargs
    private static <T> java.util.ArrayList<T> newArrayList(T... elements) {
        return new java.util.ArrayList<>(Arrays.asList(elements));
    }

    private static void assertJavaEquals(String expected, String actual) {
        // The legacy generator emits platform line separators (\r\n on Windows); normalize the
        // generated side to \n so it matches the expected Java text block, which the JLS keeps as \n.
        assertEquals(expected, actual == null ? null : actual.replace("\r\n", "\n"));
    }
}
