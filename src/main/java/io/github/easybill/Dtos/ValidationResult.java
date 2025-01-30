package io.github.easybill.Dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.helger.schematron.svrl.jaxb.FailedAssert;
import com.helger.schematron.svrl.jaxb.SchematronOutputType;
import io.github.easybill.Enums.XmlProfileType;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;

public record ValidationResult(
    @NonNull ValidationResultMetaData meta,
    @NonNull List<@NonNull ValidationResultField> errors,
    @NonNull List<@NonNull ValidationResultField> warnings
) {
    public ValidationResult {
        errors = Collections.unmodifiableList(errors);
        warnings = Collections.unmodifiableList(warnings);
    }

    @JsonProperty("is_valid")
    public boolean isValid() {
        return errors.isEmpty();
    }

    public static ValidationResult of(
        XmlProfileType xmlProfileType,
        ValidationRequest validationRequest,
        SchematronOutputType schematronOutputType
    ) {
        return new ValidationResult(
            new ValidationResultMetaData(
                validationRequest.xmlSyntaxType(),
                xmlProfileType
            ),
            getErrorsFromSchematronOutput(schematronOutputType),
            getWarningsFromSchematronOutput(schematronOutputType)
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
