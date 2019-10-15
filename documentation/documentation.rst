Rosetta Modelling Artefacts
===========================

**The Rosetta syntax can express five types of modelling artefacts**:

* Data Representation
* Mapping (or *synonym*)
* Data Integrity
* Object Qualification
* Function

This documentation details the purpose and features of each of those modelling artefacts and highlights the relationships that exists among those. As the initial live application of the Rosetta DSL, examples from the ISDA CDM will be used to illustrate each of those artefacts.

Data Representation Artefacts
-----------------------------

**Rosetta provides five data representation components** to build data artefacts in the model:

* Class
* Attribute
* Enumeration
* Enumeration Value
* Alias

Class and Attribute
^^^^^^^^^^^^^^^^^^^

Purpose
"""""""

A *class* describes an *entity* (or *object*) in the model as a set of attributes and an associated definition. *Attributes* specify the granular elements composing the entity in terms of their type, cardinality and each with an associated definition.

Syntax
""""""

The class content is delineated by curly brackets ``{`` ``}``.

A Rosetta attribute can be specified either as a basic type, a class or an enumeration. The set of **basic types** available in Rosetta are:

* Text - ``string``
* Number - ``int`` ``number``
* Logic - ``boolean``
* Date and Time - ``date`` ``time`` ``zonedDateTime``

In addition, Rosetta provides for some special types called 'qualified types', which are specific to its application in the financial domain:

* Calculation - ``calculation``
* Product and event qualification - ``productType`` ``eventType``

Those special types are designed to flag attributes which result from the execution of some logic, such that model implementations can identify where to stamp the execution output in the model.

The Rosetta convention is that class names use the PascalCase (starting with a capital letter, also referred to as the upper `CamelCase <https://en.wikipedia.org/wiki/Camel_case>`_), while attribute names use the camelCase (starting with a lower case letter, also referred to as the lower camelCase). Class names need to be unique across the model, including with respect to rule names. All those requirements are controlled by the Rosetta grammar.

A plain-text definition of each modelling artefact is added in Rosetta as a string using ``"`` ``"`` in between angle brackets: ``<`` ``>``.

.. code-block:: Java

 class ContractualProduct <"A class to specify the contractual products' economic terms, alongside their product identification and product taxonomy. The contractual product class is meant to be used across the pre-execution, execution and (as part of the Contract) post-execution lifecycle contexts.">
 {
  productIdentification ProductIdentification (0..1) <"The product identification value(s) that might be associated with a contractual product. The CDM provides the ability to associate several product identification methods with a product.">;
  productTaxonomy ProductTaxonomy (0..*) <"The product taxonomy value(s) associated with a contractual product.">;
  economicTerms EconomicTerms (1..1) <"The economic terms associated with a contractual product, i.e. the set of features that are price-forming.">;
 }

Definitions, although not generating any executable code, are integral meta-data components of the model. As modelling best practice, a definition ought to exist for every artefact and be clear and comprehensive.

Cardinality
"""""""""""

Cardinality is a model integrity mechanism to control how many of each attribute can a class contain. The Rosetta syntax borrows from XML and specifies cardinality as a lower and upper bound in between ``(`` ``..`` ``)`` braces, as shown in the ``ContractualProduct`` example above.

The lower and upper bounds can both be any number. A 0 lower bound means attribute is optional. A ``*`` upper bound means an unbounded attribute. ``(1..1)`` represents that there must be one and only one attribute of this type in that class.

Time
""""

For time zone adjustments, a time zone qualifier can be specified alongside a time in one of two ways:

* Through the ``zonedDateTime`` type, which needs to be expressed either as `UTC <https://en.wikipedia.org/wiki/Coordinated_Universal_Time>`_ or as an offset to UTC, as specified by the ISO 8601 standard.
* Through the ``BusinessCenterTime`` class, where time is specified alongside a business center.  This is used to specify a time dimension in relation to a future event, e.g. the earliest or latest exercise time of an option.

