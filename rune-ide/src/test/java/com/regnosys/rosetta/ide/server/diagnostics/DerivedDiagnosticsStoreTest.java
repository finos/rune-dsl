package com.regnosys.rosetta.ide.server.diagnostics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.validation.Issue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * What the store answers is what decides whether a resource is republished, and a republish is the only way an
 * amended marker reaches the client. The class is a map and two records, so these are direct unit tests rather
 * than language-server round trips.
 */
class DerivedDiagnosticsStoreTest {
	private static final URI URI_A = URI.createURI("file:/project/a.rosetta");
	private static final URI URI_B = URI.createURI("file:/project/b.rosetta");

	/** Stand-ins for the provider types the store keys contributions by. */
	private static final class ProviderA {
	}

	private static final class ProviderB {
	}

	private final DerivedDiagnosticsStore store = new DerivedDiagnosticsStore();

	/**
	 * The point of the comparison: {@link Issue} has no {@code equals}, so two runs of the same sweep produce
	 * lists that are never equal by identity. Reporting those as changed would republish every resource on
	 * every build.
	 */
	@Test
	void aRecomputedListWithTheSameContentIsNotAChange() {
		store.recordDerived(URI_A, Map.of(ProviderA.class, List.of(issue("Function 'F' is never used", 10, 1))));

		Assertions.assertFalse(store.derivedDiffers(URI_A,
				Map.of(ProviderA.class, List.of(issue("Function 'F' is never used", 10, 1)))));
	}

	@Test
	void aChangedMessageIsAChange() {
		store.recordDerived(URI_A, Map.of(ProviderA.class, List.of(issue("Function 'F' is never used", 10, 1))));

		Assertions.assertTrue(store.derivedDiffers(URI_A,
				Map.of(ProviderA.class, List.of(issue("Function 'G' is never used", 10, 1)))));
	}

	/**
	 * An edit above a declaration moves its marker without changing its message. Missing that leaves the
	 * marker rendered at its old position.
	 */
	@Test
	void aDiagnosticThatMovedOrResizedIsAChange() {
		store.recordDerived(URI_A, Map.of(ProviderA.class, List.of(issue("Function 'F' is never used", 10, 1))));

		Assertions.assertTrue(store.derivedDiffers(URI_A,
				Map.of(ProviderA.class, List.of(issue("Function 'F' is never used", 20, 1)))));
		Assertions.assertTrue(store.derivedDiffers(URI_A,
				Map.of(ProviderA.class, List.of(issue("Function 'F' is never used", 10, 2)))));
	}

	@Test
	void aDiagnosticsListLosingAnEntryIsAChange() {
		store.recordDerived(URI_A, Map.of(ProviderA.class,
				List.of(issue("Function 'F' is never used", 10, 1), issue("Type 'T' is never used", 30, 1))));

		Assertions.assertTrue(store.derivedDiffers(URI_A,
				Map.of(ProviderA.class, List.of(issue("Function 'F' is never used", 10, 1)))));
	}

	/**
	 * A provider that contributes nothing must compare and record as no entry at all. Otherwise the first
	 * sweep after a marker is cleared records an empty list, and every sweep after that reports the resource as
	 * changed against a map that no longer has the key.
	 */
	@Test
	void aProviderContributingNothingIsTheSameAsNoEntry() {
		Issue fromA = issue("Function 'F' is never used", 10, 1);
		store.recordDerived(URI_A, Map.of(ProviderA.class, List.of(fromA), ProviderB.class, List.of()));

		// Recorded with an empty contribution, compared without one, and the other way round.
		Assertions.assertFalse(store.derivedDiffers(URI_A, Map.of(ProviderA.class, List.of(fromA))));
		Assertions.assertFalse(store.derivedDiffers(URI_A,
				Map.of(ProviderA.class, List.of(fromA), ProviderB.class, List.of())));

		store.recordDerived(URI_B, Map.of(ProviderA.class, List.of()));

		Assertions.assertEquals(List.of(), store.derivedOf(URI_B));
		Assertions.assertEquals(Set.of(URI_A), store.urisWithDerivedDiagnostics());
	}

