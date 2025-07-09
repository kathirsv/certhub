package com.certhub.service;

import com.certhub.entity.Certificate;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@ApplicationScoped
public class CertificateService {

    private final Map<Long, Certificate> certificates = new ConcurrentHashMap<>();
    private final Map<String, Certificate> certificatesByShareableId = new ConcurrentHashMap<>();

    public void saveCertificate(Certificate certificate) {
        certificates.put(certificate.id, certificate);
        certificatesByShareableId.put(certificate.shareableId, certificate);
    }

    public List<Certificate> findAllCertificates() {
        return certificates.values().stream()
                .collect(Collectors.toList());
    }

    public Certificate findById(Long id) {
        return certificates.get(id);
    }

    public Certificate findByShareableId(String shareableId) {
        return certificatesByShareableId.get(shareableId);
    }

    public void deleteCertificate(Long id) {
        Certificate certificate = certificates.remove(id);
        if (certificate != null) {
            certificatesByShareableId.remove(certificate.shareableId);
        }
    }

    public void updateCertificate(Certificate certificate) {
        certificates.put(certificate.id, certificate);
        certificatesByShareableId.put(certificate.shareableId, certificate);
    }
}