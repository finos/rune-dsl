package com.regnosys.rosetta.validation;

import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions;
import com.regnosys.rosetta.rosetta.RosettaPackage;
import com.regnosys.rosetta.rosetta.simple.*;
import com.regnosys.rosetta.types.RAttribute;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.RObjectFactory;
import com.regnosys.rosetta.types.RType;
import com.regnosys.rosetta.utils.CsvUtil;
import jakarta.inject.Inject;
import org.eclipse.xtext.validation.Check;
import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*;

import org.w3c.dom.Attr;

import java.util.List;
import java.util.stream.Collectors;

public class FunctionValidator extends AbstractDeclarativeRosettaValidator {
    @Inject
    private RosettaFunctionExtensions functionExtensions;
    @Inject
    private CsvUtil csvUtil;
    @Inject
    private RObjectFactory rObjectFactory;
    
    @Check
    public void checkCsvIngestionInput(Function function) {
        if (hasCsvTransformAnnotation(function, "ingest")) {
            if (!function.getInputs().isEmpty()) {
                Attribute input = function.getInputs().get(0);
                RAttribute attr = rObjectFactory.buildRAttribute(input);
                RType t = attr.getRMetaAnnotatedType().getRType();
                if (!(t instanceof RDataType dt)) {
                    error("The input of a CSV ingest function must be a complex type", input, ROSETTA_TYPED__TYPE_CALL);
                } else {
                    List<RAttribute> nonSimple = csvUtil.getNonSimpleAttributes(dt);
                    if (!nonSimple.isEmpty()) {
                        String attrs = nonSimple.stream().map(a -> "`" + a.getName() + "`").collect(Collectors.joining(", "));
                        error("The input of a CSV ingest function must be a tabular type. Type `" + dt.getName() + "` has non-simple attributes: " + attrs, input, ROSETTA_TYPED__TYPE_CALL);
                    }
                }
                if (attr.isMulti()) {
                    error("The input of a CSV ingest function must be single cardinality", input, SimplePackage.Literals.ATTRIBUTE__CARD);
                }
            }
        }
    }
    @Check
    public void checkCsvProjectionOutput(Function function) {
        if (hasCsvTransformAnnotation(function, "projection")) {
            Attribute output = function.getOutput();
            if (output != null) {
                RAttribute attr = rObjectFactory.buildRAttribute(output);
                RType t = attr.getRMetaAnnotatedType().getRType();
                if (!(t instanceof RDataType dt)) {
                    error("The output of a CSV projection function must be a complex type", output, ROSETTA_TYPED__TYPE_CALL);
                } else {
                    List<RAttribute> nonSimple = csvUtil.getNonSimpleAttributes(dt);
                    if (!nonSimple.isEmpty()) {
                        String attrs = nonSimple.stream().map(a -> "`" + a.getName() + "`").collect(Collectors.joining(", "));
                        error("The output of a CSV projection function must be a tabular type. Type `" + dt.getName() + "` has non-simple attributes: " + attrs, output, ROSETTA_TYPED__TYPE_CALL);
                    }
                }
                if (attr.isMulti()) {
                    error("The output of a CSV projection function must be single cardinality", output, SimplePackage.Literals.ATTRIBUTE__CARD);
                }
            }
        }
    }
    private boolean hasCsvTransformAnnotation(Function function, String transformName) {
        return functionExtensions.getTransformAnnotations(function)
                .stream()
                .filter(a -> a.getAttribute() != null && "CSV".equals(a.getAttribute().getName()))
                .map(AnnotationRef::getAnnotation)
                .anyMatch(a -> a != null && transformName.equals(a.getName()));
    }
    
    @Check
    public void warnWhenEmptyFunctionsDontHaveCodeImplementationAnnotation(Function function) {
        if (function.getOutput() != null && function.getOutput().getName() != null) {
            Annotated annotated = (Annotated) function;
            boolean hasCodeImplementationAnnotation = annotated.getAnnotations()
            .stream()
            .map(aRef -> aRef.getAnnotation())
            .anyMatch(a -> "codeImplementation".equals(a.getName()));
            
            if (function.getOperations().isEmpty() && !hasCodeImplementationAnnotation) {
                warning("A function should specify an implementation, or they should be annotated with codeImplementation", function, ROSETTA_NAMED__NAME);
            }
            
            if (!function.getOperations().isEmpty() && hasCodeImplementationAnnotation) {
                warning("Functions annotated with codeImplementation should not have any setter operations as they will be overriden", function, ROSETTA_NAMED__NAME);
            }          
        }
    }
}
