package com.sprint.mission.discodeit.storage.s3;

import com.sprint.mission.discodeit.dto.BinaryContentDownloadResponse;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URI;
import java.time.Duration;
import java.util.UUID;

@Component
@ConditionalOnProperty(
        name = "discodeit.storage.type",
        havingValue = "s3"
)
@RequiredArgsConstructor
public class S3BinaryContentStorage implements BinaryContentStorage {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties properties;

    @Override
    public void put(UUID id, byte[] bytes) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "저장할 바이너리 콘텐츠 id는 필수입니다."
            );
        }

        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException(
                    "저장할 바이너리 데이터는 비어 있을 수 없습니다."
            );
        }

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.bucket())
                .key(resolveKey(id))
                .build();

        try {
            s3Client.putObject(
                    request,
                    RequestBody.fromBytes(bytes)
            );
        } catch (RuntimeException e) {
            throw new RuntimeException(
                    "바이너리 콘텐츠 S3 저장 중 오류가 발생했습니다. id=" + id,
                    e
            );
        }
    }

    @Override
    public byte[] get(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "조회할 바이너리 콘텐츠 id는 필수입니다."
            );
        }

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(properties.bucket())
                .key(resolveKey(id))
                .build();

        try {
            return s3Client
                    .getObjectAsBytes(request)
                    .asByteArray();

        } catch (NoSuchKeyException e) {
            throw new IllegalStateException(
                    "바이너리 콘텐츠 파일을 찾을 수 없습니다. id=" + id,
                    e
            );

        } catch (RuntimeException e) {
            throw new RuntimeException(
                    "바이너리 콘텐츠 S3 조회 중 오류가 발생했습니다. id=" + id,
                    e
            );
        }
    }

    @Override
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "삭제할 바이너리 콘텐츠 id는 필수입니다."
            );
        }

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(properties.bucket())
                .key(resolveKey(id))
                .build();

        try {
            s3Client.deleteObject(request);
        } catch (RuntimeException e) {
            throw new RuntimeException(
                    "바이너리 콘텐츠 S3 삭제 중 오류가 발생했습니다. id=" + id,
                    e
            );
        }
    }

    @Override
    public ResponseEntity<?> download(
            UUID id,
            BinaryContentDownloadResponse response
    ) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "다운로드할 바이너리 콘텐츠 id는 필수입니다."
            );
        }

        if (response == null) {
            throw new IllegalArgumentException(
                    "다운로드할 바이너리 콘텐츠 정보는 필수입니다."
            );
        }

        GetObjectRequest getObjectRequest =
                GetObjectRequest.builder()
                        .bucket(properties.bucket())
                        .key(resolveKey(id))
                        .responseContentType(response.getContentType())
                        .build();

        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(
                                Duration.ofSeconds(
                                        properties.presignedUrlExpiration()
                                )
                        )
                        .getObjectRequest(getObjectRequest)
                        .build();

        PresignedGetObjectRequest presignedRequest =
                s3Presigner.presignGetObject(presignRequest);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(
                        presignedRequest.url().toString()
                ))
                .build();
    }

    private String resolveKey(UUID id) {
        return id.toString();
    }
}