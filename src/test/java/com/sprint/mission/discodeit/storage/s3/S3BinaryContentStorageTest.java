package com.sprint.mission.discodeit.storage.s3;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentStorageException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class S3BinaryContentStorageTest {

    private final S3Client s3Client = mock(S3Client.class);
    private final S3Presigner s3Presigner = mock(S3Presigner.class);
    private final S3StorageProperties properties = new S3StorageProperties(
            "",
            "",
            "ap-northeast-2",
            "test-bucket",
            600
    );

    private final S3BinaryContentStorage storage =
            new S3BinaryContentStorage(s3Client, s3Presigner, properties);

    @Test
    void put_success() {
        UUID id = UUID.randomUUID();
        byte[] bytes = "hello".getBytes();

        UUID result = storage.put(id, bytes);

        assertThat(result).isEqualTo(id);
        verify(s3Client).putObject(any(Consumer.class), any(RequestBody.class));
    }

    @Test
    void put_fail() {
        UUID id = UUID.randomUUID();

        given(s3Client.putObject(any(Consumer.class), any(RequestBody.class)))
                .willThrow(new RuntimeException());

        assertThatThrownBy(() -> storage.put(id, "hello".getBytes()))
                .isInstanceOf(BinaryContentStorageException.class);
    }

    @Test
    void get_success() {
        UUID id = UUID.randomUUID();
        ResponseInputStream<GetObjectResponse> response = new ResponseInputStream<>(
                GetObjectResponse.builder().build(),
                new ByteArrayInputStream("hello".getBytes())
        );

        given(s3Client.getObject(any(Consumer.class))).willReturn(response);

        InputStream result = storage.get(id);

        assertThat(result).isNotNull();
        verify(s3Client).getObject(any(Consumer.class));
    }

    @Test
    void get_fail() {
        UUID id = UUID.randomUUID();

        given(s3Client.getObject(any(Consumer.class)))
                .willThrow(new RuntimeException());

        assertThatThrownBy(() -> storage.get(id))
                .isInstanceOf(BinaryContentNotFoundException.class);
    }

    @Test
    void delete_success() {
        UUID id = UUID.randomUUID();

        storage.delete(id);

        verify(s3Client).deleteObject(any(Consumer.class));
    }

    @Test
    void download_redirectsToPresignedUrl() throws Exception {
        UUID id = UUID.randomUUID();
        BinaryContentDto dto = new BinaryContentDto(
                id,
                "test.txt",
                5L,
                "text/plain"
        );

        PresignedGetObjectRequest presignedRequest = mock(PresignedGetObjectRequest.class);
        given(presignedRequest.url()).willReturn(new URL("https://test-bucket.s3.amazonaws.com/" + id));
        given(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .willReturn(presignedRequest);

        ResponseEntity<?> response = storage.download(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(response.getHeaders().getLocation()).isNotNull();
        assertThat(response.getHeaders().getLocation().toString()).contains(id.toString());
    }
}