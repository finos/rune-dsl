Rosetta Modelling Artefacts
===========================

**The Rosetta syntax can express five types of model components**:

* Data
* Data Validation (or *condition*)
* Function
* Annotation
* Mapping (or *synonym*)

This documentation details the purpose and features of each type of model component and highlights the relationships that exists among those. As the initial live application of the Rosetta DSL, examples from the ISDA CDM will be used to illustrate each of those artefacts.

Data Component
--------------

**Rosetta provides four data definition components** that are used to model data, grouped into two pairs:

* Type and Attribute
* Enumeration and Enumeration Value

Type and Attribute
^^^^^^^^^^^^^^^^^^^

Purpose
"""""""

A *type* describes an *entity* (also sometimes referred to as an *object* or a *class*) in the model and is defined by a plain-text description and a set of *attributes*. Attributes specify the granular elements composing the entity.

Syntax
""""""

The definition of a *type* starts with the keyword ``type``, followed by the type name. A colon ``:`` punctuation introduces the rest of the definition.

The first component of the definition is a plain-text description of the type. Descriptions in Rosetta use quotation marks ``"`` ``"`` (to mark a string) in between angle brackets ``<`` ``>``. Descriptions, although not generating any executable code, are integral meta-data components of the model. As modelling best practice, a definition ought to exist for every artefact and be clear and comprehensive.

Then the definition of the type lists its component attributes. Each attribute is defined by four components, syntactically ordered as:

* name
* type
* cardinality: see `Cardinality Section`_
* description

.. code-block:: Haskell

 type PeriodBound: <"The period bound is defined as a period and whether the bound is inclusive.">
   period Period (1..1) <"The period to be used as the bound, e.g. 5Y.">
   inclusive boolean (1..1) <"Whether the period bound is inclusive, e.g. for a lower bound, false would indicate greater than, whereas true would indicate greater than or equal to.">

 type Period: <"A class to define recurring periods or time offsets.">
   periodMultiplier int (1..1) <"A time period multiplier, e.g. 1, 2 or 3 etc. A negative value can be used when specifying an offset relative to another date, e.g. -2 days.">
   period PeriodEnum (1..1) <"A time period, e.g. a day, week, month or year of the stream. If the periodMultiplier value is 0 (zero) then period must contain the value D (day).">

.. note:: The Rosetta DSL does not use any delimiter to end definitions. All model definitions start with a similar opening keyword as ``type``, so the start of a new definition marks the end of the previous one. For readability more generally, the Rosetta DSL looks to eliminate all the delimiters that are often used in traditional programming languages (such as curly braces ``{`` ``}`` or semi-colon ``;``).

Each attribute can be specified either as a basic type, a type or an enumeration. The set of basic types available in the Rosetta DSL are controlled at the language level by the ``basicType`` definition:

* Text - ``string``
* Number - ``int`` (for integer) and ``number`` (for float)
* Logic - ``boolean``
* Date and Time - ``date``, ``time`` and ``zonedDateTime``

The Rosetta DSL convention is that type names use the *PascalCase* (starting with a capital letter, also referred to as the *upper* `CamelCase <https://en.wikipedia.org/wiki/Camel_case>`_), while attribute names use the *camelCase* (starting with a lower case letter, also referred to as the *lower* camelCase). Type names need to be unique across the model. All those requirements are controlled by the Rosetta grammar.

The Rosetta DSL provides for some special types called 'qualified types', which are specific to its application in the financial domain:

* Calculation - ``calculation``
* Product and event qualification - ``productType`` and ``eventType``

Those special types are designed to flag attributes which result from running some logic, such that model implementations can identify where to stamp the output in the model.

Time
""""

For time zone adjustments, a time zone qualifier can be specified alongside a time in one of two ways:

* Through the ``zonedDateTime`` basic type, which needs to be expressed either as `UTC <https://en.wikipedia.org/wiki/Coordinated_Universal_Time>`_ or as an offset to UTC, as specified by the ISO 8601 standard.
* Through the ``BusinessCenterTime`` type, where time is specified alongside a business center.  This is used to specify a time dimension in relation to a future event: e.g. the earliest or latest exercise time of an option.

While there has been discussion as to whether the Rosetta DSL should support dates which are specified as an offset to UTC with the ``Z`` suffix, no positive conclusion has been reached. The main reason is that all dates which need a business date context can already specify an associated business center.

Inheritance
"""""""""""

