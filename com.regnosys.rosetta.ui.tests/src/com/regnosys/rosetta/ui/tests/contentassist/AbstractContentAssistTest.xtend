package com.regnosys.rosetta.ui.tests.contentassist

import com.google.inject.Inject
import com.google.inject.Injector
import java.io.InputStream
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.resource.XtextResourceSet
import org.eclipse.xtext.resource.impl.ResourceDescriptionsProvider
import org.eclipse.xtext.ui.testing.ContentAssistProcessorTestBuilder
import org.eclipse.xtext.ui.testing.util.ResourceLoadHelper

import static org.junit.jupiter.api.Assertions.*

class AbstractContentAssistTest extends org.eclipse.xtext.ui.testing.AbstractContentAssistTest {
	@Inject protected extension TestExtensions
	@Inject Injector injector

	override XtextResource getResourceFor(InputStream stream) {
		val result = super.getResourceFor(stream)
		(result.resourceSet as XtextResourceSet).addLoadOption(ResourceDescriptionsProvider.LIVE_SCOPE, true)
		return result
	}

	override protected TestBuilder newBuilder() throws Exception {
		return new TestBuilder(injector, this, false)
	}
	
	def protected TestBuilder newBuilder(String model, boolean exactMatch) throws Exception {
		return new TestBuilder(injector, this, exactMatch).append(model) as TestBuilder
	}

	def protected TestBuilder newBuilder(String model) throws Exception {
		model.newBuilder(false)
	}
}

class TestBuilder extends ContentAssistProcessorTestBuilder {

	boolean exactMatch
	
	new(Injector injector, ResourceLoadHelper helper, boolean exactMatch) throws Exception {
		super(injector, helper)
		this.exactMatch = exactMatch
	}

	def assertProposalsAtCursor(String... expectedText) throws Exception {
		if(fullTextToBeParsed.indexOf('<|>') < 0)
			throw new IllegalArgumentException('Model text should contain cursor marker: <|>' )
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

	override TestBuilder append(String model) throws Exception {
		super.append(model) as TestBuilder
	}

}

class TestExtensions {
	@Inject AbstractContentAssistTest builderProvider

	/**
	 * Creates a CA test builder and asserts expected proposals contains in the proposal list at cursor &lt;|&gt;
	 */
	def operator_doubleGreaterThan(String model, String[] expect) {
		builderProvider.newBuilder(model) >> expect
	}
	
	/**
	 * Creates a CA test builder and asserts exact expected proposals at cursor &lt;|&gt;
	 */
	def >=(String model, String[] expect) {
		builderProvider.newBuilder(model, true) >> expect
	}

	def operator_doubleGreaterThan(TestBuilder builder, String[] expect) {
		builder.assertProposalsAtCursor(expect)
	}
	
}
