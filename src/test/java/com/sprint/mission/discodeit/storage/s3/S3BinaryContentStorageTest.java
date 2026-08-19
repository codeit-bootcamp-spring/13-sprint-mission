package com.sprint.mission.discodeit.storage.s3;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import java.io.InputStream;
import java.util.UUID;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "discodeit.storage.type=s3",
    "discodeit.storage.s3.region=ap-northeast-2",
    "discodeit.storage.s3.bucket=discodeit-binary-content-storage-jjs"
})
@Disabled("실제 AWS S3에 의존하는 테스트라 CI에서는 제외, 로컬에서 aws configure 설정 후 수동 실행")
class S3BinaryContentStorageTest {

  @Autowired
  private S3BinaryContentStorage s3BinaryContentStorage;

  @Test
  void put_get_성공() {
    UUID id = UUID.randomUUID();
    byte[] content = "s3 storage test".getBytes();

    UUID savedId = s3BinaryContentStorage.put(id, content);
    assertThat(savedId).isEqualTo(id);

    try (InputStream is = s3BinaryContentStorage.get(id)) {
      byte[] result = is.readAllBytes();
      assertThat(new String(result)).isEqualTo("s3 storage test");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void download_redirect_성공() {
    UUID id = UUID.randomUUID();
    byte[] content = "download redirect test".getBytes();
    s3BinaryContentStorage.put(id, content);

    BinaryContentDto dto = new BinaryContentDto(
        id, "test.txt", (long) content.length, "text/plain"
    );

    ResponseEntity<Void> response = s3BinaryContentStorage.download(dto);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
    assertThat(response.getHeaders().getLocation()).isNotNull();
  }
}