Rosetta Modelling Components
============================
**The Rosetta syntax can express seven types of model components**:

* Data
* Annotation
* Data Validation (or *condition*)
* Function
* Mapping (or *synonym*)
* Reporting
* Namespace

This documentation details the purpose and features of each type of model component and highlights the relationships that exist among those. As the initial live application of the Rosetta DSL, examples from the ISDA CDM will be used to illustrate each of those features.

Data Component
--------------
**The Rosetta DSL provides two data definition components** that are used to model data:

* `Type <#type-label>`_
* `Enumeration <#enumeration-label>`_

.. _type-label:

Type
^^^^
Purpose
"""""""
A *type* describes an *entity* (also sometimes referred to as an *object* or a *class*) in the model and is defined by a plain-text description and a set of *attributes* (also sometimes refered to as fields). Attributes specify the granular elements composing the entity.

Syntax
""""""
The definition of a type starts with the keyword ``type``, followed by the type name. A colon ``:`` punctuation introduces the rest of the definition.

The Rosetta DSL convention is that type names use the *PascalCase* (starting with a capital letter, also referred to as the *upper* `CamelCase`_). Type names need to be unique across a `namespace <#namespace-label>`_. All those requirements are controlled by the Rosetta DSL grammar.

The first component of the definition is a plain-text description of the type. Descriptions use quotation marks ``"`` ``"`` (to mark a string) in between angle brackets ``<`` ``>``. Descriptions, although not generating any executable code, are integral meta-data components of the model. As modelling best practice, a definition ought to exist for every artefact and be clear and comprehensive.

After the description come any `annotations <#annotations-label>`_ that are applied to this type. Annotations are enclosed within square brackets '[' and ']'

.. code-block:: Haskell
 
  type WorkflowStep: <"A workflow step ....">
	[metadata key]
	[rootType]

Then the definition of the type lists its component attributes. Each attribute is defined by three required components, and two optional components, syntactically ordered as:

* name - 
  Required - Attribute names use the *camelCase* (starting with a lower case letter, also referred to as the *lower* camelCase).
* type - 
  Required - Each attribute can be specified either as a `basic type <#basic-type-label>`_, `record type <#record-type-label>`_, data type or `enumeration type <#enumeration-label>`_.
* cardinality -  
  Required - see `Cardinality <#cardinality-label>`_
* description - Optional but recommended) - A description of the attribute using the sames <"..."> syntax as the type description
* annotations - Optional - Annotations such as `synonyms <mapping.html>`_ or metadata can be applied to attributes

.. code-block:: Haskell

  type PeriodBound: <"The period bound is defined as a period and whether the bound is inclusive.">
    period Period (1..1) <"The period to be used as the bound, e.g. 5Y.">
    inclusive boolean (1..1) <"Whether the period bound is inclusive, e.g. for a lower bound, false would indicate greater than, whereas true would indicate greater than or equal to.">

 type Period: <"A class to define recurring periods or time offsets.">
   periodMultiplier int (1..1) <"A time period multiplier, e.g. 1, 2 or 3 etc. A negative value can be used when specifying an offset relative to another date, e.g. -2 days.">
   period PeriodEnum (1..1) <"A time period, e.g. a day, week, month or year of the stream. If the periodMultiplier value is 0 (zero) then period must contain the value D (day).">

.. note:: The Rosetta DSL does not use any delimiter to end definitions. All model definitions start with a similar opening keyword as ``type``, so the start of a new definition marks the end of the previous one. For readability more generally, the Rosetta DSL looks to eliminate all the delimiters that are often used in traditional programming languages (such as curly braces ``{`` ``}`` or semi-colon ``;``).

Built in types
^^^^^^^^^^^^^^
.. _basic-type-label:

