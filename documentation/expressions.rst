Expressions
!!!!!!!!!!!
Rosetta Expressions are used to perfom simple calculations and comparisons. Simple expressions can be built up using `operators <#operators-label>`_ to form more complex expressions.
They are used for `Functions <ducumentation.html#function-label>`_,
`Data type validation conditions <documentation.html#condition-label>`_,
`Conditional mappings <mapping.html#when-clause-label>`_ and 
`Report Rules <documentation.html#report-rule-label>`_

Expressions can be `evaluated` with a context of a Rosetta object to `return` a result. The result of an expression is either a single `basic <documentation.html#basic-type-label>` value (2.0, True, "USD"), a single Rosetta object (e.g. a Party object) or a `List` of values, all of the same type.

The `type` of an expression is the type of the result that it will evaluate to. E.g. an expression that evaluates to True or False is of type boolean, an expression that evaluates to a list of SecurityLegs is of type `List of SecurityLeg`. A list is an ordered collection of items.

The below sections will detail the different types of Rosetta Expressions and how they are used. 

Constant Expressions
""""""""""""""""""""
An expression can be a `basic <documentation.html#basic-type-label>`_ constant such as 1, True or "USD". 

Constants are valid expressions and are useful for comparisons to more complex expressions.


Enumeration Constants
=====================

An expression can refer to a Rosetta Enumeration value using the name of the Enumeration type, followed by '->' and finally the name of a value. E.g. ``DayOfWeekEnum -> SAT``\.

List Constants
==============

