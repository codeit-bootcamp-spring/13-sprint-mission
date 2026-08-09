package com.sprint.mission.discodeit.storage.s3;

import org.junit.jupiter.api.AfterAll;
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
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class AWSS3Test {

    private static final String TEST_KEY = "test/hello.txt";
    private static final String TEST_CONTENT = "Hello S3!";

    private static S3Client s3Client;
    private static S3Presigner s3Presigner;

    private static String bucket;

    @BeforeAll
    static void setUp() throws IOException {
        Properties properties = new Properties();

        try (InputStream inputStream = Files.newInputStream(Path.of(".env"))) {
            properties.load(inputStream);
        }

        String accessKey = properties.getProperty("AWS_S3_ACCESS_KEY");
        String secretKey = properties.getProperty("AWS_S3_SECRET_KEY");
        String region = properties.getProperty("AWS_S3_REGION");

        bucket = properties.getProperty("AWS_S3_BUCKET");

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);

        s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .build();

        s3Presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @AfterAll
    static void stop() {
        s3Client.close();
        s3Presigner.close();
    }

    @Test
    void upload() {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(TEST_KEY)
                .contentType("text/plain")
                .build();

        s3Client.putObject(
                request,
                RequestBody.fromString(TEST_CONTENT)
        );
    }

    @Test
    void download() {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(TEST_KEY)
                .build();

        ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(request);

        String content = response.asUtf8String();

        assertThat(content).isEqualTo(TEST_CONTENT);
    }

    @Test
    void generatePresignedUrl() {
        GetObjectRequest getObjectRequest =
                GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(TEST_KEY)
                        .build();

        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(10))
                        .getObjectRequest(getObjectRequest)
                        .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

        String url = presignedRequest.url().toString();

        assertThat(url).isNotBlank();

        System.out.println("Presigned URL = " + url);
    }

}
