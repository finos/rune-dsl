Rosetta Modelling Artefacts
===========================

**The Rosetta syntax can express five types of modelling artefacts**, which are all used as part of the CDM:

* Data representation
* Data integrity
* Object qualification
* Function specification
* Mapping (or *synonym*)

This documentation details the purpose and features of each of those modelling artefacts and highlights the relationships that exists among those. As the initial live application of the Rosetta DSL, examples from the ISDA CDM will be used to illustrate each of those artefacts.

Data Representation Artefacts
-----------------------------

**Rosetta provides six data representation components** to build data artefacts in the model:

* Classes
* Attributes
* Enumerations
* Enumerations values
* Choice rules
* Aliases

Classes
^^^^^^^

Purpose
"""""""

Classes are objects that contain the granular data representation elements, in the form of attributes.

Syntax
""""""

The class content is delineated by curly brackets ``{`` ``}``.

Rosetta supports the concept of **abstract classes**, which cannot be instantiated as part of the generated executable code and are meant to be extended by other classes.  An example of such is the ``IdentifiedProduct`` class, which acts as the baseline for the products which terms are abstracted through reference data and can be extended by the respective variations of such products, as illustrated by the ``Loan`` class.

**Note**: For clarity purposes, the documentation snippets omit the synonyms and definitions that are associated with the classes and attributes, unless the purpose of the snippet it to highlight some of those features.

.. code-block:: Java

 abstract class IdentifiedProduct
 {
  productIdentifier ProductIdentifier (1..1);
 }

 class Loan extends IdentifiedProduct
 {
  borrower LegalEntity (0..*);
  lien string (0..1) scheme;
  facilityType string (0..1) scheme;
  creditAgreementDate date (0..1);
  tranche string (0..1) scheme;
 }

The Rosetta convention is that class names start with a capital letter. Class names need to be unique across the model, including with respect to rule names. Both those are controlled by the Rosetta grammar.

A plain-text definition of each modelling artefact can be added in Rosetta as a string using ``"`` ``"`` in between angle brackets: ``<`` ``>``.

.. code-block:: Java

 class ContractualProduct <"A class to specify the contractual products' economic terms, alongside their product identification and product taxonomy. The contractual product class is meant to be used across the pre-execution, execution and (as part of the Contract) post-execution lifecycle contexts.">
 {
  productIdentification ProductIdentification (0..1) <"The product identification value(s) that might be associated with a contractual product. The CDM provides the ability to associate several product identification methods with a product.">;
  productTaxonomy ProductTaxonomy (0..*) <"The product taxonomy value(s) associated with a contractual product.">;
  economicTerms EconomicTerms (1..1) <"The economic terms associated with a contractual product, i.e. the set of features that are price-forming.">;
 }

Such definitions, although not producing any executable code artefact, are an integral part of the model as meta-data components. As modelling best practice, a definitions ought to exist for every artefact and be clear and comprehensive.

Attributes
^^^^^^^^^^

Purpose
"""""""

Attributes specify the granular model elements in terms of type of value (e.g. ``integer``, ``string``, enumerated value), cardinality and through an associated definition.

Syntax
""""""

A Rosetta attribute can be specified either as a basic type, a class or an enumeration.

The set of **basic types** available in Rosetta are:

* Text - ``string``
* Number - ``int`` - ``number``
* Logic - ``boolean``
* Date and Time - ``date`` - ``time`` - ``zonedDateTime``
* Calculation - ``calculation``
* Product and event qualification - ``productType`` - ``eventType``

Time
""""

As it relates to time zone adjustments, a time zone qualifier can be specified alongside a time in one of two ways:

* Through the ``zonedDateTime`` type, which needs to be expressed either as UTC or as an offset to UTC, as specified by the ISO 8601 standard.
* Through the ``BusinessCenterTime`` class, where time is specified alongside a business center.  This is used to specify a time dimension in relation to a future event, e.g. the earliest or latest exercise time of an option.

While there has been discussion as to whether Rosetta should support dates which are specified as an offset to UTC with the ``Z`` suffix, no positive conclusion has been reached so far. The main reason is that all dates which need a business date context are already being provided with the ability to specify an associated business center.

Calculation
"""""""""""

The ``calculation`` qualifier represents the outcome of calculation in the model. It is currently associated with two attributes: ``cashflowCalculation`` in the ``Cashflow`` class, and ``callFunction`` in the ``computedAmount`` class.)

