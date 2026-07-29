package com.regnosys.rosetta.ide.validation;

import java.util.List;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticTag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.regnosys.rosetta.ide.tests.AbstractRosettaLanguageServerValidationTest;

/**
 * The "unused" editor marker must refresh when the last (or first) usage of an element changes in a
 * <em>different</em> file, not just in the file that declares the element.
 *
 * <p>The language server only revalidates resources it considers <em>affected</em>, and Xtext's notion of
 * affected is one-directional: a resource is affected when it references something that changed. The file
 * that <em>declares</em> the element did not change and does not reference the file that did, so it is
 * never revalidated and keeps whatever marker it had. Fixed by
 * {@code RosettaStatefulIncrementalBuilder#revalidateResourcesWithChangedIncomingReferences}, which
 * revalidates resources whose declarations gained or lost a reference from a resource the build did touch;
 * see {@code IncomingReferenceChanges} for how that set is derived.
 *
 * <p>Note this is orthogonal to how usages are detected (live AST vs. Xtext index): it is purely about
 * which resources get revalidated.
 *
 * <p>Both tests use fully qualified references ({@code decl.F()}) rather than an {@code import}, so that
 * adding/removing the call site does not also produce "Unused import" warnings that would obscure the
 * assertions.
 */
public class UnusedFunctionStalenessTest extends AbstractRosettaLanguageServerValidationTest {

	private static final String UNUSED_F = "Function 'F' is never used";

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
