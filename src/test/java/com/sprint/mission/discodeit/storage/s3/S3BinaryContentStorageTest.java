package com.sprint.mission.discodeit.storage.s3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sprint.mission.discodeit.config.S3Properties;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@ExtendWith(MockitoExtension.class)
class S3BinaryContentStorageTest {

  @Mock
  private S3Client s3Client;
  @Mock
  private S3Presigner s3Presigner;
  @Mock
  private PresignedGetObjectRequest presignedRequest;

  private S3BinaryContentStorage storage;

  @BeforeEach
  void setUp() {
    S3Properties properties = new S3Properties();
    properties.setBucket("test-bucket");
    properties.setPresignedUrlExpiration(Duration.ofMinutes(10));
    storage = new S3BinaryContentStorage(s3Client, s3Presigner, properties);
  }

  @Test
  void put_uploadsWithUuidAsObjectKey() {
    UUID id = UUID.randomUUID();
    byte[] bytes = "file content".getBytes();

    UUID result = storage.put(id, bytes);

    ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(
        PutObjectRequest.class);
    verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));
    assertThat(result).isEqualTo(id);
    assertThat(requestCaptor.getValue().bucket()).isEqualTo("test-bucket");
    assertThat(requestCaptor.getValue().key()).isEqualTo(id.toString());
  }

  @Test
  void download_redirectsToPresignedUrl() throws Exception {
    UUID id = UUID.randomUUID();
    when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
        .thenReturn(presignedRequest);
    when(presignedRequest.url()).thenReturn(new URL("https://test-bucket.s3.amazonaws.com/" + id));

    BinaryContent content = org.mockito.Mockito.spy(
        new BinaryContent("report.pdf", 10L, "application/pdf", null, null));
    when(content.getId()).thenReturn(id);
    content.updateStatus(BinaryContentStatus.SUCCESS);

    var response = storage.download(new BinaryContentMapper().toDto(content));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
    assertThat(response.getHeaders().getLocation()).hasToString(
        "https://test-bucket.s3.amazonaws.com/" + id);
  }
}
