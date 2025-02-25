package com.regnosys.rosetta.tests;

import jakarta.inject.Inject;

import org.eclipse.emf.ecore.util.EObjectValidator;
import org.eclipse.xtext.diagnostics.Diagnostic;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.tests.util.ModelHelper;

import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*;

// Verify that we are using Xtext's EcoreValidator, which does not duplicate unresolved reference errors.
@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class NoEcoreValidatorTest {
	@Inject ModelHelper modelHelper;
	@Inject ValidationTestHelper validationHelper;

	@Test
	void testNoUnresolvedProxyError() {
		RosettaModel model = modelHelper.parseRosetta("""
				namespace test
				
				type Foo:
					attr UnresolvedType (1..1)
				""");
		// There should be an unresolved error
		validationHelper.assertError(model, TYPE_CALL, Diagnostic.LINKING_DIAGNOSTIC, "Couldn't resolve reference to RosettaType 'UnresolvedType'.");
		// There should not be an "unresolved proxy" error:
		// "The feature 'type' of 'com.regnosys.rosetta.rosetta.impl.TypeCallImpl@2503ec73{__synthetic1.rosetta#/0/@elements.0/@attributes.0/@typeCall}' 
		//  contains an unresolved proxy 'com.regnosys.rosetta.rosetta.impl.RosettaBasicTypeImpl@76a14c8d{__synthetic1.rosetta#|0}'"
		validationHelper.assertNoIssue(model, TYPE_CALL, EObjectValidator.DIAGNOSTIC_SOURCE + "." + EObjectValidator.EOBJECT__EVERY_PROXY_RESOLVES);
	}
}