While there has been discussion as to whether Rosetta should support dates which are specified as an offset to UTC with the ``Z`` suffix, no positive conclusion has been reached. The main reason is that all dates which need a business date context can already specify an associated business center.

Abstract Class
""""""""""""""

Rosetta supports the concept of **abstract class**, which cannot be instantiated as part of the generated executable code and is meant to be extended by other classes.  An example of such is the ``IdentifiedProduct`` class, which is the baseline for products which terms are abstracted through reference data and can be extended by the respective variations of such products, as illustrated by the ``Loan`` class.

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

Meta-Type and Reference
"""""""""""""""""""""""

Rosetta allows to associate a set of qualifiers to an attribute:

* The ``scheme`` meta-type specifies a mechanism to control the set of values that an attribute can take. The relevant scheme reference can be specified as meta-information in the attribute synonyms, so that no originating information is disregarded.

* The ``reference`` meta-type replicates the cross-referencing mechanism used in XML to provide data integrity within the context of an instance document - in particular with ``href`` (for *hyper-text reference*) as used in the FpML standard. The cross-reference value can be specified as meta-information in the attribute synonyms.

**Note**: Synonyms are a mechanism in Rosetta to map the model components to physical data representations and is detailed in the *Synonym* section of this documentation.

To make objects internally referenceabale (beyond external cross-references provided by an instance document), Rosetta also allows to associate a unique identifier to instances of a class, by  adding a ``key`` qualifier to the class name. The ``key`` corresponds to a hash code to be generated by the model implementation. The implementation provided as part of the Rosetta DSL is the de-facto Java hash function. It is a *deep hash* that uses the complete set of attribute values that compose the class and its children, recursively.

The below ``Party`` and ``Identifier`` classes provide a good illustration as to how **meta-types** and **references** are implemented.

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

The ``key`` qualifier is associated to the ``Party`` class, while the ``reference`` qualifier is associated to the ``issuerReference`` attribute, of type ``Party``, in the ``Identifier`` class. The ``issuerReference`` can be provided as an external cross-reference, for which the value ``issuer`` is specified in the synonym source using ``href`` as the ``meta`` qualifier. The ``issuer`` attribute has an associated ``scheme``, which ``issuerIdScheme`` value is specified in the synonym source using the ``meta`` qualifier.

The below JSON extract illustrates an implementation of these meta-types in the context of a *transaction event*, which identifies the parties to the transactions as well as the *issuer* of the event (i.e. who submits the transaction message).

.. code-block:: Java

 "eventIdentifier": [
    {
      (...)
      "issuerReference": {
        "globalReference": "33f59558",
        "externalReference": "party2"
      },
      "meta": {
        "globalKey": "76cc9eab"
      }
    }
  ],
  (...)
  "party": [
    {
      "meta": {
        "globalKey": "33f59557",
        "externalKey": "party1"
      },
      "partyId": [
        {
          "value": "Party 1",
          "meta": {
            "scheme": "http://www.fpml.org/coding-scheme/external"
          }
        }
      ]
    },
    {
      "meta": {
        "globalKey": "33f59558",
        "externalKey": "party2"
      },
      "partyId": [
        {
          "value": "Party 2",
          "meta": {
            "scheme": "http://www.fpml.org/coding-scheme/external"
          }
        }
      ]
    }
  ],

There are two parties to the event, associated with ``externalKey`` identifiers as "party1" and "party2". Their actual ``partyId`` values are "Party 1" and "Party 2", which are specified through an FpML ``scheme`` referred to in meta-data. Rosetta also associates an internal ``globalKey`` hash to each party, as implementation of the ``key`` meta-data.

Thanks to the ``reference`` qualifier, the ``issuerReference`` attribute can simply reference the event issuer party as "Party 2" rather than duplicating its components. The cross-reference is sourced from the original FpML document using the implemented ``href`` synonym. The internal ``globalReference`` points to the ``globalKey`` hash while the ``externalReference`` points to the "party2" ``externalKey``, as sourced from the original FpML document. Also note that the ``issuerReference`` itself has an associated ``globalKey`` meta-data by default since its ``Identifier`` class has a ``key`` qualifier.

**Note**: This example is not part of the Rosetta DSL but corresponds to the default JSON implementation of the model. The relevance of either maintaining or shredding external references (such as "party2"), once cross-reference has been established, is up to implementors of the model.

``rosettaKeyValue`` is a variation of ``key``, which associated hash function doesn't include any of the meta-type qualifiers associated with the attributes. Some of those qualifiers are automatically generated by algorithm (typically, the anchors and references associated with XML documents) and would result in differences between two instance documents, even if those documents would have the same actual values.

The ``rosettaKeyValue`` feature is meant to support the reconciliation of economic terms, hence associated with the ``EconomicTerms`` class. Further evaluation of ``rosettaKeyValue`` is required to assess whether it is an appropriate implementation of such reconciliation use case.

.. code-block:: Java

 class EconomicTerms rosettaKeyValue
 {
  payout Payout (1..1);
  earlyTerminationProvision EarlyTerminationProvision (0..1);
  cancelableProvision CancelableProvision (0..1);
  extendibleProvision ExtendibleProvision (0..1);
 }

Qualified Types
"""""""""""""""

