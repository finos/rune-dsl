Overview of the Rosetta DSL
===========================

.. role:: raw-html(raw)
    :format: html

**Continuous Integration:** |Codefresh build status| :raw-html:`<br />`
**License:** `Apache 2.0 <http://www.apache.org/licenses/LICENSE-2.0>`_

What is the Rosetta DSL
-----------------------

*Rosetta* is a Domain-Specific Language (DSL) designed for the financial industry, which purpose is to consolidate market standards and operational practices into a cohesive *domain model* (or simply *model*). In software engineering, a `domain model <https://en.wikipedia.org/wiki/Domain_model>`_ is a conceptual model of the domain that incorporates both *data* and *behaviour* (i.e. rules and processes).

The key idea behind the Rosetta DSL is that financial markets presently have two unappealing characteristics in support of electronic data representation:

- **Variety of data representations**. The plurality of data standards (the main ones being FIX, FpML, ISO 20022 and EFET) is compounded by the many variations in the implementation of those, to which we need to add a wide range of proprietary data representations.
- **Limited availability of native digital tools** that would allow those data representations to be directly translated into executable code. Even the protocols that have a native digital representation (e.g. FpML and FIXML, which are available in the form of XML schemas) have associated specifications artefacts which require further manual specification and/or coding in order to result in a complete executable solution. In FpML, this is the case of the associated validation rules. In FIX, an example of such are the Recommended Practices/Guidelines, which are only available in the form of PDF documents.

**The Rosetta DSL addresses those shortcomings by enabling the consolidation** of various data and workflow representations into a cohesive model (hence the naming reference to the Rosetta Stone). The model is automatically translated into executable code using code generators and can directly be used as part of an implementation stack.

Rosetta DSL Components
----------------------

The open-source Rosetta DSL repository comprises 2 components:

- **Syntax**, also known as a *grammar*
- **Code Generators**, provided by default for `Java <https://www.oracle.com/java/>`_

Syntax
^^^^^^

The modelling components available in the Rosetta DSL syntax are detailed in the `Rosetta Modelling Components <https://docs.rosetta-technology.io/dsl/documentation.html>`_ section of the documentation. Those components can be used to either:

* create new model from scratch
* edit or extend existing models

The syntax is used in particular to express the *Common Domain Model* (or CDM), which provides a blueprint for the lifecycle events and processes of transactions in financial markets. The CDM is designed to be shared in open source across all financial industry participants, hence the name *common*. The CDM is hosted separately from the Rosetta DSL in its own `repository <https://github.com/REGnosys/rosetta-cdm>`_.

Code Generators
^^^^^^^^^^^^^^^

Code generators are key to remove the need for technologists to translate the model specifications into actual code. To make the model technology platform-agnostic while ensuring inter-operability of different implementations, these code generators are also open source.

Only default code generators are provided as part of the Rosetta DSL repository. A separate `Code Generators repository <https://github.com/REGnosys/rosetta-code-generators>`__ allows the community to create and make available code generators in potentially any software language. The `Code Generator documentation <https://docs.rosetta-technology.io/dsl/codegen-readme.html>`_ details the code generation mechanism and how to write and test one.

The ISDA CDM
^^^^^^^^^^^^

The `ISDA Common Domain Model <https://docs.rosetta-technology.io/cdm/index.html>`_ is the first live application of the Rosetta DSL, to the derivative's transaction lifecycle. It is openly accessible through the `CDM Portal <https://portal.cdm.rosetta-technology.io>`_, subject to the ISDA CDM licence.

For more details, please consult the `ISDA CDM documentation <https://docs.rosetta-technology.io/cdm/index.html>`_ or contact ISDA directly at marketinfrastructureandtechnology@isda.org

The Rosetta SDK
---------------

In order to use the Rosetta DSL that is open-source, a complete *Software Development Kit (SDK)* named `Rosetta Core <https://ui.rosetta-technology.io/>`_ has been provided to the community (currently in *beta*). Much like how software engineers use programming languages and tools to create software, it is useful to think of Rosetta Core as a set of tools to use when creating and editing a domain model.

Rosetta Core provides a complete adoption tool-kit for the DSL and the CDM, allowing firms to convert data into CDM objects, edit and test the model to develop their own implementations, and contribute back to the CDM project.


.. |Codefresh build status| image:: https://g.codefresh.io/api/badges/pipeline/regnosysops/REGnosys%2Frosetta-dsl%2Frosetta-dsl?branch=master&key=eyJhbGciOiJIUzI1NiJ9.NWE1N2EyYTlmM2JiOTMwMDAxNDRiODMz.ZDeqVUhB-oMlbZGj4tfEiOg0cy6azXaBvoxoeidyL0g&type=cf-1
   :target: https://g.codefresh.io/pipelines/rosetta-dsl/builds?repoOwner=REGnosys&repoName=rosetta-dsl&serviceName=REGnosys%2Frosetta-dsl&filter=trigger:build~Build;branch:master;pipeline:5d148a0543bba039bd196117~rosetta-dsl
