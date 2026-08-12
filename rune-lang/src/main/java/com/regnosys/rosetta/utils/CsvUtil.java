package com.regnosys.rosetta.utils;

import com.regnosys.rosetta.types.*;
import jakarta.inject.Inject;

import java.util.List;

public class CsvUtil {
    @Inject
    private TypeSystem typeSystem;

    public boolean isTypeTabular(RType type) {
        if (!(type instanceof RDataType dataType)) {
            return false;
        }
        return getNonSimpleAttributes(dataType).isEmpty();
    }
    public List<RAttribute> getNonSimpleAttributes(RDataType dataType) {
        return dataType.getAllAttributes().stream()
                .filter(attr -> !isSimpleAttribute(attr))
                .toList();
    }
    
    private boolean isSimpleAttribute(RAttribute attr) {
        RMetaAnnotatedType annotatedType = attr.getRMetaAnnotatedType();
        if (annotatedType.hasAttributeMeta()) {
            // A metadata-annotated attribute is not a scalar. `[metadata scheme]` or `[metadata id]`
            // on a string generates a FieldWithMetaString wrapper, and a CSV cell has no shape for an
            // object: measured in rune-common, writing one fails with "CSV generator does not support
            // Object values for properties (nested Objects)" and reading one fails with "Cannot
            // construct instance of FieldWithMetaString ... from String value". It cannot round-trip in
            // either direction, so treating it as tabular blesses a type no CSV serialiser can handle.
            //
            // hasAttributeMeta() excludes `key`, the one metadata that belongs to a type rather than an
            // attribute — the grammar already refuses `[metadata key]` on an attribute ("annotation only
            // allowed on a type"). A `key` reaching an attribute's meta therefore came from the type it
            // points at, which is an RDataType and so is rejected below regardless.
            return false;
        }
        RType baseType = typeSystem.stripFromTypeAliases(annotatedType.getRType());
        return !(baseType instanceof RDataType) && !(baseType instanceof RChoiceType);
    }
}
