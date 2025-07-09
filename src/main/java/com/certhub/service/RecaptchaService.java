package com.certhub.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class RecaptchaService {

    @ConfigProperty(name = "recaptcha.secret-key")
    String secretKey;

    @Inject
    ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public boolean verifyRecaptcha(String recaptchaResponse) {
        // For testing/development with test keys, always return true
        if ("6LeIxAcTAAAAAGG-vFI1TnRWxMZNFuojJ4WifJWe".equals(secretKey)) {
            return recaptchaResponse != null && !recaptchaResponse.isEmpty();
        }
        
        try {
            Map<String, String> params = new HashMap<>();
            params.put("secret", secretKey);
            params.put("response", recaptchaResponse);

            String postData = params.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .reduce((a, b) -> a + "&" + b)
                    .orElse("");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.google.com/recaptcha/api/siteverify"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(postData))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode jsonResponse = objectMapper.readTree(response.body());
            return jsonResponse.get("success").asBoolean();

        } catch (IOException | InterruptedException e) {
            return false;
        }
    }
}