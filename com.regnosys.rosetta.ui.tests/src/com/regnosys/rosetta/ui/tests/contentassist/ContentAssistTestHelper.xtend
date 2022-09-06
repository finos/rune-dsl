package com.regnosys.rosetta.ui.tests.contentassist

import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.Provider
import java.io.InputStream
import org.eclipse.emf.common.util.URI
import org.eclipse.xtext.resource.FileExtensionProvider
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.resource.XtextResourceSet
import org.eclipse.xtext.resource.impl.ResourceDescriptionsProvider
import org.eclipse.xtext.ui.testing.ContentAssistProcessorTestBuilder
import org.eclipse.xtext.ui.testing.util.ResourceLoadHelper

import static org.junit.jupiter.api.Assertions.*
import org.eclipse.xtext.util.StringInputStream
import org.eclipse.xtext.ui.editor.IDirtyStateManager
import org.eclipse.xtext.ui.editor.IDirtyResource
import org.eclipse.xtext.resource.IResourceDescription

class ContentAssistTestHelper {

	@Inject protected extension TestExtensions
	@Inject Injector injector
	@Inject Provider<OneResourceSetLoadHelper> loadHelpers

	def protected TestBuilder newBuilder() throws Exception {
		val loadHelper = loadHelpers.get
		return new TestBuilder(injector, loadHelper, false)
	}

	def protected TestBuilder newBuilder(String model, boolean exactMatch,
		String... additionalModels) throws Exception {
		val loadHelper = loadHelpers.get
		val builder = new TestBuilder(injector, loadHelper, exactMatch).append(model).withDirtyState as TestBuilder
		additionalModels?.forEach [ additionalModel |
			val res = loadHelper.getResourceFor(new StringInputStream(additionalModel))
			builder.dirtyStateManager.manageDirtyState(
				new IDirtyResource() {
					override String getContents() {
						return additionalModel
					}

					override String getActualContents() {
						return additionalModel
					}

					override IResourceDescription getDescription() {
						return res.getResourceServiceProvider().getResourceDescriptionManager().
							getResourceDescription(res)
					}

					override URI getURI() {
						return res.getURI()
					}
				}
			)
		]
		return builder
	}

	def protected TestBuilder newBuilder(String model) throws Exception {
		model.newBuilder(false)
	}

}

class OneResourceSetLoadHelper implements ResourceLoadHelper {

	@Inject Provider<XtextResourceSet> resourceSets
	@Inject FileExtensionProvider fileExtensions
	XtextResourceSet resourceSet

	override getResourceFor(InputStream stream) {
		if (resourceSet === null) {
			resourceSet = resourceSets.get()
			resourceSet.addLoadOption(ResourceDescriptionsProvider.LIVE_SCOPE, true)
		}
		var uri = URI.createURI('''TestModel.«fileExtensions.primaryFileExtension»''')
		var idx = 0
		while (resourceSet.getResource(uri, false) !== null) {
			uri = URI.createURI('''TestModel«idx».«fileExtensions.primaryFileExtension»''')
			idx++
		}
		val result = resourceSet.createResource(uri)
		result.load(stream, null)
		return result as XtextResource
	}
}

class TestBuilder extends ContentAssistProcessorTestBuilder {

	boolean exactMatch
	@Inject(optional=true)
	IDirtyStateManager dirtyStateManager

	new(Injector injector, ResourceLoadHelper helper, boolean exactMatch) throws Exception {
		super(injector, helper)
		this.exactMatch = exactMatch
	}

	def assertProposalsAtCursor(String... expectedText) throws Exception {
		if (fullTextToBeParsed.indexOf('<|>') < 0)
			throw new IllegalArgumentException('Model text should contain cursor marker: <|>')
		val proposals = getProposalsAtCursorIndicator().map[proposedText]
		if (exactMatch) {
			if (!expectedText.elementsEqual(proposals)) {
				val expected = expectedText.join(', ')["'" + it + "'"]
				val actual = proposals.join(', ')["'" + it + "'"]
				assertEquals(expected, actual, '''Wrong proposals proposal: «expected» Found: «actual»''')
			}
		} else {
			val missing = expectedText.filter[!proposals.contains(it)].toList
			if (!missing.empty) {
				val missingStr = missing.join(', ')["'" + it + "'"]
				val actual = proposals.join(', ')["'" + it + "'"]
				assertEquals(missingStr, actual, '''Missing expected proposals: «missingStr» Found: «actual»''')
			}
		}
		return null
	}

	def getDirtyStateManager() {
		dirtyStateManager
	}

	override TestBuilder append(String model) throws Exception {
		super.append(model) as TestBuilder
	}

}

class TestExtensions {
	@Inject ContentAssistTestHelper builderProvider

	/**
	 * Creates a CA test builder and asserts expected proposals contains in the proposal list at cursor &lt;|&gt;
	 */
	def TestBuilder >>(String model, String[] expect) {
		val builder = builderProvider.newBuilder(model)
		builder.assertProposalsAtCursor(expect)
		builder
	}

	/**
	 * Creates a CA test builder and asserts expected proposals contains in the proposal list at cursor &lt;|&gt;
	 */
	def >>(String[] model, String[] expect) {
		val builder = builderProvider.newBuilder(model.head, false, model.tail)
		builder.assertProposalsAtCursor(expect)
		builder
	}

	/**
	 * Creates a CA test builder and asserts exact expected proposals at cursor &lt;|&gt;
	 */
	def >=(String[] model, String[] expect) {
		val builder = builderProvider.newBuilder(model.head, true, model.tail)
		builder.assertProposalsAtCursor(expect)
		builder
	}

	/**
	 * Creates a CA test builder and asserts exact expected proposals at cursor &lt;|&gt;
	 */
	def >=(String model, String[] expect) {
		val builder = builderProvider.newBuilder(model, true)
		builder.assertProposalsAtCursor(expect)
		builder
	}

	/**
	 * Adds additional resource into the scope
	 */
	def String[] +(String model, Object otherModel) {
		#[model, otherModel?.toString ?: "<null>"]
	}
}
