Rosetta Modelling Components
============================

**The Rosetta syntax can express five types of model components**:

* Data
* Annotation
* Data Validation (or *condition*)
* Function
* Mapping (or *synonym*)

This documentation details the purpose and features of each type of model component and highlights the relationships that exist among those. As the initial live application of the Rosetta DSL, examples from the ISDA CDM will be used to illustrate each of those features.

Data Component
--------------

**The Rosetta DSL provides four data definition components** that are used to model data, grouped into two pairs:

* Type and Attribute
* Enumeration and Enumeration Value

Type and Attribute
^^^^^^^^^^^^^^^^^^^

Purpose
"""""""

A *type* describes an *entity* (also sometimes referred to as an *object* or a *class*) in the model and is defined by a plain-text description and a set of *attributes*. Attributes specify the granular elements composing the entity.

Syntax
""""""

The definition of a type starts with the keyword ``type``, followed by the type name. A colon ``:`` punctuation introduces the rest of the definition.

The first component of the definition is a plain-text description of the type. Descriptions use quotation marks ``"`` ``"`` (to mark a string) in between angle brackets ``<`` ``>``. Descriptions, although not generating any executable code, are integral meta-data components of the model. As modelling best practice, a definition ought to exist for every artefact and be clear and comprehensive.

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

The Rosetta DSL convention is that type names use the *PascalCase* (starting with a capital letter, also referred to as the *upper* `CamelCase`_), while attribute names use the *camelCase* (starting with a lower case letter, also referred to as the *lower* camelCase). Type names need to be unique across the model. All those requirements are controlled by the Rosetta DSL grammar.

Time
""""

For time zone adjustments, a time zone qualifier can be specified alongside a time in one of two ways:

* Through the ``zonedDateTime`` basic type, which needs to be expressed either as `UTC`_ or as an offset to UTC, as specified by the ISO 8601 standard.
* Through the ``BusinessCenterTime`` type, where time is specified alongside a business center.  This is used to specify a time dimension in relation to a future event: e.g. the earliest or latest exercise time of an option.

While there has been discussion as to whether the Rosetta DSL should support dates which are specified as an offset to UTC with the ``Z`` suffix, no positive conclusion has been reached. The main reason is that all dates which need a business date context can already specify an associated business center.

Inheritance
"""""""""""

**The Rosetta DSL supports an inheritance mechanism**, when a type inherits its definition and behaviour (and therefore all of its attributes) from another type and adds its own set of attributes on top. Inheritance is supported by the ``extends`` keyword next to the type name.

.. code-block:: Haskell

 type Offset extends Period:
    dayType DayTypeEnum (0..1)

.. note:: For clarity purposes, the documentation snippets omit the synonyms and definitions that are associated with the classes and attributes, unless the purpose of the snippet is to highlight some of those features.


Enumeration and Enumeration Value
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Purpose
"""""""

**Enumeration is the mechanism through which an attribute may only take some specific controlled values**. An *enumeration* is the container for the corresponding set of controlled (or enumeration) values.

This mimics the *scheme* concept, whose values may be specified as part of an existing standard and can be represented through an enumeration in the Rosetta DSL. Typically, a scheme with no defined values is represented as a basic ``string`` type.

Syntax
""""""

Enumerations are very simple modelling containers, which are defined in the same way as other model components. The definition of an enumeration starts with the ``enum`` keyword, followed by the enumeration name. A colon ``:`` punctuation introduces the rest of the definition, which contains a plain-text description of the enumeration and the list of enumeration values.

.. code-block:: Haskell

 enum PeriodEnum: <"The enumerated values to specify the period, e.g. day, week.">
   D <"Day">
   W <"Week">
   M <"Month">
   Y <"Year">

Enumeration names must be unique across a model. The Rosetta DSL naming convention is the same as for types and must use the upper CamelCase (PascalCase).  In addition the enumeration name should end with the suffix Enum.

Enumeration values have a restricted syntax to facilitate their integration with executable code: they cannot start with a numerical digit, and the only special character that can be associated with them is the underscore ``_``.

In order to handle the integration of scheme values which can have special characters, the Rosetta DSL allows to associate a **display name** to any enumeration value. For those enumeration values, special characters are replaced with ``_`` while the ``displayName`` entry corresponds to the actual value.

An example is the day count fraction scheme for interest rate calculation, which includes values such as ``ACT/365.FIXED`` and ``30/360``. These are associated as ``displayName`` to the ``ACT_365_FIXED`` and ``_30_360`` enumeration values, respectively.

