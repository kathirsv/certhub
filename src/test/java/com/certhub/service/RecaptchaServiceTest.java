package com.certhub.service;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class RecaptchaServiceTest {

    @Inject
    RecaptchaService recaptchaService;

    @Test
    public void testVerifyRecaptchaWithValidResponse() {
        // Using test keys from Google
        boolean result = recaptchaService.verifyRecaptcha("test-recaptcha-response");
        assertTrue(result);
    }

    @Test
    public void testVerifyRecaptchaWithInvalidResponse() {
        boolean result = recaptchaService.verifyRecaptcha("invalid-response");
        assertFalse(result);
    }

    @Test
    public void testVerifyRecaptchaWithNullResponse() {
        boolean result = recaptchaService.verifyRecaptcha(null);
        assertFalse(result);
    }

    @Test
    public void testVerifyRecaptchaWithEmptyResponse() {
        boolean result = recaptchaService.verifyRecaptcha("");
        assertFalse(result);
    }
}