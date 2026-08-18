package com.sprint.mission.discodeit.storage.s3;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = AWSS3Test.S3TestConfiguration.class,
        properties = {
                "spring.sql.init.mode=never",
                "spring.jpa.hibernate.ddl-auto=none"
        }
)
@ActiveProfiles("s3-test")
@EnableConfigurationProperties(AWSS3Properties.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("AWS S3 API 테스트")
@EnabledIfEnvironmentVariable(
        named = "RUN_AWS_S3_TESTS",
        matches = "true"
)
class AWSS3Test {

    private static final String TEST_CONTENT =
            "Discodeit AWS S3 upload and download test";

    @Autowired
    private AWSS3Properties properties;

    private S3Client s3Client;
    private S3Presigner s3Presigner;
    private String objectKey;

    @BeforeAll
    void setUp() {
        Region region = Region.of(properties.region());

        ProfileCredentialsProvider credentialsProvider =
                ProfileCredentialsProvider.create(properties.profile());

        s3Client = S3Client.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();

        s3Presigner = S3Presigner.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();

        objectKey = "test/" + UUID.randomUUID() + ".txt";
    }

    @AfterAll
    void tearDown() {
        if (s3Client != null && objectKey != null) {
            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(properties.bucket())
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

    @Test
    @Order(1)
    @DisplayName("파일을 S3에 업로드한다")
    void upload() {
        byte[] content =
                TEST_CONTENT.getBytes(StandardCharsets.UTF_8);

        PutObjectRequest request =
                PutObjectRequest.builder()
                        .bucket(properties.bucket())
                        .key(objectKey)
                        .contentType("text/plain")
                        .build();

        s3Client.putObject(
                request,
                RequestBody.fromBytes(content)
        );

        var response = s3Client.headObject(
                HeadObjectRequest.builder()
                        .bucket(properties.bucket())
                        .key(objectKey)
                        .build()
        );

        assertThat(response.contentLength())
                .isEqualTo(content.length);

        assertThat(response.contentType())
                .isEqualTo("text/plain");
    }

    @Test
    @Order(2)
    @DisplayName("S3에 업로드한 파일을 다운로드한다")
    void download() {
        GetObjectRequest request =
                GetObjectRequest.builder()
                        .bucket(properties.bucket())
                        .key(objectKey)
                        .build();

        ResponseBytes<GetObjectResponse> response =
                s3Client.getObjectAsBytes(request);

        String downloadedContent =
                response.asString(StandardCharsets.UTF_8);

        assertThat(downloadedContent)
                .isEqualTo(TEST_CONTENT);
    }

    @Test
    @Order(3)
    @DisplayName("S3 객체 다운로드용 Presigned URL을 생성한다")
    void createPresignedUrl() {
        GetObjectRequest getObjectRequest =
                GetObjectRequest.builder()
                        .bucket(properties.bucket())
                        .key(objectKey)
                        .build();

        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(5))
                        .getObjectRequest(getObjectRequest)
                        .build();

        PresignedGetObjectRequest presignedRequest =
                s3Presigner.presignGetObject(presignRequest);

        String presignedUrl =
                presignedRequest.url().toString();

        assertThat(presignedUrl)
                .isNotBlank();

        assertThat(presignedUrl)
                .contains(properties.bucket());

        assertThat(presignedRequest.expiration())
                .isNotNull();

        System.out.println("Presigned URL = " + presignedUrl);
    }

    @TestConfiguration
    static class S3TestConfiguration {
    }
}