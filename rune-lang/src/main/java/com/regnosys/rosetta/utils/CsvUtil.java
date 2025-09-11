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
        if (attr.isMulti()) {
            return false;
        }
        RType baseType = typeSystem.stripFromTypeAliases(attr.getRMetaAnnotatedType().getRType());
        return !(baseType instanceof RDataType) && !(baseType instanceof RChoiceType);
    }
}
