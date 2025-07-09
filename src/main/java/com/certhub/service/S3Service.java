package com.certhub.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class S3Service {

    @ConfigProperty(name = "aws.s3.bucket-name", defaultValue = "certhub-certificates")
    String bucketName;

    @ConfigProperty(name = "aws.region", defaultValue = "us-east-1")
    String region;

    @ConfigProperty(name = "aws.s3.endpoint")
    Optional<String> endpoint;

    @ConfigProperty(name = "aws.access-key")
    Optional<String> accessKey;

    @ConfigProperty(name = "aws.secret-key")
    Optional<String> secretKey;

    private AmazonS3 s3Client;

    @PostConstruct
    public void init() {
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();

        if (endpoint.isPresent()) {
            // MinIO configuration
            builder.withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(endpoint.get(), region)
            );
            builder.withPathStyleAccessEnabled(true);
        } else {
            // AWS S3 configuration
            builder.withRegion(region);
        }

        if (accessKey.isPresent() && secretKey.isPresent()) {
            BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey.get(), secretKey.get());
            builder.withCredentials(new AWSStaticCredentialsProvider(credentials));
        }

        this.s3Client = builder.build();

        // Create bucket if it doesn't exist (for MinIO)
        if (endpoint.isPresent()) {
            try {
                if (!s3Client.doesBucketExistV2(bucketName)) {
                    s3Client.createBucket(bucketName);
                }
            } catch (Exception e) {
                // Ignore bucket creation errors for production
            }
        }
    }

    public String uploadFile(InputStream fileStream, String fileName, String contentType, long contentLength) {
        String key = UUID.randomUUID().toString() + "-" + fileName;
        
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentLength(contentLength);
        
        PutObjectRequest putRequest = new PutObjectRequest(bucketName, key, fileStream, metadata);
        s3Client.putObject(putRequest);
        
        return key;
    }

    public S3Object getFile(String key) {
        return s3Client.getObject(bucketName, key);
    }

    public void deleteFile(String key) {
        s3Client.deleteObject(bucketName, key);
    }

    public String generatePresignedUrl(String key, int expirationMinutes) {
        return s3Client.generatePresignedUrl(bucketName, key, 
                new java.util.Date(System.currentTimeMillis() + expirationMinutes * 60 * 1000)).toString();
    }
}