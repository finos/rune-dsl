package com.regnosys.rosetta.formatting2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.formatting2.FormatterRequest;
import org.eclipse.xtext.formatting2.IFormatter2;
import org.eclipse.xtext.formatting2.regionaccess.ITextRegionAccess;
import org.eclipse.xtext.formatting2.regionaccess.ITextRegionRewriter;
import org.eclipse.xtext.formatting2.regionaccess.ITextReplacement;
import org.eclipse.xtext.formatting2.regionaccess.TextRegionAccessBuilder;
import org.eclipse.xtext.preferences.ITypedPreferenceValues;
import org.eclipse.xtext.resource.XtextResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;

public class XtextResourceFormatter implements ResourceFormatterService {
	private static Logger LOGGER = LoggerFactory.getLogger(XtextResourceFormatter.class);
	@Inject
	private Provider<FormatterRequest> formatterRequestProvider;

	@Inject
	private Provider<IFormatter2> iFormatter2Provider;

	@Inject
	private TextRegionAccessBuilder regionBuilder;

	@Override
	public void formatCollection(Collection<Resource> resources, ITypedPreferenceValues preferenceValues,
			BiConsumer<Resource, String> handler) {
		resources.stream().forEach(resource -> {
			if (resource instanceof XtextResource) {
				formatXtextResource((XtextResource) resource, preferenceValues, handler);

			} else {
				LOGGER.debug("Resource is not of type XtextResource and will be skipped: " + resource.getURI());
			}
		});
	}

	@Override
	public void formatXtextResource(XtextResource resource, ITypedPreferenceValues preferenceValues,
			BiConsumer<Resource, String> handler) {
		LOGGER.info("Formatting file at location " + resource.getURI());

		// setup request and formatter
		FormatterRequest req = formatterRequestProvider.get();
		req.setPreferences(preferenceValues);
		IFormatter2 formatter = iFormatter2Provider.get();

		ITextRegionAccess regionAccess = null;
		try {
			regionAccess = regionBuilder.forNodeModel(resource).create();
		} catch (Exception e) {
			LOGGER.info("Resource " + resource.getURI() + " is empty.", e);
			return;
		}

		req.setTextRegionAccess(regionAccess);

		// list contains all the replacements which should be applied to resource
		List<ITextReplacement> replacements = new ArrayList<>();
		try {
			replacements = formatter.format(req); // throws exception
		} catch (RuntimeException e) {
			LOGGER.error("RuntimeException in " + resource.getURI() + ": " + e.getMessage(), e);
		}

		// formatting using TextRegionRewriter
		ITextRegionRewriter regionRewriter = regionAccess.getRewriter();
		String formattedString = regionRewriter.renderToString(regionAccess.regionForDocument(), replacements);

		// Perform handler operation
		handler.accept(resource, formattedString);
	}

}