The ``calculation`` qualified type represents the outcome of a calculation in the model and is specified instead of the type for the attribute. An attribute with the ``calculation`` type is meant to be associated to a function annotated with the calculation keyword, as described in the *Function Artefacts* section. The type is implied by the function output.

An example usage is the conversion from clean price to dirty price for a bond, as part of the ``CleanPrice`` class:

.. code-block:: Java

 class CleanPrice
 {
  cleanPrice number (1..1);
  accruals number (0..1);
  dirtyPrice calculation (0..1);
 }

Similarly, ``productType`` and ``eventType`` represent the outcome of a model logic to infer the type of financial product or event for an instance of the model. Attributes of these types are associated respectively to the ``isProduct`` and ``isEvent`` qualification logic described in the *Object Qualification* section of the documentation.

Further review is required to assess the use cases and appropriateness of the implementation of these qualified types in the Rosetta DSL.

Enumeration and Enumeration Value
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Purpose
"""""""

*Enumeration* is the mechanism through which controlled values can be specified for an attribute. An enumeration is the container for the corresponding set of controlled (or enumeration) values.

A ``scheme`` which values are specified as part of an existing standard like FpML can be represented through an enumeration in Rosetta. A ``scheme`` with no defined values in the model is represented as a basic ``string`` type.

Syntax
""""""

Enumerations are very simple modelling containers. They can have associated synonyms. Similar to a class, an enumeration is delineated by brackets ``{`` ``}``.

.. code-block:: Java

 enum MarketDisruptionEnum
 {
  ModifiedPostponement,
  Omission,
  Postponement
 }

Enumeration values have a restricted syntax to facilitate their integration with executable code: they cannot start with a numerical digit, and the only special character that can be associated with them is the underscore ``_``.

In order to handle the integration of FpML scheme values such as the *dayCountFractionScheme* which has values with special characters like ``ACT/365.FIXED`` or ``30/360``, the Rosetta syntax allows to associate a **displayName** synonym. For those enumeration values, special characters are replaced with ``_`` and the ``displayName`` entry corresponds to the actual value. Examples of such are ``ACT_365_FIXED`` and ``_30_360``, with the associated display names of ``ACT/365.FIXED`` and ``30/360``, respectively.

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

Alias
^^^^^

Purpose
"""""""

An alias is an indirection for an entire Rosetta expression. Aliases have been introduced in the Rosetta syntax because:

* Model tree expressions can be cumbersome, which may contradict the primary goals of clarity and legibility.
* The same model tree expressions are often reused across multiple modelling artefacts such as data rule, event and product qualification or function.

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

Synonym
^^^^^^^

Purpose
"""""""

*Synonym* is the baseline building block to map the model in Rosetta to alternative data representations, whether those are open standards or proprietary. Synonyms can be complemented by relevant mapping logic when the relationship is not a one-to-one or is conditional.

