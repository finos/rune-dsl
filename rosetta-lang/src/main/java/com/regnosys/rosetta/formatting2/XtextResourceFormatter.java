package com.regnosys.rosetta.formatting2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.formatting2.FormatterRequest;
import org.eclipse.xtext.formatting2.IFormatter2;
import org.eclipse.xtext.formatting2.regionaccess.ITextRegionAccess;
import org.eclipse.xtext.formatting2.regionaccess.ITextRegionRewriter;
import org.eclipse.xtext.formatting2.regionaccess.ITextReplacement;
import org.eclipse.xtext.formatting2.regionaccess.TextRegionAccessBuilder;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.preferences.ITypedPreferenceValues;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.ITextRegion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.regnosys.rosetta.rosetta.Import;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.utils.ImportManagementService;

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

	@Inject
	private ImportManagementService importManagementService;

	@Override
	public void formatCollection(Collection<Resource> resources, ITypedPreferenceValues preferenceValues,
			IFormattedResourceAcceptor acceptor) {
		resources.stream().forEach(resource -> {
			if (resource instanceof XtextResource) {
				formatXtextResource((XtextResource) resource, preferenceValues, acceptor);

			} else {
				LOGGER.debug("Resource is not of type XtextResource and will be skipped: " + resource.getURI());
			}
		});
	}

	@Override
	public void formatXtextResource(XtextResource resource, ITypedPreferenceValues preferenceValues,
			IFormattedResourceAcceptor acceptor) {
		if (!resource.getAllContents().hasNext()) {
			LOGGER.info("Resource " + resource.getURI() + " is empty.");
			return;
		}

		LOGGER.info("Formatting file at location " + resource.getURI());

		// setup request and formatter
		FormatterRequest req = formatterRequestProvider.get();
		req.setPreferences(preferenceValues);
		IFormatter2 formatter = iFormatter2Provider.get();

		ITextRegionAccess regionAccess = regionBuilder.forNodeModel(resource).create();
		req.setTextRegionAccess(regionAccess);

		// list contains all the replacements which should be applied to resource
		List<ITextReplacement> replacements;
		try {
			replacements = formatter.format(req); // throws exception
		} catch (RuntimeException e) {
			LOGGER.error("RuntimeException in " + resource.getURI() + ": " + e.getMessage(), e);
			replacements = new ArrayList<>();
		}

		// get text replacement for optimized imports
		/*ITextReplacement importsReplacement = formattedImportsReplacement(resource, regionAccess);
		if (importsReplacement != null)
			replacements.add(importsReplacement);*/

		// formatting using TextRegionRewriter
		ITextRegionRewriter regionRewriter = regionAccess.getRewriter();
		String formattedString = regionRewriter.renderToString(regionAccess.regionForDocument(), replacements);

		// Perform handler operation
		acceptor.accept(resource, formattedString);
	}

	public ITextReplacement formattedImportsReplacement(XtextResource resource, ITextRegionAccess regionAccess) {
		RosettaModel model = (RosettaModel) resource.getContents().get(0);
		ITextRegion importsRegion = getImportsTextRegion(model.getImports());

		if (importsRegion == null)
			return null;

		importManagementService.cleanupImports(model);
		String sortedImportsText = importManagementService.toString(model.getImports());
		return regionAccess.getRewriter().createReplacement(importsRegion.getOffset(), importsRegion.getLength(),
				sortedImportsText);
	}

	/**
	 * Return a ITextRegion of all imports
	 * 
	 * @param imports
	 * @return ITextRegion text region of imports
	 */
	private ITextRegion getImportsTextRegion(List<Import> imports) {
		if (imports.isEmpty()) {
			return null;
		}

		Import firstImport = imports.get(0);
		Import lastImport = imports.get(imports.size() - 1);
		ITextRegion firstRegion = NodeModelUtils.getNode(firstImport).getTextRegion();
		ITextRegion lastRegion = NodeModelUtils.getNode(lastImport).getTextRegion();

		return firstRegion.merge(lastRegion);
	}

}
