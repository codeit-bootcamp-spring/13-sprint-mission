package com.sprint.mission.discodeit.storage.s3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentUploadException;
import java.io.InputStream;
import java.net.URI;
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
import org.springframework.http.ResponseEntity;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@ExtendWith(MockitoExtension.class)
class S3BinaryContentStorageTest {

  @Mock
  private S3Client s3Client;
  @Mock
  private S3Presigner s3Presigner;

  private S3Properties properties;
  private S3BinaryContentStorage storage;

  @BeforeEach
  void setUp() {
    properties = new S3Properties();
    properties.setRegion("ap-northeast-2");
    properties.setBucket("test-bucket");
    properties.setPresignedUrlExpiration(600);

    storage = new S3BinaryContentStorage(s3Client, s3Presigner, properties);
  }

  @Test
  void 파일을_S3에_업로드한다() {
    UUID id = UUID.randomUUID();
    byte[] bytes = "test-content".getBytes();

    when(s3Client.putObject(
        any(PutObjectRequest.class),
        any(RequestBody.class)
    )).thenReturn(PutObjectResponse.builder().build());

    UUID result = storage.put(id, bytes);

    assertThat(result).isEqualTo(id);

    ArgumentCaptor<PutObjectRequest> requestCaptor =
        ArgumentCaptor.forClass(PutObjectRequest.class);

    verify(s3Client).putObject(
        requestCaptor.capture(),
        any(RequestBody.class)
    );

    PutObjectRequest request = requestCaptor.getValue();

    assertThat(request.bucket()).isEqualTo("test-bucket");
    assertThat(request.key()).isEqualTo(id.toString());
  }

  @Test
  void S3에서_파일을_조회한다() {
    UUID id = UUID.randomUUID();

    @SuppressWarnings("unchecked")
    ResponseInputStream<GetObjectResponse> response =
        mock(ResponseInputStream.class);

    when(s3Client.getObject(any(GetObjectRequest.class)))
        .thenReturn(response);

    InputStream result = storage.get(id);

    assertThat(result).isSameAs(response);

    ArgumentCaptor<GetObjectRequest> requestCaptor =
        ArgumentCaptor.forClass(GetObjectRequest.class);

    verify(s3Client).getObject(requestCaptor.capture());

    GetObjectRequest request = requestCaptor.getValue();

    assertThat(request.bucket()).isEqualTo("test-bucket");
    assertThat(request.key()).isEqualTo(id.toString());
  }

  @Test
  void S3에서_파일을_삭제한다() {
    UUID id = UUID.randomUUID();

    storage.delete(id);

    ArgumentCaptor<DeleteObjectRequest> requestCaptor =
        ArgumentCaptor.forClass(DeleteObjectRequest.class);

    verify(s3Client).deleteObject(requestCaptor.capture());

    DeleteObjectRequest request = requestCaptor.getValue();

    assertThat(request.bucket()).isEqualTo("test-bucket");
    assertThat(request.key()).isEqualTo(id.toString());
  }

  @Test
  void 다운로드_요청을_PresignedUrl로_리다이렉트한다() throws Exception {
    UUID id = UUID.randomUUID();
    BinaryContentDto dto = new BinaryContentDto(
        id,
        "test.png",
        100L,
        "image/png"
    );

    PresignedGetObjectRequest presignedRequest =
        mock(PresignedGetObjectRequest.class);

    URL presignedUrl =
        URI.create("https://example.com/presigned-url").toURL();

    when(s3Presigner.presignGetObject(
        any(GetObjectPresignRequest.class)
    )).thenReturn(presignedRequest);

    when(presignedRequest.url()).thenReturn(presignedUrl);

    ResponseEntity<?> response = storage.download(dto);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
    assertThat(response.getHeaders().getLocation())
        .isEqualTo(presignedUrl.toURI());

    ArgumentCaptor<GetObjectPresignRequest> requestCaptor =
        ArgumentCaptor.forClass(GetObjectPresignRequest.class);

    verify(s3Presigner).presignGetObject(requestCaptor.capture());

    GetObjectPresignRequest request = requestCaptor.getValue();

    assertThat(request.signatureDuration())
        .isEqualTo(Duration.ofSeconds(600));
    assertThat(request.getObjectRequest().bucket())
        .isEqualTo("test-bucket");
    assertThat(request.getObjectRequest().key())
        .isEqualTo(id.toString());
    assertThat(request.getObjectRequest().responseContentType())
        .isEqualTo("image/png");
  }

  @Test
  void 업로드에_실패하면_도메인_예외를_발생시킨다() {
    UUID id = UUID.randomUUID();

    when(s3Client.putObject(
        any(PutObjectRequest.class),
        any(RequestBody.class)
    )).thenThrow(
        S3Exception.builder()
            .message("S3 upload failed")
            .build()
    );

    assertThatThrownBy(() -> storage.put(id, new byte[]{1, 2, 3}))
        .isInstanceOf(BinaryContentUploadException.class);
  }

}