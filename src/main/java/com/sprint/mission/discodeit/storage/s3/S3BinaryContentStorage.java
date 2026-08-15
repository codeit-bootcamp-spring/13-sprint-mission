package com.sprint.mission.discodeit.storage.s3;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Slf4j
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "s3")
@Component
@RequiredArgsConstructor
public class S3BinaryContentStorage implements BinaryContentStorage {

  private final S3StorageProperties properties;
  private S3Client s3Client;
  private S3Presigner s3Presigner;

  @PostConstruct
  public void init() {
    log.debug("S3BinaryContentStorage 초기화 시작");

    // AWS 기본 자격증명 생성
    AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(
        properties.accessKey(),
        properties.secretKey()
    );

    // S3Client 빌드
    this.s3Client = S3Client.builder()
        .region(Region.of(properties.region()))
        .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
        .build();

    // S3Presigner 빌드 (Presigned URL 생성용)
    this.s3Presigner = S3Presigner.builder()
        .region(Region.of(properties.region()))
        .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
        .build();

    log.info("S3BinaryContentStorage 초기화 완료: bucket={}, region={}",
        properties.bucket(), properties.region());
  }

  @Override
  public UUID put(UUID binaryContentId, byte[] bytes) {
    try {
      log.debug("S3 파일 업로드 시작: id={}, size={}", binaryContentId, bytes.length);

      // S3 업로드 요청 객체 생성
      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
          .bucket(properties.bucket())
          .key(binaryContentId.toString())
          .contentType("application/octet-stream")
          .build();

      // S3에 파일 업로드
      s3Client.putObject(putObjectRequest,
          RequestBody.fromBytes(bytes));

      log.info("S3에 파일 업로드 성공: id={}, size={}", binaryContentId, bytes.length);
      return binaryContentId;
    } catch (Exception e) {
      log.error("S3 파일 업로드 실패: id={}", binaryContentId, e);
      throw new RuntimeException("S3 파일 업로드 실패", e);
    }
  }

  @Override
  public InputStream get(UUID binaryContentId) {
    try {
      log.debug("S3 파일 다운로드 시작: id={}", binaryContentId);

      // S3 다운로드 요청 객체 생성
      GetObjectRequest getObjectRequest = GetObjectRequest.builder()
          .bucket(properties.bucket())
          .key(binaryContentId.toString())
          .build();

      // S3에서 파일 다운로드
      InputStream inputStream = s3Client.getObject(getObjectRequest,
          ResponseTransformer.toInputStream());

      log.info("S3에서 파일 다운로드 성공: id={}", binaryContentId);
      return inputStream;
    } catch (NoSuchKeyException e) {
      log.error("S3 파일 없음: id={}", binaryContentId);
      throw new NoSuchElementException("File with key " + binaryContentId + " does not exist");
    } catch (Exception e) {
      log.error("S3 파일 다운로드 실패: id={}", binaryContentId, e);
      throw new RuntimeException("S3 파일 다운로드 실패", e);
    }
  }

  @Override
  public ResponseEntity<?> download(BinaryContentDto metaData) {
    try {
      log.debug("Presigned URL 생성 시작: id={}", metaData.id());

      // Presigned URL 생성
      String presignedUrl = generatePresignedUrl(metaData.id());

      return ResponseEntity
          .status(HttpStatus.FOUND)
          .header(HttpHeaders.LOCATION, presignedUrl)
          .build();
    } catch (Exception e) {
      log.error("Presigned URL 생성 실패: id={}", metaData.id(), e);
      throw new RuntimeException("Presigned URL 생성 실패", e);
    }
  }

  private String generatePresignedUrl(UUID binaryContentId) {
    // Presigned URL 생성 요청 객체 생성
    GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
        .getObjectRequest(builder -> builder
            .bucket(properties.bucket())
            .key(binaryContentId.toString())
            .build())
        .signatureDuration(Duration.ofSeconds(properties.presignedUrlExpiration()))
        .build();

    // Presigned URL 생성
    PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
    String presignedUrl = presignedRequest.url().toString();

    log.info("Presigned URL 생성 성공: id={}, expiration={}초",
        binaryContentId, properties.presignedUrlExpiration());

    return presignedUrl;
  }
}
