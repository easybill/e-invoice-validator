package io.github.easybill.Dtos;

import io.github.easybill.Enums.XMLSyntaxType;
import io.github.easybill.Enums.XmlProfileType;
import java.nio.charset.Charset;
import org.checkerframework.checker.nullness.qual.NonNull;

public record ValidationRequest(
    @NonNull XMLSyntaxType xmlSyntaxType,
    @NonNull XmlProfileType xmlProfileType,
    @NonNull Charset xmlCharset,
    @NonNull String xml
) {}
