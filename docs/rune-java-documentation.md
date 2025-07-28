---
title: "Rune Java Documentation"
date: 2023-09-01T12:57:00+02:00
description: "This document describes the interface and usage of classes that are generated from a Rune model using the Java code generator."
draft: false
weight: 3
---

# Rune Java Documentation

## Types and enums

### Structure of Generated Model Classes

When a Rune model is compiled, each type defined in the model is translated into a Java class that implements the `RosettaModelObject` interface. These generated classes follow a consistent structure:

1. **Immutable Objects**: The generated classes are immutable. Once created, their state cannot be changed.

2. **Builder Pattern**: Each class comes with a nested builder class that implements the `RosettaModelObjectBuilder` interface. The builder is used to construct instances of the class.

3. **Attributes**: The attributes defined in the Rune model each have corresponding getter methods.

4. **Metadata**: Classes can include metadata fields that provide additional information about the object.

5. **Utility Methods**: Various utility methods are generated, such as `toString()` for debugging, `prune()` for recursively removing empty objects, and `process(...)` for traversing the object graph.

#### Example

For a Rune type definition like:

``` Haskell
type Vehicle:
    registrationID string (1..1)
    vehicleClassification VehicleClassificationEnum (1..1)
```

The generated Java interface would include:

``` Java
@RuneDataType(value="Vehicle", builder=Vehicle.VehicleBuilderImpl.class, version="x.y.z")
public interface Vehicle extends RosettaModelObject {
    // Getters
    String getRegistrationID();
    VehicleClassificationEnum getVehicleClassification();
    
    // Create a new builder instance
    public static VehicleBuilder builder() { ... }
    
    // Visitor pattern to traverse the object graph
    void process(RosettaPath path, Processor processor);

    // Builder interface
    interface VehicleBuilder extends Vehicle, RosettaModelObjectBuilder {
        // Setter methods
        VehicleBuilder setRegistrationID(String registrationID);
        VehicleBuilder setVehicleClassification(VehicleClassificationEnum vehicleClassification);

        // Build method
        Vehicle build();

        // Pruning method
        VehicleBuilder prune();

        // Other methods
        boolean hasData();
    }
}
```

### Pruning

The generated builder classes include a `prune()` method that helps to clean up object graphs by removing empty nested objects. This is particularly useful when working with large, complex object structures where many optional fields might be empty.

#### How Pruning Works

The `prune()` method recursively traverses the object graph and sets to `null` any nested `RosettaModelObject` attributes that are considered empty. An attribute is considered empty if:

1. It is `null`
2. It is an empty list
3. It is optional and all of its attributes are empty

Non-null required attributes are never considered empty, even if all of their attributes are empty.

The pruning process is implemented by:

1. Recursively calling `prune()` on all nested `RosettaModelObject` attributes
2. Filtering out `null` elements from lists and calling `prune()` on each remaining element
3. Checking if optional objects are empty after pruning (using the `hasData()` method) and setting them to `null` if they are

#### Example

Consider a simple object graph:

``` Java
Vehicle vehicle = Vehicle.builder()
    .setRegistrationID("ABC123")
    .setOwner(Person.builder()
        .setAddress(Address.builder().build())  // Empty address
        .build())
    .build();
```

After pruning:

``` Java
Vehicle.VehicleBuilder builder = vehicle.toBuilder();
builder.prune();
Vehicle prunedVehicle = builder.build();

// The empty Address object has been pruned (set to null)
assert prunedVehicle.getOwner().getAddress() == null;
```

#### When to Use Pruning

Pruning is useful in several scenarios:

1. **Before Serialization**: To reduce the size of serialized data by removing empty objects
2. **Before Comparison**: To simplify object comparison by removing empty structures that don't contribute to the object's semantic meaning
3. **Before Persistence**: To optimize storage by not storing empty objects

{{< notice info "Note" >}}
Pruning modifies the builder's state. If you need to preserve the original object, make sure to make a copy using `build().toBuilder()` before pruning.
{{< /notice >}}

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
        [label as "Vehicle Registration ID"]
        [ruleReference VehicleRegistrationID]
    vehicleClassificationType VehicleClassificationEnum (1..1)
        [label as "Vehicle Classification Type"]
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

reporting rule VehicleClassificationType from VehicleOwnership: <"Classification type of the vehicle">
    extract vehicle -> vehicleClassification
```

#### Generated Java Code

In Java, the report is represented by the following class:
``` Java
@RosettaReport(namespace="test.reg", body="EuropeanParliament", corpusList={"EmissionPerformanceStandardsEU"})
@RuneLabelProvider(labelProvider=EuropeanParliamentEmissionPerformanceStandardsEULabelProvider.class)
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
