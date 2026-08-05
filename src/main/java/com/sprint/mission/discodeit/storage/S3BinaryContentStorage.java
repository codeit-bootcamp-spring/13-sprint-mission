package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.config.S3Property;
import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.exception.FileStorageException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;

@RequiredArgsConstructor
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "s3")
public class S3BinaryContentStorage implements BinaryContentStorage {

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    private final S3Property s3Property;

    @Override
    public UUID put(UUID id, byte[] content) {

        PutObjectRequest request = PutObjectRequest
                .builder()
                .bucket(s3Property.getBucket())
                .key(id.toString())
                .build();
        try{
            s3Client.putObject(request, RequestBody.fromBytes(content));
            return id;
        } catch (SdkClientException e) {
            throw new FileStorageException("S3 storage upload error");
        }
    }

    @Override
    public InputStream get(UUID id) throws IOException {
        return null;
    }
    @Override
    public ResponseEntity<?> download(BinaryContentDto binaryContentDto) {
        String key = binaryContentDto.id().toString();
        String url = generatePresignedUrl(key, binaryContentDto.contentType());
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url)).build();
    }

    private S3Client getS3Client() {
        return null;
    }

    private String generatePresignedUrl(String key, String contentType){
        GetObjectPresignRequest request = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(s3Property.getPresignTime()))
                .getObjectRequest(GetObjectRequest.builder()
                        .bucket(s3Property.getBucket())
                        .key(key)
                        .responseContentDisposition("attachement; filename=\"" + key + "\"")
                        .build()
                ).build();
        return s3Presigner.presignGetObject(request).url().toString();
    }


}
