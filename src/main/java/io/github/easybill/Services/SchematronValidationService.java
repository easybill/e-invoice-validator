package io.github.easybill.Services;

import io.github.easybill.Contracts.ISchematronValidationService;
import io.github.easybill.Contracts.ISchematronValidator;
import io.github.easybill.Dtos.ValidationRequest;
import io.github.easybill.Dtos.ValidationResult;
import io.github.easybill.Enums.XMLSyntaxType;
import io.github.easybill.Enums.XmlProfileType;
import io.github.easybill.Exceptions.InvalidProfileException;
import io.github.easybill.Exceptions.InvalidXmlException;
import io.github.easybill.Exceptions.ValidationChainException;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mozilla.universalchardet.UniversalDetector;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@Singleton
public final class SchematronValidationService
    implements ISchematronValidationService {

    private final Instance<ISchematronValidator> schematronValidators;

    public SchematronValidationService(
        Instance<ISchematronValidator> schematronValidators
    ) {
        this.schematronValidators = schematronValidators;
    }

    @Override
    public @NonNull ValidationResult validateXml(
        @NonNull InputStream inputStream
    ) throws Exception {
        var validationRequest = createValidationRequestFromInputStream(
            inputStream
        );

        for (ISchematronValidator validator : this.schematronValidators) {
            if (!validator.supports(validationRequest)) {
                continue;
            }

            return validator
                .validate(validationRequest)
                .orElseThrow(ValidationChainException::new);
        }

        throw new Exception(
            "Schematron validation failed. No validator found."
        );
    }

    private ValidationRequest createValidationRequestFromInputStream(
        @NonNull InputStream inputStream
    )
        throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        var bytesFromSteam = inputStream.readAllBytes();

        var charset = determineCharsetForXmlPayload(bytesFromSteam);

        var xml = new String(bytesFromSteam, charset);

        if (isXmlInvalid(xml)) {
            throw new InvalidXmlException();
        }

        xml = removeBOM(xml);
        xml = removeInvalidCharsFromProlog(xml);

        var xmlSyntaxType = determineXmlSyntax(xml)
            .orElseThrow(InvalidXmlException::new);

        var xmlProfileType = determineProfileTypeForXML(xmlSyntaxType, xml)
            .orElseThrow(InvalidProfileException::new);

        return new ValidationRequest(
            xmlSyntaxType,
            xmlProfileType,
            charset,
            xml
        );
    }

    @Override
    public boolean isLoadedSchematronValid() {
        return true;
    }

    private boolean isXmlInvalid(@NonNull String xml) {
        return xml.isBlank() || (!checkIfUblXml(xml) && !checkIfCiiXml(xml));
    }

    private Optional<XmlProfileType> determineProfileTypeForXML(
        @NonNull XMLSyntaxType xmlSyntaxType,
        @NonNull String xml
    )
        throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        DocumentBuilderFactory builderFactory =
            DocumentBuilderFactory.newInstance();

        builderFactory.setNamespaceAware(true);
        builderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        DocumentBuilder db = builderFactory.newDocumentBuilder();

        Document doc = db.parse(new InputSource(new StringReader(xml)));

        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();

        XPathExpression xpathExpression =
            switch (xmlSyntaxType) {
                case CII -> xpath.compile(
                    "//*[local-name()='ExchangedDocumentContext']/*[local-name()='GuidelineSpecifiedDocumentContextParameter']/*[local-name()='ID']"
                );
                case UBL -> xpath.compile(
                    "//*[local-name()='CustomizationID']"
                );
            };

        Node node = (Node) xpathExpression.evaluate(doc, XPathConstants.NODE);

        if (node == null) {
            return Optional.empty();
        }

        var nodeContent = Optional.ofNullable(node.getTextContent());

        if (nodeContent.isEmpty()) {
            return Optional.empty();
        }

        return XmlProfileType.tryFromString(nodeContent.get());
    }

    private Charset determineCharsetForXmlPayload(byte[] bytes)
        throws InvalidXmlException {
        UniversalDetector detector = new UniversalDetector();

        detector.handleData(bytes, 0, bytes.length);
        detector.dataEnd();

        String encoding = detector.getDetectedCharset();

        detector.reset();

        if (encoding != null) {
            return Charset.forName(encoding);
        }

        throw new InvalidXmlException();
    }

    private Optional<XMLSyntaxType> determineXmlSyntax(@NonNull String xml) {
        if (checkIfCiiXml(xml)) {
            return Optional.of(XMLSyntaxType.CII);
        }

        if (checkIfUblXml(xml)) {
            return Optional.of(XMLSyntaxType.UBL);
        }

        return Optional.empty();
    }

    private boolean checkIfCiiXml(@NonNull CharSequence payload) {
        return Pattern
            .compile("[<:](CrossIndustryInvoice)")
            .matcher(payload)
            .find();
    }

    private boolean checkIfUblXml(@NonNull CharSequence payload) {
        return Pattern
            .compile("[<:](Invoice|CreditNote)")
            .matcher(payload)
            .find();
    }

    private @NonNull String removeBOM(@NonNull String payload) {
        String UTF8_BOM = "\uFEFF";
        String UTF16LE_BOM = "\uFFFE";
        String UTF16BE_BOM = "\uFEFF";

        if (payload.isEmpty()) {
            return payload;
        }

        if (
            payload.startsWith(UTF8_BOM) ||
            payload.startsWith(UTF16LE_BOM) ||
            payload.startsWith(UTF16BE_BOM)
        ) {
            return payload.substring(1);
        }

        return payload;
    }

    private @NonNull String removeInvalidCharsFromProlog(
        @NonNull String payload
    ) {
        var indexOfXmlIntro = payload.indexOf("<?xml version");

        if (indexOfXmlIntro == 0) {
            return payload;
        }

        return payload.substring(indexOfXmlIntro);
    }
}
