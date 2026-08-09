package com.sprint.mission.discodeit.storage;

import com.adobe.testing.s3mock.testcontainers.S3MockContainer;
import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;


@DisplayName("S3BinaryContentStorage 테스트")
class S3BinaryContentStorageTest {

    private static final String BUCKET = "test-bucket";

    private static S3MockContainer s3Mock;
    private static S3Client s3Client;
    private static S3BinaryContentStorage storage;

    @BeforeAll
    static void setUp() {
        // S3Mock 시작
        s3Mock = new S3MockContainer("latest");
        s3Mock.start();

        // 테스트에서 S3 상태를 직접 확인하기 위한 Client
        s3Client = S3Client.builder()
                .endpointOverride(URI.create(s3Mock.getHttpEndpoint()))
                .region(Region.AP_NORTHEAST_2)
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create("test", "test")
                        )
                )
                .forcePathStyle(true)
                .build();

        // 테스트용 버킷 생성
        s3Client.createBucket(builder ->
                builder.bucket(BUCKET)
        );

        // S3Mock endpoint를 사용하는 Storage 생성
        storage = new S3BinaryContentStorage(
                "test",
                "test",
                "ap-northeast-2",
                BUCKET,
                600L,
                URI.create(s3Mock.getHttpEndpoint())
        );
    }

    @AfterAll
    static void tearDown() {
        if (s3Client != null) {
            s3Client.close();
        }

        if (s3Mock != null) {
            s3Mock.stop();
        }
    }

    @Test
    @DisplayName("바이너리 데이터를 S3에 저장한다.")
    void put_success() {
        // given
        UUID binaryContentId = UUID.randomUUID();
        byte[] bytes = "Hello S3".getBytes(StandardCharsets.UTF_8);

        // when
        UUID result = storage.put(binaryContentId, bytes);

        // then
        assertThat(result).isEqualTo(binaryContentId);

        byte[] savedBytes = s3Client
                .getObjectAsBytes(builder -> builder
                        .bucket(BUCKET)
                        .key(binaryContentId.toString())
                )
                .asByteArray();

        assertThat(savedBytes).isEqualTo(bytes);
    }

    @Test
    @DisplayName("S3에 저장된 바이너리 데이터를 조회한다.")
    void get_success() throws Exception {
        // given
        UUID binaryContentId = UUID.randomUUID();
        byte[] bytes = "Hello Download".getBytes(StandardCharsets.UTF_8);

        s3Client.putObject(
                builder -> builder
                        .bucket(BUCKET)
                        .key(binaryContentId.toString()),
                RequestBody.fromBytes(bytes)
        );

        // when
        byte[] result;

        try (InputStream inputStream = storage.get(binaryContentId)) {
            result = inputStream.readAllBytes();
        }

        // then
        assertThat(result).isEqualTo(bytes);
    }

    @Test
    @DisplayName("다운로드 요청 시 Presigned URL로 리다이렉트한다.")
    void download_success() throws Exception {
        // given
        UUID binaryContentId = UUID.randomUUID();
        byte[] bytes = "Presigned URL Test".getBytes(StandardCharsets.UTF_8);

        s3Client.putObject(
                builder -> builder
                        .bucket(BUCKET)
                        .key(binaryContentId.toString()),
                RequestBody.fromBytes(bytes)
        );

        BinaryContentDto binaryContentDto = mock(BinaryContentDto.class);

        given(binaryContentDto.id())
                .willReturn(binaryContentId);

        given(binaryContentDto.contentType())
                .willReturn("text/plain");

        // when
        ResponseEntity<Resource> response =
                storage.download(binaryContentDto);

        // then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.FOUND);

        URI location = response.getHeaders().getLocation();

        assertThat(location).isNotNull();
        assertThat(location.toString())
                .contains("X-Amz-Signature");

        // Presigned URL로 실제 요청
        HttpResponse<byte[]> downloadResponse =
                HttpClient.newHttpClient().send(
                        HttpRequest.newBuilder(location)
                                .GET()
                                .build(),
                        HttpResponse.BodyHandlers.ofByteArray()
                );

        assertThat(downloadResponse.statusCode())
                .isEqualTo(200);

        assertThat(downloadResponse.body())
                .isEqualTo(bytes);
    }
}