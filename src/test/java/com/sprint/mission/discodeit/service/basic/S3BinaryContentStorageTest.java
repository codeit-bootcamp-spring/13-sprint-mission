package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.exception.storage.FileNotFoundException;
import com.sprint.mission.discodeit.service.TransactionalStorageAction;
import com.sprint.mission.discodeit.service.NotificationService;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.exception.storage.FileSaveFailedException;
import com.sprint.mission.discodeit.storage.BinaryContentUpload;
import com.sprint.mission.discodeit.storage.DownloadResult;
import com.sprint.mission.discodeit.storage.RedirectDownloadResult;
import com.sprint.mission.discodeit.storage.type.S3Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * 실제 S3 저장소 구현의 SDK 호출과 정리 작업 등록을 검증한다.
 * S3 클라이언트 등 외부 협력 객체는 Mock이며 상태 필드를 포함한 실제 DTO를 사용한다.
 */
@ExtendWith(MockitoExtension.class)
class S3BinaryContentStorageTest {
    @TempDir Path temporaryDirectory;
    @Mock UserReader userReader;
    @Mock NotificationService notificationService;

    private static final String BUCKET = "discodeit-test-bucket";
    private static final String REGION = "ap-northeast-2";
    private static final int PRESIGNED_URL_EXPIRATION = 600;

    @Mock
    S3Client s3Client;

    @Mock
    S3Presigner s3Presigner;

    @Mock
    StorageTransactionManager transactionManager;

    S3BinaryContentStorage storage;

    @BeforeEach
    void setUp() {
        S3Properties props = new S3Properties(
                REGION,
                BUCKET,
                "http://localhost:9090",
                PRESIGNED_URL_EXPIRATION
        );
        storage = new S3BinaryContentStorage(s3Client, s3Presigner, props, transactionManager, userReader, notificationService);
    }

