package com.regnosys.rosetta.ide.tests

import org.eclipse.xtext.testing.AbstractLanguageServerTest
import org.eclipse.xtend.lib.annotations.Accessors
import org.eclipse.xtext.testing.TextDocumentPositionConfiguration
import org.eclipse.lsp4j.InlayHint
import java.util.List
import org.eclipse.lsp4j.InlayHintParams
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.Position
import org.eclipse.xtext.resource.IResourceServiceProvider
import org.eclipse.xtext.LanguageInfo
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints
import java.io.File
import org.junit.jupiter.api.BeforeEach
import org.junit.Before
import org.junit.jupiter.api.^extension.ExtendWith
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension

@ExtendWith(InjectionExtension)
@InjectWith(RosettaServerInjectorProvider)
abstract class AbstractRosettaLanguageServerTest extends AbstractLanguageServerTest {
	new() {
		super("rosetta")
	}
	
	@Before @BeforeEach
	override void setup() {
		val injector = new RosettaServerInjectorProvider().getInjector()
		injector.injectMembers(this)

		val resourceServiceProvider = resourceServerProviderRegistry.extensionToFactoryMap.get(fileExtension)
		if (resourceServiceProvider instanceof IResourceServiceProvider)
			languageInfo = resourceServiceProvider.get(LanguageInfo)

		// register notification callbacks
		languageServer.connect(ServiceEndpoints.toServiceObject(this, languageClientClass))
		// initialize
		languageServer.supportedMethods()

		// create workingdir
		root = new File(new File("").absoluteFile, TEST_PROJECT_PATH)
	}
	
	@Accessors static class TestInlayHintsConfiguration extends TextDocumentPositionConfiguration {
		String expectedInlayHintItems = ''
		(List<? extends InlayHint>) => void assertInlayHints = null
		Range range = new Range(new Position(0, 0), new Position(Integer.MAX_VALUE, Integer.MAX_VALUE))
	}
	
	protected def void testInlayHint((TestInlayHintsConfiguration)=>void configurator) {
		val extension configuration = new TestInlayHintsConfiguration
		configuration.filePath = 'MyModel.' + fileExtension
		configurator.apply(configuration)
		val filePath = initializeContext(configuration).uri
		val range = configuration.range
		val inlayHints = languageServer.inlayHint(new InlayHintParams(
			new TextDocumentIdentifier(filePath),
			range
		))
		val result = inlayHints.get.map[languageServer.resolveInlayHint(it).get].toList

		if (configuration.assertInlayHints !== null) {
			configuration.assertInlayHints.apply(result)
		} else {
			assertEquals(expectedInlayHintItems, result.toExpectation)
		}
	}
}