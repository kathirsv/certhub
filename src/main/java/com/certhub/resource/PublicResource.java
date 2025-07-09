package com.certhub.resource;

import com.amazonaws.services.s3.model.S3Object;
import com.certhub.dto.CertificateDto;
import com.certhub.entity.Certificate;
import com.certhub.service.CertificateService;
import com.certhub.service.RecaptchaService;
import com.certhub.service.S3Service;

import jakarta.inject.Inject;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;

@Path("/api/public")
@Produces(MediaType.APPLICATION_JSON)
public class PublicResource {

    @Inject
    RecaptchaService recaptchaService;

    @Inject
    S3Service s3Service;

    @Inject
    CertificateService certificateService;

    @GET
    @Path("/certificate/{shareableId}")
    public Response getCertificate(@PathParam("shareableId") String shareableId) {
        Certificate certificate = certificateService.findByShareableId(shareableId);
        if (certificate == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Certificate not found"))
                    .build();
        }

        return Response.ok(new CertificateDto(certificate)).build();
    }

    @GET
    @Path("/certificate/{shareableId}/download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadCertificate(@PathParam("shareableId") String shareableId) {
        Certificate certificate = certificateService.findByShareableId(shareableId);
        if (certificate == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Certificate not found"))
                    .build();
        }

        try {
            S3Object s3Object = s3Service.getFile(certificate.s3Key);
            InputStream inputStream = s3Object.getObjectContent();
            
            StreamingOutput stream = output -> {
                try (InputStream input = inputStream) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = input.read(buffer)) != -1) {
                        output.write(buffer, 0, bytesRead);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Failed to stream file", e);
                }
            };

            return Response.ok(stream)
                    .header("Content-Disposition", "attachment; filename=\"" + certificate.fileName + "\"")
                    .header("Content-Type", certificate.fileType)
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Failed to download file"))
                    .build();
        }
    }

    @GET
    @Path("/certificate/{shareableId}/preview")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response previewCertificate(@PathParam("shareableId") String shareableId) {
        Certificate certificate = certificateService.findByShareableId(shareableId);
        if (certificate == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Certificate not found"))
                    .build();
        }

        try {
            S3Object s3Object = s3Service.getFile(certificate.s3Key);
            InputStream inputStream = s3Object.getObjectContent();
            
            StreamingOutput stream = output -> {
                try (InputStream input = inputStream) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = input.read(buffer)) != -1) {
                        output.write(buffer, 0, bytesRead);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Failed to stream file", e);
                }
            };

            return Response.ok(stream)
                    .header("Content-Type", certificate.fileType)
                    .header("Content-Disposition", "inline; filename=\"" + certificate.fileName + "\"")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Failed to preview file"))
                    .build();
        }
    }

    public static class ErrorResponse {
        public String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }
}