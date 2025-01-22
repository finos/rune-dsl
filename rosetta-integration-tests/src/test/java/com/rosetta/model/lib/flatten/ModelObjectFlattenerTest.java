package com.rosetta.model.lib.flatten;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.path.RosettaPathValue;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
class ModelObjectFlattenerTest {
	@Inject
	private ModelInstanceCreator creator;
	@Inject
	private ModelObjectFlattener modelObjectFlattener;

	private void assertFlattenedValues(RosettaModelObject instance, String expected) {
		List<RosettaPathValue> table = modelObjectFlattener.flatten(instance);
		String actual = table.stream().map(pv -> pv.getPath() + ": " + pv.getValue()).collect(Collectors.joining("\n", "", "\n"));
		Assertions.assertEquals(expected, actual);
	}

	@Test
	void flattenTest1() throws IOException {
		RosettaModelObject instance = creator.create("Data()", "/model-object-flattener-test/test-1.rosetta");

		assertFlattenedValues(instance, """
				r1: VALUE1
				foo.f1: VALUE2
				foo.bar.b1: VALUE3
				foo.bar.b3(0): VALUE4
				foo.bars(0).b1: VALUE5
				foo.bars(0).b2(0): VALUE6
				foo.bars(0).b2(1): VALUE7
				foo.bars(1).b1: VALUE8
				foo.bars(1).b2(0): VALUE9
				foo.bars(1).b2(1): VALUE10
				foo.bars(1).b3(0): VALUE11
				foo.bars(1).b3(1): VALUE12
				bar(0).b1: VALUE13
				bar(0).b2(0): VALUE14
				bar(0).b2(1): VALUE15
				bar(1).b1: VALUE16
				bar(1).b2(0): VALUE17
				bar(1).b2(1): VALUE18
				bar(1).b3(0): VALUE19
				bar(1).b3(1): VALUE20
				""");
	}

	@Test
	void flattenTest2() throws IOException {
		RosettaModelObject instance = creator.create("Data()", "/model-object-flattener-test/test-2.rosetta");

		assertFlattenedValues(instance, """
				attr: 42
				numberAttr: 10
				parent.subAttr: true
				parentList.subAttr: false
				stringAttr: My string
				""");
	}
}