package com.regnosys.rosetta.ide.tests;

import com.google.inject.Module;
import com.regnosys.rosetta.builtin.RosettaBuiltinsService;
import com.regnosys.rosetta.ide.semantictokens.ISemanticTokenModifier;
import com.regnosys.rosetta.ide.semantictokens.SemanticToken;
import com.regnosys.rosetta.ide.server.RosettaLanguageServerImpl;
import com.regnosys.rosetta.ide.server.RosettaServerModule;
import com.regnosys.rosetta.ide.util.RangeUtils;
import jakarta.inject.Inject;
import org.eclipse.lsp4j.*;
import org.eclipse.xtext.testing.AbstractLanguageServerTest;
import org.eclipse.xtext.testing.FileInfo;
import org.eclipse.xtext.testing.TextDocumentConfiguration;
import org.eclipse.xtext.testing.TextDocumentPositionConfiguration;
import org.junit.jupiter.api.Assertions;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * TODO: contribute to Xtext.
 */
public abstract class AbstractRosettaLanguageServerTest extends AbstractLanguageServerTest {
	@Inject
	protected RangeUtils ru;
	@Inject
	protected RosettaBuiltinsService builtins;

	public AbstractRosettaLanguageServerTest() {
		super("rosetta");
	}

	@Override
	protected Module getServerModule() {
		return RosettaServerModule.create();
	}

	protected void assertNoIssues() {
		var problems = getDiagnostics().values().stream()
				.flatMap(list -> list.stream())
				.filter(diagnostic -> diagnostic.getSeverity().getValue() <= DiagnosticSeverity.Warning.getValue())
				.toList();
		Assertions.assertEquals(0, problems.size(), "There were issues found:\n" + String.join("\n", problems.stream().map(Object::toString).toList()));
	}