.. code-block:: Haskell

 enum DayCountFractionEnum:
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


Annotation Component
--------------------

Annotation Definition
^^^^^^^^^^^^^^^^^^^^^

Purpose
"""""""

Annotations allow to associate meta-information to model components, which can serve a number of purposes:

* purely syntactic, to provide additional guidance when navigating model components
* to add constraints to a model that may be enforced by syntax validation
* to modify the actual behaviour of a model in generated code

Examples of annotations and their usage for different purposes are illustrated below.

Syntax
""""""

Annotations are defined in the same way as other model components. The definition of an annotation starts with the ``annotation`` keyword, followed by the annotation name. A colon ``:`` punctuation introduces the rest of the definition, starting with a plain-text description of the annotation.

Annotation names must be unique across a model. The Rosetta DSL naming convention is to use a (lower) camelCase.

It is possible to associate attributes to an annotation (see ``metadata`` example), even though some annotations may not require any further attribute. For instance:

.. code-block:: Haskell

 annotation rootType: <"Mark a type as a root of the rosetta model">
 
 annotation deprecated: <"Marks a type, function or enum as deprecated and will be removed/replaced.">

Meta-Data and Reference
^^^^^^^^^^^^^^^^^^^^^^^

Purpose
"""""""

The ``metadata`` annotation allows to associate a set of meta-data qualifiers to types and attributes.

.. code-block:: Haskell

 annotation metadata:
   id string (0..1)
   key string (0..1)
   scheme string (0..1)
   reference string (0..1)

Each attribute of the ``metadata`` annotation corresponds to a qualifier:

* The ``key`` meta-data qualifier indicates a type that is referenceable, so that a unique identifier can be associated to objects of that type. This allows to replicates the cross-referencing mechanism used in XML to provide data integrity within the context of an instance document. The ``key`` replicates the ``id`` meta-data as used in the FpML standard, which associates a cross-reference value to the object's data source.
* The ``id`` meta-data qualifier provides the same functionality as ``key`` but for basic types.
* The ``reference`` meta-data qualifier indicates that the attribute may be specified as a reference, using the ``key`` of a referenceable object as meta-data. This replicates the ``href`` (for *hyper-text reference*) meta-data as used in the FpML standard, where the cross-reference value may be specified as meta-information in the attribute's data source.
* The ``scheme`` meta-data qualifier specifies a mechanism to control the set of values that an attribute can take. The relevant scheme reference may be specified as meta-information in the attribute's data source, so that no originating information is disregarded.

The ``key`` corresponds to a hash code to be generated by the model implementation. The implementation provided in the Rosetta DSL is the de-facto Java hash function. It is a *deep hash* that uses the complete set of attribute values that compose the type and its attributes, recursively.

.. note:: Some annotations, such as this metadata qualification, may be provided as standard as part of the Rosetta DSL itself. Additional annotations can always be defined for any model.

Syntax
""""""

Once an annotation is defined, its name and chosen attribute, if any, are used in between square brackets ``[`` ``]`` to annotate model components. The below ``Party`` and ``Identifier`` types illustrate how meta-data annotations and their relevant attributes can be used in a model:

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

A ``key`` qualifier is associated to the ``Party`` type, which means it is referenceable. In the ``Identifier`` type, the ``reference`` qualifier, which is associated to the ``issuerReference`` attribute of type ``Party``, indicates that this attribute can be provided as a reference (via its associated key) instead of a copy. An example implementation of this cross-referencing mechanism for these types can be found in the `Synonym Section`_ of the documentation.

Partial Key
"""""""""""

Meta-data keys that are generated by a hashing algorithm from an object's attribute values often find a practical use by implementors for reconciling and matching data, where equality between hash values is considered a proxy for a data match.

In some cases, it is necessary to remove some of an object's attribute values from the hashing algorithm, when those values are not required in the reconciliation but risk adding noise in the hash that could generate false negatives. This is typically the case for meta-data qualifiers (such as meta-data keys), which may themselves be automatically generated by an algorithm. These may result in differences between two objects, even if those objects would have the same actual values.

An implementation of such partial key used to be provided as a feature of the Rosetta DSL (with a ``partialKey`` annotation).  It has now been de-commissioned, until further evaluation of its usage emerges that may lead to a redesign of this feature.


Qualified Type
^^^^^^^^^^^^^^

The Rosetta DSL provides for some special types called *qualified types*, which are specific to its application in the financial domain:

* Calculation - ``calculation``
* Object qualification - ``productType`` ``eventType``

Those special types are designed to flag attributes which result from running some logic, such that model implementations can identify where to stamp the output in the model. The logic is being captured by specific types of functions that are detailed in the `Function Definition Section`_.

Calculation
"""""""""""