The Rosetta DSL supports an **inheritance** mechanism, when a type inherits its definition and behaviour (and therefore all of its attributes) from another type and adds its own set of attributes on top. Inheritance is supported by the ``extends`` keyword next to the type name.

.. code-block:: Haskell

 type Offset extends Period:
    dayType DayTypeEnum (0..1)

.. note:: For clarity purposes, the documentation snippets omit the synonyms and definitions that are associated with the classes and attributes, unless the purpose of the snippet it to highlight some of those features.


Meta-Type and Reference
"""""""""""""""""""""""

The Rosetta DSL allows to associate a set of qualifiers to an attribute:

* The ``scheme`` meta-type specifies a mechanism to control the set of values that an attribute can take. The relevant scheme reference can be specified as meta-information in the attribute synonyms, so that no originating information is disregarded.
* The ``reference`` meta-type replicates the cross-referencing mechanism used in XML to provide data integrity within the context of an instance document - in particular with ``href`` (for *hyper-text reference*) as used in the FpML standard. The cross-reference value can be specified as meta-information in the attribute synonyms.

To make objects internally referenceabale (beyond external cross-references provided by an instance document), Rosetta allows to associate a unique identifier to instances of a type, by adding a ``key`` qualifier to the type name. The ``key`` corresponds to a hash code to be generated by the model implementation. The implementation provided in the Rosetta DSL is the de-facto Java hash function. It is a *deep hash* that uses the complete set of attribute values that compose the type and its attributes, recursively.

The below ``Party`` and ``Identifier`` classes provide an illustration as to how **meta-types** and **references** are implemented.

.. code-block:: Haskell

 type Party:
   [metadata key]
 partyId string (1..*) 
   [metadata scheme]
 name string (0..1)
   [metadata scheme]
 person NaturalPerson (0..*)
 account Account (0..1)
 
 type Identifier:
   [metadata key]
   issuerReference Party (0..1)
     [metadata reference]
   issuer string (0..1)
     [metadata scheme]
   assignedIdentifier AssignedIdentifier (1..*)

A ``key`` qualifier is associated to the ``Party`` type, which means it is referenceable. In the ``Identifier`` class, the ``reference`` qualifier, which is associated to the ``issuerReference`` attribute of type ``Party``, indicates that this attribute can be provided as a reference (via its associated key) instead of a copy. An example implementation of this cross-referencing mechanism for these types can be found in the `Synonym Section`_ of the documentation.

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

Data Validation Component
-------------------------

**Data integrity is supported by validation components that are associated to each data type** in the Rosetta DSL. There are two types of validation components:

* Cardinality
* Condition Statement

The validation components associated to a data type generate executable code designed to be executed on objects of that type. Implementors of the model can use the code generated from these validation components to build diagnostic tools that can scan objects and report on which validation rules were statisfied or broken. Typically, the validation code is included as part of any process that creates an object, to verify its validity from the point of creation. 

Cardinality
^^^^^^^^^^^

Cardinality is a data integrity mechanism to control how many of each attribute an object of a given type can contain. The Rosetta DSL borrows from XML and specifies cardinality as a lower and upper bound in between ``(`` ``..`` ``)`` braces.

.. code-block:: Haskell

 type Address:
   street string (1..*)
   city string (1..1)
   state string (0..1)
   country string (1..1)
     [metadata scheme]
   postalCode string (1..1)

The lower and upper bounds can both be any integer number. A 0 lower bound means attribute is optional. A ``*`` upper bound means an unbounded attribute. ``(1..1)`` represents that there must be one and only one attribute of this type. When the upper bound is greater than 1, the attribute will be considered as a list, to be handled as such in any generated code.

A separate validation rule is generated for each attribute's cardinality constraint, so that any cardinality breach can be associated back to the specific attribute and not just to the object overall.

Condition Statement
^^^^^^^^^^^^^^^^^^^

Purpose
"""""""

*Conditions* are logic statements associated to a data type. They are predicates on attributes of objects of that type that evaluate to True or False.

Syntax
""""""

Condition statements are included in the definition of the type that they are associated to and are usually appended after the definition of the type's attributes.

The definition of a condition starts with the ``condition`` keyword, followed by the name of the condition and a colon ``:`` punctuation. The condition's name must be unique in the context of the type that it applies to (but needs not be unique across all data types of a given model). The rest of the condition definition comprises:

* a plain-text description (optional)
* a logic expression that applies to the the type's attributes

**The Rosetta DSL offers a restricted set of language features designed to be unambiguous and understandable** by domain experts who are not software engineers, while minimising unintentional behaviour. The Rosetta DSL is not a *Turing-complete* language: it does not support looping constructs that can fail (e.g. the loop never ends), nor does it natively support concurrency or I/O operations. The language features that are available in the Rosetta DSL to express validation conditions emulate the basic boolean logic available in usual programming languages:

* conditional statements: ``if``, ``then``, ``else``
* boolean statements: ``and``, ``or``
* list statements: ``exists``, ``is absent``, ``contains``, ``count``
* comparison operators: ``=``, ``<>``, ``<``, ``<=``, ``>=``, ``>``

.. code-block:: Haskell

 type ActualPrice:
    currency string (0..1)
       [metadata scheme]
    amount number (1..1)
    priceExpression PriceExpressionEnum (1..1)
    
    condition Currency: <"The currency attribute associated with the ActualPrice should not be specified when the price is expressed as percentage of notional.">
       if priceExpression = PriceExpressionEnum -> PercentageOfNotional
       then currency is absent

.. code-block:: Haskell

 type ConstituentWeight:
    openUnits number (0..1)
    basketPercentage number (0..1)
    condition BasketPercentage: <"FpML specifies basketPercentage as a RestrictedPercentage type, meaning that the value needs to be comprised between 0 and 1.">
       if basketPercentage exists
       then basketPercentage >= 0.0 and basketPercentage <= 1.0

.. note:: Conditions are included in the definition of the data type that they are associated to, so they are "aware" of the context of that data type. This is why attributes of that data type can be directly used to express the validation logic, without the need to refer to the type itself.

Special Syntax
^^^^^^^^^^^^^^

Some specific language feature have been introduced in the Rosetta DSL, to handle validation cases where the basic boolean logic components would create unecessarily verbose, and therefore less readable, expressions. Those use-cases were deemed frequent enough to justify developing a specific syntax for them.

Choice
""""""

Choice rules define a choice constraint between the set of attributes of a class. They are meant as a simple and robust construct to translate the XML *xsd:choicesyntax* as part of any model created using Rosetta, although their usage is not limited to those XML use cases.

* ``required choice``
* ``optional choice``

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

One-of
""""""

(as complement to choice rule)

In the case where all the attributes of a given class are subject to a required choice logic that results in one and only one of them being present in any instance of that class, Rosetta allows to associate a ``one of`` qualifier to the class. This by-passes the need to implement the corresponding choice rule.

* ``one-of``

This feature is illustrated in the ``BondOptionStrike`` class.

.. code-block:: Java

 class BondOptionStrike one of
 {
  referenceSwapCurve ReferenceSwapCurve (0..1);
  price OptionStrike (0..1);
 }

Only Exists
"""""""""""

