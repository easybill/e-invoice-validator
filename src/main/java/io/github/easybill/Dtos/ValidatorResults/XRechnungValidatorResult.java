package io.github.easybill.Dtos.ValidatorResults;

import com.helger.schematron.svrl.jaxb.SchematronOutputType;
import io.github.easybill.Dtos.ValidationResultField;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class XRechnungValidatorResult extends ValidatorResult {

    public XRechnungValidatorResult(
        @NonNull String name,
        @NonNull String version,
        @NonNull List<@NonNull ValidationResultField> errors,
        @NonNull List<@NonNull ValidationResultField> warnings
    ) {
        super(name, version, errors, warnings);
    }

    public static XRechnungValidatorResult of(
        @NonNull SchematronOutputType report
    ) {
        return new XRechnungValidatorResult(
            "XRechnung",
            "3.2",
            getErrorsFromSchematronOutput(report),
            getWarningsFromSchematronOutput(report)
        );
    }
}
