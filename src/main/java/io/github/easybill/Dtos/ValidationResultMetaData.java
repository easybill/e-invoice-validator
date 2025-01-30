package io.github.easybill.Dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.easybill.Enums.XMLSyntaxType;
import io.github.easybill.Enums.XmlProfileType;
import org.checkerframework.checker.nullness.qual.NonNull;

public record ValidationResultMetaData(
    @NonNull @JsonProperty("xml_syntax_type") XMLSyntaxType xmlSyntaxType,
    @NonNull @JsonProperty("xml_profile_type") XmlProfileType xmlProfileType
) {}
