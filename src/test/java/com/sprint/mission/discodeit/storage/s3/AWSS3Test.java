package com.sprint.mission.discodeit.storage.s3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import java.io.InputStream;
import java.net.URI;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@EnabledIfEnvironmentVariable(named = "RUN_AWS_S3_TESTS", matches = "true")
public class AWSS3Test {

  private S3Client s3Client;
  private S3Presigner s3Presigner;
  private S3BinaryContentStorage storage;

  private String bucket;
  private UUID binaryContentId;

  @BeforeEach
  void setUp() {
    bucket = System.getenv("AWS_S3_BUCKET");

    assertThat(bucket)
        .as("AWS_S3_BUCKET 환경 변수가 필요합니다.")
        .isNotBlank();

    String regionName = System.getenv()
        .getOrDefault("AWS_S3_REGION", "ap-northeast-2");

    Region region = Region.of(regionName);

    s3Client = S3Client.builder()
        .region(region)
        .credentialsProvider(DefaultCredentialsProvider.builder().build())
        .build();

    s3Presigner = S3Presigner.builder()
        .region(region)
        .credentialsProvider(DefaultCredentialsProvider.builder().build())
        .build();

    S3Properties properties = new S3Properties();
    properties.setRegion(regionName);
    properties.setBucket(bucket);
    properties.setPresignedUrlExpiration(600);

    storage = new S3BinaryContentStorage(
        s3Client,
        s3Presigner,
        properties
    );
  }

  @AfterEach
  void tearDown() {
    if (binaryContentId != null) {
      try {
        storage.delete(binaryContentId);
      } catch (RuntimeException ignored) {
      }
    }

    s3Presigner.close();
    s3Client.close();
  }

  @Test
  void 실제_S3에_파일을_업로드하고_다운로드한다() throws Exception {
    binaryContentId = UUID.randomUUID();
    byte[] originalBytes = "actual aws s3 test".getBytes();

    UUID uploadedId = storage.put(binaryContentId, originalBytes);

    byte[] downloadedBytes;

    try (InputStream inputStream = storage.get(binaryContentId)) {
      downloadedBytes = inputStream.readAllBytes();
    }

    assertThat(uploadedId).isEqualTo(binaryContentId);
    assertThat(downloadedBytes).isEqualTo(originalBytes);
  }

  @Test
  void PresignedUrl로_파일을_다운로드한다() throws Exception {
    binaryContentId = UUID.randomUUID();
    byte[] originalBytes = "presigned url test".getBytes();

    storage.put(binaryContentId, originalBytes);

    BinaryContentDto dto = new BinaryContentDto(
        binaryContentId,
        "aws-test.txt",
        (long) originalBytes.length,
        "text/plain"
    );

    ResponseEntity<Void> response = storage.download(dto);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
    assertThat(response.getHeaders().getLocation()).isNotNull();

    URI presignedUri = response.getHeaders().getLocation();

    byte[] downloadedBytes;

    try (InputStream inputStream = presignedUri.toURL().openStream()) {
      downloadedBytes = inputStream.readAllBytes();
    }

    assertThat(downloadedBytes).isEqualTo(originalBytes);
  }

  @Test
  void 실제_S3의_파일을_삭제한다() {
    binaryContentId = UUID.randomUUID();

    storage.put(binaryContentId, "delete test".getBytes());
    storage.delete(binaryContentId);

    HeadObjectRequest request = HeadObjectRequest.builder()
        .bucket(bucket)
        .key(binaryContentId.toString())
        .build();

    assertThatThrownBy(() -> s3Client.headObject(request))
        .isInstanceOf(S3Exception.class)
        .satisfies(exception -> {
          S3Exception s3Exception = (S3Exception) exception;
          assertThat(s3Exception.statusCode()).isEqualTo(404);
        });

    binaryContentId = null;
  }
}
