package com.certhub.resource;

import com.certhub.dto.LoginRequest;
import com.certhub.dto.LoginResponse;
import com.certhub.service.AuthService;
import com.certhub.service.RecaptchaService;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthService authService;

    @Inject
    RecaptchaService recaptchaService;

    @POST
    @Path("/login")
    public Response login(@Valid LoginRequest loginRequest) {
        if (!authService.authenticate(loginRequest.username, loginRequest.password)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(LoginResponse.failure("Invalid credentials"))
                    .build();
        }

        String sessionToken = authService.createSession(loginRequest.username);
        
        // Set session token as cookie
        NewCookie sessionCookie = new NewCookie.Builder("sessionId")
                .value(sessionToken)
                .path("/")
                .maxAge(24 * 60 * 60) // 24 hours
                .httpOnly(true)
                .build();
        
        return Response.ok(LoginResponse.success(sessionToken))
                .cookie(sessionCookie)
                .build();
    }

    @POST
    @Path("/logout")
    public Response logout() {
        authService.logout();
        
        // Clear session cookie
        NewCookie clearCookie = new NewCookie.Builder("sessionId")
                .value("")
                .path("/")
                .maxAge(0)
                .build();
        
        return Response.ok(new LogoutResponse("Logged out successfully"))
                .cookie(clearCookie)
                .build();
    }

    @GET
    @Path("/status")
    public Response getAuthStatus(@Context HttpHeaders headers) {
        boolean isAuthenticated = authService.isAuthenticated(headers);
        String currentUser = isAuthenticated ? authService.getCurrentUser() : null;
        return Response.ok(new AuthStatusResponse(isAuthenticated, currentUser)).build();
    }

    public static class LogoutResponse {
        public String message;

        public LogoutResponse(String message) {
            this.message = message;
        }
    }

    public static class AuthStatusResponse {
        public boolean authenticated;
        public String username;

        public AuthStatusResponse(boolean authenticated, String username) {
            this.authenticated = authenticated;
            this.username = username;
        }
    }
}