Basic Types
"""""""""""
Rosetta defines five fundamental data types.  The set of basic types available in the Rosetta DSL are controlled at the language level by the ``basicType`` definition:

 * ``string`` - Text
 * ``int`` - integer numbers
 * ``number`` - decimal numbers
 * ``boolean`` - logical true of false
 * ``time`` - simple time values (e.g. "05:00:00")

.. _record-type-label:

Record Types
""""""""""""
Rosetta defines two record types ``date`` and ``zonedDateTime``.  The set of record types available in the Rosetta DSL are controlled at the language level by the ``recordType`` definition.

Record types are simplified data types:

* Record types are pure data definitions and do not allow specification of validation logic in ``conditions``.
* Record types are handled specially in the code-generators as so form part of the Rosetta DSL, rather than any Rosetta base domain model. 

Time
""""
The ``zonedDateTime`` record type unambiguously refers to a single instant of time.

Alternatively in the CDM there is the data type ``BusinessCenterTime`` , where a simple ``time`` "5:00:00" is specified alongside a business center.  The simple time should be interpreted with the timezone information of the associated business center.

Inheritance
"""""""""""

**The Rosetta DSL supports an inheritance mechanism**, when a type inherits its definition and behaviour (and therefore all of its attributes) from another type and adds its own set of attributes on top. Inheritance is supported by the ``extends`` keyword next to the type name.

.. code-block:: Haskell

 type Offset extends Period:
    dayType DayTypeEnum (0..1)

.. note:: For clarity purposes, the documentation snippets omit the synonyms and definitions that are associated with the classes and attributes, unless the purpose of the snippet is to highlight some of those features.

.. _enumeration-label:

Enumeration
^^^^^^^^^^^
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

Enumeration names must be unique across a `namespace <#namespace-label>`_. The Rosetta DSL naming convention is the same as for types and must use the upper CamelCase (PascalCase).  In addition the enumeration name should end with the suffix Enum. 
The Enumeration values cannot start with a numerical digit, and the only special character that can be associated with them is the underscore ``_``.

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

.. _namespace-label:

Namespace Component
-------------------
Purpose
"""""""
The namespace syntax allows model artifacts in a data model to be organised into groups of namespaces. A namespace is an abstract container created to hold a logical grouping of model artifacts. The approach is designed to make it easier for users to understand the model structure and adopt selected components. It also aids the development cycle by insulating groups of components from model restructuring that may occur.  Model artifacts are organised into a directory structure that follows the namespaces’ Group and Artifact structure (a.k.a. “GAV coordinates”). This directory structure is exposed in the model editor.

By convention namespaces are organised into a hierarchy, with layers going from in to out. The hierarchy therefore contains an intrinsic inheritance structure where each layer has access to (“imports”) the layer outside, and is designed to be usable without any of its inner layers. Layers can contain several namespaces (“siblings”), which can also refer to each other. 

Syntax
"""""""

The definition of a namespace starts with the `namespace` keyword, followed by the location of the namespace in the directory structure. ::

  namespace cdm.product.common

The names of all components must be unique within a given namespace. Components can refer to other components in the same namespace using just their name. Components can refer to components outside their namespace either by giving the *fully qualified name* e.g. ``cdm.base.datetime.AdjustableDate`` or by importing the namespace into the current file.

To gain access to model components contained within another namespace the `import` keyword is used. ::

  import cdm.product.asset.*

In the example above all model components contained within the cdm.product.asset namespace will be imported. Note, only components contained within the layer referenced will be imported, in order to import model components from namespaces embedded within that layer further namespaces need to be individually referenced. ::

  import cdm.base.math.*
  import cdm.base.datetime.*
  import cdm.base.staticdata.party.*
  import cdm.base.staticdata.asset.common.*
  import cdm.base.staticdata.asset.rates.*
  import cdm.base.staticdata.asset.credit.*

In the example above all model components contained within the layers of the `cdm.base` namespace are imported.

.. _annotations-label:

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

It is possible to associate attributes to an annotation (see `metadata <#metadata-label>`_ example), even though some annotations may not require any further attribute. For instance:
.. _roottype-label:

.. code-block:: Haskell

 annotation rootType: <"Mark a type as a root of the rosetta model">

 annotation deprecated: <"Marks a type, function or enum as deprecated and will be removed/replaced.">

An annotation can be added to a Rosetta Type or attribute by enclosing the name of the annotation in sqaure bracketss 

Meta-Data and Reference
^^^^^^^^^^^^^^^^^^^^^^^
Purpose
"""""""
.. _metadata-label:

The ``metadata`` annotation allows the declaration of a set of meta-data qualifiers that can be applied to types and attributes. By default Rosetta includes several metadata annotations 

.. code-block:: Haskell

 annotation metadata:
   id string (0..1)
   key string (0..1)
   scheme string (0..1)
   reference string (0..1)
   template string (0..1)
	 location string (0..1) <"Specifies this is the target of an internal reference">
	 address string (0..1) <"Specified that this is an internal reference to an object that appears elsewhere">

Each attribute of the ``metadata`` annotation corresponds to a qualifier that can be applied to a rosetta type or attribute:

* The ``scheme`` meta-data qualifier specifies a mechanism to control the set of values that an attribute can take. The relevant scheme reference may be specified as meta-information in the attribute's data source, so that no originating information is disregarded.
* The ``template`` meta-data qualifier indicates that a type is eligible to be used as a data template. Data templates provide a way to store data which may be duplicated across multiple objects into a single template, to be referenced by all these objects.
* the other metadata annotations above are used in referencing.

Referencing
"""""""""""
Referencing allows an attribute in rosetta to refer to a rosetta object in a different location. A reference consists of a metadata ID associated with an object and elsewhere an attribute that instead of having a normal value has that id as a reference metadata field. E.g. the exanple below has a Party with "globalKey" (see below) acting as an identifier and later on a reference to that party using the "globalReference" (see below also)::

 "party" : {
    "meta" : {
      "globalKey" : "3fa8e998",
      "externalKey" : "f845ge"
    },
    "name" : {
      "value" : "XYZ Bank"
    },
    "partyId" : [ {
      "value" : "XYZBICXXX",
      "meta" : {
        "scheme" : "http://www.fpml.org/coding-scheme/external/iso9362"
      }
    } ]
  }
 
 "partyReference" : {
        "globalReference" : "3d9e6ab8"
  }      


