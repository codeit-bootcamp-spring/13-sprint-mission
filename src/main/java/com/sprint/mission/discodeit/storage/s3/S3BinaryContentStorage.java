package com.sprint.mission.discodeit.storage.s3;

import com.sprint.mission.discodeit.config.S3Properties;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;
import com.sprint.mission.discodeit.event.binarycontent.BinaryContentStorageFailedEvent;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.io.InputStream;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Recover;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "s3")
public class S3BinaryContentStorage implements BinaryContentStorage, ApplicationEventPublisherAware {

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;
  private final S3Properties s3Properties;
  private ApplicationEventPublisher eventPublisher;

  @Override
  public void setApplicationEventPublisher(ApplicationEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  @Override
  @Retryable(retryFor = SdkException.class, notRecoverable = IllegalArgumentException.class,
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000, multiplier = 2))
  public UUID put(UUID id, byte[] bytes) {
    if (id == null || bytes == null || bytes.length == 0) {
      throw new IllegalArgumentException("파일 ID는 필수이며 파일 데이터는 비어 있을 수 없습니다.");
    }

    s3Client.putObject(PutObjectRequest.builder()
        .bucket(s3Properties.getBucket())
        .key(key(id))
        .build(), RequestBody.fromBytes(bytes));
    return id;
  }

  @Recover
  public UUID recoverPut(SdkException exception, UUID id, byte[] bytes) {
    String requestId = MDC.get("requestId");
    if (requestId == null) {
      requestId = "요청 ID 없음";
    }
    String operation = "S3BinaryContentStorage.put";
    log.error("S3 저장 재시도 소진: operation={}, requestId={}, binaryContentId={}",
        operation, requestId, id, exception);
    try {
      eventPublisher.publishEvent(new BinaryContentStorageFailedEvent(
          operation, requestId, id, exception.getMessage()));
    } catch (RuntimeException notificationException) {
      log.error("관리자 실패 알림 저장 실패: requestId={}, binaryContentId={}",
          requestId, id, notificationException);
      exception.addSuppressed(notificationException);
    }
    // 저장 성공으로 처리되지 않도록 원래 예외를 호출자에게 전달한다.
    throw exception;
  }

  @Override
  public InputStream get(UUID id) {
    return s3Client.getObject(GetObjectRequest.builder()
        .bucket(s3Properties.getBucket())
        .key(key(id))
        .build(), software.amazon.awssdk.core.sync.ResponseTransformer.toInputStream());
  }

  @Override
  public ResponseEntity<Resource> download(BinaryContentResponse binaryContent) {
    PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(
        GetObjectPresignRequest.builder()
            .signatureDuration(s3Properties.getPresignedUrlExpiration())
            .getObjectRequest(GetObjectRequest.builder()
                .bucket(s3Properties.getBucket())
                .key(key(binaryContent.id()))
                .responseContentType(binaryContent.contentType())
                .responseContentDisposition("attachment; filename=\"" + binaryContent.fileName() + "\"")
                .build())
            .build());

    return ResponseEntity.status(HttpStatus.FOUND)
        .location(URI.create(presignedRequest.url().toString()))
        .build();
  }

  private String key(UUID id) {
    return id.toString();
  }
}
