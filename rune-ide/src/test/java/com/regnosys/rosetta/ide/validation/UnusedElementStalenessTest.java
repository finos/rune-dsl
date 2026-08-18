package com.regnosys.rosetta.ide.validation;

import java.util.List;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DiagnosticTag;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.xtext.ide.server.UriExtensions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.regnosys.rosetta.ide.server.diagnostics.DerivedDiagnosticsStore;
import com.regnosys.rosetta.ide.tests.AbstractRosettaLanguageServerValidationTest;

import jakarta.inject.Inject;

/**
 * The "unused" editor marker must refresh when the last (or first) usage of an element changes in a
 * <em>different</em> file, not just in the file that declares the element.
 *
 * <p>The language server only revalidates resources it considers <em>affected</em>, and Xtext's notion of
 * affected is one-directional: a resource is affected when it references something that changed. The file
 * that <em>declares</em> the element did not change and does not reference the file that did, so it is never
 * revalidated and would keep whatever marker it had. This is the acceptance test for
 * {@code WorkspaceDerivedDiagnosticsService}, which sidesteps that by recomputing the markers for every
 * resource once the build has settled and republishing the ones that changed.
 *
 * <p>Note this is orthogonal to how usages are detected (live AST vs. Xtext index): it is purely about
 * which resources get their markers refreshed.
 *
 * <p>The first two tests use fully qualified references ({@code decl.F()}) rather than an {@code import}, so
 * that adding/removing the call site does not also produce "Unused import" warnings that would obscure the
 * assertions.
 */
public class UnusedElementStalenessTest extends AbstractRosettaLanguageServerValidationTest {

	private static final String UNUSED_F = "Function 'F' is never used";

	@Inject
	private DerivedDiagnosticsStore store;
	@Inject
	private UriExtensions uriExtensions;

	@Test
	void unusedMarkerIsRemovedWhenFirstCallSiteIsAddedInAnotherFile() {
		String declURI = createModel("decl.rosetta", """
				namespace decl

				func F:
					output: r int (1..1)
					set r: 42
				""");
		String callerURI = createModel("caller.rosetta", """
				namespace caller

				func Caller:
					output: r int (1..1)
					set r: 1
				""");

		assertNoIssues();

		// Precondition: `F` has no callers anywhere, so it is marked unused.
		Assertions.assertEquals(List.of(UNUSED_F), unusedMarkerMessages(declURI));

		// Introduce the first call site for `F`, in the other file.
		makeChange(callerURI, 4, 8, "1", "decl.F()");

		assertNoIssues();

		// `F` now has a caller, so its marker must be gone.
		Assertions.assertEquals(List.of(), unusedMarkerMessages(declURI),
				"Expected the unused marker on 'F' to be cleared once a call site exists in another file");
	}

	@Test
	void unusedMarkerIsAddedWhenLastCallSiteIsRemovedInAnotherFile() {
		String declURI = createModel("decl.rosetta", """
				namespace decl

				func F:
					output: r int (1..1)
					set r: 42
				""");
		String callerURI = createModel("caller.rosetta", """
				namespace caller

				func Caller:
					output: r int (1..1)
					set r: decl.F()
				""");

		assertNoIssues();

		// Precondition: `F` is called, so it carries no marker.
		Assertions.assertEquals(List.of(), unusedMarkerMessages(declURI));

		// Remove the only call site for `F`, in the other file.
		makeChange(callerURI, 4, 8, "decl.F()", "1");

		assertNoIssues();

		// `F` no longer has any caller, so it must now be marked unused.
		Assertions.assertEquals(List.of(UNUSED_F), unusedMarkerMessages(declURI),
				"Expected 'F' to be marked unused once its last call site was removed in another file");
	}

