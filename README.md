# e-invoice-validator
![Docker Image Version](https://img.shields.io/docker/v/easybill/e-invoice-validator)
[![Generic badge](https://img.shields.io/badge/License-MIT-blue.svg)]()

## Introduction
`e-invoice-validator` is a small service for validating XML against the official
schematron rules of factur-x, Peppol BIS, XRechnung and EN16931. It exposes a validation endpoint which takes the
to be validated XML and returns a JSON payload which contains possible warnings or errors. The HTTP status code indicates if the
provided XML is valid (200) or has issues (400). UBL and CII is supported.

### Currently supported validation artifacts:

- EN16931 v1.3.13
- XRechnung v3.2
- Peppol BIS 3.0
- factur-x v1.07.2
    - (only the profiles EN16931 and EXTENDED are supported)

## Usage
This service was mainly designed with containerization in mind. So general idea is to use the following
docker image and make HTTP-Requests from the main application to the service for validation.

- modifying / creating docker-compose.yaml

> The service exposes a health endpoint which can be used to check if the service ready to be used at /health

> You can find a OpenAPI documentation after you started the service at /swagger
```yaml
  e-invoice-validator:
    image: 'easybill/e-invoice-validator:latest'
    ports:
      - '8081:8080'
    environment:
        JAVA_TOOL_OPTIONS: -Xmx512m
    healthcheck:
      test: curl --fail http://localhost:8081/health || exit 0
      interval: 10s
      retries: 6
```

- starting docker compose
```
docker compose up --detach --wait --wait-timeout 30
```

- Example of using this service (PHP)
```PHP
<?php

declare(strict_types=1);

final class EN16931Validator
{
    public function isValid(string $xml): ?bool
    {
        $httpClient = new Client();

        $response = $httpClient->request('POST', 'http://localhost:8081/validation', [
            RequestOptions::HEADERS => [
                'Content-Type' => 'application/json',
            ],
            RequestOptions::BODY => $xml,
            RequestOptions::TIMEOUT => 10,
            RequestOptions::CONNECT_TIMEOUT => 10,
            RequestOptions::HTTP_ERRORS => false,
        ]);

        return 200 === $response->getStatusCode();
    }
}
```
- Example response in case the XML is valid

```JSON
{
  "meta": {
    "xml_syntax_type": "UBL",
    "xml_profile_type": "PEPPOL_30"
  },
  "validation_results": [
    {
      "name": "EN16931",
      "version": "1.3.13",
      "warnings": [],
      "errors": []
    },
    {
      "name": "Peppol BIS",
      "version": "3.0",
      "warnings": [],
      "errors": []
    }
  ],
  "is_valid": true
}
```


- Example response in case the XML is invalid

```JSON
{
  "meta": {
    "xml_syntax_type": "UBL",
    "xml_profile_type": "PEPPOL_30"
  },
  "validation_results": [
    {
      "name": "EN16931",
      "version": "1.3.13",
      "warnings": [],
      "errors": [
        {
          "rule_id": "BR-CL-14",
          "rule_location": "/*:Invoice[namespace-uri()='urn:oasis:names:specification:ubl:schema:xsd:Invoice-2'][1]/*:AccountingSupplierParty[namespace-uri()='urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2'][1]/*:Party[namespace-uri()='urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2'][1]/*:PostalAddress[namespace-uri()='urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2'][1]/*:Country[namespace-uri()='urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2'][1]/*:IdentificationCode[namespace-uri()='urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2'][1]",
          "rule_severity": "FATAL",
          "rule_messages": [
            "[BR-CL-14]-Country codes in an invoice MUST be coded using ISO code list 3166-1"
          ]
        }
      ]
    },
    {
      "name": "Peppol BIS",
      "version": "3.0",
      "warnings": [],
      "errors": []
    }
  ],
  "is_valid": false
}
```

## Insights
You may enable bug reporting via Bugsnag by supplying the env-variable `BUGSNAG_API_KEY`.
```yaml
  e-invoice-validator:
    image: 'easybill/e-invoice-validator:latest'
    ports:
      - '8081:8080'
    environment:
        JAVA_TOOL_OPTIONS: -Xmx512m
        BUGSNAG_API_KEY: <YOUR_API_KEY>
    healthcheck:
      test: curl --fail http://localhost:8081/health || exit 0
      interval: 10s
      retries: 6
```

## Issues & Contribution
Feel free to create pull-requests or issues if you have trouble with this service or any related resources. 

## Roadmap
TBA