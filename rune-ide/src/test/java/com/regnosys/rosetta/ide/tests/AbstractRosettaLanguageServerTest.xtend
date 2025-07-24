package com.regnosys.rosetta.ide.tests

import com.google.inject.Module
import com.regnosys.rosetta.RosettaStandaloneSetup
import com.regnosys.rosetta.builtin.RosettaBuiltinsService
import com.regnosys.rosetta.ide.semantictokens.SemanticToken
import com.regnosys.rosetta.ide.server.RosettaLanguageServerImpl
import com.regnosys.rosetta.ide.server.RosettaServerModule
import com.regnosys.rosetta.ide.util.RangeUtils
import java.nio.charset.StandardCharsets
import java.util.HashMap
import java.util.List
import java.util.stream.Collectors
import jakarta.inject.Inject
import org.eclipse.lsp4j.DiagnosticSeverity
import org.eclipse.lsp4j.InlayHint
import org.eclipse.lsp4j.InlayHintParams
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.SemanticTokensParams
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.xtend.lib.annotations.Accessors
import org.eclipse.xtext.testing.AbstractLanguageServerTest
import org.eclipse.xtext.testing.FileInfo
import org.eclipse.xtext.testing.TextDocumentConfiguration
import org.eclipse.xtext.testing.TextDocumentPositionConfiguration
import org.junit.jupiter.api.Assertions
import org.eclipse.lsp4j.CodeAction
import org.eclipse.lsp4j.CodeActionParams
import org.eclipse.lsp4j.CodeActionContext
import java.nio.file.Files

/**
 * TODO: contribute to Xtext.
 */
abstract class AbstractRosettaLanguageServerTest extends AbstractLanguageServerTest {
	@Inject RangeUtils ru
	@Inject RosettaBuiltinsService builtins
	
	new() {
		super("rosetta")
	}
	
	protected override Module getServerModule() {
		return RosettaServerModule.create
	}
	
	protected def void assertNoIssues() {
		val problems = getDiagnostics.values
			.flatten
			.filter[
				severity <= DiagnosticSeverity.Warning
			].toList
		Assertions.assertEquals(0, problems.size(), "There were issues found:\n" + problems.join('\n'));
	}
	
	protected def String readGeneratedFile(String relativePath) {
		val path = testRootPath.resolve("src/generated/java").resolve(relativePath)
		if (Files.exists(path)) {
			return Files.readString(path)
		}
		return null
	}
	
	@Accessors static class TestInlayHintsConfiguration extends TextDocumentPositionConfiguration {
		String expectedInlayHintItems = ''
		Integer assertNumberOfInlayHints = null
		(List<? extends InlayHint>) => void assertInlayHints = null
		Range range = new Range(new Position(0, 0), new Position(Integer.MAX_VALUE, Integer.MAX_VALUE))
		boolean assertNoIssues = true
	}
	
	protected def void testInlayHint((TestInlayHintsConfiguration)=>void configurator) {
		val extension configuration = new TestInlayHintsConfiguration
		configuration.filePath = 'MyModel.' + fileExtension
		configurator.apply(configuration)
		val filePath = initializeContext(configuration).uri
		
		if (configuration.assertNoIssues) {
			assertNoIssues()
		}
		
		val range = configuration.range
		val inlayHints = languageServer.inlayHint(new InlayHintParams(
			new TextDocumentIdentifier(filePath),
			range
		))
		val result = inlayHints.get.map[languageServer.resolveInlayHint(it).get].stream.collect(Collectors.toCollection[newArrayList])
		result.sort[a,b| ru.comparePositions(a.position, b.position)]

		val nbInlayHints = configuration.assertNumberOfInlayHints
		if (nbInlayHints !== null) {
			Assertions.assertTrue(
				result.size >= nbInlayHints,
				'''Expected «nbInlayHints» inlay hints, got «result.size».'''
			)
		}
		if (configuration.assertInlayHints !== null) {
			configuration.assertInlayHints.apply(result)
		} else {
			assertEquals(expectedInlayHintItems, result.toExpectation)
		}
		if (nbInlayHints !== null) {
			Assertions.assertEquals(nbInlayHints, result.size,
				'''Expected «nbInlayHints» inlay hints, got «result.size».'''
			)
		}
	}
	
	@Accessors static class TestSemanticTokensConfiguration extends TextDocumentPositionConfiguration {
		String expectedSemanticTokenItems = ''
		(List<? extends SemanticToken>) => void assertSemanticTokens = null
		boolean assertNoIssues = true
	}
	
	protected def void testSemanticToken((TestSemanticTokensConfiguration)=>void configurator) {
		val extension configuration = new TestSemanticTokensConfiguration
		configuration.filePath = 'MyModel.' + fileExtension
		configurator.apply(configuration)
		val filePath = initializeContext(configuration).uri
		
		if (configuration.assertNoIssues) {
			assertNoIssues()
		}
		
		val semanticTokens = languageServer.requestManager.runRead[cancelIndicator |
			(languageServer as RosettaLanguageServerImpl).semanticTokens(
				new SemanticTokensParams(
					new TextDocumentIdentifier(filePath)
				),
				cancelIndicator
			)
		]
		val result = semanticTokens.get.sort

		if (configuration.assertSemanticTokens !== null) {
			configuration.assertSemanticTokens.apply(result)
		} else {
			assertEquals(expectedSemanticTokenItems, result.toExpectation)
		}
	}
	protected def dispatch String toExpectation(SemanticToken it) {
		'''«tokenType.value»«IF !tokenModifiers.empty».«FOR tokenMod : tokenModifiers SEPARATOR '.'»«tokenMod.value»«ENDFOR»«ENDIF»: «line»:«startChar»:«length»'''
	}
	
	protected override FileInfo initializeContext(TextDocumentConfiguration configuration) {
		configuration.filesInScope = new HashMap(configuration.filesInScope) + #{
			builtins.basicTypesURI.path -> new String(builtins.basicTypesURL.openStream.readAllBytes, StandardCharsets.UTF_8),
			builtins.annotationsURI.path -> new String(builtins.annotationsURL.openStream.readAllBytes, StandardCharsets.UTF_8)
		}
		val filePath = super.initializeContext(configuration);
		return filePath
	}
	
	@Accessors static class TestCodeActionConfiguration extends TextDocumentPositionConfiguration {
		(List<CodeAction>)=>void assertCodeActionResolution= null
	}

	protected def void testResultCodeAction((TestCodeActionConfiguration)=>void configurator) {
		val extension configuration = new TestCodeActionConfiguration
		configuration.filePath = 'MyModel.' + fileExtension
		configurator.apply(configuration)
		val filePath = initializeContext(configuration).uri
		
		val codeActions = languageServer.codeAction(new CodeActionParams=>[
			textDocument = new TextDocumentIdentifier(filePath)
			range = new Range => [
				start = new Position(configuration.line, configuration.column)
				end = start
			]
			context = new CodeActionContext => [
				diagnostics = this.diagnostics.get(filePath)
			]
		]).get
		
		val resultCodeActionList = newArrayList
		
		// Add all resolved codeActions to result list
		for (codeAction : codeActions) {
    		val resolveResult = languageServer.resolveCodeAction(codeAction.getRight)
    		resultCodeActionList.add(resolveResult.get)
		}
		
		if (configuration.assertCodeActionResolution !== null) {
			configuration.assertCodeActionResolution.apply(resultCodeActionList)
		}
	}
}