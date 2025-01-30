package io.github.easybill;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.github.easybill.Enums.XMLSyntaxType;
import io.github.easybill.Enums.XmlProfileType;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
class ValidationControllerTest {

    @Test
    void testValidationEndpointWhenInvokedWithWrongMethod() {
        given().when().get("/validation").then().statusCode(405);
    }

    @Test
    void testValidationEndpointWhenInvokedWithAnEmptyPayload() {
        given().when().post("/validation").then().statusCode(415);
    }

    @Test
    void testValidationEndpointWithEmptyPayload() throws IOException {
        given()
            .body(loadFixtureFileAsStream("Invalid/Invalid_003.xml"))
            .contentType(ContentType.XML)
            .when()
            .post("/validation")
            .then()
            .statusCode(422)
            .body("error", equalTo("The provided XML is not valid"));
    }

    @Test
    void testUnknownProfileXml() throws IOException {
        given()
            .body(loadFixtureFileAsStream("Invalid/Invalid_001.xml"))
            .contentType(ContentType.XML)
            .when()
            .post("/validation")
            .then()
            .statusCode(422)
            .body(
                "error",
                equalTo(
                    "The validation profile could not be determined. Maybe the profile is not supported."
                )
            );
    }

    @Test
    void testValidationEndpointWithPayloadIncludingCharsInProlog()
        throws IOException {
        given()
            .body(loadFixtureFileAsStream("Invalid/Invalid_004.xml"))
            .contentType(ContentType.XML)
            .when()
            .post("/validation")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("is_valid", equalTo(false))
            .body("errors", not(empty()));
    }

    static Stream<Arguments> providerValuesForDifferentEncodings() {
        return Stream.of(
            Arguments.of("Peppol/Peppol_008.xml"),
            Arguments.of("Peppol/Peppol_011.xml"),
            Arguments.of("EN16931/EN16931_018.xml")
        );
    }

