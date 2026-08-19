package com.sprint.mission.discodeit.storage.s3;

import com.sprint.mission.discodeit.dto.response.BinaryContentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class S3BinaryContentStorageTest {

    private static final String BUCKET = "test-bucket";
    private static final Duration EXPIRATION = Duration.ofMinutes(10);

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    private S3BinaryContentStorage storage;

    @BeforeEach
    void setUp() {
        storage = new S3BinaryContentStorage(
                s3Client,
                s3Presigner,
                BUCKET,
                EXPIRATION
        );
    }

    @Test
    void put_바이너리_파일을_S3에_업로드한다() {
        UUID id = UUID.randomUUID();
        byte[] bytes = "test-content".getBytes();

        given(s3Client.putObject(
                any(PutObjectRequest.class),
                any(RequestBody.class)
        )).willReturn(PutObjectResponse.builder().build());

        UUID result = storage.put(id, bytes);

        ArgumentCaptor<PutObjectRequest> captor =
                ArgumentCaptor.forClass(PutObjectRequest.class);

        then(s3Client).should().putObject(
                captor.capture(),
                any(RequestBody.class)
        );

        assertThat(result).isEqualTo(id);
        assertThat(captor.getValue().bucket()).isEqualTo(BUCKET);
        assertThat(captor.getValue().key()).isEqualTo(id.toString());
    }

    @Test
    void get_S3에서_바이너리_파일을_조회한다() {
        UUID id = UUID.randomUUID();

        @SuppressWarnings("unchecked")
        ResponseInputStream<GetObjectResponse> response =
                mock(ResponseInputStream.class);

        given(s3Client.getObject(any(GetObjectRequest.class)))
                .willReturn(response);

        assertThat(storage.get(id)).isSameAs(response);

        ArgumentCaptor<GetObjectRequest> captor =
                ArgumentCaptor.forClass(GetObjectRequest.class);

        then(s3Client).should().getObject(captor.capture());

        assertThat(captor.getValue().bucket()).isEqualTo(BUCKET);
        assertThat(captor.getValue().key()).isEqualTo(id.toString());
    }

    @Test
    void download_PresignedUrl로_리다이렉트한다() throws Exception {
        UUID id = UUID.randomUUID();
        BinaryContentResponse metadata = new BinaryContentResponse(
                id,
                Instant.now(),
                "test.txt",
                12L,
                "text/plain",
                "test-content".getBytes()
        );

        URL url = URI.create(
                "https://example.com/presigned-url"
        ).toURL();

        PresignedGetObjectRequest presignedRequest =
                mock(PresignedGetObjectRequest.class);

        given(presignedRequest.url()).willReturn(url);
        given(s3Presigner.presignGetObject(
                any(GetObjectPresignRequest.class)
        )).willReturn(presignedRequest);

        ResponseEntity<?> response = storage.download(metadata);

        ArgumentCaptor<GetObjectPresignRequest> captor =
                ArgumentCaptor.forClass(GetObjectPresignRequest.class);

        then(s3Presigner).should()
                .presignGetObject(captor.capture());

        GetObjectPresignRequest request = captor.getValue();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(response.getHeaders().getLocation())
                .isEqualTo(url.toURI());
        assertThat(request.signatureDuration()).isEqualTo(EXPIRATION);
        assertThat(request.getObjectRequest().bucket())
                .isEqualTo(BUCKET);
        assertThat(request.getObjectRequest().key())
                .isEqualTo(id.toString());
        assertThat(request.getObjectRequest().responseContentType())
                .isEqualTo("text/plain");
    }
}