    @Test
    @DisplayName("S3 storage upload")
    void put_uploadsObjectAndRegistersRollbackCleanup() throws Exception {
        UUID fileId = UUID.randomUUID();
        byte[] bytes = "upload-content".getBytes();
        BinaryContentUpload upload = new BinaryContentUpload(
                Files.write(temporaryDirectory.resolve("upload"), bytes),
                "text/plain",
                bytes.length
        );
        given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .willReturn(PutObjectResponse.builder().build());

        UUID result = storage.put(fileId, upload);

        assertThat(result).isEqualTo(fileId);

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));
        PutObjectRequest request = requestCaptor.getValue();
        assertThat(request.bucket()).isEqualTo(BUCKET);
        assertThat(request.key()).isEqualTo(fileId.toString());
        assertThat(request.contentType()).isEqualTo("text/plain");

        ArgumentCaptor<TransactionalStorageAction> actionCaptor = ArgumentCaptor.forClass(TransactionalStorageAction.class);
        verify(transactionManager).afterRollback(contains("key=" + fileId), actionCaptor.capture());

        actionCaptor.getValue().execute();
        verify(s3Client).deleteObject(anyDeleteObjectRequestConsumer());
    }

    @Test
    void recoveryPreservesOriginalFailureWhenNotificationFails() {
        UUID id = UUID.randomUUID();
        var upload = new BinaryContentUpload(temporaryDirectory.resolve("upload"), "text/plain", 1);
        var failure = new FileSaveFailedException(id, "업로드 실패", new IOException("원본 오류"));
        var notificationFailure = new IllegalStateException("알림 저장 실패");
        given(userReader.findUserIdsByRole(Role.ADMIN)).willReturn(List.of(UUID.randomUUID()));
        doThrow(notificationFailure).when(notificationService).createAll(any());

        assertThatThrownBy(() -> storage.recover(failure, id, upload)).isSameAs(failure);
        assertThat(failure.getSuppressed()).containsExactly(notificationFailure);
    }

    @Test
    void recoveryWithoutAdministratorsStillPropagatesUploadFailure() {
        UUID id = UUID.randomUUID();
        var upload = new BinaryContentUpload(temporaryDirectory.resolve("upload"), "text/plain", 1);
        var failure = new FileSaveFailedException(id, "업로드 실패", new IOException("원본 오류"));
        given(userReader.findUserIdsByRole(Role.ADMIN)).willReturn(List.of());

        assertThatThrownBy(() -> storage.recover(failure, id, upload)).isSameAs(failure);
        verifyNoInteractions(notificationService);
    }

    @Test
    @DisplayName("S3 storage download stream")
    void get_returnsObjectInputStream() throws Exception {
        UUID fileId = UUID.randomUUID();
        byte[] bytes = "download-content".getBytes();
        ResponseInputStream<GetObjectResponse> objectStream = new ResponseInputStream<>(
                GetObjectResponse.builder().build(),
                new ByteArrayInputStream(bytes)
        );
        given(s3Client.getObject(any(GetObjectRequest.class))).willReturn(objectStream);

        byte[] result;
        try (var inputStream = storage.get(fileId)) {
            result = inputStream.readAllBytes();
        }

        assertThat(result).isEqualTo(bytes);

        ArgumentCaptor<GetObjectRequest> requestCaptor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(s3Client).getObject(requestCaptor.capture());
        GetObjectRequest request = requestCaptor.getValue();
        assertThat(request.bucket()).isEqualTo(BUCKET);
        assertThat(request.key()).isEqualTo(fileId.toString());
    }

    @Test
    @DisplayName("S3 storage missing object")
    void get_throwsFileNotFoundException_whenObjectDoesNotExist() {
        UUID fileId = UUID.randomUUID();
        given(s3Client.getObject(any(GetObjectRequest.class)))
                .willThrow(NoSuchKeyException.builder().message("not found").build());

        assertThatThrownBy(() -> storage.get(fileId))
                .isInstanceOf(FileNotFoundException.class);
    }

    @Test
    @DisplayName("S3 storage delete")
    void delete_registersAfterCommitCleanup() throws Exception {
        UUID fileId = UUID.randomUUID();

        storage.delete(fileId);

        ArgumentCaptor<TransactionalStorageAction> actionCaptor = ArgumentCaptor.forClass(TransactionalStorageAction.class);
        verify(transactionManager).afterCommit(contains("key=" + fileId), actionCaptor.capture());

        actionCaptor.getValue().execute();
        verify(s3Client).deleteObject(anyDeleteObjectRequestConsumer());
    }

    @Test
    @DisplayName("S3 storage presigned redirect download")
    void download_returnsRedirectDownloadResult() throws Exception {
        UUID fileId = UUID.randomUUID();
        URI location = URI.create("http://localhost:9090/" + BUCKET + "/" + fileId + "?X-Amz-Signature=test");
        PresignedGetObjectRequest presignedRequest = PresignedGetObjectRequest.builder()
                .expiration(Instant.now().plusSeconds(PRESIGNED_URL_EXPIRATION))
                .isBrowserExecutable(true)
                .signedHeaders(Map.of("host", List.of("localhost:9090")))
                .httpRequest(SdkHttpFullRequest.builder()
                        .method(SdkHttpMethod.GET)
                        .uri(location)
                        .build())
                .build();
        given(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).willReturn(presignedRequest);

        DownloadResult result = storage.download(new BinaryContentDto(
                fileId,
                "download.txt",
                100L,
                "text/plain",
                BinaryContentStatus.SUCCESS
        ));

        assertThat(result).isInstanceOf(RedirectDownloadResult.class);
        assertThat(((RedirectDownloadResult) result).location()).isEqualTo(location);

        ArgumentCaptor<GetObjectPresignRequest> requestCaptor = ArgumentCaptor.forClass(GetObjectPresignRequest.class);
        verify(s3Presigner).presignGetObject(requestCaptor.capture());
        GetObjectPresignRequest request = requestCaptor.getValue();
        assertThat(request.signatureDuration()).isEqualTo(Duration.ofSeconds(PRESIGNED_URL_EXPIRATION));
        assertThat(request.getObjectRequest().bucket()).isEqualTo(BUCKET);
        assertThat(request.getObjectRequest().key()).isEqualTo(fileId.toString());
        assertThat(request.getObjectRequest().responseContentDisposition()).contains("download.txt");
    }

    @SuppressWarnings("unchecked")
    private Consumer<DeleteObjectRequest.Builder> anyDeleteObjectRequestConsumer() {
        return any(Consumer.class);
    }
}
