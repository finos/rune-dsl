package com.regnosys.rosetta.generator.java.expression;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.generator.java.types.JavaTypeUtil;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;
import com.regnosys.rosetta.tests.util.ReflectiveInvoker;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.util.types.JavaParameterizedType;
import com.rosetta.util.types.JavaWildcardTypeArgument;
import com.fasterxml.jackson.core.type.TypeReference;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class SortOperationTest {
    @Inject
    private RosettaTestModelService modelService;
    @Inject
    private JavaTypeUtil typeUtil;
    
    @Test
    void sortNullEntries() {
        var model = modelService.toJavaTestModel("""
        		type Foo:
        			bars Bar (0..*)
        		
        		type Bar:
        			attr int (0..1)
        		""").compile();
        var sortedList = (List<?>) model.evaluateExpression(JavaParameterizedType.from(new TypeReference<List<RosettaModelObject>>() {}, JavaWildcardTypeArgument.extendsBound(typeUtil.ROSETTA_MODEL_OBJECT)), """
				Foo {
					bars: [
							Bar { attr: 1 },
							Bar { ... },
							Bar { attr: 0 }
						]
				} -> bars sort [ attr ]
				""");
        
        var attrValues = sortedList.stream().map(bar -> ReflectiveInvoker.from(bar, "getAttr", Integer.class).invoke()).toList();
        Assertions.assertEquals(Arrays.asList(0, 1, null), attrValues);
    }

    @Test
    void sortAllNullEntries() {
        var model = modelService.toJavaTestModel("""
        		type Foo:
        			bars Bar (0..*)
        		
        		type Bar:
        			attr int (0..1)
        		""").compile();
        var sortedList = (List<?>) model.evaluateExpression(JavaParameterizedType.from(new TypeReference<List<RosettaModelObject>>() {}, JavaWildcardTypeArgument.extendsBound(typeUtil.ROSETTA_MODEL_OBJECT)), """
				Foo {
					bars: [
							Bar { ... },
							Bar { ... },
							Bar { ... }
						]
				} -> bars sort [ attr ]
				""");
        
        var attrValues = sortedList.stream().map(bar -> ReflectiveInvoker.from(bar, "getAttr", Integer.class).invoke()).toList();
        Assertions.assertEquals(Arrays.asList(null, null, null), attrValues);
    }
}
