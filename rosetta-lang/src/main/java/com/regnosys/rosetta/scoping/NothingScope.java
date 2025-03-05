package com.regnosys.rosetta.scoping;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;

public class NothingScope implements IScope {
	private final IScope parent;

	protected NothingScope(IScope parent) {
		this.parent = parent;
	}

	@Override
	public IEObjectDescription getSingleElement(QualifiedName name) {
		IEObjectDescription parentResult = parent.getSingleElement(name);
		if (parentResult != null) {
			return parentResult;
		}
	}

	@Override
	public Iterable<IEObjectDescription> getElements(QualifiedName name) {
		return parent.getElements(name);
	}

	@Override
	public IEObjectDescription getSingleElement(EObject object) {
		IEObjectDescription parentResult = parent.getSingleElement(object);
		if (parentResult != null) {
			return parentResult;
		}
	}

	@Override
	public Iterable<IEObjectDescription> getElements(EObject object) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<IEObjectDescription> getAllElements() {
		// TODO Auto-generated method stub
		return null;
	}

}
