package com.regnosys.rosetta.generator.java.expression;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;
import com.regnosys.rosetta.tests.util.ReflectiveInvoker;
import com.rosetta.model.lib.RosettaModelObject;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class ConstructorTest {
    @Inject
    private RosettaTestModelService modelService;
    
    @Test
    void incompatibleSetterTest() {
        var model = modelService.toJavaTestModel("""
        		type Foo:
        			parentList Parent (0..*)
        		
        		type Bar extends Foo:
        			override parentList Child (0..*)
        		
        		type Parent:
        		type Child extends Parent:
        			attr int (1..1)
        		""").compile();
        var bar = model.evaluateExpression(RosettaModelObject.class, """
				Bar {
					parentList: [
							Child {
								attr: 1
							},
							Child {
								attr: 2
							}
						]
				}
				""");
        
        List<?> parentList = ReflectiveInvoker.from(bar, "getParentList", List.class).invoke();
        Assertions.assertEquals(2, parentList.size());
    }
}
