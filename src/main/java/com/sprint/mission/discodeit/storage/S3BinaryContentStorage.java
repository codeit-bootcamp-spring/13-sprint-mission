package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;

@Component
@ConditionalOnProperty(
        name = "discodeit.storage.type",
        havingValue = "S3"
)
public class S3BinaryContentStorage implements BinaryContentStorage{

    private final String bucketName;
    private final String region;
    private final long presignedUrlExpiration;

    public S3BinaryContentStorage(
            @Value("${discodeit.storage.s3.bucket}") String bucketName,
            @Value("${discodeit.storage.s3.region}") String region,
            @Value("${discodeit.storage.s3.presigned-url-expiration}")
            long presignedUrlExpiration
    ) {
        this.bucketName = bucketName;
        this.region = region;
        this.presignedUrlExpiration = presignedUrlExpiration;
    }

    @Override
    public UUID put(UUID binaryContentId, byte[] bytes) {
        if (binaryContentId == null) {
            throw new IllegalArgumentException(
                    "바이너리 콘텐츠 ID는 필수입니다."
            );
        }

        if (bytes == null) {
            throw new IllegalArgumentException(
                    "저장할 바이너리 데이터는 필수입니다."
            );
        }
        String key = binaryContentId.toString();
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        getS3Client().putObject(request, RequestBody.fromBytes(bytes));

        return binaryContentId;
    }

    @Override
    public InputStream get(UUID binaryContentId) {
        if (binaryContentId == null) {
            throw new IllegalArgumentException("바이너리 콘텐츠 ID는 필수입니다.");
        }
        String key = binaryContentId.toString();
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
       InputStream object = getS3Client().getObject(request);
       return object;
    }

    @Override
    public ResponseEntity<?> download(BinaryContentDto binaryContentDto) {
        if(binaryContentDto == null) {
            throw new IllegalArgumentException("다운로드할 바이너리 콘텐츠 정보는 필수입니다.");
        }
        String key = binaryContentDto.id().toString();
        String contentType = binaryContentDto.contentType();
        String presignedUrl = generatePresignedUrl(key, contentType);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, presignedUrl)
                .build();

    }

    S3Client getS3Client() {
        S3Client build = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
        return build;
    }

    String generatePresignedUrl(String key, String contentType) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .responseContentType(contentType)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(presignedUrlExpiration))
                .getObjectRequest(request)
                .build();

        PresignedGetObjectRequest presignedRequest = S3Presigner.builder().region(Region.of(region)).credentialsProvider(DefaultCredentialsProvider.create())
                .build().presignGetObject(presignRequest);
        return presignedRequest.url().toString();

    }
}
