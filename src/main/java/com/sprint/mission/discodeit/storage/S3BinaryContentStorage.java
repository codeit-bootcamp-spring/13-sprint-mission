package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.file.FileStorageException;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Component
@ConditionalOnProperty(
        value = "discodeit.storage.type",
        havingValue = "s3"
)
public class S3BinaryContentStorage implements BinaryContentStorage {

    private final String accessKey;
    private final String secretKey;
    private final String region;
    private final String bucket;
    private final long presignedUrlExpiration;

    private final URI endpoint;

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    @Autowired
    public S3BinaryContentStorage(
            @Value("${discodeit.storage.s3.region}") String region,
            @Value("${discodeit.storage.s3.bucket}") String bucket,
            @Value("${discodeit.storage.s3.presigned-url-expiration}") long presignedUrlExpiration,
            UserRepository userRepository,
            NotificationRepository notificationRepository
    ) {
        this.region = region;
        this.bucket = bucket;
        this.presignedUrlExpiration = presignedUrlExpiration;

        this.accessKey = null;
        this.secretKey = null;
        this.endpoint = null;

        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    //테스트용 생성자
    S3BinaryContentStorage(
            String accessKey,
            String secretKey,
            String region,
            String bucket,
            long presignedUrlExpiration,
            URI endpoint,
            UserRepository userRepository,
            NotificationRepository notificationRepository
    ) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.region = region;
        this.bucket = bucket;
        this.presignedUrlExpiration = presignedUrlExpiration;
        this.endpoint = endpoint;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    @Retryable(
            retryFor = FileStorageException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    @Override
    public UUID put(UUID binaryContentId, byte[] bytes) {
        String key = binaryContentId.toString();

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        try (S3Client s3Client = getS3Client()) {
            s3Client.putObject(
                    request,
                    RequestBody.fromBytes(bytes)
            );
        } catch (Exception e) {
            throw new FileStorageException(binaryContentId);
        }

        return binaryContentId;
    }

    @Recover
    public UUID recover(FileStorageException e, UUID binaryContentId, byte[] bytes) {
        String requestId = MDC.get("requestId");

        String content = """
                RequestId: %s
                BinaryContentId: %s
                Error: %s
                """.formatted(
                requestId,
                binaryContentId,
                e.getMessage()
        );

        List<User> admins = userRepository.findAllByRole(Role.ADMIN);

        List<Notification> notifications = admins.stream()
                .map(admin -> new Notification(
                        admin,
                        "S3 파일 업로드 실패",
                        content
                ))
                .toList();

        notificationRepository.saveAll(notifications);

        return binaryContentId;
    }

    @Override
    public InputStream get(UUID binaryContentId) {
        String key = binaryContentId.toString();

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        S3Client s3Client = getS3Client();

        return s3Client.getObject(request);
    }

    @Override
    public ResponseEntity<Resource> download(BinaryContentDto binaryContentDto) {
        String url = generatePresignedUrl(
                binaryContentDto.id().toString(),
                binaryContentDto.contentType()
        );

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, url)
                .build();
    }


    private S3Client getS3Client() {

        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(region));

        // 테스트 환경
        if (endpoint != null) {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

            builder.credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .endpointOverride(endpoint)
                    .forcePathStyle(true);
        }

        // 운영에서는 credentialsProvider를 지정하지 않음
        return builder.build();
    }

    private String generatePresignedUrl(String key, String contentType) {

        S3Presigner.Builder builder = S3Presigner.builder()
                .region(Region.of(region));

        if (endpoint != null) {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

            builder.credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .endpointOverride(endpoint)
                    .serviceConfiguration(
                            S3Configuration.builder()
                                    .pathStyleAccessEnabled(true)
                                    .build()
                    );
        }

        try (S3Presigner presigner = builder.build()) {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .responseContentType(contentType)
                    .build();

            GetObjectPresignRequest presignRequest =
                    GetObjectPresignRequest.builder()
                            .signatureDuration(Duration.ofSeconds(presignedUrlExpiration))
                            .getObjectRequest(getObjectRequest)
                            .build();

            return presigner
                    .presignGetObject(presignRequest)
                    .url()
                    .toString();
        }
    }

}
