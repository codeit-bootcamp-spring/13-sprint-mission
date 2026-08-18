package com.sprint.mission.discodeit.storage.s3;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentDeleteException;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentDownloadException;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentUploadException;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    prefix = "discodeit.storage",
    name = "type",
    havingValue = "s3"
)
public class S3BinaryContentStorage implements BinaryContentStorage {

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;
  private final S3Properties properties;

  @Override
  public UUID put(UUID id, byte[] bytes) {
    try {
      PutObjectRequest request = PutObjectRequest.builder()
          .bucket(properties.getBucket())
          .key(id.toString())
          .build();

      s3Client.putObject(request, RequestBody.fromBytes(bytes));

      log.debug("S3 파일 업로드 완료: binaryContentId={}", id);
      return id;
    } catch (RuntimeException e) {
      log.error("S3 파일 업로드 실패: bucket={}, binaryContentId={}", properties.getBucket(), id, e);
      throw new BinaryContentUploadException(id, e);
    }
  }

  @Override
  public InputStream get(UUID id) {
    try {
      GetObjectRequest request = GetObjectRequest.builder()
          .bucket(properties.getBucket())
          .key(id.toString())
          .build();

      return s3Client.getObject(request);
    } catch (RuntimeException e) {
      throw new BinaryContentDownloadException(id, e);
    }
  }

  @Override
  public void delete(UUID id) {
    try {
      DeleteObjectRequest request = DeleteObjectRequest.builder()
          .bucket(properties.getBucket())
          .key(id.toString())
          .build();

      s3Client.deleteObject(request);

      log.debug("S3 파일 삭제 완료: binaryContentId={}", id);
    } catch (RuntimeException e) {
      throw new BinaryContentDeleteException(id, e);
    }
  }

  @Override
  public ResponseEntity<Void> download(BinaryContentDto binaryContentDto) {
    try {
      String presignedUrl = generatePresignedUrl(binaryContentDto);

      return ResponseEntity.status(HttpStatus.FOUND)
          .location(URI.create(presignedUrl))
          .build();
    } catch (RuntimeException e) {
      throw new BinaryContentDownloadException(binaryContentDto.id(), e);
    }
  }

  private String generatePresignedUrl(BinaryContentDto binaryContentDto) {
    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(properties.getBucket())
        .key(binaryContentDto.id().toString())
        .responseContentType(binaryContentDto.contentType())
        .responseContentDisposition(
            "attachment; filename=\"" + binaryContentDto.fileName() + "\""
        )
        .build();

    GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
        .signatureDuration(
            Duration.ofSeconds(properties.getPresignedUrlExpiration())
        )
        .getObjectRequest(getObjectRequest)
        .build();

    return s3Presigner.presignGetObject(presignRequest)
        .url()
        .toString();
  }
}