The ``calculation`` qualified type, when specified instead of the type for the attribute, represents the outcome of a calculation. An example usage is the conversion from clean price to dirty price for a bond.

.. code-block:: Haskell

 type CleanPrice:
   cleanPrice number (1..1)
   accruals number (0..1)
   dirtyPrice calculation (0..1)

An attribute with the ``calculation`` type is meant to be associated to a function tagged with the ``calculation`` annotation. The attribute's type is implied by the function output.

.. code-block:: Haskell

 annotation calculation: <"Marks a function as fully implemented calculation.">

Object Qualification
""""""""""""""""""""

Similarly, ``productType`` and ``eventType`` represent the outcome of qualification logic to infer the type of an object (financial product or event) in the model. See the ``productQualifier`` attribute, alongside other identifier attributes in the ``ProductIdentification`` type:

.. code-block:: Haskell

 type ProductIdentification: <" A class to combine the CDM product qualifier with other product qualifiers, such as the FpML ones. While the CDM product qualifier is derived by the CDM from the product payout features, the other product identification elements are assigned by some external sources and correspond to values specified by other data representation protocols.">
   productQualifier productType (0..1) <"The CDM product qualifier, which corresponds to the outcome of the isProduct qualification logic. This value is derived by the CDM from the product payout features.">
   primaryAssetdata AssetClassEnum (0..1)
   secondaryAssetdata AssetClassEnum (0..*)
   productType string (0..*)
   productId string (0..*) 
	 
Attributes of these types are meant to be associated to an object qualification function tagged with the ``qualification`` annotation. The annotation has an attribute to specify which type of object (like ``Product`` or ``BusinessEvent``) is being qualified.

.. code-block:: Haskell

 annotation qualification: <"Annotation that describes a func that is used for event and product Qualification">
   [prefix Qualify]
   Product boolean (0..1)
   BusinessEvent boolean (0..1)

.. note:: The qualified type feature in the Rosetta DSL is under evaluation and may be replaced by a mechanism that is purely based on these function annotations in the future.


Data Validation Component
-------------------------

**Data integrity is supported by validation components that are associated to each data type** in the Rosetta DSL. There are two types of validation components:

* Cardinality
* Condition Statement

The validation components associated to a data type generate executable code designed to be executed on objects of that type. Implementors of the model can use the code generated from these validation components to build diagnostic tools that can scan objects and report on which validation rules were satisfied or broken. Typically, the validation code is included as part of any process that creates an object, to verify its validity from the point of creation. 

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

The definition of a condition starts with the ``condition`` keyword, followed by the name of the condition and a colon ``:`` punctuation. The condition's name must be unique in the context of the type that it applies to (but does not need to be unique across all data types of a given model). The rest of the condition definition comprises:

* a plain-text description (optional)
* a logic expression that applies to the the type's attributes

**The Rosetta DSL offers a restricted set of language features designed to be unambiguous and understandable** by domain experts who are not software engineers, while minimising unintentional behaviour. The Rosetta DSL is not a *Turing-complete* language: it does not support looping constructs that can fail (e.g. the loop never ends), nor does it natively support concurrency or I/O operations. The language features that are available in the Rosetta DSL to express validation conditions emulate the basic boolean logic available in usual programming languages:

* conditional statements: ``if``, ``then``, ``else``
* boolean operators: ``and``, ``or``
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

Some specific language features have been introduced in the Rosetta DSL, to handle validation cases where the basic boolean logic components would create unecessarily verbose, and therefore less readable, expressions. Those use-cases were deemed frequent enough to justify developing a specific syntax for them.

Choice
""""""

Choice rules define a choice constraint between the set of attributes of a type in the Rosetta DSL. They allow a simple and robust construct to translate the XML *xsd:choicesyntax*, although their usage is not limited to those XML use cases.

The choice constraint can be either:

* **optional**, represented by the ``optional choice`` syntax, when at most one of the attributes needs to be present, or
* **required**, represented by the ``required choice`` syntax, when exactly one of the attributes needs to be present