Synonyms are associated at the attribute level for a class, or at the enumeration value level for an enumeration. Mappings are typically implemented by traversing the model tree down, so knowledge of the context of an attribute (i.e. the class in which it is used) determines what it should map to. Knowledge about the upper-level class would be lost if synonyms were implemented at the class level.

There is no limit to the number of synonyms that can be associated with each of those artefacts, and there can even be several synonyms for a given data source (e.g. in the case of a conditional mapping).

Syntax
""""""

The baseline synonym syntax has two components:

* **source**, which possible values are controlled by a special ``synonym source`` type of enumeration
* **value**, which is of type ``identifier``

Example:

.. code-block:: Java

 [synonym FpML_5_10, CME_SubmissionIRS_1_0, DTCC_11_0, DTCC_9_0, CME_ClearedConfirm_1_17 value averagingInOut]

A further set of attributes can be associated with a synonym, to address specific use cases:

* **path** to allow mapping when data is nested in different ways between the respective models. The ``Payout`` class is a good illustration of such cases:

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

* **definition** to provide a more explicit reference to the FIX enumeration values which are specified through a single digit or letter positioned as a prefix to the associated definition.

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

In contrast to other data artefacts, the synonym value associated with enumeration values is of type ``string`` to facilitate integration with executable code. The alternative approach consisting in specifying the value as a compatible identifier alongside with a display name has been disregarded because it has been deemed not appropriate to create a 'code-friendly' value for the respective synonyms.  A ``string`` type removes such need.

Mapping Logic
^^^^^^^^^^^^^

Purpose
"""""""

There are cases where the mapping between existing standards and protocols and their relation to the model is not one-to-one or is conditional. Synonyms have been complemented with a syntax to express mapping logic that provides a balance between flexibility and legibility.

Syntax
""""""

The mapping logic differs from the data rule and choice rule syntax in that its syntax is not expressed as a stand-alone block with a qualifier prefix such as ``rule``. The mapping rule is positioned as an extension to the synonym expression, and each of the mapping expressions is prefixed with the ``set`` qualifier, followed by the name of the Rosetta attribute to which the synonym is being mapped to. Several mapping expressions can be associated with a given synonym.

The mapping syntax is composed of two (optional) expressions:

* **mapping value** prefixed with ``to``, to map a specific value that is distinct from the one originating from the source document
* **conditional expression** prefixed with ``when``, to associate conditional logic to the mapping expression

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
  (...)
 }

Data Integrity Artefacts
-------------------------

**There are two components to enforce data integrity** in the model in Rosetta:

* Data Rule
* Choice Rule

Data Rule
^^^^^^^^^

Purpose
"""""""

Data rules are the primary channel to enforce data validation in Rosetta.

While such validation rules are generally specified for existing data standards like FpML alongside the standard documentation, the logic needs to be evaluated and transcribed into code by the relevant teams. More often than not, it results in such logic not being consistently enforced.

As an example, the ``FpML_ird_57`` data rule implements the **FpML ird validation rule #57**, which states that if the calculation period frequency is expressed in units of month or year, then the roll convention cannot be a week day. With Rosetta, this legible view is provided alongside a programmatic implementation thanks to automatic code generation.

