package io.github.easybill.Dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.easybill.Dtos.ValidatorResults.ValidatorResult;
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
            .allMatch(element -> element.getErrors().isEmpty());
    }

    public static ValidationResult of(
        XmlProfileType xmlProfileType,
        ValidationRequest validationRequest,
        ValidatorResult... validatorResults
    ) {
        return new ValidationResult(
            new ValidationResultMetaData(
                validationRequest.xmlSyntaxType(),
                xmlProfileType
            ),
            Arrays.stream(validatorResults).toList()
        );
    }
}
