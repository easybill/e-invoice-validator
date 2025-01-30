package io.github.easybill.Dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.helger.schematron.svrl.jaxb.FailedAssert;
import com.helger.schematron.svrl.jaxb.SchematronOutputType;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;

public record ValidatorResult(
    @JsonProperty("name") @NonNull String name,
    @JsonProperty("artifact_version") @NonNull String version,
    @NonNull List<@NonNull ValidationResultField> errors,
    @NonNull List<@NonNull ValidationResultField> warnings
) {
    public ValidatorResult {
        errors = Collections.unmodifiableList(errors);
        warnings = Collections.unmodifiableList(warnings);
    }

    public static ValidatorResult of(
        @NonNull String name,
        @NonNull String version,
        @NonNull SchematronOutputType report
    ) {
        return new ValidatorResult(
            name,
            version,
            getErrorsFromSchematronOutput(report),
            getWarningsFromSchematronOutput(report)
        );
    }

    private static List<@NonNull ValidationResultField> getErrorsFromSchematronOutput(
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

    private static List<@NonNull ValidationResultField> getWarningsFromSchematronOutput(
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
}
