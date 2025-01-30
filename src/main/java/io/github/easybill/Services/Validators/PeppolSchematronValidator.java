package io.github.easybill.Services.Validators;

import com.helger.commons.io.ByteArrayWrapper;
import com.helger.commons.io.resource.ClassPathResource;
import com.helger.schematron.sch.SchematronResourceSCH;
import io.github.easybill.Contracts.ISchematronValidator;
import io.github.easybill.Dtos.ValidationRequest;
import io.github.easybill.Dtos.ValidationResult;
import io.github.easybill.Dtos.ValidatorResults.EN16931ValidatorResult;
import io.github.easybill.Dtos.ValidatorResults.PeppolValidatorResult;
import io.github.easybill.Enums.XMLSyntaxType;
import io.github.easybill.Enums.XmlProfileType;
import io.github.easybill.Exceptions.ParsingException;
import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
public final class PeppolSchematronValidator implements ISchematronValidator {

    private final SchematronResourceSCH en16931Schematron;
    private final SchematronResourceSCH peppolBisSchematron;

    public PeppolSchematronValidator() {
        en16931Schematron =
            new SchematronResourceSCH(
                new ClassPathResource("/EN16931/EN16931_1.3.13_UBL.sch")
            );

        peppolBisSchematron =
            new SchematronResourceSCH(
                new ClassPathResource("/Peppol/PEPPOL_BIS_BILLING_3.0.sch")
            );

        if (!en16931Schematron.isValidSchematron()) {
            throw new RuntimeException(
                "Schematron validation for EN16931 UBL failed"
            );
        }

        if (!peppolBisSchematron.isValidSchematron()) {
            throw new RuntimeException(
                "Schematron validation for Peppol UBL failed"
            );
        }
    }

    @Override
    public boolean validateSchematron() {
        return peppolBisSchematron.isValidSchematron();
    }

    @Override
    public boolean supports(ValidationRequest validationRequest) {
        return (
            validationRequest.xmlProfileType() == XmlProfileType.PEPPOL_30 &&
            validationRequest.xmlSyntaxType() == XMLSyntaxType.UBL
        );
    }

    @Override
    public Optional<ValidationResult> validate(
        ValidationRequest validationRequest
    ) throws Exception {
        try {
            byte[] bytes = validationRequest
                .xml()
                .getBytes(validationRequest.xmlCharset());

            var en16931Report = Optional.ofNullable(
                en16931Schematron.applySchematronValidationToSVRL(
                    new ByteArrayWrapper(bytes, false)
                )
            );

            if (en16931Report.isEmpty()) {
                return Optional.empty();
            }

            var peppolReport = Optional.ofNullable(
                peppolBisSchematron.applySchematronValidationToSVRL(
                    new ByteArrayWrapper(bytes, false)
                )
            );

            return peppolReport.map(schematronOutputType ->
                ValidationResult.of(
                    XmlProfileType.PEPPOL_30,
                    validationRequest,
                    EN16931ValidatorResult.of(en16931Report.get()),
                    PeppolValidatorResult.of(schematronOutputType)
                )
            );
        } catch (IllegalArgumentException exception) {
            throw new ParsingException(exception);
        }
    }
}