Constants can also be declared as lists using square brackets by starting with ``[``, followed by a comma separated list of expressions and closing with ``]``. E.g. ::

    [1,2]
    ["A",B"]
    [DayOfWeekEnum->SAT, DayOfWeekEnum->SUN]

.. _rosetta-path-label:

Rosetta Path Expressions
""""""""""""""""""""""""
The simplest Rosetta Path Expression is just the name of an attribute. For example, ``before`` in the context of a `condition <documentation.html#broken-link>` of a ContractFormationPrimitive will evaluate to the value of the before state of the contract formation. In the example below the before state is checked for `existence <#exists-label>`_.

.. code-block:: Haskell
  :emphasize-lines: 7

  type ContractFormationPrimitive: 

	before ExecutionState (0..1) 
	after PostContractFormationState (1..1)

	condition: <"The quantity should be unchanged.">
		if before exists ....

Attribute names can be chained together using `->` in order to refer to attributes further down the Rosetta object tree. In the example below the security of the product contained in a confirmation is checked for `existence <#exists-label>`_.

.. code-block:: Haskell
  :emphasize-lines: 10

    type Confirmation: <"A class to specify a trade confirmation.">

        identifier Identifier (1..*) 
        party Party (1..*) 
        partyRole PartyRole (1..*) 
        lineage Lineage (0..1) 
        status ConfirmationStatusEnum (1..1)

        condition BothBuyerAndSellerPartyRolesMustExist: 
            if lineage -> executionReference -> tradableProduct -> product -> security exists

..
    Not sure how to make this more helpful

.. note:: In some situations (Reporting rules and conditional mapping) it is unclear where a Rosetta Path Expression should start from. In this case the rosetta path should begin with a type name e.g. ``WorkflowStep -> eventIdentifier`` . The grammar validation in Rosetta will make it clear when this is required.

If when evaluated a Rosetta path refers to an attribute that does not have a value in the object it is being evaluated against then the result is *null* - there is no value. If an attribute of that non-existant object is referenced then the result is still null.

Cardinality
===========
A Rosetta path expression that refers to an attribute with multiple `cardinality <documentation.html#cardinality_label>`_ will result in a list of values. If a chained rosetta path expression has multiple links with multiple cardinality then the result is a flattened list. E.g. ``businessEvent -> primitives -> transfer -> cashTransfer`` (from Qualify_CashTransfer) gets all the *CashTransferComponent*\s from all the *Primitive*\s in a *WorkflowStep* as a single list.

An expression that has the potential to yield multiple values is said to have *multiple cardinality* and will always evaluate to a list of zero or more elements.

Only element
============
The keyword ``only-element`` can appear after an attribute name in a Rosetta path. ::

    observationEvent -> primitives only-element -> observation
	
This imposes a constraint that the evaluation of the path up to this point returns exactly one value. If it evaluates to `null <#null-label>`_\, an empty list or a list with more than one value then the expression result will be null.

.. _operators-label:

Operators
"""""""""
Rosetta supports operators that combine expressions into more complicated expressions.

Comparison Operators
====================
The result type of a comparison operator is always boolean

* ``=`` - Equals. Returns *true* if the left expression is equal to the right expression, otherwise false. Basic types are equal if their values are equal. Two complex rosetta types are equal if all of their attributes are equal, recursing down until all basic typed attributes are compared.
* ``<>`` - Does not equal. Returns *false* if the left expression is equal to the right expression, otherwise true.
* ``<``, ``<=``, ``>=``, ``>``  - performs mathematical comparisons on the left and right values. Both left and right have to evaluate to numbers or lists of numbers.
* ``exists`` - returns true if the left expression returns a result. This can be further modified with additional keywords.
    * ``only`` - the value of left expression exists and is the only attribute with a value in its parent object.
    * ``single`` - the value of expression either has single cardinality or is a list with exactly one value.
    * ``mutiple`` - the value expression has more than 2 results
* ``is absent`` - retuns true if the left expression does not return a result.

List Comparison Operators
=========================
Rosetta also has operators that are designed to function on lists

* ``contains`` - every element in the right hand expression is = to an element in the left hand expression
* ``disjoint`` - true if no element in the left side expression is equal to anu element in the right side expression
* ``count`` - returns the number of elements in the expression to its left
* ``(all\any) = (<>, < etc)``

If the contains operator is passed an expression that has single cardinality that expression is treated as a list containing the single element or an empty list if the element is null.

The grammar enforces that the expression for count has multiple cardinality. 

For the comparison operators if either left or right expression has multiple cardinality then either the other side should have multiple cardinality or `all` or `any` should be specified. (At present only `any` is supported for `<>` and `all` for the other comparison operators.

The semantics for list comparisons are as follows

* ``=`` 
    * if both sides are lists then the lists must contain elements that are ``=`` when compared pairwise in the order.
    * if the one side is a list and the other is single and `all` is specified then every element in the list must ``=`` the single value
    * if the one side is a list and the other is single and `any` is specified then at least one element in the list must ``=`` the single value (unimplemented)
* ``<>``
    * if both sides are lists then then true is returned if the lists have different length or every element is ``<>`` to the corresonding element by position
    * if one side is a list and the `any` is specified then true is returned if any element ``<>`` the single element
    * if one side is a list and the `all` is specified then true is returned if all elements ``<>`` the single element (unimplemented)
* ``<``, ``<=``, ``>=``, ``>``
    * if both sides are lists then every element in the first list must be ``>`` the element in the corresponding posistion in the second list
    * if one side is single and `all` is specified then every element in the list must be ``>`` that single value
    * if one side is single and `any` is specified then at least one element in the list must be ``>`` that single value (unimplemented)

An expression that is expected to return multiple cardinality that returns null is considered to be equivalent to an empty list

.. _null-label:

Comparison Operators and Null
=============================
If one or more expressions being passed to an operator is of single cardinality but is null (not present) the behavior is as follows

* null = *any value* returns false
* null <> *any value* returns true
* null  > *any value* returns false
* null  >= *any value* returns false

*any value* here includes null. The behaviour is symmetric - if the null appears on the either side of the expression the result is the same. if the null value is of multiple cardinality then it is treated as an empty list.

Boolean Operators
=================

``and`` and ``or`` can be used to logically combine boolean typed expressions.

``(`` and ``)`` can be used to group logical expressions. Expressions inside brackets are evaluated first.

Arithmetic Operators
====================
Rosetta supports basic arithmetic operators

* ``+`` can take either two numerical types or two string typed expressions. The result is the sum of two numerical types or the concatenation of two string types
* ``-``, ``*``, ``/`` take two numerical types and respectively subtract, multiply and divide them to give a number result.

Conditional Expression
""""""""""""""""""""""
Conditional expressions consist of an ``if clause`` followed by a ``then clause`` with an optional ``else clause``

The ``if clause`` consists of the keyword ``if`` followed by a boolean expression
The ``then clause`` consists of the keyword ``then`` followed by any expression
The optional ``else clause`` consists of the keyword ``else`` followed by any expression

If the ``if clause`` evaluates to true then the result of the ``then clause`` is returned by the conditional expression. if it evaluates to false then the result of the ``else clause`` is returned if present, else null is returned.

The type of the expression is the type of the expression contained in the ``then clause``\. The grammar enforces that the type of the else expression matches the then expression. 

Function calls
""""""""""""""
An expression can be a call to a `Function <documentation.html#function-label>`_. A function call consists of the function name, followed by ``(``, a comma separated list if ``arguments`` and a closing ``)``

The arguments list is a list of expressions. The number and type of the expressions must match the inputs defined by the function definition. This will be enforced by the syntax validator.

The type of a Function call expression is the type of the output of the called function.

In the last line of the example below the Max function is called to find the larger of the two WhichIsBigger function arguments, which is then compared to the first argument. The if expression surrounding this will then return "A" if the first argument was larger, "B" if the second was larger.

.. code-block:: Haskell
  :emphasize-lines: 18

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

Operator Precedence
"""""""""""""""""""
Formally expressions in rosetta are evaluated in the following order (See `Operator Precedence <https://en.wikipedia.org/wiki/Order_of_operations>`_). Higher are evaluated first

- RosettaPathExpressions - e.g. 'Lineage -> executionReference'
- Brackets - e.g. '(1+2)'
- if-then-else - e.g. 'if (1=2) then 3'
- only-element - e.g. 'Lineage -> executionReference only-element'
- count - e.g. 'Lineage -> executionReference count'
- Multiplicative operators '*','/' - e.g. '3*4'
- Additive operators '+'.'-' - e.g. '3-4'
- Comparison operators '>=', '<=','>','<' - e.g. '3>4
- Existence operators 'exists','is absent','contains','disjoint' - e.g. 'Lineage -> executionReference exists'
- and - e.g. '5>6 and true'
- or - e.g. '5>6 or true'
