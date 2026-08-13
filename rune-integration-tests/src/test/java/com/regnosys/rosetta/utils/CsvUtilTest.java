package com.regnosys.rosetta.utils;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;
import com.regnosys.rosetta.types.RAttribute;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.RObjectFactory;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.util.stream.Collectors;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class CsvUtilTest {
    @Inject
    private CsvUtil csvUtil;
    @Inject
    private RosettaTestModelService modelService;
    @Inject
    private RObjectFactory rObjectFactory;
    
    @Test
    void typeWithEnumAndBuiltinAttributesIsTabular() {
        assertTabular("Foo", """
                type Foo:
                	bar Bar (1..1)
                	baz string(maxLength: 4) (0..1)
                	qux Qux (1..1)
                	t time (0..1)
                	dt dateTime (1..1)
                
                enum Bar:
                    VALUE1
                    VALUE2
                
                typeAlias Qux:
                    date
                """);
    }
    
    @Test
    void typeWithMultiCardinalitySimpleAttributeIsTabular() {
        assertTabular("Foo", """
                type Foo:
                	stringList string (0..*)
                """);
    }

    @Test
    void typeWithMultiCardinalityEnumAttributeIsTabular() {
        assertTabular("Foo", """
                type Foo:
                	barList Bar (0..*)

                enum Bar:
                    VALUE1
                    VALUE2
                """);
    }

    @Test
    void typeWithMultiCardinalityTypeAliasOverBasicAttributeIsTabular() {
        assertTabular("Foo", """
                type Foo:
                	quxList Qux (0..*)

                typeAlias Qux:
                    date
                """);
    }

    @Test
    void typeWithMultiCardinalityDataTypeAttributeIsNotTabular() {
        assertNotTabular("Foo", """
                type Foo:
                	barList Bar (0..*)

                type Bar:
                """);
    }

    @Test
    void typeWithMultiCardinalityChoiceTypeAttributeIsNotTabular() {
        assertNotTabular("Foo", """
                type Foo:
                	fooList Baz (0..*)

                type Bar:
                    barAttr int (1..1)

                type Qux:
                    quxAttr int (1..1)

                choice Baz:
                    Bar
                    Qux
                """);
    }

    @Test
    void typeWithMultiCardinalityTypeAliasOverDataTypeAttributeIsNotTabular() {
        assertNotTabular("Foo", """
                type Foo:
                	barList Qux (0..*)

                type Bar:

                typeAlias Qux:
                    Bar
                """);
    }

    @Test
    void typeWithComplexAttributeIsNotTabular() {
        assertNotTabular("Foo", """
                type Foo:
                	complex Bar (1..1)

                type Bar:
                """);
    }
    
    private void assertTabular(String typeName, String model) {
        var type = getType(typeName, model);
        Assertions.assertTrue(csvUtil.isTypeTabular(type), () -> {
            String nonSimpleAttributes = csvUtil.getNonSimpleAttributes(type).stream().map(RAttribute::getName).collect(Collectors.joining(", "));
            return "Type " + typeName + " is not tabular: " + nonSimpleAttributes + " are not simple";
        });
    }
    private void assertNotTabular(String typeName, String model) {
        var type = getType(typeName, model);
        Assertions.assertFalse(csvUtil.isTypeTabular(type), "Type " + typeName + " is tabular: all attributes are simple");
    }
    private RDataType getType(String typeName, String rawModel) {
        var model = modelService.toTestModel(rawModel);
        var data = model.getType(typeName);
        return rObjectFactory.buildRDataType(data);
    }
    // Metadata generates a FieldWithMeta* wrapper, which a CSV cell can't hold in either direction.

    @Test
    void typeWithSchemeMetadataAttributeIsNotTabular() {
        assertNotTabular("Foo", """
                type Foo:
                	withScheme string (1..1)
                		[metadata scheme]
                """);
    }

    @Test
    void typeWithIdMetadataAttributeIsNotTabular() {
        assertNotTabular("Foo", """
                type Foo:
                	withId string (1..1)
                		[metadata id]
                """);
    }

    @Test
    void typeWithMultiCardinalityMetadataAttributeIsNotTabular() {
        assertNotTabular("Foo", """
                type Foo:
                	schemeList string (0..*)
                		[metadata scheme]
                """);
    }

    @Test
    void typeWithInheritedMetadataAttributeIsNotTabular() {
        assertNotTabular("Foo", """
                type Base:
                	withScheme string (1..1)
                		[metadata scheme]

                type Foo extends Base:
                	own string (0..1)
                """);
    }

    @Test
    void typeWhoseMetadataAttributeIsRemovedIsTabular() {
        assertTabular("Foo", """
                type Base:
                	plain string (1..1)

                type Foo extends Base:
                	own string (0..1)
                """);
    }

    @Test
    void typeWithInheritedMultiCardinalitySimpleAttributeIsTabular() {
        assertTabular("Foo", """
                type Base:
                	stringList string (0..*)

                type Foo extends Base:
                	own string (0..1)
                """);
    }

    @Test
    void typeWithInheritedComplexAttributeIsNotTabular() {
        assertNotTabular("Foo", """
                type Bar:

                type Base:
                	complexAttr Bar (1..1)

                type Foo extends Base:
                	own string (0..1)
                """);
    }


}
