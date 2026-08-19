package com.sprint.mission.discodeit.storage.s3;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.storage.S3BinaryContentStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class S3BinaryContentStorageTest {

    private S3BinaryContentStorage storage;
    private S3Client s3Client;
    private final String bucketName = "discodeit-binary-content-storage-hj";
    private UUID binaryContentId;

    @BeforeEach
    void setUp() {
      storage = new S3BinaryContentStorage(bucketName,
              "ap-northeast-2", 600);

      s3Client = S3Client.builder()
              .region(Region.AP_NORTHEAST_2)
              .credentialsProvider(DefaultCredentialsProvider.create())
              .build();

      binaryContentId = UUID.randomUUID();
    }

    @Test
    void put_uploadsAndReadsBinaryContent() throws IOException {
        byte[] bytes = "S3 storage test".getBytes(StandardCharsets.UTF_8);

        UUID result = storage.put(binaryContentId, bytes);

        assertEquals(binaryContentId, result);

        try (InputStream inputStream = storage.get(binaryContentId)) {
            byte[] downloadedBytes = inputStream.readAllBytes();
            assertArrayEquals(bytes, downloadedBytes);
        }
    }

    @Test
    void download_redirectsToPresignedUrl() {
        byte[] bytes = "S3 storage test".getBytes(StandardCharsets.UTF_8);
        storage.put(binaryContentId, bytes);

        BinaryContentDto dto = new BinaryContentDto(
                binaryContentId,
                "test.txt",
                (long) bytes.length,
                "text/plain",
                bytes
        );

        ResponseEntity<?> download = storage.download(dto);

        assertEquals(HttpStatus.FOUND, download.getStatusCode());
        assertNotNull(download.getHeaders().getFirst(HttpHeaders.LOCATION));

    }

    @AfterEach
    void tearDown() {
        s3Client.deleteObject(builder -> builder
                .bucket(bucketName)
                .key(binaryContentId.toString())
        );
    }

}
