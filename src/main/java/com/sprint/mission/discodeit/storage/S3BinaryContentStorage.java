package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Component
@ConditionalOnProperty(
        name = "discodeit.storage.type",
        havingValue = "s3"
)
public class S3BinaryContentStorage implements BinaryContentStorage {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucket;
    private final long presignedUrlExpiration;

    public S3BinaryContentStorage(
            @Value("${discodeit.storage.s3.access-key}") String accessKey,
            @Value("${discodeit.storage.s3.secret-key}") String secretKey,
            @Value("${discodeit.storage.s3.region}") String region,
            @Value("${discodeit.storage.s3.bucket}") String bucket,
            @Value("${discodeit.storage.s3.presigned-url-expiration}") long presignedUrlExpiration
    ) {
        AwsBasicCredentials credentials =
                AwsBasicCredentials.create(accessKey, secretKey);

        StaticCredentialsProvider credentialsProvider =
                StaticCredentialsProvider.create(credentials);

        Region awsRegion = Region.of(region);

        this.s3Client = S3Client.builder()
                .region(awsRegion)
                .credentialsProvider(credentialsProvider)
                .build();

        this.s3Presigner = S3Presigner.builder()
                .region(awsRegion)
                .credentialsProvider(credentialsProvider)
                .build();

        this.bucket = bucket;
        this.presignedUrlExpiration = presignedUrlExpiration;
    }

    @Override
    public void put(UUID id, byte[] bytes) {

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(id.toString())
                .build();

        s3Client.putObject(
                request,
                RequestBody.fromBytes(bytes)
        );
    }

    @Override
    public ResponseEntity<?> download(
            BinaryContentDto binaryContentDto
    ) {

        GetObjectRequest getObjectRequest =
                GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(binaryContentDto.id().toString())
                        .responseContentType(
                                binaryContentDto.contentType()
                        )
                        .responseContentDisposition(
                                "attachment; filename=\"" +
                                        binaryContentDto.fileName() +
                                        "\""
                        )
                        .build();

        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(
                                Duration.ofSeconds(
                                        presignedUrlExpiration
                                )
                        )
                        .getObjectRequest(getObjectRequest)
                        .build();

        String presignedUrl =
                s3Presigner
                        .presignGetObject(presignRequest)
                        .url()
                        .toString();

        return ResponseEntity
                .status(302)
                .header(
                        HttpHeaders.LOCATION,
                        presignedUrl
                )
                .build();
    }
}