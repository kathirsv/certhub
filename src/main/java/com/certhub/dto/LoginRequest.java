package com.certhub.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank
    public String username;
    
    @NotBlank
    public String password;
    
    @NotBlank
    public String recaptchaResponse;

    public LoginRequest() {}

    public LoginRequest(String username, String password, String recaptchaResponse) {
        this.username = username;
        this.password = password;
        this.recaptchaResponse = recaptchaResponse;
    }
}