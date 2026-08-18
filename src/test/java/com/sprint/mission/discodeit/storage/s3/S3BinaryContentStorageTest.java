package com.sprint.mission.discodeit.storage.s3;

import com.sprint.mission.discodeit.dto.BinaryContentDownloadResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("S3BinaryContentStorage 단위 테스트")
class S3BinaryContentStorageTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    private S3Properties properties;
    private S3BinaryContentStorage storage;

    @BeforeEach
    void setUp() {
        properties = new S3Properties(
                "ap-northeast-2",
                "discodeit-binary-content-storage-jhs",
                600
        );

        storage = new S3BinaryContentStorage(
                s3Client,
                s3Presigner,
                properties
        );
    }

    @Nested
    @DisplayName("바이너리 콘텐츠 업로드")
    class Put {

        @Test
        @DisplayName("UUID를 객체 키로 사용하여 S3에 데이터를 업로드한다")
        void uploadsBinaryContent() {
            UUID id = UUID.randomUUID();

            byte[] bytes = "s3 upload test"
                    .getBytes(StandardCharsets.UTF_8);

            storage.put(id, bytes);

            ArgumentCaptor<PutObjectRequest> requestCaptor =
                    ArgumentCaptor.forClass(PutObjectRequest.class);

            verify(s3Client).putObject(
                    requestCaptor.capture(),
                    any(RequestBody.class)
            );

            PutObjectRequest request = requestCaptor.getValue();

            assertThat(request.bucket())
                    .isEqualTo(properties.bucket());

            assertThat(request.key())
                    .isEqualTo(id.toString());
        }

        @Test
        @DisplayName("ID가 null이면 예외가 발생한다")
        void throwsExceptionWhenIdIsNull() {
            byte[] bytes =
                    "test".getBytes(StandardCharsets.UTF_8);

            assertThatThrownBy(
                    () -> storage.put(null, bytes)
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(
                            "저장할 바이너리 콘텐츠 id는 필수입니다."
                    );
        }

        @Test
        @DisplayName("바이너리 데이터가 null이면 예외가 발생한다")
        void throwsExceptionWhenBytesAreNull() {
            UUID id = UUID.randomUUID();

            assertThatThrownBy(
                    () -> storage.put(id, null)
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(
                            "저장할 바이너리 데이터는 비어 있을 수 없습니다."
                    );
        }

        @Test
        @DisplayName("바이너리 데이터가 비어 있으면 예외가 발생한다")
        void throwsExceptionWhenBytesAreEmpty() {
            UUID id = UUID.randomUUID();

            assertThatThrownBy(
                    () -> storage.put(id, new byte[0])
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(
                            "저장할 바이너리 데이터는 비어 있을 수 없습니다."
                    );
        }
    }

    @Nested
    @DisplayName("바이너리 콘텐츠 조회")
    class Get {

        @Test
        @DisplayName("UUID에 해당하는 S3 객체 데이터를 반환한다")
        void downloadsBinaryContent() {
            UUID id = UUID.randomUUID();

            byte[] expectedBytes = "s3 download test"
                    .getBytes(StandardCharsets.UTF_8);

            ResponseBytes<GetObjectResponse> responseBytes =
                    ResponseBytes.fromByteArray(
                            GetObjectResponse.builder().build(),
                            expectedBytes
                    );

            when(
                    s3Client.getObjectAsBytes(
                            any(GetObjectRequest.class)
                    )
            ).thenReturn(responseBytes);

            byte[] result = storage.get(id);

            assertThat(result)
                    .isEqualTo(expectedBytes);

            ArgumentCaptor<GetObjectRequest> requestCaptor =
                    ArgumentCaptor.forClass(GetObjectRequest.class);

            verify(s3Client)
                    .getObjectAsBytes(requestCaptor.capture());

            GetObjectRequest request =
                    requestCaptor.getValue();

            assertThat(request.bucket())
                    .isEqualTo(properties.bucket());

            assertThat(request.key())
                    .isEqualTo(id.toString());
        }

        @Test
        @DisplayName("ID가 null이면 예외가 발생한다")
        void throwsExceptionWhenIdIsNull() {
            assertThatThrownBy(
                    () -> storage.get(null)
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(
                            "조회할 바이너리 콘텐츠 id는 필수입니다."
                    );
        }
    }

    @Nested
    @DisplayName("바이너리 콘텐츠 삭제")
    class Delete {

        @Test
        @DisplayName("UUID를 객체 키로 사용하여 S3 객체를 삭제한다")
        void deletesBinaryContent() {
            UUID id = UUID.randomUUID();

            storage.delete(id);

            ArgumentCaptor<DeleteObjectRequest> requestCaptor =
                    ArgumentCaptor.forClass(DeleteObjectRequest.class);

            verify(s3Client)
                    .deleteObject(requestCaptor.capture());

            DeleteObjectRequest request =
                    requestCaptor.getValue();

            assertThat(request.bucket())
                    .isEqualTo(properties.bucket());

            assertThat(request.key())
                    .isEqualTo(id.toString());
        }

        @Test
        @DisplayName("ID가 null이면 예외가 발생한다")
        void throwsExceptionWhenIdIsNull() {
            assertThatThrownBy(
                    () -> storage.delete(null)
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(
                            "삭제할 바이너리 콘텐츠 id는 필수입니다."
                    );
        }
    }

    @Nested
    @DisplayName("바이너리 콘텐츠 다운로드")
    class Download {

        @Test
        @DisplayName("Presigned URL을 생성하고 해당 URL로 리다이렉트한다")
        void redirectsToPresignedUrl() throws Exception {
            UUID id = UUID.randomUUID();

            BinaryContentDownloadResponse downloadResponse =
                    mock(BinaryContentDownloadResponse.class);

            when(downloadResponse.getContentType())
                    .thenReturn("image/png");

            URL presignedUrl = URI.create(
                    "https://discodeit-binary-content-storage-jhs"
                            + ".s3.ap-northeast-2.amazonaws.com/"
                            + id
                            + "?X-Amz-Signature=test"
            ).toURL();

            PresignedGetObjectRequest presignedRequest =
                    mock(PresignedGetObjectRequest.class);

            when(presignedRequest.url())
                    .thenReturn(presignedUrl);

            when(
                    s3Presigner.presignGetObject(
                            any(GetObjectPresignRequest.class)
                    )
            ).thenReturn(presignedRequest);

            ResponseEntity<?> result =
                    storage.download(id, downloadResponse);

            assertThat(result.getStatusCode())
                    .isEqualTo(HttpStatus.FOUND);

            assertThat(result.getHeaders().getLocation())
                    .isEqualTo(presignedUrl.toURI());

            assertThat(result.getBody())
                    .isNull();

            ArgumentCaptor<GetObjectPresignRequest> requestCaptor =
                    ArgumentCaptor.forClass(
                            GetObjectPresignRequest.class
                    );

            verify(s3Presigner)
                    .presignGetObject(requestCaptor.capture());

            GetObjectPresignRequest capturedRequest =
                    requestCaptor.getValue();

            assertThat(capturedRequest.signatureDuration())
                    .isEqualTo(
                            Duration.ofSeconds(
                                    properties.presignedUrlExpiration()
                            )
                    );

            assertThat(
                    capturedRequest
                            .getObjectRequest()
                            .bucket()
            ).isEqualTo(properties.bucket());

            assertThat(
                    capturedRequest
                            .getObjectRequest()
                            .key()
            ).isEqualTo(id.toString());

            assertThat(
                    capturedRequest
                            .getObjectRequest()
                            .responseContentType()
            ).isEqualTo("image/png");
        }

        @Test
        @DisplayName("Content-Type이 없어도 Presigned URL을 생성한다")
        void createsPresignedUrlWithoutContentType()
                throws Exception {
            UUID id = UUID.randomUUID();

            BinaryContentDownloadResponse downloadResponse =
                    mock(BinaryContentDownloadResponse.class);

            when(downloadResponse.getContentType())
                    .thenReturn(null);

            URL presignedUrl = URI.create(
                    "https://discodeit-binary-content-storage-jhs"
                            + ".s3.ap-northeast-2.amazonaws.com/"
                            + id
                            + "?X-Amz-Signature=test"
            ).toURL();

            PresignedGetObjectRequest presignedRequest =
                    mock(PresignedGetObjectRequest.class);

            when(presignedRequest.url())
                    .thenReturn(presignedUrl);

            when(
                    s3Presigner.presignGetObject(
                            any(GetObjectPresignRequest.class)
                    )
            ).thenReturn(presignedRequest);

            ResponseEntity<?> result =
                    storage.download(id, downloadResponse);

            assertThat(result.getStatusCode())
                    .isEqualTo(HttpStatus.FOUND);

            assertThat(result.getHeaders().getLocation())
                    .isEqualTo(presignedUrl.toURI());

            ArgumentCaptor<GetObjectPresignRequest> requestCaptor =
                    ArgumentCaptor.forClass(
                            GetObjectPresignRequest.class
                    );

            verify(s3Presigner)
                    .presignGetObject(requestCaptor.capture());

            GetObjectRequest getObjectRequest =
                    requestCaptor
                            .getValue()
                            .getObjectRequest();

            assertThat(getObjectRequest.bucket())
                    .isEqualTo(properties.bucket());

            assertThat(getObjectRequest.key())
                    .isEqualTo(id.toString());

            assertThat(getObjectRequest.responseContentType())
                    .isNull();
        }

        @Test
        @DisplayName("ID가 null이면 예외가 발생한다")
        void throwsExceptionWhenIdIsNull() {
            BinaryContentDownloadResponse downloadResponse =
                    mock(BinaryContentDownloadResponse.class);

            assertThatThrownBy(
                    () -> storage.download(
                            null,
                            downloadResponse
                    )
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(
                            "다운로드할 바이너리 콘텐츠 id는 필수입니다."
                    );
        }

        @Test
        @DisplayName("다운로드 정보가 null이면 예외가 발생한다")
        void throwsExceptionWhenResponseIsNull() {
            UUID id = UUID.randomUUID();

            assertThatThrownBy(
                    () -> storage.download(id, null)
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(
                            "다운로드할 바이너리 콘텐츠 정보는 필수입니다."
                    );
        }
    }
}