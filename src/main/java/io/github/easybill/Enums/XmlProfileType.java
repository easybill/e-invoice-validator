package io.github.easybill.Enums;

import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;

public enum XmlProfileType {
    EN16931("urn:cen.eu:en16931:2017"),
    FACTURX_EXTENDED(
        "urn:cen.eu:en16931:2017#conformant#urn:factur-x.eu:1p0:extended"
    ),
    PEPPOL_30(
        "urn:cen.eu:en16931:2017#compliant#urn:fdc:peppol.eu:2017:poacc:billing:3.0"
    ),
    XRECHNUNG_30(
        "urn:cen.eu:en16931:2017#compliant#urn:xeinkauf.de:kosit:xrechnung_3.0"
    );

    private final String text;

    XmlProfileType(final @NonNull String text) {
        this.text = text;
    }

    public String toString() {
        return text;
    }

    public static Optional<XmlProfileType> tryFromString(final String text) {
        for (XmlProfileType type : XmlProfileType.values()) {
            if (type.text.equals(text)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }
}