Meta-Types
""""""""""

Rosetta allows to associate a set of qualifiers to an attribute: the ``scheme`` and ``reference`` meta-types.

* The ``scheme`` meta-type specifies a scheme reference to constrain the set of values that the attribute can take. The relevant scheme value is then specified as meta-information in the attribute synonyms. A scheme can exist for attribute that are of an enumeration type but can also serve to constrain the basic ``string`` type.

* The ``reference`` meta-type replicates the cross-referencing mechanism widely used in XML to provide data integrity within the context of an instance document - in particular the ``href`` mechanism, for *hyper-text reference*, as used in the FpML standard. The cross-reference value can be specified as meta-information in the attribute synonyms.

To make objects internally referenceabale (beyond cross-references that are externally provided by an instance document), Rosetta also allows to associate a unique identifier to instances of a class, by  adding a ``key`` qualifier to the class name. The ``key`` corresponds to a hash code generated by the model implementation. The implementation provided as part of the Rosetta DSL is the default Java hash function.

The below ``Party`` and ``Identifier`` classes provide a good illustration as to how **meta-types** are implemented. The ``key`` qualifier is associated to the ``Party`` class, while the ``reference`` qualifier is associated to the ``issuerReference`` attribute of the ``Identifier`` class, which is of type ``Party``. The ``issuerReference`` can be provided as an external cross-reference, for which the value ``issuer`` is specified in the synonym source using ``href`` as the ``meta`` qualifier. The ``issuer`` attribute has an associated ``scheme``, which ``issuerIdScheme`` value is specified in the synonym source using the ``meta`` qualifier.

.. code-block:: Java

 class Party key
 {
  partyId string (1..*) scheme;
   [synonym FpML_5_10 value partyId meta partyIdScheme]
  naturalPerson NaturalPerson (0..*);
 }

 class Identifier key
 {
  issuerReference Party (0..1) reference;
   [synonym FpML_5_10 value issuer meta href]
  issuer string (0..1) scheme;
   [synonym FpML_5_10, CME_SubmissionIRS_1_0 value issuer meta issuerIdScheme]
  assignedIdentifier AssignedIdentifier (1..*);
}

The ``rosettaKeyValue`` is a variation of ``key``, which associated hash function doesn't include any of those qualifiers that are associated with the attributes. Some of those qualifiers are automatically generated by algorithm (typically, the anchors and references associated with XML documents) and would result in differences between two instance documents, even if those documents would have the same actual values.

The ``RosettaKeyValue`` is meant to be used for supporting the reconciliation of economic terms, and is hence associated with the ``EconomicTerms`` class. Further evaluation of this ``rosettaKeyValue``, and whether this is an appropriate implementation of a matching algorithm for economic terms, is necessary.

.. code-block:: Java

 class EconomicTerms rosettaKeyValue
 {
  payout Payout (1..1);
  earlyTerminationProvision EarlyTerminationProvision (0..1);
  cancelableProvision CancelableProvision (0..1);
  extendibleProvision ExtendibleProvision (0..1);
 }
 
Enumerations
^^^^^^^^^^^^

Purpose
"""""""

Enumerations are the mechanism through which controlled values are specified at the attribute level. They are the container for the corresponding set of enumeration values.

As mentioned in the *Attributes* section, the schemes which values are specified as part of an existing standard like FpML are represented through enumerations in the model, while schemes with no defined values are represented as a type ``string``.  In both cases, the scheme reference associated with the originating element can be added to the relevant synonym source as meta-information, so that no originating information is disregarded.

Syntax
""""""

Enumerations are very simple modelling container artefacts. They can have associated synonyms.

Similar to the class, the enumeration is delineated by brackets ``{`` ``}``.

.. code-block:: Java

 enum MarketDisruptionEnum
 {
  ModifiedPostponement,
  Omission,
  Postponement
 }

Enumeration Values
^^^^^^^^^^^^^^^^^^

Purpose
"""""""

As indicated in the *Enumerations* section, enumeration values are the set of controlled values that are specified as part of an enumeration container.

Syntax
""""""

Enumeration values have a restricted syntax to facilitate their integration with executable code: they cannot start with a numerical digit, and the only special character that can be associated with them is the underscore ``_``.

In order to handle the integration of FpML scheme values such as the *dayCountFractionScheme* which has values such as ``ACT/365.FIXED`` or ``30/360``, the Rosetta syntax allows to associate a **displayName synonym**. Those values with special characters have those special characters replaced with ``_`` and have an associated ``displayName`` entry which corresponds to the actual value. Examples of such are ``ACT_365_FIXED`` and ``_30_360``, with the associated display names of ``ACT/365.FIXED`` and ``30/360``, respectively.

.. code-block:: Java

 enum DayCountFractionEnum
 {
  ACT_360 displayName "ACT/360"
  ACT_365L displayName "ACT/365L"
  ACT_365_FIXED displayName "ACT/365.FIXED"
  ACT_ACT_AFB displayName "ACT/ACT.AFB"
  ACT_ACT_ICMA displayName "ACT/ACT.ICMA"
  ACT_ACT_ISDA displayName "ACT/ACT.ISDA"
  ACT_ACT_ISMA displayName "ACT/ACT.ISMA"
  BUS_252 displayName "BUS/252"
  _1_1 displayName "1/1"
  _30E_360 displayName "30E/360"
  _30E_360_ISDA displayName "30E/360.ISDA"
  _30_360 displayName "30/360"
 }

Choice Rules
^^^^^^^^^^^^

Purpose
"""""""

Choice rules apply within the context of a class. They define a choice constraint between a set of attributes. They are meant as a simple and robust construct to translate the XML *xsd:choicesyntax* as part of any model created using Rosetta, although their usage is not limited to those XML use cases.

Syntax
""""""

Choice rules only apply within the context of a class, and the naming convention is ``<className>_choice``, e.g. ``NaturalPerson_choice``. If multiple choice rules exist in relation to a class, the naming convention is to suffix the 'choice' term with a number, e.g. ``NaturalPerson_choice1`` and ``NaturalPerson_choice2``.

.. code-block:: Java

 class ExerciseOutcome
 {
  contract Contract (1..1);
  physicalExercise PhysicalExercise (0..1);
  cashExercise Cashflow (0..1);
 }

 choice rule ExerciseOutcome_choice <"A option exercise results in either a physical or a cash exercise.">
  for ExerciseOutcome required choice between
  physicalExercise and cashExercise

The choice constraint can either be **required** (implying that exactly one of the attributes needs to be present) or **optional** (implying that at most one of the attributes needs to be present).

While most of the choice rules have two attributes, there is no limit to the number of attributes associated with it, within the limit of the number of attributes associated with the class at stake. ``OptionCashSettlement_choice`` is a good illustration of this.

.. code-block:: Java

 class OptionCashSettlement
 {
  cashSettlementValuationTime BusinessCenterTime (0..1);
  cashSettlementValuationDate RelativeDateOffset (0..1);
  cashSettlementPaymentDate CashSettlementPaymentDate (0..1);
  cashPriceMethod CashPriceMethod (0..1);
  cashPriceAlternateMethod CashPriceMethod (0..1);
  parYieldCurveAdjustedMethod YieldCurveMethod (0..1);
  zeroCouponYieldAdjustedMethod YieldCurveMethod (0..1);
  parYieldCurveUnadjustedMethod YieldCurveMethod (0..1);
  crossCurrencyMethod CrossCurrencyMethod (0..1);
  collateralizedCashPriceMethod YieldCurveMethod (0..1);
 }

 choice rule OptionCashSettlement_choice
  for OptionCashSettlement optional choice between
  cashPriceMethod and cashPriceAlternateMethod and parYieldCurveAdjustedMethod and zeroCouponYieldAdjustedMethod
  and parYieldCurveUnadjustedMethod and crossCurrencyMethod and collateralizedCashPriceMethod

Members of a choice rule need to have their lower cardinality set to 0, something which is enforced by a validation rule.

One of syntax as a complement to the choice rule
""""""""""""""""""""""""""""""""""""""""""""""""""""

