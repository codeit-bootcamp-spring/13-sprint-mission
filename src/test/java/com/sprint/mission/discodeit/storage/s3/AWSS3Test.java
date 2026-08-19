package com.sprint.mission.discodeit.storage.s3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.Properties;
import java.util.UUID;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("AWS S3 수동 테스트용")
class AWSS3Test {

    private Properties properties;
    private S3Client s3Client;
    private S3Presigner s3Presigner;

    @BeforeEach
    void setUp() throws IOException {
        properties = new Properties();

        Path envPath = Path.of(".env");
        if (Files.exists(envPath)) {
            try (InputStream inputStream = Files.newInputStream(envPath)) {
                properties.load(inputStream);
            }
        }

        Region region = Region.of(getProperty("AWS_S3_REGION", "ap-northeast-2"));

        s3Client = S3Client.builder()
                .region(region)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        s3Presigner = S3Presigner.builder()
                .region(region)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Test
    void upload() {
        String bucket = getRequiredProperty("AWS_S3_BUCKET");
        String key = "test/" + UUID.randomUUID() + ".txt";
        byte[] bytes = "hello s3".getBytes();

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("text/plain")
                .contentLength((long) bytes.length)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(bytes));

        ResponseInputStream<GetObjectResponse> response = s3Client.getObject(builder -> builder
                .bucket(bucket)
                .key(key));

        assertThat(response.response().contentLength()).isEqualTo(bytes.length);
    }

    @Test
    void download() throws IOException {
        String bucket = getRequiredProperty("AWS_S3_BUCKET");
        String key = "test/" + UUID.randomUUID() + ".txt";
        byte[] bytes = "download test".getBytes();

        s3Client.putObject(builder -> builder
                        .bucket(bucket)
                        .key(key)
                        .contentType("text/plain"),
                RequestBody.fromBytes(bytes));

        try (ResponseInputStream<GetObjectResponse> response = s3Client.getObject(builder -> builder
                .bucket(bucket)
                .key(key))) {
            assertThat(response.readAllBytes()).isEqualTo(bytes);
        }
    }

    @Test
    void generatePresignedUrl() throws IOException {
        String bucket = getRequiredProperty("AWS_S3_BUCKET");
        String key = "test/" + UUID.randomUUID() + ".txt";

        s3Client.putObject(builder -> builder
                        .bucket(bucket)
                        .key(key)
                        .contentType("text/plain"),
                RequestBody.fromString("presigned url test"));

        GetObjectPresignRequest request = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(600))
                .getObjectRequest(builder -> builder
                        .bucket(bucket)
                        .key(key))
                .build();

        URL url = s3Presigner.presignGetObject(request).url();

        assertThat(url).isNotNull();
        assertThat(url.toString()).contains(bucket);
    }

    private String getRequiredProperty(String key) {
        String value = getProperty(key, null);
        assertThat(value).as(key + " must be set").isNotBlank();
        return value;
    }

    private String getProperty(String key, String defaultValue) {
        return System.getenv().getOrDefault(key, properties.getProperty(key, defaultValue));
    }
}