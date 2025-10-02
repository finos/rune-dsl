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
    void typeWithMultiAttributeIsNotTabular() {
        assertNotTabular("Foo", """
                type Foo:
                	stringList string (0..*)
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
}