In the case where all the attributes of a given class are subject to a required choice logic that results in one and only one of them being present in any insatnce of that class, Rosetta allows to qualify the class with the ``one of`` qualifier. This by-passes the need to implement the choice rule. This feature is illustrated in the ``BondOptionStrike`` class.

.. code-block:: Java

 class BondOptionStrike one of
 {
  referenceSwapCurve ReferenceSwapCurve (0..1) ;
  price OptionStrike (0..1);
 }

Aliases
^^^^^^^

Purpose
"""""""

Aliases have been introduced as part of the Rosetta syntax because:

* Model tree expressions can be cumbersome at time and hence may contradict the primary goals of clarity and legibility.
* Aliases can be reused across various modelling artefacts that make use of the same model tree expressions, such as data rule, event and product qualification or calculation.

Syntax
""""""

The alias syntax is straightforward: ``alias <name> <Rosetta expression>``.

The alias name needs to be unique across the product and event qualifications, the classes and the aliases, and validation logic is in place to enforce this.  The naming convention is to have one camelCased word, instead of a composite name as for the Rosetta rules, with implied meaning.

The below snippet presents an example of such alias and its use as part of an event qualification.

.. code-block:: Java

 alias novatedContractEffectiveDate
  Event -> primitive -> inception -> after -> contract -> contractualProduct -> economicTerms -> payout -> interestRatePayout -> calculationPeriodDates -> effectiveDate -> date
  or Event -> primitive -> inception -> after -> contract -> contractualProduct -> economicTerms -> payout -> interestRatePayout -> calculationPeriodDates -> effectiveDate -> adjustableDate -> adjustedDate
  or Event -> primitive -> inception -> after -> contract -> contractualProduct -> economicTerms -> payout -> interestRatePayout -> calculationPeriodDates -> effectiveDate -> adjustableDate -> unadjustedDate

 isEvent Novation
  Event -> intent when present = IntentEnum.Novation
  and Event -> primitive -> quantityChange exists
  and Event -> primitive -> inception exists
  and quantityAfterQuantityChange = 0.0
  and Event -> primitive -> quantityChange -> after -> contract -> closedState -> state = ClosedStateEnum.Novated
  and Event -> primitive -> inception -> after -> contract -> contractIdentifier <> Event -> primitive -> quantityChange -> before -> contract -> contractIdentifier
  and Event -> eventDate = Event -> primitive -> inception -> after -> contract -> tradeDate -> date
  and Event -> effectiveDate = novatedContractEffectiveDate

