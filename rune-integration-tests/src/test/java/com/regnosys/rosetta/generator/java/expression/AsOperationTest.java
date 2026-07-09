package com.regnosys.rosetta.generator.java.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.testmodel.JavaTestModel;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class AsOperationTest {
    @Inject
    private RosettaTestModelService modelService;

    private JavaTestModel dataModel() {
        return modelService.toJavaTestModel("""
                namespace test

                type Foo:

                type Bar extends Foo:
                    barAttr int (1..1)

                type Qux extends Foo:
                    quxAttr int (1..1)
                """).compile();
    }

    private JavaTestModel choiceModel() {
        return modelService.toJavaTestModel("""
                namespace test

                type Bar:
                    barAttr int (1..1)

                type Qux:
                    quxAttr int (1..1)

                choice Foo:
                    Bar
                    Qux
                """).compile();
    }

    // --- Data types ---

    @Test
    void asDataTypeSingleMatchTest() {
        JavaTestModel model = dataModel();

        Integer result = model.evaluateExpression(Integer.class, """
                (if True then Bar { barAttr: 42 } else Foo {}) as Bar -> barAttr
                """);

        assertEquals(42, result);
    }

    @Test
    void asDataTypeSingleNoMatchReturnsEmptyTest() {
        JavaTestModel model = dataModel();

        // The value is a `Qux`, so narrowing to `Bar` yields empty.
        Integer result = model.evaluateExpression(Integer.class, """
                (if True then Qux { quxAttr: 12 } else Foo {}) as Bar -> barAttr
                """);

        assertNull(result);
    }

    @Test
    void asDataTypeMultiFiltersTest() {
        JavaTestModel model = dataModel();

        // Only the two `Bar` items survive the filter.
        Integer count = model.evaluateExpression(Integer.class, """
                [Bar { barAttr: 1 }, Qux { quxAttr: 2 }, Bar { barAttr: 3 }] as Bar count
                """);
        assertEquals(2, count);

        Integer sum = model.evaluateExpression(Integer.class, """
                [Bar { barAttr: 1 }, Qux { quxAttr: 2 }, Bar { barAttr: 3 }] as Bar -> barAttr sum
                """);
        assertEquals(4, sum);
    }

    // --- Choice types ---

    @Test
    void asChoiceOptionMatchTest() {
        JavaTestModel model = choiceModel();

        Integer result = model.evaluateExpression(Integer.class, """
                Foo { Bar: Bar { barAttr: 42 }, ... } as Bar -> barAttr
                """);

        assertEquals(42, result);
    }

    @Test
    void asChoiceOptionNoMatchReturnsEmptyTest() {
        JavaTestModel model = choiceModel();

        // The choice holds a `Qux`, so narrowing to `Bar` yields empty.
        Integer result = model.evaluateExpression(Integer.class, """
                Foo { Qux: Qux { quxAttr: 12 }, ... } as Bar -> barAttr
                """);

        assertNull(result);
    }

    @Test
    void asChoiceMultiFiltersTest() {
        JavaTestModel model = choiceModel();

        // Only the choice holding a `Bar` contributes.
        Integer sum = model.evaluateExpression(Integer.class, """
                [Foo { Bar: Bar { barAttr: 1 }, ... }, Foo { Qux: Qux { quxAttr: 2 }, ... }] as Bar -> barAttr sum
                """);

        assertEquals(1, sum);
    }

    @Test
    void asChoiceOptionWithMetadataTest() {
        // Narrowing to a choice option that carries metadata must preserve that metadata, so that the
        // metadata (e.g. `scheme`) is accessible on the result - consistent with the `switch` operator.
        JavaTestModel model = modelService.toJavaTestModel("""
                namespace test

                typeAlias MyString: string(maxLength: 42)

                choice Foo:
                    number
                    MyString
                        [metadata scheme]
                """).compile();

        String result = model.evaluateExpression(String.class, """
                Foo { MyString: "abc123" with-meta { scheme: "myScheme" }, ... } as MyString -> scheme
                """);

        assertEquals("myScheme", result);
    }

    @Test
    void asChoiceOptionThatExtendsSiblingOptionShouldNotThrow() {
        JavaTestModel model = modelService.toJavaTestModel("""
                namespace test

                choice Foo:
                    Bar
                    Qux

                type Bar:
                    barAttr string (0..1)

                type Qux extends Bar:
                    quxAttr string (0..1)
                """).compile();

        Boolean result = model.evaluateExpression(Boolean.class, """
                Foo { Bar: Bar { barAttr: "x" }, ... } as Qux is absent
                """);

        assertTrue(result);
    }

    @Test
    void asNestedChoiceOptionThatExtendsSiblingOptionShouldNotThrow() {
        JavaTestModel model = modelService.toJavaTestModel("""
                namespace test

                type Product:
        		   economicTerms EconomicTerms (0..1)

                type EconomicTerms:
        		   payout PayoutChoice (0..1)

                choice PayoutChoice:
                    InterestRatePayout
                    OtherPayout

                type InterestRatePayout:
        		    rateSpecification RateSpecficationChoice (0..1)

                type OtherPayout:
                    attr int (0..1)

                choice RateSpecficationChoice:
                    FloatingRateSpecification
                    InflationRateSpecification

                type FloatingRateSpecification:
                    attr1 int (0..1)

                type InflationRateSpecification extends FloatingRateSpecification:
                    attr2 int (0..1)
     """).compile();

        Boolean result = model.evaluateExpression(Boolean.class, """
                Product {
                    economicTerms: EconomicTerms {
                        payout: PayoutChoice {
                            InterestRatePayout: InterestRatePayout {
                                rateSpecification: RateSpecficationChoice {
                                    FloatingRateSpecification: FloatingRateSpecification { attr1: 1 },
                                    ...
                                }
                            },
                            ...
                        }
                    }
                } -> economicTerms -> payout as InterestRatePayout -> rateSpecification as InflationRateSpecification is absent
                """);
        assertTrue(result);
    }

    @Test
    void asChoiceOptionReachableViaSiblingSupertypeShouldNotThrow() {
        JavaTestModel model = modelService.toJavaTestModel("""
                namespace test

                choice Outer:
                    Inner
                    Base

                choice Inner:
                    Derived
                    Sibling

                type Base:
                    baseAttr int (0..1)

                type Derived extends Base:
                    derivedAttr int (0..1)

                type Sibling:
                    siblingAttr int (0..1)
                """).compile();

        // The Outer holds a plain `Base`. `as Derived is absent` should return true (a Base is not a
        // Derived), not throw a ClassCastException.
        Boolean result = model.evaluateExpression(Boolean.class, """
                Outer { Base: Base { baseAttr: 1 }, ... } as Derived is absent
                """);

        assertTrue(result);
    }

    private JavaTestModel siblingSupertypeAcrossBranchesModel() {
        // `Bar` is reachable both directly (via Inner1) and as a subtype of `Foo` (via Inner2). The extra
        // options `A`/`B` make `Inner1` and `Inner2` incomparable subtype-wise, so the path finder cannot
        // lean on "most specific" to disambiguate and must navigate to the exact requested option.
        return modelService.toJavaTestModel("""
                namespace test

                type Foo:

                type Bar extends Foo:
                    barAttr int (0..1)

                type Qux extends Foo:
                    quxAttr int (0..1)

                type A:

                type B:

                choice Outer:
                    Inner1
                    Inner2

                choice Inner1:
                    Bar
                    A

                choice Inner2:
                    Foo
                    B
                """).compile();
    }

    @Test
    void asNavigatesToExactOptionWhenSiblingBranchesAreIncomparable() {
        JavaTestModel model = siblingSupertypeAcrossBranchesModel();
        // Value is stored under Inner1.Bar -> `as Bar` must navigate to the exact Inner1.Bar option and
        // yield that Bar. Picking the sibling supertype branch (Inner2.Foo) instead would wrongly return null.
        Integer result = model.evaluateExpression(Integer.class, """
                Outer { Inner1: Inner1 { Bar: Bar { barAttr: 1 }, ... }, ... } as Bar -> barAttr
                """);
        assertEquals(1, result);
    }

    @Test
    void asToOptionInUnselectedBranchIsAbsentAndDoesNotThrow() {
        JavaTestModel model = siblingSupertypeAcrossBranchesModel();
        // Value is a Qux stored under Inner2.Foo -> `as Bar` navigates to the (absent) Inner1.Bar option,
        // so the result is absent and no ClassCastException is thrown.
        Boolean result = model.evaluateExpression(Boolean.class, """
                Outer { Inner2: Inner2 { Foo: Qux { quxAttr: 2 }, ... }, ... } as Bar is absent
                """);
        assertTrue(result);
    }

    @Test
    void asDoesNotMatchSubtypeStoredUnderSiblingSupertypeOption() {
        JavaTestModel model = siblingSupertypeAcrossBranchesModel();
        // A Bar stored under the Inner2.Foo slot lives in a different option than Inner1.Bar, so `as Bar`
        // (which navigates to the exact Inner1.Bar option) yields absent.
        Boolean result = model.evaluateExpression(Boolean.class, """
                Outer { Inner2: Inner2 { Foo: Bar { barAttr: 3 }, ... }, ... } as Bar is absent
                """);
        assertTrue(result);
    }

    @Test
    void asChoiceTwoAliasOptionsWithSameBaseTypeShouldNavigateToCorrectField() {
        JavaTestModel model = modelService.toJavaTestModel("""
                namespace test

                typeAlias Bar: string
                typeAlias Qux: string

                choice Foo:
                    Bar
                    Qux
                """).compile();

        // Holds Qux → `as Qux` must return the Qux value.
        String qux = model.evaluateExpression(String.class, """
                Foo { Qux: "quxValue", ... } as Qux
                """);
        assertEquals("quxValue", qux);

        // Holds Bar → `as Qux` must yield empty, not the Bar value.
        String absent = model.evaluateExpression(String.class, """
                Foo { Bar: "barValue", ... } as Qux
                """);
        assertNull(absent);
    }

    @Test
    void asChoiceTwoEnumAliasOptionsWithSameBaseEnumShouldNavigateToCorrectField() {
        JavaTestModel model = modelService.toJavaTestModel("""
                namespace test

                enum Base:
                    VALUE_1
                    VALUE_2

                typeAlias Bar: Base
                typeAlias Qux: Base

                choice Foo:
                    Bar
                    Qux
                """).compile();

        // Holds Qux with VALUE_2 → `as Qux` must return that value.
        @SuppressWarnings("rawtypes")
        Class rawEnumClass = model.getEnumJavaClass("Base");
        @SuppressWarnings("unchecked")
        Object qux = model.evaluateExpression(rawEnumClass, """
                Foo { Qux: Base -> VALUE_2, ... } as Qux
                """);
        assertEquals(model.getEnumJavaValue("Base", "VALUE_2"), qux);

        // Holds Bar → `as Qux` must yield empty, not the Bar value.
        @SuppressWarnings("unchecked")
        Object absent = model.evaluateExpression(rawEnumClass, """
                Foo { Bar: Base -> VALUE_1, ... } as Qux
                """);
        assertNull(absent);
    }

    @Test
    void asChoiceTwoDataTypeAliasOptionsWithSameBaseTypeShouldNavigateToCorrectField() {
        JavaTestModel model = modelService.toJavaTestModel("""
                namespace test

                type Base:
                    attr string (1..1)

                typeAlias Bar: Base
                typeAlias Qux: Base

                choice Foo:
                    Bar
                    Qux
                """).compile();

        // Holds Qux → `as Qux` must return the Qux value.
        String qux = model.evaluateExpression(String.class, """
                Foo { Qux: Qux { attr: "quxValue" }, ... } as Qux -> attr
                """);
        assertEquals("quxValue", qux);

        // Holds Bar → `as Qux` must yield empty, not the Bar value.
        String absent = model.evaluateExpression(String.class, """
                Foo { Bar: Bar { attr: "barValue" }, ... } as Qux -> attr
                """);
        assertNull(absent);
    }

    @Test
    void asAliasLeafOptionInNestedChoiceShouldNavigateCorrectly() {
        // `TargetAlias` is a typeAlias used as a leaf option inside a nested choice.
        // The outer `as TargetAlias` must traverse through `Inner` and find the alias option.
        JavaTestModel model = modelService.toJavaTestModel("""
                namespace test

                type TargetType:
                    attr string (1..1)

                typeAlias TargetAlias: TargetType

                choice Inner:
                    TargetAlias
                    Sibling

                type Sibling:
                    siblingAttr int (1..1)

                choice Outer:
                    Inner
                    Other

                type Other:
                    otherAttr int (1..1)
                """).compile();

        // Navigate through `Inner` to reach the aliased `TargetAlias` option.
        String result = model.evaluateExpression(String.class, """
                Outer { Inner: Inner { TargetAlias: TargetAlias { attr: "val" }, ... }, ... } as TargetAlias -> attr
                """);
        assertEquals("val", result);

        // Holds the sibling option → `as TargetAlias` must yield empty.
        String absent = model.evaluateExpression(String.class, """
                Outer { Inner: Inner { Sibling: Sibling { siblingAttr: 1 }, ... }, ... } as TargetAlias -> attr
                """);
        assertNull(absent);
    }

    @Test
    void asAliasIntermediateChoiceOptionShouldNavigateToItsNestedOptions() {
        // `InnerAlias` is a typeAlias for choice `Inner`, used as an intermediate option in `Outer`.
        // `as TargetOpt` must look through the alias to find `TargetOpt` inside `Inner`.
        JavaTestModel model = modelService.toJavaTestModel("""
                namespace test

                type TargetOpt:
                    attr string (1..1)

                type Sibling:
                    siblingAttr int (1..1)

                choice Inner:
                    TargetOpt
                    Sibling

                typeAlias InnerAlias: Inner

                choice Outer:
                    InnerAlias
                    Other

                type Other:
                    otherAttr int (1..1)
                """).compile();

        // Navigate through the `InnerAlias` intermediate to reach `TargetOpt`.
        String result = model.evaluateExpression(String.class, """
                Outer { InnerAlias: Inner { TargetOpt: TargetOpt { attr: "val" }, ... }, ... } as TargetOpt -> attr
                """);
        assertEquals("val", result);

        // Holds the sibling option → `as TargetOpt` must yield empty.
        String absent = model.evaluateExpression(String.class, """
                Outer { InnerAlias: Inner { Sibling: Sibling { siblingAttr: 1 }, ... }, ... } as TargetOpt -> attr
                """);
        assertNull(absent);
    }

    @Test
    void asNestedChoiceOptionTest() {
        JavaTestModel model = modelService.toJavaTestModel("""
                namespace test

                choice Foo:
                    Opt1
                    Bar

                choice Bar:
                    Opt2
                    Opt3

                type Opt1:
                    opt1Attr int (1..1)

                type Opt2:
                    opt2Attr int (1..1)

                type Opt3:
                    opt3Attr int (1..1)
                """).compile();

        // Navigate through the nested choice `Bar` to reach option `Opt2`.
        Integer result = model.evaluateExpression(Integer.class, """
                Foo { Bar: Bar { Opt2: Opt2 { opt2Attr: 7 }, ... }, ... } as Opt2 -> opt2Attr
                """);
        assertEquals(7, result);

        // The nested choice holds an `Opt2`, so narrowing to `Opt3` yields empty.
        Integer empty = model.evaluateExpression(Integer.class, """
                Foo { Bar: Bar { Opt2: Opt2 { opt2Attr: 7 }, ... }, ... } as Opt3 -> opt3Attr
                """);
        assertNull(empty);
    }
}
