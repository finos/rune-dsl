---
title: "Rosetta Java Documentation"
date: 2023-09-01T12:57:00+02:00
description: "This document describes the interface and usage of classes that are generated from a Rosetta model using the Java code generator."
draft: false
weight: 2
---

# Rosetta Java Documentation

## Types and enums

Coming soon.

## Functions

Coming soon.

## Validation

Coming soon.

## Reports and rules

Both reports and rules are represented in the same way as a [function](#functions). They implement the `ReportFunction<IN, OUT>` interface, which extends from `RosettaFunction`, and additionally exposes an `evaluate` method that takes in a single parameter of type `IN` (the input of a report, e.g., a `ReportableEvent`) and has a result of type `OUT` (the report output, e.g., a `CFTCPart43TransactionReport`).

### Example: Hero Reports

As a simple example, consider the following report definition:
``` Haskell
report Shield Avengers SokoviaAccords in real-time
    from Person
    when HasSuperPowers
    with type SokoviaAccordsReport

// Definition of regulatory references:
body Authority Shield
corpus Act "Avengers Initiative" Avengers
corpus Regulations "Sokovia Accords" SokoviaAccords
segment section
segment field
```
This report takes an input of type `Person`, and returns an instance of type `SokoviaAccordsReport`, defined as follows:
``` Haskell
type Person:
    name string (1..1)
    powers PowerEnum (0..*)

type SokoviaAccordsReport:
    heroName string (1..1)
        [ruleReference HeroName]
    canFly boolean (1..1)
        [ruleReference CanFly]

enum PowerEnum:
    Armour
    Flight
    SuperhumanReflexes
    SuperhumanStrength
```

The report is supported by the following rules.

``` Haskell
eligibility rule HasSuperPowers from Person:
    filter powers exists

reporting rule HeroName from Person:
    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "1" provision "Hero Name."]
    extract name as "Hero Name"

reporting rule CanFly from Person:
    [regulatoryReference Shield Avengers SokoviaAccords section "2" field "1" provision "Can Hero Fly."]
    extract powers any = PowerEnum -> Flight as "Can Hero Fly"
```

#### Generated Java Code

In Java, the report is represented by the following class:
``` Java
@ImplementedBy(ShieldAvengersSokoviaAccordsReportFunction.ShieldAvengersSokoviaAccordsReportFunctionDefault.class)
public abstract class ShieldAvengersSokoviaAccordsReportFunction implements ReportFunction<Person, SokoviaAccordsReport> {
	@Override
	public SokoviaAccordsReport evaluate(Person input) {
		SokoviaAccordsReport.SokoviaAccordsReportBuilder outputBuilder = doEvaluate(input);
		
        // ... build the output and perform validation
		
		return output;
	}

	protected abstract SokoviaAccordsReport.SokoviaAccordsReportBuilder doEvaluate(Person input);

	public static class ShieldAvengersSokoviaAccordsReportFunctionDefault extends ShieldAvengersSokoviaAccordsReportFunction {
		@Override
		protected SokoviaAccordsReport.SokoviaAccordsReportBuilder doEvaluate(Person input) { ... }
	}
}
```
Note that we rely on the Guice dependency injection framework to separate specification (`ShieldAvengersSokoviaAccordsReportFunction`) from implementation (`ShieldAvengersSokoviaAccordsReportFunctionDefault`). The default implementation will delegate to the `HeroName` and `CanFly` reporting rules, as specified in the Rosetta model. The implementation of the report can be customized by binding the report class to a custom implementation in your Guice module.

#### Running a report

To run the report, we first need to inject a report function instance using any conventional method delivered by Guice. An example:
``` Java
@Inject
private ShieldAvengersSokoviaAccordsReportFunction reportFunction;

@Test
private void testReportFunction() {
    Person input = ... // create or read a person instance
    SokoviaAccordsReport reportOutput = reportFunction.evaluate(input);

    assertEquals(input.getName(), reportOutput.getHeroName());
}
```

### Running a report that relies on rules annotated with `[legacy-syntax]`

Some reporting rules you might encounter in a Rosetta model are marked with the `[legacy-syntax]` annotation. They are written in a deprecated syntax, and they need an additional Guice binding in order to work - otherwise you will see an exception during injection. Consequently, a report definition that relies in some way on a reporting rule annotated with `[legacy-syntax]` needs this binding as well.

To run such a report, add the following binding to your Guice module.
``` Java
bind(RosettaActionFactory.class).to(RosettaActionFactoryImpl.class);
```
You might need to add a dependency to the proprietary `rosetta-reports` artifact in order to import the `RosettaActionFactoryImpl` class.
``` xml
<dependency>
    <groupId>com.regnosys</groupId>
    <artifactId>rosetta-reports</artifactId>
    <version>7.6.3</version>
</dependency>
```

{{< notice info "Note" >}}
Support for `[legacy-syntax]` will be dropped in the near future, and the `rosetta-reports` repository will be archived.
{{< /notice >}}