.. code-block:: Haskell

 type NaturalPerson: <"A class to represent the attributes that are specific to a natural person.">
   [metadata key]
 
   honorific string (0..1) <"An honorific title, such as Mr., Ms., Dr. etc.">
   firstName string (1..1) <"The natural person's first name. It is optional in FpML.">
   middleName string (0..*)
   initial string (0..*)
   surname string (1..1) <"The natural person's surname.">
   suffix string (0..1) <"Name suffix, such as Jr., III, etc.">
   dateOfBirth date (0..1) <"The natural person's date of birth.">
   
   condition Choice: <"Choice rule to represent an FpML choice construct.">
     optional choice middleName, initial

.. code-block:: Haskell

 type AdjustableOrRelativeDate:
   [metadata key]
   
   adjustableDate AdjustableDate (0..1)
   relativeDate AdjustedRelativeDateOffset (0..1)
   
   condition Choice:
     required choice adjustableDate, relativeDate

While most of the choice rules have two attributes, there is no limit to the number of attributes associated with it, within the limit of the number of attributes associated with the type.

.. note:: Members of a choice rule need to have their lower cardinality set to 0, something which is enforced by a validation rule.

One-of (as complement to choice rule)
"""""""""""""""""""""""""""""""""""""

In the case where all the attributes of a given type are subject to a required choice logic that results in one and only one of them being present in any instance of that type, the Rosetta DSL allows to associate a ``one-of`` condition to the type, as short-hand to by-pass the implementation of the corresponding choice rule.

This feature is illustrated below:

.. code-block:: Java

 type PeriodRange:
   lowerBound PeriodBound (0..1)
   upperBound PeriodBound (0..1)
   condition: one-of

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

This syntax drastically reduces the condition expression, which would otherwise require to combine one ``exists`` with multiple ``is absent`` (applied to all other attributes). It also makes the logic more robust to future model changes, where newly introduced attributes would need to be tested for ``is absent``.

.. note:: This condition is typically applied to attribues of objects whose type implements a ``one-of`` condition. In this case, the ``only`` qualifier is redundant with the ``one-of`` condition because only one of the attributes can exist. However, ``only`` makes the condition expression more explicit, and also robust to potential lifting of the ``one-of`` condition.

Function Component
------------------

**In programming languages, a function is a fixed set of logical instructions returning an output** which can be parameterised by a set of inputs (also known as *arguments*). A function is *invoked* by specifying a set of values for the inputs and running the instructions accordingly. In the Rosetta DSL, this type of component has been unified under a single *function* construct.

Functions are a fundamental building block to automate processes, because the same set of instructions can be executed as many times as required by varying the inputs to generate a different, yet deterministic, result.

Just like a spreadsheet allows users to define and make use of functions to construct complex logic, the Rosetta DSL allows to model complex processes from reusable function components. Typically, complex processes are defined by combining simpler sub-processes, where one process's output can feed as input into another process. Each of those processes and sub-processes are represented by a function. Functions can invoke other functions, so they can represent processes made up of sub-processes, sub-sub-processes, and so on.

Reusing small, modular processes has the following benefits:

* **Consistency**. When a sub-process changes, all processes that use the sub-process benefit from that single change.
* **Flexibility**. A model can represent any process by reusing existing sub-processes. There is no need to define each process explicitly: instead, we pick and choose from a set of pre-existing building blocks.

Where widely adopted industry processes already exist, they should be reused and not redefined. Some examples include:

* Mathematical functions. Functions such as sum, absolute, and average are widely understood, so do not need to be redefined in the model.
* Reference data. The process of looking-up through reference data is usually provided by existing industry utilities and a model should look to re-use it but not re-implement it.
* Quantitative finance. Many quantitative finance solutions, some open-source, already defines granular processes such as:

  * computing a coupon schedule from a set of parameters
  * adjusting dates given a holiday calendar

This concept of combining and reusing small components is also consistent with a modular component approach to modelling.


Function Specification
^^^^^^^^^^^^^^^^^^^^^^

Purpose
"""""""

**Function specification components are used to define the processes applicable to a domain model** in the Rosetta DSL. A function specification defines the function's inputs and/or output through their *types* (or *enumerations*) in the data model. This amounts to specifying the `API <https://en.wikipedia.org/wiki/Application_programming_interface>`_ that implementors should conform to when building the function that supports the corresponding process.

Standardising those APIs guarantees the integrity, inter-operability and consistency of the automated processes supported by the domain model.

Syntax
""""""

Functions are defined in the same way as other model components. The syntax of a function specification starts with the keyword ``func`` followed by the function name. A colon ``:`` punctuation introduces the rest of the definition.

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

