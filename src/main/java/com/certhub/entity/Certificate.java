package com.certhub.entity;

import java.time.LocalDateTime;
import java.util.UUID;

public class Certificate {

    public Long id;
    public String title;
    public String credentialLink;
    public String fileName;
    public String fileType;
    public String s3Key;
    public String s3Bucket;
    public Long fileSize;
    public String shareableId;
    public LocalDateTime uploadedAt;

    public Certificate() {}

    public Certificate(String title, String credentialLink, String fileName, String fileType, 
                      String s3Key, String s3Bucket, Long fileSize) {
        this.id = System.currentTimeMillis(); // Simple ID generation
        this.title = title;
        this.credentialLink = credentialLink;
        this.fileName = fileName;
        this.fileType = fileType;
        this.s3Key = s3Key;
        this.s3Bucket = s3Bucket;
        this.fileSize = fileSize;
        this.uploadedAt = LocalDateTime.now();
        this.shareableId = UUID.randomUUID().toString();
    }
}