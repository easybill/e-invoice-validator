package io.github.easybill.Contracts;

import io.github.easybill.Dtos.ValidationRequest;
import io.github.easybill.Dtos.ValidationResult;
import java.util.Optional;

public interface ISchematronValidator {
    boolean validateSchematron();

    boolean supports(ValidationRequest validationRequest);

    Optional<ValidationResult> validate(ValidationRequest validationRequest)
        throws Exception;
}