A function's inputs and output can be constrained using *conditions*. Each condition is expressed as a logical statement that evaluates to True or False, using the same language features as those available to express condition statements in data types and detailed in the `Condition Statement Section`_. 

Condition statements in a function can represent either:

* a **pre-condition**, using the ``condition`` keyword, applicable to inputs only and evaluated prior to executing the function, or
* a **post-condition**, using the ``post-condition`` keyword, applicable to inputs and output and evaluated after executing the function (once the output is known)

Conditions are an essential feature of the definition of a function. By constraining the inputs and output, they define the constraints that impementors of this function must satisfy, so that it can be safely used for its intended purpose as part of a process.

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

Function Definition
^^^^^^^^^^^^^^^^^^^

**The Rosetta DSL allows to further define the business logic of a function**, by building the function output instead of just specifying the function's API. The creation of valid output objects can be fully or partially defined as part of a function specification, or completely left to the implementor.

* A function is **fully defined** when all validation constraints on the output object have been satisfied as part of the function specification. In this case, the generated code is directly usable in an implementation.
* A function is **partially defined** when the output object's validation constraints are only partially satisfied. In this case, implementors will need to extend the generated code and assign the remaining values on the output object.

A function must be applied to a specific use case in order to determine whether it is fully *defined* or *partially defined*.  There are a number of fully defined function cases explained in further detail below.

The Rosetta DSL only provides a limited set of language features. To build the complete processing logic for a *partially defined* function, model implementors are meant to extend the code generated from the Rosetta DSL once it is expressed in a fully featured programming language. For instance in Java, a function specification generates an *interface* that needs to be extended to be executable.

The output object will be systematically validated when invoking a function, so all functions require the output object to be fully valid as part of any model implementation.

Output Construction
"""""""""""""""""""

In the ``EquityPriceObservation`` example above, the ``post-condition`` statements assert whether the observation's date and value are correctly populated according to the output of other, sub-functions, but delegates the construction of that output to implementors of the function.

In practice, implementors of the function can be expected to re-use those sub-functions (``ResolveAdjustableDate`` and ``EquitySpot``) to construct the output. The drawback is that those sub-functions are likely to be executed twice: once to build the output and once to run the validation.

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

**The Rosetta DSL also supports a number of fully defined function cases**, where the output is being built up to a valid state:

* Object qualification
* Calculation
* Short-hand function

Those functions are typically associated to an annotation, as described in the `Qualified Type Section`_, to instruct code generators to create concrete functions.

Object Qualification Function
"""""""""""""""""""""""""""""

**The Rosetta DSL supports the qualification of financial objects from their underlying components** according to a given classification taxonomy, in order to support a composable model for those objects (e.g. financial products, legal agreements or their associated lifecycle events).

Object qualification functions evaluate a combination of assertions that uniquely characterise an input object according to a chosen classification. Each function is associated to a qualification name (a ``string`` from that classification) and returns a boolean. This boolean evaluates to True when the input satisfies all the criteria to be identified according to that qualification name.

Object qualification functions are associated to a ``qualification`` annotation that specifies the type of object being qualified. The function name start with the ``Qualify`` prefix, followed by an underscore ``_``. The naming convention is to have an upper `CamelCase`_ (PascalCase) word, using ``_`` to append granular qualification names where the classification may use other types of separators (like space or colon ``:``).

Syntax validation logic based on the ``qualification`` annotation is in place to enforce this.

.. code-block:: Haskell

 func Qualify_InterestRate_IRSwap_FixedFloat_PlainVanilla: <"This product qualification doesn't represent the exact terms of the ISDA Taxonomomy V2.0 for the plain vanilla swaps, as some of those cannot be represented as part of the CDM syntax (e.g. the qualification that there is no provision for early termination which uses an off-market valuation), while some other are deemed missing in the ISDA taxonomy and have been added as part of the CDM (absence of cross-currency settlement provision, absence of fixed rate and notional step schedule, absence of stub). ">
   [qualification Product]
   inputs: economicTerms EconomicTerms (1..1)
   output: is_product boolean (1..1)

Calculation Function
""""""""""""""""""""

Calculation functions define a calculation output that is often, though not exclusively, of type ``number``. They must end with an ``assign-output`` statement that fully defines the calculation result.

Calculation functions are associated to the ``calculation`` annotation.

.. code-block:: Haskell
 
 func FixedAmount:
   [calculation]
   inputs:
     interestRatePayout InterestRatePayout (1..1)
     fixedRate FixedInterestRate (1..1)
     quantity NonNegativeQuantity (1..1)
     date date (1..1)
   output:
     fixedAmount number (1..1)
   
   alias calculationAmount: quantity -> amount
   alias fixedRateAmount: fixedRate -> rate
   alias dayCountFraction: DayCountFraction(interestRatePayout, interestRatePayout -> dayCountFraction, date)
   
   assign-output fixedAmount:
     calculationAmount * fixedRateAmount * dayCountFraction

Alias
"""""

