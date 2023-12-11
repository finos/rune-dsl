package com.rosetta.model.lib.validation;

import com.rosetta.model.lib.path.RosettaPath;

import java.util.Optional;

@Deprecated
public
class ModelValidationResult<T> implements ValidationResult<T> {

    private final String modelObjectName;
    private final String name;
    private final String definition;
    private final Optional<String> failureReason;
    private final ValidationType validationType;
    private final RosettaPath path;
    private final Optional<ValidationData> data;

    public ModelValidationResult(String name, ValidationType validationType, String modelObjectName, RosettaPath path, String definition, Optional<String> failureReason, Optional<ValidationData> data) {
        this.name = name;
        this.validationType = validationType;
        this.path = path;
        this.modelObjectName = modelObjectName;
        this.definition = definition;
        this.failureReason = failureReason;
        this.data = data;
    }

    @Override
    public boolean isSuccess() {
        return !failureReason.isPresent();
    }

    @Override
    public String getModelObjectName() {
        return modelObjectName;
    }

    @Override
    public String getName() {
        return name;
    }

    public RosettaPath getPath() {
        return path;
    }

    @Override
    public Optional<ValidationData> getData() {
        return data;
    }

    @Override
    public String getDefinition() {
        return definition;
    }

    @Override
    public Optional<String> getFailureReason() {
        if (failureReason.isPresent() && modelObjectName.endsWith("Report") && ValidationType.DATA_RULE.equals(validationType)) {
            return getUpdatedFailureReason();
        }
        return failureReason;
    }

    @Override
    public ValidationType getValidationType() {
        return validationType;
    }

    @Override
    public String toString() {
        return String.format("Validation %s on [%s] for [%s] [%s] %s",
                isSuccess() ? "SUCCESS" : "FAILURE",
                path.buildPath(),
                validationType,
                name,
                failureReason.map(s -> "because [" + s + "]").orElse(""));
    }

    // TODO: refactor this method. This is an ugly hack.
    private Optional<String> getUpdatedFailureReason() {

        String conditionName = name.replaceFirst(modelObjectName, "");
        String failReason = failureReason.get();

        failReason = failReason.replaceAll(modelObjectName, "");
        failReason = failReason.replaceAll("->get", " ");
        failReason = failReason.replaceAll("[^\\w-]+", " ");
        failReason = failReason.replaceAll("^\\s+", "");

        return Optional.of(conditionName + ":- " + failReason);
    }
}
