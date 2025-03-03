package io.github.easybill.Controllers;

import io.github.easybill.Contracts.ISchematronValidationService;
import io.github.easybill.Dtos.ValidationResult;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.io.InputStream;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.jboss.resteasy.reactive.RestResponse;

@Path("/")
public final class ValidationController {

    private final ISchematronValidationService validationService;

    public ValidationController(
        ISchematronValidationService validationService
    ) {
        this.validationService = validationService;
    }

    @POST
    @Path("/validation")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(
        {
            @APIResponse(
                responseCode = "200",
                description = "The submitted XML is valid "
            ),
            @APIResponse(
                responseCode = "422",
                description = "The provided XML could not be used for validation"
            ),
        }
    )
    public RestResponse<@NonNull ValidationResult> validation(
        InputStream xmlInputStream
    ) throws Exception {
        return RestResponse.ResponseBuilder
            .create(
                RestResponse.Status.OK,
                validationService.validateXml(xmlInputStream)
            )
            .type(MediaType.APPLICATION_JSON)
            .build();
    }
}
