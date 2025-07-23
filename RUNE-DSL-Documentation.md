# RUNE DSL Documentation

## Table of Contents
- [Introduction](#introduction)
- [Language Structure](#language-structure)
- [Basic Elements](#basic-elements)
  - [Namespaces and Imports](#namespaces-and-imports)
  - [Types](#types)
  - [Annotations](#annotations)
- [Data Modeling](#data-modeling)
  - [Data Types](#data-types)
  - [Attributes](#attributes)
  - [Choices](#choices)
  - [Enumerations](#enumerations)
  - [Type Aliases](#type-aliases)
- [Functions](#functions)
  - [Function Declaration](#function-declaration)
  - [Inputs and Outputs](#inputs-and-outputs)
  - [Conditions](#conditions)
  - [Operations](#operations)
  - [Shortcuts](#shortcuts)
- [Expressions](#expressions)
  - [Literals](#literals)
  - [References](#references)
  - [Operators](#operators)
  - [Conditional Expressions](#conditional-expressions)
  - [Collection Operations](#collection-operations)
  - [Type Conversion](#type-conversion)
- [Synonyms and External References](#synonyms-and-external-references)
  - [Synonyms](#synonyms)
  - [External Synonym Sources](#external-synonym-sources)
  - [Documentation References](#documentation-references)
- [Rules and Reporting](#rules-and-reporting)
  - [Rules](#rules)
  - [Reports](#reports)
- [Examples](#examples)

## Introduction

RUNE DSL (Domain Specific Language) is a specialized language designed for financial domain modeling and rule specification. It provides a structured way to define data models, functions, rules, and transformations that are particularly useful in financial contexts.

The language is designed to be readable by both technical and business users, bridging the gap between domain expertise and technical implementation.

## Language Structure

A RUNE DSL model consists of:

1. A namespace declaration
2. Optional imports
3. A collection of type definitions, functions, rules, and other elements

The basic structure of a RUNE file is:

```
namespace com.example.domain

import com.example.common.*

// Type definitions, functions, rules, etc.
```

## Basic Elements

### Namespaces and Imports

**Namespace Declaration**:
```
namespace com.example.domain
```

**Import Statement**:
```
import com.example.common.*
import com.example.types.Date as MyDate
```

### Types

RUNE DSL supports various types:

- Basic types (string, int, number, boolean, date, etc.)
- User-defined types (data types, choices, enumerations)
- Collections (lists)

### Annotations

Annotations provide metadata for elements:

```
[annotation MyAnnotation:
  attribute1 string (1..1)
  attribute2 int (0..1)
]

[MyAnnotation attribute1: "value"]
```

## Data Modeling

### Data Types

Data types define structured data:

```
type Person:
  name string (1..1)
  age int (0..1)
  addresses Address (0..*)
```

Data types can extend other types:

```
type Employee extends Person:
  employeeId string (1..1)
  department string (1..1)
```

### Attributes

Attributes define the properties of data types:

```
name string (1..1) <"The person's full name">
```

The format is:
- Name
- Type
- Cardinality (min..max)
- Optional documentation

Cardinality can be:
- `(1..1)`: Exactly one (required)
- `(0..1)`: Zero or one (optional)
- `(0..*)`: Zero or more
- `(1..*)`: One or more
- `(n..m)`: Between n and m

### Choices

Choices represent a selection between different attributes:

```
choice Asset:
  bond Bond (1..1)
  equity Equity (1..1)
  commodity Commodity (1..1)
```

### Enumerations

Enumerations define a set of named values:

```
enum Direction:
  BUY
  SELL
```

Enumerations can have documentation and synonyms:

```
enum AssetClass:
  EQUITY <"Stocks and equity-related instruments">
  FIXED_INCOME <"Bonds and other debt instruments">
  COMMODITY <"Raw materials or primary products">
  [synonym FIX value "AssetClassCode"]
```

### Type Aliases

Type aliases create alternative names for existing types:

```
typeAlias Percentage:
  number
```

## Functions

### Function Declaration

Functions define operations that can be performed:

```
func calculateTotal:
  inputs:
    prices number (1..*)
    taxRate number (1..1)
  output:
    total number (1..1)
```

### Inputs and Outputs

Functions can have multiple inputs and a single output:

```
inputs:
  value1 number (1..1)
  value2 number (1..1)
output:
  result number (1..1)
```

### Conditions

Conditions define constraints that must be satisfied:

```
condition validPrice:
  price > 0
```

### Operations

Operations define how values are assigned:

```
set result:
  value1 + value2
```

Operations can add to collections:

```
add prices:
  newPrice
```

### Shortcuts

Shortcuts (aliases) define reusable expressions:

```
alias taxAmount:
  subtotal * taxRate
```

## Expressions

### Literals

- String: `"text"`
- Number: `123.45`
- Integer: `42`
- Boolean: `True`, `False`
- Empty: `empty`
- List: `[1, 2, 3]`

### References

References to attributes, variables, or functions:

```
person -> name
calculateTotal(prices, taxRate)
```

Attribute access uses the arrow notation (`->`) rather than dot notation:

```
trade -> price
employee -> department
```

Attributes can be chained together:

```
trade -> instrument -> issuer -> name
```

When an attribute has multiple cardinality (e.g., (0..*) or (1..*)), accessing it automatically converts the expression to a list:

```
// If trades is a collection of Trade objects
trades -> price  // This returns a list of prices
```

### Operators

Arithmetic operators:
- Addition: `+`
- Subtraction: `-`
- Multiplication: `*`
- Division: `/`

Comparison operators:
- Equal: `=`
- Not equal: `<>`
- Greater than: `>`
- Less than: `<`
- Greater than or equal: `>=`
- Less than or equal: `<=`

Logical operators:
- And: `and`
- Or: `or`

Collection operators:
- Contains: `contains`
- Disjoint: `disjoint`

### Conditional Expressions

If-then-else expressions:

```
if price > 100 then
  price * 0.9
else
  price
```

### Collection Operations

Operations on collections:

- `filter`: Filter elements based on a condition
- `extract`: Transform elements (map)
- `reduce`: Combine elements
- `count`: Count elements
- `sum`: Sum elements
- `min`: Find minimum
- `max`: Find maximum
- `first`: Get first element
- `last`: Get last element
- `exists`: Check if elements exist
- `only-element`: Get the only element
- `flatten`: Flatten nested collections
- `distinct`: Get unique elements
- `sort`: Sort elements

Examples:

```
prices->filter price > 100
names->extract name->toUpperCase()
numbers->sum
```

### Lambda Expressions

Lambda expressions (also called closures) are anonymous functions that can be used with collection operations:

```
// Simple lambda with implicit parameter (item is the default variable name)
trades->filter price > 1000

// Same lambda with square brackets (required in nested expressions)
trades->filter [price > 1000]

// Lambda with explicit parameter
trades->filter trade [trade -> price > 1000]

// Multi-parameter lambda
pairs->filter x, y [x + y > 10]
```

The syntax for lambda expressions is:
- `collection->operation <var-name> [<var-name> -> expression]`
- If the variable name is not specified, `item` is used implicitly
- Square brackets can be omitted if the lambda is not part of a nested expression
- For multiple parameters, separate them with commas

### Expression Chaining

Expressions can be chained together using the `then` operator:

```
// Chain multiple operations
trades->filter price > 1000 ->extract quantity ->sum

// Using 'then' for more complex transformations
trades->filter price > 1000
      ->then filtered [filtered->extract quantity * price]
      ->sum
```

When using `then`, the result of the previous expression is passed as input to the next expression, allowing for more complex transformations.

### Type Conversion

Convert between types:

- `to-string`: Convert to string
- `to-number`: Convert to number
- `to-int`: Convert to integer
- `to-date`: Convert to date
- `to-time`: Convert to time
- `to-date-time`: Convert to date-time
- `to-zoned-date-time`: Convert to zoned date-time
- `to-enum`: Convert to enumeration

## Synonyms and External References

### Synonyms

Synonyms provide alternative names for elements:

```
[synonym FIX value "1"]
```

### External Synonym Sources

Define external synonym sources:

```
synonym source FIX {
  Person: 
    + name [value "PersonName"]
    + age [value "PersonAge"]
}
```

### Documentation References

Reference regulatory or other documentation:

```
[docReference for attribute body ISDA_2002 "Section 2.1"]
```

## Rules and Reporting

### Rules

Rules define conditions that must be satisfied:

```
reporting rule ValidTrade from Trade:
  trade->price > 0 and trade->quantity > 0
```

### Reports

Reports define how data should be reported:

```
report ESMA_EMIR in T+1
from Trade
when isEligibleForReporting
using standard EMIR
with type EMIRReport
```

## Examples

### Data Type Example

```
type Trade:
  <"Represents a financial trade">
  tradeId string (1..1) <"Unique identifier for the trade">
  tradeDate date (1..1) <"Date when the trade was executed">
  settlementDate date (0..1) <"Date when the trade will settle">
  buyer Party (1..1) <"The buying party">
  seller Party (1..1) <"The selling party">
  instrument Instrument (1..1) <"The traded instrument">
  quantity number (1..1) <"Quantity of the instrument">
  price number (1..1) <"Price per unit">
  currency string (1..1) <"Currency of the price">

  condition validPrice:
    price > 0

  condition validQuantity:
    quantity > 0
```

### Function Example

```
func calculateTradeValue:
  <"Calculates the total value of a trade">
  inputs:
    trade Trade (1..1) <"The trade to calculate value for">
  output:
    value number (1..1) <"The calculated trade value">

  alias baseValue:
    trade->price * trade->quantity

  condition validInputs:
    trade->price exists and trade->quantity exists

  set value:
    baseValue

  post-condition positiveValue:
    value > 0
```

### Rule Example

```
reporting rule EligibleForEMIR from Trade:
  <"Determines if a trade is eligible for EMIR reporting">
  trade->instrument->assetClass = AssetClass->EQUITY and
  trade->quantity > 100
```
