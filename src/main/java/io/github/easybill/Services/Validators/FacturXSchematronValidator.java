package io.github.easybill.Services.Validators;

import com.helger.commons.io.ByteArrayWrapper;
import com.helger.commons.io.resource.ClassPathResource;
import com.helger.schematron.sch.SchematronResourceSCH;
import io.github.easybill.Contracts.ISchematronValidator;
import io.github.easybill.Dtos.ValidationRequest;
import io.github.easybill.Dtos.ValidationResult;
import io.github.easybill.Dtos.ValidatorResults.EN16931ValidatorResult;
import io.github.easybill.Dtos.ValidatorResults.FacturXValidatorResult;
import io.github.easybill.Enums.XMLSyntaxType;
import io.github.easybill.Enums.XmlProfileType;
import io.github.easybill.Exceptions.ParsingException;
import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
public final class FacturXSchematronValidator implements ISchematronValidator {

    private final SchematronResourceSCH en16931Schematron;
    private final SchematronResourceSCH facturXSchematron;

    public FacturXSchematronValidator() {
        en16931Schematron =
            new SchematronResourceSCH(
                new ClassPathResource("/EN16931/EN16931_1.3.13_CII.sch")
            );

        facturXSchematron =
            new SchematronResourceSCH(
                new ClassPathResource("/FacturX/Factur-X_1.07.2_EXTENDED.sch")
            );

        if (!en16931Schematron.isValidSchematron()) {
            throw new RuntimeException(
                "Schematron validation for EN16931 CII failed"
            );
        }

        if (!facturXSchematron.isValidSchematron()) {
            throw new RuntimeException(
                "Schematron validation for Factur-X Extended CII failed"
            );
        }
    }

    @Override
    public boolean validateSchematron() {
        return facturXSchematron.isValidSchematron();
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

            var facutrxReport = Optional.ofNullable(
                facturXSchematron.applySchematronValidationToSVRL(
                    new ByteArrayWrapper(bytes, false)
                )
            );

            return facutrxReport.map(schematronOutputType ->
                ValidationResult.of(
                    XmlProfileType.FACTURX_EXTENDED,
                    validationRequest,
                    EN16931ValidatorResult.of(en16931Report.get()),
                    FacturXValidatorResult.of(schematronOutputType)
                )
            );
        } catch (IllegalArgumentException exception) {
            throw new ParsingException(exception);
        }
    }
}
