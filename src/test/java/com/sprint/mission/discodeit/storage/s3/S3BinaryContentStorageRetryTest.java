package com.sprint.mission.discodeit.storage.s3;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sprint.mission.discodeit.config.RetryConfig;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentUploadException;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@SpringJUnitConfig(
    classes = {
        RetryConfig.class,
        S3BinaryContentStorage.class,
        S3BinaryContentStorageRetryTest.TestConfig.class
    }
)
@TestPropertySource(
    properties = "discodeit.storage.type=s3"
)
class S3BinaryContentStorageRetryTest {

  @Autowired
  private BinaryContentStorage storage;

  @Autowired
  private S3Client s3Client;

  @Test
  void S3_업로드_실패시_3회_재시도한다() {
    UUID id = UUID.randomUUID();
    byte[] bytes = "test".getBytes();

    when(
        s3Client.putObject(
            any(PutObjectRequest.class),
            any(RequestBody.class)
        )
    ).thenThrow(new RuntimeException("S3 장애"));

    assertThatThrownBy(
        () -> storage.put(id, bytes)
    ).isInstanceOf(BinaryContentUploadException.class);

    verify(s3Client, times(3))
        .putObject(
            any(PutObjectRequest.class),
            any(RequestBody.class)
        );
  }

  @TestConfiguration
  static class TestConfig {

    @Bean
    S3Client s3Client() {
      return mock(S3Client.class);
    }

    @Bean
    S3Presigner s3Presigner() {
      return mock(S3Presigner.class);
    }

    @Bean
    S3Properties s3Properties() {
      S3Properties properties = new S3Properties();
      properties.setBucket("test-bucket");
      return properties;
    }
  }
}