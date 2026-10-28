package com.sprint.mission.discodeit.storage.s3;


import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.exception.FileStorageException;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "discodeit.storage", name = "type", havingValue = "s3")
public class S3BinaryContentStorage implements BinaryContentStorage {

  private final S3Presigner s3Presigner;
  private final S3Properties s3Properties;
  private final S3Client s3Client;

  public S3BinaryContentStorage(S3Client s3Client, S3Presigner s3Presigner,
      S3Properties properties) {
    this.s3Presigner = s3Presigner;
    this.s3Properties = properties;
    this.s3Client = s3Client;
  }

  private S3Client getS3Client() {
    return s3Client;
  }

  @Override
  public UUID put(UUID id, byte[] bytes) {
    String key = id.toString().replace("-", "");

    PutObjectRequest request = PutObjectRequest.builder()
        .bucket(s3Properties.getBucket())
        .key(key)
        .build();

    try {
      getS3Client().putObject(request, RequestBody.fromBytes(bytes));
      log.info("S3 객체 업로드 완료: {}", key);

      return id;
    } catch (S3Exception | SdkClientException e) {

      throw new FileStorageException("S3 객체 저장 실패: " + id, e);
    }
  }

  @Override
  public InputStream get(UUID id) {
    GetObjectRequest request = GetObjectRequest.builder()
        .bucket(s3Properties.getBucket())
        .key(id.toString().replace("-", ""))
        .build();

    try {
      return getS3Client().getObject(request);
    } catch (NoSuchKeyException e) {
      throw new FileStorageException("존재하지 않는 S3 객체: " + id, e);
    } catch (S3Exception | SdkClientException e) {
      throw new FileStorageException("S3 객체 조회 실패: " + id, e);
    }

  }

  @Override
  public ResponseEntity<Void> download(BinaryContentDto binaryContentDto) {
    String presignedUrl = generatePresignedUrl(binaryContentDto.getId().toString().replace("-", ""),
        binaryContentDto.getContentType(),
        binaryContentDto.getFileName());

    return ResponseEntity.status(HttpStatus.FOUND)
        .location(URI.create(presignedUrl))
        .build();
  }

  private String generatePresignedUrl(String key, String contentType, String fileName) {
    GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
        .signatureDuration(
            Duration.ofSeconds(s3Properties.getPresignedUrlExpiration())) // yaml 요구사항 초 단위 600
        .getObjectRequest(GetObjectRequest.builder()
            .bucket(s3Properties.getBucket())
            .key(key)
            .responseContentType(contentType)
            .responseContentDisposition(ContentDisposition.attachment()
                .filename(fileName, StandardCharsets.UTF_8) // 한글 파일명 처리
                .build().toString())
            .build())
        .build();

    try {
      return s3Presigner.presignGetObject(presignRequest).url().toString();
    } catch (Exception e) {
      throw new FileStorageException("S3 Presigned URL 생성 실패: " + key, e);
    }
  }
}
