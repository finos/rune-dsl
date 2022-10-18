---
title: "Rosetta DSL Overview"
date: 2022-02-09T00:38:25+09:00
description: "Rosetta is a Domain-Specific Language (DSL) that supports the modelling of operational processes for the financial markets' industry. Its purpose is to promote consistency and inter-operability between the various implementations of these processes."
draft: false
weight: 1
---

# Overview of the Rosetta DSL 

**Continuous Integration:** [![Codefresh build status](https://g.codefresh.io/api/badges/pipeline/regnosysops/REGnosys%2Frosetta-dsl%2Frosetta-dsl?branch=master&key=eyJhbGciOiJIUzI1NiJ9.NWE1N2EyYTlmM2JiOTMwMDAxNDRiODMz.ZDeqVUhB-oMlbZGj4tfEiOg0cy6azXaBvoxoeidyL0g&type=cf-1)](https://g.codefresh.io/pipelines/rosetta-dsl/builds?repoOwner=REGnosys&repoName=rosetta-dsl&serviceName=REGnosys%2Frosetta-dsl&filter=trigger:build~Build;branch:master;pipeline:5d148a0543bba039bd196117~rosetta-dsl)

**License:** [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0)

## What is the Rosetta DSL

*Rosetta* is a Domain-Specific Language (DSL) that supports the modelling of operational processes for the financial markets' industry. Its purpose is to promote consistency and inter-operability between the various implementations of these processes.

{{< notice info "Note" >}}
In software engineering, a [domain model](https://olegchursin.medium.com/a-brief-introduction-to-domain-modeling-862a30b38353) is a conceptual model of a business domain that incorporates both *data* and *logic* (i.e. rules and processes).
{{< /notice >}}

The key idea behind the Rosetta DSL is that, whilst financial markets' operational infrastructure is largely electronified, many of its underlying IT systems tend to operate in silos.

For instance, the same data are often represented differently between different applications - usually a reasonable choice when considering the respective purpose of each application. But without any formalised translation between them, data cannot easily flow from one application to another and the overall architecture looses cohesiveness. Applications also tend to mix the specification of their business logic with its technical implementation. Once buried in code, an application's logic is hard to extract and must usually be documented separately, with no guarantee of consistency.

**The Rosetta DSL allows to represent data and business logic in a system- and technology-agnostic way** into a cohesive domain model. By supporting a shared, formalised understanding of the financial markets' domain, it enables different technology implementations to "talk" to each other in the same native language (hence the naming reference to the Rosetta Stone).

A model expressed in the Rosetta DSL provides more than a technical specification: it automatically generates executable code, to be used directly in an implementation. Both the Rosetta DSL and associated code generators are available in open source.

**One important application of the Rosetta DSL concerns regulatory reporting**. While many financial institutions share the same reporting obligations, they usually implement their logic in slightly different ways because of siloed technology approaches. This exposes firms to non-compliance risk and fines and degrades the quality and comparability of the data that regulators collect.

Instead, Rosetta empowers many users within firms to take part in interpreting and codifying reporting rules, without the risk of loss-in-translation once they get implemented in IT systems. The language itself is designed to be human-readable, so that domain experts without programming experience (e.g. operations or compliance professionals) can write fully functional regulatory logic directly – a bit like in Excel.

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

In order to facilitate the use of the Rosetta DSL by industry members, a *Community Edition* of Rosetta that already features many of the platform's functionalities is available as a free web application. Through Rosetta, users can also access a number of open-source [modelling projects](https://docs.rosetta-technology.io/rosetta/projects/) that are based on the Rosetta DSL, allowing them to use, edit or extend those models.

## Developer setup

If you want to contribute to the Rosetta DSL and need to get things up and running, see [this guide](setup-for-developers.md).
