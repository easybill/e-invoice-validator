package io.github.easybill.Dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.helger.schematron.svrl.jaxb.SchematronOutputType;
import io.github.easybill.Enums.XmlProfileType;
import java.util.Arrays;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;

public record ValidationResult(
    @NonNull ValidationResultMetaData meta,
    @JsonProperty("validation_results")
    @NonNull
    List<@NonNull ValidatorResult> validationResults
) {
    @JsonProperty("is_valid")
    public boolean isValid() {
        return validationResults
            .stream()
            .allMatch(element -> element.errors().isEmpty());
    }

    public static ValidationResult of(
        XmlProfileType xmlProfileType,
        ValidationRequest validationRequest,
        SchematronOutputType... schematronOutputTypes
    ) {
        return new ValidationResult(
            new ValidationResultMetaData(
                validationRequest.xmlSyntaxType(),
                xmlProfileType
            ),
            Arrays
                .stream(schematronOutputTypes)
                .map(ValidatorResult::of)
                .toList()
        );
    }
}