The ``only exists`` component is an adaptation of the simple ``exists`` syntax, that verifies that the attribute exists but also that no other attribute of the type does.

.. code-block:: Haskell

 type PriceNotation:
    price Price (1..1)
    assetIdentifier AssetIdentifier (0..1)
 
    condition CurrencyAssetIdentifier:
       if price -> fixedInterestRate exists
       then assetIdentifier -> currency only exists
 
    condition RateOptionAssetIdentifier:
       if price -> floatingInterestRate exists
       then assetIdentifier -> rateOption only exists

This syntax drastically reduces the condition expression, which would otherwise require a combination of ``exists`` and ``is absent`` (applied to all other attributes). It also makes the logic more robust to future model changes, where newly introduced attributes would need to be tested for ``is absent``.

.. note:: This condition is typically applied to attribues of objects whose type implements a ``one-of`` condition. In this case, the ``only`` qualifier is redundant with the ``one-of`` condition because only one of the attributes can exist. However, ``only`` makes the condition expression more explicit, and also robust to potential lifting of the ``one-of`` condition.

Function Component
------------------

**In programming languages, a function is a fixed set of logical instructions returning an output** which can be parameterised by a set of inputs (also known as *arguments*). A function is *invoked* by specifying a set of values for the inputs and running the instructions accordingly. In the Rosetta DSL, this type of component has been unified under a single *function* construct.

