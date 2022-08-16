---
title: "Rosetta DSL Overview"
date: 2022-02-09T00:38:25+09:00
description: "Rosetta is a Domain-Specific Language (DSL) designed for the financial industry. Its purpose is to support the modelling of the industry's various operational processes (data formats, business logic, validation rules, etc.) to promote the consistency and inter-operability of their implementations."
draft: false
weight: 1
---

# Overview of the Rosetta DSL 

**Continuous Integration:** [![Codefresh build status](https://g.codefresh.io/api/badges/pipeline/regnosysops/REGnosys%2Frosetta-dsl%2Frosetta-dsl?branch=master&key=eyJhbGciOiJIUzI1NiJ9.NWE1N2EyYTlmM2JiOTMwMDAxNDRiODMz.ZDeqVUhB-oMlbZGj4tfEiOg0cy6azXaBvoxoeidyL0g&type=cf-1)](https://g.codefresh.io/pipelines/rosetta-dsl/builds?repoOwner=REGnosys&repoName=rosetta-dsl&serviceName=REGnosys%2Frosetta-dsl&filter=trigger:build~Build;branch:master;pipeline:5d148a0543bba039bd196117~rosetta-dsl)

**License:** [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0)

## What is the Rosetta DSL

*Rosetta* is a Domain-Specific Language (DSL) designed for the financial industry. It is designed to support the modelling of the industry's various operational processes (data formats, business logic, validation rules, etc.) to promote the consistency and inter-operability of their implementations. In software engineering, a [domain model](https://en.wikipedia.org/wiki/Domain_model) is a conceptual model of the domain that incorporates both *data* and *behaviour* (i.e. rules and processes).

The key idea behind the Rosetta DSL is that, whilst financial markets' operational infrastructure is largely electronified, the current technology implementation has two unappealing characteristics:

- **Variety of data representations**. The plurality of data standards (the main ones being FIX, FpML, ISO 20022 and EFET) is compounded by the many variations in the implementation of those, to which we need to add a wide range of proprietary data representations.
- **Limited availability of native digital tools** that would allow those data representations to be directly translated into executable code. Even the protocols that have a native digital representation (e.g. FpML and FIXML, which are available in the form of XML schemas) have associated specifications artefacts which require further manual specification and/or coding in order to result in a complete executable solution. In FpML, this is the case of the associated validation rules. In FIX, an example of such are the Recommended Practices/Guidelines, which are only available in the form of PDF documents.

**The Rosetta DSL addresses those shortcomings by enabling the representation of data and business logic** to be consolidated into cohesive domain models (hence the naming reference to the Rosetta Stone). A model expressed in the Rosetta DSL provides more than a technical specification: it is automatically translated into executable code using code generators, so it can directly be used as part of an implementation. Both the Rosetta DSL and associated code generators are available in open source.

The ISDA Common Domain Model (CDM) is the first live application of the Rosetta DSL. It provides a blueprint for the lifecycle events and processes of derivative transactions. The CDM is available in open source (subject to an ISDA licence) to all industry participants, hence the name *common*. It is openly accessible through the [Rosetta SDK](#the-rosetta-sdk), where users can view, edit, test and contribute to it, or the [CDM Portal](https://portal.cdm.rosetta-technology.io).

For more details, please consult the [ISDA CDM documentation](https://cdm.docs.rosetta-technology.io) or contact ISDA directly at <marketinfrastructureandtechnology@isda.org>.

## Rosetta DSL Components

The Rosetta DSL comprises 2 components, both open-source:

- *Syntax* - defines the language and rules for editing a model using the Rosetta DSL, also known as a *grammar*
- *Code generators* - from a model expressed in the Rosetta DSL, automatically generates executable code in other programming languages

### Syntax

The [Rosetta DSL repository](https://github.com/REGnosys/rosetta-dsl/) contains the definition of the language. It is based on the [Eclipe Modelling Framework](https://www.eclipse.org/modeling/emf/).

The language components available in the Rosetta DSL and their syntax are detailed in the [Rosetta Modelling Components](https://docs.rosetta-technology.io/rosetta/rosetta-dsl/rosetta-modelling-component/) section of the documentation.

A [demonstration model](https://github.com/rosetta-models/demo), also available in open source, provides a set of working examples of those modelling components. Snippets extracted from this model are being used to support the DSL documentation.

### Code Generator

Code generators remove the need for software developers to translate the model specifications into executable code while ensuring the inter-operability of different implementations. The Rosetta DSL repository provides one default code generator, for [Java](https://www.oracle.com/java/).

To make models agnostic to the technology platform in which they are being implemented, other code generators have been provided in a variety of languages. A separate [code generator repository](https://github.com/REGnosys/rosetta-code-generators), also open source, allows the community to create and share code generators in potentially any software language.

The [Code Generator documentation](https://docs.rosetta-technology.io/rosetta/rosetta-dsl/rosetta-code-generators/) details the available code generators, the code generation mechanism and how to write and test one.

## The Rosetta SDK

A complete *Software Development Kit (SDK)* also called [Rosetta](https://ui.rosetta-technology.io/) is provided to help industry participants to create, edit or extend models using the Rosetta DSL. Rosetta also provides integration tools designed to facilitate firms' adoption and implementation of models within their own technology architecture.

Much like how software engineers use programming languages and tools to build and test software, it is useful to think of Rosetta as a platform with a set of tools to build and test a domain model using the Rosetta DSL. The [Rosetta products documentation](https://docs.rosetta-technology.io/rosetta/rosetta-products/) details the various tools and products that are available in the Rosetta platform.

In order to facilitate the use of the Rosetta DSL by industry members, a *Community edition* of Rosetta that already features many of the platform's functionalities is available as a free web application. Through Rosetta, users can also access a number of open-source [modelling projects](https://docs.rosetta-technology.io/rosetta/projects/) that are based on the Rosetta DSL, allowing them to use, edit or extend those models.