Rosetta currenly supports 3 different mechanisms for references with different scopes. It is intended that these will all be migrated to a single mechanism.

Global References
/////////////////


The ``key`` and ``id`` metadata annotations cause a globaly unique key to be generated for the rosetta object or attribute. The value of the key corresponds to a hash code to be generated by the model implementation. The implementation provided in the Rosetta DSL is a *deep hash* that uses the complete set of attribute values that compose the type and its attributes, recursively.

The ``reference`` metadata annotation denotes that an attribute can be either a direct value like any other attribute or can be replaces with a ``reference`` to a global key defined elsewhere. The key need not be defined in the current document but can instead be a reference to an external document.

External References
///////////////////

Attributes and types that have the ``key`` or ``id`` annotation additionally have an ``externalKey`` attached to them. This is used to store keys that are read from an exernal source e.g. FpML id metadata attribute. 

Attributes with the ``referecne`` keyword have a corresponding externalReference field which is used to store references from external sources. The reference resolver processor can be used to link up the references.

Templates
/////////

When a type is annotated as a template, it is possible to specify a template reference that cross-references a template object. The template object, as well as any object that references it, are typically *incomplete* model objects that should not be validated individually. Once a template reference has been resolved, it is necessary to merge the template data to form a single fully populated object. Validation should only be performed once the template reference has been resolved and the objects merged together. 

Other than the new annotation, data templates do not have any impact on the model, i.e. no new types, attributes, or conditions.

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

A ``key`` qualifier is associated to the ``Party`` type, which means it is referenceable. In the ``Identifier`` type, the ``reference`` qualifier, which is associated to the ``issuerReference`` attribute of type ``Party``, indicates that this attribute can be provided as a reference (via its associated key) instead of a copy. An example implementation of this cross-referencing mechanism for these types can be found in the `synonym <mapping.html>`_ of the documentation.

