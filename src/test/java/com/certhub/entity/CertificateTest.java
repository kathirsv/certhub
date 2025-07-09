package com.certhub.entity;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class CertificateTest {

    @Test
    public void testCertificateCreation() {
        Certificate certificate = new Certificate(
            "Test Certificate",
            "https://example.com/credential",
            "test.pdf",
            "application/pdf",
            "s3-key-123",
            "test-bucket",
            1024L
        );

        assertNotNull(certificate.id);
        assertEquals("Test Certificate", certificate.title);
        assertEquals("https://example.com/credential", certificate.credentialLink);
        assertEquals("test.pdf", certificate.fileName);
        assertEquals("application/pdf", certificate.fileType);
        assertEquals("s3-key-123", certificate.s3Key);
        assertEquals("test-bucket", certificate.s3Bucket);
        assertEquals(1024L, certificate.fileSize);
        assertNotNull(certificate.shareableId);
        assertNotNull(certificate.uploadedAt);
    }

    @Test
    public void testUniqueShareableId() {
        Certificate cert1 = new Certificate("Cert 1", "https://example.com/1", "file1.pdf", "application/pdf", "key1", "bucket", 1024L);
        Certificate cert2 = new Certificate("Cert 2", "https://example.com/2", "file2.pdf", "application/pdf", "key2", "bucket", 2048L);

        assertNotEquals(cert1.shareableId, cert2.shareableId);
    }
}