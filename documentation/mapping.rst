Mappings in Rosetta are the annotations on the model that specify how input documents in other formats 
(e.g. FpML or ISDACreate json) can be transformed into Rosetta documents. Mappings are specified in the model as synonyms. Synonyms added throughout the model are 
combined to map the data tree of an input document into the output Rosetta document. The synonyms can be used to generate an *Ingestion Environment*, 
a library of java that when given an input document as input will output the resulting Rosetta document.

Synonyms are specified on the attribute of data type and the vlaue of enum types.

Basic Mappings
^^^^^^^^^^^^^^


If any attribute does not have a synonym on it then the mapping will search down the Rosetta tree for the 
next attributes that do have synonyms.

Synonym Source
==============
First a *synonym source* is created. This can optionally extend a different synonym source
``synonym source synonym source FpML_5_10 extends FpML``
This defines a set of synonyms that are used to ingest a category of input document, in this case FpML_5_10 documents

Extends
-------
A synonym source can extend another synonym source. This forms a a new synonym source that has all the synonyms containes in the extended synonym source and can add additional synonyms as well as remove synonyms from it.

Basic Synonym
=============
Synonyms are annotations on attributes of Rosetta types. They can be written inside the definition of the type or they can be specified separately.

Inline
------
An inline synonym consists of '[' followed by the keyword *synonym* and the name of the synonym source followed by the body of the synonym and an ']' ::

	type Collateral:
		independentAmount IndependentAmount (1..1)
			[synonym FpML_5_10 value "independentAmount"]


External synonym
----------------
External synonyms are defined inside the synonym source declaration so the synonym keyword and the synonym source are not required in every synonym. 
A synonym is added to an attribute by referencing the type and attribute name and then declaring the synonym to add as the synonym body surrounded by '[]'. The code below removes all the synonyms from the independentAmount attribute of Collateral and then adds in a new synonym ::

	synonym source FpML_5_10 extends FpML
	{
		Collateral:
			- independentAmount
			+ independentAmount
				[value "independentAmount"]
	}

Synonym Body
============
Value
-----
The simplest synonym consists of a single value ``[value "independentAmount"]``. This means that the value of the input attribute "independantAmount" will be mapped to the associated field. If both the input attribute and the Rosetta field are simple types (string, number, date etc) then the input value will be stored in the appropriate place in the output document. If they are both complex types then the attributes contained within the complex type will be compared against synonyms inside the corresponding Rosetta type.

Path
----
The value of a synonym can be followed with a path declaration. E.g. ``[value "initialFixingDate" path "resetDates"]``. This allows a path of input document elements to be matched to a single Rosetta attribute. In the example the contents of the xml path "resetDates->initialFixingDate" will be mapped to the Rosetta attribute. Note that the path is applied as a prefix to the synonym value.

Maps 2
------
By default if a single input value is mapped to multiple Rosetta output values this is considered an error however by adding the "maps 2" keyword this can be overridden allowing the input value to map to many output Rosetta attributes

meta
----
The *meta* keyword inside a synonym is used to map to metadata fields. E.g. ::

	issuer string (0..1)
     [metadata scheme]
     [synonym FpML_5_10 value "issuer" meta "issuerIdScheme"]

the input value associated withe "issuer" will be mapped to the value of the attribute issuer and the value of "issuerIdScheme" will be mapped to the scheme metadata attribute

The three commonly used meta attributes are scheme, id and reference. The scheme provides a reference with which to interpret the relevant value. issuerIdScheme will be something like a url of a page that expains how to interpret the issuer.

id is used to map an identifier from the input document that is expected to be unique within the contect of the document. The reference can then be used to reference that element from elsewhere in the resulting document

Enumerations
============
A synonym on an enumeration provide mappings from the string values in your input document to the values of the enumeration. E.g. the fpml value 'Broker' will be mapped to the Rosetta constant *NaturalPersonRoleEnum.Broker* ::

	enum NaturalPersonRoleEnum: <"The enumerated values for the natural person's role.">

   	Broker <"The person who arranged with a client to execute the trade.">
    	 [synonym FpML_5_10 value "Broker"]

External enum synonyms
----------------------
In an eternal synonym file enum synonyms are defined in a block after the type attribute synonyms, proceeded by the keyword *enums* ::

	enums

	NaturalPersonRoleEnum:
		+ Broker
			[value "Broker"]

Advanced Mapping
^^^^^^^^^^^^^^^^

Hints
=====
Hints are synonyms used to bypass a layer of rosetta without consuming an input attribute. They are required where an attribute has synonyms that would usually prevent the algorithm for searching down the Rosetta tree for attributes further down, but the current input element needs to not be consumed.

e.g. ::

	ResolvablePayoutQuantity:
		+ assetIdentifier
		[value "notionalAmount"]
		[hint "currency"]

	AssetIdentifier:
		+ currency
			[value "currency" maps 2 meta "currencyScheme"]

In this example the element "notionalAmount" is mapped to the asset identifier and the children of "notionalAmount" will be matched against the synonyms for AssetIdentifier. However the input element "currency" will also be mapped to the assetIdentifier but "currency" is still available to be mapped against the synonyms of AssetIdentifier. 