The function syntax supports the definition of *aliases* that are only available in the context of the function. Aliases work like temporary variable assignments used in programming languages and are particularly useful in fully defined functions.

The above example builds an interest rate calculation using aliases to define the *calculation amount*, *rate* and *day count fraction* as temporary variables, and finally assigns the *fixed amount* output as the product of those three variables.

Short-Hand Function
"""""""""""""""""""

Short-hand functions are functions which are designed to provide a compact syntax for operations that need to be frequently invoked in the model - for instance, model indirections when the corresponding model expression may be deemed too long or cumbersome:

.. code-block:: Haskell

 func PaymentDate:
   inputs: economicTerms EconomicTerms (1..1)
   output: result date (0..1)
   assign-output result: economicTerms -> payout -> interestRatePayout only-element -> paymentDate -> adjustedDate

which could be invoked as part of multiple other functions that use the ``EconomicTerms`` object by simply stating:

.. code-block:: Haskell

 PaymentDate( EconomicTerms )


Mapping Component
-----------------

Synonym
^^^^^^^

Purpose
"""""""

*Synonym* is the baseline building block to map a model expressed in the Rosetta DSL to alternative data representations, whether those are open standards or proprietary. Synonyms can be complemented by mapping logic when the relationship is not a one-to-one or is conditional.

Synonyms are specified at the attribute level for a data type. Synonyms can also be associated to enumerations and are specified at the enumeration value level. Mappings are typically implemented by traversing the model tree down, so knowledge of the context of an attribute (i.e. the type in which it is used) determines what it should map to. Knowledge about the upper-level type would be lost if synonyms were implemented at the class level.

There is no limit to the number of synonyms that can be associated to any attribute, and there can even be several synonyms for a given data source (e.g. in the case of a conditional mapping).

Syntax
""""""

Synonyms are introduced by the ``synonym`` keyword and are specified for each attribute in between square brackets ``[`` ``]``, same as an annotation. The baseline synonym syntax has two components:

* **source**, which possible values are controlled by a special ``synonym source`` type of enumeration
* **value**, which is a ``string`` that identifies the name of the attribute as it is found in the source

For example for a data type:

.. code-block:: Haskell

 type FxRate: <"A class describing the rate of a currency conversion: pair of currency, quotation mode and exchange rate.">
 
   quotedCurrencyPair QuotedCurrencyPair (1..1) <"Defines the two currencies for an FX trade and the quotation relationship between the two currencies.">
     [synonym FpML_5_10, CME_SubmissionIRS_1_0, CME_ClearedConfirm_1_17 value "quotedCurrencyPair"]
   rate number (0..1) <"The rate of exchange between the two currencies of the leg of a deal. Must be specified with a quote basis.">
     [synonym FpML_5_10, CME_SubmissionIRS_1_0, CME_ClearedConfirm_1_17 value "rate"]

Or an enumeration:

.. code-block:: Haskell

 enum NaturalPersonRoleEnum: <"The enumerated values for the natural person's role.">
 
   Broker <"The person who arranged with a client to execute the trade.">
     [synonym FpML_5_10 , CME_SubmissionIRS_1_0 , CME_ClearedConfirm_1_17 value "Broker"]
   Buyer <"Acquirer of the legal title to the financial instrument.">
     [synonym FpML_5_10, CME_SubmissionIRS_1_0, CME_ClearedConfirm_1_17 value "Buyer"]
   DecisionMaker <"The party or person with legal responsibility for authorization of the execution of the transaction.">
     [synonym FpML_5_10, CME_SubmissionIRS_1_0, CME_ClearedConfirm_1_17 value "DecisionMaker"]
   ExecutionWithinFirm <"Person within the firm who is responsible for execution of the transaction.">
     [synonym FpML_5_10, CME_SubmissionIRS_1_0, CME_ClearedConfirm_1_17 value "ExecutionWithinFirm"]
   InvestmentDecisionMaker <"Person who is responsible for making the investment decision.">
     [synonym FpML_5_10, CME_SubmissionIRS_1_0, CME_ClearedConfirm_1_17 value "InvestmentDecisionMaker"]
   Seller <"Seller of the legal title to the financial instrument.">
     [synonym FpML_5_10, CME_SubmissionIRS_1_0, CME_ClearedConfirm_1_17 value "Seller"]
   Trader <"The person who executed the trade.">
     [synonym FpML_5_10, CME_SubmissionIRS_1_0, CME_ClearedConfirm_1_17 value "Trader"]