	/** Each half is recorded by a different caller, so recording one must not drop the other. */
	@Test
	void recordingEitherHalfLeavesTheOtherAlone() {
		Issue base = issue("Unused import", 5, 3);
		Issue derived = issue("Function 'F' is never used", 10, 1);

		store.recordBase(URI_A, List.of(base));
		store.recordDerived(URI_A, Map.of(ProviderA.class, List.of(derived)));

		Assertions.assertEquals(List.of(base), store.baseOf(URI_A));
		Assertions.assertEquals(List.of(derived), store.derivedOf(URI_A));

		store.recordBase(URI_A, List.of(base, issue("Type 'T' is never used", 30, 1)));

		Assertions.assertEquals(2, store.baseOf(URI_A).size());
		Assertions.assertEquals(List.of(derived), store.derivedOf(URI_A));
	}

	/**
	 * The lists come from a build and from a sweep, both of which reuse their collections. Holding onto one
	 * would let it change under the store, which is what the diff is there to detect.
	 */
	@Test
	void recordedListsAreCopiedRatherThanHeld() {
		List<Issue> base = new ArrayList<>(List.of(issue("Unused import", 5, 3)));
		List<Issue> derived = new ArrayList<>(List.of(issue("Function 'F' is never used", 10, 1)));
		store.recordBase(URI_A, base);
		store.recordDerived(URI_A, Map.of(ProviderA.class, derived));

		base.clear();
		derived.clear();

		Assertions.assertEquals(1, store.baseOf(URI_A).size());
		Assertions.assertEquals(1, store.derivedOf(URI_A).size());
		Assertions.assertThrows(UnsupportedOperationException.class,
				() -> store.baseOf(URI_A).add(issue("Type 'T' is never used", 30, 1)),
				"baseOf hands out the recorded list itself, so it has to be read-only");
	}

	/** Republishing has to visit exactly the resources a sweep amended, and no others. */
	@Test
	void onlyResourcesCarryingDerivedDiagnosticsAreReported() {
		store.recordBase(URI_A, List.of(issue("Unused import", 5, 3)));
		store.recordBase(URI_B, List.of());
		store.recordDerived(URI_B, Map.of(ProviderA.class, List.of(issue("Function 'F' is never used", 10, 1))));

		Assertions.assertEquals(Set.of(URI_B), store.urisWithDerivedDiagnostics());
	}

	@Test
	void everyProvidersContributionIsPublished() {
		Issue fromA = issue("Function 'F' is never used", 10, 1);
		Issue fromB = issue("Type 'T' is never used", 30, 1);

		store.recordDerived(URI_A, Map.of(ProviderA.class, List.of(fromA), ProviderB.class, List.of(fromB)));

		Assertions.assertEquals(Set.of(fromA, fromB), Set.copyOf(store.derivedOf(URI_A)));
	}

	/**
	 * A resource the server has never reported on is not one to start reporting on: it may be a dependency
	 * loaded only to resolve references.
	 */
	@Test
	void aResourceIsPublishedOnlyOnceSomethingHasBeenRecordedForIt() {
		Assertions.assertFalse(store.isPublished(URI_A));

		store.recordBase(URI_A, List.of());

		Assertions.assertTrue(store.isPublished(URI_A));

		store.remove(URI_A);

		Assertions.assertFalse(store.isPublished(URI_A));
	}

	private static Issue issue(String message, int offset, int length) {
		Issue.IssueImpl issue = new Issue.IssueImpl();
		issue.setMessage(message);
		issue.setCode("RosettaIssueCodes.unusedDeclaration");
		issue.setOffset(offset);
		issue.setLength(length);
		return issue;
	}
}