	/**
	 * Deleting the file with the only call site must add the marker to the declaring file, exactly like
	 * editing that file to remove the call (the previous test) — deletion is just another way a build can
	 * observe the last reference disappearing. Deleting the <em>declaring</em> file afterwards must drop it
	 * from {@link DerivedDiagnosticsStore}, which is asserted on the store rather than on what the client is
	 * told: a deleted resource leaves the project's resource set, so a leaked entry can never produce a
	 * publish and no assertion over the notifications could ever catch it.
	 */
	@Test
	void deletingTheOnlyCallSiteAddsTheMarkerAndDeletingTheDeclaringFileLeavesNoStaleEntry() {
		String declURI = createModel("decl.rosetta", """
				namespace decl

				func F:
					output: r int (1..1)
					set r: 42
				""");
		String callerURI = createModel("caller.rosetta", """
				namespace caller

				func Caller:
					output: r int (1..1)
					set r: decl.F()
				""");
		String otherURI = createModel("other.rosetta", """
				namespace other

				func Other:
					output: r int (1..1)
					set r: 1
				""");

		assertNoIssues();
		Assertions.assertEquals(List.of(), unusedMarkerMessages(declURI));

		// Delete the only call site: F must be marked unused in the declaring file, just as if the call had
		// been edited away.
		deleteWatchedFile("caller.rosetta", callerURI);

		Assertions.assertEquals(List.of(UNUSED_F), unusedMarkerMessages(declURI),
				"Expected 'F' to be marked unused once the file containing its only call site is deleted");
		// Establishes that the URI below is keyed the way the store keys it, so the assertion after the
		// delete is about the entry going away and not about looking up the wrong key.
		Assertions.assertTrue(store.isPublished(uriExtensions.toUri(declURI)),
				"Expected the declaring file to be recorded in the store while it exists");

		// Delete the declaring file itself.
		deleteWatchedFile("decl.rosetta", declURI);

		// One unrelated build to let a delete's bookkeeping settle: Xtext may carry the delete delta into the
		// next build rather than the one that reported it.
		makeChange(otherURI, 4, 8, "1", "2");
		assertNoIssues();

		Assertions.assertFalse(store.isPublished(uriExtensions.toUri(declURI)),
				"Expected the deleted file's entry to be pruned from the store");
	}

	/**
	 * Guards the {@code WorkspaceDerivedDiagnosticsService} merge (the wrapper that appends the store's
	 * current hints to every build publish, so that hints do not blink off between the build and the sweep
	 * that follows it): a file carrying both an ordinary validation warning and an unused-declaration hint
	 * must have both in the <em>same</em> {@code publishDiagnostics} call, both on the initial build and
	 * after an edit that leaves both issues unchanged — the case where the sweep finds nothing to republish
	 * and the merged build-time publish is the only one the client ever sees.
	 */
	@Test
	void editedFileSinglePublishCarriesBothValidationIssuesAndUnusedHints() {
		createModel("other.rosetta", """
				namespace other

				type Thing:
					x int (1..1)
				""");
		String uri = createModel("model.rosetta", """
				namespace model

				import other.*

				func F:
					output: r int (1..1)
					set r: 42
				""");

		assertBothIssuesPresent(uri);

		// An edit that leaves both issues unchanged: the sweep finds nothing to republish, so this is the
		// merged build-time publish, not a sweep republish.
		makeChange(uri, 6, 7, "42", "43");

		assertBothIssuesPresent(uri);
	}

	private void assertBothIssuesPresent(String uri) {
		List<Diagnostic> diagnostics = getDiagnostics().get(uri);
		Assertions.assertTrue(
				diagnostics.stream().anyMatch(d -> d.getSeverity() == DiagnosticSeverity.Warning
						&& d.getMessage().startsWith("Unused import")),
				"Expected an 'Unused import' warning among: " + diagnostics);
		Assertions.assertTrue(
				diagnostics.stream().anyMatch(d -> d.getTags() != null && d.getTags().contains(DiagnosticTag.Unnecessary)
						&& d.getMessage().equals(UNUSED_F)),
				"Expected the unused marker for 'F' among: " + diagnostics);
	}

	/**
	 * Deletes the file and tells the server about it via a watched-file-deletion event, the same signal a
	 * real editor sends when a file disappears from disk. Closing it first is required: a watched-file event
	 * for a still-open document is ignored by {@code LanguageServerImpl}.
	 */
	private void deleteWatchedFile(String path, String uri) {
		deleteFile(path);
		close(uri);
		DidChangeWatchedFilesParams params = new DidChangeWatchedFilesParams();
		params.setChanges(List.of(new FileEvent(uri, FileChangeType.Deleted)));
		languageServer.didChangeWatchedFiles(params);
	}

	/**
	 * Messages of the diagnostics that render as a faded "unused" marker, i.e. those carrying the
	 * {@link DiagnosticTag#Unnecessary} tag. Returns an empty list when the file has no diagnostics at
	 * all, so that "no marker" and "no diagnostics" compare equal.
	 */
	private List<String> unusedMarkerMessages(String uri) {
		List<Diagnostic> diagnostics = getDiagnostics().get(uri);
		if (diagnostics == null) {
			return List.of();
		}
		return diagnostics.stream()
				.filter(d -> d.getTags() != null && d.getTags().contains(DiagnosticTag.Unnecessary))
				.map(Diagnostic::getMessage)
				.toList();
	}
}
