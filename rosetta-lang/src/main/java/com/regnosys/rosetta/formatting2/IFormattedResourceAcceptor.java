package com.regnosys.rosetta.formatting2;

import org.eclipse.emf.ecore.resource.Resource;

/**
 * Functional interface for handling a formatted resource and its corresponding
 * formatted text.
 * <p>
 * This interface allows customization of how formatted resources are processed,
 * such as saving the content, logging it, or using it for validations or
 * assertions in tests.
 * </p>
 */
@FunctionalInterface
public interface IFormattedResourceAcceptor {

	/**
	 * Accepts a formatted resource and its formatted content for further
	 * processing.
	 *
	 * @param resource      the {@link Resource} that was formatted
	 * @param formattedText the formatted content as a {@link String}
	 */
	void accept(Resource resource, String formattedText);
}
