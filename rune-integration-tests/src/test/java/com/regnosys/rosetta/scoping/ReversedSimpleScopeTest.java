package com.regnosys.rosetta.scoping;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;
import org.junit.jupiter.api.Test;

import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.SimpleFactory;

public class ReversedSimpleScopeTest {

	@Test
	void objectLookupDoesNotExpandAllParentElements() {
		Attribute attribute = SimpleFactory.eINSTANCE.createAttribute();
		attribute.setName("foo");
		FailingAllElementsScope parent = new FailingAllElementsScope();
		IScope scope = ReversedSimpleScope.scopeFor(List.of(attribute), parent);

		assertTrue(scope.getElements(attribute).iterator().hasNext());
		assertTrue(parent.nameLookupUsed);
	}

	private static class FailingAllElementsScope implements IScope {
		private boolean nameLookupUsed;

		@Override
		public IEObjectDescription getSingleElement(QualifiedName name) {
			return null;
		}

		@Override
		public Iterable<IEObjectDescription> getElements(QualifiedName name) {
			nameLookupUsed = true;
			return Collections.emptyList();
		}

		@Override
		public IEObjectDescription getSingleElement(EObject object) {
			return null;
		}

		@Override
		public Iterable<IEObjectDescription> getElements(EObject object) {
			return Collections.emptyList();
		}

		@Override
		public Iterable<IEObjectDescription> getAllElements() {
			throw new AssertionError("Object lookup should not enumerate every parent element.");
		}
	}
}
