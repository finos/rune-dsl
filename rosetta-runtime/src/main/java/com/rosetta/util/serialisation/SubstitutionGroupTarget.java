package com.rosetta.util.serialisation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class SubstitutionGroupTarget {
    private final String xmlElementName;
    private final String elementName;
    private final String substitutionGroup;
    private final boolean isAbstract;

    @JsonCreator
    public SubstitutionGroupTarget(@JsonProperty("xmlElementName") String xmlElementName, @JsonProperty("elementName") String elementName,
                                   @JsonProperty("substitutionGroup") String substitutionGroup,
                                   @JsonProperty("isAbstract") boolean isAbstract) {
        this.xmlElementName = xmlElementName;
        this.elementName = elementName;
        this.substitutionGroup = substitutionGroup;
        this.isAbstract = isAbstract;
    }

    public String getElementName() {
        return elementName;
    }

    public String getXmlElementName() {
        return xmlElementName;
    }

    public String getSubstitutionGroup() {
        return substitutionGroup;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SubstitutionGroupTarget that = (SubstitutionGroupTarget) o;
        return isAbstract == that.isAbstract && Objects.equals(elementName, that.elementName) && Objects.equals(xmlElementName, that.xmlElementName) && Objects.equals(substitutionGroup, that.substitutionGroup);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementName, xmlElementName, substitutionGroup, isAbstract);
    }
}