Mapping Artefacts
-----------------

Synonyms
^^^^^^^^

Purpose
"""""""

Synonym is the baseline building block in the relationship between the model in Rosetta and alternative data representations, whether those are open standards or proprietary. Synonyms can be complemented by relevant mapping logic when the relationship is not a one-to-one or is conditional.

Synonyms can be associated to all four sets of Rosetta data modelling artefacts:

*  Classes
*  Attributes
*  Enumerations
*  Enumeration values

There is no limit to the number of synonyms that can be associated with each of those artefacts, and there can even be several synonyms for a given data source (e.g. in the case of a conditional mapping).


Syntax
""""""

The baseline synonym syntax has two components:

* **source**, which possible values are controlled by the grammar
* **value**, which is of type ``identifier``

Example:

  ``[synonym FpML_5_10, CME_SubmissionIRS_1_0, DTCC_11_0, DTCC_9_0, CME_ClearedConfirm_1_17 value averagingInOut]``

A further set of attributes can be associated with a synonym, to address specific use cases:

* **path**, to allow mapping when data is nested in different ways between the respective models. The ``Payout`` class is a good illustration of such cases:

.. code-block:: Java

 class Payout
 {
  interestRatePayout InterestRatePayout (0..*);
   [synonym FpML_5_10, CME_SubmissionIRS_1_0, DTCC_11_0, DTCC_9_0, CME_ClearedConfirm_1_17 value swapStream path "trade.swap" ]
   [synonym FpML_5_10, CME_SubmissionIRS_1_0, DTCC_11_0, DTCC_9_0, CME_ClearedConfirm_1_17 value swapStream path "swap"]
   [synonym FpML_5_10, CME_SubmissionIRS_1_0, DTCC_11_0, DTCC_9_0, CME_ClearedConfirm_1_17 value swapStream]
   [synonym FpML_5_10, CME_SubmissionIRS_1_0, DTCC_11_0, DTCC_9_0, CME_ClearedConfirm_1_17 value generalTerms path "trade.creditDefaultSwap", feeLeg path "trade.creditDefaultSwap" set when "trade.creditDefaultSwap.feeLeg.periodicPayment" exists]
   [synonym FpML_5_10, CME_SubmissionIRS_1_0, DTCC_11_0, DTCC_9_0, CME_ClearedConfirm_1_17 value generalTerms path "creditDefaultSwap", feeLeg path "creditDefaultSwap" set when "creditDefaultSwap.feeLeg.periodicPayment" exists]
   [synonym FpML_5_10, CME_SubmissionIRS_1_0, DTCC_11_0, DTCC_9_0, CME_ClearedConfirm_1_17 value feeLeg, generalTerms]
   [synonym FpML_5_10, CME_SubmissionIRS_1_0, DTCC_11_0, DTCC_9_0, CME_ClearedConfirm_1_17 value capFloorStream path "trade.capFloor"]
   [synonym FpML_5_10, DTCC_11_0, DTCC_9_0, CME_ClearedConfirm_1_17 value fra path "trade" mapper FRAIRPSplitter]
   [synonym CME_SubmissionIRS_1_0 value fra mapper FRAIRPSplitter]
   [synonym FpML_5_10, CME_SubmissionIRS_1_0, DTCC_11_0, DTCC_9_0, CME_ClearedConfirm_1_17 value interestLeg path "trade.returnSwap", interestLeg path "trade.equitySwapTransactionSupplement"]
  (...)
 }

