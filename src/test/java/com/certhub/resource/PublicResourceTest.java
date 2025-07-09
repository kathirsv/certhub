package com.certhub.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class PublicResourceTest {

    @Test
    public void testGetPublicCertificateWithoutRecaptcha() {
        given()
                .when().get("/api/public/certificate/some-shareable-id")
                .then()
                .statusCode(400)
                .body("message", is("Invalid reCAPTCHA"));
    }

    @Test
    public void testGetPublicCertificateWithInvalidShareableId() {
        given()
                .queryParam("recaptcha", "test-recaptcha-response")
                .when().get("/api/public/certificate/invalid-shareable-id")
                .then()
                .statusCode(404)
                .body("message", is("Certificate not found"));
    }

    @Test
    public void testDownloadPublicCertificateWithoutRecaptcha() {
        given()
                .when().get("/api/public/certificate/some-shareable-id/download")
                .then()
                .statusCode(400)
                .body("message", is("Invalid reCAPTCHA"));
    }

    @Test
    public void testDownloadPublicCertificateWithInvalidShareableId() {
        given()
                .queryParam("recaptcha", "test-recaptcha-response")
                .when().get("/api/public/certificate/invalid-shareable-id/download")
                .then()
                .statusCode(404)
                .body("message", is("Certificate not found"));
    }
}