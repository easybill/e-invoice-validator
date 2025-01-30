package io.github.easybill.Services.Validators;

import com.helger.commons.io.ByteArrayWrapper;
import com.helger.commons.io.resource.ClassPathResource;
import com.helger.schematron.sch.SchematronResourceSCH;
import io.github.easybill.Contracts.ISchematronValidator;
import io.github.easybill.Dtos.ValidationRequest;
import io.github.easybill.Dtos.ValidationResult;
import io.github.easybill.Dtos.ValidatorResults.EN16931ValidatorResult;
import io.github.easybill.Enums.XmlProfileType;
import io.github.easybill.Exceptions.ParsingException;
import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
public final class EN16931SchematronValidator implements ISchematronValidator {

    private final SchematronResourceSCH ciiSchematron;
    private final SchematronResourceSCH ublSchematron;

    public EN16931SchematronValidator() {
        ciiSchematron =
            new SchematronResourceSCH(
                new ClassPathResource("/EN16931/EN16931_1.3.13_CII.sch")
            );

        ublSchematron =
            new SchematronResourceSCH(
                new ClassPathResource("/EN16931/EN16931_1.3.13_UBL.sch")
            );

        if (!ciiSchematron.isValidSchematron()) {
            throw new RuntimeException(
                "Schematron validation for EN16931 CII failed"
            );
        }

        if (!ublSchematron.isValidSchematron()) {
            throw new RuntimeException(
                "Schematron validation for EN16931 UBL failed"
            );
        }
    }

    @Override
    public boolean validateSchematron() {
        if (!ciiSchematron.isValidSchematron()) {
            return false;
        }

        if (!ublSchematron.isValidSchematron()) {
            return false;
        }

        return true;
    }

    @Override
    public boolean supports(ValidationRequest validationRequest) {
        return validationRequest.xmlProfileType() == XmlProfileType.EN16931;
    }

    @Override
    public Optional<ValidationResult> validate(
        ValidationRequest validationRequest
    ) throws Exception {
        var bytesWrapper = new ByteArrayWrapper(
            validationRequest.xml().getBytes(validationRequest.xmlCharset()),
            false
        );

        try {
            var report =
                switch (validationRequest.xmlSyntaxType()) {
                    case CII -> Optional.ofNullable(
                        ciiSchematron.applySchematronValidationToSVRL(
                            bytesWrapper
                        )
                    );
                    case UBL -> Optional.ofNullable(
                        ublSchematron.applySchematronValidationToSVRL(
                            bytesWrapper
                        )
                    );
                };

            return report.map(schematronOutputType ->
                ValidationResult.of(
                    XmlProfileType.EN16931,
                    validationRequest,
                    EN16931ValidatorResult.of(schematronOutputType)
                )
            );
        } catch (IllegalArgumentException exception) {
            throw new ParsingException(exception);
        }
    }
}
