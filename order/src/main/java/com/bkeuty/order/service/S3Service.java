package com.bkeuty.order.service;

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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Handles uploading files to AWS S3.
 *
 * <p>Refund evidence images are stored under:
 * <pre>
 *   {refundEvidenceFolder}/{refundOrderId}/{uuid}_{originalFilename}
 * </pre>
 * e.g. {@code refund-evident/42/8f3a-..._photo.jpg}
 */
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

    @Value("${aws.s3.refund-evidence-folder:refund-evident}")
    private String refundEvidenceFolder;

    private S3Client s3Client;

    @PostConstruct
    public void init() {
        s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
        log.info("S3Client initialised – bucket={}, region={}", bucketName, region);
    }

    /**
     * Uploads a list of evidence images for a given refund order.
     *
     * <p>S3 key pattern: {@code refund-evident/{refundOrderId}/{uuid}_{filename}}
     *
     * @param refundOrderId the ID of the refund order (used as the sub-folder name)
     * @param images        list of multipart files to upload
     * @return list of publicly accessible S3 URLs for the uploaded files
     */
    public List<String> uploadRefundEvidenceImages(Integer refundOrderId, List<MultipartFile> images) {
        List<String> urls = new ArrayList<>();

        if (images == null || images.isEmpty()) {
            return urls;
        }

        for (MultipartFile image : images) {
            if (image == null || image.isEmpty()) {
                continue;
            }

            String originalFilename = image.getOriginalFilename() != null
                    ? image.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_")
                    : "file";

            // S3 key: refund-evident/{refundOrderId}/{uuid}_{filename}
            String s3Key = String.format("%s/%d/%s_%s",
                    refundEvidenceFolder, refundOrderId, UUID.randomUUID(), originalFilename);

            try {
                PutObjectRequest putRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3Key)
                        .contentType(image.getContentType())
                        .contentLength(image.getSize())
                        .build();

                s3Client.putObject(putRequest, RequestBody.fromBytes(image.getBytes()));

                String url = buildPublicUrl(s3Key);
                urls.add(url);
                log.info("Uploaded refund evidence image to S3: {}", url);

            } catch (IOException e) {
                log.error("Failed to read bytes from uploaded file '{}': {}", originalFilename, e.getMessage());
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Failed to read uploaded file: " + originalFilename);
            } catch (Exception e) {
                log.error("Failed to upload file '{}' to S3 key '{}': {}", originalFilename, s3Key, e.getMessage());
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Failed to upload evidence image to S3: " + originalFilename);
            }
        }

        return urls;
    }

    /**
     * Builds the public HTTPS URL for an S3 object.
     * Uses the path-style URL format compatible with all AWS regions.
     */
    private String buildPublicUrl(String s3Key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, s3Key);
    }
}
