package com.regnosys.rosetta.ui.tests.contentassist

import com.google.inject.Inject
import com.google.inject.Injector
import java.io.InputStream
import java.util.List
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
		return new TestBuilder(injector, this)
	}

	def protected TestBuilder newBuilder(String model) throws Exception {
		return new TestBuilder(injector, this).append(model) as TestBuilder
	}
}

class TestBuilder extends ContentAssistProcessorTestBuilder {

	new(Injector injector, ResourceLoadHelper helper) throws Exception {
		super(injector, helper)
	}

	def assertProposalsAtCursor(String... expectedText) throws Exception {
		val proposals = getProposalsAtCursorIndicator().map[proposedText]
		if (!expectedText.elementsEqual(proposals)) {
			val expected = expectedText.join(', ')
			val actual = proposals.join(', ')
			assertEquals('''Wrong proposals proposal: «expected» Found: «actual»''', expected, actual)
		}
		return null
	}

	override TestBuilder append(String model) throws Exception {
		super.append(model) as TestBuilder
	}

	def void operator_doubleGreaterThan(TestBuilder builder, List<String> expect) {
		builder.assertProposalsAtCursor(expect)
	}
}

class TestExtensions {
	@Inject AbstractContentAssistTest builderProvider

	/**
	 * Creates a CA test builder and asserts expected proposals at cursor &lt;|&gt;
	 */
	def operator_doubleGreaterThan(String model, String[] expect) {
		builderProvider.newBuilder(model) >> expect
	}

	def operator_doubleGreaterThan(TestBuilder builder, String[] expect) {
		builder.assertProposalsAtCursor(expect)
	}
}
