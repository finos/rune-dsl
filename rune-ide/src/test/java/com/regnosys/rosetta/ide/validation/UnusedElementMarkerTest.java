package com.regnosys.rosetta.ide.validation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.regnosys.rosetta.ide.validation.UnusedElementResourceValidator.Marker;
import com.regnosys.rosetta.rosetta.RosettaFactory;
import com.regnosys.rosetta.rosetta.RosettaPackage;
import com.regnosys.rosetta.rosetta.RosettaRootElement;
import com.regnosys.rosetta.rosetta.RosettaRule;
import com.regnosys.rosetta.rosetta.simple.SimplePackage;
import com.regnosys.rosetta.validation.RosettaIssueCodes;

/**
 * Guards the mapping from a declaration to the marker reported for it, over <em>every</em> named root element
 * the model declares rather than the handful a language-server test happens to exercise.
 *
 * <p>This is the test that makes {@code UnusedElementHelper}'s "every named root element is a candidate" rule
 * safe to state. Candidacy is a rule, so a root element added to the grammar becomes a candidate with no code
 * change — and would then be passed to
 * {@link UnusedElementResourceValidator#markerFor(RosettaRootElement)}, which must produce a usable marker for
 * it rather than throw. The failure mode of throwing there is unusually bad: it happens while diagnostics are
 * being computed for publication, so it surfaces as <em>fewer</em> diagnostics rather than as a visible error.
 *
 * <p>Deliberately not a language-server test — it constructs model objects directly, so it covers kinds that
 * have no convenient concrete syntax and stays fast.
 */
public class UnusedElementMarkerTest {

	@Test
	void everyNamedRootElementGetsAUsableMarker() {
		List<EClass> kinds = namedRootElementClasses();
		// Sanity-check the reflection itself: if this ever finds nothing, the assertions below are vacuous.
		Assertions.assertTrue(kinds.size() >= 15,
				"Expected the model to declare at least 15 named root elements, found " + kinds.size());

		for (EClass eClass : kinds) {
			Marker marker = markerFor(eClass, "Thing");
			Assertions.assertTrue(RosettaIssueCodes.UNUSED_CODES.contains(marker.issueCode()),
					eClass.getName() + " produced issue code " + marker.issueCode()
							+ ", which is not in UNUSED_CODES — the language server gates the faded marker on"
							+ " that set, so it would be published as an ordinary problem instead");
			Assertions.assertTrue(marker.message().contains("'Thing'"),
					eClass.getName() + " produced the message \"" + marker.message()
							+ "\", which does not name the declaration");
			Assertions.assertTrue(Character.isUpperCase(marker.message().charAt(0)),
					eClass.getName() + " produced the message \"" + marker.message()
							+ "\", which does not start with a capital");
		}
	}

	@Test
	void knownKindsAreDescribedByTheirOwnNoun() {
		Assertions.assertEquals("Function 'Thing' is never used",
				markerFor(SimplePackage.Literals.FUNCTION, "Thing").message());
		Assertions.assertEquals("Type 'Thing' is never used",
				markerFor(SimplePackage.Literals.DATA, "Thing").message());
		Assertions.assertEquals("Type alias 'Thing' is never used",
				markerFor(RosettaPackage.Literals.ROSETTA_TYPE_ALIAS, "Thing").message());
		Assertions.assertEquals("Library function 'Thing' is never used",
				markerFor(RosettaPackage.Literals.ROSETTA_EXTERNAL_FUNCTION, "Thing").message());
		Assertions.assertEquals("Rule source 'Thing' is never used",
				markerFor(RosettaPackage.Literals.ROSETTA_EXTERNAL_RULE_SOURCE, "Thing").message());
	}

	/** A subclass is described by its parent's entry, so {@code choice} reads as a type. */
	@Test
	void subclassesInheritTheirParentsNoun() {
		Marker choice = markerFor(SimplePackage.Literals.CHOICE, "Thing");
		Assertions.assertEquals("Type 'Thing' is never used", choice.message());
		Assertions.assertEquals(RosettaIssueCodes.UNUSED_TYPE, choice.issueCode());

		Marker dispatch = markerFor(SimplePackage.Literals.FUNCTION_DISPATCH, "Thing");
		Assertions.assertEquals("Function 'Thing' is never used", dispatch.message());
		Assertions.assertEquals(RosettaIssueCodes.UNUSED_FUNCTION, dispatch.issueCode());
	}

	/**
	 * The two rule flavours are one class distinguished by a flag, and their messages differ usefully: an
	 * eligibility rule can only ever be used by a report, so naming that is more helpful than "never used".
	 */
	@Test
	void ruleMessagesDistinguishTheTwoFlavours() {
		RosettaRule rule = RosettaFactory.eINSTANCE.createRosettaRule();
		rule.setName("Thing");
		Assertions.assertEquals("Reporting rule 'Thing' is never used",
				UnusedElementResourceValidator.markerFor(rule).message());
		Assertions.assertEquals(RosettaIssueCodes.UNUSED_REPORTING_RULE,
				UnusedElementResourceValidator.markerFor(rule).issueCode());

		rule.setEligibility(true);
		Assertions.assertEquals("Eligibility rule 'Thing' is not used by any report",
				UnusedElementResourceValidator.markerFor(rule).message());
		Assertions.assertEquals(RosettaIssueCodes.UNUSED_ELIGIBILITY_RULE,
				UnusedElementResourceValidator.markerFor(rule).issueCode());
	}

	/**
	 * The fallback for a kind with no table entry of its own, which is what a root element added to the grammar
	 * would hit. Demonstrated on {@code RosettaMetaType} — a named root element that is deliberately absent
	 * from the table, because it is excluded from candidacy altogether and so never reaches this code in
	 * practice.
	 */
	@Test
	void unknownKindsFallBackToADerivedNoun() {
		Marker marker = markerFor(RosettaPackage.Literals.ROSETTA_META_TYPE, "Thing");
		Assertions.assertEquals("Meta type 'Thing' is never used", marker.message());
		Assertions.assertEquals(RosettaIssueCodes.UNUSED_DECLARATION, marker.issueCode());
	}

	private static Marker markerFor(EClass eClass, String name) {
		RosettaRootElement element = (RosettaRootElement) EcoreUtil.create(eClass);
		EStructuralFeature nameFeature = eClass.getEStructuralFeature(
				RosettaPackage.Literals.ROSETTA_NAMED__NAME.getName());
		element.eSet(nameFeature, name);
		return UnusedElementResourceValidator.markerFor(element);
	}

	/** Every instantiable named root element in the model, i.e. exactly the candidate set. */
	private static List<EClass> namedRootElementClasses() {
		List<EClass> result = new ArrayList<>();
		for (EPackage ePackage : List.of(RosettaPackage.eINSTANCE, SimplePackage.eINSTANCE)) {
			for (EClassifier classifier : ePackage.getEClassifiers()) {
				if (classifier instanceof EClass eClass
						&& !eClass.isAbstract()
						&& !eClass.isInterface()
						&& RosettaPackage.Literals.ROSETTA_ROOT_ELEMENT.isSuperTypeOf(eClass)
						&& RosettaPackage.Literals.ROSETTA_NAMED.isSuperTypeOf(eClass)) {
					result.add(eClass);
				}
			}
		}
		return result;
	}
}
