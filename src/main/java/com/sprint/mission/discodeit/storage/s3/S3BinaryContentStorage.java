package com.sprint.mission.discodeit.storage.s3;

import com.sprint.mission.discodeit.dto.response.BinaryContentResponse;
import com.sprint.mission.discodeit.entity.UserRole;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import jakarta.annotation.PreDestroy;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Slf4j
@Component
@ConditionalOnProperty(
        prefix = "discodeit.storage",
        name = "type",
        havingValue = "s3"
)
public class S3BinaryContentStorage implements BinaryContentStorage {

    private static final String PUT_TASK_NAME =
            "S3BinaryContentStorage.put";

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucket;
    private final Duration presignedUrlExpiration;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Autowired
    public S3BinaryContentStorage(
            @Value("${discodeit.storage.s3.region}")
            String region,
            @Value("${discodeit.storage.s3.bucket}")
            String bucket,
            @Value("${discodeit.storage.s3.presigned-url-expiration}")
            long presignedUrlExpiration,
            UserRepository userRepository,
            NotificationService notificationService
    ) {
        Region awsRegion = Region.of(region);
        DefaultCredentialsProvider credentialsProvider =
                DefaultCredentialsProvider.create();

        this.s3Client = S3Client.builder()
                .region(awsRegion)
                .credentialsProvider(credentialsProvider)
                .build();

        this.s3Presigner = S3Presigner.builder()
                .region(awsRegion)
                .credentialsProvider(credentialsProvider)
                .build();

        this.bucket = bucket;
        this.presignedUrlExpiration =
                Duration.ofSeconds(presignedUrlExpiration);
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    S3BinaryContentStorage(
            S3Client s3Client,
            S3Presigner s3Presigner,
            String bucket,
            Duration presignedUrlExpiration
    ) {
        this(
                s3Client,
                s3Presigner,
                bucket,
                presignedUrlExpiration,
                null,
                null
        );
    }

    S3BinaryContentStorage(
            S3Client s3Client,
            S3Presigner s3Presigner,
            String bucket,
            Duration presignedUrlExpiration,
            UserRepository userRepository,
            NotificationService notificationService
    ) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucket = bucket;
        this.presignedUrlExpiration = presignedUrlExpiration;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Retryable(
            retryFor = SdkException.class,
            maxAttempts = 3,
            backoff = @Backoff(
                    delay = 1000,
                    multiplier = 2.0
            )
    )
    public UUID put(
            UUID binaryContentId,
            byte[] bytes
    ) {
        PutObjectRequest request =
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(binaryContentId.toString())
                        .build();

        s3Client.putObject(
                request,
                RequestBody.fromBytes(bytes)
        );

        return binaryContentId;
    }

    @Recover
    public UUID recoverPut(
            SdkException exception,
            UUID binaryContentId,
            byte[] bytes
    ) {
        String requestId = Optional
                .ofNullable(MDC.get("requestId"))
                .orElse("unknown");

        String errorMessage =
                Optional.ofNullable(exception.getMessage())
                        .orElse(exception.getClass().getSimpleName());

        String notificationContent = """
                Task: %s
                RequestId: %s
                BinaryContentId: %s
                Error: %s
                """.formatted(
                PUT_TASK_NAME,
                requestId,
                binaryContentId,
                errorMessage
        );

        log.error(
                "비동기 작업 최종 실패: task={}, requestId={}, "
                        + "binaryContentId={}, error={}",
                PUT_TASK_NAME,
                requestId,
                binaryContentId,
                errorMessage,
                exception
        );

        userRepository.findAllByRole(UserRole.ADMIN)
                .forEach(admin -> notificationService.create(
                        admin.getId(),
                        "S3 바이너리 데이터 저장 실패",
                        notificationContent
                ));

        throw exception;
    }

    @Override
    public InputStream get(UUID binaryContentId) {
        GetObjectRequest request =
                GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(binaryContentId.toString())
                        .build();

        ResponseInputStream<GetObjectResponse> response =
                s3Client.getObject(request);

        return response;
    }

    @Override
    public ResponseEntity<?> download(
            BinaryContentResponse metadata
    ) {
        GetObjectRequest getObjectRequest =
                GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(metadata.id().toString())
                        .responseContentType(
                                metadata.contentType()
                        )
                        .build();

        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(
                                presignedUrlExpiration
                        )
                        .getObjectRequest(getObjectRequest)
                        .build();

        PresignedGetObjectRequest presignedRequest =
                s3Presigner.presignGetObject(presignRequest);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(
                        URI.create(
                                presignedRequest
                                        .url()
                                        .toString()
                        )
                )
                .build();
    }

    @PreDestroy
    void close() {
        s3Presigner.close();
        s3Client.close();
    }
}