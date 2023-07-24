---
title: "Rosetta Modelling Components"
date: 2022-02-09T00:38:25+09:00
description: "This documentation details the purpose and features of each type of model component and highlights their relationships. Examples drawn from the Demonstration Model, a sandbox model of the 'vehicle' domain, will be used to illustrate each of those features."
draft: false
weight: 2
---

# Rosetta Modelling Components

**The Rosetta DSL can express eight types of model components**:

- Data
- Meta-Data
- Expression (or *logic*)
- Data Validation (or *condition*)
- Function
- Namespace
- Mapping (or *synonym*)
- Reporting

This documentation details the purpose and features of each type of model component and highlights their relationships. Examples drawn from the [Demonstration Model](https://github.com/rosetta-models/demo), a sandbox model of the "vehicle" domain, will be used to illustrate each of those features.

## Data Component

**The Rosetta DSL provides four components to represent data** in a model:

- [Built-in type](#built-in-type)
- [Data type](#data-type)
- [Enumeration](#enumeration)
- [Meta-type](#meta-type)

Those four components are often collectively referred to as *types*.

### Built-in Type

Rosetta includes a number of built-in types that are deemed fundamental and applicable to any model. Those types are defined at the language level. There are three types of built-in types:
- basic type
- parameterized basic type
- record type

#### Basic Type

Rosetta defines five *basic types*. The set of basic types available in the Rosetta DSL are controlled by defining them as `basicType` at the language level.

- `int` - integer numbers
- `number` - decimal numbers
- `boolean` - logical true of false
- `string` - text
- `time` - simple time values, e.g. \"05:00:00\"

#### Parameterized Basic Type

The basic types `number` and `string` can be parameterized in order to support different precisions and conditions.

The example below specifies a positive decimal `number` with a precision of 18, that is at most 5000.

``` Haskell
number(digits: 18, fractionalDigits: 17, min: 0, max: 5000)
```

Note that all parameterized arguments are optional. By default, there are no constraints.

{{< notice info "Note" >}}
The basic type `int` is shorthand for `number(fractionalDigits: 0)`. It also supports specifying the constraints `digits`, `min` and `max`.
{{< /notice >}}

The example below specifies a `string` with 3 to 5 characters that must be alphanumeric.

``` Haskell
string(minLength: 3, maxLength: 5, pattern: "[a-zA-Z0-9]*")
```

Note that all parameterized arguments are optional. By default, there are no constraints.

##### Type Alias

Parameterized types can be given a name so modellers can then refer to these types.

``` Haskell
typeAlias PositiveInteger: number(fractionalDigits: 0, min: 0)
typeAlias AlphaNumericText: string(minLength: 1, pattern: "[a-zA-Z0-9]{1,4}")
```

Furthermore, type aliases can be used to improve readability; instead of displaying a verbose parameterized type declarations (e.g., `string(minLength: 1, pattern: "[a-zA-Z0-9]{1,4}")`), a type alias can be used (e.g.,`AlphaNumericText`).

The type aliases `PositiveInteger` and `AlphaNumericText` are both used in the example below.

``` Haskell
type DrivingLicence extends Person: <"Driving licence authorisation granted by a jurisdiction">
    countryofIssuance string (1..1)
        [metadata scheme]
    licenceNumber AlphaNumericText (1..1)
    dateofIssuance date (1..1)
    dateOfRenewal date (0..1)
    vehicleEntitlement VehicleClassificationEnum (0..*)
    penaltyPoints PositiveInteger (1..1)
```

#### Record Type

Rosetta defines two additional built-in types that are simple composites known as *record types*. The list is controlled by defining them as `recordType` at the language level.

- `date` - specified by combining a day, month and year
- `zonedDateTime` - combines a `date`, simple `time` and time-zone `string` specification to unambiguously refer to a single instant in time

Record types are different from more complex data types in that:

- they are pure data definitions and do not allow specification of validation logic in a [condition](#condition-statement).
- they are handled specially in the code-generators and so form part of the Rosetta DSL rather than any specific model.

{{< notice info "Note" >}}
As an alternative to `zonedDateTime`, a model may define a business centre time, where a simple `time` \"5:00:00\" is specified alongside a business centre. The simple time should be interpreted with the time-zone information of the associated business centre.
{{< /notice >}}

### Data Type

#### Purpose

A *data type* describes a logical concept of the business domain being modelled - also sometimes referred to as an *entity*, *object* or *class*. It is specified through a set of *attributes* defining the granular elements composing that concept - also sometimes referred to as *fields*.

Those model-specific data types are sometimes referred to as *complex types* by contrast with the basic types.

#### Syntax

A data type is defined using the keyword `type` and comprises:
- name - Required. By convention, uses the *PascalCase* (starting with a capital letter, also referred to as the *upper* [CamelCase](https://en.wikipedia.org/wiki/Camel_case)). Type names must be unique across a [namespace](#namespace-component). All those requirements are enforced by syntax validation.
- [description](#description) - Optional, recommended. A plain-text definition of the concept represented by the data type. All descriptions in the Rosetta DSL must be written as a string enclosed within angle brackets: `<"..">`.
- [annotations](#annotation) - Optional. Meta-data components that apply to the data type. All annotations in the Rosetta DSL are enclosed within square brackets `[...]`.
- [attributes](#attribute) - Optional.

``` Haskell
type <TypeName>: <"Description">
  [<annotation1>]
  [<annotation2>]
  [...]
  <attribute1>
  <attribute2>
  <...>
```

For example:

``` Haskell
type VehicleOwnership: <"Representative record of vehicle ownership">
  [metadata key]
  [rootType]
```

#### Attribute

A data type definition lists the attributes that compose this data type. Attributes are optional, so it is possible to model empty data types. When they are present, each attribute is defined by five components (three required and two optional):

- name - Required. Attribute names use the *camelCase* (starting with a lower case letter, also referred to as the *lower* camelCase). Attribute names must be unique within a data type.
- type - Required. Each attribute can be specified as either of the [data component](#data-component) types.
- cardinality - Required. Specifies the minimum and maximum allowed number of that attribute - see [cardinality](#cardinality).
- description - Optional, recommended. A plain-text [description](#description) of the attribute, in the context of the data type where it is used.
- annotations - Optional. Annotations such as [synonyms](#mapping-component) or [metadata](#meta-data-component) can be applied to attributes.

The syntax is:

``` Haskell
<attributeName> <AttributeType> (x..y) <"Description">
  [<annotation1>]
  [<annotation2>]
  [...]
```

For example:

``` Haskell
type Engine: <"Description of the engine.">
  engineType EngineTypeEnum (1..1) <"Type of engine.">
  power number (1..1) <"Break horse power.">
  mpg number (1..1) <"Miles per gallon.">
  emissionMetrics EmissionMetrics (1..1) <"List of emission metrics in grams per km.">
```

{{< notice info "Note" >}}
The Rosetta DSL does not use any delimiter to end definitions. All model definitions start with a similar opening keyword as `type`, so the start of a new definition marks the end of the previous one. For readability more generally, the Rosetta DSL looks to eliminate all the delimiters that are often used in traditional programming languages (such as curly braces `{` `}` or semi-colon `;`).
{{< /notice >}}

#### Inheritance

**The Rosetta DSL supports an inheritance mechanism**, when a data type (known as a *sub-type*) inherits all its behaviour and attributes from another data type (known as a *super-type*) and adds its own behaviour and set of attributes on top. Inheritance is supported by the `extends` keyword:

``` Haskell
type <SubType> extends <SuperType>
```

In the example below, `Vehicle` inherits all the attributes from the `VehicleFeature` data type and adds four other attributes to it:

``` Haskell
type Vehicle extends VehicleFeature:
   specification Specification (1..1)
   registrationID string (1..1)
   vehicleTaxBand VehicleTaxBandEnum (1..1)
   vehicleClassification VehicleClassificationEnum (1..1)
```

{{< notice info "Note" >}}
For clarity purposes, the documentation snippets omit the annotations and definitions that are associated with the data types and attributes, unless the purpose of the snippet is to highlight some of those features.
{{< /notice >}}

### Enumeration

#### Purpose

**Enumeration is the mechanism through which a type may only take some specific controlled values**. An *enumeration* is the container for the corresponding set of controlled values. An enumeration is sometimes also known as a *scheme*.

#### Syntax

Enumerations are very simple containers defined using the `enum` keyword, in similar way as other model components. The definition contains a plain-text description of the enumeration and the list of enumeration values.

``` Haskell
enum <EnumerationName>: <"Description">
  <Value1> (optional: displayName <"DisplayName">) <"Description">
  <Value2> <"Description">
  <...>
```

Enumeration names must be unique across a [namespace](#namespace-component). The Rosetta DSL naming convention uses the same upper CamelCase (PascalCase) as for data types. In addition the enumeration name should end with the suffix Enum. E.g.:

``` Haskell
enum PeriodEnum: <"The enumerated values to specify the period, e.g. day, week.">
  D <"Day">
  W <"Week">
  M <"Month">
  Y <"Year">
```

Enumeration values cannot start with a numerical digit, and the only special character that can be associated with them is the underscore `_`. In order to handle the integration of scheme values which can have special characters, the Rosetta DSL allows to associate a *display name* to any enumeration value. For those enumeration values, special characters are replaced with `_` while the `displayName` entry corresponds to the actual value.

An example is the day count fraction scheme for interest rate calculation, which includes values such as `ACT/365.FIXED` and `30/360`. These are associated as `displayName` to the `ACT_365_FIXED` and `_30_360` enumeration values, respectively.

``` Haskell
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
```

#### External Reference Data

In some cases, a model may rely on an enumeration whose values are already defined as a static dataset in some other data model, schema or technical specification. To avoid duplicating that information and risk it becoming stale, it is possible to annotate such enumeration with the source of the reference data, using the [document reference](#document-reference) mechanism. This ensures that the enumeration information in the model is kept up-to-date with information at the source.

This source information is usually accessible as a scheme under some URL. The document reference uses the `schemeLocation` segment to specify that URL, which instructs a model processor to import the enumeration values from that scheme. The syntax is:

``` Haskell
[docReference <Body> <Corpus> schemeLocation <"URL">]
```

For example, the set of floating rate indices maintained as part of the FpML standard can be referenced as follows:

``` Haskell
enum FloatingRateIndexEnum: <"The enumerated values to specify the list of floating rate index.">
    [docReference ISDA FpML_Coding_Scheme schemeLocation "http://www.fpml.org/coding-scheme/floating-rate-index-3-2"]

    // Those enumeration values are imported from the source scheme
    AED_EBOR_Reuters displayName "AED-EBOR-Reuters"
    <...>
```

### Meta-Type

The Rosetta DSL features some special types called *meta-types*, which are useful for its main application in the financial domain. Meta-types are designed to flag attributes that result from some functional logic. This enables a model implementation to identify where to stamp the output when running the corresponding functions.

There are two types of meta-types that are declared at the language level:

- `calculationType`: `calculation`, for calculation functions
- `qualifiedType`: `productType` and `eventType`, for object qualification functions

#### Calculation

The `calculation` type represents the outcome of a calculation. An attribute with the `calculation` type is meant to be associated to a [calculation function](#calculation-function), so that the attribute\'s type is implied by the function output.

An example usage is the conversion from clean price to dirty price for a bond:

``` Haskell
type CleanPrice:
  cleanPrice number (1..1)
  accruals number (0..1)
  dirtyPrice calculation (0..1)
```

#### Object Qualification

Similarly, `productType` and `eventType` represent the outcome of some qualification logic to classify an object (product or event) according to some taxonomy. Attributes of these types are meant to be associated to an [object qualification function](#object-qualification-function).

For example:

``` Haskell
type ProductIdentification:
  productQualifier productType (0..1) <"The CDM product qualifier, which corresponds to the outcome of the isProduct qualification logic. This value is derived by the CDM from the product payout features.">
  primaryAssetdata AssetClassEnum (0..1)
  secondaryAssetdata AssetClassEnum (0..*)
  productType string (0..*)
  productId string (0..*)
```

## Meta-Data Component

Meta-data are components that are designed to enrich other model components such as data types, attributes or functions.

### Description

#### Purpose

Plain-text descriptions can be associated to most model components. Although not generating any executable code, descriptions are first-class meta-data components of any model. As modelling best practice, a description ought to exist for every model component and be clear and comprehensive.

#### Syntax

The syntax to add a text description to a model component is to insert that description in quotation marks and in between angle brackets: `<"..">`.

Descriptions can be added to almost any model component such as:
- a data type, its attributes and its conditions,
- a function, its inputs and output, its conditions and its business logic,
- a reporting rule,
- a namespace,
- etc.

There are many examples throughout this documentation where the description has been included for the purpose of illustrating its use.

### Annotation

#### Purpose

Annotations allow to specify additional meta-data for model components beyond a simple description. Those annotation can serve several purposes:

- to add constraints to a model that may be enforced by syntax validation
- to modify the actual behaviour of a model in generated code
- purely syntactic, to provide additional guidance when navigating model components

Examples of annotations and their usage for different purposes are illustrated below.

#### Syntax

Annotations are defined with the `annotation` keyword:

``` Haskell
annotation <annotationName>: <"Description">
    <attribute1>
    <attribute2>
    <...>
```

The Rosetta DSL naming convention uses a (lower) camelCase for annotation names, which must be unique across a model. Attributes are optional and many annotations will not require any.

<a id='roottype-label'></a>

``` Haskell
annotation rootType: <"Mark a type as a root of the rosetta model">
```

{{< notice info "Note" >}}
Some annotations are provided as standard as part of the Rosetta DSL itself. Additional annotations can always be defined for any model.
{{< /notice >}}

Once an annotation is defined, model components can be annotated with its name and chosen attribute, if any, using the following syntax:

``` Haskell
[<annotationName> (optional: <annotationAttribute>)]
```

#### Meta-Data Annotation

The `metadata` annotation includes attributes that define a set qualifiers that can be applied to a data type or attribute. By default Rosetta includes several metadata annotations:

``` Haskell
annotation metadata:
  id string (0..1)
  key string (0..1)
  scheme string (0..1)
  reference string (0..1)
  template string (0..1)
  location string (0..1) <"Specifies this is the target of an internal reference">
  address string (0..1) <"Specified that this is an internal reference to an object that appears elsewhere">
```

Each attribute of the `metadata` annotation corresponds to a different qualifier:

- The `scheme` qualifier specifies a mechanism to control the set of values that an attribute can take, without having to define this attribute as an [enumeration](#enumeration) in the model. Typically, an attribute associated with a scheme is represented as a basic `string` type. The annotation indicates that the relevant scheme information should be sourced when populating the attribute.
- The `template` qualifier indicates that a data type is eligible to be used as a [data template](#data-template). Data templates provide a way to store data which may be duplicated across multiple objects into a single template, to be referenced by all these objects.
- the other metadata annotations are used for [cross-referencing](#cross-referencing).

### Document Reference

#### Purpose

A document reference is a specific type of annotation that links model components to external information published in a separate document. The Rosetta DSL allows to define any such external document, who owns it and some of its content as model components, and to associate this information to other model components such as data types or functions.

The external information may be published in text format, in which case this feature effectively associates a plain-text documentation trail to any model behaviour. As such behaviour may eventually be translated into an operational process run by software, this mechanism provides a form of self-documentation for that software.

Document references have two components: hierarchy and content.

#### Document Hierarchy Syntax

There are three keyword to define the hierarchy of document references:

1. `body` - an entity that is the author, publisher or owner of the referenced document
1. `corpus` - a document set that contains the referenced information
1. `segment` - the specific section in the document containing the information being referenced

The syntax to define a body, corpus and segment is, respectively:

``` Haskell
body <BodyType> <BodyName> (optional: <"Description">)
corpus <CorpusType> (optional: <Body>) (optional: <"Alias">) <CorpusName> (optional: <"Description">)
segment <segmentType>
```

Examples of bodies include regulatory authorities or trade associations.

``` Haskell
body Authority EuropeanCommission <"European Commission (ec.europa.eu).">
```

Examples of corpuses include rules and regulations, which may be specified according to different levels of detail such as directives and laws (as voted by lawmakers), regulatory texts and technical standards (as published by regulators), or best practice and guidance (as published by trade associations). In each case that corpus could be associated to the relevant body, itself defined as a model component.

While the name of a corpus provides a mechanism to refer to such corpus as a model component in other parts of a model, an alias provides an alternative identifier by which a given corpus may be known. In the below example, the alias refer to the official numbering of the document by the relevant authority.

``` Haskell
corpus Directive "93/59/EC" StandardEmissionsEuro1
    <"COUNCIL DIRECTIVE 93 /59/EEC of 28 June 1993 amending Directive 70/220/EEC on the approximation of the laws of the Member States relating to measures to be taken against air pollution by emissions from motor vehicles https://eur-lex.europa.eu/legal-content/EN/ALL/?uri=CELEX%3A31993L0059">
```

Corpuses are typically large document sets which may contain many provisions, clauses etc, so segments are used to refer to a specific section in a given document. Below are some examples of segment types, as are often found in regulatory texts or trade associations' best practices.

``` Haskell
segment article
segment whereas
segment annex
segment table
segment namingConvention
```

A segment is then invoked by associating some free-text name or number to identify that specifc segment:

``` Haskell
<segmentType> <"SegmentID">
```

Segments can be combine to point to a specific section in a document. For instance:

``` Haskell
article "26" paragraph "2"
```

#### Document Content Syntax

A reference to specific document content is created using the `docReference` keyword. This document reference must be associated to a corpus and segment defined according to the above document hierarchy. The `provision` keyword allows to copy the textual information being referenced from the document.

``` Haskell
[docReference <Body> <Corpus>
  <segment1> <segment2> <...>
  provision <"ProvisionText">]
```

In some instances, a data type may have a different naming convention based on the context in which it is being used: for example, a legal definition may be associated to a data type but with a different name. A document reference allows such data type to be annotated with a special `namingConvention` segment and the corresponding corpus and body that define it.

Such document reference may be associated to the data type itself or to any of its attributes.

``` Haskell
type PayerReceiver: <"Specifies the parties responsible for making and receiving payments defined by this structure.">
     payer CounterpartyRoleEnum (1..1)
       [docReference ICMA GMRA
         namingConvention "seller"
         provision "As defined in the GRMA Seller party ..."]
     <...>
```

### Data Template

When a data type is annotated as a template, it is possible to specify a reference to a template object. The template object, as well as any object that references it, are typically *incomplete* model objects that should not be validated individually. Once a template reference has been resolved, it is necessary to merge the template data to form a single fully populated object. Validation should only be performed once the template reference has been resolved and the objects merged together.

Other than the annotation itself, data templates do not have any impact on the model: they do not introduce any new type, attribute, or condition.

When a data type is annotated as a template, the designation applies to all encapsulated types in that data type. In the example below, the designation of template eligibility for `ContractualProduct` also applies to `EconomicTerms`, which is an encapsulated type in `ContractualProduct`, and likewise applies to all encapsulated types in `EconomicTerms`.

``` Haskell
type ContractualProduct:
  [metadata key]
  [metadata template]
  productIdentification ProductIdentification (0..1)
  productTaxonomy ProductTaxonomy (0..*)
  economicTerms EconomicTerms (1..1)
```

### Cross-Referencing

#### Purpose

Cross-referencing allows an attribute to refer to an object in a different location. A cross reference consists of a metadata identifier associated with an object (the *source*). Elsewhere an attribute (the *target*), instead of having a normal value, can hold that identifier as a reference metadata field.

#### Syntax

Cross-referencing uses the `key` (or `id`) / `reference` metadata pair. The `key` or `id` metadata annotations allow a key to be associated, respectively, to the source type or attribute being annotated. `id` is needed to annotate built-in types at the attribute level, since there is no data type to annotate.

In turn, a target attribute annotated with the `reference` metadata can be either a direct value like any other attribute, or replaced with a reference using a key. Syntax validation enforces that an attribute annotated as a reference is of a data type annotated with a key.

The syntax is:

``` Haskell
<SourceType>
  [metadata key]

// For built-in types only:
<sourceAttribute>
  [metadata id]

<targetAttribute>
  [metadata reference]
```

The below `Party` and `Identifier` types illustrate how cross-reference annotations and their relevant attributes can be used. The `key` qualifier associated to the `Party` type indicates that it is referenceable. In the `Identifier` type, the `reference` qualifier associated to the `issuerReference` attribute indicates that it can be provided as a reference via a key instead of a copy.

``` Haskell
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
```

Rosetta currently supports three different mechanisms for references, each with a different scope.

#### Global Reference

The `key` and `id` metadata annotations force a globally unique key to be generated for the type being annotated. The value of the key corresponds to a hash code to be generated by the model implementation. For the attribute annotated with a `reference`, the key need not be defined in the current object but can instead be a reference to an external object.

The global key and reference fields are called, respectively, `globalKey` and `globalReference` in the default implementation in Rosetta. The value of
the key is a *deep hash* that uses the complete set of attribute values that compose the data type and its attributes, recursively.

#### External Reference

Objects that are annotated with `key` or `id` can additionally be associated to a key that may be read from an external source - e.g. the FpML `id` metadata attribute. Attributes annotated with the `reference` keyword can store a reference from an external source. A reference resolver processor can be used to link up the references.

The example below features a `party` object with both a `globalKey` acting as a global identifier and an `externalKey` extracted from another source as meta-data. It also contains a `globalReference` which would resolve to that `party`.

``` JSON
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
        "globalReference" : "3fa8e998"
  }
```

#### Address and Location Reference

In some cases, an attribute may be used as a variable to be populated with different values to create different objects while other attributes are kept constant. This allows to reduce the storage of large, reusable objects which may have identical values for most of their attributes except for a few variable ones. A single object could be created that is parameterised by these variable attributes. Every individual instance only needs to specify the values of these parameters and does not need to copy the entire object.

The Rosetta DSL supports this use case with a cross-referencing mechanism that is based on pairing an address (for the placeholder containing the variable attribute in the object, i.e. the target) and a location (where the value of this attribute is specified, i.e. the source). The syntax uses the `address` / `location` metadata pair as follows:

``` Haskell
<targetAttribute>
  [metadata address "pointsTo"=<sourceAttribute>]

<sourceAttribute>
  [metadata location]
```

{{< notice info "Note" >}}
The global referencing mechanism cannot be applied to this use case because in that construct, a global key is uniquely associated to an object based on the set of values taken by its attributes. Here by contrast, the same unique key (the variable's address) needs to be used regardless of the value taken by that variable.
{{< /notice >}}

## Expression Component

**The Rosetta DSL offers a restricted set of language features to express simple logic**, such as simple operations and comparisons. The language is designed to be unambiguous and understandable by domain experts who are not software engineers while minimising unintentional behaviour. Simple expressions can be built up using operators to form more complex expressions.

{{< notice info "Note" >}}
The Rosetta DSL is not a *Turing-complete* language: e.g. it does not support looping constructs that can fail (e.g. the loop never ends), nor does it natively support concurrency or I/O operations.
{{< /notice >}}

Logical expressions are used within the following modelling components:

- [Functions](#function-component),
- [Validation conditions](#condition-statement),
- [Conditional mappings](#when-clause) and
- [Reporting rules](#rule-definition)

Expressions are evaluated within the context of a Rosetta object to return a result. The result of an expression can be either:

- a single value, which can itself be either of the available types: [built-in](#built-in-type), [complex](#data-type) or [enumeration](#enumeration)
- a [list](#list) of values, all of the same type from the above

The type of an expression is the type of the result that it will evaluate to. E.g. an expression that evaluates to True or False is of type `boolean`, an expression that evaluates to a `Party` is of type `Party`, etc.

The below sections detail the different types of Rosetta expressions and how they are used.

### Constant Expression

#### Basic Type Constant

An expression can be a [basic type](#basic-type) constant - e.g.:
- 2.0
- `True`
- "USD"

Such constant expressions are useful for comparisons to more complex expressions.

#### Enumeration Constant

An expression can refer to an enumeration value as follows:

```
<EnumName> -> <EnumValue>
```

E.g. :

```
DayOfWeekEnum -> SAT
```

#### List Constant

Constants can also be declared as lists:

```
[ <item1>, <item2>, <...>]
```

E.g.:

```
[1,2]
["A",B"]
[DayOfWeekEnum->SAT, DayOfWeekEnum->SUN]
```

### Path Expression

#### Purpose

A path expression is used to return the value of an attribute inside an object. Path expressions can be chained in order to refer to attributes located further down inside that object.

#### Syntax

The simplest path expression is just the name of an attribute. In the example below, the `before` attribute of a `ContractFormationPrimitive` object is checked for [existence](#comparison-operator) inside a [condition](#condition-statement) associated to that data type.

``` {.Haskell emphasize-lines="7"}
type ContractFormationPrimitive:

  before ExecutionState (0..1)
  after PostContractFormationState (1..1)

  condition: <"The quantity should be unchanged.">
      if before exists ....
```

The `->` operator allows to return an attribute located inside an attribute, and so on recursively.

```
<attribute1> -> <attribute2> -> <...>
```

In the example below, the penalty points of a vehicle owner's driving license is being extracted:

``` Haskell
owner -> drivingLicence -> penaltyPoints
```

{{< notice info "Note" >}}
In situations where the context of the object in which the path expression should be evaluated is not already specified (e.g. reporting rules or conditional mapping), the path should begin with the data type name e.g. `Owner -> drivingLicence`. Where applicable, this requirement is enforced by syntax validation in the Rosetta DSL.
{{< /notice >}}

#### Null

If a path expression is applied to an attribute that does not have a value in the object it is being evaluated against, the result is *null* - i.e. there is no value. If an attribute of that non-existant object is further evaluated, the result is still null.

In the above example, if `drivingLicense` is null, the final `penaltyPoints` attribute also evaluates to null.

A null value for an expression with multiple cardinality is treated as an empty [list](#list).

### Operator

#### Purpose

Rosetta supports operators that combine expressions into more complicated expressions. The language emulates the basic logic available in usual programming languages:

- conditional statements: `if`, `then`, `else`
- comparison operators: `=`, `<>`, `<`, `<=`, `>=`, `>`
- boolean operators: `and`, `or`
- arithmetic operators: `+`, `-`, `*`, `/`

#### Conditional Statement

Conditional statements consist of:

- an *if clause* with the keyword `if` followed by a boolean expression,
- a *then clause* with the keyword `then` followed by any expression and
- an optional *else clause* with the keyword `else` followed by any expression

If the *if clause* evaluates to True, the result of the *then clause* is returned by the conditional expression. If it evaluates to False, the result of the *else clause* is returned if present, else *null* is returned.

The type and cardinality of a conditional statement expression is the type and cardinality of the expression contained in the *then clause*. The Rosetta DSL enforces that the type of the *else clause* matches the *then clause*. Multiple *else clauses* can be added by combining `else if` statements ending with a final `else`.

#### Comparison Operator

The result type of a comparison operator is always a boolean.

- `=`, `<>` - equals or not equals. Equals returns true if the left expression is equal to the right expression, otherwise false, and conversely for not equals. Built-in types are equal if their values are equal. Complex types are equal if all of their attributes are equal, recursing down until all basic typed attributes are compared.
- `<`, `<=`, `>=`, `>` - performs mathematical comparisons on the left and right values of [comparable types](#comparable-type).
- `exists` - returns true if the left expression returns a result. This operator can be further modified with qualifying keywords:
  - `only` - the value of left expression exists and is the only attribute with a value in its parent object.
  - `single` - the value of expression either has single cardinality or is a list with exactly one value.
  - `mutiple` - the value expression has more than 2 results
- `is absent` - returns true if the left expression does not return a result.

The `only exists` syntax drastically reduces conditional expressions that check the exclusive existence of certain attributes, which would otherwise require to combine one `exists` with multiple `is absent` applied to all other attributes. It also makes the logic more robust to future model changes, where newly introduced attributes would need to be tested for `is absent`.

As shown in the below example, the `only exists` operator can apply to a composite set of attributes enclosed within brackets `(..)`. In this case, the operator returns true when all the attribues in the set have a value, and no other attribute in the parent object does.

``` Haskell
economicTerms -> payout -> interestRatePayout only exists
or (economicTerms -> payout -> interestRatePayout, economicTerms -> payout -> cashflow) only exists
```

{{< notice info "Note" >}}
This condition is typically applied to attributes of a type that implements a [`one-of`](#one-of) condition. In this case, the `only` qualifier is redundant with the `one-of` condition because only one of the attributes can exist. However, `only` makes the condition expression more explicit, and also robust to potential lifting of the `one-of` condition.
{{< /notice >}}

##### Comparable Type

All the following built-in types are *comparable*, which means that they can be used with mathematical comparison operators as long as both sides are of the same type:
- `int`
- `number`
- `date`
- `string`

##### Comparison Operator and Null

If an expression passed to an operator is of single cardinality and [null](#null), the behavior is as follows:

- null `=` *any value* returns false
- null `<>` *any value* returns true
- null `>` *any value* returns false
- null `>=` *any value* returns false

*Any value* here includes null. The behaviour is symmetric: if null appears on either side of the expression the result is the same.

#### Boolean Operator

`and` and `or` can be used to logically combine boolean typed expressions.

`(` and `)` can be used to group logical expressions. Expressions inside brackets are evaluated first.

#### Arithmetic Operator

Rosetta supports basic arithmetic operators

- `+` can take either two numerical types or two string typed expressions. The result is the sum of two numerical types or the concatenation of two string types
- `-`, `*`, `/` take two numerical types and respectively subtract, multiply and divide them to give a number result.

#### Operator Precedence

Expressions are evaluated in Rosetta in the following order, from first to last - see [Operator Precedence](https://en.wikipedia.org/wiki/Order_of_operations)).

1. RosettaPathExpressions - e.g. `Lineage -> executionReference`
1. Brackets - e.g. `(1+2)`
1. if-then-else - e.g. `if (1=2) then 3`
1. only-element - e.g. `Lineage -> executionReference only-element`
1. count - e.g. `Lineage -> executionReference count`
1. Multiplicative operators `*`, `/` - e.g. `3*4`
1. Additive operators `+`, `-` - e.g. `3-4`
1. Comparison operators `>=`, `<=`, `>`, `<` - e.g. `3>4`
1. Existence operators `exists`,`is absent`, `contains`, `disjoint` - e.g. `Lineage -> executionReference exists`
1. and - e.g. `5>6 and true`
1. or - e.g. `5>6 or true`

### List

A list is an ordered collection of items of the same data type (basic, complex or enumeration). A path expression that refers to an attribute with multiple [cardinality](#cardinality) will result in a list of values.

An expression that is expected to return a value with *multiple cardinality* will always evaluate to a list of zero or more elements, regardless of whether the result value contains a single or multiple elements. An expression that is expected to return multiple cardinality that returns null is considered to be equivalent to an empty list.

If a chained [path expression](#path-expression) contains multiple attributes with multiple cardinality, the result is a flattened list.

For example:

```
owner -> drivingLicence -> vehicleEntitlement
```

returns all the vehicle entitlements from all the owner's driving licenses into a single list.

The Rosetta DSL provides a number of list operators that feature in usual programming languages:

- Filter
- Map
- Reduce
- Compare

The following sections details the syntax and usage and these list operator features.

#### Filter

The `filter` keyword filters the items of a list based on a specified filtering criteria provided as a bolean expression. The syntax is:

``` Haskell
<list> filter (optional <itemName>) [ <booleanExpression> ]
```

For each list item, the boolean expression is evaluated to determine whether to include (if true) or exclude (if false) the item. The resulting filtered list is assigned as the value of the `filter` expression. By default, the keyword `item` is used to refer to the list item in the boolean expression.

Filtering a list does not change the item type - e.g. when filtering a list of `Vehicle`, the output list type is also of type `Vehicle`. The example below finds the subset of all vehicles with a given engine type:

``` Haskell
vehicles
    filter [ item -> specification -> engine -> engineType = engineType ]
```

Alternatively, the list item name can be a parameter, e.g. `vehicle` instead of `item`. The example below finds all vehicles with a maximum 0-60 mph (provided as a `zeroTo60` number):

``` Haskell
vehicles
    filter vehicle [ vehicle -> specification -> zeroTo60 < zeroTo60 ]
```

All list operations can contain expressions (e.g. if / else statements), call functions and also be chained together. The example below finds all vehicles within given emissions metrics:

``` Haskell
vehicles
    filter (if carbonMonoxideCOLimit exists then specification -> engine -> emissionMetrics -> carbonMonoxideCO <= carbonMonoxideCOLimit else True)
    then filter (if nitrogenOxydeNOXLimit exists then specification -> engine -> emissionMetrics -> nitrogenOxydeNOX <= nitrogenOxydeNOXLimit else True)
    then filter (if particulateMatterPMLimit exists then specification -> engine -> emissionMetrics -> particulateMatterPM <= particulateMatterPMLimit else True)
```

{{< notice info "Note" >}}
As the example above illustrates, usage of square brackets is optional, except in advanced usages such as nested operations where brackets are needed to avoid ambiguities.
{{< /notice >}}

List operations can also be nested within other list operations. The example below finds all vehicle owners within a maximum penalty point limit on any driver licence. Owners can have multiple licences issued by different countries.

``` Haskell
owners
    filter owner [ owner -> drivingLicence
        filter licence [ licence -> penaltyPoints > maximumPenaltyPoints ] count = 0
        ]
```

#### Extract

The `extract` keyword allows to modify the items of a list based on an expression. The syntax is:

``` Haskell
<list> extract (optional <itemName>) [ <expression> ]
```

For each list item, the expression is invoked to modify the item. The resulting list is assigned as the value of the `extract` expression. The example below filters a list of driving licenses to include only drivers with first and last names, then converts those driving licences into a list of names.

``` Haskell
drivingLicences
    filter firstName exists and surname exists
    then extract firstName + " " + surname
```

{{< notice info "Note" >}}
As the example above illustrates, usage of square brackets is optional, except in advanced usages such as nested operations where brackets are needed to avoid ambiguities.
{{< /notice >}}

#### Reduce

Reduction is an operation that returns a single value based on elements of a list. The general syntax is:

``` Haskell
<list> <reduceOperator> (optional <itemName>) (optional [ <operationExpression> ])
```

The Rosetta DSL implements the following set set of reduce operators:

- `sum` - returns the sum of the elements of a list of `int` or `number` elements
- `min`, `max` - returns the minimum or maximum of a list of [comparable](#comparable-type) elements
- `join` - returns the concatenated values of a list of `string`
- `reduce` - generic reduction operator, which requires to specify the operation to merge two elements.

The two examples below return, respectively, the sum and the highest number of a given list of numbers. They do not specify any expression parameter. In each case the output is a single number.


``` Haskell
numbers sum
```

``` Haskell
numbers max
```

The `min` and `max` keywords can also operate on complex, non-comparable types, provided that they include comparable attributes. In this case, the operator must specify an expression on which to perform the comparison. The `min` and `max` operators return the corresponding complex element, rather than the value on which the comparison is made.

The example below returns the vehicle with the highest power engine.

``` Haskell
vehicles
    max [ item -> specification -> engine -> power ]
```

For `join`, the operator can (optionally) specify a delimiter expression to insert in-between each string element. The below example concatenates a list of strings, separating each element with the given delimiter:

``` Haskell
strings join ", "
```

Reduction works by performing an operation to merge two adjacent elements of a list into one, and so on recursively until there is only one single element left. All the reduction operators above are therefore short-hand special cases of the generic `reduce` operator. The `reduce` operator must specify an expression that operates on two list elements.

The above example returning the vehicle with the highest power engine can be re-written as:

``` Haskell
vehicles
    reduce v1, v2 [
        if v1 -> specification -> engine -> power > v2 -> specification -> engine -> power then v1 else v2
        ]
```

#### List Comparison

The Rosetta DSL supports [comparison operators](#comparison-operator) to function on lists. Comparison operators are operators that always return a boolean value. The additional keywords needed to operate on lists are:

- `contains` - returns true if every element in the right hand expression is equal to an element in the left hand expression
- `disjoint` - returns true if no element in the left side expression is equal to any element in the right side expression
- (`all`/`any`) combined with comparison operators (`=`, `<>`, `<` etc.) - compares a list to a single element

If the `contains` operator is passed an expression that has single cardinality, that expression is treated as a list containing the single element or an empty list if the element is null.

For the comparison operators, if either the left or right expression has multiple cardinality then the other side should have multiple cardinality. Otherwise if one side's expression has single cardinality, `all` or `any` should be used to qualify the list on the other side. At present only `any` is supported for `<` and `>` and only `all` for the other comparison operators.

The semantics for list comparisons are as follows:

- `=`
  - if both sides are lists, returns true if all items are equal when e
  - if one side is single and `all` is specified, returns true if all items in the list are equal to the single value
  - if one side is single and `any` is specified, returns true if any Item in the list is equal to the single value (not implemented yet)
- `<>`
  - if both sides are lists, returns true if the lists have different lengths or all items are different when compared pairwise in the order of the lists
  - if one side is single and `any` is specified, returns true if any item in the list is different from the single value
  - if one side is single and `all` is specified, returns true if all items in the list are different from the single value (not implemented yet)
- `<`, `<=`, `>=`, `>`
  - if both sides are lists, returns true if the comparison returns true for every item when compared pairwise in the order of the lists
  - if one side is single and `all` is specified, returns true if the comparison returns true for all items in the list when compared to that single value
  - if one side is single and `any` is specified, returns true if the comparison returns true for any item in the list when compared to that single value (not implemented yet)

#### Other List Operator

Rosetta provides a number of additional operators that are specific to lists. For all these operators, the syntax enforces that the expression being operated on has multiple cardinality.

- `only-element` - provided that a list contains one and only one element, returns that element
- `count` - returns the number of elements in a list
- `first`, `last` - returns the first or last element of a list
- `flatten` - merges list elements into one single list, when those list elements are lists themeselves (and the syntax enforces that the expression being operated on is a list of a list)
- `sort` - for a list of [comparable](#comparable-type) elements, returns a list of those elements in sorted order
- `distinct` - returns a subset of a list containing only distinct elements (i.e. where the `<>` operator returns true) of that list

The `only-element` keyword imposes a constraint that the evaluation of the path up to this point returns exactly one value. If it evaluates to [null](#null), an empty list or a list with more than one value, then the expression result will be null:

```
observationEvent -> primitives only-element -> observation
```

The `distinct` operator is useful to remove duplicate elements from a list. It can be combined with other syntax features such as `count` to determine if all elements of a list are equal.

``` Haskell
owner -> drivingLicense -> countryofIssuance distinct count = 1
```

### Conversion operators

Rosetta provides five conversion operators: `to-enum`, `to-string`, `to-number`, `to-int`, and `to-time`. Here are examples of their usage.

Given the following enum
```
enum Foo:
    VALUE1
    VALUE2 displayName "Value 2"
```
- `Foo -> VALUE1 to-string` will result in the string `"VALUE1"`,
- `Foo -> VALUE2 to-string` will result in the string `"Value 2"`, (note that the display name is used if present)
- `"VALUE1" to-enum Foo` will result in the enum value `Foo -> VALUE1`,
- `"Value 2" to-enum Foo` will result in the enum value `Foo -> VALUE2`, (again, the display name is used if present)
- `"-3.14" to-number` will result in the number -3.14,
- `"17:05:33" to-time` will result in a value representing the local time 17 hours, 5 minutes and 33 seconds.

If the conversion fails, the result is an empty value. For example,
- `"VALUE2" to-enum Foo` results in an empty value, (because `Foo -> VALUE2` has a display name "Value 2", this conversion fails)
- `"3.14" to-int` results in an empty value.

## Data Validation Component

**Data integrity is supported by validation components that are associated to each data type** in the Rosetta DSL. There are two types of validation components:

- Cardinality
- Condition Statement

The validation components associated to a data type generate executable code designed to be executed on objects of that type. Implementors of the model can use the code generated from these validation components to build diagnostic tools that can scan objects and report on which validation rules were satisfied or broken. Typically, the validation code is included as part of any process that creates an object, to verify its validity from the point of creation.

### Cardinality

Cardinality is a data integrity mechanism to control how many of each attribute an object of a given type can contain. The Rosetta DSL borrows from other modelling languages that typically specify cardinality as `(x..y)`,  where `x` denotes the lower bound and `y` the upper bound for that attribute's number.

The lower and upper bounds can both be any integer number. A `0` lower bound means attribute is optional. A `*` upper bound means an unbounded attribute. `(1..1)` represents that there must be one and only one attribute of this type. When the upper bound is greater than 1, the attribute will be considered as a list, to be handled as such in any generated code.

For example:

``` Haskell
type Address:
  street string (1..*)
  city string (1..1)
  state string (0..1)
  country string (1..1)
    [metadata scheme]
  postalCode string (1..1)
```

A validation rule is generated for each attribute\'s cardinality constraint, so if the cardinality of the attribute does not match the requirement an error will be associated with that attribute by the validation process.

### Condition Statement

#### Purpose

*Conditions* are logic [expressions](#expression-component) associated to a data type. They are predicates on attributes of objects of that type that evaluate to True or False. As part of the object validation process, all the conditions are evaluated and if any evaluates to false then the validation fails.

#### Syntax

Condition statements are included in the definition of the type that they are associated to. They are usually appended after the definition of the type\'s attributes. The condition is defined by:

- a name
- a plain-text description - optional
- an [operator](#operator) that applies to the the type\'s attributes and returns a boolean.

``` Haskell
condition <ConditionName>: (optional: <"Description">)
  <booleanOperator>
```

The condition\'s name must be unique in the context of the type that it applies to, but it does not need to be unique across all data types of a given model.

``` Haskell
type ActualPrice:
   currency string (0..1)
      [metadata scheme]
   amount number (1..1)
   priceExpression PriceExpressionEnum (1..1)

   condition Currency: <"The currency attribute associated with the ActualPrice should not be specified when the price is expressed as percentage of notional.">
      if priceExpression = PriceExpressionEnum -> PercentageOfNotional
      then currency is absent
```

``` Haskell
type ConstituentWeight:
   openUnits number (0..1)
   basketPercentage number (0..1)
   condition BasketPercentage: <"FpML specifies basketPercentage as a RestrictedPercentage type, meaning that the value needs to be comprised between 0 and 1.">
      if basketPercentage exists
      then basketPercentage >= 0.0 and basketPercentage <= 1.0
```

{{< notice info "Note" >}}
Conditions are included in the definition of the data type that they are associated to, so they are aware of the context of that data type. This is why attributes of that data type can be directly used to express the validation logic, without the need to refer to the type itself.
{{< /notice >}}

### Choice Condition

The Rosetta DSL provides language features to handle the correlated existence or absence of attributes with regards to other attributes. Those use-cases were deemed frequent enough and handling them through basic boolean logic components would have created unnecessarily verbose, and therefore less readable, expressions.

#### Choice

A choice rule defines a mutual exclusion constraint between the set of attributes of a data type. A choice rule can be either:

- *optional*, represented by the `optional` keyword, when at most one of the attributes needs to be present, or
- *required*, represented by the `required` keyword, when exactly one of the attributes needs to be present

The syntax is:

``` Haskell
<choiceType> choice <attribute1>, <attribute2>, <...>
```

While a lot of choice rules may have two attributes, there is no limit to the number of attributes associated with it, within the limit of the number of attributes associated with the type. Members of a choice rule need to have their lower cardinality set to 0, which is enforced by syntax validation.

``` Haskell
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
```

``` Haskell
type AdjustableOrRelativeDate:
  [metadata key]

  adjustableDate AdjustableDate (0..1)
  relativeDate AdjustedRelativeDateOffset (0..1)

  condition Choice:
    required choice adjustableDate, relativeDate
```

{{< notice info "Note" >}}
Choice rules allow a simple and robust construct to translate the XML *xsd:choicesyntax*, although their usage is not limited to those XML use cases.
{{< /notice >}}

#### One-of

The Rosetta DSL supports the special case where a required choice logic applies to *all* the attributes of a given type, resulting in one and only one of them being present in any instance of that type. In this case, the `one-of` syntax provides a short-hand to by-pass the implementation of the corresponding choice rule.

This feature is illustrated below:

``` Haskell
type PeriodRange:
  lowerBound PeriodBound (0..1)
  upperBound PeriodBound (0..1)
  condition: one-of
```

## Function Component

**In programming languages, a function is a fixed set of logical instructions returning an output** which can be parameterised by a set of inputs (also known as *arguments*). A function is *invoked* by specifying a set of values for the inputs and running the instructions accordingly. In the Rosetta DSL, this type of component has been unified under a single *function* construct.

Functions are a fundamental building block to automate processes, because the same set of instructions can be executed as many times as required by varying the inputs to generate a different, yet deterministic, result.

Just like a spreadsheet allows users to define and make use of functions to construct complex logic, the Rosetta DSL allows to model complex processes from reusable function components. Typically, complex processes are defined by combining simpler sub-processes, where one process\'s output can feed as input into another process. Each of those processes and sub-processes are represented by a function. Functions can invoke other functions, so they can represent processes made up of sub-processes, sub-sub-processes, and so on.

Reusing small, modular processes has the following benefits:

- **Consistency**. When a sub-process changes, all processes that use the sub-process benefit from that single change.
- **Flexibility**. A model can represent any process by reusing existing sub-processes. There is no need to define each process explicitly: instead, we pick and choose from a set of pre-existing building blocks.

Where widely adopted industry processes already exist, they should be reused and not redefined. Some examples include:

- Mathematical functions. Functions such as sum, absolute, and average are widely understood, so do not need to be redefined in the model.
- Reference data. The process of looking-up through reference data is usually provided by existing industry utilities and a model should look to re-use it but not re-implement it.
- Quantitative finance. Many quantitative finance solutions, some open-source, already defines granular processes such as:
  - computing a coupon schedule from a set of parameters
  - adjusting dates given a holiday calendar

This concept of combining and reusing small components is also consistent with a modular component approach to modelling.

### Function Specification

#### Purpose

**Function specification components are used to define the processes applicable to a domain model** in the Rosetta DSL. A function specification defines the function\'s inputs and/or output through their types in the data model. This amounts to specifying the [API](https://en.wikipedia.org/wiki/Application_programming_interface) that implementors should conform to when building the function that supports the corresponding process.

Standardising those guarantees the integrity, inter-operability and consistency of the automated processes supported by the domain model.

#### Syntax

Functions are defined in a similar way as other model components and use the following:

- name
- plain-text description
- input and output attributes (the latter is mandatory)
- condition statements on inputs and output
- output construction statements

``` Haskell
func <FunctionName>: (optional: <"Description">)
  inputs:
    <attribute1>
    <attribute2>
    <...>
  output:
    <returnAttribute>

  (optional: <conditions>)
  (optional: <outputConstruction>)
```

The Rosetta DSL convention for a function name is to use a PascalCase (upper [CamelCase](https://en.wikipedia.org/wiki/Camel_case)) word. Function names need to be unique across all types of functions in a model and syntax validation is in place to enforce this.

{{< notice info "Note" >}}
The function syntax intentionally mimics the type syntax in the Rosetta DSL regarding the use of descriptions, attributes (inputs and output) and conditions, to provide consistency in the expression of model definitions.
{{< /notice >}}

#### Description

The role of a function must be clear for implementors of the model to build applications that provide such functionality. To better communicate the intent and use of functions, Rosetta supports multiple plain-text descriptions in functions. Descriptions can be provided for the function itself, for any input and output and for any statement block.

Look for occurrences of text descriptions in the snippets below.

#### Inputs and Output

Inputs and output are the function\'s equivalent of a type\'s attributes, and are defined using the same [attribute](#attribute) syntax with a name, type and cardinality.

Inputs are optional but at minimum, a function must specify its output attribute.

``` Haskell
func GetBusinessDate: <"Provides the business date from the underlying system implementation.">
   output:
     businessDate date (1..1) <"The provided business date.">
```

Most functions, however, also require inputs.

``` Haskell
func ResolveTimeZoneFromTimeType: <"Function to resolve a TimeType into a TimeZone based on a determination method.">
   inputs:
      timeType TimeTypeEnum (1..1)
      determinationMethod DeterminationMethodEnum (1..1)
   output:
      time TimeZone (1..1)
```

`inputs` is plural whereas `output` is singular, because a function may take several inputs but may only return one output. Inputs and outputs can both have multiple cardinality, in which case they will be treated as lists.

``` Haskell
func UpdateAmountForEachQuantity:
  inputs:
     priceQuantity PriceQuantity (0..*)
     amount number (1..1)
  output:
     updatedPriceQuantity PriceQuantity (0..*)
```

#### Condition

A function\'s inputs and output can be constrained using condition statements. A condition statement in a function can represent either:

- a **pre-condition**, using the `condition` keyword, applicable to inputs only and evaluated prior to executing the function, or
- a **post-condition**, using the `post-condition` keyword, applicable to inputs and output and evaluated after executing the function (once the output is known).

Each type of condition is expressed as a [condition statement](#condition-statement) evaluated against the function's inputs and/or output.

Conditions are an essential feature of the definition of a function. By constraining the inputs and output, they define the constraints that implementors of this function must satisfy, so that it can be safely used for its intended purpose as part of a process.

``` Haskell
func Create_VehicleOwnership: <"Creation of a vehicle ownership record file">
    inputs:
        drivingLicence DrivingLicence (0..*)
        vehicle Vehicle (1..1)
        dateOfPurchase date (1..1)
        isFirstHand boolean (1..1)
    output:
        vehicleOwnership VehicleOwnership (1..1)

    condition: <"Driving licence must not be expired">
        drivingLicence -> dateOfRenewal all > dateOfPurchase
    condition: <"Vehicle classification allowed by the driving licence needs to encompass the vehicle classification of the considered vehicle">
        drivingLicence->vehicleEntitlement contains vehicle-> vehicleClassification
    post-condition: <"The owner's driving license(s) must be contained in the vehicle ownership records.">
        vehicleOwnership -> drivingLicence contains drivingLicence
```

### Function Definition

**The Rosetta DSL allows to further define the business logic of a function**, by building the function output instead of just specifying the function\'s inputs and output. Because the Rosetta DSL only provides a limited set of language features, it is not always possible to fully define that logic in the DSL. The creation of valid output object can be fully or partially defined as part of a function specification, or completely left to the implementor.

- A function is *fully defined* when all validation constraints on the output object have been satisfied as part of the function specification. In this case, the code generated from the function expressed in the Rosetta DSL is fully functional and can be used in an implementation without any further coding.
- A function is *partially defined* when the output object\'s validation constraints are only partially satisfied. In this case, implementors will need to extend the generated code, using the features of the corresponding programming language to assign the remaining values on the output object.

A function must be applied to a specific use case to determine whether it is *fully* defined or only *partially* defined. The output object will be systematically validated when invoking a function, so all functions require the output object to be fully valid as part of any model implementation.

{{< notice info "Note" >}}
For instance in Java, a function specification that is only partially defined generates an *interface* that needs to be extended to be executable.
{{< /notice >}}

#### Output Construction

In the `Create_VehicleOwnership` example above, the `post-condition` statement asserts whether the vehicle ownership output is correctly populated by checking whether it contains the list of driving licenses passed as inputs. However, it does not directly populate that output, instead delegating its construction to implementors of the function. In that case the function is only specified but not fully defined.

Alternatively, the output can be built by directly assigning it a value. In this case, function implementors do not have to build this output themselves, so the corresponding `post-condition` is redundant and can be removed. The syntax to assign the output uses the `set` keyword as follows:

```
set <PathExpression>: <Expression>
```

The left-hand side [path expression](#path-expression) can be used to set individual attributes of the ouput object. The right-hand side [expression](#expression-component) allows to calculate that output value from the inputs.

The `Create_VehicleOwnership` example could be rewritten as follows:

``` Haskell
func Create_VehicleOwnership: <"Creation of a vehicle ownership record file">
    inputs:
        drivingLicence DrivingLicence (0..*)
        vehicle Vehicle (1..1)
        dateOfPurchase date (1..1)
        isFirstHand boolean (1..1)
    output:
        vehicleOwnership VehicleOwnership (1..1)

    set vehicleOwnership -> drivingLicence:
        drivingLicence
    set vehicleOwnership -> vehicle:
        vehicle
    set vehicleOwnership -> dateOfPurchase:
        dateOfPurchase
    set vehicleOwnership -> isFirstHand:
        isFirstHand
```

When the output is a list, `set` will override the entire list with the value returned by the expression, which also needs to be a list. The alternative `add` keyword allows to append an element to a list instead of overriding it. If the value of the expression is itself a list instead of a single element, the `add` keyword will append that list to the output.


``` Haskell
func AddDrivingLicenceToVehicleOwnership: <"Add new driving licence to vehicle owner.">
    inputs:
        vehicleOwnership VehicleOwnership (1..1)
        newDrivingLicence DrivingLicence (1..1)
    output:
        updatedVehicleOwnership VehicleOwnership (1..1)

    set updatedVehicleOwnership:
        vehicleOwnership
    add updatedVehicleOwnership -> drivingLicence: <"Add newDrivingLicence to existing list of driving licences">
        newDrivingLicence
```

``` Haskell
func GetDrivingLicenceNames: <"Get driver's names from given list of licences.">
    inputs:
        drivingLicences DrivingLicence (0..*)
    output:
        ownersName string (0..*)

    add ownersName: <"Filter lists to only include drivers with first and last names, then use 'map' to convert driving licences into list of names.">
        drivingLicences
            filter firstName exists and surname exists
            then extract firstName + " " + surname
```

{{< notice info "Note" >}}
The `assign-output` keyword also exists as an alternative to `set` and can be used with the same syntax. However, the `assign-output` keyword does not consistently treat single-cardinality (overrides the value) and list (appends the value) objects. It is therefore being phased out in favour of `set` and `add` that clearly separate those two cases.
{{< /notice >}}

**The Rosetta DSL supports a number of fully defined function cases**, where the output is being built up to a valid state:

- Object qualification
- Calculation
- Short-hand function

Those functions are typically associated to an annotation to instruct code generators to create concrete functions.

#### Object Qualification Function

**The Rosetta DSL supports the qualification of objects from their underlying components** according to a given classification taxonomy. This provides support for a composable model for those objects, which can be built based on re-usable components and classified accordingly.

An object qualification function evaluates a combination of assertions that uniquely characterise an input object according to a chosen classification. Each function is associated to a qualification name (a `string` from that classification) and returns a boolean. This boolean evaluates to True when the input satisfies all the criteria to be identified according to that qualification name.

Object qualification functions are associated to a `qualification` [annotation](#annotation), which has an attribute to specify which type of object is being qualified - e.g. product or business event in the example below.

``` Haskell
annotation qualification: <"Annotation that describes a func that is used for event and product Qualification">
  [prefix Qualify]
  Product boolean (0..1)
  BusinessEvent boolean (0..1)
```

The naming convention for a qualification function is: `Qualify_<QualificationName>`, where the qualification name uses the upper [CamelCase](https://en.wikipedia.org/wiki/Camel_case) (PascalCase). Where the qualification name may use other types of separators (like space or colon), they should be replaced by an underscore `_`. Syntax validation logic based on the qualification annotation is in place to enforce this.

``` Haskell
func Qualify_InterestRate_IRSwap_FixedFloat_PlainVanilla:
  [qualification Product]
  inputs: economicTerms EconomicTerms (1..1)
  output: is_product boolean (1..1)
```

#### Calculation Function

A calculation function defines a calculation output that is often, though not exclusively, of type `number`. It must end with a `set` (or `assign-output`) instruction that fully defines the calculation result.

Calculation functions are associated to the `calculation` [annotation](#annotation).

``` Haskell
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
```

#### Alias

The function syntax supports the definition of *aliases* that are only available in the context of the function. Aliases work like temporary variable assignments used in programming languages and are particularly useful in fully defined functions.

The above example builds an interest rate calculation using aliases to define the *calculation amount*, *rate* and *day count fraction* as temporary variables, and finally assigns the *fixed amount* output as the product of those three variables.

#### Short-Hand Function

Short-hand functions are functions that provide a compact syntax for operations that need to be frequently invoked in a model - for instance, model indirections where the corresponding path expression may be deemed too long or cumbersome:

``` Haskell
func PaymentDate:
  inputs: economicTerms EconomicTerms (1..1)
  output: result date (0..1)
  assign-output result: economicTerms -> payout -> interestRatePayout only-element -> paymentDate -> adjustedDate
```

which could be invoked as part of multiple other functions that use the `EconomicTerms` object by simply writing:

``` Haskell
PaymentDate( EconomicTerms )
```

### Function Call

#### Purpose

The Rosetta DSL allows to express a function call that returns the output of that function evaluation.

#### Syntax

A function call consists of the function name, followed by a comma-separated list of arguments enclosed within round brackets `(...)`:

```
<FunctionName>( <Argument1>, <Argument2>, ...)
```

The arguments list is a list of expressions. The number and type of the expressions must match the inputs defined by the function definition. This will be enforced by the syntax validator.

The type of a function call expression is the type of the output of the called function.

In the last line of the example below, the `Max` function is called to find the larger of the two `WhichIsBigger` function arguments, which is then compared to the first argument. The if expression surrounding this will then return \"A\" if the first argument was larger, \"B\" if the second was larger.

``` {.Haskell emphasize-lines="18"}
func Max:
    inputs:
        a number (1..1)
        b number (1..1)
    output:
        r number (1..1)
    assign-output r:
        if (a>=b) then a
        else b

func WhichIsBigger:
    inputs:
        a number (1..1)
        b number (1..1)
    output:
        r string (1..1)
    assign-output r:
        if Max(a,b)=a then "A" else "B"
```

## Namespace Component

### Namespace Definition

#### Purpose

The namespace syntax allows model artefacts in a data model to be organised into groups of namespaces. A namespace is an abstract container created to hold a logical grouping of model artefacts. The approach is designed to make it easier for users to understand the model structure and adopt selected components. It also aids the development cycle by insulating groups of components from model restructuring that may occur. Model artefacts are organised into a directory structure that follows the namespaces' Group and Artefact structure (a.k.a. "GAV coordinates"). This directory structure is exposed in the model editor.

By convention namespaces are organised into a hierarchy, with layers going from in to out. The hierarchy therefore contains an intrinsic inheritance structure where each layer has access to ("imports") the layer outside, and is designed to be usable without any of its inner layers. Layers can contain several namespaces ("siblings"), which can also refer to each other.

#### Syntax

The definition of a namespace starts with the `namespace` keyword, followed by the location of the namespace in the directory structure:

```
namespace cdm.product.common
```

The names of all components must be unique within a given namespace. Components can refer to other components in the same namespace using just their name. Components can refer to components outside their namespace either by giving the *fully qualified name* e.g. `cdm.base.datetime.AdjustableDate` or by importing the namespace into the current file.

To gain access to model components contained within another namespace the `import` keyword is used.

```
import cdm.product.asset.*
```

In the example above all model components contained within the cdm.product.asset namespace will be imported. Note, only components contained within the layer referenced will be imported, in order to import model components from namespaces embedded within that layer further namespaces need to be individually referenced.

```
import cdm.base.math.*
import cdm.base.datetime.*
import cdm.base.staticdata.party.*
import cdm.base.staticdata.asset.common.*
import cdm.base.staticdata.asset.rates.*
import cdm.base.staticdata.asset.credit.*
```

In the example above all model components contained within the layers of the `cdm.base` namespace are imported.

## Mapping Component

### Purpose

Mapping in Rosetta provides a mechanism for specifying how documents in other formats (e.g. FpML or ISDACreate) should be transformed into Rosetta documents. Mappings are specified as *synonym* annotations in the model.

Synonyms added throughout the model are combined to map the data tree of an input document into the output Rosetta document. The synonyms can be used to generate an *Ingestion Environment*, a java library which, given an input document, will output the resulting Rosetta document.

Synonyms are specified on the attributes of data type and the values of enum types.

### Basic Mapping

Basic mappings specify how a value from the input document can be directly mapped to a value in the resulting Rosetta document.

#### Synonym Source

First a *synonym source* is created. This can optionally extend a different synonym source `synonym source FpML_5_10 extends FpML` This defines a set of synonyms that are used to ingest a category of input document, in this case `FpML_5_10` documents.

##### Extends

A synonym source can extend another synonym source. This forms a new synonym source that has all the synonyms contained in the extended synonym source and can add additional synonyms as well as remove synonyms from it.

#### Basic Synonym

Synonyms are annotations on attributes of Rosetta types and the enumeration values of Rosetta Enums. The model does have some legacy synonyms remaining directly on rosetta types but the location of the synonym in the model has no impact. They can be written inside the definition of the type or they can be specified in a separate file to leave the type definitions simpler.

##### Inline

An inline synonym can be expressed next to the attribute being mapped as follows:

```
[synonym <SynonymSource> <SynonymBody>]
```

E.g.

```
type Engine:
    engineSpecification EngineSpecification(1..1)
        [synonym CONDITIONAL_SET_TO_EXAMPLE_1 value "engineDetail"]
```

##### External synonym

External synonyms are defined inside the synonym source declaration so the synonym keyword and the synonym source are not required in every synonym. A synonym is added to an attribute by referencing the type and attribute name and then declaring the synonym to add as the synonym body enclosed in square brackets `[..]`. The code below removes all the synonyms from the `fuel` attribute of `EngineSpecification`, then adds in a new synonym:

```
synonym source EXTERNAL_SYNONYM_EXAMPLE_8 extends EXTERNAL_SYNONYM_EXAMPLE_8_BASE_2
{
	EngineSpecification:
		- fuel
		+ fuel
			[value "combustible"]
}
```

#### Synonym Body

##### Value

The simplest synonym consists of a single value `[value "combustible"]`. This means that the value of the input attribute \"combustible\" will be mapped to the associated Rosetta attribute. If both the input attribute and the Rosetta attribute are basic types (string, number, date etc) then the input value will be stored in the appropriate place in the output document. If they are both complex types (with child attributes of their own) then the attributes contained within the complex type will be compared against synonyms inside the corresponding Rosetta type. If one is complex and the other is basic then a mapping error will be recorded.

##### Path

The value of a synonym can be followed with a path declaration. E.g.:

```
[synonym MULTI_CARDINALITY_EXAMPLE_2 value "combustible" path "engineDetail"]
```

This allows a path of input document elements to be matched to a single Rosetta attribute. In the example the contents of the xml path `engineDetail.combustible` will be mapped to the Rosetta attribute. Note that the path is applied as a suffix to the synonym value.

##### Maps 2

Mappings are expected to be one-to-one with each input value mapping to one Rosetta value. By default if a single input value is mapped to multiple Rosetta output values this is considered an error. However by adding the `maps 2` keyword this can be overridden allowing the input value to map to many output Rosetta values.

##### Meta

The `meta` keyword inside a synonym is used to map [metadata](#meta-data-annotation).

In the below example, the value of the "combustible" input will be mapped to the value of the `fuelType` attribute and the value of the "fuelTypeScheme" metadata associated to "combustible" will be mapped to the `scheme` metadata attribute.

```
type EngineSpecification:
    fuelType string (1..*)
        [metadata scheme]
        [synonym META_SCHEME_EXAMPLE_1 value "combustible" meta "fuelTypeScheme"]
```

#### Enumeration

A synonym on an enumeration provides mappings from the string values in the input document to the values of the enumeration. E.g. the FpML value `Hybrid` will be mapped to the enumeration value `EngineEnum.Hybrid` in Rosetta:

```
enum EngineEnum: <"The enumerated values for the natural person's role.">
    Hybrid
        [synonym CONDITIONAL_SET_TO_EXAMPLE_8 value "hybrid"]
```

##### External Enumeration Synonym

In an external synonym file, enumeration synonyms are defined in a block after the type attribute synonyms, preceded by the keyword `enums` :

```
enums

	EngineEnum:
		+ Hybrid
        		[value "Hybrid"]
```

### Advanced Mapping

The algorithm starts by *binding* the root of the input document to a pre-defined [root type](#roottype-label) in the model.

It then [recursively](https://en.wikipedia.org/wiki/Recursion_(computer_science)) traverses the input document.

Each step of the algorithm starts with the current attribute in the input document *bound* to a set of Rosetta objects in the output.

For each child attribute of the current input attribute, the rosetta attributes of the type of all Rosetta objects *bound* to the current attribute are checked for synonyms that match that child attribute. For each matching attribute a new Rosetta object instance is created and *bound* to that child attribute. The algorithm then recurses with the current child becoming the current input attribute.

When an input attribute has an associated value that value is set as the value of all the rosetta objects that are bound to the input attribute.

#### Hints

Hints are synonyms used to bypass a layer of rosetta without *consuming* an input attribute. They are required where an attribute has synonyms that would usually prevent the algorithm for searching down the Rosetta tree for attributes further down, but the current input element needs to still be available to match to synonyms.

e.g. :

```
type EngineSpecification:
    engineMetric EngineMetric (0..*)
        [synonym MULTI_CARDINALITY_EXAMPLE_12 value "capacityDetail"]
        [synonym MULTI_CARDINALITY_EXAMPLE_12 hint "combustible"]


type EngineMetric:
    fuel string (0..1)
        [synonym MULTI_CARDINALITY_EXAMPLE_12 value "combustible"]

```

In this example the input attribute \"capacityDetail\" is matched to the engineMetric and the children of \"capacityDetail\" will be matched against the synonyms for EngineMetric. However the input attribute \"combustible\" will also be matched to the engineMetric but \"combustible\" is still available to be matched against the synonyms of EngineMetric.

#### Merging inputs

Where a Rosetta attribute exists with multiple cardinality, to which more than one input element maps, synonyms can be used to either create a single instance of the Rosetta attribute that merges the input elements or to create multiple attributes - one for each input element. E.g. The synonyms :

```
engineSpecification EngineSpecification (0..*)
  [synonym MULTI_CARDINALITY_EXAMPLE_20 value "fuelDetail"]
  [synonym MULTI_CARDINALITY_EXAMPLE_20 value "capacityDetail"]
```

will produce two EngineSpecification objects. In order to create a single EngineSpecification with values from the attributes `fuelDetail ` and `capacityDetail` the synonym merging syntax should be used:

```
engineSpecification EngineSpecification (0..*)
  [synonym FpML_5_10 value fuelDetail, capacityDetail]
```

#### Conditional Mappings

Conditional mappings allow to build more complex mappings. Conditional mappings come in two types: *set to* and *set when*.

##### Set To Mapping

A set to mapping is used to set the value of an attribute to a constant value. They don\'t attempt to use any data from the input document as the value for the attribute and a synonym value must not be given. The type of the constant must be convertible to the type of the attribute. The constant value can be given as a string (converted as necessary) or an enum - e.g. :

```
engineEnum EngineEnum (0..1)
  [synonym CONDITIONAL_SET_TO_EXAMPLE_13 set to EngineEnum->Hybrid]
```

A set to mapping can be conditional on a [when clause](#when-clause) - e.g.:

```
engineEnum EngineEnum (0..1)
  [synonym CONDITIONAL_SET_TO_EXAMPLE_13 set to EngineEnum->Hybrid when "alternativeFuelDetail" exists and "fuelDetail" exists]
```

Multiple set to mappings can be combined in one synonym. They will be evaluated in the order specified with the first matching value used - e.g. :

```
engineSystem string (1..1)
  [synonym CONDITIONAL_SET_TO_EXAMPLE_6
    set to "Combustion" when "engineDetail->fuelDetail->combustible" = "Gasoline",
    set to "Electric" when "engineDetail->fuelDetail->combustible" = "Electricity",
    set to "Default"]
```

##### Set When Mapping

A set when mapping is used to set an attribute to a value derived from the input document if a given when clause is met -  e.g. :

```
alternativeFuelType string (0..1)
    [synonym CONDITIONAL_SET_EXAMPLE_5 value "complementaryEnergy" path "engineType->engineDetail" set when "engineType->engineDetail->complementaryEnergy" exists]
```

A set when synonym can include a default to set an attribute to a constant value when no other value was applicable - e.g. :

```
capacityUnit string (0..1)
  [synonym CONDITIONAL_DEFAULT_EXAMPLE_1 value "volumeCapacityUnit" path "engineType->engineDetail" default to "UK Gallon"]
```

#### When Clause

There are three types of *when* clauses: test expression, input path expression and output path expression.

##### Test Expression

A test expression consists of a synonym path and one of three types of test. The synonym path is from the mapping that is bound to this class.

- exists - tests whether a value with the given path exists in the input document
- absent - tests that a value with given path does not exist in the input document
- = or \<\> - tests if the value for the given path equals (or is not equal to) a constant value

e.g. :

```
capacityUnit string (0..1)	    
  [synonym CONDITIONAL_SET_EXAMPLE_2 value "volumeCapacityUnit" path "engineType->engineDetail" set when "engineType->engineDetail->powerUnit" exists]
capacityUnit string (0..1)
  [synonym CONDITIONAL_SET_EXAMPLE_3 value "volumeCapacityUnit" path "engineType->engineDetail" set when "engineType->engineDetail->powerUnit" is absent]
capacityUnit string (0..1)
  [synonym CONDITIONAL_SET_EXAMPLE_1 value "volumeCapacityUnit" path "engineType->engineDetail" set when "engineType->engineDetail->powerUnit" = "Cylinder"]
```

##### Input Path Expression

An input path expression checks the path through the input document that leads to the current value. The path provided can only be the direct path from the level above the current value in the input document. The condition evaluates to true when the current path is the given path:

```
volume string (0..1)
  [synonym CONDITIONAL_SET_EXAMPLE_6 value "capacity" set when path = "ukEngineVersion->terminology"]

```

##### Output Path Expression

An output path expression checks the path through the rosetta output object that leads to the current value. The path provided can start from any level in the output object. The condition evaluates to true when the current path ends with the given path.

e.g. :

```
fuelType string (1..1)
  [metadata scheme]
  [synonym CONDITIONAL_SET_EXAMPLE_16 value "combustible" path "engineDetail" set when "engineDetail->combustible->scheme"="petrolScheme" and rosettaPath = Root->engineSpecification->fuel->fuelType meta "scheme"]

```

#### Mapper

Occasionally the Rosetta mapping syntax is not powerful enough to perform the required transformation from the input document to the output document. In this case a *Mapper* can be called from a synonym :

```
fuelType string (0..1)
  // value updated by mapper
  [synonym MAPPERS_EXAMPLE_1 value "combustible" path "engineDetail->metric" mapper "Example1"]
```

When the ingestion is run a class called `Example1MappingProcessor` will be loaded and its mapping method invoked with the partially mapped Rosetta element. The creation of mapper classes is outside the scope of this document but the full power of the programming language can be used to transform the output.

#### Format

A date/time synonym can be followed by a format construct. The keyword `format` should be followed by a string. The string should follow a standardized [date format](https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html) - e.g. :

```
fabricationDate date (1..1)
	[synonym FORMAT_EXAMPLE_1 value "fabricationDate" dateFormat "MM/dd/yyyy"]
```

#### Pattern

A synonym can optionally be followed by a pattern construct. It is only applicable to enums and basic types other than date/times. The keyword `pattern` followed by two quoted strings. The first string is a [regular expression](https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html) used to match against the input value. The second string is a replacement expression used to reformat the matched input before it is processed as usual for the basictype/enum.

E.g. :

```
type EngineSpecification:
	guaranteePeriod int (1..1)
		[synonym FORMAT_EXAMPLE_1 value "guaranteePeriod" maps 2 pattern "([0-9])*.*" "$1"]
```

## Reporting Component

### Motivation

**One of the applications of the Rosetta DSL is to facilitate the process of complying with, and supervising, financial regulation** - in particular, the large body of data reporting obligations that industry participants are subject to.

The current industry processes to implement those rules are costly and inefficient. They involve translating pages of legal language, in which the rules are originally written, into business requirements which firms then have to code into their systems to support the regulatory data collection. This leads to a duplication of effort across a large number of industry participants and to inconsistencies in how each individual firm applies the rules, in turn generating data of poor quality and comparability for regulators.

By contrast, a domain-model for the business process or activity being regulated provides standardised, unambiguous definitions for business data at the source. In turn, these business data can be used as the basis for the reporting process, such that regulatory data become unambiguous views of the business data.

The Rosetta DSL allows to express those reporting rules as functional components in the same language as the model for the business domain itself. Using code generators, those functional rules are then distributed as executable code, for all industry participants to use consistently in their compliance systems.

### Regulatory Hierarchy

#### Purpose

One of the first challenges of expressing regulatory rules for the financial domain is to organise the content of the regulatory framework that mandates these rules. The financial industry is a global, highly regulated industry, where a single line of business or activity may operate across multiple jurisdictions and regulatory regimes. The applicable regulations can span thousands of pages of legal text with intricate cross-references.

#### Syntax

To organise such regulatory content within a model, the Rosetta DSL supports a number of syntax components that allow to refer to specific documents, their content and who owns them as direct model components. Those components are defined in the [document reference hierarchy](#document-hierarchy-syntax) section.

### Report Definition

#### Purpose

A report consists of an inter-connected set of regulatory obligations, which a regulated entity must implement to produce data as required by the relevant regulator.

Generically, the Rosetta DSL allows to specify any report using three types of rules:

- timing - when to report,
- eligibility - whether to report, and
- field - what to report.

A report is associated to an authoritative body and to the corpus(es) in which those rules are specified. Usually but not necessarily, the authority that mandates the rules also supervises their application and collects the data. Timing, eligibility and field rules translate into obligations of "timing, completeness and accuracy" of reporting, as often referred to by supervisors.

#### Syntax

A report is specified using the following syntax:

``` Haskell
report <Authority> <Corpus1> <Corpus2> <...> in <TimingRule>
  from <InputType>
  when <EligibilityRule1> and <EligibilityRule2> and <...>
  with type <ReportType>
```

The report type is istelf defined as a [data type](#data-type) component whose attributes are the reportable fields. Each attribute can be associated to a reporting rule containing the logic for extracting or calculating that field, using the `ruleReference` keyword:

``` Haskell
type <ReportType>:
  <field1> <Type1> (x..y)
    [ ruleReference <RuleName1> ]
  <...>
```

An example is given below.

``` Haskell
report EuropeanParliament EmissionPerformanceStandardsEU in real-time
    from ReportableEvent
    when EuroStandardsCoverage
    with type EmissionPerformanceStandardsReport
```

``` Haskell
type EmissionPerformanceStandardsReport:
    vehicleRegistrationID string (1..1)
        [ruleReference VehicleRegistrationID]
    firstRegistrationDate date (1..1)
        [ruleReference FirstRegistrationDate]
    vehicleClassificationType VehicleClassificationEnum (1..1)
        [ruleReference VehicleClassificationType]
    engineType EngineTypeEnum (1..1)
        [ruleReference EngineType]
    euroEmissionStandard string (1..1)
        [ruleReference EuroEmissionStandard]
    carbonMonoxide int (1..1)
        [ruleReference CarbonMonoxide]
```

To ensure a model's regulatory framework integrity, the authority, corpus and all the rules referred to in a report definition must exist as model components in the model's regulatory hierarchy. A report assembles all those components into a recipe, which firms can directly implement to comply with the data reporting requirement.

The next section describes how to define reporting rules as model components.

### Rule Definition

#### Purpose

The Rosetta DSL applies a functional approach to the process of regulatory reporting. A regulatory rule is a functional model component (*f*) that processes an input (*x*) through a set of logical instructions and returns an output (*y*), such that *y = f( x )*. A function can sometimes also be referred to as a *projection*. Using this terminology, the reported data (*y*) are considered projections of the business data (*x*).

For field rules, the output consists of the data to be reported. For eligibility rules, this output is a boolean that returns True when the input is eligible for reporting.

To provide transparency and auditability to the reporting process, the Rosetta DSL supports the development of reporting rules in both human-readable and machine-executable form.

- The functional expression of the reporting rules is designed to be readable by professionals with domain knowledge (e.g. regulatory analysts). It consists of a limited set of logical instructions, supported by a compact syntax.
- The machine-executable form is derived from this functional expression of the reporting logic using code generators, which directly translate it into executable code.
- In addition, the functional expression is explicitly tied to regulatory references, using the regulatory hierarchy concepts of body, corpus and segment to point to specific text provisions that support the reporting logic. This mechanism, coupled with the automatic generation of executable code, ensures that a reporting process that uses that code is fully auditable back to any applicable text.

#### Syntax

The syntax of a reporting rule is as follows:

``` Haskell
<ruleType> rule <RuleName> from <InputType>:
  [ regulatoryReference <Body> <Corpus>
    <Segment1>
    <Segment2>
    <SegmentN...>
    provision <"ProvisionText"> ]
  <FunctionalExpression>
```

The `<ruleType>` can be either `reporting` or `eligibility` (in which case it must return a boolean). The `regulatoryReference` syntax is the same as the `docReference` syntax documented in the [document reference](#document-reference) section. However it can only be applied to regulatory rules.

The functional expression of reporting rules uses the same [logical expression](#expression-component) components that are already available to define other modelling components, such as data validation or functions.

Functional expressions are composable, so a rule can also call another rule. When multiple rules may need to be applied for a single field or eligibility criteria, those rules can be specified in brackets separated by a comma, as illustrated below. Each of `Euro1Standard`, ..., `Euro6Standard` are themselves reporting rules.

``` Haskell
reporting rule EuroEmissionStandard from ReportableEvent:
   [regulatoryReference EuropeanCommission StandardEmissionsEuro6 article "1"  
    provision "Regulation (EC) No 715/2007 is amended as follows:..."]
    (
        Euro1Standard as "Emission Standards",
        Euro2Standard as "Emission Standards",
        Euro3Standard as "Emission Standards",
        Euro4Standard as "Emission Standards",
        Euro5Standard as "Emission Standards",
        Euro6Standard as "Emission Standards"
    )
```

In addition to those existing functional features, the Rosetta DSL provides specific reporting instruction components:

- extract
- filter
- repeat

Those components are documented in the next sections.

##### Extract Instruction

An extraction instruction defines a value to be either reported or used as input into another rule or instruction. The extraction keywords comprise:

- `extract`
- `then`
- `as`

The extraction syntax is:

``` Haskell
extract <Expression1>
then extract <Expression2>
<...>
(optional: as <"Label">)
```

The expressions may use any type of [expression component](#expression-component) available in the Rosetta DSL, from simple path expressions or constants to more complex conditional statements, as illustrated below:

``` Haskell
extract Vehicle -> specification -> dateOfFirstRegistration
```

``` Haskell
extract if
    Vehicle -> vehicleClassification = VehicleClassificationEnum -> M1_Passengers
    or Vehicle -> vehicleClassification = VehicleClassificationEnum -> M2_Passengers
    or Vehicle -> vehicleClassification = VehicleClassificationEnum -> M3_Passengers
    or Vehicle -> vehicleClassification = VehicleClassificationEnum -> N1I_Commercial
    or Vehicle -> vehicleClassification = VehicleClassificationEnum -> N1II_Commercial
    or Vehicle -> vehicleClassification = VehicleClassificationEnum -> N1III_Commercial
  then
    "MOrN1"
```

Extraction instructions can be chained using the keyword `then`, which means that extraction continues from the previous point. The syntax provides type safety when chaining extraction instructions: the output type of the preceding instruction must be equal to the input type of the following instruction.

``` Haskell
reporting rule VehicleForOwner:
    extract VehicleOwnership -> vehicle

reporting rule VehicleClassification:
    VehicleForOwner then extract Vehicle -> vehicleClassification
    // This is equivalent to writing directly:
    // extract VehicleOwnership -> vehicle -> vehicleClassification
```

An extraction instruction followed by `as` sets a label onto the value to appear as the column name in a computed report. The label is an arbitrary, non-functional string and should generally be aligned with the name of the reportable field as per the regulation.

``` Haskell
reporting rule FirstRegistrationDate: <"Date of first registration of the vehicle">
   extract VehicleOwnership -> vehicle -> specification -> dateOfFirstRegistration
    as "First Registration Date"
```

##### Filtering Rules

A filter instruction takes a list of input objects and return a subset of them. The output type of the rule is always the same as the input, and of multiple cardinality. The syntax is:

``` Haskell
filter when <FunctionalExpression>
```

The `filter when` keyword takes each input value and uses it as input to a provided test expression. The result type of the test expression must be boolean and its input type must be the input type of the filter rule. If the expression returns true for a given input, that value is included in the output.

The functional expression can be either a direct boolean expression or the output of another rule whose output is a boolean, in which case the syntax is:

``` Haskell
filter when rule <RuleName>
```

Filter expressions can be combined with extraction expressions. The example below extracts the date of first registration for a list of passenger-type vehicles only:

``` Haskell
extract VehicleOwnership -> vehicle
filter when Vehicle -> vehicleClassification = VehicleClassificationEnum -> M1_Passengers
    or Vehicle -> vehicleClassification = VehicleClassificationEnum -> M2_Passengers
    or Vehicle -> vehicleClassification = VehicleClassificationEnum -> M3_Passengers
extract Vehicle -> specification -> dateOfFirstRegistration
```

That example can be rewritten as:

``` Haskell
extract VehicleOwnership -> vehicle
filter when rule VehicleIsM
extract Vehicle -> specification -> dateOfFirstRegistration
```

where the filtering rule itself is defined as:

``` Haskell
reporting rule VehicleIsM:
  extract
    Vehicle -> vehicleClassification = VehicleClassificationEnum -> M1_Passengers
      or Vehicle -> vehicleClassification = VehicleClassificationEnum -> M2_Passengers
      or Vehicle -> vehicleClassification = VehicleClassificationEnum -> M3_Passengers
```

##### Repeat Instruction

The syntax also supports the reporting of *repeatable* fields, when such fields can be of multiple, variable cardinality depending on the scenario. The `repeatable` keyword specifies that a set of extract instruction must be reported as a repeatable set of fields, based on a starting point with multiple cardinality.

The syntax is:

``` Haskell
extract repeatable <ExpressionWithMultipleCardinality> then ( <ExtractInstruction1>, <ExtractInstructuction2>, <...> )
```

For example, in the CFTC Part 45 regulations, fields 33-35 require the reporting of a notional quantity schedule. For each quantity schedule step, the notional amount, effective date and end date must be reported.

In the example below, the `repeatable` keyword in reporting rule `NotionalAmountScheduleLeg1` specifies that the extracted list of quantity notional schedule steps should be reported as a repeating set of data. The rules specified within the brackets define the fields that should be reported for each repeating step.

``` Haskell
reporting rule NotionalAmountScheduleLeg1 from ReportableEvent: <"Notional Amount Schedule">
   [regulatoryReference CFTC Part45 appendix "1" item "33-35" field "Notional Amount Schedule"
       provision "Fields 33-35 are repeatable and shall be populated in the case of derivatives involving notional amount schedules"]
   TradeForEvent then
       InterestRateLeg1 then
           extract repeatable InterestRatePayout -> payoutQuantity -> quantitySchedule -> stepSchedule -> step then
           (
               NotionalAmountScheduleLeg1Amount,
               NotionalAmountScheduleLeg1EndDate,
               NotionalAmountScheduleLeg1EffectiveDate
           )

reporting rule NotionalAmountScheduleLeg1Amount from ReportableEvent: <"Notional amount in effect on associated effective date of leg 1">
   [regulatoryReference CFTC Part45 appendix "1" item "33" field "Notional amount in effect on associated effective date of leg 1"]
       CDENotionalAmountScheduleAmount
       as "33/35-$ 33 Notional amount leg 1"

reporting rule NotionalAmountScheduleLeg1EffectiveDate from ReportableEvent: <"Effective date of the notional amount of leg 1">
   [regulatoryReference CFTC Part45 appendix "1" item "34" field "Effective date of the notional amount of leg 1"]
       CDENotionalAmountScheduleEffectiveDate
       as "33/35-$ 34 Effective date leg 1"

reporting rule NotionalAmountScheduleLeg1EndDate from ReportableEvent: <"End date of the notional amount of leg 1">
   [regulatoryReference CFTC Part45 appendix "1" item "35" field "End date of the notional amount of leg 1"]
       CDENotionalAmountScheduleEndDate
       as "33/35-$ 35 End date leg 1"
```

{{< notice info "Note" >}}
The `-$` symbol in the label is used to index the repeatable group, to ensure that they appear in logical order in the report.
{{< /notice >}}
