package com.rosetta.model.lib.flatten;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.path.RosettaPathValue;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableMap.of;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
class ModelObjectFlattenerTest {

    @Inject
    private CodeGeneratorTestHelper helper;

    @Inject
    private ModelInstanceCreator creator;
    private ModelObjectFlattener modelObjectFlattener;

    @BeforeEach
    void setUp() {
        modelObjectFlattener = new ModelObjectFlattener();
    }

    @Test
    void flattenTest1() {
        RosettaModelObject instance = creator.create("test.tabulator.functions.Data",
                Path.of("src/test/resources/model-object-flattener-test/test-1.rosetta"));

        List<RosettaPathValue> table = modelObjectFlattener.flatten(instance);
        Map<String, Object> tableMap = table.stream()
                .collect(Collectors.toMap(x -> x.getPath().buildPath(), RosettaPathValue::getValue));
        
        assertEquals("VALUE1", tableMap.get("r1"));
        assertEquals("VALUE2", tableMap.get("foo.f1"));
        assertEquals("VALUE3", tableMap.get("foo.bar.b1"));
        assertEquals("VALUE4", tableMap.get("foo.bar.b3(0)"));
        assertEquals("VALUE5", tableMap.get("bar(0).b1"));
        assertEquals("VALUE6", tableMap.get("bar(0).b2(0)"));
        assertEquals("VALUE7", tableMap.get("bar(0).b2(1)"));
        assertEquals("VALUE8", tableMap.get("bar(1).b1"));
        assertEquals("VALUE9", tableMap.get("bar(1).b2(0)"));
        assertEquals("VALUE10", tableMap.get("bar(1).b2(1)"));
        assertEquals("VALUE11", tableMap.get("bar(1).b3(0)"));
        assertEquals("VALUE12", tableMap.get("bar(1).b3(1)"));
    }
}