* **tag** or a **componentID** to properly reflect the FIX standard, which uses those two artefacts. There are only limited examples of such at present, as a result of the scope focus on post-execution use cases hence the limited reference to the FIX standard.

.. code-block:: Java

 class Strike
 {
  strikeRate number (1..1);
   [synonym FIX_5_0_SP2 value StrikePrice tag 202]
  buyer PayerReceiverEnum (0..1);
  seller PayerReceiverEnum (0..1);
 }

* **definition**, to provide a more explicit reference to the FIX enumeration values which are specified through a single digit or letter positioned as a prefix to the associated definition.

.. code-block:: Java

 enum InformationProviderEnum
 {
  (...)
  Bloomberg
   [synonym FIX_5_0_SP2 value "0" definition "0 = Bloomberg"],
  (...)
  Other
   [synonym FIX_5_0_SP2 value "99" definition "99 = Other"],
  (...)
  Telerate
   [synonym FIX_5_0_SP2 value "2" definition "2 = Telerate"]
 }

In contrast to other model artefacts, the synonym value associated with enumeration values is of type ``string``, to facilitate integration with executable code. The alternative approach consisting in specifying the value as a compatible identifier alongside with a display name has been disregarded because it has been deemed not appropriate to create a 'code-friendly' value for the respective synonyms.  A ``string`` type removes such need.

Mapping Logic
^^^^^^^^^^^^^

Purpose
"""""""

There are cases where the relationship between the marketplace standards and protocols and their relation to the CDM is not one-to-one or is conditional.

Hence, the need to complement the synonyms with a syntax that provides the ability to express a mapping logic in a manner that provides a good balance between flexibility and legibility.

Syntax
""""""

The mapping logic differs from the data rule, choice rule and calculation syntax in that its syntax is not expressed as a stand-alone block with a qualifier prefix such as ``rule``. Rather, the mapping rule is positioned as an extension to the synonym expression, and each of the mapping expressions (several mapping expressions can be associated with a given synonym) is prefixed with the ``set`` qualifier, followed by the name of the Rosetta attribute to which the synonym is being mapped to.

The mapping syntax is composed of two (optional) expressions: a **mapping value** that is prefixed with ``to``, which purpose is to provide the ability to map a specific value that is distinct from the one originating from the source document, and a **conditional expression** that is prefixed with ``when``, which purpose is to associate conditional logic to the mapping expression.

The mapping logic associated with the below ``action`` attribute provides a good illustration of such logic.

.. code-block:: Java

 class Event
 {
  (...)
  action ActionEnum (1..1) <"Specifies whether the event is a new, a correction or a cancellation.">;
   [synonym Rosetta_Workbench
    set to ActionEnum.New when "isCorrection" = False,
    set to ActionEnum.Correct when "isCorrection" = True,
    set to ActionEnum.Cancel when "isRetraction" = True]
   [synonym FpML_5_10
    set to ActionEnum.New when "isCorrection" = False,
    set to ActionEnum.Correct when "isCorrection" = True]
   [synonym DTCC_11_0, DTCC_9_0 value Activity path "Header.OTC_RM.Manifest.TradeMsg"]
   [synonym CME_SubmissionIRS_1_0 value TransTyp path "TrdCaptRpt"]
  (...)
 }

Data Validation Artefacts
-------------------------

Data Rules
^^^^^^^^^^

Purpose
"""""""

Data rules are the primary channel through which data validation is enforced as part of Rosetta.

A good initial illustration of such role relates to how data constraints specified as part of the FpML documentation are expressed as part of those rules – and hence become part of the executable code case that is generated from the model.

As an example, the ``FpML_ird_57`` data rule implements the **FpML ird validation rule #57**, which states that if the notional step schedule is absent, then the initial value of the notional schedule must not be null.  While at present the FpML logic needs to be evaluated and transcribed into code by the relevant teams (with the implication that, more often than not, such logic is actually not enforced), its programmatic implementation is available alongside a legible view of it as part of Rosetta.

