package com.regnosys.rosetta.docrefs;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.ModelHelper;

@InjectWith(RosettaTestInjectorProvider.class)
@ExtendWith(InjectionExtension.class)
class DocReferenceTest {

	@Inject
	private ModelHelper modelHelper;

	@Test
	void declearCorpusWithBodyReference() {
		modelHelper.parseRosettaWithNoErrors("""
				body Organisation Org1 <"some description 1">
				corpus Agreement Org1 "Agreement 1" Agr1 <"some description 2">

				""");
	}

	@Test
	void declearCorpusWithoutBodyReference() {
		modelHelper.parseRosettaWithNoErrors("""
				corpus Agreement "Agreement 1" Agr1 <"some description 2">

				""");
	}

	@Test
	void corpusDisplaytNameIsOptional() {
		modelHelper.parseRosettaWithNoErrors("""
				corpus Agreement Agr1
				""");
	}

	@Test
	void typeCanHaveDocRef() {
		modelHelper.parseRosettaWithNoErrors("""
				body Organisation Org1
				corpus Agreement Org1 "Agreement 1" Agr1

				segment name

				type Foo:
					[docReference Org1 Agr1 name "something" provision "some provision"]
					bar string (1..1)

				""");
	}

	@Test
	void docRefProvisionIsOptional() {
		modelHelper.parseRosettaWithNoErrors("""
				body Organisation Org1
				corpus Agreement Org1 "Agreement 1" Agr1

				segment name

				type Foo:
					[docReference Org1 Agr1 name "something"]
					bar string (1..1)

				""");
	}

	@Test
	void attributeCanHaveDocRef() {
		modelHelper.parseRosettaWithNoErrors("""
				body Organisation Org1
				corpus Agreement Org1 "Agreement 1" Agr1

				segment name

				type Foo:
					bar string (1..1)
						[docReference Org1 Agr1 name "something"]

				""");
	}

	@Test
	void enumCanHaveDocRef() {
		modelHelper.parseRosettaWithNoErrors("""
				body Organisation Org1
				corpus Agreement Org1 "Agreement 1" Agr1

				segment name

				enum Foo:
				[docReference Org1 Agr1 name "something"]
					bar


				""");
	}

	@Test
	void enumValueCanHaveDocRef() {
		modelHelper.parseRosettaWithNoErrors("""
				body Organisation Org1
				corpus Agreement Org1 "Agreement 1" Agr1

				segment name

				enum Foo:
					bar
						[docReference Org1 Agr1 name "something"]
				""");
	}

	@Test
	void functionsCanHaveDocRef() {
		modelHelper.parseRosettaWithNoErrors("""
				body Organisation Org1
				corpus Agreement Org1 "Agreement 1" Agr1

				segment name

				func Sum:
					[docReference Org1 Agr1 name "something"]
					inputs: x number (0..*)
					output: s number (1..1)

				""");
	}

	@Test
	void conditionsCanHaveDocRef() {
		modelHelper.parseRosettaWithNoErrors("""
				body Organisation Org1
				corpus Agreement Org1 "Agreement 1" Agr1

				segment name

				type Foo:
					a int (1..1)

					condition:
						[docReference Org1 Agr1 name "something"]
						a > 0


				""");
	}
}
