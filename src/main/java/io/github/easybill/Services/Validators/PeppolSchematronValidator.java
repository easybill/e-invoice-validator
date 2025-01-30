package io.github.easybill.Services.Validators;

import com.helger.commons.io.ByteArrayWrapper;
import com.helger.commons.io.resource.ClassPathResource;
import com.helger.schematron.sch.SchematronResourceSCH;
import io.github.easybill.Contracts.ISchematronValidator;
import io.github.easybill.Dtos.ValidationRequest;
import io.github.easybill.Dtos.ValidationResult;
import io.github.easybill.Enums.XMLSyntaxType;
import io.github.easybill.Enums.XmlProfileType;
import io.github.easybill.Exceptions.ParsingException;
import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
public final class PeppolSchematronValidator implements ISchematronValidator {

    private final SchematronResourceSCH ublSchematron;

    public PeppolSchematronValidator() {
        ublSchematron =
            new SchematronResourceSCH(
                new ClassPathResource("/Peppol/PEPPOL_BIS_BILLING_3.0.sch")
            );

        if (!ublSchematron.isValidSchematron()) {
            throw new RuntimeException(
                "Schematron validation for Peppol UBL failed"
            );
        }
    }

    @Override
    public boolean validateSchematron() {
        return ublSchematron.isValidSchematron();
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
            var report = Optional.ofNullable(
                ublSchematron.applySchematronValidationToSVRL(
                    new ByteArrayWrapper(
                        validationRequest
                            .xml()
                            .getBytes(validationRequest.xmlCharset()),
                        false
                    )
                )
            );

            return report.map(schematronOutputType ->
                ValidationResult.of(
                    XmlProfileType.PEPPOL_30,
                    validationRequest,
                    schematronOutputType
                )
            );
        } catch (IllegalArgumentException exception) {
            throw new ParsingException(exception);
        }
    }
}
