package com.regnosys.rosetta.ide.validation;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.ide.tests.RosettaIdeInjectorProvider;
import com.regnosys.rosetta.ide.validation.UnusedElementHelper.UsageSnapshot;
import com.regnosys.rosetta.rosetta.RosettaFactory;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.rosetta.simple.SimpleFactory;

import jakarta.inject.Inject;

/**
 * The candidacy rule is "every named root element", and the naming half of it cannot be reached through the
 * parser: a declaration with no name is a syntax error, so what reaches the marker depends on how the parser
 * recovers, which is not a contract worth pinning a test to. The model objects are therefore built directly.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaIdeInjectorProvider.class)
class UnusedElementHelperTest {
	@Inject
	private UnusedElementHelper helper;

	/**
	 * A declaration whose name failed to parse gets no marker: it has no qualified name, so nothing could
	 * identify it as referenced and it would be flagged for as long as the name is missing — on top of the
	 * syntax error that is the real problem.
	 */
	@Test
	void aDeclarationWithNoNameIsNotFlagged() {
		RosettaModel model = RosettaFactory.eINSTANCE.createRosettaModel();
		model.setName("test");
		Data named = SimpleFactory.eINSTANCE.createData();
		named.setName("Named");
		Data nameless = SimpleFactory.eINSTANCE.createData();
		model.getElements().add(named);
		model.getElements().add(nameless);
		ResourceSet resourceSet = resourceSetContaining(model);

		UsageSnapshot snapshot = helper.snapshot(resourceSet);

		Assertions.assertFalse(helper.isUnused(nameless, snapshot),
				"Expected a declaration with no name to be left alone");
		Assertions.assertTrue(helper.isUnused(named, snapshot),
				"Expected its named neighbour to be flagged, so the assertion above is about the missing name "
						+ "rather than about nothing being flagged at all");
	}

	private static ResourceSet resourceSetContaining(RosettaModel model) {
		ResourceSet resourceSet = new ResourceSetImpl();
		Resource resource = new ResourceImpl(URI.createURI("test.rosetta"));
		resourceSet.getResources().add(resource);
		resource.getContents().add(model);
		return resourceSet;
	}
}