	protected String readGeneratedFile(String relativePath) {
		var path = getTestRootPath().resolve("src/generated/java").resolve(relativePath);
		if (Files.exists(path)) {
			try {
				return Files.readString(path);
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}

	public static class TestInlayHintsConfiguration extends TextDocumentPositionConfiguration {
		private String expectedInlayHintItems = "";
		private Integer assertNumberOfInlayHints = null;
		private Consumer<List<? extends InlayHint>> assertInlayHints = null;
		private Range range = new Range(new Position(0, 0), new Position(Integer.MAX_VALUE, Integer.MAX_VALUE));
		private boolean assertNoIssues = true;

		public String getExpectedInlayHintItems() {
			return expectedInlayHintItems;
		}

		public void setExpectedInlayHintItems(String expectedInlayHintItems) {
			this.expectedInlayHintItems = expectedInlayHintItems;
		}

		public Integer getAssertNumberOfInlayHints() {
			return assertNumberOfInlayHints;
		}

		public void setAssertNumberOfInlayHints(Integer assertNumberOfInlayHints) {
			this.assertNumberOfInlayHints = assertNumberOfInlayHints;
		}

		public Consumer<List<? extends InlayHint>> getAssertInlayHints() {
			return assertInlayHints;
		}

		public void setAssertInlayHints(Consumer<List<? extends InlayHint>> assertInlayHints) {
			this.assertInlayHints = assertInlayHints;
		}

		public Range getRange() {
			return range;
		}

		public void setRange(Range range) {
			this.range = range;
		}

		public boolean isAssertNoIssues() {
			return assertNoIssues;
		}

		public void setAssertNoIssues(boolean assertNoIssues) {
			this.assertNoIssues = assertNoIssues;
		}
	}

	protected void testInlayHint(Consumer<TestInlayHintsConfiguration> configurator) {
		var configuration = new TestInlayHintsConfiguration();
		configuration.setFilePath("MyModel." + getFileExtension());
		configurator.accept(configuration);
		var filePath = initializeContext(configuration).getUri();

		if (configuration.isAssertNoIssues()) {
			assertNoIssues();
		}

		var range = configuration.getRange();
		try {
			var inlayHints = languageServer.inlayHint(new InlayHintParams(
					new TextDocumentIdentifier(filePath),
					range
			)).get();
			var result = new ArrayList<InlayHint>();
			for (var hint : inlayHints) {
				result.add(languageServer.resolveInlayHint(hint).get());
			}
			result.sort((a, b) -> ru.comparePositions(a.getPosition(), b.getPosition()));

			var nbInlayHints = configuration.getAssertNumberOfInlayHints();
			if (nbInlayHints != null) {
				Assertions.assertTrue(
						result.size() >= nbInlayHints,
						String.format("Expected %d inlay hints, got %d.", nbInlayHints, result.size())
				);
			}
			if (configuration.getAssertInlayHints() != null) {
				configuration.getAssertInlayHints().accept(result);
			} else {
				assertEquals(configuration.getExpectedInlayHintItems(), toExpectationForInlayHints(result));
			}
			if (nbInlayHints != null) {
				Assertions.assertEquals(nbInlayHints, result.size(),
						String.format("Expected %d inlay hints, got %d.", nbInlayHints, result.size())
				);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static class TestSemanticTokensConfiguration extends TextDocumentPositionConfiguration {
		private String expectedSemanticTokenItems = "";
		private Consumer<List<? extends SemanticToken>> assertSemanticTokens = null;
		private boolean assertNoIssues = true;

		public String getExpectedSemanticTokenItems() {
			return expectedSemanticTokenItems;
		}

		public void setExpectedSemanticTokenItems(String expectedSemanticTokenItems) {
			this.expectedSemanticTokenItems = expectedSemanticTokenItems;
		}

		public Consumer<List<? extends SemanticToken>> getAssertSemanticTokens() {
			return assertSemanticTokens;
		}

		public void setAssertSemanticTokens(Consumer<List<? extends SemanticToken>> assertSemanticTokens) {
			this.assertSemanticTokens = assertSemanticTokens;
		}

		public boolean isAssertNoIssues() {
			return assertNoIssues;
		}

		public void setAssertNoIssues(boolean assertNoIssues) {
			this.assertNoIssues = assertNoIssues;
		}
	}

	protected void testSemanticToken(Consumer<TestSemanticTokensConfiguration> configurator) {
		var configuration = new TestSemanticTokensConfiguration();
		configuration.setFilePath("MyModel." + getFileExtension());
		configurator.accept(configuration);
		var filePath = initializeContext(configuration).getUri();

		if (configuration.isAssertNoIssues()) {
			assertNoIssues();
		}

		try {
			var semanticTokens = languageServer.getRequestManager().runRead(cancelIndicator ->
					((RosettaLanguageServerImpl) languageServer).semanticTokens(
							new SemanticTokensParams(
									new TextDocumentIdentifier(filePath)
							),
							cancelIndicator
					)
			).get();
			var result = new ArrayList<>(semanticTokens);
			result.sort(null);

			if (configuration.getAssertSemanticTokens() != null) {
				configuration.getAssertSemanticTokens().accept(result);
			} else {
				assertEquals(configuration.getExpectedSemanticTokenItems().stripTrailing(), toExpectationForSemanticTokens(result));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String toExpectation(SemanticToken token) {
		StringBuilder sb = new StringBuilder();
		sb.append(token.getTokenType().getValue());
		var modifiers = token.getTokenModifiers();
		if (modifiers != null && modifiers.length > 0) {
			sb.append(".");
			sb.append(String.join(".", java.util.Arrays.stream(modifiers)
					.map(ISemanticTokenModifier::getValue)
					.toArray(String[]::new)));
		}
		sb.append(": ").append(token.getLine())
				.append(":").append(token.getStartChar())
				.append(":").append(token.getLength());
		return sb.toString();
	}

	protected String toExpectationForSemanticTokens(List<SemanticToken> tokens) {
		return tokens.stream()
				.map(this::toExpectation)
				.collect(Collectors.joining("\n"));
	}

	protected String toExpectationForInlayHints(List<InlayHint> hints) {
		return hints.stream()
				.map(this::toExpectation)
				.collect(Collectors.joining("\n"));
	}

	protected String toExpectation(InlayHint hint) {
		// Default implementation for InlayHint
		return hint.toString();
	}

	@Override
	protected FileInfo initializeContext(TextDocumentConfiguration configuration) {
		try {
			Map<String, CharSequence> filesInScope = new HashMap<String, CharSequence>();
			Map<String, CharSequence> existingFiles = configuration.getFilesInScope();
			if (existingFiles != null) {
				filesInScope.putAll(existingFiles);
			}
			filesInScope.put(
					builtins.basicTypesURI.path(),
					new String(builtins.basicTypesURL.openStream().readAllBytes(), StandardCharsets.UTF_8)
			);
			filesInScope.put(
					builtins.annotationsURI.path(),
					new String(builtins.annotationsURL.openStream().readAllBytes(), StandardCharsets.UTF_8)
			);
			configuration.setFilesInScope(filesInScope);
			return super.initializeContext(configuration);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static class TestResolvedCodeActionConfiguration extends TextDocumentPositionConfiguration {
        private String expectedCodeActions = "";
        
        private Consumer<List<CodeAction>> assertCodeActionResolution = null;

        public String getExpectedCodeActions() {
            return this.expectedCodeActions;
        }

        public void setExpectedCodeActions(String expectedCodeActions) {
            this.expectedCodeActions = expectedCodeActions;
        }
        
		public Consumer<List<CodeAction>> getAssertCodeActionResolution() {
			return assertCodeActionResolution;
		}

		public void setAssertCodeActionResolution(Consumer<List<CodeAction>> assertCodeActionResolution) {
			this.assertCodeActionResolution = assertCodeActionResolution;
		}
	}

	protected void testResultCodeAction(Consumer<TestResolvedCodeActionConfiguration> configurator) {
		var configuration = new TestResolvedCodeActionConfiguration();
		configuration.setFilePath("MyModel." + getFileExtension());
		configurator.accept(configuration);
		var filePath = initializeContext(configuration).getUri();

		try {
			var codeActionParams = new CodeActionParams();
			codeActionParams.setTextDocument(new TextDocumentIdentifier(filePath));
			var range = new Range();
			range.setStart(new Position(configuration.getLine(), configuration.getColumn()));
			range.setEnd(range.getStart());
			codeActionParams.setRange(range);
			var context = new CodeActionContext();
			context.setDiagnostics(getDiagnostics().get(filePath));
			codeActionParams.setContext(context);

			var codeActions = languageServer.codeAction(codeActionParams).get();

			List<CodeAction> resultCodeActionList = new ArrayList<>();

			// Add all resolved codeActions to result list
			for (var codeAction : codeActions) {
				var resolveResult = languageServer.resolveCodeAction(codeAction.getRight()).get();
				resultCodeActionList.add(resolveResult);
			}

			if (configuration.getAssertCodeActionResolution() != null) {
				configuration.getAssertCodeActionResolution().accept(resultCodeActionList);
			} else {
                this.assertEquals(configuration.getExpectedCodeActions(), this.toExpectation(resultCodeActionList));
            }
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