When a data type is annotated as a ``template``, the designation applies to all encapsulated types in that data type. In the example below, the designation of template eligibility for ``ContractualProduct`` also applies to ``EconomicTerms``, which is an encapsulated type in ``ContractualProduct``, and likewise applies to all encapsulated types in ``EconomicTerms``.

.. code-block:: Haskell

 type ContractualProduct:
   [metadata key]
   [metadata template]
   productIdentification ProductIdentification (0..1)
   productTaxonomy ProductTaxonomy (0..*)
   economicTerms EconomicTerms (1..1)

.. _qualification-label:

Qualified Type
^^^^^^^^^^^^^^
The Rosetta DSL provides for some special types called *qualified types*, which are specific to its application in the financial domain:

* Calculation - ``calculation``
* Object qualification - ``productType`` ``eventType``

Those special types are designed to flag attributes which result from running some logic, such that model implementations can identify where to stamp the output in the model. The logic is being captured by specific types of functions that are detailed in the `Function Definition Section <#function-label>`_.

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

.. _cardinality-label:

Cardinality
^^^^^^^^^^^

Cardinality is a data integrity mechanism to control how many of each attribute an object of a given type can contain. The Rosetta DSL borrows from XML and specifies cardinality as a lower and upper bound in between ``(`` ``..`` ``)`` brackets.

.. code-block:: Haskell

 type Address:
   street string (1..*)
   city string (1..1)
   state string (0..1)
   country string (1..1)
     [metadata scheme]
   postalCode string (1..1)

The lower and upper bounds can both be any integer number. A 0 lower bound means attribute is optional. A ``*`` upper bound means an unbounded attribute. ``(1..1)`` represents that there must be one and only one attribute of this type. When the upper bound is greater than 1, the attribute will be considered as a list, to be handled as such in any generated code.

A validation rule is generated for each attribute's cardinality constraint, so if the cardinality of the attribute does not match the requirement an error will be associated with that attrute by the validation process.

.. _condition-label: 

Condition Statement
^^^^^^^^^^^^^^^^^^^

Purpose
"""""""

*Conditions* are logic `expressions <expressions.html>`_ associated to a data type. They are predicates on attributes of objects of that type that evaluate to True or False As part of validation all the conditins are evaluated and if any evaluate to false then the validation fails.

Syntax
""""""

Condition statements are included in the definition of the type that they are associated to and are usually appended after the definition of the type's attributes.

The definition of a condition starts with the ``condition`` keyword, followed by the name of the condition and a colon ``:`` punctuation. The condition's name must be unique in the context of the type that it applies to (but does not need to be unique across all data types of a given model). The rest of the condition definition comprises:

* a plain-text description (optional)
* a boolean `expression <expressions.html>`_ that applies to the the type's attributes

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

.. code-block:: Haskell

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

.. _function-label:

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
       
Inputs and outputs can both have multiple cardinality in which case they will be treated as lists

.. code-block:: Haskell

 func MutiplyListBy2: <"Multiplies each element in a list by 2">
    inputs:
       numbers number (1..*)
    output:
       doubles number  (1..*)