Merging inputs
==============
Where you have a Rosetta attribute with multiple cardinality to which more than one input element maps synonyms can be used to either create a single instance of the Rosetta attribute that merges the input elements or to create multiple attributes - one for each input element. E.g.
The synonyms ::

	interestRatePayout InterestRatePayout (0..*)
		[synonym FpML_5_10 value feeLeg]
		[synonym FpML_5_10 value generalTerms]

will produce two InterestRatePayout objects. In order to create a single InterestRatePayout with value from the FpML feeLeg and general terms you want to use the synonym merging syntax ::

	interestRatePayout InterestRatePayout (0..*)
		[synonym FpML_5_10 value feeLeg, generalTerms]

Conditional Mappings
====================

Conditional mappings allow more complicated mappings to be done. Conditional mappings come in two types, `Set To <#set-to-label>`_ and `Set When <#set-when-label>`_.

.. _set-to-label:

Set To Mappings
---------------

Set To mappings are used to set the value of the Rosetta attribute to a constant value
They don't attempt to use any data from the input document as the value for the attribute and a synonym value must not be given.
The type of the constant must be convertible to the type of the attribute.
The constant value can be given as a string (converted as necessary) or an enum

e.g. ::

	period PeriodEnum (1..1)
		[synonym ISDA_Create_1_0 set to PeriodEnum.D]
	itemName string (1..1) <"In this ....">;
		[synonym DTCC_11_0 set to "comment"]

A set to can be conditional on a `when clause <#set-when-label>`_

e.g. ::

	itemName string (1..1) <"In this ....">;
		[synonym DTCC_11_0 set to "comment" when path = "PartyWorkflowFields.comment"]

multiple Set Tos can be combined in one synonym. They will be evaluated in the order specified with the first matching value used

e.g. ::

	xField string (1..1);
		[synonym Bank_A
			set to "FISH2" when "b.c.d" = "FISH",
			set to "SAUSAGE2" when "b.c.d" = "SAUSAGE",
			set to "DEFAULT"]


.. _set-when-label:

Set When mappings
-----------------
A set when mapping is used to set an attribute to a value derived from the input document if a given when clause is met

e.g. ::

	execution Execution (0..1) <"The execution ...">;
		[synonym CME_SubmissionIRS_1_0 value TrdCaptRpt set when "TrdCaptRpt.VenuTyp" exists]

A Set when synonym can include a default.
Default mappings can be used to set an attribute to a constant value when no other value was applicable

e.g. ::

		[synonym Bank_A value e path "b.c" default to "DEFAULT"]

When clauses
============
There are three types of when clause Test expression, Path expression or RosettaPath expression.

Test Expression
---------------
A test expression consists of a synonym path and one of three types of test. The synonym path is from the mapping that bound to this class.

* exists - tests whether a value with the given path exists in the input document
* absent - tests that a value with given path does not exist in the input document
* = or <> - tests if the value for the given path equals (or is not equal to) a constant value

e.g. ::

	execution Execution (0..1) <"The execution ...">;
		[synonym Rosetta_Workbench value trade set when "trade.executionType" exists]
	contract Contract (0..1) <"The contract ... ">;
		[synonym Rosetta_Workbench value trade set when "trade.executionType" is absent]
	discountingType DiscountingTypeEnum (1..1) <"The discounting method that is applicable.">;
		[synonym FpML_5_10 value fraDiscounting set when "fraDiscounting" <> "NONE"]

Path Expression
---------------
A Path expression tests to see if the synonym path that led us to the current class ::

	role PartyRoleEnum (1..1) <"The party role.">;`
		[synonym FpML_5_10 set to PartyRoleEnum.DeterminingParty when path = "trade.determiningParty"]

RosettaPath Expression
----------------------
A rosettaPath expression is similar to a path expression except that it examines the path in the resulting rosetta object that leads to this object.

e.g. ::

	identifier string (1..1) scheme <"The identifier value.">;
		[synonym DTCC_11_0, DTCC_9_0 value tradeId path "partyTradeIdentifier"
			set when rosettaPath = Event -> eventIdentifier -> assignedIdentifier -> identifier]

Mapper
======
Occasionally the Rosetta mapping syntax is not powerful enough to perform the required transformation from the input document to the output document. In this case a *Mapper* can be called from a synonym ::

	NotifyingParty:
			+ buyer
				[value "buyerPartyReference" mapper "CounterpartyEnum"]

When the ingestion is run a class called CounterPartyMappingProcessor will be loaded and its mapping method invoked with the partially mapped Rosetta element. The creation of mapper classes is outside the scope of this document but the full power of the programming language can be used to transform the output.

Format
======
A date/time synonym can be followed by a format construct. The keyword `format` should be followed by a string. The string should be a `Date format <https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html>`_

E.g. ::

	[value "bar" path "baz" format "MM/dd/yy"]


Pattern
=======
A synonym can optionally be followed by a the pattern construct. It is only applicable to enums and basic types other than date/times. The keyword `pattern` followed by two quoted strings. The first string is a `regular expression <https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html>`_ 
used to match against the input value. The second string is a replacement expression used to reformat the matched input before it is processed as usual for the basictype/enum. 

E.g. ::

	[value "Tenor" maps 2 pattern "([0-9]*).*" "$1"]

