package io.github.easybill.Dtos.ValidatorResults;

import com.helger.schematron.svrl.jaxb.SchematronOutputType;
import io.github.easybill.Dtos.ValidationResultField;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class EN16931ValidatorResult extends ValidatorResult {

    public EN16931ValidatorResult(
        @NonNull String name,
        @NonNull String version,
        @NonNull List<@NonNull ValidationResultField> errors,
        @NonNull List<@NonNull ValidationResultField> warnings
    ) {
        super(name, version, errors, warnings);
    }

    public static EN16931ValidatorResult of(
        @NonNull SchematronOutputType report
    ) {
        return new EN16931ValidatorResult(
            "EN16931",
            "1.3.13",
            getErrorsFromSchematronOutput(report),
            getWarningsFromSchematronOutput(report)
        );
    }
}
