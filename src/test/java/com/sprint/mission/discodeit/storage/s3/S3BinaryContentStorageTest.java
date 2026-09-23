package com.sprint.mission.discodeit.storage.s3;

import com.sprint.mission.discodeit.config.S3Properties;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import static org.mockito.ArgumentMatchers.any;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3BinaryContentStorageTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @Mock
    private S3Properties props;

    @InjectMocks
    private S3BinaryContentStorage storage;

    private UUID id;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
    }


    @Test
    @DisplayName("put: 올바른 buket과 key로 S3에 업로드 한다.")
    void put_success() {
        // given
        when(props.getBucket()).thenReturn("test-bucket");
        byte[] bytes = "hello".getBytes(StandardCharsets.UTF_8);

        // when
        UUID result = storage.put(id, bytes);


        // then
        assertThat(result).isEqualTo(id);
        ArgumentCaptor<PutObjectRequest> requestCaptor =
                ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));

        PutObjectRequest captured = requestCaptor.getValue();
        assertThat(captured.bucket()).isEqualTo("test-bucket");
        assertThat(captured.key()).isEqualTo(id.toString());
    }

    @Test
    @DisplayName("get: 올바른 bucket과 key로 S3에서 조회한다.")
    void get_success() {
        // given
        when(props.getBucket()).thenReturn("test-bucket");

        ResponseInputStream<GetObjectResponse> fakeStream = new ResponseInputStream<>(
                GetObjectResponse.builder().build(),
                new ByteArrayInputStream("data".getBytes(StandardCharsets.UTF_8))
        );
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(fakeStream);
        var result = storage.get(id);

        // then
        assertThat(result).isNotNull();

        ArgumentCaptor<GetObjectRequest> requestCaptor =
                ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(s3Client).getObject(requestCaptor.capture());

        GetObjectRequest captured = requestCaptor.getValue();
        assertThat(captured.bucket()).isEqualTo("test-bucket");
        assertThat(captured.key()).isEqualTo(id.toString());

    }

    @Test
    @DisplayName("download: 302 리다이렉트와 presigned URL을 Location 헤더에 담아 반환한다")
    void download_success() throws Exception {
        // given
        when(props.getBucket()).thenReturn("test-bucket");
        when(props.getPresignedUrlExpiration()).thenReturn(600L);

        BinaryContentDto dto = new BinaryContentDto(id, "스크린샷.jpeg", 100L, "image/jpeg");

        String fakeUrl = "https://test-bucket.s3.amazonaws.com/" + id + "?X-Amz-Signature=abc";
        PresignedGetObjectRequest presigned = mock(PresignedGetObjectRequest.class);
        when(presigned.url()).thenReturn(new URL(fakeUrl));
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .thenReturn(presigned);

        // when
        ResponseEntity<Resource> response = storage.download(dto);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(response.getHeaders().getFirst(HttpHeaders.LOCATION)).isEqualTo(fakeUrl);
    }


}