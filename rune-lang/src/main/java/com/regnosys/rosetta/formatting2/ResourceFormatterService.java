package com.regnosys.rosetta.formatting2;

import java.util.Collection;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.preferences.ITypedPreferenceValues;
import org.eclipse.xtext.resource.XtextResource;

public interface ResourceFormatterService {

	/**
	 * Formats each {@link XtextResource} in the provided collection in-memory and
	 * passes the formatted content to a handler for further processing.
	 * <p>
	 * This method iterates over the given collection of resources and applies
	 * formatting directly to each resource. Formatting may include indentation,
	 * spacing adjustments, and other stylistic improvements to ensure consistency
	 * and readability of the resources.
	 * </p>
	 * <p>
	 * The handler, represented as a {@link java.util.function.BiConsumer}, is
	 * called with two arguments: the {@link Resource} being formatted, and the
	 * resulting formatted text as a {@link String}. This allows the caller to
	 * specify actions such as saving the formatted content, logging it, or
	 * collecting it for assertions in a test.
	 * </p>
	 * 
	 * @param resources a collection of {@link XtextResource} objects to be
	 *                  formatted
	 * @param acceptor  an {@link IFormattedResourceAcceptor} to process the
	 *                  formatted resource and its content
	 */
	default void formatCollection(Collection<Resource> resources, IFormattedResourceAcceptor acceptor) {
		formatCollection(resources, null, acceptor);
	}

	/**
	 * Formats the given {@link XtextResource} in-memory and returns the formatted
	 * content.
	 * <p>
	 * This method applies formatting directly to the specified resource. Formatting
	 * can include adjustments to indentation, spacing, and other stylistic elements
	 * to ensure consistency and readability of the resource content.
	 * </p>
	 * 
	 * @param resources the {@link XtextResource} to format
	 * @return the formatted contents
	 */
	default String formatXtextResource(XtextResource resource) {
		return formatXtextResource(resource, null);
	}

	/**
	 * Formats each {@link XtextResource} in the provided collection in-memory,
	 * applying specified formatting preferences, and passes the formatted content
	 * to a handler for further processing.
	 * <p>
	 * This method iterates over the given collection of resources, formats each
	 * resource according to the provided preferences, and invokes a handler to
	 * process the formatted content. Formatting includes indentation, spacing
	 * adjustments, and other stylistic refinements to ensure consistency and
	 * readability of the resources. The formatting behavior can be customized based
	 * on the provided {@link ITypedPreferenceValues}.
	 * </p>
	 * <p>
	 * The handler, represented as a {@link java.util.function.BiConsumer}, is
	 * called for each resource with two arguments: the {@link Resource} being
	 * formatted, and the resulting formatted text as a {@link String}. This allows
	 * the caller to specify actions such as saving the formatted content, logging
	 * it, or collecting it for assertions in a test.
	 * </p>
	 * <p>
	 * If no formatting preferences are required, the {@code preferenceValues}
	 * parameter can be set to {@code null}.
	 * </p>
	 * 
	 * @param resources        a collection of {@link XtextResource} objects to be
	 *                         formatted
	 * @param preferenceValues an {@link ITypedPreferenceValues} object containing
	 *                         formatting preferences, or {@code null} if no
	 *                         preferences are specified
	 * @param acceptor         an {@link IFormattedResourceAcceptor} to process the
	 *                         formatted resource and its content
	 */
	void formatCollection(Collection<Resource> resources, ITypedPreferenceValues preferenceValues,
			IFormattedResourceAcceptor acceptor);

	/**
	 * Formats the given {@link XtextResource} in-memory, applying specified
	 * formatting preferences, and returns the formatted content.
	 * <p>
	 * This method formats each resource according to the provided preferences, and
	 * invokes a handler to process the formatted content. Formatting includes
	 * indentation, spacing adjustments, and other stylistic refinements to ensure
	 * consistency and readability of the resources. The formatting behavior can be
	 * customized based on the provided {@link ITypedPreferenceValues}.
	 * </p>
	 * <p>
	 * If no formatting preferences are required, the {@code preferenceValues}
	 * parameter can be set to {@code null}.
	 * </p>
	 * 
	 * @param resource         the {@link XtextResource} to be formatted
	 * @param preferenceValues an {@link ITypedPreferenceValues} object containing
	 *                         formatting preferences, or {@code null} if no
	 *                         preferences are specified
	 * @return the formatted contents
	 */
	String formatXtextResource(XtextResource resource, ITypedPreferenceValues preferenceValues);
}
