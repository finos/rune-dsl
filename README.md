# Rune DSL Interpreter

An interpreter for a subset of the Rune DSl.

For most practical information, refer to the [original readme file](README_project.md)

## Authors
- Jacek Kulik
- Antonio Lupu
- Maria Cristescu
- Bogdan Damian
- Diana Şutac

## Project Structure

FIGURE HERE

Rune operations are defined in [xcore files](rosetta-lang/model/RosettaInterpreter.xcore)

All interpretable operations implement an interp method which takes in an expression and an environment.

There is a visitor which accepts the expressions and passes them along to the correct concrete interpreter to perform their operations.

## How to extend

### New Expression
The simplest extension is by adding new Rune constructs to interpret. The process for this is:
 1. Define the interp method for this expression in [RosettaExpression.xcore](rosetta-lang/model/RosettaExpression.xcore)
 2. Create a new class inside [interpreternew.visitors](rosetta-lang/src/main/java/com/regnosys/rosetta/interpreternew/visitors/) OR use one of the already existing ones if it fits thematically
 3. Implement a new interp method in [RosettaInterpreterVisitor.java](rosetta-lang/src/main/java/com/regnosys/rosetta/interpreternew/RosettaInterpreterVisitor.java) to accept the new expression
 4. Implement the interpretation of the expression
 5. Test the expression in [the testing module](rosetta-testing/src/test/java/com/regnosys/rosetta/interpreternew/visitors/)

### New Value Domain Type
Right now this process doesn't have any additional steps, but this may change in the future.

1. Create a new class in [the value domain folder](rosetta-lang/src/main/java/com/regnosys/rosetta/interpreternew/values/)
2. Optionally also test the class if it requires it

### New Expression Function

Currently there is only the interpretation function defined for the expressions, but the visitor pattern allows for easily adding a new one.

1. Create a new main visitor (like RosettaTypeCheckVisitor)
2. Define accept methods for this visitor for all the expressions
3. Implement concrete acceptors in the new visitor or in new files stemming from it