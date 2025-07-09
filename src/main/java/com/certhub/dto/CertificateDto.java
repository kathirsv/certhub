package com.certhub.dto;

import com.certhub.entity.Certificate;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class CertificateDto {
    public Long id;
    public String title;
    public String credentialLink;
    public String fileName;
    public String fileType;
    public Long fileSize;
    public String shareableId;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime uploadedAt;
    
    public String shareableUrl;

    public CertificateDto() {}

    public CertificateDto(Certificate certificate) {
        this.id = certificate.id;
        this.title = certificate.title;
        this.credentialLink = certificate.credentialLink;
        this.fileName = certificate.fileName;
        this.fileType = certificate.fileType;
        this.fileSize = certificate.fileSize;
        this.shareableId = certificate.shareableId;
        this.uploadedAt = certificate.uploadedAt;
        this.shareableUrl = "/view/" + certificate.shareableId;
    }
}