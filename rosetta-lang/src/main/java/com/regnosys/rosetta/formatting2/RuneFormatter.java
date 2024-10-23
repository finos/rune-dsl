package com.regnosys.rosetta.formatting2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.formatting2.AbstractFormatter2;
import org.eclipse.xtext.formatting2.FormatterRequest;
import org.eclipse.xtext.formatting2.IFormatter2;
import org.eclipse.xtext.formatting2.internal.RootDocument;
import org.eclipse.xtext.formatting2.regionaccess.ITextRegionAccess;
import org.eclipse.xtext.formatting2.regionaccess.ITextReplacement;
import org.eclipse.xtext.formatting2.regionaccess.TextRegionAccessBuilder;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.TextRegion;

import javax.inject.Inject;
import javax.inject.Provider;

public class RuneFormatter implements CodeFormatterService{
	@Inject
	private Provider<IFormatter2> formatter2Provider;

	@Inject
	private Provider<FormatterRequest> formatterRequestProvider;

	@Inject
	private TextRegionAccessBuilder regionBuilder;

	@Override
	public List<XtextResource> formatCollection(List<XtextResource> resources) {
		List<XtextResource> result = new ArrayList<XtextResource>();
		
		FormatterRequest req = formatterRequestProvider.get();
		
		//create new Formatter
		///RosettaFormatter formatter = new RosettaFormatter(); // Add FormatterRequest? -- done below
		
		IFormatter2 formatter = formatter2Provider.get();
		
		for (XtextResource doc: resources) {
			List<EObject> contents = doc.getContents();
			List<EObject> newContents = new ArrayList<>();
			
			for(EObject content: contents) { //for each doc, format each piece of content
				//create copy to edit
				EObject formattedContent = content;
				
				INode node = NodeModelUtils.getNode(formattedContent);
				TextRegion textRegion = new TextRegion(node.getOffset(), node.getLength());
				
				req.setRegions(Collections.singletonList(textRegion));
				
				RootDocument rootDoc = new RootDocument((AbstractFormatter2) formatter);
				
				ITextRegionAccess regionAccess = regionBuilder.forNodeModel( doc).create(); //probably an issue
				req.setTextRegionAccess(regionAccess);
				List<ITextReplacement> replacements = formatter.format(req);
				//formatter.format(formattedContent, rootDoc); //??? but what the hell comes out of this
				
				//now the content is formatted, can add it to the resource
				newContents.add(formattedContent);
			}
			
			//newContents contains all the new, formatted content
			//update the original resource

			//ResourceSet resourceSet = new ResourceSetImpl();
			//Resource formattedResource = resourceSet.createResource(doc.getURI());
			XtextResource formattedResource = new XtextResource(doc.getURI());
			formattedResource.getContents().addAll(newContents);
			
			result.add(formattedResource);
		}
		
		
		return result;
	}
	
}