Functions are a fundamental building block to automate processes, because the same set of instructions can be executed as many times as required by varying the inputs to generate a different, yet deterministic, result.

Just like a spreadsheet allows users to define and make use of functions to construct complex logic, the Rosetta DSL allows to model complex processes from reusable function components. Typically, complex processes are defined by combining simpler sub-processes, where one process's ouput can feed as input into another process. Each of those processes and sub-processes are represented by a function. Functions can invoke other functions, so they can represent processes made up of sub-processes, sub-sub-processes, and so on.

Reusing small, modular processes has the following benefits:

* **Consistency**. When a sub-process changes, all processes that use the sub-process benefit from that single change.
* **Flexibility**. A model can represent any process by reusing existing sub-processes. There is no need to define each process explicitly: instead, we pick and choose from a set of pre-existing building blocks.

Function Specification
^^^^^^^^^^^^^^^^^^^^^^

Purpose
"""""""

**Function specification components are used to define the processes applicable to a domain model** in the Rosetta DSL. A function specification defines the function's inputs and/or output through their *types* (or *enumerations*) in the data model. This amounts to specifying the `API <https://en.wikipedia.org/wiki/Application_programming_interface>`_ that implementors should conform to when building the function that supports the corresponding process. Standardising those APIs guarantees the integrity, inter-operability and consistency of the automated processes supported by the model.

As mentionned in the `Condition Statement Section`_, the Rosetta DSL is not a *Turing-complete* language. To build the complete processing logic, model implementors are meant to extend the code generated from the Rosetta DSL, once expressed in a fully featured programming language. For instance in Java, a function specification generates an *interface* that needs to be extended to be executable.

Syntax
""""""

The syntax of a function specification starts with the keyword ``func`` followed by the function name. A colon ``:`` punctuation introduces the rest of the definition.

The Rosetta DSL convention for a function name is to use a PascalCase (upper `CamelCase`_) word. The function name needs to be unique across all types of functions in a model and validation logic is in place to enforce this.

The rest of the function specification supports the following components:

* plain-text decriptions
* inputs and output attributes (the latter is mandatory)
* condition statements on inputs and output
* output construction statements

Descriptions
""""""""""""

The role of a function must be clear for implementors of the model to build applications that provide such functionality. To better communicate the intent and use of functions, Rosetta supports multiple plain-text descriptions in functions. Descriptions can be provided for the function itself, for any input and output and for any statement block.

Look for occurences of text descriptions in the snippets below.

Inputs and Output
"""""""""""""""""

Inputs and output are a function's equivalent of a type's attributes. As in a ``type``, each ``func`` attribute is defined by a name, data type (as either a ``type``, ``enum`` or ``basicType``) and cardinality.

At minimum, a function must specify its output attribute, using the ``output`` keyword also followed by a colon ``:``.

.. code-block:: Haskell

 func GetBusinessDate: <"Provides the business date from the underlying system implementation.">
    output:
      businessDate date (1..1) <"The provided business date.">

Most functions, however, also require inputs, which are also expressed as attributes, using the ``inputs`` keyword. ``inputs`` is plural whereas ``output`` is singular, because a function may only return one type of output but may take several types of inputs.

.. code-block:: Haskell

 func ResolveTimeZoneFromTimeType: <"Function to resolve a TimeType into a TimeZone based on a determination method.">
    inputs:
       timeType TimeTypeEnum (1..1)
       determinationMethod DeterminationMethodEnum (1..1)
    output:
       time TimeZone (1..1)

Conditions
""""""""""

A function's inputs and output can be constrained using *conditions*. Each condition is expressed as a logical statement that evaluates to True or False, using the same language features as those available to express condition statements in data types, as detailed in the `Condition Statement Section`_. 

Condition statements in a function can represent:

* a pre-condition, applicable to inputs only and evaluated prior to executing the function, using the ``condition`` keyword
* a post-condition, applicable to inputs and output and evaluated after executing the function (once the output is known), using the ``post-condition`` keyword

