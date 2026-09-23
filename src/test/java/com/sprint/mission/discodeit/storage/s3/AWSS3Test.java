package com.sprint.mission.discodeit.storage.s3;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;


@Slf4j
@Disabled("실제 AWS 자격증명 필요 - 로컬 수동 실행용")
class AWSS3Test {

    private static String region;
    private static String bucket;
    private static final String TEST_KEY = "testKey";
    private static final String TEST_CONTENT = "Hello S3 from discodeit!";


    @BeforeAll
    static void setUp() throws Exception {
        Properties props = new Properties();
        try(InputStream is = new FileInputStream(".env")) {
            props.load(is);
        }
        region = props.getProperty("AWS_S3_REGION");
        bucket = props.getProperty("AWS_S3_BUCKET");
    }

    private static S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Test
    void testUpload () {
        try(S3Client s3 = s3Client()) {
            s3.putObject(
                    PutObjectRequest.builder().bucket(bucket).key(TEST_KEY).build(),
                    RequestBody.fromString(TEST_CONTENT)
            );
            log.info("업로드 완료: " + TEST_KEY);
        }
    }

    @Test
    void testDownload () {
        try(S3Client s3 = s3Client()) {
            byte[] data = s3.getObjectAsBytes(
                    GetObjectRequest.builder().bucket(bucket).key(TEST_KEY).build()
            ).asByteArray();

            String result = new String(data, StandardCharsets.UTF_8);
            log.info("다운로드 내용: " + result);
            assertEquals(TEST_CONTENT, result);
        }
    }

    @Test
    void testPresignedUrl() {
        try(S3Presigner presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build()){

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))
                    .getObjectRequest(GetObjectRequest.builder().bucket(bucket).key(TEST_KEY).build())
                    .build();

            PresignedGetObjectRequest presigned = presigner.presignGetObject(presignRequest);
            String url = presigned.url().toString();
            log.info("Presigned URL: " + url);
            assertNotNull(url);
        }
    }
}