    @ParameterizedTest
    @MethodSource("providerValuesForDifferentEncodings")
    void testValidationEndpointWithDifferentEncodings(
        @NonNull String fixtureFileName
    ) throws IOException {
        given()
            .body(loadFixtureFileAsStream(fixtureFileName))
            .contentType(ContentType.XML)
            .when()
            .post("/validation")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("is_valid", equalTo(true))
            .body("errors", empty());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "EN16931/EN16931_001.xml",
            "EN16931/EN16931_002.xml",
            "EN16931/EN16931_003.xml",
            "EN16931/EN16931_004.xml",
            "EN16931/EN16931_005.xml",
            "EN16931/EN16931_006.xml",
            "EN16931/EN16931_007.xml",
            "EN16931/EN16931_008.xml",
            "EN16931/EN16931_009.xml",
            "EN16931/EN16931_010.xml",
            "EN16931/EN16931_011.xml",
            "EN16931/EN16931_012.xml",
            "EN16931/EN16931_013.xml",
            "EN16931/EN16931_014.xml",
            "EN16931/EN16931_015.xml",
            "EN16931/EN16931_016.xml",
            "EN16931/EN16931_017.xml",
            "EN16931/EN16931_018.xml",
            "EN16931/EN16931_019.xml",
            "EN16931/EN16931_020.xml",
            "EN16931/EN16931_021.xml",
            "EN16931/EN16931_022.xml",
            "EN16931/EN16931_023.xml",
            "EN16931/EN16931_024.xml",
        }
    )
    void testValidEN16931Documents(@NonNull String fixtureFileName)
        throws IOException {
        given()
            .body(loadFixtureFileAsStream(fixtureFileName))
            .contentType(ContentType.XML)
            .when()
            .post("/validation")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(
                "meta.xml_profile_type",
                equalTo(XmlProfileType.EN16931.name())
            )
            .body("is_valid", equalTo(true))
            .body("errors", empty());
    }

    @ParameterizedTest
    @ValueSource(strings = { "EN16931/EN16931_025.xml" })
    void testInvalidEN16931Documents(@NonNull String fixtureFileName)
        throws IOException {
        given()
            .body(loadFixtureFileAsStream(fixtureFileName))
            .contentType(ContentType.XML)
            .when()
            .post("/validation")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(
                "meta.xml_profile_type",
                equalTo(XmlProfileType.EN16931.name())
            )
            .body("is_valid", equalTo(false))
            .body("errors", not(empty()));
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "FacturX/FacturX_004.xml",
            "FacturX/FacturX_007.xml",
            "FacturX/FacturX_010.xml",
            "FacturX/FacturX_011.xml",
            "FacturX/FacturX_016.xml",
            "FacturX/FacturX_018.xml",
            "FacturX/FacturX_019.xml",
            "FacturX/FacturX_020.xml",
        }
    )
    void testValidFacturXDocuments(@NonNull String fixtureFileName)
        throws IOException {
        given()
            .body(loadFixtureFileAsStream(fixtureFileName))
            .contentType(ContentType.XML)
            .when()
            .post("/validation")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("meta.xml_syntax_type", equalTo(XMLSyntaxType.CII.name()))
            .body("is_valid", equalTo(true))
            .body("errors", empty());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "FacturX/FacturX_001.xml",
            "FacturX/FacturX_003.xml",
            "FacturX/FacturX_008.xml",
            "FacturX/FacturX_014.xml",
            "FacturX/FacturX_015.xml",
            "FacturX/FacturX_018.xml",
        }
    )
    void testValidFacturXExtendedDocuments(@NonNull String fixtureFileName)
        throws IOException {
        given()
            .body(loadFixtureFileAsStream(fixtureFileName))
            .contentType(ContentType.XML)
            .when()
            .post("/validation")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("meta.xml_syntax_type", equalTo(XMLSyntaxType.CII.name()))
            .body(
                "meta.xml_profile_type",
                equalTo(XmlProfileType.FACTURX_EXTENDED.name())
            )
            .body("is_valid", equalTo(true))
            .body("errors", empty());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "FacturX/FacturX_002.xml",
            "FacturX/FacturX_005.xml",
            "FacturX/FacturX_006.xml",
            "FacturX/FacturX_009.xml",
            "FacturX/FacturX_012.xml",
            "FacturX/FacturX_013.xml",
        }
    )
    void testInvalidProfilesFacturXDocuments(@NonNull String fixtureFileName)
        throws IOException {
        given()
            .body(loadFixtureFileAsStream(fixtureFileName))
            .contentType(ContentType.XML)
            .when()
            .post("/validation")
            .then()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .body(
                "error",
                equalTo(
                    "The validation profile could not be determined. Maybe the profile is not supported."
                )
            );
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "Peppol/Peppol_001.xml",
            "Peppol/Peppol_003.xml",
            "Peppol/Peppol_004.xml",
            "Peppol/Peppol_005.xml",
            "Peppol/Peppol_007.xml",
            "Peppol/Peppol_008.xml",
            "Peppol/Peppol_009.xml",
            "Peppol/Peppol_010.xml",
            "Peppol/Peppol_011.xml",
            "Peppol/Peppol_012.xml",
            "Peppol/Peppol_013.xml",
        }
    )
    void testValidPeppolDocuments(@NonNull String fixtureFileName)
        throws IOException {
        given()
            .body(loadFixtureFileAsStream(fixtureFileName))
            .contentType(ContentType.XML)
            .when()
            .post("/validation")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("meta.xml_syntax_type", equalTo(XMLSyntaxType.UBL.name()))
            .body(
                "meta.xml_profile_type",
                equalTo(XmlProfileType.PEPPOL_30.name())
            )
            .body("is_valid", equalTo(true))
            .body("errors", empty());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "Peppol/Peppol_002.xml",
            "Peppol/Peppol_006.xml",
            "Peppol/Peppol_014.xml",
        }
    )
    void testInvalidPeppolDocuments(@NonNull String fixtureFileName)
        throws IOException {
        given()
            .body(loadFixtureFileAsStream(fixtureFileName))
            .contentType(ContentType.XML)
            .when()
            .post("/validation")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("meta.xml_syntax_type", equalTo(XMLSyntaxType.UBL.name()))
            .body(
                "meta.xml_profile_type",
                equalTo(XmlProfileType.PEPPOL_30.name())
            )
            .body("is_valid", equalTo(false))
            .body("errors", not(empty()));
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "XRechnung/XRechnung_001.xml",
            "XRechnung/XRechnung_002.xml",
            "XRechnung/XRechnung_003.xml",
            "XRechnung/XRechnung_005.xml",
        }
    )
    void testValidXRechnungDocuments(@NonNull String fixtureFileName)
        throws IOException {
        given()
            .body(loadFixtureFileAsStream(fixtureFileName))
            .contentType(ContentType.XML)
            .when()
            .post("/validation")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(
                "meta.xml_profile_type",
                equalTo(XmlProfileType.XRECHNUNG_30.name())
            )
            .body("is_valid", equalTo(true))
            .body("errors", empty());
    }

    @ParameterizedTest
    @ValueSource(strings = { "XRechnung/XRechnung_004.xml" })
    void testInvalidXRechnungDocuments(@NonNull String fixtureFileName)
        throws IOException {
        given()
            .body(loadFixtureFileAsStream(fixtureFileName))
            .contentType(ContentType.XML)
            .when()
            .post("/validation")
            .then()
            .statusCode(422)
            .contentType(ContentType.JSON)
            .body(
                "error",
                equalTo(
                    "The validation profile could not be determined. Maybe the profile is not supported."
                )
            );
    }

    static Stream<Arguments> providerValuesValidationEndpointWithInvalidPayload() {
        return Stream.of(
            // Invalid currency code. HRK is deprecated
            Arguments.of("Invalid/Invalid_004.xml")
        );
    }

    @ParameterizedTest
    @MethodSource("providerValuesValidationEndpointWithInvalidPayload")
    void testValidationEndpointWithInvalidPayload(
        @NonNull String fixtureFileName
    ) throws IOException {
        given()
            .body(loadFixtureFileAsStream(fixtureFileName))
            .contentType(ContentType.XML)
            .when()
            .post("/validation")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("is_valid", equalTo(false))
            .body("errors", not(empty()));
    }

    InputStream loadFixtureFileAsStream(@NonNull String fixtureFileName)
        throws IOException {
        return Objects
            .requireNonNull(
                Thread
                    .currentThread()
                    .getContextClassLoader()
                    .getResource(fixtureFileName)
            )
            .openStream();
    }
}