Conditions are an essential feature of the definition of a function. By constraining the inputs and output, they define the "contract" that this function must satisfy, so that it can be safely used for its intended purpose as part of a process.

.. code-block:: Haskell

 func EquityPriceObservation: <"Function specification for the observation of an equity price, based on the attributes of the 'EquityValuation' class.">
    inputs:
       equity Equity (1..1)
       valuationDate AdjustableOrRelativeDate (1..1)
       valuationTime BusinessCenterTime (0..1)
       timeType TimeTypeEnum (0..1)
       determinationMethod DeterminationMethodEnum (1..1)
    output:
       observation ObservationPrimitive (1..1)
    
    condition: <"Optional choice between directly passing a time or a timeType, which has to be resolved into a time based on the determination method.">
       if valuationTime exists then timeType is absent
       else if timeType exists then valuationTime is absent
          else False
    
    post-condition: <"The date and time must be properly resolved as attributes on the output.">
       observation -> date = ResolveAdjustableDate(valuationDate)
       and if valuationTime exists then observation -> time = TimeZoneFromBusinessCenterTime(valuationTime)
          else observation -> time = ResolveTimeZoneFromTimeType(timeType, determinationMethod)
    
    post-condition: <"The number recorded in the observation must match the number fetched from the source.">
       observation -> observation = EquitySpot(equity, observation -> date, observation -> time)

.. note:: The function syntax intentionally mimics the type syntax in the Rosetta DSL regarding the use of descriptions, attributes (inputs and output) and conditions, to provide consistency in the expression of model definitions.

Output Construction
"""""""""""""""""""

In the ``EquityPriceObservation`` example above, the ``post-condition`` statements assert whether the observation's date and value are correctly populated (according to the output of other, sub-functions), but delegates the construction of that output to implementors of the function.

In practice, implementors of the function are expected to re-use those sub-functions (``ResolveAdjustableDate`` and ``EquitySpot``) to construct the output. The drawback is that those sub-functions are likely to be executed twice: once to build the output and once to run the validation.

For efficiency, the function syntax in the Rosetta DSL allows to directly build the output by assigning its values. Function implementors do not have to build those values themselves, because the function already provides them by default, so the corresponding post-conditions are redundant and can be removed.

The example above could be rewritten as follows:

.. code-block:: Haskell

 func EquityPriceObservation:
    inputs:
       equity Equity (1..1)
       valuationDate AdjustableOrRelativeDate (1..1)
       valuationTime BusinessCenterTime (0..1)
       timeType TimeTypeEnum (0..1)
       determinationMethod DeterminationMethodEnum (1..1)
    output:
       observation ObservationPrimitive (1..1)
    
    condition:
       if valuationTime exists then timeType is absent
       else if timeType exists then valuationTime is absent
          else False
    
    assign-output observation -> date:
       ResolveAdjustableDate(valuationDate)
    
    assign-output observation -> time:
       if valuationTime exists then TimeZoneFromBusinessCenterTime(valuationTime)
       else ResolveTimeZoneFromTimeType(timeType, determinationMethod)
    
    assign-output observation -> observation:
       EquitySpot(equity, observation -> date, observation -> time)

Full or Partial Functions
"""""""""""""""""""""""""

The creation of valid output objects can be fully or partially done in a function or completely left to the implementor.

The output object, and thus the function, is fully defined when all validation constraints on the output object have been satisfied. In this case, the generated code is directly usable in an implementation.

When the output object's validation constraints are only partially satisfied, the function is partially implemented. In this case, implementors will need to extend the generated code and assign the remaining values on the output object.

The output object will be systematically validated when invoking a function, so all functions require the output object to be fully valid as part of an implementation.

Aliases
"""""""

The function syntax supports defining 'aliases' that are only available in the context of the function. Aliases work like temporary variable assignments used in programming languages. Aliases in a function context behave in the same way as the root level ``alias`` construct described earlier in this document (the syntax currently differs but will be brought into alignment soon).

In the below example an ``executionPrimitive`` alias is created and is used in both the ``assign-output`` and final ``post-condition`` statements.

.. code-block:: Haskell
 
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

Calculation Function
^^^^^^^^^^^^^^^^^^^^

To mark a function as fully defined, make use of the ``calculation`` annotation per the below to pass enough information to the code generators to create concrete functions.