.. note:: The synonym value is of type ``string`` to facilitate integration with executable code. The alternative approach consisting of specifying the value as a compatible identifier alongside a display name has been disregarded because it has been deemed not appropriate to create a "code-friendly" value for the respective synonyms.

A further set of attributes can be associated with a synonym, to address specific use cases:

* **path** to allow mapping when data is nested in multiple levels within the respective model.
* **hint** to allow mapping when data is nested in different ways between the respective models.

The ``Price`` type provides a good illustration of such cases:

.. code-block:: Haskell

 type Price: <"Generic description of the price concept applicable across product types, which can be expressed in a number of ways other than simply cash price">
 
   cashPrice CashPrice (0..1) <"Price specified in cash terms, e.g. for securities proceeds or fee payment in a contractual product.">
     [synonym FpML_5_10 value "initialPrice" path "rateOfReturn", "underlyer"]
     [synonym FpML_5_10 hint "paymentAmount"]
     [synonym FpML_5_10 hint "fixedAmount"]
   exchangeRate ExchangeRate (0..1) <"Price specified as an exchange rate between two currencies.">
     [synonym FpML_5_10 value "exchangeRate"]
   fixedInterestRate FixedInterestRate (0..1) <"Price specified as a fixed interest rate.">
     [synonym FpML_5_10, CME_SubmissionIRS_1_0, CME_ClearedConfirm_1_17 value "fixedRateSchedule" path "calculationPeriodAmount->calculation"]
     [synonym FpML_5_10, CME_SubmissionIRS_1_0, CME_ClearedConfirm_1_17 value "fixedAmountCalculation"]
     [synonym FpML_5_10, CME_SubmissionIRS_1_0, CME_ClearedConfirm_1_17 value "fixedRateSchedule"]
     [synonym FpML_5_10, CME_SubmissionIRS_1_0, CME_ClearedConfirm_1_17 hint "fixedRate"]
   floatingInterestRate FloatingInterestRate (0..1) <"Price specified as a spread on top of a floating interest rate."
     [synonym FpML_5_10, CME_SubmissionIRS_1_0, CME_ClearedConfirm_1_17 value "floatingRateCalculation" path "calculationPeriodAmount->calculation"]
     [synonym FpML_5_10, CME_SubmissionIRS_1_0, CME_ClearedConfirm_1_17 value "floatingRateCalculation" path "interestCalculation"]
     [synonym FpML_5_10, CME_SubmissionIRS_1_0, CME_ClearedConfirm_1_17 value "floatingRateCalculation"]
     [synonym FpML_5_10, CME_SubmissionIRS_1_0, CME_ClearedConfirm_1_17 value "floatingAmountCalculation"]

* **tag** or a **componentID** to properly reflect the FIX standard, which uses those two components. There are only limited examples of such at present, as a result of the scope focus on post-execution use cases hence the limited reference to the FIX standard.

.. code-block:: Haskell

 type InformationSource: <"A class defining the source for a piece of information (e.g. a rate fix or an FX fixing). The attribute names have been adjusted from FpML to address the fact that the information is not limited to rates.">
   sourceProvider InformationProviderEnum (1..1)  <"An information source for obtaining a market data point. For example Bloomberg, Reuters, Telerate, etc.">
     [synonym FIX_5_0_SP2 value "RateSource" tag 1446]
   sourcePage string (0..1) <"A specific page for the source for obtaining a market data point. In FpML, this is specified as a scheme, rateSourcePageScheme, for which no coding Scheme or URI is specified.">
   sourcePageHeading string (0..1) <"The heading for the source on a given source page.">

* **definition** to provide a more explicit reference to the FIX enumeration values which are specified through a single digit or letter positioned as a prefix to the associated definition.

