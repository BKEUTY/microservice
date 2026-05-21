package com.bkeuty.review_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
public class S3Service {

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.access-key}")
    private String accessKey;

    @Value("${aws.s3.secret-key}")
    private String secretKey;

    @Value("${aws.s3.review-images-folder:review-images}")
    private String reviewImagesFolder;

    private S3Client s3Client;

    @PostConstruct
    public void init() {
        s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
        log.info("S3Client initialized – bucket={}, region={}", bucketName, region);
    }

    public String uploadReviewImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }

        String originalFilename = image.getOriginalFilename() != null
                ? image.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_")
                : "file";

        // S3 key: review-images/{uuid}_{filename}
        String s3Key = String.format("%s/%s_%s",
                reviewImagesFolder, UUID.randomUUID(), originalFilename);

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(image.getContentType())
                    .contentLength(image.getSize())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(image.getBytes()));

            String url = buildPublicUrl(s3Key);
            log.info("Uploaded review image to S3: {}", url);
            return url;

        } catch (IOException e) {
            log.error("Failed to read bytes from uploaded review image '{}': {}", originalFilename, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to read uploaded file: " + originalFilename);
        } catch (Exception e) {
            log.error("Failed to upload file '{}' to S3 key '{}': {}", originalFilename, s3Key, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to upload review image to S3: " + originalFilename);
        }
    }

    private String buildPublicUrl(String s3Key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, s3Key);
    }
}