.. code-block:: Java

 class Frequency
 {
  id (0..1) ;
  periodMultiplier int (1..1) ;
  period PeriodExtendedEnum (1..1) ;
 }

 class CalculationPeriodFrequency extends Frequency
 {
  rollConvention RollConventionEnum (1..1) ;
 }

 data rule FpML_ird_57 <"FpML validation rule ird-57 - Context: CalculationPeriodFrequency. [period eq ('M', 'Y')] not(rollConvention = ('NONE', 'SFE', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT','SUN')).">
  when CalculationPeriodFrequency -> period = PeriodExtendedEnum.M or CalculationPeriodFrequency -> period = PeriodExtendedEnum.Y
  then CalculationPeriodFrequency -> rollConvention <> RollConventionEnum.NONE
   or CalculationPeriodFrequency -> rollConvention <> RollConventionEnum.SFE
   or CalculationPeriodFrequency -> rollConvention <> RollConventionEnum.MON
   or CalculationPeriodFrequency -> rollConvention <> RollConventionEnum.TUE
   or CalculationPeriodFrequency -> rollConvention <> RollConventionEnum.WED
   or CalculationPeriodFrequency -> rollConvention <> RollConventionEnum.THU
   or CalculationPeriodFrequency -> rollConvention <> RollConventionEnum.FRI
   or CalculationPeriodFrequency -> rollConvention <> RollConventionEnum.SAT
   or CalculationPeriodFrequency -> rollConvention <> RollConventionEnum.SUN

Syntax
""""""

Data rules apply to classes and associated attributes.

Their name needs to be unique across the model, and the naming convention often used is in the form of ``<className>_<attributeName>`` where attributeName refers to the attribute to which the rule applies. If the data rule applies to several attributes, it is appropriate to have a naming in the form of ``<className>_<attributeName1>_<attributeName2>``.

Variations from this naming convention are needed, as in the case of the data rules that implement FpML data validation rules, the ``FpML_rule_#`` convention has been used.

The main data rule syntax is in the form of ``when <Rosetta expression> then <Rosetta expression>``.

Here are a set of relevant examples of this data rule syntax:

* ``CalculationPeriodDates_firstCompoundingPeriodEndDate`` combines three Boolean assertions.

 .. code-block:: Java

  data rule CalculationPeriodDates_firstCompoundingPeriodEndDate
   when InterestRatePayout -> compoundingMethod is absent
    or InterestRatePayout -> compoundingMethod = CompoundingMethodEnum.None
   then InterestRatePayout -> calculationPeriodDates -> firstCompoundingPeriodEndDate is absent

* ``CalculationPeriod_calculationPeriodNumberOfDays`` involves an operator.

 .. code-block:: Java

  data rule CalculationPeriod_calculationPeriodNumberOfDays
   when PaymentCalculationPeriod -> calculationPeriod -> calculationPeriodNumberOfDays exists
   then PaymentCalculationPeriod -> calculationPeriod -> calculationPeriodNumberOfDays >= 0

* ``Obligations_physicalSettlementMatrix`` makes use of parentheses for the purpose of supporting nested assertions.

.. code-block:: Java

 data rule Obligations_physicalSettlementMatrix
  when ( Contract -> documentation -> contractualMatrix -> matrixType <> MatrixTypeEnum.CreditDerivativesPhysicalSettlementMatrix
   or Contract -> documentation -> contractualMatrix -> matrixType is absent )
   and Contract -> contractualProduct -> economicTerms -> payout -> creditDefaultPayout -> protectionTerms -> obligations exists
  then ( Contract -> contractualProduct -> economicTerms -> payout -> creditDefaultPayout -> protectionTerms -> obligations -> notSubordinated
   and Contract -> contractualProduct -> economicTerms -> payout -> creditDefaultPayout -> protectionTerms -> obligations -> notSovereignLender
   and Contract -> contractualProduct -> economicTerms -> payout -> creditDefaultPayout -> protectionTerms -> obligations -> notDomesticLaw
   and Contract -> contractualProduct -> economicTerms -> payout -> creditDefaultPayout -> protectionTerms -> obligations -> notDomesticIssuance
  ) exists
  and (
   Contract -> contractualProduct -> economicTerms -> payout -> creditDefaultPayout -> protectionTerms -> obligations -> fullFaithAndCreditObLiability
   or Contract -> contractualProduct -> economicTerms -> payout -> creditDefaultPayout -> protectionTerms -> obligations -> generalFundObligationLiability
   or Contract -> contractualProduct -> economicTerms -> payout -> creditDefaultPayout -> protectionTerms -> obligations -> revenueObligationLiability
  ) exists

