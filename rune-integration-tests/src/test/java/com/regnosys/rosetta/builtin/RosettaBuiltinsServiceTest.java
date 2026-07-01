package com.regnosys.rosetta.builtin;

import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import javax.inject.Inject;
import javax.inject.Provider;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * A resource set that has never had basictypes.rosetta / annotations.rosetta loaded into it
 * (e.g. a brand new project's resource set, before any file has triggered a lazy cross-reference
 * into either of them) is exactly the situation the language server is in during the very first
 * build of a workspace. RosettaBuiltinsService must be able to resolve the builtins there too,
 * not just once some other, unrelated resource has happened to load them first.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class RosettaBuiltinsServiceTest {
	@Inject
	private RosettaBuiltinsService builtins;
	@Inject
	private Provider<XtextResourceSet> resourceSetProvider;

	@Test
	void getBasicTypesModelResolvesOnResourceSetThatNeverLoadedIt() {
		XtextResourceSet resourceSet = resourceSetProvider.get();
		Assertions.assertTrue(resourceSet.getResources().isEmpty());

		RosettaModel model = Assertions.assertDoesNotThrow(() -> builtins.getBasicTypesModel(resourceSet));

		Assertions.assertNotNull(model);
		Assertions.assertFalse(model.getElements().isEmpty());
	}

	@Test
	void getAnnotationsResourceResolvesOnResourceSetThatNeverLoadedIt() {
		XtextResourceSet resourceSet = resourceSetProvider.get();
		Assertions.assertTrue(resourceSet.getResources().isEmpty());

		RosettaModel model = Assertions.assertDoesNotThrow(() -> builtins.getAnnotationsResource(resourceSet));

		Assertions.assertNotNull(model);
		Assertions.assertFalse(model.getElements().isEmpty());
	}

	@Test
	void getBasicTypesModelDoesNotReloadOnceResolved() {
		XtextResourceSet resourceSet = resourceSetProvider.get();

		RosettaModel first = builtins.getBasicTypesModel(resourceSet);
		RosettaModel second = builtins.getBasicTypesModel(resourceSet);

		Assertions.assertSame(first, second);
		long matchingResources = resourceSet.getResources().stream()
				.filter(r -> r.getURI().path().endsWith("basictypes.rosetta"))
				.count();
		Assertions.assertEquals(1, matchingResources);
	}
}
