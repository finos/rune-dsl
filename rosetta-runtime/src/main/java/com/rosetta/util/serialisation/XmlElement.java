package com.rosetta.util.serialisation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class XmlElement {
    private final String name;
    private final String fullyQualifiedName;
    private final String substitutionGroup;
    private final boolean isAbstract;

    @JsonCreator
    public XmlElement(@JsonProperty("name") String name, @JsonProperty("fullyQualifiedName") String fullyQualifiedName,
                      @JsonProperty("substitutionGroup") String substitutionGroup,
                      @JsonProperty("isAbstract") boolean isAbstract) {
        this.name = name;
        this.fullyQualifiedName = fullyQualifiedName;
        this.substitutionGroup = substitutionGroup;
        this.isAbstract = isAbstract;
    }

    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    public String getName() {
        return name;
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
        XmlElement that = (XmlElement) o;
        return isAbstract == that.isAbstract && Objects.equals(fullyQualifiedName, that.fullyQualifiedName) && Objects.equals(name, that.name) && Objects.equals(substitutionGroup, that.substitutionGroup);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullyQualifiedName, name, substitutionGroup, isAbstract);
    }
}
