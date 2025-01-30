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
public final class FacturXSchematronValidator implements ISchematronValidator {

    private final SchematronResourceSCH ciiSchematron;

    public FacturXSchematronValidator() {
        ciiSchematron =
            new SchematronResourceSCH(
                new ClassPathResource("/FacturX/Factur-X_1.07.2_EXTENDED.sch")
            );

        if (!ciiSchematron.isValidSchematron()) {
            throw new RuntimeException(
                "Schematron validation for Factur-X Extended CII failed"
            );
        }
    }

    @Override
    public boolean validateSchematron() {
        return ciiSchematron.isValidSchematron();
    }

    @Override
    public boolean supports(ValidationRequest validationRequest) {
        return (
            validationRequest.xmlProfileType() ==
            XmlProfileType.FACTURX_EXTENDED &&
            validationRequest.xmlSyntaxType() == XMLSyntaxType.CII
        );
    }

    @Override
    public Optional<ValidationResult> validate(
        ValidationRequest validationRequest
    ) throws Exception {
        try {
            var report = Optional.ofNullable(
                ciiSchematron.applySchematronValidationToSVRL(
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
                    XmlProfileType.FACTURX_EXTENDED,
                    validationRequest,
                    schematronOutputType
                )
            );
        } catch (IllegalArgumentException exception) {
            throw new ParsingException(exception);
        }
    }
}
