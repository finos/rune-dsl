package com.regnosys.rosetta.formatting2;

import java.util.Collection;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.preferences.ITypedPreferenceValues;
import org.eclipse.xtext.resource.XtextResource;

public interface ResourceFormatterService {
	
	/**
	 * Formats each {@link XtextResource} in the provided collection in-place.
	 * <p>
	 * This method iterates over the given collection of resources and applies formatting 
	 * directly to each resource. Formatting may include indentation, spacing adjustments, 
	 * and other stylistic improvements to ensure consistency and readability of the resources.
	 * </p>
	 * 
	 * @param resources a collection of {@link XtextResource} objects to be formatted
	 */
	default void formatCollection(Collection<Resource> resources) {
		formatCollection(resources, null);
	}
	
	/**
	 * Formats the given {@link XtextResource} in-place.
	 * <p>
	 * This method applies formatting directly to the specified resource. Formatting can include
	 * adjustments to indentation, spacing, and other stylistic elements to ensure consistency
	 * and readability of the resource content.
	 * </p>
	 * 
	 * @param resources the {@link XtextResource} to format
	 * @param preferenceValues an {@link ITypedPreferenceValues} object containing formatting preferences, 
	 *                         or {@code null} if no preferences are specified
	 */
	default void formatXtextResource(XtextResource resource) {
		formatXtextResource(resource, null);
	}
	
	/**
	 * Formats each {@link XtextResource} in the provided collection in-place, with specified formatting preferences.
	 * <p>
	 * This method iterates over the given collection of resources and applies formatting 
	 * directly to each resource. Formatting may include indentation, spacing adjustments, 
	 * and other stylistic improvements to ensure consistency and readability of the resources.
	 * The formatting can be customized based on the specified {@link ITypedPreferenceValues}.
	 * If no preferences are required, {@code preferenceValues} can be set to {@code null}.
	 * </p>
	 * 
	 * @param resources a collection of {@link XtextResource} objects to be formatted
	 * @param preferenceValues an {@link ITypedPreferenceValues} object containing formatting preferences, 
	 *                         or {@code null} if no preferences are specified
	 */
	void formatCollection(Collection<Resource> resources, ITypedPreferenceValues preferenceValues); 
	
	/**
	 * Formats the given {@link XtextResource} in-place.
	 * <p>
	 * This method applies formatting directly to the specified resource. Formatting can include
	 * adjustments to indentation, spacing, and other stylistic elements to ensure consistency
	 * and readability of the resource content.
	 * The formatting can be customized based on the specified {@link ITypedPreferenceValues}.
	 * If no preferences are required, {@code preferenceValues} can be set to {@code null}.
	 * </p>
	 *
	 * @param resource the {@link XtextResource} to format
	 */
	void formatXtextResource(XtextResource resource, ITypedPreferenceValues preferenceValues);	
}
