Rosetta DSL
===========

.. role:: raw-html(raw)
    :format: html

**Continuous Integration:** |Codefresh build status| :raw-html:`<br />`
**License:** `Apache 2.0 <http://www.apache.org/licenses/LICENSE-2.0>`_

The Rosetta DSL
---------------

*Rosetta* is a Domain-Specific Language (DSL) designed for the financial
industry, which purpose is to consolidate market standards and
operational practices into a cohesive *domain model* (or simply
*model*). In software engineering, a `domain
model <https://en.wikipedia.org/wiki/Domain_model>`_ is a conceptual
model of the domain that incorporates both *data* and *behaviour*
(i.e. rules and processes).

This open-source DSL repository comprises 2 components:

- **Syntax** (also known as a *grammar*)

- **Code Generators** (by default: `Java <https://www.oracle.com/java/>`_)

The model for the financial industry domain, written using the syntax provided by the Rosetta DSL, is referred to as a *Common Domain Model*, or simply *CDM*. The CDM is designed to be shared openly across all industry participants, hence the name *Common*. The *ISDA Common Domain Model* (see `documentation <https://docs.rosetta-technology.io/cdm/documentation.html#the-isda-common-domain-model>`_ ) is the first live usage of the Rosetta DSL applied to the Derivatives markets. The CDM is hosted separately from the Rosetta DSL in its own repository.

The key idea behind Rosetta is that financial markets presently have two unappealing characteristics in support of electronic data representation:

- **Variety of data representations**. The plurality of data standards (the main ones being FIX, FpML, ISO 20022 and EFET) is compounded by the many variations in the implementation of those, to which we need to add a wide range of proprietary data representations.
- **Limited availability of native digital tools** that would allow those data representations to be directly translated into executable code. Even the protocols that have a native digital representation (e.g. FpML and FIXML, which are available in the form of XML schemas) have associated specifications artefacts which require further manual specification and/or coding in order to result in a complete executable solution. In FpML, this is the case of the associated validation rules. In FIX, an example of such are the Recommended Practices/Guidelines, which are only available in the form of PDF documents.

**Rosetta addresses those shortcomings by enabling the consolidation of various data and workflow representations** into a cohesive model (hence the naming reference to the Rosetta Stone). Thanks to the code generators, the model can be automatically translated into executable code and directly used as part of an implementation stack.

Code generators are therefore key to remove the need for technologists to translate the model specifications into actual code. To make the model technology platform-agnostic while ensuring inter-operability of different implementations, these code generators are also open source. Only default code generators are provided as part of this Rosetta DSL repository, but a dedicated `repository <https://github.com/REGnosys/rosetta-code-generators>`__ has been created to enable the community to create and make available other code generators in potentially any software language.


The Rosetta SDK
---------------

In order to use the Rosetta DSL that is open-source, a complete *Software Development Kit (SDK)* named `Rosetta Core <https://ui.rosetta-technology.io/>`_ will be provided to the community (ETA 2019 Q3). Much like how software engineers use programming languages and tools to create software, it is useful to think of Rosetta Core as a set of tools to use when creating and editing the domain model.

Rosetta Core provides a complete adoption tool-kit for the DSL and the CDM, allowing firms to convert data files into CDM, edit and test the model to develop their own implementations, and contribute back to the CDM project.


.. |Codefresh build status| image:: https://g.codefresh.io/api/badges/pipeline/regnosysops/REGnosys%2Frosetta-dsl%2Frosetta-dsl?branch=master&key=eyJhbGciOiJIUzI1NiJ9.NWE1N2EyYTlmM2JiOTMwMDAxNDRiODMz.ZDeqVUhB-oMlbZGj4tfEiOg0cy6azXaBvoxoeidyL0g&type=cf-1
   :target: https://g.codefresh.io/pipelines/rosetta-dsl/builds?repoOwner=REGnosys&repoName=rosetta-dsl&serviceName=REGnosys%2Frosetta-dsl&filter=trigger:build~Build;branch:master;pipeline:5d148a0543bba039bd196117~rosetta-dsl
