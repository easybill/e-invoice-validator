FROM eclipse-temurin:21-alpine

WORKDIR .

COPY ./build/e-invoice-validator--runner.jar .

ENTRYPOINT ["java", "-jar", "e-invoice-validator--runner.jar"]