package com.sprint.mission.discodeit.storage.s3;

import static org.assertj.core.api.Assertions.*;

import com.adobe.testing.s3mock.testcontainers.S3MockContainer;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@DisplayName("S3 BinaryContentStorage 테스트")
public class S3BinaryContentStorageTest {

  private static final String BUCKET = "discodeit-binary-content-storage-test";

  private static S3MockContainer s3MockContainer;
  private static S3BinaryContentStorage s3BinaryContentStorage;
  private static S3Client s3Client;
  private static S3Presigner s3Presigner;

  @BeforeAll
  static void startMock() {
    s3MockContainer = new S3MockContainer("latest");
    s3MockContainer.start();

    S3Properties s3Properties = new S3Properties();
    s3Properties.setBucket(BUCKET);
    s3Properties.setRegion("ap-northeast-2");
    s3Properties.setEndpoint(s3MockContainer.getHttpEndpoint()); // 가짜 컨테이너 주소
    s3Properties.setPresignedUrlExpiration(600);

    S3Config config = new S3Config();
    s3Client = config.s3Client(s3Properties);
    s3Presigner = config.s3Presigner(s3Properties);

    s3BinaryContentStorage = new S3BinaryContentStorage(s3Client,
        s3Presigner, s3Properties);

    s3Client.createBucket(builder -> builder.bucket(BUCKET));

  }

  @AfterAll // 전체 테스트가 종료된 후 한 번만 동작
  static void stopMock() {
    if (s3Presigner != null) {
      s3Presigner.close();
    }
    if (s3Client != null) {
      s3Client.close();
    }
    if (s3MockContainer != null) {
      s3MockContainer.stop();
    }
  }


  @Test
  @DisplayName("S3 객체를 저장하고 바이트를 읽어 올 수 있다")
  void put하고_get할_수_있다() throws IOException {
    // given
    UUID id = UUID.randomUUID();
    byte[] expected = "Discodeit S3 테스트 바이트".getBytes(StandardCharsets.UTF_8);

    // when & then
    UUID savedId = s3BinaryContentStorage.put(id, expected);
    assertThat(savedId).isEqualTo(id);

    try (InputStream inputStream = s3BinaryContentStorage.get(id)) {
      assertThat(inputStream.readAllBytes()).isEqualTo(expected);
    }
  }


  @Test
  @DisplayName("다운로드 요청은 Presigned URL을 활용해 리다이렉트한다")
  void PresignedURL로_리다이렉트해_다운로드할_수_있다() throws IOException, InterruptedException {
    // given
    UUID id = UUID.randomUUID();
    byte[] expected = "download test".getBytes(StandardCharsets.UTF_8);
    s3BinaryContentStorage.put(id, expected);

    BinaryContentDto binaryContentDto = BinaryContentDto.builder()
        .id(id)
        .fileName("테스트 파일.txt")
        .contentType("text/plain")
        .size((long) expected.length)
        .build();

    // when
    ResponseEntity<Void> response = s3BinaryContentStorage.download(binaryContentDto);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
    assertThat(response.getHeaders().getLocation()).isNotNull();

    HttpResponse<byte[]> downloadResponse = HttpClient.newHttpClient().send(
        HttpRequest.newBuilder().uri(response.getHeaders().getLocation()).GET().build(),
        BodyHandlers.ofByteArray());
    assertThat(downloadResponse.statusCode()).isEqualTo(200); // S3 객체 GET 성공  
    assertThat(downloadResponse.body()).isEqualTo(expected);
  }
}
