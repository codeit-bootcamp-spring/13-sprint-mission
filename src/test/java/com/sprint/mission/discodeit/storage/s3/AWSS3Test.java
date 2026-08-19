package com.sprint.mission.discodeit.storage.s3;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnabledIfEnvironmentVariable(
        named = "RUN_AWS_S3_TESTS",
        matches = "true"
)
class AWSS3Test {

    private static final byte[] TEST_BYTES =
            "discodeit-s3-test".getBytes(StandardCharsets.UTF_8);

    private static S3Client s3Client;
    private static S3Presigner s3Presigner;
    private static String bucket;
    private static String objectKey;
    private static long expirationSeconds;
    private static boolean uploaded;

    @BeforeAll
    static void setUp() throws IOException {
        Properties properties = loadProperties();

        String profile = properties.getProperty("AWS_PROFILE", "default");
        Region region = Region.of(
                requiredProperty(properties, "AWS_S3_REGION")
        );

        bucket = requiredProperty(properties, "AWS_S3_BUCKET");
        expirationSeconds = Long.parseLong(
                properties.getProperty(
                        "AWS_S3_PRESIGNED_URL_EXPIRATION",
                        "600"
                )
        );
        objectKey = "test/" + UUID.randomUUID();

        ProfileCredentialsProvider credentialsProvider =
                ProfileCredentialsProvider.builder()
                        .profileName(profile)
                        .build();

        s3Client = S3Client.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();

        s3Presigner = S3Presigner.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Test
    @Order(1)
    void upload() {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType("text/plain")
                .build();

        PutObjectResponse response = s3Client.putObject(
                request,
                RequestBody.fromBytes(TEST_BYTES)
        );

        uploaded = true;

        assertThat(response.eTag()).isNotBlank();
    }

    @Test
    @Order(2)
    void download() {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();

        ResponseBytes<GetObjectResponse> response =
                s3Client.getObjectAsBytes(request);

        assertThat(response.asByteArray()).isEqualTo(TEST_BYTES);
    }

    @Test
    @Order(3)
    void createPresignedUrl() {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();

        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(
                                Duration.ofSeconds(expirationSeconds)
                        )
                        .getObjectRequest(getObjectRequest)
                        .build();

        PresignedGetObjectRequest presignedRequest =
                s3Presigner.presignGetObject(presignRequest);

        assertThat(presignedRequest.url()).isNotNull();
        assertThat(presignedRequest.url().toString())
                .startsWith("https://");
    }

    @AfterAll
    static void tearDown() {
        if (s3Client != null && uploaded) {
            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(bucket)
                            .key(objectKey)
                            .build()
            );
        }

        if (s3Presigner != null) {
            s3Presigner.close();
        }
        if (s3Client != null) {
            s3Client.close();
        }
    }

    private static Properties loadProperties() throws IOException {
        Properties properties = new Properties();

        try (InputStream inputStream =
                     Files.newInputStream(Path.of(".env"))) {
            properties.load(inputStream);
        }

        return properties;
    }

    private static String requiredProperty(
            Properties properties,
            String key
    ) {
        String value = properties.getProperty(key);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    key + " 설정이 필요합니다."
            );
        }

        return value;
    }
}