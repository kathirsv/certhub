package com.certhub.resource;

import com.certhub.dto.LoginRequest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
public class AuthResourceTest {

    @Test
    public void testAuthStatus() {
        given()
                .when().get("/api/auth/status")
                .then()
                .statusCode(200)
                .body("authenticated", is(false));
    }

    @Test
    public void testLoginWithValidCredentials() {
        LoginRequest loginRequest = new LoginRequest("admin", "admin123", "test-recaptcha-response");

        given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("success", is(true))
                .body("token", notNullValue());
    }

    @Test
    public void testLoginWithInvalidCredentials() {
        LoginRequest loginRequest = new LoginRequest("admin", "wrongpassword", "test-recaptcha-response");

        given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post("/api/auth/login")
                .then()
                .statusCode(401)
                .body("success", is(false))
                .body("message", is("Invalid credentials"));
    }

    @Test
    public void testLoginWithInvalidUsername() {
        LoginRequest loginRequest = new LoginRequest("invaliduser", "admin123", "test-recaptcha-response");

        given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post("/api/auth/login")
                .then()
                .statusCode(401)
                .body("success", is(false))
                .body("message", is("Invalid credentials"));
    }

    @Test
    public void testLogout() {
        given()
                .when().post("/api/auth/logout")
                .then()
                .statusCode(200)
                .body("message", is("Logged out successfully"));
    }
}