Object Qualification Artefacts
------------------------------

The CDM modelling approach consists in inferring the product and event qualification from their relevant attributes, rather than qualifying those upfront.  As a result, the Rosetta syntax has been adjusted to meet this requirement, with slight variations in the implementation across those two use cases.

The CDM Model section of this documentation details the positioning of those product and event qualification artefacts as part of the CDM and their representation as part of the associated object instantiations.

Product Qualification
^^^^^^^^^^^^^^^^^^^^^

18 interest rate derivative products have so been qualified as part of the CDM, in effect representing the full ISDA V2.0 scope.  Credit derivatives have not yet been qualified because their ISDA taxonomy is based upon the underlying transaction type, instead of the product features as for the interest rate swaps.  Follow-up is in progress with the ISDA Credit Group to evaluate whether an alternative product qualification could be developed that would leverage the approach adopted for interest rate derivatives.

Purpose
"""""""

The product qualification leverages the **alias** syntax presented earlier in this documentation, by qualifying a product from its economic terms, those latter being expressed through a set of assertions associated with modelling components.

Syntax
""""""

The product qualification syntax is as follows: ``isProduct <name> <Rosetta expression>``.

The product name needs to be unique across the product and event qualifications, the classes and the aliases, and validation logic is in place to enforce this. The naming convention is to have one CamelCased word.

The CDM makes use of the ISDA taxonomy V2.0 leaf level to qualify the event.  The synonymity with the ISDA taxonomy V1.0 has been systematically indicated as part of the model upon request from CDM group participants, who pointed out that a number of them use it internally.

.. code-block:: Java

 isProduct InterestRate_InflationSwap_Basis_YearOn_Year
  [synonym ISDA_Taxonomy_v1 value InterestRate_IRSwap_Inflation]
  EconomicTerms -> payout -> interestRatePayout -> interestRate -> floatingRate count = 1
  and EconomicTerms -> payout -> interestRatePayout -> interestRate -> inflationRate count = 1
  and EconomicTerms -> payout -> interestRatePayout -> interestRate -> fixedRate is absent
  and EconomicTerms -> payout -> interestRatePayout -> crossCurrencyTerms -> principalExchanges is absent
  and EconomicTerms -> payout -> optionPayout is absent
  and EconomicTerms -> payout -> interestRatePayout -> paymentDates -> paymentFrequency -> periodMultiplier = 1
  and EconomicTerms -> payout -> interestRatePayout -> paymentDates -> paymentFrequency -> period = PeriodExtendedEnum.Y

Event Qualification
^^^^^^^^^^^^^^^^^^^

23 lifecycle events have currently been qualified as part of the CDM.

Purpose
"""""""

Similar to the product qualification syntax, the purpose of the event qualifier is to qualify a product from the existence of the a set of modelling attributes.

Syntax
""""""

The event qualification syntax is similar to the product and the alias, the difference being that it is possible to associate a set of data rules to it.

The event name needs to be unique across the product and event qualifications, the classes and the aliases, and validation logic is in place to enforce this.  The naming convention is to have one CamelCased word.

The ``Increase`` illustrates quite well how the syntax qualifies this event by requiring that five conditions be met:

* When specified, the value associated with the ``intent`` attribute of the ``Event`` class must be ``Increase``;
* The ``QuantityChange`` primitive must exist, possibly alongside the ``Transfer`` one;
* The quantity/notional in the before state must be lesser than in the after state. This latter argument makes use of the ``quantityBeforeQuantityChange`` and ``quantityAfterQuantityChange`` aliases;
* The ``changedQuantity`` attribute must be absent (note that a later syntax enhancement will aim at confirming that this attribute corresponds to the difference between the before and after quantity/notional);
* The ``closedState`` attribute must be absent.