.. code-block:: Haskell

 func FixedAmount: <"...">
  [calculation]
  inputs:
    interestRatePayout InterestRatePayout (1..1)
    date date (1..1)
  output:
    amount number (1..1)
  ...

Creation Function
^^^^^^^^^^^^^^^^^

*Coming soon...*

Qualification Function
^^^^^^^^^^^^^^^^^^^^^^

Purpose
"""""""

The Rosetta syntax has been developed to meet the requirement of a composable model for financial products and lifecycle events, while being able to qualify those products and events from their relevant modelling components according to a given taxonomy.

Qualification functions associate a taxonomic name (as a string) to an object, by evaluating a combination of assertions that are able to uniquely characterise that object according to the taxonomy.

Syntax
""""""

The qualification name needs to be unique across product and event qualifications, types and aliases, and validation logic is in place to enforce this.

The naming convention is to have one PascalCase (upper CamelCase) word, using ``_`` for space to append more granular qualifications.

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

Utility Function
^^^^^^^^^^^^^^^^

(previously was: *alias*)

Purpose
"""""""

An alias is an indirection for an entire Rosetta expression. Aliases have been introduced in the Rosetta syntax because:

* Model tree expressions can be cumbersome, which may contradict the primary goals of clarity and legibility.
* The same model tree expressions are often reused across multiple modelling artefacts such as data rule, event and product qualification or function.

Syntax
""""""

The alias syntax is straightforward: ``alias <name> <Rosetta expression>``.

The naming convention is to have one camelCased word, instead of a composite name as for the Rosetta rules, with implied meaning.

The below snippet presents an example of such alias and its use as part of an event qualification.

.. code-block:: Haskell

 func PaymentDate:
    inputs: economicTerms EconomicTerms(1..1)
    output: result date(0..1)
    assign-output result: economicTerms -> payout -> interestRatePayout only-element -> paymentDate -> adjustedDate

Annotation Component
--------------------

*Coming soon...*

Mapping Component
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

Meta-Data
"""""""""

When meta-data are associated to the attribute, as decribed in the `Type and Attribute Section`_, the ``meta`` synonym syntax allows to specify how to retrieve the corresponding meta-data from the source. Usage is illustrated in the example below:

.. code-block:: Haskell

 type Identifier:
   [metadata key]
   issuerReference Party (0..1)
     [metadata reference]
     [synonym FpML_5_10 value "issuer" meta "href"]
   issuer string (0..1)
     [metadata scheme]
     [synonym FpML_5_10 value "issuer" meta "issuerIdScheme"]
   assignedIdentifier AssignedIdentifier (1..*)

The ``issuer`` attribute has an associated ``scheme``. The scheme can be retrieved using the ``issuerIdScheme`` meta-data that is attached to the "issuer" value in the synonym source.

To be able to specify an attribute as a reference from an existing source, the source itself must implement some cross-referencing mechanism so that the reference can be located, as in the ``href`` / ``id`` mechanism used in XML. The cross-referencing works as follows:

* the attribute's synonym must specify the identifier value for the reference in the source. For the ``issuerReference`` attribute above, this is specified as the ``href`` meta-data of the ``issuer`` value in the source.
* an identifier value must be associated to the object being referenced. For the ``Party`` type, this is specified as the ``id`` meta-data in the source, as shown below:

.. code-block:: Haskell

 type Party:
   [metadata key]
   [synonym FpML_5_10 value "Party" meta "id"]
 partyId string (1..*) 
   [metadata scheme]
 name string (0..1)
   [metadata scheme]
 person NaturalPerson (0..*)
 account Account (0..1)
 
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

.. note:: This example is not part of the Rosetta DSL but corresponds to the default JSON implementation of the model. The relevance of either maintaining or shredding external references (such as "party2"), once cross-reference has been established, is up to implementors of the model.

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

.. _Cardinality Section: https://docs.rosetta-technology.io/dsl/documentation.html#cardinality
.. _Condition Statement Section: https://docs.rosetta-technology.io/dsl/documentation.html#condition-statement
.. _Type and Attribute Section: https://docs.rosetta-technology.io/dsl/documentation.html#type-and-attribute
.. _Synonym Section: https://docs.rosetta-technology.io/dsl/documentation.html#synonym
