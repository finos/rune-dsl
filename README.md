# Rosetta DSL

**Continuous Integration:** [![Codefresh build status]( https://g.codefresh.io/api/badges/pipeline/regnosysops/REGnosys%2Frosetta-dsl%2Frosetta-dsl?branch=master&key=eyJhbGciOiJIUzI1NiJ9.NWE1N2EyYTlmM2JiOTMwMDAxNDRiODMz.ZDeqVUhB-oMlbZGj4tfEiOg0cy6azXaBvoxoeidyL0g&type=cf-1)]( https://g.codefresh.io/pipelines/rosetta-dsl/builds?repoOwner=REGnosys&repoName=rosetta-dsl&serviceName=REGnosys%2Frosetta-dsl&filter=trigger:build~Build;branch:master;pipeline:5d148a0543bba039bd196117~rosetta-dsl) <br/>
**License:** **License:** [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0)


The Rosetta DSL
=====================
To tie the design choices made by the Working Group to how they manifest practically, we make reference to the Rosetta DSL which we will refer to simply as Rosetta.
It is useful to think of Rosetta as a set of tools to use when creating domain models. Much like how software engineers use programming languages and tools to create software.

Rosetta is a digital repository whose purpose is to consolidate market standards and practices into a cohesive model, from which executable code is automatically generated.

The key idea behind Rosetta is that financial markets presently have two unappealing characteristics as it relates to their supporting electronic data representation:

*  **Variety of data representations**. The plurality of data standards (the main ones being FIX, FpML, ISO 20022 and EFET) is compounded by the many variations in the implementation of those, to which we need to add a wide range of proprietary data representations.
*  **Limited availability of native digital tools** that would allow those data representations to be directly translated into executable code. Even the protocols that have a native digital representation (e.g. FpML and FIXML,
  which are available in the form of XML schemas) have associated specifications artefacts which require further manual specification and/or coding in order to result in a complete executable solution.
  In FpML, this is the case of the associated validation rules. In FIX, an example of such are the Recommended Practices/Guidelines, which are only available in the form of PDF documents.

Rosetta is looking to address those shortcomings by **consolidating various data and workflow representations into a cohesive model** (hence the naming reference to the Rosetta Stone), which can be **automatically translated into executable code**.

The ISDA Common Domain Model
============================
The ISDA Common Domain Model is an initiative that ISDA has spearheaded to produce a common, robust, digital blueprint for how derivatives are traded and managed across their lifecycle.
It is based on the design principles specified as part of ISDA’s October 2017 [CDM concept paper](https://www.isda.org/a/gVKDE/CDM-FINAL.pdf) for a product scope limited to simple interest and credit derivative products and an agreed sample of business events.

ISDA anticipates that establishing such digital data and processing standards will lead to the following benefits:

* Reduce the current need for continual reconciliations to address mismatches caused by variations in how each firm records trade lifecycle events;
* Enable consistency in regulatory compliance and reporting;
* Accelerate greater automation and efficiency in the derivatives market;
* Provide a common foundation for new technologies like distributed ledger, cloud and smart contracts to facilitate data consistency;
* Facilitate interoperability across firms and platforms.

A high-level presentation of the ISDA CDM and additional information is available on the ISDA website ([www.isda.org](http://www.isda.org/)) and particularly with the referred [Short Video](https://www.isda.org/2017/11/30/what-is-the-isda-cdm/).
