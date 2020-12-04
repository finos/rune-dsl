Expressions
!!!!!!!!!!!
Rosetta expressions are used to perfom simple calculations and comparisons. simple expressions can be built up using `operators <#operators-label>`_ to form more complex expressions.
They are used for `Functions <ducumentation.html#function-label>`_,
`Data type validation conditions <documentation.html#condition-label>`_,
`Conditional mappings <mapping.html#when-clause-label>`_ and 
`Report Rules <documentation.html#report-rule-label>`_

Constant Expressions
""""""""""""""""""""
An expression can be a `basic <documentation.html#basic-tpye-label>`_ constant such as 1, true or "USD"

.. _rosetta-path-label:

Rosetta Path Expressions
""""""""""""""""""""""""
The simplest rosetta path expression is just the name of an attribute. ``before`` in the context of a condition of a ContrctFormationPrimitive condition or a function will return the value of the before state of the contract formation. in the example below the before state is checked for `existence <#exists-label>`_.

.. code-block:: Haskell
  :emphasize-lines: 7

  type ContractFormationPrimitive: 

	before ExecutionState (0..1) 
	after PostContractFormationState (1..1)

	condition: <"The quantity should be unchanged.">
		if before exists ....

Attribute names can be chained together using -> in order to refer to attributes further down the Rosetta object tree. In the example below the security of the product contained in a confirmation is checked for `existence <#exists-label>`_.

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

In some situations (Reporting rules and conditional mapping) it is unclear where a RosettaPath expression should start from. In this case the rosetta path should begin with a type name e.g. ``WorkflowStep -> eventIdentifier`` The grammar validation in Rosetta will make it clear when this is required.


Cardinality
===========
A Rosetta path expression that refers to an attribute with multiple `cardinality <documentation.html#cardinality_label>`_ will result in a list of values. If a chained rosetta path expression has multiple links with multiple cardinality then the reult is a flattened list. E.g. ``businessEvent -> primitives -> transfer -> cashTransfer`` (from Qualify_CashTransfer) gets all the *CashTransferComponent*\s from all the *Primitive*\s in a *WorkflowStep* as a single list.

Only element
============
The keyword ``only-element`` can appear after an attribure name in a Rosetta path. ::

    observationEvent -> primitives  only-element -> observation

This imposes a contrant that the evaluation of the path up to this point returns exactly one value. If it evaluates to `null <#null-label>`_\, an empty list or a list with more than one value the expression result is an error.

Enumeration Constants
=====================

An expression can refer to an Rosetta Enumeration value using the same rosetta path syntax. E.g. DayOfWeekEnum->SAT.

List Constants
==============

Constants of multiple cardinality can also be declared as lists using starting with ``[``, followed by a comma separated list of expressions and closing with ``]``. E.g. ::

    [1,2]
    ["A",B"]
    [DayOfWeekEnum->SAT, DayOfWeekEnum->SUN]

.. _operators-label:

Operators
"""""""""
Rosetta supports operators that combine expressions.

Comparison Operators
====================
The result type of a comparison operator is always boolean

* ``=`` - returns *true* if the left expression is equal to the right expression, otherwise false.
* ``<>`` - returns *false* if the left expression is equal to the right expression, otherwise true.
* ``<``, ``<=``, ``>=``, ``>``  - performs mathematical comparisons on the left and right values. Both left and right have to evaluate to numbers or lists of numbers.
* ``exists`` - returns true if the left expression returns a result.
    * ``only`` - the value of left expression exists and is the only attribute with a value in its parent object.
    * ``single`` - the value of expression either has single cardinality or is list with exactly one value.
    * ``mutiple`` - the value expression has more than 2 results
* ``is absent`` - retuns true if the left expression does not return a result.
* ``when present`` - if the left expression is absent returns true otherwise retuens the left expression ``=``  right expression.

List Comparison Operators
=========================
Rosetta also has operators that are designed to function on lists

* ``contains`` or ``includes`` - every element in the right hand expression is = to an element in the left hand expression
* ``count`` - returns the number of elements in the expression to its left

If these contains operator is passed an expression that has single cardinality that expression is treated as a list containing the single element or an empty list if the element is null.

The grammar enforces that the argument for count has multiple cardinality.

If one or more arguments for the other comparison operators is multiple cardinality then the semantics are

* ``=`` 
    * if both arguments are lists then the lists must contain elements that are ``=`` and in the same order.
    * if the one argument is a list and the other is single then every element in the list must ``==`` the single value
* ``<>``
    * if both arguments are lists then then true is returned if the lists have different length or and element is ``<>`` to the corresonding element
    * if one argument is a list then true is returned if any element ``<>`` the single element
* ``>`` etc
    * If both arguments are lists then every argument int he first list must be ``>`` the argument in the corresponding posistion int he second list
    * is one argument is single then every element in the list must be ``>`` that single value

An expression that is expected to return multiple cardinality that returns null is considered to be equivalent to an empty list

.. _null-label:

Comparison Operators and Null
=============================
If one or more expressions being passed to an operator is of single cardinality but is null (not present) the behavior is as follows

* null = *any value* retuns false
* null <> *any value* returns true
* null  > *any value* returns false
* null  >= *any value* returns false

*any value* here includes null. The behaviour is symetric - if the null apears on the either side of the expression the result is the same.

if the null value is of multiple cardinality then it is treated as an empty list.

Boolean Operators
=================

``and`` and ``or`` can be used to logically combine boolean typed expressions.

``(`` and ``)`` can be used to group logical expressions. Expressions inside brackets are evaluated first.

Arithmetic Operators
====================
Rosetta supports basic arithmetic operators

* ``+`` can take either two numerical types or two string typed expressions. The result is the sum of two numerical types or the concatentation of two string types
* ``-``, ``*``, ``/`` take two numerical types and respectively subtract, multply and divide them to give a number result

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