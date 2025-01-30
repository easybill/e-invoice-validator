package io.github.easybill.Dtos.ValidatorResults;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.helger.schematron.svrl.jaxb.FailedAssert;
import com.helger.schematron.svrl.jaxb.SchematronOutputType;
import io.github.easybill.Dtos.ValidationResultField;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;

public abstract class ValidatorResult {

    @NonNull
    private final String name;

    @NonNull
    private final String version;

    @NonNull
    private final List<@NonNull ValidationResultField> warnings;

    @NonNull
    private final List<@NonNull ValidationResultField> errors;

    public ValidatorResult(
        @JsonProperty("name") @NonNull String name,
        @JsonProperty("artifact_version") @NonNull String version,
        @NonNull List<@NonNull ValidationResultField> errors,
        @NonNull List<@NonNull ValidationResultField> warnings
    ) {
        this.name = name;
        this.version = version;
        this.errors = Collections.unmodifiableList(errors);
        this.warnings = Collections.unmodifiableList(warnings);
    }

    static List<@NonNull ValidationResultField> getErrorsFromSchematronOutput(
        @NonNull SchematronOutputType outputType
    ) {
        return outputType
            .getActivePatternAndFiredRuleAndFailedAssert()
            .stream()
            .filter(element -> element instanceof FailedAssert)
            .filter(element ->
                Objects.equals(((FailedAssert) element).getFlag(), "fatal")
            )
            .map(element -> (FailedAssert) element)
            .map(ValidationResultField::fromFailedAssert)
            .toList();
    }

    static List<@NonNull ValidationResultField> getWarningsFromSchematronOutput(
        @NonNull SchematronOutputType outputType
    ) {
        return outputType
            .getActivePatternAndFiredRuleAndFailedAssert()
            .stream()
            .filter(element -> element instanceof FailedAssert)
            .filter(element ->
                Objects.equals(((FailedAssert) element).getFlag(), "warning")
            )
            .map(element -> (FailedAssert) element)
            .map(ValidationResultField::fromFailedAssert)
            .toList();
    }

    public @NonNull List<@NonNull ValidationResultField> getErrors() {
        return errors;
    }

    public @NonNull List<@NonNull ValidationResultField> getWarnings() {
        return warnings;
    }

    public @NonNull String getName() {
        return name;
    }

    public @NonNull String getVersion() {
        return version;
    }
}
