package com.rosetta.model.lib.validation;

import com.rosetta.model.lib.path.RosettaPath;

import java.util.Optional;


public
class ModelValidationResult<T>{

    private static String modelObjectName;
    private static String name;
    private String definition;
    private Optional<String> failureReason;
    private static ValidationType validationType;
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

    public static String getModelObjectName() {
        return modelObjectName;
    }

    public static String getName() {
        return name;
    }

    public String getDefinition() {
        return definition;
    }

    public static ValidationType getValidationType() {
        return validationType;
    }

    public RosettaPath getPath() {
        return path;
    }

    public Optional<ValidationData> getData() {
        return data;
    }

    public static <T> ModelValidationResult<T> success(String name, ValidationType validationType, String modelObjectName, RosettaPath path, String definition) {
        return new ModelValidationResult<>(name, validationType, modelObjectName, path, definition, Optional.empty(), Optional.empty());
    }

    public static <T> ModelValidationResult<T> failure(String name, ValidationType validationType, String modelObjectName, RosettaPath path, String definition, String failureMessage) {
        return new ModelValidationResult<>(name, validationType, modelObjectName, path, definition, Optional.of(failureMessage), Optional.empty());
    }

    public static boolean isSuccess() {
        return !getFailureReason().isPresent();
    }


    public static Optional<String> getFailureReason() {
        if (getFailureReason().isPresent() && getModelObjectName().endsWith("Report") && ValidationType.DATA_RULE.equals(getValidationType())) {
            return getUpdatedFailureReason();
        }
        return getFailureReason();
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
    private static Optional<String> getUpdatedFailureReason() {

        String conditionName = getName().replaceFirst(getModelObjectName(), "");
        String failReason = getFailureReason().get();

        failReason = failReason.replaceAll(getModelObjectName(), "");
        failReason = failReason.replaceAll("->get", " ");
        failReason = failReason.replaceAll("[^\\w-]+", " ");
        failReason = failReason.replaceAll("^\\s+", "");

        return Optional.of(conditionName + ":- " + failReason);
    }
}
