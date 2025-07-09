package com.certhub.dto;

public class LoginResponse {
    public String token;
    public String message;
    public boolean success;

    public LoginResponse() {}

    public LoginResponse(String token, String message, boolean success) {
        this.token = token;
        this.message = message;
        this.success = success;
    }

    public static LoginResponse success(String token) {
        return new LoginResponse(token, "Login successful", true);
    }

    public static LoginResponse failure(String message) {
        return new LoginResponse(null, message, false);
    }
}