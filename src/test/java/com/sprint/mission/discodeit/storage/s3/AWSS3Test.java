package com.sprint.mission.discodeit.storage.s3;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class AWSS3Test {

    private final String bucketName = "discodeit-binary-content-storage-hj";

    private final S3Client s3Client = S3Client.builder()
            .region(Region.AP_NORTHEAST_2)
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();

    private final S3Presigner s3Presigner = S3Presigner.builder()
            .region(Region.AP_NORTHEAST_2)
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();

    @Test
    void upload() {
        String key = "test/test.txt";
        byte[] data = "S3 upload test".getBytes(StandardCharsets.UTF_8);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType("text/plain")
                .build();

        PutObjectResponse response = s3Client.putObject(
                request,
                RequestBody.fromBytes(data)
        );

        System.out.println("ETag = " + response.eTag());
    }

    @Test
    void download() {
        String key = "test/test.txt";

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        byte[] data = s3Client.getObjectAsBytes(request).asByteArray();

        String content = new String(data, StandardCharsets.UTF_8);

        System.out.println("다운로드 내용 = " + content);

    }

    @Test
    void generatePresignedUrl() {
        String key = "test/test.txt";

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest =
                s3Presigner.presignGetObject(presignRequest);

        System.out.println("Presigned URL = " + presignedRequest.url());
    }

}
