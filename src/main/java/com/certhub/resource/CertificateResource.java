package com.certhub.resource;

import com.certhub.dto.CertificateDto;
import com.certhub.entity.Certificate;
import com.certhub.service.AuthService;
import com.certhub.service.CertificateService;
import com.certhub.service.S3Service;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Path("/api/certificates")
@Produces(MediaType.APPLICATION_JSON)
public class CertificateResource {

    @Inject
    S3Service s3Service;

    @Inject
    CertificateService certificateService;

    @Inject
    AuthService authService;

    @GET
    public Response getUserCertificates(@Context HttpHeaders headers) {
        // Check authentication
        if (!authService.isAuthenticated(headers)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("Authentication required"))
                    .build();
        }

        List<Certificate> certificates = certificateService.findAllCertificates();
        List<CertificateDto> certificateDtos = certificates.stream()
                .map(CertificateDto::new)
                .collect(Collectors.toList());
        return Response.ok(certificateDtos).build();
    }

    @POST
    @Path("/upload")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response uploadCertificate(CertificateUploadRequest request, @Context HttpHeaders headers) {
        // Check authentication
        if (!authService.isAuthenticated(headers)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("Authentication required"))
                    .build();
        }

        if (request.fileData == null || request.fileData.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("File data is required"))
                    .build();
        }

        if (request.fileName == null || request.fileName.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("File name is required"))
                    .build();
        }

        try {
            byte[] fileBytes = Base64.getDecoder().decode(request.fileData);
            
            if (fileBytes.length > 15 * 1024 * 1024) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("File size exceeds 15MB limit"))
                        .build();
            }

            String contentType = getContentType(request.fileName);
            if (!isValidFileType(contentType)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Only PDF and JPEG files are allowed"))
                        .build();
            }

            try (InputStream fileStream = new ByteArrayInputStream(fileBytes)) {
                String s3Key = s3Service.uploadFile(fileStream, request.fileName, 
                        contentType, fileBytes.length);

                Certificate certificate = new Certificate(
                        request.title,
                        request.credentialLink,
                        request.fileName,
                        contentType,
                        s3Key,
                        "certhub-certificates",
                        (long) fileBytes.length
                );
                certificateService.saveCertificate(certificate);

                return Response.ok(new CertificateDto(certificate)).build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid file data format"))
                    .build();
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Failed to upload file"))
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getCertificate(@PathParam("id") Long id, @Context HttpHeaders headers) {
        // Check authentication
        if (!authService.isAuthenticated(headers)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("Authentication required"))
                    .build();
        }

        Certificate certificate = certificateService.findById(id);
        
        if (certificate == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Certificate not found"))
                    .build();
        }

        return Response.ok(new CertificateDto(certificate)).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateCertificate(@PathParam("id") Long id, CertificateUpdateRequest request, @Context HttpHeaders headers) {
        // Check authentication
        if (!authService.isAuthenticated(headers)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("Authentication required"))
                    .build();
        }

        Certificate certificate = certificateService.findById(id);
        
        if (certificate == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Certificate not found"))
                    .build();
        }

        certificate.title = request.title;
        certificate.credentialLink = request.credentialLink;
        certificateService.updateCertificate(certificate);

        return Response.ok(new CertificateDto(certificate)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteCertificate(@PathParam("id") Long id) {
        Certificate certificate = certificateService.findById(id);
        
        if (certificate == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Certificate not found"))
                    .build();
        }

        s3Service.deleteFile(certificate.s3Key);
        certificateService.deleteCertificate(id);

        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/preview")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response previewCertificate(@PathParam("id") Long id, @Context HttpHeaders headers) {
        // Check authentication
        if (!authService.isAuthenticated(headers)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("Authentication required"))
                    .build();
        }

        Certificate certificate = certificateService.findById(id);
        
        if (certificate == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Certificate not found"))
                    .build();
        }

        try {
            com.amazonaws.services.s3.model.S3Object s3Object = s3Service.getFile(certificate.s3Key);
            java.io.InputStream inputStream = s3Object.getObjectContent();
            
            jakarta.ws.rs.core.StreamingOutput stream = output -> {
                try (java.io.InputStream input = inputStream) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = input.read(buffer)) != -1) {
                        output.write(buffer, 0, bytesRead);
                    }
                } catch (java.io.IOException e) {
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

    @GET
    @Path("/{id}/download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadCertificate(@PathParam("id") Long id, @Context HttpHeaders headers) {
        // Check authentication
        if (!authService.isAuthenticated(headers)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("Authentication required"))
                    .build();
        }

        Certificate certificate = certificateService.findById(id);
        
        if (certificate == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Certificate not found"))
                    .build();
        }

        try {
            com.amazonaws.services.s3.model.S3Object s3Object = s3Service.getFile(certificate.s3Key);
            java.io.InputStream inputStream = s3Object.getObjectContent();
            
            jakarta.ws.rs.core.StreamingOutput stream = output -> {
                try (java.io.InputStream input = inputStream) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = input.read(buffer)) != -1) {
                        output.write(buffer, 0, bytesRead);
                    }
                } catch (java.io.IOException e) {
                    throw new RuntimeException("Failed to stream file", e);
                }
            };

            return Response.ok(stream)
                    .header("Content-Type", certificate.fileType)
                    .header("Content-Disposition", "attachment; filename=\"" + certificate.fileName + "\"")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Failed to download file"))
                    .build();
        }
    }

    private String getContentType(String fileName) {
        if (fileName == null) return null;
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        return null;
    }

    private boolean isValidFileType(String contentType) {
        return contentType != null && (
                contentType.equals("application/pdf") ||
                contentType.equals("image/jpeg") ||
                contentType.equals("image/jpg")
        );
    }

    public static class CertificateUploadRequest {
        @NotNull
        public String title;
        public String credentialLink;
        @NotNull
        public String fileName;
        @NotNull
        public String fileData; // Base64 encoded file data
    }

    public static class CertificateUpdateRequest {
        public String title;
        public String credentialLink;
    }

    public static class ErrorResponse {
        public String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }
}