.. code-block:: Java

 isEvent Increase
  Event -> intent when present = IntentEnum.Increase
  and ( Event -> primitive -> quantityChange only exists
   or ( Event -> primitive -> quantityChange and Event -> primitive -> transfer -> cashTransfer ) exists )
  and quantityBeforeQuantityChange < quantityAfterQuantityChange
  and changedQuantity > 0.0
  and Event -> primitive -> quantityChange -> after -> contract -> closedState is absent

  alias quantityBeforeQuantityChange
   Event -> primitive -> quantityChange -> before -> contract -> contractualProduct -> economicTerms -> payout -> interestRatePayout -> quantity -> quantity -> amount
   and Event -> primitive -> quantityChange -> before -> contract -> contractualProduct -> economicTerms -> payout -> interestRatePayout -> quantity -> notionalAmount -> amount
   and Event -> primitive -> quantityChange -> before -> contract -> contractualProduct -> economicTerms -> payout -> interestRatePayout -> quantity -> notionalSchedule -> notionalStepSchedule -> initialValue
   and Event -> primitive -> quantityChange -> before -> contract -> contractualProduct -> economicTerms -> payout -> interestRatePayout -> quantity -> notionalSchedule -> notionalStepSchedule -> step -> stepValue
   and Event -> primitive -> quantityChange -> before -> contract -> contractualProduct -> economicTerms -> payout -> interestRatePayout -> quantity -> notionalSchedule -> notionalStepParameters -> notionalStepAmount
   and Event -> primitive -> quantityChange -> before -> contract -> contractualProduct -> economicTerms -> payout -> interestRatePayout -> quantity -> fxLinkedNotional -> initialValue
   and Event -> primitive -> quantityChange -> before -> contract -> contractualProduct -> economicTerms -> payout -> creditDefaultPayout -> protectionTerms -> notionalAmount -> amount
   and Event -> primitive -> quantityChange -> before -> contract -> contractualProduct -> economicTerms -> payout -> optionPayout -> quantity -> notionalAmount -> amount

  alias quantityAfterQuantityChange
   Event -> primitive -> quantityChange -> after -> contract -> contractualProduct -> economicTerms -> payout -> interestRatePayout -> quantity -> quantity -> amount
   and Event -> primitive -> quantityChange -> after -> contract -> contractualProduct -> economicTerms -> payout -> interestRatePayout -> quantity -> notionalAmount -> amount
   and Event -> primitive -> quantityChange -> after -> contract -> contractualProduct -> economicTerms -> payout -> interestRatePayout -> quantity -> notionalSchedule -> notionalStepSchedule -> initialValue
   and Event -> primitive -> quantityChange -> after -> contract -> contractualProduct -> economicTerms -> payout -> interestRatePayout -> quantity -> notionalSchedule -> notionalStepSchedule -> step -> stepValue
   and Event -> primitive -> quantityChange -> after -> contract -> contractualProduct -> economicTerms -> payout -> interestRatePayout -> quantity -> notionalSchedule -> notionalStepParameters -> notionalStepAmount
   and Event -> primitive -> quantityChange -> after -> contract -> contractualProduct -> economicTerms -> payout -> interestRatePayout -> quantity -> fxLinkedNotional -> initialValue
   and Event -> primitive -> quantityChange -> after -> contract -> contractualProduct -> economicTerms -> payout -> creditDefaultPayout -> protectionTerms -> notionalAmount -> amount
   and Event -> primitive -> quantityChange -> after -> contract -> contractualProduct -> economicTerms -> payout -> optionPayout -> quantity -> notionalAmount -> amount

Calculation Artefacts
---------------------

Purpose
^^^^^^^

One of the objectives of the CDM Initial Phase has been to express in a machine executable format some of the ISDA Definitions as a way to confirm the extent to which this digital CDM solution can be used.

The ISDA 2006 definitions of the **Fixed Amount** and **Floating Amount** have been used as an initial scope.

To this effect, the Rosetta grammar has been extended as a way to express a syntax that can support such expressions.

Syntax
^^^^^^

The calculation syntax has three components: the **calculation** itself, the **argument** used as an input to that calculation and (possibly) associated **function**.

The application of this syntax to the ``ACT/365.FIXED`` ISDA day count fraction definition provides a good illustration of that syntax:

.. code-block:: Java

 calculation DayCountFractionEnum._30E_360
 {
   number: (360 * (endYear - startYear) + 30 * (endMonth - startMonth) + (endDay - startDay)) / 360
 }

.. code-block:: Java

 arguments DayCountFractionEnum._30E_360
 {
  alias period CalculationPeriod( InterestRatePayout -> calculationPeriodDates )

  endYear : is period -> endDate -> year
  startYear : is period -> startDate -> year
  endMonth : is period -> endDate -> month
  startMonth : is period -> startDate -> month
  startDay : is Min( period -> startDate -> day, 30 )
  endDay : is Min( period -> endDate -> day, 30 )
 }

.. code-block:: Java

 function ResolveRateIndex( index FloatingRateIndexEnum )
 {
  rate number;
 }
