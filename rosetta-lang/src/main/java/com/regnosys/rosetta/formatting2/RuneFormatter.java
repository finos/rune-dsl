package com.regnosys.rosetta.formatting2;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.formatting2.FormatterRequest;
import org.eclipse.xtext.formatting2.regionaccess.ITextRegionAccess;
import org.eclipse.xtext.formatting2.regionaccess.ITextReplacement;
import org.eclipse.xtext.formatting2.regionaccess.TextRegionAccessBuilder;
import org.eclipse.xtext.formatting2.regionaccess.internal.TextRegionRewriter;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.TextRegion;

import javax.inject.Inject;
import javax.inject.Provider;

public class RuneFormatter implements CodeFormatterService{
	@Inject
	private Provider<FormatterRequest> formatterRequestProvider;
	
	@Inject 
	private Provider<RosettaFormatter> rosettaFormatterProvider;

	@Inject
	private TextRegionAccessBuilder regionBuilder;

	@Override
	public List<Resource> formatCollection(List<Resource> resources) throws IOException {
		
		List<Resource> result = new ArrayList<Resource>();
		
		for (Resource doc: resources) {
			Resource formattedDocument = formatDocument(doc);
			result.add(formattedDocument);
		}	
		return result;
	}

	private Resource formatDocument(Resource doc) throws IOException {
		//setup request and formatter
		FormatterRequest req = formatterRequestProvider.get();
		RosettaFormatter rosettaFormatter = rosettaFormatterProvider.get();
		
		// Format root object in document
		EObject content = doc.getContents().get(0);
		
		//figure out which region should be formatted (all of it basically)
		INode node = NodeModelUtils.getNode(content);
		
		TextRegion textRegion = new TextRegion(node.getOffset(), node.getLength());
		req.setRegions(Collections.singletonList(textRegion));
		
		ITextRegionAccess regionAccess = regionBuilder.forNodeModel((XtextResource) doc).create();
		req.setTextRegionAccess(regionAccess);
		
		//initialize the formatter with the request - telling it what to format
		rosettaFormatter.initialize(req);
		//list contains all the replacements which should be applied to doc
		List<ITextReplacement> replacements = rosettaFormatter.format(req);
				
		
		//formatting using TextRegionRewriter
		TextRegionRewriter regionRewriter = new TextRegionRewriter(regionAccess);
		String formattedString = regionRewriter.renderToString(regionAccess.regionForDocument(), replacements);

		//With the formatted text, update the resource
		InputStream resultStream = new ByteArrayInputStream(formattedString.getBytes(StandardCharsets.UTF_8));
		doc.unload();
		doc.load(resultStream, null);
		return doc;
	}
	
}
