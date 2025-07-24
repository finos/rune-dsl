package com.regnosys.rosetta.ide.hover;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.ide.server.Document;
import org.eclipse.xtext.ide.server.hover.HoverContext;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.ITextRegion;

public class RosettaHoverContext extends HoverContext {
	
	private final EObject owner;

	public RosettaHoverContext(Document document, XtextResource resource, int offset, ITextRegion region, EObject element, EObject owner) {
		super(document, resource, offset, region, element);
		this.owner = owner;
	}

	public EObject getOwner() {
		return owner;
	}
	
	@Override
	public String toString() {
		return "RosettaHoverContext [document=" + getDocument() + ", resource=" + (getResource() == null ? "null" : getResource().getURI())
				+ ", offset=" + getOffset() + ", region=" + getRegion() + ", element="
				+ (getElement() == null ? "null" : EcoreUtil.getURI(getElement())) + ", owner="
				+ (getOwner() == null ? "null" : EcoreUtil.getURI(getOwner())) + "]";
	}
}
