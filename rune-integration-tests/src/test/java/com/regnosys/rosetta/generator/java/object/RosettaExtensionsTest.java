package com.regnosys.rosetta.generator.java.object;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.REnumType;
import com.regnosys.rosetta.types.RObjectFactory;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
class RosettaExtensionsTest {

	@Inject
	private ParseHelper<RosettaModel> parseHelper;
	@Inject
	private RObjectFactory rObjectFactory;

	@Test
	void testSuperClasses() throws Exception {
		List<RDataType> classes = parseHelper.parse("""
				namespace test

				type Foo extends Bar:
				type Bar extends Baz:
				type Baz:
				""").getElements().stream()
				.filter(Data.class::isInstance)
				.map(Data.class::cast)
				.map(rObjectFactory::buildRDataType)
				.collect(Collectors.toList());

		assertEquals(Set.copyOf(classes), Set.copyOf(classes.get(0).getAllSuperTypes()));
		assertEquals(Set.copyOf(classes.subList(1, classes.size())), Set.copyOf(classes.get(1).getAllSuperTypes()));
		assertEquals(Set.of(classes.get(classes.size() - 1)), Set.copyOf(classes.get(2).getAllSuperTypes()));
	}

	@Test
	void testSuperClassesWithCycle() throws Exception {
		List<RDataType> classes = parseHelper.parse("""
				namespace test

				type Foo extends Bar:
				type Bar extends Baz:
				type Baz extends Foo:
				""").getElements().stream()
				.filter(Data.class::isInstance)
				.map(Data.class::cast)
				.map(rObjectFactory::buildRDataType)
				.collect(Collectors.toList());

		assertEquals(Set.copyOf(classes), Set.copyOf(classes.get(0).getAllSuperTypes()));
		assertEquals(Set.copyOf(classes), Set.copyOf(classes.get(1).getAllSuperTypes()));
		assertEquals(Set.copyOf(classes), Set.copyOf(classes.get(2).getAllSuperTypes()));
	}

	@Test
	void testEnumValue() throws Exception {
		RosettaModel model = parseHelper.parse("""
				namespace test
				version "1.2.3"

				enum Foo:
					foo0 foo1

				enum Bar extends Foo:
					bar
				enum Baz extends Bar:
					baz
				""");
		List<REnumType> elems = model.getElements().stream()
				.filter(RosettaEnumeration.class::isInstance)
				.map(RosettaEnumeration.class::cast)
				.map(rObjectFactory::buildREnumType)
				.collect(Collectors.toList());
		REnumType foo = elems.get(0);
		REnumType bar = elems.get(1);
		REnumType baz = elems.get(elems.size() - 1);
		assertEquals(Set.of(foo, bar, baz), Set.copyOf(baz.getAllParents()));
		assertEquals(Set.of(foo, bar), Set.copyOf(bar.getAllParents()));
		assertEquals(Set.of(foo), Set.copyOf(foo.getAllParents()));
		assertEquals(List.of("foo0", "foo1", "bar", "baz"),
				baz.getAllEnumValues().stream().map(v -> v.getName()).collect(Collectors.toList()));
		assertEquals(List.of("foo0", "foo1", "bar"),
				bar.getAllEnumValues().stream().map(v -> v.getName()).collect(Collectors.toList()));
		assertEquals(List.of("foo0", "foo1"),
				foo.getAllEnumValues().stream().map(v -> v.getName()).collect(Collectors.toList()));
	}
}