.. code-block:: Haskell

 enum InformationProviderEnum:
   AssocBanksSingapore
   BankOfCanada
   BankOfEngland
   BankOfJapan
   Bloomberg
     [synonym FIX_5_0_SP2 value "0" definition "0 = Bloomberg"]
   EuroCentralBank
   FHLBSF
   FederalReserve
   ISDA
   Other
     [synonym FIX_5_0_SP2 value "99" definition "99 = Other"]
   ReserveBankAustralia
   ReserveBankNewZealand
   Reuters
     [synonym FIX_5_0_SP2 value "1" definition "1 = Reuters"]
   SAFEX
   Telerate
     [synonym FIX_5_0_SP2 value "2" definition "2 = Telerate"]

Meta-Data Mapping
"""""""""""""""""

When meta-data are associated to an attribute, as decribed in the `Meta-Data and Reference Section`_, additional synonym syntax allows to specify how to retrieve the corresponding meta-data from the source. This is illustrated by the usage of the ``meta`` synonym syntax in the example below:

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

The ``issuer`` attribute has an associated ``scheme``. The scheme can be retrieved using the ``issuerIdScheme`` meta-data that is attached to the ``issuer`` value in the synonym source.

To be able to specify an attribute as a reference from an existing source, the source itself must implement some cross-referencing mechanism so that the reference can be identified, as in the ``href`` / ``id`` mechanism used in XML. The cross-referencing works as follows:

* the attribute must specify the identifier value for the reference in the synonym source. For the ``issuerReference`` attribute above, this is specified as the ``href`` meta-data of the ``issuer`` value in the source.
* an identifier value must be associated to the object being referenced. For the ``Party`` type, this is specified as the ``id`` meta-data in the synonym source, as shown below:

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
 
The below JSON extract illustrates an implementation of these meta-data in the context of a *transaction event*, which identifies the parties to the transactions as well as the *issuer* of the event (i.e. who submits the transaction message).

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

.. note:: This example is not part of the Rosetta DSL but corresponds to the default JSON implementation of the model. The choice of either maintaining or shredding external references (such as "party2"), once cross-reference has been established using the source document, is up to implementors of the model.

Mapping Rule
^^^^^^^^^^^^

Purpose
"""""""

There are cases where the mapping between existing standards and protocols and their relation to the model is not one-to-one or is conditional. Synonyms have been complemented with a syntax to express mapping logic that provides a balance between flexibility and legibility.

Syntax
""""""

The mapping rule syntax differs from the normal Rosetta DSL syntax in that it is not expressed as a stand-alone block with a qualifier prefix such as ``condition``. Instead, the mapping rule is positioned as an extension of the synonym syntax. Several mapping rule expressions can be associated with a given synonym.

A mapping rule is composed of two (optional) expressions:

* **mapping value** prefixed with ``set to``, which specifies the value that the attribute should be set to when the conditional expression is true
* **conditional expression** prefixed with ``when``, to associate conditional logic to the mapping value

The mapping logic associated with the party role example below provides a good illustration of such logic:

.. code-block:: Haskell

 type PartyRole:
 
   partyReference Party (1..1)
   role PartyRoleEnum (1..1)
     [synonym FpML_5_10 set to PartyRoleEnum -> DeterminingParty when path = "trade->determiningParty"]
     [synonym FpML_5_10 set to PartyRoleEnum -> BarrierDeterminationAgent when path = "trade->barrierDeterminationAgent"]
     [synonym FpML_5_10 set to PartyRoleEnum -> HedgingParty when path = "trade->hedgingParty"]
     [synonym FpML_5_10 set to PartyRoleEnum -> ArrangingBroker when path = "trade->brokerPartyReference"]
     [synonym FpML_5_10, CME_ClearedConfirm_1_17 value "role" path "tradeHeader->partyTradeInformation->relatedParty"]
   ownershipPartyReference Party (0..1)

.. _Cardinality Section: https://docs.rosetta-technology.io/dsl/documentation.html#cardinality
.. _Condition Statement Section: https://docs.rosetta-technology.io/dsl/documentation.html#condition-statement
.. _Meta-Data and Reference Section: https://docs.rosetta-technology.io/dsl/documentation.html#meta-data-and-reference
.. _Synonym Section: https://docs.rosetta-technology.io/dsl/documentation.html#synonym
.. _Qualified Type Section: https://docs.rosetta-technology.io/dsl/documentation.html#qualified-type
.. _Function Definition Section: https://docs.rosetta-technology.io/dsl/documentation.html#function-definition
.. _CamelCase: https://en.wikipedia.org/wiki/Camel_case
.. _UTC: https://en.wikipedia.org/wiki/Coordinated_Universal_Time
