---
title: "Rune DSL Overview"
date: 2022-02-09T00:38:25+09:00
description: "Rune is a Domain-Specific Language (DSL) that supports the modelling of operational processes for the financial markets' industry. Its purpose is to promote consistency and inter-operability between the various implementations of these processes."
draft: false
weight: 1
---

# Rune DSL

[![FINOS - Incubating](https://cdn.jsdelivr.net/gh/finos/contrib-toolbox@master/images/badge-incubating.svg)](https://community.finos.org/docs/governance/Software-Projects/stages/incubating)

**Continuous Integration:** [![Maven Central](https://img.shields.io/maven-central/v/com.regnosys.rosetta/com.regnosys.rosetta.parent.svg?maxAge=2592000)](https://search.maven.org/#artifactdetails%7Ccom.regnosys.rosetta%7Ccom.regnosys.rosetta.parent%7C2%7Cpom)

*Rune DSL* is a Domain-Specific Language (DSL) that supports the modelling of operational processes for the financial markets' industry. Its purpose is to promote consistency and inter-operability between the various implementations of these processes.

{{< notice info "Note" >}}
In software engineering, a [domain model](https://olegchursin.medium.com/a-brief-introduction-to-domain-modeling-862a30b38353) is a conceptual model of a business domain that incorporates both *data* and *logic* (i.e. rules and processes).
{{< /notice >}}

The key idea behind the Rune DSL is that, whilst financial markets' operational infrastructure is largely electronified, many of its underlying IT systems tend to operate in silos.

For instance, the same data are often represented differently between different applications - usually a reasonable choice when considering the respective purpose of each application. But without any formalised translation between them, data cannot easily flow from one application to another and the overall architecture looses cohesiveness. Applications also tend to mix the specification of their business logic with its technical implementation. Once buried in code, an application's logic is hard to extract and must usually be documented separately, with no guarantee of consistency.

**The Rune DSL allows to represent data and business logic in a system- and technology-agnostic way** into a cohesive domain model. By supporting a shared, formalised understanding of the financial markets' domain, it enables different technology implementations to "talk" to each other in the same native language.

A model expressed in the Rune DSL provides more than a technical specification: it automatically generates executable code, to be used directly in an implementation. Both the Rune DSL and associated code generators are available in open source.

**One important application of the Rune DSL concerns regulatory reporting**. While many financial institutions share the same reporting obligations, they usually implement their logic in slightly different ways because of siloed technology approaches. This exposes firms to non-compliance risk and fines and degrades the quality and comparability of the data that regulators collect.

Instead, Rune empowers many users within firms to take part in interpreting and codifying reporting rules, without the risk of loss-in-translation once they get implemented in IT systems. The language itself is designed to be human-readable, so that domain experts without programming experience (e.g. operations or compliance professionals) can write fully functional regulatory logic directly – a bit like in Excel.

## Rosetta

A complete end to end development environment called [Rosetta](https://ui.rosetta-technology.io/) is provided to help industry participants to create, edit or extend models using the Rune DSL. Rosetta also provides integration tools designed to facilitate firms' adoption and implementation of models within their own technology architecture.

Much like how software engineers use programming languages and tools to build and test software, it is useful to think of Rosetta as a platform with a set of tools to build and test a domain model using the Rune DSL. The [Rosetta products documentation](https://docs.rosetta-technology.io/rosetta/rosetta-products/) details the various tools and products that are available in the Rosetta platform.

In order to facilitate the use of the Rune DSL by industry members, a *Community Edition* of Rosetta that already features many of the platform's functionalities is available as a free web application. Through Rosetta, users can also access a number of open-source [modelling projects](https://docs.rosetta-technology.io/rosetta/projects/) that are based on the Rune DSL, allowing them to use, edit or extend those models.

## Rune DSL Components

The Rune DSL comprises 2 components, both open-source:

- *Syntax* - defines the language and rules for editing a model using the Rune DSL, also known as a *grammar*
- *Code generators* - from a model expressed in the Rune DSL, automatically generates executable code in other programming languages

### Syntax

The [Rune DSL repository](https://github.com/finos/rune-dsl/) contains the definition of the language. It is based on the [Eclipe Modelling Framework](https://www.eclipse.org/modeling/emf/).

The language components available in the Rune DSL and their syntax are detailed in the [Rune Modelling Components](https://docs.rosetta-technology.io/rosetta/rosetta-dsl/rosetta-modelling-component/) section of the documentation.

A [demonstration model](https://github.com/rosetta-models/demo), also available in open source, provides a set of working examples of those modelling components. Snippets extracted from this model are being used to support the DSL documentation.

### Code Generator

Code generators remove the need for software developers to translate the model specifications into executable code while ensuring the inter-operability of different implementations. The Rune DSL repository provides one default code generator, for [Java](https://www.oracle.com/java/).

To make models agnostic to the technology platform in which they are being implemented, other code generators have been provided in a variety of languages. A separate [code generator repository](https://github.com/REGnosys/rosetta-code-generators), also open source, allows the community to create and share code generators in potentially any software language.

The [Code Generator documentation](https://docs.rosetta-technology.io/rosetta/rosetta-dsl/rosetta-code-generators/) details the available code generators, the code generation mechanism and how to write and test one.

## Development setup

### Setup for developers
This guide is meant for everyone who wants to contribute to the Rune DSL and needs to get things up and running.

If this guide does not work for you, be sure to raise an issue. This way we can help you figure out what the problem is and update this guide to prevent the same problem for future users.

### 1. Building with Maven
Start by cloning the project: `git clone https://github.com/finos/rune-dsl`

Our project runs with Java 17. Make sure that your Maven also uses this version of Java by running `mvn -v`.

To build the project, run `mvn clean install`.

### 2. Setting things up in Eclipse
#### Install Eclipse IDE for Java and DSL Developers
Install the latest version of the "Eclipse IDE for Java and DSL Developers" using the [Eclipse Installer](https://www.eclipse.org/downloads/packages/installer).

#### Install the Checkstyle plugin
We use [Checkstyle](https://checkstyle.sourceforge.io/) for enforcing good coding practices. The Eclipse plugin for Checkstyle can be found here: [https://checkstyle.org/eclipse-cs/#!/](https://checkstyle.org/eclipse-cs/#!/).

#### Setup the project
1. **Open the project in Eclipse**: File > Open Projects from File System..., select the right folder, click Finish.
2. **Update Maven dependencies**: right click on the `com.regnosys.rosetta.parent` project > Maven > Update project... and finish.

##### Troubleshooting
Make sure you have successfully run `mvn clean install`. (see section 1 of this guide)

If you're seeing 1000+ errors in the "Problems" window of Eclipse, try the following.
1. Disable auto-building. (Project > Build automatically)
2. Close Eclipse and open it again.
3. Update Maven dependencies again.
4. Re-enable auto-building.

### 3. Setting things up in Intellij
Support for developing Xtext projects in Intellij is limited. It has no support for
- editing `Xtend` files
- editing the `Xtext` file
- running `GenerateRosetta.mwe2`.

You can however let Maven take care of that, and still edit regular Java files, run tests, etc.

Unfortunately, there is an issue in Intellij that lets the Maven build fail, see
- https://youtrack.jetbrains.com/issue/IDEA-262695
- https://github.com/eclipse/xtext/issues/1953

In the stacktrace, you'll see a reference to a file called `plexus-classworlds.license`. It is safe to delete this file.
Once you do this, the build should succeed.

## Roadmap

Coming soon...

## Contributing
For any questions, bugs or feature requests please open an [issue](https://github.com/finos/rune-dsl/issues)
For anything else please send an email to {project mailing list}.

To submit a contribution:
1. Fork it (<https://github.com/finos/rune-dsl/fork>)
2. Create your feature branch (`git checkout -b feature/fooBar`)
3. Read our [contribution guidelines](.github/CONTRIBUTING.md) and [Community Code of Conduct](https://www.finos.org/code-of-conduct)
4. Commit your changes (`git commit -am 'Add some fooBar'`)
5. Push to the branch (`git push origin feature/fooBar`)
6. Create a new Pull Request

_NOTE:_ Commits and pull requests to FINOS repositories will only be accepted from those contributors with an active, executed Individual Contributor License Agreement (ICLA) with FINOS OR who are covered under an existing and active Corporate Contribution License Agreement (CCLA) executed with FINOS. Commits from individuals not covered under an ICLA or CCLA will be flagged and blocked by the FINOS Clabot tool (or [EasyCLA](https://community.finos.org/docs/governance/Software-Projects/easycla)). Please note that some CCLAs require individuals/employees to be explicitly named on the CCLA.

* Unsure if you are covered under an existing CCLA? Email [help@finos.org](mailto:help@finos.org)*

## Get in touch with the Rune Team

 Get in touch with the Rune team by creating a [GitHub issue](https://github.com/REGnosys/rosetta-dsl/issues/new) and labelling it with "help wanted".

 We encourage the community to get in touch via the [FINOS Slack](https://www.finos.org/blog/finos-announces-new-community-slack).

## License

Copyright 2019 REGnosys

Distributed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

SPDX-License-Identifier: [Apache-2.0](https://spdx.org/licenses/Apache-2.0)
