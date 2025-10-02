package com.regnosys.rosetta.types;

import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaNamed;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.ExpressionParser;
import com.regnosys.rosetta.tests.util.ModelHelper;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;
import com.regnosys.rosetta.types.builtin.RStringType;
import java.util.Collections;
import javax.inject.Inject;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class SubtypeRelationTest {
    @Inject
    private SubtypeRelation subtypeRelation;

    @Inject
    private ModelHelper modelHelper;

    @Inject
    private RObjectFactory rObjectFactory;

    @Inject
    private ExpressionParser expressionParser;

    @Inject
    private RosettaTypeProvider rosettaTypeProvider;

    @Inject
    private RBuiltinTypeService builtinTypeService;

    private Data getData(RosettaModel model, String name) {
        return (Data) model.getElements().stream()
                .filter(element -> element instanceof RosettaNamed)
                .map(element -> (RosettaNamed) element)
                .filter(named -> named.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private RosettaEnumeration getEnum(RosettaModel model, String name) {
        return (RosettaEnumeration) model.getElements().stream()
                .filter(element -> element instanceof RosettaNamed)
                .map(element -> (RosettaNamed) element)
                .filter(named -> named.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    @Test
    public void testJoinOnMetaSubTypesReturnsParent() {
        RosettaModel model = modelHelper.parseRosettaWithNoIssues("""
            type A:
            
            type B extends A:
            
            type C extends A:
            """);

        RMetaAnnotatedType fieldBType = rosettaTypeProvider.getRTypeOfSymbol(expressionParser.parseAttribute("""
            fieldB B (1..1)
                [metadata reference]
            """, Collections.singletonList(model)));

        RMetaAnnotatedType fieldCType = rosettaTypeProvider.getRTypeOfSymbol(expressionParser.parseAttribute("""
            fieldC C (1..1)
                [metadata reference]
            """, Collections.singletonList(model)));

        RDataType fieldA = rObjectFactory.buildRDataType(getData(model, "A"));

        RMetaAnnotatedType joined = subtypeRelation.join(fieldBType, fieldCType);

        Assertions.assertEquals(RMetaAnnotatedType.withNoMeta(fieldA), joined);
    }

    @Test
    public void testJoinOnSameBaseTypeWithMetaIsCorrect() {
        RMetaAnnotatedType fieldA = rosettaTypeProvider.getRTypeOfSymbol(expressionParser.parseAttribute("""
            fieldA string (1..1)
                [metadata scheme]
                [metadata reference]
            """));

        RMetaAnnotatedType fieldB = rosettaTypeProvider.getRTypeOfSymbol(expressionParser.parseAttribute("""
            fieldB string (1..1)
                [metadata scheme]
                [metadata address]
            """));

        RMetaAnnotatedType result = subtypeRelation.join(fieldA, fieldB);
        Assertions.assertInstanceOf(RStringType.class, result.getRType());
        RMetaAttribute resultMetaAttribute = result.getMetaAttributes().getFirst();
        Assertions.assertEquals("scheme", resultMetaAttribute.getName());
        Assertions.assertEquals(builtinTypeService.UNCONSTRAINED_STRING, resultMetaAttribute.getRType());
    }

    @Test
    public void testStringWithSchemeAndReferenceIsSubtypeOfRelationWithStringWithAddressAndLocation() {
        RMetaAnnotatedType fieldAType = rosettaTypeProvider.getRTypeOfSymbol(expressionParser.parseAttribute("""
            fieldA string (1..1)
                [metadata scheme]
                [metadata reference]
            """));

        RMetaAnnotatedType fieldBType = rosettaTypeProvider.getRTypeOfSymbol(expressionParser.parseAttribute("""
            fieldB string (1..1)
                [metadata address]
                [metadata location]
            """));

        Assertions.assertTrue(subtypeRelation.isSubtypeOf(fieldAType, fieldBType, true));
        Assertions.assertTrue(subtypeRelation.isSubtypeOf(fieldBType, fieldAType, true));
    }

    @Test
    public void testStringWithSchemeIsSubtypeOfStringWithScheme() {
        RMetaAnnotatedType fieldAType = rosettaTypeProvider.getRTypeOfSymbol(expressionParser.parseAttribute("""
            fieldA string (1..1)
                [metadata scheme]
            """));

        Assertions.assertTrue(subtypeRelation.isSubtypeOf(fieldAType, fieldAType, true));
    }

    @Test
    public void testExtendedTypeIsSubtype() {
        RosettaModel model = modelHelper.parseRosettaWithNoIssues("""
            type A:
            type B extends A:
            """);

        RDataType a = rObjectFactory.buildRDataType(getData(model, "A"));
        RDataType b = rObjectFactory.buildRDataType(getData(model, "B"));

        Assertions.assertTrue(subtypeRelation.isSubtypeOf(b, a, true));
    }

    @Test
    public void testJoinTypeHierarchy() {
        RosettaModel model = modelHelper.parseRosettaWithNoIssues("""
            type A:
            type B extends A:
            type C extends A:
            type D extends C:
            """);

        RDataType a = rObjectFactory.buildRDataType(getData(model, "A"));
        RDataType b = rObjectFactory.buildRDataType(getData(model, "B"));
        RDataType d = rObjectFactory.buildRDataType(getData(model, "D"));

        Assertions.assertEquals(a, subtypeRelation.join(b, d));
    }

    @Test
    public void testExtendedEnumIsNotASupertype() {
        RosettaModel model = modelHelper.parseRosettaWithNoIssues("""
            enum A:
            enum B extends A:
            """);

        REnumType a = rObjectFactory.buildREnumType(getEnum(model, "A"));
        REnumType b = rObjectFactory.buildREnumType(getEnum(model, "B"));

        Assertions.assertFalse(subtypeRelation.isSubtypeOf(a, b, true));
    }
}