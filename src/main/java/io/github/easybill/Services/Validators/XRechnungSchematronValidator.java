package io.github.easybill.Services.Validators;

import com.helger.commons.io.ByteArrayWrapper;
import com.helger.commons.io.resource.ClassPathResource;
import com.helger.schematron.sch.SchematronResourceSCH;
import io.github.easybill.Contracts.ISchematronValidator;
import io.github.easybill.Dtos.ValidationRequest;
import io.github.easybill.Dtos.ValidationResult;
import io.github.easybill.Dtos.ValidatorResults.EN16931ValidatorResult;
import io.github.easybill.Dtos.ValidatorResults.XRechnungValidatorResult;
import io.github.easybill.Enums.XmlProfileType;
import io.github.easybill.Exceptions.ParsingException;
import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
public final class XRechnungSchematronValidator
    implements ISchematronValidator {

    private final SchematronResourceSCH en16931CiiSchematron;
    private final SchematronResourceSCH en16931UblSchematron;
    private final SchematronResourceSCH xRechnungCiiSchematron;
    private final SchematronResourceSCH xRechnungUblSchematron;

    public XRechnungSchematronValidator() {
        en16931CiiSchematron =
            new SchematronResourceSCH(
                new ClassPathResource("/EN16931/EN16931_1.3.13_CII.sch")
            );

        en16931UblSchematron =
            new SchematronResourceSCH(
                new ClassPathResource("/EN16931/EN16931_1.3.13_UBL.sch")
            );

        xRechnungCiiSchematron =
            new SchematronResourceSCH(
                new ClassPathResource("/XRechnung/XRechnung_3.2_CII.sch")
            );

        xRechnungUblSchematron =
            new SchematronResourceSCH(
                new ClassPathResource("/XRechnung/XRechnung_3.2_UBL.sch")
            );

        if (!en16931CiiSchematron.isValidSchematron()) {
            throw new RuntimeException(
                "Schematron validation for EN16931 CII failed"
            );
        }

        if (!en16931UblSchematron.isValidSchematron()) {
            throw new RuntimeException(
                "Schematron validation for EN16931 UBL failed"
            );
        }

        if (!xRechnungCiiSchematron.isValidSchematron()) {
            throw new RuntimeException(
                "Schematron validation for XRechnung CII failed"
            );
        }

        if (!xRechnungUblSchematron.isValidSchematron()) {
            throw new RuntimeException(
                "Schematron validation for XRechnung UBL failed"
            );
        }
    }

    @Override
    public boolean validateSchematron() {
        if (!xRechnungCiiSchematron.isValidSchematron()) {
            return false;
        }

        if (!xRechnungUblSchematron.isValidSchematron()) {
            return false;
        }

        return true;
    }

    @Override
    public boolean supports(ValidationRequest validationRequest) {
        return (
            validationRequest.xmlProfileType() == XmlProfileType.XRECHNUNG_30
        );
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
            var en16931Report =
                switch (validationRequest.xmlSyntaxType()) {
                    case CII -> Optional.ofNullable(
                        en16931CiiSchematron.applySchematronValidationToSVRL(
                            bytesWrapper
                        )
                    );
                    case UBL -> Optional.ofNullable(
                        en16931UblSchematron.applySchematronValidationToSVRL(
                            bytesWrapper
                        )
                    );
                };

            if (en16931Report.isEmpty()) {
                return Optional.empty();
            }

            var xRechnungReport =
                switch (validationRequest.xmlSyntaxType()) {
                    case CII -> Optional.ofNullable(
                        xRechnungCiiSchematron.applySchematronValidationToSVRL(
                            bytesWrapper
                        )
                    );
                    case UBL -> Optional.ofNullable(
                        xRechnungUblSchematron.applySchematronValidationToSVRL(
                            bytesWrapper
                        )
                    );
                };

            return xRechnungReport.map(schematronOutputType ->
                ValidationResult.of(
                    XmlProfileType.XRECHNUNG_30,
                    validationRequest,
                    EN16931ValidatorResult.of(en16931Report.get()),
                    XRechnungValidatorResult.of(schematronOutputType)
                )
            );
        } catch (IllegalArgumentException exception) {
            throw new ParsingException(exception);
        }
    }
}
