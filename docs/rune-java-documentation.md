---
title: "Rune Java Documentation"
date: 2023-09-01T12:57:00+02:00
description: "This document describes the interface and usage of classes that are generated from a Rune model using the Java code generator."
draft: false
weight: 2
---

# Rune Java Documentation

## Types and enums

Coming soon.

## Functions

Coming soon.

## Validation

Coming soon.

## Reports and rules

Both reports and rules are represented in the same way as a [function](#functions). They implement the `ReportFunction<IN, OUT>` interface, which extends from `RosettaFunction`, and additionally exposes an `evaluate` method that takes in a single parameter of type `IN` (the input of a report, e.g., a `ReportableEvent`) and has a result of type `OUT` (the report output, e.g., a `CFTCPart43TransactionReport`).

### Example: European Emission Report

As a simple example, consider the following report definition:
``` Haskell
report EuropeanParliament EmissionPerformanceStandardsEU in real-time
    from VehicleOwnership
    when IsEuroStandardsCoverage
    with type EuropeanParliamentReport

// Definition for regulatory references:
body Authority EuropeanParliament
corpus Regulation "Regulation (EU) 2019/631" EmissionPerformanceStandardsEU
```
This report takes an input of type `VehicleOwnership`, and returns an instance of type `EuropeanParliamentReport`, defined as follows:
``` Haskell
type VehicleOwnership:
    drivingLicence DrivingLicence (1..1)
    vehicle Vehicle (1..1)

type EuropeanParliamentReport:
    vehicleRegistrationID string (1..1)
        [ruleReference VehicleRegistrationID]
    vehicleClassificationType VehicleClassificationEnum (1..1)
        [ruleReference VehicleClassificationType]

type Vehicle:
    registrationID string (1..1)
    vehicleClassification VehicleClassificationEnum (1..1)

enum VehicleClassificationEnum:
    M1_Passengers
    M2_Passengers
    M3_Passengers
    N1I_Commercial
    ...

type Person:
    name string (1..1)

type DrivingLicence:
    owner Person (1..1)
    countryofIssuance string (1..1)
    dateofIssuance date (1..1)
    dateOfRenewal date (0..1)
    vehicleEntitlement VehicleClassificationEnum (0..*)
```

The report is supported by the following rules.

``` Haskell
eligibility rule IsEuroStandardsCoverage from VehicleOwnership:
    filter
        vehicle -> vehicleClassification = VehicleClassificationEnum -> M1_Passengers
            or vehicle -> vehicleClassification = VehicleClassificationEnum -> M2_Passengers
            or vehicle -> vehicleClassification = VehicleClassificationEnum -> M3_Passengers
            or vehicle -> vehicleClassification = VehicleClassificationEnum -> N1I_Commercial
            or ...

reporting rule VehicleRegistrationID from VehicleOwnership:
    extract vehicle -> registrationID
        as "Vehicle Registration ID"

reporting rule VehicleClassificationType from VehicleOwnership: <"Classification type of the vehicle">
    extract vehicle -> vehicleClassification
        as "Vehicle Classification Type"
```

#### Generated Java Code

In Java, the report is represented by the following class:
``` Java
@RosettaReport(namespace="test.reg", body="EuropeanParliament", corpusList={"EmissionPerformanceStandardsEU"})
@ImplementedBy(EuropeanParliamentEmissionPerformanceStandardsEUReportFunction.EuropeanParliamentEmissionPerformanceStandardsEUReportFunctionDefault.class)
public abstract class EuropeanParliamentEmissionPerformanceStandardsEUReportFunction implements ReportFunction<VehicleOwnership, EuropeanParliamentReport> {
    @Override
    public EuropeanParliamentReport evaluate(VehicleOwnership input) {
        EuropeanParliamentReport.EuropeanParliamentReportBuilder outputBuilder = doEvaluate(input);
        
        ... // build the output and perform validation
        
        return output;
    }

    protected abstract EuropeanParliamentReport.EuropeanParliamentReportBuilder doEvaluate(VehicleOwnership input);

    public static class EuropeanParliamentEmissionPerformanceStandardsEUReportFunctionDefault extends EuropeanParliamentEmissionPerformanceStandardsEUReportFunction {
        @Override
        protected EuropeanParliamentReport.EuropeanParliamentReportBuilder doEvaluate(VehicleOwnership input) { ... }
    }
}
```
Note that we rely on the Guice dependency injection framework to separate specification (`EuropeanParliamentEmissionPerformanceStandardsEUReportFunction`) from implementation (`EuropeanParliamentEmissionPerformanceStandardsEUReportFunctionDefault`). The default implementation will delegate to the `VehicleRegistrationID` and `VehicleClassificationType` reporting rules, as specified in the Rune model. 

{{< notice info "Note" >}}
The implementation of a report or a specific reporting rule can be customized by binding their Java class to a custom implementation in your Guice module.
{{< /notice >}}

#### Running a report

To run the report, we first need to inject a report function instance using any conventional method delivered by Guice. An example:
``` Java
@Inject
private EuropeanParliamentEmissionPerformanceStandardsEUReportFunction reportFunction;

@Test
private void testReportFunction() {
    VehicleOwnership input = ... // create or read a vehicle ownership instance
    EuropeanParliamentReport reportOutput = reportFunction.evaluate(input);

    assertEquals(input.getVehicle().getRegistrationID(), reportOutput.getVehicleRegistrationID());
}
```
