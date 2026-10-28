package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.config.RequestIdFilter;
import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Component
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "s3")
public class S3BinaryContentStorage implements BinaryContentStorage {

  private final String accessKey;
  private final String secretKey;
  private final String region;
  private final String bucket;
  private final long expiration;
  private final UserRepository userRepository;
  private final NotificationService notificationService;

  public S3BinaryContentStorage(
      @Value("${discodeit.storage.s3.access-key:}") String accessKey,
      @Value("${discodeit.storage.s3.secret-key:}") String secretKey,
      @Value("${discodeit.storage.s3.region}") String region,
      @Value("${discodeit.storage.s3.bucket}") String bucket,
      @Value("${discodeit.storage.s3.presigned-url-expiration:600}") long expiration,
      UserRepository userRepository,
      NotificationService notificationService) {
    this.accessKey = accessKey;
    this.secretKey = secretKey;
    this.region = region;
    this.bucket = bucket;
    this.expiration = expiration;
    this.userRepository = userRepository;
    this.notificationService = notificationService;
  }

  @Override
  @Retryable(
      retryFor = SdkException.class,
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000, multiplier = 2)
  )
  public UUID put(UUID id, byte[] data) {
    S3Client s3Client = getS3Client();
    String key = id.toString();

    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .build();

    s3Client.putObject(putObjectRequest, RequestBody.fromBytes(data));
    return id;
  }

  @Recover
  public UUID recover(SdkException e, UUID id, byte[] data) {
    String requestId = MDC.get(RequestIdFilter.REQUEST_ID_KEY);

    if (requestId == null) {
      requestId = "unknown";
    }

    String title = "S3 파일 업로드 실패";

    String content =
        "Task: S3 파일 업로드\n"
            + "RequestId: " + requestId + "\n"
            + "BinaryContentId: " + id + "\n"
            + "Error: " + e.getMessage();

    List<User> admins = userRepository.findByRole(Role.ADMIN);

    for (User admin : admins) {
      notificationService.create(
          admin.getId(),
          title,
          content
      );
    }

    throw e;
  }

  @Override
  public InputStream get(UUID id) {
    S3Client s3Client = getS3Client();
    String key = id.toString();

    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .build();

    return s3Client.getObject(getObjectRequest);
  }

  @Override
  public ResponseEntity<Void> download(BinaryContentDto dto) {
    String url = generatePresignedUrl(dto.id().toString(), dto.contentType());
    return ResponseEntity.status(HttpStatus.FOUND)
        .location(URI.create(url))
        .build();
  }

  private S3Client getS3Client() {
    AwsCredentialsProvider credentialsProvider;
    if (hasValidStaticCredentials()) {
      credentialsProvider = StaticCredentialsProvider.create(
          AwsBasicCredentials.create(accessKey, secretKey));
    } else {
      credentialsProvider = DefaultCredentialsProvider.create();
    }

    return S3Client.builder()
        .region(Region.of(region))
        .credentialsProvider(credentialsProvider)
        .build();
  }

  private String generatePresignedUrl(String key, String contentType) {
    AwsCredentialsProvider credentialsProvider;
    if (hasValidStaticCredentials()) {
      credentialsProvider = StaticCredentialsProvider.create(
          AwsBasicCredentials.create(accessKey, secretKey));
    } else {
      credentialsProvider = DefaultCredentialsProvider.create();
    }

    try (S3Presigner presigner = S3Presigner.builder()
        .region(Region.of(region))
        .credentialsProvider(credentialsProvider)
        .build()) {

      GetObjectRequest getObjectRequest = GetObjectRequest.builder()
          .bucket(bucket)
          .key(key)
          .responseContentType(contentType)
          .build();

      GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
          .signatureDuration(Duration.ofSeconds(expiration))
          .getObjectRequest(getObjectRequest)
          .build();

      PresignedGetObjectRequest presignedGetObjectRequest = presigner.presignGetObject(
          presignRequest);
      return presignedGetObjectRequest.url().toString();
    }
  }

  private boolean hasValidStaticCredentials() {
    return accessKey != null
        && !accessKey.isBlank()
        && secretKey != null
        && !secretKey.isBlank();
  }
}