package com.sprint.mission.discodeit.storage.s3;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AWSS3Test {

    private static S3Client s3Client;
    private static S3Presigner s3Presigner;

    private static String bucket;
    private static Region region;
    private static StaticCredentialsProvider credentialsProvider;

    @BeforeAll
    static void setUp() throws IOException {

        // .env 파일 읽기
        Properties properties = new Properties();

        try (FileInputStream input = new FileInputStream(".env")) {
            properties.load(input);
        }

        // .env에 저장된 AWS 설정 읽기
        String accessKey =
                properties.getProperty("AWS_S3_ACCESS_KEY");

        String secretKey =
                properties.getProperty("AWS_S3_SECRET_KEY");

        String regionName =
                properties.getProperty("AWS_S3_REGION");

        bucket =
                properties.getProperty("AWS_S3_BUCKET");

        // AWS 자격 증명 생성
        AwsBasicCredentials credentials =
                AwsBasicCredentials.create(
                        accessKey,
                        secretKey
                );

        credentialsProvider =
                StaticCredentialsProvider.create(credentials);

        region = Region.of(regionName);

        // S3 Client 생성
        s3Client = S3Client.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();

        // Presigned URL 생성용 Presigner
        s3Presigner = S3Presigner.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Test
    void upload() {

        String key = "test/upload-test.txt";
        String content = "Hello S3";

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("text/plain")
                .build();

        s3Client.putObject(
                request,
                RequestBody.fromString(content)
        );

        System.out.println("업로드 완료: " + key);
    }

    @Test
    void download() {

        String key = "test/upload-test.txt";

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        ResponseBytes<GetObjectResponse> response =
                s3Client.getObjectAsBytes(request);

        String content =
                response.asString(StandardCharsets.UTF_8);

        System.out.println("다운로드 내용: " + content);

        assertEquals("Hello S3", content);
    }

    @Test
    void presignedUrl() {

        String key = "test/upload-test.txt";

        GetObjectRequest getObjectRequest =
                GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build();

        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(10))
                        .getObjectRequest(getObjectRequest)
                        .build();

        String url = s3Presigner
                .presignGetObject(presignRequest)
                .url()
                .toString();

        System.out.println("Presigned URL: " + url);

        assertNotNull(url);
    }
}