.. code-block:: Java

 class Frequency key
 {
  periodMultiplier int (1..1);
  period PeriodExtendedEnum (1..1);
 }

 class CalculationPeriodFrequency extends Frequency
 {
  rollConvention RollConventionEnum (1..1);
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

Grammar rules for Boolean logic such as ``exists``, ``is absent``, ``contains``, ``count`` as well as ``and``, ``or``, ``when``, ``else`` and ``then`` statements are all usable as part of such data rules, as illustrated in the below relevant examples.
:

* ``CalculationPeriodDates_firstCompoundingPeriodEndDate`` combines three Boolean assertions:

.. code-block:: Java

 data rule CalculationPeriodDates_firstCompoundingPeriodEndDate
  when InterestRatePayout -> compoundingMethod is absent
   or InterestRatePayout -> compoundingMethod = CompoundingMethodEnum.None
   then InterestRatePayout -> calculationPeriodDates -> firstCompoundingPeriodEndDate is absent

* ``CalculationPeriod_calculationPeriodNumberOfDays`` involves an operator:

.. code-block:: Java

 data rule CalculationPeriod_calculationPeriodNumberOfDays
  when PaymentCalculationPeriod -> calculationPeriod -> calculationPeriodNumberOfDays exists
  then PaymentCalculationPeriod -> calculationPeriod -> calculationPeriodNumberOfDays >= 0

* ``Obligations_physicalSettlementMatrix`` uses parentheses for the purpose of supporting nested assertions:

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

**Note**: Usage of ``when`` instead of ``if`` statement in ``data rule`` artefacts is not consistent with other logical modelling artefacts in Rosetta, but will be normalised as part of future work on the DSL.

Choice Rule
^^^^^^^^^^^

Purpose
"""""""

Choice rules define a choice constraint between the set of attributes of a class. They are meant as a simple and robust construct to translate the XML *xsd:choicesyntax* as part of any model created using Rosetta, although their usage is not limited to those XML use cases.

Syntax
""""""

Choice rules only apply within the context of a class, and the naming convention is ``<className>_choice``, e.g. ``ExerciseOutcome_choice``. If multiple choice rules exist in relation to a class, the naming convention is to suffix the 'choice' term with a number, e.g. ``ExerciseOutcome_choice1`` and ``ExerciseOutcome_choice2``.

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

One of Syntax as Complement to Choice Rule
""""""""""""""""""""""""""""""""""""""""""""

In the case where all the attributes of a given class are subject to a required choice logic that results in one and only one of them being present in any instance of that class, Rosetta allows to associate a ``one of`` qualifier to the class. This by-passes the need to implement the corresponding choice rule.

This feature is illustrated in the ``BondOptionStrike`` class.

.. code-block:: Java

 class BondOptionStrike one of
 {
  referenceSwapCurve ReferenceSwapCurve (0..1);
  price OptionStrike (0..1);
 }

Object Qualification Artefacts
------------------------------

The Rosetta syntax has been developed to meet the requirement of a composable model for financial products and lifecycle events, while qualifying those products and events from their relevant modelling components. There are slight variations in the implementation across those two use cases.

Product Qualification
^^^^^^^^^^^^^^^^^^^^^

Purpose
"""""""

A product is qualified based on the modelling components of its economic terms, which are being tested through a set of assertions. The qualification leverages the ``alias`` syntax presented earlier in this documentation.

Syntax
""""""

The product qualification syntax works as follows: ``isProduct <name> <Rosetta expression>``.

The product name needs to be unique across the product and event qualifications, the classes and the aliases, and validation logic is in place to enforce this. The naming convention is to have one upper CamelCased word, that uses ``_`` for space to append more granular qualifications.

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

Purpose
"""""""

Similar to the product qualification syntax, an event is qualified based on its underlying components which are being tested through a set of assertions.

Syntax
""""""

The event qualification syntax is similar to the product and the alias but it is also possible to associate a set of data rules to it.

The event name needs to be unique across the product and event qualifications, the classes and the aliases, and validation logic is in place to enforce this.  The naming convention is to have one upper CamelCased word.

The ``Increase`` illustrates how the syntax qualifies this event by requiring that five conditions be met:

* When specified, the value associated with the ``intent`` attribute of the ``Event`` class must be ``Increase``
* The ``QuantityChange`` primitive must exist, possibly alongside the ``Transfer`` one
* The quantity/notional in the before state must be lesser than in the after state. This latter argument makes use of the ``quantityBeforeQuantityChange`` and ``quantityAfterQuantityChange`` aliases
* The ``changedQuantity`` attribute must be absent (note that a later syntax enhancement will aim at confirming that this attribute corresponds to the difference between the before and after quantity/notional)
* The ``closedState`` attribute must be absent

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

Function Artefacts
------------------

Functions
^^^^^^^^^

Purpose
"""""""

A function is a block of code which runs when it is called (or invoked). Data can be passed into the function and the function performs a set of actions (usually) based on that data and can be combined as building blocks into processes.

The primary goal of functions in Rosetta is to model computation and processes in the financial domain, in a way that is unambiguous and understandable by domain experts and technical experts alike. There is great potential in the financial industry to standardise computation and processes to reduce friction between technology solutions and increase interoperability. 

Rosetta's function syntax offers a restricted set of language and API features to minimise unintential behaviour of the code logic. For example, Rosetta is not turing complete and does not support looping constructs that have the potential to fail i.e. the loop never ends; nor does it natively support concurrency or I/O operations. If such features are needed, the user should make use of Rosetta's generated code that is expressed in a fully featured programming language, like Java.

Syntax
""""""

The function syntax is 

The inputs and the output
""""""""""""""""""""""""""""""""""

A function, at minimum specifies a name and an output attribute. An attribute is defined by a name, data type and cardinality, in exactly the same way as attributes on a `class`.

The Rosetta convention for a function name is to use one upper CamelCase word.

.. code-block:: Java
 func GetBusinessDate:
    output:
      businessDate date (1..1)

Most functions, however, require inputs, which are also expressed as attributes. The below describes a function called Execute, which defines four inputs and the output. 

.. code-block:: Java
 func Execute: <"Specifies the execution event should be created from at least 4 inputs: the product, the quantity and two parties.">
    inputs:
      product Product (1..1) <"The product underlying the financial transaction.">
      quantity ExecutionQuantity (1..1) <"The amount of product being transacted.">
      partyA Party (1..1) <"Party to the transaction.">
      partyB Party (1..1) <"Party to the transaction.">
    output:
      execution Event (1..1) <"The execution transaction represented as an Event model object.">

Definitions
"""""""""""

To better communicate the intention of functions and attributes, Rosetta supports definitions on the function name and attribute level. Definitions are supported after the function name, at the end of an attribute and on statement blocks, look out for examples in the code snippets below.

.. code-block:: Java
 func GetBusinessDate: <"Provides the business date from the underlying system implementation.">
    output:
      businessDate date (1..1) <"The provided buisness date.">

Constraints
"""""""""""

Function inputs and the output can be constrained for validation purposes. The `condition` keyword is used when constraining the inputs only and the `post-condition` keyword is used when constraining the output. The `condition` itself is expressed as a logical statement that evaluates to true or false, otherwise known as a boolean expression.

.. code-block:: Java
 func Execute: <"Specifies the execution event should be created from at least 4 inputs: the product, the quantity and two parties.">
    inputs:
      product Product (1..1) <"The product underlying the financial transaction.">
      quantity ExecutionQuantity (1..1) <"The amount of product being transacted.">
      partyA Party (1..1) <"Party to the transaction.">
      partyB Party (1..1) <"Party to the transaction.">
    output:
      executionEvent Event (1..1) <"The execution transaction represented as an Event model object.">
    condition: <"Parties are not the same.">
      partyA <> partyB
    post-condition: <"The execution event is the first is any post trade processes and so should not have any lineage information.">
      executionEvent -> lineage is absent
    post-condition: <"The input product was used to create the execution.">
      executionEvent -> primitive -> execution = NewExecutionPrimitive( product, quantity, partyA, partyB )

The ``condition`` and ``post-condition`` perform a validation step in the same way as ``data rule`` for a `class`, extending this key Rosetta modelling component to functions and not just data. As such, the grammatical rules for logical statements  used for ``data rule`` re-used here.

Constructing the Output
"""""""""""""""""""""""

The final `post-condition` statement in the above invokes another function called `NewExecutionPrimitive`. The `post-condition` also asserts that the value returned from `NewExecutionPrimitive` is equal to the value that was stamped onto the path: `executionEvent -> primitive -> execution` by the implementor.

This means implementors must evaluate the `NewExecutionPrimitive` function and assign its output to the correct model element when implemeting this function. Subsequently the post-condition logic will be evaluated, invoking the same `NewExecutionPrimitive` function a second time. 

For efficiency, the function syntax provides support to directly assign values to the output attribute, which avoids the need to evaluate the `NewExecutionPrimitive` function twice, see example below.

.. code-block:: Java
 func Execute: <"Specifies the execution event should be created from at least 4 inputs: the product, the quantity and two parties.">
    inputs:
      product Product (1..1) <"The product underlying the financial transaction.">
      quantity ExecutionQuantity (1..1) <"The amount of product being transacted.">
      partyA Party (1..1) <"Party to the transaction.">
      partyB Party (1..1) <"Party to the transaction.">
    output:
      executionEvent Event (1..1) <"The execution transaction represented as an Event model object.">
    condition: <"Parties are not the same.">
      partyA <> partyB
    post-condition: <"The execution event is the first is any post trade processes and so should not have any lineage information.">
      executionEvent -> lineage is absent
    assign-output executionEvent -> primitive -> execution: <"The input product was used to create the execution.">
       NewExecutionPrimitive( product, quantity, partyA, partyB )

This example demonstrates, in the context of lifecycle events, why a data representation of those events, although necessary, is not sufficient to direct the implementation of the associated processes - hence the need to define functions. The role of a function must be clear for implementors of the model to build applications that provide such functionality, so **precise descriptions** in either the function definition, input, output, pre- or post-conditions are crucial.

Full and Partial Functions
""""""""""""""""""""""""""

Functions can fully or partially define the output object. The output object (and thus the function) is thought to be fully defined if all validation constraints on the output obejct can be satisfied. 

The job of defining how to create valid output objects can be fully done in a function, partially done in a function or completely left to the implementor.

All functions require the output object to be fully valid when invoked as part of an implementation, otherwise an exception will be thrown at runtime.

Fully Defined Functions - Calculations
""""""""""""""""""""""""""""""""""""""

The output object and thus the function is fully defined when all validation constraints on the object have been satisfied. In this case, the generated code (in Java or equivelant), should be directly usable in implementations.

To mark a function as fully defined, make use of the `calculation` annotation per the below to pass enough information to the code generators to create concrete functions.

.. code-block:: Java
 func FixedAmount: <"...">
  [calculation]
  inputs:
    interestRatePayout InterestRatePayout (1..1)
		date date (1..1)
  output:
    amount number (1..1)
  ...

Partially Defined Functions
"""""""""""""""""""""""""""

When the output object's validation constraints are only partially satisfied, the function is partially implemented. In this case, implementors will need to extend the generated code and provide the remaining parts of the output object.

The output object will still need to be valid, so the job of assigning the remaining values on the output object falls to the implementor.

Aliases
"""""""

The function syntax supports defining alias' that are only available in the context of the function. It behaves in the same way at the root level alias described earlier in this document although the syntax differs currently, but will be brought into alignment in the comming weeks.

In the below example an alias `executionPrimitive` is created and is made use of in both the `assign-output` and final `post-condition` statements.

.. code-block:: Java
 func Execute: <"Specifies the execution event should be created from at least 4 inputs: the product, the quantity and two parties.">
    inputs:
      product Product (1..1) <"The product underlying the financial transaction.">
      quantity ExecutionQuantity (1..1) <"The amount of product being transacted.">
      partyA Party (1..1) <"Party to the transaction.">
      partyB Party (1..1) <"Party to the transaction.">
    output:
      executionEvent Event (1..1) <"The execution transaction represented as an Event model object.">
    alias executionPrimitive: <"The primitive event that holds details of the execution.">
      executionEvent -> primitive -> execution
    condition: <"Parties are not the same.">
      partyA <> partyB
    assign-output executionPrimitive: <"The input product was used to create the execution.">
       NewExecutionPrimitive( product, quantity, partyA, partyB )
    post-condition: <"The execution event is the first is any post trade processes and so should not have any lineage information.">
      executionEvent -> lineage is absent
    post-condition:
      executionPrimitive -> after -> execution -> executionQuantity = quantity