package com.regnosys.rosetta.ide.hover;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.inject.Inject;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.Range;
import org.eclipse.xtext.ide.server.Document;
import org.eclipse.xtext.ide.server.hover.HoverService;
import org.eclipse.xtext.ide.server.hover.IHoverService;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.resource.EObjectAtOffsetHelper;
import org.eclipse.xtext.resource.ILocationInFileProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.ITextRegion;

import com.google.common.collect.Streams;

public class RosettaHoverService extends HoverService {
	@Inject
	private EObjectAtOffsetHelper eObjectAtOffsetHelper;

	@Inject
	private ILocationInFileProvider locationInFileProvider;

	@Inject
	private RosettaDocumentationProvider documentationProvider;
	
	@Override
	public Hover hover(Document document, XtextResource resource, HoverParams params, CancelIndicator cancelIndicator) {
		int offset = document.getOffSet(params.getPosition());
		RosettaHoverContext context = createContext(document, resource, offset);
		return hover(context);
	}

	protected RosettaHoverContext createContext(Document document, XtextResource resource, int offset) {
		EObject crossLinkedEObject = eObjectAtOffsetHelper.resolveCrossReferencedElementAt(resource, offset);
		if (crossLinkedEObject != null) {
			if (crossLinkedEObject.eIsProxy()) {
				return null;
			}
			IParseResult parseResult = resource.getParseResult();
			if (parseResult == null) {
				return null;
			}
			ILeafNode leafNode = NodeModelUtils.findLeafNodeAtOffset(parseResult.getRootNode(), offset);
			if (leafNode != null && leafNode.isHidden() && leafNode.getOffset() == offset) {
				leafNode = NodeModelUtils.findLeafNodeAtOffset(parseResult.getRootNode(), offset - 1);
			}
			if (leafNode == null) {
				return null;
			}
			ITextRegion leafRegion = leafNode.getTextRegion();
			EObject owner = NodeModelUtils.findActualSemanticObjectFor(leafNode);
			return new RosettaHoverContext(document, resource, offset, leafRegion, crossLinkedEObject, owner);
		}
		EObject element = eObjectAtOffsetHelper.resolveElementAt(resource, offset);
		if (element == null) {
			return null;
		}
		ITextRegion region = locationInFileProvider.getSignificantTextRegion(element);
		return new RosettaHoverContext(document, resource, offset, region, element, element);
	}
	
	protected Hover hover(RosettaHoverContext context) {
		if (context == null) {
			return IHoverService.EMPTY_HOVER;
		}
		MarkupContent contents = getMarkupContent(context);
		if (contents == null) {
			return IHoverService.EMPTY_HOVER;
		}
		Range range = getRange(context);
		if (range == null) {
			return IHoverService.EMPTY_HOVER;
		}
		return new Hover(contents, range);
	}
	
	protected MarkupContent getMarkupContent(RosettaHoverContext ctx) {
		return toMarkupContent(getKind(ctx), getContents(ctx.getElement(), ctx.getOwner()));
	}
	
	public String getContents(EObject element, EObject owner) {
		Stream<String> allDocs = Stream.empty();
		if (element != null) {
			allDocs = Streams.concat(allDocs, documentationProvider.getDocumentationFromReference(element).stream());
		}
		if (owner != null) {
			allDocs = Streams.concat(allDocs, documentationProvider.getDocumentationFromOwner(owner).stream());
		}
		return allDocs.collect(Collectors.joining("\n\n"));
	}
}
