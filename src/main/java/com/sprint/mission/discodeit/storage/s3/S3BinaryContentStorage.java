package com.sprint.mission.discodeit.storage.s3;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentStorageException;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "s3")
public class S3BinaryContentStorage implements BinaryContentStorage {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3StorageProperties properties;

    public S3BinaryContentStorage(
            S3Client s3Client,
            S3Presigner s3Presigner,
            S3StorageProperties properties
    ) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.properties = properties;
    }

    @Override
    public UUID put(UUID binaryContentId, byte[] bytes) {
        try {
            s3Client.putObject(builder -> builder
                            .bucket(properties.bucket())
                            .key(resolveKey(binaryContentId)),
                    RequestBody.fromBytes(bytes));

            return binaryContentId;
        } catch (Exception e) {
            throw new BinaryContentStorageException(binaryContentId);
        }
    }

    @Override
    public InputStream get(UUID binaryContentId) {
        try {
            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(builder -> builder
                    .bucket(properties.bucket())
                    .key(resolveKey(binaryContentId)));

            return response;
        } catch (Exception e) {
            throw new BinaryContentNotFoundException(binaryContentId);
        }
    }

    @Override
    public void delete(UUID binaryContentId) {
        try {
            s3Client.deleteObject(builder -> builder
                    .bucket(properties.bucket())
                    .key(resolveKey(binaryContentId)));
        } catch (Exception e) {
            throw new BinaryContentStorageException(binaryContentId);
        }
    }

    @Override
    public ResponseEntity<?> download(BinaryContentDto binaryContentDto) {
        String presignedUrl = generatePresignedUrl(binaryContentDto);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(presignedUrl))
                .build();
    }

    private String generatePresignedUrl(BinaryContentDto binaryContentDto) {
        GetObjectPresignRequest request = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(properties.presignedUrlExpiration()))
                .getObjectRequest(builder -> builder
                        .bucket(properties.bucket())
                        .key(resolveKey(binaryContentDto.id()))
                        .responseContentType(binaryContentDto.contentType()))
                .build();

        return s3Presigner.presignGetObject(request)
                .url()
                .toString();
    }

    private String resolveKey(UUID binaryContentId) {
        return binaryContentId.toString();
    }
}