Conditions
""""""""""
A function's inputs and output can be constrained using *conditions*.

Condition statements in a function can represent either:

* a **pre-condition**, using the ``condition`` keyword, applicable to inputs only and evaluated prior to executing the function, or
* a **post-condition**, using the ``post-condition`` keyword, applicable to inputs and output and evaluated after executing the function (once the output is known)

Each type of condition keyword is followed by a `boolean expression <expressions.html>`_ which is evaluated to check the correctness of the function inputs and result.

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
**The Rosetta DSL allows to further define the business logic of a function**, by building the function output instead of just specifying the function's API. The creation of valid output objects can be fully or partially defined as part of a function specification, or completely left to the implementor. The parts of a function definition that have been fully defined as `Rosetta Expression <expressions.html>`_ will be be translated into functional code which don't require further implementation.

The return object or individual attributes of the return object can be set by the function definition using the assign-output syntax; the keyword ``assign-output`` is followed by a `Rosetta Path <expressions.html#rosetta-path-label>`_ , a ``:`` and then an `expression <expressions.html>`_ used to calculate the value from the inputs

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

Those functions are typically associated to an annotation, as described in the `Qualified Type Section <#qualified-label>`_, to instruct code generators to create concrete functions.

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
Mapping in rosetta provides a mechanism for specifying how documents that are not Rosetta documents should be transformed into Rosetta documents. For more information see `mapping <mapping.html>`_

Reporting Component
-------------------

Motivation
^^^^^^^^^^

**One of the applications of the Rosetta DSL is to facilitate the process of complying with, and supervising, financial regulation** – in particular, the large body of data reporting obligations that industry participants are subject to.

The current industry processes to implement those rules are costly and inefficient. They involve translating pages of legal language, in which the rules are originally written, into business requirements which firms then have to code into their systems to support the regulatory data collection. This leads to a duplication of effort across a large number of industry participants and to inconsistencies in how each individual firm applies the rules, in turn generating data of poor quality and comparability for regulators.

By contrast, a domain-model for the business process or activity being regulated provides standardised, unambiguous definitions for business data at the source. In turn, these business data can be used as the basis for the reporting process, such that regulatory data become unambiguous views of the business data.

The Rosetta DSL allows to express those reporting rules as functional components in the same language as the model for the business domain itself. Using code generators, those functional rules are then distributed as executable code, for all industry participants to use consistently in their compliance systems.


Regulatory Hierarchy
^^^^^^^^^^^^^^^^^^^^

Purpose
"""""""

One of the first challenges of expressing regulatory rules for the financial domain is to organise the content of the regulatory framework that mandates these rules. The financial industry is a global, highly regulated industry, where a single line of business or activity may operate across multiple jurisdictions and regulatory regimes. The applicable regulations can span thousands of pages of legal text with intricate cross-references.

To organise such regulatory content within a model, the Rosetta DSL supports a number of key concepts that allow to refer to specific documents, their content and who owns them as direct model components.

Syntax
""""""

There are 3 syntax components to define the hierarchy of regulatory content:

#. Body
#. Corpus
#. Segment

A body refers to an entity that is the author, publisher or owner of a regulatory document. Examples of bodies include regulatory authorities or trade associations.

The syntax to define a body is: ``body`` <Type> <Name> <Description>. Some examples of bodies, with their corresponding types, are given below.

.. code-block:: Haskell

 body Organisation ISDA
   <"Since 1985, the International Swaps and Derivatives Association has worked to make the global derivatives markets safer and more efficient">

 body Authority MAS
   <"The Monetary Authority of Singapore (MAS) is Singapore’s central bank and integrated financial regulator. MAS also works with the financial industry to develop Singapore as a dynamic international financial centre.">

A corpus refers to a document set that contains rule specifications. Rules can be specified according to different levels of detail, including laws (as voted by lawmakers), regulatory texts and technical standards (as published by regulators), or best practice and guidance (as published by trade associations).

The syntax to define a corpus is: ``corpus`` <Type> <Alias> <Name> <Description>. While the name of a corpus provides a mechanism to refer to such corpus as a model component in other parts of a model, an alias provides an alternative identifier by which a given corpus may be known.

Some examples of corpuses, with their corresponding types, are given below. In those cases, the aliases refer to the official numbering of document by the relevant authority.

.. code-block:: Haskell

 corpus Regulation "600/2014" MiFIR
   <"Regulation (EU) No 600/2014 of the European Parliament and of the Council of 15 May 2014 on markets in financial instruments and amending Regulation (EU) No 648/2012 Text with EEA relevance">

 corpus Act "289" SFA
   <"The Securities And Futures Act relates to the regulation of activities and institutions in the securities and derivatives industry, including leveraged foreign exchange trading, of financial benchmarks and of clearing facilities, and for matters connected therewith.">

Corpuses are typically large sets of documents which can contain many rule specifications. The Rosetta DSL provides the concept of segment to allow to refer to a specific section in a given document.

The syntax to define a segment is: ``segment`` <Type>. Below are some examples of segment types, as are often found in regulatory texts.

.. code-block:: Haskell

 segment article
 segment whereas
 segment annex
 segment table

Once a segment type is defined, it can be associated to an identifier (i.e some free text representing either the segment number or name) and combined with other segment types to point to a specific section in a document. For instance:

.. code-block:: Haskell

 article "26" paragraph "2"


Report Definition
^^^^^^^^^^^^^^^^^

Purpose
"""""""

A report consists of an inter-connected set of regulatory obligations, which a regulated entity must implement to produce data as required by the relevant regulator.

Generically, the Rosetta DSL allows to specify any report using 3 types of rules:

- timing – when to report,
- eligibility – whether to report, and
- field – what to report.

A report is associated to an authoritative body and to the corpus(es) in which those rules are specified. Usually but not necessarily, the authority that mandates the rules also supervises their application and collects the data. Timing, eligibility and field rules translate into obligations of “timing, completeness and accuracy” of reporting, as often referred to by supervisors.

Syntax
""""""

A report is specified using the following syntax:

  ``report`` <Authority> <Corpus1> <Corpus2> <...> ``in`` <TimingRule>

  ``when`` <EligibilityRule1> ``and`` <EligibilityRule2> ``and`` <...>

  ``with fields`` <FieldRule1> <FieldRule2> <...>

An example is given below.

.. code-block:: Haskell

 report MAS SFA MAS_2013 in T+2
   when ReportableProduct and NexusCompliant
   with fields
     UniqueTransactionIdentifier
     UniqueProductIdentifier
     PriorUniqueTransactionIdentifier
     Counterparty1
     Counterparty2

To ensure a model’s regulatory framework integrity, the authority, corpus and all the rules referred to in a report definition must exist as model components in the model’s regulatory hierarchy. A report simply assembles all those existing components into a *recipe*, which firms can directly implement to comply with the reporting obligation and provide the data as required.

The next section describes how to define reporting rules as model components.

.. _report-rule-label:

Rule Definition
^^^^^^^^^^^^^^^

Purpose
"""""""

The Rosetta DSL applies a functional approach to the process of regulatory reporting. A regulatory rule is a functional model component (``F``) that processes an input (``X``) through a set of logical instructions and returns an output (``Y``), such that ``Y = F( X )``. A function ``F`` can sometimes also be referred to as a *projection*. Using this terminology, the reported data (``Y``) are viewed as projections of the business data (``X``).

For field rules, the output ``Y`` consists of the data point to be reported. For eligibility rules, this output is a Boolean that returns True when the input is eligible for reporting.

To provide transparency and auditability to the reporting process, the Rosetta DSL supports the development of reporting rules in both human-readable and machine-executable form.

- The functional expression of the reporting rules is designed to be readable by professionals with domain knowledge (e.g. regulatory analysts). It consists of a limited set of logical instructions, supported by the compact Rosetta DSL syntax.
- The machine-executable form is derived from this functional expression of the reporting logic using the Rosetta DSL code generators, which directly translate it into executable code.
- In addition, the functional expression is explicitly tied to regulatory references, using the regulatory hierarchy concepts of body, corpus and segment to point to specific text provisions that support the reporting logic. This mechanism, coupled with the automatic generation of executable code, ensures that a reporting process that uses that code is fully auditable back to any applicable text.

Syntax
""""""

The syntax of reporting field rules is as follows:

  ``reporting rule`` <Name>

  [``regulatoryReference`` <Body> <Corpus> <Segment1> <Segment2> <...> ``provision`` <”ProvisionText”>]

  <FunctionalExpression>

For eligibility rules, the syntax is the same but starts with the keyword ``eligibility rule``.

The functional expression of reporting rules uses the same syntax components that are already available to express logical statements in other modelling components, such as the condition statements that support data validation.

Functional expressions are composable, so a rule can also call another rule. When multiple rules may need to be applied for a single field or eligibility criteria, those rules can be specified in brackets separated by a comma. An example is given below for the *Nexus* eligibility rule under the Singapore reporting regime, where ``BookedInSingapore`` and ``TradedInSingapore`` are themselves eligibility rules.

.. code-block:: Haskell

 eligibility rule NexusCompliant
   [regulatoryReference MAS SFA MAS_2013 part "1 " section "Citation and commencement"
   provision "In these Regulations, unless the context otherwise requires; Booked in Singapore, Traded in Singapore"]
   (
     BookedInSingapore,
     TradedInSingapore
   )

In addition to those existing functional features, the Rosetta DSL provides other syntax components that are specifically designed for reporting applications. Those components are:

- ``extract`` <Expression>

When defining a reporting rule, the `extract` keyword defines a value to be reported, or to be used as input into a subsequent statement or another rule. The full expressional syntax of the Rosetta DSL can be used in the expression that defines the value to be extracted, including conditional statement such as ``if`` / ``else`` / ``or`` / ``exists``.

An example is given below, that uses a mix of Boolean statements. This example looks at the fixed and floating rate specificiation of an InterestRatePayout and if there is one of each returns true

.. code-block:: Haskell

 reporting rule IsFixedFloat
   extract Trade -> tradableProduct -> product -> contractualProduct -> economicTerms -> payout -> interestRatePayout -> rateSpecification -> fixedRate count = 1
   and Trade -> tradableProduct -> product -> contractualProduct -> economicTerms -> payout -> interestRatePayout -> rateSpecification -> floatingRate count = 1

The extracted value may be coming from a data attribute in the model, as above, or may be directly specified as a value, such as a ``string`` in the below example.

.. code-block:: Haskell

 extract if WorkflowStep -> businessEvent -> primitives -> execution exists
   or WorkflowStep -> businessEvent -> primitives -> contractFormation exists
   or WorkflowStep -> businessEvent -> primitives -> quantityChange exists
     then "NEWT"

- <ReportExpression1> ``then`` <ReportExpression2>

Report statements can be chained using the keyword ``then``, which means that extraction continues from the previous point.

The syntax provides type safety when chaining rules, whereby the output type of the preceding rule must be equal to the input type of the following rule. The example below uses the TradeForEvent rule to find the Trade object and ``then`` extracts the termination date from that trade

.. code-block:: Haskell

 reporting rule MaturityDate <"Date of maturity of the financial instrument. Field only applies to debt instruments with defined maturity">
 	TradeForEvent then extract Trade -> tradableProduct -> product -> contractualProduct -> economicTerms -> terminationDate -> adjustableDate -> unadjustedDate

 reporting rule TradeForEvent
 	extract
 		if WorkflowStep -> businessEvent -> primitives -> contractFormation -> after -> trade only exists
	then WorkflowStep -> businessEvent -> primitives -> contractFormation -> after -> trade
		else WorkflowStep -> businessEvent -> primitives -> contractFormation -> after -> trade
- ``as`` <FieldName>

Any report statement can be follows by ``as`` This sets a label under which the value will appear in a report, as in the below example.

.. code-block:: Haskell

 reporting rule RateSpecification
   extract Trade -> tradableProduct -> product -> contractualProduct -> economicTerms -> payout -> interestRatePayout -> rateSpecification
   as "Rate Specification"

The label is an arbitrary ``string`` and should be aligned with the name of the reportable field as per the regulation. This field name will be used as column name when displaying computed reports, but is otherwise not functionally usable.

- ``Rule if`` statement

The rule if statement consists of the keyword ``if`` followed by condition that will be evaluated ``return`` followed by a rule. 
If the condition is true then the value of the ``return`` rule is returned.
Additional conditions and ``return`` rules can be specified with ``else if``. Only the first matching condition's ``return`` will be executed.
``else return`` can be used to provide an alternative that will be executed if no conditions match
In the below example we first extract the Payout from a Trade then we try to find the appropriate asset class.
If there is a ForwardPayout with a foreignExchange underlier then "CU" is returned as the "2.2 Asset Class"
If there is an OptionPayout with a foreignExchange underlier then "CU" is returned as the "2.2 Asset Class"
otherwise the asset class is null

.. code-block:: Haskell

  extract Trade -> tradableProduct -> product -> contractualProduct -> economicTerms -> payout then
  if filter when Payout -> forwardPayout -> underlier -> underlyingProduct -> foreignExchange exists
	    do return "CU" as "2.2 Asset Class"
	  else if filter when Payout -> optionPayout -> underlier -> underlyingProduct -> foreignExchange exists
	    do return "CU" as "2.2 Asset Class",
		do return "null" as "2.2 Asset Class"
	endif

Filtering Rules
///////////////

Filtering and max/min/first/last rules take a collection of input objects and return a subset of them. The output type of the rule is always the same as the input.

- ``filter when`` <FunctionalExpression>

The ``filter when`` keyword takes each input value and uses it as input to a provided test expression The result type of the test expression must be boolean and its input type must be the input type of the filter rule. 
If the expression returns ``true`` for a given input that value is included in the output.
The code below selects the PartyContactInformation objects then filters to only the paries that are reportingParties before then returning the partyReferences

.. code-block:: Haskell

 reporting rule ReportingParty <"Identifier of reporting entity">
   TradeForEvent then extract Trade -> partyContractInformation then
   filter when PartyContractInformation -> relatedParty -> role = PartyRoleEnum -> ReportingParty then
   extract PartyContractInformation -> partyReference

The functional expression can be either a direct Boolean expression as above, or the output of another rule, in which case the syntax is: ``filter when rule`` <RuleName>, as in the below example.
This example filters all the input trades to return only the ones that InterestRatePayouts and then extracts the fixed interest rate for them.

.. code-block:: Haskell

 reporting rule FixedFloatRateLeg1 <"Fixed Float Price">
   filter when rule IsInterestRatePayout then
   TradeForEvent then extract Trade -> tradableProduct -> priceNotation -> price -> fixedInterestRate -> rate as "II.1.9 Rate leg 1"

And the filtering rule is defined as:

.. code-block:: Haskell

 reporting rule IsInterestRatePayout
   TradeForEvent then
   extract Trade -> tradableProduct -> product -> contractualProduct -> economicTerms -> payout -> interestRatePayout only exists

- ``maximum`` / ``minimum``

The ``maximum`` and ``minimum`` keywords return only a single value (for a given key). The value returned will be the higest or lowest value. The input type to the rule must be of a comparable basic data type
e.g. date, time, number, string
In the below example, we first apply a filter and extract a ``rate`` attribute. There could be multiple rate values, so we select the highest one.

.. code-block:: Haskell

 filter when rule IsFixedFloat then
   extract Trade -> tradableProduct -> priceNotation -> price -> fixedInterestRate -> rate then
   maximum

- ``maxBy`` / ``minBy``

The syntax also supports selecting values by an ordering based on an attribute using the ``maxBy`` and ``minBy`` keywords. For each input value to the rule the provided test expression or rule is evaluated to give a test result and paired with the input value. 
When all values have been processes the pair with the highest test result is selected and the associated value is returned by the rule.
The test expression or rule must return a value of single cardinality and must be of a comparable basic data type
e.g. date, time, number, string
In the below example, we first apply a filter and extract a ``fixedInterestRate`` attribute. There could be multiple attribute values, so we select the one with the higest rate and return that FixedInterestRate object.

.. code-block:: Haskell

 filter when rule IsFixedFloat then
   extract Trade -> tradableProduct -> priceNotation -> price -> fixedInterestRate then
   maxBy FixedInterestRate -> rate




.. _CamelCase: https://en.wikipedia.org/wiki/Camel_case
.. _UTC: https://en.wikipedia.org/wiki/Coordinated_Universal_Time
