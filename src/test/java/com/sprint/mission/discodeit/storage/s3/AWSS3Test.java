package com.sprint.mission.discodeit.storage.s3;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Slf4j
@DisplayName("AWS S3 테스트")
@Tag("aws-integration") // 일반 테스트와 분리하기 위해 태그 지정
public class AWSS3Test {

  private static S3Client s3Client;
  private static S3Presigner s3Presigner;
  private static String awsS3Bucket;

  private static final String TEST_KEY = "test/test-file.txt";

  private static Properties loadProperties() throws IOException {
    Properties properties = new Properties();
    Path envFile = Path.of(".env");

    if (!Files.exists(envFile)) {
      return properties;
    }

    try (Reader reader = Files.newBufferedReader(envFile, StandardCharsets.UTF_8)) {
      properties.load(reader);
    }
    return properties;
  }

  @BeforeAll
  static void setUp() throws IOException {
    Properties properties = loadProperties();

    String awsS3Region = properties.getProperty("AWS_S3_REGION", "ap-northeast-2");
    awsS3Bucket = properties.getProperty("AWS_S3_BUCKET");

    DefaultCredentialsProvider defaultCredentialsProvider = DefaultCredentialsProvider.create();

    s3Client = S3Client.builder()
        .region(Region.of(awsS3Region))
        .credentialsProvider(defaultCredentialsProvider)
        .build();
    s3Presigner = S3Presigner.builder()
        .region(Region.of(awsS3Region))
        .credentialsProvider(defaultCredentialsProvider)
        .build();
  }

  @AfterAll
  static void tearDown() {
    if (s3Client != null) {
      s3Client.close();
    }
    if (s3Presigner != null) {
      s3Presigner.close();
    }
  }

  @Test
  @DisplayName("S3 파일 업로드 테스트")
  void upload() throws IOException {
    Path tempFile = Files.writeString(Files.createTempFile("s3-upload", ".txt"), "test-file.txt");

    PutObjectResponse putObjectResponse = s3Client.putObject(
        PutObjectRequest.builder()
            .bucket(awsS3Bucket)
            .key(TEST_KEY)
            .contentType("text/plain")
            .build(),
        RequestBody.fromFile(tempFile));
    assertThat(putObjectResponse.eTag()).isNotNull();
    Files.deleteIfExists(tempFile);
  }

  @Test
  @DisplayName("S3 파일 다운로드 테스트")
  void download() throws IOException {
    Path tempFile = Files.createTempFile("s3-download", ".txt");

    ResponseInputStream<GetObjectResponse> s3ClientObject = s3Client.getObject(
        GetObjectRequest.builder()
            .bucket(awsS3Bucket)
            .key(TEST_KEY)
            .build()
    );
    s3ClientObject.transferTo(Files.newOutputStream(tempFile));

    String readString = Files.readString(tempFile);
    System.out.println("다운로드한 파일 내용: " + readString);

    assertThat(readString).contains("test-file.txt");
    Files.deleteIfExists(tempFile);
  }

  @Test
  @DisplayName("S3 Presigned URL 생성 테스트")
  void createPresignedUrl() throws URISyntaxException {

    URI presignedUrl = s3Presigner.presignGetObject(
        GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(10))
            .getObjectRequest(
                GetObjectRequest.builder()
                    .bucket(awsS3Bucket)
                    .key(TEST_KEY)
                    .build()
            ).build()
    ).url().toURI();

    assertThat(presignedUrl).isNotNull();
    assertThat(presignedUrl.toString()).contains(awsS3Bucket);

    log.info("생성된 Presigned URL: " + presignedUrl);
  }
}
